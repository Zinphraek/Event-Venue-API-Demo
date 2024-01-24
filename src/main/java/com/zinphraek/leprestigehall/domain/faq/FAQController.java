package com.zinphraek.leprestigehall.domain.faq;

import static com.zinphraek.leprestigehall.domain.constants.Paths.FAQPath;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@Controller
@RequestMapping(FAQPath)
public class FAQController {

  @Autowired private final FAQServiceImplementation faqService;

  public FAQController(FAQServiceImplementation faqService) {
    this.faqService = faqService;
  }

  @GetMapping
  public ResponseEntity<Page<FAQ>> getFAQs(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(faqService.getAllFAQs(params), HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FAQ> getFAQ(@PathVariable Long id) {
    return new ResponseEntity<>(faqService.getFAQById(id), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @PostMapping
  public ResponseEntity<FAQ> saveFAQ(@RequestPart("faq") FAQ faq) {
    return new ResponseEntity<>(faqService.createFAQ(faq), HttpStatus.CREATED);
  }

  @PreAuthorize("hasRole('admin')")
  @PutMapping("/{id}")
  public ResponseEntity<FAQ> updateFAQ(@PathVariable Long id, @RequestPart("faq") FAQ faq) {
    return new ResponseEntity<>(faqService.updateFAQ(id, faq), HttpStatus.OK);
  }

  @PreAuthorize("hasRole('admin')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
    faqService.deleteFAQ(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
