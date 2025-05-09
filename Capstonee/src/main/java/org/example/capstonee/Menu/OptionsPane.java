package org.example.capstonee.Menu;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;



// NO FXGL SHIT



public class OptionsPane extends VBox {

    private static DoubleProperty globalVolumeProperty = new SimpleDoubleProperty(0.5);
    private static BooleanProperty globalMuteProperty = new SimpleBooleanProperty(false);

    public OptionsPane() {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(20));

        Text title = new Text("Audio Options");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font-size: 28px;");

        Text volumeLabel = new Text("Overall Volume:");
        volumeLabel.setFill(Color.WHITE);
        volumeLabel.setStyle("-fx-font-size: 18px;");

        Slider volumeSlider = new Slider(0, 1, globalVolumeProperty.get());
        volumeSlider.setPrefWidth(200);
        volumeSlider.setBlockIncrement(0.05);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.5);
        volumeSlider.setMinorTickCount(5);

        globalVolumeProperty.bind(volumeSlider.valueProperty());

        Button btnMute = new Button();
        btnMute.setPrefWidth(200);
        btnMute.setPrefHeight(40);

        String buttonStyle = "-fx-font-size: 16px; -fx-background-color: #036661; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        String buttonHoverStyle = "-fx-font-size: 16px; -fx-background-color: #047F79; -fx-text-fill: white; -fx-background-radius: 5; -fx-border-radius: 5;";
        btnMute.setStyle(buttonStyle);
        btnMute.setOnMouseEntered(e -> btnMute.setStyle(buttonHoverStyle));
        btnMute.setOnMouseExited(e -> btnMute.setStyle(buttonStyle));

        btnMute.textProperty().bind(
                Bindings.when(globalMuteProperty)
                        .then("Unmute")
                        .otherwise("Mute")
        );

        btnMute.setOnAction(e -> {
            globalMuteProperty.set(!globalMuteProperty.get());
        });

        getChildren().addAll(
                title,
                volumeLabel,
                volumeSlider,
                btnMute
        );
    }

    public static DoubleProperty globalVolumeProperty() {
        return globalVolumeProperty;
    }


    public static BooleanProperty globalMuteProperty() {
        return globalMuteProperty;
    }
}