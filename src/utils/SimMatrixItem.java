package dronelab.utils;

import java.util.ArrayList;

import dronelab.*;

// This contains the data from the simulation matrix to tell us which sims to run using which parameters
public class SimMatrixItem {

    SimMatrixItem(int num) { setNumRepetitions(num); }

    private int simulationNum = -1;
    private int relayNum = 0;
    private int socialNum = 0;
    private int antiNum = 0;
    private double wifiRange = 1.0;
    private double QoK = 1.0;

    private boolean simulationNumLoaded = false;
    private boolean relayNumLoaded = false;
    private boolean socialNumLoaded = false;
    private boolean antiNumLoaded = false;
    private boolean wifiRangeLoaded = false;
    private boolean QoKLoaded = false;

    // Booleans to check which things we actually loaded, and what is just default

    private int numRepetitions = Constants.NUM_MATRIX_REPETITIONS;
    public int getNumRepetitions() { return numRepetitions; }
    public void setNumRepetitions(int num) { 
        numRepetitions = num; 
        // Let's copy over our data so we don't wipe everything out if we call
        // the set function a few times.  If we want to wipe this out we'll just
        // remove the item entirely from the matrix and create a new one.
        int[] newSecondsTakenCameraArray = new int[numRepetitions]; 
        int[] newSecondsTakenFINDERArray = new int[numRepetitions]; 
        if (secondsTakenCameraArray != null) {
            for (int i = 0; i < secondsTakenCameraArray.length && i < newSecondsTakenCameraArray.length; i++) {
                newSecondsTakenCameraArray[i] = secondsTakenCameraArray[i];
            }
        }
        if (secondsTakenFINDERArray != null) {
            for (int i = 0; i < secondsTakenFINDERArray.length && i < newSecondsTakenFINDERArray.length; i++) {
                newSecondsTakenFINDERArray[i] = secondsTakenFINDERArray[i];
            }
        }
        
        secondsTakenCameraArray = newSecondsTakenCameraArray;
        secondsTakenFINDERArray = newSecondsTakenFINDERArray;
    }

    // One entry for each reptition; could create this on the fly later based on the simulation matrix
    public int[] secondsTakenCameraArray = null; //new int[numRepetitions];  // How long did it actually take to do this run?  We can record it here then save it out
    public int[] secondsTakenFINDERArray = null; //new int[numRepetitions];  // How long did it actually take to do this run?  We can record it here then save it out

    public void setSimulationNum(int num)   { simulationNum = num;  simulationNumLoaded = true; }
    public void setRelayNum(int num)        { relayNum = num;       relayNumLoaded = true; }
    public void setSocialNum(int num)       { socialNum = num;      socialNumLoaded = true; }
    public void setAntiNum(int num)         { antiNum = num;        antiNumLoaded = true; }
    public void setWifiRange(double num)    { wifiRange = num;      wifiRangeLoaded = true; }
    public void setQoK(double num)          { QoK = num;            QoKLoaded = true; }

    public int getSimulationNum()   { return simulationNum; }
    public int getRelayNum()        { return relayNum; }
    public int getSocialNum()       { return socialNum; }
    public int getAntiNum()         { return antiNum; }
    public double getWifiRange()    { return wifiRange; }
    public double getQoK()          { return QoK; }
    
    public boolean getSimulationNumLoaded() { return simulationNumLoaded; }
    public boolean getRelayNumLoaded()      { return relayNumLoaded; }
    public boolean getSocialNumLoaded()     { return socialNumLoaded; }
    public boolean getAntiNumLoaded()       { return antiNumLoaded; }
    public boolean getWifiRangeLoaded()     { return wifiRangeLoaded; }
    public boolean getQoKLoaded()           { return QoKLoaded; }

    public int getSecondsTakenCamera(int index) { return secondsTakenCameraArray[index]; }
    public void setSecondsTakenCamera(int index, int sec) { secondsTakenCameraArray[index] = sec; }
    public int getSecondsTakenFINDER(int index) { return secondsTakenFINDERArray[index]; }
    public void setSecondsTakenFINDER(int index, int sec) { secondsTakenFINDERArray[index] = sec; }

    /*public void copyTo(SimMatrixItem item) {
        item.simulationNum = this.simulationNum;
        item.relayNum = this.relayNum;
        item.standardNum = this.standardNum;
        item.antiNum = this.antiNum;
    }
    
    public SimMatrixItem dupe() {
        SimMatrixItem item = new SimMatrixItem();
        copyTo(item);
        return item;
    }*/

    public String toString() {
        String strRetv = "";

        strRetv += "#: " + simulationNum + " (" + simulationNumLoaded + ")"; 
        strRetv += ", NumRelays: " + relayNum + " (" + relayNumLoaded + ")"; 
        strRetv += ", NumSocs: " + socialNum + " (" + socialNumLoaded + ")"; 
        strRetv += ", NumAntis: " + antiNum + " (" +antiNumLoaded + ")"; 
        strRetv += ", WifiRange: " + wifiRange + " (" + wifiRangeLoaded + ")"; 
        strRetv += ", QoK: " + QoK + " (" + QoKLoaded + ")"; 
        strRetv += "\r\n";
        if (secondsTakenCameraArray[0] > 0) {
            strRetv += ", CAM SECONDS TAKEN: ";
            for (int i = 0; i < numRepetitions; i++) {
                strRetv += "" + secondsTakenCameraArray[i] + ", ";
            }
        }
        strRetv += "\r\n";
        if (secondsTakenFINDERArray[0] > 0) {
            strRetv += ", FINDER SECONDS TAKEN: ";
            for (int i = 0; i < numRepetitions; i++) {
                strRetv += "" + secondsTakenFINDERArray[i] + ", ";
            }
        }

        return strRetv;
    }
}