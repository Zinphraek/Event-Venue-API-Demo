package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.appointment.Appointment;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zinphraek.leprestigehall.domain.constants.Constants.*;

public class AppointmentFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();
  private static final String[] STATUS_VALUES = {
      STATUS_BOOKED,
      STATUS_CONFIRMED,
      STATUS_COMPLETED,
      STATUS_SETTLED,
  };

  public Appointment generateRandomAppointmentScheduledInThePast(Long id) {
    LocalDateTime date = utilities.generateRandomPastLocalDateTimeBetween9AMAnd730PM();
    Appointment appointment = new Appointment();
    appointment.setId(id != 0 ? id : null);
    appointment.setFirstName(utilities.generateRandomString());
    appointment.setLastName(utilities.generateRandomString());
    appointment.setPhone(utilities.generateRandomString());
    appointment.setEmail(utilities.generateRandomEmail());
    appointment.setDateTime(utilities.formatLocalDateTime(date));
    appointment.setRaison(utilities.generateRandomString());
    appointment.setAdditionalInfo(utilities.generateRandomString());
    appointment.setStatus(utilities.getRandomValueFromArray(STATUS_VALUES));
    appointment.setUserId(utilities.generateRandomString());
    return appointment;
  }

  public Appointment generateRandomAppointmentScheduledInTheFuture(Long id) {
    LocalDateTime date = utilities.generateRandomFutureLocalDateTimeBetween9AMAnd730PM();
    Appointment appointment = new Appointment();
    appointment.setId(id != 0 ? id : null);
    appointment.setFirstName(utilities.generateRandomString());
    appointment.setLastName(utilities.generateRandomString());
    appointment.setPhone(utilities.generateRandomString());
    appointment.setEmail(utilities.generateRandomEmail());
    appointment.setDateTime(utilities.formatLocalDateTime(date));
    appointment.setRaison(utilities.generateRandomString());
    appointment.setAdditionalInfo(utilities.generateRandomString());
    appointment.setStatus(utilities.getRandomValueFromArray(STATUS_VALUES));
    appointment.setUserId(utilities.generateRandomString());
    return appointment;
  }

  public List<Appointment> generateListOfRandomAppointmentsScheduledInThePast(
      int minIdValue, int maxIdValue, int listSize) {
    return utilities
        .generateListOfNDistinctRandomNumbersInRange(minIdValue, maxIdValue, listSize)
        .stream()
        .map(this::generateRandomAppointmentScheduledInThePast)
        .toList();
  }

  public List<Appointment> generateListOfRandomAppointmentsScheduledInTheFuture(
      int minIdValue, int maxIdValue, int listSize) {
    return utilities
        .generateListOfNDistinctRandomNumbersInRange(minIdValue, maxIdValue, listSize)
        .stream()
        .map(this::generateRandomAppointmentScheduledInTheFuture)
        .toList();
  }

  public Map<String, String>
  generateRandomAppointmentFilterCriteriaWithOptionalUserIdAndSpecificStatus(
      boolean shouldHaveUserId, String status) {
    Map<String, String> filterCriteria = new HashMap<>();

    String userId = shouldHaveUserId ? utilities.generateRandomStringWithDefinedLength(16) : null;
    filterCriteria.put("firstName", utilities.generateRandomString());
    filterCriteria.put("lastName", utilities.generateRandomString());
    filterCriteria.put("phone", utilities.generateRandomPhoneNumber());
    filterCriteria.put("email", utilities.generateRandomEmail());
    filterCriteria.put(
        "dateTime", utilities.formatLocalDateTime(utilities.generateRandomLocalDateTime()));
    filterCriteria.put("raison", utilities.generateRandomString());
    filterCriteria.put("additionalInfo", utilities.generateRandomString());
    filterCriteria.put(
        "status",
        status != null
            ? status
            : utilities.getRandomValueFromArray(STATUS_VALUES));
    filterCriteria.put("userId", userId);
    return filterCriteria;
  }
}
