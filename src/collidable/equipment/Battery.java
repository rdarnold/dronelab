package dronelab.collidable.equipment;

public class Battery {
    private long maxBatteryLifeMilliSeconds = 30 * 60 * 1000;
    private long batteryLifeMilliSeconds = maxBatteryLifeMilliSeconds;
    private long rechargeRate = 1;

    public Battery() {}

    public Battery(long amount) {
        setMaxLifeMilliseconds(amount);
    }

    public void setMaxLifeMilliseconds(long amount) {
        maxBatteryLifeMilliSeconds = amount;
        batteryLifeMilliSeconds = maxBatteryLifeMilliSeconds;
    }

    public void setLifeMilliseconds(long amount) {
        batteryLifeMilliSeconds = amount;
    }

    public void setLifeSeconds(long amount) {
        batteryLifeMilliSeconds = amount * 1000;
    }

    public void setLifeMinutes(long amount) {
        batteryLifeMilliSeconds = amount * 60 * 1000;
    }

    public boolean dead() { return (batteryLifeMilliSeconds <= 0); }
    public boolean full() { return (batteryLifeMilliSeconds >= maxBatteryLifeMilliSeconds); }

    public int percent() {
        // We want to round up here so that we can be just about full but not completely full,
        // and still read as full
        double perc = ((double)batteryLifeMilliSeconds / (double)maxBatteryLifeMilliSeconds) * 100;
        return (int)Math.ceil(perc); 
    }

    public double maxLife() { return maxBatteryLifeMilliSeconds; }
    public double maxLifeMinutes() { return maxLifeSeconds() / 60; }
    public double maxLifeSeconds() { return maxBatteryLifeMilliSeconds / 1000; }
    public double maxLifeMilliSeconds() { return maxBatteryLifeMilliSeconds; }
    public double life() { return batteryLifeMilliSeconds; }
    public double lifeMinutes() { return lifeSeconds() / 60; }
    public double lifeSeconds() { return batteryLifeMilliSeconds / 1000; }
    public double lifeMilliSeconds() { return batteryLifeMilliSeconds; }

    public boolean drain(long millisPassed) {
        batteryLifeMilliSeconds -= millisPassed;

        if (batteryLifeMilliSeconds <= 0) {
            batteryLifeMilliSeconds = 0;
            return false;
        }

        return true;
    }

    public long recharge(long millis) {
        batteryLifeMilliSeconds += (millis * rechargeRate);

        if (batteryLifeMilliSeconds >= maxBatteryLifeMilliSeconds) {
            batteryLifeMilliSeconds = maxBatteryLifeMilliSeconds;
        }

        return batteryLifeMilliSeconds;
    }

    public void rechargeFull() {
	    batteryLifeMilliSeconds = maxBatteryLifeMilliSeconds;
    }

}