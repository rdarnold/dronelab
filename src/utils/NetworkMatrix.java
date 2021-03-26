package dronelab.utils;

import java.util.List;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import java.io.*;
import java.nio.file.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import dronelab.*;
import dronelab.collidable.*;
import dronelab.utils.*;

// This class saves out the current connectivity of all the drones periodically as either a text file,
// or an Excel file.
public final class NetworkMatrix {
    private NetworkMatrix () { } // private constructor

    public static boolean[][] netMatrix; 

    public static int simSecondsBetweenSaves = 60; // Much faster than real-time when fast-forwarding

    private static void buildMatrix() {
        netMatrix = new boolean[DroneLab.scenario.drones.size()][DroneLab.scenario.drones.size()];

        int row = 0;
        int col = 0;
        double range = 0;
        for (Drone d : DroneLab.scenario.drones) {
            col = 0;
            range = d.getWiFiRangePixels();
            for (Drone d2 : DroneLab.scenario.drones) {
                if (Physics.withinDistance(d.getX(), d.getY(), d2.getX(), d2.getY(), range) == true) {
                    netMatrix[row][col] = true;
                }
                col++;
            }
            row++;
        }
    }

    // Save out the current state of all the drones in the scenario in terms of which ones are within
    // communication range of which other ones.
    public static void save() {
        buildMatrix();

        // Now save it out to whatever file format.  For now it is CSV.
        StringBuilder str = new StringBuilder();

        int size = DroneLab.scenario.drones.size();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (netMatrix[row][col] == true) {
                    str.append("1");
                }
                else {
                    str.append("0");
                }
                if (col < size-1) {
                    str.append(",");
                }
            }
            str.append("\r\n");
        }

        // Format is runNumber_timeStampInSeconds.csv
        String fname = "" + (DroneLab.runner.getCurrentRunNum() + 1) + "_" + DroneLab.scenario.simTime.getTotalSeconds() + ".csv";
        Utils.writeFile(str.toString(), Constants.NET_MATRIX_DATA_SAVE_PATH + fname);
    }
}