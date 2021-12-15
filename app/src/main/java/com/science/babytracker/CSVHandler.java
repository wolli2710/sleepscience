package com.science.babytracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by wolfgangvogl on 14/05/15.
 */
public class CSVHandler extends FileWriteHandler {

    Long currentTimeStamp;

    CSVHandler(String dir){
        super();

        currentTimeStamp = System.currentTimeMillis()/1000;
        FILE_NAME = "BabyTracker_"+currentTimeStamp.toString()+".csv";
        APPLICATION_DIRECTORY = dir;
        createFile(APPLICATION_DIRECTORY, FILE_NAME);

        writeToFile("Event", "Timestamp", "Time");
    }

    public void writeToFile(String name, String startTimeStamp, String dateTime){
        if( new File(APPLICATION_DIRECTORY).exists() ){
            try {
                FileWriter fw = new FileWriter(file, true);
                String entry = name +","+ startTimeStamp + "," + dateTime + "\n";
                fw.append(entry);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}