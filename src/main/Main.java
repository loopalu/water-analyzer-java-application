package main;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import jssc.SerialPort;
import jssc.SerialPortException;
import main.util.ArduinoReader;
import main.util.DatabaseAsker;
import main.util.ImageSaver;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private String currentTime = "";
    private String currentFrequency = "";
    private String androidFrequency = "";
    private String currentUser = "Regular user";
    private String currentMethod = "";
    private String currentCapillaryTotal = "20";
    private String currentCapillaryEffective = "10";
    private ObservableList<String> currentAnalytes = FXCollections.observableArrayList();
    private ObservableList<String> currentBge = FXCollections.observableArrayList();
    private String currentMatrix = "";
    private String currentCapillary = "10";
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private XYChart.Series series1;
    private XYChart.Series currentSeries1;
    private double xSeriesData = 0;
    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis xCurrentAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final NumberAxis yCurrentAxis = new NumberAxis();
    private ExecutorService executor;
    private AddToQueue addToQueue;
    private ArrayList testData = new ArrayList();
    private int counter = 0; // We don't need 100 first measurements.
    private LineChart<Number,Number> lineChart;
    private LineChart<Number,Number> currentLineChart;
    private Stack<String> arduinoData;
    private Scene scene;
    private SerialPort serialPort;
    private boolean isStarted = false;
    private boolean isHighVoltage = false;
    private String highVoltage = "h";
    private double upperBound = 300.0;
    private XYChart.Series series;
    private XYChart.Series currentSeries;
    private XYChart.Series series10min;
    private XYChart.Series series5min;
    private XYChart.Series series3min;
    private XYChart.Series series2min;
    private XYChart.Series series1min;
    private XYChart.Series series30sec;
    private XYChart.Series current10min;
    private XYChart.Series current5min;
    private XYChart.Series current3min;
    private XYChart.Series current2min;
    private XYChart.Series current1min;
    private XYChart.Series current30sec;
    private final ObservableList<XYChart.Data> seriesData = FXCollections.observableArrayList();
    private final ObservableList<XYChart.Data> currentData = FXCollections.observableArrayList();
    private ConcentrationTable concentrationTable;
    private ConcentrationTable elementsConcentrationTable;
    private String currentInjection = "Pressure";
    private String injectionTime = "0";
    private String currentDescription = "";
    private TextField currentField;
    private int millisecond = 0;
    private Timeline stopWatchTimeline;
    private String currentAnalyteValue = "mol";
    private String currentBgeValue = "mol";
    private String testTime = "00:00:00:000";
    private boolean isTimerOn = false;
    private int currentTimer = 60000;
    private String currentInjectionChoice = "Difference";
    private String currentInjectionChoiceUnit = "cm";
    private String injectionChoiceValue = "";
    private String currentValueString = "0";
    private ArrayList<String> analytes = new ArrayList<>(Arrays.asList("Na", "K", "Li", "NH4", "Ba", "Mg", "Mn", "Fe2+",
            "Br", "Cl", "SO4", "SO3", "NO3", "NO2", "F", "PO4", "Thiamine", "Nicotinic acid", "Nicotinamide",
            "Pyridoxide", "Ascorbic acid", "GABA", "Arginine", "Lysine", "Valine", "Serine", "Glycine", "Phenylalanine"));
    private ArrayList<String> bges = new ArrayList<>(Arrays.asList("Acetic acid 1M", "Acetic acid 2M", "Acetic acid 3M",
            "Acetic acid 6M", "Mes", "His"));
    private ArrayList<String> matrixes = new ArrayList<>(Arrays.asList("soil", "sand", "rocks", "tap water", "rain water",
            "spring water", "aquarium water", "sea water", "canalization water", "saliva", "blood", "urine", "plant extract",
            "juice", "drink"));
    private String hvValue = "";


    @Override
    public void start(Stage stage) throws Exception{
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

        //JÄRGMINE ON FAILIST LUGEMISE KOOD

        Parent root = FXMLLoader.load(getClass().getResource("style/structure.fxml"));
        root.getStylesheets().add(getClass().getResource("style/style.css").toExternalForm());

        stage.setTitle("Water Analyzer");
        scene = new Scene(root, 1500, Screen.getPrimary().getVisualBounds().getHeight()*0.9);
        stage.setScene(scene);
        stage.setResizable(false);

        getDataFromDatabase();

        makeFrequencyButtons(scene);
        makeTimeButtons(scene);
        makeStartStopButtons(scene, stage);
        makeMovingChart(scene);
        makeComboBoxes(scene);
        makeOptions(scene);
        makeTimer(scene);
        TextArea textArea = (TextArea) scene.lookup("#textArea");
        textArea.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/5);

        stage.show();
        //JÄRGMINE ON ARDUINO KOOD
        ArduinoReader reader = new ArduinoReader();
        reader.initialize();
        arduinoData = reader.getData();
        serialPort = reader.getSerialPort();
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
        addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }

    private void getDataFromDatabase() {
        DatabaseAsker databaseAsker = new DatabaseAsker();
        if (databaseAsker.isConnection()) {
            if (databaseAsker.isDatabaseUp()) {

            }
        }
    }

    private void makeTimer(Scene scene) {
        Text textField = (Text) scene.lookup("#timerData");
        textField.setText("00:00:00:000");
        stopWatchTimeline = new Timeline(new KeyFrame(Duration.millis(1), (ActionEvent event) -> {
            millisecond++;
            testTime = String.format("%02d:%02d:%02d:%03d", millisecond / 3600000 %24, millisecond / 60000 %60, millisecond/ 1000 %60, millisecond % 1000);
            textField.setText(testTime);
            if (isTimerOn && millisecond > currentTimer) {
                saveTest();
                clear(scene);
            }
        }));
        stopWatchTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("andmed.txt"));
        String voltage = reader.readLine();
        while (voltage != null) {
            testData.add(voltage);
            Integer data = Integer.parseInt(voltage);
            dataQ.add(data);
            // read next line
            voltage = reader.readLine();
        }
        reader.close();
    }

    private void makeOptions(Scene scene) {
        List<String> methods = new ArrayList<>();

        ComboBox comboBox1 = (ComboBox) scene.lookup("#comboBox1");
        ComboBox comboBox2 = (ComboBox) scene.lookup("#comboBox2");

        CheckComboBox<String> checkElementsComboBox = new CheckComboBox();
        GridPane elementsHBox = (GridPane) scene.lookup("#elementsHBox");
        checkElementsComboBox.getItems().addAll(analytes);
        checkElementsComboBox.setStyle("-fx-min-width: 192.0");
        checkElementsComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                checkElementsComboBox.getCheckModel().getCheckedItems();
                currentAnalytes = checkElementsComboBox.getCheckModel().getCheckedItems();
                System.out.println(checkElementsComboBox.getCheckModel().getCheckedItems());
            }
        });
        Button elementsButton = (Button) scene.lookup("#elementsButton");
        elementsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage elementWindow = new Stage();
                elementsConcentrationTable = new ConcentrationTable(currentAnalytes);
                elementsConcentrationTable.start(elementWindow);
            }
        });
        elementsButton.setText("Concentration");
        elementsButton.setMinWidth(145.0);
        elementsHBox.getChildren().addAll(checkElementsComboBox);

        CheckComboBox<String> checkBgeComboBox = new CheckComboBox();
        GridPane bgeHBox = (GridPane) scene.lookup("#bgeHBox");
        checkBgeComboBox.getItems().addAll(bges);
        checkBgeComboBox.setStyle("-fx-min-width: 192.0");
        checkBgeComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                checkBgeComboBox.getCheckModel().getCheckedItems();
                currentBge = checkBgeComboBox.getCheckModel().getCheckedItems();
                System.out.println(checkBgeComboBox.getCheckModel().getCheckedItems());
            }
        });
        Button bgeButton = (Button) scene.lookup("#bgeButton");
        bgeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage bgeWindow = new Stage();
                concentrationTable = new ConcentrationTable(currentBge);
                concentrationTable.start(bgeWindow);
            }
        });
        bgeButton.setText("Concentration");
        bgeButton.setMinWidth(145.0);
        bgeHBox.getChildren().addAll(checkBgeComboBox);

        makeComboBoxEditable(comboBox1, methods);
        makeComboBoxEditable(comboBox2, matrixes);

    }

    private void makeComboBoxEditable(ComboBox comboBox, List<String> dropDownList) {
        comboBox.getItems().addAll(dropDownList);

        comboBox.setEditable(true);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(comboBox.getValue());
                if (comboBox.getId().equals("comboBox1")){
                    currentMethod = (String) comboBox.getValue();
                } else if (comboBox.getId().equals("comboBox2")) {
                    currentMatrix = (String) comboBox.getValue();
                }
            }
        });

        comboBox.getEditor().setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                KeyCode kc = ke.getCode();
                if ((kc.isLetterKey())||kc.isArrowKey()||kc.equals(KeyCode.BACK_SPACE)) {
                    TextFields.bindAutoCompletion(comboBox.getEditor(), comboBox.getItems());
                }
            }
        });
        comboBox.getEditor().setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println(comboBox.getEditor().getText());
            }
        });
        comboBox.getEditor().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(comboBox.getEditor().getText());
            }
        });
    }

    private void makeComboBoxes(Scene scene) {

        ChoiceBox timerBox = (ChoiceBox) scene.lookup("#timerBox");
        for (int i = 1; i < 61; i++) {
            timerBox.getItems().add((i + " min"));
        }
        timerBox.getSelectionModel().selectFirst();
        timerBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentTimer = Integer.parseInt(((String) timerBox.getItems().get((Integer) number2)).split(" ")[0]) * 60000;
                System.out.println(currentTimer);
            }
        });

        ChoiceBox userBox = (ChoiceBox) scene.lookup("#userbox");
        userBox.getItems().addAll("Regular user", "Scientist", "Administrator");
        userBox.getSelectionModel().selectFirst();
        userBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentUser = (String) userBox.getItems().get((Integer) number2);
                System.out.println(currentUser);
            }
        });

        ChoiceBox elementsValueBox = (ChoiceBox) scene.lookup("#elementsValueBox");
        Button elementsButton = (Button) scene.lookup("#elementsButton");
        elementsValueBox.setMinHeight(elementsButton.getMinHeight());
        elementsValueBox.setPrefHeight(elementsButton.getPrefHeight());
        elementsValueBox.getItems().addAll("mol", "mmol", "μmol", "ppm", "ppb");
        elementsValueBox.getSelectionModel().selectFirst();
        elementsValueBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentAnalyteValue = (String) elementsValueBox.getItems().get((Integer) number2);
                System.out.println(currentAnalyteValue);
            }
        });

        ChoiceBox bgeValueBox = (ChoiceBox) scene.lookup("#bgeValueBox");
        bgeValueBox.getItems().addAll("mol", "mmol", "μmol", "ppm", "ppb");
        bgeValueBox.getSelectionModel().selectFirst();
        bgeValueBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentBgeValue = (String) bgeValueBox.getItems().get((Integer) number2);
                System.out.println(currentBgeValue);
            }
        });

        ChoiceBox capillaryBox = (ChoiceBox) scene.lookup("#capillaryBox");
        capillaryBox.getItems().addAll("25/150 μm", "25/350 μm", "50/150 μm", "50/350 μm", "75/175 μm", "75/350 μm", "100/175 μm", "100/350 μm");
        capillaryBox.getSelectionModel().select(2);
        capillaryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillary = (String) capillaryBox.getItems().get((Integer) number2);
                System.out.println(currentCapillary);
            }
        });

        ChoiceBox capillaryTotalBox = (ChoiceBox) scene.lookup("#capillaryTotalBox");
        for (int i = 4; i < 16; i++) {
            capillaryTotalBox.getItems().add((i * 5 + " cm"));
        }
        capillaryTotalBox.getSelectionModel().select(4);
        capillaryTotalBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillaryTotal = (String) capillaryTotalBox.getItems().get((Integer) number2);
                System.out.println(currentCapillaryTotal);
            }
        });

        ChoiceBox capillaryEffectiveBox = (ChoiceBox) scene.lookup("#capillaryEffectiveBox");
        for (int i = 2; i < 13; i++) {
            capillaryEffectiveBox.getItems().add((i * 5 + " cm"));
        }
        capillaryEffectiveBox.getSelectionModel().select(3);
        capillaryEffectiveBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillaryEffective = (String) capillaryEffectiveBox.getItems().get((Integer) number2);
                System.out.println(currentCapillaryEffective);
            }
        });

        ChoiceBox injectionBox = (ChoiceBox) scene.lookup("#injectionBox");
        injectionBox.getItems().addAll("Pressure", "Vacuum", "Electricity");
        injectionBox.getSelectionModel().selectFirst();
        Text injectionChoiceText = (Text) scene.lookup("#injectionChoiceText");
        ChoiceBox injectionChoiceUnitBox = (ChoiceBox) scene.lookup("#injectionChoiceUnitBox");
        injectionChoiceUnitBox.setMinWidth(100.0);
        injectionChoiceUnitBox.setPrefWidth(100.0);
        injectionChoiceText.setText("Difference");
        injectionChoiceUnitBox.getItems().addAll("cm", "mbar");
        injectionChoiceUnitBox.getSelectionModel().selectFirst();
        injectionBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentInjection = (String) injectionBox.getItems().get((Integer) number2);
                System.out.println("currentInjection " + currentInjection);
                injectionChoiceUnitBox.getItems().clear();
                if (currentInjection.equals("Vacuum")) {
                    injectionChoiceText.setText("Difference");
                    currentInjectionChoice = "Difference";
                    injectionChoiceUnitBox.getItems().addAll("cm", "mbar");
                } else if (currentInjection.equals("Pressure")) {
                    injectionChoiceText.setText("Difference");
                    currentInjectionChoice = "Difference";
                    injectionChoiceUnitBox.getItems().addAll("cm", "mbar");
                } else {
                    injectionChoiceText.setText("Voltage");
                    currentInjectionChoice = "Voltage";
                    injectionChoiceUnitBox.getItems().addAll("kV");
                }
                injectionChoiceUnitBox.getSelectionModel().selectFirst();
            }
        });
        injectionChoiceUnitBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number number, Number number2) {
                currentInjectionChoiceUnit = (String) injectionChoiceUnitBox.getItems().get((Integer) number2);
                System.out.println(currentInjectionChoiceUnit);
            }
        });

        TextField durationField = (TextField) scene.lookup("#durationField");
        durationField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                injectionTime = durationField.getText();
                System.out.println(injectionTime);
            }
        });

        durationField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                injectionTime = durationField.getText();
                System.out.println(injectionTime);
            }
        });

        TextField injectionChoiceValueField = (TextField) scene.lookup("#injectionChoiceValue");
        injectionChoiceValueField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                injectionChoiceValue = injectionChoiceValueField.getText();
                System.out.println(injectionChoiceValue);
            }
        });

        injectionChoiceValueField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                injectionChoiceValue = injectionChoiceValueField.getText();
                System.out.println(injectionChoiceValue);
            }
        });

        TextArea commentaryField = (TextArea) scene.lookup("#textArea");
        commentaryField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                currentDescription = commentaryField.getText();
                System.out.println(currentDescription);
            }
        });

        commentaryField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                currentDescription = commentaryField.getText();
                System.out.println(currentDescription);
            }
        });
    }

    private void makeTimeButtons(Scene scene) {
        final ToggleButton button1 = new ToggleButton("10 min");
        final ToggleButton button2 = new ToggleButton("5 min");
        final ToggleButton button3 = new ToggleButton("3 min");
        final ToggleButton button4 = new ToggleButton("2 min");
        final ToggleButton button5 = new ToggleButton("1 min");
        final ToggleButton button6 = new ToggleButton("30 sec");
        HBox box1 = new HBox(button1);
        box1.setId("hbox");
        HBox box2 = new HBox(button2);
        box2.setId("hbox");
        HBox box3 = new HBox(button3);
        box3.setId("hbox");
        HBox box4 = new HBox(button4);
        box4.setId("hbox");
        HBox box5 = new HBox(button5);
        box5.setId("hbox");
        HBox box6 = new HBox(button6);
        box6.setId("hbox");

        ToggleGroup group = new ToggleGroup();
        button1.setToggleGroup(group);
        button2.setToggleGroup(group);
        button3.setToggleGroup(group);
        button4.setToggleGroup(group);
        button5.setToggleGroup(group);
        button6.setToggleGroup(group);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle) {
                if(selectedToggle!=null) {
                    currentTime = ((ToggleButton) selectedToggle).getText();
                    double oldUpperBound = upperBound;
                    switch (currentTime) {
                        case "10 min": // 6000 punkti 1200ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 6000;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series10min);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current10min);

                            break;
                        case "5 min": // 3000 punkti 500ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 3000;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series5min);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current5min);

                            break;
                        case "3 min": // 1800 punkti 360ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 1800;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series3min);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current3min);

                            break;
                        case "2 min": // 1200 punkti 240ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 1200;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series2min);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current2min);

                            break;
                        case "1 min": // 600 punkti 120ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 600;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series1min);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current1min);

                            break;
                        case "30 sec": // 300 punkti 60ste vahedega  Default start
                            oldUpperBound = upperBound;
                            upperBound = 300;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound((int)(testData.size()/2.0));
                            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xAxis.setTickUnit(upperBound/5);
                            lineChart.getData().clear();
                            lineChart.getData().addAll(series30sec);

                            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
                            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
                            xCurrentAxis.setTickUnit(upperBound/5);
                            currentLineChart.getData().clear();
                            currentLineChart.getData().addAll(current30sec);

                            break;
                    }
                }
                else {
                    //label.setText("...");
                }
            }
        });
        // select the first button to start with
        group.selectToggle(button6);
        // add buttons and label to grid and set their positions
        GridPane.setConstraints(box1,0,8);
        GridPane.setConstraints(box2,1,8);
        GridPane.setConstraints(box3,2,8);
        GridPane.setConstraints(box4,3,8);
        GridPane.setConstraints(box5,4,8);
        GridPane.setConstraints(box6,0,9);

        GridPane grid = (GridPane) scene.lookup("#testSettings");

        grid.getChildren().addAll(box1, box2, box3, box4, box5, box6);
    }

    private void makeFrequencyButtons(Scene scene) {
        final ToggleButton button1 = new ToggleButton("2 MHz"); //2 MHz
        final ToggleButton button2 = new ToggleButton("1.6 MHz"); //1.6 MHz
        final ToggleButton button3 = new ToggleButton("1.3 MHz"); //1.3 MHz
        final ToggleButton button4 = new ToggleButton("1 MHz"); //1 MHz
        final ToggleButton button5 = new ToggleButton("880 kHz"); //880 kHz
        final ToggleButton button6 = new ToggleButton("800 kHz"); //800 kHz
        final ToggleButton button7 = new ToggleButton("660 kHz"); //660 kHz
        final ToggleButton button8 = new ToggleButton("500 kHz"); //500 kHz
        final ToggleButton button9 = new ToggleButton("400 kHz"); // 400 kHz
        final ToggleButton button10 = new ToggleButton("300 kHz"); // 300 kHz
        HBox box1 = new HBox(button1);
        box1.setId("hbox");
        HBox box2 = new HBox(button2);
        box2.setId("hbox");
        HBox box3 = new HBox(button3);
        box3.setId("hbox");
        HBox box4 = new HBox(button4);
        box4.setId("hbox");
        HBox box5 = new HBox(button5);
        box5.setId("hbox");
        HBox box6 = new HBox(button6);
        box6.setId("hbox");
        HBox box7 = new HBox(button7);
        box7.setId("hbox");
        HBox box8 = new HBox(button8);
        box8.setId("hbox");
        HBox box9 = new HBox(button9);
        box9.setId("hbox");
        HBox box10 = new HBox(button10);
        box10.setId("hbox");

        ToggleGroup group = new ToggleGroup();
        button1.setToggleGroup(group);
        button2.setToggleGroup(group);
        button3.setToggleGroup(group);
        button4.setToggleGroup(group);
        button5.setToggleGroup(group);
        button6.setToggleGroup(group);
        button7.setToggleGroup(group);
        button8.setToggleGroup(group);
        button9.setToggleGroup(group);
        button10.setToggleGroup(group);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle) {
                if(selectedToggle!=null) {
                    currentFrequency = ((ToggleButton) selectedToggle).getText();
                    switch (currentFrequency) {
                        case "2 MHz":
                            androidFrequency = "G9\n";
                            break;
                        case "1.6 MHz":
                            androidFrequency = "G7\n";
                            break;
                        case "1.3 MHz":
                            androidFrequency = "G8\n";
                            break;
                        case "1 MHz":
                            androidFrequency = "G0\n";
                            break;
                        case "880 kHz":
                            androidFrequency = "G1\n";
                            break;
                        case "800 kHz":
                            androidFrequency = "G2\n";
                            break;
                        case "660 kHz":
                            androidFrequency = "G3\n";
                            break;
                        case "500 kHz":
                            androidFrequency = "G4\n";
                            break;
                        case "400 kHz":
                            androidFrequency = "G5\n";
                            break;
                        case "300 kHz":
                            androidFrequency = "G6\n";
                            break;
                    }
                    //JÄRGMINE ON ARDUINO KOOD
                    try {
                        serialPort.writeString(androidFrequency);
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                    System.out.println(series.getData().size());
                    System.out.println(androidFrequency);
                }
                else {
                }
            }
        });
        group.selectToggle(button1);
        // select the first button to start with
        // add buttons and label to grid and set their positions
        GridPane.setConstraints(box1,0,2);
        GridPane.setConstraints(box2,1,2);
        GridPane.setConstraints(box3,2,2);
        GridPane.setConstraints(box4,3,2);
        GridPane.setConstraints(box5,4,2);
        GridPane.setConstraints(box6,0,3);
        GridPane.setConstraints(box7,1,3);
        GridPane.setConstraints(box8,2,3);
        GridPane.setConstraints(box9,3,3);
        GridPane.setConstraints(box10,4,3);

        GridPane grid = (GridPane) scene.lookup("#testSettings");
        grid.getChildren().addAll(box1, box2, box3, box4, box5, box6, box7, box8, box9, box10);
    }

    private void makeStartStopButtons(Scene scene, Stage stage) {
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        Button clearButton = new Button("Clear");
        Button saveButton = new Button("Save");
        startButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("start");
                isStarted = true;
                stopWatchTimeline.play();
                try {
                    serialPort.writeString("G9\n");
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });
        stopButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("stop");
                isStarted = false;
                stopWatchTimeline.pause();
            }
        });
        clearButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                clear(scene);
            }
        });


        saveButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("save");
                saveTest();
            }
        });


        Button onOff = (Button) scene.lookup("#onOff");
        onOff.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isHighVoltage) {
                    turnHighVoltageOff(onOff);
                } else {
                    turnHighVoltageOn(onOff);
                }
            }
        });

        Button sendPercentageButton = (Button) scene.lookup("#sendPercentageButton");
        sendPercentageButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("send");
                TextField field = (TextField) scene.lookup("#percentageField");
                field.setOnMouseClicked(event1 -> {
                    field.setStyle("-fx-text-inner-color: black;");
                    field.clear();
                });
                String fieldData = field.getText();
                try {
                    int out = Integer.parseInt(fieldData);
                    if (out < 0 || out > 100) {
                        field.setStyle("-fx-text-inner-color: red;");
                        field.setText("ERROR");
                    } else {
                        hvValue = "v" + (int) (127 - out * 1.27)+"\n";
                        //Arduino kood
                        try {
                            serialPort.writeString(hvValue);
                            //outputStream.write(outData.getBytes());
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                        System.out.println(hvValue);
                    }
                } catch (NumberFormatException ex) {
                    field.setStyle("-fx-text-inner-color: red;");
                    field.setText("ERROR");
                }
            }
        });

        Button timerButton = (Button) scene.lookup("#timerButton");
        timerButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isTimerOn) {
                    isTimerOn = false;
                    timerButton.setStyle("-fx-background-color: red;");
                    timerButton.setText("TIMER OFF");
                } else {
                    isTimerOn = true;
                    timerButton.setStyle("-fx-background-color: lawngreen;");
                    timerButton.setText("TIMER ON");
                }
            }
        });

        HBox box1 = new HBox(startButton);
        box1.setId("hbox");
        HBox box2 = new HBox(stopButton);
        box2.setId("hbox");
        HBox box3 = new HBox(clearButton);
        box3.setId("hbox");
        HBox box4 = new HBox(saveButton);
        box4.setId("hbox");
        GridPane.setConstraints(box1,0,13);
        GridPane.setConstraints(box2,1,13);
        GridPane.setConstraints(box3,2,13);
        GridPane.setConstraints(box4,3,13);
        GridPane grid = (GridPane) scene.lookup("#testSettings");
        grid.getChildren().addAll(box1, box2, box3, box4);
    }

    private void turnHighVoltageOn(Button onOff) {
        isHighVoltage = true;
        onOff.setStyle("-fx-background-color: lawngreen;");
        onOff.setText("ON");
        highVoltage = "H\n";
        //Arduino kood
        try {
            serialPort.writeString(highVoltage);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private void turnHighVoltageOff(Button onOff) {
        isHighVoltage = false;
        onOff.setStyle("-fx-background-color: red;");
        onOff.setText("OFF");
        highVoltage = "h\n";
        //Arduino kood
        try {
            serialPort.writeString(highVoltage);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    private void clear(Scene scene) {
        System.out.println("clear");
        testData = new ArrayList();
        series10min.getData().clear();
        series5min.getData().clear();
        series3min.getData().clear();
        series2min.getData().clear();
        series1min.getData().clear();
        series30sec.getData().clear();
        current10min.getData().clear();
        current5min.getData().clear();
        current3min.getData().clear();
        current2min.getData().clear();
        current1min.getData().clear();
        current30sec.getData().clear();
        xSeriesData = 0;
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(upperBound);
        xCurrentAxis.setLowerBound(0);
        xCurrentAxis.setUpperBound(upperBound);
        millisecond = 0;
        testTime = "00:00:00:000";
        stopWatchTimeline.stop();
        Text timerText = (Text) scene.lookup("#timerData");
        timerText.setText("00:00:00:000");
    }

    private void saveTest() {
        isTimerOn = false;
        isStarted = false;
        currentTimer = 60000;
        stopWatchTimeline.stop();
        Text textField = (Text) scene.lookup("#timerData");
        textField.setText("00:00:00:000");
        Button timerButton = (Button) scene.lookup("#timerButton");
        timerButton.setStyle("-fx-background-color: red;");
        timerButton.setText("TIMER OFF");
        ChoiceBox timerBox = (ChoiceBox) scene.lookup("#timerBox");
        timerBox.getSelectionModel().selectFirst();
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MMMM_yyyy_HH_mm");
        Date resultdate = new Date(time);
        String timeStamp = sdf.format(resultdate);
        String current;
        try {
            current = new File( "." ).getCanonicalPath();
            File newDirectory = new File(current + "/" + timeStamp);
            boolean isCreated = newDirectory.mkdirs();
            if (isCreated) {
                System.out.printf("1. Successfully created directories, path:%s",
                        newDirectory.getCanonicalPath());
            } else if (newDirectory.exists()) {
                System.out.printf("1. Directory path already exist, path:%s",
                        newDirectory.getCanonicalPath());
            } else {
                System.out.println("1. Unable to create directory");
                return;
            }

            BufferedWriter writer;
            PrintWriter dataWriter;
            dataWriter = new PrintWriter(current+"/" + timeStamp + File.separator + timeStamp + "_data.txt");
            for (int i = 0; i < testData.size(); i++) {
                dataWriter.println(testData.get(i));
            }
            System.out.println(testData.size());
            System.out.println("done");
            dataWriter.close();
            ImageSaver.saveImage(testData, current+"/" + timeStamp + File.separator + timeStamp + "_image.png");

            if (concentrationTable != null) {
                writer = new BufferedWriter(new FileWriter((current+"/" + timeStamp + File.separator + timeStamp + "_settings.txt")));
                writer.write("User: "+ currentUser);
                writer.newLine();
                writer.write("Method: "+ currentMethod);
                writer.newLine();
                writer.write("Matrix: "+ currentMatrix);
                writer.newLine();
                writer.write("Capillary ID/OD: "+ currentCapillary);
                writer.newLine();
                writer.write("Total length of capillary: "+ currentCapillaryTotal);
                writer.newLine();
                writer.write("Effective length of capillary: "+ currentCapillaryEffective);
                writer.newLine();
                writer.write("Frequency: "+ currentFrequency);
                writer.newLine();
                writer.write("Injection method: "+ currentInjection + " " + currentInjectionChoice + ": " + injectionChoiceValue + " " + currentInjectionChoiceUnit + " Injection time: " + injectionTime + " s");
                writer.newLine();
                writer.write("Current: "+ currentValueString + " µA");
                writer.newLine();
                writer.write("HV value: " + hvValue + " %");
                writer.newLine();
                writer.write("Analytes:");
                writer.newLine();
                TableView<Analyte> analytesTable = elementsConcentrationTable.getTable();
                ObservableList<Analyte> observableAnalytesList = analytesTable.getItems();
                for (Analyte analyte:observableAnalytesList) {
                    writer.write(analyte.getAnalyte()+": "+analyte.getConcentration()+" " + currentAnalyteValue);
                    writer.newLine();
                }
                writer.write("BGE:");
                writer.newLine();
                TableView<Analyte> bgeTable = concentrationTable.getTable();
                ObservableList<Analyte> observableBgeList = bgeTable.getItems();
                for (Analyte analyte:observableBgeList) {
                    writer.write(analyte.getAnalyte()+": "+analyte.getConcentration()+" " + currentBgeValue);
                    writer.newLine();
                }
                writer.write("Commentary:");
                writer.newLine();
                writer.write(currentDescription);
                writer.newLine();
                writer.write("Test duration: " + testTime);
                writer.newLine();
                writer.close();
            }
            testTime = "00:00:00:000";
            Button onOff = (Button) scene.lookup("#onOff");
            turnHighVoltageOff(onOff);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeMovingChart(Scene scene) {
        NumberAxis yAxis = new NumberAxis();
        NumberAxis yCurrentAxis = new NumberAxis();

        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);

        yCurrentAxis.setAutoRanging(true);
        yCurrentAxis.setForceZeroInRange(false);

        series1 = new XYChart.Series<Number, Number>();
        currentSeries1 = new XYChart.Series<Number, Number>();

        xAxis = new NumberAxis(0, upperBound, 1);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false); // Peab olema false. Muidu muudab ise graafiku laiust.
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickUnit(upperBound/5);
        lineChart = new LineChart<>(xAxis, yAxis); //Siis on palju kitsam graafik
        series = new XYChart.Series(seriesData);

        xCurrentAxis = new NumberAxis(0, upperBound, 1);
        xCurrentAxis.setForceZeroInRange(false);
        xCurrentAxis.setAutoRanging(false); // Peab olema false. Muidu muudab ise graafiku laiust.
        xCurrentAxis.setTickLabelsVisible(true);
        xCurrentAxis.setTickMarkVisible(true);
        xCurrentAxis.setMinorTickVisible(false);
        xCurrentAxis.setTickUnit(upperBound/5);
        currentLineChart = new LineChart<>(xCurrentAxis, yCurrentAxis); //Siis on palju kitsam graafik
        currentSeries = new XYChart.Series(currentData);

        ObservableList<XYChart.Data> seriesData10min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> seriesData5min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> seriesData3min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> seriesData2min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> seriesData1min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> seriesData30sec = FXCollections.observableArrayList();
        series10min = new XYChart.Series(seriesData10min);
        series5min = new XYChart.Series(seriesData5min);
        series3min = new XYChart.Series(seriesData3min);
        series2min = new XYChart.Series(seriesData2min);
        series1min = new XYChart.Series(seriesData1min);
        series30sec = new XYChart.Series(seriesData30sec);

        ObservableList<XYChart.Data> currentData10min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> currentData5min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> currentData3min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> currentData2min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> currentData1min = FXCollections.observableArrayList();
        ObservableList<XYChart.Data> currentData30sec = FXCollections.observableArrayList();
        current10min = new XYChart.Series(currentData10min);
        current5min = new XYChart.Series(currentData5min);
        current3min = new XYChart.Series(currentData3min);
        current2min = new XYChart.Series(currentData2min);
        current1min = new XYChart.Series(currentData1min);
        current30sec = new XYChart.Series(currentData30sec);

        lineChart.getData().addAll(series30sec);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.setVerticalGridLinesVisible(true);
        lineChart.setAnimated(false);

        currentLineChart.getData().addAll(current30sec);
        currentLineChart.setCreateSymbols(false);
        currentLineChart.setLegendVisible(false);
        currentLineChart.setHorizontalGridLinesVisible(true);
        currentLineChart.setVerticalGridLinesVisible(true);
        currentLineChart.setAnimated(false);

        GridPane.setConstraints(currentLineChart, 1, 1); //column ja row vaja muuta !!!!!!!!!! @@@@@@@@@@@@@@@@@@
        GridPane.setConstraints(lineChart, 1, 0);
        GridPane mainPane = (GridPane) scene.lookup("#chartPane");
        mainPane.getChildren().addAll(lineChart, currentLineChart);

    }

    private class AddToQueue implements Runnable {
        @Override
        public void run() {
            try {
                // add a item of random data to queue
                //dataQ1.add(Math.random());
                dataQ1.add(dataQ.remove());

                Thread.sleep(1);
                executor.execute(this);

            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //-- Timeline gets called in the JavaFX Main thread
    private void prepareTimeline() {
        // Every frame to take any data from queue and add to chart
        new AnimationTimer() {
            @Override public void handle(long now) {
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        //JÄRGMINE ON ARDUINO KOOD
        if (arduinoData.isEmpty()) {
            return;
        }
        String androidData = arduinoData.pop();
        Number measurement = Integer.parseInt(androidData.split(" ")[1]);
        long current = Long.parseLong(androidData.split(" ")[2]);
        if (current > 2147483647L) {
            current = current - 4294967295L;
        }
        currentField = (TextField) scene.lookup("#currentAmper");
        long currentValue = Math.round(current/256.0);
        currentValueString = String.valueOf(currentValue);
        currentField.setText(currentValueString);
        double voltagePercent = Double.parseDouble(androidData.split(" ")[4]);
        TextField percentageField = (TextField) scene.lookup("#voltagePercentBox");
        percentageField.setText(String.valueOf(Math.round((127.0-voltagePercent)/1.27)));
        //counter += 1;
        TextField textField = (TextField) scene.lookup("#androidData");
        if (androidData.length() - androidData.replace("C", "").length() == 1) {
            textField.setText(androidData);
        }
        if (isStarted) {
            xSeriesData += 0.5;

            series10min.getData().add(new AreaChart.Data(xSeriesData, measurement));
            series5min.getData().add(new AreaChart.Data(xSeriesData, measurement));
            series3min.getData().add(new AreaChart.Data(xSeriesData, measurement));
            series2min.getData().add(new AreaChart.Data(xSeriesData, measurement));
            series1min.getData().add(new AreaChart.Data(xSeriesData, measurement));
            series30sec.getData().add(new AreaChart.Data(xSeriesData, measurement));

            current30sec.getData().add(new AreaChart.Data(xSeriesData, currentValue));
            current10min.getData().add(new AreaChart.Data(xSeriesData, currentValue));
            current5min.getData().add(new AreaChart.Data(xSeriesData, currentValue));
            current3min.getData().add(new AreaChart.Data(xSeriesData, currentValue));
            current2min.getData().add(new AreaChart.Data(xSeriesData, currentValue));
            current1min.getData().add(new AreaChart.Data(xSeriesData, currentValue));

            if (series10min.getData().size() > 12000) {
                series10min.getData().remove(0);
                current10min.getData().remove(0);
            }
            if (series5min.getData().size() > 6000) {
                series5min.getData().remove(0);
                current5min.getData().remove(0);
            }
            if (series3min.getData().size() > 3600) {
                series3min.getData().remove(0);
                current3min.getData().remove(0);
            }
            if (series2min.getData().size() > 2400) {
                series2min.getData().remove(0);
                current2min.getData().remove(0);
            }
            if (series1min.getData().size() > 1200) {
                series1min.getData().remove(0);
                current1min.getData().remove(0);
            }
            if (series30sec.getData().size() > 600) {
                series30sec.getData().remove(0);
                current30sec.getData().remove(0);
            }

            series.getData().add(new AreaChart.Data(xSeriesData, measurement));
            testData.add(measurement);

            currentSeries.getData().add(new AreaChart.Data(xSeriesData, currentValue));

            xAxis.setUpperBound((int)(testData.size()/2.0));
            xAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
            xAxis.setTickUnit(upperBound/5);

            xCurrentAxis.setUpperBound((int)(testData.size()/2.0));
            xCurrentAxis.setLowerBound((int)(testData.size()/2.0 - upperBound));
            xCurrentAxis.setTickUnit(upperBound/5);
        }
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        launch(args);
    }

}
