package dronelab.collidable;

import java.util.ArrayList;
import java.util.EnumSet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color; 
import javafx.scene.shape.Circle;
import javafx.geometry.Point2D;

import dronelab.Scenario;
import dronelab.Sector;
import dronelab.collidable.behaviors.*;
import dronelab.collidable.equipment.*;
import dronelab.utils.*;


public class Drone extends Mobile {

    public static enum DroneType {
        DJI_PHANTOM_3(0), 
        DJI_PHANTOM_4(1), 
        DJI_INSPIRE_1(2), 
        MAVIC_PRO(3);

        private final int _value;
        private DroneType(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }

        private static DroneType[] cachedValues = null;
        public static DroneType fromInt(int i) {
            if (DroneType.cachedValues == null) {
                DroneType.cachedValues = DroneType.values();
            }
            return DroneType.cachedValues[i];
        }
    }

    public static enum DroneRole {
        SOCIAL(0), 
        RELAY(1), 
        ANTISOCIAL(2);

        private final int _value;
        private DroneRole(int value) {
            this._value = value;
        }

        public int getValue() {
            return _value;
        }

        private static DroneRole[] cachedValues = null;
        public static DroneRole fromInt(int i) {
            if (DroneRole.cachedValues == null) {
                DroneRole.cachedValues = DroneRole.values();
            }
            return DroneRole.cachedValues[i];
        }
    }

    // Private variables
    private DroneType droneType = DroneType.DJI_PHANTOM_4;
    private DroneRole droneRole = DroneRole.SOCIAL;
    private double rotorHeading = 0; // Just for visual effect
    private int rotorSpeed = 16; //31;

    private static final int dotSize = 6;
    private static final int rotarSize = 10;

    private Color droneColor = Color.DARKBLUE;

    private boolean fake = false;

    private boolean flying = true;
    private boolean broken = false;
    private int maxHealth = 20;
    private int health = maxHealth; 

    private int dataId = 0;

    // Which sector am I in for sensor picture formulation (collision detection)?
    // This is for efficiency of the simulation and is not a real life concept, it
    // ties to the simulated sensors and the building of the simulated sensor
    // picture.  In a real system this would not be needed.
    public Sector sector = null;

    private ArrayList<DroneData> droneList; // Data on the other drones flying nearby

    // How many people have been found so far?  This is a collaborative list between all
    // drones.
    public ArrayList<Point2D> peopleLocations; 

    // Public variables
    public PathTracker tracker = null;
    public double targetHeightDistance = Distance.pixelsFromMeters(4); // Desired difference in height between obj below us and us.

    public SonarSensor ss = null;
    public LocationSensor ls = null;
    public CameraSensor cam = null;
    public FinderSensor fs = null;
    public FLIRSensor flir = null;

    public BehaviorModule lastActiveModule = null;
    public WiFiCommunicator wifi = null;

    // Is it seeking any particular area?  This affects some behaviors, most notably Seek.
    public Point2D seekLocation = null;

    public ArrayList<String> behaviorOrder; // The list of behaviors in order by name
    public ArrayList<BehaviorModule> behaviors;
    public ArrayList<Sensor> sensors;
    public ArrayList<Battery> batteries;

    public Drone() {
        super();
        setupDrone(0);
    }

    public Drone(int newId) {
        super();
        setupDrone(newId);
    }

    // We can make fake drones to test various things like
    // collisions
    public Drone(boolean isFake) {
        super();
        fake = isFake;
        setupDrone(0);
    }

    public String getCurrentXYZ() {
        return ""+x()+","+y()+","+z()+"";
    }
    public void setDataId(int hashCode) {
        this.dataId = hashCode;
    }
    public int getDataId() {
        return this.dataId;
    }

    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }

    public void setColor(Color col) { droneColor = col; }

    public ArrayList<DroneData> getDroneList() { return droneList; }

    private void setupDrone(int newId) {
        peopleLocations = new ArrayList<Point2D>();
        setupBehaviorOrder();

        DroneType theType = DroneType.DJI_PHANTOM_4;

        if (fake == true) {
            setDroneType(theType);
        } else {
            droneList = new ArrayList<DroneData>();
            setId(newId);
            setDroneType(theType);
            // This should come after the setDroneType so that the sizes
            // are in place.
            setupSensorsAndModules();
            tracker = new PathTracker(this);
        }

        // This sets up speed, acceleration, etc.
        DroneTemplate.setDroneParams(this, theType);
    }

    // Set the default behavior order
    public void setupBehaviorOrder() {
        behaviorOrder = new ArrayList<String>();

        // From most important to least.
        behaviorOrder.add(Constants.STR_LAUNCH);
        behaviorOrder.add(Constants.STR_AVOID);
        behaviorOrder.add(Constants.STR_CLIMB);
        behaviorOrder.add(Constants.STR_RECHARGE);
        behaviorOrder.add(Constants.STR_MAINTAIN_HEIGHT);
        behaviorOrder.add(Constants.STR_SPIRAL);
        behaviorOrder.add(Constants.STR_RELAY);
        behaviorOrder.add(Constants.STR_FORM);
        behaviorOrder.add(Constants.STR_ANTI);
        behaviorOrder.add(Constants.STR_REPEL);
        behaviorOrder.add(Constants.STR_SEEK);
        behaviorOrder.add(Constants.STR_SCATTER);
        behaviorOrder.add(Constants.STR_ASSIGNED_PATH);
        behaviorOrder.add(Constants.STR_SEARCH);
        behaviorOrder.add(Constants.STR_WANDER);

        // They ALL must be ordered, even though they're not used at the same time.
        // The system HAS to know how to prioritize them in the event that they WERE
        // all used at the same time.
        if (behaviorOrder.size() != Constants.STR_BEHAVIORS.length) {
            Utils.log("************************************************");
            Utils.log("ERROR:  Behavior not added to setupBehaviorOrder");
            Utils.log("Did you code in a new behavior but not add it to");
            Utils.log("setupBehaviorOrder() in Drone.java?");
            Utils.log("************************************************");
        }
    }

    /*@Override
    public void setBounds(int left, int top, int right, int bottom)
    {
        super.setBounds(left, top, right, bottom);
        if (wm != null) {
            wm.setBounds(left, top, right, bottom); 
        }
    }*/

    public DroneType getDroneType() {
        return droneType;
    }
    public DroneRole getDroneRole() {
        return droneRole;
    }

    public boolean hasCamera()          { return cam != null; }
    public boolean hasSonarSensor()     { return ss != null; }
    public boolean hasFinderSensor()    { return fs != null; }
    public boolean hasLocationSensor()  { return ls != null; }
    public boolean hasFLIRSensor()      { return flir != null; }

    public boolean isFlying() { return flying; }

    public void setSeekLocation(double x, double y) {
        seekLocation = new Point2D(x, y);
    }

    public void setDroneRole(DroneRole role) {
        droneRole = role;
        
        // Now set up the behaviors based on the role; we should be able to do
        // whatever we need here as this is called after the initial drone setup
        // Actually this is currently done within the scenario itself
        switch (role) {
            case SOCIAL: 
                break;
            case RELAY: 
                break;
            case ANTISOCIAL: 
                break;
        }
    }

    public void setDroneType(String typeName) {
        if (typeName == Constants.STR_DJI_PHANTOM_3) {
            setDroneType(DroneType.DJI_PHANTOM_3);
        } else if (typeName == Constants.STR_DJI_PHANTOM_4) {
            setDroneType(DroneType.DJI_PHANTOM_4);
        } else if (typeName == Constants.STR_DJI_INSPIRE_1) {
            setDroneType(DroneType.DJI_INSPIRE_1);
        } else if (typeName == Constants.STR_MAVIC_PRO) {
            setDroneType(DroneType.MAVIC_PRO);
        } else {
            // Default to phantom 4
            setDroneType(DroneType.DJI_PHANTOM_4);
        }
    }

    public void setDroneType(DroneType newType) {
        double x = 0;
        double y = 0;

        if (super.getShape() != null) {
            x = x();
            y = y();
        }
	    droneType = newType;
        switch (newType) {
            /*case FAKE:
                // The fake drone defaults to a rotor drone but it automatically
                // gets set to the size of its parent "real" drone anyway soon after
                super.makeCircle(rotarSize/2);
                break;
            //case DOT:
                //super.makeCircle(dotSize/2);
              //  break;
            case DJI_PHANTOM_3:
            case DJI_PHANTOM_4:
                super.makeCircle(rotarSize/2);
                break;*/
            default:
                // All the same.. makes no diff
                super.makeCircle(rotarSize/2);
                break;
        }

        // And we have to reset the position since it got messed up when we remade the
        // shape
        setPos(x, y);

        // This must occurr after the makeCircle and all various processing and setup is
        // done on the drone so that we know how to proportion our collision checking.
        setupBehaviorModules();
    }

    public void setupBehaviorModules() {
        if (behaviors == null) {
            return;
        }
        for (BehaviorModule mod : behaviors) {
            mod.setup();
        }
    }

    public void signalPersonLocated(Person p) {
        peopleLocations.add(new Point2D(p.getX(), p.getY()));
        for (BehaviorModule mod : behaviors) {
            mod.onPersonDetected(p, peopleLocations);
        }
        wifi.broadcastPerson(p);
    }

    public void signalPersonSeen(Person p) {
        // We really dont do much here right now because being "seen" is kind
        // of meaningless for the drone; it doesn't know what that is.  The drone
        // hasn't confirmed a survivor, just that the camera has flow over where a 
        // survivor is so theoretically a person on the ground could have spotted
        // the person.
    }

    public void updatePersonList(double x, double y) {
        // Generally sent from another drone, add a person
        // to our list.
        // Right now we only get people that havent been detected
        // yet since we only detect non-detected people but that doesnt
        // actually make sense.  In the future we should be using each drone's
        // own list and checking to see if we have duplicate locations.
        // This list will end up not being super accurate.  It could even use
        // some kind of population algorithm where areas are assigned a "heat"
        // value based on how many different locations are reported there.
        peopleLocations.add(new Point2D(x, y));
    }

    public void resetBehaviorModules() {
        if (behaviors == null) {
            return;
        }
        for (BehaviorModule mod : behaviors) {
            mod.reset();
        }
    }

    // Reset to default starting parameters
    @Override
    public void reset() {
        super.reset();
        health = maxHealth; 
        broken = false;
        land();
	    rechargeBatteriesFull();
        setElevation(startingElevation);
        setTargetSpeed(0);
        seekLocation = null;
        resetBehaviorModules();
        peopleLocations.clear();
    }

    // We should allow multiple redundant sonar sensors
    // facing different directions
    public Sensor getSensor(String name) {
        for (Sensor sensor : sensors) {
            if (sensor.getName() == name)
                return sensor;
        }
        return null;
    }

    public void removeSensor(String strName) {
        Sensor sensor = getSensor(strName);

        if (strName == Constants.STR_SENSOR_SONAR)
	        ss = null;
        else if (strName == Constants.STR_SENSOR_CAMERA)
	        cam = null;
	    else if (strName == Constants.STR_SENSOR_LOCATION)
	        ls = null;
	    else if (strName == Constants.STR_SENSOR_FINDER)
	        fs = null;
        else if (strName == Constants.STR_SENSOR_FLIR)
	        flir = null;

        if (sensor != null) {
            sensors.remove(sensor);
        } 
    }

    public void addSensor(String strName) {
        Sensor sensor = null;
        if (strName == Constants.STR_SENSOR_SONAR)
            sensor = (ss = new SonarSensor(this));
        else if (strName == Constants.STR_SENSOR_CAMERA)
            sensor = (cam = new CameraSensor(this));
	    else if (strName == Constants.STR_SENSOR_LOCATION)
            sensor = (ls = new LocationSensor(this));
	    else if (strName == Constants.STR_SENSOR_FINDER)
            sensor = (fs = new FinderSensor(this));
	    else if (strName == Constants.STR_SENSOR_FLIR)
            sensor = (flir = new FLIRSensor(this));

        if (sensor != null) {
            sensors.add(sensor);
        } 
    }

    public void setupSensorsAndModules() {
        sensors = new ArrayList<Sensor>();
        for (String str : Constants.STR_SENSORS) {
            addSensor(str);
        }

        BehaviorLoader.setBehaviors(this);
        
        batteries = new ArrayList<Battery>();
        // Give it a battery so it can move ...
        batteries.add(new Battery());

        wifi = new WiFiCommunicator(this);
    }

    public void removeBehavior(String strName) {
        BehaviorLoader.removeModule(behaviors, strName);
    }

    public void addBehavior(String strName) {
        BehaviorModule mod = BehaviorLoader.addModule(behaviors, behaviorOrder, strName);
        if (mod == null) { 
            Utils.err("Drone.addBehavior: Behavior '" + strName + "' not found");
            return;
        }
        mod.assign(this);
    }

    public String printBehaviors() {
        String str = "";
        for (BehaviorModule mod : behaviors) {
            str += mod.getName() + " ";
        }
        return str;
    }

    // Duplicate the position parameters and possibly other ones
    // later if we need them
    public void copySizeTo(Drone other) {
        other.wid = this.wid;
        other.hgt = this.hgt;
        Circle otherCircle = other.getCircle();
        Circle thisCircle = this.getCircle();
        otherCircle.setRadius(thisCircle.getRadius());
    }

    public void changeSizeBy(int adjustment) {
        this.wid += adjustment;
        this.hgt += adjustment;
        Circle circle = getCircle();
        circle.setRadius(circle.getRadius() + (adjustment/2));
        setPos(x(), y());
    }

    public void pruneDroneList() {
        // Remove entries that are stale; i.e. we havent heard from in awhile.  Assume
        // those are broken or out of range or not in the mix anymore so we dont want to
        // consider them to be part of the sim.  Give them maybe 10 seconds.
        for (int i = droneList.size()-1; i >= 0; i--) {
            //Utils.log(droneList.get(i).id);
            //Utils.log(droneList.get(i).x);
            //Utils.log(droneList.get(i).y);
            if (droneList.get(i).expired() == true) {
                //Utils.log("Pruned");
                droneList.remove(i);
            }
        }
    }

    // This list should probably have drone entries rather than
    // drones themselves.
    public DroneData getDroneDataForId(int droneId) {
        for (DroneData data : droneList) {
            if (data.id == droneId) {
                return data;
            }
        }
        return null;
    }

    // This is called from communication modules like wifi
    public void updateDroneDataXY(int droneId, int roleNum, double newX, double newY) {
        if (droneId == id) {
            return;
        }

        DroneData data = getDroneDataForId(droneId);
        if (data == null) {
            data = new DroneData();
            data.id = droneId;
            droneList.add(data);
        }
	    data.lastDetectedTimeMillis = System.currentTimeMillis();
        data.x = newX;
        data.y = newY;
        data.role = DroneRole.fromInt(roleNum);
    }

    public void drawSensors(GraphicsContext gc) {
        for (Sensor sensor : sensors) {
            sensor.draw(gc);
        }
    }

    public void drawModules(GraphicsContext gc) {
        for (BehaviorModule mod : behaviors) {
            mod.draw(gc);
        }
    }

    public void drawWifi(GraphicsContext gc) {
        if (wifi != null) {
            wifi.draw(gc);
        }
    }

    public void drawElevationMovement(GraphicsContext gc) {
        // If we are moving up or down, draw an animating arrow.
        int len = GraphicsHelper.getUpCounter() / 10;
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        if (getZSpeed() > 0) {
            double x = x() - wid/2 - 8;
            double y = y() + hgt/2;
            gc.strokeLine(x, y, x, y - len);

            // Now draw an arrowhead
            y = y - len;
            gc.setLineWidth(1);
            gc.strokeLine(x - 4, y - 1, x + 4, y - 1);
            gc.strokeLine(x - 3, y - 2, x + 3, y - 2);
            gc.strokeLine(x - 2, y - 3, x + 2, y - 3);
            gc.strokeLine(x - 1, y - 4, x + 1, y - 4);
            gc.strokeLine(x, y - 5, x, y - 5);
        } else if (getZSpeed() < 0) {
            double x = x() + wid + 5;
            double y = y() - hgt/2;
            gc.strokeLine(x, y, x, y + len);

            y = y + len;
            gc.setLineWidth(1);
            gc.strokeLine(x - 4, y + 1, x + 4, y + 1);
            gc.strokeLine(x - 3, y + 2, x + 3, y + 2);
            gc.strokeLine(x - 2, y + 3, x + 2, y + 3);
            gc.strokeLine(x - 1, y + 4, x + 1, y + 4);
            gc.strokeLine(x, y + 5, x, y + 5);
        }
    }

    public void draw(GraphicsContext gc) {
        draw(gc, Constants.DrawFlag.ALL); //false, false);   
    }

    //public void draw(GraphicsContext gc, boolean bDrawSensorRanges, boolean bDrawModules) {
    public void draw(GraphicsContext gc, EnumSet<Constants.DrawFlag> drawFlags) {
        //gc.setFill(Color.RED);
        int x = (int)x() - (int)wid/2;
        int y = (int)y() - (int)hgt/2;

        if (fake == true) {
            //setSize(wid);
            gc.setFill(Color.DARKGRAY);
            gc.fillOval(x, y, wid, hgt);
            //gc.fillOval(x - 2, y - 2, wid +4, hgt+4);
            return;
        } 

        if (drawFlags != null) {
            if (drawFlags.contains(Constants.DrawFlag.MODULES))
                drawModules(gc);
            if (drawFlags.contains(Constants.DrawFlag.SENSORS))
                drawSensors(gc);
            if (drawFlags.contains(Constants.DrawFlag.WIFI))
                drawWifi(gc);
        }

        if (tracker != null) {
            tracker.draw(gc);
        }

        //Circle circle = (Circle)this.shape;
        //gc.fillOval(circle.getLayoutX() - circle.getRadius(), circle.getLayoutY() - circle.getRadius(), 
        //circle.getRadius() * 2, circle.getRadius() * 2);
            //System.out.println("X: " + circle.getLayoutX() + "Y: " + circle.getLayoutY()); 

        /*if (droneType == Constants.DroneType.DOT) {
            //setSize(dotSize);
            //setSize(wid);
            gc.setFill(Color.RED);
            gc.fillOval(x, y, wid, hgt);
            //gc.strokeOval(60, 60, 30, 30);
        } else if (droneType == Constants.DroneType.ROTOR) {*/
            //setSize(rotarSize);
            //setSize(wid);
            gc.setStroke(Color.HONEYDEW);
            gc.setLineWidth(3);
            //gc.strokeLine(x, y, x+wid, y+hgt);
            double x1 = x + (wid/2);
            double y1 = y + (hgt/2);
            double dx = 0;
            double dy = 0;
            double dx2 = 0;
            double dy2 = 0;
            double x2 = x + wid;
            double y2 = y + hgt;

            dx = ((x2-x1)*Math.cos(Math.toRadians(rotorHeading+180))) - ((y2-y1)*Math.sin(Math.toRadians(rotorHeading+180))) +x1;
            dy = ((x2-x1)*Math.sin(Math.toRadians(rotorHeading+180))) + ((y2-y1)*Math.cos(Math.toRadians(rotorHeading+180))) +y1;

            dx2 = ((x2-x1)*Math.cos(Math.toRadians(rotorHeading))) - ((y2-y1)*Math.sin(Math.toRadians(rotorHeading))) +x1;
            dy2 = ((x2-x1)*Math.sin(Math.toRadians(rotorHeading))) + ((y2-y1)*Math.cos(Math.toRadians(rotorHeading))) +y1;

            gc.strokeLine(dx, dy, dx2, dy2);

            
            dx = ((x2-x1)*Math.cos(Math.toRadians(rotorHeading+270))) - ((y2-y1)*Math.sin(Math.toRadians(rotorHeading+270))) +x1;
            dy = ((x2-x1)*Math.sin(Math.toRadians(rotorHeading+270))) + ((y2-y1)*Math.cos(Math.toRadians(rotorHeading+270))) +y1;

            dx2 = ((x2-x1)*Math.cos(Math.toRadians(rotorHeading+90))) - ((y2-y1)*Math.sin(Math.toRadians(rotorHeading+90))) +x1;
            dy2 = ((x2-x1)*Math.sin(Math.toRadians(rotorHeading+90))) + ((y2-y1)*Math.cos(Math.toRadians(rotorHeading+90))) +y1;

            gc.strokeLine(dx, dy, dx2, dy2);

            if (broken == true) {
                gc.setStroke(Color.BLACK);
            } else {
                gc.setStroke(droneColor);
            }
            gc.setLineWidth(3);
            gc.strokeOval(x - 2, y - 2, wid +4, hgt+4);
       // } 

        if (hasPower() == false) {
            /*gc.setFill(Color.rgb(125 + GraphicsHelper.getFlashColorLevel() / 2, 
                                    125 + GraphicsHelper.getFlashColorLevel() / 2,
                                    125 + GraphicsHelper.getFlashColorLevel() / 2));
            gc.fillOval(x, y, wid, hgt);
            
            gc.setStroke(Color.DARKBLUE);
            gc.setLineWidth(2);
            gc.strokeOval(x - 2, y - 2, wid +4, hgt+4);*/

            // Ran out of batteries!  Draw the no battery symbol.
            gc.drawImage(GraphicsHelper.batteryDead, x-7, y-22, 20, 20);
        } 

        if (broken == true) {
            // Draw a broken sign over it, or maybe a big red X or just a slash. 
            gc.setStroke(Color.DARKRED);
            gc.setLineWidth(3);
            int spc = 3;
            gc.strokeLine(x - spc, y - spc, x + wid + spc, y + wid + spc);
            gc.strokeLine(x + wid + spc, y - spc, x - spc, y + wid + spc);
            //gc.strokeOval(x - 5, y - 5, wid + 10, wid + 10);
            //gc.drawImage(GraphicsHelper.droneBroken, x-5, y-22, 20, 20);
            //gc.drawImage(GraphicsHelper.droneBroken, x-5, y-5, 20, 20);

            // Another option is maybe draw two halves of the drone now.
        }

        if (getZSpeed() != 0) {
            drawElevationMovement(gc);
        }
        
        super.draw(gc);
    }    
    
    public void updateGraphics() {
        if (flying == true) {
            rotorHeading += rotorSpeed;
            if (rotorHeading >= 360) {
                rotorHeading -= 360;
            }
        }
    }

    public void updateSensorData(Scenario scenario) {
        for (Sensor sensor : sensors) {
            sensor.sense(scenario);
        }

        if (ls != null && tracker != null) {
            // The tracker tracks as the location sensor updates, not as the
            // drone "actually moves" because how does it know.
            tracker.track();
        }
    }

    // We should do something like lower elevation too or some crap, and probably
    // set a landed variable, or maybe if our elevation is zero we know we have landed.
    // Have to research how a drone knows when it is not flying.  I guess when it turns off
    // its rotors.
    public void land() {
        // If we have landed we dont want to be trying to move towards a target, because that
        // will make us try to move.
        setElevation(0);
	    setTargetLocation(ls.x(), ls.x(), 0);
        setVerticalHeading(Constants.VerticalHeading.NONE);
        setTargetElevation(0);
        stopMoving();

        // Basically we have turned off the rotors.  This could probably be done in a more
        // interesting and realistic way although modern drones probably have an automated
        // landing capability.  So we will assume this is done by the drone.
        flying = false;  
    }

    public void launch() {
        flying = true;
        //setTargetElevation(10); // Target elevation is always 10 meters higher than whatever you are over
        //setElevation(10); // Perhaps it should slowly increase elevation according to algorithms
    }

    @Override
    public void damage(int amt) {
        health -= amt;
        if (health <= 0) {
            breakDrone();
        }
    }

    // Probably this draws differently, maybe it happens when we have
    // too many collisions or collisions at too high speed.  Like give
    // each drone hit points, and as they collide at certain speeds they
    // take more damage until they break.
    public void breakDrone() {
        broken = true;
    }

    public void rechargeBatteries(long millis) {
        for (Battery battery : batteries) {
            if (battery.full() == false) {
                battery.recharge(millis);
                continue;
            }
        }
    }

    public void rechargeBatteriesFull() {
        if (batteries == null) {
            return;
        }
        for (Battery battery : batteries) {
            battery.rechargeFull();
        }
    }

    public void setBatteryLifeMinutes(long nMinutes) {
        if (batteries == null) {
            return;
        }
        // Just set one battery
        for (Battery battery : batteries) {
            battery.setLifeMinutes(nMinutes);
            //battery.rechargeFull();
            break;
        }
    }

    public int getBatteryPercent() {
        double overall = 0;
        double ratio = batteries.size();
        for (Battery battery : batteries) {
            overall += (double)battery.percent() / ratio;
        }
        return (int)Math.ceil(overall);
    }
    
    public boolean batteriesFull() {
        for (Battery battery : batteries) {
            if (battery.full() == false) {
                return false;
            }
        }
        return true;
    }

    public double getWiFiRangeMeters() {
        if (wifi == null) {
            return 0;
        }
        return wifi.getRangeMeters();
    }

    public double getWiFiRangePixels() {
        if (wifi == null) {
            return 0;
        }
        return wifi.getRangePixels();
    }

    // All the life of all batteries added together
    public int getBatteryLifeMinutes() {
        int mins = 0;
        for (Battery battery : batteries) {
            mins += battery.lifeMinutes();
        }
        return mins;
    }

    // Do we have any power left?
    public boolean hasPower() {
        for (Battery battery : batteries) {
            if (battery.dead() == false) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean move() {
        if (flying == false) {
            return false;
        }
        return super.move();
    } 

    public boolean updateBatteries() {
        // It really is about how much time we consider each tick or
        // update to be.  Right now lets sort of assume 60 FPS and make
        // each tick a frame of that.  So, each frame is 1/60 of a second.
	    //long now = System.currentTimeMillis();
        for (Battery battery : batteries) {
            if (battery.dead() == true) {
                continue;
            }
            //Math.round(1000/60)
            // rounding 1000 / 60 = 16.666, equals 17, just putting it 
            // straight in for efficiency reasons since this is called
            // constantly
            battery.drain(17);
            return true;
        }

        return false;
    }

    // Some tasks we just dont need to do constantly.
    public void updateOneSecond() {
        // See if anyone on our drone list has expired.
        pruneDroneList();        
    }
    
    public void updateTwoTimesASecond() {
        // Can add anything here
    }

    public void updateThreeTimesASecond() {
        // Broadcast our position.  This is probably
        // enough.  If we do it too often we kinda overload the system.
        if (wifi != null) {
            wifi.broadcastPosition();
        }
    }

    public void updateSixTimesASecond() {
        // Can add anything here
    }

    public void updateTenTimesASecond() {
        // Can add anything here
    }

    public boolean checkModule(boolean activated, BehaviorModule mod) {
        if (activated == true || mod == null)
            return activated;
        if (mod.react() == true) {
            lastActiveModule = mod;
            return true;
        }
        return false;
    }
    
    // A way to reset a behavior module externally to the drone
    public boolean resetModule(String strModuleName) {
        BehaviorModule mod = BehaviorLoader.getModule(behaviors, strModuleName);
        if (mod == null) {
            return false;
        }
        mod.reset();
        return true;
    }

    public void updateContinuous(Scenario scenario) {
        if (updateBatteries() == false) {
            // Oops, our batteries ran out, we are hosed!
            // Actually we should drop out of the sky in this
            // case.  So we should keep moving, just not of our
            // own accord.  For now let's just stop.
            stopMoving();
            return;
        }
        else if (broken == true) {
            // Broken, also hosed.
            // Here we also should drop out of the sky.
            // Basically our acceleration rate should decrease
            // as per wind resistance and we should fall down as per
            // gravity.
            stopMoving();
            return;
        }

        updateSensorData(scenario);

        // Use our subsumption-inspired architecture to check each
        // of the modules one by one, since they essentially compete
        // we could get some squirrely behavior by changing the order
        // of the modules in the list.
        boolean activated = false;
        for (BehaviorModule mod : behaviors) {
            activated = checkModule(activated, mod);
        }
        updateGraphics();
    }

    int ticks = 0;
    public void update(Scenario scenario) {
        // These are things that just always happen
        updateContinuous(scenario);

        ticks++;

        if (ticks % 6 == 0) {
            updateTenTimesASecond();
        }

        if (ticks % 10 == 0) {
            updateSixTimesASecond();
        }

        if (ticks % 20 == 0) {
            updateThreeTimesASecond();
        }

        if (ticks % 30 == 0) {
            updateTwoTimesASecond();
        }

        // We set 60 FPS for the simulation.  This is the timescale it runs on.
        // So 60 ticks can be considered one second of simulation time.
        if (ticks % 60 == 0) {
            updateOneSecond();
        }

        // Roll over when we hit one minute. 
        if (ticks > 360) {
            // We could also put an updateOneMinute in here if we wanted.  Maybe
            // run some diagnostics or something.
            ticks = 0;
        }
    }
}

