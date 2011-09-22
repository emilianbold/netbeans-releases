/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package keystrokemotion;

import java.util.Random;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * An example of animation generated from key events. Click the grey area to
 * give it focus and try typing letters.
 *
 * @see javafx.scene.input.KeyEvent
 * @see javafx.animation.Interpolator
 */
public class KeyStrokeMotion extends Application {
    private LettersPane lettersPane;

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 480,480));
        lettersPane = new LettersPane();
        root.getChildren().add(lettersPane);
    }

    public void play() {
        // request focus so we get key events
        Platform.runLater(new Runnable() {
            @Override public void run() {
                lettersPane.requestFocus();
            }
        });
    }

    public static class LettersPane extends Region {
        private static final Font FONT_DEFAULT = new Font(Font.getDefault().getFamily(), 200);
        private static final Random RANDOM = new Random();
        private static final Interpolator INTERPOLATOR = Interpolator.SPLINE(0.295,0.800,0.305,1.000);
        private Text pressText;

        public LettersPane() {
            setId("LettersPane");
            setPrefSize(480,480);
            setFocusTraversable(true);
            setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent me) {
                    requestFocus();
                }
            });
            setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                    createLetter(ke.getText());
                }
            });
            // create press keys text
            pressText = new Text("Press Keys");
            pressText.setTextOrigin(VPos.TOP);
            pressText.setFont(new Font(Font.getDefault().getFamily(), 40));
            pressText.setLayoutY(5);
            pressText.setFill(Color.rgb(80, 80, 80));
            DropShadow effect = new DropShadow();
            effect.setRadius(0);
            effect.setOffsetY(1);
            effect.setColor(Color.WHITE);
            pressText.setEffect(effect);
            getChildren().add(pressText);
        }

        @Override protected void layoutChildren() {
            // center press keys text
            pressText.setLayoutX((getWidth() - pressText.getLayoutBounds().getWidth()) / 2);
        }

        private void createLetter(String c) {
            final Text letter = new Text(c);
            letter.setFill(Color.BLACK);
            letter.setFont(FONT_DEFAULT);
            letter.setTextOrigin(VPos.TOP);
            letter.setTranslateX((getWidth() - letter.getBoundsInLocal().getWidth()) / 2);
            letter.setTranslateY((getHeight() - letter.getBoundsInLocal().getHeight()) / 2);
            getChildren().add(letter);
            // over 3 seconds move letter to random position and fade it out
            final Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(3), new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // we are done remove us from scene
                            getChildren().remove(letter);
                        }
                    },
                    new KeyValue(letter.translateXProperty(), getRandom(0.0f, getWidth() - letter.getBoundsInLocal().getWidth()),INTERPOLATOR),
                    new KeyValue(letter.translateYProperty(), getRandom(0.0f, getHeight() - letter.getBoundsInLocal().getHeight()),INTERPOLATOR),
                    new KeyValue(letter.opacityProperty(), 0f)
            ));
            timeline.play();
        }

        private static float getRandom(double min, double max) {
            return (float)(RANDOM.nextFloat() * (max - min) + min);
        }
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }
    public static void main(String[] args) { launch(args); }
}
