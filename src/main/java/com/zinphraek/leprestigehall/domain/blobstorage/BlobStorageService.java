package com.zinphraek.leprestigehall.domain.blobstorage;

import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BlobStorageService {
  Pair<String, String> uploadBlob(String containerName, MultipartFile mediaFile) throws IOException;

  String generatePresignedUrl(String containerName, String blobName);

  void deleteBlob(String containerName, String blobName);
}
