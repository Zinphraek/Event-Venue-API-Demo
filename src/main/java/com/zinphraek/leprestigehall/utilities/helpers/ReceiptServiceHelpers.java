package com.zinphraek.leprestigehall.utilities.helpers;

import static com.zinphraek.leprestigehall.domain.constants.Constants.CUSTOMER_SERVICE_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CUSTOMER_SERVICE_PHONE_NUMBER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.EMAIL_RECEIPT_TEMPLATE;
import static com.zinphraek.leprestigehall.domain.constants.Constants.INVOICE_TEMPLATE_DATE_FORMAT;
import static com.zinphraek.leprestigehall.domain.constants.Constants.RECEIPT_PDF_TEMPLATE;

import com.zinphraek.leprestigehall.domain.addon.RequestedAddOn;
import com.zinphraek.leprestigehall.domain.constants.FacilityInfo;
import com.zinphraek.leprestigehall.domain.email.Attachment;
import com.zinphraek.leprestigehall.domain.email.Mail;
import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.domain.receipt.Receipt;
import com.zinphraek.leprestigehall.utilities.tables.invoice.InvoiceTableColumnData;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

@Component
public class ReceiptServiceHelpers {

  private final ReservationServiceHelper reservationServiceHelper = new ReservationServiceHelper();

  /**
   * Create a receipt attachment.
   *
   * @param receipt The receipt to extract data from.
   * @param invoice The invoice to extract data from.
   * @return An attachment object.
   */
  public Attachment createReceiptAttachment(Receipt receipt, Invoice invoice) {

    DecimalFormat df = new DecimalFormat("#.##");
    double totalPrice = invoice.getReservation().getTotalPrice();
    Double taxRate = invoice.getReservation().getTaxRate();
    Double subtotal = totalPrice / (1 + taxRate);
    Double tax = (subtotal * taxRate);

    Attachment attachment = new Attachment();
    attachment.setName("Receipt.pdf");
    attachment.setTemplate(RECEIPT_PDF_TEMPLATE);
    attachment.addVariable("receipt", receipt);

    attachment.addVariable(
        "paymentDate",
        receipt
            .getReceiptDate()
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern(INVOICE_TEMPLATE_DATE_FORMAT)));
    attachment.addVariable("user", invoice.getUser());
    attachment.addVariable("tax", df.format(tax));
    attachment.addVariable("subtotal", df.format(subtotal));
    attachment.addVariable("totalPrice", df.format(totalPrice));
    FacilityInfo facilityInfo = new FacilityInfo();
    attachment.addVariable("facilityInfo", facilityInfo);

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
        reservationServiceHelper.computeOvertime(
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
   * Create a mail object for the receipt.
   *
   * @param receipt The receipt to send.
   * @param invoice The invoice to send.
   * @param sender The sender of the email.
   * @param subject The subject of the email.
   * @return A mail object.
   * @throws IOException If an error occurs when creating the attachment.
   */
  public Mail getMail(Receipt receipt, Invoice invoice, String sender, String subject)
      throws IOException {
    Mail mail = new Mail();
    mail.setEmailTemplate(EMAIL_RECEIPT_TEMPLATE);
    mail.setSubject(subject);
    mail.setFrom(sender);
    mail.setTo(invoice.getUser().getEmail());

    // Setting the mail variables template variables.
    String customerName = invoice.getUser().getFirstName() + " " + invoice.getUser().getLastName();

    mail.addVariable("customerName", customerName);

    mail.addVariable("customerServicePhoneNumber", CUSTOMER_SERVICE_PHONE_NUMBER);
    mail.addVariable("customerServiceEmail", CUSTOMER_SERVICE_EMAIL_ADDRESS);

    Attachment attachment = createReceiptAttachment(receipt, invoice);

    mail.addAttachment(attachment);
    return mail;
  }
}
