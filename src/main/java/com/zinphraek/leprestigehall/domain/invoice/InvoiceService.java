package com.zinphraek.leprestigehall.domain.invoice;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public interface InvoiceService {

  Page<Invoice> getInvoices(Map<String, String> params);

  Page<Invoice> getInvoicesByUserId(String userId, Map<String, String> params);

  Invoice getInvoice(Long invoiceId);

  Invoice createInvoice(Invoice newInvoice);

  Invoice updateInvoice(Long invoiceId, Invoice newInvoice);

  void deleteInvoice(Long invoiceId);

  void deleteMultipleInvoices(List<Long> ids);
}
