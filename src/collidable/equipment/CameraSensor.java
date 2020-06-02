package dronelab.collidable.equipment;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import dronelab.*;
import dronelab.utils.*;
import dronelab.collidable.*;
import dronelab.Scenario;
import dronelab.Sector;

public class CameraSensor extends Sensor {

    public CameraSensor(Drone d) {
        super(Constants.STR_SENSOR_CAMERA, Constants.STR_SENSOR_CAMERA_J, d);
        range = Distance.pixelsFromMeters(30);
        drawRange = true;
    }

    private void detectPeople(ArrayList<Person> people) {
        for (Person p : people) {
            if (p.isSeen() == true)
                continue;
            if (Physics.withinDistance(drone.x(), drone.y(), p.getX(), p.getY(), range/2)) {
                p.setSeen();
                if (drone != null) {
                    drone.signalPersonSeen(p);
                }

                // Write the timestamp out to text file now
                if (Constants.RECORD_ALL_TIMESTAMPS == true) {
                    DroneLab.sim.addSurvivorTimestamp();
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

        // I guess this return basically does nothing.
        return false;
    }

    // We can draw the "sensor range fan" basically
    @Override
    public boolean draw(GraphicsContext gc) {
        if (drawRange == false)
            return false;

	    gc.setFill(Color.rgb(150, 0, 150, 0.3));
        gc.setStroke(Color.rgb(100, 0, 100, 0.3));
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