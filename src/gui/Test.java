package gui;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class Test {
    public static void main(String[] args) {
        ArduinoReader reader = new ArduinoReader();
        reader.initialize();
        Stack<String> arduinoData = reader.getData();
        while (true) {
            if (!arduinoData.isEmpty()) {
                System.out.println(arduinoData.pop());
            }
        }
    }
}
