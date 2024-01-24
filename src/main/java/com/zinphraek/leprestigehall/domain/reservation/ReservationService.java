package com.zinphraek.leprestigehall.domain.reservation;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Reservation service interface.
 */
public interface ReservationService {

  Page<Reservation> getReservations(Map<String, String> params);

  Page<Reservation> getReservationsByUserId(String userId, Map<String, String> params);

  Reservation createReservation(Reservation newReservation);

  Reservation updateReservation(Long reservationId, Reservation newReservation);

  void cancelReservation(Long reservationId);

  void cancelMultipleReservations(List<Long> ids);

  void restoreReservation(Long reservationId, String status);

  void restoreMultipleReservations(List<Long> ids);

  void updateReservationStatus(Long reservationId, String action);
}
