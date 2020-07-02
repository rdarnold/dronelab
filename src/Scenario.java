package dronelab;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import javafx.scene.image.Image;
//import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.io.*; 
import javafx.application.Platform;
//import java.awt.image.BufferedImage;

import dronelab._3d.*;
import dronelab.collidable.*;
import dronelab.gui.*;
import dronelab.utils.*;

public class Scenario extends ScenarioLoader {

    Color victimColor = null;

    public boolean buildMode = false;

    public EnumSet<Constants.DrawFlag> drawFlags = EnumSet.noneOf(Constants.DrawFlag.class);
    private int timeFactor = 1; 
    public TimeData simTime;
    public SimParams simParams;

    public double zoomFactor = 1.0;
    
    public EnvironmentBuilder builder;
    public EnvironmentGenerator generator;

    double canvasMouseX = 0;
    double canvasMouseY = 0;

    public boolean loading = false;
    public boolean paused = false; // Nothing moves when it's paused although it keeps running

    PhysicsEngine engine;
    DistanceScale distanceScale;

    // Start and end ticks so we can record how long a simulation
    // run took in real time when being fast-forwarded it etc.
    private long m_nStartTicks = 0;
    private long m_nEndTicks = 0;
    private long m_nLastRunMilliseconds = 0;

    // Unfortunately we need a pointer back to our main application in case
    // we want to trigger any draw events outside of the mainloop update cycle
    // like when it is turned off during build mode.
    public DroneLab sim;

    public void init(DroneLab s) {
        init(s, null);
    }

    public void init(DroneLab s, String jsonFile) {
        simParams = new SimParams();
        simTime = new TimeData();
        distanceScale = new DistanceScale(this);
        engine = new PhysicsEngine(this);
        generator = new EnvironmentGenerator(this);
 	    sim = s;

        initDrawFlags();
	    GraphicsHelper.init();

        // Do this first
        if (load(jsonFile) == false) {
            loadNewBackground("Scenario1_BG_Sendai.jpg"); //, 6500);
            createDefault();
        } else {
            deployAll();
        }

        if (drones != null && drones.size() > 0) {
            setSelectedDrone(drones.get(0));
        }
    }

    public void setupAlgorithm(SimParams.AlgorithmFlag flag) {
        // Start by stripping them all off.
        processRemoveAllBehaviors();
        if (flag == SimParams.AlgorithmFlag.STANDARD) {
            processAddBehavior(Constants.STR_AVOID);
            processAddBehavior(Constants.STR_SEEK);
            processAddBehavior(Constants.STR_SEARCH);
            processAddBehavior(Constants.STR_WANDER);
            processAddBehavior(Constants.STR_FORM);
            processAddBehavior(Constants.STR_RECHARGE);
            processAddBehavior(Constants.STR_LAUNCH);
            processAddBehavior(Constants.STR_MAINTAIN_HEIGHT);
            processAddBehavior(Constants.STR_CLIMB);
        }
        else if (flag == SimParams.AlgorithmFlag.SPIRAL) {
            processAddBehavior(Constants.STR_AVOID);
            processAddBehavior(Constants.STR_SEEK);
            processAddBehavior(Constants.STR_SEARCH);
            processAddBehavior(Constants.STR_WANDER);
            processAddBehavior(Constants.STR_FORM);
            processAddBehavior(Constants.STR_RECHARGE);
            processAddBehavior(Constants.STR_LAUNCH);
            processAddBehavior(Constants.STR_MAINTAIN_HEIGHT);
            processAddBehavior(Constants.STR_CLIMB);
            processAddBehavior(Constants.STR_SPIRAL);
        }
        else if (flag == SimParams.AlgorithmFlag.SCATTER) {
            processAddBehavior(Constants.STR_AVOID);
            processAddBehavior(Constants.STR_SEEK);
            processAddBehavior(Constants.STR_WANDER);
            processAddBehavior(Constants.STR_RECHARGE);
            processAddBehavior(Constants.STR_LAUNCH);
            processAddBehavior(Constants.STR_MAINTAIN_HEIGHT);
            processAddBehavior(Constants.STR_CLIMB);
            processAddBehavior(Constants.STR_REPEL);
            processAddBehavior(Constants.STR_SCATTER);
        }
        else if (flag == SimParams.AlgorithmFlag.MIX_SRA) {
            // This was loaded from a sim matrix and we have mixed roles with different
            // behaviors we want to assign, so this works differently.
            // All drones receive these common behaviors
            processAddBehavior(Constants.STR_AVOID);
            processAddBehavior(Constants.STR_SEEK);
            processAddBehavior(Constants.STR_SEARCH);
            processAddBehavior(Constants.STR_WANDER);
            processAddBehavior(Constants.STR_RECHARGE);
            processAddBehavior(Constants.STR_LAUNCH);
            processAddBehavior(Constants.STR_MAINTAIN_HEIGHT);
            processAddBehavior(Constants.STR_CLIMB);

            // Now only ones with certain roles receive other behaviors
            // This really should be done within the drone class itself, like it assigns its own
            // behaviors based on the role it has - TODO future improvement

            // Just for debugging
            int numRole1 = 0;
            int numRole2 = 0;
            int numRole3 = 0;
            int numRole_boo = 0;
            int wifi_range = (int)Constants.MAX_WIFI_RANGE;
            for (Drone d : drones) {
                wifi_range = (int)d.wifi.getRangeMeters();
                switch (d.getDroneRole()) {
                    case SOCIAL:
                        numRole1++;
                        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(Constants.STR_FORM));
                        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(Constants.STR_SPIRAL));
                        break;
                    case RELAY:
                        numRole2++;
                        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(Constants.STR_RELAY));
                        break;
                    case ANTISOCIAL:
                        numRole3++;
                        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(Constants.STR_SPIRAL));
                        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(Constants.STR_ANTI));
                        break;
                    default:
                        numRole_boo++;
                        break;
                }
            }
            if (numRole_boo > 0) {
                Utils.log("UNDEFINED ROLE: " + numRole_boo);
            }
            Utils.log("NEW SIM SETUP; SOCIAL: " + numRole1 + ", RELAY: " + numRole2 + ", ANTI: " + numRole3 + 
                ", WIFI: " + wifi_range);
        }
        else {
            // Just add 'em all if we didn't specify.
            for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
                processAddBehavior(Constants.STR_BEHAVIORS[i]);
            }
        }
    }

    public void applyAlgorithm() {
        setupAlgorithm(simParams.getAlgorithmFlag());
        // Really I should just have like algorithms
        // which are behavior sets that can be applied or not.
        /*if (simParams.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD) {
            setupAlgorithm(SimParams.AlgorithmFlag.STANDARD);
        } 
        else if (simParams.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL) {
            setupAlgorithm(SimParams.AlgorithmFlag.SPIRAL);
        } 
        else if (simParams.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER) {
            setupAlgorithm(SimParams.AlgorithmFlag.SCATTER);
        }
        else {
            setupAlgorithm(SimParams.AlgorithmFlag.NOT_DEFINED);
        }*/
    }

    // Get the simulation ready to be run.  Deploy drones in the
    // appropriate places, and generate appropriate survivors as
    // needed.
    public void deployAll() {
        clearDrones();
        clearVictims();
        collisions.clear();
        simTime.reset();
        simTime.setMaxSeconds(simParams.getTimeLimitSeconds());
        for (Deployment dep : deployments) {
            dep.deploy(this);
        }

        // First load the original survivor list from our scenario,
        // then overtop of that we load our random distribution.
        addVictims(loadedSurvivors);

        // Generate random survivors as needed, this really should
        // happen when we start the sim, like "do one sim run"
        // But only generate if we did not autoload the previously generated
        // file.
        if (Config.getAutoLoaded() == false) {
            int nNumRandomSurvivors = simParams.getNumRandomSurvivors();
            if (nNumRandomSurvivors > 0) {
                generator.generateVictims(nNumRandomSurvivors);
    
                // Now that we've generated, let's actually save this out as the
                // current scenario.
                saveFile(Constants.SCENARIO_CURRENT_FILE_NAME);
            }
        }
        else {
            // At this point we can turn off the autoload
            Config.save();
        }

        if (drones != null && drones.size() > 0) {
            setSelectedDrone(drones.get(0));
        }

        // Also do the algorithms
        applyAlgorithm();

        m_nStartTicks = System.currentTimeMillis();
        m_nEndTicks = System.currentTimeMillis();
    }

    // Do whatever weird stuff I want here after load.
    public void customProcess() {
        /*for (Obstacle obs : obstacles) {
            obs.setElevation(obs.getElevation() * 2);
        }*/
    }

    public boolean load(String fileName) {
        loading = true;
        if (super.loadFile(fileName) == false) {
            loading = false;
            return false;
        }
        if (drones != null && drones.size() > 0) {
            setSelectedDrone(drones.get(0));
        }
        customProcess();
        reset();
        loading = false;

        // Send it back to the main thread to update
        // the gui.  This actually seems to work a lot of the time.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                sim.signalNewScenarioLoaded();
                //customProcess();
            }
        });
        return true;
    }

    public double getStartingHvalue() {
        return ((startingX + 50) / currentWidth);
    }

    public double getStartingVvalue() {
        return ((startingY + 150) / currentHeight);
    }

    public void initDrawFlags() {
        drawFlags.add(Constants.DrawFlag.MODULES);
        drawFlags.add(Constants.DrawFlag.COLLISIONS);
        drawFlags.add(Constants.DrawFlag.STRUCTURES);
        drawFlags.add(Constants.DrawFlag.VICTIMS);
        drawFlags.add(Constants.DrawFlag.DEPLOYMENTS);
    }

    public void updateDrawFlags(boolean newVal, Constants.DrawFlag flag) {
        if (newVal == true) {
            drawFlags.add(flag);
        } else {
            drawFlags.remove(flag);
        }
    }

    public int getTimeFactor() { return timeFactor; }
    public int setTimeFactor(int factor) {
        timeFactor = factor;
        if (timeFactor <= 1) {
            timeFactor = 1;
        }
        else if (timeFactor > Constants.MAX_FFW_RATE) {
            timeFactor = Constants.MAX_FFW_RATE;
        }
        return timeFactor;
    }

    public void reset() {
	    resetDrones();
        resetVictims();
        resetDeployments();
        collisions.clear();
        simTime.reset();
    }

    public void togglePause() {
        if (paused == true) {
            paused = false;
        } else {
            paused = true;
        }
    }

    public void deleteDrone(Drone delDrone) {
        boolean setNewDrone = false;
        if (drone == delDrone) {
            setNewDrone = true;
            drone = null;
        }
        drones.remove(delDrone);
        mobiles.remove(delDrone);

        if (setNewDrone == true && drones.size() > 0) {
            setSelectedDrone(drones.get(0));
        }
    }

    public void deleteCurrentDrone() {
        // Cant delete the last one, we need at least one drone
        if (drones.size() <= 1) {
            return;
        }
        deleteDrone(drone);
    }

    public void processKeyRelease(KeyCode keyCode) {
        switch (keyCode) {
            case A:
                processAKey();
                break;
            case S:
                processSKey();
                break;
            case DELETE:
                processDeleteKey();
                break;
        }
	    sim.signalRedraw();
    }

    public void processAKey() {
        // Add a new drone
        addDrone(canvasMouseX, canvasMouseY);
    }

    public void processSKey() {
        //drone.stabilizeMovement();
        drone.stabilize = !drone.stabilize;
    }

    public void processDeleteKey() {
        deleteCurrentDrone();
    }

    public void processChangeDroneType(String sel) {
        for (Drone d : drones) {
	        processChangeDroneType(d, sel);
        }
    }

    public void processChangeDroneType(Drone d, String sel) {
        String typeName = GuiUtils.getDroneTypeNameBilingual(sel);
        d.setDroneType(typeName);
        DroneTemplate.setDroneParams(d, typeName);
    }

    public void processAddBehavior(String sel) {
        for (Drone d : drones) {
	        d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(sel));
        }
        sim.updateDroneParameterFields();
    }

    public void processRemoveAllBehaviors(Drone d) {
        for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
            d.removeBehavior(Constants.STR_BEHAVIORS[i]);
        }
        sim.updateDroneParameterFields();
    }

    public void processRemoveAllBehaviors() {
        for (Drone d : drones) {
	        processRemoveAllBehaviors(d);
        }
    }

    public void processRemoveBehavior(String sel) {
        for (Drone d : drones) {
	        d.removeBehavior(GuiUtils.getBehaviorModuleNameBilingual(sel));
        }
        sim.updateDroneParameterFields();
    }

    public void processAddBehavior(Drone d, String sel) {
        if (d == null) {
            return;
        }
	    d.addBehavior(GuiUtils.getBehaviorModuleNameBilingual(sel));
        sim.updateDroneParameterFields();
    }

    public void processRemoveBehavior(Drone d, String sel) {
        if (d == null) {
            return;
        }
	    d.removeBehavior(GuiUtils.getBehaviorModuleNameBilingual(sel));
        sim.updateDroneParameterFields();
    }

    public void startBuilding() {
        if (builder == null) {
            builder = new EnvironmentBuilder(this);
        }
        builder.start();
        buildMode = true;
    }

    public void save() {
        if (builder != null) {
            builder.saveCodeForObstacles(obstacles);
            builder.saveCodeForVictims(victims);
            builder.saveCodeForDrones(drones);
        }
        saveFile();
    }

    public void stopBuilding() {
        buildMode = false;
        if (builder != null) {
            builder.stop();
        }
    }

    public void updateCanvasMouseCoordinates(double x, double y) {
        canvasMouseX = x;
        canvasMouseY = y;
    }

    public Drone addDrone() {
        return addDrone(-1, -1);
    }

    @Override
    public Drone addDrone(double x, double y) {
        Drone dro = super.addDrone(x, y);
        if (drone == null) {
            setSelectedDrone(dro);
        }
        return dro;
    }

    public void setSelectedDrone(Drone d) {
        drone = d;
        sim.updateAllCurrentDroneInfo();
    }

    public void handleScrollEvent(double delta) {
        if (buildMode == true) {
            if (builder == null) {
                return;
            }
            if (delta > 0) {
                builder.decreaseElevation(1);
            } else {
                builder.increaseElevation(1);
            }
        }
        /*if (delta > 0) {
            // Zooming in
            zoomLevel*=1.1;
        } else {
            // Zooming out
            zoomLevel/=1.1;
            //zoomLevel-=0.1;
            if (zoomLevel < 0.1) {
                zoomLevel = 0.1;
            }
        }*/
    }

    public int getNumVictims() {
        return victims.size();
    }

    public int getNumVictimsLocated() {
        int num = 0;
        for (Person person : victims) {
            if (person.isLocated() == true) {
                num++;
            }
        }
        return num;
    }

    public int getNumVictimsSeen() {
        int num = 0;
        for (Person person : victims) {
            if (person.isSeen() == true) {
                num++;
            }
        }
        return num;
    }

    public void updateSectors(Mobile mob) {
        // It has just moved.  Check if it has changed
        // sectors.

        // First, is it still within the sectors it says it is?
        for (int i = mob.sectors.size() - 1; i >= 0; i--) {
            Sector sect = mob.sectors.get(i);
            if (sect.isInsideBounds(mob) == false) {
                mob.sectors.remove(sect);
                sect.remove(mob);
            }
        }

        // Now let's just see if he's in any new ones
        for (Sector sect : sectors) {
            if (sect.isInsideBounds(mob) == false)
                continue;
            if (mob.sectors.contains(sect)) 
                continue;
            sect.add(mob);
        }
    }

    public void updateBroadcastQueue() {
        for (Drone d : drones) {
            if (d.wifi != null) {
                for (String msg : d.wifi.getQueue()) {
                    BroadcastMessage bc = new BroadcastMessage(d.getId(), d.x(), d.y(), d.wifi.getRange(), msg, Constants.CommType.WIFI);
                    broadcasts.add(bc);
                }
                d.wifi.clearQueue();
            }
        }
    }

    public void updateBroadcasts() {
        // What broadcasts have just been sent? These should be
        // "instantaneous" so we go through and see whos in range;
        // anyone in range receives, if they have a receiver for the broadcast
        // type
        // First gather broadcasts from the drones
        updateBroadcastQueue();

        // Now send it out to each drone
        for (BroadcastMessage broad : broadcasts) {
            for (Drone d : drones) {
                // Dont try to send to yourself
                if (d.getId() == broad.fromId) {
                    continue;
                }

                // What could we do with latency here?  If there is a high latency maybe
                // messages just skip beats?  Like assign them latency values that tick down
                // maybe and when they are done, the message sends?

                // If not in range, you dont receive it.
                if (Physics.withinDistance(d.x(), d.y(), broad.x, broad.y, broad.range) == false) {
                    continue;
                }

                if (broad.commType == Constants.CommType.WIFI) {
                    d.wifi.receive(broad.msg);
                }
            }
        }
        broadcasts.clear();
    }

    public void endRun() {
        m_nEndTicks = System.currentTimeMillis();
        m_nLastRunMilliseconds = m_nEndTicks - m_nStartTicks;
        sim.signalComplete();
    }

    public int getLastRunSeconds() {
        return (int)(m_nLastRunMilliseconds / 1000);
    }

    public boolean update() {
        // So, we update X number of times here depending on our timeFactor
        for (int upd = 0; upd < timeFactor; upd++) {
            if (simTime.advanceFrame() == false) {
                // Stop!  Time limit reached.
                endRun();
                return false;
            }
            updateBroadcasts();
            for (Drone d : drones) {
                d.update(this);
            }
            if (paused == false) {
                engine.moveAll();
            }
            
            Iterator<Collision> i = collisions.iterator();
            while (i.hasNext()) {
                Collision c = i.next(); // must be called before you can call i.remove()
                if (c.update() == false) {
                    i.remove();
                }
            }
        }
        return true;
    }
                
    public void draw(GraphicsContext gc, Rectangle canvasVisibleRect) {

        GraphicsHelper.updateDrawCounters();

        // This would probably be more efficient
        /*double xMin = canvasVisibleRect.getX();
        double xMax = xMin + canvasVisibleRect.getWidth();
        double yMin = canvasVisibleRect.getY();
        double yMax = yMin + canvasVisibleRect.getHeight();*/

        // Might it be better to actually use an ImageView for the background instead?
        // and put it behind the canvas on the stackpane?  It might make the drawing
        // and loading more efficient.  We could load in native resolution and then
        // just let the image expand and contract as needed instead of loading it into
        // a much larger size as we do today.  Or we could load it as its native
        // resolution and then just zoom in ... same difference right?
        //imageView.setFitWidth
        if (backGround != null) {
            //gc.drawImage(backGround, 0, 0);
            // Much more efficient of course to draw only the region of the image that
            // we can see.
            gc.drawImage(backGround, canvasVisibleRect.getX(), canvasVisibleRect.getY(), 
                canvasVisibleRect.getWidth(), canvasVisibleRect.getHeight(),
                canvasVisibleRect.getX(), canvasVisibleRect.getY(), 
                canvasVisibleRect.getWidth(), canvasVisibleRect.getHeight());
        }


        if (drawFlags.contains(Constants.DrawFlag.STRUCTURES) == true) {
            for (Obstacle o : obstacles) {
                if (o.isVisibleOnScreen(canvasVisibleRect) == false) {
                    continue;
                }
                o.draw(gc); 
            }
        }

        if (drawFlags.contains(Constants.DrawFlag.DEPLOYMENTS) == true) {
            for (Deployment dep : deployments) {
                dep.draw(gc);
            }
        }

        if (drawFlags.contains(Constants.DrawFlag.COLLISIONS) == true) {
            for (Collision c : collisions) {
                c.draw(gc);
            }
        }

        if (drawFlags.contains(Constants.DrawFlag.VICTIMS) == true) {
            for (Person v : victims) {
                if (v.isVisibleOnScreen(canvasVisibleRect) == false) {
                    continue;
                }
                v.draw(gc);
            }
        }
        
        for (RescueWorker rw : rescueWorkers) {
            if (rw.isVisibleOnScreen(canvasVisibleRect) == false) {
                continue;
            }
            rw.draw(gc);
        }

        for (Drone d : drones) {
            if (d.isVisibleOnScreen(canvasVisibleRect) == false) {
                continue;
            }
            d.draw(gc, drawFlags);
        }

        if (builder != null && buildMode == true) {
            builder.draw(gc, canvasMouseX, canvasMouseY);
        }

        distanceScale.draw(gc, canvasVisibleRect);
    }

    public void setSeekLocationForDrones(double x, double y) {
        for (Drone d : drones) {
            d.setSeekLocation(x, y);
        }
    }

    public void onMouseMove(double x, double y) {
        if (buildMode == false) {
            
        } else {
            builder.onMouseMove(x, y);
        }
    }

    public void onMouseDrag(double x, double y) {
        if (buildMode == false) {
            
        } else {
            builder.onMouseDrag(x, y);
        }
    }

    public void leftClick(double x, double y) {
        if (buildMode == false) {
            // If we have a SeekModule, set our seek location
            setSeekLocationForDrones(x, y);
            sim.signalRedraw();
        } else {
            builder.leftClick(x, y);
        }
    }

    public void rightClick(double x, double y) {
        if (buildMode == true) {
            builder.rightClick(x, y);
        } else {
            sim.signalRedraw();
        }
    }
}