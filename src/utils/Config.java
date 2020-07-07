package dronelab.utils;

import java.util.Random;
import java.util.ArrayList;
//import javafx.scene.shape.*;
import java.io.*;
import java.nio.file.*;
import javax.json.JsonValue;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.stream.JsonParser;

import dronelab.SimParams;
import dronelab.utils.SimMatrixItem;


/*
Allowances for auto-running so that it doesn't get too laggy:
- save numRuns to config file
- if sim restarts itself, save auto 1 to config file
- Load from config file when program opens
- if "auto" is set to 1, then:
    - load up current_scenario.json
    - automatically start from numRuns rounded to nearest 10
- if "auto" is set to 0, then:
    - Start from scratch unless the user presses "Load Current Config" button.
    - if "Load Current Config" pressed, process it like auto was 1.
- Every 100 runs, save "auto 1" to config along with numRuns, start up
    a batch script that waits for 10 seconds, exit program, the batch script
    then just runs the program again
*/

// This is essentially like a static class in C#
public final class Config {
    private Config () { // private constructor
    }

    private static int numRunsLoaded = 0;
    private static int autoLoaded = 0;
    private static int drawLoaded = 1;
    private static String simMatrixFilename = "Simulation_Matrix.xlsx";  // Default to the regular one
    private static String scenarioName = "Arahama1";

    // This is just for the process monitor which also uses the Config class, not the main program
    private static long timeStampLoaded = 0;
    public static long getTimeStampLoaded() { return timeStampLoaded; }
    
    // This matrix is not fully populated; it only has the output data from the previous/current
    // experiment in it, it doesn't contain any of the configurations or anything.
    private static SimMatrix previousMatrix = null;
    
    private static String strConfigFilename = "config.txt";
    private static String strProcMonFilename = "procmon.txt";

    public static SimMatrix getPreviousMatrix() { return previousMatrix; }
    public static String getSimMatrixFilename() { return simMatrixFilename; }
    public static String getScenarioName() { return scenarioName; }
    public static int getNumRunsLoaded() { return numRunsLoaded; }
    public static boolean getDrawLoaded() { 
        if (drawLoaded != 0) { 
            return true;
        }
        return false;
    }
    public static boolean getAutoLoaded() { 
        if (autoLoaded != 0) { 
            return true;
        }
        return false;
    }
    
    public static void load() {
        String config = Utils.readFile(Constants.CONFIG_LOAD_PATH + strConfigFilename);
        if (config == null || config.length() <= 0) {
            // If we didn't have one just save out a blank one
            save();
            return;
        }
        String lines[] = config.split("\\r?\\n");
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            // If we wrote a matrix to the config file, use it, otherwise just default
            if (Utils.stringStartsWith(line, "Matrix: ") == true) {
                simMatrixFilename = line.substring(("Matrix: ").length(), line.length());
            }
            else if (Utils.stringStartsWith(line, "Scenario: ") == true) {
                scenarioName = line.substring(("Scenario: ").length(), line.length());
            }
            else if (Utils.stringStartsWith(line, "draw: ") == true) {
                drawLoaded = Utils.tryParseInt(line.substring(("draw: ").length(), line.length()));
            }
            else if (Utils.stringStartsWith(line, "numRuns: ") == true) {
                numRunsLoaded = Utils.tryParseInt(line.substring(("numRuns: ").length(), line.length()));
            }
            else if (Utils.stringStartsWith(line, "auto: ") == true) {
                autoLoaded = Utils.tryParseInt(line.substring(("auto: ").length(), line.length()));
            }
        }

        // OK if we auto-loaded, then auto-load previous data too.
        if (getAutoLoaded() == true) {
            loadPreviousData();
        }

        Utils.log("numRunsLoaded: " + numRunsLoaded + ", autoLoaded: " + autoLoaded);
    }

    private static void loadPreviousData() {
        // We will try to maintain the single-file list of times, even though it's not our primary
        // source of data which is now the list of text files.  The single file of times is still
        // useful for analysis using excel and getting an idea of which approaches worked the best.
        String data = Utils.readFile(Constants.DATA_LOAD_PATH + Constants.DATA_FILE_CAMERA);

        // Now we could either just try to paste this onto the beginning of the file, or actually
        // generate our simulation matrix based on the data.  I think let's actually regenerate the
        // matrix; we're continuing from where we left off because we loaded from an auto-load, so
        // it's better to have it in the matrix which would let us overwrite as needed.
        previousMatrix = new SimMatrix();
        previousMatrix.loadPreviousDataFromText(data);
    }

    public static void save() {
        save(0, false);
    }

    public static void save(int numRuns, boolean auto) {
        // Save config data
        String contents = "";
        
        contents += "Matrix: " + simMatrixFilename + "\r\n";
        contents += "Scenario: " + scenarioName + "\r\n";
        contents += "draw: " + drawLoaded + "\r\n" ;
        contents += "numRuns: " + numRuns + "\r\n" ;
        contents += "auto: ";
        if (auto == true) {
            contents += "1";
        }
        else {
            contents += "0";
        }
        Utils.writeFile(contents, Constants.CONFIG_SAVE_PATH + strConfigFilename);
    }

    // The following are for the process monitor which restarts the sim if it freezes
    public static void saveProcMonFile(int numRuns) {
        String contents = "";
        contents += "ts: " + System.currentTimeMillis() + "\r\n";
        contents += "numRuns: " + numRuns;

        Utils.writeFile(contents, Constants.CONFIG_SAVE_PATH + strProcMonFilename);
    }

    public static void loadProcMonFile() {
        String contents = Utils.readFile(Constants.CONFIG_LOAD_PATH + strProcMonFilename);
        if (contents == null || contents.length() <= 0) {
            return;
        }
        String lines[] = contents.split("\\r?\\n");
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            // If we wrote a matrix to the config file, use it, otherwise just default
            if (Utils.stringStartsWith(line, "ts: ") == true) {
                timeStampLoaded = Utils.tryParseLong(line.substring(("ts: ").length(), line.length()));
            }
            else if (Utils.stringStartsWith(line, "numRuns: ") == true) {
                numRunsLoaded = Utils.tryParseInt(line.substring(("numRuns: ").length(), line.length()));
            }
        }

        Utils.log("ts: " + timeStampLoaded + ", numRuns: " + numRunsLoaded);
    }
}