/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package mouseevents;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.Stage;

/**
 * A sample that demonstrates various mouse events and their usage. Click the
 * circles, drag them across the screen, resize them with the mouse wheel. All
 * mouse events are logged to the console.
 *
 * @see javafx.scene.Cursor
 * @see javafx.scene.input.MouseEvent
 * @see javafx.event.EventHandler
 */
public class MouseEvents extends Application {
    //create a console for logging mouse events
    final ListView<String> console = new ListView<String>();
    //create a observableArrayList of logged events that will be listed in console
    final ObservableList<String> consoleObservableList = FXCollections.observableArrayList();
    {
        //set up the console
        console.setItems(consoleObservableList);
        console.setLayoutY(305);
        console.setPrefSize(500, 195);
    }
    //create a rectangle - (500px X 300px) in which our circles can move
    final Rectangle rectangle = RectangleBuilder.create()
            .width(500).height(300)
            .fill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop[] {
                new Stop(1, Color.rgb(156,216,255)),
                new Stop(0, Color.rgb(156,216,255, 0.5))
            }))
            .stroke(Color.BLACK)
            .build();
    //variables for storing initial position before drag of circle
    private double initX;
    private double initY;
    private Point2D dragAnchor;

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 500,500));
        // create circle with method listed below: paramethers: name of the circle, color of the circle, radius
        Circle circleSmall = createCircle("Blue circle", Color.DODGERBLUE, 25);
        circleSmall.setTranslateX(200);
        circleSmall.setTranslateY(80);
        // and a second, bigger circle
        Circle circleBig = createCircle("Orange circle", Color.CORAL, 40);
        circleBig.setTranslateX(300);
        circleBig.setTranslateY(150);
        // we can set mouse event to any node, also on the rectangle
        rectangle.setOnMouseMoved(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                //log mouse move to console, method listed below
                showOnConsole("Mouse moved, x: " + me.getX() + ", y: " + me.getY() );
            }
        });
        // show all the circle , rectangle and console
        root.getChildren().addAll(rectangle, circleBig, circleSmall, console);
    }

    private Circle createCircle(final String name, final Color color, int radius) {
        //create a circle with desired name,  color and radius
        final Circle circle = new Circle(radius, new RadialGradient(0, 0, 0.2, 0.3, 1, true, CycleMethod.NO_CYCLE, new Stop[] {
            new Stop(0, Color.rgb(250,250,255)),
            new Stop(1, color)
        }));
        //add a shadow effect
        circle.setEffect(new InnerShadow(7, color.darker().darker()));
        //change a cursor when it is over circle
        circle.setCursor(Cursor.HAND);
        //add a mouse listeners
        circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                showOnConsole("Clicked on" + name + ", " + me.getClickCount() + "times");
                //the event will be passed only to the circle which is on front
                me.consume();
            }
        });
        circle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                double dragX = me.getSceneX() - dragAnchor.getX();
                double dragY = me.getSceneY() - dragAnchor.getY();
                //calculate new position of the circle
                double newXPosition = initX + dragX;
                double newYPosition = initY + dragY;
                //if new position do not exceeds borders of the rectangle, translate to this position
                if ((newXPosition>=circle.getRadius()) && (newXPosition<=500-circle.getRadius())) {
                    circle.setTranslateX(newXPosition);
                }
                if ((newYPosition>=circle.getRadius()) && (newYPosition<=300-circle.getRadius())){
                    circle.setTranslateY(newYPosition);
                }
                showOnConsole(name + " was dragged (x:" + dragX + ", y:" + dragY +")");
            }
        });
        circle.setOnMouseEntered(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                //change the z-coordinate of the circle
                circle.toFront();
                showOnConsole("Mouse entered " + name);
            }
        });
        circle.setOnMouseExited(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                showOnConsole("Mouse exited " +name);
            }
        });
        circle.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                 //when mouse is pressed, store initial position
                initX = circle.getTranslateX();
                initY = circle.getTranslateY();
                dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
                showOnConsole("Mouse pressed above " + name);
            }
        });
        circle.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                showOnConsole("Mouse released above " +name);
            }
        });
        circle.impl_setOnMouseWheelRotated(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                //limit radius to <10, 60>
                double newRadius = circle.getRadius()+me.impl_getWheelRotation();
                if ((newRadius <= 60) && (newRadius >= 10)) {
                  //set new radius
                  circle.setRadius(newRadius);
                }
                showOnConsole("Mouse wheel rotated above " + name + ", with wheel rotation: " + me.impl_getWheelRotation() );
            }
        });

        return circle;
    }

    private void showOnConsole(String text) {
         //if there is 8 items in list, delete first log message, shift other logs and  add a new one to end position
         if (consoleObservableList.size()==8) {
            consoleObservableList.remove(0);
         }
         consoleObservableList.add(text);
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}
