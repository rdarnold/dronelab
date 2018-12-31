package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class RechargeModule extends BehaviorModule {

    private boolean recharging = false;

    public RechargeModule() {
        super(Constants.STR_RECHARGE, Constants.STR_RECHARGE_J);
        drawLetter = "R";
    }

    @Override
    public boolean setup() { return true; }
    
    @Override
    public boolean reset() { return true; }
    
    @Override
    public boolean usesDrawLocation() { return false; }

    @Override
    public boolean draw(GraphicsContext gc) {
        return true;
    }

    @Override
    public boolean receive(String msg) { 
        return true;
    }

    @Override
    public boolean react() { 
        if (drone == null) {
            return false;
        }

        // Check to see if our batteries are low, if so, attempt to head back to our
        // charging location if we can find it 
        if (drone.ls == null) {
            return false;
        }

        if (recharge() == true) {
            return true;
        }
        recharging = false;

        // Check to see if we are at less than half battery and are like relatively close
        // to the charging station, or at like a "local minimum" actually this requires some
        // thought, how do we determine?  This could be a separate paper too, the battery recharge
        // algorithm.  Or I could just write about how this is an area of improvement - algorithms
        // for determining when the best time is to recharge the battery.

        if (drone.getBatteryLifeMinutes() > 5) {
            return false;
        }

	    drone.setTargetLocation(drone.getStartingX(), drone.getStartingY(), drone.getMaxSpeed()*.75);
        
        // If we are within a reasonable distance, we should now land and recharge.  But for
        // now we'll just like spend a few minutes recharging or something.  Or maybe just
        // land and stop permanently.  Maybe for now, just auto-recharge to full for the sake
        // of the simulation.
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), drone.getStartingX(), drone.getStartingY(), 10) == true) {
            // Descend!
            //drone.setTargetElevation(1);
            drone.setVerticalHeading(Constants.VerticalHeading.DOWN);
            // We made it, then we land.
            // We need to know whether we are landed or not, there should probably be some landing
            // type of capability.
            // For now  lets just say if our speed is less than a certain amount we are landed
            if (drone.getXSpeed() < 2 && drone.getYSpeed() < 2 && drone.getElevation() <= 1) {
                // For now just recharge.  Or do we want another behavior which is,
                // if we are within range and the batteries are dead, recharge?  Or should
                // that just be the same module?  Does it really matter?
                drone.land();
                recharging = true;
            }
        }

        return true;
    }

    private boolean recharge() {
        if (recharging == true) {
	        drone.rechargeBatteries(2000);
            if (drone.batteriesFull() == true) {
                return false;
            }
            return true;
        }

        return false;
    }
}