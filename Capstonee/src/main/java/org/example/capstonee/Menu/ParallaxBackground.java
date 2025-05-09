package org.example.capstonee.Menu;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.texture.Texture;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ParallaxBackground {

    private Pane root;
    private List<Timeline> timelines;
    private double appWidth;
    private double appHeight;

    public ParallaxBackground() {
        appWidth = FXGL.getAppWidth();
        appHeight = FXGL.getAppHeight();

        root = new Pane();
        timelines = new ArrayList<>();
        root.setPrefSize(appWidth, appHeight);
        root.setMaxSize(appWidth, appHeight);
    }

    public void addLayer(String textureName, double speedFactor) {
        try {
            Texture baseTexture = FXGL.texture(textureName);
            double originalImageWidth = baseTexture.getImage().getWidth();
            double originalImageHeight = baseTexture.getImage().getHeight();

            double scaledImageWidth = originalImageWidth;
            if (originalImageHeight > 0) {
                scaledImageWidth = originalImageWidth * (appHeight / originalImageHeight);
            }

            int numCopies = (int) Math.ceil((appWidth + scaledImageWidth) / scaledImageWidth);

            if (speedFactor > 0 && numCopies < 2) {
                numCopies = 2;
            } else if (speedFactor == 0 && numCopies < 1) {
                numCopies = 1;
            }

            Pane layerPane = new Pane();
            layerPane.setPrefSize(scaledImageWidth * numCopies, appHeight);

            for (int i = 0; i < numCopies; i++) {
                Texture copy = FXGL.texture(textureName);
                copy.setFitHeight(appHeight);
                copy.setPreserveRatio(true);
                copy.setTranslateX(i * scaledImageWidth);
                layerPane.getChildren().add(copy);
            }

            root.getChildren().add(layerPane);

            if (speedFactor > 0 && numCopies > 1) {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(layerPane.translateXProperty(), 0)),
                        new KeyFrame(Duration.seconds(scaledImageWidth / speedFactor), new KeyValue(layerPane.translateXProperty(), -scaledImageWidth))
                );

                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.setOnFinished(event -> {
                    layerPane.setTranslateX(0);
                });

                timelines.add(timeline);
                timeline.play();
            } else if (numCopies == 1 && speedFactor == 0) {
                if (!layerPane.getChildren().isEmpty()) {
                    Texture singleCopy = (Texture) layerPane.getChildren().get(0);
                    double xOffset = (appWidth - scaledImageWidth) / 2.0;
                    singleCopy.setTranslateX(xOffset);
                }
            }

        } catch (Exception e) {
            System.err.println("error '" + textureName + "': " + e.getMessage());
        }
    }

    public Pane getRoot() {
        return root;
    }

    public void stop() {
        timelines.forEach(Timeline::stop);
    }

    public void start() {
        timelines.forEach(Timeline::play);
    }
}