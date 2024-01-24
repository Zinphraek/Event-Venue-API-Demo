package com.zinphraek.leprestigehall.domain.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  Page<Reservation> findByUserId(String userId, Pageable pageable);

  @Query(
      "SELECT r FROM Reservation r WHERE (CAST(:startedBefore AS timestamp) IS NULL OR r.startingDateTime <= CAST(:startedBefore AS timestamp)) "
          + "AND (CAST(:startedAfter AS timestamp) IS NULL OR r.startingDateTime >= CAST(:startedAfter AS timestamp)) "
          + "AND (:eventType IS NULL OR LOWER(r.eventType) LIKE LOWER(CONCAT('%', :eventType, '%'))) "
          + "AND (:isFullPackage IS NULL OR r.isFullPackage =:isFullPackage) "
          + "AND (:status IS NULL OR LOWER(r.status) LIKE LOWER(CONCAT('%', :status, '%'))) "
          + "AND (:isSecurityDepositRefunded IS NULL OR r.isSecurityDepositRefunded =:isSecurityDepositRefunded) "
          + "AND (:minTotalPrice IS NULL OR r.totalPrice >=:minTotalPrice) AND (:maxTotalPrice IS NULL OR r.totalPrice <=:maxTotalPrice) "
          + "AND (:userId IS NULL OR r.userId =:userId) "
          + "AND (:priceComputationMethod IS NULL OR LOWER(r.priceComputationMethod) LIKE LOWER(CONCAT('%', :priceComputationMethod, '%')))")
  Page<Reservation> findAllAndFilter(
      @Param("startedBefore") LocalDateTime startedBefore,
      @Param("startedAfter") LocalDateTime startedAfter,
      @Param("eventType") String eventType,
      @Param("isFullPackage") Boolean isFullPackage,
      @Param("status") String status,
      @Param("isSecurityDepositRefunded") Boolean isSecurityDepositRefunded,
      @Param("minTotalPrice") Double minTotalPrice,
      @Param("maxTotalPrice") Double maxTotalPrice,
      @Param("userId") String userId,
      @Param("priceComputationMethod") String priceComputationMethod,
      Pageable pageable);

  boolean existsByStartingDateTimeBetweenAndStatusNot(
      LocalDateTime dateTime1, LocalDateTime dateTime2, String status);

  boolean existsByEndingDateTimeBetweenAndStatusNot(
      LocalDateTime dateTime1, LocalDateTime dateTime2, String status);

  boolean existsByStartingDateTimeBetweenAndStatusNotAndIdNot(LocalDateTime dateTime1, LocalDateTime dateTime2, String status, Long id);

  boolean existsByEndingDateTimeBetweenAndStatusNotAndIdNot(LocalDateTime dateTime1, LocalDateTime dateTime2, String status, Long id);

  boolean existsByEffectiveEndingDateTimeBetweenAndStatusNotAndIdNot(LocalDateTime dateTime1, LocalDateTime dateTime2, String status, Long id);
}
