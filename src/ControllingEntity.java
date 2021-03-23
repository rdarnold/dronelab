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
    private int pathSize = 20; // Number of locations we want to select in the waypoint path
    private int quality = 100; // -100 to 100, quality of knowledge
    private ArrayList<Point2D> locations = new ArrayList<Point2D>();  // the pathSize selected locations

    private int numPointsToChooseFrom = 1000;  // How many total points can we pick from to generate the wp path?

    public int getQuality() { return quality; }
    public int getPathSize() { return pathSize; }
    public ArrayList<Point2D> getLocations() { return locations; }

    // The victims we know / think we know about based on quality of knowledge
    //private ArrayList<Person> knownVictims = new ArrayList<Person>();

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
            // is within 50m of our coordinate, it counts
            int dens = 0;
            for (Person v : DroneLab.scenario.victims) {
            //for (Person v : knownVictims) {
                if (Physics.withinDistance(v.x(), v.y(), getX(), getY(), 50) == true) {
                    dens++;
                }
            }
            density = new Integer(dens);
        }
        
        // This allows us to easily sort an array containing these objects in ascending order by density
        @Override
        public int compareTo(ControllerPoint2D o) {
            return this.getDensity().compareTo(o.getDensity());
        }
    }

    public void reset() {
        locations.clear();
        //knownVictims.clear();
    }

    int guessNextPathIndex(int size) {
        int max = size;
        int index = 0;

        // So we know the points are ordered from best at index 0 to worst
        // at index max - 1.  We use "quality" as the guesswork probability measure.
        // quality of 100 means we always guess correctly.  
        // quality of -100 means we always guess incorrectly.
        // quality of 0 means it's totally random.

        // TODO so what is the probability calculation here?  If you have a 50% chance
        // to guess "correctly" what does that mean?  How can I "weigh" the point indeces?

        // At 0, you're rolling the whole spectrum.
        // At 1, you have a 1% chance of doing better than total randomness.
        // At 20, you have a 20% chance of doing better than total randomness.

        // The way it should work is, if you have 90% quality, then your choices
        // over the entire number of points in the set (maybe 10-20) should average
        // out to being at the 90th percentile of goodness.

        // First you pick random 0 to max
        index = Utils.number(0, max-1);

        // Now, influence it by the quality of knowledge
        int influence = Math.abs(quality);
        if (quality > 0) {

        }
        else if (quality < 0) {
            
        }

        return index;
    }

    // Based on quality of knowledge, set up an array of victims that we think we know about
    // This is the one used to calculate density for choosing waypoint path
    /*public void setupKnownVictims() {
        // DID NOT DO THIS
        // because I realized that the results will always be the same/similar because
        // even if you know 'less victim locations' the relative density is still the same
        // so this isn't a useful way to simulate
        // let's say quality is literally your accuracy
        // of knowledge.  So if you have 50% quality, you are literally 50% accurate,
        // so your guesses fall into the middle range.  It's easier to explain this in a
        // research context; it's like saying a human has knowledge of where some survivors
        // are, but only half?  Could that work?
        // Maybe knowledge is HOW MANY survivors they know about.  So if quality is 50%,
        // they know the locations of 50% of survivors.  Using those locations, they then
        // decide on the best waypoints based on that knowledge.
        // So create a new array of victims size, and if we know 70% of locations, pick 70%
        // of those at random and mark them as "known" and then use THOSE as the way to
        // calculate density, ignoring ones that are not "known"
        // END DID NOT DO THIS

        // if quality is 50%,
        // they know the locations of 50% of survivors.  Using those locations, they then
        // decide on the best waypoints based on that knowledge.
        // So create a new array of victims size, and if we know 70% of locations, pick 70%
        // of those at random and mark them as "known" and then use THOSE as the way to
        // calculate density, ignoring ones that are not "known"

        // Clear it
        knownVictims.clear();

        // Add everything from the scenario
        knownVictims.addAll(DroneLab.scenario.victims);

        // Calculate target size
        double proportion = (double)Math.abs(quality) * 0.01;
        int targetSize = (int)((double)knownVictims.size() * proportion);

        // Whittle down the known victims until it's the target size
        while (knownVictims.size() > targetSize() {
            knownVictims.remove(Utils.number(0, knownVictims.size()-1));
        }
    }*/

    public void generatePath() {
        reset();
        
        // General strategy:
        // Pick a bunch of points at random on the map
        // Organize them from most dense per 50m radius to least dense per 50m radius, 
        // most dense being slot 0 and least being slot size of points array-1.
        // Index into this array based on "quality of knowledge"
        // Quality of knowledge should be loaded into SimMatrixItem from the simulation matrix

        // We must set up the known victims first, because these are used to calculate density
        // when the points are created below
        //setupKnownVictims();

        // Now choose the points at random, this also calculates density based on the
        // victims we actually think we know about.
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

        // Now, 

        // Choose the points that we want based on the quality of knowledge
        // It's a "guessing game" so quality of knowledge indicates how likely the controller
        // is to "guess correctly" or "guess incorrectly" and therefore it is a probability
        for (int i = 0; i < pathSize; i++) {
            int index = guessNextPathIndex(points.size());
            ControllerPoint2D point = points.get(index);
            locations.add(point.createNewPoint2D());
            points.remove(index); // And take that point out so we don't use it again
        } 

        // Now, should the points be ordered by proximity to each other?  Is that more likely
        // to be a human behavior rather than sending the swarm back and forth across the map
        // to dense locations?
        // TODO think this through

        // Clear this now just so we're not holding these victims in memory if we clear them for
        // any other reason; we don't need this list anymore
        //knownVictims.clear();
    }
}