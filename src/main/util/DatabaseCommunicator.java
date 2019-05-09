package main.util;

import main.Method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;

public class DatabaseCommunicator {
    private String apiAddress = "http://localhost:8080/";
    private String databaseAddress = "localhost";
    private JSONArray jsonArray;
    private HttpURLConnection con;

    public ArrayList<String> getMethodNames() {
        return null;
    }

    public HashMap<String,Method> getMethods() {
        return null;
    }

    public ArrayList<String> getAnalytes() {
        ArrayList<String> analytes = new ArrayList<>();
        getData(analytes, "getAnalytes");
        return analytes;
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
        //System.out.println(test.isApiAvailable()); //töötab
        //System.out.println(test.isDatabaseUp());
        ArrayList<String> analytes = test.getAnalytes();
        System.out.println(analytes);
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

    private boolean isApiAvailable(String string) { //sama asi läheb API sisse, et vaadata, kas andmebaas on üleval
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
