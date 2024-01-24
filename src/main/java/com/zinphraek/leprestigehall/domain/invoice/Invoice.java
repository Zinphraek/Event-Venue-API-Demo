package com.zinphraek.leprestigehall.domain.invoice;

import com.zinphraek.leprestigehall.domain.reservation.Reservation;
import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;

@Entity
@Table(name = "invoices")
public class Invoice {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Invoice number is required")
  private String invoiceNumber;

  @NotNull(message = "Issued date is required")
  private LocalDateTime issuedDate;

  @NotNull(message = "Due date is required")
  private LocalDateTime dueDate;

  private Double totalAmountPaid = 0.0;

  private Double amountDue;

  @NotBlank
  @AllowedValues(
      allowedValues = {
          STATUS_PAID,
          STATUS_DUE,
          STATUS_PARTIALLY_PAID,
          STATUS_OVERDUE,
          STATUS_DUE_IMMEDIATELY,
          STATUS_WITHDRAWN,
      },
      message = "Invalid status")
  private String status;

  @NotNull(message = "Reservation is required")
  @OneToOne
  @JoinColumn(name = "reservation_id")
  private Reservation reservation;

  @NotNull(message = "User is required")
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  public Invoice() {
  }

  public Invoice(
      String invoiceNumber,
      String issuedDate,
      String dueDate,
      Double totalAmountPaid,
      Double amountDue,
      String status,
      Reservation reservation,
      User user) {
    this.invoiceNumber = invoiceNumber;
    this.issuedDate =
        LocalDateTime.parse(issuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.totalAmountPaid = totalAmountPaid;
    this.amountDue = amountDue;
    this.dueDate = LocalDateTime.parse(dueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.status = status;
    this.reservation = reservation;
    this.user = user;
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

  public LocalDateTime getIssuedDate() {
    return issuedDate;
  }

  public void setIssuedDate(CharSequence issuedDate) {
    this.issuedDate =
        LocalDateTime.parse(issuedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getDueDate() {
    return dueDate;
  }

  public void setDueDate(CharSequence dueDate) {
    this.dueDate = LocalDateTime.parse(dueDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public Double getTotalAmountPaid() {
    return totalAmountPaid;
  }

  public void setTotalAmountPaid(Double totalAmountPaid) {
    this.totalAmountPaid = totalAmountPaid;
  }

  public void addPaymentSessionToTotalAmountPaid(Double amountPaid) {
    this.totalAmountPaid += amountPaid;
  }

  public Double getAmountDue() {
    return amountDue;
  }

  public void setAmountDue(Double amountDue) {
    this.amountDue = amountDue;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Reservation getReservation() {
    return reservation;
  }

  public void setReservation(Reservation reservation) {
    this.reservation = reservation;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}
