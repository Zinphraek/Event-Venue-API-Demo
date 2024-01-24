package com.zinphraek.leprestigehall.domain.review;

import com.google.common.collect.Ordering;
import com.zinphraek.leprestigehall.domain.data.factories.ReviewFactory;
import com.zinphraek.leprestigehall.domain.data.factories.UserFactory;
import com.zinphraek.leprestigehall.domain.user.*;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplementationTest {

  UserFactory userFactory = new UserFactory();
  ReviewFactory reviewFactory = new ReviewFactory();
  FactoriesUtilities utilities = new FactoriesUtilities();

  @Mock
  private UserMapper userMapper;
  @Mock
  private UserServiceImpl userService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewRepository reviewRepository;
  @InjectMocks
  private ReviewServiceImplementation reviewServiceImplementation;

  @BeforeEach
  void setUp() {
    reviewServiceImplementation = new ReviewServiceImplementation(reviewRepository, userRepository, userService, userMapper);
  }

  // ------------------------- getAllReviews -------------------------

  @Test
  void getAllReviewsUnpagedSuccessCase() {
    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);
    List<ReviewDTO> reviewDTOs = reviewFactory.updateReviewDTOUserSummaryDTO(userSummaryDTO,
        reviewFactory.generateRandomReviewDTOs(5, true, false,
            true, true));
    List<Review> reviews = reviewFactory.convertReviewDTOsToReviews(reviewDTOs);
    Page<ReviewDTO> reviewDTOsPage = new PageImpl<>(reviewDTOs);


    when(reviewRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(reviews));
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getAllReviews(new HashMap<>());

    assertEquals(reviewDTOsPage, fetchedReviewDTOsPage);
  }

  @Test
  void getAllReviewsThrowsDataAccessExceptionWhenFetchingReviewsFails() {
    when(reviewRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException("Data access error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getAllReviews(new HashMap<>())
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getAllReviewsThrowsRuntimeExceptionWhenFetchingReviewsFails() {
    when(reviewRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("Runtime error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getAllReviews(new HashMap<>())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getAllReviewsPaginationTest() {

    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);
    Page<Review> reviewsPage = new PageImpl<>(reviewFactory.generateListOfRandomReviews(5, true, false));

    when(reviewRepository.findAll(any(Pageable.class))).thenReturn(reviewsPage);
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Map<String, String> params = utilities.getCustomPageTestParams("postedDate", "5");

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getAllReviews(params);

    assertEquals(5, fetchedReviewDTOsPage.getContent().size());
  }

  @Test
  void getAllReviewsSortingTest() {
    List<Review> reviews = reviewFactory.sortReviewDTOsByPostedDate(reviewFactory
        .generateListOfRandomReviews(5, true, false), false);
    Page<Review> reviewsPage = new PageImpl<>(reviews);

    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);

    when(reviewRepository.findAll(any(Pageable.class))).thenReturn(reviewsPage);
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Map<String, String> params = utilities.getCustomPageTestParams("postedDate", "2");

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getAllReviews(params);

    assertTrue(Ordering.from(Comparator.comparing(ReviewDTO::postedDate)).reverse().isOrdered(fetchedReviewDTOsPage.getContent()));
  }

  // ------------------------- getAllReviewsByUserId -------------------------

  @Test
  void getAllReviewsByUserIdUnpagedSuccessCase() {
    String userId = utilities.generateRandomString();
    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);
    List<ReviewDTO> reviewDTOs = reviewFactory.updateReviewDTOUserSummaryDTO(userSummaryDTO,
        reviewFactory.generateRandomReviewDTOs(5, true, false,
            true, true));
    List<Review> reviews = reviewFactory.convertReviewDTOsToReviews(reviewDTOs);
    Page<ReviewDTO> reviewDTOsPage = new PageImpl<>(reviewDTOs);

    when(reviewRepository.findReviewsByUserId(any(), any(Pageable.class))).thenReturn(new PageImpl<>(reviews));
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getReviewsByUserId(userId, new HashMap<>());

    assertEquals(reviewDTOsPage, fetchedReviewDTOsPage);
  }

  @Test
  void getAllReviewsByUserIdThrowsDataAccessExceptionWhenFetchingReviewsFails() {
    String userId = utilities.generateRandomString();
    when(reviewRepository.findReviewsByUserId(any(), any(Pageable.class))).thenThrow(new DataAccessException("Data access error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewsByUserId(userId, new HashMap<>())
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getAllReviewsByUserIdThrowsRuntimeExceptionWhenFetchingReviewsFails() {
    String userId = utilities.generateRandomString();
    when(reviewRepository.findReviewsByUserId(any(), any(Pageable.class))).thenThrow(new RuntimeException("Runtime error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewsByUserId(userId, new HashMap<>())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getAllReviewsByUserIdPaginationTest() {
    String userId = utilities.generateRandomString();
    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);
    Page<Review> reviewsPage = new PageImpl<>(reviewFactory.generateListOfRandomReviews(5, true, false));

    when(reviewRepository.findReviewsByUserId(any(), any(Pageable.class))).thenReturn(reviewsPage);
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Map<String, String> params = utilities.getCustomPageTestParams("postedDate", "5");

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getReviewsByUserId(userId, params);

    assertEquals(5, fetchedReviewDTOsPage.getContent().size());
  }

  @Test
  void getAllReviewsByUserIdSortingTest() {
    String userId = utilities.generateRandomString();
    List<Review> reviews = reviewFactory.sortReviewDTOsByPostedDate(reviewFactory
        .generateListOfRandomReviews(5, true, false), false);
    Page<Review> reviewsPage = new PageImpl<>(reviews);

    UserSummaryDTO userSummaryDTO = userFactory.generateRandomUserSummaryDTO(true);

    when(reviewRepository.findReviewsByUserId(any(), any(Pageable.class))).thenReturn(reviewsPage);
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(userSummaryDTO);

    Map<String, String> params = utilities.getCustomPageTestParams("postedDate", "2");

    Page<ReviewDTO> fetchedReviewDTOsPage = reviewServiceImplementation.getReviewsByUserId(userId, params);

    assertTrue(Ordering.from(Comparator.comparing(ReviewDTO::postedDate)).reverse().isOrdered(fetchedReviewDTOsPage.getContent()));
  }

  // ------------------------- createReview -------------------------
  @Test
  void createReviewSuccessCase() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    User user = userFactory.generateRandomUser(reviewDTO.user().userId());

    when(userService.getUserById(any())).thenReturn(user);
    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(reviewDTO.user());

    ReviewDTO createdReviewDTO = reviewServiceImplementation.createReview(reviewDTO);

    assertEquals(reviewDTO, createdReviewDTO);
  }

  @Test
  void createReviewThrowsConflictExceptionWhenReviewExist() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    when(reviewRepository.existsById(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(CREATE_CONFLICT_MESSAGE1, "review", reviewDTO.id()), exception.getReason());
  }

  @Test
  void createReviewThrowsBadRequestExceptionWhenUserDoesNotExist() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, false);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "user"), exception.getReason());
  }

  @Test
  void createReviewThrowsBadRequestExceptionWhenUserIdIsNull() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, false, true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"), exception.getReason());
  }

  @Test
  void createReviewThrowsBadRequestExceptionWhenProvidedUserDoesNotExists() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "user", reviewDTO.user().userId()), exception.getReason());
  }

  @Test
  void createReviewThrowsDataAccessExceptionWhenSavingReviewFails() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.save(any())).thenThrow(new DataAccessException("Data access error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void createReviewThrowsRuntimeExceptionWhenSavingReviewFails() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.save(any())).thenThrow(new RuntimeException("Runtime error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.createReview(reviewDTO)
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }


  // ------------------------- updateReview -------------------------

  @Test
  void updateReviewSuccessCase() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    Review review = reviewFactory.generateRandomReview(reviewDTO.id(), true, false);
    review.setUserId(reviewDTO.user().userId());

    when(reviewRepository.existsById(any())).thenReturn(true);
    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.findById(any())).thenReturn(Optional.of(review));

    ReviewDTO createdReviewDTO = reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO);

    assertEquals(reviewDTO, createdReviewDTO);
  }

  @Test
  void updateReviewThrowsNotFoundExceptionWhenReviewDoesNotExist() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    when(reviewRepository.existsById(any())).thenReturn(false);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "review", reviewDTO.id()), exception.getReason());
  }

  @Test
  void updateReviewThrowsBadRequestExceptionWhenIdDoesNotMatch() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    Long differentId = utilities.generateRandomLong();

    when(reviewRepository.existsById(any())).thenReturn(true);
    when(userRepository.existsByUserId(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(differentId, reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(FIELD_MISMATCH_ERROR_MESSAGE, "The review's id " + reviewDTO.id()), exception.getReason());
  }

  @Test
  void updateReviewThrowsBadRequestExceptionWhenUserIdDoesNotMatch() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    Review review = reviewFactory.generateRandomReview(reviewDTO.id(), true, false);
    review.setUserId(utilities.generateRandomString());

    when(reviewRepository.existsById(any())).thenReturn(true);
    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.findById(any())).thenReturn(Optional.of(review));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(FIELD_MISMATCH_ERROR_MESSAGE, "The review's user id "
        + reviewDTO.user().userId()), exception.getReason());
  }

  @Test
  void updateReviewThrowsDataAccessExceptionWhenUpdatingReviewFails() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    Review review = reviewFactory.generateRandomReview(reviewDTO.id(), true, false);
    review.setUserId(reviewDTO.user().userId());

    when(reviewRepository.existsById(any())).thenReturn(true);
    when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.save(any())).thenThrow(new DataAccessException("Data access error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void updateReviewThrowsRuntimeExceptionWhenUpdatingReviewFails() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);
    Review review = reviewFactory.generateRandomReview(reviewDTO.id(), true, false);
    review.setUserId(reviewDTO.user().userId());

    when(reviewRepository.existsById(any())).thenReturn(true);
    when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
    when(userRepository.existsByUserId(any())).thenReturn(true);
    when(reviewRepository.save(any())).thenThrow(new RuntimeException("Runtime error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void updateReviewThrowsBadRequestExceptionWhenUserIdIsNull() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, false, true);

    when(reviewRepository.existsById(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "user id"), exception.getReason());
  }

  @Test
  void updateReviewThrowsBadRequestExceptionWhenUserDoesNotExist() {
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(utilities.generateRandomLong(),
        true, false, true, true);

    when(reviewRepository.existsById(any())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.updateReview(reviewDTO.id(), reviewDTO)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "user", reviewDTO.user().userId()), exception.getReason());
  }


  // ------------------------- deleteReview -------------------------

  @Test
  void deleteReviewSuccessCase() {
    Long reviewId = utilities.generateRandomLong();

    when(reviewRepository.existsById(any())).thenReturn(true);

    assertDoesNotThrow(() -> reviewServiceImplementation.deleteReview(reviewId));
  }

  @Test
  void deleteReviewThrowsNotFoundExceptionWhenReviewDoesNotExist() {
    Long reviewId = utilities.generateRandomLong();

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.deleteReview(reviewId)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "review", reviewId), exception.getReason());
  }

  @Test
  void deleteReviewThrowsDataAccessExceptionWhenDeletingReviewFails() {
    Long reviewId = utilities.generateRandomLong();

    when(reviewRepository.existsById(any())).thenReturn(true);
    doThrow(new DataAccessException("Data access error") {
    }).when(reviewRepository).deleteById(any());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.deleteReview(reviewId)
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void deleteReviewThrowsBadRequestExceptionWhenIdIsNull() {
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.deleteReview(null)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "id"), exception.getReason());
  }

  @Test
  void deleteReviewThrowsRuntimeExceptionWhenDeletingReviewFails() {

    when(reviewRepository.existsById(any())).thenReturn(true);
    doThrow(new RuntimeException("Runtime error") {
    }).when(reviewRepository).deleteById(any());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.deleteReview(utilities.generateRandomLong())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  // ------------------------- getReviewById -------------------------

  @Test
  void getReviewByIdSuccessCase() {
    Long reviewId = utilities.generateRandomLong();
    ReviewDTO reviewDTO = reviewFactory.generateRandomReviewDTO(reviewId, true, false, true, true);
    Review review = reviewFactory.generateRandomReview(reviewId, true, false);
    review.setUserId(reviewDTO.user().userId());
    review.setComment(reviewDTO.comment());
    review.setRating(reviewDTO.rating());
    review.setTitle(reviewDTO.title());
    review.setPostedDate(reviewDTO.postedDate());

    when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
    when(userMapper.fromUserToUserSummaryDTO(any())).thenReturn(reviewDTO.user());

    ReviewDTO fetchedReviewDTO = reviewServiceImplementation.getReviewById(reviewId);

    assertEquals(reviewDTO, fetchedReviewDTO);
  }

  @Test
  void getReviewByIdThrowsNotFoundExceptionWhenReviewDoesNotExist() {
    Long reviewId = utilities.generateRandomLong();

    when(reviewRepository.findById(any())).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewById(reviewId)
    );

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(GET_NOT_FOUND_MESSAGE, "review", reviewId), exception.getReason());
  }

  @Test
  void getReviewByIdThrowsDataAccessExceptionWhenFetchingReviewFails() {
    when(reviewRepository.findById(any())).thenThrow(new DataAccessException("Data access error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewById(utilities.generateRandomLong())
    );

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

  @Test
  void getReviewByIdThrowsBadRequestExceptionWhenIdIsNull() {

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewById(null)
    );

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(MISSING_FIELD_ERROR_MESSAGE, "id"), exception.getReason());
  }

  @Test
  void getReviewByIdThrowsRuntimeExceptionWhenFetchingReviewFails() {
    when(reviewRepository.findById(any())).thenThrow(new RuntimeException("Runtime error") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> reviewServiceImplementation.getReviewById(utilities.generateRandomLong())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
  }

}
