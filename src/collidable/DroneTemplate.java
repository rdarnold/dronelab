package dronelab.collidable;

import dronelab.utils.*;

public final class DroneTemplate {
    private DroneTemplate () { // private constructor
    }

    // This sets up drones correctly for various types
    // Might be better to just have like a big matrix or individual data sets
    // but this does give us more flexibility in how the drones are set up.
    public static void setDroneParams(Drone drone, Drone.DroneType type) {
        switch (type) {
            case DJI_PHANTOM_3:
                setupDJIPhantom3(drone);
                break;
            case DJI_PHANTOM_4:
                setupDJIPhantom4(drone);
                break;
            case DJI_INSPIRE_1:
                setupDJIInspire1(drone);
                break;
            case MAVIC_PRO:
                setupMavicPro(drone);
                break;
        }
        drone.rechargeBatteriesFull();
    }
    public static void setDroneParams(Drone drone, String droneTypeName) {
        Drone.DroneType droneType = Drone.DroneType.DJI_PHANTOM_4;

        if (droneTypeName == null || droneTypeName == "") {
            return;
        }

        if (droneTypeName.equals(Constants.STR_DJI_PHANTOM_3) == true) {
            droneType = Drone.DroneType.DJI_PHANTOM_3;
        } 
        else if (droneTypeName.equals(Constants.STR_DJI_PHANTOM_4) == true) {
            droneType = Drone.DroneType.DJI_PHANTOM_4;
        }
        else if (droneTypeName.equals(Constants.STR_DJI_INSPIRE_1) == true) {
            droneType = Drone.DroneType.DJI_INSPIRE_1;
        }
        else if (droneTypeName.equals(Constants.STR_MAVIC_PRO) == true) {
            droneType = Drone.DroneType.MAVIC_PRO;
        }

        setDroneParams(drone, droneType);
    }

    public static void setupDefault(Drone drone) {
        // 60 frames per second.
        //setMaxSpeed(Distance.pixelsPerSecondFromMPH(45) / 60.0);
        drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(20));
        drone.setMaxVerticalSpeed(drone.getMaxSpeed() / 3.0); // Typical vertical speed is a third of regular speed

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        drone.setMaxAccelerationRate(drone.getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());
    }

    public static void setupDJIPhantom3(Drone drone) {
        /*
            Max Descent Speed. 3 m/s. 
            Max Speed. 16 m/s (ATTI mode)
        */

        // 60 frames per second.
        //setMaxSpeed(Distance.pixelsPerSecondFromMPH(45) / 60.0);
        drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(16));
        drone.setMaxAscentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(5));
        drone.setMaxDescentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(3));

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        drone.setMaxAccelerationRate(drone.getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());

        drone.setBatteryLifeMinutes(28);
    }

    public static void setupDJIPhantom4(Drone drone) {
        /*
            Max Descent Speed 4 m/s (Sport mode) 
            Max Speed 20 m/s (Sport mode) 
            Max Service Ceiling Above Sea Level 19685 feet (6000 m) 
            Max Flight Time Approx. 28 minutes 

            Weight 3 lbs. 
            Max Ascent Speed 13.4 MPH 
            Max Descent Speed 8.9 MPH 
            Max Forward Speed 45 MPH 
            Max Ceiling 400 ft. (Electronically Limited) 
            Max Flight Time 28 min. 
            Operating Temp 32° to 104°F 
            Satellite Systems GPS & GLONASS 
            Obstacle Sensory Range 2 to 49 ft. 
            Gimbal Control Range -90° to +30° Pitch 
            Camera Sensor 1/2.3" 
            Lens FOV 94° 
            ISO Range 100-3200 
            Max Image Size 4000x3000 
            Max Video Size 4096x2160 
            Max Video Bitrate 60 Mbps 
            File Systems FAT32, exFAT 
            Photo Formats JPEG, DNG 
            Video Formats MP4, MOV, MPEG 
            Charger Specs 17.4v, 100w 
            Remote Frequency 2.400-2.483 GHz 
            Max Transmission Range 3.1 mi. 
            Battery Model Intelligent Flight PH4 
            Battery Specs 5,350 mAh, 15.2v 
        */

        drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(20));
        drone.setMaxAscentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(6));
        drone.setMaxDescentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(4));

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        drone.setMaxAccelerationRate(drone.getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());

        drone.setBatteryLifeMinutes(28);
    }

    public static void setupMavicPro(Drone drone) {
        /*
            Weight (Including Battery):
            1.62 lbs (734 g) (exclude gimbal cover)
            1.64 lbs (743 g)(include gimbal cover)

            Max Ascent Speed:
            16.4 ft/s (5 m/s) in Sport mode (Sport mode)

            Max Descent Speed:
            9.8 ft/s (3 m/s)

            Max Speed:
            40 mph (65 kph) in Sport mode without wind (Sport mode)

            Max Service Ceiling Above Sea Level:
            16404 feet (5000 m)

            Max Flight Time:
            27 minutes (0 wind at a consistent 15.5 mph (25 kph)) 

            Max Hovering Time:
            24 minutes (0 wind)

            Overall flight time:
            21 munites ( In normal flight, 15% remaining battery level )

            Max Flight Distance:
            8 mi 
        */

        drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(20));
        drone.setMaxAscentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(5));
        drone.setMaxDescentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(3));

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        drone.setMaxAccelerationRate(drone.getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());

        drone.setBatteryLifeMinutes(27);
    }

    public static void setupDJIInspire1(Drone drone) {
        /*
            DJI Inspire 1 maximum climb rate 5 m/s or 18 km/h and maximum horizontal speed 22 m/s 
            22 mins battery life
        */

        drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(22));
        drone.setMaxAscentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(5));
        drone.setMaxDescentSpeed(Distance.pixelsPerFrameFromMetersPerSecond(3));

        // Lets say generally we reach max speed in 2 seconds.
        // So it takes us 120 frames to reach max speed.
        // Thus our accelerate rate should be the maxSpeed per
        // frame / 120.
        drone.setMaxAccelerationRate(drone.getMaxSpeed() / 120.0);

        // I guess set vertical acceleration to the same rate?  I'm not sure what is standard here.
        drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());
        drone.setBatteryLifeMinutes(22);
    }
}