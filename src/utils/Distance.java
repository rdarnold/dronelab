package dronelab.utils;

import java.util.ArrayList;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;

// This is essentially like a static class in C#
public final class Distance {
    private Distance () { // private constructor
    }

    private static final double METERS_TO_FEET = 3.28084;
    private static final double FEET_TO_METERS = 0.3048;

    private static final double SECONDS_PER_HOUR = 3600;
    private static final double FEET_PER_MILE = 5280;
    private static final double METERS_PER_MILE = 1609.34;

    public static final double GRAVITY_MPS = 9.8; // Meters per second
    public static double GRAVITY_PPS = 0; // Pixels per second
    public static double GRAVITY_PPF = 0; // Pixels per frame

    public static double WIDTH_IN_PIXELS = 6500.0;

    // The size of the entire Sendai map is almost exactly 2000 meters wide.
    // In the Kobe photo, 1215 pixels is one km
    public static void setGlobalScale(double mapWidthMeters, double mapWidthPixels) {
        WIDTH_IN_PIXELS = mapWidthPixels;
        WIDTH_IN_METERS = mapWidthMeters;
        WIDTH_IN_FEET = WIDTH_IN_METERS * METERS_TO_FEET;
        METERS_PER_PIXEL = WIDTH_IN_METERS / mapWidthPixels;
        // Set gravity too
        GRAVITY_PPS = pixelsFromMeters(GRAVITY_MPS);
        GRAVITY_PPF = GRAVITY_PPS / 60.0;
    }

    public static double WIDTH_IN_METERS = 2000;
    public static double WIDTH_IN_FEET = WIDTH_IN_METERS * METERS_TO_FEET;

    // This is our standard scale
    public static double METERS_PER_PIXEL = ((2500.0 / WIDTH_IN_METERS) * (2500.0 / WIDTH_IN_PIXELS));


    public static double feetFromPixels(double pixels) {
        return (metersFromPixels(pixels) * METERS_TO_FEET);
    }

    public static double pixelsFromMeters(double meters) {
        return (meters / METERS_PER_PIXEL);
    }

    public static double metersFromPixels(double pixels) {
        return (pixels * METERS_PER_PIXEL);
    }

    public static double metersFromFeet(double feet) {
        return (feet * FEET_TO_METERS);
    }

    public static double feetFromMeters(double meters) {
        return (meters * METERS_TO_FEET);
    }

    public static double feetPerSecondFromMPH(double mph) {
        return ((mph * FEET_PER_MILE) / SECONDS_PER_HOUR);
    }

    public static double metersPerSecondFromMPH(double mph) {
        return ((mph * METERS_PER_MILE) / SECONDS_PER_HOUR);
    }

    public static double MPHFromMetersPerSecond(double mps) {
        return ((mps / METERS_PER_MILE) / SECONDS_PER_HOUR);
    }

    public static double pixelsPerSecondFromMPH(double mph) {
        return (pixelsFromMeters(metersPerSecondFromMPH(mph)));
    }

    public static double pixelsPerFrameFromMPH(double mph) {
        return (pixelsPerSecondFromMPH(mph) / 60.0);
    }

    public static double pixelsPerFrameFromMetersPerSecond(double mps) {
        return (pixelsFromMeters(mps) / 60.0);
    }

    public static double metersPerSecondFromPixelsPerFrame(double ppf) {
        return ((ppf * 60.0) * METERS_PER_PIXEL);
    }

    public static double accSecondsFromPixelsPerFrame(double ppf, double maxSpeed) {
        // So, how many seconds does it take to get up to maxSpeed using ppf as acc.
        return ((maxSpeed / ppf) / 60.0);
    }

    public static double accPixelsPerFrameFromSeconds(double seconds, double maxSpeed) {
        return (maxSpeed / (seconds * 60.0));
    }
}