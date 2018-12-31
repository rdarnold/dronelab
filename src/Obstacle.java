import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Obstacle extends Collidable {
    double heading = 0;
    public String name = "Building"; 
    boolean stroke = true;
    boolean fill = true;
    boolean path = false;
    ColorSet cs = new ColorSet();

    public Obstacle() {
        super();
        init(Constants.ShapeType.POLYGON);
    }

    public Obstacle(Constants.ShapeType newShape) {
        super();
        init(newShape);
    }

    private void init(Constants.ShapeType newShape) {
        switch (newShape) {
            case CIRCLE:
                super.makeCircle(1);
                break;
            case RECT:
                // Actually we treat a rect as just a polygon with 4 sides,
                // this is because we still need it rotated.  A true rect in
                // javaFX is not rotated.
                super.makeRectangle();
                break;
            case POLYGON:
                super.makePolygon();
                break;
        }

        // Default to 15 elevation
        setElevation(15);
    }

    @Override
    public void setElevation(int newEl) {
        super.setElevation(newEl);
        setColors(150, 50, 50);
    }

    public void setColorsToDefault() {
        setColors(150, 50, 50);
    }

    public void setColors(int r, int g, int b) {
        // Fill and outline are based on elevation
        // Higher is less transparent
        double alpha = getElevation() * 4 * 0.01;
        alpha = Utils.clamp(alpha, 0.1, 1.0);
	   // cs.setFill(r, g, b, alpha / 2);
	   // cs.setStroke(r, g, b, alpha);
        cs.setDefaultColors(r, g, b, alpha, alpha/2);
        cs.setColorsToDefault();
    }

    public void drawCircle(GraphicsContext gc) {
        if (path == true) {
            gc.setStroke(cs.getStroke()); 
            gc.setLineWidth(3);
            gc.strokeOval(x() - wid/2, y() - hgt/2, wid, hgt);
        }
        else {
            if (fill == true) {
                gc.setFill(cs.getFill()); 
                gc.fillOval(x() - wid/2, y() - hgt/2, wid, hgt);
            }
            if (stroke == true) {
                gc.setLineWidth(4);
                gc.setStroke(cs.getStroke()); 
                gc.strokeOval(x() - wid/2, y() - hgt/2, wid, hgt);
            }
            if (getSelected() == true) {
                gc.setLineWidth(1);
                gc.setStroke(Color.PINK); 
                int space = 2;
                gc.strokeOval(x() - wid/2 - space, y() - hgt/2 - space, wid + space*2, hgt + space*2);
            }
        }

    }

    public void drawPolygon(GraphicsContext gc) {
        // If path is true we draw only the polyline
        CanvasPolygon poly = getCanvasPolygon();
        if (path == true) {
            gc.setLineWidth(4);
            gc.setStroke(cs.getStroke()); 
            poly.strokeUnclosed(gc);

            // And maybe a smaller line to show the final connection between first and last point.
            gc.setLineWidth(2);
            gc.setStroke(cs.getFill()); 
            poly.strokeClosure(gc);
            //gc.strokeLine(xPoints[0], yPoints[0], 
              //  xPoints[xPoints.length-1], yPoints[yPoints.length-1]);
        } 
        else {
            if (fill == true) {
                gc.setFill(cs.getFill()); 
                //gc.fillPolygon(xPoints, yPoints, xPoints.length);
                poly.fill(gc);
            }
            if (stroke == true) {
                gc.setLineWidth(4);
                gc.setStroke(cs.getStroke()); 
                poly.stroke(gc);
            }
            if (getSelected() == true) {
                gc.setLineWidth(1);
                gc.setStroke(Color.PINK); 
                poly.stroke(gc);
            }
        }
    }
        
    public boolean draw(GraphicsContext gc, Rectangle canvasVisibleRect) {
        // Check if we are within the drawing area.  If not, dont draw.
        if (canvasVisibleRect != null && 
            Physics.rectanglesOverlap(getBoundingRect(), canvasVisibleRect) == false) {
            return false;
        }

        // Only draw if we are within the visible canvas.  The easiest way we are going to
        // do this is to just check to see if any of our points are inside the canvas,
        // if we are super zoomed in this wont work but so what.
        //for (int i = 0; i < xPoints.length; i++) {
          //  if (xPoints[i])
        //}
        //gc.setFill(Color.FIREBRICK);
	    //gc.fillRoundRect(x, y, wid, hgt, 10, 10);
	   /* gc.setLineWidth(2);
	    gc.setStroke(Color.GOLDENROD);
	    gc.strokeRoundRect(x(), y(), wid, hgt, 10, 10);
	    gc.setLineWidth(4);
	    gc.setStroke(Color.FIREBRICK);
	    gc.strokeRoundRect(x() + 2, y() + 2, wid - 4, hgt - 4, 10, 10);*/

        if (isCircle() == true) {
 	        drawCircle(gc);
        }
        else {
            drawPolygon(gc);
        }
        super.draw(gc);
        //List<Double> coordinates = poly.getPoints();
        //System.out.println(coordinates);
        //for (int i = 0 ; i < coordinates.size(); i++) {

        //}

        /*double xpoints[] = {rect.getBoundsInLocal().getMinX(), rect.getBoundsInLocal().getMaxX()};
        double ypoints[] = {85, 75, 10, 75, 85, 125,
            190, 150, 190, 125};
        gc.strokePolygon(xpoints, ypoints, xpoints.length);*/
        return true;
    }    
}