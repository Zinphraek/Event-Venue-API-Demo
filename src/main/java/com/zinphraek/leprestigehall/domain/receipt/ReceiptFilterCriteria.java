package com.zinphraek.leprestigehall.domain.receipt;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReceiptFilterCriteria {

  private Long id;

  private String receiptNumber;

  private LocalDateTime beforeReceiptDate;

  private LocalDateTime afterReceiptDate;

  private Double greaterThanAmountPaid;

  private Double lessThanAmountPaid;

  private Long reservationId;

  private String userId;

  private Long invoiceId;

  private String paymentMethod;

  private LocalDateTime beforePaymentDate;

  private LocalDateTime afterPaymentDate;

  private Double greaterThanBalanceDue;

  private Double lessThanBalanceDue;

  private String paidBy;

  private String cashierName;

  public ReceiptFilterCriteria() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getReceiptNumber() {
    return receiptNumber;
  }

  public void setReceiptNumber(String receiptNumber) {
    this.receiptNumber = receiptNumber;
  }

  public LocalDateTime getBeforeReceiptDate() {
    return beforeReceiptDate;
  }

  public void setBeforeReceiptDate(String beforeReceiptDate) {
    this.beforeReceiptDate =
        LocalDateTime.parse(beforeReceiptDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getAfterReceiptDate() {
    return afterReceiptDate;
  }

  public void setAfterReceiptDate(String afterReceiptDate) {
    this.afterReceiptDate =
        LocalDateTime.parse(afterReceiptDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public Double getGreaterThanAmountPaid() {
    return greaterThanAmountPaid;
  }

  public void setGreaterThanAmountPaid(Double greaterThanAmountPaid) {
    this.greaterThanAmountPaid = greaterThanAmountPaid;
  }

  public Double getLessThanAmountPaid() {
    return lessThanAmountPaid;
  }

  public void setLessThanAmountPaid(Double lessThanAmountPaid) {
    this.lessThanAmountPaid = lessThanAmountPaid;
  }

  public Long getReservationId() {
    return reservationId;
  }

  public void setReservationId(Long reservationId) {
    this.reservationId = reservationId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Long getInvoiceId() {
    return invoiceId;
  }

  public void setInvoiceId(Long invoiceId) {
    this.invoiceId = invoiceId;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public LocalDateTime getBeforePaymentDate() {
    return beforePaymentDate;
  }

  public void setBeforePaymentDate(String beforePaymentDate) {
    this.beforePaymentDate =
        LocalDateTime.parse(beforePaymentDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getAfterPaymentDate() {
    return afterPaymentDate;
  }

  public void setAfterPaymentDate(String afterPaymentDate) {
    this.afterPaymentDate =
        LocalDateTime.parse(afterPaymentDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public Double getGreaterThanBalanceDue() {
    return greaterThanBalanceDue;
  }

  public void setGreaterThanBalanceDue(Double greaterThanBalanceDue) {
    this.greaterThanBalanceDue = greaterThanBalanceDue;
  }

  public Double getLessThanBalanceDue() {
    return lessThanBalanceDue;
  }

  public void setLessThanBalanceDue(Double lessThanBalanceDue) {
    this.lessThanBalanceDue = lessThanBalanceDue;
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public String getCashierName() {
    return cashierName;
  }

  public void setCashierName(String cashierName) {
    this.cashierName = cashierName;
  }
}
