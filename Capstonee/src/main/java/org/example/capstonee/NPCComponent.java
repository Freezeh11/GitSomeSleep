package org.example.capstonee;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class NPCComponent extends Component {

    private PhysicsComponent physics;
    private boolean isMovable;
    private double speed = 20;
    private boolean movingRight = true;
    private final String dialog;
    private double minX;
    private double maxX;

    public NPCComponent(boolean isMovable, String dialog, double minX, double maxX) {
        this.isMovable = isMovable;
        this.dialog = dialog;
        this.minX = minX;
        this.maxX = maxX;
    }

    public String getDialog() {
        return dialog;
    }

    public boolean isMovable() {
        return isMovable;
    }

    @Override
    public void onUpdate(double tpf) {
        if (isMovable) {
            if (movingRight) {
                entity.translateX(speed * tpf);
                if (entity.getX() > maxX) {
                    movingRight = false;
                }
            } else {
                entity.translateX(-speed * tpf);
                if (entity.getX() < minX) {
                    movingRight = true;
                }
            }
        }
    }
}