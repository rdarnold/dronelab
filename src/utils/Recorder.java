package dronelab.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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

// Save each frame of the simulation and record it out to a file which can then be played back
// later to see what happened at any point in time.
public final class Recorder {
    private Recorder () { } // private constructor
    
    static StringBuilder strRecord = new StringBuilder();
    static StringBuilder strRecordHeader = new StringBuilder();

    // Assumptions:
    // - No UAV attrition / UAVs do not get destroyed or change IDs during mission
    // - Survivor locations do not change

    public static void startRecord(Scenario scen) {
        strRecordHeader = new StringBuilder();
        strRecord = new StringBuilder();

        // Start record with:
        // - Role of each UAV (for coloration)
        // - Locations of all survivors
        // - Timestamps of when each survivor is located (this is loaded into the player and used to show when a survivor is found)
        //   Actually I have this data already in the "seen" files, I can just use that same data for this.
        //   Timestamps cannot be recorded until after survivors are found obviously, so they aren't done until we save the record.

        // Set up the header
        strRecordHeader.append("Roles:\r\n");
        for (Drone d : scen.getDrones()) {
            strRecordHeader.append(d.getDroneRole().getValue());
            strRecordHeader.append("\r\n");
        }

        strRecordHeader.append("SurvivorLocations:\r\n");
        for (Person p : scen.getVictims()) {
            strRecordHeader.append(p.getX());
            strRecordHeader.append(" ");
            strRecordHeader.append(p.getY());
            strRecordHeader.append("\r\n");
        }
    }

    // Should this be serialized instead of saved as a string in order to handle
    // the potentially large amount of data better?
    public static void recordFrame(Scenario scen) {
        // Record every frame:
        // - Position of each UAV
        for (Drone d : scen.getDrones()) {
            strRecord.append(d.getX());
            strRecord.append(":");
            strRecord.append(d.getY());
            strRecord.append(",");
        }
        strRecord.append("\r\n");

        // Issue:  Can we actually assume 60 FPS or do we need time-stamps on these
        // records to sync them up with when survivors were rescued and how it plays back?
        // Or do we need to embed all the data into these frame by frame so that we input the
        // times that the survivors were rescued within the drone location data, such that timing
        // doesn't matter as much?
    }

    // Should I use json for this like I do with ScenarioLoader?
    public static void saveRecord(Scenario scen) {
        StringBuilder str = new StringBuilder();

        // Now do the timestamps for survivors; we don't know that until the "end"
        strRecordHeader.append("SurvivorFoundTimes:\r\n");
        strRecordHeader.append(DroneLab.getSurvivorFoundTimes());

        // Slap the header on
        str.append(strRecordHeader);
        str.append("\r\n");
        str.append(strRecord);

        // Format is runNumber_timeStampInSeconds.csv
        String fname =  "" + (DroneLab.runner.getCurrentRunNum() + 1) + 
                        "_pbdata" + 
                        ".txt";
        Utils.writeFile(str.toString(), Constants.PLAYBACK_DATA_SAVE_PATH + fname);
    }
}