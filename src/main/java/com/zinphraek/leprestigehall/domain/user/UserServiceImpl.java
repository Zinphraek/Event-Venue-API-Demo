package com.zinphraek.leprestigehall.domain.user;

import com.zinphraek.leprestigehall.domain.blobstorage.BlobStorageService;
import com.zinphraek.leprestigehall.domain.media.MediaService;
import com.zinphraek.leprestigehall.domain.media.UserMedia;
import com.zinphraek.leprestigehall.utilities.helpers.CustomMultipartFile;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;
import static com.zinphraek.leprestigehall.domain.constants.GenericResponseMessages.*;
import static com.zinphraek.leprestigehall.utilities.helpers.GenericHelper.*;

@Service
public class UserServiceImpl implements UserService {

  private final Logger logger = LogManager.getLogger(UserServiceImpl.class);

  @Autowired
  private final Keycloak keycloak;

  @Value("${azure.storage.container-name}")
  private String containerName;

  @Autowired
  private final UserMapper userMapper;

  @Autowired
  private final MediaService mediaService;

  @Autowired
  private final UserRepository userRepository;

  @Autowired
  private final BlobStorageService blobStorageService;

  public UserServiceImpl(
      Keycloak keycloak,
      UserMapper userMapper,
      MediaService mediaService,
      UserRepository userRepository,
      BlobStorageService blobStorageService) {
    this.keycloak = keycloak;
    this.userMapper = userMapper;
    this.mediaService = mediaService;
    this.userRepository = userRepository;
    this.blobStorageService = blobStorageService;
  }

  private void generatePresignedUrl(@NotNull User user) {
    UserMedia userMedia = user.getUserMedia();
    if (Objects.nonNull(userMedia)) {
      String url = blobStorageService.generatePresignedUrl(containerName, userMedia.getBlobName());
      user.getUserMedia().setMediaUrl(url);
    }
  }


  /**
   * Handle the response from keycloak when creating a new user.
   *
   * @param response The response from keycloak
   * @return The newly created user
   */
  private User handleUserCreationResponse(@NotNull Response response) {
    switch (response.getStatus()) {
      case 201:
        URI location = response.getLocation();
        if (location == null) {
          logger.error("User created but the location header is missing");
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, "User created but the location header is missing");
        }
        String userId = extractUserIdFromLocation(location);
        UserRepresentation newUserFromKeycloak = keycloak.realm(KEYCLOAK_REALM).users().get(userId).toRepresentation();
        User newUser = userMapper.fromKeycloakUserToCustomUser(newUserFromKeycloak);
        saveNewlyRegisteredUser(newUser);
        logger.info("User successfully created.");
        return newUser;
      case 400:
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Invalid request to create user. Please review and correct the user information.");
      case 409:
        logger.error(String.format(FIELD_CONFLICT_MESSAGE2, "user", "the provided", "credentials"), response);
        throw new ResponseStatusException(
            HttpStatus.CONFLICT, String.format(FIELD_CONFLICT_MESSAGE2, "user", "the provided", "credentials"));
      default:
        logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE);
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Extract the user id from the location header.
   *
   * @param location The location header
   * @return The user id
   */
  private @NotNull String extractUserIdFromLocation(URI location) {
    String path = location.getPath();
    return path.substring(path.lastIndexOf('/') + 1);
  }

  private List<User> fetchFromKeycloak() {
    List<User> customUsers;
    try {
      List<UserRepresentation> users = keycloak.realm(KEYCLOAK_REALM).users().list();
      customUsers =
          users.stream()
              .map(userMapper::fromKeycloakUserToCustomUser)
              .collect(Collectors.toList());
      userRepository.saveAll(customUsers);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new RuntimeException(e.getMessage());
    }

    logger.info("Users fetched from Keycloak.");
    return customUsers;
  }

  /**
   * Fetch all users from the app or keycloak database.
   *
   * @param params - The query parameters
   * @return A Page of users
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  public Page<User> getUsers(Map<String, String> params) {

    logger.info("Fetching users...");
    try {
      Pageable pageable = params.isEmpty() ? Pageable.unpaged()
          : buildPageRequestFromCustomPage(createCustomPageFromParams(params));

      Page<User> usersPage = userRepository.findAll(pageable);

      if (usersPage.isEmpty()) {
        List<User> users = fetchFromKeycloak();
        usersPage = new PageImpl<>(users, pageable, users.size());
      }
      usersPage.getContent().parallelStream().forEach(this::generatePresignedUrl);
      logger.info("Users successfully fetched.");
      return usersPage;
    } catch (DataAccessException dae) {
      logger.error(dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Fetch a specific user from keycloak
   *
   * @param userId The target user's id.
   * @return A user.
   */
  @Override
  public User getUserById(String userId) {

    Optional<User> optionalUser;
    UserRepresentation userRepresentation;
    User user;
    try {
      optionalUser = userRepository.findByUserId(userId);
      if (optionalUser.isEmpty()) {
        userRepresentation = keycloak.realm(KEYCLOAK_REALM).users().get(userId).toRepresentation();
        if (userRepresentation != null) {
          user = userMapper.fromKeycloakUserToCustomUser(userRepresentation);
          saveNewlyRegisteredUser(user);
          generatePresignedUrl(user);
          logger.info("User successfully fetched.");
          return user;
        } else {
          logger.error(String.format(GET_NOT_FOUND_MESSAGE, "user", userId));
          throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, String.format(GET_NOT_FOUND_MESSAGE, "user", userId));
        }
      }
      generatePresignedUrl(optionalUser.get());
      logger.info("User successfully fetched.");
      return optionalUser.get();
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      logger.error(rse);
      throw rse;
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Persists a new user in the App and keycloak databases
   *
   * @param newUser The new user to persist
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  public User createUser(@NotNull User newUser) {

    newUser.setRequiredActions(List.of(VERIFY_EMAIL, UPDATE_PASSWORD));

    UserRepresentation userRepresentation = userMapper.fromCustomUserToKeycloakUser(newUser);
    try (Response response = keycloak.realm(KEYCLOAK_REALM).users().create(userRepresentation)) {
      return handleUserCreationResponse(response);
    } catch (ResponseStatusException rse) {
      throw rse;
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Save a user in the database
   *
   * @param newUser The user to save
   */
  @Override
  public void saveNewlyRegisteredUser(User newUser) {
    try {
      if (!userRepository.existsByUserId(newUser.getUserId())) {
        userRepository.save(newUser);
        logger.info("New user successfully saved");
      }
    } catch (RuntimeException e) {
      logger.error(e);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
  }

  /**
   * Update a user in the keycloak database
   *
   * @param userId  The id of the user to update.
   * @param newUser The updated user's information
   * @return The newly updated user
   */
  @Transactional
  @Override
  public User updateUser(String userId, @NotNull User newUser) {

    if (!Objects.equals(userId, newUser.getUserId())) {
      logger.error(String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "user"));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(PARAMETER_MISMATCH_ERROR_MESSAGE, "user"));
    }

    try {
      User existingUser = getUserById(userId);
      userMapper.transferData(newUser, existingUser);

      // Updating the user in keycloak
      UserResource userResource =
          keycloak.realm(KEYCLOAK_REALM).users().get(existingUser.getUserId());
      userResource.update(userMapper.updateUserRepresentation(userResource.toRepresentation(), existingUser));
      userRepository.save(existingUser);
      logger.info("User successfully updated");

      return existingUser;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      logger.error(rse);
      throw rse;
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Update a user's profile picture
   *
   * @param userId          The id of the user to update
   * @param userPictureFile The new profile picture
   */
  @Override
  public void updateUserPicture(String userId, MultipartFile userPictureFile) {

    try {
      if (!userRepository.existsByUserId(userId)) {
        logger.error("Could not update user picture. User with id: " + userId + " not found.");
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Could not update picture for non-existent user.");
      }

      UserMedia userMedia;
      UserMedia existingUserMedia;
      String fileName = Objects.requireNonNull(userPictureFile.getOriginalFilename()).strip().split("\\.")[0];
      validateFileName(fileName.toLowerCase());
      String userPictureBlobName = userId + "_" + fileName;

      User user = getUserById(userId);
      existingUserMedia = user.getUserMedia();
      if (existingUserMedia != null) {
        userMedia = mediaService.updateUserMedia(existingUserMedia,
            new CustomMultipartFile(userPictureFile, userPictureBlobName));
      } else {
        userMedia = mediaService.saveUserMedia(userPictureFile);
      }
      user.setUserMedia(userMedia);
      userRepository.save(user);
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (ResponseStatusException rse) {
      logger.error(rse);
      throw rse;
    } catch (IOException | IllegalAccessError e) {
      logger.error(e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Update a user's password
   *
   * @param userId      The id of the user to update
   * @param newPassword The new password
   */
  @Override
  public void updateUserPassword(String userId, String newPassword) {

    try {
      if (!userRepository.existsByUserId(userId)) {
        logger.error("Could not update password for user with id: " + userId + ". User not found.");
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Could not update password for non-existent user.");
      }

      UserResource userResource;
      CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
      credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
      credentialRepresentation.setValue(newPassword);
      credentialRepresentation.setTemporary(false);

      userResource = keycloak.realm(KEYCLOAK_REALM).users().get(userId);
      userResource.resetPassword(credentialRepresentation);
      logger.info("User password successfully updated.");
    } catch (ResponseStatusException rse) {
      logger.error(rse);
      throw rse;
    } catch (DataAccessException dae) {
      logger.error(DATA_ACCESS_EXCEPTION_LOG_MESSAGE, dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Delete a target user from the database.
   *
   * @param userId The id of the target user
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  @Transactional
  public void deleteUser(String userId) {
    try {
      if (!userRepository.existsByUserId(userId)) {
        logger.error(String.format(DELETE_NOT_FOUND_MESSAGE, "user with id: " + userId));
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND, String.format(DELETE_NOT_FOUND_MESSAGE, "user with id: " + userId));
      }
      UserResource userResource = keycloak.realm(KEYCLOAK_REALM).users().get(userId);
      userResource.remove();
      userRepository.deleteByUserId(userId);
      logger.info("User successfully deleted.");
    } catch (DataAccessException dae) {
      logger.error(dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    } catch (ResponseStatusException rse) {
      logger.error(rse);
      throw rse;
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }
  }

  /**
   * Delete more than one user.
   *
   * @param usersIds List of users id to delete.
   */
  @PreAuthorize("hasRole('admin')")
  @Override
  public void deleteMultipleUsers(List<String> usersIds) {
    List<String> existingUsersIds = new ArrayList<>();
    StringBuilder nonexistentUsers = new StringBuilder();

    try {
      usersIds.parallelStream().forEach(userId -> {
        if (userRepository.existsByUserId(userId)) {
          existingUsersIds.add(userId);
        } else {
          nonexistentUsers.append(userId).append(", ");
        }
      });

      if (!existingUsersIds.isEmpty()) {
        existingUsersIds.parallelStream().forEach(this::deleteUser);
      }

    } catch (DataAccessException dae) {
      logger.error(dae);
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, dae.getMessage());
    } catch (RuntimeException e) {
      logger.error(GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_UNEXPECTED_ERROR_MESSAGE);
    }

    if (!nonexistentUsers.isEmpty()) {
      String errorMessageIds =
          Arrays.stream(nonexistentUsers.toString().split(", "))
              .sorted()
              .collect(Collectors.joining(", "));
      logger.error(String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "users", errorMessageIds));
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, String.format(MASS_DELETE_NOT_FOUND_MESSAGE, "users", errorMessageIds));
    }

  }

}