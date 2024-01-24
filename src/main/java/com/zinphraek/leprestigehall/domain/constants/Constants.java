package com.zinphraek.leprestigehall.domain.constants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Constants {

  public static final String FRONTEND_CLIENT_URL = "https://www.leprestigehall.net";
  public static final String ADMIN_CLIENT_URL = "https://www.admin.leprestigehall.net";
  public static final String FEEDBACK_FORM_LINK = FRONTEND_CLIENT_URL + "/reviews";

  // Facility Information
  public static final String SUPPORT_EMAIL_ADDRESS = "demo-no-reply@leprestigehall.net";
  public static final String DEFAULT_PHONE_NUMBER = "+16143161430";
  public static final String DEFAULT_ADMIN_PHONE_NUMBER = "+16143161430";
  public static final String SUPPORT_PHONE_NUMBER = "(614) 316-1430";
  public static final String CUSTOMER_SERVICE_PHONE_NUMBER = "(614) 316-1430";
  public static final String CUSTOMER_SERVICE_EMAIL_ADDRESS = "demo-no-reply@leprestigehall.net";
  public static final String NO_REPLY_EMAIL_ADDRESS = "demo-no-reply@leprestigehalls.com";
  public static final String INFO_EMAIL_ADDRESS = "demo-no-reply@leprestigehall.net";
  public static final String INVOICES_EMAIL_ADDRESS = "demo-no-reply@leprestigehall.net";
  public static final String BILLING_EMAIL_ADDRESS = "demo-no-reply@leprestigehall.net";
  public static final String FACILITY_STREET_LOCATION = "740 Lakeview Plaza Blvd";
  public static final String FACILITY_CITY_LOCATION = "Worthington";
  public static final String FACILITY_STATE_LOCATION = "OH";
  public static final String FACILITY_ZIP_CODE_LOCATION = "43085";

  public static final String DATE_TIME_FORMAT = "yyyy-MM-dd, hh:mm a";

  public static final String DATE_FORMAT = "dd MMM yyyy";
  public static final String INVOICE_TEMPLATE_DATE_FORMAT = "MMM dd, yyyy";

  public static final String TIME_FORMAT = "hh:mm a";

  // public static final Double REGULAR_HOURLY_RATE = 100D;
  public static final Double OVERTIME_HOURLY_RATE = 150D;
  public static final Double SEAT_RATE = 2.5D;
  public static final Double REGULAR_DAYS_FACILITY_RATE = 1500D;
  public static final Double SATURDAY_FACILITY_RATE = 2000D;
  // public static final Double SECURITY_DEPOSIT = 500D;
  public static final Double CLEANING_FEES_LARGE_GUESTS_COUNT = 250D;
  public static final Double CLEANING_FEES_SMALL_GUESTS_COUNT = 150D;
  public static final String CATEGORY_FACILITY = "Facility";
  public static final String SEAT_RATE_NAME = "Seat Rate";
  public static final String OVERTIME_HOURLY_RATE_NAME = "Overtime Hourly Rate";
  public static final String REGULAR_FACILITY_RATE_NAME = "Regular Facility Rate";
  public static final String SATURDAY_FACILITY_RATE_NAME = "Saturday Facility Rate";

  public static final List<String> GENDERS = Arrays.asList("Male", "Female", "Non Binary");
  public static final String CLEANING_FEES_LARGE_GUESTS_COUNT_NAME = "Cleaning Large Guests Count";
  public static final String CLEANING_FEES_SMALL_GUESTS_COUNT_NAME = "Cleaning Small Guests Count";
  public static final String COMPUTATION_METHOD_AUTO_FLAG = "Auto";
  public static final String COMPUTATION_METHOD_MANUAL_FLAG = "Manual";

  // Reservation status
  public static final String STATUS_PENDING = "Pending";
  public static final String STATUS_BOOKED = "Booked";
  public static final String STATUS_SETTLED = "Settled";
  public static final String STATUS_CANCELLED = "Cancelled";
  public static final String STATUS_REQUESTED = "Requested";
  public static final String STATUS_DONE = "Done";

  public static final String STATUS_COMPLETED = "Completed";
  public static final String STATUS_IN_PROGRESS = "In Progress";
  public static final String STATUS_CONFIRMED = "Confirmed";
  public static final Map<String, String> RESERVATION_STATUSES_UPDATE_ACTIONS = Map.of(
      "Approve", STATUS_BOOKED,
      "Confirm", STATUS_CONFIRMED,
      "Cancel", STATUS_CANCELLED,
      "Mark as Done", STATUS_DONE,
      "Mark as Settled", STATUS_SETTLED,
      "Restore to Pending", STATUS_PENDING,
      "Restore to Booked", STATUS_BOOKED
  );

  public static final Map<String, String> RESERVATION_STATUSES_CHANGE_EMAIL_SUBJECTS = Map.of(
      STATUS_BOOKED, "Reservation Restored",
      STATUS_CANCELLED, "Reservation Cancellation",
      STATUS_DONE, "Reservation Completion",
      STATUS_SETTLED, "Reservation Settlement",
      STATUS_PENDING, "Reservation Restoration",
      STATUS_REQUESTED, "Reservation Request",
      STATUS_CONFIRMED, "Reservation Confirmation"
  );

  public static final Map<String, String> RESERVATION_STATUSES_CHANGE_EMAIL_BODIES = Map.of(
      STATUS_BOOKED, "Your reservation scheduled on %s has been restored. Please check your account for the reservation details at " + FRONTEND_CLIENT_URL + ", should you have any questions, please contact us at " + SUPPORT_PHONE_NUMBER + " or " + SUPPORT_EMAIL_ADDRESS + ".",
      STATUS_CANCELLED, "Your reservation scheduled on %s has been cancelled. Please check your account for the reservation details at " + FRONTEND_CLIENT_URL + ", should you have any questions, please contact us at " + SUPPORT_PHONE_NUMBER + " or " + SUPPORT_EMAIL_ADDRESS + ".",
      STATUS_DONE, "Your reservation scheduled on %s has been completed. Please check your account for the reservation details.",
      STATUS_SETTLED, "Your reservation scheduled on %s has been settled. Please check your account for the reservation details.",
      STATUS_PENDING, "Your reservation scheduled on %s has been restored. Please check your account for the reservation details at " + FRONTEND_CLIENT_URL,
      STATUS_REQUESTED, "Your reservation scheduled on %s has been requested. Please check your account for the reservation details.",
      STATUS_CONFIRMED, "Your reservation scheduled on %s has been confirmed. Please check your account for the reservation details at " + FRONTEND_CLIENT_URL
  );
  // Invoice status
  public static final String STATUS_PAID = "Paid";
  public static final String STATUS_DUE = "Due";
  public static final String STATUS_OVERDUE = "Overdue";
  public static final String STATUS_DUE_IMMEDIATELY = "Due Immediately";
  public static final String STATUS_PARTIALLY_PAID = "Partially Paid";

  public static final String DISCOUNT_TYPE_PERCENTAGE = "Percentage";
  public static final String DISCOUNT_TYPE_AMOUNT = "Amount";

  public static final String STATUS_WITHDRAWN = "Withdrawn";

  public static final String KEYCLOAK_REALM = "EventVenueDemo";

  // User required actions

  public static final String TANK_YOU_MESSAGE =
      "Thank you for choosing Le Prestige Hall. We hope to see you again soon.";

  public static final String LE_PRESTIGE_HALL = "Le Prestige Hall";

  // Templates
  public static final String GENERIC_EMAIL_TEMPLATE = "GenericEmailTemplate";
  public static final String EMAIL_RECEIPT_TEMPLATE = "EmailReceiptTemplate";
  public static final String APPOINTMENT_EMAIL_TEMPLATE = "AppointmentEmailTemplate";
  public static final String INVOICE_PDF_TEMPLATE = "InvoicePDFTemplate";
  public static final String RECEIPT_PDF_TEMPLATE = "ReceiptPDFTemplate";
  public static final String RESERVATION_EMAIL_TEMPLATE = "ReservationEmailTemplate";
  public static final String RESERVATION_CANCELLATION_EMAIL_TEMPLATE =
      "ReservationCancellationEmailTemplate";

  public static final String APPOINTMENT_CONFIRMATION_SUBJECT = "Appointment Confirmation";
  public static final String APPOINTMENT_CONFIRMATION_BODY =
      "Your appointment has been successfully booked. Please check your account for the appointment details.";

  public static final String APPOINTMENT_REMINDER_SUBJECT = "Appointment Reminder";
  public static final String APPOINTMENT_REMINDER_BODY =
      "This is a reminder that you have an appointment scheduled for tomorrow. Please check your account for the appointment details.";

  public static final String APPOINTMENT_CANCELLATION_SUBJECT = "Appointment Cancellation";
  public static final String APPOINTMENT_CANCELLATION_BODY =
      "Your appointment has been cancelled. Please check your account for the appointment details.";

  public static final String APPOINTMENT_RESCHEDULE_SUBJECT = "Appointment Reschedule";
  public static final String APPOINTMENT_RESCHEDULE_BODY =
      "Your appointment has been rescheduled. Please check your account for the appointment details.";

  public static final String APPOINTMENT_UPDATE_SUBJECT = "Appointment Update";
  public static final String APPOINTMENT_UPDATE_BODY =
      "Your appointment has been updated. Please check your account for the appointment details.";

  public static final String RESERVATION_CONFIRMATION_SUBJECT = "Reservation Confirmation";
  public static final String RESERVATION_CONFIRMATION_BODY =
      "Your reservation has been confirmed. Please check your account for the reservation details.";

  public static final String RESERVATION_REMINDER_SUBJECT = "Reservation Reminder";
  public static final String RESERVATION_REMINDER_BODY =
      "This is a reminder that you have a reservation scheduled for tomorrow. Please check your account for the reservation details.";

  public static final String RESERVATION_CANCELLATION_SUBJECT =
      "Confirmation of Your Reservation Cancellation";
  public static final String RESERVATION_CANCELLATION_BODY =
      "Your reservation has been cancelled. Please check your account for the reservation details.";

  public static final String RESERVATION_RESCHEDULE_SUBJECT = "Reservation Reschedule";
  public static final String RESERVATION_RESCHEDULE_BODY =
      "Your reservation has been rescheduled. Please check your account for the reservation details.";

  public static final String INVOICE_SUBJECT = "Invoice";
  public static final String INVOICE_BODY = "Please check your account for the invoice details.";

  public static final String PAYMENT_CONFIRMATION_SUBJECT = "Payment Confirmation";
  public static final String PAYMENT_CONFIRMATION_BODY =
      "Your payment has been confirmed. Please check your account for the payment details.";

  public static final String PAYMENT_REMINDER_SUBJECT = "Payment Reminder";
  public static final String PAYMENT_REMINDER_BODY =
      "This is a reminder that you have a payment due tomorrow. Please check your account for the payment details.";

  public static final String RECEIPT_PAYMENT_SUBJECT = "Payment Received – Thank You!";

  // Appointments and Reservations texts notifications messages to admins
  public static final String APPOINTMENT_CONFIRMATION_SMS =
      "A customer has booked an appointment. Here are the details: date and time: %s, customer name: %s, customer phone number: %s, customer email address: %s, customer message: %s";
  public static final String APPOINTMENT_CANCELLATION_SMS = "A customer has cancelled an appointment. Here are the details: date and time: %s, customer name: %s, customer phone number: %s, customer email address: %s.";
  public static final String APPOINTMENT_UPDATE_SMS = "A customer has updated an appointment. Here are the details: previous date and time: %s, new date and time: %s, customer name: %s, customer phone number: %s, customer email address: %s.";
  public static final String APPOINTMENT_RESTORATION_SMS = "The appointment with %s on %s, has been restored. Phone number: %s, email address: %s.";

  public static final String RESERVATION_CONFIRMATION_SMS =
      "A customer has booked a reservation for %s. Reservation id: %s, customer name: %s, customer email address: %s. Login to the admin portal at https://www.admin.leprestigehall.net to view the details and confirm the reservation.";
  public static final String RESERVATION_CANCELLATION_SMS = "A customer has cancelled a reservation. Here are the details: id: %s, date and time: %s, customer name: %s, customer email address: %s.";
  public static final String RESERVATION_UPDATE_SMS = "A customer has updated a reservation. Here are the details: reservation id: %s, previous date and time: %s, new date and time: %s, customer name: %s, customer email address: %s. Login to the admin portal at https://www.admin.leprestigehall.net to view the details and confirm the reservation.";


  public static final String VERIFY_EMAIL = "Verify Email";
  public static final String UPDATE_PASSWORD = "Update Password";
  public static final List<String> WEEK_DAYS =
      Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

  public static final Map<String, String> PAYMENT_METHODS =
      Map.of(
          "cash", "Cash", "cashApp", "CashApp", "card", "Card", "check", "Check", "zelle", "Zelle",
          "other", "Other");

  public static final String FAQ1 = "What is the hall's maximum capacity?";
  public static final String FAQ1_ANSWER = "The hall can accommodate up to 200 guests.";
  public static final String FAQ2 = "What time does the hall close?";
  public static final String FAQ2_ANSWER = "The hall closes at 3:00 AM.";
  public static final String FAQ3 = "What are the business office hours?";
  public static final String FAQ3_ANSWER = "The office is open from 9:00 AM to 3:00 AM.";
  public static final String FAQ4 = "Is the pricing customizable?";
  public static final String FAQ4_ANSWER =
      "Yes, the pricing is customizable. Contact us for more details.";
  public static final String FAQ5 = "How much is the security deposit?";
  public static final String FAQ5_ANSWER = "The security deposit is $500.";
  public static final String FAQ6 = "Do I have to clean up after the event?";
  public static final String FAQ6_ANSWER = "No, we will take care of the cleaning.";
}
