package com.zinphraek.leprestigehall.domain.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

  @Query(
      "SELECT i FROM Invoice i WHERE (:invoiceNumber IS NULL OR i.invoiceNumber =:invoiceNumber) "
          + "AND (CAST(:beforeIssuedDate AS timestamp) IS NULL OR i.issuedDate <= CAST(:beforeIssuedDate AS timestamp)) "
          + "AND (CAST(:afterIssuedDate AS timestamp) IS NULL OR i.issuedDate >= CAST(:afterIssuedDate AS timestamp)) "
          + "AND (CAST(:beforeDueDate AS timestamp) IS NULL OR i.dueDate <= CAST(:beforeDueDate AS timestamp)) "
          + "AND (CAST(:afterDueDate AS timestamp) IS NULL OR i.dueDate >= CAST(:afterDueDate AS timestamp)) "
          + "AND (:status IS NULL OR LOWER(i.status) LIKE LOWER(CONCAT('%', :status, '%'))) "
          + "AND (:greaterThanTotalAmountPaid IS NULL OR i.totalAmountPaid >=:greaterThanTotalAmountPaid) "
          + "AND (:lessThanTotalAmountPaid IS NULL OR i.totalAmountPaid <=:lessThanTotalAmountPaid) "
          + "AND (:greaterThanAmountDue IS NULL OR i.amountDue >=:greaterThanAmountDue) "
          + "AND (:lessThanAmountDue IS NULL OR i.amountDue <=:lessThanAmountDue) "
          + "AND (:reservationId IS NULL OR i.reservation.id =:reservationId) "
          + "AND (:userId IS NULL OR i.user.userId =:userId)")
  Page<Invoice> findAllAndFilter(
      @Param("invoiceNumber") String invoiceNumber,
      @Param("beforeIssuedDate") LocalDateTime beforeIssuedDate,
      @Param("afterIssuedDate") LocalDateTime afterIssuedDate,
      @Param("beforeDueDate") LocalDateTime beforeDueDate,
      @Param("afterDueDate") LocalDateTime afterDueDate,
      @Param("status") String status,
      @Param("greaterThanTotalAmountPaid") Double greaterThanTotalAmountPaid,
      @Param("lessThanTotalAmountPaid") Double lessThanTotalAmountPaid,
      @Param("greaterThanAmountDue") Double greaterThanAmountDue,
      @Param("lessThanAmountDue") Double lessThanAmountDue,
      @Param("reservationId") Long reservationId,
      @Param("userId") String userId,
      Pageable pageable);

  @Query("SELECT i FROM Invoice i WHERE i.user.userId =:userId")
  Page<Invoice> findByUserId(@Param("userId") String userId, Pageable pageable);

  Optional<Invoice> findByReservationId(Long reservationId);

  boolean existsByInvoiceNumber(String invoiceNumber);

  boolean existsByReservationId(Long reservationId);
}
