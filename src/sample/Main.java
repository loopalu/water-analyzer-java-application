package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("structure.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("Water Analyzer");
        Scene scene = new Scene(root, 1400, 800);
        stage.setScene(scene);
        stage.setResizable(false);
        CategoryAxis xAxis = (CategoryAxis) scene.lookup("#xtelg");
        xAxis.setLabel("Time (s)");
        CategoryAxis yAxis = (CategoryAxis) scene.lookup("#ytelg");
        yAxis.setLabel("conducivity");
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
