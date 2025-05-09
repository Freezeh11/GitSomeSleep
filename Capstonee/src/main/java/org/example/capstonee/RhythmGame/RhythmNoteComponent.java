package org.example.capstonee.RhythmGame;


import com.almasb.fxgl.entity.component.Component;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getAppHeight;


public class RhythmNoteComponent extends Component {

    private final double speed;
    private final long targetHitTimestamp;
    private final int laneIndex;

    public RhythmNoteComponent(double speed, long targetHitTimestamp, int laneIndex, double hitLineY) {

        if (speed <= 0) {
            System.err.println("Warning: RhythmNoteComponent created with non-positive speed: " + speed);
            this.speed = 100;
        } else {
            this.speed = speed;
        }
        this.targetHitTimestamp = targetHitTimestamp;
        this.laneIndex = laneIndex;
    }

    @Override
    public void onUpdate(double tpf) {

        entity.translateY(speed * tpf);
        if (entity.getY() > getAppHeight() + 200) {
            entity.removeFromWorld();
        }
    }


    public long getTargetHitTimestamp() {
        return targetHitTimestamp;
    }

    public int getLaneIndex() {
        return laneIndex;
    }

    public double getSpeed() { return speed; }
}