package dronelab.collidable;

import java.util.ArrayList;
import java.util.EnumSet;

import dronelab.Deployment;
import dronelab.DroneLab;
import dronelab.PhysicsEngine;
import dronelab.Scenario;
import dronelab.Sector;
import dronelab.collidable.Person;

import dronelab.utils.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ambulatory extends Person {

    private final static double WALKING_SPEED = 0.049227; //Equivalent of 1.42m/s in px/fr divided by 2 for slow movers

    private static ArrayList<Point2D> deploymentZones = new ArrayList<Point2D>();

    private final static int TARGET_TOLERANCE = 3; // Acceptable pixel offset for hitting x/y target
    private final static double VISIBLE_RANGE = 500.0; // Range in m at which the deployment location is visible

    // Additional behavior states for Ambulatory survivors
    // Note: departed is only used in the OPEN environment (CLOSED by default)
    private boolean walking = true;
    private boolean notified = false;
    private boolean wandering = true;
    private boolean departed = false;

    // Wandering variables
    private long startWanderTime = 0;
    private int wanderMilliSeconds = 0;
    private int wanderAngle = 0;

    // String output variables
    private static String departedList = "";
    private static int departedCount = 0;
    private static String detectedList = "";
    
    // Avoid variables needed if using collision avoidance
    // private long startAvoidTime = 0;
    // private int avoidMilliSeconds = 0;
    // private double avoidAngle = 0;

    public Ambulatory() {
        super();
        setupMovementParams();
    }
    
    public Ambulatory(Person copyFrom) {
        super(copyFrom);
        setupMovementParams();
    }

    // For use by Deployment.java to inform Ambulatory survivors where it's located.
    // We do this instead of completely creating full "eyesight" and it is completed 
    // before Ambulatory survivors are created, so they can use the info during init
    public static void addDeploymentLocation(Deployment dep) {
        // Calculate the center (ish) from the top left
        double centerX = dep.origWid / 2.0;
        double centerY = dep.origLen / 2.0;

        // Rotate the center by the top left axis
        double angleRad = Math.toRadians(dep.angleDegrees);
        double rotatedX = dep.origX + centerX * Math.cos(angleRad) - centerY * Math.sin(angleRad);
        double rotatedY = dep.origY + centerX * Math.sin(angleRad) + centerY * Math.cos(angleRad);

        Point2D center = new Point2D(rotatedX, rotatedY);
        deploymentZones.add(center);
    }

    private void setupMovementParams() {
        // Average walking speed in m/s = 1.42s
        setMaxSpeed(WALKING_SPEED);

        // Assume we reach max speed in 0.5 seconds, which is 30 frames;
        setMaxAccelerationRate(WALKING_SPEED / 30.0);

        // Set an initial wander angle to be any direction
        wanderAngle = Utils.rand.nextInt(359);

        // Check if we can see the deployment location from our current location.
        // If so, go there. Otherwise, wander aimlessly.
        Point2D depPoint = deploymentVisible();
        if (depPoint != null)
            targetDeployment(depPoint);
        else
            wanderBehavior();
    }

    // No ambulatory survivor is actually "located" until they make it to the deployment zone
    // OR they can be located if they reached a building and are in the "helping" state
    @Override
    public void setLocated() {
        if (!departed) {
            if (!walking) {
                int time = DroneLab.scenario.simTime.getTotalSeconds();
                detectedList += id + " located at " + time + System.getProperty("line.separator");
                super.setLocated();
            }
            else if (!notified) {
                int time = DroneLab.scenario.simTime.getTotalSeconds();
                detectedList += id + " notified at " + time + System.getProperty("line.separator");
                notified = true;
                setColors(255, 165, 0); // notified of direction to travel (orange)
            }
        }
    }

    @Override
    public boolean isLocated()  { return super.isLocated() || departed; }

    // If we reach the deployment zone, we're "located", but if it's any other building, we start "helping"
    private void reachedBuilding(boolean deploymentZone) {
        walking = false;
        stopMoving();
        if (deploymentZone) {
            int time = DroneLab.scenario.simTime.getTotalSeconds();
            detectedList += id + " located at DZ at " + time + System.getProperty("line.separator");
            super.setLocated();
        }
    }

    @Override
    public boolean move() {
        if (walking == false) {
            return false;
        }       

        // Will initially trigger as the Person must move before being added to a sector
        // But still we want to record the person ID and time at which they exited
        int time = DroneLab.scenario.simTime.getTotalSeconds();

        if (sectors.size() == 0 && time != 0) {
            if (!departed) { // If they haven't departed already, then we want to record it.
                departedList += id + " departed at " + time + System.getProperty("line.separator");
                // Uncomment below to enable the OPEN environment
                // departed = true;
                // departedCount++;
                // walking = false;

                // Comment out everything below to disable the CLOSED environment
                double maxX = DroneLab.scenario.currentWidth;
                double maxY = DroneLab.scenario.currentHeight;
                // If we went out of bounds from our X, restart on the other side (test left then right)
                if (getX() - 10 <= 0) {
                    this.setPos(maxX - 10, getY());
                }
                else if (getX() + 10 >= maxX) {
                    this.setPos(10, getY());
                }
                else if (getY() <= 0) {
                    this.setPos(getX(), maxY - 10);
                }
                else {
                    this.setPos(getX(), 10);
                }
            }
        }

        // Check if we made it to the deployment location within 5 pixels in any direction
        if (hitDeployment()) {
            reachedBuilding(true);
            return false;
        }

        // For use with collision avoidance techniques
        // ArrayList<Obstacle> nearbyObstacles = detectObstacles();
        // if (nearbyObstacles.size() > 0)
        //     return (avoidObstacles(nearbyObstacles) && super.move());

        // Execute different behaviors in priority order:
        // 1. Seek deployment location
        // 2. Wander around
        Point2D depPoint = deploymentVisible();
        if (depPoint != null)
            return (targetDeployment(depPoint) && super.move());
        else
            return (wanderBehavior() && super.move());
    } 

    public static String getDepartedList() { return departedList; }
    
    public static void setDepartedList(String newList) { departedList = newList; }

    public static int getDepartedCount() { return departedCount; }

    public static void setDepartedCount(int newCount) { departedCount = newCount; }

    public static String getDetectedList() { return detectedList; }
    
    public static void setDetectedList(String newList) { detectedList = newList; }

    @Override
    public void damage(int amt) {
        reachedBuilding(false);
        assignCorrectColorsForStatus();
    }

    private boolean hitDeployment() {
        double minDistance = Double.MAX_VALUE;
        for (Point2D depPoint : deploymentZones) {
            double dist = Physics.calcDistanceMeters(x(), y(), depPoint.getX(), depPoint.getY());
            if (dist < minDistance)
                minDistance = dist;
        }
        // If we're under our target tolerance, then we made it
        return minDistance < TARGET_TOLERANCE;
    }
    // Get the closest deployment zone by distance from its center, if applicable
    // Requirements: must be within the VISIBLE_RANGE or notified by a drone of the nearest location
    private Point2D deploymentVisible() {
        Point2D minDistancePoint = null;
        double minDistance = 0;
        for (Point2D depPoint : deploymentZones) {
            double dist = Physics.calcDistanceMeters(x(), y(), depPoint.getX(), depPoint.getY());
            if (dist <= VISIBLE_RANGE || notified) {
                if (minDistancePoint == null) {
                    minDistancePoint = depPoint;
                    minDistance = dist;
                } else if (dist < minDistance) {
                    minDistancePoint = depPoint;
                    minDistance = dist;
                }
            }
        }
        return minDistancePoint;
    }

    private boolean wanderBehavior() {
        if (!wandering)
            return false;

        long time = System.currentTimeMillis();
	    if (time - startWanderTime > wanderMilliSeconds) {
            // Assign some wandering location with a time & direction
            // 5-10 seconds
            wanderMilliSeconds = (1000 * (5 + Utils.rand.nextInt(5)));

            // Add/subtract 0-10 degrees each iteration. Random +/- chance
            int driftAmt = Utils.rand.nextInt(11);
            if (Utils.rand.nextBoolean())
                wanderAngle += driftAmt;
            else
                wanderAngle -= driftAmt;
            stopSeekingTarget();
            setHeadingDegrees((double) wanderAngle);
            setSpeedToMaxAlongCurrentHeading();

            // Begin wandering somehow
            startWanderTime = time;
            return false;
        }
        return true;
    }
    private boolean targetDeployment(Point2D target) {
        wandering = false;
        setTargetLocation(target.getX(), target.getY(), WALKING_SPEED);
        startSeekingTarget();
        return true;
    }

    @Override
    public void draw(GraphicsContext gc) {
        if (isLocated() == true || isSeen() == true || isRescued() == true) {
            draw(gc, cs.getFill(), cs.getStroke());
        } 
        else {
            Color color = Color.rgb(0, 0, GraphicsHelper.getFlashColorLevel());
            draw(gc, color, color);
        }
    }

    private void draw(GraphicsContext gc, Color fillCol, Color strokeCol) {
        int x = (int)x() - (int)wid/2;
        int y = (int)y() - (int)hgt/2;

        gc.setFill(fillCol);
        gc.fillOval(x, y, wid, hgt);
        gc.setLineWidth(1);
        gc.setStroke(strokeCol); 
        //gc.strokePolyline(xPoints, yPoints, xPoints.length);

        // arms
        gc.strokeLine(x + wid / 2, y + hgt, x,       y + hgt*1.2);
        gc.strokeLine(x + wid / 2, y + hgt, x + wid, y + hgt*1.2);

        // body
        gc.strokeLine(x + wid / 2, y + hgt, x + wid / 2, y + hgt*1.5);
        
        // legs
        gc.strokeLine(x + wid / 2, y + hgt*1.5, x, y + hgt * 2);
        gc.strokeLine(x + wid / 2, y + hgt*1.5, x + wid, y + hgt * 2);

        // I'm sure somewhere a true object-oriented programmer is really upset with me for this method.
        superDraw(gc);
    }
    /*
    // This functionality allows for collision avoidance instead of the stop and "help" behavior
    // The compute time was too much for large sims, but may be of use in smaller experiments


    // nearbyObstacles will be populated with all nearby obstacles within 10m
    private ArrayList<Obstacle> detectObstacles() {
        ArrayList<Obstacle> nearbyObstacles = new ArrayList<Obstacle>();
        Circle visionRadius = new Circle(x(), y(), AVOID_VISION_RADIUS);

        for (Sector sect : sectors) {
            Physics.generateSensorPictureObstacles(nearbyObstacles, visionRadius, getElevation(), sect.obstacles);            
        }
        return nearbyObstacles;
    }

    // set heading to avoid building
    private boolean avoidObstacles(ArrayList<Obstacle> nearbyObstacles) {
        if (nearbyObstacles == null || nearbyObstacles.size() == 0)
            return false;
        

        long time = System.currentTimeMillis();
        if (time - startAvoidTime > avoidMilliSeconds) {
            // Avoid for 2 seconds and reasses
            avoidMilliSeconds = 7500;

            stopSeekingTarget();
            // Find the closest obstacle and avoid it
            // Treat all obstacles as Circles, so we can avoid along the radius
            double closestObsCenterX = 0.0;
            double closestObsCenterY = 0.0;
            double closestObsRadius = 0.0;
            double distanceToObstacle = 0.0;
            double minDistance = Double.MAX_VALUE;
            for (Obstacle obs : nearbyObstacles) {
                // Get the least/leftmost x value and least/leftmost y value
                double obsCenterX = obs.getX() + (obs.wid / 2);
                double obsCenterY = obs.getY() + (obs.hgt / 2);
                double obsRadius = obs.wid / 2;
                if (obs.wid < obs.hgt)
                    obsRadius = obs.hgt / 2;

                
                distanceToObstacle = Physics.calcDistancePixels(x(), y(), obsCenterX, obsCenterY);
                if (distanceToObstacle < minDistance) {
                    minDistance = distanceToObstacle;
                    closestObsCenterX = obsCenterX;
                    closestObsCenterY = obsCenterY;
                    closestObsRadius = obsRadius;
                }
            }
            distanceToObstacle = minDistance;

            double theta = Math.toDegrees(Math.atan((closestObsRadius + SAFE_PASSING_DISTANCE) / distanceToObstacle));

            // Determine the new heading
            double angleToObs = Physics.getAngleDegrees(x(), y(), closestObsCenterX, closestObsCenterY);
            double newHeading = getHeadingDegrees();
            
            // This means the obstacle is to the left, so turn clockwise for 0-90 (-), 180-270 (-)
            if (angleToObs > 0 && angleToObs < 90)
                newHeading -= theta;
            else if (angleToObs >= 180 && angleToObs < 270)
                newHeading -= theta;
            else if (angleToObs >= 90 && angleToObs < 180)
                newHeading += theta;
            else
                newHeading += theta;
        
            // Ensure heading stays within 0-360 degrees
            if (newHeading < 0)
                newHeading += 360;
            else if (newHeading >= 360)
                newHeading -= 360;
    
            setHeadingDegrees(newHeading);
            setSpeedToMaxAlongCurrentHeading();

            startAvoidTime = time;
            return false;
        }
        
        return true;
    } */
}
