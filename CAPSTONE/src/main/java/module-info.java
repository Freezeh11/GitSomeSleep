module com.example.capstone {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens com.example.capstone to javafx.fxml;
    exports com.example.capstone;
}