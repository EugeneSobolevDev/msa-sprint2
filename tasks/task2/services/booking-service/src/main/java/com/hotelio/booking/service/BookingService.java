package com.hotelio.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.booking.entity.Booking;
import com.hotelio.booking.entity.OutboxEvent;
import com.hotelio.booking.entity.PromoCode;
import com.hotelio.booking.enums.OutboxEventStatus;
import com.hotelio.booking.events.BookingCreatedEvent;
import com.hotelio.booking.repositories.BookingRepository;
import com.hotelio.booking.repositories.OutboxEventRepository;
import com.hotelio.proto.booking.BookingListRequest;
import com.hotelio.proto.booking.BookingListResponse;
import com.hotelio.proto.booking.BookingRequest;
import com.hotelio.proto.booking.BookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final PromoCodeService promoCodeService;
    private final ReviewService reviewService;
    private final AppUserService userService;
    private final HotelService hotelService;
    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.topics.booking-created}")
    private String bookingCreatedTopic;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) throws JsonProcessingException {
        validateUser(request.getUserId());
        validateHotel(request.getHotelId());

        double basePrice = resolveBasePrice(request.getUserId());
        double discount = resolvePromoDiscount(request.getPromoCode(), request.getUserId());

        double finalPrice = basePrice - discount;
        log.info("Final price calculated: base={}, discount={}, final={}", basePrice, discount, finalPrice);


        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setHotelId(request.getHotelId());
        booking.setPromoCode(request.getPromoCode());
        booking.setDiscountPercent(discount);
        booking.setPrice(100.0);
        booking = bookingRepository.save(booking);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType("BOOKING")
                .aggregateId(booking.getId().toString())
                .eventType("BOOKING_CREATED")
                .payload(objectMapper.writeValueAsString(
                        Map.of(
                                "bookingId", booking.getId(),
                                "userId", booking.getUserId(),
                                "hotelId", booking.getHotelId())
                ))
                .status(OutboxEventStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();
        outboxRepository.save(outboxEvent);

        eventPublisher.publishEvent(new BookingCreatedEvent(booking));
        return entityToProto(booking);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        outboxRepository.findOldestPending().ifPresent(outboxEvent -> {
            try {
                kafkaTemplate.send(bookingCreatedTopic, outboxEvent.getPayload());
                outboxRepository.markAsSent(outboxEvent.getId(), LocalDateTime.now());
            } catch (Exception e) {
                outboxRepository.markAsFailed(outboxEvent.getId(), e.getMessage());
            }
        });
    }

    public BookingListResponse listBookings(BookingListRequest request) {
        List<Booking> bookings = request.getUserId().isBlank()
                ? bookingRepository.findAll()
                : bookingRepository.findByUserIdOrderByCreatedAtDesc(request.getUserId());
        log.info("Found {} bookings for user {}", bookings.size(), request.getUserId());
        return BookingListResponse.newBuilder().addAllBookings(bookings
                        .stream()
                        .map(this::entityToProto)
                        .toList())
                .build();
    }

    private BookingResponse entityToProto(Booking entity) {
        return BookingResponse.newBuilder()
                .setId(String.valueOf(entity.getId()))
                .setUserId(entity.getUserId())
                .setHotelId(entity.getHotelId())
                .setPromoCode(entity.getPromoCode() == null ? "" : entity.getPromoCode())
                .setDiscountPercent(entity.getDiscountPercent())
                .setPrice(entity.getPrice())
                .setCreatedAt(entity.getCreatedAt().toString())
                .build();
    }

    private void validateUser(String userId) {
        if (!userService.isUserActive(userId)) {
            log.warn("User {} is inactive", userId);
            throw new IllegalArgumentException("User is inactive");
        }
        if (userService.isUserBlacklisted(userId)) {
            log.warn("User {} is blacklisted", userId);
            throw new IllegalArgumentException("User is blacklisted");
        }
    }

    private void validateHotel(String hotelId) {
        if (!hotelService.isHotelOperational(hotelId)) {
            log.warn("Hotel {} is not operational", hotelId);
            throw new IllegalArgumentException("Hotel is not operational");
        }
        if (!reviewService.isTrustedHotel(hotelId)) {
            log.warn("Hotel {} is not trusted", hotelId);
            throw new IllegalArgumentException("Hotel is not trusted based on reviews");
        }
        if (hotelService.isHotelFullyBooked(hotelId)) {
            log.warn("Hotel {} is fully booked", hotelId);
            throw new IllegalArgumentException("Hotel is fully booked");
        }
    }

    private double resolveBasePrice(String userId) {
        Optional<String> statusOpt = userService.getUserStatus(userId);
        return statusOpt.map(status -> {
            boolean isVip = status.equalsIgnoreCase("VIP");
            log.debug("User {} has status '{}', base price is {}", userId, status, isVip ? 80.0 : 100.0);
            return isVip ? 80.0 : 100.0;
        }).orElseGet(() -> {
            log.debug("User {} has unknown status, default base price 100.0", userId);
            return 100.0;
        });
    }

    private double resolvePromoDiscount(String promoCode, String userId) {
        if (promoCode == null) return 0.0;

        PromoCode promo = promoCodeService.validate(promoCode, userId);
        if (promo == null) {
            log.info("Promo code '{}' is invalid or not applicable for user {}", promoCode, userId);
            return 0.0;
        }

        log.debug("Promo code '{}' applied with discount {}", promoCode, promo.getDiscount());
        return promo.getDiscount();
    }
}
