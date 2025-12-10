package com.hotelio.booking.dto;

import lombok.Data;

@Data
public class BookingEvent {
    private String bookingId;
    private String userId;
    private String hotelId;
}
