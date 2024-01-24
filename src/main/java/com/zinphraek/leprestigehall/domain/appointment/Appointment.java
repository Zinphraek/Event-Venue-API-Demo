package com.zinphraek.leprestigehall.domain.appointment;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;
import static com.zinphraek.leprestigehall.domain.constants.Regex.PHONE_REGEX;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @NotBlank(message = "First name is required")
  private String firstName;

  @NotBlank(message = "Last name is required")
  private String lastName;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = PHONE_REGEX, message = "Invalid phone number")
  private String phone;

  @Email(message = "Invalid email address")
  @NotBlank(message = "Email is required")
  private String email;

  @NotNull(message = "Date and time is required")
  private LocalDateTime dateTime;

  @NotBlank(message = "Reason is required")
  private String raison;

  private String additionalInfo;

  private String status;

  private String userId;

  public Appointment() {
  }

  public Appointment(String firstName, String lastName, String phone,
      String email, CharSequence dateTime, String raison,
      String additionalInfo, String status, String userId) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phone = phone;
    this.dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    this.email = email;
    this.raison = raison;
    this.additionalInfo = additionalInfo;
    this.status = status;
    this.userId = userId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(CharSequence dateTime) {
    this.dateTime = LocalDateTime.parse(dateTime,
        DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public String getRaison() {
    return raison;
  }

  public void setRaison(String raison) {
    this.raison = raison;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Appointment that)) {
      return false;
    }
    return Objects.equals(id, that.id) && firstName.equals(that.firstName)
        && lastName.equals(that.lastName) && phone.equals(that.phone) && email.equals(that.email)
        && dateTime.equals(that.dateTime) && raison.equals(that.raison) && Objects.equals(
        additionalInfo, that.additionalInfo) && Objects.equals(status, that.status)
        && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, firstName, lastName, phone, email,
        dateTime, raison, additionalInfo, status, userId);
  }

  @Override
  public String toString() {
    return "Appointment{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", phone='" + phone + '\'' +
        ", email='" + email + '\'' +
        ", dateTime=" + dateTime +
        ", raison='" + raison + '\'' +
        ", additionalInfo='" + additionalInfo + '\'' +
        ", status='" + status + '\'' +
        ", userId='" + userId + '\'' +
        '}';
  }
}
