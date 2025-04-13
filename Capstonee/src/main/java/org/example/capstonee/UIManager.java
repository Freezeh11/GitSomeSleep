package org.example.capstonee;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.text.Text;

public class UIManager {

    public static Text createPixelCounterText() {
        Text text = new Text();
        text.setTranslateX(50);
        text.setTranslateY(100);
        text.textProperty().bind(FXGL.getWorldProperties().intProperty("pixelsMoved").asString());
        return text;
    }

    public static void addToScene(Text node) {
        FXGL.getGameScene().addUINode(node);
    }
}
