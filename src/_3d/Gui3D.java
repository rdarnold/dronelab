package dronelab._3d;

import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import dronelab.Scenario;

public class Gui3D extends BorderPane {
    private Scenario scenario;
    private PerspectiveViewer viewer;
    CheckBox cbLock;
    CheckBox cbLight;

    public Gui3D(PerspectiveViewer newViewer) {
        super();
        // We want this pane to be transparent so we can click
        // through it to the 3d scene behind.
        setPickOnBounds(false);
        // The viewer in return calls setViewer back at us.
        newViewer.setGui(this);
        create();
    }

    public void setCheckLock(boolean check) {
        cbLock.setSelected(check);
    }
    public void setCheckLight(boolean check) {
        cbLight.setSelected(check);
    }

    private void create() {
        VBox box = new VBox();
        box.setPickOnBounds(false);
        box.setPadding(new Insets(8));
        box.setSpacing(8);

        cbLock = new CheckBox("Lock");
        setCheckLock(true);
        cbLock.setPrefWidth(100);
        cbLock.setPrefHeight(25);
        box.getChildren().add(cbLock);

        cbLight = new CheckBox("Light");
        setCheckLight(true);
        cbLight.setPrefWidth(100);
        cbLight.setPrefHeight(25);
        box.getChildren().add(cbLight);

        setupCheckBoxListeners();

        setLeft(box);
    }

    public void setViewer(PerspectiveViewer newViewer) {
        scenario = newViewer.scenario;
        viewer = newViewer;
    }

    private void setupCheckBoxListeners() {
        cbLock.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                viewer.processLockTarget(new_val);
            }
        });

        cbLight.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                viewer.processDroneLight(new_val);
            }
        });
    }
}