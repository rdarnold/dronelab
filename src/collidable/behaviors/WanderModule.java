package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class WanderModule extends BehaviorModule {
    private int maxWanderDistance = 300;

    // For high fidelity wandering
    private Point2D wanderLocation = null;

    // For low fidelity wandering
    private long startWanderTime = 0;
    private int wanderMilliSeconds = 0;
    private int wanderAngle = 0;

    public WanderModule() {
        super(Constants.STR_WANDER, Constants.STR_WANDER_J);
        drawLetter = "W";
        //setBounds(d.leftBound, d.topBound, d.rightBound, d.bottomBound); 
    }

    @Override
    public boolean setup() { return true; }
    
    @Override
    public boolean reset() { return true; }

    @Override
    public boolean usesDrawLocation() { return true; }
    
    @Override
    public boolean draw(GraphicsContext gc) {
        if (wanderLocation == null) 
            return false;

        return drawLocation(gc, (int)wanderLocation.getX(), (int)wanderLocation.getY());
    }
    
    @Override
    public boolean receive(String msg) { 
        return true;
    }

    @Override
    public boolean react() { 
        return wander();
    }

    private boolean assignHighFidelityWanderLocation() {
        if (drone.hasLocationSensor() == false) {
            assignLowFidelityWanderLocation();
            return false;
        }
        // Has to be within maxWanderDistance of current location if there is one.
        double randX = Utils.rand.nextInt(maxWanderDistance * 2) - maxWanderDistance;
        double randY = Utils.rand.nextInt(maxWanderDistance * 2) - maxWanderDistance;

        // We cant do location-based wandering unless we have a location.
        // actually why do we need a location for wandering.  Why not just
        // pick a direction and distance and just go.  Well lets call this
        // high fidelity wandering.  We could wander without a GPS if that was
        // useful, but wed basically just wander for maybe a time-burst, like
        // 10 seconds in x direction.

        // So this is high fidelity wandering using the GPS sensor
        double x = drone.ls.x() + randX;
        double y = drone.ls.y() + randY;

        //x = Utils.clamp(x, leftBound, rightBound);
        //y = Utils.clamp(y, topBound, bottomBound);

        wanderLocation = new Point2D(x, y);
        return true;
    }

    private void assignLowFidelityWanderLocation() {
        // We have no location sensor so just pick a time and direction and
        // go.
        wanderLocation = null;
        // 5-10 seconds
        wanderMilliSeconds = (1000 * (5 + Utils.rand.nextInt(5)));
        wanderAngle = Utils.rand.nextInt(359);
    }

    private void assignNewWanderLocation() { 
        if (drone.hasLocationSensor() == true) {
            assignHighFidelityWanderLocation();
        } else {
            assignLowFidelityWanderLocation();
        }
    }

    private boolean wanderHighFidelity() {
        // Choose a random point and wander towards it
        if (wanderLocation == null) {
            assignNewWanderLocation();
            return true;
        }

        // Now, once we reach the location, assign a new one
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), wanderLocation.getX(), wanderLocation.getY(), 20) == true) {
            assignNewWanderLocation();
            return false;
        }

        // Wander at half speed
        drone.setTargetLocation(wanderLocation.getX(), wanderLocation.getY(), drone.getMaxSpeed()/2);
        return true;
    }

    private boolean wanderLowFidelity() {
        // Check time
        long time = System.currentTimeMillis();
	    if (time - startWanderTime > wanderMilliSeconds) {
	        assignLowFidelityWanderLocation();
            startWanderTime = time;
            return false;
        }

        // TODO This needs to actually set something on the drone to make it
        // do this.
        // Basically we'd want to say, dont set a target location but set a
        // target speed and heading.  And we'd need to keep setting it here because
        // the avoid module might subsume this one and do something else.
        return true;
    }

    private boolean wander() {
        if (drone == null) {
            return false;
        }
       /* if (rightBound == 0) {
            wanderLocation = null;
            System.out.println("Error in WanderModule: No xBound set");
            return false;
        }
        if (bottomBound == 0) {
            wanderLocation = null;
            System.out.println("Error in WanderModule: No yBound set");
            return false;
        }*/

        // If we don't have a location sensor we need to do some low fidelity
        // wandering
        if (drone.hasLocationSensor() == false) {
           return wanderLowFidelity();
        } else {
           return wanderHighFidelity();
        }
    }
}