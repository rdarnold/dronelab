package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class SeekModule extends BehaviorModule {

    public SeekModule() {
        super(Constants.STR_SEEK, Constants.STR_SEEK_J);
        drawLetter = "S";
    }

    @Override
    public boolean setup() { return true; }
    
    @Override
    public boolean reset() { return true; }

    @Override
    public boolean usesDrawLocation() { return true; }

    @Override
    public boolean draw(GraphicsContext gc) {
        if (drone == null || drone.seekLocation == null) 
            return false;

        return drawLocation(gc, (int)drone.seekLocation.getX(), (int)drone.seekLocation.getY());
    }
    
    @Override
    public boolean receive(String msg) { 
        return true;
    }

    @Override
    public boolean react() { 
        return seek();
    }

    private boolean seek() {
        if (drone == null) {
            return false;
        }
        /*if (drone.isSeekingTarget() == false) {
            // If we are calling the seek module but there is no target to seek,
            // set our target location to current location.
            if (drone.isSeekingTarget() == false) {
                drone.setTargetLocation(drone.x(), drone.y());
            }
            return false;
        }*/

        if (drone.seekLocation == null) {
	        //drone.setTargetLocation(Math.round(drone.x() + drone.wid/2), Math.round(drone.y() + drone.hgt/2));
            //drone.stopSeekingTarget();
            return false;
        }

        // We can't really seek if we dont know where we are.
        if (drone.ls == null) {
            return false;
        }

        // Check to see if we are there, if we are, then delete our location
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), drone.seekLocation.getX(), drone.seekLocation.getY(), 20) == true) {
            drone.seekLocation = null;
            //drone.setTargetLocation(Math.round(drone.x() + drone.wid/2), Math.round(drone.y() + drone.hgt/2));
            return false;
        }
        //System.out.println("" + inputLocation.getX());
        drone.setTargetLocation(drone.seekLocation.getX(), drone.seekLocation.getY());
        return true;
    }
}