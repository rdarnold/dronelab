package dronelab.collidable.equipment;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import dronelab.collidable.*;
import dronelab.utils.*;
import dronelab.Scenario;
import dronelab.Sector;

public class SonarSensor extends Sensor {
    Circle circle = new Circle();

    // Ideally our sensor picture is just a shape union
    //Shape sensorPicture;

    // But unfortunately unions can start to take massive amounts of time
    // when shapes overlap, for some reason.  So instead, we also have the
    // option of using a shape list as our sensor picture.  This isn't as
    // "cool" as a single picture as it will contain parts of shapes
    // that are outside of the sensor:s range but it works fine for the
    // simulation.
    private ArrayList<Obstacle> sensorPictureObstacles = new ArrayList<Obstacle>();


    // What is the closest obstacle below us?  The sonar sensor should figure that out
    // as well.
    //Obstacle below = null;
    //boolean groundIsBelow = true;
    //double heightDistance = 0;  // Distance from ground or closest object.

    public SonarSensor(Drone d) {
        super(Constants.STR_SENSOR_SONAR, Constants.STR_SENSOR_SONAR_J, d);
        range = Distance.pixelsFromMeters(20);
        drawRange = true;
    }
    
    public ArrayList<Obstacle> getSensorPictureObstacles() { return sensorPictureObstacles; }

    @Override
    public boolean sense(Scenario scenario) {
        circle.setRadius(range);
        circle.setCenterX(drone.x());
        circle.setCenterY(drone.y());

        /*sensorPicture = Physics.generateSensorPicture(circle, drone.elevation, obstacles);
        if (sensorPicture != null) {
            return true;
        }*/

        //Physics.generateSensorPictureObstacles(
            //sensorPictureObstacles, circle, drone.getElevation(), scenario.obstacles);

        sensorPictureObstacles.clear();
        for (Sector sect : drone.sectors) {
            Physics.generateSensorPictureObstacles(
                sensorPictureObstacles, circle, drone.getElevation(), sect.obstacles);
        }
        //below = Physics.findHighestObstacleBelow((double)range/2, drone.x(), drone.y(), drone.getElevation(), scenario.obstacles);
        /*below = Physics.findHighestObstacleBelow((double)range/2, drone.getCircle(), 
                    drone.getElevation(), sensorPictureObstacles);
        if (below == null) {
            groundIsBelow = false;
            heightDistance = range / 2; // Too far.
            // Check if we are close enough to say we can detect the ground below us.
            if (drone.getElevation() <= range / 2) {
                groundIsBelow = true;
                heightDistance = drone.getElevation();
            }
        }
        else {
            heightDistance = drone.getElevation() - below.getElevation();
        }*/

        if (sensorPictureObstacles != null && sensorPictureObstacles.size() > 0) {
            return true;
        }
        return false;
    }

    // We can draw the "sensor range fan" basically
    @Override
    public boolean draw(GraphicsContext gc) {
        if (drawRange == false)
            return false;

	    gc.setFill(Color.rgb(100, 200, 100, 0.3));
        gc.setStroke(Color.rgb(50, 100, 50, 0.3));
        gc.setLineWidth(3);

        int wid = (int)range * 2;
        int hgt = (int)range * 2;
        int x = (int)drone.x() - wid/2;
        int y = (int)drone.y() - hgt/2;

        gc.fillOval(x, y, wid, hgt);
        gc.strokeOval(x, y, wid, hgt);
        return true;
    }
}