package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.invoice.Invoice;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.time.LocalDateTime;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;

public class InvoiceFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();
  private static final String[] STATUS_VALUES = {
      STATUS_PAID,
      STATUS_DUE,
      STATUS_PARTIALLY_PAID,
      STATUS_OVERDUE,
      STATUS_DUE_IMMEDIATELY,
      STATUS_WITHDRAWN,
  };

  public Invoice generateRandomInvoice(Long id, String invoiceNumber, boolean shouldBeIssuedInThePast) {
    Invoice invoice = new Invoice();

    LocalDateTime issuedDate = shouldBeIssuedInThePast
        ? utilities.generateRandomPastLocalDateTime(0)
        : utilities.generateRandomFutureLocalDateTime(0);

    LocalDateTime dueDate = issuedDate.plusMonths(utilities.getRandomPositiveInt(1, 3));

    invoice.setId(id);
    invoice.setInvoiceNumber(invoiceNumber);
    invoice.setIssuedDate(utilities.formatLocalDateTime(issuedDate));
    invoice.setDueDate(utilities.formatLocalDateTime(dueDate));
    invoice.setTotalAmountPaid(utilities.generateRandomDoubleWithin(1000D, 2500D));
    invoice.setAmountDue(utilities.generateRandomDoubleWithin(2500D, 5000D));
    invoice.setStatus(utilities.getRandomValueFromArray(STATUS_VALUES));

    return invoice;
  }
}
