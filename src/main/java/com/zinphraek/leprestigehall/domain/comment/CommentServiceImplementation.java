package com.zinphraek.leprestigehall.domain.comment;

import com.zinphraek.leprestigehall.domain.event.Event;
import com.zinphraek.leprestigehall.domain.event.EventRepository;
import com.zinphraek.leprestigehall.domain.user.UserMapper;
import com.zinphraek.leprestigehall.domain.user.UserService;
import com.zinphraek.leprestigehall.domain.usersemotion.CommentLikesDislikes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;

@Service
public class CommentServiceImplementation implements CommentService {

  private final Logger logger = LogManager.getLogger(CommentServiceImplementation.class);
  @Autowired
  private final EventCommentRepository eventCommentRepository;
  @Autowired
  private final CommentMapper eventCommentMapper;
  @Autowired
  private final EventRepository eventRepository;
  @Autowired
  private final UserService userService;
  @Autowired
  private final UserMapper userMapper;

  public CommentServiceImplementation(
      EventCommentRepository eventCommentRepository,
      CommentMapper eventCommentMapper,
      EventRepository eventRepository,
      UserService userService,
      UserMapper userMapper) {
    this.eventCommentRepository = eventCommentRepository;
    this.eventCommentMapper = eventCommentMapper;
    this.eventRepository = eventRepository;
    this.userService = userService;
    this.userMapper = userMapper;
  }

  /**
   * @param id The id of the targeted event.
   * @return A list all comment related to an event.
   */
  @Override
  public List<EventCommentDTO> getAllCommentsByEventId(UUID id) {

    List<EventCommentDTO> eventCommentDTOList = new ArrayList<>();
    logger.info("Fetching all comments related to the event with id " + id + " ...");
    try {
      List<EventComment> eventComments = eventCommentRepository.findFirstLevelCommentsByEventId(id);
      if (!eventComments.isEmpty()) {
        eventComments.parallelStream()
            .forEach(
                eventComment ->
                    eventCommentDTOList.add(
                        eventCommentMapper.toEventCommentDTO(
                            eventComment, userMapper, userService)));
      }
      logger.info("All comments related to the event with id " + id + " successfully fetched.");
      return eventCommentDTOList;

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param id The id of the targeted event.
   * @return A list all comments (replies) related to a specific comment.
   */
  @Override
  public List<EventCommentDTO> getAllCommentsByBasedCommentId(Long id) {

    List<EventCommentDTO> eventCommentDTOList = new ArrayList<>();
    try {
      List<EventComment> eventComments = eventCommentRepository.findByBasedCommentId(id);
      if (!eventComments.isEmpty()) {
        eventComments.parallelStream()
            .forEach(
                eventComment ->
                    eventCommentDTOList.add(
                        eventCommentMapper.toEventCommentDTO(
                            eventComment, userMapper, userService)));
      }
      return eventCommentDTOList;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param id The targeted comment's id
   * @return The targeted comment or a not found response.
   */
  @Override
  public EventCommentDTO getEventCommentById(Long id) {

    try {
      Optional<EventComment> eventComment = eventCommentRepository.findById(id);
      if (eventComment.isPresent()) {
        return eventCommentMapper.toEventCommentDTO(eventComment.get(), userMapper, userService);
      }
      logger.info(String.format(GET_NOT_FOUND_MESSAGE, "event comment", id));
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "event comment", id));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Retrieve the comment entity from the database.
   *
   * @param id The targeted comment's id
   * @return The targeted comment or a not found response.
   */
  @Override
  public EventComment getEventCommentEntityById(Long id) {

    try {
      Optional<EventComment> eventComment = eventCommentRepository.findById(id);
      if (eventComment.isPresent()) {
        return eventComment.get();
      }
      logger.info(String.format(GET_NOT_FOUND_MESSAGE, "event comment", id));
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "event comment", id));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param eventCommentDTO The comment to save.
   * @return The newly saved comment.
   */
  @Override
  public EventCommentDTO saveEventComment(EventCommentDTO eventCommentDTO) {

    EventComment eventComment = eventCommentMapper.toEventComment(eventCommentDTO);
    CommentLikesDislikes commentLikesDislikes = new CommentLikesDislikes();
    eventComment.setCommentLikesDislikes(commentLikesDislikes);
    commentLikesDislikes.setEventComment(eventComment);
    try {
      eventCommentRepository.save(eventComment);
      Optional<Event> event = eventRepository.findById(eventComment.getEventId());
      if (event.isPresent()) {
        event.get().setCommentsCount(event.get().getCommentsCount() + 1);
        eventRepository.save(event.get());
      }
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "event comment"));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return eventCommentMapper.toEventCommentDTO(eventComment, userMapper, userService);
  }

  /**
   * @param id                 The targeted comment's id.
   * @param newEventCommentDTO The updated comment version
   * @return The newly updated comment.
   */
  @Override
  public EventCommentDTO updateEventComment(Long id, EventCommentDTO newEventCommentDTO) {

    EventComment newEventComment;
    try {
      if (!Objects.equals(id, newEventCommentDTO.id())) {
        logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "event comment"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "event comment"));
      }

      if (!eventCommentRepository.existsById(id)) {
        logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "event comment"));
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, String.format(UPDATE_NOT_FOUND_MESSAGE, "event comment"));
      }

      newEventComment = eventCommentMapper.toEventComment(newEventCommentDTO);
      logger.info("Updating comment with id " + id + " ...");
      eventCommentRepository.save(newEventComment);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Comment", id));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return eventCommentMapper.toEventCommentDTO(newEventComment, userMapper, userService);
  }

  /**
   * @param id The targeted comment's id.
   */
  @Override
  public void deleteEventComment(Long id) {

    try {
      if (!eventCommentRepository.existsById(id)) {
        logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "event comment"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }

      List<EventComment> replies = eventCommentRepository.findByBasedCommentId(id);
      Optional<EventComment> eventComment = eventCommentRepository.findById(id);

      eventCommentRepository.deleteById(id);
      eventCommentRepository.deleteAll(replies);
      if (eventComment.isPresent()) {
        Optional<Event> event = eventRepository.findById(eventComment.get().getEventId());
        if (event.isPresent()) {
          event.get().setCommentsCount(event.get().getCommentsCount() - replies.size());
          eventRepository.save(event.get());
        }
      }
      logger.info(String.format(DELETE_SUCCESS_MESSAGE, "event comment", id));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param id The targeted event's id.
   */
  @Override
  public void deleteCommentsByEventId(UUID id) {
    try {
      eventCommentRepository.deleteAll(eventCommentRepository.findByEventId(id));
      logger.info("All comments related to the event with id " + id + " successfully deleted.");
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param comments The list of comment to delete.
   */
  @Override
  public void deleteAllEventComment(List<EventComment> comments) {
    List<Long> existentComments = new ArrayList<>();
    StringBuilder nonexistentComments = new StringBuilder();

    try {
      comments.parallelStream()
          .forEach(
              comment -> {
                if (eventCommentRepository.existsById(comment.getId())) {
                  existentComments.add(comment.getId());
                } else {
                  nonexistentComments.append(comment.getId()).append(", ");
                }
              });

      if (!existentComments.isEmpty()) {
        existentComments.parallelStream().forEach(this::deleteEventComment);
      }

      if (!nonexistentComments.isEmpty()) {
        logger.error(
            String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "event comments", nonexistentComments));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "event comments", nonexistentComments));
      }
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}
