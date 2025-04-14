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

    private static final double SPEED = 100;
    private static final double JUMP_SPEED = -200;

    public PlayerComponent() {
        Image image = image("player.png"); // Replace with your actual image file

        animIdle = new AnimationChannel(image, 4, 16, 32, Duration.seconds(1), 1, 1);
        animWalk = new AnimationChannel(image, 4, 16, 32, Duration.seconds(0.66), 0, 3);

        texture = new AnimatedTexture(animIdle);
        texture.loop();
    }

    @Override
    public void onAdded() {
        entity.getTransformComponent().setScaleOrigin(new Point2D(8, 16)); // Adjusted for 16x16 size
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        if (physics != null && physics.isMovingX()) {
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
        if (physics != null) {
            getEntity().setScaleX(-1);
            physics.setVelocityX(-SPEED);
        }
    }

    public void right() {
        if (physics != null) {
            getEntity().setScaleX(1);
            physics.setVelocityX(SPEED);
        }
    }

    public void stop() {
        if (physics != null) {
            physics.setVelocityX(0);
        }
    }

    public void jump() {
        if (physics != null && physics.isOnGround()) {
            physics.setVelocityY(JUMP_SPEED);
        }
    }
}