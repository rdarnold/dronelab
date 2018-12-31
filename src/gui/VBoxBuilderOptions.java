package dronelab.gui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
/*import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Toggle;
import javafx.scene.control.ChoiceBox;*/
import javafx.scene.control.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;

import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.EnvironmentGenerator;
import dronelab.collidable.*;
import dronelab.utils.*;

public class VBoxBuilderOptions extends VBoxCustom {

    RadioButton rbBuildings;
    RadioButton rbVictims;
    RadioButton rbDrones;
    RadioButton rbDeployment;

    RadioButton rbCircle;
    RadioButton rbRectangle;
    RadioButton rbPolygon;

    RadioButton rbSelect;
    RadioButton rbBuild;
    RadioButton rbDelete;

    Button btnDelete;
    Button btnUndo;
    Button btnSave;

    // Distressed persons area
    Text distressedPersonsText;
    LabelTextField minPersons;
    LabelTextField maxPersons;
    ChoiceBox cbPersonDistribution;
    Text distributionText;
    Button btnDeleteAllPersons;
    Button btnGeneratePersons;

    private static final int btn_wid = 100;


    public VBoxBuilderOptions(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        rbBuildings = new RadioButton();
        rbVictims = new RadioButton();
        rbDrones = new RadioButton();
        rbDeployment = new RadioButton();

        rbCircle = new RadioButton();
        rbRectangle = new RadioButton();
        rbPolygon = new RadioButton();

        rbSelect = new RadioButton();
        rbBuild = new RadioButton();
        rbDelete = new RadioButton();

        btnDelete = new Button();
        btnUndo = new Button();
        btnSave = new Button();
        btnDelete.setPrefWidth(btn_wid);
        btnUndo.setPrefWidth(btn_wid);
        btnSave.setPrefWidth(btn_wid);

        // Generator stuff
        distressedPersonsText = new Text();
        minPersons = new LabelTextField();
        maxPersons = new LabelTextField();
        cbPersonDistribution = new ChoiceBox();
        distributionText = new Text();
        btnDeleteAllPersons = new Button();
        btnGeneratePersons = new Button();

        setLanguage(Constants.Language.ENGLISH);
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                strTitle.setText("Build Options");

                rbBuildings.setText("Buildings");
                rbVictims.setText("Victims");
                rbDrones.setText("Drones");
                rbDeployment.setText("Rescue Deployment");

                rbCircle.setText("Circle");
                rbRectangle.setText("Rectangle");
                rbPolygon.setText("Polygon");

                rbBuild.setText("Build");
                rbSelect.setText("Select");
                rbDelete.setText("Delete");

                btnDelete.setText("Delete");
                btnUndo.setText("Undo");
                btnSave.setText("Save");

                distressedPersonsText.setText("Distressed Persons");
                minPersons.setLabelText("Min:");
                maxPersons.setLabelText("Max:");
                distributionText.setText("Distribution: ");
                btnDeleteAllPersons.setText("Delete All");
                btnGeneratePersons.setText("Generate");
                break;

            case JAPANESE:
                btnSave.setText("セーブ");
                break;

            case BOTH:
                btnSave.setText("セーブ Save");
                break;
        }
    }

    //public void updateFpsText(int newFps) {
      //  fpsText.setText("" + newFps);
    //}

    @Override
    public void clear() {
        if (scenario == null || scenario.builder == null) {
            return;
        }

        switch (scenario.builder.getBuildType()) {
            case BUILDINGS:
                rbBuildings.setSelected(true);
                break;
            case VICTIMS:
                rbVictims.setSelected(true);
                break;
            case DRONES:
                rbDrones.setSelected(true);
                break;
            case DEPLOYMENT:
                rbDeployment.setSelected(true);
                break;
        }

        switch (scenario.builder.getBuildingShape()) {
            case CIRCLE:
                rbCircle.setSelected(true);
                break;
            case RECT:
                rbRectangle.setSelected(true);
                break;
            case POLYGON:
                rbPolygon.setSelected(true);
                break;
        }

        switch (scenario.builder.getMode()) {
            case SELECT:
                rbSelect.setSelected(true);
                break;
            case BUILD:
                rbBuild.setSelected(true);
                break;
            case DELETE:
                rbDelete.setSelected(true);
                break;
        }
    }

    @Override
    public void updateChoiceBoxText() {
    }    

    // When we need to update the fields, we get notified that an update is needed
    @Override
    public void update() {
    }

    @Override
    public void updateOnDemand() {
    }

    @Override
    protected void create() {
        addRadioButtons();
        addGeneratorArea();
    }

    private VBox createSubSectionVBox() {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(8));
        vbox.setSpacing(8);
        vbox.setStyle("-fx-padding: 5;" + 
                    "-fx-border-style: solid inside;" + 
                    "-fx-border-width: 2;" +
                    "-fx-border-insets: 5;" + 
                    "-fx-border-radius: 5;" + 
                    "-fx-border-color: #336699;");
        return vbox;
    }

    public void setBuildType(Constants.BuildType type) {
        if (scenario != null && scenario.builder != null) {
            scenario.builder.setBuildType(type);
        }
    }

    public void setBuildingShape(Constants.ShapeType type) {
        if (scenario != null && scenario.builder != null) {
            scenario.builder.setBuildingShape(type);
        }
    }

    public void setMode(Constants.BuildMode mode) {
        if (scenario != null && scenario.builder != null) {
            scenario.builder.setMode(mode);
        }
    }

    public ObservableList<String> createDistributionList() {
        ObservableList<String> items = FXCollections.observableArrayList();

        for (EnvironmentGenerator.PersonDistribution distro : 
                EnvironmentGenerator.PersonDistribution.values()) {
            items.add(distro.name());
        }

        return items;
    }

    public void addGeneratorArea() {
        // So we have, Distressed Persons:
        // Delete All
        // min [ ]  max [ ]
        // Distribution (combo)
        // Generate

        // Set widths
        //minPersons.setLabelPrefWidth(40);
        //maxPersons.setLabelPrefWidth(40);
        minPersons.setFieldPrefWidth(40);
        maxPersons.setFieldPrefWidth(40);
        minPersons.setSpacing(5);
        maxPersons.setSpacing(5);
        btnGeneratePersons.setPrefWidth(btn_wid);
        btnDeleteAllPersons.setPrefWidth(btn_wid);
        cbPersonDistribution.setPrefWidth(btn_wid);

        // Put them all in the right places
        VBox vbox = createSubSectionVBox();
        vbox.setAlignment(Pos.CENTER);
	    vbox.getChildren().add(distressedPersonsText);
	    vbox.getChildren().add(btnDeleteAllPersons);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setSpacing(10);
	    hbox.getChildren().add(minPersons);
	    hbox.getChildren().add(maxPersons);

	    vbox.getChildren().add(hbox);

        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
	    hbox.getChildren().add(distributionText);
	    hbox.getChildren().add(cbPersonDistribution);
	    vbox.getChildren().add(hbox);

	    vbox.getChildren().add(btnGeneratePersons);
	    this.getChildren().add(vbox);
        
        // Do some behavior
        btnGeneratePersons.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                int min = minPersons.getValueAsInt();
                int max = maxPersons.getValueAsInt();
                EnvironmentGenerator.PersonDistribution distro;
                String name = cbPersonDistribution.getSelectionModel().getSelectedItem().toString();
                distro = EnvironmentGenerator.getDistributionForString(name);
                scenario.generator.generateVictims(min, max, distro);
                sim.signalRedraw();
            }
        });

        btnDeleteAllPersons.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                scenario.clearVictims();
                sim.signalRedraw();
            }
        });

        cbPersonDistribution.setItems(createDistributionList());
        cbPersonDistribution.setTooltip(new Tooltip("Select the type of distribution for the generated distressed persons"));
        cbPersonDistribution.getSelectionModel().selectFirst();
    }
    
    public void addRadioButtons() {
        rbBuildings.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildType(Constants.BuildType.BUILDINGS);
            }
        });

        rbVictims.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildType(Constants.BuildType.VICTIMS);
            }
        });

        rbDrones.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildType(Constants.BuildType.DRONES);
            }
        });

        rbDeployment.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildType(Constants.BuildType.DEPLOYMENT);
            }
        });

        rbCircle.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildingShape(Constants.ShapeType.CIRCLE);
            }
        });

        rbRectangle.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildingShape(Constants.ShapeType.RECT);
            }
        });

        rbPolygon.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setBuildingShape(Constants.ShapeType.POLYGON);
            }
        });

        rbBuild.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setMode(Constants.BuildMode.BUILD);
            }
        });

        rbSelect.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setMode(Constants.BuildMode.SELECT);
            }
        });

        rbDelete.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                setMode(Constants.BuildMode.DELETE);
            }
        });


        btnSave.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                scenario.save();
            }
        });

        ToggleGroup group = new ToggleGroup();

        /*group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
        public void changed(ObservableValue<? extends Toggle> ov,
            Toggle old_toggle, Toggle new_toggle) {
                if (group.getSelectedToggle() == rbBuildings) {
                    if (scenario != null && scenario.builder != null) {
                        scenario.builder.setMode(Constants.BuildMode.BUILDINGS);
                    }
                } else {
                    if (scenario != null && scenario.builder != null) {
                        scenario.builder.setMode(Constants.BuildMode.VICTIMS);
                    }
                }
            }
        });*/

        rbBuildings.setToggleGroup(group);
        rbVictims.setToggleGroup(group);
        rbDrones.setToggleGroup(group);
        rbDeployment.setToggleGroup(group);

        group = new ToggleGroup();
        rbCircle.setToggleGroup(group);
        rbRectangle.setToggleGroup(group);
        rbPolygon.setToggleGroup(group);

        group = new ToggleGroup();
        rbBuild.setToggleGroup(group);
        rbSelect.setToggleGroup(group);
        rbDelete.setToggleGroup(group);
        
        VBox vbox = createSubSectionVBox();
	    vbox.getChildren().add(rbBuildings);
	    vbox.getChildren().add(rbVictims);
	    vbox.getChildren().add(rbDrones);
	    vbox.getChildren().add(rbDeployment);
	    this.getChildren().add(vbox);

        vbox = createSubSectionVBox();
	    vbox.getChildren().add(rbCircle);
	    vbox.getChildren().add(rbRectangle);
	    vbox.getChildren().add(rbPolygon);
	    this.getChildren().add(vbox);

        vbox = createSubSectionVBox();
	    vbox.getChildren().add(rbBuild);
	    vbox.getChildren().add(rbSelect);
	    vbox.getChildren().add(rbDelete);
	    this.getChildren().add(vbox);

        vbox = createSubSectionVBox();
	    vbox.getChildren().add(btnDelete);
	    vbox.getChildren().add(btnUndo);
	    vbox.getChildren().add(btnSave);
	    this.getChildren().add(vbox);

        rbBuildings.setSelected(true);
        rbPolygon.setSelected(true);
        rbBuild.setSelected(true);
    }
}