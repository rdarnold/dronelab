package dronelab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import javafx.scene.image.Image;
//import javafx.scene.shape.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;

import javafx.scene.shape.Rectangle;
import javafx.scene.input.KeyCode;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.StringReader;
//import java.io.*; 
//import java.awt.image.BufferedImage;

import javax.json.*;
/*import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonValue;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonString;*/
import javax.json.stream.JsonParser;
//import javax.json.JsonGenerator;
// In our version it is stream.JsonGenerator for some reason.
import javax.json.stream.JsonGenerator;

import dronelab.collidable.*;
import dronelab.utils.*;

public class ScenarioLoader extends ScenarioCreator {

    // Originally based on 2500 x 1850 scenario so thats the native
    // resolution.  We need this to calculate how to do zooming on a specific
    // mouse point if the background image has been increased in size.
    // for example if we load in a 2500x1850 image but at 6500 sizes instead.
    public double nativeWidth; //= 2500;
    public double nativeHeight;// = 1850;
    public double currentWidth = Distance.WIDTH_IN_PIXELS;
    public double currentHeight; // = (nativeHeight / nativeWidth) * currentWidth;

    public double metersWidth = 2000; // This is recorded in JSON data as MetersWidth
    //double nativeXRatio = (nativeWidth / currentWidth);
    //double nativeYRatio = (nativeHeight / currentHeight);

    String name = "";
    String strMainBackgroundImage = "";

    public Image backGround = null;

    public void loadNewBackground(String fileName) {
        loadNewBackground(fileName, currentWidth);
    }
    
    public boolean memorySafeLoadBackground(String fileName) {
        return memorySafeLoadBackground(fileName, 0);
    }

    public boolean memorySafeLoadBackground(String fileName, int numTries) {
        // We need to gc here because our image size is too large.
        // this may not always work, but we try anyway.
        if (numTries > 2) {
            // Ok ok, it took us a half second and we still couldnt
            // load it, just give up at this point.  We likely have no
            // background now since the gc will certainly clean it up
            // soon but at least we didnt crash.
            Utils.err("Failed to load image due to out of memory errors.");
            return false;
        }
        backGround = null;
        System.gc();
        System.runFinalization();
        try {
            //backGround = new Image(path, currentWidth, 0, true, true); 
            backGround = GraphicsHelper.loadImage(fileName, (int)currentWidth, 0, true, true); 
        } catch (OutOfMemoryError e) {
            Utils.err("Out of memory when loading image, sleeping, garbage collecting and trying again...");
            // If we fail, sleep to give the gc time to execute and try again
            try {
                Thread.sleep(25);
            }
            catch (InterruptedException i) {
                System.out.println(i);
            }
            return memorySafeLoadBackground(fileName, numTries + 1);
        }
        return true;
    }

    // We need to do some work to get a new background image in place, since we
    // scale it by a certain amount but we still need to know the native resolution.
    public void loadNewBackground(String fileName, double width) {
        // If filename is the same as our old one, dont do anything.
        if (fileName.equals(strMainBackgroundImage)) {
            return;
        }
        strMainBackgroundImage = fileName;
        setCurrentWidth(width);
        //Utils.log(width);
        getNativeWidthHeight(fileName);

        // We have, by this time, loaded metersWidth from the JSON file, or we had
        // no JSON file and therefore defaulted to 2000 meters.
        // metersWidth meters is the entire scale of the map.  
        // currentWidth is the draw width in pixels.  So it might be a 6800 pixel wide
        // map that is only 2000 meters wide.
        // We need to get some constants in place that let us understand how that 
        // translates to movement and drawing on the map based on realistic 
        // measurements and speeds
        Distance.setGlobalScale(metersWidth, currentWidth);

        memorySafeLoadBackground(fileName);
    }

    public void setCurrentWidth(double newWidth) {
        currentWidth = newWidth;
        resetCurrentHeight();
    }

    public void getNativeWidthHeight(String fileName) {
        //Image img = new Image(Constants.RES_PATH + fileName); //6000, 0, true, true);
        // Can't I use the same background Ive already loaded somehow?
        Image img = GraphicsHelper.loadImage(fileName);
        nativeWidth = img.getWidth();
        nativeHeight = img.getHeight();
        resetCurrentHeight();
    }

    public void resetCurrentHeight() {
        currentHeight = (nativeHeight / nativeWidth) * currentWidth;
        // Now set up the sectors for sensor picture formulation and collision detection
        createSectors();
    }

    public void createSectors() {
        // Based on width and height, create the appropriate sectors.
        sectors.clear();

        if (Double.isNaN(currentWidth) || Double.isNaN(currentHeight))
            return;

        // How many sectors did we ask for?  We will try to divide up into
        // something generally sensible.

        // We will say, generally we are probably wider than longer,
        // so we'll give the width the larger number of sectors if we have
        // them.  Number of sectors must be a divisible number.

        // We will just assume we have only a set 
        numSectors = sectorType.getValue();
        switch (sectorType) {
            case FOUR:
                // 2 x 2
                sectorColumns = 2;
                sectorRows = 2;
                break;
            case SIX:
                // 3 x 2
                sectorColumns = 3;
                sectorRows = 2;
                break;
            case EIGHT:
                // 4 x 2
                sectorColumns = 4;
                sectorRows = 2;
                break;
            case NINE:
                // 3 x 3
                sectorColumns = 3;
                sectorRows = 3;
                break;
            case TWELVE:
                // 4 x 3
                sectorColumns = 4;
                sectorRows = 3;
                break;
            case SIXTEEN:
                // 4 x 4
                sectorColumns = 4;
                sectorRows = 4;
                break;
            case TWENTY:
                // 5 x 4
                sectorColumns = 5;
                sectorRows = 4;
                break;
            case TWENTYFIVE:
                // 5 x 5
                sectorColumns = 5;
                sectorRows = 5;
                break;
            case FIFTY:
                // 5 x 10
                sectorColumns = 5;
                sectorRows = 10;
                break;
            case ONEHUNDRED:
                // 10 x 10
                sectorColumns = 10;
                sectorRows = 10;
                break;
            default:
                // ErrRoar.
                numSectors = 1;
                sectorColumns = 1;
                sectorRows = 1;
                break;
        }
        sectorWidth = currentWidth / (double)sectorColumns;
        sectorHeight = currentHeight / (double)sectorRows;

        // Ceil them to make sure we cover the entire map even with fractions.
        sectorWidth = Math.ceil(sectorWidth);
        sectorHeight = Math.ceil(sectorHeight);

        int row = 0;
        int col = 0;

        int x = 0;
        int y = 0;
        int id = 0;
        while (y < currentHeight) {
            while (x < currentWidth) {
                Sector sector = new Sector(id, x, y, (int)sectorWidth, (int)sectorHeight);
                sector.row = row;
                sector.col = col;
                id++;
                sectors.add(sector);
                x += sectorWidth;
                row++;
            }
            row = 0;
            col++;

            x = 0;
            y += sectorHeight;
        }
    }

    public boolean loadFile(String scenarioName) {
        if (scenarioName == null || scenarioName == "") {
            return false;
        }

        String fileName = Constants.SCENARIO_LOAD_PATH + scenarioName + ".json";

        String jsonData = Utils.readFile(fileName);

        if (jsonData == null) {
            return false;
        }

        // Reset these to something reasonable
        startingX = 50;
        startingY = 50;

        //sim.drawLoading();
        //sim.redrawCanvas();
        Obstacle obs = null;
        Person vict = null;
        Drone dro = null;
        Deployment dep = null;

        // Clear everything and load this new scenario.
        clear();
        JsonParser parser = Json.createParser(new StringReader(jsonData));
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            switch (event) {
                case START_ARRAY:
                    // See which kind of array we have.
                    //obs = new Obstacle();
                    //Utils.log("Start obstacle");
                    break;
                case END_ARRAY:
                    if (obs != null) {
                        // Obstacle done, add it and move on.
                        addObstacle(obs);
                        obs = null;
                        //Utils.log("Done with obstacle");
                    } else if (vict != null) {
                        loadSurvivor(vict);
                        vict = null;
                    } else if (dro != null) {
                        addDrone(dro);
                        dro = null;
                    } else if (dep != null) {
                        addDeployment(dep);
                        dep = null;
                    }
                    break;
                case START_OBJECT:
                case END_OBJECT:
                case VALUE_FALSE:
                case VALUE_NULL:
                case VALUE_TRUE:
                    //System.out.println(event.toString());
                    break;
                case KEY_NAME:
                    //Utils.log(" [" + parser.getString() + "] ");
                    if (obs != null) {
                        if (parser.getString().equals("el")) {
                            // This sucks, I cant just get a key value pair as a json object?
                            // I have to like call a second parse?  Dumb as brix
                            obs.setElevation(Utils.getNextJsonValueAsInt(parser));
                            //Utils.log("Elevation is " + obs.getElevation());
                        } else if (parser.getString().equals("co")) {
                            // Go through in our little loop here until we hit the end
                            // of the array.
                            while (parser.hasNext()) {
                                JsonParser.Event currentEvent = parser.next();
                                if (currentEvent == JsonParser.Event.VALUE_STRING ||
                                    currentEvent == JsonParser.Event.VALUE_NUMBER) {
                                    // x is our current one
                                    int x = Utils.tryParseInt(parser.getString());
                                    // y will be the next one so get that
                                    int y = Utils.getNextJsonValueAsInt(parser);
                                    obs.addPoint(x, y);
                                }
                                else if (currentEvent == JsonParser.Event.END_ARRAY) {
                                    // Done with this array
                                    break;
                                }
                            }
                        } else if (parser.getString().equals("rect")) {
                            int x = Utils.getNextJsonValueAsInt(parser);
                            int y = Utils.getNextJsonValueAsInt(parser);
                            int wid = Utils.getNextJsonValueAsInt(parser);
                            int hgt = Utils.getNextJsonValueAsInt(parser);
                            int angle = Utils.getNextJsonValueAsInt(parser);

                            obs.makeRectangle(x, y, wid, hgt, angle);
                            // Now get to the end of the array before moving on, otherwise
                            // we will hit the end of the loc array and think its the end
                            // of the entire vict object, which it may not be
                            Utils.moveToEndOfJsonArray(parser);
                        } else if (parser.getString().equals("circ")) {
                            int x = Utils.getNextJsonValueAsInt(parser);
                            int y = Utils.getNextJsonValueAsInt(parser);
                            int rad = Utils.getNextJsonValueAsInt(parser);
                            obs.makeCircle(x, y, rad);
                            // Now get to the end of the array before moving on, otherwise
                            // we will hit the end of the loc array and think its the end
                            // of the entire vict object, which it may not be
                            Utils.moveToEndOfJsonArray(parser);
                        }
                    } else if (vict != null) {
                        if (parser.getString().equals("el")) {
                            // This sucks, I cant just get a key value pair as a json object?
                            // I have to like call a second parse?  Dumb as brix
                            vict.setElevation(Utils.getNextJsonValueAsInt(parser));
                            //Utils.log("Elevation is " + obs.getElevation());
                        } else if (parser.getString().equals("loc")) {
                            int x = Utils.getNextJsonValueAsInt(parser);
                            int y = Utils.getNextJsonValueAsInt(parser);
                            vict.setStartPos(x, y);
                            // Now get to the end of the array before moving on, otherwise
                            // we will hit the end of the loc array and think its the end
                            // of the entire vict object, which it may not be
                            Utils.moveToEndOfJsonArray(parser);
                        }
                    } else if (dro != null) {
                        if (parser.getString().equals("el")) {
                            // This sucks, I cant just get a key value pair as a json object?
                            // I have to like call a second parse?  Dumb as brix
                            dro.setElevation(Utils.getNextJsonValueAsInt(parser));
                            //Utils.log("Elevation is " + obs.getElevation());
                        } else if (parser.getString().equals("loc")) {
                            int x = Utils.getNextJsonValueAsInt(parser);
                            int y = Utils.getNextJsonValueAsInt(parser);
                            dro.setStartPos(x, y);
                            Utils.moveToEndOfJsonArray(parser);
                        }
                    } else if (dep != null) {
                        if (parser.getString().equals("rect")) {
                            int x = Utils.getNextJsonValueAsInt(parser);
                            int y = Utils.getNextJsonValueAsInt(parser);
                            int wid = Utils.getNextJsonValueAsInt(parser);
                            int hgt = Utils.getNextJsonValueAsInt(parser);
                            int angle = Utils.getNextJsonValueAsInt(parser);

                            dep.make(x, y, wid, hgt, angle);
                            // Now get to the end of the array before moving on, otherwise
                            // we will hit the end of the loc array and think its the end
                            // of the entire vict object, which it may not be
                            Utils.moveToEndOfJsonArray(parser);
                        } 
                    } else {
                        if (parser.getString().contains("obs")) {
                            obs = new Obstacle();
                        }
                        else if (parser.getString().contains("vict")) {
                            vict = new Person();
                        }
                        else if (parser.getString().contains("drone")) {
                            dro = new Drone();
                        }
                        else if (parser.getString().contains("deployment")) {
                            dep = new Deployment();
                        }
                        else if (parser.getString().equals("Name")) {
                            name = Utils.getNextJsonValueAsString(parser);
                           // Utils.log("Name is " + name);
                        }
                        else if (parser.getString().equals("MetersWidth")) {
                            metersWidth = Utils.getNextJsonValueAsInt(parser);
                            //Utils.log("Width is " + currentWidth);
                        }
                        else if (parser.getString().equals("DrawWidth")) {
                            setCurrentWidth(Utils.getNextJsonValueAsInt(parser));
                            //Utils.log("Width is " + currentWidth);
                        }
                        else if (parser.getString().equals("BackgroundImage")) {
                            loadNewBackground(Utils.getNextJsonValueAsString(parser));
                            //Utils.log("Image is " + strMainBackgroundImage);
                        }
                        else if (parser.getString().equals("StartingX")) {
                            startingX = Utils.getNextJsonValueAsInt(parser);
                        }
                        else if (parser.getString().equals("StartingY")) {
                            startingY = Utils.getNextJsonValueAsInt(parser);
                        }
                    }
                    // Zoids.              
 	                break;
                case VALUE_STRING:
                case VALUE_NUMBER:
                    //System.out.println(event.toString() + " " +
                      //                  parser.getString());
                    break;
            }
        }

	    return true;
    }

    public void saveFile() {
        saveFile(name);
    }

    public void saveFile(String strFileName) {
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

        jsonBuilder.add("Name", name);
        jsonBuilder.add("MetersWidth", (int)metersWidth);
        jsonBuilder.add("DrawWidth", (int)currentWidth);
        jsonBuilder.add("BackgroundImage", strMainBackgroundImage);
        jsonBuilder.add("StartingX", (int)startingX);
        jsonBuilder.add("StartingY", (int)startingY);

        int num = 0;
        for (Obstacle obs : obstacles) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            arrayBuilder.add(Json.createObjectBuilder().add("el", obs.getElevation()));

            if (obs.isCircle() == true) {
                JsonObjectBuilder b = Json.createObjectBuilder();
                b.add("circ", Json.createArrayBuilder().add(
                    Math.round(obs.x())).add(
                    Math.round(obs.y())).add(
                    Math.round(obs.getRadius())
                ));
                arrayBuilder.add(b);
            }
            else if (obs.isRectangle() == true) {
                JsonObjectBuilder b = Json.createObjectBuilder();
                CanvasRectangle rect = obs.getCanvasRectangle();
                b.add("rect", Json.createArrayBuilder().add(
                    Math.round(rect.origX)).add(
                    Math.round(rect.origY)).add(
                    Math.round(rect.origWid)).add(
                    Math.round(rect.origLen)).add(
                    Math.round(rect.angleDegrees)
                ));
                arrayBuilder.add(b);
            }
            else {
                JsonArrayBuilder coordArray = Json.createArrayBuilder();
                CanvasPolygon poly = obs.getCanvasPolygon();
                for (int i = 0; i < poly.getXPoints().length; i++) {
                    coordArray.add(Math.round(poly.getXPoint(i))).add(Math.round(poly.getYPoint(i)));
                }
                JsonObjectBuilder b = Json.createObjectBuilder();
                b.add("co", coordArray);
                arrayBuilder.add(b);
            }

            jsonBuilder.add("obs" + num, arrayBuilder);
            num++;
        }
        
        num = 0;
        for (Person vict : victims) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            arrayBuilder.add(Json.createObjectBuilder().add("el", vict.getElevation()));

            JsonObjectBuilder b = Json.createObjectBuilder();
            b.add("loc", Json.createArrayBuilder().add(Math.round(vict.x())).add(Math.round(vict.y())));
            arrayBuilder.add(b);

            jsonBuilder.add("vict" + num, arrayBuilder);
            num++;
        }

        num = 0;
        for (Drone dro : drones) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            arrayBuilder.add(Json.createObjectBuilder().add("el", dro.getElevation()));
            
            JsonObjectBuilder b = Json.createObjectBuilder();
            b.add("loc", Json.createArrayBuilder().add(
                Math.round(dro.getStartingX())).add(
                Math.round(dro.getStartingY())));
            arrayBuilder.add(b);

            jsonBuilder.add("drone" + num, arrayBuilder);
            num++;
        }

        num = 0;
        for (Deployment dep : deployments) {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            JsonObjectBuilder b = Json.createObjectBuilder();
            CanvasRectangle rect = dep;
            b.add("rect", Json.createArrayBuilder().add(
                Math.round(rect.origX)).add(
                Math.round(rect.origY)).add(
                Math.round(rect.origWid)).add(
                Math.round(rect.origLen)).add(
                Math.round(rect.angleDegrees)
            ));
            arrayBuilder.add(b);

            jsonBuilder.add("deployment" + num, arrayBuilder);
            num++;
        }

        JsonObject model = jsonBuilder.build();
        /*JsonObject model = Json.createObjectBuilder()
            .add("firstName", "Duke")
            .add("lastName", "Java")
            .add("age", 18)
            .add("streetAddress", "100 Internet Dr")
            .add("city", "JavaTown")
            .add("state", "JA")
            .add("postalCode", "12345")
            .add("phoneNumbers", Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                    .add("type", "mobile")
                    .add("number", "111-111-1111"))
                .add(Json.createObjectBuilder()
                    .add("type", "home")
                    .add("number", "222-222-2222")))
            .build();*/

        StringWriter stWriter = new StringWriter();
        Map<String, Object> properties = new HashMap<>(1);
            properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(stWriter);
        //JsonWriter jsonWriter = Json.createWriter(stWriter);
        jsonWriter.writeObject(model);
        jsonWriter.close();

        String jsonData = stWriter.toString();
        //System.out.println(jsonData);

        Utils.writeFile(jsonData, Constants.SCENARIO_SAVE_PATH + strFileName + ".json");
        // true to append, false to overwrite
        /*try (BufferedWriter bw = new BufferedWriter(new FileWriter("Scenario1.json", false))) {
	        bw.write(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}