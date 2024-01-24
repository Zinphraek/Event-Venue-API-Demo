package com.zinphraek.leprestigehall.domain.review;

import java.util.Map;
import org.springframework.data.domain.Page;

public interface ReviewService {

  Page<ReviewDTO> getAllReviews(Map<String, String> params);

  Page<ReviewDTO> getReviewsByUserId(String userId, Map<String, String> params);

  ReviewDTO getReviewById(Long id);

  ReviewDTO createReview(ReviewDTO reviewDTO);

  ReviewDTO updateReview(Long id, ReviewDTO reviewDTO);

  void deleteReview(Long id);
}
