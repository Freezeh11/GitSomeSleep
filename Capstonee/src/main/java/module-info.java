module org.example.capstonee {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;

    opens org.example.capstonee to javafx.fxml;
    exports org.example.capstonee;
}