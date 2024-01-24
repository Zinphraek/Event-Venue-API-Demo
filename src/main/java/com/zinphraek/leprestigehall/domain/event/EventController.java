package com.zinphraek.leprestigehall.domain.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.zinphraek.leprestigehall.domain.constants.Paths.EventPath;

@Controller
public class EventController {

  @Autowired
  private final EventServiceImplementation eventService;

  public EventController(EventServiceImplementation eventService) {
    this.eventService = eventService;
  }

  @DeleteMapping(EventPath)
  public ResponseEntity<Void> deleteEvents(@RequestPart("ids") List<UUID> ids) {
    eventService.deleteEvents(ids);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @DeleteMapping(EventPath + "/{id}")
  public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
    eventService.deleteEvent(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping(EventPath)
  public ResponseEntity<Page<Event>> getEvents(
      @RequestParam(required = false) Map<String, String> params) {
    return new ResponseEntity<>(eventService.getAllEvents(params), HttpStatus.OK);
  }

  @GetMapping(EventPath + "/{id}")
  public ResponseEntity<Event> getEvent(@PathVariable UUID id) {
    return new ResponseEntity<>(eventService.getEvent(id), HttpStatus.OK);
  }

  @PostMapping(EventPath)
  public ResponseEntity<Event> saveEvent(
      @RequestPart("event") EventDTO eventDTO,
      @RequestPart("media") Collection<MultipartFile> media) {
    return new ResponseEntity<>(eventService.createEvent(eventDTO, media), HttpStatus.CREATED);
  }

  @PutMapping(EventPath + "/{id}")
  public ResponseEntity<Event> updateEvent(
      @PathVariable UUID id,
      @RequestPart("event") EventDTO eventDTO,
      @RequestPart(value = "media", required = false) Collection<MultipartFile> media,
      @RequestPart(value = "retainedMediaIds", required = false) List<Long> retainedMediaIds) {
    return new ResponseEntity<>(eventService.updateEvent(id, eventDTO, media, retainedMediaIds), HttpStatus.OK);
  }

  @PutMapping(EventPath + "/{id}/publish")
  public ResponseEntity<Event> publishEvent(@RequestPart("event") EventDTO eventDTO) {
    return new ResponseEntity<>(eventService.publishEvent(eventDTO), HttpStatus.OK);
  }
}
