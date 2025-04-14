package org.example.capstonee;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.image;

public class PlayerComponent extends Component {

    private PhysicsComponent physics;

    private AnimatedTexture texture;

    private AnimationChannel animIdle, animWalk;


    public PlayerComponent() {

        Image image = image(" ");

        animIdle = new AnimationChannel(image, 4, 16, 16, Duration.seconds(1), 1, 1);
        animWalk = new AnimationChannel(image, 4, 16, 16, Duration.seconds(0.66), 0, 3);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(8, 8)); // Adjusted for 16x16 size
        entity.getViewComponent().addChild(texture);

        // Remove the listener for resetting jumps
        physics.onGroundProperty().addListener((obs, old, isOnGround) -> {

        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (physics.isMovingX()) {
            if (texture.getAnimationChannel() != animWalk) {
                texture.loopAnimationChannel(animWalk);
            }
        } else {
            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }
    }

    public void left() {
        getEntity().setScaleX(-1);
        physics.setVelocityX(-100);
    }

    public void right() {
        getEntity().setScaleX(1);
        physics.setVelocityX(100);
    }

    public void stop() {
        physics.setVelocityX(0);
    }

    public void jump() {
        if (!physics.isOnGround()) // Only allow jumping when on the ground
            return;

        physics.setVelocityY(-200);
    }
}