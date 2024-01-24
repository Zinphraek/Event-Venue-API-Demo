package com.zinphraek.leprestigehall.utilities.validator;

import com.zinphraek.leprestigehall.utilities.annotations.DatePattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DatePatternValidator implements ConstraintValidator<DatePattern, String> {

  private String pattern;

  @Override
  public void initialize(DatePattern constraintAnnotation) {
    pattern = constraintAnnotation.pattern();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
          .withResolverStyle(ResolverStyle.STRICT);
      LocalDate date = LocalDate.parse(value, formatter);

      // Check if the date is not in the past
      LocalDate today = LocalDate.now();
      return !date.isBefore(today);
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}