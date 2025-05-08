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

/**
 * A helper class to create and manage a parallax background effect
 * using multiple moving layers.
 */
public class ParallaxBackground {

    private Pane root;
    private List<Timeline> timelines;
    private double appWidth;
    private double appHeight;

    /**
     * Creates a new ParallaxBackground.
     * Layers should be added using addLayer().
     */
    public ParallaxBackground() {
        appWidth = FXGL.getAppWidth();
        appHeight = FXGL.getAppHeight();

        root = new Pane();
        timelines = new ArrayList<>();
        // Ensure the root pane is the size of the application window
        root.setPrefSize(appWidth, appHeight);
        root.setMaxSize(appWidth, appHeight); // Good practice to constrain size
    }

    /**
     * Adds a new parallax layer using the specified texture and speed factor.
     * Layers added first will be behind layers added later.
     * The texture will be scaled to fit the application height while preserving aspect ratio.
     * It will then be tiled horizontally.
     *
     * @param textureName The name of the texture file (e.g., "backgrounds/sky.png").
     *                    Assumes the texture is in assets/textures/
     * @param speedFactor Controls the movement speed. Higher value means faster movement.
     *                    Use smaller values for distant layers, larger for closer layers.
     *                    A speedFactor of 0 means no movement.
     */
    public void addLayer(String textureName, double speedFactor) {
        System.out.println("Adding parallax layer: " + textureName + " with speed " + speedFactor);
        try {
            // Load the texture once to get original dimensions
            // This will throw exception if file not found or is corrupted in a way FXGL can't handle
            Texture baseTexture = FXGL.texture(textureName);
            double originalImageWidth = baseTexture.getImage().getWidth();
            double originalImageHeight = baseTexture.getImage().getHeight();

            System.out.println("  Original size: " + originalImageWidth + "x" + originalImageHeight);

            // Calculate the scaled width based on fitting to app height while preserving aspect ratio
            // We need original height to be > 0 to avoid division by zero
            double scaledImageWidth = originalImageWidth; // Default to original if height is 0 or less
            if (originalImageHeight > 0) {
                scaledImageWidth = originalImageWidth * (appHeight / originalImageHeight);
            }

            System.out.println("  Calculated scaled size (to appHeight " + appHeight + ", aspect ratio preserved): " + scaledImageWidth + "x" + appHeight);


            // Check for invalid dimensions after calculation. If original height was 0, scaled width might still be originalImageWidth
            if (originalImageHeight <= 0 || scaledImageWidth < 1.0) {
                String reason = (originalImageHeight <= 0) ? "Original height is zero or less" : "Calculated scaled width is too small (" + scaledImageWidth + ")";
                System.err.println("Warning: Cannot add layer '" + textureName + "'. " + reason + ". Skipping layer.");
                // Use fallback message consistent with asset not found if height is 0, implies load issue
                if (originalImageHeight <= 0) {
                    System.err.println("Ensure the texture file exists and is valid in src/main/resources/assets/textures/" + textureName);
                }
                return; // Avoid issues with zero width or height
            }


            // Calculate how many copies are needed to cover the screen width PLUS one buffer width
            // for seamless looping. This ensures enough copies for animation.
            // If scaledImageWidth is exactly appWidth, this is ceil((appWidth + appWidth) / appWidth) = ceil(2) = 2.
            // If scaledImageWidth > appWidth, this is ceil((appWidth + scaledWidth) / scaledWidth) = ceil(appWidth/scaledWidth + 1) which is 1 + 1 = 2.
            // If scaledImageWidth < appWidth, this is ceil(appWidth/scaledWidth + 1) which is correct copies + 1.
            int numCopies = (int) Math.ceil((appWidth + scaledImageWidth) / scaledImageWidth);

            // Ensure at least 2 copies if speedFactor > 0 for the animation to make sense.
            // If speedFactor is 0, numCopies can be 1 if the scaled image is wider than the screen.
            if (speedFactor > 0 && numCopies < 2) {
                numCopies = 2; // Need at least 2 copies to animate a loop
            } else if (speedFactor == 0 && numCopies < 1) {
                numCopies = 1; // Need at least 1 copy if not animating but image is tiny/narrow
            }


            System.out.println("  App size: " + appWidth + "x" + appHeight);
            System.out.println("  Copies needed for coverage + buffer: " + numCopies);
            System.out.println("  Total layer pane width: " + (scaledImageWidth * numCopies));


            Pane layerPane = new Pane();
            layerPane.setPrefSize(scaledImageWidth * numCopies, appHeight);
            // Vertical alignment is top=0 by default, which works well when scaling to appHeight.

            // Create and position texture copies side-by-side within the layerPane
            // Load texture again for each copy instance needed for tiling
            for (int i = 0; i < numCopies; i++) {
                Texture copy = FXGL.texture(textureName); // Load a new Texture instance for each copy
                copy.setFitHeight(appHeight); // Scale height
                copy.setPreserveRatio(true); // Preserve aspect ratio
                copy.setTranslateX(i * scaledImageWidth); // Position horizontally using the *calculated* width
                layerPane.getChildren().add(copy);
            }

            root.getChildren().add(layerPane);

            // Create animation timeline ONLY if movement is needed (speedFactor > 0 and numCopies > 1)
            if (speedFactor > 0 && numCopies > 1) {
                // Animate the layerPane's translateX property
                // from 0 to -scaledImageWidth (the width of one scaled copy)
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(layerPane.translateXProperty(), 0)),
                        // Animate by exactly one scaled image width. Duration is calculated based on speed factor.
                        new KeyFrame(Duration.seconds(scaledImageWidth / speedFactor), new KeyValue(layerPane.translateXProperty(), -scaledImageWidth))
                );

                timeline.setCycleCount(Timeline.INDEFINITE); // Loop forever
                // Although INDEFINITE should loop seamlessly, adding the onFinished reset is a fail-safe
                timeline.setOnFinished(event -> {
                    // When one cycle finishes (moved left by one image width),
                    // instantly reset the translateX back to 0
                    layerPane.setTranslateX(0);
                });


                timelines.add(timeline);
                timeline.play(); // Start the animation for this layer upon creation
            } else if (numCopies == 1 && speedFactor == 0) {
                // If only one copy is needed AND no movement, center the single copy
                // This handles static background layers correctly.
                if (!layerPane.getChildren().isEmpty()) {
                    Texture singleCopy = (Texture) layerPane.getChildren().get(0);
                    double xOffset = (appWidth - scaledImageWidth) / 2.0; // Center calculation
                    singleCopy.setTranslateX(xOffset);
                    System.out.println("  Centering single static layer with offset: " + xOffset);
                }
            }
            // If numCopies > 1 and speedFactor is 0, the tiled images will just sit there
            // starting from the left edge. This correctly covers the width.


        } catch (Exception e) {
            System.err.println("Error adding parallax layer '" + textureName + "': " + e.getMessage());
            System.err.println("Ensure the texture file exists in src/main/resources/assets/textures/" + textureName);
            // Don't add the layer if loading fails
        }
        System.out.println("---");
    }

    /**
     * Returns the root Pane containing all the parallax layers.
     * This pane should be added to the FXGL menu's getContentRoot().
     */
    public Pane getRoot() {
        return root;
    }

    /**
     * Stops all ongoing parallax animations.
     * Should be called when the menu is exited.
     */
    public void stop() {
        timelines.forEach(Timeline::stop);
    }

    /**
     * Starts all ongoing parallax animations.
     * Can be called when the menu is entered if animations were stopped.
     */
    public void start() {
        timelines.forEach(Timeline::play);
    }
}