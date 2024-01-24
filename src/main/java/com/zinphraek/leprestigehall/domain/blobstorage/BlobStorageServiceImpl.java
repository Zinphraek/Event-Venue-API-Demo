package com.zinphraek.leprestigehall.domain.blobstorage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

import static com.zinphraek.leprestigehall.domain.media.MediaTypeValidator.isVideo;

@Service
public class BlobStorageServiceImpl implements BlobStorageService {

  private final BlobServiceClient blobServiceClient;

  public BlobStorageServiceImpl(BlobServiceClient blobServiceClient) {
    this.blobServiceClient = blobServiceClient;
  }


  /**
   * Uploads a blob to the specified container.
   *
   * @param containerName The name of the container to upload the blob to.
   *                      If the container does not exist, it will be created.
   * @param mediaFile     The file to upload.
   * @return A pair containing the file name and the URL to the blob.
   */
  @Override
  public Pair<String, String> uploadBlob(String containerName, MultipartFile mediaFile) throws IOException {

    BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

    if (!containerClient.exists()) {
      containerClient.create();
    }

    BlobClient blobClient = containerClient.getBlobClient(mediaFile.getOriginalFilename());

    blobClient.upload(mediaFile.getInputStream(), mediaFile.getSize(), true);

    String mediaUrl = blobClient.getBlobUrl();

    if (isVideo(mediaFile)) {
      BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("video/mp4");
      blobClient.setHttpHeaders(headers);
    }

    mediaFile.getInputStream().close(); // Closing the input stream

    return Pair.of(Objects.requireNonNull(mediaFile.getOriginalFilename()), mediaUrl);
  }

  /**
   * Generates a presigned URL for the specified blob.
   *
   * @param containerName The name of the container the blob is in.
   * @param blobName      The name of the blob to generate a presigned URL for.
   * @return The presigned URL for the blob.
   */
  @Override
  public String generatePresignedUrl(String containerName, String blobName) {
    BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
    BlobClient blobClient = containerClient.getBlobClient(blobName);

    OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1); // 1 hour expiration time
    BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);

    BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
        .setStartTime(OffsetDateTime.now().minusMinutes(5));
    String sasToken = blobClient.generateSas(values);

    return blobClient.getBlobUrl() + "?" + sasToken;
  }

  /**
   * Deletes a blob from the specified container.
   *
   * @param containerName The name of the container to delete the blob from.
   * @param blobName      The name of the blob to delete.
   */
  @Override
  public void deleteBlob(String containerName, String blobName) {
    blobServiceClient.getBlobContainerClient(containerName)
        .getBlobClient(blobName)
        .delete();
  }
}
