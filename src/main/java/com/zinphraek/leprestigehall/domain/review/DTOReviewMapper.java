package com.zinphraek.leprestigehall.domain.review;

import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserMapper;
import com.zinphraek.leprestigehall.domain.user.UserService;
import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;

import java.time.format.DateTimeFormatter;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

public class DTOReviewMapper {

  public static ReviewDTO reviewToReviewDTO(
      Review review, UserService userService, UserMapper userMapper) {
    User user = userService.getUserById(review.getUserId());
    UserSummaryDTO userSummaryDTO = userMapper.fromUserToUserSummaryDTO(user);
    String lastEditedDate =
        review.getLastEditedDate() != null ? review.getLastEditedDate().toString() : null;
    String postedDate = review.getPostedDate().format(
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    return new ReviewDTO(
        review.getId(),
        review.getTitle(),
        review.getRating(),
        review.getComment(),
        postedDate,
        lastEditedDate,
        userSummaryDTO);
  }

  public static Review reviewDTOToReview(ReviewDTO reviewDTO) {
    Review review = new Review();
    review.setId(reviewDTO.id());
    review.setTitle(reviewDTO.title());
    review.setRating(reviewDTO.rating());
    review.setComment(reviewDTO.comment());
    review.setPostedDate(reviewDTO.postedDate());
    review.setUserId(reviewDTO.user().userId());

    if (reviewDTO.lastEditedDate() != null && !reviewDTO.lastEditedDate().isEmpty()) {
      review.setLastEditedDate(reviewDTO.lastEditedDate());
    }
    return review;
  }
}
