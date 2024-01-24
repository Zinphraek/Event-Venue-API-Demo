package com.zinphraek.leprestigehall.domain.event;

import com.zinphraek.leprestigehall.domain.comment.CommentServiceImplementation;
import com.zinphraek.leprestigehall.domain.media.EventMedia;
import com.zinphraek.leprestigehall.domain.media.MediaService;
import com.zinphraek.leprestigehall.domain.usersemotion.EventLikesDislikes;
import com.zinphraek.leprestigehall.domain.usersemotion.EventLikesDislikesRepository;
import com.zinphraek.leprestigehall.utilities.helpers.CustomMultipartFile;
import com.zinphraek.leprestigehall.utilities.helpers.CustomPage;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.createCustomPageFromParams;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.generateFileName;

@Service
public class EventServiceImplementation implements EventService {

  private final Logger logger = LogManager.getLogger(EventServiceImplementation.class);

  @Autowired
  private final EventRepository eventRepository;

  @Autowired
  private final CommentServiceImplementation commentService;
  @Autowired
  private final EventLikesDislikesRepository userSentimentRepository;

  @Autowired
  private final MediaService mediaService;

  @Autowired
  private final EventMapper eventMapper;

  public EventServiceImplementation(
      EventMapper eventMapper,
      MediaService mediaService,
      EventRepository eventRepository,
      CommentServiceImplementation commentService,
      EventLikesDislikesRepository userSentimentRepository) {
    this.userSentimentRepository = userSentimentRepository;
    this.eventRepository = eventRepository;
    this.commentService = commentService;
    this.mediaService = mediaService;
    this.eventMapper = eventMapper;

  }

  /**
   * Generate a tuple containing a customPage entity and an EventFilterCriteria.
   *
   * @param params The sorting and filtering options
   * @return A tuple containing a customPage entity and the eventFilterCriteria.
   */
  private Pair<CustomPage, EventFilterCriteria> generateCustomPageAndEventFilterCriteria(
      Map<String, String> params) {

    CustomPage customPage = createCustomPageFromParams(params);
    EventFilterCriteria eventFilterCriteria = new EventFilterCriteria();

    // Setting up the eventFilterCriteria  with the corresponding value in the param object.

    if (params.containsKey("title")) {
      eventFilterCriteria.setTitle(params.get("title"));
    }

    if (params.containsKey("maxLikes")) {
      eventFilterCriteria.setMaxLikes(Integer.parseInt(params.get("maxLikes")));
    }

    if (params.containsKey("minLikes")) {
      eventFilterCriteria.setMinLikes(Integer.parseInt(params.get("minLikes")));
    }

    if (params.containsKey("maxDislikes")) {
      eventFilterCriteria.setMaxDislikes(Integer.parseInt(params.get("maxDislikes")));
    }

    if (params.containsKey("minDislikes")) {
      eventFilterCriteria.setMinDislikes(Integer.parseInt(params.get("minDislikes")));
    }

    return Pair.of(customPage, eventFilterCriteria);
  }

  /**
   * Persist the media files associated with an event.
   *
   * @param event          The event to associate the media files with.
   * @param multipartFiles The collection of media files to persist.
   */
  private void persistEventMedia(Event event, Collection<MultipartFile> multipartFiles) {
    if (multipartFiles != null && !multipartFiles.isEmpty()) {
      multipartFiles.parallelStream()
          .forEach(
              multipartFile -> {
                if (multipartFile != null) {
                  try {
                    String baseName = event.getId().toString() + Objects.requireNonNull(multipartFile.getOriginalFilename()).split("\\.")[0];
                    String mediaFileName = generateFileName(baseName, multipartFile);
                    mediaService.saveEventMedia(new CustomMultipartFile(multipartFile, mediaFileName), event.getId());
                  } catch (IllegalAccessError | IOException ia_io_e) {
                    logger.error(IllegalAccessError_IOException_LOG_MESSAGE, ia_io_e);
                    throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
                  }
                }
              });
    }
  }

  /**
   * Fetch all events from the database.
   *
   * @param params The sorting and filtering options
   * @return - A list of events
   */
  @Override
  public Page<Event> getAllEvents(Map<String, String> params) {
    Page<Event> events;
    Pageable pageable = Pageable.unpaged();
    Pair<CustomPage, EventFilterCriteria> pageEventFilterCriteriaPair =
        Pair.of(new CustomPage(), new EventFilterCriteria());

    logger.info("Fetching events...");
    try {
      if (!params.isEmpty()) {
        pageEventFilterCriteriaPair =
            generateCustomPageAndEventFilterCriteria(params);

        pageable =
            PageRequest.of(
                pageEventFilterCriteriaPair.getFirst().getPageNumber(),
                pageEventFilterCriteriaPair.getFirst().getPageSize(),
                pageEventFilterCriteriaPair.getFirst().getSortDirection(),
                pageEventFilterCriteriaPair.getFirst().getSortBy());
      }
      events =
          eventRepository.findAllAndFilter(
              pageEventFilterCriteriaPair.getSecond().getTitle(),
              pageEventFilterCriteriaPair.getSecond().getMinLikes(),
              pageEventFilterCriteriaPair.getSecond().getMaxLikes(),
              pageEventFilterCriteriaPair.getSecond().getMinDislikes(),
              pageEventFilterCriteriaPair.getSecond().getMaxDislikes(),
              pageable);

    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    logger.info(String.format(BULK_GET_SUCCESS_MESSAGE, "Events"));
    return events;
  }

  /**
   * @param id The event's id to retrieve.
   * @return The retrieved event.
   */
  @Override
  public Event getEvent(UUID id) {

    try {
      Optional<Event> optionalEvent = eventRepository.findById(id);
      if (optionalEvent.isPresent()) {
        logger.info(String.format(GET_SUCCESS_MESSAGE, "Event", id));
        return optionalEvent.get();
      }
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    logger.info(String.format(GET_NOT_FOUND_MESSAGE, "Event", id));
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "Event", id));
  }

  /**
   * Persist an event in the database.
   *
   * @param eventDTO       The event to persist.
   * @param multipartFiles The media files associated with the event
   * @return The newly saved event.
   */
  @Override
  public Event createEvent(EventDTO eventDTO, Collection<MultipartFile> multipartFiles) {

    Event newEvent = eventMapper.fromEventDTOToEvent(eventDTO);

    try {
      if (newEvent.getId() != null && eventRepository.existsById(newEvent.getId())) {
        logger.error(String.format(CREATE_CONFLICT_MESSAGE2, "event", newEvent.getId()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(CREATE_CONFLICT_MESSAGE2, "event", newEvent.getId()));
      }

      if (eventRepository.existsByTitle(newEvent.getTitle())) {
        logger.error(String.format(FIELD_CONFLICT_MESSAGE2, "event", "title", newEvent.getTitle()));
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            String.format(FIELD_CONFLICT_ERROR_MESSAGE, "title"));
      }

      // Setting the likes and dislikes state object
      EventLikesDislikes eventLikesDislikes = new EventLikesDislikes();
      eventLikesDislikes.setEvent(newEvent);
      newEvent.setLikesDislikes(eventLikesDislikes);
      newEvent.registerInstant();

      eventRepository.save(newEvent);

      logger.info("Uploading media files...");
      // Saving and setting the media files.
      persistEventMedia(newEvent, multipartFiles);
      logger.info(String.format(CREATE_SUCCESS_MESSAGE, "Event"));

    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
          GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    return newEvent;
  }

  /**
   * @param eventDTO The event to publish.
   * @return The newly published event.
   */
  @Override
  public Event publishEvent(EventDTO eventDTO) {
    Event event = getEvent(eventDTO.id());
    try {
      event.setActive(eventDTO.active());
      if (eventDTO.postedDate() != null && eventDTO.active()) {
        event.setPostedDate(eventDTO.postedDate());
      }
      eventRepository.save(event);
      logger.info(String.format(GENERIC_ACTION_SUCCESS_MESSAGE, "Event", event.getId(), "published"));
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return event;
  }

  /**
   * Update an existing event
   *
   * @param id               The event's id to update.
   * @param eventDTO         The updated event information
   * @param multipartFiles   The collection of media files associated with the updated event.
   * @param retainedMediaIds The List of ids of media files to keep from the existing media files.
   * @return The newly updated event.
   */
  @Override
  public Event updateEvent(UUID id, EventDTO eventDTO, Collection<MultipartFile> multipartFiles,
                           List<Long> retainedMediaIds) {

    Event newEvent = eventMapper.fromEventDTOToEvent(eventDTO);

    try {
      if (!Objects.equals(id, newEvent.getId())) {
        logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "event"));
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "event"));
      }

      if (!eventRepository.existsById(id)) {
        logger.error(String.format(UPDATE_NOT_FOUND_MESSAGE, "event"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(UPDATE_NOT_FOUND_MESSAGE, "event"));
      }

      Optional<Event> optionalEvent = eventRepository.findById(id);

      if (optionalEvent.isPresent()) {
        if (!Objects.equals(optionalEvent.get().getTitle(), newEvent.getTitle())) {
          if (eventRepository.existsByTitle(newEvent.getTitle())) {
            logger.error(String.format(FIELD_CONFLICT_MESSAGE2, "event", "title", newEvent.getTitle()));
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                String.format(FIELD_CONFLICT_ERROR_MESSAGE, "title"));
          }
        }
        optionalEvent.get().setTitle(newEvent.getTitle());
        optionalEvent.get().setActive(newEvent.isActive());
        optionalEvent.get().setDescription(newEvent.getDescription());
        optionalEvent.get().setCommentsCount(newEvent.getCommentsCount());
        optionalEvent.get().setLikesDislikes(newEvent.getLikesDislikes());
        if (newEvent.getPostedDate() != null) {
          optionalEvent.get().setPostedDate(newEvent.getPostedDate().format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
      }

      // Updating media files.
      List<EventMedia> existingMedia = mediaService.getEventMediaByEventId(id);

      if (retainedMediaIds != null && !retainedMediaIds.isEmpty()) {
        if (existingMedia.size() > retainedMediaIds.size()) {
          List<EventMedia> mediaToDelete = existingMedia.stream().filter(media -> !retainedMediaIds.contains(media.getId())).toList();
          mediaToDelete.parallelStream().forEach(mediaService::deleteEventMediaFile);
        }
      }

      persistEventMedia(newEvent, multipartFiles);

      eventRepository.save(newEvent);
      logger.info(String.format(UPDATE_SUCCESS_MESSAGE, "Event", id));
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
    return newEvent;
  }

  /**
   * @param id The event's id to delete.
   */
  @Override
  @Transactional
  public void deleteEvent(UUID id) {
    try {
      if (eventRepository.existsById(id)) {

        // Deleting all associated media
        mediaService.deleteMediaFileByEventId(id);
        // Deleting all associated comments
        commentService.deleteCommentsByEventId(id);

        // Breaking the relationship between the event and the likes and dislikes state object
        Optional<EventLikesDislikes> optionalEventLikesDislikes = userSentimentRepository.findByEventId(id);
        if (optionalEventLikesDislikes.isPresent()) {
          optionalEventLikesDislikes.get().setEvent(null);
          userSentimentRepository.save(optionalEventLikesDislikes.get());
        }

        // Deleting the event
        eventRepository.deleteById(id);
        logger.info(String.format(DELETE_SUCCESS_MESSAGE, "Event", id));
      } else {
        logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "event"));
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(DELETE_NOT_FOUND_MESSAGE, "event"));
      }
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (RuntimeException re) {
      logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param ids The list of events id to delete.
   */
  @Override
  public void deleteEvents(List<UUID> ids) {
    if (!ids.isEmpty()) {
      List<UUID> existentEvents = new ArrayList<>();
      StringBuilder nonexistentEvents = new StringBuilder();

      try {
        ids.parallelStream()
            .forEach(
                id -> {
                  if (eventRepository.existsById(id)) {
                    existentEvents.add(id);
                  } else {
                    nonexistentEvents.append(id).append(", ");
                  }
                });

        if (!existentEvents.isEmpty()) {
          existentEvents.parallelStream().forEach(this::deleteEvent);
        }

        if (!nonexistentEvents.isEmpty()) {
          String errorMessageIds =
              Arrays.stream(nonexistentEvents.toString().split(", "))
                  .sorted()
                  .collect(Collectors.joining(", "));
          logger.error(
              String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "events", errorMessageIds));
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "events", errorMessageIds));
        }
      } catch (ResponseStatusException rse) {
        throw rse;
      } catch (DataAccessException e) {
        logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
      } catch (RuntimeException re) {
        logger.error(RUNTIME_EXCEPTION_LOG_MESSAGE, re);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
      }
    }
  }
}
