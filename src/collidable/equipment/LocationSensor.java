
package dronelab.collidable.equipment;

import javafx.scene.canvas.GraphicsContext;
import javafx.geometry.Point2D;

import dronelab.Scenario;
import dronelab.utils.*;
import dronelab.collidable.Drone;
import dronelab.Scenario;

public class LocationSensor extends Sensor {
    double x = 0;
    double y = 0;
    double z = 0;
    double uncertainty = 0;

    public LocationSensor(Drone d) {
        super(Constants.STR_SENSOR_LOCATION, Constants.STR_SENSOR_LOCATION_J, d);
        x = drone.x();
        y = drone.y();
        z = drone.getElevation();
    }

    public Point2D getLocation() {
        return new Point2D(x, y);
    }
    public double getElevation() { return z; }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }


    @Override
    public boolean sense(Scenario scenario) {
        // This is very simple right now, all we really need are the coordinates.
        // In the future we could use real lat/lon, and introduce various error
        // conditions and uncertainties that would be present in actual GPS
        // sensors.
        x = drone.x();
        y = drone.y();
        z = drone.getElevation();
        return true;
    }

    // What do we draw for the location sensor? 
    // Maybe we draw where it thinks the drone is?
    @Override
    public boolean draw(GraphicsContext gc) {
        return false;
    }
}