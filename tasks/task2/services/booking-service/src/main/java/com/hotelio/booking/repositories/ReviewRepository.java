package com.hotelio.booking.repositories;

import com.hotelio.booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByHotelId(String hotelId);
    int countByHotelId(String hotelId);
}
