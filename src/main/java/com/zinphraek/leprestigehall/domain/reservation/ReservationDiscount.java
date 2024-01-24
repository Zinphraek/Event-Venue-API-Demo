package com.zinphraek.leprestigehall.domain.reservation;

import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "reservation_discounts")
public class ReservationDiscount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  private Double percentage;
  private Double amount;
  private String name;
  private String code;
  @AllowedValues(
      allowedValues = {
          "Percentage",
          "Amount"
      },
      message = "Invalid discount type")
  @NotBlank(message = "Discount type is required")
  private String type;
  private Boolean isAvailable;
  private String description;

  public ReservationDiscount() {
  }

  public ReservationDiscount(
      Long id,
      Double percentage,
      Double amount,
      String name,
      String code,
      String type,
      Boolean isAvailable,
      String description) {
    this.id = id;
    this.percentage = percentage;
    this.amount = amount;
    this.name = name;
    this.code = code;
    this.type = type;
    this.isAvailable = isAvailable;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getPercentage() {
    return percentage;
  }

  public void setPercentage(Double percentage) {
    this.percentage = percentage;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Boolean getAvailable() {
    return isAvailable;
  }

  public void setAvailable(Boolean available) {
    isAvailable = available;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Double applyDiscount(Double price) {
    if (this.type.equals("Percentage")) {
      return price * (1 - this.percentage / 100);
    } else {
      return price - this.amount;
    }
  }
}
