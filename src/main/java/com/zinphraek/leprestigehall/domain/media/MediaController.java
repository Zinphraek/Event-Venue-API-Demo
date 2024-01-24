package com.zinphraek.leprestigehall.domain.media;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

import static com.zinphraek.leprestigehall.domain.constants.Paths.EventPath;

@Controller
public class MediaController {

  @Autowired
  private final MediaServiceImplementation mediaService;

  public MediaController(MediaServiceImplementation mediaService) {
    this.mediaService = mediaService;
  }

  @GetMapping(EventPath + "/{eventId}/media")
  public ResponseEntity<List<EventMedia>> getMediaByEventId(@PathVariable UUID eventId) {
    return ResponseEntity.ok(mediaService.getEventMediaByEventId(eventId));
  }

}
