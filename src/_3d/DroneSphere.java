package dronelab._3d;

import javafx.scene.shape.Sphere;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import dronelab.collidable.Drone;

public class DroneSphere extends Sphere {
    public Drone drone;

    public DroneSphere(Drone d) {
        super(d.wid/4);
        drone = d;

        setDrawMode(DrawMode.FILL);
        PhongMaterial m = new PhongMaterial();
        m.setDiffuseColor(Color.WHITE); //new Color(Math.random(),Math.random(),Math.random(),1));
        m.setSpecularColor(Color.WHITE);
        setMaterial(m);

        update();
    }

    public void update() {
        setTranslateX(drone.getX() + drone.wid / 4);
        setTranslateY(drone.getY() + drone.hgt / 4);
        setTranslateZ(-1 * (drone.getElevation()));
    }
}