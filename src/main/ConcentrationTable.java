package main;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class ConcentrationTable extends Application {
    private List<String> elements;
    private TableView<Analyte> table = new TableView<>();
    private final ObservableList<Analyte> data = FXCollections.observableArrayList();
    private final HBox hBox = new HBox();

    public ConcentrationTable(List<String> elements) {
        this.elements = elements;
    }

    public TableView<Analyte> getTable() {
        return table;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(new Group());
        stage.setTitle("ConcentrationTable");
        stage.setWidth(450);
        stage.setHeight(550);

        final Label label = new Label("Concentration of analytes");
        label.setFont(new Font("Arial", 20));

        table.setEditable(true);

        TableColumn analyteCol = new TableColumn("Analyte");
        analyteCol.setMinWidth(200);
        analyteCol.setCellValueFactory(
                new PropertyValueFactory<Analyte, String>("analyte"));
        analyteCol.setCellFactory(TextFieldTableCell.forTableColumn());
        analyteCol.setOnEditCommit(
                new EventHandler<CellEditEvent<Analyte, String>>() {
                    @Override
                    public void handle(CellEditEvent<Analyte, String> t) {
                        ((Analyte) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setAnalyte(t.getNewValue());
                    }
                }
        );


        TableColumn concentrationCol = new TableColumn("Concentration");
        concentrationCol.setMinWidth(200);
        concentrationCol.setCellValueFactory(
                new PropertyValueFactory<Analyte, String>("concentration"));
        concentrationCol.setCellFactory(TextFieldTableCell.forTableColumn());
        concentrationCol.setOnEditCommit(
                new EventHandler<CellEditEvent<Analyte, String>>() {
                    @Override
                    public void handle(CellEditEvent<Analyte, String> t) {
                        ((Analyte) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setConcentration(t.getNewValue());
                    }
                }
        );
        for (String element: elements) {
            data.add(new Analyte(element, ""));
        }
        table.setItems(data);
        table.getColumns().addAll(analyteCol, concentrationCol);

        final TextField addAnalyte = new TextField();
        addAnalyte.setPromptText("Analyte");
        addAnalyte.setMaxWidth(analyteCol.getMinWidth()*0.6);

        final TextField addConcentration = new TextField();
        addConcentration.setMaxWidth(concentrationCol.getMinWidth()*0.6);
        addConcentration.setPromptText("Concentration");

        final Button addButton = new Button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                data.add(new Analyte(
                        addAnalyte.getText(),
                        addConcentration.getText()));
                addAnalyte.clear();
                addConcentration.clear();
            }
        });

        final Button deleteButton = new Button("Remove");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Analyte selectedItem = table.getSelectionModel().getSelectedItem();
                table.getItems().remove(selectedItem);
            }
        });

        hBox.getChildren().addAll(addAnalyte, addConcentration, addButton, deleteButton);
        hBox.setSpacing(3);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, hBox);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }
}