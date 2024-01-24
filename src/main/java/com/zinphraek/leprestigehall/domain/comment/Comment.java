package com.zinphraek.leprestigehall.domain.comment;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Comment content is required")
  private String content;

  @NotNull(message = "User ID is required")
  private String userId;

  private LocalDateTime postedDate;

  private boolean edited;

  public Comment() {}

  public Comment(Long id, String content, String userId) {
    this.id = id;
    this.content = content;
    this.userId = userId;
  }

  public Comment(Long id, String content, String userId, LocalDateTime postedDate, boolean edited) {
    this.id = id;
    this.content = content;
    this.userId = userId;
    this.postedDate = postedDate;
    this.edited = edited;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public LocalDateTime getPostedDate() {
    return postedDate;
  }

  public void setPostedDate(LocalDateTime postedDate) {
    this.postedDate = postedDate;
  }

  public boolean isEdited() {
    return edited;
  }

  public void setEdited(boolean edited) {
    this.edited = edited;
  }
}
