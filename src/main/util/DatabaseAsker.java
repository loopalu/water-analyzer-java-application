package main.util;

import main.Method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseAsker {
    private String apiAddress = "http://123.123.123.123/api";

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

    public boolean isConnection() { //sama asi läheb API sisse, et vaadata, kas andmebaas on üleval
        InetAddress address;
        try {
            address = InetAddress.getByName(apiAddress);
        } catch (UnknownHostException e) {
            return false;
        }
        try {
            return address.isReachable(5000);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isDatabaseUp() {
        URL url = null;
        try {
            url = new URL(apiAddress+"/isDatabaseUp");
        } catch (MalformedURLException e) {
            return false;
        }
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return false;
        }
        try {
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            return Boolean.parseBoolean(content.toString());
        } catch (IOException e) {
            return false;
        }
    }
}
