package com.hotelio.booking.repositories;

import com.hotelio.booking.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
}
