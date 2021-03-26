package dronelab;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import java.util.Collections;

import dronelab.utils.*;
import dronelab.collidable.*;

/*
A "centralized controller" simulating a human or automated GCS controlling the swarm
collectively; specifically assigning a waypoint path 
*/
public class ControllingEntity {
    // Currently the only function of this entity is to generate a waypoint path for the
    // drone swarm.
    private int pathSize = 10; // Number of locations we want to select in the waypoint path
    private int quality = 100; // -100 to 100, quality of knowledge
    private ArrayList<Point2D> locations = new ArrayList<Point2D>();  // the pathSize selected locations
    private ArrayList<Point2D> allSelectedLocations = new ArrayList<Point2D>();  // all of the locations provided for this particular sim run

    private int numPointsToChooseFrom = 1000;  // How many total points can we pick from to generate the wp path?
    private int densityRadiusMeters = 100; // How many meters' radius do we use to calculate density of a particular point?

    public int getQuality() { return quality; }
    public int getPathSize() { return pathSize; }
    public ArrayList<Point2D> getLocations() { return locations; }

    public void setQuality(int newQual) {
        quality = newQual;
    }

    public ControllingEntity() { }

    // a Point2D with a density, used only in this class
    public class ControllerPoint2D extends Point2D implements Comparable<ControllerPoint2D> {
        public ControllerPoint2D(int x, int y) {
            super(x, y);
            calculateDensity();
        }

        // Spawn a new Point2D object from this object
        public Point2D createNewPoint2D() {
            return new Point2D(getX(), getY());
        }

        // How many survivors within distance?
        private Integer density;
     
        public Integer getDensity() {
            return density;
        }

        public void calculateDensity() {
            // Find the density of survivors based on x, y in the current scenario
            // We'll use a simple method, just look through all survivors and if their coordinate
            // is within densityRadius meters of our coordinate, it counts
            int dens = 0;
            for (Person v : DroneLab.scenario.victims) {
                if (v.isSeen() == true)
                    continue;
                if (Physics.withinDistanceMeters(v.x(), v.y(), getX(), getY(), densityRadiusMeters) == true) {
                    dens++;
                }
            }
            density = new Integer(dens);
        }
        
        // This allows us to easily sort an array containing these objects in descending order by density
        // For ascending order simply reverse "o" and "this"
        @Override
        public int compareTo(ControllerPoint2D o) {
            return o.getDensity().compareTo(this.getDensity());
        }
    }

    public void reset() {
        locations.clear();
        allSelectedLocations.clear();
    }

    /* 
    use cases:
    - You might know nothing
    - You might think you know some things based on reports, personal eyewitness accounts
    - You might have a terrain map that you can use to predict where things might be
    - You might have some overwatch capability but without the ability to distinctly identify
    numbers, or with a less-capable camera or without ATR capability.
    - You might have outdated knowledge that was correct some time ago but now has gotten
    worse.

    So we can confirm or deny:  Even WITH good knowledge, operator still does not
    work as well as non-operator.  Or, you need REALLY good knowledge (80%+) before
    operator is useful.  Or, you just need to know a LITTLE bit (20%) about where your
    targets are before operator is useful.
    
    Reasonable human behavior:
    - Send the swarm to the densest point first
    - Send the swarm to the next-densest point unless something close in density is 
    much closer in proximity.
    (Or we could just assume that the operator just sends to the next-densest regardless
    of proximity, because the area is not extremely large)

    (Function for prioritizing by combining density with proximity?  A way to decide 
    with both of those as factors - what would a human pick reasonably?  Proximity should
    weigh something.)

    */

    ControllerPoint2D getNextDensestPoint(ArrayList<ControllerPoint2D> fromPoints, ArrayList<Point2D> excludedPoints) {
        // excludedPoints are points already selected as being among the most dense -
        // as we select new points, we discard any that are within the density radius
        // of any point already selected.  i.e. any NEW point MUST not be within the density
        // radius of any existing point.
        ControllerPoint2D newPoint = null;
        
        // Just go down the list, take the next densest on the list that is not within the radius of
        // anything in excludedPoints
        boolean failed = false;
        for (int i = 0; i < fromPoints.size(); i++) {
            failed = false;
            newPoint = fromPoints.get(i);
            // Check through all excluded points to see if this one is within any of their radii
            for (int j = 0; j < excludedPoints.size(); j++) {
                Point2D excludedPoint = excludedPoints.get(j);
                if (Physics.withinDistanceMeters(newPoint.getX(), newPoint.getY(), excludedPoint.getX(), excludedPoint.getY(), densityRadiusMeters) == true) {
                    failed = true;
                    break;
                }
            }
            if (failed == true) {
                // It was within the radius of an existing point, so set to null and try again
                newPoint = null;
                continue;
            }
            // Succeeded, break and return the one we just checked.
            break;
        }

        return newPoint;
    }

    // Drones are informing the operator when they complete the search pattern.
    // As soon as ONE of them hits the final index, generate a new set of points and send it
    // out to all the drones.
    // This all happens instantaneously right now, but in theory, this could be actually a 
    // WIFI message communicated to an entity called the GCS from the swarm and back.  Also
    // if you are outside of the WIFI range of the GCS, that should affect things.  But right now
    // this is all just assumed to occur instantaneously.
    public void informComplete() {
        generateNextPathWithinSimRun();
        // If we generate a new set of points, assign to all entities in the swarm.  In theory
        // this could actually use the WIFI broadcast, and the operator could have a WIFI
        // module, that way it would be affected by WIFI delays if I ever implement that capability
        for (Drone d : DroneLab.scenario.drones) {
            // Just reset the AssignedPathModule of each drone, this will case each module to 
            // grab the newly generated pattern and reset to a 'ground zero' state which is
            // what we want since we are starting the waypoint path again from scratch.
            d.resetModule(Constants.STR_ASSIGNED_PATH);
        }
    }

    // From the start of a NEW sim run, generate the path from scratch.
    public void generateNewPath() {
        reset();
        generateNextPathWithinSimRun();
    }

    private void sortLocationsByProximity(ArrayList<Point2D> points) {
        // So, easy enough, start with location 0, then find the closest location to that, 
        // and put it into location 1.
        ArrayList<Point2D> tempPoints = new ArrayList<Point2D>();
        tempPoints.addAll(points);

        // Clear the points and add in the first one which is where we start from, which is
        // also the densest point.  The operator always sends the drones to the densest location
        // first, then based on proximity, fans out from that point.  Seems reasonable if not
        // always entirely true.
        points.clear();
        points.add(tempPoints.get(0));
        tempPoints.remove(0);

        // Now add in the points based on proximity
        Point2D currentPoint = points.get(0);
        Point2D closestPoint = tempPoints.get(0); // 0 is the "new 1" because we removed 0
        double dist = 0;
        double prevDist = 0;

        // Remove from tempPoints and add to points each time we find the next-closest point.
        while (tempPoints.size() > 0) {
            closestPoint = tempPoints.get(0);
            dist = 0;
            // pixels is fine here, we are just looking for a flat distance, doesn't matter
            // what the units are
            prevDist = Physics.calcDistancePixels(closestPoint.getX(), closestPoint.getY(), currentPoint.getX(), currentPoint.getY());
            for (Point2D point : tempPoints) {
                dist = Physics.calcDistancePixels(point.getX(), point.getY(), currentPoint.getX(), currentPoint.getY());
                if (dist < prevDist) {
                    closestPoint = point;
                    prevDist = dist;
                }
            }
            // closestPoint is now true to its name
            tempPoints.remove(closestPoint);
            points.add(closestPoint);
            currentPoint = closestPoint;
        }
    }

    // From within a CURRENT sim run, generate the NEXT SET of waypoints for the swarm.
    public void generateNextPathWithinSimRun() {
        locations.clear();
        
        // General strategy:
        // Pick a bunch of points at random on the map
        // Organize them from most dense per 50m radius to least dense per 50m radius, 
        // most dense being slot 0 and least being slot size of points array-1.
        // Index into this array based on "quality of knowledge"
        // Quality of knowledge should be loaded into SimMatrixItem from the simulation matrix

        // We must set up the known victims first, because these are used to calculate density
        // when the points are created below
        //setupKnownVictims();

        // Now choose points at random, this also calculates density for each point
        ArrayList<ControllerPoint2D> points = new ArrayList<ControllerPoint2D>();
        for (int i = 0; i < numPointsToChooseFrom; i++) {
            // Pick our points from 50 to pixel width minus 50
            int x = Utils.number(50, (int)DroneLab.scenario.currentWidth - 50);
            int y = Utils.number(50, (int)DroneLab.scenario.currentHeight - 50);
            ControllerPoint2D newPoint = new ControllerPoint2D(x, y);
            points.add(newPoint);
        }
        
        // Sort the list in ascending order, we can do this because we assigned a compareTo
        // method on the ControllerPoint2D class
        Collections.sort(points);

        //for (ControllerPoint2D point : points) {
        //    Utils.log("Density: " + point.getDensity());
        //}
 
        // Choose the points that we want based on the quality of knowledge
        // It's a "guessing game" so quality of knowledge indicates how likely the controller
        // is to "guess correctly" the current densest point that has not already been selected
        ControllerPoint2D point = null;
        for (int i = 0; i < pathSize; i++) {
            point = null;
            //int index = guessNextPathIndex(points.size());
            // Now, "roll" to see if the controller succeeded in guessing a dense location
            // If quality is 100, controller always succeeds.
            if (quality > Utils.number(0, 100)) {
                point = getNextDensestPoint(points, allSelectedLocations);
            }
            else {
                // Otherwise, we just pick a point at random.  Should this be considered
                // a "fail" and thus select a low density location?  Therefore a quality
                // of zero actually means faulty information rather than no information
                point = points.get(Utils.number(0, points.size()-1));
            }
            // Ran out of points... highly unlikely but if it happens, just stop
            if (point == null) {
                break;
            }
            Point2D addedPoint = point.createNewPoint2D();
            locations.add(addedPoint); // Make a regular Point2D out of it and add to locations
            allSelectedLocations.add(addedPoint); // Also add it to the master list of points used so far
            points.remove(point); // And take that point out so we don't use it again
        } 

        // Now, order the points by proximity to each other.  That is more likely
        // to be a human behavior than sending the swarm back and forth across the map
        // to dense locations.
        sortLocationsByProximity(locations);
    }
}