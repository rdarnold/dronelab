package dronelab;

import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import dronelab.collidable.*;
import dronelab.utils.*;

// Basically, the data generated from one run of a simulation.


// UPDATE I DO NOT THINK I USE THIS CLASS AT ALL ANYMORE!!!
public class SimData {
    
    // How many data sets is this an average of?  This lets us average in other
    // data sets and keep a running total.  Or I could have a SimDataSet class
    // which is a list of SimData objects and it can do functions like average
    // them all and such.  Maybe better.

    // This could potentially be designed with properties and such, and/or set up
    // to be saved out as XML.  Right now it's just data saving / loading as txt files.
    public SimParams.AlgorithmFlag algorithm = SimParams.AlgorithmFlag.STANDARD;
    public int numDrones = 0;
    public int numSurvivors = 0;
    public int timeLimitSeconds = 0;
    public double maxCameraCoverage = 0.0;
    public double maxFinderCoverage = 0.0;
    public ArrayList<TimeData> cameraBenchmarks = new ArrayList<TimeData>(); // 10%, 20%, etc.
    public ArrayList<TimeData> finderBenchmarks = new ArrayList<TimeData>();

    // This points to the original string that was read from file to create this SimData
    // object, if one exists.
    String strOriginalData;

    // Some keys to make parsing easier and more robust
    public static final String algorithmKey = "Algorithm: ";
    public static final String dronesKey = "Drones: ";
    public static final String survivorsKey = "Survivors: ";
    public static final String simTimeLimitKey = "SimTimeLimit: ";
    public static final String maxCameraCoverageKey = "MaxCameraCoverage: ";
    public static final String maxFinderCoverageKey = "MaxFINDERCoverage: ";
    public static final String cameraBenchmarksKey = "CameraBenchmarks: ";
    public static final String finderBenchmarksKey = "FINDERBenchmarks: ";

    public SimData() {}
    public SimData(String fromFileContents) {
        parseString(fromFileContents);
    }

    public void reset() {
        algorithm = SimParams.AlgorithmFlag.STANDARD;
        numDrones = 0;
        numSurvivors = 0;
        timeLimitSeconds = 0;
        maxCameraCoverage = 0.0;
        maxFinderCoverage = 0.0;
        cameraBenchmarks.clear();
        finderBenchmarks.clear();
    }

    // Check if it's a matching type of simulation run
    public boolean matches(SimData data) {
        return ((algorithm == data.algorithm) &&
                (numDrones == data.numDrones) &&
                (numSurvivors == data.numSurvivors) &&
                (timeLimitSeconds == data.timeLimitSeconds));
    }

    public String ls = System.getProperty("line.separator");
    public String getStringForKey(String key) {
        return (getStringForKey(strOriginalData, key));
    }
    public String getStringForKey(String str, String key) {
        int start = str.indexOf(key) + key.length();
        int end = str.indexOf(ls, start);
        return (str.substring(start, end));
    }

    // Take a string array and create a bunch of timedata objects
    // out of it
    public void interpretTimeDataFromParts(ArrayList<TimeData> list, String[] parts) {
        list.clear();
        for (String str : parts) {
            // If we somehow get a wrong string in there, just move on.
            // The length has to be at least 0:00:00 so 7 characters.
            //if (str.length() < 7) 
            if (TimeData.validTimeStr(str) == false)
                continue;
            TimeData time = new TimeData(str);
            list.add(time);
        }
    }

    // Look through a string containing the relevant data and load it into
    // our parameters.
    public void parseString(String str) {
        strOriginalData = str;

        String cur;

        // Does this work?  I don't know and don't think I use this class anymore anyway
        cur = getStringForKey(algorithmKey);
        for (SimParams.AlgorithmFlag flag : SimParams.AlgorithmFlag.values()) {
            if (cur.equals(flag.toString())) {
                Utils.log("Loaded: " + flag.toString());
                algorithm = flag;
                break;
            }
        }

        /*if (cur.equals(SimParams.AlgorithmFlag.STANDARD.toString())) {
            algorithm = SimParams.AlgorithmFlag.STANDARD;
        }
        else if (cur.equals(SimParams.AlgorithmFlag.SPIRAL.toString())) {
            algorithm = SimParams.AlgorithmFlag.SPIRAL;
        }
        else if (cur.equals(SimParams.AlgorithmFlag.SCATTER.toString())) {
            algorithm = SimParams.AlgorithmFlag.SCATTER;
        }
        else if (cur.equals(SimParams.AlgorithmFlag.MIX_SRA.toString())) {
            algorithm = SimParams.AlgorithmFlag.MIX_SRA;
        }*/

        cur = getStringForKey(dronesKey);
        numDrones = Utils.tryParseInt(cur);

        cur = getStringForKey(survivorsKey);
        numSurvivors = Utils.tryParseInt(cur);

        cur = getStringForKey(simTimeLimitKey);
        timeLimitSeconds = Utils.tryParseInt(cur);

        // Has a % sign at the end so we need to parse the percent not just
        // the straight up number.
        cur = getStringForKey(maxCameraCoverageKey);
        maxCameraCoverage = Utils.tryParseDoublePercent(cur);

        cur = getStringForKey(maxFinderCoverageKey);
        maxFinderCoverage = Utils.tryParseDoublePercent(cur);

        // I can now use the keys moving forward.
        int start = str.indexOf(cameraBenchmarksKey) + cameraBenchmarksKey.length();
        // Now find the end of that line
        start = str.indexOf(ls, start);
        start++;
        int end = str.indexOf(ls, start);
        cur = str.substring(start, end);
        String[] parts = cur.split(" "); // String array, each element is text between spaces
        interpretTimeDataFromParts(cameraBenchmarks, parts);

        // Now the next one.
        start = str.indexOf(finderBenchmarksKey) + finderBenchmarksKey.length();
        start = str.indexOf(ls, start);
        start++;
        cur = str.substring(start, str.length());
        //Utils.log(cur);
        parts = cur.split(" "); 
        interpretTimeDataFromParts(finderBenchmarks, parts);

        // And the last two lines are the percentages of data for camera
        // and finder coverage.
        // So I'd like to use the new key but as I don't have that
        // yet I'll just find the last two lines of the file the "hard way"
        /*int start = str.indexOf(maxFinderCoverageKey) + maxFinderCoverageKey.length();
        // Now find the end of that line
        start = str.indexOf(ls, start);
        start++;
        int end = str.indexOf(ls, start);
        // Ok theoretically this is the start of the next line?
        cur = str.substring(start, end);
        // So this is correct and has all correct times on it.
        //Utils.log(cur);
        String[] parts = cur.split(" "); // String array, each element is text between spaces
        interpretTimeDataFromParts(cameraBenchmarks, parts);

        // Now the next one.
        start = end + 1;
        cur = str.substring(start, str.length());
        //Utils.log(cur);
        parts = cur.split(" "); 
        interpretTimeDataFromParts(finderBenchmarks, parts);*/
    }

    // Header info is common for any simulation run using these parameters
    public String headerToString() {
        StringBuilder str = new StringBuilder();
        
        str.append(algorithmKey + algorithm.toString()); 
        str.append(ls);
        str.append(dronesKey + numDrones);
        str.append(ls);
        str.append(survivorsKey + numSurvivors);
        str.append(ls);
        str.append(simTimeLimitKey + timeLimitSeconds);
        str.append(ls);
        return str.toString();
    }

    public String cameraBenchmarksToString() {
        return (timeArrayToString(cameraBenchmarks));
    }
    public String finderBenchmarksToString() {
        return (timeArrayToString(finderBenchmarks));
    }

    public String timeArrayToString(ArrayList<TimeData> timeArray) {
        StringBuilder str = new StringBuilder();
        for (TimeData time : timeArray) {
            // Write it out as a proper time string.
            str.append(time.toString() + " ");
        }
        return str.toString();
    }

    // Content info is unique to this one run - the exact timings and
    // percentiles that the run produced
    public String contentToString() {
        StringBuilder str = new StringBuilder();
        
        str.append(maxCameraCoverageKey + maxCameraCoverage);
        str.append(ls);
        str.append(maxFinderCoverageKey + maxFinderCoverage);
        str.append(ls);

        str.append(cameraBenchmarksKey);
        str.append(ls);

        String retv = str.toString();

        retv += cameraBenchmarksToString();
        retv += ls;
        retv += finderBenchmarksKey;
        retv += ls;
        retv += finderBenchmarksToString();

        /*for (TimeData time : cameraBenchmarks) {
            // Write it out as a proper time string.
            str.append(time.toString() + " ");
        }
        str.append(ls);
        str.append(finderBenchmarksKey);
        str.append(ls);
        for (TimeData time : finderBenchmarks) {
            // Write it out as a proper time string.
            str.append(time.toString() + " ");
        }
        return str.toString();*/
        return retv;
    }

    public String toString() {
        return (headerToString() + contentToString());
    }

    public String getFilename() {
        String filename = "";

        String strDate = new SimpleDateFormat("'('yyyy-MM-dd'_'HH-mm-ss').txt'").format(new Date());

        // Start with the algorithm so we can easily sort by name.
        if (algorithm == SimParams.AlgorithmFlag.STANDARD)
            filename += "st_"; 
        else if (algorithm == SimParams.AlgorithmFlag.SPIRAL)
            filename += "sp_"; 
        else if (algorithm == SimParams.AlgorithmFlag.SCATTER)
            filename += "sc_"; 
        else
            filename += "nd_"; 

        filename += numDrones + "uav_"; 
        filename += numSurvivors + "ppl_"; 
        filename += (timeLimitSeconds / 60) + "min_"; 
        filename += strDate;
        return filename;
    }

    // Record the data for one sim run
    public void save() {
        String filename = getFilename();
        /*String strSeen = vBoxCurrentData.getSeenBenchmarkData();
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
        Utils.log(str);*/

        Utils.writeFile(toString(), Constants.DATA_SAVE_PATH + filename);
    }  
}