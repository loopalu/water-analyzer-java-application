package main.util;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.Analyte;
import main.LabTest;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import main.User;
import org.json.JSONArray;

/**
 * Makes connection to the REST API that deals with data and database.
 */
public class DatabaseCommunicator {
    private String apiAddress = "http://localhost:8080/";
    private String databaseAddress = "localhost";
    private JSONArray jsonArray;
    private HttpURLConnection con;

    /**
     * Gets users from REST API.
     *
     * @return HashMap of users.
     */
    public HashMap<String, Integer> getUsers() {
        HashMap<String, Integer> users = new HashMap<>();
        getData(users, "getUsers");
        return users;
    }

    /**
     * Gets methods from REST API.
     *
     * @return HashMap of methods (LabTest objects).
     */
    public HashMap<String, LabTest> getMethods() {
        HashMap<String, LabTest> methods = new HashMap<>();
        if (isApiAvailable("getMethods")) {
            makeConnection("getMethods");
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = null;
            StringBuilder content = new StringBuilder();
            while (true) {
                try {
                    assert in != null;
                    if ((inputLine = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content.append(inputLine);
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String jsonstring = content.toString();
            jsonArray = new JSONArray(jsonstring);
            for (int i = 0; i < jsonArray.length(); i++) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject jsonObject = gson.fromJson(String.valueOf(jsonArray.get(i)), JsonObject.class);
                LabTest method = makeMethod(jsonObject);
                methods.put(method.getNameOfMethod(), method);
            }
            con.disconnect();
        }
        return methods;
    }

    /**
     * Makes LabTest object with test data based on the JSON received from REST API.
     *
     * @param jsonObject JSON received from REST API.
     * @return LabTest object with test data.
     */
    private LabTest makeMethod(JsonObject jsonObject) {
        LabTest labTest = new LabTest();
        labTest.setNameOfTest(""); // Not important for method.
        labTest.setNameOfUser(""); // Not important for method.
        labTest.setUserClass(""); // Not important for method.
        labTest.setNameOfMethod(String.valueOf(jsonObject.get("nameOfMethod")).replace('"', ' ').trim());
        labTest.setMatrix(String.valueOf(jsonObject.get("matrix")).replace('"', ' ').trim());
        labTest.setCapillary(String.valueOf(jsonObject.get("capillary")).replace('"', ' ').trim());
        labTest.setCapillaryTotalLength(String.valueOf(jsonObject.get("capillaryTotalLength")).replace('"', ' ').trim());
        labTest.setCapillaryEffectiveLength(String.valueOf(jsonObject.get("capillaryEffectiveLength")).replace('"', ' ').trim());
        labTest.setFrequency(String.valueOf(jsonObject.get("frequency")).replace('"', ' ').trim());
        labTest.setInjectionMethod(String.valueOf(jsonObject.get("injectionMethod")).replace('"', ' ').trim());
        labTest.setInjectionChoice(String.valueOf(jsonObject.get("injectionChoice")).replace('"', ' ').trim());
        labTest.setInjectionChoiceValue(String.valueOf(jsonObject.get("injectionChoiceValue")).replace('"', ' ').trim());
        labTest.setInjectionChoiceUnit(String.valueOf(jsonObject.get("injectionChoiceUnit")).replace('"', ' ').trim());
        labTest.setInjectionTime(String.valueOf(jsonObject.get("injectionTime")).replace('"', ' ').trim().split(" ")[0]);
        labTest.setCurrent("-15 µA"); // Actually this is not important.
        labTest.setHvValue(String.valueOf(jsonObject.get("hvValue")).replace('"', ' ').trim().split(" ")[0]);

        ObservableList<Analyte> analytes = FXCollections.observableArrayList();
        JsonElement analyteJson = jsonObject.get("analytes");
        JsonArray jsonArray = analyteJson.getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject subObject = jsonArray.get(i).getAsJsonObject();
            String analyteName = String.valueOf(subObject.get("analyte").getAsJsonObject().get("value")).replace('"', ' ').trim();
            Integer analyteValue = Integer.valueOf(String.valueOf(subObject.get("concentration").getAsJsonObject().get("value")).replace('"', ' ').trim());
            Analyte analyte = new Analyte(analyteName, String.valueOf(analyteValue));
            analytes.add(analyte);
        }
        labTest.setAnalytes(analytes);

        labTest.setAnalyteUnit(String.valueOf(jsonObject.get("analyteUnit")).replace('"', ' ').trim());

        ObservableList<Analyte> bges = FXCollections.observableArrayList();
        JsonElement bgeJson = jsonObject.get("bge");
        JsonArray bgeArray = bgeJson.getAsJsonArray();
        for (int i = 0; i < bgeArray.size(); i++) {
            JsonObject subObject = bgeArray.get(i).getAsJsonObject();
            String bgeName = String.valueOf(subObject.get("analyte").getAsJsonObject().get("value")).replace('"', ' ').trim();
            Integer bgeValue = Integer.valueOf(String.valueOf(subObject.get("concentration").getAsJsonObject().get("value")).replace('"', ' ').trim());
            Analyte bge = new Analyte(bgeName, String.valueOf(bgeValue));
            bges.add(bge);
        }
        labTest.setBge(bges);

        labTest.setBgeUnit(String.valueOf(jsonObject.get("bgeUnit")).replace('"', ' ').trim());
        labTest.setDescription(String.valueOf(jsonObject.get("description")).replace('"', ' ').trim());
        labTest.setTestTime("00:00:23:231"); // Not important for method.
        labTest.setTestData(new ArrayList()); // Not important for method.
        return labTest;
    }

    /**
     * Sends data to the REST API.
     *
     * @param labTest All the data of test in the form of LabTest object.
     */
    public void postTest(LabTest labTest) {
        URL url = null;
        try {
            url = new URL(apiAddress+"postTest");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(labTest);
            String encodedString = Base64.getEncoder().encodeToString(json.getBytes());

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setInstanceFollowRedirects( false );
            con.setRequestMethod( "GET" );
            con.setRequestProperty( "Content-Type", "application/json");
            con.setRequestProperty( "charset", "utf-8");
            con.setRequestProperty( "Content-Length", Integer.toString( encodedString.getBytes().length ));
            con.setRequestProperty("Data", encodedString);
            con.setUseCaches( false );
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets analytes from REST API.
     *
     * @return Resturns list of analytes.
     */
    public ArrayList<String> getAnalytes() {
        ArrayList<String> analytes = new ArrayList<>();
        getData(analytes, "getAnalytes");
        return analytes;
    }

    /**
     * Asks for data from REST API.
     *
     * @param users Users of the desktop application.
     * @param string Name of the REST API address for database.
     */
    private void getData(HashMap<String, Integer> users, String string) {
        if (isApiAvailable(string)) {
            makeConnection(string);
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = null;
            StringBuilder content = new StringBuilder();
            while (true) {
                try {
                    assert in != null;
                    if ((inputLine = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content.append(inputLine);
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String jsonstring = content.toString();
            jsonArray = new JSONArray(jsonstring);
            for (int i = 0; i < jsonArray.length(); i++) {
                Gson gson = new Gson();
                User user = gson.fromJson(jsonArray.get(i).toString(), User.class);
                users.put(user.getName(), user.getUserClass());
            }
            con.disconnect();
        }
    }

    /**
     * Asks for data from REST API.
     *
     * @param elements List of the asked elements.
     * @param string Name of the REST API address for database.
     */
    private void getData(ArrayList<String> elements, String string) {
        if (isApiAvailable(string)) {
            makeConnection(string);
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String inputLine = null;
            StringBuilder content = new StringBuilder();
            while (true) {
                try {
                    assert in != null;
                    if ((inputLine = in.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content.append(inputLine);
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String jsonstring = content.toString();
            jsonArray = new JSONArray(jsonstring);
            for (Object value:jsonArray) {
                elements.add((String) value);
            }
            con.disconnect();
        }
    }

    /**
     * Asks for matrixes from REST API.
     *
     * @return List of matrixes.
     */
    public ArrayList<String> getMatrixes() {
        ArrayList<String> matrixes = new ArrayList<>();
        getData(matrixes, "getMatrixes");
        return matrixes;
    }

    /**
     * Asks for BGE-s from REST API.
     *
     * @return List of BGE-s.
     */
    public ArrayList<String> getBges() {
        ArrayList<String> bges = new ArrayList<>();
        getData(bges, "getBges");
        return bges;
    }

    /**
     * Reads integers from the list. Used for testing the database connection.
     *
     * @return List of integers used for testing the database connection.
     */
    private ArrayList<Integer> fileReader() {
        ArrayList<Integer> data = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("andmed.txt"));
            while (scanner.hasNextLine()) {
                data.add(Integer.valueOf(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Tests the database connection.
     *
     * @param args Given arguments.
     */
    public static void main(String[] args) {
        DatabaseCommunicator test = new DatabaseCommunicator();
//        HashMap<String,LabTest> methods = test.getMethods();
//        System.out.println(methods.get("mingi labTest").getMatrix());
        LabTest labTest = new LabTest();
        ArrayList testData = test.fileReader();
        DatabaseCommunicator.testSendingToDatabase(test, labTest, testData);
    }

    /**
     * Fakes the test, makes LabTest object and sends it to REST API.
     *
     * @param databaseCommunicator Communicator with REST API.
     * @param labTest Fake lab test data in the form of LabTest object.
     * @param testData Fake list of integers.
     */
    private static void testSendingToDatabase(DatabaseCommunicator databaseCommunicator, LabTest labTest, ArrayList testData) {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MMMM_yyyy_HH_mm");
        Date resultdate = new Date(time);
        String timeStamp = sdf.format(resultdate);
        labTest.setNameOfTest(timeStamp);
        labTest.setNameOfUser("Aivar");
        labTest.setUserClass("1");
        labTest.setNameOfMethod("333");
        labTest.setMatrix("kraanivesi");
        labTest.setCapillary("50/150 mm");
        labTest.setCapillaryTotalLength("20 cm");
        labTest.setCapillaryEffectiveLength("10 cm");
        labTest.setFrequency("2 MHz");
        labTest.setInjectionMethod("Pressure");
        labTest.setInjectionChoice("Difference");
        labTest.setInjectionChoiceValue("20");
        labTest.setInjectionChoiceUnit("cm");
        labTest.setInjectionTime("5 s");
        labTest.setCurrent("-15 µA");
        labTest.setHvValue("87 %");
        labTest.setAnalyteUnit("cm");
        Analyte analyte1 = new Analyte("K", "11");
        Analyte analyte2 = new Analyte("Nicotinamide", "50");
        ObservableList<Analyte> analytes = FXCollections.observableArrayList(analyte1, analyte2);
        labTest.setAnalytes(analytes);
        labTest.setAnalyteUnit("mol");
        Analyte analyte3 = new Analyte("His", "50");
        ObservableList<Analyte> bge = FXCollections.observableArrayList(analyte3);
        labTest.setBge(bge);
        labTest.setBgeUnit("ppb");
        labTest.setDescription("mingi test");
        labTest.setTestTime("00:00:23:231");
        labTest.setTestData(testData);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(labTest);
        System.out.println(json);
        databaseCommunicator.postTest(labTest);
    }

    /**
     * Makes connection to the REST API on given address.
     *
     * @param string REST API address.
     */
    private void makeConnection(String string) {
        URL url = null;
        try {
            url = new URL(apiAddress + string);
        } catch (MalformedURLException e) {
            System.out.println(e);
        }
        try {
            assert url != null;
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Checks if REST API is available.
     *
     * @param string Address of the REST API.
     * @return The boolean if API is available or not.
     */
    public boolean isApiAvailable(String string) {
        URL url;
        try {
            url = new URL(apiAddress + string);
        } catch (MalformedURLException e) {
            System.out.println(e);
            return false;
        }
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        try {
            con.connect();
            if (con.getResponseCode() == 200) {
                con.disconnect();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }
}
