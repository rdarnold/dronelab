package dronelab;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Collision  {
    int x = 0;
    int y = 0;
    private int size = 10;
    long time = 0;
    Color col = Color.RED;
    double alpha = 1.0;
    // Could put more data here like the type of collision, etc.
    
    public Collision(int collidingX, int collidingY) {
        x = collidingX;
        y = collidingY;
    }
    
    public Collision(double collidingX, double collidingY) {
        x = (int)collidingX;
        y = (int)collidingY;
    }

    // Return of false means its time for me to die
    public boolean update() {
        // Basically my transparency grows over time
        alpha -= 0.02;
        if (alpha <= 0.00) {
            return false;
        }
        col = Color.rgb(255, 0, 0, alpha);
        return true;
    }

    public void draw(GraphicsContext gc) {
        //gc.setFill(Color.FIREBRICK);
	    //gc.fillRoundRect(x, y, wid, hgt, 10, 10);
        /*
	    gc.setLineWidth(4);
	    gc.setStroke(Color.RED);
	    gc.strokeLine(x - size/2, y - size/2, x + size/2, y + size/2);
	    gc.strokeLine(x + size/2, y - size/2, x - size/2, y + size/2);*/
	    gc.setStroke(col);
        int x1, y1, x2, y2;

        x1 = x - size/2;
        y1 = y - size/2;
        x2 = x + size/2;
        y2 = y + size/2;

	    gc.setLineWidth(2);
	    gc.strokeLine(x1, y1, x2, y2);
	    //gc.setLineWidth(2);
	    //gc.strokeLine(x1+1, y1+1, x2-1, y2-1);
	    gc.setLineWidth(3);
	    gc.strokeLine(x1+2, y1+2, x2-2, y2-2);
	    //gc.setLineWidth(4);
	    //gc.strokeLine(x1+3, y1+3, x2-3, y2-3);
        
        x1 = x + size/2;
        y1 = y - size/2;
        x2 = x - size/2;
        y2 = y + size/2;
        
	    gc.setLineWidth(2);
	    gc.strokeLine(x1, y1, x2, y2);
	    //gc.setLineWidth(2);
	    //gc.strokeLine(x1-1, y1+1, x2+1, y2-1);
	    gc.setLineWidth(3);
	    gc.strokeLine(x1-2, y1+2, x2+2, y2-2);
	    //gc.setLineWidth(4);
	    //gc.strokeLine(x1-3, y1+3, x2+3, y2-3);
    }    
}