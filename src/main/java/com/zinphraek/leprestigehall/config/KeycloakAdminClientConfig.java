package com.zinphraek.leprestigehall.config;

import static com.zinphraek.leprestigehall.domain.constants.Constants.KEYCLOAK_REALM;
import static com.zinphraek.leprestigehall.domain.constants.Paths.KeycloakUrl;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminClientConfig {

  @Value("${spring.security.oauth2.client.provider.keycloak.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.provider.keycloak.client_secret}")
  private String clientSecret;

  @Value("${spring.security.oauth2.client.provider.keycloak.authorization-grant-type}")
  private String grantType;

  @Value("${spring.security.oauth2.client.provider.keycloak.scope}")
  private String scope;

  @Bean
  public Keycloak keycloak() {
    return KeycloakBuilder.builder()
        .serverUrl(KeycloakUrl)
        .realm(KEYCLOAK_REALM)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .grantType(grantType)
        .scope(scope)
        .build();
  }

}
