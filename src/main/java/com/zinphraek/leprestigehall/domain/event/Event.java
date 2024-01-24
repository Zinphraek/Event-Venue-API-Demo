package com.zinphraek.leprestigehall.domain.event;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.zinphraek.leprestigehall.domain.usersemotion.EventLikesDislikes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

@Entity
@Table(name = "events")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id")
  private UUID id;

  @CreatedDate
  private Instant createdDate;

  @NotNull
  @NotBlank
  @Column(unique = true)
  private String title;

  @NotBlank(message = "Description is required")
  @Size(max = 2000, message = "Description cannot be more than 2000 characters")
  private String description;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "likes_dislikes_id", referencedColumnName = "id")
  private EventLikesDislikes likesDislikes;

  private Long commentsCount = 0L;

  @DateTimeFormat(pattern = DATE_TIME_FORMAT)
  private LocalDateTime postedDate;

  private boolean isActive;

  public Event() {
  }

  public Event(
      UUID id,
      String title,
      String description,
      EventLikesDislikes likesDislikes,
      Long commentsCount,
      String postedDate) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.likesDislikes = likesDislikes;
    this.commentsCount = commentsCount;
    this.postedDate =
        LocalDateTime.parse(postedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void registerInstant() {
    this.createdDate = Instant.now();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public EventLikesDislikes getLikesDislikes() {
    return likesDislikes;
  }

  public void setLikesDislikes(EventLikesDislikes likesDislikes) {
    this.likesDislikes = likesDislikes;
  }

  public Long getCommentsCount() {
    return commentsCount;
  }

  public void setCommentsCount(Long commentsCount) {
    this.commentsCount = commentsCount;
  }

  public LocalDateTime getPostedDate() {
    return postedDate;
  }

  public void setPostedDate(CharSequence postedDate) {
    this.postedDate =
        LocalDateTime.parse(postedDate, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public void setPostedDate(LocalDateTime postedDate) {
    this.postedDate = postedDate;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Event event)) {
      return false;
    }
    return id.equals(event.id)
        && title.equals(event.title)
        && description.equals(event.description)
        && Objects.equals(likesDislikes, event.likesDislikes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, likesDislikes, commentsCount, postedDate, isActive);
  }
}
