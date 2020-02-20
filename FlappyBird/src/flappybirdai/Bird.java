package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {
    public static final double RADIUS = 25;
    private double velocity = 0;
    private boolean falling;
    private boolean jump = false;
    private final int DELAY = 10;
    private int delay = DELAY;
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            jump();
            gravity();
        }
    };

    Bird(Color color) {
        this.setRadius(RADIUS);
        this.setTranslateX(Game.BOUNDSX / 3);
        this.setTranslateY(Game.BOUNDSY / 2);
        this.setFill(color);
        timer.start();
    }

    public double getVelocity() {
        return -velocity;
    }

    public void addVelocity(double change) {
        velocity -= change;
    }

    public void setVelocity(double velocity) {
        this.velocity = -velocity;
    }

    public boolean getJump() {
        return jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void gravity() {
        falling = getVelocity() < 0;// < 0 = true; > 0 = false
        addVelocity(-.49);
        for (int j = 0; j < Math.abs(velocity); j++) {
            setTranslateY(getTranslateY() + (falling ? 1 : -1));
            if (getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1)) {//Hitting the bottom
                death();
            } else if (getBoundsInParent().intersects(0, 0, BOUNDSX, 1)) {//Hitting the top
                death();
            }
        }
    }

    public void jump() {
        delay--;
        if (jump && delay < 0) {
            setVelocity(11);
            delay = DELAY;
            jump = false;
        }
    }

    public void death() {
        timer.stop();
        this.setVisible(false);
    }
}
