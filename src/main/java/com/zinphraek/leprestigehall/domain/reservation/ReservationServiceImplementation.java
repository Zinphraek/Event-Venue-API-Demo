package com.zinphraek.leprestigehall.domain.reservation;

import com.twilio.exception.TwilioException;
import com.zinphraek.leprestigehall.domain.addon.AddOnRepository;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOnRepository;
import com.zinphraek.leprestigehall.domain.email.EmailServiceImplementation;
import com.zinphraek.leprestigehall.domain.email.Mail;
import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.domain.invoice.InvoiceRepository;
import com.zinphraek.leprestigehall.domain.invoice.InvoiceService;
import com.zinphraek.leprestigehall.domain.sms.SMSServiceImplementation;
import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserServiceImpl;
import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import com.zinphraek.leprestigehall.utilities.helpers.ReservationServiceHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.buildPageRequestFromCustomPage;

/**
 * Implementing the reservation service class.
 */
@Service
public class ReservationServiceImplementation implements ReservationService {

  private final Logger logger = LogManager.getLogger(ReservationServiceImplementation.class);

  @Autowired
  private final ReservationServiceHelper serviceHelper;

  @Autowired
  private final ReservationRepository reservationRepository;

  @Autowired
  private final EmailServiceImplementation emailService;

  @Autowired
  private final SMSServiceImplementation smsService;

  @Autowired
  private final UserServiceImpl userService;

  @Autowired
  private final AddOnRepository addOnRepository;

  @Autowired
  private final InvoiceRepository invoiceRepository;

  @Autowired
  private final InvoiceService invoiceService;

  @Autowired
  private final RequestedAddOnRepository requestedAddOnRepository;

  @Autowired
  private final ReservationDiscountRepo reservationDiscountRepo;

  @Autowired
  private final ReservationRateRepo reservationRateRepo;

  @Autowired
  public ReservationServiceImplementation(
      ReservationServiceHelper serviceHelper,
      ReservationRepository reservationRepository,
      EmailServiceImplementation emailService,
      SMSServiceImplementation smsService, UserServiceImpl userService,
      AddOnRepository addOnRepository,
      InvoiceRepository invoiceRepository,
      InvoiceService invoiceService,
      RequestedAddOnRepository requestedAddOnRepository, ReservationDiscountRepo reservationDiscountRepo, ReservationRateRepo reservationRateRepo) {
    this.serviceHelper = serviceHelper;
    this.reservationRepository = reservationRepository;
    this.emailService = emailService;
    this.smsService = smsService;
    this.userService = userService;
    this.addOnRepository = addOnRepository;
    this.invoiceRepository = invoiceRepository;
    this.invoiceService = invoiceService;
    this.requestedAddOnRepository = requestedAddOnRepository;
    this.reservationDiscountRepo = reservationDiscountRepo;
    this.reservationRateRepo = reservationRateRepo;
  }

  /**
   * Check whether a specific date time interval is available for booking.
   *
   * @param dateTime1 - Starting date-time value.
   * @param dateTime2 - Ending date-time value.
   * @param dateTime3 - Effective ending date-time value.
   * @param id        - The id of the targeted reservation (0 if the action is a creation).
   */
  private void checkDateTimeIntervalAvailability(LocalDateTime dateTime1, LocalDateTime dateTime2, LocalDateTime dateTime3, long id) {
    LocalDateTime checkStart = dateTime1.minusHours(2L);
    LocalDateTime checkEnd = dateTime2.plusHours(2L);
    LocalDateTime checkEffectiveEnd = dateTime3 != null ? dateTime3.plusHours(2L) : null;

    if (id != 0) {
      if (dateTime3 != null) {
        if (reservationRepository.existsByStartingDateTimeBetweenAndStatusNotAndIdNot(
            checkStart, checkEffectiveEnd, STATUS_CANCELLED, id)
            || reservationRepository.existsByEffectiveEndingDateTimeBetweenAndStatusNotAndIdNot(
            checkStart, checkEffectiveEnd, STATUS_CANCELLED, id)) {
          logger.error(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
          throw new ResponseStatusException(HttpStatus.CONFLICT, UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
        }
      } else {
        if (reservationRepository.existsByStartingDateTimeBetweenAndStatusNotAndIdNot(
            checkStart, checkEnd, STATUS_CANCELLED, id)
            || reservationRepository.existsByEndingDateTimeBetweenAndStatusNotAndIdNot(
            checkStart, checkEnd, STATUS_CANCELLED, id)) {
          logger.error(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
          throw new ResponseStatusException(HttpStatus.CONFLICT, UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
        }
      }
    } else {
      if (reservationRepository.existsByStartingDateTimeBetweenAndStatusNot(checkStart, checkEnd, STATUS_CANCELLED)
          || reservationRepository.existsByEndingDateTimeBetweenAndStatusNot(checkStart, checkEnd, STATUS_CANCELLED)) {
        logger.error(UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
        throw new ResponseStatusException(HttpStatus.CONFLICT, UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE);
      }
    }
  }

  /**
   * Check whether a provided date is available to book when creating and updating reservations.
   *
   * @param targetStartingDateTime        - The date and time when the reservation starts.
   * @param targetEndingDateTime          - The date and time when the reservation ends.
   * @param targetEffectiveEndingDateTime - The date and time when the reservation effectively ends.
   * @param requestType                   - The scenario in which the action occur (either "create" or "update")
   * @param id                            - The id of the targeted reservation (0 if the action is a creation).
   * @param dateTimes                     - The previously selected date time interval for the targeted reservation
   *                                      (Only apply in the "update" scenario).
   */
  private void checkDateTimeAvailability(
      LocalDateTime targetStartingDateTime,
      LocalDateTime targetEndingDateTime,
      LocalDateTime targetEffectiveEndingDateTime,
      String requestType,
      long id, LocalDateTime... dateTimes) {
    LocalDateTime today = LocalDateTime.now();

    if (targetStartingDateTime.isBefore(today)) {
      logger.error(RESERVATION_START_IN_THE_PAST_ERROR_MESSAGE);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, RESERVATION_START_IN_THE_PAST_ERROR_MESSAGE);
    }

    if (targetEndingDateTime.isBefore(targetStartingDateTime)) {
      logger.error(String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "ending"));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "ending"));
    }

    if (targetEffectiveEndingDateTime != null && targetEffectiveEndingDateTime.isBefore(targetStartingDateTime)) {
      logger.error(String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "effective ending"));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(RESERVATION_END_BEFORE_START_ERROR_MESSAGE, "effective ending"));

    }

    switch (requestType) {
      case "create" -> checkDateTimeIntervalAvailability(
          targetStartingDateTime, targetEndingDateTime, null, id);
      case "update" -> {
        if ((targetEffectiveEndingDateTime != null && dateTimes[2] != null && !targetEffectiveEndingDateTime.isEqual(dateTimes[2]))
            || (targetEffectiveEndingDateTime != null && dateTimes[2] == null && !targetEffectiveEndingDateTime.isEqual(dateTimes[1]))
            || !targetStartingDateTime.isEqual(dateTimes[0]) || !targetEndingDateTime.isEqual(dateTimes[1])) {
          checkDateTimeIntervalAvailability(targetStartingDateTime, targetEndingDateTime, targetEffectiveEndingDateTime, id);
        }
      }
    }
  }

  /**
   * Persist all selected AddOns provided by the user.
   *
   * @param newReservation The reservation to manage.
   */
  private void persistRequestedAddOn(Reservation newReservation) {
    Collection<RequestedAddOn> requestedAddOns = newReservation.getAddOns();

    if (requestedAddOns != null) {
      requestedAddOns.forEach(
          requestedAddOn -> {
            requestedAddOn.setReservation(newReservation);
            requestedAddOnRepository.save(requestedAddOn);
            logger.info(String.format(CREATE_SUCCESS_MESSAGE, "Requested AddOn"));
          });
    }
  }

  /**
   * Checking that each AddOn provided exists in the database.
   *
   * @param requestedAddOns The collection of chosen AddOns.
   */
  private void checkIfChosenAddOnExists(Collection<RequestedAddOn> requestedAddOns) {
    if (requestedAddOns != null && !requestedAddOns.isEmpty()) {
      requestedAddOns.forEach(
          requestedAddOn -> {
            if (!addOnRepository.existsById(requestedAddOn.getAddOn().getId())) {
              logger.error(String.format(RESERVATION_WITH_NON_EXISTENT_ADD_ON_ERROR_MESSAGE,
                  requestedAddOn.getAddOn().getId()));
              throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST, String.format(RESERVATION_WITH_NON_EXISTENT_ADD_ON_ERROR_MESSAGE,
                  requestedAddOn.getAddOn().getId()));
            }
          });
    }
  }

  /**
   * Persist the rate associated to a reservation if nonexistent.
   *
   * @param rate The reservation rate to manage.
   */
  private void saveRateIfNotExistent(ReservationRate rate) {
    if (rate != null) {
      if (!reservationRateRepo.existsBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
          rate.getSeatRate(), rate.getCleaningRate(), rate.getFacilityRate(), rate.getOvertimeRate())) {
        reservationRateRepo.save(rate);
      } else {
        Optional<ReservationRate> existingRate = reservationRateRepo.findBySeatRateAndCleaningRateAndFacilityRateAndOvertimeRate(
            rate.getSeatRate(), rate.getCleaningRate(), rate.getFacilityRate(), rate.getOvertimeRate());
        existingRate.ifPresent(reservationRate -> rate.setId(reservationRate.getId()));
      }
    }
  }

  /**
   * Persist the discount associated to a reservation if nonexistent.
   *
   * @param discount The reservation discount to manage.
   */
  private void saveDiscountIfNotExistent(ReservationDiscount discount) {
    if (discount != null) {
      if (!reservationDiscountRepo.existsByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
          discount.getPercentage(), discount.getAmount(), discount.getName(), discount.getCode(), discount.getType(),
          discount.getAvailable(), discount.getDescription())) {
        reservationDiscountRepo.save(discount);
      } else {
        Optional<ReservationDiscount> existingDiscount = reservationDiscountRepo.findByPercentageAndAmountAndNameAndCodeAndTypeAndIsAvailableAndDescription(
            discount.getPercentage(), discount.getAmount(), discount.getName(), discount.getCode(), discount.getType(),
            discount.getAvailable(), discount.getDescription());
        existingDiscount.ifPresent(reservationDiscount -> discount.setId(reservationDiscount.getId()));
      }
    }
  }

  /**
   * Fetch a specific reservation from the database.
   *
   * @param reservationFilterCriteria - The sorting and filtering options
   * @param pageable                  - The pagination options
   * @return - The targeted reservations.
   */
  private Page<Reservation> retrieveReservations(ReservationFilterCriteria reservationFilterCriteria, Pageable pageable) {
    return reservationRepository.findAllAndFilter(
        reservationFilterCriteria.getStartedBefore(),
        reservationFilterCriteria.getStartedAfter(),
        reservationFilterCriteria.getEventType(),
        reservationFilterCriteria.isFullPackage(),
        reservationFilterCriteria.getStatus(),
        reservationFilterCriteria.isSecurityDepositRefunded(),
        reservationFilterCriteria.getMinTotalPrice(),
        reservationFilterCriteria.getMaxTotalPrice(),
        reservationFilterCriteria.getUserId(),
        reservationFilterCriteria.getPriceComputationMethod(),
        pageable);
  }

  /**
   * Fetch all reservations from the database.
   *
   * @param params The sorting and filtering options
   * @return - A list of reservations
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  public Page<Reservation> getReservations(Map<String, String> params) {

    logger.info("Fetching reservations...");
    Page<Reservation> reservations;
    Pageable pageable = Pageable.unpaged();
    Pair<CustomPage, ReservationFilterCriteria> pageReservationFilterCriteriaPair =
        Pair.of(new CustomPage(), new ReservationFilterCriteria());

    try {
      if (!params.isEmpty()) {
        pageReservationFilterCriteriaPair =
            serviceHelper.generateCustomPageAndReservationFilterCriteria(params);

        pageable = buildPageRequestFromCustomPage(pageReservationFilterCriteriaPair.getFirst());
      }
      reservations = retrieveReservations(pageReservationFilterCriteriaPair.getSecond(), pageable);

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Reservations"));
    return reservations;
  }

  /**
   * Fetch all reservations associated to a single user.
   *
   * @param userId - The id of the target user.
   * @return - A list of reservations
   */
  @Override
  public Page<Reservation> getReservationsByUserId(String userId, Map<String, String> params) {

    logger.info("Fetching reservations associated to the user id: " + userId + "...");
    Page<Reservation> reservations;

    try {
      if (params.isEmpty()) {
        reservations = reservationRepository.findByUserId(userId, Pageable.unpaged());
      } else {

        // Setting up the customPage entity and the reservationFilterCriteria with the corresponding
        // value in the param object.
        Pair<CustomPage, ReservationFilterCriteria> pageReservationFilterCriteriaPair =
            serviceHelper.generateCustomPageAndReservationFilterCriteria(params);
        CustomPage customPage = pageReservationFilterCriteriaPair.getFirst();
        ReservationFilterCriteria reservationFilterCriteria = pageReservationFilterCriteriaPair.getSecond();

        // Setting the userId filter criteria to the provided user id.
        reservationFilterCriteria.setUserId(userId);

        reservations = retrieveReservations(reservationFilterCriteria, buildPageRequestFromCustomPage(customPage));
      }
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info("Reservations associated to the user id: " + userId + " successfully fetched.");
    return reservations;
  }

  /**
   * Persists a newly created reservation
   *
   * @param newReservation - The reservation to persist.
   * @return - The newly created reservation.
   */
  @Override
  public Reservation createReservation(Reservation newReservation) {
    try {
      logger.info("Creating reservation...");
      if (newReservation.getId() != null
          && reservationRepository.existsById(newReservation.getId())) {
        logger.error(String.format(CREATE_CONFLICT_MESSAGE1, "reservation", newReservation.getId()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(CREATE_CONFLICT_MESSAGE1, "reservation", newReservation.getId()));
      }

      checkDateTimeAvailability(
          newReservation.getStartingDateTime(), newReservation.getEndingDateTime(),
          newReservation.getEffectiveEndingDateTime(), "create", 0);

      User user;
      Invoice invoice = new Invoice();

      if (newReservation.getUserId() == null || newReservation.getUserId().isBlank()) {
        logger.error(String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"));
      }
      user = userService.getUserById(newReservation.getUserId());

      checkIfChosenAddOnExists(newReservation.getAddOns());

      // Computing the total price.
      serviceHelper.computeTotalPrice(
          newReservation, addOnName -> addOnRepository.findByName(addOnName).orElse(null));

      // Persisting the rates and discount associated to the reservation.
      saveRateIfNotExistent(newReservation.getRates());
      saveDiscountIfNotExistent(newReservation.getDiscount());

      reservationRepository.save(newReservation);
      persistRequestedAddOn(newReservation);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "Reservation"));

      // Create invoice
      invoice.setUser(user);
      invoice.setStatus(STATUS_DUE);
      invoice.setReservation(newReservation);
      invoice.setAmountDue(newReservation.getTotalPrice());
      invoice.setInvoiceNumber("INV-000" + newReservation.getId());
      invoice.setDueDate(serviceHelper.computeDueDate(newReservation.getStartingDateTime()));
      invoice.setIssuedDate(
          LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));

      invoiceRepository.save(invoice);

      // Sending confirmation email.
      Mail mail =
          serviceHelper.getMail(
              invoice,
              RESERVATION_EMAIL_TEMPLATE,
              NO_REPLY_EMAIL_ADDRESS,
              RESERVATION_CONFIRMATION_SUBJECT,
              true);
      logger.info("Sending reservation booking confirmation email...");
      emailService.sendEmail(mail);

      // Notifying admin via SMS.
      String customerName = user.getFirstName() + " " + user.getLastName();
      String message = String.format(RESERVATION_CONFIRMATION_SMS,
          newReservation.getStartingDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)), newReservation.getId(),
          customerName, user.getEmail()
      );

      logger.info("Notifying admin via SMS...");
      // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);

    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(e.getMessage());
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.", e);
    } catch (TwilioException e) {
      logger.error("Couldn't send SMS.", e);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newReservation;
  }

  /**
   * Update a reservation.
   *
   * @param id             - The id of the targeted reservation
   * @param newReservation - The new reservation
   * @return - The new reservation.
   */
  @Override
  public Reservation updateReservation(Long id, Reservation newReservation) {
    try {
      Optional<Reservation> prevReservation;

      prevReservation = reservationRepository.findById(id);

      if (prevReservation.isPresent()) {
        checkDateTimeAvailability(
            newReservation.getStartingDateTime(),
            newReservation.getEndingDateTime(),
            newReservation.getEffectiveEndingDateTime(),
            "update",
            id, prevReservation.get().getStartingDateTime(),
            prevReservation.get().getEndingDateTime(), prevReservation.get().getEffectiveEndingDateTime());
      } else {
        logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "reservation"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(UPDATE_NOT_FOUND_MESSAGE, "reservation"));
      }

      if (!Objects.equals(prevReservation.get().getUserId(), newReservation.getUserId())) {
        logger.error(String.format(UPDATE_NON_MATCHING_FIELD_MESSAGE, "user's id", "reservation",
            prevReservation.get().getUserId(), newReservation.getUserId()));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(FIELD_MISMATCH_ERROR_MESSAGE, "user's id"));
      }

      checkIfChosenAddOnExists(newReservation.getAddOns());

      // Computing the total price.
      serviceHelper.computeTotalPrice(
          newReservation, addOnName -> addOnRepository.findByName(addOnName).orElse(null));


      LocalDateTime prevStartingDateTime = prevReservation.get().getStartingDateTime();

      // Persisting the rates and discount associated to the reservation.
      saveRateIfNotExistent(newReservation.getRates());
      saveDiscountIfNotExistent(newReservation.getDiscount());

      logger.info("Updating reservation...");
      reservationRepository.save(newReservation);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Reservation", id));

      if (prevReservation.get().getAddOns() != newReservation.getAddOns()) {
        persistRequestedAddOn(newReservation);
      }

      // Updating the associated invoice and sending a confirmation email.

      Optional<Invoice> invoice = invoiceRepository.findByReservationId(id);
      User user;
      if (invoice.isPresent()) {
        user = invoice.get().getUser();
        invoiceService.updateInvoice(invoice.get().getId(), invoice.get());
        Mail mail =
            serviceHelper.getMail(
                invoice.get(),
                RESERVATION_EMAIL_TEMPLATE,
                NO_REPLY_EMAIL_ADDRESS,
                RESERVATION_CONFIRMATION_SUBJECT,
                true);
        logger.info("Sending reservation update confirmation email...");
        emailService.sendEmail(mail);

        // Notifying admin via SMS.
        String customerName = user.getFirstName() + " " + user.getLastName();
        String message = String.format(RESERVATION_UPDATE_SMS, newReservation.getId(),
            prevStartingDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            newReservation.getStartingDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
            customerName, user.getEmail());

        logger.info("Notifying admin via SMS...");
        // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);
      }

    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.", e);
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (TwilioException e) {
      logger.error("Couldn't send SMS.", e);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newReservation;
  }

  /**
   * Mark a specific reservation as cancel.
   *
   * @param reservationId The id of the reservation to cancel.
   */
  @Override
  public void cancelReservation(Long reservationId) {
    try {
      Optional<Reservation> reservation;
      reservation = reservationRepository.findById(reservationId);
      if (reservation.isPresent()) {
        reservation.get().setStatus(STATUS_CANCELLED);
        logger.info("Cancelling reservation...");
        reservationRepository.save(reservation.get());
        logger.info(String.format(GENERIC_ACTION_SUCCESS_MESSAGE, "Reservation", reservationId, "cancelled"));

        // Updating the associated invoice and sending a confirmation email.

        Optional<Invoice> invoice = invoiceRepository.findByReservationId(reservationId);
        if (invoice.isPresent()) {
          invoice.get().setStatus(STATUS_WITHDRAWN);
          logger.info("Updating invoice...");
          invoiceService.updateInvoice(invoice.get().getId(), invoice.get());
          logger.info(String.format(GENERIC_ACTION_SUCCESS_MESSAGE, "Invoice", invoice.get().getId(), "updated"));
          Mail mail =
              serviceHelper.getMail(
                  invoice.get(),
                  RESERVATION_CANCELLATION_EMAIL_TEMPLATE,
                  NO_REPLY_EMAIL_ADDRESS,
                  RESERVATION_CANCELLATION_SUBJECT,
                  false);
          logger.info("Sending reservation cancellation confirmation email...");
          emailService.sendEmail(mail);

          // Notifying admin via SMS.
          String customerName = invoice.get().getUser().getFirstName() + " " + invoice.get().getUser().getLastName();
          String message = String.format(RESERVATION_CANCELLATION_SMS, reservationId, reservation.get().getStartingDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)),
              customerName, invoice.get().getUser().getEmail());

          logger.info("Notifying admin via SMS...");
          // smsService.sendSMS(DEFAULT_ADMIN_PHONE_NUMBER, message);
        }
      } else {
        logger.error(String.format(CANCEL_NOT_FOUND_MESSAGE, "reservation"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(CANCEL_NOT_FOUND_MESSAGE, "reservation"));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (TwilioException e) {
      logger.error("Couldn't send SMS.", e);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Mark all reservations which id was provided as cancel.
   *
   * @param ids The list of ids of the reservations to cancel.
   */
  @Override
  public void cancelMultipleReservations(List<Long> ids) {
    List<Long> existentReservations = new ArrayList<>();
    StringBuilder nonexistentReservations = new StringBuilder();

    try {
      ids.forEach(
          id -> {
            if (reservationRepository.existsById(id)) {
              existentReservations.add(id);
            } else {
              nonexistentReservations.append(id).append(", ");
            }
          });

      if (!existentReservations.isEmpty()) {
        existentReservations.parallelStream().forEach(this::cancelReservation);
      }

      if (!nonexistentReservations.isEmpty()) {
        String errorMessageIds =
            Arrays.stream(nonexistentReservations.toString().split(", "))
                .sorted()
                .collect(Collectors.joining(", "));
        logger.error(String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "cancel", "reservations", errorMessageIds));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "cancel", "reservations", errorMessageIds));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Restore a previously cancelled reservation.
   *
   * @param reservationId The id of the reservation to restore.
   * @param status        The status to set.
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  public void restoreReservation(Long reservationId, String status) {

    Optional<Reservation> reservation;
    try {
      reservation = reservationRepository.findById(reservationId);
      if (reservation.isPresent()) {

        reservation.get().setStatus(status);
        reservationRepository.save(reservation.get());
        logger.info(String.format(GENERIC_ACTION_SUCCESS_MESSAGE, "Reservation", reservationId, "restored"));

        // Updating the associated invoice and sending a confirmation email.

        Optional<Invoice> invoice = invoiceRepository.findByReservationId(reservationId);
        if (invoice.isPresent()) {
          invoice.get().setStatus(STATUS_DUE);
          invoiceService.updateInvoice(invoice.get().getId(), invoice.get());
          Mail mail =
              serviceHelper.getMail(
                  invoice.get(),
                  RESERVATION_EMAIL_TEMPLATE,
                  NO_REPLY_EMAIL_ADDRESS,
                  RESERVATION_CONFIRMATION_SUBJECT,
                  true);
          logger.info("Sending reservation update confirmation email...");
          emailService.sendEmail(mail);
        }
      } else {
        logger.error(String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, "restore", "reservation"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, "restore", "reservation"));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Restore all previously cancelled reservations which id is provided.
   *
   * @param ids The list of ids of the reservations to restore.
   */
  @Override
  public void restoreMultipleReservations(List<Long> ids) {

    List<Long> existentReservations = new ArrayList<>();
    StringBuilder nonexistentReservations = new StringBuilder();

    try {
      ids.forEach(
          id -> {
            if (reservationRepository.existsById(id)) {
              existentReservations.add(id);
            } else {
              nonexistentReservations.append(id).append(", ");
            }
          });

      if (!existentReservations.isEmpty()) {
        existentReservations.parallelStream().forEach(reservationId -> restoreReservation(reservationId, STATUS_BOOKED));
      }

      if (!nonexistentReservations.isEmpty()) {
        String errorMessageIds =
            Arrays.stream(nonexistentReservations.toString().split(", "))
                .sorted()
                .collect(Collectors.joining(", "));
        logger.error(String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "restore", "reservations", errorMessageIds));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(MASS_ACTION_NOT_FOUND_MESSAGE, "restore", "reservations", errorMessageIds));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Generic method to update the status of a reservation.
   *
   * @param id     The id of the reservation to update.
   * @param status The status to set.
   */
  private void genericReservationStatusUpdate(Long id, String status) {
    Optional<Reservation> reservation;
    try {
      if (status == null || !RESERVATION_STATUSES_UPDATE_ACTIONS.containsValue(status)) {
        logger.error("Invalid status provided.");
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid status provided.");
      }
      reservation = reservationRepository.findById(id);
      if (reservation.isPresent()) {
        reservation.get().setStatus(status);
        reservationRepository.save(reservation.get());
        logger.info(String.format(GENERIC_ACTION_SUCCESS_MESSAGE, "Reservation", id, "updated to " + status));

        User user = userService.getUserById(reservation.get().getUserId());
        String reservationDate = reservation.get().getStartingDateTime().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));

        // Sending confirmation email.
        Mail mail = serviceHelper.getGenericMail(GENERIC_EMAIL_TEMPLATE, NO_REPLY_EMAIL_ADDRESS, user.getEmail(),
            String.format(RESERVATION_STATUSES_CHANGE_EMAIL_SUBJECTS.get(status)),
            String.format(RESERVATION_STATUSES_CHANGE_EMAIL_BODIES.get(status), reservationDate));

        logger.info("Sending reservation status change confirmation email...");
        emailService.sendEmail(mail);
      } else {
        logger.error(String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, status, "reservation"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(GENERIC_ACTION_NOT_FOUND_MESSAGE, "update status for", "reservation"));
      }
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (MailException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param reservationId The id of the reservation to update.
   * @param action        The status update action to perform.
   */
  @Override
  public void updateReservationStatus(Long reservationId, String action) {

    switch (action) {
      case "cancel" -> cancelReservation(reservationId);
      case "Restore to Booked" -> restoreReservation(reservationId, STATUS_BOOKED);
      case "Restore to Pending" -> restoreReservation(reservationId, STATUS_PENDING);
      default -> genericReservationStatusUpdate(reservationId, RESERVATION_STATUSES_UPDATE_ACTIONS.get(action));
    }
  }
}
