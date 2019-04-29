package main;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {
    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MMMM_yyyy_HH_mm");
        Date resultdate = new Date(time);
        String timeStamp = sdf.format(resultdate);
        try {
            String current = new File( "." ).getCanonicalPath();
            File newDirectory = new File(current + "/" + timeStamp);
            boolean isCreated = newDirectory.mkdirs();
            if (isCreated) {
                System.out.printf("1. Successfully created directories, path:%s",
                        newDirectory.getCanonicalPath());
            } else if (newDirectory.exists()) {
                System.out.printf("1. Directory path already exist, path:%s",
                        newDirectory.getCanonicalPath());
            } else {
                System.out.println("1. Unable to create directory");
                return;
            }

            BufferedWriter writer;
            try {
                writer = new BufferedWriter(new FileWriter((current+"/" + timeStamp + File.separator + timeStamp + ".txt")));
                //writer = new BufferedWriter(new FileWriter(("bob" + ".txt")));
                writer.write("BGE:");
                writer.newLine();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            File newFile = new File(current+"/" + timeStamp + File.separator + timeStamp + ".txt");
//            //Create new file under specified directory
//            isCreated = newFile.createNewFile();
//            if (isCreated) {
//                System.out.printf("\n2. Successfully created new file, path:%s",
//                        newFile.getCanonicalPath());
//            } else { //File may already exist
//                System.out.printf("\n2. Unable to create new file");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
