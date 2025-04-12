package com.example.capstone;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {
    private final List<Rectangle> collisionRects = new ArrayList<>();
    private static final double COLLISION_TOLERANCE = 0.5;

    public MapLoader(String tmxPath, Pane root) {
        loadMap(tmxPath, root);
    }

    private void loadMap(String tmxPath, Pane root) {
        try {
            File file = new File(tmxPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList objectGroups = doc.getElementsByTagName("objectgroup");
            for (int i = 0; i < objectGroups.getLength(); i++) {
                Element group = (Element) objectGroups.item(i);
                if (group.getAttribute("name").equals("Collision")) {
                    loadCollisionObjects(group, root);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading map: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCollisionObjects(Element group, Pane root) {
        NodeList objects = group.getElementsByTagName("object");
        for (int j = 0; j < objects.getLength(); j++) {
            Element obj = (Element) objects.item(j);
            try {
                double x = Double.parseDouble(obj.getAttribute("x"));
                double y = Double.parseDouble(obj.getAttribute("y"));
                double width = Double.parseDouble(obj.getAttribute("width"));
                double height = Double.parseDouble(obj.getAttribute("height"));

                Rectangle rect = createCollisionRect(x, y, width, height);
                root.getChildren().add(rect);
                collisionRects.add(rect);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing collision object coordinates: " + e.getMessage());
            }
        }
    }

    private Rectangle createCollisionRect(double x, double y, double width, double height) {
        Rectangle rect = new Rectangle(width, height);
        rect.setTranslateX(x);
        rect.setTranslateY(y);
        rect.setFill(Color.TRANSPARENT);
        rect.setStroke(Color.rgb(255, 0, 0, 0.3));
        return rect;
    }

    public void handleCollisions(Player player) {
        boolean wasOnGround = player.isOnGround();
        boolean onGround = false;
        boolean hitCeiling = false;
        boolean hitLeftWall = false;
        boolean hitRightWall = false;

        for (Rectangle block : collisionRects) {
            if (player.getBoundsInParent().intersects(block.getBoundsInParent())) {
                double playerBottom = player.getTranslateY() + player.getBoundsInLocal().getHeight();
                double blockTop = block.getTranslateY();

                double playerTop = player.getTranslateY();
                double blockBottom = block.getTranslateY() + block.getBoundsInLocal().getHeight();

                double playerRight = player.getTranslateX() + player.getBoundsInLocal().getWidth();
                double blockLeft = block.getTranslateX();

                double playerLeft = player.getTranslateX();
                double blockRight = block.getTranslateX() + block.getBoundsInLocal().getWidth();

                double bottomOverlap = blockTop - playerBottom;
                double topOverlap = playerTop - blockBottom;
                double rightOverlap = blockLeft - playerRight;
                double leftOverlap = playerLeft - blockRight;

                double minOverlap = Double.MAX_VALUE;
                int collisionSide = -1; // 0=top, 1=right, 2=bottom, 3=left

                if (topOverlap > -COLLISION_TOLERANCE && topOverlap < minOverlap) {
                    minOverlap = topOverlap;
                    collisionSide = 0;
                }
                if (rightOverlap > -COLLISION_TOLERANCE && rightOverlap < minOverlap) {
                    minOverlap = rightOverlap;
                    collisionSide = 1;
                }
                if (bottomOverlap > -COLLISION_TOLERANCE && bottomOverlap < minOverlap) {
                    minOverlap = bottomOverlap;
                    collisionSide = 2;
                }
                if (leftOverlap > -COLLISION_TOLERANCE && leftOverlap < minOverlap) {
                    minOverlap = leftOverlap;
                    collisionSide = 3;
                }

                switch (collisionSide) {
                    case 0: // Top collision
                        player.setTranslateY(blockBottom);
                        player.velocityY = Math.max(0, player.velocityY); // Stop upward movement
                        hitCeiling = true;
                        break;

                    case 1: // Right collision
                        player.setTranslateX(blockLeft - player.getBoundsInLocal().getWidth());
                        player.velocityX = Math.min(0, player.velocityX); // Stop rightward movement
                        hitRightWall = true;
                        break;

                    case 2: // Bottom collision (landing)
                        player.setTranslateY(blockTop - player.getBoundsInLocal().getHeight());
                        player.velocityY = Math.min(0, player.velocityY); // Stop downward movement
                        onGround = true;
                        break;

                    case 3: // Left collision
                        player.setTranslateX(blockRight);
                        player.velocityX = Math.max(0, player.velocityX); // Stop leftward movement
                        hitLeftWall = true;
                        break;
                }
            }
        }

        if (onGround && !wasOnGround && player.velocityY > 0) {
            player.velocityY = 0;
        }

        player.setOnGround(onGround);
        player.setWallCollision(hitLeftWall || hitRightWall);
    }
}