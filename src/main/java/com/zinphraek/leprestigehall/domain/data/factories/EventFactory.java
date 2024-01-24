package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.event.Event;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();

  public Event generateRandomEvent(UUID id) {
    Event event = new Event();
    LocalDateTime postedDate = utilities.generateRandomLocalDateTime();
    event.setId(id);
    event.setTitle(utilities.generateRandomString());
    event.setDescription(utilities.generateRandomString());
    event.setPostedDate(utilities.formatLocalDateTime(postedDate));

    return event;
  }

  public List<Event> generateListOfRandomEvents(int listSize) {
    return utilities
        .generateListOfRandomUUIDs(listSize)
        .stream()
        .map(this::generateRandomEvent)
        .toList();
  }

  /**
   * Generates a random event filter criteria with specific values.
   *
   * @param title       the title of the event.
   * @param minLikes    the minimum number of likes.
   * @param maxLikes    the maximum number of likes.
   * @param minDislikes the minimum number of dislikes.
   * @param maxDislikes the maximum number of dislikes.
   * @return a random event filter criteria with specific values.
   */
  public Map<String, String> generateRandomEventFilterCriteriaWithSpecificValues(
      String title, String minLikes, String maxLikes, String minDislikes, String maxDislikes) {
    Map<String, String> filterCriteria = new HashMap<>();
    filterCriteria.put("title", title);
    filterCriteria.put("maxDislikes", maxDislikes);
    filterCriteria.put("maxLikes", maxLikes);
    filterCriteria.put("minDislikes", minDislikes);
    filterCriteria.put("minLikes", minLikes);
    return filterCriteria;
  }
}
