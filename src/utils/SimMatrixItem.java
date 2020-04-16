package dronelab.utils;

import java.util.ArrayList;

import dronelab.*;

// This contains the data from the simulation matrix to tell us which sims to run using which parameters
public class SimMatrixItem {

    SimMatrixItem() { }

    public int simulationNum = 0;
    public int relayNum = 0;
    public int socialNum = 0;
    public int antiNum = 0;
    public double wifiRange = 1.0;

    // One entry for each reptition; could create this on the fly later based on the simulation matrix
    public int[] secondsTakenCameraArray = new int[10];  // How long did it actually take to do this run?  We can record it here then save it out
    public int[] secondsTakenFINDERArray = new int[10];  // How long did it actually take to do this run?  We can record it here then save it out

    public int getSimulationNum() { return simulationNum; }
    public int getRelayNum() { return relayNum; }
    public int getSocialNum() { return socialNum; }
    public int getAntiNum() { return antiNum; }
    public double getWifiRange() { return wifiRange; }

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

        strRetv += "#: " + simulationNum;
        strRetv += ", NumRelays: " + relayNum ;
        strRetv += ", NumSocs: " + socialNum;
        strRetv += ", NumAntis: " + antiNum;
        strRetv += ", WifiRange: " + wifiRange;
        strRetv += "\r\n";
        if (secondsTakenCameraArray[0] > 0) {
            strRetv += ", CAM SECONDS TAKEN: ";
            for (int i = 0; i < 10; i++) {
                strRetv += "" + secondsTakenCameraArray[i] + ", ";
            }
        }
        strRetv += "\r\n";
        if (secondsTakenFINDERArray[0] > 0) {
            strRetv += ", FINDER SECONDS TAKEN: ";
            for (int i = 0; i < 10; i++) {
                strRetv += "" + secondsTakenFINDERArray[i] + ", ";
            }
        }

        return strRetv;
    }
}