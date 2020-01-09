package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

// This is basically the same as Repel but with a larger desiredRange
public class AntiSocialModule extends BehaviorModule {
    private double desiredRange = Distance.pixelsFromMeters(100); 

    public AntiSocialModule() {
        super(Constants.STR_ANTI, Constants.STR_ANTI_J);
        drawLetter = "N";
    }

    @Override
    public boolean setup() { return true; }
    
    @Override
    public boolean reset() { return true; }

    @Override
    public boolean usesDrawLocation() { return false; }
    
    @Override
    public boolean draw(GraphicsContext gc) {
        return true;
    }

    @Override
    public boolean receive(String msg) { 
        return true;
    }

    @Override
    public boolean react() { 
        if (drone == null) {
            return false;
        }
        // Basically we check to see if we are in formation with other drones.
        // If we see them, we try to form up.  If we dont, we just dont care.
        if (drone.ls == null || drone.getDroneList() == null || drone.getDroneList().size() == 0) {
            return false;
        }

        double x = drone.ls.x();
        double y = drone.ls.y();

        // So its simple, from our closest drone, just try to make the distance correct.
        // So if we are too close, try to get further away.  If we are too far, try to get closer.
        DroneData data = findClosestDroneData();
        if (data == null) {
            return false;
        }
        double dist = Physics.calcDistance(x, y, data.x, data.y);

        // If we are within a reasonable threshold, we are ok.
        if (dist >= desiredRange) {
            return false;
        }

        // I guess we need to turn off seeking here?  I do this in the avoid module.  That seems
        // very strange, I shouldnt have to do that.  The architecture should just "take care" of 
        // that naturally.  This needs revisiting.
        drone.stopSeekingTarget();

        // Too close!  Accelerate away from drone.  So calculate the angle
        // from the drone to us, and make that our heading
        drone.setHeadingRadians(Physics.getAngleRadians(data.x, data.y, x, y));
        drone.maxAcceleration();
        return true;
    }

    // If you are anti, you only anti-form with SOCIAL and ANTISOCIAL drones
    private DroneData findClosestSocialOrAntiDroneData() {
        double x = drone.ls.x();
        double y = drone.ls.y();
        double closestDist = 0;
        DroneData closest = null;

        // DroneData closest = drone.getDroneList().get(0);
        // closestDist = Physics.calcDistance(x, y, closest.x, closest.y);
        for (DroneData data : drone.getDroneList()) {
            if (data.role != Drone.DroneRole.SOCIAL && data.role != Drone.DroneRole.ANTISOCIAL) {
                continue;
            }
            double dist = Physics.calcDistance(x, y, data.x, data.y);
            if (closestDist == 0 || dist < closestDist) {
                closestDist = dist;
                closest = data; 
            }
        }
        return closest;
    }

    private DroneData findClosestDroneData() {
        double x = drone.ls.x();
        double y = drone.ls.y();
        DroneData closest = drone.getDroneList().get(0);
        double closestDist = Physics.calcDistance(x, y, closest.x, closest.y);
        for (DroneData data : drone.getDroneList()) {
            double dist = Physics.calcDistance(x, y, data.x, data.y);
            if (dist < closestDist) {
                closestDist = dist;
                closest = data; 
            }
        }
        return closest;
    }
}