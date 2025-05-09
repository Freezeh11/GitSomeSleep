package org.example.capstonee;

import com.almasb.fxgl.entity.component.Component;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TextComponent extends Component {
    private final Text text;

    public TextComponent(String content, int fontSize, Color color) {
        this.text = new Text(content);
        this.text.setFont(Font.font(fontSize));
        this.text.setFill(color);
    }

    @Override
    public void onAdded() {
        // Clear any existing views first
        entity.getViewComponent().clearChildren();
        entity.getViewComponent().addChild(text);
    }
}