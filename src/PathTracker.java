
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class PathTracker {
    double xPoints[];
    double yPoints[];
    Color strokeCol;

    private boolean active = true;
    private boolean drawPath = false;
    Drone drone;
    // Actually the point should probably have an associated timestamp as well
    ArrayList<Point2D> points = new ArrayList<Point2D>();
    int maxPoints = 200;

    public PathTracker(Drone d) {
        drone = d;
	    strokeCol = Color.rgb(250, 200, 0, 0.8);
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
	    gc.setLineWidth(5);
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