package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class ClimbModule extends BehaviorModule {
    private Circle circle = new Circle();

    public ClimbModule() {
        super(Constants.STR_CLIMB, Constants.STR_CLIMB_J);
        circle.setRadius(20);
        drawLetter = "C";
    }

    @Override
    public boolean setup() { return true; }
    
    @Override
    public boolean reset() { return true; }
    
    @Override
    public boolean usesDrawLocation() { return false; }

    @Override
    public boolean draw(GraphicsContext gc) {
        //GraphicsHelper.drawCircle(gc, circle, Color.DARKGRAY);
        return true;
    }

    @Override
    public boolean receive(String msg) { 
        return true;
    }
 
    // Climb behavior - within a certain distance of an obstacle, stabilize 2D flight 
    // and climb until you are a short distance above the object. 
    @Override
    public boolean react() { 
        if (drone == null) {
            return false;
        }

        // If we have no sonar sensor, we cant use this module since we can't then
        // detect objects.
        if (drone.ss == null) {
            return false;
        }

        // First, are we within distance of an object we need to climb over.
        ArrayList<Obstacle> obsList = drone.ss.getSensorPictureObstacles();
        if (obsList == null) {
            return false;
        }

        circle.setCenterX(drone.x());
        circle.setCenterY(drone.y());

        // These obstacles are all in "our way" in terms of that their heights
        // are not below us.  So we simply look thru them and see if any are
        // close enough that we should climb.
        for (Obstacle obs : obsList) {
            // The way we do this is to create a new "sensor picture" based on a circle
            // with a smaller radius.  This is actually not very efficient ... although
            // really, its not bad; as soon as we find anything, we just exit and climb,
            // and we are using a circle too.
            
            // If we are a few feet above, then we are high enough so dont
            // try to climb.
            if (drone.getElevation() - 3 > obs.getElevation()) {
                continue;
            }

            if (Physics.intersecting(circle, obs.getShape()) != false) {
                drone.setVerticalHeading(Constants.VerticalHeading.UP);

                // Also, attempt to stabilize our horizontal movement
                drone.stabilizeMovement();
                return true;
            }
        }

        // We are done climbing.  Of course another module after this may
        // set the heading back to up or to down, in order to change height later.
        drone.setVerticalHeading(Constants.VerticalHeading.NONE);
        return false;
    }
}