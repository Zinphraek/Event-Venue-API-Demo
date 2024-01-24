package com.zinphraek.leprestigehall.domain.reservation;

import com.zinphraek.leprestigehall.domain.addon.AddOnRepository;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOnRepository;
import com.zinphraek.leprestigehall.domain.data.factories.AddonFactory;
import com.zinphraek.leprestigehall.domain.data.factories.InvoiceFactory;
import com.zinphraek.leprestigehall.domain.data.factories.ReservationFactory;
import com.zinphraek.leprestigehall.domain.data.factories.UserFactory;
import com.zinphraek.leprestigehall.domain.email.EmailServiceImplementation;
import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.domain.invoice.InvoiceRepository;
import com.zinphraek.leprestigehall.domain.invoice.InvoiceService;
import com.zinphraek.leprestigehall.domain.sms.SMSServiceImplementation;
import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserServiceImpl;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;
import com.zinphraek.leprestigehall.utilities.helpers.ReservationServiceHelper;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplementationTest {

  UserFactory userFactory = new UserFactory();
  AddonFactory addonFactory = new AddonFactory();
  InvoiceFactory invoiceFactory = new InvoiceFactory();
  FactoriesUtilities utilities = new FactoriesUtilities();
  ReservationFactory reservationFactory = new ReservationFactory();


  @Mock
  private UserServiceImpl userService;
  @Mock
  private InvoiceService invoiceService;
  @Mock
  private AddOnRepository addOnRepository;
  @Mock
  private InvoiceRepository invoiceRepository;
  @Mock
  private SMSServiceImplementation smsService;
  @Mock
  private ReservationServiceHelper serviceHelper;
  @Mock
  private EmailServiceImplementation emailService;
  @Mock
  private ReservationRepository reservationRepository;
  @Mock
  private RequestedAddOnRepository requestedAddOnRepository;
  @Mock
  private ReservationDiscountRepo reservationDiscountRepo;
  @Mock
  private ReservationRateRepo reservationRateRepo;
  @InjectMocks
  private ReservationServiceImplementation reservationServiceImplementation;


  @BeforeEach
  void setUp() {
    reservationServiceImplementation = new ReservationServiceImplementation(
        serviceHelper, reservationRepository, emailService, smsService, userService,
        addOnRepository, invoiceRepository, invoiceService, requestedAddOnRepository,
        reservationDiscountRepo, reservationRateRepo);
  }

  // --------------------- Tests for createReservation ---------------------
  @Test
  void createReservationTestReservationWithoutAddOnSuccessfulCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(serviceHelper.computeDueDate(any())).thenCallRealMethod();
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());

    Reservation createdReservation = reservationServiceImplementation.createReservation(reservation);

    assertEquals(reservation, createdReservation);
    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  void createReservationTestReservationWithAddOnSuccessfulCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    RequestedAddOn requestedAddOn = addonFactory.generateRandomRequestedAddOn(1L, 5d);
    reservation.setAddOns(List.of(requestedAddOn));
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(addOnRepository.existsById(any())).thenReturn(true);
    when(serviceHelper.computeDueDate(any())).thenCallRealMethod();
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());

    Reservation createdReservation = reservationServiceImplementation.createReservation(reservation);

    assertEquals(reservation, createdReservation);
    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  void createReservationWithExistingRate() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    ReservationRate reservationRates = new ReservationRate(1L, 5d, 5d, 5d, 5d);
    reservation.setRates(reservationRates);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(serviceHelper.computeDueDate(any())).thenCallRealMethod();
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());
    when(reservationRateRepo.existsBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
        any(), any(), any(), any()
    )).thenReturn(true);
    when(reservationRateRepo.findBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
        any(), any(), any(), any()
    )).thenReturn(Optional.of(reservationRates));

    Reservation createdReservation = reservationServiceImplementation.createReservation(reservation);

    assertEquals(reservation, createdReservation);
    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  void createReservationWithNonExistingDiscount() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    ReservationDiscount reservationDiscount = new ReservationDiscount();
    reservationDiscount.setType("Percentage");
    reservationDiscount.setPercentage(5d);
    reservation.setDiscount(reservationDiscount);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(serviceHelper.computeDueDate(any())).thenCallRealMethod();
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());

    Reservation createdReservation = reservationServiceImplementation.createReservation(reservation);

    assertEquals(reservation, createdReservation);
    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  void createReservationWithExistingDiscount() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    ReservationDiscount reservationDiscount = new ReservationDiscount();
    reservationDiscount.setType("Percentage");
    reservationDiscount.setPercentage(5d);
    reservation.setDiscount(reservationDiscount);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(serviceHelper.computeDueDate(any())).thenCallRealMethod();
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());
    when(reservationDiscountRepo.existsByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
        any(), any(), any(), any(), any(), any(), any()
    )).thenReturn(true);
    when(reservationDiscountRepo.findByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
        any(), any(), any(), any(), any(), any(), any()
    )).thenReturn(Optional.of(reservationDiscount));

    Reservation createdReservation = reservationServiceImplementation.createReservation(reservation);

    assertEquals(reservation, createdReservation);
    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any());
  }

  @Test
  void createReservationTestIdConflictCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.existsById(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(CREATE_CONFLICT_MESSAGE1, "reservation", reservation.getId()), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(userService, times(0)).getUserById(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createdReservationTestReservationScheduledInThePastCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, true, false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(RESERVATION_START_IN_THE_PAST_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestReservationEndsBeforeItStartsCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "ending"), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestReservationEffectivelyEndsBeforeItStartsCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    reservation.setEffectiveEndingDateTime(utilities.formatLocalDateTime(utilities.generateRandomFutureDateBefore(reservation.getStartingDateTime())));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "effective ending"), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestConflictingTimeSlotCase1() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.existsByStartingDateTimeBetweenAndStatusNot(any(), any(), any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestConflictingTimeSlotCase2() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.existsByEndingDateTimeBetweenAndStatusNot(any(), any(), any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestMissingUserIdCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    reservation.setUserId(null);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestNotFoundUserCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    reservation.setUserId(utilities.generateRandomStringWithDefinedLength(16));

    when(userService.getUserById(any())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
        String.format(GET_NOT_FOUND_MESSAGE, "user", reservation.getUserId())));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "user", reservation.getUserId()), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationTestReservationWithNonExistingAddOnCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    RequestedAddOn requestedAddOn = addonFactory.generateRandomRequestedAddOn(1L, 5d);
    reservation.setAddOns(List.of(requestedAddOn));
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));

    when(userService.getUserById(any())).thenReturn(user);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(RESERVATION_WITH_NON_EXISTENT_ADD_ON_ERROR_MESSAGE,
        requestedAddOn.getAddOn().getId()), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
    verify(requestedAddOnRepository, times(0)).save(any());
  }

  @Test
  void createReservationTestDataAccessExceptionCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(reservationRepository.save(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  @Test
  void createReservationThrowsConflictWhenReservationExists() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.existsById(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(CREATE_CONFLICT_MESSAGE1, "reservation", reservation.getId()), exception.getReason());
  }

  @Test
  void createReservationThrowsBadRequestWhenReservationScheduledInThePast() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, true, false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(RESERVATION_START_IN_THE_PAST_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void createReservationThrowsBadRequestWhenReservationEndsBeforeItStarts() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "ending"), exception.getReason());
  }

  @Test
  void createReservationThrowsConflictWhenTimeSlotIsUnavailable() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.existsByStartingDateTimeBetweenAndStatusNot(any(), any(), any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void createReservationThrowsBadRequestWhenUserIdIsMissing() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    reservation.setUserId(null);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"), exception.getReason());
  }

  @Test
  void createReservationTestRuntimeExceptionCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(userService.getUserById(any())).thenReturn(user);
    when(reservationRepository.save(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.createReservation(reservation));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(1)).save(any());
    verify(invoiceRepository, times(0)).save(any());
    verify(emailService, times(0)).sendEmail(any());
  }

  // --------------------- Tests for updateReservation ---------------------

  @Test
  void updateReservationTestSuccessfulCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation prevReservation = reservationFactory.generateRandomReservation(1L, false, false);
    RequestedAddOn requestedAddOn = addonFactory.generateRandomRequestedAddOn(1L, 5d);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    Invoice invoice = invoiceFactory.generateRandomInvoice(1L, "INV-0001", true);
    invoice.setUser(user);
    invoice.setReservation(prevReservation);
    reservation.setUserId(user.getUserId());
    prevReservation.setUserId(user.getUserId());
    reservation.setAddOns(List.of(requestedAddOn));

    when(reservationRepository.findById(any())).thenReturn(java.util.Optional.of(prevReservation));
    when(invoiceRepository.findByReservationId(any())).thenReturn(Optional.of(invoice));
    when(addOnRepository.existsById(any())).thenReturn(true);
    when(addOnRepository.existsById(any())).thenReturn(true);
    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());

    Reservation updatedReservation = reservationServiceImplementation.updateReservation(reservation.getId(), reservation);

    assertEquals(reservation, updatedReservation);
    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationTestUpdateNonExistentReservationCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(reservation.getId(), reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(UPDATE_NOT_FOUND_MESSAGE, "reservation"), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
  }

  @Test
  void updateReservationTestMismatchingUserIdCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation prevReservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());
    prevReservation.setUserId(utilities.generateRandomStringWithDefinedLength(16));

    when(reservationRepository.findById(any())).thenReturn(java.util.Optional.of(prevReservation));

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(reservation.getId(), reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(FIELD_MISMATCH_ERROR_MESSAGE, "user's id"), exception.getReason());

    verify(reservationRepository, times(0)).save(any());
  }

  @Test
  void updateReservationTestDataAccessExceptionCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation prevReservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());
    prevReservation.setUserId(user.getUserId());

    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());
    when(reservationRepository.findById(any())).thenReturn(java.util.Optional.of(prevReservation));
    when(reservationRepository.save(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(reservation.getId(), reservation));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationTestGenericExceptionCase() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation prevReservation = reservationFactory.generateRandomReservation(1L, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());
    prevReservation.setUserId(user.getUserId());

    doCallRealMethod().when(serviceHelper).computeTotalPrice(any(), any());
    when(reservationRepository.findById(any())).thenReturn(java.util.Optional.of(prevReservation));
    when(reservationRepository.save(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(reservation.getId(), reservation));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationThrowsBadRequestWhenReservationDoesNotExist() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(1L, reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(UPDATE_NOT_FOUND_MESSAGE, "reservation"), exception.getReason());
  }

  @Test
  void updateReservationThrowsBadRequestWhenUserIdDoesNotMatch() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation existingReservation = reservationFactory.generateRandomReservation(1L, false, false);
    existingReservation.setUserId("differentUserId");

    when(reservationRepository.findById(any())).thenReturn(Optional.of(existingReservation));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(1L, reservation));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(FIELD_MISMATCH_ERROR_MESSAGE, "user's id"), exception.getReason());
  }

  @Test
  void updateReservationConflictCase1() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation existingReservation = reservationFactory.generateRandomReservation(1L, false, false);
    LocalDateTime dateTime = reservation.getEndingDateTime().plusHours(1);
    reservation.setEffectiveEndingDateTime(utilities.formatLocalDateTime(dateTime));
    existingReservation.setUserId(reservation.getUserId());

    when(reservationRepository.findById(any())).thenReturn(Optional.of(existingReservation));
    when(reservationRepository.existsByStartingDateTimeBetweenAndStatusNotAndIdNot(any(), any(), any(), any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(1L, reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void updateReservationConflictCase2() {
    Reservation reservation = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation existingReservation = reservationFactory.generateRandomReservation(1L, false, false);
    existingReservation.setUserId(reservation.getUserId());

    when(reservationRepository.findById(any())).thenReturn(Optional.of(existingReservation));
    when(reservationRepository.existsByStartingDateTimeBetweenAndStatusNotAndIdNot(any(), any(), any(), any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservation(1L, reservation));

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE, exception.getReason());
  }

  // --------------------- Tests for getReservations ---------------------

  @Test
  void getReservationsReturnsAllReservations() {
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);
    Page<Reservation> reservations = new PageImpl<>(List.of(reservation1, reservation2));

    when(reservationRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class))).thenReturn(reservations);

    Page<Reservation> returnedReservations = reservationServiceImplementation.getReservations(new HashMap<>());

    assertEquals(reservations, returnedReservations);
    verify(reservationRepository, times(1)).findAllAndFilter(any(), any(), any(), any(),
        any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getReservationWithPaginationReturnsAllReservations() {
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);
    Page<Reservation> reservations = new PageImpl<>(List.of(reservation1, reservation2));
    Map<String, String> pageParams = utilities.getCustomPageTestParams("id", "10");

    when(serviceHelper.generateCustomPageAndReservationFilterCriteria(any())).thenCallRealMethod();
    when(reservationRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class))).thenReturn(reservations);

    Page<Reservation> returnedReservations = reservationServiceImplementation.getReservations(pageParams);

    assertEquals(reservations, returnedReservations);
    verify(reservationRepository, times(1)).findAllAndFilter(any(), any(), any(), any(),
        any(), any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getReservationsThrowsDataAccessException() {
    when(reservationRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class))).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.getReservations(new HashMap<>()));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getReservationsThrowsInternalServerError() {
    when(reservationRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class))).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.getReservations(new HashMap<>()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  // --------------------- Tests for getReservationsByUserId ---------------------
  @Test
  void getReservationsByUserIdReturnsUnpagedUserReservations() {
    String userId = "testUserId";
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    reservation1.setUserId(userId);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);
    reservation2.setUserId(userId);
    Page<Reservation> reservations = new PageImpl<>(List.of(reservation1, reservation2));

    when(reservationRepository.findByUserId(any(), any())).thenReturn(reservations);

    Page<Reservation> returnedReservations = reservationServiceImplementation.getReservationsByUserId(userId, new HashMap<>());

    assertEquals(reservations, returnedReservations);
    verify(reservationRepository, times(1)).findByUserId(any(), any());
  }

  @Test
  void getReservationsByUserIdReturnsPagedUserReservations() {
    String userId = "testUserId";
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    reservation1.setUserId(userId);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);
    reservation2.setUserId(userId);
    Page<Reservation> reservations = new PageImpl<>(List.of(reservation1, reservation2));
    Map<String, String> pageParams = utilities.getCustomPageTestParams("id", "10");

    when(serviceHelper.generateCustomPageAndReservationFilterCriteria(any())).thenCallRealMethod();
    when(reservationRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class))).thenReturn(reservations);

    Page<Reservation> returnedReservations = reservationServiceImplementation.getReservationsByUserId(userId, pageParams);

    assertEquals(reservations, returnedReservations);
    verify(reservationRepository, times(1)).findAllAndFilter(any(), any(), any(), any(), any(), any(), any(), any(), any(),
        any(), any(Pageable.class));
  }

  @Test
  void getReservationsByUserIdThrowsDataAccessException() {
    when(reservationRepository.findByUserId(any(), any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.getReservationsByUserId("testUserId", new HashMap<>()));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getReservationsByUserIdThrowsInternalServerError() {
    when(reservationRepository.findByUserId(any(), any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.getReservationsByUserId("testUserId", new HashMap<>()));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // --------------------- Tests for cancelReservation ---------------------

  @Test
  void cancelReservationSuccessCase() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

    reservationServiceImplementation.cancelReservation(reservationId);

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void cancelReservationThrowsBadRequestWhenReservationDoesNotExist() {
    Long reservationId = 1L;

    when(reservationRepository.findById(any())).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelReservation(reservationId));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(CANCEL_NOT_FOUND_MESSAGE, "reservation"), exception.getReason());
  }

  @Test
  void cancelReservationWithInvoiceSuccessCase() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);
    Invoice invoice = invoiceFactory.generateRandomInvoice(1L, "INV-0001", true);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    invoice.setReservation(reservation);
    invoice.setUser(user);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(invoiceRepository.findByReservationId(any())).thenReturn(Optional.of(invoice));

    reservationServiceImplementation.cancelReservation(reservationId);

    verify(reservationRepository, times(1)).save(any());
    // verify(smsService, times(1)).sendSMS(any(), any());
    verify(invoiceService, times(1)).updateInvoice(any(), any());
  }

  @Test
  void cancelReservationThrowsDataAccessException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelReservation(reservationId));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void cancelReservationThrowsInternalServerErrorForRuntimeException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelReservation(reservationId));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // --------------------- Tests for cancelMultipleReservations ---------------------

  @Test
  void cancelMultipleReservationsCancelsReservations() {
    List<Long> ids = List.of(1L, 2L);
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);

    when(reservationRepository.existsById(any())).thenReturn(true);
    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1), Optional.of(reservation2));

    reservationServiceImplementation.cancelMultipleReservations(ids);

    verify(reservationRepository, times(2)).save(any());
  }

  @Test
  void cancelMultipleReservationsThrowsBadRequestWhenReservationDoesNotExist() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenReturn(false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelMultipleReservations(ids));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "cancel", "reservations", "1, 2"), exception.getReason());
  }

  @Test
  void cancelMultipleReservationsThrowsDataAccessException() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelMultipleReservations(ids));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void cancelMultipleReservationsThrowsInternalServerErrorForRuntimeException() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.cancelMultipleReservations(ids));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // --------------------- Tests for restoreReservation ---------------------
  @Test
  void restoreReservationSuccessCase() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

    reservationServiceImplementation.restoreReservation(reservationId, "Booked");

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void restoreReservationWhitInvoiceSuccessCase() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);
    Invoice invoice = invoiceFactory.generateRandomInvoice(1L, "INV-0001", true);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    invoice.setReservation(reservation);
    invoice.setUser(user);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(invoiceRepository.findByReservationId(any())).thenReturn(Optional.of(invoice));

    reservationServiceImplementation.restoreReservation(reservationId, "Booked");

    verify(reservationRepository, times(1)).save(any());
    verify(invoiceService, times(1)).updateInvoice(any(), any());
  }

  @Test
  void restoreReservationThrowsBadRequestWhenReservationDoesNotExist() {
    Long reservationId = 1L;

    when(reservationRepository.findById(any())).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreReservation(reservationId, "Booked"));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, "restore", "reservation"), exception.getReason());
  }

  @Test
  void restoreReservationThrowsDataAccessException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreReservation(reservationId, "Booked"));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void restoreReservationThrowsInternalServerErrorForRuntimeException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreReservation(reservationId, "Booked"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // --------------------- Tests for restoreMultipleReservations ---------------------

  @Test
  void restoreMultipleReservationsRestoresReservations() {
    List<Long> ids = List.of(1L, 2L);
    Reservation reservation1 = reservationFactory.generateRandomReservation(1L, false, false);
    Reservation reservation2 = reservationFactory.generateRandomReservation(2L, false, false);

    when(reservationRepository.existsById(any())).thenReturn(true);
    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation1), Optional.of(reservation2));

    reservationServiceImplementation.restoreMultipleReservations(ids);

    verify(reservationRepository, times(2)).save(any());
  }

  @Test
  void restoreMultipleReservationsThrowsBadRequestWhenReservationDoesNotExist() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenReturn(false);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreMultipleReservations(ids));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "restore", "reservations", "1, 2"), exception.getReason());
  }

  @Test
  void restoreMultipleReservationsThrowsDataAccessException() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreMultipleReservations(ids));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void restoreMultipleReservationsThrowsInternalServerErrorForRuntimeException() {
    List<Long> ids = List.of(1L, 2L);
    when(reservationRepository.existsById(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.restoreMultipleReservations(ids));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // --------------------- Tests for updateReservationStatus ---------------------

  @Test
  void updateReservationStatusCaseCancel() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

    reservationServiceImplementation.updateReservationStatus(reservationId, "cancel");

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationStatusCaseRestoreToBooked() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

    reservationServiceImplementation.updateReservationStatus(reservationId, "Restore to Booked");

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationStatusCaseRestoreToPending() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

    reservationServiceImplementation.updateReservationStatus(reservationId, "Restore to Pending");

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationStatusCaseMarkAsDone() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);
    User user = userFactory.generateRandomUser(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setUserId(user.getUserId());

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(userService.getUserById(any())).thenReturn(user);

    reservationServiceImplementation.updateReservationStatus(reservationId, "Mark as Done");

    verify(reservationRepository, times(1)).save(any());
  }

  @Test
  void updateReservationStatusThrowsBadRequestWhenReservationDoesNotExist() {
    Long reservationId = 1L;

    when(reservationRepository.findById(any())).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservationStatus(reservationId, "Confirm"));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, "update status for", "reservation"), exception.getReason());
  }

  @Test
  void updateReservationStatusThrowsDataAccessException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new DataAccessException("Data access exception encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservationStatus(reservationId, "Confirm"));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void updateReservationStatusThrowsInternalServerErrorForRuntimeException() {
    Long reservationId = 1L;
    Reservation reservation = reservationFactory.generateRandomReservation(reservationId, false, false);

    when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
    when(reservationRepository.save(any())).thenThrow(new RuntimeException("Unexpected runtime error encountered.") {
    });

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservationStatus(reservationId, "Approve"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void updateReservationStatusThrowsBadRequestWhenReservationStatusIsInvalid() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> reservationServiceImplementation.updateReservationStatus(1L, "Invalid Status"));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("Invalid status provided.", exception.getReason());
  }
}
