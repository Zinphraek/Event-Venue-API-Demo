package com.zinphraek.leprestigehall.domain.media;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zinphraek.leprestigehall.domain.addon.AddOn;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class AddOnMedia extends Media {

  @OneToOne(mappedBy = "media")
  @JsonIgnore
  private AddOn addOn;

  public AddOnMedia() {
  }

  public AddOnMedia(String mediaType, String blobName, String mediaUrl, long size) {
    super(mediaType, blobName, mediaUrl, size);
  }

  public AddOn getAddOn() {
    return addOn;
  }

  public void setAddOn(AddOn addOn) {
    this.addOn = addOn;
  }

}