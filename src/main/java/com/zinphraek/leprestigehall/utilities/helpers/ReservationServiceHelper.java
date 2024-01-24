package com.zinphraek.leprestigehall.utilities.helpers;

import com.zinphraek.leprestigehall.domain.addon.AddOn;
import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.domain.constants.FacilityInfo;
import com.zinphraek.leprestigehall.domain.email.Attachment;
import com.zinphraek.leprestigehall.domain.email.Mail;
import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.domain.reservation.Reservation;
import com.zinphraek.leprestigehall.domain.reservation.ReservationFilterCriteria;
import com.zinphraek.leprestigehall.domain.reservation.ReservationRate;
import com.zinphraek.leprestigehall.utilities.tables.invoice.InvoiceTableColumnData;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Component
public class ReservationServiceHelper {
  /**
   * Compute the next day 3:00 AM from a date reference.
   *
   * @param dateTime The LocalDateTime object reference.
   * @return The next day at 3:00 AM
   */
  public LocalDateTime getNextDayAt3AM(LocalDateTime dateTime) {
    // Add one day to the input date-time object
    LocalDateTime nextDay = dateTime.plusDays(1);

    // Set the time to 3:00 AM
    return nextDay.with(LocalTime.of(3, 0));
  }

  /**
   * Compute the time duration between two LocalDateTime object.
   *
   * @param dateTimeA First dateTime object
   * @param dateTimeB Second dateTime object
   * @return The duration in hours.
   */
  private double computeDuration(LocalDateTime dateTimeA, LocalDateTime dateTimeB) {
    Duration duration = Duration.between(dateTimeA, dateTimeB);
    return duration.toMinutes() / 60.0;
  }

  /**
   * Compute the overtime cost.
   *
   * @param effectiveEndingDateTime The time at which the reservation event effectively ended.
   * @param endingDateTime          The time at which the reservation event effectively ended.
   * @param startingDateTime        The date and time at which the reservation event was scheduled.
   * @return The computed cost.
   */
  public double computeOvertime(
      LocalDateTime effectiveEndingDateTime,
      LocalDateTime endingDateTime,
      LocalDateTime startingDateTime) {
    double overtime = 0d;
    LocalDateTime nextDayAt3AM = getNextDayAt3AM(startingDateTime);
    if (effectiveEndingDateTime != null
        && effectiveEndingDateTime.isAfter(endingDateTime)
        && effectiveEndingDateTime.isAfter(nextDayAt3AM)) {

      overtime = computeDuration(nextDayAt3AM, effectiveEndingDateTime);

    } else {
      if (effectiveEndingDateTime == null
          && endingDateTime != null
          && endingDateTime.isAfter(nextDayAt3AM)) {
        overtime = computeDuration(nextDayAt3AM, endingDateTime);
      }
    }
    return overtime;
  }

  /**
   * Compute the overtime cost.
   *
   * @param effectiveEndingDateTime The time at which the reservation event effectively ended.
   * @param endingDateTime          The time at which the reservation event effectively ended.
   * @param startingDateTime        The date and time at which the reservation event was scheduled.
   * @param overtimeRate            The rate at which each additional hour is charged.
   * @return The computed cost.
   */
  public double computeOvertimeCost(
      LocalDateTime effectiveEndingDateTime,
      LocalDateTime endingDateTime,
      LocalDateTime startingDateTime,
      double overtimeRate) {
    return computeOvertime(effectiveEndingDateTime, endingDateTime, startingDateTime)
        * overtimeRate;
  }

  /**
   * Compute the total cost of the reservation.
   *
   * @param reservation The reservation to compute the total cost.
   */
  public void computeTotalPrice(Reservation reservation, Function<String, AddOn> getAddOnByName) {

    double totalPrice = 0d;

    double addOnsTotalCost;

    ReservationRate rates = new ReservationRate();

    String day = reservation.getStartingDateTime().getDayOfWeek().toString().toLowerCase();

    // Extracting defined rates through addOn if they exist.
    AddOn seatRateAddOn = getAddOnByName.apply(SEAT_RATE_NAME);
    AddOn overtimeRateAddOn = getAddOnByName.apply(OVERTIME_HOURLY_RATE_NAME);
    AddOn regularFacilityRateAddOn = getAddOnByName.apply(REGULAR_FACILITY_RATE_NAME);
    AddOn saturdayFacilityRateAddOn = getAddOnByName.apply(SATURDAY_FACILITY_RATE_NAME);
    AddOn cleaningFeesSmallGuestsCountAddOn =
        getAddOnByName.apply(CLEANING_FEES_SMALL_GUESTS_COUNT_NAME);
    AddOn cleaningFeesLargeGuestsCountAddOn =
        getAddOnByName.apply(CLEANING_FEES_LARGE_GUESTS_COUNT_NAME);

    // Setting rates
    double seatRate = seatRateAddOn != null ? seatRateAddOn.getPrice() : SEAT_RATE;

    rates.setSeatRate(seatRate);

    double overtimeRate =
        overtimeRateAddOn != null ? overtimeRateAddOn.getPrice() : OVERTIME_HOURLY_RATE;

    rates.setOvertimeRate(overtimeRate);

    double regularFacilityRate =
        regularFacilityRateAddOn != null
            ? regularFacilityRateAddOn.getPrice()
            : REGULAR_DAYS_FACILITY_RATE;
    double saturdayFacilityRate =
        saturdayFacilityRateAddOn != null
            ? saturdayFacilityRateAddOn.getPrice()
            : SATURDAY_FACILITY_RATE;
    double cleaningFeesSmallGuestsCountRate =
        cleaningFeesSmallGuestsCountAddOn != null
            ? cleaningFeesSmallGuestsCountAddOn.getPrice()
            : CLEANING_FEES_SMALL_GUESTS_COUNT;
    double cleaningFeesLargeGuestsCountRate =
        cleaningFeesLargeGuestsCountAddOn != null
            ? cleaningFeesLargeGuestsCountAddOn.getPrice()
            : CLEANING_FEES_LARGE_GUESTS_COUNT;

    if (reservation.getNumberOfSeats() > 0) {
      totalPrice += reservation.getNumberOfSeats() * seatRate;
    }

    // Checking if the customer has requested any addOn.
    if (reservation.getAddOns() != null && !reservation.getAddOns().isEmpty()) {
      addOnsTotalCost =
          reservation.getAddOns().parallelStream()
              .mapToDouble(
                  requestedAddOn ->
                      requestedAddOn.getAddOn().getPrice() * requestedAddOn.getQuantity())
              .sum();

      reservation.setAddOnsTotalCost(addOnsTotalCost);
      totalPrice += addOnsTotalCost;
    }

    // Adding facility fees.
    double facilityFee =
        Objects.equals(day, WEEK_DAYS.get(6).toLowerCase())
            ? saturdayFacilityRate
            : regularFacilityRate;

    rates.setFacilityRate(facilityFee);

    totalPrice += facilityFee;

    // Adding cleaning fees
    double cleaningFees =
        (reservation.getNumberOfSeats() > 0 && reservation.getNumberOfSeats() <= 100)
            ? cleaningFeesSmallGuestsCountRate
            : cleaningFeesLargeGuestsCountRate;

    rates.setCleaningRate(cleaningFees);

    totalPrice += cleaningFees;

    // Computing and adding overtime cost.
    totalPrice +=
        computeOvertimeCost(
            reservation.getEffectiveEndingDateTime(),
            reservation.getEndingDateTime(),
            reservation.getStartingDateTime(),
            overtimeRate);

    // Applying discount if any.
    if (reservation.getDiscount() != null) {
      totalPrice = reservation.getDiscount().applyDiscount(totalPrice);
    }

    // Adding taxes
    if (reservation.getTaxRate() != null) {
      totalPrice += totalPrice * reservation.getTaxRate();
    }

    reservation.setTotalPrice(totalPrice);
    reservation.setRates(rates);
  }

  public Attachment generateInvoiceAttachment(Invoice invoice) {

    DecimalFormat df = new DecimalFormat("#.##");
    double totalPrice = invoice.getReservation().getTotalPrice();
    Double taxRate = invoice.getReservation().getTaxRate();
    Double subtotal = totalPrice / (1 + taxRate);
    Double tax = (subtotal * taxRate);

    Attachment attachment = new Attachment();
    attachment.setName("Invoice.pdf");
    attachment.setTemplate(INVOICE_PDF_TEMPLATE);
    attachment.addVariable("invoice", invoice);
    attachment.addVariable(
        "issuedDate",
        invoice
            .getIssuedDate()
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern(INVOICE_TEMPLATE_DATE_FORMAT)));
    attachment.addVariable(
        "dueDate",
        invoice
            .getDueDate()
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern(INVOICE_TEMPLATE_DATE_FORMAT)));
    attachment.addVariable("user", invoice.getUser());
    attachment.addVariable("tax", df.format(tax));
    attachment.addVariable("subtotal", df.format(subtotal));
    attachment.addVariable("totalPrice", df.format(totalPrice));
    FacilityInfo facilityInfo = new FacilityInfo();
    attachment.addVariable("facilityInfo", facilityInfo);
    attachment.addVariable("discount", invoice.getReservation().getDiscount());

    // Setting the table values.
    List<InvoiceTableColumnData> columns = new ArrayList<>();

    columns.add(new InvoiceTableColumnData("#", "col1", 1));
    columns.add(new InvoiceTableColumnData("Services/Product", "col2", 3));
    columns.add(new InvoiceTableColumnData("Quantity", "col3", 2));
    columns.add(new InvoiceTableColumnData("Rate", "col4", 2));
    columns.add(new InvoiceTableColumnData("Unit", "col5", 2));
    columns.add(new InvoiceTableColumnData("Total", "col6", 2));

    attachment.addVariable("columns", columns);

    List<Map<String, Object>> rows = new ArrayList<>();

    Map<String, Object> row1 = new HashMap<>();
    row1.put("col1", "1");
    row1.put("col2", "Facility");
    row1.put("col3", 1);
    row1.put("col4", invoice.getReservation().getRates().getFacilityRate());
    row1.put("col5", "Dollar ($)");
    row1.put("col6", invoice.getReservation().getRates().getFacilityRate());

    rows.add(row1);

    Map<String, Object> row2 = new HashMap<>();
    row2.put("col1", "2");
    row2.put("col2", "Guests Count");
    row2.put("col3", invoice.getReservation().getNumberOfSeats());
    row2.put("col4", invoice.getReservation().getRates().getSeatRate());
    row2.put("col5", "Dollar ($)");
    row2.put(
        "col6",
        invoice.getReservation().getNumberOfSeats()
            * invoice.getReservation().getRates().getSeatRate());

    rows.add(row2);

    Map<String, Object> row3 = new HashMap<>();
    row3.put("col1", "3");
    row3.put("col2", "Cleaning Fees");
    row3.put("col3", 1);
    row3.put("col4", invoice.getReservation().getRates().getCleaningRate());
    row3.put("col5", "Dollar ($)");
    row3.put("col6", invoice.getReservation().getRates().getCleaningRate());

    rows.add(row3);

    double overtime =
        computeOvertime(
            invoice.getReservation().getEffectiveEndingDateTime(),
            invoice.getReservation().getEndingDateTime(),
            invoice.getReservation().getStartingDateTime());

    if (overtime > 0d) {
      Map<String, Object> row4 = new HashMap<>();
      row4.put("col1", "4");
      row4.put("col2", "Overtime");
      row4.put("col3", overtime);
      row4.put("col4", invoice.getReservation().getRates().getOvertimeRate());
      row4.put("col5", "Dollar ($)");
      row4.put("col6", invoice.getReservation().getRates().getOvertimeRate() * overtime);

      rows.add(row4);
    }

    Collection<RequestedAddOn> addOns = invoice.getReservation().getAddOns();
    List<RequestedAddOn> requestedAddOnList =
        addOns != null ? addOns.stream().toList() : new ArrayList<>();
    int baseIndex = overtime > 0d ? 5 : 4;
    if (!requestedAddOnList.isEmpty()) {
      List<Map<String, Object>> addOnRows =
          IntStream.range(0, requestedAddOnList.size())
              .mapToObj(
                  index -> {
                    RequestedAddOn requestedAddOn = requestedAddOnList.get(index);
                    Map<String, Object> addOnRow = new HashMap<>();
                    addOnRow.put("col1", index + baseIndex);
                    addOnRow.put("col2", requestedAddOn.getAddOn().getName());
                    addOnRow.put("col3", requestedAddOn.getQuantity());
                    addOnRow.put("col4", requestedAddOn.getAddOn().getPrice());
                    addOnRow.put("col5", "Dollar ($)");
                    addOnRow.put(
                        "col6",
                        requestedAddOn.getQuantity() * requestedAddOn.getAddOn().getPrice());
                    return addOnRow;
                  })
              .toList();
      rows.addAll(addOnRows);
    }
    attachment.addVariable("rows", rows);

    return attachment;
  }

  /**
   * Generate a mail object with an invoice attachment with the provided invoice and subject
   *
   * @param invoice       The invoice to attach.
   * @param emailTemplate The mail's template.
   * @param sender        The mail's sender.
   * @param subject       the mail's subject.
   * @param hasAttachment whether the mail has an attachment or not.
   * @return The constructed mail.
   */
  public Mail getMail(
      Invoice invoice, String emailTemplate, String sender, String subject, boolean hasAttachment)
      throws IOException {
    Mail mail = new Mail();
    mail.setEmailTemplate(emailTemplate);
    mail.setSubject(subject);
    mail.setFrom(sender);
    mail.setTo(invoice.getUser().getEmail());

    // Setting the mail variables template variables.
    String customerName = invoice.getUser().getFirstName() + " " + invoice.getUser().getLastName();

    mail.addVariable("customerName", customerName);
    mail.addVariable(
        "reservationDate",
        invoice
            .getReservation()
            .getStartingDateTime()
            .toLocalDate());
    mail.addVariable("accountLink", String.format("%s/account/reservations", FRONTEND_CLIENT_URL));
    mail.addVariable("customerServicePhoneNumber", CUSTOMER_SERVICE_PHONE_NUMBER);
    mail.addVariable("customerServiceEmail", CUSTOMER_SERVICE_EMAIL_ADDRESS);

    if (Objects.equals(emailTemplate, RESERVATION_CANCELLATION_EMAIL_TEMPLATE)) {
      mail.addVariable("guestCount", invoice.getReservation().getNumberOfSeats());
      mail.addVariable("feedbackFormLink", FEEDBACK_FORM_LINK);
    }

    if (hasAttachment) {
      // Setting the attachment.
      Attachment attachment = generateInvoiceAttachment(invoice);
      mail.addAttachment(attachment);
    }

    return mail;
  }

  public Mail getGenericMail(
      String emailTemplate, String sender, String to, String subject, String body) {
    Mail mail = new Mail();
    mail.setEmailTemplate(emailTemplate);
    mail.setSubject(subject);
    mail.setFrom(sender);
    mail.setTo(to);

    mail.addVariable("body", body);
    return mail;
  }

  /**
   * Generate a tuple containing a customPage entity and a ReservationFilterCriteria.
   *
   * @param params The sorting and filtering options
   * @return A tuple containing a customPage entity and a ReservationFilterCriteria.
   */
  public Pair<CustomPage, ReservationFilterCriteria> generateCustomPageAndReservationFilterCriteria(
      Map<String, String> params) {

    CustomPage customPage = createCustomPageFromParams(params);
    ReservationFilterCriteria reservationFilterCriteria = createReservationFilterCriteriaFromParams(params);

    return Pair.of(customPage, reservationFilterCriteria);
  }


  private ReservationFilterCriteria createReservationFilterCriteriaFromParams(Map<String, String> params) {
    ReservationFilterCriteria reservationFilterCriteria = new ReservationFilterCriteria();

    for (Map.Entry<String, String> entry : params.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      switch (key) {
        case "eventType":
          reservationFilterCriteria.setEventType(value);
          break;
        case "isFullPackage":
          reservationFilterCriteria.setFullPackage(Boolean.parseBoolean(value));
          break;
        case "maxTotalPrice":
          reservationFilterCriteria.setMaxTotalPrice(Double.parseDouble(value));
          break;
        case "minTotalPrice":
          reservationFilterCriteria.setMinTotalPrice(Double.parseDouble(value));
          break;
        case "priceComputationMethod":
          reservationFilterCriteria.setPriceComputationMethod(value);
          break;
        case "isSecurityDepositRefunded":
          reservationFilterCriteria.setSecurityDepositRefunded(Boolean.parseBoolean(value));
          break;
        case "startedAfter":
          if (StringUtils.isNotBlank(value)) {
            reservationFilterCriteria.setStartedAfter(value);
          }
          break;
        case "startedBefore":
          if (StringUtils.isNotBlank(value)) {
            reservationFilterCriteria.setStartedBefore(value);
          }
          break;
        case "status":
          reservationFilterCriteria.setStatus(value);
          break;
        case "userId":
          reservationFilterCriteria.setUserId(value);
          break;
      }
    }

    return reservationFilterCriteria;
  }

  /**
   * Compute the date by which a reservation should be fully paid.
   *
   * @param startingDateTime The starting date of the reservation.
   * @return The due date.
   */
  public String computeDueDate(LocalDateTime startingDateTime) {
    LocalDateTime dueDate = startingDateTime.minusWeeks(1);
    if (dueDate.isBefore(LocalDateTime.now())) {
      dueDate = LocalDateTime.now();
    }
    return dueDate.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }
}
