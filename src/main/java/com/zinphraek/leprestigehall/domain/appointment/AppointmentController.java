package com.zinphraek.leprestigehall.domain.appointment;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.zinphraek.leprestigehall.domain.constants.Paths.AppointmentPath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

/**
 * Appointment endpoint.
 */
@RestController
public class AppointmentController {

  private final AppointmentServiceImplementation appointmentService;

  public AppointmentController(AppointmentServiceImplementation appointmentService) {
    this.appointmentService = appointmentService;
  }

  /**
   * Endpoint to retrieve all appointments
   *
   * @return -All appointments
   */
  @PreAuthorize("hasRole('admin')")
  @GetMapping(AppointmentPath)
  public ResponseEntity<Page<Appointment>> getAllAppointments(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(appointmentService.getAllAppointments(params), HttpStatus.OK);
  }

  /**
   * Endpoint to retrieve all upcoming appointments
   *
   * @return -A list of all upcoming appointments.
   */
  @PreAuthorize("hasRole('admin')")
  @GetMapping(AppointmentPath + "/upcoming")
  public ResponseEntity<Page<Appointment>> getUpcomingAppointments(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(appointmentService.getUpcomingAppointments(params), HttpStatus.OK);
  }

  /**
   * Endpoint to retrieve an appointment by id
   *
   * @return -An appointment by id.
   */
  @PreAuthorize("hasRole('admin')")
  @GetMapping(AppointmentPath + "/{id}")
  public ResponseEntity<Appointment> adminGetAppointmentById(@PathVariable Long id) {
    return new ResponseEntity<>(appointmentService.getAppointmentById(id), HttpStatus.OK);
  }

  /**
   * Endpoint to retrieve an appointment by id
   *
   * @return -An appointment by id.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @GetMapping(UserPath + "/{userId}" + AppointmentPath + "/{id}")
  public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
    return new ResponseEntity<>(appointmentService.getAppointmentById(id), HttpStatus.OK);
  }

  /**
   * Endpoint to retrieve all appointments by user id
   *
   * @return -A list of all appointments by user id.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @GetMapping(UserPath + "/{userId}" + AppointmentPath)
  public ResponseEntity<Page<Appointment>> getAppointmentsByUserId(
      @RequestParam(required = false) Map<String, String> params, @PathVariable String userId) {
    return new ResponseEntity<>(
        appointmentService.getAppointmentsByUserIdOrUserInfo(params, userId), HttpStatus.OK);
  }

  /**
   * Endpoint to post new appointments
   *
   * @param newAppointment -The new appointment to post.
   * @return -The newly posted appointment.
   */
  @PostMapping(AppointmentPath)
  public ResponseEntity<Appointment> createAppointment(
      @RequestPart("appointment") Appointment newAppointment) {
    return new ResponseEntity<>(
        appointmentService.createAppointment(newAppointment), HttpStatus.CREATED);
  }

  /**
   * Endpoint to update an appointment
   *
   * @param id             -The id of the appointment to update.
   * @param newAppointment -The new appointment to update.
   * @param principal      -The user principal information.
   * @return -The updated appointment.
   */
  // @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @PutMapping(UserPath + "/{userId}" + AppointmentPath + "/{id}")
  public ResponseEntity<Appointment> updateAppointment(
      @PathVariable Long id,
      @RequestPart("appointment") Appointment newAppointment,
      @AuthenticationPrincipal Jwt principal) {
    return new ResponseEntity<>(
        appointmentService.updateAppointment(id, newAppointment, principal), HttpStatus.OK);
  }

  /**
   * Endpoint to update an appointment
   *
   * @param id             -The id of the appointment to update.
   * @param newAppointment -The new appointment to update.
   * @return -The updated appointment.
   */
  @PreAuthorize("hasRole('admin')")
  @PutMapping(AppointmentPath + "/{id}")
  public ResponseEntity<Appointment> adminUpdateAppointment(
      @PathVariable Long id,
      @RequestPart("appointment") Appointment newAppointment,
      @AuthenticationPrincipal Jwt principal) {
    return new ResponseEntity<>(
        appointmentService.updateAppointment(id, newAppointment, principal), HttpStatus.OK);
  }

  /**
   * Endpoint to delete an appointment
   *
   * @param id -The id of the appointment to delete.
   * @return -No content.
   */
  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(AppointmentPath + "/{id}")
  public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
    appointmentService.deleteAppointment(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Endpoint to cancel an appointment
   *
   * @param id -The id of the appointment to cancel.
   * @return -No content.
   */
  @DeleteMapping(UserPath + "/{userId}" + AppointmentPath + "/cancel/{id}")
  public ResponseEntity<Void> cancelAppointment(
      @PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
    appointmentService.cancelAppointment(id, principal);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }


  /**
   * Endpoint to cancel an appointment
   *
   * @param id -The id of the appointment to cancel.
   * @return -No content.
   */
  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(AppointmentPath + "/cancel/{id}")
  public ResponseEntity<Void> adminCancelAppointment(
      @PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
    appointmentService.cancelAppointment(id, principal);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Endpoint to restore an appointment
   *
   * @param id -The id of the appointment to restore.
   * @return -The restored appointment.
   */
  @PreAuthorize("hasRole('admin')")
  @PutMapping(AppointmentPath + "/restore/{id}")
  public ResponseEntity<Appointment> restoreAppointment(@PathVariable Long id) {
    return new ResponseEntity<>(appointmentService.restoreAppointment(id), HttpStatus.OK);
  }
}
