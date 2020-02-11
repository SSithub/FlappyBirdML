package flappybirdai;

import java.util.ArrayList;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final static Group ROOT = new Group();
    private final static Group BIRDS = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() - 25;
    private final static Rectangle BACKGROUND = new Rectangle(0, 0, BOUNDSX, BOUNDSY);

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long l) {
            for (int i = 0; i < BIRDS.getChildren().size(); i++) {
                movement((Bird) BIRDS.getChildren().get(i), ((Bird) BIRDS.getChildren().get(i)).velocity);
                if (((Bird) BIRDS.getChildren().get(i)).velocity < 10) {
                    ((Bird) BIRDS.getChildren().get(i)).velocity += .15;
                }
            }
        }
    };

    private void movement(Bird bird, double velocity) {
        for (int i = 0; i < BIRDS.getChildren().size(); i++) {
            for (int j = 0; j < velocity; j++) {
                bird.setTranslateY(bird.getTranslateY() + 1);
                if (bird.getBoundsInParent().intersects(0, BOUNDSY, BOUNDSX, 1)) {
                    Platform.runLater(() -> bird.setTranslateY(bird.getTranslateY() - 1));
                }
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
        BIRDS.getChildren().add(new Bird());
        ROOT.getChildren().addAll(BACKGROUND, BIRDS);
        Scene scene = new Scene(ROOT, 0, 0);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
