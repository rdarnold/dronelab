package dronelab.collidable;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import dronelab.utils.*;

public class Person extends Mobile {
    ColorSet cs;

    // Should these bit in a bit flag?
    // Just had the same thought months later, these should be a bit flag.
    private boolean seen = false;  // Camera has seen it but drone doesnt know
    private boolean located = false; // Person confirmed as found with FINDER sensor
    private boolean rescued = false;
    //private boolean distressed = false;

    public Person() {
        super();
        init();
    }

    public Person(Person copyFrom) {
        super();
        setStartPos(copyFrom.getStartingX(), copyFrom.getStartingY(), copyFrom.getStartingElevation());
        init();
    }

    private void init() {
        // For now lets just make it a circle, well have to figure
        // out some other way later, maybe a polygon or an obj with multiple
        // shapes or just a rect
        super.makeCircle(Constants.personWidth / 2);
        cs = new ColorSet();
        reset();
	    //setColors(flashCounter);
    }

    @Override
    public void reset() {
        super.reset();
        seen = false;
        located = false;
        rescued = false;
        assignCorrectColorsForStatus();
    }

    public boolean isSeen()     { return seen; }
    public boolean isLocated()  { return located; }
    public boolean isRescued()  { return rescued; }

    public void setRescued() {
        rescued = true;
        assignCorrectColorsForStatus();
    }

    public void setLocated() {
        located = true;
        assignCorrectColorsForStatus();
    }

    public void setSeen() {
        seen = true;
        assignCorrectColorsForStatus();
    }

    private void assignCorrectColorsForStatus() {
        if (cs == null) {
            return;
        }
        if (rescued == true) {
            setColors(0, 0, 230); // located
        } else if (located == true) {
            setColors(0, 230, 0); // located
        } else if (seen == true) {
            setColors(230, 230, 0); // seen
        } else {
            setColors(148, 148, 148);
        }
    }

    public void setColors(int r, int g, int b) {
        cs.setStroke(r, g, b);
        cs.setFill(r, g, b);
    }

    public void draw(GraphicsContext gc) {
        if (located == true || seen == true || rescued == true) {
            draw(gc, cs.getFill(), cs.getStroke());
        } 
        else {
            Color color = Color.rgb(GraphicsHelper.getFlashColorLevel(), 0, 0);
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
        /*gc.strokeLine(x + wid / 2, y + hgt, x,       y + hgt*1.2);
        gc.strokeLine(x + wid / 2, y + hgt, x + wid, y + hgt*1.2);

        // body
        gc.strokeLine(x + wid / 2, y + hgt, x + wid / 2, y + hgt*1.5);
        
        // legs
        gc.strokeLine(x + wid / 2, y + hgt*1.5, x, y + hgt * 2);
        gc.strokeLine(x + wid / 2, y + hgt*1.5, x + wid, y + hgt * 2);   */
        super.draw(gc);
    }
}