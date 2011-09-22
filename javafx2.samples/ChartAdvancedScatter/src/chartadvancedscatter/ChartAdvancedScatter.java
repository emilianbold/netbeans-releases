/**
 * Copyright (c) 2008, 2011 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
package chartadvancedscatter;

import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * An advanced scatter chart with a variety of controls.
 * 
 * @see javafx.scene.chart.ScatterChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.Axis
 * @see javafx.scene.chart.NumberAxis
 */
public class ChartAdvancedScatter extends Application {

    private void init(Stage primaryStage) {
        Group root = new Group();
        primaryStage.setScene(new Scene(root));
        root.getChildren().add(createChart());
    }

    protected ScatterChart<Number, Number> createChart() {
        final NumberAxis xAxis = new NumberAxis();
        xAxis.setSide(Side.TOP);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setSide(Side.RIGHT);
        final ScatterChart<Number,Number> sc = new ScatterChart<Number,Number>(xAxis,yAxis);
        // setup chart
        xAxis.setLabel("X Axis");
        yAxis.setLabel("Y Axis");
        // add starting data
        for (int s=0;s<5;s++) {
            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            series.setName("Data Series "+s);
            for (int i=0; i<30; i++) series.getData().add(new XYChart.Data<Number, Number>(Math.random()*98, Math.random()*98));
            sc.getData().add(series);
        }
        return sc;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }
    public static void main(String[] args) { launch(args); }
}
