package com.zinphraek.leprestigehall.domain.review;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

@Entity
@Table(name = "reviews")
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "Title is required")
  private String title;

  @NotNull(message = "Rating is required")
  private Long rating;

  @NotNull(message = "Comment is required")
  @Size(message = "Must not exceed 2000", max = 2000)
  private String comment;

  @NotNull(message = "Posted date is required")
  private LocalDateTime postedDate;

  private LocalDateTime lastEditedDate;

  @NotBlank(message = "User ID is required")
  private String userId;

  public Review(Long id, String title, Long rating, String comment,
                String postedDate, String lastEditedDate, String userId) {
    this.id = id;
    this.title = title;
    this.rating = rating;
    this.comment = comment;
    this.postedDate = LocalDateTime.parse(postedDate,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.lastEditedDate = LocalDateTime.parse(lastEditedDate,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getRating() {
    return rating;
  }

  public void setRating(Long rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public LocalDateTime getPostedDate() {
    return postedDate;
  }

  public void setPostedDate(CharSequence postedDate) {
    this.postedDate = LocalDateTime.parse(postedDate,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public LocalDateTime getLastEditedDate() {
    return lastEditedDate;
  }

  public void setLastEditedDate(CharSequence lastEditedDate) {
    this.lastEditedDate = LocalDateTime.parse(lastEditedDate,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }


  public String getUserId() {
    return userId;
  }

  public void setUserId(String user) {
    this.userId = user;
  }

  public Review() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Review review)) {
      return false;
    }
    return Objects.equals(id, review.id) && title.equals(review.title) && rating.equals(
        review.rating) && comment.equals(review.comment) && postedDate.equals(
        review.postedDate) && Objects.equals(lastEditedDate, review.lastEditedDate)
        && Objects.equals(userId, review.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, rating, comment, postedDate, lastEditedDate, userId);
  }

  @Override
  public String toString() {
    return "Review{" +
        "id=" + id +
        ", title='" + title + '\'' +
        ", rating=" + rating +
        ", comment='" + comment + '\'' +
        ", postedDate=" + postedDate +
        ", lastEditedDate=" + lastEditedDate +
        ", userId='" + userId + '\'' +
        '}';
  }
}
