/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package transitionrotate;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A sample in which a node rotates around its center over a given time.
 *
 * @related animation/transitions/ScaleTransition
 * @related animation/transitions/TranslateTransition
 * @related animation/transitions/FadeTransition
 * @see javafx.animation.TransitionRotate
 * @see javafx.animation.Transition
 */
public class TransitionRotate extends Application {

    private RotateTransition rotateTransition;

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 140,140));

        Rectangle rect = new Rectangle(20, 20, 100, 100);
        rect.setArcHeight(20);
        rect.setArcWidth(20);
        rect.setFill(Color.ORANGE);
        root.getChildren().add(rect);

        rotateTransition = new RotateTransition(Duration.seconds(4), rect);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(720);

        rotateTransition.setCycleCount(Timeline.INDEFINITE);
        rotateTransition.setAutoReverse(true);
    }

    public void play() {
        rotateTransition.play();
    }

    @Override public void stop() {
        rotateTransition.stop();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }
    public static void main(String[] args) { launch(args); }
}
