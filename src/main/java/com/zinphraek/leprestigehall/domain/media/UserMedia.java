package com.zinphraek.leprestigehall.domain.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zinphraek.leprestigehall.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class UserMedia extends Media {

  @OneToOne(mappedBy = "userMedia")
  @JsonIgnore
  private User user;

  public UserMedia() {
  }

  public UserMedia(String mediaType, String blobName, String mediaUrl, long size) {
    super(mediaType, blobName, mediaUrl, size);
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
}