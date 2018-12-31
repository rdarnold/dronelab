package dronelab._3d;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.application.ConditionalFeature;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.transform.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.AmbientLight;
import javafx.scene.PointLight;
import dronelab.utils.*;
import dronelab.collidable.*;
import dronelab.Scenario;

public class PerspectiveViewer {

    private SubScene subScene;
    Scenario scenario;
    ArrayList<ObstacleMesh> obstacleMeshes;
    ArrayList<SimCylinder> obstacleCylinders;
    PerspectiveCamera camera;
    CameraGroup cameraGroup;
    Box backgroundBox;
    //DroneSphere droneSphere;
    ArrayList<Drone3D> drones;
    Drone3D drone3D;
    Gui3D gui3D;

    Group rootGroup;

    private double mouseX = 0;
    private double mouseY = 0;
    private double mouseOldX = 0;
    private double mouseOldY = 0;
    private double mouseDeltaX = 0;
    private double mouseDeltaY = 0;

    public PerspectiveViewer(Scenario scen) {
        scenario = scen;
        obstacleMeshes = new ArrayList<ObstacleMesh>();
        obstacleCylinders = new ArrayList<SimCylinder>();
        drones = new ArrayList<Drone3D>();
        create();
    }

    public SubScene getSubScene() { return subScene; }

    private void setupBackground(Group root) {
        // Set up our background
        //Appearance app = new Appearance();
        //URL texImage = new java.net.URL("file:" + f);
        PhongMaterial mat = new PhongMaterial(); 
        mat.setDiffuseMap(scenario.backGround);
        //mat.setDiffuseMap(new Image(
          //  getClass().getResource(Constants.RES_PATH + "Scenario1_BG_Sendai.jpg").toExternalForm())); 

        /*Texture tex = new TextureLoader(Constants.RES_PATH + "Scenario1_BG_Sendai.jpg", this).getTexture();
        app.setTexture(tex);
        TextureAttributes texAttr = new TextureAttributes();
        texAttr.setTextureMode(TextureAttributes.MODULATE);
        app.setTextureAttributes(texAttr);*/
        backgroundBox = new Box(scenario.currentWidth, scenario.currentHeight, 1);
        // Fix this so that 0, 0 is top left like our regular scene
        backgroundBox.setTranslateX(scenario.currentWidth / 2);
        backgroundBox.setTranslateY(scenario.currentHeight / 2);

       // backgroundBox.setTranslateY(0);
        backgroundBox.setMaterial(mat);
        root.getChildren().add(backgroundBox);
    }

    private void setupCamera(Group root) {
        // Create and position camera
        camera = new PerspectiveCamera(true);
        cameraGroup = new CameraGroup();       
        cameraGroup.getChildren().add(camera);
        root.getChildren().add(cameraGroup);
 	    
        //camera.rotate(5);
        camera.setFieldOfView(62); // Standard 40-62
        camera.setNearClip(1); // If this is zero the z-order doesnt work...
        camera.setFarClip(3000);

        cameraGroup.setPos(1200, 2500, -100);
        cameraGroup.lookAt(scenario.drone);
    }

    private void setupLights(Group root) {
        //PointLight light = new PointLight();
        PointLight light = new PointLight();
        light.setColor(Color.WHITE);
        light.setTranslateX(800);
        light.setTranslateY(2000);
        light.setTranslateZ(-2000);
        root.getChildren().add(light);
    }
    private void create3DObjects(Group root) {
        //drone3D = new Drone3D(scenario.drone, root);
        createDrones(root);
        //droneSphere = new DroneSphere(scenario.drone);
        //root.getChildren().add(droneSphere);

        for (Obstacle obs : scenario.obstacles) {
            /*SimBox box = new SimBox(50, 50, obs.getElevation());
            box.theObs = obs;
            shapes.add(box);
            //box.setMaterial(new PhongMaterial(Color.RED));
            box.setDrawMode(DrawMode.FILL);

            box.setTranslateX(obs.getX());
            box.setTranslateY(obs.getY()); // Since the coordinates are in the middle ...
            box.setTranslateZ(-1 * (obs.getElevation() / 2));
            PhongMaterial m = new PhongMaterial();
            m.setDiffuseColor(new Color(Math.random(),Math.random(),Math.random(),1));
            m.setSpecularColor(Color.BLACK);
            box.setMaterial(m);
            root.getChildren().add(box);*/


            // Maybe for ones that are exact rectangles we just use SimBox instead of
            // building our own meshes.
            switch (obs.getShapeType()) {
                case CIRCLE:
                    // Cylinder...
                    SimCylinder cyl = new SimCylinder(obs);
                    obstacleCylinders.add(cyl);
                    root.getChildren().add(cyl);
                    break;
                default:
                    ObstacleMesh bm = new ObstacleMesh(obs);
                    obstacleMeshes.add(bm);
                    root.getChildren().add(bm);
                    break;
            }

        }

        setupLights(root);
        setupBackground(root);
        setupCamera(root);
    }

    private void create() {
        //System.out.println("3D supported? " +  
          //  Platform.isSupported(ConditionalFeature.SCENE3D));

        // Build the Scene Graph
        Group root = new Group();
        rootGroup = root;
        create3DObjects(root);

        // Use a SubScene       
        subScene = new SubScene(root, 1000, 400, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(camera);
        subScene.setPickOnBounds(true);

        subScene.setOnMousePressed((MouseEvent event) -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                mouseOldX = mouseX;
                mouseOldY = mouseY;
                mouseX = event.getSceneX();
                mouseY = event.getSceneY();
                mouseDeltaX = mouseX - mouseOldX;
                mouseDeltaY = mouseY - mouseOldY;
                CameraGroup cam = cameraGroup;
                if (event.isPrimaryButtonDown()) {
                    if (event.isShiftDown() && event.isPrimaryButtonDown()) {
                        //cam.changeRollBy(mouseDeltaX);
                    }
                    else if (event.isControlDown() && event.isPrimaryButtonDown()) {
                        //cam.changePitchBy(mouseDeltaY);
                    }
                    else {
                        //overallYaw += mouseDeltaX;
                        //overallYaw = Utils.normalizeAngle(overallYaw);
                        //GraphicsHelper.matrixRotate(cameraGroup, 0, 0, Math.toRadians(cam.getYaw));
                        //cam.changePitchYaw(mouseDeltaY, -mouseDeltaX);
                        
                        cameraGroup.updateTrackHeight(-mouseDeltaY);
                        cameraGroup.updateTrackAngle(mouseDeltaX);
                    }
                }
                /*else if (me.isControlDown() && me.isSecondaryButtonDown()) {
                    double scale = cam.s.getX();
                    double newScale = scale + mouseDeltaX*0.01;
                    cam.s.setX(newScale); cam.s.setY(newScale); cam.s.setZ(newScale);
                }
                else if (me.isControlDown() && me.isMiddleButtonDown()) {
                    cam.t.setX(cam.t.getX() + mouseDeltaX);
                    cam.t.setY(cam.t.getY() + mouseDeltaY);
                }*/
            }
        });

        /*subScene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                Utils.log();
                processKeyRelease(keyEvent.getCode());
            }
        });*/

        EventHandler filter = 
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        //Utils.log(event.getDeltaY());
 	                    //event.consume();
                        cameraGroup.updateTrackDistance(-1 * (event.getDeltaY() / 2));
                    }
                };
        subScene.addEventFilter(ScrollEvent.ANY, filter);
    }

    public void setGui(Gui3D gui) {
        gui3D = gui;
        gui3D.setViewer(this);
    }

    public boolean processDroneLight(boolean turnOn) {
 	    return drone3D.setLightOn(turnOn);
    }

    public boolean processLockTarget() {
 	    return cameraGroup.toggleLockTarget();
    }

    public boolean processLockTarget(boolean lock) {
 	    return cameraGroup.lockTarget(lock);
    }

    public void processKeyRelease(KeyCode keyCode) {

        switch (keyCode) {
            case L:
                gui3D.setCheckLock(processLockTarget());
                break;
            case UP:
                //cameraGroup.moveBy(0, -20, 0);
                cameraGroup.updateTrackDistance(-20);
                break;
            case DOWN:
                //cameraGroup.moveBy(0, 20, 0);
                cameraGroup.updateTrackDistance(20);
                break;
            case LEFT:
                //cameraGroup.moveBy(10, 0, 0);
                cameraGroup.updateTrackAngle(20);
                break;
            case RIGHT:
                //cameraGroup.moveBy(-20, 0, 0);
                cameraGroup.updateTrackAngle(-20);
                break;
            case HOME:
                //cameraGroup.moveBy(0, 0, -20);
                cameraGroup.updateTrackHeight(-20);
                break;
            case END:
                //cameraGroup.moveBy(0, 0, 20);
                cameraGroup.updateTrackHeight(20);
                break;
        }
    }

    public void removeDrones(ArrayList<Drone3D> drones) {
        for (Drone3D drone : drones) {
            drone.removeFromRoot();
        }
    }

    public void createDrones(Group root) {
        //drone3D = new Drone3D(scenario.drone, root);
        removeDrones(drones);
        drones.clear();

        for (Drone drone : scenario.drones) {
           Drone3D d3d = new Drone3D(drone, root); 
           drones.add(d3d);
        }
        drone3D = drones.get(0);
        resetDrone();
    }

    public void reset() {
        createDrones(rootGroup);
        //resetDrone();
    }

    public void resetDrone() {
        drone3D.setDrone(scenario.drone);
    }

    public void update() {
        /*for (SimBox shape : shapes) {
            shape.setTranslateX(shape.theObs.getX());
            shape.setTranslateY(shape.theObs.getY());
            shape.setTranslateZ(-1 * (shape.theObs.getElevation() / 2));
        }*/
        //for (BuildingMesh shape : shapes) {
            //shape.update();
            //shape.setTranslateX(shape.theObs.getX());
            //shape.setTranslateY(shape.theObs.getY());
            //shape.setTranslateZ(-1 * (shape.theObs.getElevation() / 2));
        //}
        //droneSphere.update();
        for (Drone3D drone : drones) {
           drone.update();
        }
        //drone3D.update();
 
        cameraGroup.track(scenario.drone);
        //cameraGroup.setPos(scenario.drone.getX() -100, scenario.drone.getY() - 100, -40 - scenario.drone.getElevation());
        //cameraGroup.lookAt(scenario.drone);
        // Starting location of the drone
        //moveCameraTo(shapes.get(0).theObs.getX(), shapes.get(0).theObs.getY());
    }

    /*@Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        Scene scene = new Scene(createContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }*/
}