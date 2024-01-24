package com.zinphraek.leprestigehall.domain.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface MediaService {

  AddOnMedia saveAddOnMedia(MultipartFile addOnMediaFile) throws IOException;

  AddOnMedia updateAddOnMedia(AddOnMedia existingAddOnMedia, MultipartFile addOnMediaFile)
      throws IOException;

  UserMedia updateUserMedia(UserMedia existingUserMedia, MultipartFile userMediaFile)
      throws IOException;

  EventMedia saveEventMedia(MultipartFile eventMediaFile, UUID eventId) throws IOException;


  List<EventMedia> getEventMediaByEventId(UUID eventId);

  UserMedia saveUserMedia(MultipartFile userMediaFile) throws IOException;

  EventMedia updateEventMedia(EventMedia existingEventMedia, MultipartFile eventMediaFile)
      throws IOException;

  AddOnMedia getAddOnMedia(Long id);

  EventMedia getEventMedia(Long id);

  void deleteAddOnMediaFile(AddOnMedia addOnMedia);

  void deleteEventMediaFile(EventMedia eventMedia);

  void deleteMediaFileByEventId(UUID eventId);

  void deleteUserMediaFile(UserMedia userMedia);
}
