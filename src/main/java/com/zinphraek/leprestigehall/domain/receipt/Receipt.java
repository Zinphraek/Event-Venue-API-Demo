package com.zinphraek.leprestigehall.domain.receipt;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "receipts")
public class Receipt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Receipt number is required")
  private String receiptNumber;

  @NotNull(message = "Receipt date is required")
  private LocalDateTime receiptDate;

  @NotNull(message = "Amount paid is required")
  private Double amountPaid;

  @NotNull(message = "Reservation is required")
  private Long reservationId;

  @NotNull(message = "User is required")
  private String userId;

  @NotNull(message = "Invoice is required")
  private Long invoiceId;

  @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  @NotNull(message = "Payment method is required")
  private Collection<Payment> payments = new ArrayList<>();

  private Double totalAmountPaidTillToday = 0.0;

  @NotNull(message = "Paid By is required")
  private String paidBy;

  @NotNull(message = "Payment date is required")
  private LocalDateTime paymentDate;

  private Double prevBalance;

  @NotNull(message = "Balance due is required")
  private Double balanceDue;

  @NotNull(message = "Cashier name is required")
  private String cashierName;

  public Receipt() {}

  public Receipt(
      Long id,
      String receiptNumber,
      String receiptDate,
      Double amountPaid,
      Long reservationId,
      String userId,
      Long invoiceId,
      Collection<Payment> payments,
      String paidBy,
      String paymentDate,
      Double prevBalance,
      Double balanceDue,
      String cashierName) {
    this.id = id;
    this.receiptNumber = receiptNumber;
    this.receiptDate =
        LocalDateTime.parse(receiptDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.amountPaid = amountPaid;
    this.reservationId = reservationId;
    this.payments = payments;
    this.userId = userId;
    this.invoiceId = invoiceId;
    this.paidBy = paidBy;
    this.paymentDate =
        LocalDateTime.parse(paymentDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.prevBalance = prevBalance;
    this.balanceDue = balanceDue;
    this.cashierName = cashierName;
  }

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

  public LocalDateTime getReceiptDate() {
    return receiptDate;
  }

  public void setReceiptDate(String receiptDate) {
    this.receiptDate =
        LocalDateTime.parse(receiptDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public Double getAmountPaid() {
    return amountPaid;
  }

  public void setAmountPaid(Double amountPaid) {
    this.amountPaid = amountPaid;
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

  public Collection<Payment> getPayments() {
    return payments;
  }

  public void setPayments(Collection<Payment> payments) {
    this.payments = payments;
  }

  public void addPayment(Payment payment) {
    this.payments.add(payment);
  }

  public Double getThisPaymentSessionSum() {
    return this.payments.stream().mapToDouble(Payment::getAmount).sum();
  }

  public void removePayment(Payment payment) {
    this.payments.remove(payment);
  }

  public Double getTotalAmountPaidTillToday() {
    return totalAmountPaidTillToday;
  }

  public void addThisPaymentSessionToTotalAmountPaid(Double amountPaid) {
    this.totalAmountPaidTillToday += amountPaid;
  }

  public void setTotalAmountPaidTillToday(Double totalAmountPaidTillToday) {
    this.totalAmountPaidTillToday = totalAmountPaidTillToday;
  }

  public String getPaidBy() {
    return paidBy;
  }

  public void setPaidBy(String paidBy) {
    this.paidBy = paidBy;
  }

  public LocalDateTime getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(String paymentDate) {
    this.paymentDate =
        LocalDateTime.parse(paymentDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public Double getPrevBalance() {
    return prevBalance;
  }

  public void setPrevBalance(Double prevBalance) {
    this.prevBalance = prevBalance;
  }

  public Double getBalanceDue() {
    return balanceDue;
  }

  public void setBalanceDue(Double balanceDue) {
    this.balanceDue = balanceDue;
  }

  public String getCashierName() {
    return cashierName;
  }

  public void setCashierName(String cashierName) {
    this.cashierName = cashierName;
  }
}
