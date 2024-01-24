package com.zinphraek.leprestigehall.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {

  Page<User> getUsers(Map<String, String> params);

  User getUserById(String userId);

  User createUser(User newUser);

  void saveNewlyRegisteredUser(User newUser);

  User updateUser(String userId, User newUser);

  void updateUserPicture(String userId, MultipartFile userPictureFile);

  void updateUserPassword(String userId, String newPassword);

  void deleteUser(String userId);

  void deleteMultipleUsers(List<String> usersIds);
}
