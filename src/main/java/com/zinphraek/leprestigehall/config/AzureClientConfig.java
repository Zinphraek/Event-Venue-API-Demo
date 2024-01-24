package com.zinphraek.leprestigehall.config;


import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureClientConfig {

  @Value("${azure.storage.account-name}")
  private String accountName;

  @Value("${azure.storage.account-key}")
  private String accountKey;

  @Bean
  public BlobServiceClient blobServiceClient() {
    StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

    return new BlobServiceClientBuilder()
        .endpoint(String.format("https://%s.blob.core.windows.net", accountName))
        .credential(credential)
        .buildClient();
  }
}