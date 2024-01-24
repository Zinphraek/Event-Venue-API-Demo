package com.zinphraek.leprestigehall.domain.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

  @Query(
      "SELECT a FROM Appointment a WHERE (:firstName IS NULL OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) "
          + "AND (:lastName IS NULL OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) "
          + "AND (:phone IS NULL OR a.phone LIKE CONCAT('%', :phone, '%')) "
          + "AND (:email IS NULL OR LOWER(a.email) LIKE LOWER(CONCAT('%', :email, '%'))) "
          + "AND (CAST(:dateTime AS timestamp) IS NULL OR (a.dateTime) >= CAST(:dateTime AS timestamp)) "
          + "AND (:status IS NULL OR LOWER(a.status) LIKE LOWER(CONCAT('%', :status, '%')))"
          + "AND (:userId IS NULL OR a.userId=:userId)")
  Page<Appointment> findAllAndFilter(
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("phone") String phone,
      @Param("email") String email,
      @Param("dateTime") LocalDateTime dateTime,
      @Param("status") String status,
      @Param("userId") String userId,
      Pageable pageable);

  @Query(
      "SELECT a FROM Appointment a WHERE a.dateTime >= CURRENT_TIMESTAMP AND a.status = 'Booked'")
  Page<Appointment> findAllUpcomingAppointments(Pageable pageable);

  @Query(
      "SELECT a FROM Appointment a WHERE a.userId = :userId OR (a.email = :email AND a.userId IS NULL) "
          + "OR (a.firstName = :firstName AND a.lastName = :lastName "
          + "AND a.phone = :phone AND a.userId IS NULL)")
  Page<Appointment> findAllByUserIdOrUserInfo(
      @Param("userId") String userId,
      @Param("email") String email,
      @Param("firstName") String firstName,
      @Param("lastName") String lastName,
      @Param("phone") String phone,
      Pageable pageable);

  @Query(
      "SELECT CASE WHEN COUNT (a) > 0 THEN true ELSE false END FROM Appointment a "
          + "WHERE a.dateTime >=:dateTime1 AND a.dateTime <=:dateTime2 "
          + "AND a.status = 'Booked' AND (a.id != :id OR (:id IS NULL AND a.id IS NOT NULL))")
  boolean isConflicting(
      @Param("dateTime1") LocalDateTime dateTime1,
      @Param("dateTime2") LocalDateTime dateTime2,
      @Param("id") Long id);
}
