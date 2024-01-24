package com.zinphraek.leprestigehall.domain.data;

import static com.zinphraek.leprestigehall.domain.constants.Constants.CATEGORY_FACILITY;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CLEANING_FEES_LARGE_GUESTS_COUNT;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CLEANING_FEES_LARGE_GUESTS_COUNT_NAME;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CLEANING_FEES_SMALL_GUESTS_COUNT;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CLEANING_FEES_SMALL_GUESTS_COUNT_NAME;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ1;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ1_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ2;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ2_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ3;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ3_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ4;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ4_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ5;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ5_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ6;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FAQ6_ANSWER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.OVERTIME_HOURLY_RATE;
import static com.zinphraek.leprestigehall.domain.constants.Constants.OVERTIME_HOURLY_RATE_NAME;
import static com.zinphraek.leprestigehall.domain.constants.Constants.REGULAR_DAYS_FACILITY_RATE;
import static com.zinphraek.leprestigehall.domain.constants.Constants.REGULAR_FACILITY_RATE_NAME;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SATURDAY_FACILITY_RATE;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SATURDAY_FACILITY_RATE_NAME;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SEAT_RATE;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SEAT_RATE_NAME;

import com.zinphraek.leprestigehall.domain.addon.AddOn;
import com.zinphraek.leprestigehall.domain.addon.AddOnRepository;
import com.zinphraek.leprestigehall.domain.faq.FAQ;
import com.zinphraek.leprestigehall.domain.faq.FAQRepository;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** This class is responsible for seeding the database with the necessary data. */
@Component
public class SeedData implements CommandLineRunner {

  private final Logger logger = LogManager.getLogger(SeedData.class);

  @Autowired private final AddOnRepository addOnRepository;
  @Autowired private final FAQRepository faqRepository;

  public SeedData(AddOnRepository addOnRepository, FAQRepository faqRepository) {
    this.addOnRepository = addOnRepository;
    this.faqRepository = faqRepository;
  }

  /**
   * @param args All arguments
   */
  @Override
  public void run(String... args) {

    // Creating all necessary facilities' addon.
    List<AddOn> facilityFees = getAddOns();

    // Seeding FAQs
    List<FAQ> faqs = getFaqList();

    logger.info("Loading facility utilities rates...");
    // Filtering out all existing facility fees and saving the rest.
    facilityFees.parallelStream()
        .filter(addOn -> !addOnRepository.existsByName(addOn.getName()))
        .forEach(addOnRepository::save);
    logger.info("Facility utilities fees successfully loaded.");

    logger.info("Loading FAQs...");
    // Filtering out all existing FAQs and saving the rest.
    faqs.parallelStream()
        .filter(faq -> !faqRepository.existsByQuestion(faq.getQuestion()))
        .forEach(faqRepository::save);
    logger.info("FAQs successfully loaded.");
    logger.info("Requests can now be processed.");
  }

  private static List<FAQ> getFaqList() {
    FAQ faq1 = new FAQ("Facility", FAQ1, FAQ1_ANSWER, null);
    FAQ faq2 = new FAQ("Facility", FAQ2, FAQ2_ANSWER, null);
    FAQ faq3 = new FAQ("Facility", FAQ3, FAQ3_ANSWER, null);
    FAQ faq4 = new FAQ("Facility", FAQ4, FAQ4_ANSWER, null);
    FAQ faq5 = new FAQ("Facility", FAQ5, FAQ5_ANSWER, null);
    FAQ faq6 = new FAQ("Facility", FAQ6, FAQ6_ANSWER, null);

    return Arrays.asList(faq1, faq2, faq3, faq4, faq5, faq6);
  }

  private static List<AddOn> getAddOns() {
    AddOn seatRate =
        new AddOn(
            null,
            SEAT_RATE_NAME,
            CATEGORY_FACILITY,
            null,
            "The rate at which each seat is charged.",
            SEAT_RATE,
            false);
    AddOn overtimeRate =
        new AddOn(
            null,
            OVERTIME_HOURLY_RATE_NAME,
            CATEGORY_FACILITY,
            null,
            "The facility rate per hour after 2:00 AM.",
            OVERTIME_HOURLY_RATE,
            false);
    AddOn saturdayFacilityRate =
        new AddOn(
            null,
            SATURDAY_FACILITY_RATE_NAME,
            CATEGORY_FACILITY,
            null,
            "The facility rate on Saturday",
            SATURDAY_FACILITY_RATE,
            false);
    AddOn regularFacilityRate =
        new AddOn(
            null,
            REGULAR_FACILITY_RATE_NAME,
            CATEGORY_FACILITY,
            null,
            "The facility rate for days other than Saturday.",
            REGULAR_DAYS_FACILITY_RATE,
            false);
    AddOn cleaningFeesSmallGuestsCount =
        new AddOn(
            null,
            CLEANING_FEES_SMALL_GUESTS_COUNT_NAME,
            CATEGORY_FACILITY,
            null,
            "The cleaning fees for a reservation with guest count up to 100.",
            CLEANING_FEES_SMALL_GUESTS_COUNT,
            false);
    AddOn cleaningFeesLargeGuestsCount =
        new AddOn(
            null,
            CLEANING_FEES_LARGE_GUESTS_COUNT_NAME,
            CATEGORY_FACILITY,
            null,
            "The cleaning fees for a reservation with guest count above 100.",
            CLEANING_FEES_LARGE_GUESTS_COUNT,
            false);

    return Arrays.asList(
        seatRate,
        overtimeRate,
        regularFacilityRate,
        saturdayFacilityRate,
        cleaningFeesLargeGuestsCount,
        cleaningFeesSmallGuestsCount);
  }
}
