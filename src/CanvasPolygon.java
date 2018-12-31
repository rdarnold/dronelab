import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.List;
import javafx.geometry.Point2D;
import java.util.ArrayList;

public class CanvasPolygon extends Polygon {
    // This is a polygon that we can actually use in the
    // sim, since it maintains a list of points that can
    // be drawn on a canvas.

    // The leftmost X and Y coordinates
    private double leftX;
    private double leftY;

    public double getLeftX() { return leftX; }
    public double getLeftY() { return leftY; }

    double xPoints[];
    double yPoints[];

    public void addPoint(Point2D point) {
        addPoint(point.getX(), point.getY());
    }

    public void addPoint(int x, int y) {
        addPoint((double)x, (double)y);
    }

    public void addPoint(double x, double y) {
        //xPoints.add(x);
        //yPoints.add(y);
        // This is so stupid, we have to maintain multiple lists of
        // the same types of things because JavaFX cant even handle
        // its own types consistently

        // First we have to maintain the regular polygon that we use
        // for collision detection
        getPoints().addAll(x, y);

        // But we cant use this polygon for drawing!  I mean holy crap!
        // so we have to maintain a separate list of x and y points
        // for drawing, but we need a growable list, so we have to use
        // the list object
        //xPointList.add(x);
        //yPointList.add(y);
        
        // But we cant pass list objects into the drawing methods in canvas,
        // and we cant even give it a polygon from its own library!  I mean
        // holy crap^2!  So we need to create two arrays of doubles that we
        // can pass into the strokePolygon method.  Madness!  But its ok
        // because we wont be doing this often, as Obstacles are static
        // objs.  For something that moves we'd need a different method.
        refreshDrawPoints();
    }

    public void updatePoint(int index, Point2D point) {
        updatePoint(index, point.getX(), point.getY());
    }

    public void updatePoint(int index, double x, double y) {
        // The polygon stores them all as one huge array so the index
        // is only half of what we need.
        int polyIndex = index * 2;
        getPoints().set(polyIndex, x);
        getPoints().set(polyIndex + 1, y);

        // This one uses the normal index
        refreshDrawPoint(index, x, y);
    }

    public void refreshDrawPoint(int index, double x, double y) {
        if (xPoints == null || yPoints == null) {
            return;
        }
        xPoints[index] = x;
        yPoints[index] = y;
    }

    public void refreshDrawPoints() {
        int len = getPoints().size()/2;
        if (xPoints == null || xPoints.length != len) {
            xPoints = new double[len];
            yPoints = new double[len];
        }

        boolean isX = true;
        int xIndex = 0;
        int yIndex = 0;
        for (double point : getPoints()) {
            if (isX == true) {
                xPoints[xIndex] = point;
                xIndex++;
            } else {
                yPoints[yIndex] = point;
                yIndex++;
            }
            isX = !isX;
        }

        // Finally update our leftmost x,y coordinate.
        updateLeftXY();
    }

    // Same as number of sides
    public int getNumCorners() {
        return (getPoints().size() / 2);
    }

    public int getNumSides() {
        // The number of sides is equal to the number of coordinate pairs in the polygon
        //   (so half the number of points in the array)
        return (getPoints().size() / 2);
    }

    public void moveBy(double moveX, double moveY) {
        List<Double> points = getPoints();
        for (int i = 0; i < points.size(); i+=2) {
            points.set(i, points.get(i) + moveX);
        }
        for (int i = 1; i < points.size(); i+=2) {
            points.set(i, points.get(i) + moveY);
        }
        refreshDrawPoints();
    }

    public double findLeftX() {
        double left = 0;
        List<Double> points = getPoints();
        if (points == null || points.size() < 1) {
            return 0;
        }
        left = points.get(0);
        for (int i = 2; i < points.size(); i+=2) {
            if (points.get(i) < left) {
                left = points.get(i);
            }
        }
        return left;
    }

    public double findLeftY() {
        double left = 0;
        List<Double> points = getPoints();
        if (points == null || points.size() < 1) {
            return 0;
        }
        left = points.get(1);
        for (int i = 3; i < points.size(); i+=2) {
            if (points.get(i) < left) {
                left = points.get(i);
            }
        }
        return left;
    }

    public void updateLeftXY() {
        List<Double> points = getPoints();
        if (points == null || points.size() < 1) {
            leftX = 0;
            leftY = 0;
            return;
        }
        leftX = points.get(0);
        leftY = points.get(1);
        for (int i = 2; i < points.size(); i+=2) {
            if (points.get(i) < leftX) {
                leftX = points.get(i);
                leftY = points.get(i + 1);
            }
        }
    }

    // Some draw methods.
    public void fill(GraphicsContext gc) {
	    gc.fillPolygon(xPoints, yPoints, xPoints.length);
    }

    public void stroke(GraphicsContext gc) {
	    gc.strokePolygon(xPoints, yPoints, xPoints.length);
    }

    // Just stroke the unclosed line
    public void strokeUnclosed(GraphicsContext gc) {
	    gc.strokePolyline(xPoints, yPoints, xPoints.length);
    }

    // Stroke a line between the first and last points to "close"
    // the polygon if we are drawing it as a line
    public void strokeClosure(GraphicsContext gc) {
        gc.strokeLine(xPoints[0], yPoints[0], 
            xPoints[xPoints.length-1], yPoints[yPoints.length-1]);
    }
}