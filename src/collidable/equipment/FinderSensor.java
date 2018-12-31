package dronelab.collidable.equipment;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import dronelab.utils.*;
import dronelab.Scenario;
import dronelab.Sector;
import dronelab.collidable.*;

public class FinderSensor extends Sensor {

    public FinderSensor(Drone d) {
        super(Constants.STR_SENSOR_FINDER, Constants.STR_SENSOR_FINDER_J, d);
        // Supposedly has a range of 30 feet,
        // This should really be height detection distance as well.
        // So if we are x distance above building, we do not detect things inside.
        range = Distance.pixelsFromMeters(10); 
        drawRange = true;
    }

    private void detectPeople(ArrayList<Person> people) {
        for (Person p : people) {
            if (p.isLocated() == true)
                continue;
            if (Physics.withinDistance(drone.x(), drone.y(), p.getX(), p.getY(), range/2)) {
                p.setLocated();
                if (drone != null) {
                    drone.signalPersonLocated(p);
                }
            }
        }
    }

    @Override
    public boolean sense(Scenario scenario) {
        // So we have our circle, now see if any of these
        // people are in that circle, if so, they have been
        // detected.  We can just check if the distance is close
        // enough we dont really even have to do an intersection.
        // Research needed - how deep can an ir cam penetrate?
        //detectPeople(scenario.people);

        for (Sector sect : drone.sectors) {
            detectPeople(sect.people);
        }

        return false;
    }

    // We can draw the "sensor range fan" basically
    @Override
    public boolean draw(GraphicsContext gc) {
        if (drawRange == false)
            return false;

	    gc.setFill(Color.rgb(15, 150, 150, 0.3));
        gc.setStroke(Color.rgb(10, 100, 100, 0.3));
        gc.setLineWidth(3);

        int wid = (int)range*2;
        int hgt = (int)range*2;
        int x = (int)drone.x() - wid/2;
        int y = (int)drone.y() - hgt/2;

        gc.fillOval(x, y, wid, hgt);
        gc.strokeOval(x, y, wid, hgt);
        return true;
    }
}