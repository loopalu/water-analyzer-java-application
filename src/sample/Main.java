package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Random random = new Random();
        Parent root = FXMLLoader.load(getClass().getResource("structure.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("Water Analyzer");
        Scene scene = new Scene(root, 1400, 800);
        stage.setScene(scene);
        stage.setResizable(false);

        CategoryAxis xAxis = (CategoryAxis) scene.lookup("#xtelg");
        xAxis.setLabel("Time (s)");
        NumberAxis yAxis = (NumberAxis) scene.lookup("#ytelg");
        yAxis.setLabel("Conductivity");

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < 100; i++) {
            series.getData().add(new XYChart.Data<>(Integer.toString(i), random.nextInt(4000)+1000));
        }
        LineChart lineChart = (LineChart) scene.lookup("#linechart");
        lineChart.getData().add(series);

        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
