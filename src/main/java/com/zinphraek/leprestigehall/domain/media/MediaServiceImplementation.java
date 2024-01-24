package com.zinphraek.leprestigehall.domain.media;


import com.zinphraek.leprestigehall.domain.blobstorage.BlobStorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;

@Service
public class MediaServiceImplementation implements MediaService {

  private final Logger logger = LogManager.getLogger(MediaServiceImplementation.class);
  @Autowired
  private final BlobStorageService blobStorageService;

  @Autowired
  private final AddOnMediaRepository addOnMediaRepository;

  @Autowired
  private final EventMediaRepository eventMediaRepository;

  @Autowired
  private final UserMediaRepository userMediaRepository;

  @Value("${azure.storage.container-name}")
  private String containerName;

  public MediaServiceImplementation(
      BlobStorageService blobStorageService,
      AddOnMediaRepository addOnMediaRepository,
      EventMediaRepository eventMediaRepository,
      UserMediaRepository userMediaRepository) {
    this.blobStorageService = blobStorageService;
    this.addOnMediaRepository = addOnMediaRepository;
    this.eventMediaRepository = eventMediaRepository;
    this.userMediaRepository = userMediaRepository;
  }


  /**
   * Save the media file into the external database
   *
   * @param addOnMediaFile The media file to save.
   * @return The new media.
   */
  @Override
  public AddOnMedia saveAddOnMedia(MultipartFile addOnMediaFile) throws IOException {

    try {
      if (MediaTypeValidator.isNotImage(addOnMediaFile)) {
        logger.error("Invalid media type. Only images are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, addOnMediaFile);

      AddOnMedia addOnMedia = new AddOnMedia(addOnMediaFile.getContentType(),
          uploadedMediaNameAndUrl.getFirst(), uploadedMediaNameAndUrl.getSecond(), addOnMediaFile.getSize());
      return addOnMediaRepository.save(addOnMedia);
    } catch (IOException e) {
      logger.error("Error saving add on media file: " + e.getMessage(), e);
      throw new IOException("Error saving add on media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error saving add on media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error saving add on media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Save media file related to an event.
   *
   * @param eventMediaFile The media file to save
   * @param eventId        The event id to associate the media file with
   * @return The new saved media file
   */
  @Override
  public EventMedia saveEventMedia(MultipartFile eventMediaFile, UUID eventId) throws IOException {

    try {

      if (MediaTypeValidator.isNotImageOrVideo(eventMediaFile)) {
        logger.error("Invalid media type. Only images and videos are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images and videos are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, eventMediaFile);
      EventMedia eventMedia = new EventMedia(eventMediaFile.getContentType(),
          uploadedMediaNameAndUrl.getFirst(), uploadedMediaNameAndUrl.getSecond(), eventId, eventMediaFile.getSize());
      return eventMediaRepository.save(eventMedia);
    } catch (IOException e) {
      logger.error("Error saving event media file: " + e.getMessage(), e);
      throw new IOException("Error saving event media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error saving event media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error saving event media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Find all media files related to an event.
   *
   * @param eventId The event id to retrieve the media files for
   * @return The list of media files for the event
   */
  @Override
  public List<EventMedia> getEventMediaByEventId(UUID eventId) {
    try {
      List<EventMedia> eventMediaList = eventMediaRepository.findByEventId(eventId);
      if (!eventMediaList.isEmpty()) {
        eventMediaList.forEach(eventMedia -> eventMedia.setMediaUrl(blobStorageService.generatePresignedUrl(containerName, eventMedia.getBlobName())));
      }
      return eventMediaList;
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving media");
    }
  }

  /**
   * Save media file related to a user.
   *
   * @param userMediaFile The media file to save
   * @return The new saved media file
   * @throws IOException If the media file is not an image
   */
  @Override
  public UserMedia saveUserMedia(MultipartFile userMediaFile) throws IOException {

    try {
      if (MediaTypeValidator.isNotImage(userMediaFile)) {
        logger.error("Invalid media type. Only images are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, userMediaFile);

      UserMedia userMedia = new UserMedia(userMediaFile.getContentType(),
          uploadedMediaNameAndUrl.getFirst(), uploadedMediaNameAndUrl.getSecond(), userMediaFile.getSize());
      return userMediaRepository.save(userMedia);
    } catch (IOException e) {
      logger.error("Error saving user media file: " + e.getMessage(), e);
      throw new IOException("Error saving user media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error saving user media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error saving user media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Update the media file into the external database
   *
   * @param existingAddOnMedia The existing file to replace.
   * @param updatedMediaFile   The Updated version.
   * @return The updated version.
   */
  @Override
  public AddOnMedia updateAddOnMedia(AddOnMedia existingAddOnMedia, MultipartFile updatedMediaFile)
      throws IOException {

    try {
      if (MediaTypeValidator.isNotImage(updatedMediaFile)) {
        logger.error("Invalid media type. Only images are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, updatedMediaFile);

      existingAddOnMedia.setBlobName(uploadedMediaNameAndUrl.getFirst());
      existingAddOnMedia.setType(updatedMediaFile.getContentType());
      existingAddOnMedia.setMediaUrl(uploadedMediaNameAndUrl.getSecond());
      existingAddOnMedia.setSize(updatedMediaFile.getSize());

      return addOnMediaRepository.save(existingAddOnMedia);
    } catch (IOException e) {
      logger.error("Error updating add on media file: " + e.getMessage(), e);
      throw new IOException("Error updating add on media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error updating add on media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error updating add on media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Update the media file into the external database
   *
   * @param existingUserMedia The existing file to replace.
   * @param userMediaFile     The Updated version.
   * @return The updated version.
   * @throws IOException If the media file is not an image
   */
  @Override
  public UserMedia updateUserMedia(UserMedia existingUserMedia, MultipartFile userMediaFile)
      throws IOException {

    try {
      if (MediaTypeValidator.isNotImage(userMediaFile)) {
        logger.error("Invalid media type. Only images are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, userMediaFile);

      UserMedia userMedia = new UserMedia(userMediaFile.getContentType(),
          uploadedMediaNameAndUrl.getFirst(), uploadedMediaNameAndUrl.getSecond(), userMediaFile.getSize());

      existingUserMedia.setBlobName(userMedia.getBlobName());
      existingUserMedia.setType(userMedia.getType());
      existingUserMedia.setMediaUrl(userMedia.getMediaUrl());

      return userMediaRepository.save(existingUserMedia);
    } catch (IOException e) {
      logger.error("Error updating user media file: " + e.getMessage(), e);
      throw new IOException("Error updating user media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error updating user media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error updating user media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Update a media file in the external database
   *
   * @param existingEventMedia    The existing file to replace
   * @param updatedEventMediaFile The updated version
   * @return The updated file version.
   */
  @Override
  public EventMedia updateEventMedia(
      EventMedia existingEventMedia, MultipartFile updatedEventMediaFile) throws IOException {

    try {
      if (MediaTypeValidator.isNotImageOrVideo(updatedEventMediaFile)) {
        logger.error("Invalid media type. Only images and videos are allowed.");
        throw new IllegalArgumentException("Invalid media type. Only images and videos are allowed.");
      }

      Pair<String, String> uploadedMediaNameAndUrl = blobStorageService.uploadBlob(containerName, updatedEventMediaFile);

      existingEventMedia.setBlobName(uploadedMediaNameAndUrl.getFirst());
      existingEventMedia.setType(updatedEventMediaFile.getContentType());
      existingEventMedia.setMediaUrl(uploadedMediaNameAndUrl.getSecond());


      return eventMediaRepository.save(existingEventMedia);
    } catch (IOException e) {
      logger.error("Error updating event media file: " + e.getMessage(), e);
      throw new IOException("Error updating event media file: " + e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Error updating event media file: " + e.getMessage(), e);
      throw new IllegalArgumentException("Error updating event media file: " + e.getMessage());
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * @param id The media's id to retrieve
   * @return The retrieved media
   */
  @Override
  public AddOnMedia getAddOnMedia(Long id) {
    return null;
  }

  /**
   * @param id The media's id to retrieve
   * @return The retrieved media
   */
  @Override
  public EventMedia getEventMedia(Long id) {
    try {
      return eventMediaRepository.findById(id)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException e) {
      logger.error("Error retrieving event media file: " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving media");
    }
  }

  /**
   * Delete a media from the external and embedded databases.
   *
   * @param addOnMedia The media file to delete.
   */
  @Override
  public void deleteAddOnMediaFile(AddOnMedia addOnMedia) {
    try {
      blobStorageService.deleteBlob(containerName, addOnMedia.getBlobName());
      addOnMediaRepository.delete(addOnMedia);
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Delete a media file from the embedded and S3 external databases.
   *
   * @param eventMedia The media file to delete.
   */
  @Override
  public void deleteEventMediaFile(EventMedia eventMedia) {
    try {
      // If the mediaUrl is not used by any other event media, then delete the blob
      if (eventMediaRepository.countByMediaUrl(eventMedia.getMediaUrl()) == 1) {
        blobStorageService.deleteBlob(containerName, eventMedia.getBlobName());
      }
      eventMediaRepository.delete(eventMedia);
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Delete all media files related to an event.
   *
   * @param eventId The event id to delete the media files for
   */
  @Override
  public void deleteMediaFileByEventId(UUID eventId) {
    try {
      List<EventMedia> eventMediaList = eventMediaRepository.findByEventId(eventId);
      for (EventMedia eventMedia : eventMediaList) {
        // If the mediaUrl is not used by any other event media, then delete the blob
        if (eventMediaRepository.countByMediaUrl(eventMedia.getMediaUrl()) == 1) {
          blobStorageService.deleteBlob(containerName, eventMedia.getBlobName());
        }
      }
      eventMediaRepository.deleteByEventId(eventId);
    } catch (DataAccessException e) {
      logger.error("Error deleting event media file: " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException e) {
      logger.error("Error deleting event media file: " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found");
    } catch (RuntimeException e) {
      logger.error("Error deleting event media file: " + e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting media");
    }
  }

  /**
   * Delete a media file from the embedded and S3 external databases.
   *
   * @param userMedia The media file to delete.
   */
  @Override
  public void deleteUserMediaFile(UserMedia userMedia) {
    try {
      blobStorageService.deleteBlob(containerName, userMedia.getBlobName());
      userMediaRepository.delete(userMedia);
    } catch (DataAccessException e) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }
}