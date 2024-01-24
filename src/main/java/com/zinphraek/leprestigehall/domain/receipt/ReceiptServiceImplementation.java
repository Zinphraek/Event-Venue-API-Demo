package com.zinphraek.leprestigehall.domain.receipt;

import com.zinphraek.leprestigehall.domain.email.EmailServiceImplementation;
import com.zinphraek.leprestigehall.domain.email.Mail;
import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.domain.invoice.InvoiceService;
import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import com.zinphraek.leprestigehall.utilities.helpers.ReceiptServiceHelpers;
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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.DATA_ACCESS_EXCEPTION_LOG_MESSAGE;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.GENERIC_UNEXPECTED_ERROR_MESSAGE;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class ReceiptServiceImplementation implements ReceiptService {

  private final Logger logger = LogManager.getLogger(ReceiptServiceImplementation.class);

  @Autowired
  private EmailServiceImplementation emailService;
  @Autowired
  private final ReceiptServiceHelpers serviceHelpers;

  @Autowired
  private ReceiptRepository receiptRepository;
  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private InvoiceService invoiceService;

  public ReceiptServiceImplementation(
      ReceiptServiceHelpers serviceHelpers,
      ReceiptRepository receiptRepository,
      EmailServiceImplementation emailService,
      PaymentRepository paymentRepository,
      InvoiceService invoiceService) {
    this.serviceHelpers = serviceHelpers;
    this.receiptRepository = receiptRepository;
    this.emailService = emailService;
    this.paymentRepository = paymentRepository;
    this.invoiceService = invoiceService;
  }

  /**
   * Persist the payments to the database.
   *
   * @param receipt The receipt entity.
   */
  private void persistPayments(Receipt receipt) {
    receipt.getPayments().parallelStream().forEach(payment -> payment.setReceipt(receipt));
    try {
      logger.info("Persisting payments...");
      paymentRepository.saveAll(receipt.getPayments());
      logger.info("Successfully persisted payments.");
    } catch (DataAccessException e) {
      logger.error("Oops! Something went wrong while trying to persist payments.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
  }

  /**
   * Generate a customPage and receiptFilterCriteria entity from the params object.
   *
   * @param params The params object.
   * @return A pair of customPage and receiptFilterCriteria entity.
   */
  private Pair<CustomPage, ReceiptFilterCriteria> generateCustomPageAndReceiptFilterCriteria(
      Map<String, String> params) {

    CustomPage customPage = createCustomPageFromParams(params);
    ReceiptFilterCriteria receiptFilterCriteria = new ReceiptFilterCriteria();

    // Setting up the receiptFilterCriteria entity with the corresponding value in the param object.

    if (params.containsKey("id")) {
      receiptFilterCriteria.setId(Long.parseLong(params.get("id")));
    }

    if (params.containsKey("invoiceId")) {
      receiptFilterCriteria.setInvoiceId(Long.parseLong(params.get("invoiceId")));
    }

    if (params.containsKey("userId")) {
      receiptFilterCriteria.setUserId(params.get("userId"));
    }

    if (params.containsKey("reservationId")) {
      receiptFilterCriteria.setReservationId(Long.parseLong(params.get("reservationId")));
    }

    if (params.containsKey("receiptNumber")) {
      receiptFilterCriteria.setReceiptNumber(params.get("receiptNumber"));
    }

    if (params.containsKey("beforeReceiptDate")) {
      receiptFilterCriteria.setBeforeReceiptDate(params.get("beforeReceiptDate"));
    }

    if (params.containsKey("afterReceiptDate")) {
      receiptFilterCriteria.setAfterReceiptDate(params.get("afterReceiptDate"));
    }

    if (params.containsKey("beforePaymentDate")) {
      receiptFilterCriteria.setBeforePaymentDate(params.get("beforePaymentDate"));
    }

    if (params.containsKey("afterPaymentDate")) {
      receiptFilterCriteria.setAfterPaymentDate(params.get("afterPaymentDate"));
    }

    if (params.containsKey("paymentMethod")) {
      receiptFilterCriteria.setPaymentMethod(params.get("paymentMethod"));
    }

    if (params.containsKey("greaterThanAmountPaid")) {
      receiptFilterCriteria.setGreaterThanAmountPaid(
          Double.parseDouble(params.get("greaterThanAmountPaid")));
    }

    if (params.containsKey("lessThanAmountPaid")) {
      receiptFilterCriteria.setLessThanAmountPaid(
          Double.parseDouble(params.get("lessThanAmountPaid")));
    }

    if (params.containsKey("greaterThanBalanceDue")) {
      receiptFilterCriteria.setGreaterThanBalanceDue(
          Double.parseDouble(params.get("greaterThanBalanceDue")));
    }

    if (params.containsKey("lessThanBalanceDue")) {
      receiptFilterCriteria.setLessThanBalanceDue(
          Double.parseDouble(params.get("lessThanBalanceDue")));
    }

    if (params.containsKey("paidBy")) {
      receiptFilterCriteria.setPaidBy(params.get("paidBy"));
    }

    if (params.containsKey("cashierName")) {
      receiptFilterCriteria.setCashierName(params.get("cashierName"));
    }

    return Pair.of(customPage, receiptFilterCriteria);
  }

  /**
   * Update the invoice with the receipt information.
   *
   * @param receipt The receipt entity.
   */
  private void updateInvoiceWithReceiptInformation(Receipt receipt, Invoice invoice) {

    if (invoice.getAmountDue() < 0) {
      logger.error("Amount paid cannot be greater than the balance due.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Amount paid cannot be greater than the balance due.");
    }

    if (receipt.getPrevBalance() == null) {
      receipt.setPrevBalance(invoice.getReservation().getTotalPrice());
    } else {
      receipt.setPrevBalance(invoice.getAmountDue());
    }

    if (Objects.equals(invoice.getAmountDue(), receipt.getAmountPaid())) {
      invoice.setAmountDue(0.0);
      invoice.setStatus(STATUS_PAID);
    } else {
      invoice.setStatus(STATUS_PARTIALLY_PAID);
      invoice.addPaymentSessionToTotalAmountPaid(receipt.getAmountPaid());
      invoice.setAmountDue(invoice.getReservation().getTotalPrice() - invoice.getTotalAmountPaid());
    }

    receipt.setTotalAmountPaidTillToday(invoice.getTotalAmountPaid());
    receipt.setBalanceDue(invoice.getAmountDue());
  }

  /**
   * Send a receipt to the user.
   *
   * @param receiptId      - The receipt id.
   * @param receiptMailDTO - The receipt mail dto.
   */
  @Override
  public void emailReceipt(Long receiptId, ReceiptMailDTO receiptMailDTO) {
    Receipt receipt = getReceipt(receiptId);
    Invoice invoice = invoiceService.getInvoice(receipt.getInvoiceId());

    try {
      Mail mail =
          serviceHelpers.getMail(
              receipt, invoice, receiptMailDTO.address(), receiptMailDTO.subject());
      mail.addVariable("emailBody", receiptMailDTO.body());
      mail.setFrom(CUSTOMER_SERVICE_EMAIL_ADDRESS);
      mail.setEmailTemplate(GENERIC_EMAIL_TEMPLATE);
      logger.info("Sending receipt to: " + receiptMailDTO.address());
      emailService.sendEmail(mail);
    } catch (IOException | RuntimeException e) {
      logger.error("Couldn't send receipt email.", e);
    }

  }

  /**
   * Fetch all receipts from the database.
   *
   * @param params The params object.
   * @return A page of receipts.
   */
  @Override
  public Page<Receipt> getAllReceipts(Map<String, String> params) {
    Page<Receipt> receipts;
    Pageable pageable = Pageable.unpaged();
    Pair<CustomPage, ReceiptFilterCriteria> customPageAndReceiptFilterCriteria =
        Pair.of(new CustomPage(), new ReceiptFilterCriteria());

    logger.info("Fetching receipts...");
    try {
      if (!params.isEmpty()) {
        // Extracting the pageable object from the params object.
        customPageAndReceiptFilterCriteria =
            generateCustomPageAndReceiptFilterCriteria(params);

        pageable =
            PageRequest.of(
                customPageAndReceiptFilterCriteria.getFirst().getPageNumber(),
                customPageAndReceiptFilterCriteria.getFirst().getPageSize(),
                customPageAndReceiptFilterCriteria.getFirst().getSortDirection(),
                customPageAndReceiptFilterCriteria.getFirst().getSortBy()
            );
      }
      receipts =
          receiptRepository.findAllAndFilter(
              customPageAndReceiptFilterCriteria.getSecond().getReceiptNumber(),
              customPageAndReceiptFilterCriteria.getSecond().getBeforeReceiptDate(),
              customPageAndReceiptFilterCriteria.getSecond().getAfterReceiptDate(),
              customPageAndReceiptFilterCriteria.getSecond().getGreaterThanAmountPaid(),
              customPageAndReceiptFilterCriteria.getSecond().getLessThanAmountPaid(),
              customPageAndReceiptFilterCriteria.getSecond().getReservationId(),
              customPageAndReceiptFilterCriteria.getSecond().getUserId(),
              customPageAndReceiptFilterCriteria.getSecond().getInvoiceId(),
              customPageAndReceiptFilterCriteria.getSecond().getBeforePaymentDate(),
              customPageAndReceiptFilterCriteria.getSecond().getAfterPaymentDate(),
              customPageAndReceiptFilterCriteria.getSecond().getGreaterThanBalanceDue(),
              customPageAndReceiptFilterCriteria.getSecond().getLessThanBalanceDue(),
              customPageAndReceiptFilterCriteria.getSecond().getPaidBy(),
              customPageAndReceiptFilterCriteria.getSecond().getCashierName(),
              pageable);
    } catch (DataAccessException e) {
      logger.error("Oops! Something went wrong while trying to retrieve all receipts.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return receipts;
  }

  /**
   * Fetch all receipts from the database belonging to a single user.
   *
   * @param userId The user id.
   * @param params The params object.
   * @return A page of receipts.
   */
  @Override
  public Page<Receipt> getReceiptsByUserId(String userId, Map<String, String> params) {
    Page<Receipt> receipts;
    Pageable pageable = Pageable.unpaged();
    try {
      if (params.isEmpty()) {
        receipts = receiptRepository.findByUserId(userId, pageable);
      } else {

        // Extracting the pageable object from the params object.
        Pair<CustomPage, ReceiptFilterCriteria> customPageAndReceiptFilterCriteria =
            generateCustomPageAndReceiptFilterCriteria(params);

        pageable =
            PageRequest.of(
                customPageAndReceiptFilterCriteria.getFirst().getPageNumber(),
                customPageAndReceiptFilterCriteria.getFirst().getPageSize(),
                customPageAndReceiptFilterCriteria.getFirst().getSortDirection(),
                customPageAndReceiptFilterCriteria.getFirst().getSortBy());

        receipts =
            receiptRepository.findAllAndFilter(
                customPageAndReceiptFilterCriteria.getSecond().getReceiptNumber(),
                customPageAndReceiptFilterCriteria.getSecond().getBeforeReceiptDate(),
                customPageAndReceiptFilterCriteria.getSecond().getAfterReceiptDate(),
                customPageAndReceiptFilterCriteria.getSecond().getGreaterThanAmountPaid(),
                customPageAndReceiptFilterCriteria.getSecond().getLessThanAmountPaid(),
                customPageAndReceiptFilterCriteria.getSecond().getReservationId(),
                userId,
                customPageAndReceiptFilterCriteria.getSecond().getInvoiceId(),
                customPageAndReceiptFilterCriteria.getSecond().getBeforePaymentDate(),
                customPageAndReceiptFilterCriteria.getSecond().getAfterPaymentDate(),
                customPageAndReceiptFilterCriteria.getSecond().getGreaterThanBalanceDue(),
                customPageAndReceiptFilterCriteria.getSecond().getLessThanBalanceDue(),
                customPageAndReceiptFilterCriteria.getSecond().getPaidBy(),
                customPageAndReceiptFilterCriteria.getSecond().getCashierName(),
                pageable);
      }
    } catch (DataAccessException e) {
      logger.error(
          "Oops! Something went wrong while trying to retrieve all receipts belonging to user with id: "
              + userId);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    } catch (RuntimeException re) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return receipts;
  }

  /**
   * Retrieve a single receipt from the database.
   *
   * @param receiptId The receipt id.
   * @return The receipt.
   */
  @Override
  public Receipt getReceipt(Long receiptId) {
    Optional<Receipt> optionalReceipt;

    try {
      optionalReceipt = receiptRepository.findById(receiptId);
    } catch (DataAccessException e) {
      logger.error(
          "Oops! Something went wrong while trying to retrieve optionalReceipt with id: "
              + receiptId);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    if (optionalReceipt.isEmpty()) {
      logger.error("Receipt with id: " + receiptId + " does not exist.");
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Receipt with id: " + receiptId + " does not exist.");
    }
    return optionalReceipt.get();
  }

  /**
   * Persist a new receipt to the database.
   *
   * @param newReceipt The new receipt.
   * @return The new receipt.
   */
  @Override
  public Receipt createReceipt(Receipt newReceipt) {

    try {
      if (newReceipt.getReceiptNumber() != null
          && receiptRepository.existsByReceiptNumber(newReceipt.getReceiptNumber())) {
        logger.error(
            "Receipt with receipt number: " + newReceipt.getReceiptNumber() + " already exists.");
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Receipt with receipt number: " + newReceipt.getReceiptNumber() + " already exists.");
      }
    } catch (DataAccessException e) {
      logger.error(
          "Oops! Something went wrong while trying to check if receipt with receipt number: "
              + newReceipt.getReceiptNumber()
              + " already exists.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    Invoice invoice = invoiceService.getInvoice(newReceipt.getInvoiceId());
    long numberOfReceipts = receiptRepository.countByInvoiceId(newReceipt.getInvoiceId()) + 1;

    String receiptNumber =
        "RR" + invoice.getReservation().getId() + "IN000" + invoice.getId() + numberOfReceipts;

    newReceipt.setReceiptNumber(receiptNumber);
    newReceipt.addThisPaymentSessionToTotalAmountPaid(newReceipt.getThisPaymentSessionSum());

    updateInvoiceWithReceiptInformation(newReceipt, invoice);

    try {
      logger.info("Creating a new receipt...");
      receiptRepository.save(newReceipt);
      persistPayments(newReceipt);
      logger.info("Successfully created a new receipt with id: " + newReceipt.getId());

      // Update the invoice with the receipt information.
      logger.info("Updating invoice with id: " + invoice.getId());
      invoiceService.updateInvoice(invoice.getId(), invoice);
      logger.info("Successfully updated invoice with id: " + invoice.getId());

      // Sending email to the user.
      Mail mail =
          serviceHelpers.getMail(
              newReceipt, invoice, INVOICES_EMAIL_ADDRESS, PAYMENT_CONFIRMATION_SUBJECT);
      logger.info("Sending confirmation email to user with id: " + newReceipt.getUserId());
      emailService.sendEmail(mail);
    } catch (DataAccessException e) {
      logger.error("Oops! Something went wrong while trying to create a new receipt.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    }
    return newReceipt;
  }

  /**
   * Update an existing receipt in the database.
   *
   * @param receiptId  The receipt id.
   * @param newReceipt The new receipt.
   * @return The updated receipt.
   */
  @Override
  public Receipt updateReceipt(Long receiptId, Receipt newReceipt) {

    if (!Objects.equals(receiptId, newReceipt.getId())) {
      logger.error("Receipt id: " + receiptId + " does not match the id in the receipt object.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Receipt id: " + receiptId + " does not match the id in the receipt object.");
    }

    if (!receiptRepository.existsById(receiptId)) {
      logger.error("Receipt with id: " + receiptId + " does not exist.");
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Receipt with id: " + receiptId + " does not exist.");
    }

    // Update the invoice with the receipt information.
    Invoice invoice = invoiceService.getInvoice(newReceipt.getInvoiceId());

    updateInvoiceWithReceiptInformation(newReceipt, invoice);

    try {
      receiptRepository.save(newReceipt);
      logger.info("Receipt with id: " + receiptId + " successfully updated.");

      invoiceService.updateInvoice(invoice.getId(), invoice);

      // Sending email to the user.
      Mail mail =
          serviceHelpers.getMail(
              newReceipt, invoice, INVOICES_EMAIL_ADDRESS, PAYMENT_CONFIRMATION_SUBJECT);
      logger.info("Sending confirmation email to user with id: " + newReceipt.getUserId());
      emailService.sendEmail(mail);
    } catch (DataAccessException e) {
      logger.error(
          "Oops! Something went wrong while trying to update receipt with id: " + receiptId);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    } catch (MailException | IOException e) {
      logger.error("Couldn't send confirmation email.");
      logger.error(e);
    }

    return newReceipt;
  }

  /**
   * Delete a single receipt from the database.
   *
   * @param receiptId The receipt id.
   */
  @Override
  public void deleteReceipt(Long receiptId) {
    if (!receiptRepository.existsById(receiptId)) {
      logger.error("Receipt with id: " + receiptId + " does not exist.");
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Receipt with id: " + receiptId + " does not exist.");
    }
    try {
      receiptRepository.deleteById(receiptId);
      logger.info("Receipt with id: " + receiptId + " was deleted.");
    } catch (DataAccessException e) {
      logger.error(
          "Oops! Something went wrong while trying to delete receipt with id: " + receiptId);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
  }

  /**
   * Delete multiple receipts from the database.
   *
   * @param ids The list of receipt ids.
   */
  @Override
  public void deleteMultipleReceipts(List<Long> ids) {
    List<Long> existingReceiptIds = new ArrayList<>();
    StringBuilder nonExistingReceiptIds = new StringBuilder();

    ids.parallelStream()
        .forEach(
            id -> {
              if (receiptRepository.existsById(id)) {
                existingReceiptIds.add(id);
              } else {
                nonExistingReceiptIds.append(id).append(", ");
              }
            });

    if (!existingReceiptIds.isEmpty()) {
      try {
        receiptRepository.deleteAllById(existingReceiptIds);
        logger.info("Receipts with ids: " + existingReceiptIds + " were deleted.");
      } catch (DataAccessException e) {
        logger.error(
            "Oops! Something went wrong while trying to delete receipts with ids: "
                + existingReceiptIds);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
      }
    }

    if (!nonExistingReceiptIds.toString().isEmpty()) {
      logger.error("Receipts with ids: " + nonExistingReceiptIds + "do not exist.");
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Receipts with ids: " + nonExistingReceiptIds + "do not exist.");
    }
  }
}
