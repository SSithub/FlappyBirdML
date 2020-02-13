package flappybirdai;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Obstacle {

    Obstacle(double x, double distanceOfGapFromTop) {
        Rectangle top = new Rectangle(x, 0, Game.OBSTACLE_WIDTH, distanceOfGapFromTop);
        Rectangle bottom = new Rectangle(x, distanceOfGapFromTop + Game.OBSTACLE_GAP, Game.OBSTACLE_WIDTH, Game.BOUNDSY - (distanceOfGapFromTop + Game.OBSTACLE_GAP));
        top.setFill(Color.GRAY);
        bottom.setFill(Color.GRAY);
        Game.getObstacles().addAll(top, bottom);
    }
}
