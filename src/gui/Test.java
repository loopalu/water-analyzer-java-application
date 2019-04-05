package gui;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Test {
    public static void main(String[] args) {
        System.out.println((int)'ए');
        String s = "एक गाव में एक किसान";
        try {
            String out = new String(s.getBytes("Windows-1252"), StandardCharsets.UTF_8);
            System.out.println(out);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
