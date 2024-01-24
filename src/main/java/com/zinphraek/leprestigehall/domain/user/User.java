package com.zinphraek.leprestigehall.domain.user;

import com.zinphraek.leprestigehall.domain.media.UserMedia;
import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

import static com.zinphraek.leprestigehall.domain.constants.Regex.*;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "User ID is required")
  @Column(unique = true)
  private String userId;

  private String username;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email address")
  private String email;

  @Pattern(regexp = PHONE_REGEX, message = "Invalid phone number")
  private String phone;

  @Pattern(regexp = DATE_REGEX, message = "Invalid date of birth format")
  private String dateOfBirth;

  @AllowedValues(allowedValues = {"Male", "Female", "Non Binary"})
  private String gender;

  private String street;

  private String city;

  @AllowedValues(
      allowedValues = {
          "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
          "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
          "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
          "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
          "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
      })
  private String state;

  @Pattern(regexp = ZIP_CODE_REGEX, message = "Invalid zip code")
  private String zipCode;

  private boolean enabled = true;

  private List<String> requiredActions;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_media_id", referencedColumnName = "id")
  private UserMedia userMedia;

  public User() {
  }

  public User(
      Long id,
      String userId,
      String username,
      String firstName,
      String lastName,
      String email,
      String phone,
      String dateOfBirth,
      String gender,
      String street,
      String city,
      String state,
      String zipCode,
      UserMedia userMedia,
      boolean enabled) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.phone = phone;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.street = street;
    this.city = city;
    this.state = state;
    this.zipCode = zipCode;
    this.userMedia = userMedia;
    this.enabled = enabled;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public UserMedia getUserMedia() {
    return userMedia;
  }

  public void setUserMedia(UserMedia userMedia) {
    this.userMedia = userMedia;
  }

  public List<String> getRequiredActions() {
    return requiredActions;
  }

  public void setRequiredActions(List<String> requiredActions) {
    this.requiredActions = requiredActions;
  }
}
