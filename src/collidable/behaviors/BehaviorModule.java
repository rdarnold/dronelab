package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public abstract class BehaviorModule {
    protected String name;
    protected String nameJapanese;
    protected Drone drone; // the owner of this module
    protected int leftBound = 0;
    protected int rightBound = 0;
    protected int topBound = 0;
    protected int bottomBound = 0;
    protected String drawLetter = "B";

    // Basically where does this one sit in the behavior list?
    // this is for drawing purposes only; the list order itself is the
    // simple determination of behavior priority.
    private int nDrawPriority = 1;

    public BehaviorModule(String strNameEnglish, String strNameJapanese) {
        name = strNameEnglish;
        nameJapanese = strNameJapanese;
    }

    public void assign(Drone d) {
        drone = d;
    }

    public void setDrawPriority(int pri) {
        nDrawPriority = pri;
    }

    public String getName() { return name; }
    public String getNameJapanese() { return nameJapanese; }

    // How does this module react to circumstances, basically.
    public abstract boolean react();
    public abstract boolean draw(GraphicsContext gc);
    public abstract boolean usesDrawLocation();
    public abstract boolean setup(); // Called when Drone.setDroneType is called
    public abstract boolean reset(); // Called when Drone.reset is called
    public abstract boolean receive(String msg); // Called when drone receives wifi message directed to this module
    
    // Optional to overload this or not.  It is called when a person
    // is detected; some behavior modules may find this useful.
    public void onPersonDetected(Person p, ArrayList<Point2D> peopleLocations) { }

    // Every behavior module can have bounds, though it doesn't have to set or use them.
    public void setBounds(int left, int top, int right, int bottom)
    {
        leftBound = left;
        topBound = top;
        rightBound = right;
        bottomBound = bottom;
    }

    // If this behavior has an associated location, like a destination point,
    // draw it commonly using this method.
    public boolean drawLocation(GraphicsContext gc, int x, int y) {
        int colorAmt = GraphicsHelper.getFlashColorLevel();
        Color fillCol = Color.rgb(0, 0, colorAmt, 0.7);
        Color strokeCol = Color.rgb(colorAmt, colorAmt, colorAmt, 0.8);

	    //gc.setFill(Color.rgb(0, 0, 255, 0.7));
        //gc.setStroke(Color.rgb(255, 255, 255, 0.8));
	    gc.setFill(fillCol);
        gc.setStroke(strokeCol);
        gc.setLineWidth(2);

        int wid = (int)20;
        int hgt = (int)20;
        int drawX = x - wid/2;
        int drawY = y - hgt/2;

        gc.fillOval(drawX, drawY, wid, hgt);

        int off = 4;
        //gc.strokeText("" + nDrawPriority, x - off, y + off);
        gc.strokeText(drawLetter, x - off, y + off);

        // And now draw the priority number
        //int off = 4;
        //gc.strokeLine(x+off, y+off, x+wid-off, y+hgt-off);
        //gc.strokeLine(x + wid - +off, y+off, x+off, y+hgt-off);
        return true;
    }
}