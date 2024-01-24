package com.zinphraek.leprestigehall.domain.reservation;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationFilterCriteria {

  private Long id;

  private LocalDateTime startedBefore;

  private LocalDateTime startedAfter;

  private String eventType;

  private boolean isFullPackage;

  private String status;

  private boolean isSecurityDepositRefunded;

  private Double taxRate;

  private Double minTotalPrice;

  private Double maxTotalPrice;

  private String priceComputationMethod;

  private String userId;

  public ReservationFilterCriteria() {
  }

  public ReservationFilterCriteria(String startedBefore, String startedAfter,
      String eventType, boolean isFullPackage,
      String status, boolean isSecurityDepositRefunded,
      Double taxRate, Double minTotalPrice,
      Double maxTotalPrice, String priceComputationMethod,
      String userId) {

    this.startedBefore = LocalDateTime.parse(startedBefore,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.startedAfter = LocalDateTime.parse(startedAfter,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.eventType = eventType;
    this.isFullPackage = isFullPackage;
    this.status = status;
    this.isSecurityDepositRefunded = isSecurityDepositRefunded;
    this.taxRate = taxRate;
    this.minTotalPrice = minTotalPrice;
    this.maxTotalPrice = maxTotalPrice;
    this.priceComputationMethod = priceComputationMethod;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getStartedBefore() {
    return startedBefore;
  }

  public void setStartedBefore(CharSequence startedBefore) {
    this.startedBefore = LocalDateTime.parse(startedBefore,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getStartedAfter() {
    return startedAfter;
  }

  public void setStartedAfter(CharSequence startedAfter) {
    this.startedAfter = LocalDateTime.parse(startedAfter,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public boolean isFullPackage() {
    return isFullPackage;
  }

  public void setFullPackage(boolean fullPackage) {
    isFullPackage = fullPackage;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }


  public boolean isSecurityDepositRefunded() {
    return isSecurityDepositRefunded;
  }

  public void setSecurityDepositRefunded(boolean securityDepositRefunded) {
    isSecurityDepositRefunded = securityDepositRefunded;
  }

  public Double getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(Double taxRate) {
    this.taxRate = taxRate;
  }

  public Double getMinTotalPrice() {
    return minTotalPrice;
  }

  public void setMinTotalPrice(Double minTotalPrice) {
    this.minTotalPrice = minTotalPrice;
  }

  public Double getMaxTotalPrice() {
    return maxTotalPrice;
  }

  public void setMaxTotalPrice(Double maxTotalPrice) {
    this.maxTotalPrice = maxTotalPrice;
  }

  public String getPriceComputationMethod() {
    return priceComputationMethod;
  }

  public void setPriceComputationMethod(String priceComputationMethod) {
    this.priceComputationMethod = priceComputationMethod;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
