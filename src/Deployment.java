package dronelab;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.shape.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.collidable.*;
import dronelab.utils.*;

public class Deployment extends CanvasRectangle {
    //Scenario scenario;
    //ArrayList<RescueWorker> workers;
    //ArrayList<Drone> drones;
    int nNumDrones = 1;

    public Deployment() { //Scenario scen) {
        super();
        // Attach it to a scenario so it can help populate the scenario
        // with appropriate workers.
        //scenario = scen;
        //workers = new ArrayList<RescueWorker>();
        //drones = new ArrayList<Drone>();
        make(0, 0, 1, 1, 0); // It must have some defaults.
    }
    // A rectangular shape that represents a rescue worker deployment,
    // also has some other data and functions
    /*public void init(int numDrones) {
        // Spawns workers and drones at random points within
        // the rectangle.
        reset();
        nNumDrones = numDrones;
    }*/

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.rgb(50, 50, 170, 0.5)); 
        this.fill(gc);

        gc.setLineWidth(3);
        gc.setStroke(Color.rgb(200, 200, 20, 0.7)); 
        this.stroke(gc);
    }

    /*public ArrayList<RescueWorker> getRescueWorkers() {
        return workers;
    }

    public ArrayList<Drone> getDrones() {
        return drones;
    }*/

    public void reset() {
        // Reset everything to original if things have changed
    }

    public void resize(double addX, double addY) {
        updateOriginalSize(origWid + addX, origLen + addY);
    }

    public void deploy(Scenario scen) {
        SimParams params = scen.simParams;

        if (params.getSimMatrixItem() != null) {
            // We have a simulation matrix, use that
            // Set number of drones to whatever we wanted in the simulation
            Drone drone = null;

            // Set the quality of knowledge if we're using a "centralized" operator, if not
            // it doesn't matter if we set it or not.  QoK is loaded as double from 0 - 1,
            // so we convert it to a 0-100 percent.
            DroneLab.operator.setQuality((int)(params.getQoK() * 100.0));

            int runNumber;
            if (DroneLab.runner == null)
                runNumber = 1;
            else
                runNumber = DroneLab.runner.getCurrentRunNum() + 1;
            // Deploy all our drones randomly in the deployment area.
            int x = 0;
            int y = 0;
            int[] allX = new int[params.getNumDrones()];
            int[] allY = new int[params.getNumDrones()];
            int absIdx = 0;

            for (int i = 0; i < params.getNumRole1(); i++) {
                // For some reason I have X as the top right corner...
                // I should fix that.  But until then, this has to
                // be a minus for the X coord.
                boolean overlap = false;
                do {
                    x = (int)origX - Utils.number(0, (int)origWid);
                    y = (int)origY + Utils.number(0, (int)origLen);
                    for (int j=0;j<absIdx;j++) {
                        if (allX[j] == x && allY[j] == y) {
                            overlap = true;
                            break;
                        }
                        else {
                            overlap = false;
                        }
                    }
                } while (overlap);
                allX[absIdx] = x;
                allY[absIdx] = y;
                absIdx++;
        
                drone = scen.addDrone(x, y);
                drone.setDroneRole(params.getRole1());
                drone.wifi.setRangeByVariable(params.getWifiRange());
                
                drone.setDataId(drone.hashCode());
                    
                String fname = "Run_" + runNumber + "_Drone_" + drone.getDataId() +"_Type_" + drone.getDroneRole().getValue() + ".csv";
                Utils.writeFile("Timestamp,x,y,z\r\n0,"+drone.getCurrentXYZ()+"\r\n", "output/xyzData/"+fname);
                //Utils.log(" " + params.getRole1());
            }

            x = 0;
            y = 0;
            for (int i = 0; i < params.getNumRole2(); i++) {
                // For some reason I have X as the top right corner...
                // I should fix that.  But until then, this has to
                // be a minus for the X coord.
                boolean overlap = false;
                do {
                    x = (int)origX - Utils.number(0, (int)origWid);
                    y = (int)origY + Utils.number(0, (int)origLen);
                    for (int j=0;j<absIdx;j++) {
                        if (allX[j] == x && allY[j] == y) {
                            overlap = true;
                            break;
                        }
                        else {
                            overlap = false;
                        }
                    }
                } while (overlap);
                allX[absIdx] = x;
                allY[absIdx] = y;
                absIdx++;

                drone = scen.addDrone(x, y);
                drone.setDroneRole(params.getRole2());
                drone.wifi.setRangeByVariable(params.getWifiRange());

                drone.setDataId(drone.hashCode());
                    
                String fname = "Run_" + runNumber + "_Drone_" + drone.getDataId() +"_Type_" + drone.getDroneRole().getValue() + ".csv";
                Utils.writeFile("Timestamp,x,y,z\r\n0,"+drone.getCurrentXYZ()+"\r\n", "output/xyzData/"+fname);
                //Utils.log(" " + params.getRole2());
            }

            x = 0;
            y = 0;
            for (int i = 0; i < params.getNumRole3(); i++) {
                // For some reason I have X as the top right corner...
                // I should fix that.  But until then, this has to
                // be a minus for the X coord.
                boolean overlap = false;
                do {
                    x = (int)origX - Utils.number(0, (int)origWid);
                    y = (int)origY + Utils.number(0, (int)origLen);
                    for (int j=0;j<absIdx;j++) {
                        if (allX[j] == x && allY[j] == y) {
                            overlap = true;
                            break;
                        }
                        else {
                            overlap = false;
                        }
                    }
                } while (overlap);
                allX[absIdx] = x;
                allY[absIdx] = y;
                absIdx++;

                drone = scen.addDrone(x, y);
                drone.setDroneRole(params.getRole3());
                drone.wifi.setRangeByVariable(params.getWifiRange());

                drone.setDataId(drone.hashCode());
                    
                String fname = "Run_" + runNumber + "_Drone_" + drone.getDataId() +"_Type_" + drone.getDroneRole().getValue() + ".csv";
                Utils.writeFile("Timestamp,x,y,z\r\n0,"+drone.getCurrentXYZ()+"\r\n", "output/xyzData/"+fname);
                //Utils.log(" " + params.getRole3());
            }
        }
        else {
            // No matrix, just run a normal scenario

            // Set number of drones to whatever we wanted in the simulation
            nNumDrones = params.getNumDrones();

            // Deploy all our drones randomly in the deployment area.
            int x = 0;
            int y = 0;
            int[] allX = new int[nNumDrones];
            int[] allY = new int[nNumDrones];
            int absIdx = 0;

            for (int i = 0; i < nNumDrones; i++) {
                // For some reason I have X as the top right corner...
                // I should fix that.  But until then, this has to
                // be a minus for the X coord.
                boolean overlap = false;
                do {
                    x = (int)origX - Utils.number(0, (int)origWid);
                    y = (int)origY + Utils.number(0, (int)origLen);
                    for (int j=0;j<absIdx;j++) {
                        if (allX[j] == x && allY[j] == y) {
                            overlap = true;
                            break;
                        }
                        else {
                            overlap = false;
                        }
                    }
                } while (overlap);
                allX[absIdx] = x;
                allY[absIdx] = y;
                absIdx++;

                scen.addDrone(x, y);
            }
        }
    }
}

