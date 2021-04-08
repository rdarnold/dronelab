package dronelab.gui;

import javafx.scene.layout.Pane;
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
import dronelab.SimParams;
import dronelab.DataCompiler;
import dronelab.collidable.*;
import dronelab.utils.*;


public class VBoxSimulation extends VBoxCustom {

    Button btnBegin;
    Button btnPerformAllRuns;

    ChoiceBox cbPreset;
    LabelTextField ltfNumRandomSurvivors;
    LabelTextField ltfNumDrones;
    LabelTextField ltfTimeLimit;
    ChoiceBox cbAlgorithm;

    Button btnApply;
    Button btnMax;
    Button btnMin;
    LabelTextField ltfSimSpeed;

    public VBoxSimulation(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        // What am I going to want here ...
        /* 
        - Time span (default 24 hours)
        - Speed rate (ffwd up to some time)

        Measures:
        - Camera Coverage (read-only; showing drone coverage)
            - Coverage over time (a graph?)
        - Distressed Persons spotted
            - Spotted over time (graph as well?)*/

        btnBegin = new Button();
        btnPerformAllRuns = new Button();
        cbPreset = new ChoiceBox();
        ltfNumRandomSurvivors = new LabelTextField();
        ltfNumDrones = new LabelTextField();
        ltfTimeLimit = new LabelTextField();
        cbAlgorithm = new ChoiceBox();
        btnApply = new Button();
        btnMax = new Button();
        btnMin = new Button();
        ltfSimSpeed = new LabelTextField();
    }

    @Override
    public void clear() {
    }

    @Override
    public void update() {
        // We dont do anything continuously here right now
    }

    @Override
    public void updateOnDemand() {
    }

    @Override
    public void updateChoiceBoxText() {
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                strTitle.setText("Simulation:");
                btnBegin.setText("Begin");
                btnPerformAllRuns.setText("Perform All Runs");
                ltfNumRandomSurvivors.setLabelText("Number of Random Survivors: ");
                ltfNumDrones.setLabelText("Number of Drones: ");
                ltfTimeLimit.setLabelText("Time Limit (Seconds): ");
                ltfSimSpeed.setLabelText("Fast Forward: ");
                btnMax.setText("Max");
                btnMin.setText("Min");
                btnApply.setText("Apply");
                break;

            case JAPANESE:
                strTitle.setText("Simulation:");
                btnBegin.setText("Begin");
                btnPerformAllRuns.setText("Perform All Runs");
                ltfNumRandomSurvivors.setLabelText("Number of Random Survivors: ");
                ltfNumDrones.setLabelText("Number of Drones: ");
                ltfTimeLimit.setLabelText("Time Limit (Seconds): ");
                ltfSimSpeed.setLabelText("Fast Forward: ");
                btnMax.setText("Max");
                btnMin.setText("Min");
                btnApply.setText("Apply");
                break;

            case BOTH:
                strTitle.setText("Simulation:");
                btnBegin.setText("Begin");
                btnPerformAllRuns.setText("Perform All Runs");
                ltfNumRandomSurvivors.setLabelText("Number of Random Survivors: ");
                ltfNumDrones.setLabelText("Number of Drones: ");
                ltfTimeLimit.setLabelText("Time Limit (Seconds): ");
                ltfSimSpeed.setLabelText("Fast Forward: ");
                btnMax.setText("Max");
                btnMin.setText("Min");
                btnApply.setText("Apply");
                break;
        }
    }

    @Override
    protected void create() {
        // Set up width / height preferences first
        ltfNumRandomSurvivors.setFieldPrefWidth(70);
        ltfNumDrones.setFieldPrefWidth(70);
        ltfTimeLimit.setFieldPrefWidth(70);
        btnMax.setPrefWidth(50);
        btnMin.setPrefWidth(50);
        ltfSimSpeed.setFieldPrefWidth(50);
        btnApply.setPrefWidth(100);
        btnBegin.setPrefWidth(125);
        btnBegin.setPrefHeight(35);
        btnPerformAllRuns.setPrefWidth(225);
        btnPerformAllRuns.setPrefHeight(55);

        // Now add the choicebox
        addCbPreset(this);

        // We need a grid pane of check boxes for different algorithm tests too.

        // Set up variables for boxes to visually simplify this function
        VBox vbox;
        HBox hbox;

        // The area with the label text for the parameters
        vbox = new VBox();
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(ltfNumRandomSurvivors, ltfNumDrones, ltfTimeLimit);
        // Now the algorithm choicebox
        addCbAlgorithm(vbox);
	    this.getChildren().add(vbox);

        // Now the area for the fast forward button.  It is an hbox within a vbox.
        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));
        hbox.getChildren().addAll(ltfSimSpeed, btnMax, btnMin);
        
        vbox = new VBox();
        // Pad this area generously
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
	    vbox.getChildren().add(hbox);
        // Put the apply button under the hbox
	    vbox.getChildren().add(btnApply);
	    this.getChildren().add(vbox);
        
        // Now finally the begin button
        vbox = new VBox();
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(btnBegin);
	    this.getChildren().add(vbox);

        vbox = new VBox();
        vbox.setPadding(new Insets(30));
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(btnPerformAllRuns);
	    this.getChildren().add(vbox);

        // Set up button behaviors now.
        btnMax.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                ltfSimSpeed.setFieldText(Constants.MAX_FFW_RATE);
                applyFastForward();
            }
        });

        btnMin.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                ltfSimSpeed.setFieldText(Constants.MAX_FFW_RATE);
                applyFastForward();
            }
        });

        btnApply.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                apply();
            }
        });

        btnBegin.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                begin();
            }
        });

        btnPerformAllRuns.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                performAllRuns();
            }
        });
    }

    @Override
    public void setScenario(Scenario s) {
        super.setScenario(s);
        resetFields();
    }

    public void addCbPreset(Pane addTo) {
        Label label = new Label("Preset:");

        // These should generate from a set of SimParam presents created
        // programmatically and/or loaded from file.
        cbPreset = new ChoiceBox(FXCollections.observableArrayList(
            "300p /  1dr / 8h", 
            "300p /  5dr / 8h", 
            "300p / 20dr / 8h"
            )
        );
        cbPreset.getSelectionModel().select(0);
        // Note the use of selectedIndexProperty and not selectedItemProperty
        // Lets us use Number instead of String
        cbPreset.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue observable, Number oldValue, Number newValue) {
	            // Do something
                applyPreset(newValue.intValue());
            }
	    });
	    addTo.getChildren().add(GuiUtils.addLabelChoiceHBox(label, cbPreset));
    }

    public void addCbAlgorithm(Pane addTo) {
        Label label = new Label("Algorithm:");

        // These should generate from a set of SimParam presents created
        // programmatically and/or loaded from file.
        /*cbAlgorithm = new ChoiceBox(FXCollections.observableArrayList(
            "None", 
            "Standard", 
            "Spiral",
            "Scatter",
            "MixSocialRelayAnti"
            )
        );*/
        ObservableList<String> oal = FXCollections.observableArrayList();
        for (SimParams.AlgorithmFlag flag : SimParams.AlgorithmFlag.values()) {
            oal.add(flag.getName());
        }
        cbAlgorithm = new ChoiceBox(oal);

        cbAlgorithm.getSelectionModel().selectFirst();
        // Note the use of selectedIndexProperty and not selectedItemProperty
        // Lets us use Number instead of String
        cbAlgorithm.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue observable, Number oldValue, Number newValue) {
	            // Do something
                setAlgorithm();
            }
	    });
	    addTo.getChildren().add(GuiUtils.addLabelChoiceHBox(label, cbAlgorithm));
    }

    public void applyPreset(int val) {
        int numPeople = 300;
        int hours = TimeData.ONE_HOUR_IN_SECONDS * 8;
        switch (val) {
            case 0:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(1);
                ltfTimeLimit.setFieldText(hours);
                //cbAlgorithm.getSelectionModel().select(0);
                break;
            case 1:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(5);
                ltfTimeLimit.setFieldText(hours);
                //cbAlgorithm.getSelectionModel().select(0);
                break;
            case 2:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(20);
                ltfTimeLimit.setFieldText(hours);
                //cbAlgorithm.getSelectionModel().select(0);
                break;
            case 3:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(30);  // We'll default to 30 for no particular reason
                ltfTimeLimit.setFieldText(hours);
                //cbAlgorithm.getSelectionModel().select(0);
                break;
            /*case 3:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(1);
                ltfTimeLimit.setFieldText(hours);
                cbAlgorithm.getSelectionModel().select(1);
                break;
            case 4:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(5);
                ltfTimeLimit.setFieldText(hours);
                cbAlgorithm.getSelectionModel().select(1);
                break;
            case 5:
                ltfNumRandomSurvivors.setFieldText(numPeople);
                ltfNumDrones.setFieldText(20);
                ltfTimeLimit.setFieldText(hours);
                cbAlgorithm.getSelectionModel().select(1);
                break;*/
        }
    }

    public void setAlgorithm() {
        SimParams params = scenario.simParams;
        //params.getAlgorithmFlags().clear();
        int index = cbAlgorithm.getSelectionModel().getSelectedIndex();

        params.setAlgorithmFlag((SimParams.AlgorithmFlag.fromValue(index)));

        /*switch (index) {
            case 0:
                //params.getAlgorithmFlags().add(SimParams.AlgorithmFlag.NOT_DEFINED);
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.NOT_DEFINED);
                break;
            case 1:
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.STANDARD);
                break;
            case 2:
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.SPIRAL);
                break;
            case 3:
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.SCATTER);
                break;
            case 4:
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.MIX_SRA);
                break;
            case 5:
                params.setAlgorithmFlag(SimParams.AlgorithmFlag.MIX_SRA_C);
                break;
        }*/
        // And apply it to the scenario immediately.
        scenario.applyAlgorithm();
    }
    
    public void setNumRandomSurvivorsFromConfig() {
        ltfNumRandomSurvivors.setFieldText(Config.getNumRandomSurvivorsLoaded());
    }

    public void apply() {
        applyFastForward();

        SimParams params = scenario.simParams;
        if (params == null) {
            return;
        }

        int num;

        // Should probably do some checking here
        num = ltfNumRandomSurvivors.getValueAsInt();
        params.setNumRandomSurvivors(num);

        num = ltfNumDrones.getValueAsInt();
        params.setNumDrones(num);

        num = ltfTimeLimit.getValueAsInt();
        params.setTimeLimitSeconds(num);

        // Get the selection
        setAlgorithm();
    }

    public void setFFW(int factor) {
        ltfSimSpeed.setFieldText(factor);
    }

    private void applyFastForward() {
        int factor = ltfSimSpeed.getValueAsInt();
        int newFac = scenario.setTimeFactor(factor);
        if (factor != newFac) {
            ltfSimSpeed.setFieldText(newFac);
        }
    }

    // Set sim parameters to this screen
    public void resetFields() {
        // This function can get called before the scenario is ready.
        // We best check for that.
        if (scenario == null) {
            return;
        }
        SimParams params = scenario.simParams;
        ltfNumRandomSurvivors.setFieldText(params.getNumRandomSurvivors());
        ltfNumDrones.setFieldText(params.getNumDrones());
        ltfTimeLimit.setFieldText(params.getTimeLimitSeconds());

        // Set the choicebox
        int index = params.getAlgorithmFlag().toInt();
        cbAlgorithm.getSelectionModel().select(index);
        /*if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD) {
            cbAlgorithm.getSelectionModel().select(1);
        } else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL) {
            cbAlgorithm.getSelectionModel().select(2);
        } else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER) {
            cbAlgorithm.getSelectionModel().select(3);
        } else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.MIX_SRA) {
            cbAlgorithm.getSelectionModel().select(4);
        } else  {
            cbAlgorithm.getSelectionModel().select(0);
        }*/

        ltfSimSpeed.setFieldText(scenario.getTimeFactor());
    }

    public void begin() {
        // Pull from fields
        apply();

        // Then do all other stuff
        sim.stop();
        sim.reset(); //this calls scenario.deployAll();
        sim.start();
    }

    public void performAllRuns() {
        //DataCompiler.compileData();
        /*
        // Pull from fields
        apply();

        // Call into the sim runner to start the sim, this will do our automatic
        // runs so this isn't to be used for experimental stuff generated by
        // the GUI, this currently just runs our hard-coded scenarios
        sim.runner.startRuns();*/

        // So we don't pull from fields, we have specific parameters we want to do when
        // we use this button
        sim.runner.startRuns();
    }
}