package dronelab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import javax.json.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
//import javax.json.JsonGenerator;
// In our version it is stream.JsonGenerator for some reason.
import javax.json.stream.JsonGenerator;
import javafx.scene.image.Image;
import javafx.scene.shape.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.PrintWriter;
//import java.io.StringWriter;
import java.io.*; 
//import java.awt.image.BufferedImage;

import javax.json.JsonValue;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.stream.JsonParser;

import dronelab.collidable.*;
import dronelab.utils.*;

public class ScenarioCreator {

    public static enum SectorType {
        FOUR(4),
        SIX(6),
        EIGHT(8),
        NINE(9),
        TWELVE(12),
        SIXTEEN(16),
        TWENTY(20),
        TWENTYFIVE(25),
        FIFTY(50),
        ONEHUNDRED(100);

        private final int value;
        private SectorType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        } 
    }

    public Drone drone;
    public ArrayList<Mobile> mobiles = new ArrayList<Mobile>();
    public ArrayList<Person> people = new ArrayList<Person>();
    public ArrayList<Drone> drones = new ArrayList<Drone>();
    public ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    public ArrayList<Collision> collisions = new ArrayList<Collision>();
    public ArrayList<Person> victims = new ArrayList<Person>();
    public ArrayList<Person> loadedSurvivors = new ArrayList<Person>();
    public ArrayList<RescueWorker> rescueWorkers = new ArrayList<RescueWorker>();
    public ArrayList<BroadcastMessage> broadcasts = new ArrayList<BroadcastMessage>();
    public ArrayList<Deployment> deployments = new ArrayList<Deployment>();

    // The sectors for collision detection, these are actually created
    // in the subclasses when we know what the width/height of the area is
    public ArrayList<Sector> sectors = new ArrayList<Sector>();
    public SectorType sectorType = SectorType.ONEHUNDRED;
    // These are calculated based on the number of sectors
    public int numSectors = 0; 
    public int sectorColumns = 1;
    public int sectorRows = 1;
    public double sectorWidth = 0; 
    public double sectorHeight = 0;
    
    public double startingX = 1250; // Out of 6500
    public double startingY = 2450;
    public double startingZoom = 1.0;

    int nextDroneId = 0;

    public void clear() {
        clearVictims();
        clearDrones();
        clearObstacles();
        
        drone = null;
    	drones.clear();
    	obstacles.clear();
    	collisions.clear();
    	rescueWorkers.clear();
    	broadcasts.clear();
    	deployments.clear();
        mobiles.clear();
    	people.clear();
        victims.clear();
    }

    public void clearVictims() {
        for (Person pers : victims) {
            removeFromSectors(pers);
        }
        mobiles.removeAll(victims);
        people.removeAll(victims);
        victims.clear();
    }

    public void clearDrones() {
        for (Drone dro : drones) {
            removeFromSectors(dro);
        }
        drone = null;
        mobiles.removeAll(drones);
        drones.clear();
    	broadcasts.clear();
        nextDroneId = 0;
    }

    public void clearDeployments() {
        deployments.clear();
    }

    public void clearObstacles() {
        for (Obstacle obs : obstacles) {
            removeFromSectors(obs);
        }
        obstacles.clear();
    }

    public void createDefault() {
        createDefaultMobiles();
        createDefaultObstacles();
    }

    public void removeObstacle(Obstacle obs) {
        obstacles.remove(obs);

        // And remove from sector
        removeFromSectors(obs);
    }

    /*public void removeDrone(Drone d) {
        drones.remove(d);
        mobiles.remove(d);

        // And remove from sector
        removeFromSectors(d);
    }

    public void removeVictim(Person vict) {
        victims.remove(vict);
        mobiles.remove(vict);
        people.remove(vict);

        // And remove from sector
        removeFromSectors(vict);
    }*/

    public void addDeployment(Deployment dep) {
        deployments.add(dep);
    }

    // This one is different than mobiles because it can have a lot of points, it
    // doesnt just have a set position
    public void addObstacle(Obstacle obs) {
        obstacles.add(obs);

        // And add to the proper sector as well
        addToCorrectSectors(obs);
    }

    protected Drone addDrone(double x, double y) {
        Drone d = new Drone(nextDroneId++);

        if (x >= 0 && y >= 0) {
            d.setStartPos(x, y);
        }
        d.reset();
        return addDrone(d);
    }

    public Drone addDrone(Drone d) {
        drones.add(d);
        mobiles.add(d);
        
        // And add to the proper sector as well
        addToCorrectSectors(d);
        return d;
    }

    public Person addVictim(double x, double y) {
        return addVictim(x, y, 0);
    }

    public Person addVictim(double x, double y, int elevation) {
        Person vict = new Person();
        vict.setPos(x, y);
        return addVictim(vict);
    }

    // Just load it into our loaded survivors list.  Then we can
    // do whatever we need to do with this list later.
    public void loadSurvivor(Person vict) {
        loadedSurvivors.add(vict);
    }

    public void addVictims(ArrayList<Person> survivorList) {
        // Actually copy these so I retain the originally loaded values.
        for (Person person : survivorList) {
            Person surv = new Person(person);
            addVictim(surv);
        }
        //victims.addAll(survivorList);
        //mobiles.addAll(survivorList);
        //people.addAll(survivorList);
    }

    public Person addVictim(Person vict) {
        victims.add(vict);
        mobiles.add(vict);
        people.add(vict);

        // And add to the proper sector as well
        addToCorrectSectors(vict);
        return vict;
    }

    public void resetDrones() {
        for (Drone d : drones) {
            d.reset();
        }
    }

    public void resetVictims() {
        for (Person vict : victims) {
            vict.reset();
        }
    }

    public void resetDeployments() {
        for (Deployment dep : deployments) {
            dep.reset();
        }
    }

    public void removeFromSectors(Drone drone) {
        if (sectors == null) {
            return;
        }
        for (Sector sect : sectors) {
            sect.remove(drone);
        }
    }

    public void removeFromSectors(Obstacle obs) {
        if (sectors == null) {
            return;
        }
        for (Sector sect : sectors) {
            sect.remove(obs);
        }
    }
    
    public void removeFromSectors(Person pers) {
        if (sectors == null) {
            return;
        }
        for (Sector sect : sectors) {
            sect.remove(pers);
        }
    }
    
    
    public void addToCorrectSectors(Drone drone) {
        // If it has sectors already, remove them, since drones
        // can move.

        // Things can be in multiple sectors at once if they
        // span across boundaries.  That is not a problem.
        for (Sector sect : sectors) {
            if (sect.isInsideBounds(drone) == true) {
                sect.add(drone);
            }
        }
    }

    public void addToCorrectSectors(Obstacle obs) {
        // Things can be in multiple sectors at once if they
        // span across boundaries.  That is not a problem.
        for (Sector sect : sectors) {
            if (sect.isInsideBounds(obs) == true) {
                sect.add(obs);
            }
        }
    }

    public void addToCorrectSectors(Person pers) {
        // Things can be in multiple sectors at once if they
        // span across boundaries.  That is not a problem.
        for (Sector sect : sectors) {
            if (sect.isInsideBounds(pers) == true) {
                sect.add(pers);
            }
        }
    }

    private void createDefaultMobiles() {
        createDefaultDrones();
    }

    private void createDefaultDrones() {
        drone = addDrone(startingX, startingY);
        resetDrones();
    }
    
    private void createDefaultObstacles() {
        Obstacle obs = null;

        double xRatio = 1; //currentWidth / nativeWidth;
        double yRatio = 1; //currentHeight / nativeHeight;

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(669 * xRatio, 488 * yRatio);
        obs.addPoint(720 * xRatio, 502 * yRatio);
        obs.addPoint(710 * xRatio, 557 * yRatio);
        obs.addPoint(670 * xRatio, 547 * yRatio);
        obs.addPoint(673 * xRatio, 530 * yRatio);
        obs.addPoint(662 * xRatio, 527 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(708 * xRatio, 478 * yRatio);
        obs.addPoint(715 * xRatio, 462 * yRatio);
        obs.addPoint(742 * xRatio, 474 * yRatio);
        obs.addPoint(735 * xRatio, 491 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(598 * xRatio, 480 * yRatio);
        obs.addPoint(647 * xRatio, 492 * yRatio);
        obs.addPoint(637 * xRatio, 525 * yRatio);
        obs.addPoint(590 * xRatio, 513 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(554 * xRatio, 445 * yRatio);
        obs.addPoint(578 * xRatio, 453 * yRatio);
        obs.addPoint(573 * xRatio, 469 * yRatio);
        obs.addPoint(580 * xRatio, 477 * yRatio);
        obs.addPoint(572 * xRatio, 499 * yRatio);
        obs.addPoint(539 * xRatio, 492 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(534 * xRatio, 495 * yRatio);
        obs.addPoint(529 * xRatio, 525 * yRatio);
        obs.addPoint(561 * xRatio, 532 * yRatio);
        obs.addPoint(571 * xRatio, 506 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(317 * xRatio, 508 * yRatio);
        obs.addPoint(356 * xRatio, 524 * yRatio);
        obs.addPoint(342 * xRatio, 552 * yRatio);
        obs.addPoint(304 * xRatio, 534 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(359 * xRatio, 524 * yRatio);
        obs.addPoint(350 * xRatio, 545 * yRatio);
        obs.addPoint(383 * xRatio, 562 * yRatio);
        obs.addPoint(396 * xRatio, 540 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(384 * xRatio, 379 * yRatio);
        obs.addPoint(367 * xRatio, 417 * yRatio);
        obs.addPoint(394 * xRatio, 429 * yRatio);
        obs.addPoint(413 * xRatio, 393 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(395 * xRatio, 429 * yRatio);
        obs.addPoint(416 * xRatio, 442 * yRatio);
        obs.addPoint(441 * xRatio, 410 * yRatio);
        obs.addPoint(412 * xRatio, 395 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(480 * xRatio, 428 * yRatio);
        obs.addPoint(472 * xRatio, 464 * yRatio);
        obs.addPoint(506 * xRatio, 474 * yRatio);
        obs.addPoint(514 * xRatio, 468 * yRatio);
        obs.addPoint(526 * xRatio, 444 * yRatio);
        obs.addPoint(494 * xRatio, 436 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(291 * xRatio, 564 * yRatio);
        obs.addPoint(281 * xRatio, 595 * yRatio);
        obs.addPoint(312 * xRatio, 606 * yRatio);
        obs.addPoint(323 * xRatio, 575 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(1351 * xRatio, 2516 * yRatio);
        obs.addPoint(1390 * xRatio, 2536 * yRatio);
        obs.addPoint(1374 * xRatio, 2570 * yRatio);
        obs.addPoint(1336 * xRatio, 2555 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(1391 * xRatio, 2537 * yRatio);
        obs.addPoint(1445 * xRatio, 2563 * yRatio);
        obs.addPoint(1432 * xRatio, 2593 * yRatio);
        obs.addPoint(1374 * xRatio, 2568 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(15);
        obs.addPoint(1529 * xRatio, 2510 * yRatio);
        obs.addPoint(1516 * xRatio, 2542 * yRatio);
        obs.addPoint(1559 * xRatio, 2560 * yRatio);
        obs.addPoint(1571 * xRatio, 2523 * yRatio);
        addObstacle(obs);

        int el = 11;
        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1620 * xRatio, 2541 * yRatio);
        obs.addPoint(1603 * xRatio, 2576 * yRatio);
        obs.addPoint(1621 * xRatio, 2583 * yRatio);
        obs.addPoint(1636 * xRatio, 2549 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1645 * xRatio, 2554 * yRatio);
        obs.addPoint(1663 * xRatio, 2562 * yRatio);
        obs.addPoint(1650 * xRatio, 2593 * yRatio);
        obs.addPoint(1632 * xRatio, 2588 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1601 * xRatio, 2585 * yRatio);
        obs.addPoint(1618 * xRatio, 2593 * yRatio);
        obs.addPoint(1603 * xRatio, 2621 * yRatio);
        obs.addPoint(1587 * xRatio, 2616 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1628 * xRatio, 2597 * yRatio);
        obs.addPoint(1645 * xRatio, 2607 * yRatio);
        obs.addPoint(1628 * xRatio, 2641 * yRatio);
        obs.addPoint(1613 * xRatio, 2633 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1653 * xRatio, 2608 * yRatio);
        obs.addPoint(1669 * xRatio, 2617 * yRatio);
        obs.addPoint(1655 * xRatio, 2657 * yRatio);
        obs.addPoint(1640 * xRatio, 2650 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1678 * xRatio, 2628 * yRatio);
        obs.addPoint(1693 * xRatio, 2637 * yRatio);
        obs.addPoint(1680 * xRatio, 2675 * yRatio);
        obs.addPoint(1660 * xRatio, 2669 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(el);
        obs.addPoint(1690 * xRatio, 2593 * yRatio);
        obs.addPoint(1696 * xRatio, 2585 * yRatio);
        obs.addPoint(1707 * xRatio, 2582 * yRatio);
        obs.addPoint(1712 * xRatio, 2595 * yRatio);
        obs.addPoint(1716 * xRatio, 2603 * yRatio);
        obs.addPoint(1704 * xRatio, 2603 * yRatio);
        obs.addPoint(1693 * xRatio, 2599 * yRatio);
        addObstacle(obs);

        obs = new Obstacle();
        obs.setElevation(7);
        obs.addPoint(1443 * xRatio, 2555 * yRatio);
        obs.addPoint(1471 * xRatio, 2555 * yRatio);
        obs.addPoint(1475 * xRatio, 2602 * yRatio);
        obs.addPoint(1449 * xRatio, 2603 * yRatio);
        obs.addPoint(1434 * xRatio, 2612 * yRatio);
        obs.addPoint(1428 * xRatio, 2593 * yRatio);
        obs.addPoint(1441 * xRatio, 2579 * yRatio);
        addObstacle(obs);
    }
}