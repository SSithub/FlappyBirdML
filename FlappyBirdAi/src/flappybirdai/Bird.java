package flappybirdai;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bird extends Circle{
    Bird(){
        this.setRadius(25);
        this.setTranslateX(Game.BOUNDSX/3);
        this.setTranslateY(Game.BOUNDSY/2);
        this.setFill(Color.ALICEBLUE);
    }
}
