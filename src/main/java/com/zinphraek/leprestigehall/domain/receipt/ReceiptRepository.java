package com.zinphraek.leprestigehall.domain.receipt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

  Page<Receipt> findByUserId(String userId, Pageable pageable);

  @Query(
      "SELECT r FROM Receipt r WHERE (:receiptNumber IS NULL OR r.receiptNumber LIKE CONCAT('%', :receiptNumber, '%')) "
          + "AND (CAST(:beforeReceiptDate AS timestamp) IS NULL OR r.receiptDate <= CAST(:beforeReceiptDate AS timestamp)) "
          + "AND (CAST(:afterReceiptDate AS timestamp) IS NULL OR r.receiptDate >= CAST(:afterReceiptDate AS timestamp)) "
          + "AND (:greaterThanAmountPaid IS NULL OR r.amountPaid >=:greaterThanAmountPaid) "
          + "AND (:lessThanAmountPaid IS NULL OR r.amountPaid <=:lessThanAmountPaid) "
          + "AND (:reservationId IS NULL OR r.reservationId =:reservationId) AND (:userId IS NULL OR r.userId =:userId) "
          + "AND (:invoiceId IS NULL OR r.invoiceId =:invoiceId) "
          + "AND (CAST(:beforePaymentDate AS timestamp) IS NULL OR r.paymentDate <= CAST(:beforePaymentDate AS timestamp)) "
          + "AND (CAST(:afterPaymentDate AS timestamp) IS NULL OR r.paymentDate >= CAST(:afterPaymentDate AS timestamp)) "
          + "AND (:lessThanBalanceDue IS NULL OR r.balanceDue <=:lessThanBalanceDue) "
          + "AND (:greaterThanBalanceDue IS NULL OR r.balanceDue >=:greaterThanBalanceDue) "
          + "AND (:paidBy IS NULL OR LOWER(r.paidBy) LIKE CONCAT('%', :paidBy, '%')) "
          + "AND (:cashierName IS NULL OR LOWER(r.cashierName) LIKE CONCAT('%', :cashierName, '%')) ")
  Page<Receipt> findAllAndFilter(
      @Param("receiptNumber") String receiptNumber,
      @Param("beforeReceiptDate") LocalDateTime beforeReceiptDate,
      @Param("afterReceiptDate") LocalDateTime afterReceiptDate,
      @Param("greaterThanAmountPaid") Double greaterThanAmountPaid,
      @Param("lessThanAmountPaid") Double lessThanAmountPaid,
      @Param("reservationId") Long reservationId,
      @Param("userId") String userId,
      @Param("invoiceId") Long invoiceId,
      @Param("beforePaymentDate") LocalDateTime beforePaymentDate,
      @Param("afterPaymentDate") LocalDateTime afterPaymentDate,
      @Param("lessThanBalanceDue") Double lessThanBalanceDue,
      @Param("greaterThanBalanceDue") Double greaterThanBalanceDue,
      @Param("paidBy") String paidBy,
      @Param("cashierName") String cashierName,
      Pageable pageable);

  @Query(
      "SELECT r FROM Receipt r WHERE (:id IS NULL OR r.id =:id) AND (:receiptNumber IS NULL OR r.receiptNumber LIKE CONCAT('%', :receiptNumber, '%')) "
          + "AND (CAST(:beforeReceiptDate AS timestamp) IS NULL OR r.receiptDate <= CAST(:beforeReceiptDate AS timestamp)) "
          + "AND (CAST(:afterReceiptDate AS timestamp) IS NULL OR r.receiptDate >= CAST(:afterReceiptDate AS timestamp)) "
          + "AND (:greaterThanAmountPaid IS NULL OR r.amountPaid >=:greaterThanAmountPaid) "
          + "AND (:lessThanAmountPaid IS NULL OR r.amountPaid <=:lessThanAmountPaid) "
          + "AND (:reservationId IS NULL OR r.reservationId =:reservationId) AND (:userId IS NULL OR r.userId =:userId) "
          + "AND (:invoiceId IS NULL OR r.invoiceId =:invoiceId) "
          + "AND (CAST(:beforePaymentDate AS timestamp) IS NULL OR r.paymentDate <= CAST(:beforePaymentDate AS timestamp)) "
          + "AND (CAST(:afterPaymentDate AS timestamp) IS NULL OR r.paymentDate >= CAST(:afterPaymentDate AS timestamp)) "
          + "AND (:lessThanBalanceDue IS NULL OR r.balanceDue <=:lessThanBalanceDue) "
          + "AND (:greaterThanBalanceDue IS NULL OR r.balanceDue >=:greaterThanBalanceDue) "
          + "AND (:paidBy IS NULL OR LOWER(r.paidBy) LIKE CONCAT('%', :paidBy, '%')) "
          + "AND (:cashierName IS NULL OR LOWER(r.cashierName) LIKE CONCAT('%', :cashierName, '%')) ")
  Page<Receipt> findByUserIdAndFilter(
      @Param("id") Long id,
      @Param("receiptNumber") String receiptNumber,
      @Param("beforeReceiptDate") LocalDateTime beforeReceiptDate,
      @Param("afterReceiptDate") LocalDateTime afterReceiptDate,
      @Param("greaterThanAmountPaid") Double greaterThanAmountPaid,
      @Param("lessThanAmountPaid") Double lessThanAmountPaid,
      @Param("reservationId") Long reservationId,
      @Param("userId") String userId,
      @Param("invoiceId") Long invoiceId,
      @Param("beforePaymentDate") LocalDateTime beforePaymentDate,
      @Param("afterPaymentDate") LocalDateTime afterPaymentDate,
      @Param("lessThanBalanceDue") Double lessThanBalanceDue,
      @Param("greaterThanBalanceDue") Double greaterThanBalanceDue,
      @Param("paidBy") String paidBy,
      @Param("cashierName") String cashierName,
      Pageable pageable);

  Long countByInvoiceId(Long invoiceId);

  boolean existsByReceiptNumber(String receiptNumber);
}
