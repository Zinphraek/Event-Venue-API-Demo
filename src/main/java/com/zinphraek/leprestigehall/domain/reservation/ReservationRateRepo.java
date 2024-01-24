package com.zinphraek.leprestigehall.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRateRepo extends JpaRepository<ReservationRate, Long> {
  boolean existsBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
      Double seatRate, Double cleaningRate, Double facilityRate, Double overtimeRate);

  Optional<ReservationRate> findBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
      Double seatRate, Double cleaningRate, Double facilityRate, Double overtimeRate);
}
