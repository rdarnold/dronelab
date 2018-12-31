package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.scene.shape.Circle;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class MaintainHeightModule extends BehaviorModule {
    private Circle circle = new Circle();
    private long startMaintainHeightTime = 0;
    private double lastElevation = 0;

    //boolean groundIsBelow = true;
    private double heightDistance = 0;  // Distance from ground or closest object.

    public MaintainHeightModule() {
        super(Constants.STR_MAINTAIN_HEIGHT, Constants.STR_MAINTAIN_HEIGHT_J);
        drawLetter = "H";

        // Has to be larger than the Climb Module circle, otherwise these two can
        // end up infinitely fighting each other as the climb module tries to climb,
        // but this module says no, I want to go lower.  As long as this module has
        // a larger radius, that shouldn't be an issue.
        circle.setRadius(25);
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

        double targetElevation = drone.targetHeightDistance;
        if (drone.ls != null) {
        	targetElevation = drone.ls.getElevation();
        }

        // Start by defaulting to not moving up or down.
     	drone.setVerticalHeading(Constants.VerticalHeading.NONE);

        // Basically, we attempt to maintain the height specified by the drone's target height.
        // if we are not at that height, because the thing below us is too tall or too short,
        // then we change height to get there by setting our target height.
        if (drone.isFlying() == false) {
            // Not flying, no need to do anything.
            startMaintainHeightTime = 0;
            return false;
        }

        if (drone.ss == null && drone.ls == null) {
            // We are blind, we dont know our height and we dont know what is below us.
            // So do nothing.
            startMaintainHeightTime = 0;
            return false;
        }

        // We need to check to see if the sonar sensor range is less than the distance
        // we want to be above sea level.  If it is, we should double check with the GPS to make
        // sure we arent too close to sea level.  But I'm not doing that right now.  The
        // sonar sensor basically trumps the GPS in all ways, unless the sonar sensor simply
        // does not exist.

        if (drone.ss != null) {
            // We have a sonar sensor so lets try to match distance with whatever
            // is below us.
            findHighestBelow();
            if (tooClose() == true) {
                // We are too close, attempt to distance ourselves appropriately
                return heightAdjustmentDesired(Constants.VerticalHeading.UP);
            }
            else if (Utils.withinTolerance(heightDistance, drone.targetHeightDistance, 1.0) == true) {
                // We're good.
                drone.setVerticalHeading(Constants.VerticalHeading.NONE);
                startMaintainHeightTime = 0;
                return false;
            }
            // We are not too close to anything but we are not good.  That
            // means we must be too high.
            //targetElevation = drone.targetHeightDistance;
            return heightAdjustmentDesired(Constants.VerticalHeading.DOWN);
        } 

        // I know I have a location sensor, so default to using that elevation
        // and attempting to just move to height above sea level.
        return setCorrectHeading(drone.ls.getElevation(), drone.targetHeightDistance);
    }

    private void findHighestBelow() {
        // We dont know what it is yet.  So lets say there is no distance
        // between us, until we can detect something.  Basically dont change
        // behavior unless our sensors are telling us information.
        heightDistance = drone.getElevation();

        circle.setCenterX(drone.x());
        circle.setCenterY(drone.y());

        double range = drone.ss.getRange();

        Obstacle below = Physics.findHighestObstacleBelow(range, circle, 
                    drone.getElevation(), drone.ss.getSensorPictureObstacles());

        if (below == null) {
            //groundIsBelow = false;
            // Check if we are close enough to say we can detect the ground below us.
            if (drone.getElevation() <= range) {
                //groundIsBelow = true;
                heightDistance = drone.getElevation();
            }
        }
        else {
            heightDistance = drone.getElevation() - below.getElevation();
        }
    }

    private boolean tooClose() {
        if (heightDistance < drone.targetHeightDistance) {
            return true;
        }
        return false;
    }

    // We might  pass either the true elevation or the GPS sensor elevation as the droneElevation
    // here so we need to leave it generic.
    private boolean setCorrectHeading(double droneElevation, double targetElevation) {
        if (droneElevation > targetElevation) {
            return heightAdjustmentDesired(Constants.VerticalHeading.DOWN);
        }
        else if (droneElevation < targetElevation) {
            return heightAdjustmentDesired(Constants.VerticalHeading.UP);
        }

        startMaintainHeightTime = 0;
        drone.setVerticalHeading(Constants.VerticalHeading.NONE);
        return false;
    }

    // If we call this function, that means according to this module, 
    // our height is wrong and we want to adjust it.  But we may or may
    // not actually do it yet, depending on some other factors.
    private boolean heightAdjustmentDesired(Constants.VerticalHeading newHeading) {
        
        // We dont want to start time tracking until our elevation has leveled off.
        // This isnt enough, because the elevation might be level for long enough while
        // another behavior is still trying to change it.
        //if (Math.abs(drone.getElevation() - lastElevation) > 1) {
         //   startMaintainHeightTime = 0;
       // }
        //lastElevation = drone.getElevation();
        
        if (checkTimer() == false) {
            if (startMaintainHeightTime == 0) {
                startMaintainHeightTime = System.currentTimeMillis();
            }
            // Our timer was not appropriate, so we "fall through" and let other
            // behaviors run.  Maybe we will be moved to a point where we dont
            // need to maintain height anyway in the near future.
            return false;
        }

        drone.setVerticalHeading(newHeading);
        // Also, attempt to stabilize our horizontal movement
        drone.stabilizeMovement();
        return true;
    }
 
    // If we've been detecting that we need to change height continuously for a certain period
    // of time, then we do it.  If not, we wait.
    private boolean checkTimer() {
        if (startMaintainHeightTime == 0) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        if (now - startMaintainHeightTime > 250) {
            return true;
        }
        return false;
    }
}