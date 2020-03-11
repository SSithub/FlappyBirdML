package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.elapsed;
import static flappybirdai.Game.obstacleAhead;
import static flappybirdai.Game.OBSTACLE_GAP;
import javafx.animation.AnimationTimer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNest.NN nn;
    private float[][] inputs;
    public static final double RADIUS = 25;
    private double velocity = 0;
    private boolean falling;
    private boolean jump = false;
    private final int DELAY = 10;
    private int delay = DELAY;
    private int fitness = 0;
    private double distanceFromGapLevel;
    private boolean alive;
//    private final AnimationTimer loop = new AnimationTimer() {
//        @Override
//        public void handle(long l) {
//            distanceFromGapLevel = Math.abs((getBoundsInParent().getMinY() + RADIUS) - (getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY() + OBSTACLE_GAP));
//            fitness = (int) (2 * elapsed + (BOUNDSY / (1 + distanceFromGapLevel)));
//            //Y of the top of the bird, Y of the bottom of the bird, X of the left side of the first obstacle, X of the right side of the first obstacle, X of the left side of the second obstacle, X of the right side of the second obstacle, Y of the top of the first obstacle's gap, Y of the bottom of the first obstacle's gap, Y of the top of the second obstacle's gap, Y of the bottom of the second obstacle's gap, fitness, velocity
////         
//            inputs = new float[][]{{(float) getBoundsInParent().getMinY(),
//                (float) getBoundsInParent().getMaxY(),
//                (float) getVelocity(),
//                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMinX(),
//                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(),
//                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY(),
//                (float) getObstacles().get(obstacleAhead + 1).getBoundsInParent().getMinY()}};
//            jump = nn.feedforward(inputs)[0][0] >= .5;
//            jump();
//            gravity();
//        }
//    };

    public void update() {
        if (alive) {
            distanceFromGapLevel = Math.abs((getBoundsInParent().getMinY() + RADIUS) - (getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY() + OBSTACLE_GAP));
            fitness = (int) (2 * elapsed + (BOUNDSY / (1 + distanceFromGapLevel)));

            inputs = new float[][]{{(float) getBoundsInParent().getMinY(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMinX(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY(),
                (float) getObstacles().get(obstacleAhead + 1).getBoundsInParent().getMinY()}};
            jump = nn.feedforward(inputs)[0][0] >= .5;
            jump();
            gravity();
        }
    }

    Bird(Color color) {
        this.setRadius(RADIUS);
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setFill(color);
        alive = true;
//        loop.start();
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
            if (falling) {
                setTranslateY(getTranslateY() + 1);
            } else {
                setTranslateY(getTranslateY() - 1);
            }
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
        }
    }

    public void death() {
//        loop.stop();
        this.setVisible(false);
        alive = false;
    }

    public int getFitness() {
        return fitness;
    }

    public NNest.NN getBrain() {
        return nn;
    }

    public void setBrain(NNest.NN brain) {
        nn = brain.clone();
    }
}
