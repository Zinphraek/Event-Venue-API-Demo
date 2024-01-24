package com.zinphraek.leprestigehall.domain.appointment;

import com.twilio.exception.TwilioException;
import com.zinphraek.leprestigehall.config.KeycloakRoleConverter;
import com.zinphraek.leprestigehall.domain.email.EmailServiceImplementation;
import com.zinphraek.leprestigehall.domain.email.Mail;
import com.zinphraek.leprestigehall.domain.sms.SMSServiceImplementation;
import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserService;
import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class AppointmentServiceImplementation implements AppointmentService {

  private final Logger logger = LogManager.getLogger(AppointmentServiceImplementation.class);

  @Autowired
  private final KeycloakRoleConverter keycloakRoleConverter;

  @Autowired
  private final AppointmentRepository appointmentRepository;

  @Autowired
  private final EmailServiceImplementation emailService;

  @Autowired
  private final UserService userService;

  @Autowired
  private final SMSServiceImplementation smsService;

  public AppointmentServiceImplementation(
      KeycloakRoleConverter keycloakRoleConverter,
      AppointmentRepository appointmentRepository,
      EmailServiceImplementation emailService,
      UserService userService, SMSServiceImplementation smsService) {
    this.keycloakRoleConverter = keycloakRoleConverter;
    this.appointmentRepository = appointmentRepository;
    this.emailService = emailService;
    this.userService = userService;
    this.smsService = smsService;
  }

  /**
   * Check whether the provided date time is available to be booked.
   *
   * @param dateTime - Date-time to be checked. - The date-time must be in the future. - The
   *                 date-time must be at least 30 minutes after the current date-time.
   * @param id       - The id of the appointment to be checked
   */
  private void checkDateTimeAvailability(LocalDateTime dateTime, Long id) {
    LocalDateTime now = LocalDateTime.now();
    if (now.isAfter(dateTime)) {
      logger.error("Cannot schedule appointment in the past.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot schedule appointment in the past.");
    }
    if (appointmentRepository.isConflicting(
        dateTime.minusMinutes(15L), dateTime.plusMinutes(15L), id)
        || now.plusMinutes(30L).isAfter(dateTime)) {
      logger.error("Unavailable time interval");
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Unavailable time interval");
    }
  }

  /**
   * Generate and populate a mail object with the provided appointment information.
   *
   * @param newAppointment The appointment to be confirmed.
   * @return A mail object containing the appointment information.
   */
  private static Mail getMail(Appointment newAppointment, String subject) {
    Mail mail = new Mail();
    mail.setFrom(NO_REPLY_EMAIL_ADDRESS);
    mail.setTo(newAppointment.getEmail());
    mail.setSubject(subject);
    mail.setEmailTemplate("AppointmentEmailTemplate");

    mail.addVariable(
        "recipientName", newAppointment.getFirstName() + " " + newAppointment.getLastName());
    mail.addVariable("appointmentStatus", newAppointment.getStatus().toLowerCase());
    mail.addVariable(
        "appointmentDate",
        newAppointment
            .getDateTime());
    mail.addVariable("websiteLink", FRONTEND_CLIENT_URL);
    mail.addVariable("supportServiceEmail", SUPPORT_EMAIL_ADDRESS);
    mail.addVariable("supportServicePhone", SUPPORT_PHONE_NUMBER);

    return mail;
  }

  /**
   * Generate a tuple containing a customPage entity and a ReservationFilterCriteria.
   *
   * @param params The sorting and filtering options
   * @return A tuple containing a customPage entity and a ReservationFilterCriteria.
   */
  private Pair<CustomPage, AppointmentFilterCriteria> generateCustomPageAppointmentFilterCriteria(
      Map<String, String> params) {
    CustomPage customPage = createCustomPageFromParams(params);
    AppointmentFilterCriteria appointmentFilterCriteria = new AppointmentFilterCriteria();

    // Setting up the appointmentFilterCriteria  with the corresponding value in the param object.

    if (params.containsKey("firstName")) {
      appointmentFilterCriteria.setFirstName(params.get("firstName"));
    }

    if (params.containsKey("lastName")) {
      appointmentFilterCriteria.setLastName(params.get("lastName"));
    }

    if (params.containsKey("phone")) {
      appointmentFilterCriteria.setPhone(params.get("phone"));
    }

    if (params.containsKey("email")) {
      appointmentFilterCriteria.setEmail(params.get("email"));
    }

    if (params.containsKey("dateTime")) {
      if (Objects.nonNull(params.get("dateTime")) && !params.get("dateTime").isBlank()) {
        appointmentFilterCriteria.setDateTime(params.get("dateTime"));
      }
    }

    if (params.containsKey("status")) {
      appointmentFilterCriteria.setStatus(params.get("status"));
    }

    if (params.containsKey("userId")) {
      appointmentFilterCriteria.setUserId(params.get("userId"));
    }

    return Pair.of(customPage, appointmentFilterCriteria);
  }

  /**
   * Retrieve all appointments from the database
   *
   * @param params The sorting and filtering options
   * @return - A list of all appointments
   */
  @Override
  public Page<Appointment> getAllAppointments(Map<String, String> params) {

    Page<Appointment> appointments;
    Pageable pageable = Pageable.unpaged();
    Pair<CustomPage, AppointmentFilterCriteria> pageAppointmentFilterCriteriaPair =
        Pair.of(new CustomPage(), new AppointmentFilterCriteria());

    logger.info("Fetching appointments...");
    try {
      if (!params.isEmpty()) {
        // Extracting the customPage entity and the appointmentFilterCriteria from the params.
        pageAppointmentFilterCriteriaPair =
            generateCustomPageAppointmentFilterCriteria(params);

        pageable =
            PageRequest.of(
                pageAppointmentFilterCriteriaPair.getFirst().getPageNumber(),
                pageAppointmentFilterCriteriaPair.getFirst().getPageSize(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortDirection(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortBy());
      }
      appointments =
          appointmentRepository.findAllAndFilter(
              pageAppointmentFilterCriteriaPair.getSecond().getFirstName(),
              pageAppointmentFilterCriteriaPair.getSecond().getLastName(),
              pageAppointmentFilterCriteriaPair.getSecond().getPhone(),
              pageAppointmentFilterCriteriaPair.getSecond().getEmail(),
              pageAppointmentFilterCriteriaPair.getSecond().getDateTime(),
              pageAppointmentFilterCriteriaPair.getSecond().getStatus(),
              pageAppointmentFilterCriteriaPair.getSecond().getUserId(),
              pageable);

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Appointments"));
    return appointments;
  }

  /**
   * Retrieve all upcoming appointments
   *
   * @param params Object defining which data are needed.
   * @return - A list of upcoming appointments.
   */
  @Override
  public Page<Appointment> getUpcomingAppointments(Map<String, String> params) {

    Page<Appointment> upcomingAppointments;
    Pageable pageable = Pageable.unpaged();

    logger.info("Fetching upcoming appointments...");
    try {
      if (!params.isEmpty()) {
        // Extracting the customPage entity and the appointmentFilterCriteria from the params.
        Pair<CustomPage, AppointmentFilterCriteria> pageAppointmentFilterCriteriaPair =
            generateCustomPageAppointmentFilterCriteria(params);

        pageable =
            PageRequest.of(
                pageAppointmentFilterCriteriaPair.getFirst().getPageNumber(),
                pageAppointmentFilterCriteriaPair.getFirst().getPageSize(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortDirection(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortBy());
      }
      upcomingAppointments = appointmentRepository.findAllUpcomingAppointments(pageable);

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Upcoming appointments"));
    return upcomingAppointments;
  }

  /**
   * @param id The appointment's id to retrieve.
   * @return An appointment or a not found status exception.
   */
  @Override
  public Appointment getAppointmentById(Long id) {
    Optional<Appointment> appointment;
    try {
      logger.info("Fetching appointment...");
      appointment = appointmentRepository.findById(id);
      if (appointment.isPresent()) {
        logger.info(String.format(GET_SUCCESS_MESSAGE, "Appointment", id));
        return appointment.get();
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    logger.error(String.format(GET_NOT_FOUND_MESSAGE, "Appointment", id));
    throw new ResponseStatusException(
        HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "Appointment", id));
  }

  /**
   * Retrieve all appointments for a specific user
   *
   * @param params The sorting and filtering options
   * @param userId The user's id
   * @return A list of appointments
   */
  @Override
  public Page<Appointment> getAppointmentsByUserIdOrUserInfo(
      Map<String, String> params, String userId) {

    logger.info("Fetching appointments...");
    Pair<CustomPage, AppointmentFilterCriteria> pageAppointmentFilterCriteriaPair;
    Pageable pageable = Pageable.unpaged();
    Page<Appointment> appointments = null;
    User user;

    try {
      user = userService.getUserById(userId);
      if (!params.isEmpty()) {
        // Setting up the customPage entity and the appointmentFilterCriteria with the corresponding
        // values in the param object.

        pageAppointmentFilterCriteriaPair =
            generateCustomPageAppointmentFilterCriteria(params);

        pageable =
            PageRequest.of(
                pageAppointmentFilterCriteriaPair.getFirst().getPageNumber(),
                pageAppointmentFilterCriteriaPair.getFirst().getPageSize(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortDirection(),
                pageAppointmentFilterCriteriaPair.getFirst().getSortBy());
      }
      appointments =
          appointmentRepository.findAllByUserIdOrUserInfo(
              userId,
              user.getEmail(),
              user.getFirstName(),
              user.getLastName(),
              user.getPhone(),
              pageable);

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException e) {
      logger.error(RESPONSE_STATUS_EXCEPTION_LOG_MESSAGE, e);
      if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, String.format(GET_FOR_NON_EXISTENT_USER_MESSAGE, "appointments"));
      }
      if (e.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE)) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
      }
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Appointments"));
    return appointments;
  }

  /**
   * Update existing appointment
   *
   * @param id             - The appointment's id.
   * @param newAppointment - The updated appointment.
   * @param principal      - The user's JWT.
   * @return - The newly updated appointment.
   */
  @Override
  public Appointment updateAppointment(Long id, Appointment newAppointment, Jwt principal) {
    Optional<Appointment> prevAppointment;
    try {
      if (!appointmentRepository.existsById(id)) {
        logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "appointment"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(UPDATE_NOT_FOUND_MESSAGE, "appointment"));
      }

      if (!id.equals(newAppointment.getId())) {
        logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "appointment"));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "appointment"));
      }

      prevAppointment = appointmentRepository.findById(id);

      Collection<GrantedAuthority> userAuthorities = keycloakRoleConverter.convert(principal);
      boolean hasRoleAdmin =
          userAuthorities != null
              && userAuthorities.contains(new SimpleGrantedAuthority("ROLE_admin"));

      if (!hasRoleAdmin) {
        if (prevAppointment.isPresent()) {
          if (prevAppointment.get().getUserId() != null
              && !prevAppointment.get().getUserId().equals(principal.getSubject())) {
            logger.error(String.format(AUTHORIZATION_ERROR_MESSAGE, "update", "appointment"));
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, String.format(AUTHORIZATION_ERROR_MESSAGE, "update", "appointment"));
          } else {
            if (!prevAppointment.get().getEmail().equals(newAppointment.getEmail())) {
              logger.error(String.format(AUTHORIZATION_ERROR_MESSAGE, "update", "appointment"));
              throw new ResponseStatusException(
                  HttpStatus.FORBIDDEN, String.format(AUTHORIZATION_ERROR_MESSAGE, "update", "appointment"));
            }
          }
        }
      }
      checkDateTimeAvailability(newAppointment.getDateTime(), id);

      LocalDateTime prevDateTime = prevAppointment.map(Appointment::getDateTime).orElse(null);

      logger.info("Updating appointment...");
      appointmentRepository.save(newAppointment);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Appointment", id));

      // Sending confirmation email.
      Mail mail = getMail(newAppointment, APPOINTMENT_UPDATE_SUBJECT);
      logger.info("Sending appointment update confirmation email...");
      emailService.sendEmail(mail);

      // Sending confirmation SMS to admin.
      String customerName = newAppointment.getFirstName() + " " + newAppointment.getLastName();
      assert prevDateTime != null;
      String message = String.format(APPOINTMENT_UPDATE_SMS, prevDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
          newAppointment.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
          customerName, newAppointment.getPhone(), newAppointment.getEmail());

      logger.info("Notifying admin of appointment update...");
      // // // // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);

    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (TwilioException e) {
      logger.error("Couldn't send confirmation SMS.");
      logger.error(e);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newAppointment;
  }

  /**
   * Persists a newly generated appointment
   *
   * @param newAppointment The new appointment
   * @return - The persisted appointment.
   */
  @Override
  public Appointment createAppointment(Appointment newAppointment) {

    try {
      if (newAppointment.getId() != null
          && appointmentRepository.existsById(newAppointment.getId())) {
        logger.error(String.format(CREATE_CONFLICT_MESSAGE2, "appointment", newAppointment.getId()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(CREATE_CONFLICT_MESSAGE2, "appointment", newAppointment.getId()));
      }
      checkDateTimeAvailability(newAppointment.getDateTime(), newAppointment.getId());
      if (newAppointment.getStatus() == null) {
        newAppointment.setStatus(STATUS_BOOKED);
      }

      logger.info("Saving appointment...");
      appointmentRepository.save(newAppointment);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "Appointment"));

      // Sending confirmation email
      Mail mail = getMail(newAppointment, APPOINTMENT_CONFIRMATION_SUBJECT);
      logger.info("Sending appointment booking confirmation email...");
      emailService.sendEmail(mail);

      // Sending confirmation SMS to admin.
      String customerName = newAppointment.getFirstName() + " " + newAppointment.getLastName();
      String message = String.format(APPOINTMENT_CONFIRMATION_SMS, newAppointment.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
          customerName, newAppointment.getPhone(), newAppointment.getEmail(), newAppointment.getRaison());

      logger.info("Notifying admin of appointment booking...");
      // // // // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);

    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (TwilioException e) {
      logger.error("Couldn't send confirmation SMS.");
      logger.error(e);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newAppointment;
  }

  /**
   * Delete an appointment.
   *
   * @param id The id of the appointment to cancel.
   */
  @Override
  public void deleteAppointment(Long id) {
    if (appointmentRepository.existsById(id)) {
      try {
        appointmentRepository.deleteById(id);
        logger.info(String.format(DELETE_SUCCESS_MESSAGE, "Appointment", id));
      } catch (DataAccessException e) {
        logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
      } catch (RuntimeException e) {
        logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
      }
    } else {
      logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "appointment"));
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format(DELETE_NOT_FOUND_MESSAGE, "appointment"));
    }
  }

  /**
   * Restore an appointment.
   *
   * @param id The id of the appointment to restore.
   */
  @Override
  public Appointment restoreAppointment(Long id) {

    Appointment appointment = getAppointmentById(id);
    try {
      if (appointment.getStatus().equals(STATUS_CANCELLED)) {
        checkDateTimeAvailability(appointment.getDateTime(), id);
        appointment.setStatus(STATUS_BOOKED);
        logger.info("Restoring appointment...");
        appointmentRepository.save(appointment);
        logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Appointment", id));

        // Sending cancellation email.
        Mail mail = getMail(appointment, APPOINTMENT_RESCHEDULE_SUBJECT);
        logger.info("Sending appointment restoration email...");
        emailService.sendEmail(mail);

        // Sending confirmation SMS to admin.
        String customerName = appointment.getFirstName() + " " + appointment.getLastName();
        String message = String.format(APPOINTMENT_RESTORATION_SMS, customerName, appointment.getDateTime()
                .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), appointment.getPhone(),
            appointment.getEmail());

        logger.info("Notifying admin of appointment update...");
        // // // // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (MailException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (TwilioException e) {
      logger.error("Couldn't send confirmation SMS.");
      logger.error(e);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return appointment;
  }

  /**
   * Cancel an appointment.
   *
   * @param id The id of the appointment to cancel.
   */
  @Override
  public void cancelAppointment(Long id, Jwt principal) {
    Appointment appointment;

    try {
      appointment = getAppointmentById(id);


      // Checking if the user is authorized to cancel the appointment.
      Collection<GrantedAuthority> userAuthorities = keycloakRoleConverter.convert(principal);
      boolean hasRoleAdmin =
          userAuthorities != null
              && userAuthorities.contains(new SimpleGrantedAuthority("ROLE_admin"));

      assert appointment != null;
      if (!hasRoleAdmin) {
        if (appointment.getUserId() != null
            && !appointment.getUserId().equals(principal.getSubject())) {
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN, String.format(AUTHORIZATION_ERROR_MESSAGE, "cancel", "appointment"));
        } else {
          if (appointment.getEmail() != null
              && !appointment.getEmail().equals(principal.getClaimAsString("email"))) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, String.format(AUTHORIZATION_ERROR_MESSAGE, "cancel", "appointment"));
          }
        }
      }

      if (appointment.getDateTime().isBefore(LocalDateTime.now())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Cannot cancel an appointment which meeting time has passed.");
      }
      appointment.setStatus(STATUS_CANCELLED);

      logger.info("Cancelling appointment...");
      appointmentRepository.save(appointment);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Appointment", id));

      // Sending cancellation email.
      Mail mail = getMail(appointment, APPOINTMENT_CANCELLATION_SUBJECT);
      logger.info("Sending appointment cancellation email...");
      emailService.sendEmail(mail);

      // Sending cancellation SMS to admin.
      String customerName = appointment.getFirstName() + " " + appointment.getLastName();
      String message = String.format(APPOINTMENT_CANCELLATION_SMS, appointment.getDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
          customerName, appointment.getPhone(), appointment.getEmail());

      logger.info("Notifying admin of appointment cancellation...");
      // // // // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);

    } catch (ResponseStatusException rse) {
      if (rse.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, String.format(CANCEL_NOT_FOUND_MESSAGE, "appointment"));
      }
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException e) {
      logger.error("Couldn't send cancellation email.");
      logger.error(e);
    } catch (TwilioException e) {
      logger.error("Couldn't send cancellation SMS.");
      logger.error(e);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}
