package com.zinphraek.leprestigehall.domain.user;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserMapper {

  public UserRepresentation fromCustomUserToKeycloakUser(User customUser) {
    UserRepresentation user = new UserRepresentation();
    Map<String, List<String>> attributes = new HashMap<>();
    attributes.put("street", Collections.singletonList(customUser.getStreet()));
    attributes.put("city", Collections.singletonList(customUser.getCity()));
    attributes.put("state", Collections.singletonList(customUser.getState()));
    attributes.put("zipCode", Collections.singletonList(customUser.getZipCode()));

    attributes.put("dateOfBirth", Collections.singletonList(customUser.getDateOfBirth()));
    attributes.put("gender", Collections.singletonList(customUser.getGender()));
    attributes.put("phone", Collections.singletonList(customUser.getPhone()));

    // Mapping the keycloak user representation.
    user.setUsername(customUser.getUsername());
    user.setEmail(customUser.getEmail());
    user.setEnabled(customUser.isEnabled());
    user.setFirstName(customUser.getFirstName());
    user.setLastName(customUser.getLastName());
    user.setAttributes(attributes);
    user.setRequiredActions(customUser.getRequiredActions());

    return user;
  }

  public User fromKeycloakUserToCustomUser(UserRepresentation user) {
    User customUser = new User();

    Map<String, List<String>> attributes = user.getAttributes();

    customUser.setUserId(user.getId());
    customUser.setUsername(user.getUsername());
    customUser.setEmail(user.getEmail());
    customUser.setEnabled(user.isEnabled());
    customUser.setFirstName(user.getFirstName());
    customUser.setLastName(user.getLastName());
    customUser.setRequiredActions(user.getRequiredActions());

    if (attributes != null) {
      customUser.setStreet(getAttribute(attributes, "street"));
      customUser.setCity(getAttribute(attributes, "city"));
      customUser.setState(getAttribute(attributes, "state"));
      customUser.setZipCode(getAttribute(attributes, "zipCode"));

      customUser.setDateOfBirth(getAttribute(attributes, "dateOfBirth"));
      customUser.setGender(getAttribute(attributes, "gender"));
      customUser.setPhone(getAttribute(attributes, "phone"));
    }

    return customUser;
  }

  public User fromUserDTOToUser(UserDTO userDTO) {
    User customUser = new User();

    customUser.setUserId(userDTO.userId());
    customUser.setUsername(userDTO.username());
    customUser.setEmail(userDTO.email());
    customUser.setEnabled(userDTO.enabled());
    customUser.setFirstName(userDTO.firstName());
    customUser.setLastName(userDTO.lastName());
    customUser.setRequiredActions(userDTO.requiredActions());
    customUser.setStreet(userDTO.street());
    customUser.setCity(userDTO.city());
    customUser.setState(userDTO.state());
    customUser.setZipCode(userDTO.zipCode());
    customUser.setDateOfBirth(userDTO.dateOfBirth());
    customUser.setGender(userDTO.gender());
    customUser.setPhone(userDTO.phone());

    return customUser;
  }

  public UserDTO fromUserToUserDTO(User customUser) {
    return new UserDTO(
        customUser.getId(),
        customUser.getUserId(),
        customUser.getUsername(),
        customUser.getFirstName(),
        customUser.getLastName(),
        customUser.getEmail(),
        customUser.getPhone(),
        customUser.getDateOfBirth(),
        customUser.getGender(),
        customUser.getStreet(),
        customUser.getCity(),
        customUser.getState(),
        customUser.getZipCode(),
        customUser.getUserMedia(),
        customUser.isEnabled(),
        customUser.getRequiredActions());
  }

  public UserSummaryDTO fromUserToUserSummaryDTO(User customUser) {
    String profilePictureUrl =
        customUser.getUserMedia() == null ? null : customUser.getUserMedia().getMediaUrl();
    return new UserSummaryDTO(
        customUser.getUserId(),
        customUser.getFirstName(),
        customUser.getLastName(),
        profilePictureUrl);
  }

  public void transferData(User newUser, User existingUser) {
    existingUser.setUserId(newUser.getUserId());
    existingUser.setUsername(newUser.getUsername());
    existingUser.setEmail(newUser.getEmail());
    existingUser.setEnabled(newUser.isEnabled());
    existingUser.setFirstName(newUser.getFirstName());
    existingUser.setLastName(newUser.getLastName());
    existingUser.setStreet(newUser.getStreet());
    existingUser.setCity(newUser.getCity());
    existingUser.setState(newUser.getState());
    existingUser.setZipCode(newUser.getZipCode());
    existingUser.setDateOfBirth(newUser.getDateOfBirth());
    existingUser.setGender(newUser.getGender());
    existingUser.setPhone(newUser.getPhone());
  }

  public UserRepresentation updateUserRepresentation(UserRepresentation existingUserRepresentation, User user) {
    UserRepresentation userRepresentation = fromCustomUserToKeycloakUser(user);
    existingUserRepresentation.setUsername(userRepresentation.getUsername());
    existingUserRepresentation.setEmail(userRepresentation.getEmail());
    existingUserRepresentation.setEnabled(userRepresentation.isEnabled());
    existingUserRepresentation.setFirstName(userRepresentation.getFirstName());
    existingUserRepresentation.setLastName(userRepresentation.getLastName());
    existingUserRepresentation.setAttributes(userRepresentation.getAttributes());
    existingUserRepresentation.setRequiredActions(userRepresentation.getRequiredActions());
    return existingUserRepresentation;
  }

  private String getAttribute(Map<String, List<String>> attributes, String key) {
    if (attributes.containsKey(key) && !attributes.get(key).isEmpty()) {
      return attributes.get(key).get(0);
    }
    return null;
  }
}