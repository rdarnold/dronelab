package dronelab._3d;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Box;
import dronelab.utils.*;
import dronelab.collidable.*;

public class SimBox extends Box {
    private Obstacle obstacle;

    public SimBox(Obstacle obs) {
        super(obs.getElevation(), obs.hgt, obs.wid);
        obstacle = obs;

        // They are along the Y axis so we need to rotate..
        Rotate rx = new Rotate();
        rx.setAxis(Rotate.X_AXIS);
        rx.setAngle(90);
        getTransforms().add(rx); 

        // But also we need to rotate about the z axis to reflect
        // their rotation angle.
        Rotate rz = new Rotate();
        rz.setAxis(Rotate.Z_AXIS);
        rz.setAngle(90);
        getTransforms().add(rz); 
        
        setDrawMode(DrawMode.FILL);
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseMap(GraphicsHelper.wallTexture1); //6000, 0, t
        setMaterial(mat);
        update();
    }
    
    public void update() {
        setTranslateX(obstacle.getX());
        setTranslateY(obstacle.getY());
        setTranslateZ(-1 * (obstacle.getElevation() / 2));
    }
}