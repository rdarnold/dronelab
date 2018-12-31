

public class DroneData {
    int id;
    long lastDetectedTimeMillis; // When did we last see or hear from this drone?  Remove it from the list if it gets stale
    double x;
    double y;

    public boolean expired() {
        if (System.currentTimeMillis() - lastDetectedTimeMillis >= Constants.droneDataStaleThresholdMillis) {
            return true;
        }
        return false;
    }
}