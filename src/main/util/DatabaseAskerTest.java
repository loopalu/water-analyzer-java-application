package main.util;

import main.Method;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;

public class DatabaseAskerTest {
    private String apiAddress = "http://localhost:8080/getOperators";
    private String databaseAddress = "localhost";
    JSONArray jsonArray;

    public ArrayList<String> getMethodNames() {
        return null;
    }

    public HashMap<String,Method> getMethods() {
        return null;
    }

    public ArrayList<String> getAnalytes() {
        return null;
    }

    public ArrayList<String> getMatrixes() {
        return null;
    }

    public ArrayList<String> getBge() {
        return null;
    }

    public static void main(String[] args) {
        DatabaseAskerTest test = new DatabaseAskerTest();
        System.out.println(test.isConnection()); //töötab
        //System.out.println(test.isDatabaseUp());
    }

    public boolean isConnection() { //sama asi läheb API sisse, et vaadata, kas andmebaas on üleval
        URL url;
        try {
            url = new URL(apiAddress);
        } catch (MalformedURLException e) {
            System.out.println(e);
            return false;
        }
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        try {
            con.connect();
            System.out.println(con.getResponseCode());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            String string = content.toString();
            jsonArray = new JSONArray(string);
            for (Object value:jsonArray) {
                System.out.println((String) value);
            }
            con.disconnect();
            return true;
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
