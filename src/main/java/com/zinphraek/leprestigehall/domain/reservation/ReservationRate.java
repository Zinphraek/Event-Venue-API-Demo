package com.zinphraek.leprestigehall.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation_rates")
public class ReservationRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  private Double cleaningRate;

  private Double facilityRate;

  private Double overtimeRate;

  private Double seatRate;

  public ReservationRate() {}

  public ReservationRate(
      Long id, Double cleaningRate, Double facilityRate, Double overtimeRate, Double seatRate) {
    this.id = id;
    this.cleaningRate = cleaningRate;
    this.facilityRate = facilityRate;
    this.overtimeRate = overtimeRate;
    this.seatRate = seatRate;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getCleaningRate() {
    return cleaningRate;
  }

  public void setCleaningRate(Double cleaningRate) {
    this.cleaningRate = cleaningRate;
  }

  public Double getFacilityRate() {
    return facilityRate;
  }

  public void setFacilityRate(Double facilityRate) {
    this.facilityRate = facilityRate;
  }

  public Double getOvertimeRate() {
    return overtimeRate;
  }

  public void setOvertimeRate(Double overtimeRate) {
    this.overtimeRate = overtimeRate;
  }

  public Double getSeatRate() {
    return seatRate;
  }

  public void setSeatRate(Double seatRate) {
    this.seatRate = seatRate;
  }
}
