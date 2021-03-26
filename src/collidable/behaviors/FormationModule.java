package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class FormationModule extends BehaviorModule {
    private double desiredRange = Distance.pixelsFromMeters(50); // Let start with this
    private double threshold = 0.1;

    public FormationModule() {
        super(Constants.STR_FORM, Constants.STR_FORM_J);
        drawLetter = "F";
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
        if ((drone.ls == null) || drone.getDroneList() == null || drone.getDroneList().size() == 0) {
            return false;
        }

        double x = drone.ls.x();
        double y = drone.ls.y();

        // So its simple, from our closest drone, just try to make the distance correct.
        // So if we are too close, try to get further away.  If we are too far, try to get closer.
        DroneData data = findClosestSocialDroneData();
        if (data == null) {
            return false;
        }

        // TODO: calcDistance is in PIXELS not meters though so this is not doing what we think it's doing.
        // It's not necessarily a "problem" it's just not maintaining 50 meters it's maintaining 50 pixels,
        // so we should figure out what 50 pixels IS in meters and change it to that (so that we're consistent)
        // and then change this to do distance in meters.  The behavior will not change on the original scenario
        // but will be consistent to additional scenarios.
        double dist = Physics.calcDistancePixels(x, y, data.x, data.y);

        // If we are within a reasonable threshold, we are ok.
        if ((dist >= desiredRange * (1 - threshold)) && (dist <= desiredRange * (1 + threshold))) {
            return false;
        }

        // I guess we need to turn off seeking here?  I do this in the avoid module.  That seems
        // very strange, I shouldnt have to do that.  The architecture should just "take care" of 
        // that naturally.  This needs revisiting.
        drone.stopSeekingTarget();

        // Ok so we are not.  Are we too close, or too far?
        if (dist < desiredRange) {
            // Too close!  Accelerate away from drone.  So calculate the angle
            // from the drone to us, and make that our heading
            drone.setHeadingRadians(Physics.getAngleRadians(data.x, data.y, x, y));
            drone.maxAcceleration();
        } else {
            // Too far!  Go towards.
            drone.setHeadingRadians(Physics.getAngleRadians(x, y, data.x, data.y));
            drone.maxAcceleration();
        }

        return true;
    }

    // If you are forming, you only form with SOCIAL drones
    private DroneData findClosestSocialDroneData() {
        double x = drone.ls.x();
        double y = drone.ls.y();
        double closestDist = 0;
        DroneData closest = null;

        // DroneData closest = drone.getDroneList().get(0);
        // closestDist = Physics.calcDistancePixels(x, y, closest.x, closest.y);
        for (DroneData data : drone.getDroneList()) {
            if (data.role != Drone.DroneRole.SOCIAL) {
                continue;
            }
            double dist = Physics.calcDistancePixels(x, y, data.x, data.y);
            if (closestDist == 0 || dist < closestDist) {
                closestDist = dist;
                closest = data; 
            }
        }
        return closest;
    }
    
    /*int forming = 0;
    double avAngle = 0;
    double avAngleDeflection = 20;
    // Apply acceleration at some angle until we are in the right spot
    public void form() {
        // Quite simple, just literally accelerate full throttle in whatever
        // way you should be going
        if (forming == 0) {
            forming = 3;
            // If you have speed, accelerate in opposite direction.  But if you have no
            // speed, what do you do?  Check where you were going and just flip that around.
            if (drone.ySpeed == 0 && drone.xSpeed == 0) {
                avAngle = drone.heading + Math.toRadians(180);
	            //Utils.log("avoid(): avAngle: " + avAngle);
            } else {
                avAngle = Math.atan2(-drone.ySpeed, -drone.xSpeed);
            }
            // Just add 20 degrees so we have some variation, this should not be so random though,
            // because this can cause a crash if it makes us go 20 degrees towards another thing.
            avAngle += Math.toRadians(avAngleDeflection); 
            drone.heading = avAngle;
        }
        // No we shouldnt apply acceleration for the drone, let it do its thing.
	    //drone.applyAcceleration(avAngle);

        // But this must be maintained for at least a few ticks so we can get out of the way
        // and try other behaviors again
        forming--;
        if (forming < 0) {
            forming = 0;
        }
    }*/
}