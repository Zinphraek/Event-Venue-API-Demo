package com.zinphraek.leprestigehall.domain.invoice;

import com.zinphraek.leprestigehall.domain.reservation.ReservationRepository;
import com.zinphraek.leprestigehall.domain.user.UserRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class InvoiceServiceImplementation implements InvoiceService {

  private final Logger logger = LogManager.getLogger(InvoiceServiceImplementation.class);

  @Autowired
  private final ReservationRepository reservationRepository;

  @Autowired
  private final InvoiceRepository invoiceRepository;

  @Autowired
  private final UserRepository userRepository;

  public InvoiceServiceImplementation(
      ReservationRepository reservationRepository,
      InvoiceRepository invoiceRepository,
      UserRepository userRepository) {
    this.reservationRepository = reservationRepository;
    this.invoiceRepository = invoiceRepository;
    this.userRepository = userRepository;
  }

  private void checkIfDueDateIsOneWeekPriorToReservationDateAndUpdateStatus(Invoice newInvoice) {

    LocalDateTime now = LocalDateTime.now();
    if (newInvoice.getReservation().getStartingDateTime().minusWeeks(1).isBefore(now)
        && !Objects.equals(newInvoice.getStatus(), STATUS_PAID)) {
      logger.info("Invoice is due immediately. Updating due date to now.");
      newInvoice.setDueDate(now.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
      newInvoice.setStatus(STATUS_DUE);
    }
  }

  private Pair<CustomPage, InvoiceFilterCriteria> generateCustomPageAndInvoiceFilterCriteria(
      Map<String, String> params) {
    CustomPage customPage = createCustomPageFromParams(params);
    InvoiceFilterCriteria invoiceFilterCriteria = new InvoiceFilterCriteria();


    // Setting up the invoiceFilterCriteria entity with the corresponding value in the param object.
    if (params.containsKey("invoiceNumber")) {
      invoiceFilterCriteria.setInvoiceNumber(params.get("invoiceNumber"));
    }

    if (params.containsKey("beforeIssuedDate")) {
      invoiceFilterCriteria.setBeforeIssuedDate(params.get("beforeIssuedDate"));
    }

    if (params.containsKey("afterIssuedDate")) {
      invoiceFilterCriteria.setAfterIssuedDate(params.get("afterIssuedDate"));
    }

    if (params.containsKey("beforeDueDate")) {
      invoiceFilterCriteria.setBeforeDueDate(params.get("beforeDueDate"));
    }

    if (params.containsKey("afterDueDate")) {
      invoiceFilterCriteria.setAfterDueDate(params.get("afterDueDate"));
    }

    if (params.containsKey("greaterThanAmountPaid")) {
      invoiceFilterCriteria.setGreaterThanTotalAmountPaid(
          Double.parseDouble(params.get("greaterThanAmountPaid")));
    }

    if (params.containsKey("lessThanAmountPaid")) {
      invoiceFilterCriteria.setLessThanTotalAmountPaid(
          Double.parseDouble(params.get("lessThanAmountPaid")));
    }

    if (params.containsKey("greaterThanAmountDue")) {
      invoiceFilterCriteria.setGreaterThanAmountDue(
          Double.parseDouble(params.get("greaterThanAmountDue")));
    }

    if (params.containsKey("lessThanAmountDue")) {
      invoiceFilterCriteria.setLessThanAmountDue(
          Double.parseDouble(params.get("lessThanAmountDue")));
    }

    if (params.containsKey("status")) {
      invoiceFilterCriteria.setStatus(params.get("status"));
    }

    if (params.containsKey("reservationId")) {
      invoiceFilterCriteria.setReservationId(Long.parseLong(params.get("reservationId")));
    }

    if (params.containsKey("userId")) {
      invoiceFilterCriteria.setUserId(params.get("userId"));
    }

    return Pair.of(customPage, invoiceFilterCriteria);
  }

  /**
   * Retrieves a list of invoices based on the query parameters.
   *
   * @param params Map<String, String> object containing the query parameters.
   * @return Page<Invoice> object containing the list of invoices.
   */
  @Override
  public Page<Invoice> getInvoices(Map<String, String> params) {

    logger.info("Fetching invoices...");
    Page<Invoice> invoices;
    Pageable pageable = Pageable.unpaged();
    Pair<CustomPage, InvoiceFilterCriteria> customPageAndInvoiceFilterCriteria =
        Pair.of(new CustomPage(), new InvoiceFilterCriteria());

    try {
      if (!params.isEmpty()) {
        // Extracting the customPage and invoiceFilterCriteria entities from the params object.
        customPageAndInvoiceFilterCriteria =
            generateCustomPageAndInvoiceFilterCriteria(params);

        pageable =
            PageRequest.of(
                customPageAndInvoiceFilterCriteria.getFirst().getPageNumber(),
                customPageAndInvoiceFilterCriteria.getFirst().getPageSize(),
                customPageAndInvoiceFilterCriteria.getFirst().getSortDirection(),
                customPageAndInvoiceFilterCriteria.getFirst().getSortBy());
      }
      invoices = invoiceRepository.findAllAndFilter(
          customPageAndInvoiceFilterCriteria.getSecond().getInvoiceNumber(),
          customPageAndInvoiceFilterCriteria.getSecond().getBeforeIssuedDate(),
          customPageAndInvoiceFilterCriteria.getSecond().getAfterIssuedDate(),
          customPageAndInvoiceFilterCriteria.getSecond().getBeforeDueDate(),
          customPageAndInvoiceFilterCriteria.getSecond().getAfterDueDate(),
          customPageAndInvoiceFilterCriteria.getSecond().getStatus(),
          customPageAndInvoiceFilterCriteria.getSecond().getGreaterThanTotalAmountPaid(),
          customPageAndInvoiceFilterCriteria.getSecond().getLessThanTotalAmountPaid(),
          customPageAndInvoiceFilterCriteria.getSecond().getGreaterThanAmountDue(),
          customPageAndInvoiceFilterCriteria.getSecond().getLessThanAmountDue(),
          customPageAndInvoiceFilterCriteria.getSecond().getReservationId(),
          customPageAndInvoiceFilterCriteria.getSecond().getUserId(),
          pageable);

    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Invoices"));
    return invoices;
  }

  /**
   * Retrieves a list of invoices based on the user id.
   *
   * @param userId The id of the user.
   * @param params Map<String, String> object containing the query parameters.
   * @return Page<Invoice> object containing the list of invoices.
   */
  @Override
  public Page<Invoice> getInvoicesByUserId(String userId, Map<String, String> params) {

    Page<Invoice> invoices;
    Pageable pageable = Pageable.unpaged();
    logger.info("Fetching invoices associated to user with id " + userId + "...");

    try {
      if (!params.isEmpty()) {
        // Extracting the customPage and invoiceFilterCriteria entities from the params object.
        Pair<CustomPage, InvoiceFilterCriteria> customPageAndInvoiceFilterCriteria =
            generateCustomPageAndInvoiceFilterCriteria(params);

        pageable =
            PageRequest.of(
                customPageAndInvoiceFilterCriteria.getFirst().getPageNumber(),
                customPageAndInvoiceFilterCriteria.getFirst().getPageSize(),
                customPageAndInvoiceFilterCriteria.getFirst().getSortDirection(),
                customPageAndInvoiceFilterCriteria.getFirst().getSortBy());
      }
      invoices = invoiceRepository.findByUserId(userId, pageable);
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Invoices"));
    return invoices;
  }

  /**
   * Retrieves a single invoice based on the invoice id.
   *
   * @param invoiceId The id of the invoice to be retrieved.
   * @return Invoice object containing the invoice.
   */
  @Override
  public Invoice getInvoice(Long invoiceId) {
    try {
      Optional<Invoice> invoice = invoiceRepository.findById(invoiceId);
      if (invoice.isPresent()) {
        logger.info(String.format(GET_SUCCESS_MESSAGE, "Invoice", invoiceId));
        return invoice.get();
      }

      logger.info(String.format(GET_NOT_FOUND_MESSAGE, "Invoice", invoiceId));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "Invoice", invoiceId));
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Persists a new invoice to the database.
   *
   * @param newInvoice The invoice to be created.
   * @return Invoice object containing the newly created invoice.
   */
  @Override
  public Invoice createInvoice(Invoice newInvoice) {

    try {
      if (invoiceRepository.existsByInvoiceNumber(newInvoice.getInvoiceNumber())) {
        logger.info(String.format(CREATE_CONFLICT_MESSAGE2, "invoice", newInvoice.getInvoiceNumber()));
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            String.format(CREATE_CONFLICT_MESSAGE2, "invoice", newInvoice.getInvoiceNumber()));
      }

      if (invoiceRepository.existsByReservationId(newInvoice.getReservation().getId())) {
        logger.info(
            String.format(
                FIELD_CONFLICT_MESSAGE2, "invoice", "reservation id", newInvoice.getReservation().getId()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, String.format(FIELD_CONFLICT_MESSAGE2, "invoice", "reservation id", newInvoice.getReservation().getId()));
      }

      if (!reservationRepository.existsById(newInvoice.getReservation().getId())) {
        logger.info("Invoice must be associated with a reservation");
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invoice must be associated with a reservation");
      }

      if (!userRepository.existsByUserId(newInvoice.getUser().getUserId())) {
        logger.info("Invoice must be associated with a user");
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invoice must be associated with a user");
      }

      checkIfDueDateIsOneWeekPriorToReservationDateAndUpdateStatus(newInvoice);

      Invoice invoice = invoiceRepository.save(newInvoice);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "Invoice"));
      return invoice;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Updates an existing invoice in the database.
   *
   * @param invoiceId  The id of the invoice to be updated.
   * @param newInvoice The invoice containing the updated info.
   * @return Invoice object containing the updated invoice.
   */
  @Override
  public Invoice updateInvoice(Long invoiceId, Invoice newInvoice) {

    try {
      if (!Objects.equals(invoiceId, newInvoice.getId())) {
        logger.info(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "invoice"));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "invoice"));
      }

      if (!invoiceRepository.existsById(invoiceId)) {
        logger.info(String.format(UPDATE_NOT_FOUND_MESSAGE, "invoice"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(UPDATE_NOT_FOUND_MESSAGE, "invoice"));
      }

      if (!reservationRepository.existsById(newInvoice.getReservation().getId())) {
        logger.info("Invoice must be associated with a reservation");
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invoice must be associated with a reservation");
      }

      if (!userRepository.existsByUserId(newInvoice.getUser().getUserId())) {
        logger.info("Invoice must be associated with a user");
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invoice must be associated with a user");
      }

      checkIfDueDateIsOneWeekPriorToReservationDateAndUpdateStatus(newInvoice);

      Invoice invoice = invoiceRepository.save(newInvoice);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Invoice", invoiceId));
      return invoice;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Deletes an existing invoice from the database.
   *
   * @param invoiceId The id of the invoice to be deleted.
   */
  @Override
  public void deleteInvoice(Long invoiceId) {

    try {
      if (!invoiceRepository.existsById(invoiceId)) {
        logger.info(String.format(DELETE_NOT_FOUND_MESSAGE, "invoice"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            String.format(DELETE_NOT_FOUND_MESSAGE, "invoice"));
      }

      invoiceRepository.deleteById(invoiceId);
      logger.info(String.format(DELETE_SUCCESS_MESSAGE, "Invoice", invoiceId));
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Deletes multiple invoices from the database.
   *
   * @param ids The list of ids of the invoices to be deleted.
   */
  @Override
  public void deleteMultipleInvoices(List<Long> ids) {
    List<Long> existingIds = new ArrayList<>();
    StringBuilder nonExistingIds = new StringBuilder();

    try {
      ids.parallelStream()
          .forEach(
              id -> {
                if (invoiceRepository.existsById(id)) {
                  existingIds.add(id);
                } else {
                  nonExistingIds.append(id).append(", ");
                }
              });

      if (!existingIds.isEmpty()) {
        invoiceRepository.deleteAllById(existingIds);
        logger.info(String.format(DELETE_SUCCESS_MESSAGE, "Invoices", existingIds));
      }

      if (!nonExistingIds.isEmpty()) {
        logger.info(
            String.format(
                MASS_DELETE_NOT_FOUND_MESSAGE, "invoices", nonExistingIds));
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            String.format(
                MASS_DELETE_NOT_FOUND_MESSAGE, "invoices", nonExistingIds));
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}
