package com.zinphraek.leprestigehall.domain.sms;

public interface SMSService {
  void sendSMS(String toPhoneNumber, String message);
}
