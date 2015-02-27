package parser;

import javafx.scene.Scene;

import java.io.*;
import java.util.Scanner;

/**
 * Created by John McAvey on 2/26/2015.
 */
public class FileManager {
    Scene scene;
    public FileManager(Scene scene) {
        this.scene = scene;
    }

    public void writeToFile(File path, String Message){

        if (path != null){
            try {
                Writer writer = new BufferedWriter(new FileWriter(path.getAbsolutePath()));
                writer.write(Message);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFromFile(File path){
        try {

            Scanner scanner = new Scanner(new File(path.getAbsolutePath()));
            String lines = "";
            while(scanner.hasNextLine()){
                lines += scanner.nextLine() + "\n";
            }
            scanner.close();
            return lines;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
