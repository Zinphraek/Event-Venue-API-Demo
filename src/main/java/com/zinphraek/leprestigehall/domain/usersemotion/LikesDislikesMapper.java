package com.zinphraek.leprestigehall.domain.usersemotion;

import org.springframework.stereotype.Component;

@Component
public class LikesDislikesMapper {

  public CommentLikesDislikes toCommentLikesDislikes(
      CommentLikesDislikesDTO commentLikesDislikesDTO) {
    CommentLikesDislikes commentLikesDislikes = new CommentLikesDislikes();

    commentLikesDislikes.setId(commentLikesDislikesDTO.id());
    commentLikesDislikes.updateUsersLikedAndDisliked(
        commentLikesDislikesDTO.userId(), commentLikesDislikesDTO.likeOrDislike());

    return commentLikesDislikes;
  }

  public EventLikesDislikes toEventLikesDislikes(EventLikesDislikesDTO eventLikesDislikesDTO) {
    EventLikesDislikes eventLikesDislikes = new EventLikesDislikes();

    eventLikesDislikes.setId(eventLikesDislikesDTO.id());
    eventLikesDislikes.updateUsersLikedAndDisliked(
        eventLikesDislikesDTO.userId(), eventLikesDislikesDTO.likeOrDislike());

    return eventLikesDislikes;
  }
}
