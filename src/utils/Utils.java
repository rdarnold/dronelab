package dronelab.utils;

import java.util.Random;
import java.util.ArrayList;
//import javafx.scene.shape.*;
import java.io.*;
import java.nio.file.*;
import javax.json.JsonValue;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.stream.JsonParser;

// This is essentially like a static class in C#
public final class Utils {
    public static Random rand;

    private Utils() { // private constructor
    }

    public static void init() {
        rand = new Random();
    }

    // Integer between min and max, inclusive of both.
    // Like old style C.
    public static int number(int min, int max) {
        return (min + rand.nextInt(((max + 1) - min)));
    }

    public static void print() {
        System.out.println(" ");
    }

    public static void print(String str) {
        System.out.println("" + str);
    }

    public static void print(int num) {
        print("" + num);
    }

    public static void print(double num) {
        print("" + num);
    }

    public static void log() {
        System.out.println("LOG");
    }

    public static void log(String str) {
        System.out.println("LOG: " + str);
    }

    public static void log(int num) {
        log("" + num);
    }

    public static void log(double num) {
        log("" + num);
    }

    public static void err() {
        System.out.println("ERROR");
    }

    public static void err(String str) {
        System.out.println("ERR: " + str);
    }

    public static void err(int num) {
        err("" + num);
    }

    public static void err(double num) {
        err("" + num);
    }

    public static String removeFileExtension(String s) {
        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex == -1)
            return filename;

        return filename.substring(0, extensionIndex);
    }

    public static boolean stringStartsWith(String str, String strStartsWith) {
        if (str == null || strStartsWith == null || str.equals("") == true || strStartsWith.equals("") == true) {
            return false;
        }
        if (str.length() >= strStartsWith.length() && str.substring(0, strStartsWith.length()).equals(strStartsWith)) {
            return true;
        }
        return false;
    }

    public static String readFile(String fileName) {
        try {
            // Man, Java sucks here.  We can't check Files.exists using the same
            // path as we use to read the file !!  we have to actually strip off
            // the first slash or it will give false negative...
            String existsPath = fileName.substring(1, fileName.length());
            if (Files.exists(Paths.get(existsPath)) == false) {
                Utils.log("readFile: " + existsPath + " not found");
                return "";
            }
            //BufferedReader reader = new BufferedReader(new FileReader (file));
            // This way works both inside and outside of a jar file.  Using a File as
            // above does not.
            Utils.log("readFile: " + fileName);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(Utils.class.getResourceAsStream(fileName)));
            String         line = null;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            try {
                while((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }

                return stringBuilder.toString();
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            Utils.err("Failed to read file " + fileName);
            err();
            e.printStackTrace();
            return null;
        }
    }

    /*
    public static String readFile(String path) 
    {
        String contents = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            contents = new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Utils.err("Failed to read file " + path);
            err();
            e.printStackTrace();
            return contents;
        }
	    return contents;
    }*/

    public static boolean writeFile(String str, String fileName) {
        // Make the folder if we dont have it.
        Path pathToFile = Paths.get(fileName);
        if (pathToFile.toString().contains("/") || pathToFile.toString().contains("\\")) {
            try {
                Files.createDirectories(pathToFile.getParent());
            }
            catch (IOException e) {
                err("Could not create folder " + pathToFile.getParent().toString());
                e.printStackTrace();
                return false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            //log(str);
	        writer.write(str);
        } catch (IOException e) {
            err("Failed to write file " + fileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean appendFile(String str, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
	        writer.write(str);
        } catch (IOException e) {
            err("Failed to append to file " + fileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean withinTolerance(double n1, double n2, double tol) {
        if (Math.abs(n1 - n2) < tol) {
            return true;
        }
        return false;
    }

    public static int clampColor(int num) {
        return clamp(num, 0, 255);
    }
    
    public static int clamp(int num, int min, int max) {
        if (num < min) return min;
        if (num > max) return max;
        return num;
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) return min;
        if (num > max) return max;
        return num;
    }

    public static double normalizeAngle(double angleDegrees) {
        double deg = angleDegrees;
        // More efficient to do this through division but w/e,
        // I'll never be more than 360 off anyway so same diff
        while (deg < 0)     { deg += 360; }
        while (deg >= 360)  { deg -= 360; }
        return deg;
    }

    public static double round1Decimal(double num) {
        return (Math.round(num * 10.0) / 10.0);
    }

    public static double round2Decimals(double num) {
        return (Math.round(num * 100.0) / 100.0);
    }

    public static double round3Decimals(double num) {
        return (Math.round(num * 1000.0) / 1000.0);
    }

    // Methods that help us parse ints and doubles and allow them to interchange.
    public static int tryParseInt(String value) {  
        return tryParseInt(value, true);
    }

    private static int tryParseInt(String value, boolean tryDoubleToo) {  
        try {  
            int i = Integer.parseInt(value);  
            return i;  
        } catch (NumberFormatException e) {  
            if (tryDoubleToo == true) {
                return (int)tryParseDouble(value, false);
            }
            return 0;  
        }  
    }

    public static double tryParseDoublePercent(String value) {  
        // Strip a % sign off the end of it 
        int index = value.indexOf("%");
        if (index > 0) {
            return tryParseDouble(value.substring(0, index), true);
        }
        return tryParseDouble(value, true);
    }

    public static double tryParseDouble(String value) {  
        return tryParseDouble(value, true);
    }

    private static double tryParseDouble(String value, boolean tryIntToo) {   
        try {  
            double d = Double.parseDouble(value);  
            return d;  
        } catch (NumberFormatException e) {  
            // Ok so try as an int now.
            if (tryIntToo == true) {
                return (double)tryParseInt(value, false);
            }
            return 0;  
        }  
    }

    // If we're done parsing in an array, we may want to move past the array
    // end so that we can get to the next thing.
    public static void moveToEndOfJsonArray(JsonParser parser) {
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            if (event == JsonParser.Event.END_ARRAY) {
                return;
            }
	    }
    }
    
    // This is so stupid, I cant believe these methods arent built into the parser,
    // the java JsonParser class SUCKS.
    public static String getNextJsonValueAsString(JsonParser parser ) {
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            if (event == JsonParser.Event.VALUE_STRING ||
                event == JsonParser.Event.VALUE_NUMBER) {
                return parser.getString();
            }
	    }
        return null;
    }

    public static double getNextJsonValueAsDouble(JsonParser parser) {
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            if (event == JsonParser.Event.VALUE_STRING ||
                event == JsonParser.Event.VALUE_NUMBER) {
                return Utils.tryParseDouble(parser.getString());
            }
	    }
        return 0;
    }

    public static int getNextJsonValueAsInt(JsonParser parser) {
        while (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            if (event == JsonParser.Event.VALUE_STRING ||
                event == JsonParser.Event.VALUE_NUMBER) {
                return Utils.tryParseInt(parser.getString());
            }
	    }
        return 0;
    }
}