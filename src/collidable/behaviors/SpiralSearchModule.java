package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class SpiralSearchModule extends BehaviorModule {
    private int locationIndex = -1; // Which one we are currently moving towards
    private Point2D centerLocation;
    private Point2D nextLocation = null;
    private ArrayList<Point2D> locations = new ArrayList<Point2D>();

    public SpiralSearchModule() {
        super(Constants.STR_SPIRAL, Constants.STR_SPIRAL_J);
        drawLetter = "I";
    }

    @Override
    public void assign(Drone d) {
        super.assign(d);
    }

    @Override
    public boolean setup() { return true; }

    @Override
    public boolean reset() 
    { 
        locationIndex = -1;
        centerLocation = null;
        nextLocation = null;
        locations.clear();
        return true; 
    }
    
    @Override
    public boolean usesDrawLocation() { return false; }

    @Override
    public boolean draw(GraphicsContext gc) {
        if (nextLocation == null) 
            return false;

        return drawLocation(gc, (int)nextLocation.getX(), (int)nextLocation.getY());
    }

    @Override
    public boolean receive(String msg) { 
        //Utils.log(msg);
        /*String[] parts = msg.split("\\:"); // String array, each element is text between colons
        if (parts[0].equals("ST") == true) {
            //setSearchPatternIndex(Utils.tryParseInt(parts[1]));
            Utils.log("1: " + Utils.tryParseInt(parts[1]));
        }*/
        return true;
    }

    @Override
    public boolean react() { 
        return search();
    }
    
    @Override
    public void onPersonDetected(Person person, ArrayList<Point2D> peopleLocations) { 
        if (centerLocation == null) {
            // Ok so this is a new person, see if we have some other people within
            // a short distance of this person. 
            int personThreshold = 4;
            int count = 0;
            double distance = Distance.pixelsFromMeters(20); // Within 20 meters seems good.

            double x1 = person.getX();
            double y1 = person.getY();
            for (Point2D p : peopleLocations) {
                //Utils.log(p.getX());
                if (Physics.withinDistance(x1, y1, p.getX(), p.getY(), distance) == true) {
                    count++;
                }

                if (count >= personThreshold) {
                    setSearchPattern(person);
                    return;
                }
            }
        }
    }

    private void broadcastSpiralSearchStarted(int x, int y) {
        if (drone.wifi == null) {
            return;
        }
        // Behavior modules can broadcast messages like so.
        drone.wifi.broadcastBehaviorMsg(this, "ST:" + x + ":" + y);
    }

    // Set up a search pattern
    private void setSearchPattern(Person person) {
        if (drone == null) {
            return;
        }
        // Starting with where we are
        double startX = person.getX();
        double startY = person.getY();
        double x = startX;
        double y = startY;

        if (x <= -1 || y <= -1) {
            // Starting position hasnt been set yet so lets not set up the pattern yet.
            return;
        }

        // Set the new center location
        centerLocation = new Point2D(x, y);
        locations.clear();

        int maxDistance = 150;
        int distance = 10;
        int distInterval = 10;
        double angle = 0;

        //locations.add(new Point2D(x, y));
        // We now spiral outwards for X number of steps.  Basically, we just keep tossing
        // out increasing angles at increasing distances until we hit our max distance.
        while (distance < maxDistance) {
            distance += distInterval;

            // Basically we use an octagonal pattern.
            angle += 45;
            angle = Utils.normalizeAngle(angle);

            // Calc from straight line going up
            x = startX;
            y = startY - distance;
            Point2D point = Physics.calcRotatedPoint(startX, startY, x, y, angle);
            locations.add(point);
        }

        setNextSearchLocation();
    }
    
    private void setNextSearchLocation() {
        if (locations == null || locations.size() == 0) {
            return;
        }
        locationIndex++;
        if (locationIndex > locations.size() - 1) {
            // Done!  
            // We don't really need to do anything else.  The rest
            // just takes care of itself due to our behavior-based AI.
            reset();
            return;
        }

	    nextLocation = locations.get(locationIndex);
    }

    private boolean search() {
        if (drone == null) {
            return false;
        }

        // We can't really search if we dont know where we are.
        if (drone.ls == null) {
            return false;
        }
        
        // If we detect more than X people within X meters, kick off a spiral search.
        // If a new concentration of people is found, signal for another
        // spiral search to begin.
        // So this requires polling the "person detector" capability on the drone, which
        // tells us 
        if (centerLocation == null) {
            return false;
        }

        double distance = 20;
        double speedMod = 0.75; // Need to slow it a bit, but 50% is too much

        // Check to see if we are there
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), nextLocation.getX(), nextLocation.getY(), distance) == true) {
            setNextSearchLocation();
            if (nextLocation != null) {
                drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed() * speedMod);
                return true;
            }
            return false;
        }
        //System.out.println("" + inputLocation.getX());
        drone.setTargetLocation(nextLocation.getX(), nextLocation.getY(), drone.getMaxSpeed() * speedMod);
        return true;
    }
}