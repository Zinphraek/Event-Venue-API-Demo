package com.zinphraek.leprestigehall.domain.comment;

import static com.zinphraek.leprestigehall.domain.constants.Paths.EventCommentPath;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;

/** Endpoints to manage comments. */
@Controller
public class CommentController {

  @Autowired private final CommentServiceImplementation commentService;

  public CommentController(CommentServiceImplementation commentService) {
    this.commentService = commentService;
  }

  /**
   * Get all event comments.
   *
   * @return List of event comments.
   */
  @GetMapping(EventCommentPath)
  public ResponseEntity<List<EventCommentDTO>> getAllCommentsByEventId(@PathVariable UUID eventId) {
    return new ResponseEntity<>(commentService.getAllCommentsByEventId(eventId), HttpStatus.OK);
  }

  /**
   * Get all event comments by based comment id.
   *
   * @param id Based comment id.
   * @return List of event comments.
   */
  @GetMapping(EventCommentPath + "/{commentId}/replies")
  public ResponseEntity<List<EventCommentDTO>> getAllCommentsByBasedCommentId(
      @PathVariable("commentId") Long id) {
    return new ResponseEntity<>(commentService.getAllCommentsByBasedCommentId(id), HttpStatus.OK);
  }

  /**
   * Get event comment by id.
   *
   * @param id Event comment id.
   * @return Event comment.
   */
  @GetMapping(EventCommentPath + "/{commentId}")
  public ResponseEntity<EventCommentDTO> getEventCommentById(@PathVariable("commentId") Long id) {
    return new ResponseEntity<>(commentService.getEventCommentById(id), HttpStatus.OK);
  }

  /**
   * Save event comment.
   *
   * @param comment Event comment.
   * @return Created event comment.
   */
  @PostMapping(EventCommentPath)
  public ResponseEntity<EventCommentDTO> saveEventComment(
      @RequestPart("eventComment") EventCommentDTO comment) {
    return new ResponseEntity<>(commentService.saveEventComment(comment), HttpStatus.CREATED);
  }

  /**
   * Update event comment.
   *
   * @param id Event comment id.
   * @param comment Event comment.
   * @return Updated event comment.
   */
  @PreAuthorize("#comment.userId == authentication.principal.subject")
  @PutMapping(EventCommentPath + "/{commentId}")
  public ResponseEntity<EventCommentDTO> updateEventComment(
      @PathVariable("commentId") Long id, @RequestPart("eventComment") EventCommentDTO comment) {
    return new ResponseEntity<>(commentService.updateEventComment(id, comment), HttpStatus.OK);
  }

  /**
   * Delete event comment.
   *
   * @param id Event comment id.
   * @return No content.
   */
  @PreAuthorize("#comment.userId == authentication.principal.subject || hasRole('admin')")
  @DeleteMapping(EventCommentPath + "/{commentId}")
  public ResponseEntity<Void> deleteEventComment(@PathVariable("commentId") Long id) {
    commentService.deleteEventComment(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
