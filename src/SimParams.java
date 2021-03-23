package dronelab;

import java.util.List;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import dronelab.collidable.Drone.DroneRole;
import dronelab.collidable.Drone;
import dronelab.utils.*;

public class SimParams {
    
    // Params can have a simulation matrix read in from file which dictates the distributions
    // of drones of different roles for each run
    private SimMatrix simMatrix = null;
    public void setSimMatrix(SimMatrix mat) { simMatrix = mat; }
    public SimMatrix getSimMatrix() { return simMatrix; }

    // Which item are we "currently" on during the simulation:
    private SimMatrixItem currentItem = null;
    public void setSimMatrixItem(SimMatrixItem item) { currentItem = item; }
    public SimMatrixItem getSimMatrixItem() { return currentItem; }

    // Conversely I can name them differently fro the ones in the actual
    // XML using the annotation @XmlElement(name="XYZ") where XYZ is the actual
    // name in the XML file.  If not specified it defaults to the name of the variable.
    /*private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty text = new SimpleStringProperty("");
    private ObjectProperty<AnswerType> answerType = new SimpleObjectProperty<>(AnswerType.Input);

    // So this works just fine if I dont have it wrapped in answerOptions
    //@XmlElement(name="answerOption")
    //private ArrayList<String> answerOptions = new ArrayList<String>();
    //public ArrayList<String> getAnswerOptions() { return answerOptions;  }

    // If I do have it wrapped, I need a new AnswerOptionsWrapper class.
    // Basically anytime you have a list of things that is down one level in
    // some kind of XML element, you need to wrap it in a class
    // and that class holds the actual list.
    // Start wrapping stuff
    @XmlElement(name="answerOptions")
    private AnswerOptionsWrapper answerOptionsWrapper; // Don't need a new since the class is static

    public static class AnswerOptionsWrapper {
        // We need the annotations because the variable name is different from the XML element name.
        @XmlElement(name="answerOption")
        public ArrayList<String> answerOptions = new ArrayList<String>();
    }
    public ArrayList<String> getAnswerOptions() {  return answerOptionsWrapper.answerOptions;  }
    //End wrapping stuff
    */

    // Bit flag on which algorithms to test
    // This is kind of weird as a bit flag.  We don't test these at the 
    // same time.  -- Update, what the hell did I mean here???  same time??? 
    // -- Update2, of course I meant we don't use them with two or more flags assigned to
    // a single variable, so there's no need to make them bit flags.  Somehow that's
    // more clear to me now at 2:45am on a Sunday night than it was when I last read this
    public static enum AlgorithmFlag {
        /*NOT_DEFINED (1 << 0),
        STANDARD    (1 << 1),
        SPIRAL      (1 << 2),
        SCATTER     (1 << 3);*/
        NOT_DEFINED (0, "NotDefined",           "NOT_DEFINED"),
        STANDARD    (1, "Standard",             "STANDARD"),
        SPIRAL      (2, "Spiral",               "SPIRAL"),
        SCATTER     (3, "Scatter",              "SCATTER"),
        MIX_SRA     (4, "MixSocialRelayAnti",   "MIX_SRA"),  // Mixes are defined by a simulation matrix file
        MIX_SRA_C   (5, "C-MixSocialRelayAnti", "MIX_SRA_C");  // MIX_SRA but the pattern search is assigned by a centralized controller

        private final long value;
        private final String name;
        private final String loadName;
        private AlgorithmFlag(long value, String name, String loadName) {
            this.value = value;
            this.name = name;
            this.loadName = loadName;
        }

        public long getValue() {
            return value;
        } 

        public String toString() {
            return name;
        }

        public String toLoadString() {
            return loadName;
        }

        public static AlgorithmFlag fromLoadString(String strFrom) {
            if (strFrom.equals("STANDARD"))         { return STANDARD; }
            else if (strFrom.equals("SPIRAL"))      { return SPIRAL; }
            else if (strFrom.equals("SCATTER"))     { return SCATTER; }
            else if (strFrom.equals("MIX_SRA"))     { return MIX_SRA; }
            else if (strFrom.equals("MIX_SRA_C"))   { return MIX_SRA_C; }
            else                                    { return NOT_DEFINED; }
        }
        //public static final EnumSet<AlgorithmFlag> ALL = EnumSet.allOf(AlgorithmFlag.class);
        //public static final EnumSet<AlgorithmFlag> NONE = EnumSet.noneOf(AlgorithmFlag.class);
    }

    // The parameters that control how a simulation is run
    private final IntegerProperty timeLimitSeconds = new SimpleIntegerProperty(TimeData.ONE_HOUR_IN_SECONDS * 8);
    //private final IntegerProperty timeLimitSeconds = new SimpleIntegerProperty(TimeData.ONE_HOUR_IN_SECONDS);
    private final IntegerProperty numRandomSurvivors = new SimpleIntegerProperty(300);
    private final IntegerProperty numDrones = new SimpleIntegerProperty(1);
    private final IntegerProperty numRepetitions = new SimpleIntegerProperty(1);
    
    // Start with the full gambit of algorithms whatever they are.
    // No that's not the way I do it anymore, each algorithm is a set of behaviors
    // and they cannot be joined together so the concept of a flag is not relevant anymore.
    //private EnumSet<AlgorithmFlag> algorithmFlags = AlgorithmFlag.NONE;//EnumSet.allOf(AlgorithmFlag.class);
    // Does not need to be a flag
    private AlgorithmFlag algorithmFlag = AlgorithmFlag.STANDARD;

    // Why do I have everything as properties?  Don't think I do any XML processing here???
    // These will just be normal member vars for now...
    Drone.DroneRole role1 = Drone.DroneRole.SOCIAL;
    int role1_num = 0;
    public Drone.DroneRole getRole1() { return role1; }
    public int getNumRole1() { return role1_num; }

    Drone.DroneRole role2 = Drone.DroneRole.RELAY;
    int role2_num = 0;
    public Drone.DroneRole getRole2() { return role2; }
    public int getNumRole2() { return role2_num; }

    Drone.DroneRole role3 = Drone.DroneRole.ANTISOCIAL;
    int role3_num = 0;
    public Drone.DroneRole getRole3() { return role3; }
    public int getNumRole3() { return role3_num; }

    double wifi_range = 1.0;
    public double getWifiRange() { return wifi_range; }

    public SimParams() { 
        // Set the num survivors to whatever we have set in the config
        setNumRandomSurvivors(Config.getNumRandomSurvivorsLoaded());
        Utils.log("SimParams set to " + Config.getNumRandomSurvivorsLoaded() + " random survivors");
    }

    /*public EnumSet<AlgorithmFlag> getAlgorithmFlags() {
        return algorithmFlags;
    }*/

    public AlgorithmFlag getAlgorithmFlag() {
        return algorithmFlag;
    }

    public void setAlgorithmFlag(AlgorithmFlag flag) {
        algorithmFlag = flag;
    }

    public int getTimeLimitHours() {
        return (getTimeLimitMinutes() / 60);
    }

    public int getTimeLimitMinutes() {
        return (getTimeLimitSeconds() / 60);
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds.get();
    }

    public void setTimeLimitSeconds(int num) {
        timeLimitSeconds.set(num);
    }

    public IntegerProperty timeLimitSecondsProperty() {
        return timeLimitSeconds;
    }

    public int getNumRandomSurvivors() {
        return numRandomSurvivors.get();
    }

    public void setNumRandomSurvivors(int num) {
        numRandomSurvivors.set(num);
    }

    public IntegerProperty numRandomSurvivorsProperty() {
        return numRandomSurvivors;
    }

    public int getNumDrones() {
        return numDrones.get();
    }

    public void setNumDrones(int num) {
        numDrones.set(num);
    }

    public IntegerProperty numDronesProperty() {
        return numDrones;
    }

    public int getNumRepetitions() {
        return numRepetitions.get();
    }

    public void setNumRepetitions(int num) {
        numRepetitions.set(num);

        // If we have a simulation matrix (which we should), then when we
        // set the number of reps, we need to tell that to the matrix so
        // it can allocate enough space
        if (getSimMatrix() != null) {
            getSimMatrix().setNumRepetitionsPerItem(num);
        }
    }

    public IntegerProperty numRepetitionsProperty() {
        return numRepetitions;
    }


    // Added to support loading different role distributions from a simulation matrix
    public boolean setup(int numRuns) {
        if (simMatrix == null) {
            Utils.log("ERROR - tried to run setup on SimParams without a simulation matrix");
            return false;
        }

        if (numRuns >= simMatrix.size()) {
            Utils.log("ERROR - numRuns is larger than sim matrix size in SimParams setup");
            return false;
        }

        SimMatrixItem item = simMatrix.get(numRuns);
        if (item == null) {
            Utils.log("ERROR - tried to load sim matrix item " + numRuns + "but it does not exist");
            return false;
        }

        currentItem = item;

        role1_num = item.getSocialNum();
        role2_num = item.getRelayNum();
        role3_num = item.getAntiNum();

        wifi_range = item.getWifiRange();

        //Utils.log(role1_num);
        //Utils.log(role2_num);
        //Utils.log(role3_num);

        setNumDrones(role1_num + role2_num + role3_num);

        return true;
    }

    /*public AnswerType getAnswerType() {
        return answerType.get();
    }

    public void setAnswerType(AnswerType newType) {
        answerType.set(newType);
    }

    public ObjectProperty<AnswerType> answerTypeProperty() {
        return answerType;
    }*/
}