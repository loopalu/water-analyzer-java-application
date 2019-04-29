package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWritter {

    /**
     * Write file
     *
     * @param time Timestamp
     * @param id Request id
     * @throws IOException The exception when it is not possible to write into file
     */
    public static void write(String time, String id) throws IOException {
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter((time + ".txt")));
        writer.write(id);
        writer.newLine();
        writer.close();
    }
}
