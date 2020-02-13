package flappybirdai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final static Group ROOT = new Group();
    private final static Group BIRDS = new Group();
    private final static Group OBSTACLES = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() - 25;
    private final static Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);
    private final HashMap<KeyCode, Boolean> keys = new HashMap<>();
    public final static double OBSTACLE_WIDTH = 100;
    public final static double OBSTACLE_GAP = 250;
    public final static double OBSTACLE_SPACING = 750;

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            update();
            for (int i = 0; i < BIRDS.getChildren().size(); i++) {
                movement((Bird) BIRDS.getChildren().get(i), ((Bird) BIRDS.getChildren().get(i)).getVelocity());
            }
        }
    };

    private void movement(Bird bird, double velocity) {
        boolean falling = velocity < 0;// < 0 = true; > 0 = false
        if (bird.getVelocity() > -10) {//Terminal velocity
            bird.addVelocity(-.2);
        }
        for (int i = 0; i < BIRDS.getChildren().size(); i++) {
            for (int j = 0; j < Math.abs(velocity); j++) {
                bird.setTranslateY(bird.getTranslateY() + (falling ? 1 : -1));//Pixel space
                if (bird.getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1) || bird.getBoundsInParent().intersects(0, 0, BOUNDSX, 1)) {
                    Platform.runLater(() -> bird.setTranslateY(bird.getTranslateY() - (falling ? 1 : -1)));
                }
            }
        }
    }

    private boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    private void update() {
        if (isPressed(KeyCode.SPACE)) {
            ((Bird) BIRDS.getChildren().get(0)).setVelocity(7);
        }
        keys.put(KeyCode.SPACE, false);//Key cannot be held down
        //Move obstacles instead birds to maybe help performance
        for (int i = 0; i < getObstacles().size(); i++) {
            getObstacles().get(i).setTranslateX(getObstacles().get(i).getTranslateX() - 5);
        }
        if (getObstacles().get(0).getBoundsInParent().intersects(-OBSTACLE_WIDTH, 0, 1, BOUNDSY)) {//Obstacle goes off screen
            getObstacles().remove(0);//Remove top
            getObstacles().remove(0);//Remove bottom

        }
    }

    public static ObservableList<Node> getObstacles() {
        return OBSTACLES.getChildren();
    }

    @Override
    public void start(Stage stage) throws Exception {
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
        BIRDS.getChildren().add(new Bird());
        ROOT.getChildren().addAll(BACKGROUND, BIRDS, OBSTACLES);
        Scene scene = new Scene(ROOT, 0, 0);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        timer.start();
        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        for (int i = 1; i <= 3; i++) {
            Obstacle a = new Obstacle(OBSTACLE_SPACING * i + 1000, Math.random() * (2 * BOUNDSY / 3) + (BOUNDSY / 6));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
