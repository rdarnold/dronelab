package dronelab.gui;

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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.collidable.equipment.*;
import dronelab.utils.*;

public class VBoxSensor extends VBoxCustom {

    ChoiceBox cbAdd;
    ChoiceBox cbRemove;
    //TextArea behaviorTa = new TextArea();
    Label lblSensorText;
    Label lblAdd;
    Label lblRemove;
    Label lblSensors;

    public VBoxSensor(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        cbAdd = new ChoiceBox();
        cbRemove = new ChoiceBox();
        // behaviorTa = new TextArea();
        lblSensorText = new Label();
        lblSensorText.setTextFill(Color.DARKGREEN);
        lblAdd = new Label();
        lblRemove = new Label();
        lblSensors = new Label();
    }

    public void processAddSensor(Drone d, String sel) {
        if (d == null) {
            return;
        }
	    d.addSensor(GuiUtils.getSensorNameBilingual(sel));
    }

    public void processRemoveSensor(Drone d, String sel) {
        if (d == null) {
            return;
        }
	    d.removeSensor(GuiUtils.getSensorNameBilingual(sel));
    }

    public boolean hasSensor(Drone d, String name) {
        for (Sensor s : d.sensors) {
            if (s.getName() == name) {
                return true;
            }
        }
        return false;
    }

    public void addNonexistentSensor(ObservableList<String> items, Drone d, String name, String strAdd) {
        if (hasSensor(d, name) == false) {
            items.add(strAdd);
        }
    }

    // Create a list of behaviors based on what the drone has or doesn't have
    public ObservableList<String> createSensorList(Drone d, boolean bHas) {
        ObservableList<String> items = FXCollections.observableArrayList();

        if (d == null) {
            return items;
        }

        switch (sim.language) {
            case ENGLISH:
                if (bHas == true) {
                    for (Sensor s : d.sensors) {
                        items.add(s.getName());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_SENSORS.length; i++) {
                        addNonexistentSensor(items, d, Constants.STR_SENSORS[i], 
                            Constants.STR_SENSORS[i]);
                    }
                }
	            break;
            case JAPANESE:
                if (bHas == true) {
                    for (Sensor s : d.sensors) {
                        items.add(s.getNameJapanese());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_SENSORS.length; i++) {
                        addNonexistentSensor(items, d, Constants.STR_SENSORS[i], 
                            Constants.STR_SENSORS_J[i]);
                    }
                }
	            break;
            case BOTH:
                if (bHas == true) {
                    for (Sensor s : d.sensors) {
                        items.add(s.getNameJapanese() + " " + s.getName());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_SENSORS.length; i++) {
                        addNonexistentSensor(items, d, Constants.STR_SENSORS[i], 
                            Constants.STR_SENSORS_J[i] + " " + Constants.STR_SENSORS[i]);
                    }
                }
	            break;
        }

        return items;
    }

    @Override
    public void clear() {
        lblSensorText.setText("");
        cbAdd.setItems(FXCollections.observableArrayList());
        cbRemove.setItems(FXCollections.observableArrayList());
    }

    @Override
    public void update() {
        // We dont do anything continuously here right now
    }

    @Override
    public void updateOnDemand() {
        String text = "";
        int prefHeight = 25;
        //int rowHeight = 0;
        Drone drone = drone();

        if (drone == null) {
            clear();
            return;
        }

        for (Sensor s : drone.sensors) {
            if (text != "")
                text += ", ";
            text += "" + s.getName();
        }

        lblSensorText.setPrefHeight(prefHeight);
        lblSensorText.setText(text);

        updateChoiceBoxText();
    }

    @Override
    public void updateChoiceBoxText() {
        Drone drone = drone();
        cbAdd.setItems(createSensorList(drone, false));
        cbRemove.setItems(createSensorList(drone, true));
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                strTitle.setText("Sensors:");
                lblAdd.setText("Add:");
                lblRemove.setText("Remove:");
                lblSensors.setText("Sensors:");
                break;

            case JAPANESE:
                strTitle.setText("Sensors:");
                lblAdd.setText("Add:");
                lblRemove.setText("Remove:");
                lblSensors.setText("Sensors:");
                break;

            case BOTH:
                strTitle.setText("Sensors:");
                lblAdd.setText("Add:");
                lblRemove.setText("Remove:");
                lblSensors.setText("Sensors:");
                break;
        }
    }

    @Override
    protected void create() {

        cbAdd.setPrefSize(10, 10);
        cbAdd.setTooltip(new Tooltip("Select the sensor to be added"));
        cbAdd.getSelectionModel().selectFirst();

        cbAdd.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                processAddSensor(drone(), newValue);
                sim.updateDroneParameterFields();
                cbAdd.getSelectionModel().clearSelection();
                // Clear value of ComboBox because clearSelection() does not do it
                cbAdd.setValue(null);
            }
	    });

        cbRemove.setPrefSize(10, 10);
        cbRemove.setTooltip(new Tooltip("Select the sensor to be removed"));
        cbRemove.getSelectionModel().selectFirst();

        cbRemove.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            processRemoveSensor(drone(), newValue);
	            //cbRemoveBehavior.getSelectionModel().selectFirst();
                sim.updateDroneParameterFields();
                cbRemove.getSelectionModel().clearSelection();
                // Clear value of ComboBox because clearSelection() does not do it
                cbRemove.setValue(null);
            }
	    });

        HBox hb = new HBox();
        hb.getChildren().addAll(lblAdd, cbAdd);
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER_LEFT);

        HBox hb2 = new HBox();
        hb2.getChildren().addAll(lblRemove, cbRemove);
        hb2.setSpacing(5);
        hb2.setAlignment(Pos.CENTER_LEFT);

        HBox hb3 = new HBox();
        hb3.setSpacing(30);
        hb3.setAlignment(Pos.CENTER_LEFT);
        hb3.getChildren().addAll(hb, hb2);

        this.getChildren().add(hb3);

        hb = GuiUtils.addLabelTextHBox(lblSensors, lblSensorText);
        hb.setPrefHeight(lblSensorText.getPrefHeight());
        hb.setAlignment(Pos.CENTER_LEFT);
	    this.getChildren().add(hb);

        updateChoiceBoxText();
    }
}