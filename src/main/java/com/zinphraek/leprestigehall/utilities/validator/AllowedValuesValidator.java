package com.zinphraek.leprestigehall.utilities.validator;

import com.zinphraek.leprestigehall.utilities.annotations.AllowedValues;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

  private Set<String> allowedValues;

  @Override
  public void initialize(AllowedValues constraintAnnotation) {
    allowedValues = new HashSet<>(Arrays.asList(constraintAnnotation.allowedValues()));
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || allowedValues.contains(value);
  }
}
