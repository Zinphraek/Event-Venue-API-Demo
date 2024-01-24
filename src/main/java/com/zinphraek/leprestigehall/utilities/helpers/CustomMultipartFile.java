package com.zinphraek.leprestigehall.utilities.helpers;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.io.InputStream;

public class CustomMultipartFile implements MultipartFile {

  private final MultipartFile originalFile;
  private final String newName;

  public CustomMultipartFile(MultipartFile file, String newName) {
    this.originalFile = file;
    this.newName = UriUtils.encodePath(newName, "UTF-8");
  }

  @Override
  public @NotNull String getName() {
    return originalFile.getName();
  }

  @Override
  public String getOriginalFilename() {
    return this.newName;
  }

  @Override
  public String getContentType() {
    return originalFile.getContentType();
  }

  @Override
  public boolean isEmpty() {
    return originalFile.isEmpty();
  }

  @Override
  public long getSize() {
    return originalFile.getSize();
  }

  @Override
  public byte @NotNull [] getBytes() throws IOException {
    return originalFile.getBytes();
  }

  @Override
  public @NotNull InputStream getInputStream() throws IOException {
    return originalFile.getInputStream();
  }

  @Override
  public void transferTo(java.io.@NotNull File dest) throws IOException, IllegalStateException {
    originalFile.transferTo(dest);
  }
}
