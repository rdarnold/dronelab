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
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.AmbientLight;
import javafx.scene.DirectionalLight;
import javafx.scene.PointLight;
import dronelab.utils.*;
import dronelab.collidable.*;
import dronelab.Scenario;

public class PerspectiveViewer {

    private SubScene subScene;
    private SurvivorView survivorView;
    Scenario scenario;
    private final Group buildings = new Group();
    private final Group cylinders = new Group();
    PerspectiveCamera camera;
    CameraGroup cameraGroup;
    private final Box backgroundBox = new Box(1, 1, 1);
    private final PhongMaterial backgroundMaterial = new PhongMaterial(Color.BLACK);
    private String mapTextureSource;
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
        drones = new ArrayList<Drone3D>();
        create();
    }

    public SubScene getSubScene() { return subScene; }

    public void refreshScenario() {
        refreshMapTexture();
        backgroundBox.setWidth(scenario.currentWidth);
        backgroundBox.setHeight(scenario.currentHeight);
        // Box coordinates are centered. Put the map's upper-left corner at the origin.
        backgroundBox.setTranslateX(scenario.currentWidth / 2);
        backgroundBox.setTranslateY(scenario.currentHeight / 2);

        buildings.getChildren().clear();
        cylinders.getChildren().clear();
        for (Obstacle obstacle : scenario.obstacles) {
            switch (obstacle.getShapeType()) {
                case CIRCLE:
                    cylinders.getChildren().add(new SimCylinder(obstacle));
                    break;
                default:
                    buildings.getChildren().add(new ObstacleMesh(obstacle));
                    break;
            }
        }
    }

    private void refreshMapTexture() {
        String source = scenario.getBackgroundImageName();
        if (source.equals(mapTextureSource) && backgroundMaterial.getSelfIlluminationMap() != null) {
            return;
        }

        Image original = GraphicsHelper.loadImage(source);
        PixelReader reader = original == null ? null : original.getPixelReader();
        if (reader == null) {
            backgroundMaterial.setSelfIlluminationMap(scenario.backGround);
            mapTextureSource = null;
            return;
        }

        // Keep the aerial photo independent of the scene and drone lights.
        backgroundMaterial.setSelfIlluminationMap(sharpenMap(original, reader));
        mapTextureSource = source;
    }

    private Image sharpenMap(Image original, PixelReader reader) {
        int width = (int) original.getWidth();
        int height = (int) original.getHeight();
        var format = PixelFormat.getIntArgbInstance();
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, format, pixels, 0, width);
        int[] sharpened = pixels.clone();

        // Sharpen fine detail in the original image before stretching it across the map.
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int i = y * width + x;
                int pixel = pixels[i];
                int detail = (4 * luminance(pixel) - luminance(pixels[i - 1]) - luminance(pixels[i + 1])
                        - luminance(pixels[i - width]) - luminance(pixels[i + width])) / 8;
                if (Math.abs(detail) < 2) {
                    continue;
                }
                int adjustment = Math.clamp(detail, -12, 12);
                int red = Math.clamp(((pixel >>> 16) & 255) + adjustment, 0, 255);
                int green = Math.clamp(((pixel >>> 8) & 255) + adjustment, 0, 255);
                int blue = Math.clamp((pixel & 255) + adjustment, 0, 255);
                sharpened[i] = (pixel & 0xff000000) | (red << 16) | (green << 8) | blue;
            }
        }

        WritableImage result = new WritableImage(width, height);
        result.getPixelWriter().setPixels(0, 0, width, height, format, sharpened, 0, width);
        return result;
    }

    private int luminance(int pixel) {
        return (54 * ((pixel >>> 16) & 255) + 183 * ((pixel >>> 8) & 255) + 19 * (pixel & 255)) >>> 8;
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
        camera.setFarClip(9000);

        cameraGroup.setPos(1200, 2500, -100);
        cameraGroup.lookAt(scenario.drone);
    }

    private void setupLights(Group root) {
        AmbientLight fill = new AmbientLight(Color.color(0.44, 0.47, 0.52));
        DirectionalLight sun = new DirectionalLight(Color.color(0.80, 0.76, 0.69));
        sun.setDirection(new Point3D(0.5, -0.7, 1));
        fill.getScope().add(buildings);
        sun.getScope().add(buildings);

        PointLight sceneLight = new PointLight(Color.WHITE);
        sceneLight.setTranslateX(800);
        sceneLight.setTranslateY(2000);
        sceneLight.setTranslateZ(-2000);
        sceneLight.getExclusionScope().add(buildings);
        root.getChildren().addAll(sceneLight, fill, sun);
    }
    private void create3DObjects(Group root) {
        backgroundBox.setMaterial(backgroundMaterial);
        root.getChildren().addAll(backgroundBox, buildings, cylinders);
        refreshScenario();
        createDrones(root);
        setupLights(root);
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
        survivorView = new SurvivorView(scenario, root, subScene);

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

        EventHandler<ScrollEvent> filter =
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

    public javafx.scene.canvas.Canvas getSurvivorOverlay() {
        return survivorView.overlay();
    }

    void revealSurvivors(boolean reveal) {
        survivorView.revealAll(reveal);
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
        survivorView.reset();
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
        survivorView.update();
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
