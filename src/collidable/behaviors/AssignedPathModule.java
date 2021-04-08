package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.*;
import dronelab.utils.*;
import dronelab.collidable.*;

/*
Behavior module for a waypoint path assigned by an external entity such as a controlling
human or a Ground Control Station
Could this inherit from the search pattern module as a base class, and then subclass
the specific pattern that the search pattern module actually does now?
*/
public class AssignedPathModule extends BehaviorModule {
    private int lastLocationIndex = -1; // Last one we were moving towards
    private int locationIndex = -1; // Which one we are currently moving towards
    private int numLocations = 0; // Just so we dont have to call size() over and over
    private Point2D nextLocation = null;
    private ArrayList<Point2D> locations = new ArrayList<Point2D>();

    public AssignedPathModule() {
        super(Constants.STR_ASSIGNED_PATH, Constants.STR_ASSIGNED_PATH_J);
        drawLetter = "A";
    }

    @Override
    public void assign(Drone d) {
        super.assign(d);
        getSearchPattern();
    }

    @Override
    public boolean setup() { return true; }

    @Override
    public boolean reset() 
    { 
        nextLocation = null;
        locationIndex = -1;
        lastLocationIndex = -1;
        getSearchPattern();
        return true; 
    }
    
    @Override
    public boolean usesDrawLocation() { return true; }

    @Override
    public boolean draw(GraphicsContext gc) {
        if (nextLocation == null) 
            return false;

        // Draw the waypoint path
        drawPath(gc, locations);

        return drawLocation(gc, (int)nextLocation.getX(), (int)nextLocation.getY());
    }

    @Override
    public boolean receive(String msg) { 
        //Utils.log(msg);
        String[] parts = msg.split("\\:"); // String array, each element is text between colons
        if (parts[0].equals("AX") == true) {
            setAssignedPathIndex(Utils.tryParseInt(parts[1]));
            //Utils.log("Index: " + Utils.tryParseInt(parts[1]));
        }
        return true;
    }

    @Override
    public boolean react() { 
        return search();
    }

    // For debugging we may want to be able to see what the search pattern is
    public String printSearchPattern() {
        String str = "";
        for (Point2D point : locations) {
            str += (int)point.getX() + "," + (int)point.getY() + " ";
        }
        return str;
    }

	// Set up the search pattern, it's just based on what the controlling entity / operator has
    // selected for us
    private void getSearchPattern() {
        if (drone == null) {
            return;
        }

        // Clear the old one
        locations.clear();
        
        // Grab the pattern from the human controller
        locations.addAll(DroneLab.operator.getLocations());
        numLocations = locations.size();

        //Utils.log(printSearchPattern());
    }

    // Probably if we are located somewhere, we start with the closest
    // search location in our pattern.

    private void broadcastPatternIndex(int patternIndex) {
        if (drone.wifi == null) {
            return;
        }
        // Behavior modules can broadcast messages like so.
        drone.wifi.broadcastBehaviorMsg(this, "AX:" + patternIndex);
        //Utils.log("Sent pattern index " + patternIndex);
    }

    private boolean setNextSearchLocation() {
        return setAssignedPathIndex(locationIndex + 1);
        /*if (locations == null || locations.size() == 0) {
            return;
        }
        locationIndex++;
        if (locationIndex > locations.size() - 1) {
            locationIndex = 0;
        }

	    nextLocation = locations.get(locationIndex);*/
    }

    public boolean setAssignedPathIndex(int index) {
        if (locations == null || locations.size() == 0) {
            return false;
        }

        // If it's trying to go back to the previous one we had,
        // do not allow this.  We cannot have only two locations 
        // in a search pattern, and we dont want locations to oscillate
        // as drones in a swarm fight for locations.  So, the "show must
        // go on" as they say.
        if (index == locationIndex || index == lastLocationIndex) {
            return false;
        }

        lastLocationIndex = locationIndex;
        locationIndex = index;
        if (locationIndex >= locations.size()) {
            // We've done all the locations, so now get a new set of locations
            // from the operator
            // Inform the operator that we're done
            DroneLab.operator.informComplete();
            // That's it, the operator will take care of the rest.
            return false;
        }

        // If we've changed locations, tell all other drones to do
        // the same.
        //if (locationIndex != oldIndex) {
            // Also send out a message to say hey, I reached my search location.  Other drones can
            // pick this up and respond.  For example if I reach my next location, I can inform all
            // drones to "move on."  Thus the entire swarm can act as one single unit, with each drone
            // competing to get to the next (same) search location, but once one drone reaches it,
            // they are all done.  This can create some fairly robust behavior where drones can be
            // added to the swarm later and they simply "pick up" where the rest of the drones already
            // are.
            broadcastPatternIndex(locationIndex);
        //}

	    nextLocation = locations.get(locationIndex);
        return true;
    }

    private boolean search() {
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

        if (nextLocation == null) {
	        //drone.setTargetLocation(Math.round(drone.x() + drone.wid/2), Math.round(drone.y() + drone.hgt/2));
            //drone.stopSeekingTarget();
            setNextSearchLocation();
            return false;
        }

        // We can't really search if we dont know where we are.
        if (drone.ls == null) {
            return false;
        }

        double distance = 20;

        // Lets use the camera distance, it really depends what kind of thing
        // we are trying to do here - we will treat it as "see this location" rather
        // than actually reaching it for the pattern search, since its a search.
        if (drone.cam != null) {
            distance = drone.cam.getRange();
        }

        // Check to see if we are there, if we are, then delete our location
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), nextLocation.getX(), nextLocation.getY(), distance) == true) {
            //nextLocation = null;
            //drone.setTargetLocation(Math.round(drone.x() + drone.wid/2), Math.round(drone.y() + drone.hgt/2));
            // Now move to the next location.  If we finish, we inform the controller of completion and then
            // await further instructions which will come shortly.
            if (setNextSearchLocation() == false)
                return true;
            drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed());
            return false;
        }
        //System.out.println("" + inputLocation.getX());
        drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed());
        return true;
    }
}