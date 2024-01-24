package com.zinphraek.leprestigehall.domain.constants;

public class GenericResponseMessages {

  // Successful CRUD operations messages
  public static final String CREATE_SUCCESS_MESSAGE = "%s successfully created.";
  public static final String DELETE_SUCCESS_MESSAGE = "%s with id: %s successfully deleted.";
  public static final String UPDATE_SUCCESS_MESSAGE = "%s with id: %s successfully updated.";
  public static final String GET_SUCCESS_MESSAGE = "%s with id: %s successfully retrieved.";
  public static final String GET_BY_FIELD_SUCCESS_MESSAGE = "%s with %s: %s successfully retrieved.";
  public static final String BULK_GET_SUCCESS_MESSAGE = "%s successfully retrieved.";
  public static final String GENERIC_ACTION_SUCCESS_MESSAGE = "%s with id: %s successfully %s.";

  // Conflicting CRUD operations messages
  // - CRUD Specific
  public static final String CREATE_CONFLICT_MESSAGE1 = "A %s with id: %s already exists in the database.";
  public static final String CREATE_CONFLICT_MESSAGE2 = "An %s with id: %s already exists in the database.";
  public static final String FIELD_CONFLICT_MESSAGE1 = "A %s with %s: %s already exists in the database.";
  public static final String FIELD_CONFLICT_MESSAGE2 = "An %s with %s: %s already exists in the database.";
  public static final String UPDATE_NON_MATCHING_FIELD_MESSAGE = "The %s associated with this %s does not match" +
      " the value on file. On file: %s, but %s was provided.";

  // Not found CRUD operations messages
  public static final String DELETE_NOT_FOUND_MESSAGE = "Could not delete non existent %s.";
  public static final String UPDATE_NOT_FOUND_MESSAGE = "Could not update non existent %s.";
  public static final String GET_NOT_FOUND_MESSAGE = "No %s with id: %s found in the database.";
  public static final String GET_FOR_NON_EXISTENT_USER_MESSAGE = "Cannot fetch %s for non existent user";
  public static final String CANCEL_NOT_FOUND_MESSAGE = "Could not cancel non existent %s.";
  public static final String GENERIC_ACTION_NOT_FOUND_MESSAGE = "Could not %s non existent %s.";

  public static final String GET_BY_FIELD_NOT_FOUND_MESSAGE = "No %s with %s: %s found in the database.";
  public static final String MASS_DELETE_NOT_FOUND_MESSAGE = "Could not delete the following %s with ids: %s, as they do not exist in the database";
  public static final String MASS_ACTION_NOT_FOUND_MESSAGE = "Could not %s the following %s with ids: %s, as they do not exist in the database";


  // Generic operations messages
  public static final String PARAMETER_MISMATCH_ERROR_MESSAGE = "Parameter id does not match the %s id.";
  public static final String FIELD_MISMATCH_ERROR_MESSAGE = "%s provided does not match the one on file.";
  public static final String MISSING_FIELD_ERROR_MESSAGE = "The %s field is required.";
  public static final String MIN_AND_MAX_ASSIGNMENT_ERROR_MESSAGE = "The minimum %s cannot be greater than the maximum %s.";
  public static final String FIELD_CONFLICT_ERROR_MESSAGE = "That %s is already taken.";

  // Generic error messages
  public static final String DATA_ACCESS_EXCEPTION_LOG_MESSAGE = "Data access issue encountered.";
  public static final String RUNTIME_EXCEPTION_LOG_MESSAGE = "Unexpected runtime error encountered.";
  public static final String GENERIC_UNEXPECTED_ERROR_LOG_MESSAGE = "Unexpected error encountered.";
  public static final String IllegalAccessError_IOException_LOG_MESSAGE = "Illegal access error or IO exception encountered.";
  public static final String ILLEGAL_ARGUMENT_EXCEPTION_LOG_MESSAGE = "Illegal argument exception encountered.";

  public static final String GENERIC_UNEXPECTED_ERROR_MESSAGE = "Oops, something unexpected happened. Please try again later.";
  public static final String BAD_REQUEST_RESPONSE_ERROR_MESSAGE = "Bad request. Please check your request and try again.";
  public static final String RESPONSE_STATUS_EXCEPTION_LOG_MESSAGE = "Response status exception encountered.";
  public static final String AUTHORIZATION_ERROR_MESSAGE = "You are not authorized to %s this %s.";

  // Reservation and Appointment specific
  public static final String APPOINTMENT_OVERLAP_ERROR_MESSAGE = "The requested date and time is already booked.";
  public static final String UNAVAILABLE_TIME_SLOT_ERROR_MESSAGE = "The chosen interval is already booked.";
  public static final String APPOINTMENT_IN_THE_PAST_ERROR_MESSAGE = "The cannot schedule an appointment in the past.";
  public static final String RESERVATION_END_BEFORE_START_ERROR_MESSAGE = "The %s date and time cannot be before the starting date and time.";
  public static final String RESERVATION_START_IN_THE_PAST_ERROR_MESSAGE = "The starting date and time cannot be in the past.";
  public static final String RESERVATION_WITH_NON_EXISTENT_ADD_ON_ERROR_MESSAGE = "The reservation cannot be created because the add-on with id: %s does not exist in the database.";
}
