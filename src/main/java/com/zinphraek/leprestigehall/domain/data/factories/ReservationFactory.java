package com.zinphraek.leprestigehall.domain.data.factories;

import com.zinphraek.leprestigehall.domain.reservation.Reservation;
import com.zinphraek.leprestigehall.utilities.helpers.EventType;
import com.zinphraek.leprestigehall.utilities.helpers.FactoriesUtilities;

import java.time.LocalDateTime;

import static com.zinphraek.leprestigehall.domain.constants.Constants.STATUS_BOOKED;

public class ReservationFactory {

  FactoriesUtilities utilities = new FactoriesUtilities();

  private static final String[] EVENT_TYPE_VALUES = {
      EventType.ANNIVERSARIES.getName(),
      EventType.BABY_SHOWERS.getName(),
      EventType.BIRTHDAY_PARTIES.getName(),
      EventType.BRIDAL_SHOWERS.getName(),
      EventType.CONFERENCES.getName(),
      EventType.GRADUATIONS.getName(),
      EventType.HOLIDAY_PARTIES.getName(),
      EventType.MEETINGS.getName(),
      EventType.PHOTO_BOOTH_RENTAL.getName(),
      EventType.SEMINARS.getName(),
      EventType.WEDDINGS.getName(),
      EventType.WORK_EVENTS.getName(),
      EventType.TRADITIONAL_EVENTS.getName(),
      EventType.OTHER_EVENTS.getName()
  };

  public Reservation generateRandomReservation(Long id, boolean shouldStartInThePast, boolean shouldEndBeforeStart) {
    Reservation reservation = new Reservation();

    LocalDateTime startingDateTime = shouldStartInThePast
        ? utilities.generateRandomPastLocalDateTime(0)
        : utilities.generateRandomFutureLocalDateTime(0);

    LocalDateTime endingDateTime = shouldEndBeforeStart
        ? utilities.generateRandomFutureDateBefore(startingDateTime)
        : startingDateTime.isBefore(LocalDateTime.now())
        ? utilities.generateRandomLocalDateTime()
        : utilities.generateRandomFutureDateAfter(startingDateTime);

    reservation.setId(id);
    reservation.setStartingDateTime(utilities.formatLocalDateTime(startingDateTime));
    reservation.setEndingDateTime(utilities.formatLocalDateTime(endingDateTime));
    reservation.setNumberOfSeats(utilities.getRandomPositiveInt(1, 200));
    reservation.setEventType(utilities.getRandomValueFromArray(EVENT_TYPE_VALUES));
    reservation.setFullPackage(false);
    reservation.setAddOnsTotalCost(utilities.generateRandomDoubleWithin(1D, 100D));
    reservation.setStatus(STATUS_BOOKED);
    reservation.setTaxRate(.7);
    reservation.setUserId(utilities.generateRandomStringWithDefinedLength(16));
    reservation.setPriceComputationMethod("Auto");
    return reservation;
  }


}
