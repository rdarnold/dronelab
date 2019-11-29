//package sim;
import java.util.ArrayList;
import java.util.EnumSet;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class Drone extends Mobile {
    private Constants.DroneType droneType = Constants.DroneType.ROTOR;
    double rotarHeading = 0; // Just for visual effect
    int rotarSpeed = 16; //31;
    PathTracker tracker = null;
    double targetHeightDistance = Distance.pixelsFromMeters(4); // Desired difference in height between obj below us and us.

    private boolean flying = true;
    private boolean broken = false;
    private int maxHealth = 20;
    private int health = maxHealth; 

    SonarSensor ss = null;
    LocationSensor ls = null;
    ThermalCameraSensor irCam = null;
    FinderSensor fs = null;

    BehaviorModule lastActiveModule = null;
    AvoidModule av = null;
    SeekModule sm = null;
    WanderModule wm = null;
    SearchPatternModule sch = null;
    FormationModule form = null;
    RechargeModule rch = null;
    LaunchModule lnch = null;
    MaintainHeightModule mhgt = null;
    ClimbModule clmb = null;
    RelayModule rly = null;
    AntiSocialMdoule anti = null;

    WiFiCommunicator wifi = null;

    ArrayList<BehaviorModule> behaviors;
    ArrayList<Sensor> sensors;
    ArrayList<Battery> batteries;

    ArrayList<DroneData> droneList; // Data on the other drones flying nearby

    //String lastActiveBehaviorName = "";

    static final int dotSize = 6;
    static final int rotarSize = 10;

    int id = 0;

    public Drone() {
        super();
        setupDrone(0, false);
    }

    public Drone(int newId) {
        super();
        setupDrone(newId, false);
    }

    // We can make fake drones to test various things like
    // collisions
    public Drone(boolean isFake) {
        super();
        setupDrone(0, isFake);
    }

    public int getMaxHealth() { return maxHealth; }
    public int getHealth() { return health; }

    private void setupDrone(int newId, boolean isFake) {
        // 60 frames per second.
        //setMaxSpeed(Distance.pixelsPerSecondFromMPH(45) / 60.0);
        setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(20));
        setMaxVerticalSpeed(getMaxSpeed() / 3.0); // Typical vertical speed is a third of regular speed

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        setMaxAccelerationRate(getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        setMaxVerticalAccelerationRate(getMaxAccelerationRate());

        if (isFake == true) {
            setDroneType(Constants.DroneType.FAKE);
        } else {
            droneList = new ArrayList<DroneData>();
            setId(newId);
            // This should come after the setDroneType so that the sizes
            // are in place.
            setDroneType(Constants.DroneType.ROTOR);
            setupSensorsAndModules();
            tracker = new PathTracker(this);
        }
    }

    public void setId(int nId) {
        id = nId;
    }

    /*@Override
    public void setBounds(int left, int top, int right, int bottom)
    {
        super.setBounds(left, top, right, bottom);
        if (wm != null) {
            wm.setBounds(left, top, right, bottom); 
        }
    }*/

    public Constants.DroneType getDroneType() {
        return droneType;
    }

    public boolean hasThermalCamera()      { return irCam != null; }
    public boolean hasSonarSensor()        { return ss != null; }
    public boolean hasFinderSensor()       { return fs != null; }
    public boolean hasLocationSensor()     { return ls != null; }

    public boolean isFlying() { return flying; }

    public void setDroneType(String typeName) {
        if (typeName == Constants.STR_DRONE_ROTOR) {
            setDroneType(Constants.DroneType.ROTOR);
        } else {
            setDroneType(Constants.DroneType.DOT);
        }
    }

    public void setDroneType(Constants.DroneType newType) {
        double x = 0;
        double y = 0;

        if (super.getShape() != null) {
            x = x();
            y = y();
        }
	    droneType = newType;
        switch (newType) {
            case FAKE:
                // The fake drone defaults to a rotor drone but it automatically
                // gets set to the size of its parent "real" drone anyway soon after
                super.makeCircle(rotarSize/2);
                break;
            case DOT:
                super.makeCircle(dotSize/2);
                break;
            case ROTOR:
                super.makeCircle(rotarSize/2);
                break;
        }

        // And we have to reset the position since it got messed up when we remade the
        // shape
        setPos(x, y);

        //AvoidModule av = (AvoidModule)getBehaviorModule(Constants.STR_AVOID);
        if (av != null) {
            // This must occurr after the makeCircle and all various processing and setup is
            // done on the drone so that we know how to proportion our collision checking.
            av.setupCollisionDetection();
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
        if (sch != null) {
            sch.setSearchPattern();
        }
    }

    // We should allow multiple redundant sonar sensors
    // facing different directions
    public Sensor getSensor(String name) {
        for (Sensor sensor : sensors) {
            if (sensor.name == name)
                return sensor;
        }
        return null;
    }

    public void removeSensor(String strName) {
        Sensor sensor = getSensor(strName);

        if (strName == Constants.STR_SENSOR_SONAR)
	        ss = null;
        else if (strName == Constants.STR_SENSOR_THERMAL)
	        irCam = null;
	    else if (strName == Constants.STR_SENSOR_LOCATION)
	        ls = null;
	    else if (strName == Constants.STR_SENSOR_FINDER)
	        fs = null;

        if (sensor != null) {
            sensors.remove(sensor);
        } 
    }

    public void addSensor(String strName) {
        Sensor sensor = null;
        if (strName == Constants.STR_SENSOR_SONAR)
            sensor = (ss = new SonarSensor(this));
        else if (strName == Constants.STR_SENSOR_THERMAL)
            sensor = (irCam = new ThermalCameraSensor(this));
	    else if (strName == Constants.STR_SENSOR_LOCATION)
            sensor = (ls = new LocationSensor(this));
	    else if (strName == Constants.STR_SENSOR_FINDER)
            sensor = (fs = new FinderSensor(this));

        if (sensor != null) {
            sensors.add(sensor);
        } 
    }

    public void addModuleToList(ArrayList<BehaviorModule> list, String strName) {
        BehaviorModule mod = getBehaviorModule(strName);
        if (mod != null) {
            list.add(mod);
        }
    }

    public void updateBehaviorDrawPriorities() {
        int i = 1;
        for (BehaviorModule mod : behaviors) {
            if (mod.usesDrawLocation() == true) {
                mod.setDrawPriority(i);
                i++;
            }
        }
    }

    // Properly order the behavior/subsumption list, otherwise we could get squirrely stuff.
    public void orderBehaviors() {
        ArrayList<BehaviorModule> tempList = new ArrayList<BehaviorModule>();

        addModuleToList(tempList, Constants.STR_LAUNCH);
        addModuleToList(tempList, Constants.STR_AVOID);
        addModuleToList(tempList, Constants.STR_CLIMB);
        addModuleToList(tempList, Constants.STR_RECHARGE);
        addModuleToList(tempList, Constants.STR_MAINTAIN_HEIGHT);
        addModuleToList(tempList, Constants.STR_FORM);
        addModuleToList(tempList, Constants.STR_SEEK);
        addModuleToList(tempList, Constants.STR_SEARCH);
        addModuleToList(tempList, Constants.STR_WANDER);

        behaviors.clear();
        behaviors.addAll(tempList);

        // Now assign them all their correct priorities for drawing
        // locations, so that we can see 1, 2, 3, etc., for the locations
        // instead of all these different colors and symbols
        updateBehaviorDrawPriorities();

        /*if (av != null)     behaviors.add(av);
        if (rch != null)    behaviors.add(rch);
        if (form != null)   behaviors.add(form);
        if (sm != null)     behaviors.add(sm);
        if (sch != null)    behaviors.add(sch);
        if (wm != null)     behaviors.add(wm);*/
    }

    public BehaviorModule getBehaviorModule(String strName) {
        if (behaviors == null) {
            return null;
        }
        for (BehaviorModule mod : behaviors) {
            if (mod.name == strName) {
                return mod;
            }
        }
        return null;
    }

    public void removeModule(String strName) {
        BehaviorModule mod = getBehaviorModule(strName);

        if (strName == Constants.STR_AVOID)
	        av = null;
        else if (strName == Constants.STR_SEEK)
	        sm = null;
	    else if (strName == Constants.STR_SEARCH)
	        sch = null;
	    else if (strName == Constants.STR_WANDER)
	        wm = null;
	    else if (strName == Constants.STR_FORM)
	        form = null;
	    else if (strName == Constants.STR_RECHARGE)
	        rch = null;
	    else if (strName == Constants.STR_LAUNCH)
	        lnch = null;
	    else if (strName == Constants.STR_MAINTAIN_HEIGHT)
	        mhgt = null;
	    else if (strName == Constants.STR_CLIMB)
	        clmb = null;
            
        if (mod != null) {
            behaviors.remove(mod);
            orderBehaviors();
        } 
    }

    public void addModule(String strName) {
        BehaviorModule mod = null;
        if (strName == Constants.STR_AVOID)
            mod = (av = new AvoidModule(this));
        else if (strName == Constants.STR_SEEK)
            mod = (sm = new SeekModule(this));
	    else if (strName == Constants.STR_SEARCH)
            mod = (sch = new SearchPatternModule(this));
	    else if (strName == Constants.STR_WANDER)
            mod = (wm = new WanderModule(this));
	    else if (strName == Constants.STR_FORM)
            mod = (form = new FormationModule(this));
	    else if (strName == Constants.STR_RECHARGE)
            mod = (rch = new RechargeModule(this));
	    else if (strName == Constants.STR_LAUNCH)
            mod = (lnch = new LaunchModule(this));
	    else if (strName == Constants.STR_MAINTAIN_HEIGHT)
            mod = (mhgt = new MaintainHeightModule(this));
	    else if (strName == Constants.STR_CLIMB)
            mod = (clmb = new ClimbModule(this));

        if (mod != null) {
            behaviors.add(mod);
            orderBehaviors();
        } 
    }

    public void setupSensorsAndModules() {
        sensors = new ArrayList<Sensor>();
        for (String str : Constants.STR_SENSORS) {
            addSensor(str);
        }

        behaviors = new ArrayList<BehaviorModule>();
        for (String str : Constants.STR_BEHAVIORS) {
            addModule(str);
        }
        
        batteries = new ArrayList<Battery>();
        // Give it a battery so it can move ...
        batteries.add(new Battery());

        wifi = new WiFiCommunicator(this);
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
        // consider them to be part of the game.  Give them maybe 10 seconds.
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
    public void updateDroneDataXY(int droneId, double newX, double newY) {
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

        if (droneType == Constants.DroneType.FAKE) {
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

        if (droneType == Constants.DroneType.DOT) {
            //setSize(dotSize);
            //setSize(wid);
            gc.setFill(Color.RED);
            gc.fillOval(x, y, wid, hgt);
            //gc.strokeOval(60, 60, 30, 30);
        } else if (droneType == Constants.DroneType.ROTOR) {
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

            dx = ((x2-x1)*Math.cos(Math.toRadians(rotarHeading+180))) - ((y2-y1)*Math.sin(Math.toRadians(rotarHeading+180))) +x1;
            dy = ((x2-x1)*Math.sin(Math.toRadians(rotarHeading+180))) + ((y2-y1)*Math.cos(Math.toRadians(rotarHeading+180))) +y1;

            dx2 = ((x2-x1)*Math.cos(Math.toRadians(rotarHeading))) - ((y2-y1)*Math.sin(Math.toRadians(rotarHeading))) +x1;
            dy2 = ((x2-x1)*Math.sin(Math.toRadians(rotarHeading))) + ((y2-y1)*Math.cos(Math.toRadians(rotarHeading))) +y1;

            gc.strokeLine(dx, dy, dx2, dy2);

            
            dx = ((x2-x1)*Math.cos(Math.toRadians(rotarHeading+270))) - ((y2-y1)*Math.sin(Math.toRadians(rotarHeading+270))) +x1;
            dy = ((x2-x1)*Math.sin(Math.toRadians(rotarHeading+270))) + ((y2-y1)*Math.cos(Math.toRadians(rotarHeading+270))) +y1;

            dx2 = ((x2-x1)*Math.cos(Math.toRadians(rotarHeading+90))) - ((y2-y1)*Math.sin(Math.toRadians(rotarHeading+90))) +x1;
            dy2 = ((x2-x1)*Math.sin(Math.toRadians(rotarHeading+90))) + ((y2-y1)*Math.cos(Math.toRadians(rotarHeading+90))) +y1;

            gc.strokeLine(dx, dy, dx2, dy2);

            if (broken == true) {
                gc.setStroke(Color.BLACK);
            } else {
                gc.setStroke(Color.DARKBLUE);
            }
            gc.setLineWidth(3);
            gc.strokeOval(x - 2, y - 2, wid +4, hgt+4);
        } 

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
            rotarHeading += rotarSpeed;
            if (rotarHeading >= 360) {
                rotarHeading -= 360;
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
        for (Battery battery : batteries) {
            battery.rechargeFull();
        }
    }

    public void setBatteryLifeMinutes(long nMinutes) {
        // Just set one battery
        for (Battery battery : batteries) {
            battery.setLifeMinutes(nMinutes);
            //battery.rechargeFull();
            break;
        }
    }

    public int batteryPercent() {
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
            battery.drain((Math.round(1000/60)));
            return true;
        }

        return false;
    }

    // Some tasks we just dont need to do constantly.
    public void updateOneSecond() {
        // See if anyone on our drone list has expired.
        pruneDroneList();
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

        // Now process our wifi sending, we have to decide how often
        // to broadcast our position, this might be too often.
        if (wifi != null) {
            wifi.broadcastPosition();
        }

        boolean activated = false;

        // Use our subsumption-inspired architecture to check each
        // of the modules one by one, since they essentially compete
        // we could get some squirrely behavior by changing the order
        // of the modules in the list.
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

        // We assume 60 FPS for the sake of the simulation.  This is the timescale it
        // runs on.
        if (ticks % 60 == 0) {
            updateOneSecond();
        }

        // Roll over when we hit one minute. 
        if (ticks > 360) {
            // We could also put an updateOneMinute in here if we wanted.
            ticks = 0;
        }
    }
}

