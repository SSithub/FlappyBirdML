package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.elapsed;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNest.NN nn;
    private double velocity = 0;
    private boolean jump = false;
    private final int DELAY = 1;
    private int delay = DELAY;
    private int fitness = 0;
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            fitness = elapsed;
            //Y of the top of the bird, Y of the bottom of the bird, X of the left side of the first obstacle, X of the right side of the first obstacle, X of the left side of the second obstacle, X of the right side of the second obstacle, Y of the top of the first obstacle's gap, Y of the bottom of the first obstacle's gap, Y of the top of the second obstacle's gap, Y of the bottom of the second obstacle's gap, fitness, velocity
//            float[][] inputs = {{(float) getBoundsInParent().getMinY(), (float) getBoundsInParent().getMaxY(), (float) getObstacles().get(0).getBoundsInParent().getMinX(), (float) getObstacles().get(0).getBoundsInParent().getMaxX(), (float) getObstacles().get(2).getBoundsInParent().getMinX(), (float) getObstacles().get(2).getBoundsInParent().getMaxX(), (float) getObstacles().get(0).getBoundsInParent().getMaxY(), (float) getObstacles().get(1).getBoundsInParent().getMinY(), (float) getObstacles().get(2).getBoundsInParent().getMaxY(), (float) getObstacles().get(3).getBoundsInParent().getMinY(), (float) fitness, (float) getVelocity()}};
            float[][] inputs = {{(float) getBoundsInParent().getMinY(),
                (float) getBoundsInParent().getMaxY(),
                (float) getObstacles().get(0).getBoundsInParent().getMinX(),
                (float) getObstacles().get(0).getBoundsInParent().getMaxX(),
                (float) getObstacles().get(0).getBoundsInParent().getMaxY(),
                (float) getObstacles().get(1).getBoundsInParent().getMinY(),
                (float) getObstacles().get(2).getBoundsInParent().getMinX(),
                (float) getObstacles().get(2).getBoundsInParent().getMaxX(),
                (float) getObstacles().get(2).getBoundsInParent().getMaxY(),
                (float) getObstacles().get(3).getBoundsInParent().getMinY(),
                (float) getObstacles().get(4).getBoundsInParent().getMinX(),
                (float) getObstacles().get(4).getBoundsInParent().getMaxX(),
                (float) getObstacles().get(4).getBoundsInParent().getMaxY(),
                (float) getObstacles().get(5).getBoundsInParent().getMinY(),
                (float) getVelocity()}};
            jump = nn.feedforward(inputs)[0][0] >= .5;
//            nn.print(nn.normalize(inputs), "");
            jump();
            gravity();
        }
    };

    Bird() {
        this.setRadius(25);
        this.setTranslateX(Game.BOUNDSX / 3);
        this.setTranslateY(Game.BOUNDSY / 2);
        this.setFill(Color.ALICEBLUE);
        timer.start();
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

    public boolean getJump() {
        return jump;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void gravity() {
        boolean falling = getVelocity() < 0;// < 0 = true; > 0 = false
        if (getVelocity() > -10) {//Terminal velocity
            addVelocity(-.3);
        }
        for (int j = 0; j < Math.abs(velocity); j++) {
            setTranslateY(getTranslateY() + (falling ? 1 : -1));
            if (getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1)) {//Hitting the bottom
                death();
            } else if (getBoundsInParent().intersects(0, 0, BOUNDSX, 1)) {
                death();
            }
        }
    }

    public void jump() {
//        delay--;
//        if (jump && delay < 0) {
//            setVelocity(7);
//            delay = DELAY;
//        }
        if (jump) {
            setVelocity(8);
        }
    }

    public void death() {
        timer.stop();
        this.setVisible(false);
    }

    public int getFitness() {
        return fitness;
    }

    public NNest.NN getBrain() {
        return nn;
    }

    public void setBrain(NNest.NN brain) {
        nn = brain.copy();
    }
}
