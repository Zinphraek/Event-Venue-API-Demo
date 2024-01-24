package com.zinphraek.leprestigehall.domain.comment;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.zinphraek.leprestigehall.domain.usersemotion.CommentLikesDislikes;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EventComment extends Comment {

  @NotNull(message = "Event ID is required")
  private UUID eventId;

  private Long basedCommentId;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "comment_likes_dislikes_id", referencedColumnName = "id")
  private CommentLikesDislikes commentLikesDislikes;

  public EventComment() {
    super();
  }

  public EventComment(
      Long id,
      String content,
      String userId,
      UUID eventId,
      Long basedCommentId,
      LocalDateTime postedDate,
      boolean edited) {
    super(id, content, userId, postedDate, edited);
    this.eventId = eventId;
    this.basedCommentId = basedCommentId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }

  public Long getBasedCommentId() {
    return basedCommentId;
  }

  public void setBasedCommentId(Long basedCommentId) {
    this.basedCommentId = basedCommentId;
  }

  public CommentLikesDislikes getCommentLikesDislikes() {
    return commentLikesDislikes;
  }

  public void setCommentLikesDislikes(CommentLikesDislikes commentLikesDislikes) {
    this.commentLikesDislikes = commentLikesDislikes;
  }
}
