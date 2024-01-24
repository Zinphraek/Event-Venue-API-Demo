package com.zinphraek.leprestigehall.config;

import com.zinphraek.leprestigehall.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

import static com.zinphraek.leprestigehall.domain.constants.Paths.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Value("${spring.security.oauth2.client.provider.claims.aud}")
  private String claimAud;

  @Value("${spring.security.oauth2.client.provider.keycloak.jwk-set-uri}")
  private String urlJwk;

  @Value("${appAllowedUrls}")
  private List<String> urls;

  @Bean
  JwtDecoder jwtDecoder() {
    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(urlJwk).build();

    MappedJwtClaimSetConverter converter =
        MappedJwtClaimSetConverter.withDefaults(Collections.singletonMap("aud", aud -> claimAud));
    jwtDecoder.setClaimSetConverter(converter);

    return jwtDecoder;
  }

  @Bean
  public UserExtractionFilter userExtractionFilter(
      UserRepository userRepository, JwtDecoder jwtDecoder) {
    return new UserExtractionFilter(userRepository, jwtDecoder);
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http, UserExtractionFilter userExtractionFilter) throws Exception {
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName("_csrf");
    JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

    http.sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .cors()
        .configurationSource(
            request -> {
              CorsConfiguration config = new CorsConfiguration();
              config.setAllowedOrigins(urls);
              config.setAllowedMethods(Collections.singletonList("*"));
              config.setAllowCredentials(true);
              config.setAllowedHeaders(Collections.singletonList("*"));
              config.setExposedHeaders(List.of("Authorization"));
              config.setMaxAge(3600L);
              return config;
            })
        .and()
        .csrf(
            (csrf) ->
                csrf.csrfTokenRequestHandler(requestHandler)
                    .ignoringRequestMatchers(AppointmentPath)
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .addFilterAfter(userExtractionFilter, CsrfCookieFilter.class)
        .authorizeHttpRequests(
            (authorize) ->
                authorize
                    .requestMatchers(POST, AppointmentPath)
                    .permitAll()
                    .requestMatchers(
                        GET,
                        EventPath + "/**",
                        EventCommentPath + "/**",
                        FAQPath + "/**",
                        ReviewPath + "/**",
                        LikesDislikesPath + "/**",
                        CommentsLikesDislikesPath + "/**")
                    .permitAll()
                    .requestMatchers(UserPath, AddOnPath)
                    .hasAnyRole("user", "admin")
                    .requestMatchers(AdminPath)
                    .hasRole("admin")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(jwtAuthenticationConverter);
    return http.build();
  }
}
