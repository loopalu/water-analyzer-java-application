package main.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class for reading file.
 */
public class FileManager {

    /**
     * Returns data from txt file.
     *
     * @return String of data.
     */
    public static String readFile(String path) {
        StringBuilder data = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNextLine()) {
                data.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No offlineData.txt file.");
        }
        return data.toString();
    }

    /**
     * Reads integers from the list. Used for testing the database connection.
     *
     * @return List of integers used for testing the database connection.
     */
    public static ArrayList<Integer> readIntegers() {
        ArrayList<Integer> data = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("debug.txt"));
            while (scanner.hasNextLine()) {
                data.add(Integer.valueOf(scanner.nextLine()));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("No debug.txt file.");
        }
        return data;
    }

    /**
     * Hides given file.
     *
     * @param fileName Name of file to be hidden.
     */
    public static void hide(String fileName) {
        Path path = Paths.get(fileName);
        try {
            Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS); //< set hidden attribute
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
