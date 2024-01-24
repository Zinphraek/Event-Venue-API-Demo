package com.zinphraek.leprestigehall.domain.receipt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.zinphraek.leprestigehall.domain.constants.Paths.AdminPath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

@Controller
public class ReceiptController {

  @Autowired
  private final ReceiptServiceImplementation receiptService;

  public ReceiptController(ReceiptServiceImplementation receiptService) {
    this.receiptService = receiptService;
  }

  @PreAuthorize("hasRole('admin')")
  @GetMapping(AdminPath + "/receipts")
  public ResponseEntity<Page<Receipt>> getAllReceipts(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(receiptService.getAllReceipts(params), HttpStatus.OK);
  }

  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @GetMapping(UserPath + "{userId}/receipts")
  public ResponseEntity<Page<Receipt>> getReceiptsByUserId(
      @PathVariable("userId") String userId,
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(receiptService.getReceiptsByUserId(userId, params), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @GetMapping(AdminPath + "/receipts/{receiptId}")
  public ResponseEntity<Receipt> getReceipt(@PathVariable("receiptId") Long receiptId) {
    return new ResponseEntity<>(receiptService.getReceipt(receiptId), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @PostMapping(AdminPath + "/receipts")
  public ResponseEntity<Receipt> createReceipt(@RequestPart("receipt") Receipt newReceipt) {
    return new ResponseEntity<>(receiptService.createReceipt(newReceipt), HttpStatus.CREATED);
  }

  @PreAuthorize("hasRole('admin')")
  @PostMapping(AdminPath + "/receipts/{receiptId}/email")
  public ResponseEntity<Void> emailReceipt(
      @PathVariable("receiptId") Long receiptId,
      @RequestPart("receiptMailDTO") ReceiptMailDTO receiptMailDTO) {
    receiptService.emailReceipt(receiptId, receiptMailDTO);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("hasRole('admin')")
  @PutMapping(AdminPath + "/receipts/{receiptId}")
  public ResponseEntity<Receipt> updateReceipt(
      @PathVariable("receiptId") Long receiptId, @RequestPart("receipt") Receipt updatedReceipt) {
    return new ResponseEntity<>(
        receiptService.updateReceipt(receiptId, updatedReceipt), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(AdminPath + "/receipts/{receiptId}")
  public ResponseEntity<Void> deleteReceipt(@PathVariable("receiptId") Long receiptId) {
    receiptService.deleteReceipt(receiptId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(AdminPath + "/receipts")
  public ResponseEntity<Void> deleteMultipleReceipts(
      @RequestPart("receiptIds") List<Long> receiptIds) {
    receiptService.deleteMultipleReceipts(receiptIds);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
