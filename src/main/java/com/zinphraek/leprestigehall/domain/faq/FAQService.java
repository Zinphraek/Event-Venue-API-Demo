package com.zinphraek.leprestigehall.domain.faq;

import java.util.Map;
import org.springframework.data.domain.Page;

public interface FAQService {

  Page<FAQ> getAllFAQs(Map<String, String> params);

  FAQ getFAQById(Long id);

  FAQ createFAQ(FAQ faq);

  FAQ updateFAQ(Long id, FAQ faq);

  void deleteFAQ(Long id);
}
