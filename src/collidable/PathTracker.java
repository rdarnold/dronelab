package dronelab.collidable;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import dronelab.utils.*;

public class PathTracker {
    double xPoints[];
    double yPoints[];
    Color strokeCol;

    private boolean active = false; // Turn it off for now to speed up the sim runs
    private boolean drawPath = false;
    Drone drone;
    // Actually the point should probably have an associated timestamp as well
    ArrayList<Point2D> points = new ArrayList<Point2D>();
    int maxPoints = 20000;

    public PathTracker(Drone d) {
        drone = d;
        // Base color off of ID
        int r = 250;
        int g = 200;
        int b = 0;
        // Just for my research paper so I can show some different colored paths
        // so it's easier to follow.
        if (drone.getId() == 3) {
          r = 0;
          g = 200;
          b = 250;
        }
        else if (drone.getId() == 4) {
          r = 0;
          g = 150;
          b = 250;
        } 
        else if (drone.getId() == 5) {
          r = 0;
          g = 100;
          b = 250;
        }
	    strokeCol = Color.rgb(r, g, b, 0.8);
    }

    public void on() { reset(); active = true; }
    public void off() { active = false; }
    public void setDraw(boolean newDraw) {drawPath = newDraw; }

    public void reset() {
        points.clear();
    }

    public boolean track() {
        if (active == false) {
            return false;
        }
        
        // This actually tracks by GPS location
        if (drone.hasLocationSensor() == false) {
            return false;
        }

        double x = drone.ls.x();
        double y = drone.ls.y();

        if (points.size() > 0) {
            Point2D lastPoint = points.get(points.size()-1);

            if (lastPoint.getX() == x && lastPoint.getY() == y) {
                return false;
            }
        }

        // When we hit maxPoints, start dropping points from the back.
        if (points.size() >= maxPoints) {
            points.remove(0);
        }

        points.add(new Point2D(x, y));
        return true;
    }

    // We can draw our current path
    public void draw(GraphicsContext gc) {
        if (drawPath == false) {
            return;
        }
	    gc.setLineWidth(3);
        gc.setStroke(strokeCol); 
        //gc.strokePolyline(xPoints, yPoints, xPoints.length);

        // I cant really stroke the polyline because I have no polyline and I dont
        // want to maintain all the points in separate arrays and keep remaking them.
        // so I just draw the lines...
        for (int i = 0; i < points.size()-2; i++) {
            gc.strokeLine(points.get(i).getX(), points.get(i).getY(), 
                points.get(i+1).getX(), points.get(i+1).getY());
        }
        return;
    }
}