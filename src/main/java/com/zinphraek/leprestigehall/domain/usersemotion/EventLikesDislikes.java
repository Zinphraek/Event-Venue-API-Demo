package com.zinphraek.leprestigehall.domain.usersemotion;

import com.zinphraek.leprestigehall.domain.event.Event;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class EventLikesDislikes extends LikesDislikes {

  @OneToOne(mappedBy = "likesDislikes")
  private Event event;

  public EventLikesDislikes() {}

  public EventLikesDislikes(Long id, String userId, String likeOrDislike, Event event) {
    super(id, userId, likeOrDislike);
    this.event = event;
  }

  public Event getEvent() {
    return event;
  }

  public void setEvent(Event event) {
    this.event = event;
  }
}
