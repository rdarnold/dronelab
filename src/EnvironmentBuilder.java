package dronelab;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.transform.Rotate;

import dronelab.collidable.*;
import dronelab.utils.*;

// Allows a user to build an environment manually in realtime
public class EnvironmentBuilder  {
    Scenario scen;
    //ArrayList<Point2D> points;
    Obstacle selectedObs;
    Obstacle tempObs; // The working obstacle before its added to the world
    Deployment tempDep;  // Same but for deployments
    double currentElevation = 15;
    DropShadow dsElevation;
    private Constants.BuildMode mode = Constants.BuildMode.BUILD;
    private Constants.BuildType buildType = Constants.BuildType.BUILDINGS;
    private Constants.ShapeType buildingShape = Constants.ShapeType.POLYGON;

    private Constants.BuildSubMode subMode = Constants.BuildSubMode.DROP;

    // For circle obstacles
    int lastRadius = 10;

    // for rect obstacles
    int lastWidth = 80;
    int lastLength = 30;
    double lastAngleDegree = 0;

    double lastMouseX = 0;
    double lastMouseY = 0;

    public EnvironmentBuilder(Scenario s) {
        scen = s;
        dsElevation = new DropShadow(25, Color.WHITE);
        dsElevation.setSpread(0.75);
    }

    public void start() {
        // No difference in the initial startup right now,
        // maybe this will change in the future.
        reset();
    }

    public void reset() {
        //points = new ArrayList<Point2D>(); 
        tempObs = null;
        tempDep = null;
        unselectSelectedObs();
        setSubMode(Constants.BuildSubMode.DROP);
        triggerRedraw();
    }

    public void stop() {
        unselectSelectedObs();
        setSubMode(Constants.BuildSubMode.DROP);
        save();
    }

    public void save() {
        if (tempObs != null) {
            if (tempObs.isCircle() == true) {
                tempObs.setColorsToDefault();
                tempObs.path = false;
                scen.addObstacle(tempObs);
            }
            else {
                CanvasPolygon poly = tempObs.getCanvasPolygon();
                if (poly.getXPoints().length >= 3) {
                    tempObs.setColorsToDefault();
                    tempObs.path = false;
                    scen.addObstacle(tempObs);
                }
            } 
        }
        /*Obstacle obs = new Obstacle();
        obs.getElevation() = 15;
        for (Point2D point : points) {
            obs.addPoint(point.getX(), point.getY());
        }*/
        reset();
    }

    public Constants.BuildMode getMode() {
        return mode;
    }

    public Constants.BuildType getBuildType() {
        return buildType;
    }

    public Constants.ShapeType getBuildingShape() {
        return buildingShape;
    }

    public void setMode(Constants.BuildMode newMode) {
        if (mode == newMode) {
            return;
        }
        stop();
        mode = newMode;
    }

    public void setBuildType(Constants.BuildType newType) {
        if (buildType == newType) {
            return;
        }
        stop();
        buildType = newType;
    }

    public void setBuildingShape(Constants.ShapeType newShape) {
        if (buildingShape == newShape) {
            return;
        }
        stop();
        buildingShape = newShape;
    }

    public void setSubMode(Constants.BuildSubMode newMode) {
        subMode = newMode;
    }

    public void updateElevation(double newElevation) {
        currentElevation = newElevation;
        if (selectedObs != null) {
            selectedObs.setElevation(currentElevation);
        }
        triggerRedraw();
    }

    public void increaseElevation(int num) {
        updateElevation(currentElevation + (double)num);
    }

    public void decreaseElevation(int num) {
        updateElevation(currentElevation - (double)num);
    }

    public void drawElevation(Obstacle obs, GraphicsContext gc, double canvasMouseX, double canvasMouseY) {
        Font oldFont = gc.getFont();
        TextAlignment oldAlign = gc.getTextAlign();
        VPos oldVPos = gc.getTextBaseline();

        gc.setFont(Font.font ("Verdana", 20));
        gc.setFill(Color.BLACK);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setEffect(dsElevation);
        int x = 0;
        int y = 0;
        if (obs.isCircle() == true) {
            x = (int)obs.x() - 50;
            y = (int)obs.y() - 50;
        } else {
            x = (int)obs.getXPoint(0) - 50;
            y = (int)obs.getYPoint(0) - 50;
        }
        gc.fillText("Elevation: " + Utils.round1Decimal(Distance.metersFromPixels(obs.getElevation())) + "m", x, y);
        gc.setEffect(null);

        // Restore to previous values
        gc.setFont(oldFont);
        gc.setTextAlign(oldAlign);
        gc.setTextBaseline(oldVPos);
    }
    
    public void draw(GraphicsContext gc, double canvasMouseX, double canvasMouseY) {
        // Draw our current list of points as some kind of polygon
        if (tempObs != null) {
            tempObs.draw(gc);
        }

        if (selectedObs != null) {
            // Draw current elevation next to mouse pointer
            drawElevation(selectedObs, gc, canvasMouseX, canvasMouseY);
        }

	    //gc.setStroke(Color.rgb(150, 50, 50, 1.0));
        //gc.strokePolygon(xPoints, yPoints, xPoints.length);
    }

    public String generateCodeForObstacles(ArrayList<Obstacle> obstacles) {
        // Should use a stringbuilder type of thing here
        String code = "";
        for (Obstacle obs : obstacles) {

            if (obs.isCircle() == true) {
                code += "\nobs = new Obstacle(Constants.ShapeType.CIRCLE);\n";
                code += "obs.setPos(" + Math.round(obs.x()) + " * xRatio, " +  
                        Math.round(obs.y()) + " * yRatio);\n";
                code += "obs.setRadius(" + obs.wid / 2 + ");\n";
            }
            else {
                CanvasPolygon poly = obs.getCanvasPolygon();
                code += "\nobs = new Obstacle(Constants.ShapeType.POLYGON);\n";
                for (int i = 0; i < poly.getXPoints().length; i++) {
                    code += "obs.addPoint(" + Math.round(poly.getXPoint(i)) + " * xRatio, " +  
                        Math.round(poly.getYPoint(i)) + " * yRatio);\n";
                }
            }
            code += "obs.setElevation(" + obs.getElevation() + ");\n";
            code += "addObstacle(obs);\n";
        }
        return code;
    }

    public void saveCodeForObstacles(ArrayList<Obstacle> obstacles) {
        String str = generateCodeForObstacles(obstacles);
        Utils.writeFile(str, Constants.OUTPUT_SAVE_PATH + "ObstacleTextFile.txt");
    }

    public String generateCodeForVictims(ArrayList<Person> victims) {
        // Should use a stringbuilder type of thing here
        String code = "";
        for (Person vict : victims) {
            code += "addVictim(" + Math.round(vict.x()) + ", " +  
                    Math.round(vict.y()) + ", " + vict.getElevation() + ");\n";
        }
        return code;
    }

    public void saveCodeForVictims(ArrayList<Person> victims) {
        String str = generateCodeForVictims(victims);
        Utils.writeFile(str, Constants.OUTPUT_SAVE_PATH + "VictimTextFile.txt");
    }

    public String generateCodeForDrones(ArrayList<Drone> drones) {
        // Should use a stringbuilder type of thing here
        String code = "";
        for (Drone d : drones) {
            code += "addDrone(" + Math.round(d.getStartingX()) + ", " +  
                    Math.round(d.getStartingY()) + ");\n";
        }
        return code;
    }

    public void saveCodeForDrones(ArrayList<Drone> drones) {
        String str = generateCodeForDrones(drones);
        Utils.writeFile(str, Constants.OUTPUT_SAVE_PATH + "DroneTextFile.txt");
    }

    public void addBuildingPoint(double x, double y) {
        if (tempObs != null && tempObs.isCircle() == true) {
            stop();
            return;
        }
        if (tempObs == null) {
            tempObs = new Obstacle(buildingShape);
            tempObs.setElevation(currentElevation);
            tempObs.setColors(230, 200, 0);
            tempObs.path = true;
            selectObstacle(tempObs);
        }

        // For circle and rect this will only happen the first time.
        // For polygon we would keep adding points.
        switch (tempObs.getShapeType()) {
            case CIRCLE:
                tempObs.setRadius(lastRadius);
                tempObs.setPos(x, y);
                break;
            case RECT:
                tempObs.getCanvasRectangle().update(x, y, lastWidth, lastLength, lastAngleDegree);
                setSubMode(Constants.BuildSubMode.ROTATE);
                break;
            case POLYGON:
                tempObs.addPoint(x, y);
                break;
        }
    }

    public void addDeploymentPoint(double x, double y) {
        if (tempDep == null) {
            tempDep = new Deployment();
            scen.addDeployment(tempDep);
        }
        tempDep.update(x, y, lastWidth, lastLength, lastAngleDegree);
        setSubMode(Constants.BuildSubMode.ROTATE);
    }

    public void addVictim(double x, double y) {
        addVictim(x, y, 0);
    }

    public void addVictim(double x, double y, int elevation) {
        scen.addVictim(x, y, elevation);
    }

    public void addDrone(double x, double y) {
        scen.addDrone(x, y);
    }

    public Obstacle getObsAtPosition(double x, double y) {
        Obstacle ret = null;
        for (Obstacle obs : scen.obstacles) {
            if (Physics.withinPoint(obs, x, y) == true) {
                // Take either the only one here, or the highest one here.
                if (ret == null) {
                    ret = obs;
                }
                else if (obs.getElevation() > ret.getElevation()) {
                    ret = obs;
                }
            }
        }
        return ret;
    }

    public void selectObstacle(Obstacle obs) {
        unselectSelectedObs();
        selectedObs = obs;
        if (selectedObs != null) {
            selectedObs.select();
            currentElevation = selectedObs.getElevation();
        }
    }

    public void unselectSelectedObs() {
        if (selectedObs != null) {
            selectedObs.unselect();
        }
        selectedObs = null;
    }

    public void selectOnLeftClick(double x, double y) {
        Obstacle obs = getObsAtPosition(x, y);
        selectObstacle(obs);
    }

    public void deleteOnLeftClick(double x, double y) {
        Obstacle obs = getObsAtPosition(x, y);
        if (obs != null) {
            if (obs == selectedObs) {
                unselectSelectedObs();
            }
            scen.removeObstacle(obs);
        }
    }

    // If we have something selected, we can move it over.
    public void onMouseDrag(double x, double y) {
        if (selectedObs != null) {
            double moveX = x - lastMouseX;
            double moveY = y - lastMouseY;
            selectedObs.moveBy(moveX, moveY);
            triggerRedraw();
        }

        lastMouseX = x;
        lastMouseY = y;
    }

    public void processRectMouseMove(double x, double y) {
        switch (subMode) {
            case ROTATE:
                lastAngleDegree += x - lastMouseX;
                lastAngleDegree = Utils.normalizeAngle(lastAngleDegree);
                if (tempObs != null) {
                    tempObs.rotate(lastAngleDegree);
                }
                else if (tempDep != null) {
                    tempDep.rotate(lastAngleDegree);
                }
                triggerRedraw();
                break;
            case RESIZE:
                if (tempObs != null) {
                    tempObs.resize(x - lastMouseX, y - lastMouseY);
                }
                else if (tempDep != null) {
                    tempDep.resize(x - lastMouseX, y - lastMouseY);
                }
                triggerRedraw();
                break;
        }
    }

    // If we are doing a rect or circle, we do various things
    // with mousemove
    public void onMouseMove(double x, double y) {
        if (tempObs != null) {
            switch (tempObs.getShapeType()) {
                case CIRCLE:
                    int rad = (int)Math.round(Physics.calcDistance(x, y, tempObs.x(), tempObs.y()));
                    tempObs.setRadius(rad);
                    triggerRedraw();
                    break;
                case RECT:
                    processRectMouseMove(x, y);
                    break;
                case POLYGON:
                    break;
            }
        }
        else if (tempDep != null) {
            processRectMouseMove(x, y);
        }

        lastMouseX = x;
        lastMouseY = y;
    }

    public void leftClick(double x, double y) {
        switch (mode) {
            case BUILD:
                switch (buildType) {
                    case BUILDINGS:
                        switch (subMode) {
                            case DROP:
                                addBuildingPoint(x, y);
                                break;
                            case ROTATE:
                                setSubMode(Constants.BuildSubMode.RESIZE);
                                break;
                            case RESIZE:
                                stop();
                                break;
                        }
                        break;
                    case VICTIMS:
                        addVictim(x, y);
                        Utils.log("" + scen.victims.size() + " survivors");
                        break;
                    case DRONES:
                        addDrone(x, y);
                        break;
                    case DEPLOYMENT:
                        // Treat this same as placing a rectangle building
                        switch (subMode) {
                            case DROP:
                                addDeploymentPoint(x, y);
                                break;
                            case ROTATE:
                                setSubMode(Constants.BuildSubMode.RESIZE);
                                break;
                            case RESIZE:
                                stop();
                                break;
                        }
                        break;
                }
                break;
            case SELECT:
                selectOnLeftClick(x, y);
                break;
            case DELETE:
                deleteOnLeftClick(x, y);
                break;
        }

        // Yikes, calling that redraw event from all the way out here.
        // this stinks of smelly code.
        triggerRedraw();
    }

    public void rightClick(double x, double y) {
        //System.out.println("obs.addPoint(" + x + ", " + y + ");");
        //save();
        stop();
    }

    private void triggerRedraw() {
        // Yikes, calling that redraw event from all the way out here.
        // this stinks of smelly code.
        scen.sim.signalRedraw();
    }
}