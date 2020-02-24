package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.epsilon;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.obstacleAhead;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNest.NN nn;
    public static final double RADIUS = 25;
    private double velocity = 0;
    private boolean falling;
    private boolean jump = false;
    private final int DELAY = 10;
    private int delay = DELAY;
    private float[][] s;
    private float[][] sPrime;
    private int a = 0;
    private int aPrime;
    private final float DISCOUNT = .8f;
    private boolean startup = true;
    public boolean throughObstacle = false;

    Bird(Color color) {
        this.setRadius(RADIUS);
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setFill(color);
    }

    public void update() {
        if (startup) {
            s = nn.normalizeTanhEstimator(new float[][]{{(float) getBoundsInParent().getMinY(),
                (float) getBoundsInParent().getMaxY(),
                (float) getVelocity(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMinX(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(),
                (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY(),
                (float) getObstacles().get(obstacleAhead + 1).getBoundsInParent().getMinY()}});
            startup = false;
        }
        sPrime = nn.normalizeTanhEstimator(new float[][]{{(float) getBoundsInParent().getMinY(),
            (float) getBoundsInParent().getMaxY(),
            (float) getVelocity(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMinX(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY(),
            (float) getObstacles().get(obstacleAhead + 1).getBoundsInParent().getMinY()}});
        train();
        if (Math.random() < epsilon) {//Epsilon Greedy Strategy
            aPrime = Math.random() < .5 ? 0 : 1;
//                epsilon -= .00001;
//                System.out.println(epsilon);
        } else {//Take action with the max Q value
            aPrime = nn.indexMax(nn.feedforward(sPrime));
        }
        jump = aPrime == 0;
//            System.out.println(jump);
//            System.out.println(action);
        jump();
        gravity();
        s = nn.copy(sPrime);
        a = aPrime;
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
        }
    }

    public void death() {
        this.setVisible(false);
        train();
    }

    public NNest.NN getBrain() {
        return nn;
    }

    public void setBrain(NNest.NN brain) {
        nn = brain.clone();
    }

    public float reward() {
        if (isVisible() && throughObstacle) {
            throughObstacle = false;
//            System.out.println("woooo");
            return 10;
        } else if (isVisible() && !throughObstacle) {
            return .1f;
        } else {
            return -10;
        }
    }

    public void train() {
        float target = reward() + DISCOUNT * nn.feedforward(sPrime)[0][nn.indexMax(nn.feedforward(sPrime))];
        float[][] targets = nn.feedforward(s);
        targets[0][a] = target;
//        System.out.println(target);
        nn.backpropagation(s, targets);
    }
}
