package org.example.capstonee;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

public class InteractionZoneComponent extends Component {

    private Entity npc;

    public InteractionZoneComponent(Entity npc) {
        this.npc = npc;
    }

    public Entity getNpc() {
        return npc;
    }

    @Override
    public void onUpdate(double tpf) {
        if (npc != null) {
            entity.setPosition(npc.getPosition());
        }
    }
}