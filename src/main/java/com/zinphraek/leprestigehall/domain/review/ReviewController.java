package com.zinphraek.leprestigehall.domain.review;

import static com.zinphraek.leprestigehall.domain.constants.Paths.ReviewPath;
import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;

@Controller
public class ReviewController {

  private final ReviewServiceImplementation reviewService;

  public ReviewController(ReviewServiceImplementation reviewService) {
    this.reviewService = reviewService;
  }

  @GetMapping(ReviewPath)
  public ResponseEntity<Page<ReviewDTO>> getAllReviews(Map<String, String> params) {
    return ResponseEntity.ok(reviewService.getAllReviews(params));
  }

  @GetMapping(ReviewPath + "/{id}")
  public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
    return ResponseEntity.ok(reviewService.getReviewById(id));
  }

  @GetMapping(UserPath + "/{userId}" + ReviewPath)
  public ResponseEntity<Page<ReviewDTO>> getReviewsByUserId(
      @PathVariable String userId, Map<String, String> params) {
    return ResponseEntity.ok(reviewService.getReviewsByUserId(userId, params));
  }

  @PostMapping(ReviewPath)
  public ResponseEntity<ReviewDTO> createReview(@RequestPart("review") ReviewDTO review) {
    return new ResponseEntity<>(reviewService.createReview(review), HttpStatus.CREATED);
  }

  @PutMapping(ReviewPath + "/{id}")
  public ResponseEntity<ReviewDTO> updateReview(
      @PathVariable Long id, @RequestPart("review") ReviewDTO review) {
    return ResponseEntity.ok(reviewService.updateReview(id, review));
  }

  @DeleteMapping(ReviewPath + "/{id}")
  public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
    reviewService.deleteReview(id);
    return ResponseEntity.noContent().build();
  }
}
