package com.zinphraek.leprestigehall.domain.comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {

  List<EventCommentDTO> getAllCommentsByEventId(UUID id);

  List<EventCommentDTO> getAllCommentsByBasedCommentId(Long id);

  EventCommentDTO getEventCommentById(Long id);

  EventComment getEventCommentEntityById(Long id);

  EventCommentDTO saveEventComment(EventCommentDTO eventCommentDTO);

  EventCommentDTO updateEventComment(Long id, EventCommentDTO newEventCommentDTO);

  void deleteEventComment(Long id);

  void deleteCommentsByEventId(UUID id);

  void deleteAllEventComment(List<EventComment> comments);
}
