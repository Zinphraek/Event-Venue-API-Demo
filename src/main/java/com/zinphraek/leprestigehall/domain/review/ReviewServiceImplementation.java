package com.zinphraek.leprestigehall.domain.review;

import com.zinphraek.leprestigehall.domain.user.UserMapper;
import com.zinphraek.leprestigehall.domain.user.UserRepository;
import com.zinphraek.leprestigehall.domain.user.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.domain.review.DTOReviewMapper.reviewToReviewDTO;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.buildPageRequestFromCustomPage;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;

@Service
public class ReviewServiceImplementation implements ReviewService {

  @Autowired
  private final ReviewRepository reviewRepository;
  @Autowired
  private final UserRepository userRepository;
  @Autowired
  private final UserService userService;
  @Autowired
  private final UserMapper userMapper;

  private final Logger logger = LogManager.getLogger(ReviewServiceImplementation.class);

  public ReviewServiceImplementation(
      ReviewRepository reviewRepository, UserRepository userRepository, UserService userService, UserMapper userMapper) {
    this.reviewRepository = reviewRepository;
    this.userRepository = userRepository;
    this.userService = userService;
    this.userMapper = userMapper;
  }

  /**
   * Get all reviews from the database.
   *
   * @param params - Map<String, String> - The query parameters.
   * @return Page<ReviewDTO> - The page of reviews.
   */
  @Override
  public Page<ReviewDTO> getAllReviews(Map<String, String> params) {

    logger.info("Fetching reviews...");
    try {
      Pageable pageable = params.isEmpty() ? Pageable.unpaged()
          : buildPageRequestFromCustomPage(createCustomPageFromParams(params));
      Page<Review> reviews = reviewRepository.findAll(pageable);
      Page<ReviewDTO> reviewsDTO =
          reviews.map(review -> reviewToReviewDTO(review, userService, userMapper));

      logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Reviews"));
      return reviewsDTO;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Get all reviews associated with a specific user.
   *
   * @param userId - String - The user id.
   * @param params - Map<String, String> - The query parameters.
   * @return Page<ReviewDTO> - The page of reviews.
   */
  @Override
  public Page<ReviewDTO> getReviewsByUserId(String userId, Map<String, String> params) {

    logger.info("Fetching reviews associated to user with id: " + userId);
    try {
      Pageable pageable = params.isEmpty() ? Pageable.unpaged()
          : buildPageRequestFromCustomPage(createCustomPageFromParams(params));
      Page<Review> reviews = reviewRepository.findReviewsByUserId(userId, pageable);
      Page<ReviewDTO> reviewsDTO =
          reviews.map(review -> reviewToReviewDTO(review, userService, userMapper));

      logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Reviews associated to user with id: " + userId));
      return reviewsDTO;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Fetches a review by its id
   *
   * @param id the id of the review to fetch
   * @return the review with the given id
   */
  @Override
  public ReviewDTO getReviewById(Long id) {
    try {
      if (id == null) {
        logger.error(String.format(MISSING_FIELD_ERROR_MESSAGE, "id"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(MISSING_FIELD_ERROR_MESSAGE, "id"));
      }

      Optional<Review> review = reviewRepository.findById(id);
      if (review.isEmpty()) {
        logger.error(String.format(GET_NOT_FOUND_MESSAGE, "review", id));
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "review", id));
      }

      logger.info(String.format(GET_SUCCESS_MESSAGE, "Review", id));
      return reviewToReviewDTO(review.get(), userService, userMapper);
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Creates a new review
   *
   * @param review the review to create
   * @return the created review
   */
  @Override
  public ReviewDTO createReview(ReviewDTO review) {

    if (review.id() != null && reviewRepository.existsById(review.id())) {
      logger.error(String.format(CREATE_CONFLICT_MESSAGE1, "review", review.id()));
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          String.format(CREATE_CONFLICT_MESSAGE1, "review", review.id()));
    }

    if (review.user() == null || review.user().userId() == null) {
      String reason = review.user() == null ? String.format(MISSING_FIELD_ERROR_MESSAGE, "user")
          : String.format(MISSING_FIELD_ERROR_MESSAGE, "user id");
      logger.error(reason);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, reason);
    }

    if (!review.user().userId().isEmpty() && !userRepository.existsByUserId(review.user().userId())) {
      logger.error(String.format(GET_NOT_FOUND_MESSAGE, "user", review.user().userId()));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(GET_NOT_FOUND_MESSAGE, "user", review.user().userId()));
    }

    Review reviewToCreate = DTOReviewMapper.reviewDTOToReview(review);

    try {
      reviewRepository.save(reviewToCreate);
      logger.info("Review with id: " + reviewToCreate.getId() + " successfully created.");
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return reviewToReviewDTO(reviewToCreate, userService, userMapper);
  }

  /**
   * Updates an existing review
   *
   * @param id     the id of the review to update
   * @param review the review with updated fields
   * @return the updated review
   */
  @Override
  public ReviewDTO updateReview(Long id, ReviewDTO review) {

    if (!reviewRepository.existsById(id)) {
      logger.error(String.format(GET_NOT_FOUND_MESSAGE, "review", id));
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "review", id));
    }

    if (review.user() == null || review.user().userId() == null) {
      String reason = review.user() == null ? String.format(MISSING_FIELD_ERROR_MESSAGE, "user")
          : String.format(MISSING_FIELD_ERROR_MESSAGE, "user id");
      logger.error(reason);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, reason);
    }

    if (!review.user().userId().isEmpty() && !userRepository.existsByUserId(review.user().userId())) {
      logger.error(String.format(GET_NOT_FOUND_MESSAGE, "user", review.user().userId()));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(GET_NOT_FOUND_MESSAGE, "user", review.user().userId()));
    }

    if (!Objects.equals(id, review.id())) {
      logger.error(
          String.format(UPDATE_NON_MATCHING_FIELD_MESSAGE, "id", "review", id, review.id()));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          String.format(FIELD_MISMATCH_ERROR_MESSAGE, "The review's id " + review.id()));
    }

    Optional<Review> reviewToUpdate = reviewRepository.findById(id);
    if (reviewToUpdate.isPresent()
        && !Objects.equals(reviewToUpdate.get().getUserId(), review.user().userId())) {
      logger.error(String.format(UPDATE_NON_MATCHING_FIELD_MESSAGE, "user id", "review",
          reviewToUpdate.get().getUserId(), review.user().userId()));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(FIELD_MISMATCH_ERROR_MESSAGE, "The review's user id " + review.user().userId()));
    }

    reviewToUpdate = Optional.of(DTOReviewMapper.reviewDTOToReview(review));
    try {
      reviewRepository.save(reviewToUpdate.get());
      logger.info("Review with id: " + review.id() + " successfully updated.");
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return review;
  }

  /**
   * Deletes a review
   *
   * @param id the id of the review to delete
   */
  @Override
  public void deleteReview(Long id) {

    if (id == null || !reviewRepository.existsById(id)) {
      String reason = id == null ? String.format(MISSING_FIELD_ERROR_MESSAGE, "id")
          : String.format(GET_NOT_FOUND_MESSAGE, "review", id);

      HttpStatusCode statusCode = id == null ? HttpStatus.BAD_REQUEST : HttpStatus.NOT_FOUND;
      logger.error(reason);
      throw new ResponseStatusException(
          statusCode, reason);
    }

    try {
      reviewRepository.deleteById(id);
      logger.info(String.format(DELETE_SUCCESS_MESSAGE, "Review", id));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}
