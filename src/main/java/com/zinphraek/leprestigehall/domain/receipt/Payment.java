package com.zinphraek.leprestigehall.domain.receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JsonBackReference
  private Receipt receipt;

  @AllowedValues(
      allowedValues = {"Card", "Cash", "CashApp", "Check", "zelle", "Zelle", "Other"},
      message = "Invalid payment method")
  private String method;

  private Double amount;

  private String cardLastFour;

  private String otherDetails;

  public Payment() {}

  public Payment(
      Receipt receipt, String method, Double amount, String cardLastFour, String otherDetails) {
    this.receipt = receipt;
    this.method = method;
    this.amount = amount;
    this.cardLastFour = cardLastFour;
    this.otherDetails = otherDetails;
  }

  public Receipt getReceipt() {
    return receipt;
  }

  public void setReceipt(Receipt receipt) {
    this.receipt = receipt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public String getCardLastFour() {
    return cardLastFour;
  }

  public void setCardLastFour(String cardLastFour) {
    this.cardLastFour = cardLastFour;
  }

  public String getOtherDetails() {
    return otherDetails;
  }

  public void setOtherDetails(String otherDetails) {
    this.otherDetails = otherDetails;
  }
}
