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
    private String apiAddress = "http://localhost:8080/getAnalytes";
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
        if (isApiAvailable()) {
            makeConnection();
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
            String string = content.toString();
            jsonArray = new JSONArray(string);
            for (Object value:jsonArray) {
                analytes.add((String) value);
            }
            con.disconnect();
        }
        return analytes;
    }

    public ArrayList<String> getMatrixes() {
        return null;
    }

    public ArrayList<String> getBge() {
        return null;
    }

    public static void main(String[] args) {
        DatabaseCommunicator test = new DatabaseCommunicator();
        //System.out.println(test.isApiAvailable()); //töötab
        //System.out.println(test.isDatabaseUp());
        ArrayList<String> analytes = test.getAnalytes();
        System.out.println(analytes);
    }

    public void makeConnection() {
        URL url = null;
        try {
            url = new URL(apiAddress);
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

    public boolean isApiAvailable() { //sama asi läheb API sisse, et vaadata, kas andmebaas on üleval
        URL url;
        try {
            url = new URL(apiAddress);
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
