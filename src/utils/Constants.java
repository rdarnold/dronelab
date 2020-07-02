package dronelab.utils;

import java.util.EnumSet;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

// This is essentially like a static class in C#
public final class Constants {
    private Constants () { // private constructor
    }

    // Just a global counter to help us debug stuff.
    public static long debugCounter = 0;

    public static final String RES_LOAD_PATH = "/res/";
    public static final String SCENARIO_LOAD_PATH = "/scenario/";
    public static final String SCENARIO_SAVE_PATH = "scenario/";
    public static final String OUTPUT_SAVE_PATH = "output/";
    public static final String INPUT_LOAD_PATH = "/input/";
    public static final String DATA_SAVE_PATH = "output/data/";
    public static final String DATA_LOAD_PATH = "/output/data/";
    public static final String DATASET_SAVE_PATH = "output/data/datasets/";
    public static final String SEEN_DATA_SAVE_PATH = "output/data/seen/";
    public static final String CONFIG_LOAD_PATH = "/";
    public static final String CONFIG_SAVE_PATH = "";

    public static final String DATA_FILE_CAMERA = "matrix_output_camera.txt";
    public static final String DATA_FILE_FINDER = "matrix_output_FINDER.txt";

    public static final String RESTART_BATCH_FILE_NAME_PATH = "restart.bat";
    public static final String SCENARIO_CURRENT_FILE_NAME = "current_scenario";

    public static final int NUM_MATRIX_REPETITIONS = 10;

    //one second in nanoseconds
    public static final long ONE_SECOND_IN_NANOSECONDS = 1000000000;

    // Set this to true if we want to write out the timestamp we "see" every single
    // survivor to a file for each particular sim run.
    public static final boolean RECORD_ALL_TIMESTAMPS = true;

    // How long until drones considered stale?  Not long here since they are constantly
    // communicating.  No issue if they drop off the list and are re-added either.
    public static final long droneDataStaleThresholdMillis = 5 * 1000;

    // The Solo from 3DR has a Wi Fi HD range of half a mile.
    // We can use that as a guide here.
    public static final double MAX_WIFI_RANGE = 800.0;
    // ~150 ft. is like home wifi indoors
    public static final double MIN_WIFI_RANGE = 50.0; 

    public static final int MAX_FFW_RATE = 200;

    public static final String STR_AVOID = "Avoid";
    public static final String STR_SEEK = "Seek";
    public static final String STR_SEARCH = "Pattern";
    public static final String STR_WANDER = "Wander";
    public static final String STR_FORM = "Flock";
    public static final String STR_RECHARGE = "Recharge";
    public static final String STR_LAUNCH = "Launch";
    public static final String STR_MAINTAIN_HEIGHT = "Height";
    public static final String STR_CLIMB = "Climb";
    public static final String STR_SPIRAL = "Spiral";
    public static final String STR_SCATTER = "Scatter";
    public static final String STR_REPEL = "Repel";
    public static final String STR_RELAY = "Relay";
    public static final String STR_ANTI = "AntiSocial";

    public static final String[] STR_BEHAVIORS = { 
        STR_AVOID, STR_SEEK, STR_SEARCH, STR_WANDER, STR_FORM, STR_RECHARGE, STR_LAUNCH,
        STR_MAINTAIN_HEIGHT, STR_CLIMB, STR_SPIRAL, STR_SCATTER, STR_REPEL, STR_RELAY, STR_ANTI
    };

    public static final String STR_AVOID_J = "避ける";
    public static final String STR_SEEK_J = "シーク";
    public static final String STR_SEARCH_J = "パターン検索";
    public static final String STR_WANDER_J = "流離う";
    public static final String STR_FORM_J= "形成を維持する";
    public static final String STR_RECHARGE_J = "充電地";
    public static final String STR_LAUNCH_J = "打ち上げ";
    public static final String STR_MAINTAIN_HEIGHT_J = "高さを維持する";
    public static final String STR_CLIMB_J = "登る";
    public static final String STR_SPIRAL_J = "スパイラル検索";
    public static final String STR_SCATTER_J  = "Scatter";
    public static final String STR_REPEL_J  = "Repel";
    public static final String STR_RELAY_J = "Relay";
    public static final String STR_ANTI_J = "AntiSocial";

    public static final String[] STR_BEHAVIORS_J = { 
        STR_AVOID_J, STR_SEEK_J, STR_SEARCH_J, STR_WANDER_J, STR_FORM_J, STR_RECHARGE_J, 
        STR_LAUNCH_J, STR_MAINTAIN_HEIGHT_J, STR_CLIMB_J, STR_SPIRAL_J, STR_SCATTER_J, STR_REPEL_J, STR_RELAY_J, STR_ANTI_J
    };

    public static final String STR_SENSOR_SONAR = "Sonar";
    public static final String STR_SENSOR_CAMERA = "Camera";
    public static final String STR_SENSOR_LOCATION = "GPS";
    public static final String STR_SENSOR_FINDER = "FINDER";
    public static final String STR_SENSOR_FLIR = "FLIR";

    public static final String[] STR_SENSORS = { 
        STR_SENSOR_SONAR, STR_SENSOR_CAMERA, STR_SENSOR_LOCATION, STR_SENSOR_FINDER, STR_SENSOR_FLIR
    };

    public static final String STR_SENSOR_SONAR_J = "ソナー";
    public static final String STR_SENSOR_CAMERA_J = "カメラ";
    public static final String STR_SENSOR_LOCATION_J = "GPS";
    public static final String STR_SENSOR_FINDER_J = "FINDER";
    public static final String STR_SENSOR_FLIR_J = "FLIR";

    public static final String[] STR_SENSORS_J = { 
        STR_SENSOR_SONAR_J, STR_SENSOR_CAMERA_J, STR_SENSOR_LOCATION_J,  STR_SENSOR_FINDER_J, STR_SENSOR_FLIR_J
    };

    public static final String STR_DJI_PHANTOM_3 = "DJI Phantom 3";
    public static final String STR_DJI_PHANTOM_4 = "DJI Phantom 4";
    public static final String STR_DJI_INSPIRE_1 = "DJI Inspire 1";
    public static final String STR_MAVIC_PRO = "Mavic Pro";

    public static final String[] STR_DRONE_TYPES = { 
        STR_DJI_PHANTOM_3, STR_DJI_PHANTOM_4, STR_DJI_INSPIRE_1,  STR_MAVIC_PRO
    };
    
    public static final String STR_LANG_ENGLISH = "English";
    public static final String STR_LANG_JAPANESE = "日本語";
    public static final String STR_LANG_BOTH = "日本語 / English";

    // Various default sizes of things
    public static final int personWidth = 8;

    public static long nextId = 0;
    public static long getNextId() {
        long id = nextId;
        nextId++;
        return id;
    }

    public enum Language {
        ENGLISH, JAPANESE, BOTH
    }

    public enum ShapeType {
        CIRCLE, RECT, POLYGON
    }

    public enum BuildType {
        BUILDINGS, VICTIMS, DRONES, DEPLOYMENT
    }

    public enum BuildMode {
        BUILD, SELECT, DELETE
    }

    public enum BuildSubMode {
        DROP, ROTATE, RESIZE
    }

    public enum VerticalHeading {
        NONE, UP, DOWN
    }

    public enum CommType {
        WIFI(0);

        private final int value;
        private CommType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum DrawFlag {
        //MODULES, SENSORS, WIFI;
        NOT_DEFINED (1 << 0),
        MODULES     (1 << 1), 
        SENSORS     (1 << 2),
        WIFI        (1 << 3),
        VICTIMS     (1 << 4),
        COLLISIONS  (1 << 5),
        STRUCTURES  (1 << 6),
        DEPLOYMENTS (1 << 7);

        private final long value;
        private DrawFlag(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        } 
        public static final EnumSet<DrawFlag> ALL = EnumSet.allOf(DrawFlag.class);
        public static final EnumSet<DrawFlag> NONE = EnumSet.noneOf(DrawFlag.class);
    }
}