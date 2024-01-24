package com.zinphraek.leprestigehall.domain.constants;

public class Regex {

  public static final String DATE_REGEX = "^(\\d{2}/){2}\\d{4}$";
  public static final String PHONE_REGEX = "^\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}$";
  public static final String ZIP_CODE_REGEX = "^\\d{5}(-\\d{4})?$";
  public static final String LOWERCASE_LETTERS_AND_UNDERSCORE_REGEX = "^[a-z_]{3,}$";
}
