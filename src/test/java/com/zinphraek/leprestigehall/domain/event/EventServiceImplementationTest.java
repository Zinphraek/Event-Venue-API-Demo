package com.zinphraek.leprestigehall.domain.event;

import com.zinphraek.leprestigehall.domain.comment.CommentServiceImplementation;
import com.zinphraek.leprestigehall.domain.data.factories.EventFactory;
import com.zinphraek.leprestigehall.domain.data.factories.MediaFactory;
import com.zinphraek.leprestigehall.domain.media.EventMedia;
import com.zinphraek.leprestigehall.domain.media.MediaService;
import com.zinphraek.leprestigehall.domain.usersemotion.EventLikesDislikesRepository;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EventServiceImplementation}.
 */
@ExtendWith(MockitoExtension.class)
public class EventServiceImplementationTest {

  FactoriesUtilities utilities = new FactoriesUtilities();

  EventFactory eventFactory = new EventFactory();
  MediaFactory mediaFactory = new MediaFactory();
  EventMapper mapper = new EventMapper();
  @InjectMocks
  private EventServiceImplementation eventService;
  @Mock
  private EventLikesDislikesRepository userSentimentRepository;
  @Mock
  private CommentServiceImplementation commentService;
  @Mock
  private EventRepository eventRepository;
  @Mock
  private MediaService mediaService;
  @Mock
  private EventMapper eventMapper;


  @BeforeEach
  void setUp() {
    eventService =
        new EventServiceImplementation(
            this.eventMapper,
            this.mediaService,
            this.eventRepository,
            this.commentService,
            userSentimentRepository
        );
  }

  @Test
  void getEventTestSuccessfulCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(id);
    when(eventRepository.findById(id)).thenReturn(Optional.of(event));

    Event result = eventService.getEvent(id);
    assertEquals(event, result);
    verify(eventRepository, times(1)).findById(id);
  }

  @Test
  void getEventTestNoFoundCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getEvent(id)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(eventRepository, times(1)).findById(id);
  }

  @Test
  void getEventTestDataAccessExceptionCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.findById(id)).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getEvent(id)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).findById(id);
  }

  @Test
  void getEventTestGenericExceptionCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.findById(id)).thenThrow(new RuntimeException("Generic exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getEvent(id)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).findById(id);
  }


  @Test
  void deleteEventTestSuccessfulCase() {
    UUID id = UUID.randomUUID();
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(id);
    when(eventRepository.existsById(id)).thenReturn(true);

    eventService.deleteEvent(id);
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(1)).deleteById(id);
  }

  @Test
  void deleteEventTestNotFoundCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.existsById(id)).thenReturn(false);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvent(id)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(DELETE_NOT_FOUND_MESSAGE, "event"), exception.getReason());
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(0)).findById(id);
    verify(eventRepository, times(0)).deleteById(id);
    verify(mediaService, times(0)).deleteEventMediaFile(any());
    verify(commentService, times(0)).deleteAllEventComment(any());
  }

  @Test
  void deleteEventTestDataAccessExceptionCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.existsById(id)).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvent(id)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(0)).deleteById(id);
    verify(mediaService, times(0)).deleteEventMediaFile(any());
    verify(commentService, times(0)).deleteAllEventComment(any());
  }

  @Test
  void deleteEventTestGenericExceptionCase() {
    UUID id = UUID.randomUUID();
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.existsById(id)).thenThrow(new RuntimeException("Generic exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvent(id)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(0)).deleteById(id);
    verify(mediaService, times(0)).deleteEventMediaFile(any());
    verify(commentService, times(0)).deleteAllEventComment(any());
  }

  @Test
  void createEventTestSuccessfulCase() throws IOException {
    Event event = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(mediaService.saveEventMedia(any(), any())).thenReturn(eventMedia);
    when(eventRepository.save(event)).thenReturn(event);

    Event result = eventService.createEvent(eventDTO, List.of(multipartFile));
    assertEquals(event, result);
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void createEventTestIdConflictCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(CREATE_CONFLICT_MESSAGE2, "event", id), exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(0)).existsByTitle(any());
  }

  @Test
  void createEventTestTitleConflictCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(false);
    when(eventRepository.existsByTitle(event.getTitle())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(FIELD_CONFLICT_ERROR_MESSAGE, "title"), exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(id);
  }

  @Test
  void createEventTestIOExceptionCase() throws IOException {
    Event event = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(mediaService.saveEventMedia(any(), any())).thenThrow(new IOException("IO exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void createEventTestDataAccessExceptionCase1() {
    Event event = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(event.getId())).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(event.getId());
    verify(eventRepository, times(0)).existsByTitle(event.getTitle());
  }

  @Test
  void createEventTestDataAccessExceptionCase2() {
    Event event = eventFactory.generateRandomEvent(null);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsByTitle(event.getTitle())).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(0)).existsById(event.getId());
    verify(eventRepository, times(1)).existsByTitle(event.getTitle());
  }

  @Test
  void createEventTestDataAccessExceptionCase3() {
    Event event = eventFactory.generateRandomEvent(null);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.save(event)).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void createEventTestGenericExceptionCase1() {
    Event event = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(event.getId())).thenThrow(new RuntimeException("Runtime exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(event.getId());
    verify(eventRepository, times(0)).existsByTitle(event.getTitle());
  }

  @Test
  void createEventTestGenericExceptionCase2() {
    Event event = eventFactory.generateRandomEvent(null);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsByTitle(event.getTitle())).thenThrow(new RuntimeException("Runtime exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of())
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(0)).existsById(event.getId());
  }

  @Test
  void createEventTestGenericExceptionCase3() {
    Event event = eventFactory.generateRandomEvent(null);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.save(event)).thenThrow(new RuntimeException("Runtime exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.createEvent(eventDTO, List.of(multipartFile))
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void updateEventTestSuccessfulCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    Event existingEvent = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(id);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenReturn(Optional.of(existingEvent));

    Event result = eventService.updateEvent(id, eventDTO, List.of(multipartFile), null);
    assertEquals(event, result);
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void updateEventTestNotFoundCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(false);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals(String.format(UPDATE_NOT_FOUND_MESSAGE, "event"), exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(0)).findById(id);
  }

  @Test
  void updateEventTestTitleConflictCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    Event existingEvent = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(id);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenReturn(Optional.of(existingEvent));
    when(eventRepository.existsByTitle(event.getTitle())).thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    assertEquals(String.format(FIELD_CONFLICT_ERROR_MESSAGE, "title"), exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(1)).findById(id);
    verify(eventRepository, times(1)).existsByTitle(event.getTitle());
  }

  @Test
  void updateEventTestParamIdMismatchCase() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(UUID.randomUUID(), eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "event"), exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(0)).existsById(id);
    verify(eventRepository, times(0)).findById(id);
    verify(eventRepository, times(0)).existsByTitle(event.getTitle());
  }

  @Test
  void updateEventTestDataAccessExceptionCase1() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(1)).findById(id);
    verify(eventRepository, times(0)).existsByTitle(event.getTitle());
  }

  @Test
  void updateEventTestDataAccessExceptionCase2() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void updateEventTestGenericExceptionCase1() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenThrow(new RuntimeException("Runtime exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(), null)
    );

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).save(event);
    verify(eventRepository, times(1)).existsById(id);
    verify(eventRepository, times(0)).existsByTitle(event.getTitle());
  }

  @Test
  void updateEventTestGenericExceptionCase2() {
    UUID id = UUID.randomUUID();
    Event event = eventFactory.generateRandomEvent(id);
    EventDTO eventDTO = mapper.fromEventToEventDTO(event);
    MultipartFile multipartFile = new MockMultipartFile("test", "test", "image/png", new byte[0]);

    when(eventMapper.fromEventDTOToEvent(eventDTO)).thenReturn(event);
    when(eventRepository.existsById(id)).thenReturn(true);
    when(eventRepository.findById(id)).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenThrow(new RuntimeException("Runtime exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.updateEvent(id, eventDTO, List.of(multipartFile), null)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).save(event);
  }

  @Test
  void getAllEventsTestSuccessfulUnpagedCase() {
    Event eventWithMedia = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(eventWithMedia.getId());
    List<Event> events = new ArrayList<>(eventFactory.generateListOfRandomEvents(10));
    Page<Event> mockPage = new PageImpl<>(events);
    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(mockPage);

    Page<Event> result = eventService.getAllEvents(new HashMap<>());
    assertEquals(mockPage, result);
    verify(eventRepository, times(1))
        .findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllEventsTestSuccessfulPagedCase() {
    Event eventWithMedia = eventFactory.generateRandomEvent(UUID.randomUUID());
    EventMedia eventMedia = mediaFactory.generateARandomEventMedia(1L);
    eventMedia.setEventId(eventWithMedia.getId());
    List<Event> events = new ArrayList<>(eventFactory.generateListOfRandomEvents(10));
    events.add(eventWithMedia);
    Page<Event> mockPage = new PageImpl<>(events);

    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "1", "12", "7", "16"));

    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(mockPage);

    Page<Event> result = eventService.getAllEvents(params);
    assertEquals(mockPage, result);
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1))
        .findAllAndFilter(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
  }

  @Test
  void getAllEventsTestSuccessfulEmptyPageCase1() {
    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "14", "12", "7", "16"));
    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(Page.empty());

    Page<Event> result = eventService.getAllEvents(params);
    assertEquals(Page.empty(), result);
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1))
        .findAllAndFilter(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
  }

  @Test
  void getAllEventsTestSuccessfulEmptyPageCase2() {
    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "1", "12", "7", "6"));
    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(Page.empty());

    Page<Event> result = eventService.getAllEvents(params);
    assertEquals(Page.empty(), result);
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1))
        .findAllAndFilter(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
  }

  @Test
  void getAllEventTestSuccessfulEmptyPageCase3() {
    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "0", "12", "17", "9"));
    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class))).thenReturn(Page.empty());

    Page<Event> result = eventService.getAllEvents(params);
    assertEquals(Page.empty(), result);
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1))
        .findAllAndFilter(anyString(), any(), any(), any(), any(), any(Pageable.class));
  }

  @Test
  void getAllEventsTestDataAccessExceptionCase() {
    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "1", "12", "7", "16"));

    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new DataAccessException("Data access exception") {
        });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getAllEvents(params)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1))
        .findAllAndFilter(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
  }

  @Test
  void getAllEventsTestGenericExceptionCase() {
    Map<String, String> params = new HashMap<>(utilities.getCustomPageTestParams("id", "10"));
    params.putAll(eventFactory.generateRandomEventFilterCriteriaWithSpecificValues("title", "1", "12", "7", "16"));

    when(eventRepository.findAllAndFilter(any(), any(), any(), any(), any(), any(Pageable.class)))
        .thenThrow(new RuntimeException("Generic exception") {
        });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.getAllEvents(params)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).findAll(any(Pageable.class));
    verify(eventRepository, times(1)).findAllAndFilter(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), any(Pageable.class));
  }

  @Test
  void deleteEventsTestSuccessfulCase() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(5);
    for (UUID id : ids) {
      when(eventRepository.existsById(id)).thenReturn(true);
    }
    eventService.deleteEvents(ids);
    verify(eventRepository, times(5)).deleteById(any());
  }

  @Test
  void deleteEventsTestNotFoundCase() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(5);
    StringBuilder expectedErrorReason = new StringBuilder();
    expectedErrorReason.append("Could not delete the following events with ids: ");

    for (UUID id : ids) {
      when(eventRepository.existsById(id)).thenReturn(false);
    }
    expectedErrorReason.append(ids.stream().map(UUID::toString)
        .sorted()
        .collect(Collectors.joining(", ")));
    expectedErrorReason.append(", as they do not exist in the database");

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(expectedErrorReason.toString(), exception.getReason());
    verify(eventRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteEventsTestPartiallySuccessfulAndNotFoundCase() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(5);
    StringBuilder expectedErrorReason = new StringBuilder();
    expectedErrorReason.append("Could not delete the following events with ids: ");

    for (UUID id : ids.subList(0, 3)) {
      when(eventRepository.existsById(id)).thenReturn(true);
    }
    for (String id : ids.subList(3, 5).stream().map(UUID::toString).sorted().toList()) {
      expectedErrorReason.append(id).append(", ");
      when(eventRepository.existsById(UUID.fromString(id))).thenReturn(false);
    }
    expectedErrorReason.append("as they do not exist in the database");

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals(expectedErrorReason.toString(), exception.getReason());
    verify(eventRepository, times(3)).deleteById(any());
  }

  @Test
  void deleteEventsTestDataAccessExceptionCase1() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(5);
    when(eventRepository.existsById(any())).thenThrow(new DataAccessException("Data access exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteEventsTestDataAccessExceptionCase2() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(1);
    when(eventRepository.existsById(any())).thenReturn(true);
    doThrow(new DataAccessException("Data access exception") {
    }).when(eventRepository).deleteById(any());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).deleteById(any());
  }

  @Test
  void deleteEventsTestGenericExceptionCase1() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(5);
    when(eventRepository.existsById(any())).thenThrow(new RuntimeException("Generic exception") {
    });

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(0)).deleteById(any());
  }

  @Test
  void deleteEventsTestGenericExceptionCase2() {
    List<UUID> ids = utilities.generateListOfRandomUUIDs(1);
    when(eventRepository.existsById(any())).thenReturn(true);
    doThrow(new RuntimeException("Generic exception") {
    }).when(eventRepository).deleteById(any());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> eventService.deleteEvents(ids)
    );
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertEquals(GENERIC_UNEXPECTED_ERROR_MESSAGE, exception.getReason());
    verify(eventRepository, times(1)).deleteById(any());
  }
}