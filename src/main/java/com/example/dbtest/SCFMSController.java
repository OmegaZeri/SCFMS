package com.example.dbtest;

import com.twilio.rest.api.v2010.account.MessageCreator;
import javafx.fxml.FXML;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SCFMSController {

    @FXML public void textSend(){
        MessageCreator twoFactorAuth = Message.creator(new PhoneNumber("+12058269239"),new PhoneNumber("+18559970149"), "Hello!");
        }

}
