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

    public static int numRunsLoaded = 0;
    public static int autoLoaded = 0;
    public static int drawLoaded = 1;
    public static String simMatrixFilename = "Simulation_Matrix.xlsx";  // Default to the regular one
    public static String scenarioName = "Arahama1";

    public static String getSimMatrixFilename() { return simMatrixFilename; }
    public static String getScenarioName() { return scenarioName; }
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

    private static String strConfigFilename = "config.txt";
    
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
        Utils.log("numRunsLoaded: " + numRunsLoaded + ", autoLoaded: " + autoLoaded);
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
}