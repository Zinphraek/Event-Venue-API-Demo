package com.zinphraek.leprestigehall.domain.faq;

import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class FAQServiceImplementation implements FAQService {

  private final Logger logger = LogManager.getLogger(FAQServiceImplementation.class);
  @Autowired
  private FAQRepository faqRepository;

  public FAQServiceImplementation(FAQRepository faqRepository) {
    this.faqRepository = faqRepository;
  }

  /**
   * Fetches a page of FAQs.
   *
   * @param params The parameters to filter the results by.
   * @return A page of FAQs.
   */
  @Override
  @Transactional(readOnly = true)
  public Page<FAQ> getAllFAQs(Map<String, String> params) {
    Page<FAQ> faqs;
    Pageable pageable = Pageable.unpaged();

    logger.info("Fetching all FAQs.");
    try {
      if (!params.isEmpty()) {
        CustomPage customPage = createCustomPageFromParams(params);
        pageable =
            PageRequest.of(
                customPage.getPageNumber(),
                customPage.getPageSize(),
                customPage.getSortDirection(),
                customPage.getSortBy());
      }
      faqs = faqRepository.findAllAndFilter(
          params.getOrDefault("category", null),
          params.getOrDefault("question", null),
          pageable);

    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return faqs;
  }

  /**
   * Fetches the FAQ with the given id.
   *
   * @param id The id of the FAQ to retrieve.
   * @return The FAQ with the given id.
   */
  @Override
  public FAQ getFAQById(Long id) {

    try {
      logger.info("Fetching FAQ with id: " + id);
      Optional<FAQ> faq = faqRepository.findById(id);
      if (faq.isPresent()) {
        return faq.get();
      }
      logger.error(String.format(GET_NOT_FOUND_MESSAGE, "FAQ", id));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "FAQ", id));
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Creates a new FAQ.
   *
   * @param faq The FAQ to create.
   * @return The created FAQ.
   */
  @Override
  public FAQ createFAQ(FAQ faq) {

    try {
      logger.info("Creating FAQ...");
      faqRepository.save(faq);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "FAQ"));
      return faq;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Updates the FAQ with the given id.
   *
   * @param id  The id of the FAQ to update.
   * @param faq The FAQ to update.
   * @return The updated FAQ.
   */
  @Override
  public FAQ updateFAQ(Long id, FAQ faq) {

    try {
      logger.info("Updating FAQ with id: " + id);
      if (!Objects.equals(faq.getId(), id)) {
        logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "FAQ"));
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "FAQ"));
      }
      Optional<FAQ> faqToUpdate = faqRepository.findById(id);
      if (faqToUpdate.isPresent()) {
        faqToUpdate.get().setCategory(faq.getCategory());
        faqToUpdate.get().setQuestion(faq.getQuestion());
        faqToUpdate.get().setAnswer(faq.getAnswer());
        faqToUpdate.get().setMoreDetail(faq.getMoreDetail());
        faqRepository.save(faqToUpdate.get());
        logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "FAQ", id));
        return faqToUpdate.get();
      }
      logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "FAQ"));
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(UPDATE_NOT_FOUND_MESSAGE, "FAQ"));
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Deletes the FAQ with the given id.
   *
   * @param id The id of the FAQ to delete.
   */
  @Override
  public void deleteFAQ(Long id) {

    try {
      logger.info("Deleting FAQ with id: " + id);
      Optional<FAQ> faq = faqRepository.findById(id);
      if (faq.isPresent()) {
        faqRepository.delete(faq.get());
        logger.info(String.format(DELETE_SUCCESS_MESSAGE, "FAQ", id));
      } else {
        logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "FAQ"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(DELETE_NOT_FOUND_MESSAGE, "FAQ"));
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}
