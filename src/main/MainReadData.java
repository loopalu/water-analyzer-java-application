package main;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jssc.SerialPortException;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainReadData extends Application {
    private String currentTime;
    private String currentFrequency;
    private String androidFrequency;
    private String currentUser;
    private String currentMethod;
    private String currentMatrix;
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private XYChart.Series series1;
    private double xSeriesData = 0;
    private NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private ExecutorService executor;
    private AddToQueue addToQueue;
    private HashMap<Integer, String> testData = new HashMap<>();
    private int lowest = Integer.MAX_VALUE;
    private int timeMoment = 0;
    private int counter = 0; // We don't need 100 first measurements.
    private LineChart<Number,Number> lineChart;
    private Stack<String> arduinoData;
    private OutputStream outputStream;
    final ObservableList<XYChart.Data> seriesData = FXCollections.observableArrayList();
    private XYChart.Series series;
    private int upperBound = 300;
    private String currentCapillary;
    private String currentCapillaryTotal;
    private String currentCapillaryEffective;
    private ObservableList<String> currentAnalytes = FXCollections.observableArrayList();
    private ArrayList testData1 = new ArrayList();
    private List<String> elements = new ArrayList<>();
    private List<String> matrixes = new ArrayList<>();
    private List<String> methods = new ArrayList<>();
    private BGE bge;

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
        readFile();

        Parent root = FXMLLoader.load(getClass().getResource("structure.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Water Analyzer");
        Scene scene = new Scene(root, 1500, Screen.getPrimary().getVisualBounds().getHeight() * 0.9);
        stage.setScene(scene);
        stage.setResizable(false);
        makeFrequencyButtons(scene);
        makeTimeButtons(scene);
        makeMovingChart(scene);
        makeComboBoxes(scene);
        makeComboBox(scene);
        TextArea textArea = (TextArea) scene.lookup("#textArea");
        textArea.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/5);

        stage.show();

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

    private void readFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("andmed.txt"));
        String voltage = reader.readLine();
        while (voltage != null) {
            testData.put(timeMoment, voltage);
            Integer data = Integer.parseInt(voltage);
            dataQ.add(data);
            // read next line
            voltage = reader.readLine();
        }
        reader.close();
    }

    private void makeComboBox(Scene scene) {

        elements.add("Na");
        elements.add("K");
        elements.add("Li");
        elements.add("NH4");
        elements.add("Ba");
        elements.add("Mg");
        elements.add("Mn");
        elements.add("Fe2+");
        elements.add("Br");
        elements.add("Cl");
        elements.add("SO4");
        elements.add("SO3");
        elements.add("NO3");
        elements.add("NO2");
        elements.add("F");
        elements.add("PO4");
        elements.add("Thiamine");
        elements.add("Nicotinic acid");
        elements.add("Nicotinamide");
        elements.add("Pyridoxide");
        elements.add("Ascorbic acid");
        elements.add("GABA");
        elements.add("Arginine");
        elements.add("Lysine");
        elements.add("Valine");
        elements.add("Serine");
        elements.add("Glycine");
        elements.add("Phenylalanine");

        matrixes.add("soil");
        matrixes.add("sand");
        matrixes.add("rocks");
        matrixes.add("tap water");
        matrixes.add("rain water");
        matrixes.add("spring water");
        matrixes.add("aquarium water");
        matrixes.add("sea water");
        matrixes.add("salted water");
        matrixes.add("canalization water");
        matrixes.add("salvia");
        matrixes.add("blood");
        matrixes.add("urine");
        matrixes.add("plant extract");
        matrixes.add("juice");
        matrixes.add("drink");

        ComboBox comboBox1 = (ComboBox) scene.lookup("#comboBox1");
        ComboBox comboBox2 = (ComboBox) scene.lookup("#comboBox2");
        //ComboBox comboBox3 = (ComboBox) scene.lookup("#comboBox3");
        CheckComboBox<String> checkComboBox = new CheckComboBox();
        HBox elementsHBox = (HBox) scene.lookup("#elementsHBox");
        checkComboBox.getItems().addAll(elements);
        checkComboBox.setStyle("-fx-min-width: 430.0");

        checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                checkComboBox.getCheckModel().getCheckedItems();
                currentAnalytes = checkComboBox.getCheckModel().getCheckedItems();
                System.out.println(checkComboBox.getCheckModel().getCheckedItems());
            }
        });


        elementsHBox.getChildren().add(checkComboBox);

        makeComboBoxEditable(comboBox1, methods);
        makeComboBoxEditable(comboBox2, matrixes);
        //makeComboBoxEditable(comboBox3, countries);

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
                //Tried dispose method here but dint worked[![enter image description here][1]][1]
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

        ChoiceBox capillaryBox = (ChoiceBox) scene.lookup("#capillaryBox");
        capillaryBox.getItems().addAll("10", "25", "50", "75", "150", "350");
        capillaryBox.getSelectionModel().selectFirst();
        capillaryBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillary = (String) capillaryBox.getItems().get((Integer) number2);
                System.out.println(currentCapillary);
            }
        });

        ChoiceBox capillaryTotalBox = (ChoiceBox) scene.lookup("#capillaryTotalBox");
        for (int i = 4; i < 16; i++) {
            capillaryTotalBox.getItems().add((String.valueOf(i*5)));
        }
        capillaryTotalBox.getSelectionModel().selectFirst();
        capillaryTotalBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillaryTotal = (String) capillaryTotalBox.getItems().get((Integer) number2);
                System.out.println(currentCapillaryTotal);
            }
        });

        ChoiceBox capillaryEffectiveBox = (ChoiceBox) scene.lookup("#capillaryEffectiveBox");
        for (int i = 2; i < 13; i++) {
            capillaryEffectiveBox.getItems().add((String.valueOf(i*5)));
        }
        capillaryEffectiveBox.getSelectionModel().selectFirst();
        capillaryEffectiveBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentCapillaryEffective = (String) capillaryEffectiveBox.getItems().get((Integer) number2);
                System.out.println(currentCapillaryEffective);
            }
        });

        Button bgeButton = (Button) scene.lookup("#bgeButton");
        bgeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage bgeWindow = new Stage();
                bge = new BGE(currentAnalytes);
                bge.start(bgeWindow);
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
                            upperBound = 12000;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "5 min": // 3000 punkti 500ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 6000;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "3 min": // 1800 punkti 360ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 3600;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "2 min": // 1200 punkti 240ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 2400;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "1 min": // 600 punkti 120ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 1200;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "30 sec": // 300 punkti 60ste vahedega  Default start
                            oldUpperBound = upperBound;
                            upperBound = 600;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData1.size());
                            xAxis.setLowerBound(testData1.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
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
//                    try {
//                        serialPort.writeString(androidFrequency);
//                    } catch (SerialPortException e) {
//                        e.printStackTrace();
//                    }
                    System.out.println(series.getData().size());
                    System.out.println(androidFrequency);
                }
                else {
                }
            }
        });
        group.selectToggle(button4);
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

    private void makeMovingChart(Scene scene) {
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);

        series1 = new XYChart.Series<Number, Number>();

        xAxis = new NumberAxis(0, upperBound, 1);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false); // Peab olema false. Muidu muudab ise graafiku laiust.
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickUnit(upperBound/5);
        lineChart = new LineChart<Number,Number>(xAxis,yAxis); //Siis on palju kitsam graafik
        series = new XYChart.Series(seriesData);

        lineChart.getData().addAll(series);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.setVerticalGridLinesVisible(true);
        lineChart.setAnimated(false);

        GridPane.setConstraints(lineChart, 1, 0);
        GridPane mainPane = (GridPane) scene.lookup("#chartPane");
        mainPane.getChildren().add(lineChart);
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
                Logger.getLogger(MainReadData.class.getName()).log(Level.SEVERE, null, ex);
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
        //JÄRGMINE ON FAILIST LUGEMISE KOOD
        if (dataQ1.isEmpty()) {
            return;
        }
        Number measurement = dataQ1.remove();
        series.getData().add(new AreaChart.Data(xSeriesData++, measurement));
        testData1.add(measurement);

//        xAxis.setLowerBound(xAxis.getLowerBound() + 1);
//        xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        xAxis.setUpperBound(testData1.size());
        xAxis.setLowerBound(testData1.size() - upperBound);
        xAxis.setTickUnit(upperBound/5);
        if (dataQ1.isEmpty()) {
            String time = String.valueOf(System.currentTimeMillis());
            ImageSaver.saveImage(testData1);
            BufferedWriter writer;
            try {
                writer = new BufferedWriter(new FileWriter((time + ".txt")));
                writer.write("User: "+ currentUser);
                writer.newLine();
                writer.write("Method: "+ currentMethod);
                writer.newLine();
                writer.write("Matrix: "+ currentMatrix);
                writer.newLine();
                writer.write("Capillary: "+ currentCapillary);
                writer.newLine();
                writer.write("Total length of capillary: "+ currentCapillaryTotal);
                writer.newLine();
                writer.write("Effective length of capillary: "+ currentCapillaryEffective);
                writer.newLine();
                writer.write("Frequency: "+ currentFrequency);
                writer.newLine();
                writer.write("BGE:");
                writer.newLine();
                TableView<Analyte> table = bge.getTable();
                ObservableList<Analyte> observableList = table.getItems();
                for (Analyte analyte:observableList) {
                    writer.write(analyte.getAnalyte()+": "+analyte.getConcentration()+"%");
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        launch(args);
    }

    private void exportPng(final Node node, final String filePath) {

        final int w = (int) node.getLayoutBounds().getWidth();
        final int h = (int) node.getLayoutBounds().getHeight();
        final WritableImage full = new WritableImage(w, h);

        // defines the number of tiles to export (use higher value for bigger resolution)
        final int size = 1;
        final int tileWidth = w / size;
        final int tileHeight = h / size;

        System.out.println("Exporting node (building " + (size * size) + " tiles)");

        try {
            for (int row = 0; row < size; ++row) {

                final int x = row * tileWidth;
                System.out.println("1 done");
                final int y = row * tileHeight;
                System.out.println("2 done");
                final SnapshotParameters params = new SnapshotParameters();
                System.out.println("3 done");
                params.setViewport(new Rectangle2D(x, y, tileWidth, tileHeight));
                System.out.println("4 done");

                final CompletableFuture<Image> future = new CompletableFuture<>();
                System.out.println("5 done");

                // keeps fx application thread unblocked
                Platform.runLater(() -> future.complete(node.snapshot(params, null)));
                System.out.println("6 done");
                PixelWriter pixelWriter = full.getPixelWriter();
                System.out.println("7 done");
                Image image = future.get();
                System.out.println("8 done");
                PixelReader pixelReader = image.getPixelReader();
                System.out.println("9 done");
                pixelWriter.setPixels(x, y, tileWidth, tileHeight, pixelReader, 0, 0);
                System.out.println("10 done");
            }

            System.out.println("Exporting node (saving to file)");

            ImageIO.write(SwingFXUtils.fromFXImage(full, null), "png", new File(filePath));

            System.out.println("Exporting node (finished)");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
