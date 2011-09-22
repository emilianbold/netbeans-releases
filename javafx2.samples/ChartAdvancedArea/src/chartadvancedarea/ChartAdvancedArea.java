/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package chartadvancedarea;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 *
 * An advanced area chart with a variety of actions and settable properties
 * for experimenting with the charts features.
 *
 * @see javafx.scene.chart.AreaChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.LineChart
 * @see javafx.scene.chart.NumberAxis
 * @see javafx.scene.chart.XYChart
 */
public class ChartAdvancedArea extends Application {

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        root.getChildren().add(createChart());
    }

    protected AreaChart<Number, Number> createChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<Number,Number> ac = new AreaChart<Number,Number>(xAxis,yAxis);
        // setup chart
        ac.setTitle("Area Chart Example");
        xAxis.setLabel("X Axis");
        yAxis.setLabel("Y Axis");
        // add starting data
        for (int s=0;s<3;s++) {
            XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
            series.setName("Data Series "+s);
            double x = 0;
            while (x<95) {
                series.getData().add(new XYChart.Data<Number,Number>(x, Math.random()*99));
                x += 5 + (15*Math.random());
            }
            series.getData().add(new XYChart.Data<Number,Number>(99d, Math.random()*99));
            ac.getData().add(series);
        }
        return ac;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}
