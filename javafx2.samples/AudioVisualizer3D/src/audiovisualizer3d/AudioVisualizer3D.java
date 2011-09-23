/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package audiovisualizer3d;

import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

/**
 * A sample that demonstrates a 3D animated audio visualizer sample. When the
 * application runs in standalone mode, the scene must be constructed with
 * the depthBuffer argument set to true, and the root node must have depthTest
 * set to true.
 *
 * @see javafx.scene.transform.Rotate
 * @see javafx.scene.paint.Color
 * @see javafx.scene.shape.RectangleBuilder
 */
public class AudioVisualizer3D extends Application implements AudioSpectrumListener {

    Xform cubeXform[];
    Cube cube[];

    private void init(Stage primaryStage) {
        Group root = new Group();
        root.setDepthTest(DepthTest.ENABLE);
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 400, 500, true));
        primaryStage.getScene().setCamera(new PerspectiveCamera());
        root.getTransforms().addAll(
            new Translate(400 / 2, 500 / 2 + 100),
            new Rotate(180, Rotate.X_AXIS)
        );
        root.getChildren().add(create3dContent());
    }

    private AudioSpectrumListener audioSpectrumListener;

    private static final String AUDIO_URI = System.getProperty("demo.audio.url","http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
    private static MediaPlayer audioMediaPlayer;
    private static final boolean PLAY_AUDIO = Boolean.parseBoolean(System.getProperty("demo.play.audio","true"));

    @Override public void spectrumDataUpdate(double timestamp, double duration,
                                             float[] magnitudes, float[] phases) {
        for (int i = 0; i < magnitudes.length; i++) {
            cube[i].s.setX((magnitudes[i] + 60.01)*0.1 + 1.0);
            cube[i].s.setY((magnitudes[i] + 60.01)*1.5 + 1.0);
            cube[i].s.setZ((magnitudes[i] + 60.01)*0.8 + 1.0);
            cubeXform[i].rx.setAngle((magnitudes[i] + 60.0)*2.0);
            cubeXform[i].ry.setAngle((magnitudes[i] + 60.0)*2.1 + 10.0);
            cubeXform[i].rz.setAngle((magnitudes[i] + 60.0)*2.1 + 10.0);
            cubeXform[i].setTranslateY((magnitudes[i] + 60.0)*-1.0);
        }
    }

    public void play() {
        this.startAudio();
    }

    @Override public void stop() {
        this.stopAudio();
    }

    public Node create3dContent() {

        Xform sceneRoot = new Xform();

        cubeXform = new Xform[128];
        cube = new Cube[128];

        int i;
        for (i = 0; i < 128; i++) {
            cubeXform[i] = new Xform();
            cubeXform[i].setTranslateX((double) 2);
            cube[i] = new Cube(1.0, Color.hsb((double) i*1.2, 1.0, 1.0, 0.3), 1.0);
            if (i == 0) {
                sceneRoot.getChildren().add(cubeXform[i]);
            }
            else if (i >= 1) {
                cubeXform[i-1].getChildren().add(cubeXform[i]);
            }
            cubeXform[i].getChildren().add(cube[i]);
        }

        audioSpectrumListener = this;
        getAudioMediaPlayer().setAudioSpectrumListener(audioSpectrumListener);
        getAudioMediaPlayer().play();
        getAudioMediaPlayer().setAudioSpectrumInterval(0.02);
        getAudioMediaPlayer().setAudioSpectrumNumBands(128);
        getAudioMediaPlayer().setCycleCount(Timeline.INDEFINITE);

        sceneRoot.setRotationAxis(Rotate.X_AXIS);
        sceneRoot.setRotate(180.0);
        sceneRoot.setTranslateY(-100.0);

        return sceneRoot;
    }

    class Xform extends Group {
        final Rotate rx = new Rotate(0, Rotate.X_AXIS);
        final Rotate ry = new Rotate(0, Rotate.Y_AXIS);
        final Rotate rz = new Rotate(0, Rotate.Z_AXIS);
        public Scale s = new Scale();
        public Xform() { 
            super(); 
            getTransforms().addAll(rz, ry, rx, s); 
        }
    }

    public class Cube extends Group {
        final Rotate rx = new Rotate(0,Rotate.X_AXIS);
        final Rotate ry = new Rotate(0,Rotate.Y_AXIS);
        final Rotate rz = new Rotate(0,Rotate.Z_AXIS);
        public Scale s = new Scale();
        public Cube(double size, Color color, double shade) {
            getTransforms().addAll(rz, ry, rx, s);
            getChildren().addAll(
                RectangleBuilder.create() // back face
                    .width(size).height(size)
                    .fill(color.deriveColor(0.0, 1.0, (1 - 0.5*shade), 1.0))
                    .translateX(-0.5*size)
                    .translateY(-0.5*size)
                    .translateZ(0.5*size)
                    .build(),
                RectangleBuilder.create() // bottom face
                    .width(size).height(size)
                    .fill(color.deriveColor(0.0, 1.0, (1 - 0.4*shade), 1.0))
                    .translateX(-0.5*size)
                    .translateY(0)
                    .rotationAxis(Rotate.X_AXIS)
                    .rotate(90)
                    .build(),
                RectangleBuilder.create() // right face
                    .width(size).height(size)
                    .fill(color.deriveColor(0.0, 1.0, (1 - 0.3*shade), 1.0))
                    .translateX(-1*size)
                    .translateY(-0.5*size)
                    .rotationAxis(Rotate.Y_AXIS)
                    .rotate(90)
                    .build(),
                RectangleBuilder.create() // left face
                    .width(size).height(size)
                    .fill(color.deriveColor(0.0, 1.0, (1 - 0.2*shade), 1.0))
                    .translateX(0)
                    .translateY(-0.5*size)
                    .rotationAxis(Rotate.Y_AXIS)
                    .rotate(90)
                    .build(),
                RectangleBuilder.create() // top face
                    .width(size).height(size)
                    .fill(color.deriveColor(0.0, 1.0, (1 - 0.1*shade), 1.0))
                    .translateX(-0.5*size)
                    .translateY(-1*size)
                    .rotationAxis(Rotate.X_AXIS)
                    .rotate(90)
                    .build(),
                RectangleBuilder.create() // top face
                    .width(size).height(size)
                    .fill(color)
                    .translateX(-0.5*size)
                    .translateY(-0.5*size)
                    .translateZ(-0.5*size)
                    .build()
            );
        }
    }

    private void startAudio() {
        if (PLAY_AUDIO) {
            getAudioMediaPlayer().setAudioSpectrumListener(audioSpectrumListener);
            getAudioMediaPlayer().play();
        }
    }

    private void stopAudio() {
        if (getAudioMediaPlayer().getAudioSpectrumListener() == audioSpectrumListener) {
            getAudioMediaPlayer().pause();
        }
    }

   private static MediaPlayer getAudioMediaPlayer() {
        if (audioMediaPlayer == null) {
            Media audioMedia = new Media(AUDIO_URI);
            audioMediaPlayer = new MediaPlayer(audioMedia);
        }
        return audioMediaPlayer;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
        play();
    }
    public static void main(String[] args) { launch(args); }
}
