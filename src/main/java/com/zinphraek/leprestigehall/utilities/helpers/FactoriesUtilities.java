package com.zinphraek.leprestigehall.utilities.helpers;

import org.springframework.security.oauth2.jwt.Jwt;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FactoriesUtilities {

  private static final Random random = new Random();

  /**
   * Generates a random string.
   *
   * @return a random string.
   */
  public String generateRandomString() {
    return UUID.randomUUID().toString();
  }

  /**
   * Generates a random URL.
   *
   * @return a random URL.
   * @throws MalformedURLException if the URL is malformed.
   */
  public URL generateRandomURL() throws MalformedURLException {
    return new URL("https://example.com/" + generateRandomString());
  }

  /**
   * Generates a random string with a specified length.
   *
   * @param length the length of the string.
   * @return a random string with a specified length.
   */
  public String generateRandomStringWithDefinedLength(int length) {
    return UUID.randomUUID().toString().substring(0, length);
  }

  /**
   * Generates a random double in a provided range.
   *
   * @param minValue  the minimum value the double can be.
   * @param maxDouble the maximum value the double can be.
   * @return a random double.
   */
  public double generateRandomDoubleWithin(double minValue, double maxDouble) {
    return random.nextDouble(minValue, maxDouble);
  }

  /**
   * Generates a random long.
   *
   * @return a random long.
   */
  public long generateRandomLong() {
    return random.nextLong();
  }

  /**
   * Generates a list of distinct random longs.
   *
   * @param listSize the size of the list.
   * @return a list of distinct random longs.
   */
  public List<Long> generateListOfRandomDistinctLongs(int listSize) {
    return IntStream.range(0, listSize)
        .mapToObj(i -> random.nextLong())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Generates a random integer in a range of N number exclusive.
   *
   * @return a random integer.
   */
  public int generateRandomIntBetween0AndNExclusive(int n) {
    return random.nextInt(n);
  }

  /**
   * Generates a random integer.
   *
   * @param min the minimum value the integer can be.
   * @param max the maximum value the integer can be.
   * @return a random integer in a specified range.
   */
  public int getRandomPositiveInt(Integer min, Integer max) {
    return random.nextInt(min, max);
  }

  public long getRandomPositiveLong(long min, long max) {
    return random.nextLong(min, max);
  }

  public List<Integer> generateListOfRandomDistinctPositiveInt(int listSize) {
    return IntStream.range(0, listSize)
        .mapToObj(i -> random.nextInt(0, Integer.MAX_VALUE))
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * Generates a random phone number.
   *
   * @return a random phone number.
   */
  public String generateRandomPhoneNumber() {
    return String.format(
        "%s-%s-%s",
        generateRandomIntBetween0AndNExclusive(999),
        generateRandomIntBetween0AndNExclusive(999),
        generateRandomIntBetween0AndNExclusive(9999));
  }

  /**
   * Generates a specified size list of random numbers.
   *
   * @param listSize the size of the list.
   * @return a list of random integers.
   */
  public List<Long> generateListOfRandomInt(int listSize) {
    return IntStream.range(0, listSize)
        .mapToObj(i -> random.nextLong(Long.MAX_VALUE))
        .collect(Collectors.toList());
  }

  /**
   * Gets a random value from an array of strings.
   *
   * @param array the array of strings.
   * @return a random value from an array of strings.
   */
  public String getRandomValueFromArray(String[] array) {
    return array[random.nextInt(array.length)];
  }

  /**
   * Generates a specified size list of random universal unique identifiers.
   *
   * @param listSize the size of the list.
   * @return a list of random universal unique identifiers.
   */
  public List<UUID> generateListOfRandomUUIDs(int listSize) {
    return IntStream.range(0, listSize)
        .mapToObj(i -> UUID.randomUUID())
        .collect(Collectors.toList());
  }

  /**
   * Generates a specified size list of distinct random numbers in a specified range.
   *
   * @param minValue the minimum value a number can be.
   * @param maxValue the maximum value a number can be.
   * @param listSize the size of the list.
   * @return a list of random numbers in a specified range.
   */
  public List<Long> generateListOfNDistinctRandomNumbersInRange(
      int minValue, int maxValue, int listSize) {
    if (maxValue - minValue < listSize) {
      throw new IllegalArgumentException(
          "The range of values must be greater than the size of the list.");
    }
    List<Long> list = new ArrayList<>();
    while (list.size() < listSize) {
      long randomValue =
          random.longs(minValue, maxValue).distinct().limit(1).findFirst().getAsLong();
      if (!list.contains(randomValue)) {
        list.add(randomValue);
      }
    }
    return list;
  }

  /**
   * Generates a map of parameters for a custom page test.
   *
   * @param sortField the field to sort by.
   * @param pageSize  the size of the page.
   * @return a map of parameters for a custom page test.
   */
  public Map<String, String> getCustomPageTestParams(String sortField, String pageSize) {
    Map<String, String> params = new HashMap<>();
    params.put("sortField", sortField);
    params.put("pageSize", pageSize);
    params.put("sortOrder", "DESC");
    params.put("page", "0");

    return params;
  }

  /**
   * Generates a random Jwt with or without admin role.
   *
   * @param shouldHaveAdminRole whether the Jwt should have admin role.
   * @return a random Jwt with or without admin role.
   */
  public Jwt generateRandomJwt(boolean shouldHaveAdminRole) {
    Instant currentTimestamp = Instant.now();
    Instant expirationTimestamp = currentTimestamp.plusSeconds(3600);

    List<String> roles =
        shouldHaveAdminRole ? Collections.singletonList("admin") : Collections.emptyList();

    String userId = generateRandomStringWithDefinedLength(16);

    return Jwt.withTokenValue(generateRandomString())
        .header("alg", "RS256")
        .header("typ", "JWT")
        .claim("iss", "https://example.com")
        .claim("aud", "https://example.com")
        .claim("sub", userId)
        .claim("azp", "example")
        .claim("scope", "openid profile email")
        .claim("auth_time", currentTimestamp)
        .claim("iat", currentTimestamp)
        .claim("exp", expirationTimestamp)
        .claim("email", generateRandomEmail())
        .claim("roles", roles)
        .build();
  }

  /**
   * Generates a random email.
   *
   * @return a random email.
   */
  public String generateRandomEmail() {
    return generateRandomString() + "@example.com";
  }

  /**
   * Generates a random LocalDateTime.
   *
   * @return a random LocalDateTime.
   */
  public LocalDateTime generateRandomLocalDateTime() {
    return LocalDateTime.now();
  }

  /**
   * Generates a random LocalDateTime at a specific year from the current year in the future.
   *
   * @param year the number of years from the current year.
   * @return a random LocalDateTime in the future.
   */
  public LocalDateTime generateRandomFutureLocalDateTime(long year) {
    return LocalDateTime.now().plusYears(year).plusDays(random.nextInt(2, 30));
  }

  /**
   * Generates a random LocalDateTime in the future before a specific date.
   *
   * @param date the date to generate a random LocalDateTime before.
   * @return a random LocalDateTime in the future before a specified date.
   */
  public LocalDateTime generateRandomFutureDateBefore(LocalDateTime date) {
    LocalDateTime now = LocalDateTime.now();
    long hoursDifference = now.until(date, ChronoUnit.HOURS);
    if (hoursDifference < 0) {
      throw new IllegalArgumentException("The date provided must be in the future.");
    }
    return now.plusHours(getRandomPositiveLong(1, hoursDifference));
  }

  /**
   * Generates a random LocalDateTime in the future after a specific date.
   *
   * @param date the date to generate a random LocalDateTime after.
   * @return a random LocalDateTime in the future after a specified date.
   */
  public LocalDateTime generateRandomFutureDateAfter(LocalDateTime date) {
    LocalDateTime now = LocalDateTime.now();
    long hoursDifference = now.until(date, ChronoUnit.HOURS);

    if (hoursDifference < 0) {
      throw new IllegalArgumentException("The date provided must be in the future.");
    }

    long hoursToAdd = getRandomPositiveLong(1, hoursDifference > 2 ? hoursDifference : 5);
    return date.plusHours(hoursToAdd);
  }

  /**
   * Generates a random LocalDateTime at a specific year from the current year in the past.
   *
   * @param year the number of years from today.
   * @return a random LocalDateTime in the past.
   */
  public LocalDateTime generateRandomPastLocalDateTime(long year) {
    return LocalDateTime.now().minusDays(random.nextInt(30)).plusYears(year);
  }

  /**
   * Generates a random LocalDateTime in the future.
   *
   * @return a random LocalDateTime in the future.
   */
  public String formatLocalDateTime(LocalDateTime localDateTime) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, hh:mm a");
    return localDateTime.format(formatter);
  }

  /**
   * Generates a random LocalDateTime in the future falling between 9 AM and 7:30 PM.
   *
   * @return a random LocalDateTime in the future falling between 9 AM and 7:30 PM.
   */
  public LocalDateTime generateRandomFutureLocalDateTimeBetween9AMAnd730PM() {
    return LocalDateTime.now().plusDays(random.nextInt(1, 30)).withHour(random.nextInt(10) + 9);
  }

  /**
   * Generates a random LocalDateTime in the past falling between 9 AM and 7:30 PM.
   *
   * @return a random LocalDateTime in the past falling between 9 AM and 7:30 PM.
   */
  public LocalDateTime generateRandomPastLocalDateTimeBetween9AMAnd730PM() {
    return LocalDateTime.now().minusDays(random.nextInt(30)).withHour(random.nextInt(10) + 9);
  }

  /**
   * Generates a random LocalDateTime in the future falling outside 9 AM and 7:30 PM.
   *
   * @return a random LocalDateTime in the future falling outside 9 AM and 7:30 PM.
   */
  public LocalDateTime generateRandomFutureLocalDateTimeOutside9AMTo730PM() {
    return LocalDateTime.now().plusDays(random.nextInt(30)).withHour(random.nextInt(24));
  }

  /**
   * Generates a random LocalDateTime in the past falling outside 9 AM and 7:30 PM.
   *
   * @return a random LocalDateTime in the past falling outside 9 AM and 7:30 PM.
   */
  public LocalDateTime generateRandomPastLocalDateTimeOutside9AMTo730PM() {
    return LocalDateTime.now().minusDays(random.nextInt(30)).withHour(random.nextInt(24));
  }
}
