package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;


public class TestGui5 extends Application {

    @Override public void start(Stage stage) {
        stage.setTitle("Line Chart Sample");
        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Number of Month");
        //creating the chart
        final LineChart<Number,Number> lineChart =
                new LineChart<>(xAxis, yAxis);

        lineChart.setTitle("Stock Monitoring, 2010");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("My portfolio");
        //populating the series with data
        series.getData().add(new XYChart.Data(1, 23));
        series.getData().add(new XYChart.Data(2, 14));
        series.getData().add(new XYChart.Data(3, 15));
        series.getData().add(new XYChart.Data(4, 24));
        series.getData().add(new XYChart.Data(5, 34));
        series.getData().add(new XYChart.Data(6, 36));
        series.getData().add(new XYChart.Data(7, 22));
        series.getData().add(new XYChart.Data(8, 45));
        series.getData().add(new XYChart.Data(9, 43));
        series.getData().add(new XYChart.Data(10, 17));
        series.getData().add(new XYChart.Data(11, 29));
        series.getData().add(new XYChart.Data(12, 25));

        ScrollPane root = new ScrollPane(lineChart);
        root.setMinSize(1000,600);
        lineChart.setMinSize(root.getMinWidth(),root.getMinHeight()-20);
        Scene scene  = new Scene(root,800,600);
        lineChart.getData().add(series);

        stage.setScene(scene);
        stage.show();
    }

    private void addDataToSeries(XYChart.Series series1) {
        //series1.getData().add(new AreaChart.Data(xSeriesData++, value));
        // remove points to keep us at no more than MAX_DATA_POINTS
//        if (series1.getData().size() > MAX_DATA_POINTS) {
//            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS);
//        }
        // update
    }

    public static void main(String[] args) {
        launch(args);
    }
}