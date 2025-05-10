module org.example.capstonee {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jlayer;


    opens org.example.capstonee to javafx.fxml;
    exports org.example.capstonee;
}