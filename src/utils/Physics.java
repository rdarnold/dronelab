
package dronelab.utils;

import java.util.ArrayList;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;

import dronelab.collidable.*;

// This is essentially like a static class in C#
public final class Physics {
    private Physics () { // private constructor
    }

    // IMPORTANT, this is the ARC tan, NOT the regular tangent
    // This is NOT the angle between the points, it is the angle based off of the X axis.
    //  (i.e. the arc tan, it is the angle of the entire arc)
    // So like, if your x2, y2 is in the top left quandrant (related to your x1, y1), 
    // the return from this function will be the acute angle between the
    // points plus 90 (because the first quad is 90 degrees, it starts at 0 as facing directly
    // "right" and the degrees move counter-clockwise).  This is actually good because it makes 
    // it easy for us to understand where things are and how to move them in relation to the entire
    // 360 degree arc.
    public static double getAngleDegrees(double x1, double y1, double x2, double y2) {
        return (Utils.normalizeAngle(Math.toDegrees(getAngleRadians(x1, y1, x2, y2))));
    }

    // in radians
    public static double getAngleRadians(double x1, double y1, double x2, double y2) {
        return (Math.atan2(y2 - y1, x2 - x1));
    }

    public static double getAngleRadians(double xSpeed, double ySpeed) {
        return (Math.atan2(ySpeed, xSpeed));
    }
    public static double getAngleDegrees(double xSpeed, double ySpeed) {
        return (Utils.normalizeAngle(Math.toDegrees(getAngleRadians(xSpeed, ySpeed))));
    }

    public static double getSpeed(double vx, double vy) {
        return Math.sqrt(Math.pow(vx, 2) + Math.pow(vy, 2));
    }

    // From x1, y1, as the origin point, rotate x2, y2
    public static Point2D calcRotatedPoint(double originX, double originY, double x2, double y2, double angleDegrees) {
        double hyp = calcDistancePixels(originX, originY, x2, y2);
        double angleRadians = Math.toRadians(angleDegrees);

        double rotX = Math.cos(angleRadians) * hyp;
        double rotY = Math.sin(angleRadians) * hyp;

        Point2D point = new Point2D(originX + rotX, originY + rotY);
        return point;
    }

    // This is the ideal x velocity to get to desired location from current position
    // if we were standing still.
    public static double calcIdealXSpeed(double x0, double y0, double x1, double y1, double speed) {
        double dist = Physics.calcDistancePixels(x0, y0, x1, y1);
        if (dist <= 0)
            return 0;
        return ((speed / dist) * (x1 - x0));
    }

    public static double calcIdealXSpeed(Mobile mob, double targetX, double targetY) {
        return calcIdealXSpeed(mob.x(), mob.y(), targetX, targetY, mob.getTargetSpeed());
    }

    public static double calcIdealYSpeed(double x0, double y0, double x1, double y1, double speed) {
        double dist = Physics.calcDistancePixels(x0, y0, x1, y1);
        if (dist <= 0)
            return 0;
        return ((speed / dist) * (y1 - y0));
    }
 
    public static double calcIdealYSpeed(Mobile mob, double targetX, double targetY) {
        return calcIdealYSpeed(mob.x(), mob.y(), targetX, targetY, mob.getTargetSpeed());
    }

    // Pixels
    public static double calcDistancePixels(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
    }

    // Meters
    public static double calcDistanceMeters(double x1, double y1, double x2, double y2) {
        return Distance.metersFromPixels(Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)));
    }

    public static Shape getIntersectingShape(Collidable a, Collidable b) {
        return getIntersectingShape(a.getShape(), b.getShape());
    }

    public static Shape getIntersectingShape(Collidable a, Shape b) {
        return getIntersectingShape(a.getShape(), b);
    }

    public static Shape getIntersectingShape(Shape a, Collidable b) {
        return getIntersectingShape(a, b.getShape());
    }

    public static Shape getIntersectingShape(Shape a, Shape b) {
        Shape intersect = Shape.intersect(a, b);
        if (intersect.getBoundsInLocal().getWidth() != -1) {
          //System.out.println("I: " + intersect.getBoundsInLocal().getMinX());
          return intersect;
        }
        return null;
    }

    public static boolean boundsIntersect(Shape a, Shape b) {
        return (a.getBoundsInParent().intersects(b.getBoundsInParent()));
    }

    public static boolean intersecting(Shape a, Shape b) {
        if (boundsIntersect(a, b) == true) {
            return (getIntersectingShape(a, b) != null);
        }
        return false;
    }

    public static boolean rectanglesOverlap(Rectangle first, Rectangle second) {
        return (rectanglesOverlap(first.getX(), first.getY(), first.getWidth(), first.getHeight(),
                                  second.getX(), second.getY(), second.getWidth(), second.getHeight()));
    }

    public static boolean rectanglesOverlap(Rectangle first, double left2, double top2, double wid2, double hgt2) {
        return (rectanglesOverlap(first.getX(), first.getY(), first.getWidth(), first.getHeight(),
                                  left2, top2, wid2, hgt2));
    }

    public static boolean rectanglesOverlap(double left1, double top1, double wid1, double hgt1, Rectangle second) {
        return (rectanglesOverlap(left1, top1, wid1, hgt1,
                                  second.getX(), second.getY(), second.getWidth(), second.getHeight()));
    }

    public static boolean rectanglesOverlap(double left1, double top1, double wid1, double hgt1,
                                            double left2, double top2, double wid2, double hgt2) {
        // If left of 1 is further right than right of 2
        if (left1 > left2 + wid2) { return false; }

        // If top of 1 is further down than bottom of 2
        if (top1 > top2 + hgt2) { return false; }

        // If right of 1 is further left than left of 2
        if (left1 + wid1 < left2) { return false; }

        // If bottom of 1 is further up than top of 2
        if (top1 + hgt1 < top2) { return false; }

        // Otherwise, we are overlapping.
        return true;
    }

    // To save overhead on constant heap allocations, we create this here.  Doesn't look pretty
    // but works better performance-wise.  It does seem to work ...
    private static Circle withinPointCircle = new Circle(1.0);
    public static boolean withinPoint(Collidable col, double clickX, double clickY) {
        // Is our clicked x and y within this collidable?
        // first check the boundary rectangles
        if (rectanglesOverlap(col.getBoundingRect(), clickX, clickY, 1, 1) == false) {
            return false;
        }
        // Bounding rects overlap, now check an intersection
        // This is a little weird, we'll create a mini circle on the clicked point with 
        // a radius of 1 and use that for shapes.intersect.  There are more efficient ways
        // to do this surely, but this only occurs after the bounding rects have been verified
        // as overlapping anyway so it shouldn't cost a lot of overhead.
        withinPointCircle.setCenterX(clickX);
        withinPointCircle.setCenterY(clickY);
        //withinPointCircle.setRadius(1.0f);
        return (intersecting(withinPointCircle, col.getShape()));
        //if (checkIntersection(withinPointCircle, col) != null) {
          //  return true;
       // }
       // return false;
    }

    // This is in PIXELS
    public static boolean withinDistance(double x1, double y1, double x2, double y2, double distancePixels) {
        return (calcDistancePixels(x1, y1, x2, y2) <= distancePixels);
    }

    // This is in METERS
    public static boolean withinDistanceMeters(double x1, double y1, double x2, double y2, double distanceMeters) {
       return (calcDistancePixels(x1, y1, x2, y2) <= Distance.pixelsFromMeters(distanceMeters));
    }

    public static Shape getCollisionShape(Mobile mob, ArrayList<Obstacle> obstacles) {
        Shape intersect = null;
        for (Obstacle obs : obstacles) {
            // Can fly over things
            if (mob.getElevation() > obs.getElevation()) {
                continue;
            }

            // First see if their bounding rectangles even overlap to save us some crunch time.
            if (rectanglesOverlap(mob.getBoundingRect(), obs.getBoundingRect()) == false) {
                continue;
            }
            // Now check the actual intersection
            intersect = getIntersectingShape(mob, obs);
            if (intersect != null) {
                return intersect;
            }
        }
        return null;
    }

    // Same utility as checkCollisions but does not return a shape and is slightly more
    // efficient, maybe, I should test to see though.
    public static boolean checkCollisions(Mobile mob, ArrayList<Obstacle> obstacles) {
        for (Obstacle obs : obstacles) {
            // Can fly over things
            if (mob.getElevation() > obs.getElevation()) {
                continue;
            }

            // First see if their bounding rectangles even overlap to save us some crunch time.
            // Do I need to do this or can I use their boundsInParent instead.
            if (rectanglesOverlap(mob.getBoundingRect(), obs.getBoundingRect()) == false) {
                continue;
            }

            if (intersecting(mob.getShape(), obs.getShape()) == true) {
                return true;
            }
        }
        return false;
    }

    // Note that this is going to grow at O(n) with the number of obstacles passed in.
    public static void generateSensorPictureObstacles(ArrayList<Obstacle> sensorObstacles, Shape sensorRangeCircle, double elevation, ArrayList<Obstacle> obstacles) {
        Shape intersect = null;
        //Shape pic = null;
        //sensorObstacles.clear();
        for (Obstacle obs : obstacles) {
            // No collisions but we should still detect unless its really out of range...
            //if (elevation > obs.getElevation()) {
              //  continue;
           // }
            
            // Check bounds before we check intersects so that we are more efficient.
            if (boundsIntersect(sensorRangeCircle, obs.getShape()) == false) {
                continue;
            }

            // Actually it is MUCH more performant to just add the obstacle without
            // checking the intersecting shape.  We dont need to do that anyway as we
            // check that in the various other collision methods.  Not checking shape
            // here just means our sensor picture has a few obstacles in it that might be
            // slightly out of range of the sensor due to rotation of polygons and such.
            // The thing is we already are not using a shape as the sensor picture - we are
            // using an obstacle array.  So it really doesnt make a difference as it is already
            // "imperfect" and includes parts of shapes that arent really in sensor range (but are
            // connected to other parts of shapes that are - and those parts are closer to the drone
            // and thus are the relevant pieces)
            sensorObstacles.add(obs);
            //intersect = getIntersectingShape(sensorRangeCircle, obs.getShape());

            //if (intersect != null) {
                //sensorObstacles.add(obs);
            //}
        }
    }

    /* Cool as this is in terms of generating a realistic sensor picture, it is too
       inefficient to work in our simulation.

    // Note that this is going to grow at O(n) with the number of obstacles passed in.
    public static Shape generateSensorPicture(Shape sensorRangeCircle, double elevation, ArrayList<Obstacle> obstacles) {
        Shape intersect = null;
        Shape pic = null;
        for (Obstacle obs : obstacles) {
            // No collisions but we should still detect unless its really out of range...
            if (elevation > obs.getElevation()) {
                continue;
            }
            intersect = checkIntersection(sensorRangeCircle, obs);

            // This shape intersects, so we need to join it with the current sensor
            // picture so we end up with a complete picture and not just return with
            // one shape.
            if (intersect != null) {
              if (pic == null) {
                  pic = intersect;
              } else {
                  pic = Shape.union(pic, intersect);
              }
            }
        }

        return pic;
    }*/

    public static Obstacle findHighestObstacleBelow(double range, Circle circle, double elevation, ArrayList<Obstacle> obstacles) {
        Obstacle highest = null;
        for (Obstacle obs : obstacles) {

            // If its out of range, don't detect.
            if (elevation - obs.getElevation() > range)
                continue;

            //if (checkIntersection(circle, obs) != null) {
            if (intersecting(circle, obs.getShape()) == true) {
                if (highest == null) {
                    highest = obs;
                } else if (obs.getElevation() > highest.getElevation()) {
                    highest = obs;
                }
            }
        }
        return highest;
    }
    
    public static Obstacle findHighestObstacleBelow(double range, double x, double y, double elevation, ArrayList<Obstacle> obstacles) {
        Obstacle highest = null;
        for (Obstacle obs : obstacles) {

            // If its out of range, don't detect.
            if (elevation - obs.getElevation() > range)
                continue;

            if (withinPoint(obs, x, y)) {
                if (highest == null) {
                    highest = obs;
                } else if (obs.getElevation() > highest.getElevation()) {
                    highest = obs;
                }
            }
        }
        return highest;
    }
}