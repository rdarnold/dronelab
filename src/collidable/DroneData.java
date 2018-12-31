package dronelab.collidable;

import dronelab.utils.*;

public class DroneData {
    public int id;
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