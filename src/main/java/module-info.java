module com.example.dbtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires twilio;
    requires java.sql;
    requires javafaker;


    opens com.example.dbtest to javafx.fxml;
    exports com.example.dbtest;
}