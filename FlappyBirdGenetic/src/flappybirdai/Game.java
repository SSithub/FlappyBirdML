package flappybirdai;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private static final Group ROOT = new Group();
    private static final Group BIRDS = new Group();
    private static final Group OBSTACLES = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() + 50;
    private static final Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);
    public static final double OBSTACLE_WIDTH = 100;
    public static final double OBSTACLE_GAP = 200;
    public static final double OBSTACLE_SPACING = 650;
    private static final double OBSTACLE_SPEED = 1;
    private static final Text SCORE = new Text("0");
    private static boolean canPass = false;
    private static int counter = 0;
    private static int generations = 0;
    private static NNest.NN nn = new NNest().new NN(0, "relu", "sigmoid", "quadratic", "none", false, 7, 7, 1);
    private static boolean newBrain = false;
    public static int elapsed = 0;
    public static int obstacleAhead = 0;
    private static int highscore = 0;
    private static final Text HIGH = new Text("Highscore: " + highscore);

    private static final int POPULATION = 20000;
    private static final double MUTATION_RATE = .05;
    private static final double MUTATION_RANGE = 2;
    private static final double RANDOMIZE_RANGE = 2;

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
            getObstacles().get(i).setTranslateX(getObstacles().get(i).getTranslateX() - 5 * OBSTACLE_SPEED);
        }
        if (getObstacles().get(0).getBoundsInParent().intersects(-OBSTACLE_WIDTH, 0, 1, BOUNDSY)) {//Obstacle goes off screen
            getObstacles().remove(0);//Remove top
            getObstacles().remove(0);//Remove bottom
            newObstacle(getObstacles().get(getObstacles().size() - 1).getLayoutX() + OBSTACLE_SPACING * 4 - OBSTACLE_WIDTH);
        }
        //Search for first obstacle ahead of birds
        for (int j = 0; j < getObstacles().size(); j++) {
            if (getObstacles().get(j).getBoundsInParent().getMaxX() >= (BOUNDSX / 3) - 2 * Bird.RADIUS) {
                obstacleAhead = j;
                break;
            }
        }
        //Check for collisions with obstacles
        for (int i = 0; i < getBirds().size(); i++) {
            if (getObstacles().get(obstacleAhead).getBoundsInParent().intersects(getBirds().get(i).getBoundsInParent()) || getObstacles().get(obstacleAhead + 1).getBoundsInParent().intersects(getBirds().get(i).getBoundsInParent())) {
                ((Bird) getBirds().get(i)).death();
            }
        }
        //Prevent multiple score increments from passing one obstacle
        counter++;
        if (counter > 130 / OBSTACLE_SPEED) {
            counter = 0;
            canPass = true;
        }
        //Check for birds passing an obstacle
        if (getBirds().get(0).getBoundsInParent().intersects(getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX(), 0, 1, BOUNDSY) && canPass) {
            SCORE.setText(Integer.toString(Integer.parseInt(SCORE.getText()) + 1));
            SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
            canPass = false;
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
                getBirds().add(new Bird(Color.hsb(Math.random() * 361, .9, .9)));
                ((Bird) getBirds().get(i)).setBrain(nn.copy());
                ((Bird) getBirds().get(i)).getBrain().randomizeNetwork(RANDOMIZE_RANGE);
            }
            newBrain = false;
        } else {
            for (int i = 0; i < POPULATION; i++) {
                getBirds().add(new Bird(Color.hsb(Math.random() * 361, .9, .9)));
                ((Bird) getBirds().get(i)).setBrain(nn.copy());
                ((Bird) getBirds().get(i)).getBrain().mutateNewValues(MUTATION_RATE, MUTATION_RANGE);
            }
            getBirds().add(new Bird(Color.WHITE));
            ((Bird) getBirds().get(getBirds().size() - 1)).setBrain(nn.copy());
        }
        for (int i = 0; i < 4; i++) {
            newObstacle(BOUNDSX + i * OBSTACLE_SPACING);
        }
        timer.start();
    }

    private void reset() {
        generations++;
        int bestBirdIndex = 0;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < getBirds().size(); i++) {
            if (((Bird) getBirds().get(i)).getFitness() > max) {
                max = ((Bird) getBirds().get(i)).getFitness();
                bestBirdIndex = i;
            }
        }
        nn = ((Bird) getBirds().get(bestBirdIndex)).getBrain().copy();
        System.out.println("Best Fitness For Generation " + generations + ": " + ((Bird) getBirds().get(bestBirdIndex)).getFitness());
        System.out.println("Ticks Elapsed: " + elapsed);
        getBirds().clear();
        getObstacles().clear();
        if (highscore < Integer.parseInt(SCORE.getText())) {
            highscore = Integer.parseInt(SCORE.getText());
            HIGH.setText("Highscore: " + highscore);
        }
        SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
        SCORE.setText("0");
        setup();
        elapsed = 0;
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
            if (!newBrain) {
                nn.save();
            }
        });
        Text close = new Text("Press Backspace To Save Current High And Quit,\nAlt + F4 To Save Last Generation's High");
        close.setTextAlignment(TextAlignment.LEFT);
        close.setTranslateX(50);
        close.setTranslateY(50);
        close.setScaleX(1.2);
        close.setScaleY(1.2);
        if (nn.load() == false) {
            newBrain = true;
        }
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
//        BACKGROUND.setFill(Color.BLACK);
        SCORE.setTranslateX(BOUNDSX - 100);
        SCORE.setTranslateY(100);
        SCORE.setScaleX(10);
        SCORE.setScaleY(10);
        HIGH.setTranslateX(BOUNDSX - 250);
        HIGH.setTranslateY(30);
        HIGH.setScaleX(2);
        HIGH.setScaleY(2);
        ROOT.getChildren().addAll(BACKGROUND, BIRDS, OBSTACLES, SCORE, HIGH, close);
        Scene scene = new Scene(ROOT, 0, 0);
        scene.setOnKeyPressed(eh -> {
            if (eh.getCode() == KeyCode.BACK_SPACE) {
                if (!newBrain) {
                    int bestBirdIndex = 0;
                    int max = Integer.MIN_VALUE;
                    for (int i = 0; i < getBirds().size(); i++) {
                        if (((Bird) getBirds().get(i)).getFitness() > max) {
                            max = ((Bird) getBirds().get(i)).getFitness();
                            bestBirdIndex = i;
                        }
                    }
                    nn = ((Bird) getBirds().get(bestBirdIndex)).getBrain().copy();
                }
                System.exit(0);
            }
        });
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
        setup();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
