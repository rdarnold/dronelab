package dronelab;

import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import dronelab.collidable.*;
import dronelab.utils.*;

// An entire set of SimData objects (simulation runs) that are all matching.
// UPDATE I DO NOT THINK I USE THIS CLASS AT ALL ANYMORE!!!!!
public class SimDataSet {
    public ArrayList<SimData> dataSet = new ArrayList<SimData>();

    // This object holds the averages of the data contained in dataSet
    private SimData avgData = new SimData();

    public boolean add(SimData data) {
        // If it's the first one, add it.
        if (dataSet.size() == 0) {
            dataSet.add(data);
            updateAverages();
            return true;
        }

        // Otherwise, check if it matches what we currently have. If so,
        // add it.  If not, reject.
        SimData current = dataSet.get(0);
        if (current.matches(data) == false)
            return false;
        dataSet.add(data);
        updateAverages();
        return true;
    }

    private int shortestCameraBenchmarkLength() {
        int shortest = dataSet.get(0).cameraBenchmarks.size();
        for (SimData data : dataSet) {
            if (data.cameraBenchmarks.size() < shortest)
                shortest = data.cameraBenchmarks.size();
        }
        return shortest;
    }

    private int shortestFinderBenchmarkLength() {
        int shortest = dataSet.get(0).finderBenchmarks.size();
        for (SimData data : dataSet) {
            if (data.finderBenchmarks.size() < shortest)
                shortest = data.finderBenchmarks.size();
        }
        return shortest;
    }

    private void updateAverages() {
        // Run through the list and update all our averages
        avgData.reset();

        for (SimData data : dataSet) {
            avgData.maxCameraCoverage += data.maxCameraCoverage;
            avgData.maxFinderCoverage += data.maxFinderCoverage;
        }
        avgData.maxCameraCoverage /= dataSet.size();
        avgData.maxFinderCoverage /= dataSet.size();

        // Keep the rounding the same 
        avgData.maxCameraCoverage = Utils.round1Decimal(avgData.maxCameraCoverage);
        avgData.maxFinderCoverage = Utils.round1Decimal(avgData.maxFinderCoverage);
        
        // Now, find the shortest length of time data array within
        // the set and create those number of time data elements for us.
        int shortest = shortestCameraBenchmarkLength();
        for (int i = 0; i < shortest; i++) {
            TimeData timeData = new TimeData();
            avgData.cameraBenchmarks.add(timeData);

            // At the same time, average in all the ones in the data set.
            for (SimData data : dataSet) {
                timeData.totalSeconds += data.cameraBenchmarks.get(i).totalSeconds;
            }
            timeData.totalSeconds /= dataSet.size();
            timeData.fromSeconds(timeData.totalSeconds);
        }

        shortest = shortestFinderBenchmarkLength();
        for (int i = 0; i < shortest; i++) {
            TimeData timeData = new TimeData();
            avgData.finderBenchmarks.add(timeData);

            // At the same time, average in all the ones in the data set.
            for (SimData data : dataSet) {
                timeData.totalSeconds += data.finderBenchmarks.get(i).totalSeconds;
            }
            timeData.totalSeconds /= dataSet.size();
            timeData.fromSeconds(timeData.totalSeconds);
        }
    }

    public String getFilename() {
        // Start with a filename from the SimData and revise from there.
        if (dataSet.size() == 0) {
            return "";
        }

        String filename = dataSet.get(0).getFilename();
        return ("DataSet_" + filename);
    }

    public String ls = System.getProperty("line.separator");
    public String toString() {
        String str;
        if (dataSet.size() == 0) {
            return "";
        }

     /* Algorithm: Scatter
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
        0:05:00 0:25:03 0:38:50 0:45:30 0:48:50 0:54:36 1:19:30 2:34:33 */

        // First we print a standard header as all header data is common
        // within a set.
        str = dataSet.get(0).headerToString();
        StringBuilder sb = new StringBuilder();
        // Now, we iterate through all the data, printing out each of
        // their data contents.  Then we print out averages as well.

        // First camera benchmarks
        sb.append(SimData.maxCameraCoverageKey);
        sb.append(ls);
        for (SimData data : dataSet) {
            sb.append(data.maxCameraCoverage);
            sb.append(ls);
        }
        sb.append(SimData.cameraBenchmarksKey);
        sb.append(ls);
        for (SimData data : dataSet) {
            sb.append(data.cameraBenchmarksToString());
            sb.append(ls);
        }
        // And the average:
        sb.append("Average" + SimData.maxCameraCoverageKey);
        sb.append(ls);
        sb.append(avgData.maxCameraCoverage);
        sb.append(ls);
        sb.append("Average" + SimData.cameraBenchmarksKey);
        sb.append(ls);
        sb.append(avgData.cameraBenchmarksToString());
        sb.append(ls);

        // Now do the same for finder benchmarks
        sb.append(SimData.maxFinderCoverageKey);
        sb.append(ls);
        for (SimData data : dataSet) {
            sb.append(data.maxFinderCoverage);
            sb.append(ls);
        }
        sb.append(SimData.finderBenchmarksKey);
        sb.append(ls);
        for (SimData data : dataSet) {
            sb.append(data.finderBenchmarksToString());
            sb.append(ls);
        }
        // And the average:
        sb.append("Average" + SimData.maxFinderCoverageKey);
        sb.append(ls);
        sb.append(avgData.maxFinderCoverage);
        sb.append(ls);
        sb.append("Average" + SimData.finderBenchmarksKey);
        sb.append(ls);
        sb.append(avgData.finderBenchmarksToString());

        Utils.log(str + sb.toString());
        return str + sb.toString();
    }
}