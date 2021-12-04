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

        writeToFile("UserId", "User Sex", "User Age", "", "");
        //writeToFile(userId, userSex, userAge, "", "");
        writeToFile("", "", "", "", "");
        writeToFile("Event", "Start Timestamp", "End Timestamp", "Start Zeit", "End Zeit");
    }

    public void writeToFile(String name, String startTimeStamp, String timeStamp, String startDateTime, String stopDateTime){
        if( new File(APPLICATION_DIRECTORY).exists() ){
            try {
                FileWriter fw = new FileWriter(file, true);
                String entry = name +","+ startTimeStamp +","+ timeStamp +","+ startDateTime +","+ stopDateTime +"\n";
                fw.append(entry);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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