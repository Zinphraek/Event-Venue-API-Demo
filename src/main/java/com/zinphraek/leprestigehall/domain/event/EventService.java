package com.zinphraek.leprestigehall.domain.event;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface EventService {

  Page<Event> getAllEvents(Map<String, String> params);

  Event getEvent(UUID id);

  Event createEvent(EventDTO eventDTO, Collection<MultipartFile> multipartFiles);

  Event publishEvent(EventDTO eventDTO);

  Event updateEvent(UUID id, EventDTO eventDTO, Collection<MultipartFile> multipartFiles, List<Long> retainedMediaIds);

  void deleteEvent(UUID id);

  void deleteEvents(List<UUID> ids);
}
