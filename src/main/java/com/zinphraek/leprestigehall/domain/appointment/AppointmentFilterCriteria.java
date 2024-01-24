package com.zinphraek.leprestigehall.domain.appointment;

import static com.zinphraek.leprestigehall.domain.constants.Constants.DATE_TIME_FORMAT;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentFilterCriteria {

  private Long id;
  private String firstName;

  private String lastName;

  private String phone;

  private String email;

  private LocalDateTime dateTime;

  private String status;

  private String userId;

  public AppointmentFilterCriteria() {}

  public AppointmentFilterCriteria(
      Long id,
      String firstName,
      String lastName,
      String phone,
      String email,
      CharSequence dateTime,
      String status,
      String userId) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phone = phone;
    this.email = email;
    this.dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
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
    this.dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
