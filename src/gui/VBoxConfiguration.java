package dronelab.gui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
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
import javafx.scene.layout.GridPane;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class VBoxConfiguration extends VBoxCustom {

    Text numSkipsText;
    Label lblOverlay;
    Label lblCollisions;
    Label lblSensors;
    Label lblTracker;
    Label lblVictims;
    Label lblModules;
    Label lblWifi;
    ChoiceBox languageCb;
    ChoiceBox scenarioCb;

    CheckBox cbStructures;
    CheckBox cbCollisions;
    CheckBox cbSensors;
    CheckBox cbTracker;
    CheckBox cbVictims;
    CheckBox cbModules;
    CheckBox cbWifi;
    
    public VBoxConfiguration(DroneLab s) {
        super(s);
    }

    // We cant create them when we declare them because those creations dont
    // get executed until the object is done being created with the constructor.
    // However here we are creating them inside the constructor calling it from the
    // base class so we need to allocate memory here instead.
    @Override
    public void createControls() {
        numSkipsText = new Text();
        lblOverlay = new Label();
        lblCollisions = new Label();
        lblSensors = new Label();
        lblTracker = new Label();
        lblVictims = new Label();
        lblModules = new Label();
        lblWifi = new Label();
        cbStructures = new CheckBox();
        cbCollisions = new CheckBox();
        cbSensors = new CheckBox();
        cbTracker = new CheckBox();
        cbVictims = new CheckBox();
        cbModules = new CheckBox();
        cbWifi = new CheckBox();
    }

    public void setCheckBoxLabelWidths() {
        int wid = 0;
        // Since the check box draws to the right we want
        // these all to hug the boxes
        lblOverlay.setAlignment(Pos.CENTER_RIGHT);
        lblCollisions.setAlignment(Pos.CENTER_RIGHT);
        lblSensors.setAlignment(Pos.CENTER_RIGHT);
        lblTracker.setAlignment(Pos.CENTER_RIGHT);
        lblVictims.setAlignment(Pos.CENTER_RIGHT);
        lblModules.setAlignment(Pos.CENTER_RIGHT);
        lblWifi.setAlignment(Pos.CENTER_RIGHT);
    }

/*
    public void addSkipCount() {
        HBox hb = new HBox();
        Label lbl = new Label("Frame Skips: ");

	    numSkipsText.setText("" + sim.desiredSkipCount);
        Button btn1 = new Button("+");
        btn1.setPrefSize(20, 20);
        btn1.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                sim.desiredSkipCount++;
                numSkipsText.setText("" + sim.desiredSkipCount);

                if (sim.desiredSkipCount == 0)
                    scenario.drone.rotorSpeed = 16;
                else 
                    scenario.drone.rotorSpeed = 31;
            }
        });

        Button btn2 = new Button("-");
        btn2.setPrefSize(20, 20);
        btn2.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                sim.desiredSkipCount--;
                if (sim.desiredSkipCount < 0)
                    sim.desiredSkipCount = 0;
                numSkipsText.setText("" + sim.desiredSkipCount);

                if (sim.desiredSkipCount == 0)
                    scenario.drone.rotorSpeed = 16;
                else 
                    scenario.drone.rotorSpeed = 31;
            }
        });

        hb.getChildren().addAll(lbl, numSkipsText, btn1, btn2);
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_LEFT);

	    this.getChildren().add(hb);
    }*/

    public void addLanguageCB() {
        Label label = new Label("言語 / Language:");
        languageCb = new ChoiceBox(FXCollections.observableArrayList(
            Constants.STR_LANG_ENGLISH, Constants.STR_LANG_JAPANESE, Constants.STR_LANG_BOTH)
        );
        languageCb.getSelectionModel().selectFirst();
        languageCb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            sim.setLanguage(newValue);
            }
	    });
	    this.getChildren().add(GuiUtils.addLabelChoiceHBox(label, languageCb));
    }

    public void addScenarioCb() {
        Label label = new Label("Scenario:");

        // These should read off of the files in the scenario folder.  Or just have them
        // hard coded for good measure.
        scenarioCb = new ChoiceBox(FXCollections.observableArrayList(
            "Arahama", "Kobe")
        );
        scenarioCb.getSelectionModel().selectFirst();
        scenarioCb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            sim.selectScenario(newValue);
                sim.signalRedraw();
            }
	    });
	    this.getChildren().add(GuiUtils.addLabelChoiceHBox(label, scenarioCb));
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                // Sim config
                strTitle.setText("Settings");
                lblOverlay.setText("Structures:");
                lblCollisions.setText("Collisions:");
                lblSensors.setText("Sensors:");
                lblTracker.setText("Path:");
                lblVictims.setText("Victims:");
                lblModules.setText("Modules:");
                lblWifi.setText("Wi-Fi:");
                break;

            case JAPANESE:
                // Sim config
                strTitle.setText("シミュレーション構成");
                lblOverlay.setText("構造");
                lblCollisions.setText("衝突");
                lblSensors.setText("センサ:");
                lblTracker.setText("パス:");
                lblVictims.setText("犠牲者:");
                lblModules.setText("モジュール:");
                lblWifi.setText("Wi-Fi:");
                break;

            case BOTH:
                // Sim config
                strTitle.setText("シミュレーション構成 Simulation Config");
                lblOverlay.setText("構造 Structures:");
                lblCollisions.setText("衝突 Collisions:");
                lblSensors.setText("センサ Sensors:");
                lblTracker.setText("パス Path:");
                lblVictims.setText("犠牲者 Victims:");
                lblModules.setText("モジュール Modules:");
                lblWifi.setText("Wi-Fi:");
                break;
        }
    }

    @Override
    public void clear() {
        // There isn't much to clear in the config box
    }

    @Override
    public void updateChoiceBoxText() {
        // Nothing to update right now either since its just settings.
    }

    @Override
    public void update() {
        // We dont do anything continuously here right now.
    }

    @Override
    public void updateOnDemand() {
        // This function update when the drone's parameters are updated.  These
        // settings arent realated to that so we do nothing here.
    }

    public void populateCheckBoxes() {
        checkSetInitial(cbStructures, Constants.DrawFlag.STRUCTURES);
        checkSetInitial(cbCollisions, Constants.DrawFlag.COLLISIONS);
        checkSetInitial(cbSensors, Constants.DrawFlag.SENSORS);
        checkSetInitial(cbVictims, Constants.DrawFlag.VICTIMS);
        checkSetInitial(cbModules, Constants.DrawFlag.MODULES);
        checkSetInitial(cbWifi, Constants.DrawFlag.WIFI);
    }

    private void checkSetInitial(CheckBox cb, Constants.DrawFlag flag) {
        //ltfSimSpeed.setFieldText("1.0");
        // We shouldnt be calling this until we have a scenario ready because thats
        // the whole point of this function.
        if (scenario == null) {
            Utils.err("SimConfigVBox.java: checkSetInitial called without scenario");
            return;
        }
        if (scenario.drawFlags.contains(flag) == true) {
            cb.setSelected(true);
        }
    }

    private HBox createCheckBoxHBox() {
        HBox hb = new HBox();
        hb.setSpacing(10);
        hb.setAlignment(Pos.CENTER_LEFT);
        return hb;
    }

    private void setupCheckBoxLayout() {
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(6);
        gridpane.setVgap(6);

        //GridPane.setHalignment(candidatesLbl, HPos.CENTER);
        //gridpane.add(candidatesLbl, 0, 0);
        gridpane.add(lblOverlay,    0, 0);
        gridpane.add(cbStructures,  1, 0);
        gridpane.add(lblCollisions, 2, 0);
        gridpane.add(cbCollisions,  3, 0);
        gridpane.add(lblSensors,    4, 0);
        gridpane.add(cbSensors,     5, 0);

        gridpane.add(lblTracker,    0, 1);
        gridpane.add(cbTracker,     1, 1);
        gridpane.add(lblVictims,    2, 1);
        gridpane.add(cbVictims,     3, 1);
        gridpane.add(lblModules,    4, 1);
        gridpane.add(cbModules,     5, 1);

        gridpane.add(lblWifi,       0, 2);
        gridpane.add(cbWifi,        1, 2);

        GridPane.setHalignment(lblOverlay, HPos.RIGHT);
        GridPane.setHalignment(lblCollisions, HPos.RIGHT);
        GridPane.setHalignment(lblSensors, HPos.RIGHT);
        GridPane.setHalignment(lblTracker, HPos.RIGHT);
        GridPane.setHalignment(lblVictims, HPos.RIGHT);
        GridPane.setHalignment(lblModules, HPos.RIGHT);
        GridPane.setHalignment(lblWifi, HPos.RIGHT);

        getChildren().add(gridpane);
    }

    private void setupCheckBoxListeners() {
        cbStructures.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                //scenario.showOverlay = new_val;
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.STRUCTURES);
                sim.signalRedraw();
            }
        });


        cbCollisions.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                //scenario.showCollisions = new_val;
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.COLLISIONS);
                sim.signalRedraw();
            }
        });

        cbSensors.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                //scenario.showSensorRanges = new_val;
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.SENSORS);
                sim.signalRedraw();
            }
        });

        cbTracker.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                scenario.drone.tracker.setDraw(new_val);
                sim.signalRedraw();
            }
        });

        cbVictims.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                //scenario.showVictims = new_val;
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.VICTIMS);
                sim.signalRedraw();
            }
        });


        cbModules.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                //scenario.showModules = new_val;
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.MODULES);
                sim.signalRedraw();
            }
        });

        cbWifi.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                scenario.updateDrawFlags(new_val, Constants.DrawFlag.WIFI);
                sim.signalRedraw();
            }
        });
    }

    @Override
    protected void create() {
        setCheckBoxLabelWidths();

        //addSkipCountToSimConfig(vbox);
        addLanguageCB();
        addScenarioCb();

        setupCheckBoxListeners();
        setupCheckBoxLayout();
    }
}