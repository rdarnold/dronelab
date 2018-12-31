package dronelab.collidable;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.List;
import javafx.geometry.Point2D;

import dronelab.utils.*;

public class Collidable {
    protected String name; 
    protected long id = 0;
    public double wid = 0;
    public double hgt = 0;
    private boolean selected = false;
    protected double elevation = 0;
    private BoundingRectangle boundingRect;
    private boolean drawBoundingRect = false;
    private Shape shape;
    private Constants.ShapeType shapeType = Constants.ShapeType.POLYGON;

    public Collidable() {
        boundingRect = new BoundingRectangle();
        id = Constants.getNextId();
    }

    public double getX() { 
        switch (shapeType) {
            case CIRCLE:
                return shape.getLayoutX(); 
            case RECT:
                return getCanvasRectangle().getLeftX();
            case POLYGON:
                return getCanvasPolygon().getLeftX();
        }
        return shape.getLayoutX(); 
    }
    public double getY() { 
        switch (shapeType) {
            case CIRCLE:
                return shape.getLayoutY(); 
            case RECT:
                return getCanvasRectangle().getLeftY();
            case POLYGON:
                return getCanvasPolygon().getLeftY();
        }
        return shape.getLayoutY(); 
    }
    public double getZ()    { return getElevation(); }
    public double x()       { return getX(); }
    public double y()       { return getY(); }
    public double z()       { return getZ(); }

    public String getName() { return name; }
    public void setName(String strName) { name = strName; }

    public void select()                    { selected = true; }
    public void unselect()                  { selected = false; }
    public void setSelected(boolean sel)    { selected = sel;  }
    public boolean getSelected()            { return selected; }

    public long getId() { return id; }
    public void setId(long nId) {
        id = nId;
    }

    public boolean isVisibleOnScreen(Rectangle canvasVisibleRect) {
        if (canvasVisibleRect != null && 
            Physics.rectanglesOverlap(boundingRect, canvasVisibleRect) == false) {
            return false;
        }
        return true;
    }

    public double getElevation() { return elevation; }

    // This is override and called by subclasses
    public void setElevation(double newEl) {
        elevation = newEl;
    }
    
    public void setElevation(int newEl) {
        elevation = (double)newEl;
    }
        
    public void setX(int newX) {
        setPos(newX, y());
    }

    public void setY(int newY) {
        setPos(x(), newY);
    }

    public void setPos(int newX, int newY) {
        setPos((double)newX, (double)newY);
    }

    public void setPos(double newX, double newY) {
        switch (shapeType) {
            case CIRCLE:
                // You have to take into account the width and length
                // Remember this is strange and auto adjusts so we have to adjust back
                shape.relocate(newX - wid/2, newY - hgt/2);
                setBoundingRect();
                break;
            case RECT:
                // For a rectangle we need to update the base rect.
                if (getCanvasRectangle() != null) {
                    getCanvasRectangle().updateOriginalPos(newX, newY);
                }
                break;
            case POLYGON:
                // Do nothing, its just a series of lines, it has no pos
                break;
        }
    }

    public void moveBy(double moveX, double moveY) {
        switch (getShapeType()) {
            case CIRCLE:
                setPos(x() + moveX, y() + moveY);
                break;
            case RECT:
                CanvasRectangle rect = getCanvasRectangle();
                rect.moveBy(moveX, moveY);
                //selectedObs.setPos(newX, newY);
                break;
            case POLYGON:
                CanvasPolygon poly = getCanvasPolygon();
                poly.moveBy(moveX, moveY);
                break;
        }
        setBoundingRect();
    }

    public void setSize(int newSize) {
        setSize(newSize, newSize);
    }

    public void setSize(int newWid, int newHgt) {
        // If its a circle, try to set the radius.  If its not,
        // this will just do nothing.
        switch (shapeType) {
            case CIRCLE:
        	    setRadius(newWid / 2);
                break;
            case RECT:
                CanvasRectangle rect = getCanvasRectangle();
                wid = newWid;
                hgt = newHgt;
                rect.updateOriginalSize(wid, hgt);
                break;
            case POLYGON:
                wid = newWid;
                hgt = newHgt;
                break;
        }
 	    setBoundingRect();
    }

    public boolean setRadius(int radius) {
        Circle circle = getCircle();
        if (circle != null) {
            circle.setRadius(radius);
            wid = radius * 2;
            hgt = radius * 2;
            setBoundingRect();
            return true;
        }
        return false;
    }

    public void resize(double addX, double addY) {
        // Not a simple set size as we need to see what we
        // already have.
        switch (shapeType) {
            case CIRCLE:
                // This isnt a good way to resize a circle. We dont use it.
                break;
            case RECT:
                CanvasRectangle rect = getCanvasRectangle();
                wid = rect.origWid + addX;
                hgt = rect.origLen + addY;
                rect.updateOriginalSize(wid, hgt);
                break;
            case POLYGON:
                // Polygon doesnt have size in this sense.
                break;
        }
 	    setBoundingRect();
    }

    public Rectangle getBoundingRect() { return boundingRect; }

    public void setBoundingRect() {
	    boundingRect.set(this);
    }

    public double getRadius() {
        Circle circle = getCircle();
        if (circle == null) {
            return 0;
        }
        return circle.getRadius();
    }

    public boolean isCircle()       { return (shapeType == Constants.ShapeType.CIRCLE);   }
    public boolean isRectangle()    { return (shapeType == Constants.ShapeType.RECT);     }
    public boolean isPolygon()      { return (shapeType == Constants.ShapeType.POLYGON);  }

    public Constants.ShapeType getShapeType() { return shapeType; }
    public Shape getShape() { return shape; }

    public Circle getCircle() {
        if (shapeType != Constants.ShapeType.CIRCLE) {
            return null;
        }
        return (Circle)shape;
    }

    public CanvasRectangle getCanvasRectangle() {
        if (shapeType != Constants.ShapeType.RECT) {
            return null;
        }
        return (CanvasRectangle)shape;
    }

    public CanvasPolygon getCanvasPolygon() {
        // A CanvasRect is also a polygon so we can return that here if we need to.
        if (shapeType != Constants.ShapeType.POLYGON && 
            shapeType != Constants.ShapeType.RECT) {
            return null;
        }
        return (CanvasPolygon)shape;
    }

    //Arc, Circle, CubicCurve, Ellipse, Line, Path, Polygon, Polyline, QuadCurve, Rectangle, SVGPath, Text
    public Circle makeCircle(double radius) {
        return makeCircle(0, 0, radius);
    }

    public Circle makeCircle(double x, double y, double radius) {
        shapeType = Constants.ShapeType.CIRCLE;
        shape = new Circle(radius);
        setSize((int)radius * 2);
        setPos(x, y);
        setBoundingRect();
        return (Circle)shape;
    }

    public CanvasRectangle makeRectangle() {
        return makeRectangle(0, 0, 1, 1, 0);
    }

    public CanvasRectangle makeRectangle(int x, int y, int wid, int hgt) {
        return makeRectangle(x, y, wid, hgt, 0);
    }

    public CanvasRectangle makeRectangle(int x, int y, int wid, int hgt, int angle) {
        shapeType = Constants.ShapeType.RECT;
        shape = new CanvasRectangle();
        CanvasRectangle rect = getCanvasRectangle();
        rect.make(x, y, wid, hgt, angle);
        setBoundingRect();
        return rect;
    }

    public CanvasPolygon makePolygon() {
        shapeType = Constants.ShapeType.POLYGON;
        shape = new CanvasPolygon();
        return (CanvasPolygon)shape;
    }

    // Only want to call this draw method from within subclasses...
    protected void draw(GraphicsContext gc) {
        if (drawBoundingRect == true) {
            boundingRect.draw(gc);
        }
    }

    // Rotate, if we do that
    public void rotate(double angleDegrees) {
        CanvasRectangle rect = getCanvasRectangle();
        if (rect == null) {
            return;
        }
        rect.rotate(angleDegrees);
        setBoundingRect();
    }

    public double getXPoint(int slot) {
        CanvasPolygon poly = getCanvasPolygon();
        return poly.getXPoint(slot);
    }

    public double getYPoint(int slot) {
        CanvasPolygon poly = getCanvasPolygon();
        return poly.getYPoint(slot);
    }

    public void addPoint(Point2D point) {
        addPoint(point.getX(), point.getY());
    }

    public void addPoint(int x, int y) {
        addPoint((double)x, (double)y);
    }

    public void addPoint(double x, double y) {
        if (isCircle() == true) {
            return;
        }

        CanvasPolygon poly = getCanvasPolygon();
        poly.addPoint(x, y);

        setBoundingRect();
    }
}