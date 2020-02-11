package flappybirdai;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Game extends Application {

    private final static Group ROOT = new Group();
    public static final double BOUNDSX = Screen.getPrimary().getVisualBounds().getMaxX();
    public static final double BOUNDSY = Screen.getPrimary().getVisualBounds().getMaxY() + 50;
    private final static Rectangle BACKGROUND = new Rectangle(0,0,BOUNDSX,BOUNDSY);

    @Override
    public void start(Stage stage) throws Exception {
        BACKGROUND.setFill(Color.DEEPSKYBLUE);
        ROOT.getChildren().add(BACKGROUND);
        ROOT.getChildren().add(new Bird());
        Scene scene = new Scene(ROOT, 0, 0);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
