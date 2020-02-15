package flappybirdai;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private double velocity = 0;

    Bird() {
        this.setRadius(25);
        this.setTranslateX(Game.BOUNDSX / 3);
        this.setTranslateY(Game.BOUNDSY / 2);
        this.setFill(Color.ALICEBLUE);
    }

    public double getVelocity() {
        return -velocity;
    }

    public void addVelocity(double delta) {
        velocity -= delta;
    }

    public void setVelocity(double velocity) {
        this.velocity = -velocity;
    }
}
