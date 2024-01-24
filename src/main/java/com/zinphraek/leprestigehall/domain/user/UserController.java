package com.zinphraek.leprestigehall.domain.user;

import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.zinphraek.leprestigehall.domain.constants.Paths.UserPath;

@Controller
public class UserController {

  @Autowired
  private final UserServiceImpl userService;

  public UserController(UserServiceImpl userService) {
    this.userService = userService;
  }


  /**
   * Get page of users.
   *
   * @return Page of users.
   */
  @PreAuthorize("hasRole('admin')")
  @GetMapping(UserPath)
  public ResponseEntity<Page<User>> getPagedUsers(@RequestParam(required = false) Map<String, String> params) {
    Page<User> users = userService.getUsers(params);
    return new ResponseEntity<>(users, HttpStatus.OK);
  }

  /**
   * Get user by id.
   *
   * @param userId User id.
   * @return User.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @GetMapping(UserPath + "/{userId}")
  public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
    User user;
    try {
      user = userService.getUserById(userId);
      return new ResponseEntity<>(user, HttpStatus.OK);
    } catch (NotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Create user.
   *
   * @param user The user to create.
   * @return The created user.
   */
  @PreAuthorize("hasRole('admin')")
  @PostMapping(UserPath)
  public ResponseEntity<User> createUser(@RequestPart("user") User user) {
    return new ResponseEntity<>(userService.createUser(user), HttpStatus.CREATED);
  }

  /**
   * Update user.
   *
   * @param userId The user id.
   * @param user   The user to update.
   * @return The updated user.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @PutMapping(UserPath + "/{userId}")
  public ResponseEntity<User> updateUser(
      @PathVariable("userId") String userId, @RequestPart("user") User user) {
    return new ResponseEntity<>(userService.updateUser(userId, user), HttpStatus.OK);
  }

  /**
   * Update user picture.
   *
   * @param userId          The user id.
   * @param userPictureFile The user picture file.
   * @return No content.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @PutMapping(UserPath + "/{userId}/picture")
  public ResponseEntity<Void> updateUserPicture(
      @PathVariable("userId") String userId,
      @RequestPart("userPictureFile") MultipartFile userPictureFile) {
    userService.updateUserPicture(userId, userPictureFile);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Update user password.
   *
   * @param userId      The user id.
   * @param newPassword The new password.
   * @return No content.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @PutMapping(UserPath + "/{userId}/password")
  public ResponseEntity<Void> updateUserPassword(
      @PathVariable("userId") String userId, @RequestPart("newPassword") String newPassword) {
    userService.updateUserPassword(userId, newPassword);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Delete user.
   *
   * @param userId The user id.
   * @return No content.
   */
  @PreAuthorize("#userId == authentication.principal.subject or hasRole('admin')")
  @DeleteMapping(UserPath + "/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
    userService.deleteUser(userId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Delete multiple users.
   *
   * @param userIds The user ids.
   * @return No content.
   */
  @PreAuthorize("hasRole('admin')")
  @DeleteMapping(UserPath)
  public ResponseEntity<Void> deleteManyUsers(
      @RequestPart("ids") Map<String, List<String>> userIds) {
    userService.deleteMultipleUsers(userIds.get("ids"));
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
