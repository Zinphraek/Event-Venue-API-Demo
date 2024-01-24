package com.zinphraek.leprestigehall.domain.event;

import org.springframework.stereotype.Component;

@Component
public class EventMapper {

  public EventDTO fromEventToEventDTO(Event event) {
    return new EventDTO(
        event.getId(), false, event.getTitle(), event.getDescription(), event.getPostedDate().toString());
  }

  public Event fromEventDTOToEvent(EventDTO eventDTO) {
    Event event = new Event();

    event.setId(eventDTO.id() != null ? eventDTO.id() : event.getId());
    event.setTitle(eventDTO.title());
    event.setDescription(
        eventDTO.description() != null ? eventDTO.description() : event.getDescription());
    if (eventDTO.postedDate() != null) {
      event.setPostedDate(eventDTO.postedDate());
    }

    return event;
  }
}
