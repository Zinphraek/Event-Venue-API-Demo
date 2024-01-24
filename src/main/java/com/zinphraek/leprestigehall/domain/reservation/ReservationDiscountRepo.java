package com.zinphraek.leprestigehall.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationDiscountRepo extends JpaRepository<ReservationDiscount, Long> {

  boolean existsByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
      Double percentage,
      Double amount,
      String name,
      String code,
      String type,
      Boolean isAvailable,
      String description);

  Optional<ReservationDiscount> findByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
      Double percentage,
      Double amount,
      String name,
      String code,
      String type,
      Boolean isAvailable,
      String description);
}
