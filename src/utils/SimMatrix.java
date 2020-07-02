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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import dronelab.*;

// This contains the data from the simulation matrix to tell us which sims to run using which parameters
public class SimMatrix {

    // I also could just extend ArrayList but anyway I'm doing it this way for now
    ArrayList<SimMatrixItem> matrixItems = new ArrayList<SimMatrixItem>();

    public SimMatrix() { }

    public ArrayList<SimMatrixItem> getItems() { return matrixItems; }

    private int numRepsPerItem = Constants.NUM_MATRIX_REPETITIONS;
    public void setNumRepetitionsPerItem(int num) {
        // If it's already the same, just ignore it.
        if (num == numRepsPerItem) {
            return;
        }

        numRepsPerItem = num;

        // If we have items, reset them to work properly
        if (matrixItems == null) {
            return;
        }
        
        // If this has reps already, this will wipe out the data in there
        for (SimMatrixItem item : matrixItems) {
            item.setNumRepetitions(num);
        }
    }

    public int size() { 
        return matrixItems.size(); 
    }
    public boolean isEmpty() { 
        return matrixItems.isEmpty(); 
    }
    public SimMatrixItem get(int index) {
        return matrixItems.get(index);
    }

    public String toString() {
        String strRetv = "";

        for (SimMatrixItem item : matrixItems) {
            strRetv += item.toString() + "\r\n";
        }

        return strRetv;
    }
    
    // Take an existing matrix and populate just the completed items
    // from the existing.
    public void populateCompletedItemsFrom(SimMatrix mat) {
        if (mat == null) {
            return;
        }

        // So go through and set any of our incomplete matrix items to
        // any that are complete in the passed in one.
        int index = 0;
        for (SimMatrixItem fromItem : mat.getItems()) {
            SimMatrixItem toItem = get(index);
            for (int i = 0; i < fromItem.getNumRepetitions() && i < toItem.getNumRepetitions(); i++) {
                toItem.setSecondsTakenCamera(i, fromItem.getSecondsTakenCamera(i));
            }
            index++;
        }
    }

    public void load(String fileName) {
        try {
            InputStream stream = ExcelUtils.class.getResourceAsStream(fileName);
            Workbook wb = new XSSFWorkbook(stream);
            Sheet sheet = wb.getSheetAt(0);
            Row row;
            Cell cell;

            int numRows = sheet.getPhysicalNumberOfRows();
            int numCols = 7; //ExcelUtils.getNumberOfColumns(wb, sheet);

            // Start at row 2
            int r = 1;
            for (; r <= numRows; r++) {
                row = sheet.getRow(r);
                if (row == null) 
                    continue;
                SimMatrixItem item = new SimMatrixItem(numRepsPerItem);
                matrixItems.add(item);
                for (int c = 0; c < numCols; c++) {
                    cell = row.getCell(c);
                    if (ExcelUtils.isCellEmpty(cell)) {
                        if (c == 1) { 
                            // Empty, remove it and move on
                            matrixItems.remove(item);
                            break;
                        }
                        continue;
                    }
                    switch (c) {
                        case 0:
                            item.simulationNum = ExcelUtils.getIntForCell(cell);
                            break;
                        case 1:
                            // Batch ID - we ignore this
                            break;
                        case 2:
                            item.relayNum = ExcelUtils.getIntForCell(cell);
                            break;
                        case 3:
                            item.socialNum = ExcelUtils.getIntForCell(cell);
                            break;
                        case 4:
                            item.antiNum = ExcelUtils.getIntForCell(cell);
                            break;
                        case 5:
                            // Total - we ignore this
                            break;
                        case 6:
                            item.wifiRange = ExcelUtils.getDoubleForCell(cell);
                            break;
                }
                } 
            }
            Utils.log(fileName + " successfully loaded.");
        } catch (Exception ioe) {
            Utils.log(fileName + " failed to load.");
            matrixItems.clear();
            //ioe.printStackTrace();
        }

        //Utils.log(toString());
    }
    
    // This loads up a set of comma-delimeted data that had previously been recorded
    // in the text, and is only the result values of each item; doesn't have any
    // configurations or anything
    public void loadPreviousDataFromText(String strData) {
        // What we'll do is, we will see if we have a matrix item at a certain index;
        // if we do, we'll populate that, but only if it's not already populated.
        // Basically we "fill in the blanks" with this loaded data.  And if we don't
        // have a matrix item, we create one and fill it in.
        String lines[] = strData.split("\\r?\\n");
        if (lines == null) {
            return;
        }
        int index = 0;
        for (String line : lines) {
            SimMatrixItem item;
            if (matrixItems.size() > index) {
                // We have an item then
                item = matrixItems.get(index);
            }
            else {
                item = new SimMatrixItem(numRepsPerItem);
                matrixItems.add(item);
            }

            // Now go through the lines
            String entries[] = line.split(",");
            int numReps = entries.length;
            if (numReps > this.numRepsPerItem) {
                this.setNumRepetitionsPerItem(numReps);
            }
            item.setNumRepetitions(numReps);
            int i = 0;
            for (String entry : entries) {
                int sec = Utils.tryParseInt(entry); // Let's hope this works every time
                item.setSecondsTakenCamera(i, sec);
                i++;
            }

            index++;
        }
    }
}