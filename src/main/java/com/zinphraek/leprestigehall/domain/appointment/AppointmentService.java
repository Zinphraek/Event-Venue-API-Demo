package com.zinphraek.leprestigehall.domain.appointment;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

public interface AppointmentService {

  Page<Appointment> getAllAppointments(Map<String, String> params);

  Page<Appointment> getUpcomingAppointments(Map<String, String> params);

  Appointment getAppointmentById(Long id);

  Page<Appointment> getAppointmentsByUserIdOrUserInfo(Map<String, String> params, String userId);

  Appointment updateAppointment(Long id, Appointment newAppointment, Jwt principal);

  Appointment createAppointment(Appointment newAppointment);

  void deleteAppointment(Long id);

  Appointment restoreAppointment(Long id);

  void cancelAppointment(Long id, Jwt principal);
}
