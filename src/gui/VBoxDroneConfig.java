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
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class VBoxDroneConfig extends VBoxCustom {

    ChoiceBox cbDroneType;
    Label lblDroneType;
    Label lblSpeed;
    Label lblAcc;
    Label lblSonar;
    Label lblIr;
    Label lblWifi;
    //Label lblBattery;
    TextField speedTf;
    TextField accTf;
    TextField sonarTf;
    TextField irTf;
    TextField wifiTf;
    //TextField batteryTf;
    LabelTextField batteryLtf;
    Button btnApply;

    public VBoxDroneConfig(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        cbDroneType = new ChoiceBox();
        lblDroneType = new Label();
        lblSpeed = new Label();
        lblAcc = new Label();
        lblSonar = new Label();
        lblIr = new Label();
        lblWifi = new Label();
	    //lblBattery = new Label();
        speedTf = new TextField();
        accTf = new TextField();
        sonarTf = new TextField();
        irTf = new TextField();
        wifiTf = new TextField();
        //batteryTf = new TextField();
        btnApply = new Button();

        batteryLtf = new LabelTextField();

        int prefWidth = 70;
        speedTf.setPrefWidth(prefWidth);
        accTf.setPrefWidth(prefWidth);
        sonarTf.setPrefWidth(prefWidth);
        irTf.setPrefWidth(prefWidth);
        wifiTf.setPrefWidth(prefWidth);
        batteryLtf.setFieldPrefWidth(prefWidth);
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                // Drone Config
                strTitle.setText("Drone Configuration");
                lblDroneType.setText("Drone Type:");
                lblSpeed.setText("Max Speed (m/s):");
                lblAcc.setText("Acceleration (seconds to max):");
                lblSonar.setText("Sonar Range (m):");
                lblIr.setText("Camera Range (m):");
                lblWifi.setText("Wi-Fi Range (m):");
                //lblBattery.setText("Battery Life (min):");
                batteryLtf.setLabelText("Battery Life (min):");
                btnApply.setText("Apply");
                break;

            case JAPANESE:
                // Drone Config
                strTitle.setText("ドローン設定");
                lblDroneType.setText("ドローンタイプ:");
                lblSpeed.setText("最高速度:");
                lblAcc.setText("加速度:");
                lblSonar.setText("ソナーレンジ:");
                lblIr.setText("カメラレンジ:");
                lblWifi.setText("Wi-Fi レンジ:");
                batteryLtf.setLabelText("Battery Life (min):");
                btnApply.setText("適用する");
                break;

            case BOTH:
                strTitle.setText("ドローン設定 Drone Config");
                lblDroneType.setText("ドローンタイプ Drone Type:");
                lblSpeed.setText("最高速度 Max Speed:");
                lblAcc.setText("加速度 Acceleration:");
                lblSonar.setText("ソナーレンジ Sonar Range:");
                lblIr.setText("カメラレンジ Camera Range:");
                lblWifi.setText("Wi-Fi Range:");
                batteryLtf.setLabelText("Battery Life (min):");
                btnApply.setText("適用する Apply");
                break;
        }
    }

    public ObservableList<String> createDroneTypeList() {
        ObservableList<String> items = null;

        items = FXCollections.observableArrayList(Constants.STR_DRONE_TYPES);
        /*switch (sim.language) {
            case ENGLISH:
                items = FXCollections.observableArrayList(Constants.STR_DRONE_TYPES);
                break;
            case JAPANESE:
                items = FXCollections.observableArrayList(STR_DRONE_TYPES);
                break;
            case BOTH:
                items = FXCollections.observableArrayList(
                    Constants.STR_DJI_PHANTOM_3_J + " " + Constants.STR_DJI_PHANTOM_3, 
                    Constants.STR_DJI_PHANTOM_4_J + " " + Constants.STR_DJI_PHANTOM_4);
                break;
        }*/

        return items;
    }

    //public void applyDroneSettings(Drone drone) {
    public void applyDroneSettings() {
        for (Drone drone : scenario.drones) {

            double speed = Utils.tryParseDouble(speedTf.getText());
            drone.setMaxSpeed(Distance.pixelsPerFrameFromMetersPerSecond(speed));

            double accSeconds = Utils.tryParseDouble(accTf.getText());
            // acc is seconds to max speed.  60 seconds per frame, so 2 seconds
            // would be 120 frames.  The max speed is in pixels per frame, so we
            // just do the same here.
            double accRate = Distance.accPixelsPerFrameFromSeconds(accSeconds, drone.getMaxSpeed());
            drone.setMaxAccelerationRate(accRate);

            drone.setMaxVerticalAccelerationRate(drone.getMaxAccelerationRate());

            if (drone.ss != null) {
                double val = Utils.tryParseDouble(sonarTf.getText());
                //drone.ss.range = Distance.pixelsFromMeters(val);
                drone.ss.setRangeMeters(val);
            }
            if (drone.cam != null) {
                double val = Utils.tryParseDouble(irTf.getText());
                //drone.irCam.range = Distance.pixelsFromMeters(val);
                drone.cam.setRangeMeters(val);
            }
            if (drone.wifi != null) {
                double val = Utils.tryParseDouble(wifiTf.getText());
                //drone.wifi.range = Distance.pixelsFromMeters(val);
                drone.wifi.setRangeMeters(val);
            }
            drone.setBatteryLifeMinutes(batteryLtf.getValueAsLong());
        }
	    sim.signalRedraw();
    }

    @Override
    public void clear() {
        speedTf.setText("");
        accTf.setText("");
        sonarTf.setText("");
        irTf.setText("");
        wifiTf.setText("");
        batteryLtf.setFieldText("");
    }

    @Override
    public void update() {
        // We dont do anything continuously here right now
    }

    @Override
    public void updateOnDemand() {
        Drone drone = drone();
        if (drone == null) {
            clear();
            return;
        }
        // We should probably define these in an easier to use format, like meters
        // per second.
        double speed = Distance.metersPerSecondFromPixelsPerFrame(drone.getMaxSpeed());
        speedTf.setText("" + Utils.round2Decimals(speed));
        
        double accSeconds = Distance.accSecondsFromPixelsPerFrame(drone.getMaxAccelerationRate(), drone.getMaxSpeed());
        accTf.setText("" + Utils.round2Decimals(accSeconds));
        if (drone.ss != null) {
            sonarTf.setText("" + Math.round(Distance.metersFromPixels(drone.ss.getRange())));
        }
        if (drone.cam != null) {
            irTf.setText(""+ Math.round(Distance.metersFromPixels(drone.cam.getRange())));
        }
        if (drone.wifi != null) {
            wifiTf.setText(""+ Math.round(Distance.metersFromPixels(drone.wifi.getRangePixels())));
        }
        batteryLtf.setFieldText(""+ Math.round(drone.getBatteryLifeMinutes()));
    }

    @Override
    public void updateChoiceBoxText() {
        ObservableList<String> items = createDroneTypeList();
        cbDroneType.setItems(items);

        if (drone() == null) {
            return;
        }
        // Now set it to the correct one
        cbDroneType.getSelectionModel().select(drone().getDroneType().getValue());
    }

    @Override
    protected void create() {

        speedTf.setTooltip(new Tooltip("The drone's maximum speed in meters per second."));
        accTf.setTooltip(new Tooltip("The number of seconds it takes for the drone to accelerate from 0 to max speed."));
        sonarTf.setTooltip(new Tooltip("The range of the sonar sensor in meters."));
        irTf.setTooltip(new Tooltip("The range of the camera in meters."));
        wifiTf.setTooltip(new Tooltip("The range of the WiFi in meters."));
        batteryLtf.setTooltip(new Tooltip("The number of minutes until the battery dies."));

	    this.getChildren().add(GuiUtils.addLabelChoiceHBox(lblDroneType, cbDroneType, Pos.CENTER_LEFT));

        updateChoiceBoxText();
        cbDroneType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //processChangeDroneType(drone(), newValue);
                scenario.processChangeDroneType(newValue);
                sim.updateDroneParameterFields();
            }
	    });
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblSpeed, speedTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblAcc, accTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblSonar, sonarTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblIr, irTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblWifi, wifiTf));
	    this.getChildren().add(batteryLtf);

        btnApply.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                applyDroneSettings();
            }
        });

	    this.getChildren().add(btnApply);
    }
}