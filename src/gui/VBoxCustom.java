package dronelab.gui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public abstract class VBoxCustom extends VBox {
    protected DroneLab sim = null;
    protected Scenario scenario = null;
    protected Text strTitle;

    public VBoxCustom(DroneLab s) {
        super();
        sim = s;
        setScenario(sim.scenario);

        strTitle = new Text();
        setPadding(new Insets(8));
        setSpacing(8);
        strTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        //getChildren().add(strTitle);

        createControls();
        create();
//-fx-background-color: #336699;
        setStyle("-fx-padding: 5;" + 
                 "-fx-border-style: solid inside;" + 
                 "-fx-border-width: 2;" +
                 "-fx-border-insets: 5;" + 
                 "-fx-border-radius: 5;" + 
                 "-fx-border-color: #336699;");
    }

    public void setScenario(Scenario s) {
        scenario = s;
    }

    public Drone drone() {
        if (scenario == null) {
            return null;
        }
        return scenario.drone;
    }

    // All children classes must implement the create method
    public abstract void createControls();
    protected abstract void create();
    public abstract void update(); // Called continously
    public abstract void updateOnDemand(); // Called on demand only
    public abstract void updateChoiceBoxText();
    public abstract void clear();
    public abstract void setLanguage(Constants.Language lang);
}
