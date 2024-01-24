package com.zinphraek.leprestigehall.domain.usersemotion;

import com.zinphraek.leprestigehall.domain.comment.EventComment;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class CommentLikesDislikes extends LikesDislikes {

  @OneToOne(mappedBy = "commentLikesDislikes")
  private EventComment eventComment;

  public CommentLikesDislikes() {}

  public CommentLikesDislikes(
      Long id, String userId, String likeOrDislike, EventComment eventComment) {
    super(id, userId, likeOrDislike);
    this.eventComment = eventComment;
  }

  public EventComment getEventComment() {
    return eventComment;
  }

  public void setEventComment(EventComment eventComment) {
    this.eventComment = eventComment;
  }
}
