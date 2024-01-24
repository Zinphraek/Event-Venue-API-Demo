package com.zinphraek.leprestigehall.domain.invoice;

import static com.zinphraek.leprestigehall.domain.constants.Paths.InvoicePath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@Controller
public class InvoiceController {

  @Autowired private final InvoiceServiceImplementation invoiceService;

  public InvoiceController(InvoiceServiceImplementation invoiceService) {
    this.invoiceService = invoiceService;
  }

  @PreAuthorize("hasRole('admin')")
  @GetMapping(InvoicePath)
  public ResponseEntity<Page<Invoice>> getInvoices(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(invoiceService.getInvoices(params), HttpStatus.OK);
  }

  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @GetMapping(UserPath +  "/{userId}" + InvoicePath)
  public ResponseEntity<Page<Invoice>> getInvoicesByUserId(
      @PathVariable String userId, @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(invoiceService.getInvoicesByUserId(userId, params), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @GetMapping(InvoicePath + "/{id}")
  public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
    return new ResponseEntity<>(invoiceService.getInvoice(id), HttpStatus.OK);
  }
  @PreAuthorize("hasRole('admin')")
  @PostMapping(InvoicePath)
  public ResponseEntity<Invoice> createInvoice(@RequestPart("invoice") Invoice newInvoice) {
    Invoice invoice = invoiceService.createInvoice(newInvoice);
    return new ResponseEntity<>(invoice, HttpStatus.CREATED);
  }

  @PreAuthorize("hasRole('admin')")
  @PutMapping(InvoicePath + "/{id}")
  public ResponseEntity<Invoice> updateInvoice(
      @PathVariable Long id, @RequestPart("invoice") Invoice newInvoice) {
    Invoice invoice = invoiceService.updateInvoice(id, newInvoice);
    return new ResponseEntity<>(invoice, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(InvoicePath + "/{id}")
  public ResponseEntity<HttpStatus> deleteInvoice(@PathVariable Long id) {
    invoiceService.deleteInvoice(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(InvoicePath)
  public ResponseEntity<HttpStatus> deleteMultipleInvoices(
      @RequestPart("ids") Map<String, List<Long>> invoicesId) {
    invoiceService.deleteMultipleInvoices(invoicesId.get("ids"));
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
