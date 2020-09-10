
package dronelab;

//import java.io.File;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

import dronelab.collidable.*;
import dronelab.utils.*;

// Reads all the data in the output/data folder and compiles it into
// a single file that includes averages of all the corresponding simulation
// runs as well as lists all the data for corresponding runs.

// UPDATE I DO NOT THINK I USE THIS CLASS AT ALL ANYMORE!!!!!
public final class DataCompiler {

    private static ArrayList<SimDataSet> dataSets;

    private DataCompiler() { // private constructor
    }    

    /* 
    So we want this to generate a file that looks like this:
    sc_10uav_650ppl_240min
    Algorithm: Scatter
    Drones: 10
    Survivors: 650
    SimTimeLimit: 4:00:00
    MaxCameraCoverage: 
    97.4%  (633/650)
    94.4%  (x/650)
    96.0%  (y/650)
    0:01:50 0:10:10 0:28:50 0:37:26 0:39:23 0:42:06 0:48:50 0:56:16 1:12:30 
    0:01:50 0:10:10 0:28:50 0:37:26 0:39:23 0:42:06 0:48:50 0:56:16 1:12:30 
    0:01:50 0:10:10 0:28:50 0:37:26 0:39:23 0:42:06 0:48:50 0:56:16 1:12:30 
    Average:
    95.2%
    0:01:50 0:10:10 0:28:50 0:37:26 0:39:23 0:42:06 0:48:50 0:56:16 1:12:30 
    MaxFINDERCoverage: 
    85.4%  (555/650)
    80.8%  (x/650)
    78.2%  (y/650)
    0:05:00 0:25:03 0:38:50 0:45:30 0:48:50 0:54:36 1:19:30 2:34:33 
    0:05:00 0:25:03 0:38:50 0:45:30 0:48:50 0:54:36 1:19:30 2:34:33 
    0:05:00 0:25:03 0:38:50 0:45:30 0:48:50 0:54:36 1:19:30 2:34:33 
    Average:
    82.2%
    0:05:00 0:25:03 0:38:50 0:45:30 0:48:50 0:54:36 1:19:30 2:34:33 
    */

    private static void addToDataSets(SimData data) {
         // Either find a matching data set to add to,
        for (SimDataSet ds : dataSets) {
            if (ds.add(data) == true)
                return;
        }

        // Or, if none exist, create a new one.
        SimDataSet dataSet = new SimDataSet();
        dataSets.add(dataSet);
        dataSet.add(data);
    }

    public static void compileData() {
        if (dataSets == null) {
             dataSets = new ArrayList<SimDataSet>();
        }
        dataSets.clear();

        File dir = new File(Constants.DATA_SAVE_PATH);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.exists() == false || 
                    child.isFile() == false ||
                    child.isDirectory() == true)
                    continue;
                // Do something with child
                String content = Utils.readFile("/" + child.getPath());
                SimData data = new SimData(content);
                addToDataSets(data);
            }
        }

        recordData();
    }

    private static void recordData() {
        for (SimDataSet dataSet : dataSets) {
            String filename = dataSet.getFilename();
            //Utils.print("---------------------------------");
            //Utils.log("Filename: " + filename);
            Utils.writeFile(dataSet.toString(), Constants.DATASET_SAVE_PATH + filename);
        }
    }

    // So first, read all the files in the folder, looking for common
    // names.  Load up all the data for each then average them and print /
    // append to a new file.
    /*public static String getSimDataFilename() {
        String filename = "";

        String strDate = new SimpleDateFormat("'('yyyy-MM-dd'_'HH-mm-ss').txt'").format(new Date());

        // Filename based on various parameters
        SimParams params = scenario.simParams;

        // Start with the algorithm so we can easily sort by name.
        if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD)
            filename += "st_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL)
            filename += "sp_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER)
            filename += "sc_"; 
        else
            filename += "nd_"; 

        filename += params.getNumDrones() + "uav_"; 
        filename += scenario.getNumVictims() + "ppl_"; 
        filename += params.getTimeLimitMinutes() + "min_"; 
        filename += strDate;
        return filename;
    }

    // Record the data for one sim run
    public static void recordSimData() {
        String filename = getSimDataFilename();
        String strSeen = vBoxCurrentData.getSeenBenchmarkData();
        String strLocated = vBoxCurrentData.getLocatedBenchmarkData();
        String strMaxSeen = vBoxCurrentData.getMaxSeen();
        String strMaxLocated = vBoxCurrentData.getMaxLocated();

        SimParams params = scenario.simParams;
        String str = "";
        
        if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD)
            str += "Algorithm: Standard"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL)
            str += "Algorithm: Spiral"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER)
            str += "Algorithm: Scatter"; 
        else
            str += "Algorithm: NotDefined"; 
        str += "\r\n"; 
        
        str += "RealSeconds: " + scenario.getLastRunSeconds();
        str += "\r\n"; 
        str += "Drones: " + params.getNumDrones();
        str += "\r\n"; 
        str += "Survivors: " + scenario.getNumVictims();
        str += "\r\n"; 
        str += "SimTimeLimit: " + params.getTimeLimitSeconds();
        str += "\r\n"; 
        str += "SimTimeStoppedAt: " + scenario.simTime.toString();
        str += "\r\n"; 
        str += "MaxCameraCoverage: " + strMaxSeen;
        str += "\r\n"; 
        str += "MaxFINDERCoverage: " + strMaxLocated;
        str += "\r\n"; 
        str += strSeen;
        str += "\r\n"; 
        str += strLocated;

        // Log it so we can see too.
        Utils.print("---------------------------------");
        Utils.log("Filename: " + filename);
        Utils.log(str);

        Utils.writeFile(str, Constants.DATA_SAVE_PATH + filename);
    }*/
}