package com.example.dbtest;

import com.twilio.Twilio;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        Application.launch(SCFMSApplication.class, args);
        Twilio.init("AC82eaebf56afcec16ba38bc16775dbabd", "b72b2705cda0f56836e032bafcde5bcf");

    }


}
