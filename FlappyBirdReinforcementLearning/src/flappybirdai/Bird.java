package flappybirdai;

import static flappybirdai.Game.BOUNDSX;
import static flappybirdai.Game.BOUNDSY;
import static flappybirdai.Game.OBSTACLE_GAP;
import static flappybirdai.Game.epsilon;
import static flappybirdai.Game.getObstacles;
import static flappybirdai.Game.obstacleAhead;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle {

    private NNLib.NN nn;
    private NNLib.NN tn;
    public static final double RADIUS = 25;
    private double velocity = 0;
    private boolean falling;
    private boolean jump = false;
    private float[][] s;
    private int a;
    private float[][] s_;
    private final float DISCOUNT = .9f;
    public boolean throughObstacle = false;
    private ArrayList<Experience> replay = new ArrayList<>();
    private final int BATCHSIZE = 200;
    private final int FRAMESKIP = 10;
    private final int REPLAYSIZE = 10000;
    private int frames = 0;
    public static int deaths = 0;
    public static final int TNRESET = 20;

    Bird(Color color) {
        this.setRadius(RADIUS);
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setFill(color);
    }

    public void reset() {
        s_ = getState();//observe new state s_
        addExperience(s, a, reward(), s_, !isVisible());//observe reward and store in replay
        this.setTranslateX(BOUNDSX / 3);
        this.setTranslateY(BOUNDSY / 2);
        this.setVisible(true);
        velocity = 0;
        jump = false;
        frames = 0;
    }

    public void update() {
        frames++;
        if (frames % FRAMESKIP == 0) {
            s = getState();//initialize state s
            if (Math.random() < epsilon) {//Epsilon Greedy Strategy
                if (Math.random() < .5) {
                    a = 0;
                } else {
                    a = 1;
                }
//            System.out.println("wooo");
            } else {//Take action with the max Q value
                a = nn.argmax(nn.feedforward(s));
            }
//        float[][] probabilities = qn.softmax(qn.feedforward(s));
//        if (Math.random() < probabilities[0][0]) {
//            a = 0;
//        } else {
//            a = 1;
//        }
            jump = a == 0;//perform best or exploration action from state s
            jump();
        }
        gravity();
    }

    public void update2() {//observe after action a
        if (frames % FRAMESKIP == 0) {
            s_ = getState();//observe new state s_
            addExperience(s, a, reward(), s_, !isVisible());//observe reward and store in replay
        }
    }

    public void addExperience(float[][] s, int a, float r, float[][] s_, boolean terminal) {
        replay.add(new Experience(s, a, r, s_, terminal));
        if (replay.size() > REPLAYSIZE) {
            replay.remove(0);
        }
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
        setVelocity(11);
    }

    public void death() {
        deaths++;
        this.setVisible(false);
        update2();
    }

    public NNLib.NN getNN() {
        return nn;
    }

    public void setNN(NNLib.NN brain) {
        nn = brain.clone();
    }

    private float[][] getState() {
        float[][] state = new float[][]{{
            (float) getVelocity(),
            (float) getBoundsInParent().getMaxY(),
            (float) getBoundsInParent().getMinY(),
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
            reward = 1;
        } else if (isVisible() && !throughObstacle) {
            reward = 0;
        } else {
            reward = -1;
        }
//        reward -= .0001 * Math.abs((getBoundsInParent().getMaxY() - RADIUS) - (getObstacles().get(obstacleAhead).getBoundsInParent().getMaxY() - OBSTACLE_GAP));
//        totalReward += reward;
        return reward;
    }

    public void train() {
        for (int i = 0; i < BATCHSIZE; i++) {
            try {
                int index = nn.getRandom().nextInt(replay.size());
                Experience e = replay.get(index);
                float[][] Q_sa = nn.feedforward(e.s);
                float[][] Q_sa_ = tn.feedforward(e.s_);
                if (!e.t) {
                    Q_sa[0][e.a] = e.r + DISCOUNT * Q_sa_[0][tn.argmax(Q_sa_)];
                } else {
                    Q_sa[0][e.a] = e.r;
                }

//                nn.print(Q_sa, "before");
                nn.backpropagation(e.s, Q_sa);
//                nn.print(nn.feedforward(e.s), "after");
            } catch (Exception e) {

            }
        }
        if(deaths % TNRESET == 0){
            tn = nn.clone();
        }
    }

    public void setTN(NNLib.NN brain) {
        tn = brain.clone();
    }
}
