package com.zinphraek.leprestigehall.domain.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.zinphraek.leprestigehall.domain.constants.Paths.ReservationPath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

/**
 * Reservation endpoint.
 */
@RestController
public class ReservationController {

  @Autowired
  private final ReservationServiceImplementation reservationService;

  public ReservationController(ReservationServiceImplementation reservationService) {
    this.reservationService = reservationService;
  }

  @PreAuthorize("hasRole('admin')")
  @GetMapping(ReservationPath)
  public ResponseEntity<Page<Reservation>> getReservations(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(reservationService.getReservations(params), HttpStatus.OK);
  }

  @PreAuthorize("#userId == #principal.subject or hasRole('admin')")
  @GetMapping(UserPath + "/{userId}" + ReservationPath)
  public ResponseEntity<Page<Reservation>> getReservationByUserId(
      @PathVariable String userId,
      @RequestParam(required = false) Map<String, String> params,
      @AuthenticationPrincipal Jwt principal) {
    Page<Reservation> userReservations = reservationService.getReservationsByUserId(userId, params);
    return new ResponseEntity<>(userReservations, HttpStatus.OK);
  }

  @PostMapping(ReservationPath)
  public ResponseEntity<Reservation> createReservation(
      @RequestPart("reservation") Reservation newReservation) {
    Reservation reservation = reservationService.createReservation(newReservation);
    return new ResponseEntity<>(reservation, HttpStatus.CREATED);
  }

  @PreAuthorize("#updatedReservation.userId == #principal.subject or hasRole('admin')")
  @PutMapping(UserPath + "/{userId}" + ReservationPath + "/{id}")
  public ResponseEntity<Reservation> updateReservation(
      @PathVariable Long id,
      @RequestPart("reservation") Reservation updatedReservation,
      @AuthenticationPrincipal Jwt principal) {
    Reservation reservation = reservationService.updateReservation(id, updatedReservation);
    return new ResponseEntity<>(reservation, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @PutMapping(UserPath + "/{userId}" + ReservationPath + "/action/{id}")
  public ResponseEntity<Void> updateReservationStatus(
      @PathVariable Long id,
      @RequestPart("reservation") String action,
      @AuthenticationPrincipal Jwt principal) {
    reservationService.updateReservationStatus(id, action);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("#userId == #principal.subject or hasRole('admin')")
  @DeleteMapping(UserPath + "/{userId}" + ReservationPath + "/{id}")
  public ResponseEntity<Void> cancelReservation(
      @PathVariable String userId, @PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
    reservationService.cancelReservation(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(ReservationPath + "/cancel")
  public ResponseEntity<Void> cancelMultipleReservations(
      @RequestPart("ids") List<Long> ids, @AuthenticationPrincipal Jwt principal) {
    reservationService.cancelMultipleReservations(ids);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("#id == #principal.subject or hasRole('admin')")
  @DeleteMapping(ReservationPath + "/{id}/restore/{status}")
  public ResponseEntity<Void> restoreReservation(
      @PathVariable("id") Long id, @PathVariable("status") String status, @AuthenticationPrincipal Jwt principal) {
    reservationService.restoreReservation(id, status);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(ReservationPath + "/restore")
  public ResponseEntity<Void> restoreMultipleReservations(
      @RequestPart("ids") Map<String, List<Long>> ids, @AuthenticationPrincipal Jwt principal) {
    reservationService.restoreMultipleReservations(ids.get("ids"));
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
