package com.zinphraek.leprestigehall.domain.constants;

public class Paths {

  public static final String AdminPath = "/admin";
  public static final String AddOnPath = AdminPath + "/addons";
  public static final String AppointmentPath = "/appointments";
  public static final String EventPath = "/events";
  public static final String EventCommentPath = EventPath + "/{eventId}/comments";
  public static final String LikesDislikesPath = EventPath + "/{eventId}/likes";
  public static final String CommentsLikesDislikesPath =
      EventPath + "/{eventId}/comments/{commentId}/likes";
  public static final String KeycloakUrl = "http://localhost:8080";
  public static final String ReservationPath = "/reservations";
  public static final String InvoicePath = "/invoices";
  public static final String ReviewPath = "/reviews";
  public static final String UserPath = "/users";
  public static final String FAQPath = "/faqs";
}
