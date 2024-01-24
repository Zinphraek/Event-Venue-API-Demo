package com.zinphraek.leprestigehall.domain.usersemotion;

public interface LikesDislikesService {

  EventLikesDislikes getEventLikesDislikesById(Long id);

  CommentLikesDislikes getCommentLikesDislikesById(Long id);

  EventLikesDislikes saveEventLikesDislikes(EventLikesDislikesDTO eventLikesDislikesDTO);

  CommentLikesDislikes saveCommentLikesDislikes(CommentLikesDislikesDTO commentLikesDislikesDTO);

  EventLikesDislikes updateEventLikesDislikes(Long id, EventLikesDislikesDTO eventLikesDislikesDTO);

  CommentLikesDislikes updateCommentLikesDislikes(
      Long id, CommentLikesDislikesDTO commentLikesDislikesDTO);

  void deleteEventLikesDislikes(Long id);

  void deleteCommentLikesDislikes(Long id);
}
