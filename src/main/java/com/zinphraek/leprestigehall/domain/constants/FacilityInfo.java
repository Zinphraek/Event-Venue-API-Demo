package com.zinphraek.leprestigehall.domain.constants;

import static com.zinphraek.leprestigehall.domain.constants.Constants.BILLING_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CUSTOMER_SERVICE_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.CUSTOMER_SERVICE_PHONE_NUMBER;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FACILITY_CITY_LOCATION;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FACILITY_STATE_LOCATION;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FACILITY_STREET_LOCATION;
import static com.zinphraek.leprestigehall.domain.constants.Constants.FACILITY_ZIP_CODE_LOCATION;
import static com.zinphraek.leprestigehall.domain.constants.Constants.INFO_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.INVOICES_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.NO_REPLY_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SUPPORT_EMAIL_ADDRESS;
import static com.zinphraek.leprestigehall.domain.constants.Constants.SUPPORT_PHONE_NUMBER;

public class FacilityInfo {
  private final Address address = new Address();

  public static class Address {
    public String getCity() {
      return FACILITY_CITY_LOCATION;
    }

    public String getState() {
      return FACILITY_STATE_LOCATION;
    }

    public String getStreet() {
      return FACILITY_STREET_LOCATION;
    }

    public String getZipCode() {
      return FACILITY_ZIP_CODE_LOCATION;
    }
  }

  public FacilityInfo() {}

  public Address getAddress() {
    return address;
  }

  public String getSupportEmailAddress() {
    return SUPPORT_EMAIL_ADDRESS;
  }

  public String getSupportPhoneNumber() {
    return SUPPORT_PHONE_NUMBER;
  }

  public String getCustomerServicePhoneNumber() {
    return CUSTOMER_SERVICE_PHONE_NUMBER;
  }

  public String getCustomerServiceEmailAddress() {
    return CUSTOMER_SERVICE_EMAIL_ADDRESS;
  }

  public String getInvoicesEmailAddress() {
    return INVOICES_EMAIL_ADDRESS;
  }

  public String getNoReplyEmailAddress() {
    return NO_REPLY_EMAIL_ADDRESS;
  }

  public String getInfoEmailAddress() {
    return INFO_EMAIL_ADDRESS;
  }

  public String getBillingEmailAddress() {
    return BILLING_EMAIL_ADDRESS;
  }
}
