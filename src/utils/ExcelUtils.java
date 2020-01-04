package dronelab.utils;

import java.io.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public final class ExcelUtils {
    private ExcelUtils() { } // private constructor

    public static void readExcelFile(String fileName) {
        if (fileName.charAt(fileName.length()-1) == 'x') {
            readXlsxFile(fileName);
        } 
        else {
            readXlsFile(fileName);
        }
    }

    public static void readXlsFile(String fileName) {
        try {
            InputStream stream = ExcelUtils.class.getResourceAsStream(fileName);
            //FileInputStream stream =  new FileInputStream(fileName);
            POIFSFileSystem fs = new POIFSFileSystem(stream);
            Workbook wb = new HSSFWorkbook(fs);
            readExcelWorkbook(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readXlsxFile(String fileName) {
        try {
            InputStream stream = ExcelUtils.class.getResourceAsStream(fileName);
            //FileInputStream stream =  new FileInputStream(fileName);
            Workbook wb = new XSSFWorkbook(stream);
            readExcelWorkbook(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readExcelWorkbook(Workbook wb) {
        try {
            Sheet sheet = wb.getSheetAt(0);
            Row row;
            Cell cell;

            int rows; // No of rows
            rows = sheet.getPhysicalNumberOfRows();

            int cols = 0; // No of columns
            int tmp = 0;

            // This trick ensures that we get the data properly even if it doesn't 
            // start from first few rows
            for (int i = 0; i < 10 || i < rows; i++) {
                row = sheet.getRow(i);
                if (row != null) {
                    tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                    if (tmp > cols) cols = tmp;
                }
            }

            for (int r = 0; r <= rows; r++) {
                row = sheet.getRow(r);
                if (row != null) {
                    //JMPData jmpData = new JMPData();
                    for (int c = 0; c < cols; c++) {
                        cell = row.getCell(c);
                        if (cell != null) {
                            // Your code here
                            if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                                Utils.log(cell.getNumericCellValue());
                            } 
                            else  {
                                Utils.log(cell.getStringCellValue());
                            }
                        } 
                    }
                } 
            }
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    public static double getNumberForCell(Cell cell) {
        if (isCellEmpty(cell) == true) {
            return 0;
        }
        else if (cell.getCellTypeEnum() != CellType.NUMERIC) {
            return 0;
        }
        return cell.getNumericCellValue();
    }    

    public static int getIntForCell(Cell cell) {
        return (int)getNumberForCell(cell);
    }    

    public static boolean isCellEmpty(final Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return true;
        }

        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().isEmpty()) {
            return true;
        }

        return false;
    }

    public static int getNumberOfColumns(Workbook wb, Sheet sheet) {
        Row row;
        int numRows = sheet.getPhysicalNumberOfRows();
        int numCols = 0;

        int tmp = 0;

        // This trick ensures that we get the data properly even if it doesn't 
        // start from first few rows
        for (int i = 0; i < 10 || i < numRows; i++) {
            row = sheet.getRow(i);
            if (row != null) {
                tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                if (tmp > numCols) {
                    numCols = tmp;
                }
            }
        }
        return numCols;
    }

    public static int getRowForString(String strKey, Workbook wb, Sheet sheet) {
        Row row;
        Cell cell;
        int numRows = sheet.getPhysicalNumberOfRows();
        int numCols = getNumberOfColumns(wb, sheet);
        // So first iterate down to our first actual entry.
        int r = 0;
        for (r = 0; r <= numRows; r++) {
            row = sheet.getRow(r);
            if (row == null) 
                continue;
            for (int c = 0; c < numCols; c++) {
                cell = row.getCell(c);
                if (cell == null) 
                    continue;
                if (cell.getStringCellValue().equals(strKey)) {
                    return r;
                }
            }
        }
        return r;
    }


    public static void writeExcelFile(String fileName) {
        Workbook wb = new HSSFWorkbook();
        //Workbook wb = new XSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("new sheet");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(0);
        // Create a cell and put a value in it.
        Cell cell = row.createCell(0);
        cell.setCellValue(1);

        // Or do it on one line.
        row.createCell(1).setCellValue(1.2);
        row.createCell(2).setCellValue(
            createHelper.createRichTextString("This is a string"));
        row.createCell(3).setCellValue(true);

        try {
            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(fileName + ".xls");
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
}