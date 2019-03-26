/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;

import java.util.Random;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Line;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 *
 * @author luizrobertofreitas
 */
public class TestGui1 extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Scroll Me");

        Random random = new Random(12);

        NumberAxis xAxis = new NumberAxis("Time: s", 0, 4, 1);
        xAxis.setAutoRanging(false);
        NumberAxis yAxis = new NumberAxis();

        final ObservableList<Data> seriesData = FXCollections.observableArrayList();
        Series series = new Series("Rotation", seriesData);

        final ObservableList<Data> series2Data = FXCollections.observableArrayList();
        Series series2 = new Series("Speed", series2Data);


        double tempoXAxis = 0.0;

        for (int i = 0; i < 100; i++) {
            seriesData.add(new Data(tempoXAxis, random.nextInt(12)));
            series2Data.add(new Data(tempoXAxis, random.nextInt(12)));
            tempoXAxis += 0.04;
        }

        final ObservableList<Series> allSeriesData = FXCollections.observableArrayList(series, series2);

        final AreaChart chart = new AreaChart(xAxis, yAxis);

        chart.getData().addAll(allSeriesData);
        chart.setPrefSize(500, 200);

        final ScrollPane pane = new ScrollPane();
        pane.setContent(chart);
        pane.setPrefSize(600, 300);

        pane.setContent(chart);
        pane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
                chart.setMinSize(Math.max(chart.getPrefWidth(), newBounds.getWidth()), Math.max(chart.getPrefHeight(), newBounds.getHeight()));
                pane.setPannable((chart.getPrefWidth() > newBounds.getWidth()) || (chart.getPrefHeight() > newBounds.getHeight()));
            }
        });

        chart.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent ev) {
                double zoomFactor = 1.05;
                double deltaY = ev.getDeltaY();

                if (deltaY < 0) {
                    zoomFactor = 2 - zoomFactor;
                }

                System.out.println("DeltaX = " + ev.getDeltaX());
                System.out.println("DeltaY = " + ev.getDeltaY());
                System.out.println("Zoomfactor = " + zoomFactor);

                NumberAxis xAxisLocal = ((NumberAxis) chart.getXAxis());

                xAxisLocal.setUpperBound(xAxisLocal.getUpperBound() * zoomFactor);
                xAxisLocal.setLowerBound(xAxisLocal.getLowerBound() * zoomFactor);
                xAxisLocal.setTickUnit(xAxisLocal.getTickUnit() * zoomFactor);

                ev.consume();
            }
        });

        Line cursorLine = new Line();

        Scene scene = new Scene(pane);
        stage.setScene(scene);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.show();
    }
}