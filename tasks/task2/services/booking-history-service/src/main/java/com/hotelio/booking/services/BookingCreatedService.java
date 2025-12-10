package com.hotelio.booking.services;

import com.hotelio.booking.dto.BookingEvent;
import com.hotelio.booking.entity.BookingHistoryEntity;
import com.hotelio.booking.repositories.BookingHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingCreatedService {
    private final BookingHistoryRepository bookingHistoryRepository;

    @Transactional
    public void process(BookingEvent bookingEvent) {
        bookingHistoryRepository.save(toEntity(bookingEvent));
    }

    public BookingHistoryEntity toEntity(BookingEvent event) {
        BookingHistoryEntity entity = new BookingHistoryEntity();
        entity.setBookingId(event.getBookingId());
        entity.setUserId(event.getUserId());
        entity.setHotelId(event.getHotelId());
        return entity;
    }
}
