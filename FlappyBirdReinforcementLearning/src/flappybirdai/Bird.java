package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.epsilon;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.obstacleAhead;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNest.NN qn;
    private NNest.NN tn;
    public static final double RADIUS = 25;
    private double velocity = 0;
    private boolean falling;
    private boolean jump = false;
    private final int DELAY = 10;
    private int delay = DELAY;
    private float[][] s;
    private float[][] s_;
    private int a;
    private final float DISCOUNT = .2f;
    public boolean throughObstacle = false;
    private ArrayList<Experience> experienceReplay = new ArrayList<>();
    float totalReward;
    private final int BATCHSIZE = 1000;

    Bird(Color color) {
        this.setRadius(RADIUS);
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setFill(color);
    }

    public void reset() {
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setVisible(true);
        velocity = 0;
        jump = false;
    }

    public void update() {
        s = getState();//initialize state s
        if (Math.random() < epsilon) {//Epsilon Greedy Strategy
            if (Math.random() < .2) {
                a = 0;
            } else {
                a = 1;
            }
//            System.out.println("wooo");
        } else {//Take action with the max Q value
            a = qn.argmax(qn.feedforward(s));
        }
//        float[][] probabilities = qn.softmax(qn.feedforward(s));
//        if (Math.random() < probabilities[0][0]) {
//            a = 0;
//        } else {
//            a = 1;
//        }
        jump = a == 0;//perform best or exploration action from state s
        jump();
        gravity();
    }

    public void update2() {//observe after action a
        s_ = getState();//observe new state s_
        experienceReplay.add(new Experience(s, a, reward(), s_, !isVisible()));//observe reward and store in replay
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
        update2();
    }

    public NNest.NN getQN() {
        return qn;
    }

    public void setQN(NNest.NN brain) {
        qn = brain.clone();
    }

    private float[][] getState() {
        float[][] state = new float[][]{{(float) getBoundsInParent().getMaxY(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMinX(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(),
            (float) getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY(),
            (float) getObstacles().get(obstacleAhead + 1).getBoundsInParent().getMinY()}};
        return state;
    }

    public float reward() {
        float reward;
        if (isVisible() && throughObstacle) {
            throughObstacle = false;
            reward = 10;
        } else if (isVisible() && !throughObstacle) {
            reward = 0;
        } else {
            reward = -1;
        }
        totalReward += reward;
        return reward;
    }

    public void train(float[][] s, int a, float r, float[][] s_, boolean terminal) {
        float[][] predictTN = tn.feedforward(s_);
        float target;
        if (!terminal) {
            target = r + DISCOUNT * predictTN[0][tn.argmax(predictTN)];
        } else {
            target = r;
        }
//        System.out.println(target);
        float[][] targets = qn.feedforward(s);
        targets[0][a] = target;
        qn.backpropagation(s, targets);
    }

    public void setTN(NNest.NN brain) {
        tn = brain.clone();
    }

    public void experienceReplayTraining() {
        for (int i = 0; i < BATCHSIZE; i++) {
            Experience e = experienceReplay.get((int) (Math.random() * experienceReplay.size()));
            train(e.s, e.a, e.r, e.s_, e.terminal);
        }
        tn = qn.clone();
        experienceReplay = new ArrayList<>();
        totalReward = 0;
    }
}
