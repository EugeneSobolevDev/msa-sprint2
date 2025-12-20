package com.hotelio.booking.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelio.booking.dto.BookingEvent;
import com.hotelio.booking.services.BookingCreatedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCreatedListener {
    private final BookingCreatedService bookingCreatedService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.booking-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> consumerRecord) {
        try {
            log.info("Message received: {}", consumerRecord.value());
            //TODO Retry or DLQ logic
            BookingEvent bookingEvent = objectMapper.readValue(consumerRecord.value(), BookingEvent.class);
            if (!isValid(bookingEvent)) {
                log.error("Invalid booking event: {}", bookingEvent);
            }
            bookingCreatedService.process(bookingEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize message: {}", consumerRecord.value(), e);
        } catch (Exception e) {
            log.error("Failed to process message, topic: {}, partition: {}, offset: {}",
                    consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), e);
        }
    }

    private boolean isValid(BookingEvent event) {
        return event.getBookingId() != null &&
                event.getUserId() != null &&
                event.getHotelId() != null;
    }
}