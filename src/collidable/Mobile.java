package dronelab.collidable;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import dronelab.Sector;
import dronelab.utils.*;

public class Mobile extends Collidable {

    // Theoretically all mobiles can move and thus they can keep track of the
    // sectors they are in
    public ArrayList<Sector> sectors = new ArrayList<Sector>();

    // These are set properly in DroneTemplate.java
    private double maxSpeed = 3.0; // this is usually super fast actually
    private double maxAscentSpeed = maxSpeed / 3.0; // Typical speed
    private double maxDescentSpeed = maxSpeed / 3.0; // Typical speed
    private double maxAccelerationRate = maxSpeed / 120.0; // 60 FPS, 2 seconds to reach max speed.
    private double accelerationRate = 0.0;
    private double maxVerticalAccelerationRate = maxAccelerationRate;
    private double verticalAccelerationRate = 0.0;
    private double targetSpeed = 0;
    private double targetElevation = 0;
    private boolean seekTargetElevation = false;
    private double speed = 0.0; // This is calculated based on the vectors x and y speed
    private double xSpeed = 0.0;
    private double ySpeed = 0.0;
    private double zSpeed = 0.0;
    private Constants.VerticalHeading verticalHeading = Constants.VerticalHeading.NONE;
    private double headingDegrees = 20; 
    private double previousHeadingDegrees = 0;
    private double targetX = 0;
    private double targetY = 0;
    private boolean seekTarget = true;
    double prevX = 0;
    double prevY = 0;
    double prevZ = 0;

    // If this is set, the drone attempts to nullify horizontal movement instead of moving.
    public boolean stabilize = false;
    public boolean brake = false;

    // What was this mobile's first position?
    private double startingX = -1;
    private double startingY = -1;
    protected int startingElevation = 0;

    public Mobile() { super(); }

    public void setAccelerationToMax() { accelerationRate = maxAccelerationRate; }

    public double getHeadingDegrees() { return headingDegrees; }
    public double getHeadingRadians() { return Math.toRadians(headingDegrees); }
    public double getPreviousHeadingDegrees() { return previousHeadingDegrees; }
    public double getPreviousHeadingRadians() { return Math.toRadians(previousHeadingDegrees); }

    public void setHeadingRadians(double rad) { setHeadingDegrees(Math.toDegrees(rad)); }
    public void setHeadingDegrees(double deg) { 
        // Record our previous heading
        previousHeadingDegrees = getHeadingDegrees();
        headingDegrees = deg; 
    }

    public double getStartingX() { return startingX; }
    public double getStartingY() { return startingY; }
    public int getStartingElevation() { return startingElevation; }

    public double getTargetX() { return targetX; }
    public double getTargetY() { return targetY; }
    
    public void setTargetLocation(double newX, double newY) {
        setTargetLocation(newX, newY, maxSpeed);
    }
    public void setTargetLocation(double newX, double newY, double newSpeed) {
        targetX = newX - wid/2;
        targetY = newY - hgt/2;
        setTargetSpeed(newSpeed);
        accelerationRate = maxAccelerationRate;
        startSeekingTarget();
    }

    public double getTargetElevation()  { return targetElevation; }
    public void setTargetElevation(double newE) { 
        targetElevation = newE; 
        seekTargetElevation = true;
        verticalAccelerationRate = maxVerticalAccelerationRate;
    }

    public void setVerticalHeading(Constants.VerticalHeading newH) {
        verticalHeading = newH;
        verticalAccelerationRate = maxVerticalAccelerationRate;
    }
    public Constants.VerticalHeading getVerticalHeading() { return verticalHeading; }

    public double getMaxSpeed()                     { return maxSpeed; }
    public double getMaxAscentSpeed()             { return maxAscentSpeed; }
    public double getMaxDescentSpeed()             { return maxDescentSpeed; }
    public double getAccelerationRate()             { return accelerationRate; }
    public double getMaxAccelerationRate()          { return maxAccelerationRate; }
    public double getVerticalAccelerationRate()     { return verticalAccelerationRate; }
    public double getMaxVerticalAccelerationRate()  { return maxVerticalAccelerationRate; }

    public void setMaxSpeed(double spd) {
        maxSpeed = spd;
    }
    public void setMaxVerticalSpeed(double spd) {
        setMaxAscentSpeed(spd);
        setMaxDescentSpeed(spd);
    }
    public void setMaxAscentSpeed(double spd) {
        maxAscentSpeed = spd;
    }
    public void setMaxDescentSpeed(double spd) {
        maxDescentSpeed = spd;
    }
    public void setMaxAccelerationRate(double rate) {
        maxAccelerationRate = rate;
    }
    public void setMaxVerticalAccelerationRate(double rate) {
        maxVerticalAccelerationRate = rate;
    }

    public double getSpeed() { return speed; }
    public double getXSpeed() { return xSpeed; }
    public double getYSpeed() { return ySpeed; }
    public double getZSpeed() { return zSpeed; }

    // I only use these for the fake drone and they really should be removed;
    // speed is controlled by the physics engine through acceleration and other
    // factors.
    public void setXSpeed(double spd) { xSpeed = spd; }
    public void setYSpeed(double spd) { ySpeed = spd; }
    public void setZSpeed(double spd) { zSpeed = spd; }
    public void changeXSpeed(double amt) { xSpeed += amt; }
    public void changeYSpeed(double amt) { ySpeed += amt; }
    public void changeZSpeed(double amt) { zSpeed += amt; }

    public void updateSpeedValue() {
        speed = Physics.getSpeed(xSpeed, ySpeed);
    }

    // Basically attempt to reduce our velocity in whatever direction
    // we are currently moving 
    public void applyBrakes() {
        double oldSpeed = getSpeed();
        double revAngleRadians = Math.atan2(-getYSpeed(), -getXSpeed());
        applyAcceleration(revAngleRadians, getMaxAccelerationRate());

        // We should check to make sure we didnt like reverse directions now.
        if (oldSpeed > 0 && getSpeed() < 0) {
            xSpeed = 0;
            ySpeed = 0;
        }
        else if (oldSpeed < 0 && getSpeed() > 0) {
            xSpeed = 0;
            ySpeed = 0;
        }
    }

    public void stopMoving() {
        speed = 0;
        xSpeed = 0;
        ySpeed = 0;
        zSpeed = 0;
        accelerationRate = 0;
        verticalAccelerationRate = 0;
    }

    public void reset() {
        stopMoving();
        resetPos();
        targetX = x();
        targetY = y();
    }


    public void resetPos() {
        if (startingX >= 0 && startingY >= 0) {
       	    setPos(startingX, startingY);
        } 
        elevation = startingElevation;
    }

    public void setStartPos(double newX, double newY) {
        setStartPos(newX, newY, startingElevation);
    }

    public void setStartPos(double newX, double newY, int newE) {
        startingX = newX;
        startingY = newY;
        startingElevation = newE;
    }

    public void maxAcceleration() {
        accelerationRate = maxAccelerationRate;
    }

    // Duplicate the position parameters and possibly other ones
    // later if we need them
    public void copyPositionTo(Mobile other) {
        other.setElevation(this.getElevation());
        other.setPos(x(), y());
    }

    public void copySpeedAndHeadingTo(Mobile other) {
        other.xSpeed = this.xSpeed;
        other.ySpeed = this.ySpeed;
        other.zSpeed = this.zSpeed;
        other.headingDegrees = this.headingDegrees;
    }

    // Duplicate the position parameters and possibly other ones
    // later if we need them
    public void copyPositionSpeedHeadingTo(Mobile other) {
        copyPositionTo(other);
        copySpeedAndHeadingTo(other);
    }

    public double getTargetSpeed() { return targetSpeed; }
    public void setTargetSpeed(double newSpeed) {
        targetSpeed = newSpeed;
        if (targetSpeed > maxSpeed)
            targetSpeed = maxSpeed;
        if (targetSpeed < 0)
            targetSpeed = 0;
    }

    public boolean isSeekingTarget() {
        return seekTarget;
    }

    public void stopSeekingTarget() {
        seekTarget = false;
    }

    public void startSeekingTarget() {
        seekTarget = true;
    }

    public void calculateNewHeading(double newTargetX, double newTargetY) {
        // Figure out where heading is based on current location vs. target location
        double angle = Math.toDegrees(Math.atan2(newTargetY - y(), newTargetX - x()));
        setHeadingDegrees(angle);
    }

    public boolean isMoving() {
        if (xSpeed == 0 && ySpeed == 0 && zSpeed == 0)
            return false;
        return true;
    }

    public void undoMove() {
        setPos(prevX, prevY);
        setElevation(prevZ);
    }

    public boolean move() { 
        if (xSpeed == 0 && ySpeed == 0 && zSpeed == 0) {
            return false;
        }

        prevX = x(); 
        prevY = y();
        prevZ = z();

        setPos(prevX + xSpeed, prevY + ySpeed);
        setElevation(prevZ + zSpeed);
        return true;
    }

    // Attempt to stabilize our horizontal movement so that we end up
    // staying still.
    public void stabilizeMovement() {
        // Must turn off the seeker so that we follow the regular heading.
        // This is fine; the seek behavior will just turn it back on when it
        // reactivates.
        stopSeekingTarget();
        
        // Literally, just flip the heading around
        // so that our drone accelerates in the opposite direction.
        setHeadingRadians(Math.atan2(-ySpeed, -xSpeed));
        accelerationRate = maxAccelerationRate / 10;
    }

    public void adjustSpeed() {
        // If we are braking, just do it.
        if (brake == true) {
            applyBrakes();
            brake = false;
            return;
        }

        // This is really only used as a helper function right now.
        if (stabilize == true) {
            stabilizeMovement();
        }

        // Really though we wouldnt just shut off the engines, we'd start
        // decelerating first.

        // Accelerate up or down depending on heading.  This is overly
        // simplified and should include some acceleration really.
        switch (getVerticalHeading()) {
            case NONE:
                //zSpeed = 0;
                if (zSpeed > 0) {
                    //zSpeed -= verticalAccelerationRate;
                    // Gravity accelerates you downwards.
                    zSpeed -= Distance.GRAVITY_PPF;
                    if (zSpeed < 0) {
                        zSpeed = 0;
                    }
                }
                else if (zSpeed < 0) {
                    zSpeed += verticalAccelerationRate;
                    if (zSpeed > 0) {
                        zSpeed = 0;
                    }
                }
                break;
            case UP:
                zSpeed += verticalAccelerationRate; // = (double)maxVerticalSpeed;
                if (zSpeed > maxAscentSpeed) {
                    zSpeed = maxAscentSpeed;
                }
                break;
            case DOWN:
                // Basically the idea here is, the drone will not let itself fall faster
                // than a certain rate.  So it lets gravity stop it from going higher
                // but as soon as its not going higher anymore, it cushions itself.
                if (zSpeed > 0) {
                    zSpeed -= Distance.GRAVITY_PPF;
                } else {
                	zSpeed -= verticalAccelerationRate;  
                }
                // Really max down speed is terminal velocity, but the drone wont let that
                // happen.  We just dont let ourselves fall all that fast because we would
                // have trouble lifting ourselves up against gravity with crappy acceleration.
                // double minSpeed = (maxVerticalSpeed / 2) * -1;

                double minSpeed = maxDescentSpeed * -1;
                if (zSpeed < minSpeed) {
                    zSpeed = minSpeed;
                }
                break;
        }

        // If we arent seeking a target, then we accelerate towards
        // our current heading
        if (this.isSeekingTarget() == false) {
            accelerateTowardsHeading();
            return;
        }
        accelerateTowardsTargetLocation();
    }

    public void setSpeedToMaxAlongHeading(double angleRadians) {
        // Ok so basically the hypoteneuse will be the max speed
        // And the angle is our heading
        //double Adjacent = Math.cos(angleInRadians) * Hypotenuse;
        //double Opposite = Math.sin(angleInRadians) * Hypotenuse;
        xSpeed = Math.cos(angleRadians) * maxSpeed;
        ySpeed = Math.sin(angleRadians) * maxSpeed;
        speed = maxSpeed;
    }

    public void setSpeedToMaxAlongCurrentHeading() {
        setSpeedToMaxAlongHeading(getHeadingRadians());
    }
    
    // So it takes the current speed and just increases it in the same direction
    public void accelerateByNoLimit(double accRate) {
        changeXSpeed(accRate * Math.cos(getHeadingRadians()));
        changeYSpeed(accRate * Math.sin(getHeadingRadians()));
        updateSpeedValue();
    }

    // So it takes the current speed and just increases it in the same direction
    public void accelerateBy(double accRate) {
        applyAcceleration(getHeadingRadians(), accRate);
    }

    private void applyAcceleration(double angleRadians, double accRate) {
        double xAcc = accRate * Math.cos(angleRadians); //Math.toRadians(angle));
        double yAcc = accRate * Math.sin(angleRadians); //Math.toRadians(angle));

       // double newSpeed = Physics.getSpeed(Math.abs(xSpeed + xAcc) + Math.abs(ySpeed + yAcc);
        double newSpeed = Physics.getSpeed(xSpeed + xAcc, ySpeed + yAcc);
        if (newSpeed > maxSpeed) {
            // Exceeding the speed limit!  We can only continue if this acceleration
            // is slowing us down
            //double oldSpeed = Physics.getSpeed(xSpeed, ySpeed);
            if (newSpeed >= this.getSpeed()) { //Physics.getSpeed(xSpeed, ySpeed)) {
                // We should max our speed here though.
                setSpeedToMaxAlongHeading(angleRadians);
                return;
            }
        }

        //this.xSpeed += xAcc;
        //this.ySpeed += yAcc;
        changeXSpeed(xAcc);
        changeYSpeed(yAcc);
        updateSpeedValue();
    }

    public void accelerateTowardsHeading() {
	    applyAcceleration(getHeadingRadians(), this.accelerationRate);
    }

    public boolean accelerateTowardsTargetLocation() {
        return accelerateTowardsLocation(targetX, targetY);
    }

    public boolean accelerateTowardsLocation(double x, double y) {
        // Based on target location calculate a new heading
        calculateNewHeading(x, y);

        // We cant just blindly accelerate towards the heading here because if we do
        // that, we might end up like circling around our target as the
        // velocity would not take into account the distance and ideal speed
        // to reach our goal.  We may not want to apply full acceleration towards
        // the heading because that may result in us circling the target and never
        // reaching it.

        // Update the direction, movement, etc., based on its parameters.

        // Basically take the current direction and velocity,
        // and apply an update to it based on the heading and acceleration value.
        // So if its moving northeast at 10 kmh and we want to move southwest,
        // We need to start trying to move the opposite direction, which starts
        // by applying our acceleration in the direction we want to go.
        double idealXSpeed = Physics.calcIdealXSpeed(this, x, y);
               // System.out.println("idealX " +idealX);
        double idealYSpeed = Physics.calcIdealYSpeed(this, x, y);

        // Now we have the ideal x and y velocities to reach our
        // destination.  Contrast these with our current velocities to
        // see how far we're off on each one then, allocate acceleration accordingly.
        double xDiff = idealXSpeed - xSpeed;
        double yDiff = idealYSpeed - ySpeed;
                //System.out.println("xDiff " +xDiff);
        double total = Math.abs(idealXSpeed) + Math.abs(idealYSpeed);
                //System.out.println("total " +total);
        if (total == 0) {
            return false;
        }

        double xAcc = accelerationRate * (xDiff / total);//drone.maxSpeed); 
        double yAcc = accelerationRate * (yDiff / total);//drone.maxSpeed);
	    //System.out.println("" + accelerationRate);

        //xSpeed += xAcc;//*Math.cos(Math.toRadians(drone.heading));
        //ySpeed += yAcc;//*Math.sin(Math.toRadians(drone.heading));  
        changeXSpeed(xAcc);
        changeYSpeed(yAcc);
        updateSpeedValue();
        return true;
    }

    public void damage(int amt) {
        // We dont need to do anything here for generic mobiles but we use
        // this as an abstract basically
    }
}