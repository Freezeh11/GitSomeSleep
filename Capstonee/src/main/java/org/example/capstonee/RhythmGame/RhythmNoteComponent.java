package org.example.capstonee.RhythmGame;


import com.almasb.fxgl.entity.component.Component;


public class RhythmNoteComponent extends Component {

    private double speed;
    private long targetHitTimestamp;
    private int laneIndex;

    public RhythmNoteComponent(double speed, long targetHitTimestamp, int laneIndex) {
        this.speed = speed;
        this.targetHitTimestamp = targetHitTimestamp;
        this.laneIndex = laneIndex;
    }

    @Override
    public void onUpdate(double tpf) {
        entity.translateY(speed * tpf);
    }


    public long getTargetHitTimestamp() {
        return targetHitTimestamp;
    }

    public int getLaneIndex() {
        return laneIndex;
    }
}
