package com.zinphraek.leprestigehall.utilities.helpers;

public enum EventType {
  ANNIVERSARIES("Anniversaries"),
  BABY_SHOWERS("Baby Showers"),
  BIRTHDAY_PARTIES("Birthday Parties"),
  BRIDAL_SHOWERS("Bridal Showers"),
  CONFERENCES("Conferences"),
  GRADUATIONS("Graduations"),
  HOLIDAY_PARTIES("Holiday Parties"),
  MEETINGS("Meetings"),
  PHOTO_BOOTH_RENTAL("Photo Booth Rental"),
  SEMINARS("Seminars"),
  WEDDINGS("Weddings"),
  WORK_EVENTS("Work Events"),
  TRADITIONAL_EVENTS("Traditional Events"),
  OTHER_EVENTS("Other Events");

  private final String name;

  EventType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
