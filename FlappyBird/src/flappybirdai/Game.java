package flappybirdai;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final Group ROOT = new Group();
    private static final Group BIRDS = new Group();
    private static final Group OBSTACLES = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() + 50;
    private final Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);
    public static final double OBSTACLE_WIDTH = 100;
    public static final double OBSTACLE_GAP = 200;
    public final double OBSTACLE_SPACING = 650;
    private final double OBSTACLE_SPEED = 5;
    private final Text SCORE = new Text("0");
    private boolean canPass = false;
    public static int obstacleAhead = 0;
    private int highscore = 0;
    private final Text HIGH = new Text("Highscore: " + highscore);

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            update();
        }
    };

    private void update() {
        //Move obstacles instead birds to maybe help performance
        for (int i = 0; i < getObstacles().size(); i++) {
            for (int j = 0; j < OBSTACLE_SPEED; j++) {
                getObstacles().get(i).setTranslateX(getObstacles().get(i).getTranslateX() - 1);
                //Check if an obstacle has passed
                if (BOUNDSX / 3 == getObstacles().get(obstacleAhead).getBoundsInParent().getMaxX() && canPass) {
                    SCORE.setText(Integer.toString(Integer.parseInt(SCORE.getText()) + 1));
                    SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
                    canPass = false;//Prevent multiple increments
                }
            }
        }
        canPass = true;
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
        getBirds().add(new Bird(Color.WHITE));
        for (int i = 0; i < 4; i++) {
            newObstacle(BOUNDSX + i * OBSTACLE_SPACING);
        }
        timer.start();
    }

    private void reset() {
        getBirds().clear();
        getObstacles().clear();
        if (highscore < Integer.parseInt(SCORE.getText())) {
            highscore = Integer.parseInt(SCORE.getText());
            HIGH.setText("Highscore: " + highscore);
        }
        SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
        SCORE.setText("0");
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
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
        SCORE.setTranslateX(BOUNDSX - 100);
        SCORE.setTranslateY(100);
        SCORE.setScaleX(10);
        SCORE.setScaleY(10);
        HIGH.setTranslateX(BOUNDSX - 250);
        HIGH.setTranslateY(30);
        HIGH.setScaleX(2);
        HIGH.setScaleY(2);
        ROOT.getChildren().addAll(BACKGROUND, BIRDS, OBSTACLES, SCORE, HIGH);
        Scene scene = new Scene(ROOT, 0, 0);
        scene.setOnKeyPressed(eh -> {
            if (eh.getCode() == KeyCode.SPACE) {
                try {
                    ((Bird) getBirds().get(0)).setJump(true);
                } catch (Exception e) {

                }
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
