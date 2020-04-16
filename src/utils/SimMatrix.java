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
                SimMatrixItem item = new SimMatrixItem();
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
}