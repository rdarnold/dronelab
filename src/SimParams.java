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

import dronelab.utils.TimeData;

public class SimParams {
    
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
    // same time.
    public static enum AlgorithmFlag {
        /*NOT_DEFINED (1 << 0),
        STANDARD    (1 << 1),
        SPIRAL      (1 << 2),
        SCATTER     (1 << 3);*/
        NOT_DEFINED (0, "NotDefined"),
        STANDARD    (1, "Standard"),
        SPIRAL      (2, "Spiral"),
        SCATTER     (3, "Scatter");

        private final long value;
        private final String name;
        private AlgorithmFlag(long value, String name) {
            this.value = value;
            this.name = name;
        }

        public long getValue() {
            return value;
        } 

        public String toString() {
            return name;
        }
        //public static final EnumSet<AlgorithmFlag> ALL = EnumSet.allOf(AlgorithmFlag.class);
        //public static final EnumSet<AlgorithmFlag> NONE = EnumSet.noneOf(AlgorithmFlag.class);
    }

    // The parameters that control how a simulation is run
    private final IntegerProperty timeLimitSeconds = new SimpleIntegerProperty(TimeData.ONE_HOUR_IN_SECONDS * 8);
    //private final IntegerProperty timeLimitSeconds = new SimpleIntegerProperty(TimeData.ONE_HOUR_IN_SECONDS);
    private final IntegerProperty numRandomSurvivors = new SimpleIntegerProperty(300);
    private final IntegerProperty numDrones = new SimpleIntegerProperty(1);
    
    // Start with the full gambit of algorithms whatever they are.
    // No that's not the way I do it anymore, each algorithm is a set of behaviors
    // and they cannot be joined together so the concept of a flag is not relevant anymore.
    //private EnumSet<AlgorithmFlag> algorithmFlags = AlgorithmFlag.NONE;//EnumSet.allOf(AlgorithmFlag.class);
    // Does not need to be a flag
    private AlgorithmFlag algorithmFlag = AlgorithmFlag.STANDARD;

    public SimParams() { 
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