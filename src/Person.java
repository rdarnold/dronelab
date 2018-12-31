import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

public class Person extends Mobile {
    ColorSet cs;
    boolean located = false;
    boolean rescued = false;

    public Person() {
        super();
        // For now lets just make it a circle, well have to figure
        // out some other way later, maybe a polygon or an obj with multiple
        // shapes or just a rect
        super.makeCircle(Constants.personWidth / 2);

        cs = new ColorSet();
        setColors(148, 148, 148);
	    //setColors(flashCounter);
    }

    @Override
    public void reset() {
        super.reset();
        located = false;
        rescued = false;
    }

    public void setColors(int r, int g, int b) {
        cs.setStroke(r, g, b);
        cs.setFill(r, g, b);
    }

    public void detected() {
        located = true;
    }

    public void drawDefault(GraphicsContext gc) {
        draw(gc, cs.getFill(), cs.getStroke());
    }

    public void draw(GraphicsContext gc) {
	    drawDefault(gc);
    }

    public void draw(GraphicsContext gc, Color col) {
        if (located == true) {
            // We cant call the regular draw(gc) here because it
            // may call back into the subclass!  So we have to
            // call the other draw down below and hope it wasnt overridden...
            drawDefault(gc);
        } 
        else {
            draw(gc, col, col);
        }
    }
    
    public void draw(GraphicsContext gc, Color fillCol, Color strokeCol) {
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