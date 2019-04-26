package main;

//import gnu.io.SerialPort;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
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
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortException;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private String currentTime;
    private String currentFrequency;
    private String androidFrequency;
    private String currentUser;
    private String currentMethod;
    private String currentCapillaryTotal;
    private String currentCapillaryEffective;
    private ObservableList<String> currentAnalytes = FXCollections.observableArrayList();
    private String currentMatrix;
    private String currentBGE;
    private String currentCapillary;
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private XYChart.Series series1;
    private double xSeriesData = 0;
    private NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private ExecutorService executor;
    private AddToQueue addToQueue;
    private ArrayList testData = new ArrayList();
    private int counter = 0; // We don't need 100 first measurements.
    private LineChart<Number,Number> lineChart;
    private Stack<String> arduinoData;
    private Scene scene;
    private SerialPort serialPort;
    private boolean isStarted = false;
    private boolean isHighVoltage = false;
    private String highVoltage = "h";
    private double upperBound = 600.0;
    private XYChart.Series series;
    final ObservableList<XYChart.Data> seriesData = FXCollections.observableArrayList();


    @Override
    public void start(Stage stage) throws Exception{
        //JÄRGMINE ON FAILIST LUGEMISE KOOD

        Parent root = FXMLLoader.load(getClass().getResource("structure.fxml"));
        root.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Water Analyzer");
        scene = new Scene(root, 1500, Screen.getPrimary().getVisualBounds().getHeight()*0.9);
        stage.setScene(scene);
        stage.setResizable(false);
        makeFrequencyButtons(scene);
        makeTimeButtons(scene);
        makeStartStopButtons(scene, stage);
        makeMovingChart(scene);
        makeComboBoxes(scene);
        makeComboBox(scene);
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

    private void makeComboBox(Scene scene) {
        List<String> countries = new ArrayList<>();
        List<String> elements = new ArrayList<>();
        List<String> matrixes = new ArrayList<>();

        countries.add("Afghanistan");
        countries.add("Albania");
        countries.add("Algeria");
        countries.add("Andorra");
        countries.add("Angola");
        countries.add("Antigua and Barbuda");
        countries.add("Argentina");
        countries.add("Armenia");
        countries.add("Australia");
        countries.add("Austria");
        countries.add("Azerbaijan");
        countries.add("Bahamas");
        countries.add("Bahrain");
        countries.add("Bangladesh");
        countries.add("Barbados");
        countries.add("Belarus");
        countries.add("Belgium");
        countries.add("Belize");
        countries.add("Benin");
        countries.add("Bhutan");
        countries.add("Bolivia");
        countries.add("Bosnia and Herzegovina");
        countries.add("Botswana");
        countries.add("Brazil");
        countries.add("Brunei");
        countries.add("Bulgaria");
        countries.add("Burkina Faso");
        countries.add("Burundi");
        countries.add("Cabo Verde");
        countries.add("Cambodia");
        countries.add("Cameroon");
        countries.add("Canada");
        countries.add("Central African Republic (CAR)");
        countries.add("Chad");
        countries.add("Chile");
        countries.add("China");
        countries.add("Colombia");
        countries.add("Comoros");
        countries.add("Democratic Republic of the Congo");
        countries.add("Republic of the Congo");
        countries.add("Costa Rica");
        countries.add("Cote d'Ivoire");
        countries.add("Croatia");
        countries.add("Cuba");
        countries.add("Cyprus");
        countries.add("Czech Republic");
        countries.add("Denmark");
        countries.add("Djibouti");
        countries.add("Dominica");
        countries.add("Dominican Republic");
        countries.add("Ecuador");
        countries.add("Egypt");
        countries.add("El Salvador");
        countries.add("Equatorial Guinea");
        countries.add("Eritrea");
        countries.add("Estonia");
        countries.add("Ethiopia");
        countries.add("Fiji");
        countries.add("Finland");
        countries.add("France");
        countries.add("Gabon");
        countries.add("Gambia");
        countries.add("Georgia");
        countries.add("Germany");
        countries.add("Ghana");
        countries.add("Greece");
        countries.add("Grenada");
        countries.add("Guatemala");
        countries.add("Guinea");
        countries.add("Guinea-Bissau");
        countries.add("Guyana");
        countries.add("Haiti");
        countries.add("Honduras");
        countries.add("Hungary");
        countries.add("Iceland");
        countries.add("India");
        countries.add("Indonesia");
        countries.add("Iran");
        countries.add("Iraq");
        countries.add("Ireland");
        countries.add("Israel");
        countries.add("Italy");
        countries.add("Jamaica");
        countries.add("Japan");
        countries.add("Jordan");
        countries.add("Kazakhstan");
        countries.add("Kenya");
        countries.add("Kiribati");
        countries.add("Kosovo");
        countries.add("Kuwait");
        countries.add("Kyrgyzstan");
        countries.add("Laos");
        countries.add("Latvia");
        countries.add("Lebanon");
        countries.add("Lesotho");
        countries.add("Liberia");
        countries.add("Libya");
        countries.add("Liechtenstein");
        countries.add("Lithuania");
        countries.add("Luxembourg");
        countries.add("Macedonia (FYROM)");
        countries.add("Madagascar");
        countries.add("Malawi");
        countries.add("Malaysia");
        countries.add("Maldives");
        countries.add("Mali");
        countries.add("Malta");
        countries.add("Marshall Islands");
        countries.add("Mauritania");
        countries.add("Mauritius");
        countries.add("Mexico");
        countries.add("Micronesia");
        countries.add("Moldova");
        countries.add("Monaco");
        countries.add("Mongolia");
        countries.add("Montenegro");
        countries.add("Morocco");
        countries.add("Mozambique");
        countries.add("Myanmar (Burma)");
        countries.add("Namibia");
        countries.add("Nauru");
        countries.add("Nepal");
        countries.add("Netherlands");
        countries.add("New Zealand");
        countries.add("Nicaragua");
        countries.add("Niger");
        countries.add("Nigeria");
        countries.add("North Korea");
        countries.add("Norway");
        countries.add("Oman");
        countries.add("Pakistan");
        countries.add("Palau");
        countries.add("Palestine");
        countries.add("Panama");
        countries.add("Papua New Guinea");
        countries.add("Paraguay");
        countries.add("Peru");
        countries.add("Philippines");
        countries.add("Poland");
        countries.add("Portugal");
        countries.add("Qatar");
        countries.add("Romania");
        countries.add("Russia");
        countries.add("Rwanda");
        countries.add("Saint Kitts and Nevis");
        countries.add("Saint Lucia");
        countries.add("Saint Vincent and the Grenadines");
        countries.add("Samoa");
        countries.add("San Marino");
        countries.add("Sao Tome and Principe");
        countries.add("Saudi Arabia");
        countries.add("Senegal");
        countries.add("Serbia");
        countries.add("Seychelles");
        countries.add("Sierra Leone");
        countries.add("Singapore");
        countries.add("Slovakia");
        countries.add("Slovenia");
        countries.add("Solomon Islands");
        countries.add("Somalia");
        countries.add("South Africa");
        countries.add("South Korea");
        countries.add("South Sudan");
        countries.add("Spain");
        countries.add("Sri Lanka");
        countries.add("Sudan");
        countries.add("Suriname");
        countries.add("Swaziland");
        countries.add("Sweden");
        countries.add("Switzerland");
        countries.add("Syria");
        countries.add("Taiwan");
        countries.add("Tajikistan");
        countries.add("Tanzania");
        countries.add("Thailand");
        countries.add("Timor-Leste");
        countries.add("Togo");
        countries.add("Tonga");
        countries.add("Trinidad and Tobago");
        countries.add("Tunisia");
        countries.add("Turkey");
        countries.add("Turkmenistan");
        countries.add("Tuvalu");
        countries.add("Uganda");
        countries.add("Ukraine");
        countries.add("United Arab Emirates (UAE)");
        countries.add("United Kingdom (UK)");
        countries.add("United States of America (USA)");
        countries.add("Uruguay");
        countries.add("Uzbekistan");
        countries.add("Vanuatu");
        countries.add("Vatican City (Holy See)");
        countries.add("Venezuela");
        countries.add("Vietnam");
        countries.add("Yemen");
        countries.add("Zambia");
        countries.add("Zimbabwe");

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
        ComboBox comboBox3 = (ComboBox) scene.lookup("#comboBox3");
        CheckComboBox<String> checkComboBox = new CheckComboBox();
        HBox elementsHBox = (HBox) scene.lookup("#elementsHBox");
        checkComboBox.getItems().addAll(elements);

        checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                checkComboBox.getCheckModel().getCheckedItems();
                currentAnalytes = checkComboBox.getCheckModel().getCheckedItems();
                System.out.println(checkComboBox.getCheckModel().getCheckedItems());
            }
        });


        elementsHBox.getChildren().add(checkComboBox);

        makeComboBoxEditable(comboBox1, countries);
        makeComboBoxEditable(comboBox2, matrixes);
        makeComboBoxEditable(comboBox3, countries);

    }

    private void makeComboBoxEditable(ComboBox comboBox, List<String> countries) {
        comboBox.getItems().addAll(countries);

        comboBox.setEditable(true);
        comboBox.setMaxWidth(Double.MAX_VALUE);

        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(comboBox.getValue());
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
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "5 min": // 3000 punkti 500ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 6000;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "3 min": // 1800 punkti 360ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 3600;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "2 min": // 1200 punkti 240ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 2400;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "1 min": // 600 punkti 120ste vahedega
                            oldUpperBound = upperBound;
                            upperBound = 1200;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
                            xAxis.setTickUnit(upperBound/5);
                            break;
                        case "30 sec": // 300 punkti 60ste vahedega  Default start
                            oldUpperBound = upperBound;
                            upperBound = 600;
                            System.out.println(oldUpperBound + " " + upperBound);
                            xAxis.setUpperBound(testData.size());
                            xAxis.setLowerBound(testData.size() - upperBound);
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
            }
        });
        stopButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("stop");
                isStarted = false;
            }
        });
        clearButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("clear");
                testData = new ArrayList();
                series.getData().clear();
                xSeriesData = 0;
                xAxis.setLowerBound(0);
                xAxis.setUpperBound(upperBound);
            }
        });
        saveButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("save");
                FileChooser fileChooser = new FileChooser();

                //Set extension filter for text files
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    saveTextToFile(file);
                }
            }
        });
        Button onOff = (Button) scene.lookup("#onOff");
        onOff.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (isHighVoltage) {
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
                } else {
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
                        String outData = "v" + (int) (127 - out * 1.27)+"\n";
                        //Arduino kood
                        try {
                            serialPort.writeString(outData);
                            //outputStream.write(outData.getBytes());
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                        System.out.println(outData);
                    }
                } catch (NumberFormatException ex) {
                    field.setStyle("-fx-text-inner-color: red;");
                    field.setText("ERROR");
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
        GridPane.setConstraints(box1,0,11);
        GridPane.setConstraints(box2,1,11);
        GridPane.setConstraints(box3,2,11);
        GridPane.setConstraints(box4,3,11);
        GridPane grid = (GridPane) scene.lookup("#testSettings");
        grid.getChildren().addAll(box1, box2, box3, box4);
    }

    private void saveTextToFile(File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println("C4d_2015-test");
            writer.println("aeg, juhtivus");
            for (int i = 0; i < testData.size(); i++) {
                writer.println(" "+i+", "+testData.get(i));
            }
            System.out.println(testData.size());
            System.out.println("done");
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        TextField currentField = (TextField) scene.lookup("#currentAmper");
        currentField.setText(String.valueOf(Math.round(current/256.0)));
        double voltagePercent = Double.parseDouble(androidData.split(" ")[4]);
        TextField percentageField = (TextField) scene.lookup("#voltagePercentBox");
        percentageField.setText(String.valueOf(Math.round((127.0-voltagePercent)/1.27)));
        //counter += 1;
        TextField textField = (TextField) scene.lookup("#androidData");
        if (androidData.length() - androidData.replace("C", "").length() == 1) {
            textField.setText(androidData);
        }
        if (isStarted) {

            series.getData().add(new AreaChart.Data(xSeriesData++, measurement));
            testData.add(measurement);

            xAxis.setLowerBound(xAxis.getLowerBound() + 1);
            xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        }
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        launch(args);
    }

}
