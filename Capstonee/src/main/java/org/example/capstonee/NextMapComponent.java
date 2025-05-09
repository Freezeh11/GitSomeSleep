package org.example.capstonee;

import com.almasb.fxgl.entity.component.Component;

public class NextMapComponent extends Component {
    private final String nextMap;

    public NextMapComponent(String nextMap) {
        this.nextMap = nextMap;
    }

    public String getNextMap() {
        return nextMap;
    }
}