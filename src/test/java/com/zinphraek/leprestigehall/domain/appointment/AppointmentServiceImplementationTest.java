package com.zinphraek.leprestigehall.domain.appointment;

import com.zinphraek.leprestigehall.config.KeycloakRoleConverter;
import com.zinphraek.leprestigehall.domain.data.factories.AppointmentFactory;
import com.zinphraek.leprestigehall.domain.data.factories.UserFactory;
import com.zinphraek.leprestigehall.domain.email.EmailServiceImplementation;
import com.zinphraek.leprestigehall.domain.sms.SMSServiceImplementation;
import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserService;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.Constants.STATUS_CANCELLED;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AppointmentServiceImplementation}.
 */
@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplementationTest {

  UserFactory userFactory = new UserFactory();
  FactoriesUtilities utilities = new FactoriesUtilities();
  AppointmentFactory appointmentFactory = new AppointmentFactory();
  @InjectMocks
  private AppointmentServiceImplementation appointmentService;
  @Mock
  private AppointmentRepository appointmentRepository;
  @Mock
  private KeycloakRoleConverter keycloakRoleConverter;
  @Mock
  private EmailServiceImplementation emailService;
  @Mock
  private UserService userService;

  @Mock
  private SMSServiceImplementation smsService;

  @BeforeEach
  void setUp() {
    appointmentService =
        new AppointmentServiceImplementation(
            keycloakRoleConverter, appointmentRepository, emailService, userService, smsService);
  }

  @Test
  void deleteAppointmentTestSuccessfulCase() {
    long id = 1L;
    when(appointmentRepository.existsById(id)).thenReturn(true);
    this.appointmentService.deleteAppointment(id);

    verify(appointmentRepository, times(1)).deleteById(id);
  }

  @Test
  void deleteAppointmentTestThrowsExceptionWhenAppointmentDoesNotExist() {
    long id = 1L;
    String expectedReasonMessage = "Could not delete non existent appointment.";
    when(appointmentRepository.existsById(id)).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.deleteAppointment(id));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(expectedReasonMessage, exception.getReason());
    verify(appointmentRepository, times(0)).deleteById(id);
  }

  @Test
  void deleteAppointmentTestThrowsDataAccessException() {
    long id = 1L;
    when(appointmentRepository.existsById(id)).thenReturn(true);
    doThrow(new DataAccessException("Database error") {
    })
        .when(appointmentRepository)
        .deleteById(id);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.deleteAppointment(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(appointmentRepository, times(1)).deleteById(id);
  }

  @Test
  void deleteAppointmentTestThrowsExceptionWhenUnexpectedErrorOccurs() {
    long id = 1L;
    when(appointmentRepository.existsById(id)).thenReturn(true);
    doThrow(new RuntimeException("Unexpected error") {
    }).when(appointmentRepository).deleteById(id);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.deleteAppointment(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(appointmentRepository, times(1)).deleteById(id);
  }

  @Test
  void getAppointmentByIdTestSuccessfulCase() {
    long id = 1L;
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(id);
    when(appointmentRepository.findById(id)).thenReturn(Optional.ofNullable(appointment));

    Appointment fetchedAppointment = this.appointmentService.getAppointmentById(id);

    assertEquals(appointment, fetchedAppointment);
    verify(appointmentRepository, times(1)).findById(id);
  }

  @Test
  void getAppointmentTestNotFoundCase() {
    long id = 1L;
    when(appointmentRepository.findById(id)).thenReturn(Optional.empty());
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.getAppointmentById(id));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "Appointment", id), exception.getReason());
    verify(appointmentRepository, times(1)).findById(id);
  }

  @Test
  void getAppointmentByIdTestThrowsExceptionWhenUnexpectedErrorOccurs() {
    long id = 1L;
    when(appointmentRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error") {
    });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.getAppointmentById(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));

    verify(appointmentRepository, times(1)).findById(id);
  }

  @Test
  void getAppointmentByIdTestThrowsDataAccessExceptionCase() {
    long id = 1L;
    when(appointmentRepository.findById(id))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class, () -> this.appointmentService.getAppointmentById(id));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(appointmentRepository, times(1)).findById(id);
  }

  @Test
  void createAppointmentTestSuccessfulCase() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.save(appointment)).thenReturn(appointment);
    Appointment createdAppointment = this.appointmentService.createAppointment(appointment);
    assertEquals(appointment, createdAppointment);
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void createAppointmentThrowsConflictErrorWhenAppointmentAlreadyExists() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(
        String.format(CREATE_CONFLICT_MESSAGE2, "appointment", appointment.getId()),
        exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void createAppointmentThrowsExceptionWhenUnexpectedErrorOccurs() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    when(appointmentRepository.save(appointment))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void createAppointmentTestDataAccessExceptionCase() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    when(appointmentRepository.save(appointment))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void createAppointmentTestThrowsBadRequestErrorWhenAppointmentIsInThePastCase() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInThePast(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Cannot schedule appointment in the past.", exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void createAppointmentTestUnavailableTimeIntervalCase() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    when(appointmentRepository.isConflicting(any(), any(), any())).thenReturn(true);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Unavailable time interval", exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void createAppointmentTestThrowsConflictErrorWhenScheduledTimeIsLessThan30MinutesFromNowCase() {
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setDateTime(utilities.formatLocalDateTime(LocalDateTime.now().plusMinutes(15L)));
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.createAppointment(appointment));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Unavailable time interval", exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestSuccessfulCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setEmail(prevAppointment.getEmail());
    appointment.setUserId(principal.getSubject());
    prevAppointment.setUserId(principal.getSubject());

    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    when(appointmentRepository.save(appointment)).thenReturn(appointment);
    Appointment updatedAppointment =
        this.appointmentService.updateAppointment(1L, appointment, principal);
    assertEquals(appointment, updatedAppointment);
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsBadRequestExceptionWhenAppointmentDoesNotExistCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(false);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(UPDATE_NOT_FOUND_MESSAGE, "appointment"), exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsBadRequestExceptionWhenIdsDoNotMatchCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(any())).thenReturn(true);
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(2L, appointment, principal));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "appointment"), exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsForbiddenExceptionForNonMatchingEmailCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setEmail("somthing1@example.com");
    prevAppointment.setEmail("somthing2@example.com");
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals(String.format(AUTHORIZATION_ERROR_MESSAGE, "update", "appointment"), exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsBadRequestExceptionWhenDateTimeProvidedIsInThePastCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInThePast(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setEmail(prevAppointment.getEmail());
    appointment.setUserId(principal.getSubject());
    prevAppointment.setUserId(principal.getSubject());
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Cannot schedule appointment in the past.", exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void
  updateAppointmentTestThrowsConflictExceptionWhenDateTimeProvidedIsLessThan30MinutesFromNowCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(prevAppointment.getEmail());
    prevAppointment.setUserId(principal.getSubject());
    appointment.setDateTime(utilities.formatLocalDateTime(LocalDateTime.now().plusMinutes(15L)));
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals("Unavailable time interval", exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsDataAccessExceptionCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(prevAppointment.getEmail());
    prevAppointment.setUserId(principal.getSubject());
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    when(appointmentRepository.save(appointment))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsDataAccessExceptionWhenRetrievingPrevAppointmentCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(0)).save(appointment);
  }

  @Test
  void updateAppointmentTestThrowsExceptionWhenUnexpectedErrorOccurs() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    Appointment prevAppointment =
        appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(prevAppointment.getEmail());
    prevAppointment.setUserId(principal.getSubject());
    when(appointmentRepository.existsById(appointment.getId())).thenReturn(true);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(prevAppointment));
    when(appointmentRepository.save(appointment))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.updateAppointment(1L, appointment, principal));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void cancelAppointmentTestSuccessfulCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(principal.getClaimAsString("email"));

    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    when(appointmentRepository.save(appointment)).thenReturn(appointment);

    this.appointmentService.cancelAppointment(1L, principal);
    assertEquals(appointment.getStatus(), STATUS_CANCELLED);
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void cancelAppointmentTestThrowsNotFoundRequestExceptionWhenAppointmentDoesNotExistCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(CANCEL_NOT_FOUND_MESSAGE, "appointment"), exception.getReason());
    verify(appointmentRepository, times(0)).save(any());
  }

  @Test
  void cancelAppointmentTestThrowsForbiddenExceptionWhenUserIsNotAuthorizedCase1() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId("someOtherUserId");
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You are not authorized to cancel this appointment.", exception.getReason());
    verify(appointmentRepository, times(0)).save(any());
  }

  @Test
  void cancelAppointmentTestThrowsForbiddenExceptionWhenUserIsNotAuthorizedCase2() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setEmail("somthing@example.com");
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    assertEquals("You are not authorized to cancel this appointment.", exception.getReason());
    verify(appointmentRepository, times(0)).save(any());
  }

  @Test
  void cancelAppointmentTestThrowsBadRequestExceptionWhenAppointmentIsInThePastCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInThePast(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(principal.getClaimAsString("email"));
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(
        "Cannot cancel an appointment which meeting time has passed.", exception.getReason());
    verify(appointmentRepository, times(0)).save(any());
  }

  @Test
  void cancelAppointmentTestThrowsDataAccessExceptionWhenSavingAppointmentCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(principal.getClaimAsString("email"));
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    when(appointmentRepository.save(appointment))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void cancelAppointmentTestThrowsExceptionWhenUnexpectedErrorOccursCase() {
    Jwt principal = utilities.generateRandomJwt(false);
    Appointment appointment = appointmentFactory.generateRandomAppointmentScheduledInTheFuture(1L);
    appointment.setUserId(principal.getSubject());
    appointment.setEmail(principal.getClaimAsString("email"));
    when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
    when(appointmentRepository.save(appointment))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.cancelAppointment(1L, principal));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1)).save(appointment);
  }

  @Test
  void getUpcomingAppointmentsTestSuccessfulUnpagedCase() {
    List<Appointment> appointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(1, 15, 10);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenReturn(mockPage);
    Page<Appointment> fetchedAppointments =
        this.appointmentService.getUpcomingAppointments(new HashMap<>());
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getUpcomingAppointmentsTestSuccessfulPagedCase() {
    List<Appointment> appointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(1, 15, 10);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenReturn(mockPage);
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    Page<Appointment> fetchedAppointments = this.appointmentService.getUpcomingAppointments(params);
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getUpcomingAppointmentsTestThrowsDataAccessExceptionCase1() {
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getUpcomingAppointments(new HashMap<>()));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getUpcomingAppointmentsTestThrowsDataAccessExceptionCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getUpcomingAppointments(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getUpcomingAppointmentsTestThrowsExceptionWhenUnexpectedErrorOccursCase1() {
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getUpcomingAppointments(new HashMap<>()));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getUpcomingAppointmentsTestThrowsExceptionWhenUnexpectedErrorOccursCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    when(appointmentRepository.findAllUpcomingAppointments(any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getUpcomingAppointments(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1)).findAllUpcomingAppointments(any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestSuccessfulCase1() {
    List<Appointment> appointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(1, 15, 10);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(mockPage);
    Page<Appointment> fetchedAppointments =
        this.appointmentService.getAppointmentsByUserIdOrUserInfo(new HashMap<>(), userId);
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestSuccessfulCase2() {
    List<Appointment> appointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(1, 15, 10);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(mockPage);
    Page<Appointment> fetchedAppointments =
        this.appointmentService.getAppointmentsByUserIdOrUserInfo(params, userId);
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void
  getAppointmentsByUserIdOrUserInfoTestThrowsNotFoundResponseExceptionForNonExistentUserCase() {
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    when(userService.getUserById(userId))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.NOT_FOUND, "No such user exist in the database"));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () ->
                this.appointmentService.getAppointmentsByUserIdOrUserInfo(new HashMap<>(), userId));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_FOR_NON_EXISTENT_USER_MESSAGE, "appointments"), exception.getReason());
    verify(appointmentRepository, times(0))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestThrowsDataAccessExceptionCase1() {
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () ->
                this.appointmentService.getAppointmentsByUserIdOrUserInfo(new HashMap<>(), userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestThrowsDataAccessExceptionCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAppointmentsByUserIdOrUserInfo(params, userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestThrowsExceptionWhenUnexpectedErrorOccursCase1() {
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () ->
                this.appointmentService.getAppointmentsByUserIdOrUserInfo(new HashMap<>(), userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestThrowsExceptionWhenUnexpectedErrorOccursCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    String userId = utilities.generateRandomStringWithDefinedLength(16);
    User user = userFactory.generateRandomUser(userId);
    when(userService.getUserById(userId)).thenReturn(user);
    when(appointmentRepository.findAllByUserIdOrUserInfo(
        any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAppointmentsByUserIdOrUserInfo(params, userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAppointmentsByUserIdOrUserInfoTestThrowsExceptionWhenUnexpectedErrorOccursCase3() {

    String userId = utilities.generateRandomStringWithDefinedLength(16);
    when(userService.getUserById(userId))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Oops, something unexpected happened. Please try again later."));
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () ->
                this.appointmentService.getAppointmentsByUserIdOrUserInfo(new HashMap<>(), userId));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(0))
        .findAllByUserIdOrUserInfo(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestSuccessfulUnpagedCase() {
    List<Appointment> futureAppointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(8, 15, 6);
    List<Appointment> pastAppointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInThePast(1, 7, 5);
    List<Appointment> appointments = new ArrayList<>(futureAppointments);
    appointments.addAll(pastAppointments);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(mockPage);
    Page<Appointment> fetchedAppointments =
        this.appointmentService.getAllAppointments(new HashMap<>());
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestSuccessfulPagedCase() {
    List<Appointment> futureAppointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInTheFuture(8, 15, 6);
    List<Appointment> pastAppointments =
        appointmentFactory.generateListOfRandomAppointmentsScheduledInThePast(1, 7, 5);
    List<Appointment> appointments = new ArrayList<>(futureAppointments);
    appointments.addAll(pastAppointments);
    Page<Appointment> mockPage = new PageImpl<>(appointments);
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(mockPage);
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    Page<Appointment> fetchedAppointments = this.appointmentService.getAllAppointments(params);
    assertEquals(mockPage, fetchedAppointments);
    verify(appointmentRepository, times(0))
        .findAll(any(Pageable.class));
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestThrowsDataAccessExceptionCase1() {
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAllAppointments(new HashMap<>()));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestThrowsDataAccessExceptionCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new DataAccessException("Database error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAllAppointments(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(appointmentRepository, times(0))
        .findAll(any(Pageable.class));
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestThrowsExceptionWhenUnexpectedErrorOccursCase1() {
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAllAppointments(new HashMap<>()));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllAppointmentsTestThrowsExceptionWhenUnexpectedErrorOccursCase2() {
    Map<String, String> customPageParams = utilities.getCustomPageTestParams("id", "10");
    Map<String, String> filterCriteria =
        appointmentFactory
            .generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
                false, "Booked");
    Map<String, String> params = new HashMap<>(customPageParams);
    params.putAll(filterCriteria);
    when(appointmentRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("Unexpected error") {
        });
    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> this.appointmentService.getAllAppointments(params));
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertTrue(
        exception
            .getMessage()
            .contains("Oops, something unexpected happened. Please try again later."));
    verify(appointmentRepository, times(0))
        .findAll(any(Pageable.class));
    verify(appointmentRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }
}
