package com.zinphraek.leprestigehall.domain.receipt;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ReceiptService {

  void emailReceipt(Long receiptId, ReceiptMailDTO receiptMailDTO);

  Page<Receipt> getAllReceipts(Map<String, String> params);

  Page<Receipt> getReceiptsByUserId(String userId, Map<String, String> params);

  Receipt getReceipt(Long receiptId);

  Receipt createReceipt(Receipt newReceipt);

  Receipt updateReceipt(Long receiptId, Receipt newReceipt);

  void deleteReceipt(Long receiptId);

  void deleteMultipleReceipts(List<Long> ids);
}
