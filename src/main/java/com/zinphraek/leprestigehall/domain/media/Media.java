package com.zinphraek.leprestigehall.domain.media;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.Objects;

@MappedSuperclass
public abstract class Media {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String type;
  private String blobName;
  private String mediaUrl;
  private Long size;

  public Media() {
  }

  public Media(String type, String blobName, String mediaUrl, Long size) {
    this.type = type;
    this.blobName = blobName;
    this.mediaUrl = mediaUrl;
    this.size = size;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBlobName() {
    return blobName;
  }

  public void setBlobName(String blobName) {
    this.blobName = blobName;
  }

  public String getMediaUrl() {
    return mediaUrl;
  }

  public void setMediaUrl(String mediaUrl) {
    this.mediaUrl = mediaUrl;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Media media)) {
      return false;
    }
    return Objects.equals(id, media.id) && Objects.equals(type, media.type)
        && Objects.equals(blobName, media.blobName) && Objects.equals(mediaUrl, media.mediaUrl)
        && Objects.equals(size, media.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, blobName, mediaUrl, size);
  }
}