package flappybirdai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final static Group ROOT = new Group();
    private final static Group BIRDS = new Group();
    private final static Group OBSTACLES = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() + 50;
    private final static Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);
    private final HashMap<KeyCode, Boolean> keys = new HashMap<>();
    public final static double OBSTACLE_WIDTH = 100;
    public final static double OBSTACLE_GAP = 200;
    public final static double OBSTACLE_SPACING = 750;
    private static final Text SCORE = new Text();
    private static boolean isPassed = false;
    private static int counter = 0;
    Stage stage;

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            update();
            for (int i = 0; i < getBirds().size(); i++) {
                movement((Bird) getBirds().get(i), ((Bird) getBirds().get(i)).getVelocity());
            }
        }
    };

    private void movement(Bird bird, double velocity) {
        boolean falling = velocity < 0;// < 0 = true; > 0 = false
        if (bird.getVelocity() > -10) {//Terminal velocity
            bird.addVelocity(-.2);
        }
        for (int i = 0; i < getBirds().size(); i++) {
            for (int j = 0; j < Math.abs(velocity); j++) {
                bird.setTranslateY(bird.getTranslateY() + (falling ? 1 : -1));
                if (bird.getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1) || bird.getBoundsInParent().intersects(0, 0, BOUNDSX, 1)) {
                    Platform.runLater(() -> bird.setTranslateY(bird.getTranslateY() - (falling ? 1 : -1)));
                }
                if (bird.getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1)) {//Hitting the bottom
                    reset();
                }
            }
        }
    }

    private boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    private void update() {
        if (isPressed(KeyCode.SPACE)) {
            ((Bird) getBirds().get(0)).setVelocity(7);
        }
        keys.put(KeyCode.SPACE, false);//Key cannot be held down
        //Move obstacles instead birds to maybe help performance
        for (int i = 0; i < getObstacles().size(); i++) {
            getObstacles().get(i).setTranslateX(getObstacles().get(i).getTranslateX() - 5);
        }
        if (getObstacles().get(0).getBoundsInParent().intersects(-OBSTACLE_WIDTH, 0, 1, BOUNDSY)) {//Obstacle goes off screen
            getObstacles().remove(0);//Remove top
            getObstacles().remove(0);//Remove bottom
            new Obstacle(getObstacles().get(getObstacles().size() - 1).getLayoutX() + OBSTACLE_SPACING * 4 - OBSTACLE_WIDTH, Math.random() * (2 * BOUNDSY / 3) + (BOUNDSY / 6));
        }
        //Prevent multiple score increments from passing one obstacle
        counter++;
        if (counter > 150) {
            counter = 0;
            isPassed = true;
        }
        if (getBirds().get(0).getBoundsInParent().intersects(getObstacles().get(0).getBoundsInParent().getMaxX(), 0, 1, BOUNDSY) && isPassed) {
            SCORE.setText(Integer.toString(Integer.parseInt(SCORE.getText()) + 1));
            SCORE.setTranslateX(BOUNDSX - 100 - 45 * (int) (Math.log10(Integer.parseInt(SCORE.getText()))));
            isPassed = false;
        }
        if (getObstacles().get(0).getBoundsInParent().intersects(getBirds().get(0).getBoundsInParent()) || getObstacles().get(1).getBoundsInParent().intersects(getBirds().get(0).getBoundsInParent())) {
            reset();
        }
    }

    private void setup() {
        BIRDS.getChildren().add(new Bird());
        for (int i = 0; i < 4; i++) {
            new Obstacle(BOUNDSX + i * OBSTACLE_SPACING, Math.random() * (BOUNDSY / 3) + (BOUNDSY / 6));
        }
        timer.start();
    }

    private void reset() {
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
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
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
        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        setup();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
