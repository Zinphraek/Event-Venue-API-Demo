package com.zinphraek.leprestigehall.domain.invoice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

public class InvoiceFilterCriteria {

  private Long id;

  private String invoiceNumber;

  private LocalDateTime beforeIssuedDate;

  private LocalDateTime afterIssuedDate;

  private LocalDateTime beforeDueDate;

  private LocalDateTime afterDueDate;

  private Double greaterThanTotalAmountPaid;

  private Double lessThanTotalAmountPaid;

  private Double greaterThanAmountDue;

  private Double lessThanAmountDue;

  private String status;

  private Long reservationId;

  private String userId;

  public InvoiceFilterCriteria() {
  }

  public InvoiceFilterCriteria(
      String invoiceNumber,
      String beforeIssuedDate,
      String afterIssuedDate,
      String beforeDueDate,
      String afterDueDate,
      Double greaterThanTotalAmountPaid,
      Double lessThanTotalAmountPaid,
      Double greaterThanAmountDue,
      Double lessThanAmountDue,
      String status,
      Long reservationId,
      String userId) {

    this.invoiceNumber = invoiceNumber;
    this.beforeIssuedDate =
        LocalDateTime.parse(beforeIssuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.afterIssuedDate =
        LocalDateTime.parse(afterIssuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.beforeDueDate =
        LocalDateTime.parse(beforeDueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.afterDueDate =
        LocalDateTime.parse(afterDueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.greaterThanTotalAmountPaid = greaterThanTotalAmountPaid;
    this.lessThanTotalAmountPaid = lessThanTotalAmountPaid;
    this.greaterThanAmountDue = greaterThanAmountDue;
    this.lessThanAmountDue = lessThanAmountDue;
    this.status = status;
    this.reservationId = reservationId;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getInvoiceNumber() {
    return invoiceNumber;
  }

  public void setInvoiceNumber(String invoiceNumber) {
    this.invoiceNumber = invoiceNumber;
  }

  public LocalDateTime getBeforeIssuedDate() {
    return beforeIssuedDate;
  }

  public void setBeforeIssuedDate(CharSequence beforeIssuedDate) {
    this.beforeIssuedDate =
        LocalDateTime.parse(beforeIssuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getAfterIssuedDate() {
    return afterIssuedDate;
  }

  public void setAfterIssuedDate(CharSequence afterIssuedDate) {
    this.afterIssuedDate =
        LocalDateTime.parse(afterIssuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getBeforeDueDate() {
    return beforeDueDate;
  }

  public void setBeforeDueDate(CharSequence beforeDueDate) {
    this.beforeDueDate =
        LocalDateTime.parse(beforeDueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getAfterDueDate() {
    return afterDueDate;
  }

  public void setAfterDueDate(CharSequence afterDueDate) {
    this.afterDueDate =
        LocalDateTime.parse(afterDueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Double getGreaterThanTotalAmountPaid() {
    return greaterThanTotalAmountPaid;
  }

  public void setGreaterThanTotalAmountPaid(Double greaterThanTotalAmountPaid) {
    this.greaterThanTotalAmountPaid = greaterThanTotalAmountPaid;
  }

  public Double getLessThanTotalAmountPaid() {
    return lessThanTotalAmountPaid;
  }

  public void setLessThanTotalAmountPaid(Double lessThanTotalAmountPaid) {
    this.lessThanTotalAmountPaid = lessThanTotalAmountPaid;
  }

  public Double getGreaterThanAmountDue() {
    return greaterThanAmountDue;
  }

  public void setGreaterThanAmountDue(Double greaterThanAmountDue) {
    this.greaterThanAmountDue = greaterThanAmountDue;
  }

  public Double getLessThanAmountDue() {
    return lessThanAmountDue;
  }

  public void setLessThanAmountDue(Double lessThanAmountDue) {
    this.lessThanAmountDue = lessThanAmountDue;
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
}
