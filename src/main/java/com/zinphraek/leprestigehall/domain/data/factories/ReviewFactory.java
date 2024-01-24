package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.review.Review;
import com.zinphraek.leprestigehall.domain.review.ReviewDTO;
import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.zinphraek.leprestigehall.domain.review.DTOReviewMapper.reviewDTOToReview;

/**
 * Factory class for creating and manipulating Review and ReviewDTO objects.
 */
public class ReviewFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();
  UserFactory userFactory = new UserFactory();


  /**
   * Generates a random Review object.
   *
   * @param id                The ID of the review.
   * @param shouldBeInThePast Whether the review's posted date should be in the past.
   * @param shouldBeEdited    Whether the review should have an edit date.
   * @return A randomly generated Review object.
   */
  public Review generateRandomReview(Long id, boolean shouldBeInThePast, boolean shouldBeEdited) {
    Review review = new Review();
    LocalDateTime postedDateTime = shouldBeInThePast
        ? utilities.generateRandomPastLocalDateTime(0)
        : utilities.generateRandomFutureLocalDateTime(0);

    LocalDateTime lastEditedDate = shouldBeEdited
        ? postedDateTime.plusDays(utilities.getRandomPositiveLong(1, 20))
        : null;

    review.setId(id);
    review.setTitle(utilities.generateRandomStringWithDefinedLength(16));
    review.setRating(utilities.getRandomPositiveLong(1, 5));
    review.setComment(utilities.generateRandomString());
    review.setPostedDate(utilities.formatLocalDateTime(postedDateTime));
    review.setUserId(utilities.generateRandomStringWithDefinedLength(16));

    if (lastEditedDate != null) {
      review.setLastEditedDate(utilities.formatLocalDateTime(lastEditedDate));
    }
    return review;
  }

  /**
   * Generates a random ReviewDTO object.
   *
   * @param id                The ID of the review.
   * @param shouldBeInThePast Whether the review's posted date should be in the past.
   * @param shouldBeEdited    Whether the review should have an edit date.
   * @param shouldHaveUserId  Whether the review should have a user ID.
   * @param shouldHaveUser    Whether the review should have a user.
   * @return A randomly generated ReviewDTO object.
   */
  public ReviewDTO generateRandomReviewDTO(Long id, boolean shouldBeInThePast, boolean shouldBeEdited,
                                           boolean shouldHaveUserId, boolean shouldHaveUser) {
    LocalDateTime postedDateTime = shouldBeInThePast
        ? utilities.generateRandomPastLocalDateTime(0)
        : utilities.generateRandomFutureLocalDateTime(0);

    LocalDateTime lastEditedDate = shouldBeEdited
        ? postedDateTime.plusDays(utilities.getRandomPositiveLong(1, 20))
        : null;

    return new ReviewDTO(
        id, utilities.generateRandomString(), utilities.getRandomPositiveLong(1, 5),
        utilities.generateRandomString(), utilities.formatLocalDateTime(postedDateTime),
        lastEditedDate != null ? utilities.formatLocalDateTime(lastEditedDate) : null,
        shouldHaveUser ? userFactory.generateRandomUserSummaryDTO(shouldHaveUserId) : null
    );
  }

  /**
   * Generates a list of random ReviewDTO objects.
   *
   * @param size              The number of ReviewDTO objects to generate.
   * @param shouldBeInThePast Whether the reviews' posted dates should be in the past.
   * @param shouldBeEdited    Whether the reviews should have an edit date.
   * @param shouldHaveUserId  Whether the reviews should have user IDs.
   * @param shouldHaveUser    Whether the reviews should have users.
   * @return A list of randomly generated ReviewDTO objects.
   */
  public List<ReviewDTO> generateRandomReviewDTOs(int size, boolean shouldBeInThePast, boolean shouldBeEdited,
                                                  boolean shouldHaveUserId, boolean shouldHaveUser) {
    List<ReviewDTO> reviewDTOs = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      reviewDTOs.add(generateRandomReviewDTO(
          (long) i, shouldBeInThePast, shouldBeEdited, shouldHaveUserId, shouldHaveUser));
    }
    return reviewDTOs;
  }

  /**
   * Generates a list of random Review objects.
   *
   * @param size              The number of Review objects to generate.
   * @param shouldBeInThePast Whether the reviews' posted dates should be in the past.
   * @param shouldBeEdited    Whether the reviews should have an edit date.
   * @return A list of randomly generated Review objects.
   */
  public List<Review> generateListOfRandomReviews(int size, boolean shouldBeInThePast, boolean shouldBeEdited) {
    List<Review> reviews = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      reviews.add(generateRandomReview((long) i, shouldBeInThePast, shouldBeEdited));
    }
    return reviews;
  }

  /**
   * Updates the UserSummaryDTO of each ReviewDTO in a list.
   *
   * @param userSummaryDTO The UserSummaryDTO to update to each ReviewDTO by.
   * @param reviewDTOs     The list of ReviewDTO objects to update.
   * @return A list of updated ReviewDTO objects.
   */
  public List<ReviewDTO> updateReviewDTOUserSummaryDTO(UserSummaryDTO userSummaryDTO, List<ReviewDTO> reviewDTOs) {
    List<ReviewDTO> updatedReviewDTOs = new ArrayList<>();
    reviewDTOs.forEach(reviewDTO -> updatedReviewDTOs.add(new ReviewDTO(
        reviewDTO.id(), reviewDTO.title(), reviewDTO.rating(), reviewDTO.comment(),
        reviewDTO.postedDate(), reviewDTO.lastEditedDate(), userSummaryDTO)));
    return updatedReviewDTOs;
  }

  /**
   * Converts a list of ReviewDTO objects to Review objects.
   *
   * @param reviewDTOs The list of ReviewDTO objects to convert.
   * @return A list of converted Review objects.
   */
  public List<Review> convertReviewDTOsToReviews(List<ReviewDTO> reviewDTOs) {
    List<Review> reviews = new ArrayList<>();
    reviewDTOs.forEach(reviewDTO -> reviews.add(reviewDTOToReview(reviewDTO)));
    return reviews;
  }

  /**
   * Sorts a list of ReviewDTO objects by their posted dates.
   *
   * @param reviewList The list of Review objects to sort.
   * @param ascending  Whether the list should be sorted in ascending order.
   * @return A sorted list of ReviewDTO objects.
   */
  public List<Review> sortReviewDTOsByPostedDate(List<Review> reviewList, boolean ascending) {
    reviewList.sort((review1, review2) -> ascending
        ? review1.getPostedDate().compareTo(review2.getPostedDate())
        : review2.getPostedDate().compareTo(review1.getPostedDate()));
    return reviewList;
  }
}
