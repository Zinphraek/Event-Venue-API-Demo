package com.zinphraek.leprestigehall.domain.comment;

import com.zinphraek.leprestigehall.domain.user.UserMapper;
import com.zinphraek.leprestigehall.domain.user.UserService;
import com.zinphraek.leprestigehall.domain.user.UserSummaryDTO;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

  public EventCommentDTO toEventCommentDTO(
      EventComment eventComment, UserMapper userMapper, UserService userService) {
    UserSummaryDTO userSummaryDTO =
        userMapper.fromUserToUserSummaryDTO(userService.getUserById(eventComment.getUserId()));
    return new EventCommentDTO(
        eventComment.getId(),
        eventComment.getContent(),
        userSummaryDTO,
        eventComment.getEventId(),
        eventComment.getBasedCommentId(),
        eventComment.getCommentLikesDislikes(),
        eventComment.getPostedDate(),
        eventComment.isEdited());
  }

  public EventComment toEventComment(EventCommentDTO eventCommentDTO) {
    EventComment eventComment = new EventComment();
    eventComment.setId(eventCommentDTO.id());
    eventComment.setContent(eventCommentDTO.content());
    eventComment.setUserId(eventCommentDTO.user().userId());
    eventComment.setEventId(eventCommentDTO.eventId());
    eventComment.setBasedCommentId(eventCommentDTO.basedCommentId());
    eventComment.setCommentLikesDislikes(eventCommentDTO.likesDislikes());
    eventComment.setPostedDate(eventCommentDTO.postedDate());
    eventComment.setEdited(eventCommentDTO.edited());
    return eventComment;
  }
}
