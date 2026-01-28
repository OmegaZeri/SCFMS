package com.example.dbtest;

import com.twilio.rest.api.v2010.account.MessageCreator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


import java.io.IOException;

public class SCFMSApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(SCFMSApplication.class.getResource("SCFMS.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
        Twilio.init("AC82eaebf56afcec16ba38bc16775dbabd", "b72b2705cda0f56836e032bafcde5bcf");
        //MessageCreator twoFactorAuth = Message.creator(new PhoneNumber("+112058269239"),new PhoneNumber("+18559970149"), "Hello!");
    }
}
