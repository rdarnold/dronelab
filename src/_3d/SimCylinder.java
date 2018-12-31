package dronelab._3d;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import dronelab.utils.*;
import dronelab.collidable.*;

public class SimCylinder extends Cylinder {
    private Obstacle obstacle;

    public SimCylinder(Obstacle obs) {
        super(obs.getRadius(), obs.getElevation());
        obstacle = obs;

        // They are along the Y axis so we need to rotate..
        Rotate rx = new Rotate();
        rx.setAxis(Rotate.X_AXIS);
        rx.setAngle(90);
        getTransforms().add(rx); 
        
        setDrawMode(DrawMode.FILL);
        PhongMaterial mat = new PhongMaterial();
        //mat.setDiffuseColor(new Color(Math.random(),Math.random(),Math.random(), 1));
        //mat.setSpecularColor(new Color(Math.random(),Math.random(),Math.random(), 1));
        //mat.setDiffuseColor(Color.rgb(150, 150, 0));
        //mat.setSpecularColor(Color.rgb(250, 250, 0));
        mat.setDiffuseMap(GraphicsHelper.steelTexture1); //6000, 0, true, true);
        //mat.setDiffuseColor(Color.rgb(150, 50, 50, 1));
        //mat.setSpecularColor(Color.PINK);
        setMaterial(mat);
        update();
    }
    
    public void update() {
        setTranslateX(obstacle.getX());
        setTranslateY(obstacle.getY());
        setTranslateZ(-1 * (obstacle.getElevation() / 2));
    }
}