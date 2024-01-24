package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.media.AddOnMedia;
import com.zinphraek.leprestigehall.domain.media.EventMedia;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

public class MediaFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();

  public AddOnMedia generateARandomAddOnMedia(long id) {
    AddOnMedia addOnMedia = new AddOnMedia();
    addOnMedia.setId(id != 0 ? id : null);
    addOnMedia.setType(utilities.generateRandomString());
    addOnMedia.setBlobName(utilities.generateRandomString());
    addOnMedia.setMediaUrl(utilities.generateRandomString());
    return addOnMedia;
  }

  public EventMedia generateARandomEventMedia(long id) {
    EventMedia eventMedia = new EventMedia();
    eventMedia.setId(id != 0 ? id : null);
    eventMedia.setType(utilities.generateRandomString());
    eventMedia.setBlobName(utilities.generateRandomString());
    eventMedia.setMediaUrl(utilities.generateRandomString());
    return eventMedia;
  }
}