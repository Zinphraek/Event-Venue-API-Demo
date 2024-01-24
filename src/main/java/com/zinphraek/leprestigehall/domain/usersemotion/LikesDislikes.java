package com.zinphraek.leprestigehall.domain.usersemotion;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
public abstract class LikesDislikes {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  private final List<String> usersLiked = new ArrayList<>();

  private final List<String> usersDisliked = new ArrayList<>();

  public LikesDislikes() {
  }

  public LikesDislikes(Long id, String userId, String likeOrDislike) {
    this.id = id;
    updateUsersLikedAndDisliked(userId, likeOrDislike);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getLikes() {
    return usersLiked.size();
  }

  public int getDislikes() {
    return usersDisliked.size();
  }

  public List<String> getUsersLiked() {
    return usersLiked;
  }

  public List<String> getUsersDisliked() {
    return usersDisliked;
  }

  public void updateUsersLikedAndDisliked(String userId, String likeOrDislike) {
    switch (likeOrDislike) {
      case "like" -> {
        usersLiked.add(userId);
        usersDisliked.remove(userId);
      }
      case "dislike" -> {
        usersDisliked.add(userId);
        usersLiked.remove(userId);
      }
      default -> {
        usersDisliked.remove(userId);
        usersLiked.remove(userId);
      }
    }
  }
}
