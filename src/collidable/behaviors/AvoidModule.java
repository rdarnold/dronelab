package dronelab.collidable.behaviors;

import java.util.ArrayList;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;

import dronelab.utils.*;
import dronelab.collidable.*;

public class AvoidModule extends BehaviorModule {
    private int avoiding = 0;
    private double avAngle = 0;
    private double avAngleDeflection = 20;
    private Point2D startAvoidPoint;
    private long startAvoidTime = 0;

    // We keep a fake drone around for collision testing and such.
    private Drone fakeDrone = null;

    public AvoidModule() {
        super(Constants.STR_AVOID, Constants.STR_AVOID_J);
        drawLetter = "A";
        startAvoidTime = System.currentTimeMillis();
        fakeDrone = new Drone(true);
    }

    @Override
    public boolean setup() {
        if (drone == null) {
            return false;
        }
        setupCollisionDetection();
        return true;
    }

    @Override
    public void assign(Drone d) {
        super.assign(d);
        setupCollisionDetection();
    }
    
    @Override
    public boolean reset() { return true; }

    @Override
    public boolean usesDrawLocation() { return false; }

    @Override
    public boolean draw(GraphicsContext gc) {
        /*if (fakeDrone == null)
            return false;
        if (avoiding == 0) {
            drone.copyPositionSpeedHeadingTo(fakeDrone);
            //int frames = 3; //= (int)(drone.getSpeed() / drone.getAccelerationRate());
            fixSpeed(fakeDrone);
            for (int i = 0; i < avoidFrames; i++) {
                // Move it a few times and see
                fakeDrone.move();
                fakeDrone.draw(gc);
            }
        }*/
        return true;
    }

    @Override
    public boolean receive(String msg) { 
        return true;
    }

    // So how does this module react to circumstances, basically.
    @Override
    public boolean react() {
        if (drone == null) {
            return false;
        }
        
        if (avoiding > 0) {
	        avoid();
            return true;
        } 
        // We have the sensor picture, now what do we want to do based on
        // what we are sensing?  Basically, based on our speed and direction,
        // if we think we are going to collide, we need to slow the hell down,
        // back up, and or move upwards.  Just something simple to start
        // with.

        // Problem is, at this point sonar would literally be telling you how
        // close you were.  How do we simulate that here?  What we'll do is kind
        // of weird and may seem unnecessary from a software perspective but it'll 
        // be more realistic.  We will take the sensor data and check for a collision
        // on that data if we continue at our same trajectory.  Its basically what you
        // would do in actual drone software I would think, and the good thing is that
        // because we are using the sensor picture, we can mess with the sensors in all
        // kinds of ways to actually simulate how that affects things.

        if (detectPotentialCollision() == false) {
            return false;
        }

        // Turn off the drone's seeker and focus on avoid patterns
        drone.stopSeekingTarget();

        // If we are going really really slow / stopped, try to move away.
        // Like if we are going less than our acceleration rate, we are going mad slow.
        if (Utils.withinTolerance(drone.getSpeed(), 0.0, drone.getMaxAccelerationRate()) == false) {
            // Hit the brakes.
            drone.brake = true;
            return true;
        }

        // So every few seconds, we reset this, and check if we are within
        // a certain distance of the point when we last checked.  Its fairly simple,
        // it just says, if we are avoiding collisions right now, and we are close to
        // where we were avoiding collisions X seconds ago, change angle.  Doesnt matter
        // what happened in between.

        // But we can only do this if we have a location sensor since we need the location of
        // the drone for this.
        if (drone.ls != null) {
            long now = System.currentTimeMillis();
            if (now - startAvoidTime > 12000) {
                if (startAvoidPoint != null && 
                        Physics.withinDistance(drone.ls.x(), startAvoidPoint.getX(), 
                                               drone.ls.y(), startAvoidPoint.getY(), 20)) {
                    adjustDeflection(); 
                }
                startAvoidTime = now;
                startAvoidPoint = new Point2D(drone.ls.x(), drone.ls.y());
            }
        }
	    avoid();
        return true;
    }

    private void setupCollisionDetection() {
        // At this point all we need for collision detection is a correctly
        // proportioned fake drone.
        drone.copySizeTo(fakeDrone);
    }

    private int avoidFrames = 3;
    private boolean testFakeDroneCollision(double headingDegrees) {
        // So take our current position and say, ok, if we continue at this speed
        // for like one second, will we collide.
        fakeDrone.setHeadingDegrees(headingDegrees);
        fixSpeed(fakeDrone);

        // Actually with the combination of behaviors, 3 frames is enough, but if we only
        // have collision avoidance without other behaviors like climb or maintain height,
        // our "cooperative intelligence" is not good enough and we will hit things.
        // So "collision avoidance" as a goal is really a systemic result of 
        // a combination of several behaviors. Systems ftw.
        //int frames = 4; //= (int)(drone.getSpeed() / drone.getAccelerationRate());

        // It really should say, ok, if we wanted to stop, going at this speed, at this
        // acceleration rate (because we will speed up) based on this max speed, with this
        // ability to accelerate in reverse, how many pixels can we actually move?  And
        // that is the measure it should use here.

        // At the very least we'll keep it simple and just base it on speed and acceleration.
        double val = drone.getSpeed() / drone.getMaxAccelerationRate();
        avoidFrames = (int)Math.ceil(val / ((double)fakeDrone.wid));
        if (avoidFrames < 1) {
            avoidFrames = 1;
        }
        if (avoidFrames > 4) {
            avoidFrames = 4;
        }
        for (int i = 0; i < avoidFrames; i++) {
            // Move it a few times and see
            fakeDrone.move();
            if (Physics.checkCollisions(fakeDrone, drone.ss.getSensorPictureObstacles()) == true) {
                return true;
            }
        }
        return false;
    }
    
    private void setAvoidAngleRadians() {
        avAngle = drone.getHeadingRadians() + Math.toRadians(180);

        // We originally did the below because we werent braking first, so
        // we needed to push against our current speed.  Now we brake to a stop first
        // so we dont want to look at the speed, we might end up countering our own behavior.
        /*if (drone.getYSpeed() == 0 && drone.getXSpeed() == 0) {
            avAngle = drone.getHeadingRadians() + Math.toRadians(180);
        } else {
            avAngle = Math.atan2(-drone.getYSpeed(), -drone.getXSpeed());
        }*/
        // Just add 20 degrees so we have some variation, this should not be so random though,
        // because this can cause a crash if it makes us go 20 degrees towards another thing.
        avAngle += Math.toRadians(avAngleDeflection); 
    }

    // Apply acceleration at some angle until collision
    // is no longer imminent
    private void avoid() {
        // Quite simple, just literally accelerate full throttle in the
        // opposite direction you are currently going, because we have
        // just said that you are going to hit something if you keep
        // going this way.   
        if (avoiding == 0) {
            // We dont climb in the vaoid module; thats because climbing
            // is taken care of by other behaviors.

            // Base this on our acceleration rate, to make sure we get
            // far enough away.
            // at 1.0 acceleration, 3 is generally decent for this.
            avoiding = (int)(3.0 / drone.getMaxAccelerationRate());
            //avoiding = (int)(drone.getSpeed() / drone.getAccelerationRate());
            if (avoiding < 3) {
                avoiding = 3; // Never go less than 3
            }
            if (avoiding > 30) {
                avoiding = 30; // Limit the upper bound.
            }

            setAvoidAngleRadians();
            drone.setHeadingRadians(avAngle);
            drone.setAccelerationToMax();

            // This is a good idea but doesnt work.  The idea is to look for an angle
            // that doesnt have a collision.  The problem is that if we give ourselves too
            // much time to kick away without detecting again, we bump into stuff.  But
            // if we dont give ourselves enough time to kick away, we dont really move
            // anywhere.
            /*boolean found = false;
            drone.copyPositionTo(fakeDrone); //AndSpeedTo(fakeDrone);
            double newAngle = avAngle;
            for (int i = 0; i < 361; i+=10) {
                newAngle += 5;
                fakeDrone.setHeadingDegrees(avAngle);
                if (testFakeDroneCollision(drone.getHeadingDegrees()) == false) {
                    // If we find a good angle, use it. Otherwise just stick
                    // with our default.
                    avAngle = newAngle;
                    break;
                }
            }*/

        }
        // No we shouldnt apply acceleration for the drone, let it do its thing.
	    //drone.applyAcceleration(avAngle);

        // But this must be maintained for at least a few ticks so we can get out of the way
        // and try to seek again
        avoiding--;
        if (avoiding < 0) {
            avoiding = 0;
        }
    }

    private void fixSpeed(Drone fakeDrone) {
        // This should be adjusted in a better way.  With a fast moving drone
        // this will cause some odd behavior where it will try to avoid collisions long
        // before necessary.  Though those speeds are unrealistic for drones actually.
        // They'd have be going like 300 mph.
        fakeDrone.stopMoving();
        fakeDrone.accelerateByNoLimit(fakeDrone.wid); 
    }

    private boolean detectPotentialCollision() {
        boolean bColliding = false;
        // So look at the sensor picture from the sonar data.  This should
        // actually be a fused picture from multiple sensors but for now it's
        // just the one.
        if (drone.ss == null) {
            return false;
        }
        ArrayList<Obstacle> sensorObstacles = drone.ss.getSensorPictureObstacles();
        if (sensorObstacles == null || sensorObstacles.size() == 0)
            return false;

        //Shape pic = drone.ss.sensorPicture;
        // We detect nothing so no reaction from this module
        //if (pic == null)
            //return false;

        // If we are not moving, lets see if we're just too close to anything

        // This should depend on our speed.
        // The slower our acceleration relative to our speed,
        // The further back we need to look.  Speed / acceleration
        // gives us a decent approximation.
        //int frames = 4; //= (int)(drone.getSpeed() / drone.getAccelerationRate());
        //fixSpeed(fakeDrone);
        drone.copyPositionTo(fakeDrone); //AndSpeedTo(fakeDrone);
        bColliding = testFakeDroneCollision(drone.getHeadingDegrees());

        // If we are not moving, let's just see if we are flat out too
        // close to something else.  This prevents us from getting into a "stopped"
        // situation which is not realistic in real life but can happen in the sim.
        // Also check if we are going just way too slow.
        if (bColliding == false && drone.isMoving() == false) {
	        //System.out.println("Zoids");
            fakeDrone.changeSizeBy(5);
            //det = Physics.checkIntersection(fakeDrone, pic);
            if (Physics.checkCollisions(fakeDrone, sensorObstacles) == true) {
                bColliding = true;
            }
            fakeDrone.changeSizeBy(-5);
        }

        return bColliding;
    }

    private void adjustDeflection() {
        if (avAngleDeflection == 20)
            avAngleDeflection = -20;
        else
            avAngleDeflection = 20;
        // Choose a different direction to try
        // to fly for 5 seconds, completely at random.
        // If we haven't gotten far enough away.
        //avAngleDeflection = (double)rand.nextInt(90) - 45; // So a spread of 90 degrees on each side
    }
}