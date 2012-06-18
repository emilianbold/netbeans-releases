/*
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    /**
     * The main() method is ignored in correctly deployed JavaFX 
     * application. main() serves only as fallback in case the 
     * application can not be launched through deployment artifacts,
     * e.g., in IDEs with limited FX support. NetBeans ignores main().
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
