
    
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.List;
import javafx.geometry.Point2D;

// Rectangle used for bounding the entire volume of Collidables.
// It knows how to form itself properly for different types of Collidables.
// It also knows how to draw itself.
// That is all.
public class BoundingRectangle extends Rectangle {
    public void set(Collidable col) {
        switch (col.getShapeType()) {
            case CIRCLE:
                this.setX(col.x() - col.wid/2);
                this.setY(col.y() - col.hgt/2);
                this.setWidth(col.wid);
                this.setHeight(col.hgt);
                break;
            case RECT:
            case POLYGON:
                // Go through our points, find the top, left, bottom, and right.
                // this becomes our rectangle.
                List<Double> points = col.getCanvasPolygon().getPoints();
                if (points.size() <= 0) {
                    return;
                }
                double left = points.get(0);
                double right = points.get(0);
                double top = points.get(1);
                double bottom = points.get(1);
                boolean isX = true;
                for (Double point : points) {
                    if (isX == true) {
                        if (point < left)
                            left = point;
                        else if (point > right)
                            right = point;
                    } else {
                        if (point < top)
                            top = point;
                        else if (point > bottom)
                            bottom = point;
                    }
                    isX = !isX;
                }

                this.setX(left);
                this.setY(top);
                this.setWidth(right - left);
                this.setHeight(bottom - top);
                break;
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setLineWidth(2);
        gc.setStroke(Color.PURPLE); 
        gc.strokeRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}