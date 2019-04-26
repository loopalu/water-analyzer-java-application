package proovid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class TestGui3 extends Application {

    final static String austria = "Austria";
    final static String brazil = "Brazil";
    final static String france = "France";
    final static String italy = "Italy";
    final static String usa = "USA";

    @Override
    public void start(Stage stage) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final NumberAxis yAxis2 = new NumberAxis();

        // base chart
        final LineChart<String, Number> barChart = new LineChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        xAxis.setLabel("Country");
        yAxis.setLabel("Value");
        yAxis.setStyle("-fx-side: right;");
        yAxis.setTranslateX(-30.0);

        // overlay chart
        LineChart<String, Number> lineChart = new LineChart<String, Number>(xAxis, yAxis2);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false);
        lineChart.setCreateSymbols(true);
        lineChart.setAlternativeRowFillVisible(false);
        lineChart.setAlternativeColumnFillVisible(false);
        lineChart.setHorizontalGridLinesVisible(false);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.getXAxis().setVisible(false);
        lineChart.getYAxis().setVisible(false);
        lineChart.getStylesheets().addAll(getClass().getResource("chart.css").toExternalForm());

        barChart.getData().add(createChartSeries());
        lineChart.getData().add( createChartSeries2());

        StackPane root = new StackPane();
        root.getChildren().addAll( barChart, lineChart);

        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.show();
    }

    private XYChart.Series<String,Number> createChartSeries() {

        XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
        series.getData().add(new XYChart.Data<String,Number>("1", 25601.34));
        series.getData().add(new XYChart.Data<String,Number>("2", 20148.82));
        series.getData().add(new XYChart.Data<String,Number>("3", 10000));
        series.getData().add(new XYChart.Data<String,Number>("4", 35407.15));
        series.getData().add(new XYChart.Data<String,Number>("5", 12000));

        return series;
    }

    private XYChart.Series<String,Number> createChartSeries2() {

        XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
        series.getData().add(new XYChart.Data<String,Number>("1", 232));
        series.getData().add(new XYChart.Data<String,Number>("2", 123));
        series.getData().add(new XYChart.Data<String,Number>("3", 222));
        series.getData().add(new XYChart.Data<String,Number>("4", 113));
        series.getData().add(new XYChart.Data<String,Number>("5", 321));

        return series;
    }


    public static void main(String[] args) {
        launch(args);
    }
}