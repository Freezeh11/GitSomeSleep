package org.example.capstonee.RhythmGame;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import static com.almasb.fxgl.dsl.FXGL.*; // Needed for getGameTimer potentially

public class GlowEffectComponent extends Component {

    private ColorAdjust glowEffect;
    // Adjust speed - 0.03 might be a good balance (fades in ~0.5s)
    private double fadeSpeed = 0.03;

    public GlowEffectComponent() {
        // Simpler ColorAdjust: Just max saturation, maybe slight brightness boost.
        // NO hue shift initially. Adjust brightness (3rd param) if needed.
        this.glowEffect = new ColorAdjust(0, 1.0, 1.0, 0); // Hue=0, Sat=1, Bright=1, Contrast=0
        // Alternative: Only saturation
        // this.glowEffect = new ColorAdjust(0, 1.0, 0.0, 0); // Hue=0, Sat=1, Bright=0, Contrast=0
    }

    @Override
    public void onAdded() {
        // System.out.println("GlowEffectComponent added to entity: " + entity + " at " + entity.getPosition()); // Keep for debugging
        if (!entity.getViewComponent().getChildren().isEmpty() &&
                entity.getViewComponent().getChildren().get(0) instanceof ImageView) {
            ImageView imageView = (ImageView) entity.getViewComponent().getChildren().get(0);
            // System.out.println("Applying glow effect to ImageView: " + imageView); // Keep for debugging
            imageView.setEffect(glowEffect);
        } else {
            System.err.println("Warning: GlowEffectComponent failed to find ImageView on entity: " + entity);
            // Add more details if needed...
        }
    }

    @Override
    public void onUpdate(double tpf) {
        double currentSaturation = glowEffect.getSaturation();
        // Optional: fade brightness too if you increased it
        double currentBrightness = glowEffect.getBrightness();

        // System.out.println("Glow Update: Sat=" + currentSaturation + " Bright=" + currentBrightness + " Entity=" + entity.getPosition()); // DEBUG LOG

        boolean fadedOut = true; // Assume faded out unless proven otherwise

        if (currentSaturation > 0) {
            glowEffect.setSaturation(Math.max(0, currentSaturation - fadeSpeed));
            fadedOut = false; // Still fading saturation
        }
        // Example: Fade brightness back to 0 if you started it at 1.0
        if (currentBrightness > 0) {
            glowEffect.setBrightness(Math.max(0, currentBrightness - fadeSpeed)); // Fade brightness too
            fadedOut = false; // Still fading brightness
        }


        if (fadedOut) { // Remove only when all relevant properties are faded
            if (entity != null && entity.isActive()) {
                // System.out.println("Removing Glow Entity: " + entity); // DEBUG LOG
                entity.removeFromWorld();
            }
        }
    }
}
