package dronelab.collidable;

import dronelab.utils.*;

public class DroneData {
    public int id;
    public Drone.DroneRole role = Drone.DroneRole.SOCIAL; // What role is this drone if we have a multi-role swarm?
    public long lastDetectedTimeMillis; // When did we last see or hear from this drone?  Remove it from the list if it gets stale
    public double x;
    public double y;

    public boolean expired() {
        if (System.currentTimeMillis() - lastDetectedTimeMillis >= Constants.droneDataStaleThresholdMillis) {
            return true;
        }
        return false;
    }
}