package com.zinphraek.leprestigehall.domain.reservation;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;

@Entity
@Table(name = "reservations")
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotNull(message = "Starting date and time is required")
  private LocalDateTime startingDateTime;

  @NotNull(message = "Ending date and time is required")
  private LocalDateTime endingDateTime;

  private LocalDateTime effectiveEndingDateTime;

  @NotNull(message = "Number of seats is required")
  @Max(value = 200, message = "Maximum number of seats is 200")
  private int numberOfSeats;

  @NotBlank(message = "Event type is required")
  @AllowedValues(
      allowedValues = {
          "Anniversaries",
          "Baby Showers",
          "Birthday Parties",
          "Bridal Showers",
          "Conferences",
          "Graduations",
          "Holiday Parties",
          "Meetings",
          "Photo Booth Rental",
          "Seminars",
          "Weddings",
          "Work Events",
          "Traditional Events",
          "Other Events"
      },
      message = "Invalid event type"
  )
  private String eventType;

  @NotNull(message = "Full package is required")
  private boolean isFullPackage;

  @OneToMany(mappedBy = "reservation")
  @JsonManagedReference
  private Collection<RequestedAddOn> addOns;

  private Double addOnsTotalCost;

  @NotBlank(message = "Status is required")
  @AllowedValues(
      allowedValues = {
          STATUS_BOOKED,
          STATUS_CANCELLED,
          STATUS_CONFIRMED,
          STATUS_DONE,
          STATUS_PENDING,
          STATUS_REQUESTED,
          STATUS_SETTLED,
          STATUS_COMPLETED,
          STATUS_IN_PROGRESS
      },
      message = "Invalid status")
  private String status;

  private boolean isSecurityDepositRefunded;

  @NotNull(message = "Tax rate is required")
  private Double taxRate;

  @NotNull(message = "Total price is required")
  private Double totalPrice;

  @ManyToOne
  @JoinColumn(name = "rates_id")
  private ReservationRate rates;

  @ManyToOne
  @JoinColumn(name = "discount_id")
  private ReservationDiscount discount;

  @AllowedValues(
      allowedValues = {
          COMPUTATION_METHOD_AUTO_FLAG,
          COMPUTATION_METHOD_MANUAL_FLAG,
      },
      message = "Invalid price computation method")
  private String priceComputationMethod;

  @NotBlank(message = "User is required")
  private String userId;

  public Reservation() {
  }

  public Reservation(String startingDateTime, String endingDateTime,
                     int numberOfSeats, String eventType, boolean isFullPackage,
                     Collection<RequestedAddOn> addOns, Double addOnsTotalCost, String status,
                     boolean isSecurityDepositRefunded, Double taxRate, Double totalPrice,
                     String userId) {

    this.startingDateTime = LocalDateTime.parse(startingDateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.endingDateTime = LocalDateTime.parse(endingDateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.numberOfSeats = numberOfSeats;
    this.eventType = eventType;
    this.isFullPackage = isFullPackage;
    this.addOns = addOns;
    this.addOnsTotalCost = addOnsTotalCost;
    this.status = status;
    this.isSecurityDepositRefunded = isSecurityDepositRefunded;
    this.taxRate = taxRate;
    this.totalPrice = totalPrice;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDateTime getStartingDateTime() {
    return startingDateTime;
  }

  public void setStartingDateTime(CharSequence startingDateTime) {
    this.startingDateTime = LocalDateTime.parse(startingDateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getEndingDateTime() {
    return endingDateTime;
  }

  public void setEndingDateTime(CharSequence endingDateTime) {
    this.endingDateTime = LocalDateTime.parse(endingDateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getEffectiveEndingDateTime() {
    return effectiveEndingDateTime;
  }

  public void setEffectiveEndingDateTime(CharSequence effectiveEndingDateTime) {
    this.effectiveEndingDateTime = LocalDateTime.parse(effectiveEndingDateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public int getNumberOfSeats() {
    return numberOfSeats;
  }

  public void setNumberOfSeats(int numberOfSeats) {
    this.numberOfSeats = numberOfSeats;
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

  public Collection<RequestedAddOn> getAddOns() {
    return addOns;
  }

  public void setAddOns(Collection<RequestedAddOn> addOns) {
    this.addOns = addOns;
  }

  public void addAddon(RequestedAddOn addOn) {
    this.addOns.add(addOn);
  }

  public Double getAddOnsTotalCost() {
    return addOnsTotalCost;
  }

  public void setAddOnsTotalCost(Double addOnsTotalCost) {
    this.addOnsTotalCost = addOnsTotalCost;
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

  public double getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(Double totalPrice) {
    this.totalPrice = totalPrice;
  }

  public ReservationRate getRates() {
    return rates;
  }

  public void setRates(ReservationRate rates) {
    this.rates = rates;
  }

  public ReservationDiscount getDiscount() {
    return discount;
  }

  public void setDiscount(ReservationDiscount discount) {
    this.discount = discount;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Reservation that)) {
      return false;
    }
    return numberOfSeats == that.numberOfSeats && isFullPackage == that.isFullPackage
        && status.equals(that.status)
        && isSecurityDepositRefunded == that.isSecurityDepositRefunded
        && Double.compare(that.taxRate, taxRate) == 0
        && Double.compare(that.totalPrice, totalPrice) == 0
        && Objects.equals(id, that.id) && startingDateTime.equals(that.startingDateTime)
        && endingDateTime.equals(that.endingDateTime)
        && Objects.equals(effectiveEndingDateTime, that.effectiveEndingDateTime)
        && eventType.equals(that.eventType) && Objects.equals(addOns, that.addOns) && userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, startingDateTime, endingDateTime, effectiveEndingDateTime,
        numberOfSeats, eventType, isFullPackage, addOns, status,
        isSecurityDepositRefunded, taxRate, totalPrice, userId);
  }
}
