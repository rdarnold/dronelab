package dronelab._3d;

import java.util.ArrayList;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.transform.Scale;
import javafx.geometry.Point3D;
import javafx.util.Callback;
//import com.javafx.experiments.jfx3dviewer;

import dronelab.utils.*;
import dronelab.collidable.*;

public class CameraGroup extends Group {

        public enum Dir {
            NORTH, NE, EAST, SE, SOUTH, SW, WEST, NW
        }

        private double yawDegrees = 0;
        private double pitchDegrees = 0;
        private double rollDegrees = 0;

        private Point3D ydir = new Point3D(0, 0, 1);

        double xPos = 0;
        double yPos = 0;
        double zPos = 0;

        double trackDistance = 100;
        double trackHeight = -40;
        double trackAngle = 0;
        double targetTrackAngle = 0;

        boolean locked = true;

        public Affine a = new Affine();
        public CameraGroup() { 
            super(); 
            getTransforms().addAll(a); 
        }

        public double getYaw() {
            double val = 0;
            Transform T = getLocalToSceneTransform();
            //val = Math.atan2(T.getMzx(),Math.sqrt(T.getMzy()*T.getMzy()+T.getMzz()*T.getMzz()));
            val = Math.asin(T.getMxz());
            return val;
        }
        public double getPitch() {
            double val = 0;
            Transform T = getLocalToSceneTransform();
            val = Math.atan2(-T.getMzy(),T.getMzz());
            return val;
        }
        public double getRoll() {
            double val = 0;
            Transform T = getLocalToSceneTransform();
            val = Math.atan2(-T.getMyx(),T.getMxx());
            return val;
        }
        public double getYawDegrees() { return yawDegrees; }
        public double getPitchDegrees() { return pitchDegrees; }
        public double getRollDegrees() { return rollDegrees; }
        public double getYawRadians() { return Math.toRadians(yawDegrees); }
        public double getPitchRadians() { return Math.toRadians(pitchDegrees); }
        public double getRollRadians() { return Math.toRadians(rollDegrees); }

        /*public void changeRollBy(double amount) {
            rollDegrees += amount;
            rollDegrees = Utils.normalizeAngle(rollDegrees);
            
            rollDegrees = Utils.normalizeAngle(amount + Math.toDegrees(getRoll()));
            updateRollPitchYaw();
            //GraphicsHelper.applyRoll(this, getRollRadians());
        }

        public void changePitchBy(double amount) {
            pitchDegrees += amount;
            pitchDegrees = Utils.normalizeAngle(pitchDegrees);
            updateRollPitchYaw();
            //GraphicsHelper.applyPitch(this, amount);
        }

        public void changeYawBy(double amount) {
            yawDegrees += amount;
            yawDegrees = Utils.normalizeAngle(yawDegrees);
           
            rx.pivotXProperty().set(1000);
            rx.pivotYProperty().set(2500);
            rx.pivotZProperty().set(-100);

            rx.setAngle(yawDegrees);
            //updateRollPitchYaw();
            //GraphicsHelper.applyYaw(this, getYawRadians());
        }

        // In 3d you need to rotate them together mathematically
        public void changePitchYaw(double pitchDelta, double yawDelta) {
            changePitchBy(pitchDelta);
            changeYawBy(yawDelta);
            //pitchDegrees += pitchDelta;
            //yawDegrees += yawDelta;
            //pitchDegrees = Utils.normalizeAngle(pitchDegrees);
            //yawDegrees = Utils.normalizeAngle(yawDegrees);
            //updateRollPitchYaw();
        }

        // Do all three at once if necessary
        public void changeRollPitchYaw(double rd, double pd, double yd) {
            rollDegrees += rd;
            pitchDegrees += pd;
            yawDegrees += yd;
            rollDegrees = Utils.normalizeAngle(rollDegrees);
            pitchDegrees = Utils.normalizeAngle(pitchDegrees);
            yawDegrees = Utils.normalizeAngle(yawDegrees);
            
            //rollDegrees = Utils.normalizeAngle(rd + Math.toDegrees(getRoll()));
            //pitchDegrees = Utils.normalizeAngle(pd + Math.toDegrees(getPitch()));

            updateRollPitchYaw();
        }

        // These always need to be done together in 3d space
        public void updateRollPitchYaw() {
            GraphicsHelper.matrixRotate(this, getRollRadians(), getPitchRadians(), getYawRadians());
            
            //rz.setAngle(rollDegrees);
            //rx.setAngle(pitchDegrees);
            //ry.setAngle(yawDegrees);
        }

        public void moveForwardBy(double val) {

            // Actually to do this right shouldnt be TOO bad,
            // I can take my current angles on the axes, and
            // figure out how to advance 1 within that space.
            double y = getYawRadians(); // Yaw angle
            double p = getPitchRadians(); // Pitch angle
            double k = val; // Move distance

            double xzLength = Math.cos(p) * k;
            double dz = xzLength * Math.cos(y);
            double dx = xzLength * Math.sin(y);
            double dy = k * Math.sin(p);

            // Update your camera
            //camera.x += dx;
            //camera.y += dy;
            //camera.z += dz;
            setTranslateX(getTranslateX() + dx);
            setTranslateY(getTranslateY() + dy);
            setTranslateZ(getTranslateZ() + dz);
        }*/

        public void moveBy(double x, double y, double z) {
            xPos += x;
            yPos += y;
            zPos += z;
        }

        public void setPos(double x, double y, double z) {
            xPos = x;
            yPos = y;
            zPos = z;
        }   

        public boolean lockTarget(boolean lock) {
            locked = lock;
            return locked;
        }

        public boolean toggleLockTarget() {
            if (locked == true) {
                locked = false;
            }
            else {
                locked = true;
            }
            return locked;
        }

        public void updateTrackHeight(double change) {
            trackHeight += change;
        }

        public void updateTrackDistance(double change) {
            trackDistance += change;
        }

        public void updateTrackAngle(double change) {
            targetTrackAngle += change;
            targetTrackAngle = Utils.normalizeAngle(targetTrackAngle);
        }

        public void track(Mobile mob) {
            if (mob == null)
                return;
            if (locked == true) {
                //targetTrackAngle = mob.getHeadingDegrees();
                // Actually the angle should be the direction that the mobile is actually moving,
                // not the angle it wants to move in.
                targetTrackAngle = Physics.getAngleDegrees(mob.getXSpeed(), mob.getYSpeed());
            }
            TransitionTowardsTargetAngle();
            
            // It's merely the tracking angle, with the distance as the hyp
            double angleRadians = Math.toRadians(trackAngle);
            double xOff = Math.cos(angleRadians) * trackDistance;
            double yOff = Math.sin(angleRadians) * trackDistance;
            
            //Point2D point = new Point2D(originX + rotX, originY + rotY);

            setPos(mob.getX() - xOff, mob.getY() - yOff, trackHeight - mob.getElevation());
            lookAt(mob);
        }

        public void lookAt(Collidable col) {
            if (col == null)
                return;
            Point3D lookPoint = new Point3D(col.getX(), col.getY(), col.getElevation() * -1);
            Point3D origin = new Point3D(xPos, yPos, zPos);
            GraphicsHelper.lookAt(this, origin, lookPoint, ydir);
        }

        public void TransitionTowardsTargetAngle() {
            // If we are not at our target angle, move towards it
            // First which direction is quicker?
            if (targetTrackAngle == trackAngle) {
                return;
            }

            double d = targetTrackAngle - trackAngle;
            if (d > 180) {
                d -= 360;
            } else if (d<-180) {
                d += 360;
            }

            boolean lessThan = false;
            if (trackAngle < targetTrackAngle) {
                lessThan = true; 
            }

            // Rotate based on how far we are.
            trackAngle += d/10;

            // If we overshot, set it to equal.
            if (trackAngle > targetTrackAngle && lessThan == true) {
                trackAngle = targetTrackAngle; 
            } else if  (trackAngle < targetTrackAngle && lessThan == false) {
                trackAngle = targetTrackAngle; 
            }
            
            trackAngle = Utils.normalizeAngle(trackAngle);

            /*if (targetTrackAngle - trackAngle > 180) {

            }
            if (targetTrackAngle > trackAngle) {
                trackAngle += 1;
                trackAngle = Utils.normalizeAngle(trackAngle);
            } else if (targetTrackAngle < trackAngle) {
                trackAngle -= 1;
                trackAngle = Utils.normalizeAngle(trackAngle);
            }  */ 
        }

        public void updatePivot() {
            Rotate rot;

            /*rot = rx;
            rot.pivotXProperty().set(getTranslateX());
            rot.pivotYProperty().set(getTranslateY());
            rot.pivotZProperty().set(getTranslateZ());*/
        }
    }