package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class ScatterSearchModule extends BehaviorModule {
    private int lastLocationIndex = -1; // Last one we were moving towards
    private int locationIndex = -1; // Which one we are currently moving towards
    private int numLocations = 0; // Just so we dont have to call size() over and over
    private Point2D nextLocation = null;
    private ArrayList<Point2D> locations = new ArrayList<Point2D>();

    public ScatterSearchModule() {
        super(Constants.STR_SCATTER, Constants.STR_SCATTER_J);
        drawLetter = "T";
        setSearchPattern();
    }

    @Override
    public void assign(Drone d) {
        super.assign(d);
        setSearchPattern();
    }

    @Override
    public boolean setup() { return true; }

    @Override
    public boolean reset() 
    { 
        nextLocation = null;
        setSearchPattern();
        return true; 
    }
    
    @Override
    public boolean usesDrawLocation() { return true; }

    @Override
    public boolean draw(GraphicsContext gc) {
        if (nextLocation == null) 
            return false;

        return drawLocation(gc, (int)nextLocation.getX(), (int)nextLocation.getY());
    }

    @Override
    public boolean receive(String msg) { 
        //Utils.log(msg);
        String[] parts = msg.split("\\:"); // String array, each element is text between colons
        if (parts[0].equals("PSX") == true) {
            // So I should say, if I'm closer to this point, I'm going here, you go
            // somewhere else.  But if you're closer, I'll go somewhere else.
            //setSearchPatternIndex(Utils.tryParseInt(parts[1]));
        }
        return true;
    }

    @Override
    public boolean react() { 
        return search();
    }

    private void setSearchPattern(double startX, double startY, double xDist, double yDist, double yInterval) {
        // Right now this is actually pixels not meters,
        // I just don't have time for the conversion

        double x = startX;
        double y = startY;

        //Location zero is where we are.
        //locations.add(new Point2D(x, y));

        double yEnd = startY + yDist;

        // Now go right and left, going down by yInterval pixels
        // each time, until we reach the bottom.
        while (y <= yEnd) {
            locations.add(new Point2D(x, y));
            x += xDist;
            locations.add(new Point2D(x, y));
            y += yInterval;
            if (y > yEnd)
                break;
            locations.add(new Point2D(x, y));
            x -= xDist;
            locations.add(new Point2D(x, y));
            y += yInterval;
            //locations.add(new Point2D(x, y));
        }

        numLocations = locations.size();
    }

	// Set up a search pattern, there should be various types we can pass in here.
    private void setSearchPattern() {
        if (drone == null) {
            return;
        }
        // Lets start with like a basic back and forth pattern.
        double x = drone.getStartingX();
        double y = drone.getStartingY();

        if (x <= -1 || y <= -1) {
            // Starting position hasnt been set yet so lets not set up the pattern yet.
            return;
        }

        locations.clear();
        setSearchPattern(50, 50, 6300, 4800, 100);

        // This one is for the paper, just showing a small portion.
        //setSearchPattern(1300, 3800, 700, 600, 300);
    }

    private void yShift(int amount) {
        ArrayList<Point2D> newLocations = new ArrayList<Point2D>();
        for (Point2D loc : locations) {
            newLocations.add(new Point2D(loc.getX(), loc.getY() + amount));
        }
        locations = newLocations;
    }

    // Probably if we are located somewhere, we start with the closest
    // search location in our pattern.

    private void broadcastPatternIndex(int patternIndex) {
        if (drone.wifi == null) {
            return;
        }
        // Behavior modules can broadcast messages like so.
        drone.wifi.broadcastBehaviorMsg(this, "PSX:" + patternIndex);
        //Utils.log("Sent pattern index " + patternIndex);
    }

    private void setNextSearchLocation() {
        setSearchPatternIndex(locationIndex + 1);
    }

    public void setSearchPatternIndex(int index) {
        if (locations == null || locations.size() == 0) {
            return;
        }

        // If it's trying to go back to the previous one we had,
        // do not allow this.  We cannot have only two locations 
        // in a search pattern, and we dont want locations to oscillate
        // as drones in a swarm fight for locations.  So, the "show must
        // go on" as they say.
        if (index == locationIndex || index == lastLocationIndex) {
            return;
        }

        lastLocationIndex = locationIndex;
        locationIndex = index;
        if (locationIndex > locations.size() - 1) {
            locationIndex = 0;
            // Downshift by 20 meters.
            //yShift(20);
        }

        // I'm now moving to a new location; the next in my pattern.  Actually
        // it doesn't necessarily matter what the other drones are doing at
        // this point, I'll just keep doing my pattern. Or does it?  I probably
        // should use that staleness factor to determine where I should go next,
        // that and proximity maybe.
        broadcastPatternIndex(locationIndex);

	    nextLocation = locations.get(locationIndex);
    }

    private boolean search() {
        if (drone == null) {
            return false;
        }

        if (nextLocation == null) {
            // Ok so we are just gonna base this off of IDs right now for the sake
            // of the paper but I need to make it more robust for the actual sim
            // and algorithm.
            locationIndex = (int)drone.getId() - 1; 
            while (locationIndex >= locations.size()) {
                locationIndex -= locations.size();
            }
            //Utils.log(locationIndex);
            /*if (drone.getId() == 4)
                locationIndex = (locations.size() / 2) -1;
            if (drone.getId() == 5)
                locationIndex = locations.size() - 2;*/
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
            // Now move to the next location.  If we finish, go back to the first location.
            setNextSearchLocation();
            drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed());
            return false;
        }
        //System.out.println("" + inputLocation.getX());
        drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed());
        return true;
    }
}