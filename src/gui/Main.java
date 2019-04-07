package gui;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import gnu.io.SerialPort;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
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
    private ArrayList<String> currentAnalytes = new ArrayList<>();
    private String currentMatrix;
    private String currentBGE;
    private String currentKapilaar;
    private ConcurrentLinkedQueue<Number> dataQ = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private XYChart.Series series1;
    private int xSeriesData = 0;
    private NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private ExecutorService executor;
    private AddToQueue addToQueue;
    private ArrayList testData = new ArrayList();
    private int lowest = Integer.MAX_VALUE;
    private int timeMoment = 0;
    private int counter = 0; // We don't need 100 first measurements.
    private LineChart<Number,Number> lineChart;
    private Stack<String> arduinoData;
    private Scene scene;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private double zoom = 1;
    private boolean isStarted = false;
    private boolean isHighVoltage = false;
    private String highVoltage = "h";


    @Override
    public void start(Stage stage) throws Exception{
        //JÄRGMINE ON FAILIST LUGEMISE KOOD
        readFile();

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
//        ArduinoReader reader = new ArduinoReader();
//        reader.initialize();
//        arduinoData = reader.getData();
//        serialPort = reader.getSerialPort();

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

        ComboBox<HideableItem<String>> comboBox1 = createComboBoxWithAutoCompletionSupport(countries);
        comboBox1.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/4.5);

        ComboBox<HideableItem<String>> comboBox2 = createComboBoxWithAutoCompletionSupport(countries);
        comboBox2.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/4.5);

        ComboBox<HideableItem<String>> comboBox3 = createComboBoxWithAutoCompletionSupport(countries);
        comboBox3.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/4.5);

        ComboBox<HideableItem<String>> comboBox4 = createComboBoxWithAutoCompletionSupport(countries);
        comboBox4.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()/4.5);

        GridPane.setConstraints(comboBox1,1,1);
        GridPane.setConstraints(comboBox2,1,3);
        GridPane.setConstraints(comboBox3,1,4);
        GridPane.setConstraints(comboBox4,1,5);
        GridPane gridPane = (GridPane) scene.lookup("#testOptions");
        gridPane.getChildren().addAll(comboBox1, comboBox2, comboBox3, comboBox4);
    }

    private static <T> ComboBox<HideableItem<T>> createComboBoxWithAutoCompletionSupport(List<T> items) {
        ObservableList<HideableItem<T>> hideableHideableItems = FXCollections.observableArrayList(hideableItem -> new Observable[]{
                hideableItem.hiddenProperty()
        });

        items.forEach(item -> {
            HideableItem<T> hideableItem = new HideableItem<>(item);
            hideableHideableItems.add(hideableItem);
        });

        FilteredList<HideableItem<T>> filteredHideableItems = new FilteredList<>(hideableHideableItems, t -> !t.isHidden());

        ComboBox<HideableItem<T>> comboBox = new ComboBox<>();
        comboBox.setItems(filteredHideableItems);

        @SuppressWarnings("unchecked")
        HideableItem<T>[] selectedItem = (HideableItem<T>[]) new HideableItem[1];

        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(!comboBox.isShowing()) return;

            comboBox.setEditable(true);
            comboBox.getEditor().clear();
        });

        comboBox.showingProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                @SuppressWarnings("unchecked")
                ListView<HideableItem> lv = ((ComboBoxListViewSkin<HideableItem>) comboBox.getSkin()).getListView();

                Platform.runLater(() -> {
                    if(selectedItem[0] == null) {
                        double cellHeight = ((Control) lv.lookup(".list-cell")).getHeight();
                        lv.setFixedCellSize(cellHeight);
                    }
                });

                lv.scrollTo(comboBox.getValue());
            } else {
                HideableItem<T> value = comboBox.getValue();
                if(value != null) selectedItem[0] = value;

                comboBox.setEditable(false);

                Platform.runLater(() -> {
                    comboBox.getSelectionModel().select(selectedItem[0]);
                    comboBox.setValue(selectedItem[0]);
                });
            }
        });

        comboBox.setOnHidden(event -> hideableHideableItems.forEach(item -> item.setHidden(false)));

        comboBox.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if(!comboBox.isShowing()) return;

            Platform.runLater(() -> {
                if(comboBox.getSelectionModel().getSelectedItem() == null) {
                    hideableHideableItems.forEach(item -> item.setHidden(!item.getObject().toString().toLowerCase().contains(newValue.toLowerCase())));
                } else {
                    boolean validText = false;

                    for(HideableItem hideableItem : hideableHideableItems) {
                        if(hideableItem.getObject().toString().equals(newValue))
                        {
                            validText = true;
                            break;
                        }
                    }

                    if(!validText) comboBox.getSelectionModel().select(null);
                }
            });
        });

        return comboBox;
    }

    private void makeComboBoxes(Scene scene) {
        ChoiceBox cb = (ChoiceBox) scene.lookup("#userbox");
        cb.getItems().addAll("Regular user", "Scientist", "Administrator");
        cb.getSelectionModel().selectFirst();
        cb.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentUser = (String) cb.getItems().get((Integer) number2);
                System.out.println(currentUser);
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
                    switch (currentTime) {
                        case "10 min":
                            zoom = 1;
                            break;
                        case "5 min":
                            zoom = 10;
                            break;
                        case "3 min":
                            zoom = 100;
                            break;
                        case "2 min":
                            zoom = 1000;
                            break;
                        case "1 min":
                            zoom = 0.1;
                            break;
                        case "30 sec":
                            zoom = 0.01;
                            break;
                    }
                    System.out.println(lineChart.widthProperty());
//                    xAxis.setUpperBound(xAxis.getUpperBound() * zoom);
//                    xAxis.setLowerBound(xAxis.getLowerBound() * zoom);
//                    xAxis.setTickUnit(xAxis.getTickUnit() * zoom);
//                    System.out.println(currentTime);
                    //label.setText(((ToggleButton) selectedToggle).getText());
                }
                else {
                    //label.setText("...");
                }
            }
        });
        // select the first button to start with
        //group.selectToggle(tb1);
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
                            androidFrequency = "Q9";
                            break;
                        case "1.6 MHz":
                            androidFrequency = "Q7";
                            break;
                        case "1.3 MHz":
                            androidFrequency = "Q8";
                            break;
                        case "1 MHz":
                            androidFrequency = "Q0";
                            break;
                        case "880 kHz":
                            androidFrequency = "Q1";
                            break;
                        case "800 kHz":
                            androidFrequency = "Q2";
                            break;
                        case "660 kHz":
                            androidFrequency = "Q3";
                            break;
                        case "500 kHz":
                            androidFrequency = "Q4";
                            break;
                        case "400 kHz":
                            androidFrequency = "Q5";
                            break;
                        case "300 kHz":
                            androidFrequency = "Q6";
                            break;
                    }
                    //JÄRGMINE ON ARDUINO KOOD
//                    try {
//                        outputStream = serialPort.getOutputStream();
//                        outputStream.write(androidFrequency.getBytes()); //KAS TÖÖTAB ?????
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    System.out.println(androidFrequency);
                }
                else {
                }
            }
        });
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
                makeMovingChart(scene); //Ei tööta päris õigesti.
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
                    highVoltage = "h";
                    //Androidi kood
//                    try {
//                        outputStream.write(highVoltage.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                } else {
                    isHighVoltage = true;
                    onOff.setStyle("-fx-background-color: lawngreen;");
                    onOff.setText("ON");
                    highVoltage = "H";
                    //Androidi kood
//                    try {
//                        outputStream.write(highVoltage.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
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
        //lineChart = new LineChart<Number,Number>(xAxis,yAxis); //Siis on palju laiemalt graafik

        xAxis = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false); // Peab olema false. Muidu muudab ise graafiku laiust.
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickUnit(100); // See on ühe mõõtmisühiku suurus. Sellest ei sõltu graafiku kitsus.
        xAxis.setMinorTickLength(1000000);

        lineChart = new LineChart<Number,Number>(xAxis,yAxis); //Siis on palju kitsam graafik

        lineChart.getData().addAll(series1);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.setVerticalGridLinesVisible(false);
        lineChart.setAnimated(false);

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pane.setPannable(true);
        pane.setFitToWidth(false); //false teeb venimist vähemaks.
        pane.setFitToHeight(true);
        pane.setContent(lineChart);

        GridPane.setConstraints(pane, 1, 0);
        GridPane mainPane = (GridPane) scene.lookup("#chartPane");
        mainPane.getChildren().add(pane);
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
        //SEE OSA TULEB ÄRA MUUTA KUI LISADA HIGH VOLTAGE OSA!!!
        //JÄRGMINE ON FAILIST LUGEMISE KOOD
        while (counter < 20) { // Enne 20 tekib imelik piik. Seda pole vaja lugeda.
            if (dataQ1.isEmpty()) {
                return;
            }
            dataQ1.remove();
            this.counter += 1;
        }
        if (!dataQ1.isEmpty()) {
            Number data = dataQ1.remove();
            if (isStarted) {
                series1.getData().add(new AreaChart.Data(xSeriesData++, data));
                lineChart.setMinWidth(lineChart.getWidth()+4);
                xAxis.setUpperBound(xAxis.getUpperBound()+1);
            }
        } else {
            return;
        }

//        if (zoom == 1) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        } else if (zoom == 10) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        } else if (zoom == 100) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        } else if (zoom == 1000) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        } else if (zoom == 0.1) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        } else if (zoom == 0.01) {
//            lineChart.setMinWidth(lineChart.getWidth()+4);
//            xAxis.setUpperBound(xAxis.getUpperBound()+1);
//        }
        //lineChart.setMinWidth(lineChart.getWidth()+1); //Ei veni, kui välja kommenteerida
        //JÄRGMINE ON ARDUINO KOOD
//        if (arduinoData.isEmpty()) {
//            return;
//        }
//        String androidData = arduinoData.pop();
//        counter += 1;
//        if (counter > 10) {
//            if (isStarted) {
//                Number measurement = Integer.parseInt(androidData.split(" ")[1]);
//                series1.getData().add(new AreaChart.Data(xSeriesData++, measurement));
//                TextField textField = (TextField) scene.lookup("#androidData");
//                textField.setText(androidData);
//                lineChart.setMinWidth(lineChart.getWidth()+4);
//                xAxis.setUpperBound(xAxis.getUpperBound()+1);
//            }
//        }
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        launch(args);
    }

}
