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
import dronelab.collidable.behaviors.*;
import dronelab.utils.*;


public class VBoxBehavior extends VBoxCustom {

    ChoiceBox cbAddBehavior;
    ChoiceBox cbRemoveBehavior;
    //TextArea behaviorTa = new TextArea();
    Label lblBehaviorText;
    Label lblAddBehavior;
    Label lblRemoveBehavior;
    Label lblBehaviorModules;
    Label lblActiveBehavior;
    Label lblActiveBehaviorText;

    public VBoxBehavior(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        cbAddBehavior = new ChoiceBox();
        cbRemoveBehavior = new ChoiceBox();
        // behaviorTa = new TextArea();
        lblBehaviorText = new Label();
        lblBehaviorText.setTextFill(Color.DARKGREEN);
        lblAddBehavior = new Label();
        lblRemoveBehavior = new Label();
        lblBehaviorModules = new Label();
        lblActiveBehavior = new Label();
        lblActiveBehaviorText = new Label();
    }

    void addModItemString(ObservableList<String> items, BehaviorModule mod, boolean bHas, String str) {
        if ((bHas && mod != null) || (bHas == false && mod == null)) {
            items.add(str);
        }
    }

    public boolean hasBehavior(Drone d, String name) {
        for (BehaviorModule mod : d.behaviors) {
            if (mod.getName() == name) {
                return true;
            }
        }
        return false;
    }

    public void addNonexistentBehavior(ObservableList<String> items, Drone d, String name, String strAdd) {
        if (hasBehavior(d, name) == false) {
            items.add(strAdd);
        }
    }

    // Create a list of behaviors based on what the drone has or doesn't have
    public ObservableList<String> createBehaviorList(Drone d, boolean bHas) {
        ObservableList<String> items = FXCollections.observableArrayList();

        if (d == null) {
            return items;
        }

        switch (sim.language) {
            case ENGLISH:
                if (bHas == true) {
                    for (BehaviorModule mod : d.behaviors) {
                        items.add(mod.getName());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
                        addNonexistentBehavior(items, d, Constants.STR_BEHAVIORS[i], 
                            Constants.STR_BEHAVIORS[i]);
                    }
                }
	            break;
            case JAPANESE:
                if (bHas == true) {
                    for (BehaviorModule mod : d.behaviors) {
                        items.add(mod.getNameJapanese());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
                        addNonexistentBehavior(items, d, Constants.STR_BEHAVIORS[i], 
                            Constants.STR_BEHAVIORS_J[i]);
                    }
                }
	            break;
            case BOTH:
                if (bHas == true) {
                    for (BehaviorModule mod : d.behaviors) {
                        items.add(mod.getNameJapanese() + " " + mod.getName());
                    }
                }
                else {
                    for (int i = 0; i < Constants.STR_BEHAVIORS.length; i++) {
                        addNonexistentBehavior(items, d, Constants.STR_BEHAVIORS[i], 
                            Constants.STR_BEHAVIORS_J[i] + " " + Constants.STR_BEHAVIORS[i]);
                    }
                }
	            break;
        }

        return items;
    }

    @Override
    public void clear() {
        lblBehaviorText.setText("");
        cbAddBehavior.setItems(FXCollections.observableArrayList());
        cbRemoveBehavior.setItems(FXCollections.observableArrayList());
    }

    @Override
    public void update() {
        Drone drone = drone();

        if (drone == null) {
            return;
        }
        if (drone.lastActiveModule == null) {
            lblActiveBehaviorText.setText("");
        }
        lblActiveBehaviorText.setText(drone.lastActiveModule.getName());
    }

    @Override
    public void updateOnDemand() {
        String text = "";
        int prefHeight = 0;
        int rowHeight = 25;
        Drone drone = drone();

        if (drone == null) {
            clear();
            return;
        }

        int pgDownAt = 3;
        int count = 0;
        for (BehaviorModule mod : drone.behaviors) {
            if (count == pgDownAt) {
                count = 0;
                prefHeight += rowHeight;
                text += "\n";
            }

            text += "" + mod.getName();
            if (mod != drone.behaviors.get(drone.behaviors.size()-1)) {
                text += ", ";
            }
            count++;
        }
        /*if (drone.av != null) {
            if (text != "")
                text += ", ";
            text += "" + drone.av.name; // + "\n";
            prefHeight += rowHeight;
        }*/

        lblBehaviorText.setPrefHeight(prefHeight);
        lblBehaviorText.setText(text);

        updateChoiceBoxText();
        
        // This doesnt seem to work
        //lblActiveBehaviorText.textProperty().bind(new SimpleStringProperty(drone().lastActiveBehaviorName));
    }

    @Override
    public void updateChoiceBoxText() {
       // ObservableList<String> items = createBehaviorList();
        Drone drone = drone();
        cbAddBehavior.setItems(createBehaviorList(drone, false));
        cbRemoveBehavior.setItems(createBehaviorList(drone, true));
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                strTitle.setText("Behavior Config:");
                lblActiveBehavior.setText("Active:");
                lblAddBehavior.setText("Add:");
                lblRemoveBehavior.setText("Remove:");
                lblBehaviorModules.setText("Behaviors:");
                break;

            case JAPANESE:
                strTitle.setText("行動:");
                lblActiveBehavior.setText("アクティブ:");
                lblAddBehavior.setText("追加する:");
                lblRemoveBehavior.setText("除去する:");
                lblBehaviorModules.setText("行動:");
                break;

            case BOTH:
                strTitle.setText("行動 Behavior Config:");
                lblActiveBehavior.setText("アクティブ Active:");
                lblAddBehavior.setText("追加する Add:");
                lblRemoveBehavior.setText("除去する Remove:");
                lblBehaviorModules.setText("行動 Behaviors:");
                break;
        }
    }

    @Override
    protected void create() {
        HBox hb0 = new HBox();
        hb0 = GuiUtils.addLabelTextHBox(lblActiveBehavior, lblActiveBehaviorText);
        hb0.setPrefHeight(lblActiveBehaviorText.getPrefHeight());
        hb0.setAlignment(Pos.CENTER_LEFT);
	    this.getChildren().add(hb0);

        //cbAddBehavior = new ChoiceBox(FXCollections.observableArrayList(
         //   Constants.STR_AVOID, Constants.STR_SEEK, Constants.STR_WANDER)
        //);
        cbAddBehavior.setPrefSize(10, 10);
        cbAddBehavior.setTooltip(new Tooltip("Select the behavior to be added"));
        //cbAddBehavior.setStyle("-fx-selected-mark-color: red;");
        cbAddBehavior.getSelectionModel().selectFirst();

        cbAddBehavior.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //processAddBehavior(drone(), newValue);
                if (newValue == null) {
                    return;
                }
                scenario.processAddBehavior(newValue);
                cbAddBehavior.getSelectionModel().clearSelection();
                // Clear value of ComboBox because clearSelection() does not do it
                cbAddBehavior.setValue(null);
            }
	    });

        //cbRemoveBehavior = new ChoiceBox(FXCollections.observableArrayList(
          //  Constants.STR_AVOID, Constants.STR_SEEK, Constants.STR_WANDER)
        //);

        cbRemoveBehavior.setPrefSize(10, 10);
        cbRemoveBehavior.setTooltip(new Tooltip("Select the behavior to be removed"));
        //cbRemoveBehavior.setStyle("-fx-mark-color: transparent;");
        //cbRemoveBehavior.setStyle("-fx-selected-mark-color: transparent;");
        //cbRemoveBehavior.getSelectionModel().setStyle("-fx-selected-mark-color: transparent;");
        cbRemoveBehavior.getSelectionModel().selectFirst();

        cbRemoveBehavior.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
	            //processRemoveBehavior(drone(), newValue);
                if (newValue == null) {
                    return;
                }
                scenario.processRemoveBehavior(newValue);
                cbRemoveBehavior.getSelectionModel().clearSelection();
                // Clear value of ComboBox because clearSelection() does not do it
                cbRemoveBehavior.setValue(null);
            }
	    });


        HBox hb = new HBox();
        hb.getChildren().addAll(lblAddBehavior, cbAddBehavior);
        hb.setSpacing(5);
        hb.setAlignment(Pos.CENTER_LEFT);

        HBox hb2 = new HBox();
        hb2.getChildren().addAll(lblRemoveBehavior, cbRemoveBehavior);
        hb2.setSpacing(5);
        hb2.setAlignment(Pos.CENTER_LEFT);

        HBox hb3 = new HBox();
        hb3.setSpacing(30);
        hb3.setAlignment(Pos.CENTER_LEFT);
        hb3.getChildren().addAll(hb, hb2);

        this.getChildren().add(hb3);

        hb = GuiUtils.addLabelTextHBox(lblBehaviorModules, lblBehaviorText);
        hb.setPrefHeight(lblBehaviorText.getPrefHeight());
        hb.setAlignment(Pos.CENTER_LEFT);
	    this.getChildren().add(hb);


        
	    //vbox.getChildren().add(addLabelChoiceHBox(lblAddBehavior, cbAddBehavior));
	    //vbox.getChildren().add(addLabelChoiceHBox(lblRemoveBehavior, cbRemoveBehavior));
       // System.out.println(cbRemoveBehavior.getStyle());

        //behaviorTa.setPrefRowCount(2); 
        //behaviorTa.setFitToWidth(100);
        /*VBox box = new VBox();
        box.setAlignment(Pos.TOP_LEFT);
        box.getChildren().add(lblBehaviorModules);
        box.getChildren().add(lblBehaviorText);
	    this.getChildren().add(box);*/

        updateChoiceBoxText();
    }
}