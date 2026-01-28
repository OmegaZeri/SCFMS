module com.example.dbtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires twilio;


    opens com.example.dbtest to javafx.fxml;
    exports com.example.dbtest;
}