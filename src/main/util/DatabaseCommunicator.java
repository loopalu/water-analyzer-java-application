package main.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.Analyte;
import main.Method;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import main.User;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseCommunicator {
    private String apiAddress = "http://localhost:8080/";
    private String databaseAddress = "localhost";
    private JSONArray jsonArray;
    private HttpURLConnection con;

    public ArrayList<String> getMethodNames() {
        return null;
    }

    public HashMap<String, Integer> getUsers() {
        HashMap<String, Integer> users = new HashMap<>();
        getData(users, "getUsers");
        return users;
    }

    public HashMap<String,Method> getMethods() {
        return null;
    }

    public void postMethod(Method method) {
        URL url = null;
        try {
            url = new URL(apiAddress+"postMethod");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(method);
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

    public ArrayList<String> getAnalytes() {
        ArrayList<String> analytes = new ArrayList<>();
        getData(analytes, "getAnalytes");
        return analytes;
    }

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

    public ArrayList<String> getMatrixes() {
        ArrayList<String> matrixes = new ArrayList<>();
        getData(matrixes, "getMatrixes");
        return matrixes;
    }

    public ArrayList<String> getBges() {
        ArrayList<String> bges = new ArrayList<>();
        getData(bges, "getBges");
        return bges;
    }

    public static void main(String[] args) {
        DatabaseCommunicator test = new DatabaseCommunicator();
        Method method = new Method();
        method.setNameOfTest("11 jaanuar");
        method.setNameOfUser("Aivar");
        method.setUserClass("1");
        method.setNameOfMethod("mingi method");
        method.setMatrix("kraanivesi");
        method.setCapillary("50/150 mm");
        method.setCapillaryTotalLength("20 cm");
        method.setCapillaryEffectiveLength("10 cm");
        method.setFrequency("2 MHz");
        method.setInjectionMethod("Pressure");
        method.setInjectionChoice("Difference");
        method.setInjectionChoiceValue("20");
        method.setInjectionChoiceUnit("cm");
        method.setInjectionTime("5 s");
        method.setCurrent("-15 µA");
        method.setHvValue("87 %");
        method.setAnalyteUnit("cm");
        Analyte analyte1 = new Analyte("raud", "11");
        Analyte analyte2 = new Analyte("sool", "100");
        ObservableList<Analyte> analytes = FXCollections.observableArrayList(analyte1, analyte2);
        method.setAnalytes(analytes);
        method.setAnalyteUnit("mol");
        Analyte analyte3 = new Analyte("MisMos", "100");
        ObservableList<Analyte> bge = FXCollections.observableArrayList(analyte3);
        method.setBge(bge);
        method.setBgeUnit("ppb");
        method.setDescription("mingi test");
        method.setTestTime("00:00:23:231");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(method);
        System.out.println(json);
        test.postMethod(method);
    }

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

    public boolean isApiAvailable(String string) { //sama asi läheb API sisse, et vaadata, kas andmebaas on üleval
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

    public boolean isDatabaseUp() {
        boolean ret = false;
        try {
            Socket s = new Socket(databaseAddress,5432);
            ret = true;
            s.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ret;
    }
}
