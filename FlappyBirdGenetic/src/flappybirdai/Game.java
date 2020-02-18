package flappybirdai;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final static Group ROOT = new Group();
    private final static Group BIRDS = new Group();
    private final static Group OBSTACLES = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() + 50;
    private final static Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);
    public final static double OBSTACLE_WIDTH = 100;
    public final static double OBSTACLE_GAP = 300;
    public final static double OBSTACLE_SPACING = 1100;
    private final static double OBSTACLE_SPEED = 2;
    private static final Text SCORE = new Text();
    private static boolean canPass = false;
    private static int counter = 0;
    private static int generations = 0;
    private static NNest.NN nn = new NNest().new NN(0, "relu", "sigmoid", "quadratic", "none", false, 15, 15, 1);
    private static boolean newBrain = false;
    public static int elapsed = 0;

    private final static int POPULATION = 10000;
    private final static double MUTATION_RATE = .1;
    private final static double MUTATION_RANGE = 2;
    private final static double RANDOMIZE_RANGE = 2;

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            update();
        }
    };

    private void update() {
        elapsed++;
        //Move obstacles instead birds to maybe help performance
        for (int i = 0; i < getObstacles().size(); i++) {
            getObstacles().get(i).setTranslateX(getObstacles().get(i).getTranslateX() - 5*OBSTACLE_SPEED);
        }
        if (getObstacles().get(0).getBoundsInParent().intersects(-OBSTACLE_WIDTH, 0, 1, BOUNDSY)) {//Obstacle goes off screen
            getObstacles().remove(0);//Remove top
            getObstacles().remove(0);//Remove bottom
            newObstacle(getObstacles().get(getObstacles().size() - 1).getLayoutX() + OBSTACLE_SPACING * 4 - OBSTACLE_WIDTH);
        }
        //Prevent multiple score increments from passing one obstacle
        counter++;
        if (counter > 150/OBSTACLE_SPEED) {
            counter = 0;
            canPass = true;
        }
        for (int i = 0; i < getBirds().size(); i++) {
            if (getBirds().get(i).getBoundsInParent().intersects(getObstacles().get(0).getBoundsInParent().getMaxX(), 0, 1, BOUNDSY) && canPass) {
                SCORE.setText(Integer.toString(Integer.parseInt(SCORE.getText()) + 1));
                SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
                canPass = false;
            }
            if (getObstacles().get(0).getBoundsInParent().intersects(getBirds().get(i).getBoundsInParent()) || getObstacles().get(1).getBoundsInParent().intersects(getBirds().get(i).getBoundsInParent())) {
                ((Bird) getBirds().get(i)).death();
            }
        }
        //Check for all birds dead
        int dead = 0;
        for (int i = 0; i < getBirds().size(); i++) {
            if (getBirds().get(i).isVisible() == false) {
                dead++;
            }
        }
        if (dead == getBirds().size()) {
            reset();
        }
    }

    private void newObstacle(double x) {
        new Obstacle(x, Math.random() * (BOUNDSY - OBSTACLE_GAP - (BOUNDSY / 24)) + (BOUNDSY / 48));
    }

    private void setup() {
        if (newBrain) {
            for (int i = 0; i < POPULATION; i++) {
                getBirds().add(new Bird());
                ((Bird) getBirds().get(i)).setFill(Color.hsb(Math.random() * 361, .9, .9));
                ((Bird) getBirds().get(i)).setBrain(nn.copy());
                ((Bird) getBirds().get(i)).getBrain().randomize(RANDOMIZE_RANGE);
            }
            newBrain = false;
        } else {
            for (int i = 0; i < POPULATION; i++) {
                getBirds().add(new Bird());
                ((Bird) getBirds().get(i)).setFill(Color.hsb(Math.random() * 361, .9, .9));
                ((Bird) getBirds().get(i)).setBrain(nn.copy());
                ((Bird) getBirds().get(i)).getBrain().mutate(MUTATION_RATE, MUTATION_RANGE);
            }
            getBirds().add(new Bird());
            ((Bird) getBirds().get(getBirds().size() - 1)).setBrain(nn.copy());
        }
        for (int i = 0; i < 4; i++) {
            newObstacle(BOUNDSX + i * OBSTACLE_SPACING);
        }
        timer.start();
    }

    private void reset() {
        elapsed = 0;
        generations++;
        System.out.println("New Generation " + generations);
        int bestBirdIndex = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < getBirds().size(); i++) {
            if (((Bird) getBirds().get(i)).getFitness() > max) {
                max = ((Bird) getBirds().get(i)).getFitness();
                bestBirdIndex = i;
            }
        }
        System.out.println(((Bird) getBirds().get(bestBirdIndex)).getFitness());
        nn = ((Bird) getBirds().get(bestBirdIndex)).getBrain().copy();
        getBirds().clear();
        getObstacles().clear();
        SCORE.setText("0");
        SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
        setup();
    }

    public static ObservableList<Node> getObstacles() {
        return OBSTACLES.getChildren();
    }

    public static ObservableList<Node> getBirds() {
        return BIRDS.getChildren();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setOnCloseRequest(event -> {
            if (nn != null) {
                nn.save();
            }
        });
        if (nn.load() == false) {
            newBrain = true;
        }
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
//        BACKGROUND.setFill(Color.BLACK);
        SCORE.setText("0");
        SCORE.setTranslateX(BOUNDSX - 100);
        SCORE.setTranslateY(100);
        SCORE.setScaleX(10);
        SCORE.setScaleY(10);
        ROOT.getChildren().addAll(BACKGROUND, BIRDS, OBSTACLES, SCORE);
        Scene scene = new Scene(ROOT, 0, 0);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        setup();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
