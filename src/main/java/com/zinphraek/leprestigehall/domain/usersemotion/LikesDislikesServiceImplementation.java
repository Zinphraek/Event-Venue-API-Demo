package com.zinphraek.leprestigehall.domain.usersemotion;

import com.zinphraek.leprestigehall.domain.comment.EventCommentRepository;
import com.zinphraek.leprestigehall.domain.event.EventRepository;
import com.zinphraek.leprestigehall.domain.user.UserRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikesDislikesServiceImplementation implements LikesDislikesService {

  private final Logger logger = LogManager.getLogger(LikesDislikesServiceImplementation.class);

  @Autowired private final UserRepository userRepository;

  @Autowired private final EventRepository eventRepository;

  @Autowired private final EventCommentRepository eventCommentRepository;

  @Autowired private final EventLikesDislikesRepository eventLikesDislikesRepository;

  @Autowired private final CommentLikesDislikesRepository commentLikesDislikesRepository;

  @Autowired private final LikesDislikesMapper likesDislikesMapper;

  public LikesDislikesServiceImplementation(
      UserRepository userRepository,
      EventRepository eventRepository,
      EventCommentRepository eventCommentRepository,
      EventLikesDislikesRepository eventLikesDislikesRepository,
      CommentLikesDislikesRepository commentLikesDislikesRepository,
      LikesDislikesMapper likesDislikesMapper) {
    this.userRepository = userRepository;
    this.eventRepository = eventRepository;
    this.eventCommentRepository = eventCommentRepository;
    this.eventLikesDislikesRepository = eventLikesDislikesRepository;
    this.commentLikesDislikesRepository = commentLikesDislikesRepository;
    this.likesDislikesMapper = likesDislikesMapper;
  }

  /**
   * Check if the user exists in the database.
   *
   * @param userId The user id.
   */
  private void checkIfUserExists(String userId) {
    if (userId != null && !userRepository.existsByUserId(userId)) {
      logger.error("Operation not allowed for non registered users.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Operation not allowed for non registered users.");
    }
  }

  /**
   * Check if a specific event exists in the database.
   *
   * @param eventId The specified event id.
   */
  private void checkIfEventExists(UUID eventId) {

    if (eventId != null && !eventRepository.existsById(eventId)) {
      logger.error("The eventLikesDislikes object must be associated to an existing event.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "The eventLikesDislikes object must be associated to an existing event.");
    }
  }

  /**
   * Check if a specific comment exists in the database.
   *
   * @param commentId The specified comment id.
   */
  private void checkIfCommentExists(Long commentId) {

    if (commentId != null && !eventCommentRepository.existsById(commentId)) {
      logger.error("The commentLikesDislikes object must be associated to an existing comment.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "The commentLikesDislikes object must be associated to an existing comment.");
    }
  }

  /**
   * @param id The target object identifier.
   * @return The targeted users' emotion state.
   */
  @Override
  public EventLikesDislikes getEventLikesDislikesById(Long id) {
    try {
      Optional<EventLikesDislikes> optionalEventLikesDislikes =
          eventLikesDislikesRepository.findById(id);
      if (optionalEventLikesDislikes.isPresent()) {
        logger.info(
            "Likes dislikes object associated to event with id "
                + optionalEventLikesDislikes.get().getEvent()
                + " successfully retrieved.");
        return optionalEventLikesDislikes.get();
      }
      logger.error("No eventLikesDislikes object with that id exist in the database.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (DataAccessException e) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getLocalizedMessage());
    }
  }

  /**
   * Retrieves the collective users' emotion state associated to a comment.
   *
   * @param id The target object identifier.
   * @return The targeted users' emotion state.
   */
  @Override
  public CommentLikesDislikes getCommentLikesDislikesById(Long id) {

    try {
      Optional<CommentLikesDislikes> optionalCommentLikesDislikes =
          commentLikesDislikesRepository.findById(id);
      if (optionalCommentLikesDislikes.isPresent()) {
        logger.info(
            "Likes dislikes object associated with id "
                + optionalCommentLikesDislikes.get().getId()
                + " successfully retrieved.");
        return optionalCommentLikesDislikes.get();
      }
      logger.error("No commentLikesDislikes object with that id exist in the database.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } catch (DataAccessException e) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getLocalizedMessage());
    }
  }

  /**
   * @param eventLikesDislikesDTO The collective users' emotion state to save.
   * @return The newly saved collective users' emotion state.
   */
  @Override
  public EventLikesDislikes saveEventLikesDislikes(EventLikesDislikesDTO eventLikesDislikesDTO) {

    checkIfUserExists(eventLikesDislikesDTO.userId());
    checkIfEventExists(eventLikesDislikesDTO.eventId());

    EventLikesDislikes eventLikesDislikes =
        likesDislikesMapper.toEventLikesDislikes(eventLikesDislikesDTO);

    eventLikesDislikes.setEvent(
        eventRepository.findById(eventLikesDislikesDTO.eventId()).orElse(null));

    try {
      eventLikesDislikesRepository.save(eventLikesDislikes);
      logger.info(
          "Likes dislikes object associated to event with id "
              + eventLikesDislikes.getEvent().getId()
              + " successfully saved.");
    } catch (DataAccessException dae) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    }
    return eventLikesDislikes;
  }

  /**
   * Persists the collective users' emotion state toward a specific comment.
   *
   * @param commentLikesDislikesDTO The collective users' emotion state to save.
   * @return The newly saved collective users' emotion state.
   */
  @Override
  public CommentLikesDislikes saveCommentLikesDislikes(
      CommentLikesDislikesDTO commentLikesDislikesDTO) {

    checkIfUserExists(commentLikesDislikesDTO.userId());
    checkIfCommentExists(commentLikesDislikesDTO.commentId());

    CommentLikesDislikes commentLikesDislikes =
        likesDislikesMapper.toCommentLikesDislikes(commentLikesDislikesDTO);

    commentLikesDislikes.setEventComment(
        eventCommentRepository.findById(commentLikesDislikesDTO.commentId()).orElse(null));

    try {
      commentLikesDislikesRepository.save(commentLikesDislikes);
      logger.info(
          "Likes dislikes object associated with id "
              + commentLikesDislikes.getId()
              + " successfully saved.");
    } catch (DataAccessException dae) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    }
    return commentLikesDislikes;
  }

  /**
   * @param eventLikesDislikesDTO The users collective emotion toward a specific event.
   * @return The collective users emotional state toward the targeted event.
   */
  @Override
  public EventLikesDislikes updateEventLikesDislikes(
      Long id, EventLikesDislikesDTO eventLikesDislikesDTO) {

    if (!Objects.equals(eventLikesDislikesDTO.id(), id)) {
      logger.error("The id in the path and the id in the body do not match.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The id in the path and the id in the body do not match.");
    }

    checkIfUserExists(eventLikesDislikesDTO.userId());
    checkIfEventExists(eventLikesDislikesDTO.eventId());

    Optional<EventLikesDislikes> existingLikesDislikes = eventLikesDislikesRepository.findById(id);

    // Check if the emotion state is associated to the same event
    if (existingLikesDislikes.isPresent()
        && !Objects.equals(
            existingLikesDislikes.get().getEvent().getId(), eventLikesDislikesDTO.eventId())) {
      logger.error("Cannot update emotion state for another event");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update emotion state for another event.");
    }

    if (existingLikesDislikes.isPresent()) {

      try {
        EventLikesDislikes eventLikesDislikes = existingLikesDislikes.get();
        eventLikesDislikes.updateUsersLikedAndDisliked(
            eventLikesDislikesDTO.userId(), eventLikesDislikesDTO.likeOrDislike());
        eventLikesDislikesRepository.save(eventLikesDislikes);
        logger.info(
            "Likes dislikes object associated to event with id "
                + eventLikesDislikes.getEvent().getId().toString()
                + " successfully updated.");
        return eventLikesDislikes;

      } catch (DataAccessException dae) {
        logger.error("Oops, something unexpected happened. Please try again later.");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
      }
    } else {
      logger.error("No likes dislikes object with that id exist in the database.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Updates the collective users' emotion state toward a specific comment.
   *
   * @param id The target object identifier.
   * @param commentLikesDislikesDTO The collective users' emotion state to update.
   * @return The updated collective users' emotion state.
   */
  @Override
  public CommentLikesDislikes updateCommentLikesDislikes(
      Long id, CommentLikesDislikesDTO commentLikesDislikesDTO) {

    if (!Objects.equals(id, commentLikesDislikesDTO.id())) {
      logger.error("Parameter id and emotion id do not match.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Parameter id and emotion id do not match.");
    }

    checkIfUserExists(commentLikesDislikesDTO.userId());
    checkIfCommentExists(commentLikesDislikesDTO.commentId());

    Optional<CommentLikesDislikes> existingLikesDislikes =
        commentLikesDislikesRepository.findById(id);

    if (existingLikesDislikes.isPresent()
        && !Objects.equals(
            existingLikesDislikes.get().getEventComment().getId(),
            commentLikesDislikesDTO.commentId())) {
      logger.error("Cannot update emotion state for another comment.");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Cannot update emotion state for another comment.");
    }

    if (existingLikesDislikes.isPresent()) {
    try {
        CommentLikesDislikes likesDislikes = existingLikesDislikes.get();
        likesDislikes.updateUsersLikedAndDisliked(
            commentLikesDislikesDTO.userId(), commentLikesDislikesDTO.likeOrDislike());

      commentLikesDislikesRepository.save(likesDislikes);
      logger.info("Emotion state successfully updated.");
      return likesDislikes;
    } catch (DataAccessException dae) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    }
    } else {
      logger.error("No likes dislikes object with that id exist in the database.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * @param id The users emotions' id to delete.
   */
  @Override
  public void deleteEventLikesDislikes(Long id) {
    if (!eventLikesDislikesRepository.existsById(id)) {
      logger.error("Cannot delete non existent collection of user emotion state.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    try {
      eventLikesDislikesRepository.deleteById(id);
      logger.info("Emotion with id " + id + " successfully deleted.");
    } catch (DataAccessException dae) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    }
  }

  /**
   * Deletes the collective users' emotion state toward a specific comment.
   *
   * @param id The users emotions' id to delete.
   */
  @Override
  public void deleteCommentLikesDislikes(Long id) {
    if (!commentLikesDislikesRepository.existsById(id)) {
      logger.error("Cannot delete non existent collection of user emotion state.");
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    try {
      commentLikesDislikesRepository.deleteById(id);
      logger.info("Emotion with id " + id + " successfully deleted.");
    } catch (DataAccessException dae) {
      logger.error("Oops, something unexpected happened. Please try again later.");
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    }
  }
}
