import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import java.util.List;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import javafx.scene.transform.Rotate;

public class CanvasRectangle extends CanvasPolygon {
    // This is a CanvasPolygon but then it
    // is actually bounded by a rectangle shape.
    Rectangle originalRect;
    double origX, origY, origWid, origLen;
    double angleDegrees = 0;
    Point2D rotationOriginPoint;

    public void make(double x, double y, double wid, double len) {
        make(x, y, wid, len, 0);
    }

    public void make(double x, double y, double wid, double len, double angle) {
        updateRotationOrigin(0, 0);
        originalRect = new Rectangle(wid, len);
        // add 4 points 
        super.addPoint(x, y);
        super.addPoint(x, y);
        super.addPoint(x, y);
        super.addPoint(x, y);
        update(x, y, wid, len, angle);
    }

    public void update(double x, double y) {
        update(x, y, origWid, origLen, angleDegrees);
    }

    public void update(double x, double y, double wid, double len) {
        update(x, y, wid, len, angleDegrees);
    }

    public void update(double x, double y, double wid, double len, double angle) {
        angleDegrees = angle;
        updateOriginalPos(x, y);
        updateOriginalSize(wid, len);
    }

    public void updateRotationOrigin(double newX, double newY) {
        rotationOriginPoint = new Point2D(newX, newY);
    }

    public void updateOriginalPos(double x, double y) {
        origX = x;
        origY = y;
        originalRect.setX(x);
        originalRect.setY(y);
        super.updatePoint(0, x, y);
        rotate(angleDegrees);
    }

    public void updateOriginalSize(double wid, double len) {
        originalRect.setWidth(wid);
        originalRect.setHeight(len);
        origWid = wid;
        origLen = len;
        rotate(angleDegrees);
    }

    // We take the original rectangle and rotate it by the
    // specified degrees to create a new polygon.  More or less.
    public void rotate(double newAngleDegrees) {
        angleDegrees = newAngleDegrees;
        originalRect.getTransforms().clear();
        originalRect.getTransforms().add(new Rotate(angleDegrees, rotationOriginPoint.getX(), rotationOriginPoint.getY())); 

        // find out where is coordinate in rotated 
        //Point2D localToParent = rect.localToParent(lastWidth, 0);
        //Utils.log("" + (x + lastWidth) + ", " + (x + localToParent.getX()) + ", " + y + ", " + (y + localToParent.getY()));
        // Now create a polygon from that rect.
        Point2D point;

        point = originalRect.localToParent(origWid, 0);
        super.updatePoint(1, origX + point.getX(), origY + point.getY());

        point = originalRect.localToParent(origWid, origLen);
        super.updatePoint(2, origX + point.getX(), origY + point.getY());

        point = originalRect.localToParent(0, origLen);
        super.updatePoint(3, origX + point.getX(), origY + point.getY());
    }

    @Override
    public void moveBy(double x, double y) {
        update(origX + x, origY + y);
    }
}