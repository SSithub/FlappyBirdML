package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.elapsed;
import static flappybirdai.Game.epsilon;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.obstacleAhead;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNest.NN nn;
    private NNest.NN qn;
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
    private final float DISCOUNT = .2f;
    private boolean startup = true;
    public boolean throughObstacle = false;
    private float[][] predictNN;
    private float[][] predictQN;

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
        predictNN = nn.feedforward(sPrime);

//        float sum = nn.sum(nn.softmax(predictNN));
//        double prob = Math.exp(predictNN[0][nn.argmax(predictNN)]) / sum;
//        if(Math.random() < prob){
//            aPrime = nn.argmax(predictNN);
//        }
//        System.out.println(prob);
        if (Math.random() < epsilon) {//Epsilon Greedy Strategy
            if (Math.random() < .5) {
                aPrime = 0;
            } else {
                aPrime = 1;
            }
        } else {//Take action with the max Q value
            aPrime = nn.argmax(predictNN);
        }
        jump = aPrime == 0;
//            System.out.println(jump);
//            System.out.println(action);
        jump();
        gravity();
        train();

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
            return 1;
        } else if (isVisible() && !throughObstacle) {
            return .1f;
        } else {
            return -1;
        }
    }

    public void train() {
        predictQN = qn.feedforward(sPrime);
        float target = reward() + DISCOUNT * predictQN[0][qn.argmax(predictQN)];
        float[][] targets = qn.feedforward(s);
        targets[0][a] = target;
//        if (elapsed % 50 == 0) {
//            System.out.println(target);
//        }
        nn.backpropagation(s, targets);
    }

    public void setQN(NNest.NN brain) {
        qn = brain.clone();
    }
}
