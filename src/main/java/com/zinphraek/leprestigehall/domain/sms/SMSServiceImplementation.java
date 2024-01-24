package com.zinphraek.leprestigehall.domain.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SMSServiceImplementation implements SMSService {

  @Value("${twilio.account.sid}")
  private String ACCOUNT_SID;

  @Value("${twilio.auth.token}")
  private String AUTH_TOKEN;

  @Value("${twilio.phone.number}")
  private String FROM_PHONE_NUMBER;

  private final Logger logger = LogManager.getLogger(SMSServiceImplementation.class);

  @Async
  public void sendSMS(String toPhoneNumber, String message) {
    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    Message.creator(new PhoneNumber(toPhoneNumber), new PhoneNumber(FROM_PHONE_NUMBER), message).create();
    logger.info("SMS successfully sent to " + toPhoneNumber);
  }
}
