package com.hotelio.booking.events;

import com.hotelio.booking.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookingCreatedEvent {
    private final Booking booking;
    private final LocalDateTime createdAt = LocalDateTime.now();
}
