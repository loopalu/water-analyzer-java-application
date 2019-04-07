package gui;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

public class Test {
    public static void main(String[] args) {
        String[] output = {"Q", "0"};
        System.out.println(to_byte(output));
    }

    private static byte[][] to_byte(String[] strings) {
        byte[][] params = new byte[strings.length][];
        for (int i=0;  i<params.length;  i++) {
            params[i] = strings[i].getBytes();
        }
        return params;
    }
}
