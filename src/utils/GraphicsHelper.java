package dronelab.utils;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.shape.*;
import javafx.geometry.Point3D;
import javafx.scene.transform.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Affine; 
import java.io.InputStream;

import dronelab._3d.*;

public final class GraphicsHelper {
    private GraphicsHelper () { // private constructor
    }

    public static Image batteryDead = null;
    public static Image wallTexture1 = null;
    public static Image wallTexture2 = null;
    public static Image wallTexture3 = null;
    public static Image steelTexture1 = null;
    public static Image bldgTexture1 = null;
    public static Image houseTexture1 = null;
    //public static Image droneBroken = null;

    private static double upCounter = 0;
    private static double upCounterMax = 100;
    private static double upCounterMin = 0;

    private static double flashCounter = 0;
    private static double flashMax = 100;
    private static double flashMin = 20;
    private static double flashMultiplier = (250.0 / flashMax);
    private static boolean countingUp = false;
    
    public static void init() {
        batteryDead = loadImage("battery_dead.png", 20, 20); 
        wallTexture1 = loadImage("wall_tex_1.jpg"); 
        wallTexture2 = loadImage("wall_tex_2.jpg"); 
        wallTexture3 = loadImage("wall_tex_3.jpg"); 
        steelTexture1 = loadImage("steeltex1.jpg"); 
        bldgTexture1 = loadImage("bldgtex1.jpg"); 
        houseTexture1 = loadImage("housetex1.jpg");
    }
    
    public static Image loadImage(String imageName) {
        return loadImage(imageName, -1, -1, false, false);
    }

    public static Image loadImage(String imageName, int wid, int hgt) {
        return loadImage(imageName, wid, hgt, false, false);
    }

    public static Image loadImage(String imageName, int wid, int hgt, boolean preserveRatio, boolean smooth) {
        String fileName = Constants.RES_LOAD_PATH + imageName;

        // This works both with and without a jar
        InputStream input = GraphicsHelper.class.getResourceAsStream(fileName);
        if (input != null) {
            if (wid == -1 || hgt == -1) {
                return new Image(input);
            }
            return new Image(input, wid, hgt, preserveRatio, smooth);
        }
        /*else if (imgError != null) {
            return imgError;
        }*/
        return null;
    }

    public static int getUpCounter() {
        return (int)upCounter;
    }

    public static int getFlashCounter() {
        return (int)flashCounter;
    }

    public static int getFlashColorLevel() {
        return (int)(flashMultiplier * flashCounter);
    }

    public static void updateDrawCounters() {
        updateFlashCounter();
        updateUpCounter();
    }

    private static void updateUpCounter() {
        upCounter++;

        if (upCounter > upCounterMax) {
            upCounter = upCounterMin;
            countingUp = false;
        } 
    }

    private static void updateFlashCounter() {
        if (countingUp == true)
            flashCounter++;
        else
            flashCounter--;

        if (countingUp == true && flashCounter > flashMax) {
            flashCounter = flashMax;
            countingUp = false;
        } else if (countingUp == false && flashCounter < flashMin) {
            flashCounter = flashMin;
            countingUp = true;
        }
        //return Color.rgb(flashCounter * 5, 0, 0); //flashCounter * 5);
    }

    public static void drawCircle(GraphicsContext gc, Circle circle, Color color) {
	    gc.setFill(color);
        double wid = circle.getRadius() * 2;
        double hgt = circle.getRadius() * 2;
        double drawX = circle.getCenterX() - wid/2;
        double drawY = circle.getCenterY() - hgt/2;
        gc.fillOval(drawX, drawY, wid, hgt);
    }

    // Wow, this does, in fact, seem to work.  But we cant have a translate XYZ set.
    public static Affine lookAt(Point3D from, Point3D to, Point3D ydir) { 
        Point3D zVec = to.subtract(from).normalize(); 
        Point3D xVec = ydir.normalize().crossProduct(zVec).normalize(); 
        Point3D yVec = zVec.crossProduct(xVec).normalize(); 
        return new Affine(xVec.getX(), yVec.getX(), zVec.getX(), from.getX(), 
                        xVec.getY(), yVec.getY(), zVec.getY(), from.getY(), 
                        xVec.getZ(), yVec.getZ(), zVec.getZ(), from.getZ()); 
    } 

    public static void lookAt(CameraGroup cam, Point3D from, Point3D to, Point3D ydir) { 
        Point3D zVec = to.subtract(from).normalize(); 
        Point3D xVec = ydir.normalize().crossProduct(zVec).normalize(); 
        Point3D yVec = zVec.crossProduct(xVec).normalize(); 

       // cam.getTransforms().clear();

        Affine aff = cam.a;
        aff.setMxx(xVec.getX());
        aff.setMxy(yVec.getX());
        aff.setMxz(zVec.getX());
        aff.setTx(from.getX());

        aff.setMyx(xVec.getY());
        aff.setMyy(yVec.getY());
        aff.setMyz(zVec.getY());
        aff.setTy(from.getY());

        aff.setMzx(xVec.getZ());
        aff.setMzy(yVec.getZ());
        aff.setMzz(zVec.getZ());
        aff.setTz(from.getZ());

       // cam.getTransforms().addAll(cam.rx, cam.a);
        /*Affine(double mxx, double mxy, double mxz, double tx, 
        double myx, double myy, double myz, double ty, 
        double mzx, double mzy, double mzz, double tz) 
        return new Affine(xVec.getX(), yVec.getX(), zVec.getX(), from.getX(), 
                        xVec.getY(), yVec.getY(), zVec.getY(), from.getY(), 
                        xVec.getZ(), yVec.getZ(), zVec.getZ(), from.getZ()); */
    } 


    // This is actually not correct, it only updates along axes, its not truly
    // roll pitch and yaw. Its rotate across xyz axes.
    /*public static void matrixRotate(CameraGroup n, double roll, double pitch, double yaw) {
        // So I have my roll pitch and yaw relative to the direction I'm facing.
        // we need to convert that into roll  as z, pitch as x, and yaw as y.
        double z = roll;
        double x = pitch;
        double y = yaw;

        double A11 = Math.cos(z) * Math.cos(y);
        double A12 = Math.cos(x) * Math.sin(z)+Math.cos(z)*Math.sin(x)*Math.sin(y);
        double A13 = Math.sin(z) * Math.sin(x)-Math.cos(z)*Math.cos(x)*Math.sin(y);
        double A21 = -Math.cos(y) * Math.sin(z);
        double A22 = Math.cos(z) * Math.cos(x)-Math.sin(z)*Math.sin(x)*Math.sin(y);
        double A23 = Math.cos(z) * Math.sin(x)+Math.cos(x)*Math.sin(z)*Math.sin(y);
        double A31 = Math.sin(y);
        double A32 = -Math.cos(y) * Math.sin(x);
        double A33 = Math.cos(x) * Math.cos(y);

        double d = Math.acos((A11+A22+A33-1d)/2d);
        if (d != 0d) {
            double den = 2d * Math.sin(d);
            Point3D p = new Point3D((A32-A23)/den,(A13-A31)/den,(A21-A12)/den);
            Rotate r = n.r;
            r.setAxis(p);    
            r.setAngle(Math.toDegrees(d));            
        }
    }*/
}