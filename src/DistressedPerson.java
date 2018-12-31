
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

public class DistressedPerson extends Person {

    public DistressedPerson() {
        super();

        setColors(0, 230, 0);
    }

    @Override
    public void draw(GraphicsContext gc) {
        //super.draw(gc);
        super.draw(gc, Color.rgb(GraphicsHelper.getFlashColorLevel(), 0, 0));
    }

    //public void draw(GraphicsContext gc, Color col) {
	 //   super.draw(gc, col);
    //}
}