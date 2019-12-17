package dronelab.utils;

public class TimeData {
    private int hour = 0;
    private int minute = 0; 
    private int second = 0; 
    private int frame = 0;
    public int totalSeconds = 0;
    private int maxSeconds = 0;

    public static final int ONE_MIN_IN_SECONDS = 60;
    public static final int ONE_HOUR_IN_SECONDS = ONE_MIN_IN_SECONDS * 60;

    public TimeData() {}
    public TimeData(String timeStr) {
        fromString(timeStr);
    }

    public void reset() {
        hour = 0;
        minute = 0; 
        second = 0; 
        frame = 0;
        totalSeconds = 0;
    }

    public void setMaxSeconds(int sec) {
        maxSeconds = sec;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public void advanceHour() {
        hour++;
    }

    public void advanceMinute() {
        minute++;
        if (minute >= 60) {
            minute = 0;
            advanceHour();
        }
    }

    public boolean advanceSecond() {
        second++;
        totalSeconds++;
        if (second >= 60) {
            second = 0;
            advanceMinute();
        }
        if (maxSeconds != 0 && totalSeconds >= maxSeconds) {
            return false;
        }
        return true;
    }

    public boolean advanceFrame() {
        // This assumes 60 FPS
        frame++;
        if (frame >= 60) {
            frame = 0;
            return advanceSecond();
        }
        return true;
    }

    public String toString() {
        return ("" + hour + ":" + 
            (minute < 10 ? "0" : "") + minute + ":" + 
            (second < 10 ? "0" : "") + second);
    }

    // A check to make sure it's something we can actually interpret.
    // This isn't foolproof but at least checks a few things.
    public static boolean validTimeStr(String timeStr) {
        // It must have at least 7 characters: 0:00:00
        if (timeStr == null || timeStr.length() < 7) 
            return false;

        // It must have two colons
        // First colon
        int index = timeStr.indexOf(":");
        if (index == -1)
            return false;

        // Second colon
        if (timeStr.indexOf(":", index) == -1)
            return false;

        return true;
    }

    // Interpret a time string and spit back the total seconds it took.
    public static int totalSecondsFromString(String timeStr) {
        int start = 0;
        int end = 0;

        // Take the same type of string we output and interpret that
        // as seconds, which we can then translate into a set of time data
        // or use for various other things.
        end = timeStr.indexOf(":");
        int hours = Utils.tryParseInt(timeStr.substring(start, end));

        start = end + 1;
        end = timeStr.indexOf(":", start);
        int minutes = Utils.tryParseInt(timeStr.substring(start, end));
        
        start = end + 1;
        end = timeStr.length();
        int seconds = Utils.tryParseInt(timeStr.substring(start, end));

        int total = seconds + 
                    minutes * 60 +
                    hours * 60 * 60;
        
        return total;
    }

    // Pass it a total amount of seconds and set the various other values
    // based on that.
    public void fromSeconds(int seconds) {
        totalSeconds = seconds;
        int left = seconds;

        hour = left / 60 / 60;
        left -= hour * 60 * 60;
        
        minute = left / 60;
        left -= minute * 60;

        second = left;
    }

    // Take an input string such as:
    // 11:41:17 or 00:13:01
    // and turn it into numbered time data.
    public void fromString(String timeStr) {
        // It must have at least 7 characters: 0:00:00
        if (timeStr.length() < 7) 
            return;

        reset();

        // Take the same type of string we output and interpret that
        // as a set of time data.  So like, 4:10:56 or whatnot.
        fromSeconds(totalSecondsFromString(timeStr));
        // We assume if this has zero hours, it has printed 0 and
        // hasn't just left it blank, as per the toString method.
        /*int start = 0;
        int end = 0;

        end = timeStr.indexOf(":");
        hour = Utils.tryParseInt(timeStr.substring(start, end));

        start = end + 1;
        end = timeStr.indexOf(":", start);
        minute = Utils.tryParseInt(timeStr.substring(start, end));
        
        start = end + 1;
        end = timeStr.length();
        second = Utils.tryParseInt(timeStr.substring(start, end));

        totalSeconds = second + 
                       minute * 60 +
                       hour * 60 * 60;*/
    }

    // Just a utility function really.  It doesn't need to be on this
    // class but it sort of makes sense.
    public static String printHMSFromMillis(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000*60)) % 60);
        int hours = (int) (millis / (1000*60*60));
        String str = String.format("%01d:%02d:%02d", hours, minutes, seconds);
        return str;
    }
}