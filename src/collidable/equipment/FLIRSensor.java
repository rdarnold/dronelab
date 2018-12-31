package dronelab.collidable.equipment;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import dronelab.utils.*;
import dronelab.Scenario;
import dronelab.collidable.*;

/*
 Forward-looking infrared, a way to distinguish heat sources from things like buildings
 foliage.  TODO implement this
 based on FLIR Vue which is $1500 and attachable via Go Pro mount and weighs about 100g
 options for field of view at 640x512 resolution are:
  9 mm; 69° x 56°
  13 mm; 45° x 37°
  19 mm; 32° x 26°
*/
public class FLIRSensor extends Sensor {
    Circle circle = new Circle();

    public FLIRSensor(Drone d) {
        super(Constants.STR_SENSOR_FLIR, Constants.STR_SENSOR_FLIR_J, d);
        // Supposedly has a range of 30 feet,
        // This should really be height detection distance as well.
        // So if we are x distance above building, we do not detect things inside.
        range = Distance.pixelsFromMeters(10); 
        drawRange = true;
    }

    @Override
    public boolean sense(Scenario scenario) {
        return false;
    }

    // We can draw the "sensor range fan" basically
    @Override
    public boolean draw(GraphicsContext gc) {
        return true;
    }
}