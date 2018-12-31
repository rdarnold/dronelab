package dronelab.gui;

import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;


// This class is for drawing the distance scale on the main map,
// to help the user understand the distances between objects on
// the map.  It can be set to have different distances and such
// depending on what the user wants.
public class DistanceScale {

    private enum ScaleUnit {
        METERS, FEET
    }

    private Scenario scenario;
    private ScaleUnit unit = ScaleUnit.METERS;
    private int distance = 100;

    public DistanceScale(Scenario scen) {
        scenario = scen;
    }

    public void useFeet() {
        unit = ScaleUnit.FEET;
    }

    public void useMeters() {
        unit = ScaleUnit.METERS;
    }

    public void setDistance(int newDist) {
        distance = newDist;
    }

    public void drawLine(GraphicsContext gc, double x, double y) {
        double lineLength = 0; //scenario.currentWidth / 10;

        // So now we determine how long this thing should be
        // based on various factors
        switch (unit) {
            case METERS:
                lineLength = scenario.currentWidth * ((double)distance / Distance.WIDTH_IN_METERS);
                // This line would also work.  Keeping it as the above just for sanity checking.
                //lineLength = Distance.pixelsFromMeters((double)distance);
                break;
            case FEET:
                lineLength = scenario.currentWidth * ((double)distance / Distance.WIDTH_IN_FEET);
                break;
        }
	    gc.strokeLine(x, y, x + lineLength, y);

        // And draw the little ends
	    gc.strokeLine(x, y - 5, x, y + 5);
	    gc.strokeLine(x + lineLength, y - 5, x + lineLength, y + 5);
    }

    public void drawUnit(GraphicsContext gc, double x, double y) {
        switch (unit) {
            case METERS:
                gc.fillText("" + distance + "m", x, y);
                break;
            case FEET:
                gc.fillText("" + distance + "f", x, y);
                break;
        }
    }

    public void draw(GraphicsContext gc, Rectangle canvasVisibleRect) {
        // Draw the 200m scale or the 2000m scale or whatever it is we want.
        // Our whole map is 2000m so we will just say 10% of that is 200m
        // and use that as a general scale factor.
        Font oldFont = gc.getFont();
        TextAlignment oldAlign = gc.getTextAlign();
        VPos oldVPos = gc.getTextBaseline();

        // This should be based on zoom factor.
        double zoom = scenario.zoomFactor;

	    gc.setStroke(Color.BLACK);
        double x = canvasVisibleRect.getX() + (30.0 / zoom);
        double y = canvasVisibleRect.getY() + (30.0 / zoom);
        double unitX = x + 80;
        double unitY = y + (12 / zoom);

        // 2.25, 1.5, 1.0 too big, .66 good,, .44, too small, .296, .197 too small, 0.13 too small
        if (zoom >= 2.25) {
	        gc.setLineWidth(2);
            gc.setFont(Font.font ("Verdana", 8));
        }
        else if (zoom >= 1.5) {
	        gc.setLineWidth(3);
            gc.setFont(Font.font ("Verdana", 10));
        }
        else if (zoom >= 1.0) {
	        gc.setLineWidth(4);
            gc.setFont(Font.font ("Verdana", 12));
        }
        else if (zoom > 0.66) {
	        gc.setLineWidth(4);
            gc.setFont(Font.font ("Verdana", 16));
        }
        else if (zoom > 0.44) {
	        gc.setLineWidth(6);
            gc.setFont(Font.font ("Verdana", 24));
        }
        else if (zoom > 0.29) {
	        gc.setLineWidth(10);
            gc.setFont(Font.font ("Verdana", 36));
        }
        else {
            // 0.197. 0.13, 
	        gc.setLineWidth(14);
            gc.setFont(Font.font ("Verdana", 52));
        }

        // Could also draw it on the bottom right if we wanted.
        //canvasVisibleRect.getWidth()
        //canvasVisibleRect.getHeight()
        drawLine(gc, x, y);

        gc.setFill(Color.BLACK);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        drawUnit(gc, unitX, unitY);
        //gc.setEffect(null);

        // Reset to the originals.
        gc.setFont(oldFont);
        gc.setTextAlign(oldAlign);
        gc.setTextBaseline(oldVPos);
    }
}


