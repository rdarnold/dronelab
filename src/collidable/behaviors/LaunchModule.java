package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;

public class LaunchModule extends BehaviorModule {

    public LaunchModule() {
        super(Constants.STR_LAUNCH, Constants.STR_LAUNCH_J);
        drawLetter = "L";
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
        // Basically, we try to launch the thing unless certain circumstances are present,
        // such as if we are at our place of origin without a full battery.

        if (drone.isFlying() == true) {
            // Already flying, no need to do anything.
            return false;
        }

        // Ok we have no idea where we are.  Whatever, lets just launch.  Maybe when
        // we're in the air someone can catch us with an RF signal and drive us back.
        if (drone.ls == null) {
            drone.launch();
            return true;
        }
        
        if (Physics.withinDistance(drone.ls.x(), drone.ls.y(), drone.getStartingX(), drone.getStartingY(), 10) == true) {
            // Ok we are really close or right at our starting point, which means we are probably charging,
            // were charging, or something along those lines.  If we have max battery, launch anyway.  If
            // we dont, better hang out and get a recharge because chances are we came back here for one.
            // Even if we failed to auto charge, maybe someone will like pick us up and put us on the
            // charger and then we can go.
            if (drone.getBatteryPercent() >= 99) {
                drone.launch();
                return true;
            }
        }

        return false;
    }
}