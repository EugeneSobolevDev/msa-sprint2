package com.hotelio.booking.repositories;

import com.hotelio.booking.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, String> {
    List<Hotel> findByCity(String city);

    List<Hotel> findByCityOrderByRatingDesc(String city);

}
