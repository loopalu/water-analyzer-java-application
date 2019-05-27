package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
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
import javafx.scene.Node;
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
import main.util.DatabaseCommunicator;
import main.util.FileManager;
import main.util.ImageSaver;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.textfield.TextFields;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Makes the graphical interface and runs all the methods.
 */
public class Main extends Application {
    private static final int COUNTER_BLOCKING_NUMBER = 100;
    private String currentTime = "";
    private String currentFrequency = "2 MHz";
    private String androidFrequency = "G9\n";
    private String currentUser = "Regular user";
    private String currentMethod = "";
    private String currentCapillaryTotal = "40 cm";
    private String currentCapillaryEffective = "25 cm";
    private ObservableList<String> currentAnalytes = FXCollections.observableArrayList();
    private ObservableList<String> currentBge = FXCollections.observableArrayList();
    private String currentMatrix = "";
    private String currentCapillary = "50/150 μm";
    private double xSeriesData = 0;
    private ArrayList testData = new ArrayList();
    private int counter = 0; // We don't need 100 first measurements.
    private LineChart<Number,Number> lineChart10, lineChart5, lineChart3, lineChart2, lineChart1, lineChart30, currentLineChart10, currentLineChart5, currentLineChart3, currentLineChart2, currentLineChart1, currentLineChart30;
    private Stack<String> arduinoData;
    private Scene scene;
    private SerialPort serialPort;
    private boolean isStarted = false;
    private boolean isHighVoltage = false;
    private String highVoltage = "h";
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
    private ConcentrationTable concentrationTable;
    private ConcentrationTable elementsConcentrationTable;
    private String currentInjection = "Pressure";
    private String injectionTime = "0";
    private String currentDescription = "";
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
    private HashMap<String, Integer> users = new HashMap<String, Integer>() {{ put("Regular user", 3);put("Scientist", 2);put("Administrator", 1);}};
    private String hvValue = "0";
    private int out = 0;
    private int currentUserClass = 1;
    private HashMap<String, LabTest> methods = new HashMap<>();
    private ToggleGroup group;
    private PrintWriter debugWriter;
    private int size = 0;
    private HBox box1, box2, box3, box4, box5, box6, box7, box8, box9, box10;
    private ArrayList<String> tempAnalytes = new ArrayList<>();
    private ArrayList<String> tempBges = new ArrayList<>();
    private ArrayList<String> tempMatrixes = new ArrayList<>();
    private NumberAxis xAxis10, xAxis5, xAxis3, xAxis2, xAxis1, xAxis30, xCurrentAxis10, xCurrentAxis5, xCurrentAxis3, xCurrentAxis2, xCurrentAxis1, xCurrentAxis30;

    /**
     * Starts the graphical interface.
     *
     * @param stage Canvas for the graphical interface.
     * @throws Exception Possible exception that can be rise.
     */
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

        sendUnsentData();
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
        switchObjectPermissions(scene, currentUserClass);
        //JÄRGMINE ON ARDUINO KOOD
        ArduinoReader reader = new ArduinoReader();
        reader.initialize();
        arduinoData = reader.getData();
        serialPort = reader.getSerialPort();
        //-- Prepare Timeline
        prepareTimeline();
    }

    /**
     * Sends unsent test results to REST API.
     */
    private void sendUnsentData() {
        DatabaseCommunicator communicator = new DatabaseCommunicator();
        if (communicator.isApiAvailable("getUsers")) {
            String current;
            ArrayList<String> tests = getUnsentTests();
            while (tests.size() > 0) {
                try {
                    current = new File( "." ).getCanonicalPath();
                    String data = FileManager.readFile(current+"/" + tests.get(0) + "/" + tests.get(0) + "unsent.txt");
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    JsonObject jsonObject = gson.fromJson(data, JsonObject.class);
                    LabTest labTest = communicator.makeMethod(jsonObject);
                    communicator.postTest(labTest);
                    tests.remove(0);
                    if (tests.size() > 0) {
                        Files.deleteIfExists(Paths.get(current+"/" + tests.get(0) + "/" + tests.get(0) + "unsent.txt"));
                    }
                } catch (IOException e) {
                    System.out.println("No unsent.txt file.");
                    return;
                }
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("unsentTests.txt"));
                writer.write("");
                writer.newLine();
                writer.close();
                FileManager.hide("unsentTests.txt");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns unsent tests from txt file.
     *
     * @return Arraylist of unsent tests.
     */
    private ArrayList<String > getUnsentTests() {
        ArrayList<String> strings = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("unsentTests.txt"));
            while (scanner.hasNextLine()) {
                String string = scanner.nextLine();
                if (!string.equals("")){
                    strings.add(string);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No unsentTests.txt file.");
        }
        return strings;
    }

    /**
     * In the case of no internet, all new analytes/bges/matrixes will be saved to txt file.
     *
     * @param current Current folder.
     * @param timeStamp Timestamp of the test.
     * @param labTest LabTest object with the data of test.
     */
    private void saveOffline(String current, String timeStamp, LabTest labTest) {
        DatabaseCommunicator communicator = new DatabaseCommunicator();
        if (!communicator.isApiAvailable("getUsers")) {
            BufferedWriter writer;
            ArrayList<String> savedAnalytes = new ArrayList<>();
            ArrayList<String> savedBges = new ArrayList<>();
            ArrayList<String> savedMatrixes = new ArrayList<>();
            for (String string : currentAnalytes) {
                if (!analytes.contains(string)) {
                    savedAnalytes.add(string);
                }
            }
            for (String string : currentBge) {
                if (!bges.contains(string)) {
                    savedBges.add(string);
                }
            }
            if (!matrixes.contains(currentMatrix)) {
                savedMatrixes.add(currentMatrix);
            }

            try {
                writer = new BufferedWriter(new FileWriter("offlineData.txt"));
                FileManager.hide("offlineData.txt");
                OfflineData offlineData = new OfflineData();
                savedAnalytes.addAll(tempAnalytes);
                savedBges.addAll(tempBges);
                savedMatrixes.addAll(tempMatrixes);
                offlineData.setAnalytes(savedAnalytes);
                offlineData.setBges(savedBges);
                offlineData.setMatrixes(savedMatrixes);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(offlineData);
                writer.write(json);
                writer.newLine();
                writer.close();
            } catch (IOException e) {
                System.out.println("No offlineData.txt file where to save.");
            }
            BufferedWriter writer2;
            try {
                writer2 = new BufferedWriter(new FileWriter((current+"/" + timeStamp + File.separator + timeStamp + "unsent.txt")));
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(labTest);
                writer2.write(json);
                writer2.newLine();
                writer2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileManager.hide(current+"/" + timeStamp + File.separator + timeStamp + "unsent.txt");

            ArrayList<String> tests = getUnsentTests();
            tests.add(timeStamp);
            System.out.println("302 timestamp " + tests);
            BufferedWriter writer3;
            try {
                writer3 = new BufferedWriter(new FileWriter("unsentTests.txt"));
                for (String test : tests) {
                    System.out.println(test);
                    writer3.write(test);
                    writer3.newLine();
                }
                writer3.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //FileManager.hide("unsentTests.txt");
        }
    }

    /**
     * Based on user class (administrator/scientist/regular user) this method turns on/off the ability
     * to change elements on graphical interface.
     *
     * @param scene Canvas for the graphical interface.
     * @param userClass The class of user.
     */
    private void switchObjectPermissions(Scene scene, int userClass) {
        TextArea commentaryField = (TextArea) scene.lookup("#textArea");
        CheckComboBox<String> checkElementsComboBox = (CheckComboBox<String>) scene.lookup("#elementsComboBoxId");
        ChoiceBox elementsValueBox = (ChoiceBox) scene.lookup("#elementsValueBox");
        CheckComboBox<String> checkBgeComboBox = (CheckComboBox<String>) scene.lookup("#bgeComboBoxId");
        ChoiceBox bgeValueBox = (ChoiceBox) scene.lookup("#bgeValueBox");
        ChoiceBox capillaryBox = (ChoiceBox) scene.lookup("#capillaryBox");
        ChoiceBox capillaryTotalBox = (ChoiceBox) scene.lookup("#capillaryTotalBox");
        ChoiceBox capillaryEffectiveBox = (ChoiceBox) scene.lookup("#capillaryEffectiveBox");
        ChoiceBox injectionBox = (ChoiceBox) scene.lookup("#injectionBox");
        ChoiceBox injectionChoiceUnitBox = (ChoiceBox) scene.lookup("#injectionChoiceUnitBox");
        TextField injectionChoiceValueField = (TextField) scene.lookup("#injectionChoiceValue");
        TextField durationField = (TextField) scene.lookup("#durationField");
        ComboBox comboBox = (ComboBox) scene.lookup("#comboBox2");
        TextField field = (TextField) scene.lookup("#percentageField");
        ComboBox methodBox = (ComboBox) scene.lookup("#comboBox1");
        switch (userClass) {
            case 1:
                box1.setMouseTransparent(false);
                box2.setMouseTransparent(false);
                box3.setMouseTransparent(false);
                box4.setMouseTransparent(false);
                box5.setMouseTransparent(false);
                box6.setMouseTransparent(false);
                box7.setMouseTransparent(false);
                box8.setMouseTransparent(false);
                box9.setMouseTransparent(false);
                box10.setMouseTransparent(false);
                methodBox.setEditable(true);
                field.setEditable(true);
                comboBox.setMouseTransparent(false);
                durationField.setEditable(true);
                injectionChoiceValueField.setEditable(true);
                injectionChoiceUnitBox.setMouseTransparent(false);
                injectionBox.setMouseTransparent(false);
                capillaryEffectiveBox.setMouseTransparent(false);
                capillaryTotalBox.setMouseTransparent(false);
                capillaryBox.setMouseTransparent(false);
                bgeValueBox.setMouseTransparent(false);
                checkBgeComboBox.setMouseTransparent(false);
                elementsValueBox.setMouseTransparent(false);
                commentaryField.setEditable(true);
                checkElementsComboBox.setMouseTransparent(false);
                System.out.println("admin");
                break;
            case 2:
                box1.setMouseTransparent(false);
                box2.setMouseTransparent(false);
                box3.setMouseTransparent(false);
                box4.setMouseTransparent(false);
                box5.setMouseTransparent(false);
                box6.setMouseTransparent(false);
                box7.setMouseTransparent(false);
                box8.setMouseTransparent(false);
                box9.setMouseTransparent(false);
                box10.setMouseTransparent(false);
                methodBox.setEditable(false);
                field.setEditable(true);
                comboBox.setMouseTransparent(false);
                durationField.setEditable(true);
                injectionChoiceValueField.setEditable(true);
                injectionChoiceUnitBox.setMouseTransparent(false);
                injectionBox.setMouseTransparent(false);
                capillaryEffectiveBox.setMouseTransparent(false);
                capillaryTotalBox.setMouseTransparent(false);
                capillaryBox.setMouseTransparent(false);
                bgeValueBox.setMouseTransparent(false);
                checkBgeComboBox.setMouseTransparent(false);
                elementsValueBox.setMouseTransparent(false);
                commentaryField.setEditable(true);
                checkElementsComboBox.setMouseTransparent(false);
                System.out.println("scientist");
                break;
            case 3:
                box1.setMouseTransparent(true);
                box2.setMouseTransparent(true);
                box3.setMouseTransparent(true);
                box4.setMouseTransparent(true);
                box5.setMouseTransparent(true);
                box6.setMouseTransparent(true);
                box7.setMouseTransparent(true);
                box8.setMouseTransparent(true);
                box9.setMouseTransparent(true);
                box10.setMouseTransparent(true);
                methodBox.setEditable(false);
                field.setEditable(false);
                comboBox.setMouseTransparent(true);
                durationField.setEditable(false);
                injectionChoiceValueField.setEditable(false);
                injectionChoiceUnitBox.setMouseTransparent(true);
                injectionBox.setMouseTransparent(true);
                capillaryEffectiveBox.setMouseTransparent(true);
                capillaryTotalBox.setMouseTransparent(true);
                capillaryBox.setMouseTransparent(true);
                bgeValueBox.setMouseTransparent(true);
                checkBgeComboBox.setMouseTransparent(true);
                elementsValueBox.setMouseTransparent(true);
                commentaryField.setEditable(false);
                checkElementsComboBox.setMouseTransparent(true);
                System.out.println("user");
                break;
        }
    }

    /**
     * Gets data from database and implements it into graphical interface.
     */
    private void getDataFromDatabase() {
        String offlineData = FileManager.readFile("offlineData.txt");
        OfflineData dataObject;
        if (offlineData.contains("analytes") || offlineData.contains("bges") || offlineData.contains("matrixes")) {
            Gson gson = new Gson();
            dataObject = gson.fromJson(offlineData, OfflineData.class);
            tempAnalytes = dataObject.getAnalytes();
            tempBges = dataObject.getBges();
            tempMatrixes = dataObject.getMatrixes();
            for (String string: tempAnalytes) {
                if (!analytes.contains(string)) {
                    analytes.add(string);
                }
            }
            for (String string: tempBges) {
                if (!bges.contains(string)) {
                    bges.add(string);
                }
            }
            for (String string: tempMatrixes) {
                if (!matrixes.contains(string)) {
                    matrixes.add(string);
                }
            }
        }
        DatabaseCommunicator databaseCommunicator = new DatabaseCommunicator();
        ArrayList<String> tempAnalytes2 = databaseCommunicator.getAnalytes();
        if (tempAnalytes2 != null) {
            for (String analyte:tempAnalytes2) {
                if (!analytes.contains(analyte)) {
                    analytes.add(analyte);
                }
            }
        }
        ArrayList<String> tempBges2 = databaseCommunicator.getBges();
        if (tempBges2 != null) {
            for (String bge:tempBges2) {
                if (!bges.contains(bge)) {
                    bges.add(bge);
                }
            }
        }
        ArrayList<String> tempMatrixes2 = databaseCommunicator.getMatrixes();
        if (tempMatrixes2 != null) {
            for (String matrix:tempMatrixes2) {
                if (!matrixes.contains(matrix)) {
                    matrixes.add(matrix);
                }
            }
        }
        ArrayList<String> userList = new ArrayList<>(users.keySet());
        HashMap<String, Integer> newUsers = databaseCommunicator.getUsers();
        ArrayList<String> newUserList = new ArrayList<>(newUsers.keySet());
        for (String user:newUserList) {
            if (!userList.contains(user)) {
                users.put(user,newUsers.get(user));
            }
        }
        methods = databaseCommunicator.getMethods();
    }

    /**
     * Makes timer for the graphical interface.
     *
     * @param scene Canvas for the graphical interface.
     */
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

    /**
     * Makes dropdown menus and buttons for test options.
     *
     * @param scene Canvas for the graphical interface.
     */
    private void makeOptions(Scene scene) {
        ArrayList<String> methodsList;
        if (methods.size() > 0) {
            methodsList = new ArrayList<String>(methods.keySet());
        } else {
            methodsList = new ArrayList<>();
        }

        ComboBox comboBox1 = (ComboBox) scene.lookup("#comboBox1");
        ComboBox comboBox2 = (ComboBox) scene.lookup("#comboBox2");

        CheckComboBox<String> checkElementsComboBox = new CheckComboBox();
        checkElementsComboBox.setId("elementsComboBoxId");
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
        checkBgeComboBox.setId("bgeComboBoxId");
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

        makeComboBoxEditable(comboBox1, methodsList);
        makeComboBoxEditable(comboBox2, matrixes);

    }

    /**
     * Makes combobox (dropdown list) editable.
     *
     * @param comboBox Given dropdown list..
     * @param dropDownList Given list of elements for the dropdown list.
     */
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
                    LabTest labTest = methods.get(currentMethod);
                    changeSettingsWithMethod(labTest);
                } else if (comboBox.getId().equals("comboBox2")) {
                    currentMatrix = (String) comboBox.getValue();
                }
            }
        });

        comboBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Mouse event");
                System.out.println(comboBox.getEditor().getText());
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

    /**
     * Receives LabTest object and changes the settings of the interface by using the information from LabTest object.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeSettingsWithMethod(LabTest labTest) {
        if (!currentMethod.equals("")) {
            changeAnalytes(labTest);
            changeCommentary(labTest);
            changeAnalytesConcentrations(labTest);
            changeAnalytesUnit(labTest);
            changeMatrix(labTest);
            changeBge(labTest);
            changeBgesConcentrations(labTest);
            changeBgeUnit(labTest);
            changeCapillary(labTest);
            changeCapillaryTotalLength(labTest);
            changeCapillaryEffectiveLength(labTest);
            changeInjectionMethod(labTest);
            changeInjectionMethodChoice();
            changeInjectionValue(labTest);
            changeInjectionUnit(labTest);
            changeInjectionDuration(labTest);
            changeFrequency(labTest);
            changeHVValue(labTest);
        }
    }

    /**
     * Changes the concentrations of BGE-s on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeBgesConcentrations(LabTest labTest) {
        TextArea commentaryField = (TextArea) scene.lookup("#textArea");
        String commentary = commentaryField.getText();
        ObservableList<Analyte> bges = labTest.getBge();
        commentary += "--------------------------------\n";
        commentary += "BGE concentrations:\n";
        for (Analyte bge:bges) {
            commentary += bge.getAnalyte()+ " " + bge.getConcentration() + " " + labTest.getBgeUnit() + "\n";
        }
        commentaryField.setText(commentary);
    }

    /**
     * Changes the concentrations of analytes on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeAnalytesConcentrations(LabTest labTest) {
        TextArea commentaryField = (TextArea) scene.lookup("#textArea");
        String commentary = commentaryField.getText();
        ObservableList<Analyte> analytes = labTest.getAnalytes();
        commentary += "\n";
        commentary += "--------------------------------\n";
        commentary += "Analyte concentrations:\n";
        for (Analyte analyte:analytes) {
            commentary += analyte.getAnalyte()+ " " + analyte.getConcentration() + " " + labTest.getAnalyteUnit() + "\n";
        }
        commentaryField.setText(commentary);
    }

    /**
     * Changes the matrix on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeMatrix(LabTest labTest) {
        ComboBox comboBox2 = (ComboBox) scene.lookup("#comboBox2");
        comboBox2.getSelectionModel().select(labTest.getMatrix());
        currentMatrix = labTest.getMatrix();
    }

    /**
     * Changes the BGE on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeBge(LabTest labTest) {
        CheckComboBox<String> checkBgeComboBox = (CheckComboBox<String>) scene.lookup("#bgeComboBoxId");
        ObservableList<Analyte> bgeObservableList = labTest.getBge();
        checkBgeComboBox.getCheckModel().clearChecks();
        for (Analyte bge:bgeObservableList) {
            checkBgeComboBox.getCheckModel().check(bges.indexOf(bge.getAnalyte()));
        }
        checkBgeComboBox.getCheckModel().getCheckedItems();
        currentBge = checkBgeComboBox.getCheckModel().getCheckedItems();
    }

    /**
     * Changes the analytes on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeAnalytes(LabTest labTest) {
        CheckComboBox<String> checkElementsComboBox = (CheckComboBox<String>) scene.lookup("#elementsComboBoxId");
        ObservableList<Analyte> analytesObservableList = labTest.getAnalytes();
        checkElementsComboBox.getCheckModel().clearChecks();
        for (Analyte analyte:analytesObservableList) {
            checkElementsComboBox.getCheckModel().check(analytes.indexOf(analyte.getAnalyte()));
        }
        checkElementsComboBox.getCheckModel().getCheckedItems();
        currentAnalytes = checkElementsComboBox.getCheckModel().getCheckedItems();
    }

    /**
     * Changes the unit of BGE on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeBgeUnit(LabTest labTest) {
        ChoiceBox bgeValueBox = (ChoiceBox) scene.lookup("#bgeValueBox");
        bgeValueBox.getSelectionModel().select(labTest.getBgeUnit());
        currentBgeValue = labTest.getBgeUnit();
    }

    /**
     * Changes the unit of analytes on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeAnalytesUnit(LabTest labTest) {
        ChoiceBox elementsValueBox = (ChoiceBox) scene.lookup("#elementsValueBox");
        elementsValueBox.getSelectionModel().select(labTest.getAnalyteUnit());
        currentAnalyteValue = labTest.getAnalyteUnit();
    }

    /**
     * Changes the the value of high voltage percentage on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeHVValue(LabTest labTest) {
        TextField field = (TextField) scene.lookup("#percentageField");
        field.setText(labTest.getHvValue());
    }

    /**
     * Changes the frequency on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeFrequency(LabTest labTest) {
        ToggleButton button = (ToggleButton) scene.lookup(("#"+labTest.getFrequency().split(" ")[0]));
        button.setToggleGroup(group);
        group.selectToggle(button);
        currentFrequency = labTest.getFrequency();
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
    }

    /**
     * Changes the commentary of the method on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeCommentary(LabTest labTest) {
        TextArea commentaryField = (TextArea) scene.lookup("#textArea");
        commentaryField.setText(labTest.getDescription());
        currentDescription = labTest.getDescription();
    }

    /**
     * Changes the duration of the injection on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeInjectionDuration(LabTest labTest) {
        TextField durationField = (TextField) scene.lookup("#durationField");
        durationField.setText(labTest.getInjectionTime());
        injectionTime = labTest.getInjectionTime();
    }

    /**
     * Changes the value of injection measurement.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeInjectionValue(LabTest labTest) {
        TextField injectionChoiceValueField = (TextField) scene.lookup("#injectionChoiceValue");
        injectionChoiceValueField.setText(labTest.getInjectionChoiceValue());
        injectionChoiceValue = labTest.getInjectionChoiceValue();
    }

    /**
     * Changes the unit of the injection type.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeInjectionUnit(LabTest labTest) {
        ChoiceBox injectionChoiceUnitBox = (ChoiceBox) scene.lookup("#injectionChoiceUnitBox");
        injectionChoiceUnitBox.getSelectionModel().select(labTest.getInjectionChoiceUnit());
        currentInjectionChoiceUnit = labTest.getInjectionChoiceUnit();
    }

    /**
     * Sets the target of injection measurement.
     */
    private void changeInjectionMethodChoice() {
        Text injectionChoiceText = (Text) scene.lookup("#injectionChoiceText");
        if (currentInjection.equals("Vacuum")) {
            injectionChoiceText.setText("Difference");
            currentInjectionChoice = "Difference";
        } else if (currentInjection.equals("Pressure")) {
            injectionChoiceText.setText("Difference");
            currentInjectionChoice = "Difference";
        } else {
            injectionChoiceText.setText("Voltage");
            currentInjectionChoice = "Voltage";
        }
    }

    /**
     * Changes the type of injection (a.la. electricity/pressure/vacuum).
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeInjectionMethod(LabTest labTest) {
        ChoiceBox injectionBox = (ChoiceBox) scene.lookup("#injectionBox");
        injectionBox.getSelectionModel().select(labTest.getInjectionMethod());
        currentInjection = labTest.getInjectionMethod();
    }

    /**
     * Changes the effective length of capillary on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeCapillaryEffectiveLength(LabTest labTest) {
        ChoiceBox capillaryEffectiveBox = (ChoiceBox) scene.lookup("#capillaryEffectiveBox");
        capillaryEffectiveBox.getSelectionModel().select(labTest.getCapillaryEffectiveLength());
        currentCapillaryEffective = labTest.getCapillaryEffectiveLength();
    }

    /**
     * Changes the total length of capillary on interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeCapillaryTotalLength(LabTest labTest) {
        ChoiceBox capillaryTotalBox = (ChoiceBox) scene.lookup("#capillaryTotalBox");
        capillaryTotalBox.getSelectionModel().select(labTest.getCapillaryTotalLength());
        currentCapillaryTotal = labTest.getCapillaryTotalLength();
    }

    /**
     * Changes the capillary on the interface.
     *
     * @param labTest LabTest object with the data received from the database.
     */
    private void changeCapillary(LabTest labTest) {
        ChoiceBox capillaryBox = (ChoiceBox) scene.lookup("#capillaryBox");
        capillaryBox.getSelectionModel().select(labTest.getCapillary());
        currentCapillary = labTest.getCapillary();
    }

    /**
     * Makes dropdown lists.
     *
     * @param scene Canvas for the graphical interface.
     */
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
        ArrayList<String> userList = new ArrayList<>(users.keySet());
        for (String user:userList) {
            userBox.getItems().add(user);
        }
        userBox.getSelectionModel().select("Administrator");
        //userBox.getSelectionModel().selectFirst();
        userBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                currentUser = (String) userBox.getItems().get((Integer) number2);
                currentUserClass = users.get(currentUser);
                switchObjectPermissions(scene, currentUserClass);
                System.out.println(currentUser + " " + users.get(currentUser));
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

    /**
     * Makes the time scale buttons for the chart.
     *
     * @param scene Canvas for the graphical interface.
     */
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
                    GridPane mainPane = (GridPane) scene.lookup("#chartPane");
                    mainPane.getChildren().remove(4);
                    mainPane.getChildren().remove(3);
                    switch (currentTime) {
                        case "10 min": // 6000 punkti 1200ste vahedega
                            mainPane.getChildren().addAll(lineChart10, currentLineChart10);
                            break;
                        case "5 min": // 3000 punkti 500ste vahedega
                            mainPane.getChildren().addAll(lineChart5, currentLineChart5);
                            break;
                        case "3 min": // 1800 punkti 360ste vahedega
                            mainPane.getChildren().addAll(lineChart3, currentLineChart3);
                            break;
                        case "2 min": // 1200 punkti 240ste vahedega
                            mainPane.getChildren().addAll(lineChart2, currentLineChart2);
                            break;
                        case "1 min": // 600 punkti 120ste vahedega
                            mainPane.getChildren().addAll(lineChart1, currentLineChart1);
                            break;
                        case "30 sec": // 300 punkti 60ste vahedega  Default start
                            mainPane.getChildren().addAll(lineChart30, currentLineChart30);
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

    /**
     * Makes frequency buttons.
     *
     * @param scene Canvas for the graphical interface.
     */
    private void makeFrequencyButtons(Scene scene) {
        ToggleButton button1 = new ToggleButton("2 MHz"); //2 MHz
        button1.setId("2");
        ToggleButton button2 = new ToggleButton("1.6 MHz"); //1.6 MHz
        button2.setId("1.6");
        ToggleButton button3 = new ToggleButton("1.3 MHz"); //1.3 MHz
        button3.setId("1.3");
        ToggleButton button4 = new ToggleButton("1 MHz"); //1 MHz
        button4.setId("1");
        ToggleButton button5 = new ToggleButton("880 kHz"); //880 kHz
        button5.setId("880");
        ToggleButton button6 = new ToggleButton("800 kHz"); //800 kHz
        button6.setId("800");
        ToggleButton button7 = new ToggleButton("660 kHz"); //660 kHz
        button7.setId("660");
        ToggleButton button8 = new ToggleButton("500 kHz"); //500 kHz
        button8.setId("500");
        ToggleButton button9 = new ToggleButton("400 kHz"); // 400 kHz
        button9.setId("400");
        ToggleButton button10 = new ToggleButton("300 kHz"); // 300 kHz
        button10.setId("300");
        box1 = new HBox(button1);
        box1.setId("hbox");
        box2 = new HBox(button2);
        box2.setId("hbox");
        box3 = new HBox(button3);
        box3.setId("hbox");
        box4 = new HBox(button4);
        box4.setId("hbox");
        box5 = new HBox(button5);
        box5.setId("hbox");
        box6 = new HBox(button6);
        box6.setId("hbox");
        box7 = new HBox(button7);
        box7.setId("hbox");
        box8 = new HBox(button8);
        box8.setId("hbox");
        box9 = new HBox(button9);
        box9.setId("hbox");
        box10 = new HBox(button10);
        box10.setId("hbox");

        group = new ToggleGroup();
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
                    System.out.println(androidFrequency);
                    //JÄRGMINE ON ARDUINO KOOD
                    try {
                        serialPort.writeString(androidFrequency);
                    } catch (SerialPortException e) {
                        e.printStackTrace();
                    }
                    //System.out.println(series.getData().size());
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

    /**
     * Makes buttons related to the test state (Start/Stop/Save/etc)
     *
     * @param scene Canvas for the graphical interface.
     * @param stage Canvas for the graphical interface.
     */
    private void makeStartStopButtons(Scene scene, Stage stage) {
        Button startButton = new Button("Start");
        Button stopButton = new Button("Stop");
        Button clearButton = new Button("Clear");
        Button saveButton = new Button("Save");
        startButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("start");
                try {
                    debugWriter = new PrintWriter("debug.txt");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                isStarted = true;
                stopWatchTimeline.play();
                try {
                    serialPort.writeString(androidFrequency);
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
                //sendDataToDatabase();
                getDataFromDatabase();
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
                    out = Integer.parseInt(fieldData);
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

    /**
     * Turns high voltage on.
     *
     * @param onOff Button for turning high voltage on.
     */
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

    /**
     * Turns high voltage off.
     *
     * @param onOff Button for turning high voltge off.
     */
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

    /**
     * Clears the chart and everything that is related to the test.
     *
     * @param scene Canvas for graphical interface.
     */
    private void clear(Scene scene) {
        System.out.println("clear");
        testData = new ArrayList();
        counter = 0;
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
        resetXaxis(xAxis10, 6000);
        resetXaxis(xAxis5, 3000);
        resetXaxis(xAxis3, 1800);
        resetXaxis(xAxis2, 1200);
        resetXaxis(xAxis1, 600);
        resetXaxis(xAxis30, 300);
        resetXaxis(xCurrentAxis10, 6000);
        resetXaxis(xCurrentAxis5, 3000);
        resetXaxis(xCurrentAxis3, 1800);
        resetXaxis(xCurrentAxis2, 1200);
        resetXaxis(xCurrentAxis1, 600);
        resetXaxis(xCurrentAxis30, 300);

        millisecond = 0;
        testTime = "00:00:00:000";
        stopWatchTimeline.stop();
        Text timerText = (Text) scene.lookup("#timerData");
        timerText.setText("00:00:00:000");
    }

    private void resetXaxis(NumberAxis xAxis, int bound) {
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(bound);
    }

    /**
     * Saves the data that is related to the test.
     */
    private void saveTest() {
        isTimerOn = false;
        isStarted = false;
        currentTimer = 60000;
        stopWatchTimeline.stop();
        Text textField = (Text) scene.lookup("#timerData");
        textField.setText("00:00:00:000");
        counter = 0;
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
        debugWriter.close();
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
//            PrintWriter dataWriter;
//            dataWriter = new PrintWriter(current+"/" + timeStamp + File.separator + timeStamp + "_data.txt");
//            for (int i = 0; i < testData.size(); i++) {
//                dataWriter.println(testData.get(i));
//            }
            System.out.println("done");
//            dataWriter.close();
//            ImageSaver.saveImage(testData, current+"/" + timeStamp + File.separator + timeStamp + "_image.png");
            testData = FileManager.readIntegers();

            if (concentrationTable != null) {
                LabTest labTest = new LabTest();
                labTest.setNameOfTest(timeStamp);
                labTest.setNameOfUser(currentUser);
                labTest.setUserClass(String.valueOf(currentUserClass));
                labTest.setNameOfMethod(currentMethod);
                labTest.setMatrix(currentMatrix);
                labTest.setCapillary(currentCapillary);
                labTest.setCapillaryTotalLength(currentCapillaryTotal);
                labTest.setCapillaryEffectiveLength(currentCapillaryEffective);
                labTest.setFrequency(currentFrequency);
                labTest.setInjectionMethod(currentInjection);
                labTest.setInjectionChoice(currentInjectionChoice);
                labTest.setInjectionChoiceValue(injectionChoiceValue);
                labTest.setInjectionChoiceUnit(currentInjectionChoiceUnit);
                labTest.setInjectionTime(injectionTime + " s");
                labTest.setCurrent(currentValueString + " µA");
                labTest.setHvValue(out + " %");
                writer = new BufferedWriter(new FileWriter((current+"/" + timeStamp + File.separator + timeStamp + "_settings.txt")));
                writer.write("User: "+ currentUser);
                writer.newLine();
                writer.write("LabTest: "+ currentMethod);
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
                writer.write("Injection labTest: "+ currentInjection + " " + currentInjectionChoice + ": " + injectionChoiceValue + " " + currentInjectionChoiceUnit + " Injection time: " + injectionTime + " s");
                writer.newLine();
                writer.write("Current: "+ currentValueString + " µA");
                writer.newLine();
                writer.write("HV value: " + out + " %");
                writer.newLine();
                writer.write("Analytes:");
                writer.newLine();
                TableView<Analyte> analytesTable = elementsConcentrationTable.getTable();
                ObservableList<Analyte> observableAnalytesList = analytesTable.getItems();
                labTest.setAnalytes(observableAnalytesList);
                labTest.setAnalyteUnit(currentAnalyteValue);
                for (Analyte analyte:observableAnalytesList) {
                    String string = analyte.getAnalyte();
                    if (!analytes.contains(string)) {
                        tempAnalytes.add(string);
                    }
                    writer.write(string+": "+analyte.getConcentration()+" " + currentAnalyteValue);
                    writer.newLine();
                }
                writer.write("BGE:");
                writer.newLine();
                TableView<Analyte> bgeTable = concentrationTable.getTable();
                ObservableList<Analyte> observableBgeList = bgeTable.getItems();
                labTest.setBge(observableBgeList);
                labTest.setBgeUnit(currentBgeValue);
                for (Analyte analyte:observableBgeList) {
                    String string = analyte.getAnalyte();
                    if (!bges.contains(string)) {
                        tempBges.add(string);
                    }
                    writer.write(string+": "+analyte.getConcentration()+" " + currentBgeValue);
                    writer.newLine();
                }
                labTest.setDescription(currentDescription);
                writer.write("Commentary:");
                writer.newLine();
                writer.write(currentDescription);
                writer.newLine();
                labTest.setTestTime(testTime);
                System.out.println("TestData size: " + testData.size());
                labTest.setTestData(testData);
                writer.write("Test duration: " + testTime);
                writer.newLine();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(labTest);
//                writer.write(json);
//                writer.newLine();
                writer.close();
                DatabaseCommunicator communicator = new DatabaseCommunicator();
                if (communicator.isApiAvailable("getUsers")) {
                    communicator.postTest(labTest);
                } else {
                    saveOffline(current, timeStamp, labTest);
                    System.out.println("api is not available");
                }
            }
            ImageSaver.saveImage(testData, current+"/" + timeStamp + File.separator + timeStamp + "_image.png");
            Path temp = Files.move(Paths.get("debug.txt"), Paths.get(current + "/" + timeStamp + File.separator + timeStamp + "_data.txt"));
//            File file = new File("debug.txt");
//            file.delete();
            testTime = "00:00:00:000";
            Button onOff = (Button) scene.lookup("#onOff");
            turnHighVoltageOff(onOff);
            TextField field = (TextField) scene.lookup("#percentageField");
            field.clear();
            TextField field2 = (TextField) scene.lookup("#voltagePercentBox");
            field2.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void manageYaxis(NumberAxis yAxis) {
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
    }

    private void manageXaxis(NumberAxis xAxis, int bound) {
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false); // Peab olema false. Muidu muudab ise graafiku laiust.
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);
        xAxis.setMinorTickVisible(false);
        xAxis.setTickUnit(bound/5);
    }

    private void manageLinechart(LineChart lineChart, XYChart.Series series) {
        lineChart.getData().addAll(series);
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);
        lineChart.setHorizontalGridLinesVisible(true);
        lineChart.setVerticalGridLinesVisible(true);
        lineChart.setAnimated(false);
    }


    /**
     * Makes moving chart.
     *
     * @param scene Canvas for graphical interface.
     */
    private void makeMovingChart(Scene scene) {
        NumberAxis yAxis10 = new NumberAxis();
        NumberAxis yCurrentAxis10 = new NumberAxis();
        NumberAxis yAxis5 = new NumberAxis();
        NumberAxis yCurrentAxis5 = new NumberAxis();
        NumberAxis yAxis3 = new NumberAxis();
        NumberAxis yCurrentAxis3 = new NumberAxis();
        NumberAxis yAxis2 = new NumberAxis();
        NumberAxis yCurrentAxis2 = new NumberAxis();
        NumberAxis yAxis1 = new NumberAxis();
        NumberAxis yCurrentAxis1 = new NumberAxis();
        NumberAxis yAxis30 = new NumberAxis();
        NumberAxis yCurrentAxis30 = new NumberAxis();

        manageYaxis(yAxis10);
        manageYaxis(yCurrentAxis10);
        manageYaxis(yAxis5);
        manageYaxis(yCurrentAxis5);
        manageYaxis(yAxis3);
        manageYaxis(yCurrentAxis3);
        manageYaxis(yAxis2);
        manageYaxis(yCurrentAxis2);
        manageYaxis(yAxis1);
        manageYaxis(yCurrentAxis1);
        manageYaxis(yAxis30);
        manageYaxis(yCurrentAxis30);

        xAxis10 = new NumberAxis(0, 6000, 1);
        xAxis5 = new NumberAxis(0, 3000, 1);
        xAxis3 = new NumberAxis(0, 1800, 1);
        xAxis2 = new NumberAxis(0, 1200, 1);
        xAxis1 = new NumberAxis(0, 600, 1);
        xAxis30 = new NumberAxis(0, 300, 1);
        xCurrentAxis10 = new NumberAxis(0, 6000, 1);
        xCurrentAxis5 = new NumberAxis(0, 3000, 1);
        xCurrentAxis3 = new NumberAxis(0, 1800, 1);
        xCurrentAxis2 = new NumberAxis(0, 1200, 1);
        xCurrentAxis1 = new NumberAxis(0, 600, 1);
        xCurrentAxis30 = new NumberAxis(0, 300, 1);

        manageXaxis(xAxis10, 6000);
        manageXaxis(xAxis5, 3000);
        manageXaxis(xAxis3, 1800);
        manageXaxis(xAxis2, 1200);
        manageXaxis(xAxis1, 600);
        manageXaxis(xAxis30, 300);
        manageXaxis(xCurrentAxis10, 6000);
        manageXaxis(xCurrentAxis5, 3000);
        manageXaxis(xCurrentAxis3, 1800);
        manageXaxis(xCurrentAxis2, 1200);
        manageXaxis(xCurrentAxis1, 600);
        manageXaxis(xCurrentAxis30, 300);

        lineChart10 = new LineChart<>(xAxis10, yAxis10);
        lineChart5 = new LineChart<>(xAxis5, yAxis5);
        lineChart3 = new LineChart<>(xAxis3, yAxis3);
        lineChart2 = new LineChart<>(xAxis2, yAxis2);
        lineChart1 = new LineChart<>(xAxis1, yAxis1);
        lineChart30 = new LineChart<>(xAxis30, yAxis30);
        lineChart10.setUserData("linechart");
        lineChart5.setUserData("linechart");
        lineChart3.setUserData("linechart");
        lineChart2.setUserData("linechart");
        lineChart1.setUserData("linechart");
        lineChart30.setUserData("linechart");

        currentLineChart10 = new LineChart<>(xCurrentAxis10, yCurrentAxis10);
        currentLineChart5 = new LineChart<>(xCurrentAxis5, yCurrentAxis5);
        currentLineChart3 = new LineChart<>(xCurrentAxis3, yCurrentAxis3);
        currentLineChart2 = new LineChart<>(xCurrentAxis2, yCurrentAxis2);
        currentLineChart1 = new LineChart<>(xCurrentAxis1, yCurrentAxis1);
        currentLineChart30 = new LineChart<>(xCurrentAxis30, yCurrentAxis30);
        currentLineChart10.setUserData("currentLinechart");
        currentLineChart5.setUserData("currentLinechart");
        currentLineChart3.setUserData("currentLinechart");
        currentLineChart2.setUserData("currentLinechart");
        currentLineChart1.setUserData("currentLinechart");
        currentLineChart30.setUserData("currentLinechart");

        series10min = new XYChart.Series(FXCollections.observableArrayList());
        series5min = new XYChart.Series(FXCollections.observableArrayList());
        series3min = new XYChart.Series(FXCollections.observableArrayList());
        series2min = new XYChart.Series(FXCollections.observableArrayList());
        series1min = new XYChart.Series(FXCollections.observableArrayList());
        series30sec = new XYChart.Series(FXCollections.observableArrayList());

        current10min = new XYChart.Series(FXCollections.observableArrayList());
        current5min = new XYChart.Series(FXCollections.observableArrayList());
        current3min = new XYChart.Series(FXCollections.observableArrayList());
        current2min = new XYChart.Series(FXCollections.observableArrayList());
        current1min = new XYChart.Series(FXCollections.observableArrayList());
        current30sec = new XYChart.Series(FXCollections.observableArrayList());

        manageLinechart(lineChart10, series10min);
        manageLinechart(lineChart5, series5min);
        manageLinechart(lineChart3, series3min);
        manageLinechart(lineChart2, series2min);
        manageLinechart(lineChart1, series1min);
        manageLinechart(lineChart30, series30sec);
        manageLinechart(currentLineChart10, current10min);
        manageLinechart(currentLineChart5, current5min);
        manageLinechart(currentLineChart3, current3min);
        manageLinechart(currentLineChart2, current2min);
        manageLinechart(currentLineChart1, current1min);
        manageLinechart(currentLineChart30, current30sec);


        GridPane.setConstraints(currentLineChart10, 1, 1);
        GridPane.setConstraints(currentLineChart5, 1, 1);
        GridPane.setConstraints(currentLineChart3, 1, 1);
        GridPane.setConstraints(currentLineChart2, 1, 1);
        GridPane.setConstraints(currentLineChart1, 1, 1);
        GridPane.setConstraints(currentLineChart30, 1, 1);

        GridPane.setConstraints(lineChart10, 1, 0);
        GridPane.setConstraints(lineChart5, 1, 0);
        GridPane.setConstraints(lineChart3, 1, 0);
        GridPane.setConstraints(lineChart2, 1, 0);
        GridPane.setConstraints(lineChart1, 1, 0);
        GridPane.setConstraints(lineChart30, 1, 0);

        GridPane mainPane = (GridPane) scene.lookup("#chartPane");
        mainPane.getChildren().addAll(lineChart30, currentLineChart30);
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

    /**
     * Adds Arduino data to the chart.
     */
    private void addDataToSeries() {
        //JÄRGMINE ON ARDUINO KOOD
        if (arduinoData.isEmpty()) {
            return;
        }
        while (!arduinoData.isEmpty()) {
            String androidData = arduinoData.pop();
            Number measurement = Integer.parseInt(androidData.split(" ")[1]);
            if (Integer.valueOf(String.valueOf(measurement)) > 40000 && Integer.valueOf(String.valueOf(measurement)) < 10000000) {
                if (isHighVoltage) {
                    debugWriter.println(measurement);
                }
                long current = Long.parseLong(androidData.split(" ")[2]);
                if (current > 2147483647L) {
                    current = current - 4294967295L;
                }
                TextField currentField = (TextField) scene.lookup("#currentAmper");
                long currentValue = Math.round(current/256.0);
                currentValueString = String.valueOf(currentValue);
                currentField.setText(currentValueString);
                double voltagePercent = Double.parseDouble(androidData.split(" ")[4]);
                TextField percentageField = (TextField) scene.lookup("#voltagePercentBox");
                percentageField.setText(String.valueOf(Math.round((127.0-voltagePercent)/1.27)));
                TextField textField = (TextField) scene.lookup("#androidData");
                if (androidData.length() - androidData.replace("C", "").length() == 1) {
                    textField.setText(androidData);
                }
                if (isStarted) {
                    if (counter > COUNTER_BLOCKING_NUMBER) {
                        counter += 1;
                        xSeriesData += 0.5;
                        size = counter - COUNTER_BLOCKING_NUMBER;
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
                        changeXaxis(xAxis10, 6000);
                        changeXaxis(xAxis5, 3000);
                        changeXaxis(xAxis3, 1800);
                        changeXaxis(xAxis2, 1200);
                        changeXaxis(xAxis1, 600);
                        changeXaxis(xAxis30, 300);
                        changeXaxis(xCurrentAxis10, 6000);
                        changeXaxis(xCurrentAxis5, 3000);
                        changeXaxis(xCurrentAxis3, 1800);
                        changeXaxis(xCurrentAxis2, 1200);
                        changeXaxis(xCurrentAxis1, 600);
                        changeXaxis(xCurrentAxis30, 300);
                    } else {
                        counter += 1;
                    }
                }
            }
        }
        if (arduinoData.isEmpty()) {
            return;
        }
    }

    private void changeXaxis(NumberAxis xAxis, int bound) {
        xAxis.setUpperBound((int)(size/2.0));
        xAxis.setLowerBound((int)(size/2.0 - bound));
        xAxis.setTickUnit(bound/5);
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        launch(args);
    }

}
