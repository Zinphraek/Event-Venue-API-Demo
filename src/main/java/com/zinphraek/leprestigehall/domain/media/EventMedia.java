package com.zinphraek.leprestigehall.domain.media;

import jakarta.persistence.Entity;

import java.util.UUID;

@Entity
public class EventMedia extends Media {

  private UUID eventId;

  public EventMedia() {
  }

  public EventMedia(String mediaType, String blobName, String mediaUrl, UUID eventId, long size) {
    super(mediaType, blobName, mediaUrl, size);
    this.eventId = eventId;
  }

  public UUID getEventId() {
    return eventId;
  }

  public void setEventId(UUID eventId) {
    this.eventId = eventId;
  }
}