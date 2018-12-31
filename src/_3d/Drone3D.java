package dronelab._3d;

import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import dronelab.collidable.Drone;
import dronelab.utils.*;

public class Drone3D {
    public Drone drone;
    public Box box1;
    public Box box2;
    public Sphere sphere;
    private PointLight light = new PointLight();

    private Group root;

    Rotate rz1;
    Rotate rz2;
    double rz1Angle = 90;
    double rz2Angle = 0;

    // This is whatever we want the drone to be in our 3d space.
    // I'll try two spinning boxes.
    public Drone3D(Drone d, Group root) {
        //super(d.wid/4);
        setDrone(d);

        setup(root);
        update();
    }

    public void setDrone(Drone d) {
        drone = d;
    }

    public void removeFromRoot() {
        root.getChildren().remove(sphere);
        root.getChildren().remove(box1);
        root.getChildren().remove(box2);
        root.getChildren().remove(light);
    }

    public void setup(Group rootGroup) {
        root = rootGroup;
        rz1 = new Rotate();
        rz1.setAxis(Rotate.Z_AXIS);
        rz1.setAngle(rz1Angle);

        rz2 = new Rotate();
        rz2.setAxis(Rotate.Z_AXIS);
        rz2.setAngle(rz2Angle);

        double width = 20;
        if (drone != null) {
            width = drone.wid;
        }
        double height = 2;
        double depth = 1;
        box1 = new Box(width, height, depth);
        box2 = new Box(width, height, depth);

        box1.getTransforms().add(rz1);
        box2.getTransforms().add(rz2);

        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(Color.WHITE); //new Color(Math.random(),Math.random(),Math.random(),1));
        mat.setSpecularColor(Color.BLUE);

        box1.setDrawMode(DrawMode.FILL);
        box2.setDrawMode(DrawMode.FILL);
        box1.setMaterial(mat);
        box2.setMaterial(mat);

        sphere = new Sphere(3);
        sphere.setMaterial(mat);

        root.getChildren().addAll(box1, box2, sphere);

        setupLight(root);
    }

    private void setupLight(Group root) {
        //PointLight light = new PointLight();
        light = new PointLight();
        light.setColor(Color.WHITE);
        root.getChildren().add(light);
    }

    public void update() {
        if (drone == null)
            return;
        box1.setTranslateX(drone.getX()); // + drone.wid / 2);
        box1.setTranslateY(drone.getY()); // + drone.hgt / 2);
        box1.setTranslateZ(-1 * (drone.getElevation()));

        box2.setTranslateX(drone.getX()); // + drone.wid / 2);
        box2.setTranslateY(drone.getY()); // + drone.hgt / 2);
        box2.setTranslateZ(-1 * (drone.getElevation()));

        sphere.setTranslateX(drone.getX()); // + drone.wid / 2);
        sphere.setTranslateY(drone.getY()); // + drone.hgt / 2);
        sphere.setTranslateZ(3 + -1 * (drone.getElevation()));

        // Move our light to the right spot...
        light.setTranslateX(drone.getX());
        light.setTranslateY(drone.getY());
        light.setTranslateZ(-1 * (drone.getElevation()));

        if (drone.isFlying() == true) {
            double turn = 13;
            rz1Angle += turn;
            rz2Angle += turn;

            rz1Angle = Utils.normalizeAngle(rz1Angle);
            rz2Angle = Utils.normalizeAngle(rz2Angle);

            // And update our rotation...
            rz1.setAngle(rz1Angle);
            rz2.setAngle(rz2Angle);
        }
    }

    public boolean setLightOn(boolean turnOn) {
        light.setLightOn(turnOn);
        return turnOn;
    }

    public void turnLightOn() {
        light.setLightOn(true);
    }

    public void turnLightOff() {
        light.setLightOn(false);
    }
}