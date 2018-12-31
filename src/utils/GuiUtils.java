
package dronelab.utils;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public final class GuiUtils {

    private GuiUtils() { // private constructor
    }    

    public static void disableTextField(TextField tf) {
        tf.setDisable(true); 
        tf.setStyle("-fx-opacity: 1; -fx-background-color: #EEE");
    }

    public static HBox addLabelTextOnlyHBox(Label label, Text t) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, t);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_RIGHT);
	    return hb;
    }

    public static HBox addLabelTextHBox(Label label, Label textLabel) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, textLabel);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_RIGHT);
	    return hb;
    }

    public static HBox addLabelTextHBox(Label label, TextArea ta) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, ta);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_RIGHT);
	    return hb;
    }

    public static HBox addLabelTextHBox(Label label, TextField tf) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, tf);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_RIGHT);
	    return hb;
    }

    public static HBox addLabelChoiceHBox(Label label, ChoiceBox cb) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, cb);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_RIGHT);
	    return hb;
    }

    public static HBox addLabelChoiceHBox(Label label, ChoiceBox cb, Pos align) {
        HBox hb = new HBox();
        hb.getChildren().addAll(label, cb);
        hb.setSpacing(10);
        hb.setAlignment(align);
	    return hb;
    }

    public static String getSensorNameBilingual(String text) {
        if (text == null) {
            return null;
        }
        
        for (int i = 0; i < Constants.STR_SENSORS.length; i++) {
            if (text.contains(Constants.STR_SENSORS[i])) {
                return Constants.STR_SENSORS[i];
            }
            if (text.contains(Constants.STR_SENSORS_J[i])) {
                return Constants.STR_SENSORS[i];
            }
        }

	    return null;
    }

    public static String getBehaviorModuleNameBilingual(String text) {
        if (text == null) {
            return null;
        }

        for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
            if (text.contains(Constants.STR_BEHAVIORS[i])) {
                return Constants.STR_BEHAVIORS[i];
            }
            if (text.contains(Constants.STR_BEHAVIORS_J[i])) {
                return Constants.STR_BEHAVIORS[i];
            }
        }

	    return null;
    }
    
    public static String getDroneTypeNameBilingual(String text) {
        if (text == null) {
            return null;
        }
        String name = text;
        /*if (text.contains(Constants.STR_DJI_PHANTOM_3) || text.contains(Constants.STR_DJI_PHANTOM_3_J))
            name = Constants.STR_DJI_PHANTOM_3;
        else if (text.contains(Constants.STR_DJI_PHANTOM_4) || text.contains(Constants.STR_DJI_PHANTOM_4_J))
            name = Constants.STR_DJI_PHANTOM_4;*/

	    return name;
    }
}