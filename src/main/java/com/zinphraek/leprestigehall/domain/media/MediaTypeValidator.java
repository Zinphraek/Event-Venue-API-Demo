package com.zinphraek.leprestigehall.domain.media;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class MediaTypeValidator {

  private static final Tika tika = new Tika();

  /**
   * Checks if the given media file is neither an image not a video.
   *
   * @param mediaFile the media file to check
   * @return true if the file is neither an image nor a video, false otherwise
   * @throws RuntimeException if reading the content of the media file fails
   */
  public static boolean isNotImageOrVideo(MultipartFile mediaFile) {
    try (InputStream inputStream = mediaFile.getInputStream()) {
      String detectedContentType = tika.detect(inputStream);

      // Return true if the content type is neither an image nor a video
      return detectedContentType != null && !detectedContentType.startsWith("image/") && !detectedContentType.startsWith("video/");
    } catch (IOException e) {
      throw new RuntimeException("Failed to read the content of the media file", e);
    }
  }


  /**
   * Check if the media is type not an image
   *
   * @param mediaFile The file to check the content type.
   * @return boolean
   */
  public static boolean isNotImage(MultipartFile mediaFile) {
    try (InputStream inputStream = mediaFile.getInputStream()) {
      String detectedContentType = tika.detect(inputStream);

      // Check if the detected content type is not an image
      return detectedContentType != null && !detectedContentType.startsWith("image/");
    } catch (IOException e) {
      throw new RuntimeException("Failed to read the content of the media file", e);
    }
  }

  /**
   * Check if the media is type video
   *
   * @param mediaFile The file to check the content type.
   * @return boolean true if the file is a video, false otherwise
   */
  public static boolean isVideo(MultipartFile mediaFile) {
    try (InputStream inputStream = mediaFile.getInputStream()) {
      String detectedContentType = tika.detect(inputStream);

      // Check if the detected content type is a video
      return detectedContentType != null && detectedContentType.startsWith("video/");
    } catch (IOException e) {
      throw new RuntimeException("Failed to read the content of the media file", e);
    }
  }
}
