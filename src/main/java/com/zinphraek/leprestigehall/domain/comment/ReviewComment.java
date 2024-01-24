package com.zinphraek.leprestigehall.domain.comment;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_comments")
public class ReviewComment extends Comment {

  @NotNull private Long reviewId;

  @NotNull private Long basedCommentId;

  public ReviewComment() {
    super();
  }

  public ReviewComment(
      Long id,
      String content,
      String userId,
      LocalDateTime postedDate,
      boolean edited,
      Long reviewId,
      Long basedCommentId) {
    super(id, content, userId, postedDate, edited);
    this.reviewId = reviewId;
    this.basedCommentId = basedCommentId;
  }

  public Long getReviewId() {
    return reviewId;
  }

  public void setReviewId(Long reviewId) {
    this.reviewId = reviewId;
  }

  public Long getBasedCommentId() {
    return basedCommentId;
  }

  public void setBasedCommentId(Long basedCommentId) {
    this.basedCommentId = basedCommentId;
  }
}
