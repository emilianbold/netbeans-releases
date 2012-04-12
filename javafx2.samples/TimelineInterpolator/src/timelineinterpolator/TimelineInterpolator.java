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
package timelineinterpolator;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * A sample that shows various types of  interpolation  between key frames in a
 * timeline. There are five circles, each animated with a different
 * interpolation method.  The Linear interpolator is the default. Use the
 * controls to reduce opacity to zero for some circles to compare with others,
 * or change circle color to distinguish between individual interpolators.
 *
 * @see javafx.animation.Interpolator
 * @see javafx.animation.KeyFrame
 * @see javafx.animation.KeyValue
 * @see javafx.animation.Timeline
 * @see javafx.util.Duration
 */
public class TimelineInterpolator extends Application {
    private final Timeline timeline = new Timeline();
    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 250, 90));

        //create circles by method createMovingCircle listed below
        Circle circle1 = createMovingCircle(Interpolator.LINEAR); //default interpolator
        circle1.setOpacity(0.7);
        Circle circle2 = createMovingCircle(Interpolator.EASE_BOTH); //circle slows down when reached both ends of trajectory
        circle2.setOpacity(0.45);
        Circle circle3 = createMovingCircle(Interpolator.EASE_IN);
        Circle circle4 = createMovingCircle(Interpolator.EASE_OUT);
        Circle circle5 = createMovingCircle(Interpolator.SPLINE(0.5, 0.1, 0.1, 0.5)); //one can define own behaviour of interpolator by spline method
        
        root.getChildren().addAll(
                circle1,
                circle2,
                circle3,
                circle4,
                circle5
        );
    }

    private Circle createMovingCircle(Interpolator interpolator) {
        //create a transparent circle
        Circle circle = new Circle(45,45, 40,  Color.web("1c89f4"));
        circle.setOpacity(0);
        //add effect
        circle.setEffect(new Lighting());

        //create a timeline for moving the circle
       
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);

        //create a keyValue for horizontal translation of circle to the position 155px with given interpolator
        KeyValue keyValue = new KeyValue(circle.translateXProperty(), 155, interpolator);

        //create a keyFrame with duration 4s
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(4), keyValue);

        //add the keyframe to the timeline
        timeline.getKeyFrames().add(keyFrame);

        return circle;
    }

   public void play() {
        timeline.play();      
    }
 
    @Override public void stop() {
        timeline.stop();
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }
    public static void main(String[] args) { launch(args); }
}
