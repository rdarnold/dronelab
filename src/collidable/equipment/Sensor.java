package dronelab.collidable.equipment;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
//import javafx.scene.shape.*;

import dronelab.Scenario;
import dronelab.utils.*;
import dronelab.collidable.Drone;

public abstract class Sensor  {
    protected String name;
    protected String nameJapanese;
    protected Drone drone;
    protected double range = 0; // Currently in pixels, should be changed to something more relevant
    protected boolean drawRange = false;

    public double getRange() { return range; }
    public void setRangeMeters(double nMeters) {
        range = Distance.pixelsFromMeters(nMeters); 
    }

    public Sensor(String strNameEnglish, String strNameJapanese, Drone d) {
        drone = d;
        name = strNameEnglish;
        nameJapanese = strNameJapanese;
    }

    public String getName() { return name; }
    public String getNameJapanese() { return nameJapanese; }

    // Meant to be called regularly, and overridden by higher level classes
    public abstract boolean sense(Scenario scenario);
    public abstract boolean draw(GraphicsContext gc);
}