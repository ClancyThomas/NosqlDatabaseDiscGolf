import java.io.*;
import java.util.ArrayList;

import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.values.MapValue;

public class DataHandler {

    CloudActions databaseWorker;

    public DataHandler(NoSQLHandle handle) {
        databaseWorker = new CloudActions(handle);
    }

    public void uploadCsvFileToDatabase(String fileName, String letter, String course, String week, String year, String format) {
        String csvLine = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            Integer count = 0;
            reader.readLine(); // This will skip the first line which contains header info

            while((csvLine = reader.readLine()) != null) {
                String[] playerData = csvLine.split(",");

                if(!playerData[1].contains("DNF")) { // Don't use the data if the player did not finish the round (DNF)
                    String[] player = prepareDataForUpload(playerData, course, week, year, format);
                    uploadToDatabase(player, letter+count);
                    count++;
                }
            }
        } catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public String[] prepareDataForUpload(String[] data, String course, String week, String year, String format) {
        String[] player = new String[8];

        String fullName = data[2];
        String cleanFullName = fullName.replaceAll("\"", "");
        String[] splitNames = cleanFullName.split(" ");

        player[0] = splitNames[0];
        if (splitNames.length > 1) // Some players will only have a first name
            player[1] = splitNames[1];
        else
            player[1] = "";
        player[2] = data[3];
        player[3] = data[4];
        player[4] = course;
        player[5] = week;
        player[6] = year;
        player[7] = format;

        return player;
    }

    public void uploadToDatabase(String[] player, String key) {
        int scoreRelativeToPar = 0;
        int scoreTotal = 0;
        int week = 0;
        int year = 0;

        try {
            scoreRelativeToPar = Integer.parseInt(player[2]);
            scoreTotal = Integer.parseInt(player[3]);
            week = Integer.parseInt(player[5]);
            year = Integer.parseInt(player[6]);

        }  catch (NumberFormatException e) {
            e.printStackTrace();
        }

        databaseWorker.addRow(key, player[0], player[1], scoreRelativeToPar, scoreTotal, player[4], week, year);

    }

    public void writeQueryToConsole(ArrayList<MapValue> results) {
        System.out.println("Query Result:");
        for (MapValue res : results) {
            System.out.println("\t" + res);
        }
    }

    public void writeQueryToTextFile(ArrayList<MapValue> results) {
        String fileName = "results.txt";

        try {
            File printFile = new File(fileName);
            if (printFile.createNewFile()) {
                System.out.println("New file created "+printFile.getName());
            } else {
                System.out.println("This file already exists.");
            }
        } catch (IOException e) {
            System.out.println("Error creating a file!");
            e.printStackTrace();
        }

        try {
            FileWriter fileWriter = new FileWriter(fileName);
            fileWriter.write("Query Result: ");
            for (MapValue res : results) {
                fileWriter.write("\n" + res);
            }
            fileWriter.close();
            System.out.println("Finished a successful write to the text file.");
        } catch (IOException e) {
            System.out.println("An error occurred trying to write to the file!");
            e.printStackTrace();
        }
    }

    // Currently unused, but this function can be used to quickly upload a lot of files - [INSERT]
    public void uploadListOfCsvFiles(String[] fileNames) {
        for(int i = 0; i<fileNames.length; i++) {
            uploadCsvFileToDatabase(fileNames[i], i+"a", "Course Name", String.valueOf(i+1), "Year", "Format");
        }
    }
}
