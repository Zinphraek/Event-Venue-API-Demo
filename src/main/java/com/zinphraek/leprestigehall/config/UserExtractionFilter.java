package com.zinphraek.leprestigehall.config;

import com.zinphraek.leprestigehall.domain.user.User;
import com.zinphraek.leprestigehall.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserExtractionFilter extends OncePerRequestFilter {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtDecoder jwtDecoder;

  public UserExtractionFilter() {
  }

  public UserExtractionFilter(UserRepository userRepository, JwtDecoder jwtDecoder) {
    this.userRepository = userRepository;
    this.jwtDecoder = jwtDecoder;
  }

  /**
   * Extracts the user information from the JWT and saves it to the database.
   *
   * @param request     Request.
   * @param response    Response.
   * @param filterChain Filter chain.
   * @throws ServletException Servlet exception.
   * @throws IOException      IO exception.
   */
  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = getTokenFromRequest(request);

    if (token != null) {

      Jwt jwt = jwtDecoder.decode(token);

      // Extracting user id information from JWT claims
      String userId = jwt.getClaimAsString("sub");

      if (isNotNullOrBlank(userId) && !userRepository.existsByUserId(userId)) {
        // User is newly registered. Extracting the rest of the information from the JWT claims
        logger.info("New user detected, extracting user information from JWT claims");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        String username = jwt.getClaimAsString("preferred_username");

        boolean isUserInfoPresent =
            isNotNullOrBlank(username)
                && isNotNullOrBlank(email)
                && isNotNullOrBlank(firstName)
                && isNotNullOrBlank(lastName);

        // Creating the user object
        if (isUserInfoPresent) {
          User user = new User();
          user.setUsername(username);
          user.setEmail(email);
          user.setUserId(userId);
          user.setFirstName(firstName);
          user.setLastName(lastName);

          // Saving user to the database
          try {
            userRepository.save(user);
            logger.info("New user saved to the database: " + user.getUserId());
          } catch (DataIntegrityViolationException e) {
            logger.info("User already exists: " + user.getUserId());
          }
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extracts the token from the request.
   *
   * @param request Request.
   * @return Token.
   */
  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * Checks if the string is not null or blank.
   *
   * @param string String.
   * @return True if the string is not null or blank.
   */
  private boolean isNotNullOrBlank(String string) {
    return string != null && !string.isBlank();
  }
}
