package com.example.dbtest;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.MessageCreator;
import javafx.fxml.FXML;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SCFMSController {

    @FXML public void textSend(){
        MessageCreator twoFactorAuth = Message.creator(new PhoneNumber("+1"),new PhoneNumber("+1"), "Fuck Twilio");
        }

}
