package dronelab.gui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
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
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.text.DecimalFormat;

import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class VBoxCurrentData extends VBoxCustom {

    // Current Data
    private Text debugText;

    private double m_fLastSeenPercent = 0;
    private double m_fLastLocatedPercent = 0;
    
    ListView<HBox> benchmarkSeenList;
    ListView<HBox> benchmarkLocatedList;

    // We keep track of the times we reached 90% on both measures, this is not
    // really a great place to do this but I'm under the gun
    int seen90PercSeconds = 0;
    int located90PercSeconds = 0;
    public int getSeen90PercSeconds() { return seen90PercSeconds; }
    public int getLocated90PercSeconds() { return located90PercSeconds; }

    //Text fpsText;
    Label lblPos;
    Label lblHeading;
    Label lblSeeking;
    Label lblHeight;
    Label lblBattery;
    Label lblDamage;
    Label lblLocated;
    Label lblSeen;
    TextField posTf;
    TextField seekingTf;
    TextField headingTf;
    TextField heightTf;
    TextField batteryTf;
    TextField damageTf;
    TextField seenTf;
    TextField locatedTf;

    public VBoxCurrentData(DroneLab s) {
        super(s);
    }

    @Override
    public void createControls() {
        //fpsText = new Text();

        lblPos = new Label();
        lblHeading = new Label();
        lblSeeking = new Label();
        lblHeight = new Label();
        lblBattery = new Label();
        lblDamage = new Label();
        lblSeen = new Label();
        lblLocated = new Label();

        posTf = new TextField();
        seekingTf = new TextField();
        headingTf = new TextField();
        heightTf = new TextField();
        batteryTf = new TextField();
        damageTf = new TextField();
        seenTf = new TextField();
        locatedTf = new TextField();

        benchmarkSeenList = new ListView<HBox>();
        benchmarkLocatedList = new ListView<HBox>();
    }

    @Override
    public void setLanguage(Constants.Language lang) {
        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                strTitle.setText("Current Data");
                lblPos.setText("Position:");
                lblHeading.setText("Heading:");
                lblSeeking.setText("Seeking:");
                lblHeight.setText("Height:");
                lblBattery.setText("Battery Life:");
                lblDamage.setText("Condition:");
                lblSeen.setText("Camera Coverage:");
                lblLocated.setText("FINDER Coverage:");
                break;

            case JAPANESE:
                strTitle.setText("現在のデータ:");
                lblPos.setText("ポジション:");
                lblHeading.setText("方向:");
                lblSeeking.setText("シーク:");
                lblHeight.setText("高さ:");
                lblBattery.setText("電池寿命:");
                lblDamage.setText("Condition:");
                lblSeen.setText("Camera Coverage:");
                lblLocated.setText("FINDER Coverage:");
                break;

            case BOTH:
                strTitle.setText("現在のデータ Current Data");
                lblPos.setText("ポジション Position:");
                lblHeading.setText("方向 Heading:");
                lblSeeking.setText("シーク Seeking:");
                lblHeight.setText("高さ Height:");
                lblBattery.setText("電池寿命 Battery Life:");
                lblDamage.setText("Condition:");
                lblSeen.setText("Camera Coverage:");
                lblLocated.setText("FINDER Coverage:");
                break;
        }
    }

    //public void updateFpsText(int newFps) {
      //  fpsText.setText("" + newFps);
    //}

    @Override
    public void clear() {
        seekingTf.setText("");
        posTf.setText("");
        headingTf.setText("");
        heightTf.setText("");
        batteryTf.setText("");
        damageTf.setText("");
        seenTf.setText("");
        locatedTf.setText("");
        resetBenchmarkLists();
        m_fLastSeenPercent = 0;
        m_fLastLocatedPercent = 0;
    }

    @Override
    public void updateChoiceBoxText() {
    }    

    private void addBenchmark(ListView<HBox> list, Text text) {
        HBox hbox = new HBox();
        hbox.getChildren().add(text);
        list.getItems().add(hbox);
    }

    private boolean checkOneBenchmark(ListView<HBox> list, double percCheck, double currentPerc, double lastPerc) {
        if (currentPerc >= percCheck && lastPerc < percCheck) {
            Text percText = new Text("" + (int)percCheck + "%: " + scenario.simTime.toString());
            addBenchmark(list, percText);
            String data = (String)list.getUserData();
            list.setUserData(data + scenario.simTime.toString() + " ");

            // Keep track of when/if we hit 90%; these really should just be arrays and we record
            // the seconds for every benchmark
            if (list.equals(benchmarkSeenList) == true && ((int)percCheck == 90)) {
                seen90PercSeconds = scenario.simTime.getTotalSeconds();
            }
            else if (list.equals(benchmarkLocatedList) == true && ((int)percCheck == 90)) {
                located90PercSeconds = scenario.simTime.getTotalSeconds();
            }
            return true;
        }
        return false;
    }

    private void checkSeenBenchmarks() {
        double perc = 100.0 * (double)scenario.getNumVictimsSeen() / (double)scenario.getNumVictims();
        // No increase, do nothing.
        if (perc <= m_fLastSeenPercent) { 
            return;
        }

        for (int i = 10; i <= 100; i += 10) {
            //Utils.log("BLARGE: " + i);
            if (checkOneBenchmark(benchmarkSeenList, i, perc, m_fLastSeenPercent) == true) {
                // Remove this later if we want the FINDER completness to be able to get up
                // to 90% too; this makes it so that only camera can go up to 90%, and then
                // once that happens, the sim will stop there
                if (sim.signalPercentComplete(i) == true) {
                    return;
                }
            }
        }

        m_fLastSeenPercent = perc;
    }

    private void checkLocatedBenchmarks() {
        double perc = 100.0 * (double)scenario.getNumVictimsLocated() / (double)scenario.getNumVictims();
        // No increase, do nothing.
        if (perc <= m_fLastLocatedPercent) {
            return;
        }

        for (int i = 10; i <= 100; i += 10) {
            if (checkOneBenchmark(benchmarkLocatedList, i, perc, m_fLastLocatedPercent) == true) {
                /*if (sim.signalPercentComplete(i) == true) {
                    return;
                }*/
            }
        }

        m_fLastLocatedPercent = perc;
    }

    private DecimalFormat m_df1 = new DecimalFormat("0.0");
    private void setPersonsSeenText() {
        String text = "" + m_df1.format(m_fLastSeenPercent) + "%";
        text += "  (" + scenario.getNumVictimsSeen() + "/" + scenario.getNumVictims() + ")";
        seenTf.setText(text);
    }

    private void setPersonsLocatedText() {
        String text = "" + m_df1.format(m_fLastLocatedPercent) + "%";
        text += "  (" + scenario.getNumVictimsLocated() + "/" + scenario.getNumVictims() + ")";
        locatedTf.setText(text);
    }
    // When we need to update the fields, we get notified that an update is needed
    @Override
    public void update() {
        Drone drone = drone();
        if (drone == null) {
            clear();
            return;
        }
        seekingTf.setText("X: " + Math.round(drone.getTargetX()) + ", Y: " + Math.round(drone.getTargetY()));
        posTf.setText("X: " + Math.round(drone.x()) + ", Y: " + Math.round(drone.y()));
        headingTf.setText("" + Math.round(Utils.normalizeAngle(drone.getHeadingDegrees())) + "°");
        heightTf.setText("" + Utils.round2Decimals(Distance.metersFromPixels(drone.getElevation())) + "m");
        batteryTf.setText("" + (drone.getBatteryLifeMinutes()) + " minutes");
        damageTf.setText("" + Math.ceil(100.0 * ((double)drone.getHealth() / (double)drone.getMaxHealth())) + "%");
        
        checkSeenBenchmarks();
        checkLocatedBenchmarks();
        
        // Little more complicated so this is separated out.
        setPersonsSeenText();
        setPersonsLocatedText();
    }

    @Override
    public void updateOnDemand() {
        // We dont do anything on demand here right now
    }

    public void resetBenchmarkLists() {
        seen90PercSeconds = 0;
        located90PercSeconds = 0;

        benchmarkSeenList.getItems().clear();
        benchmarkLocatedList.getItems().clear();
        
        // Makes copy/pasting easy, we just kind of "write whatever we want"
        // in the user data so we can copy it out.
        benchmarkSeenList.setUserData(new String(""));
        benchmarkLocatedList.setUserData(new String(""));

        Text text = new Text("Camera Progress");
        text.setStyle("-fx-font-weight: bold"); 
        //addBenchmark(benchmarkSeenList, text);
        HBox hbox;
        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(text);
        benchmarkSeenList.getItems().add(hbox);

        text = new Text("FINDER Progress");
        text.setStyle("-fx-font-weight: bold"); 
        //addBenchmark(benchmarkLocatedList, text);
        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(text);
        benchmarkLocatedList.getItems().add(hbox);

        Button btn = new Button("Copy");
        btn.setPrefWidth(100);
        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(btn);
        benchmarkSeenList.getItems().add(hbox);
        btn.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                copyToClipboard(benchmarkSeenList);
            }
        });

        btn = new Button("Copy");
        btn.setPrefWidth(100);
        hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().add(btn);
        benchmarkLocatedList.getItems().add(hbox);
        btn.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                copyToClipboard(benchmarkLocatedList);
            }
        });
    }

    @Override
    protected void create() {
        GuiUtils.disableTextField(posTf);
        GuiUtils.disableTextField(headingTf); 
        GuiUtils.disableTextField(seekingTf); 
        GuiUtils.disableTextField(heightTf); 
        GuiUtils.disableTextField(batteryTf); 
        GuiUtils.disableTextField(damageTf); 
        GuiUtils.disableTextField(seenTf); 
        GuiUtils.disableTextField(locatedTf); 
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblPos, posTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblHeading, headingTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblSeeking, seekingTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblHeight, heightTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblBattery, batteryTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblDamage, damageTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblSeen, seenTf));
	    this.getChildren().add(GuiUtils.addLabelTextHBox(lblLocated, locatedTf));

        benchmarkSeenList.setPrefWidth(120);
        benchmarkLocatedList.setPrefWidth(120);

        resetBenchmarkLists();

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
	    hbox.getChildren().add(benchmarkSeenList);
	    hbox.getChildren().add(benchmarkLocatedList);
	    this.getChildren().add(hbox);
        //benchmarkList.setMinWidth(checkButtonWidth);
        //benchmarkList.setPrefWidth(checkButtonWidth);
        //benchmarkList.setFixedCellSize(rowHeight);
        //benchmarkList.setPrefHeight(benchmarkList.getItems().size() * rowHeight + hgtAdd);
        //benchmarkList.setItems(buildAnswerList());

        //Label debugLabel = new Label("デバッグ Debug:");
        //debugText = new Text();
	    //vbox.getChildren().add(addLabelTextOnlyHBox(debugLabel, debugText));
    }

    public void copyToClipboard(ListView<HBox> list) {
        String str = (String)list.getUserData();
        final ClipboardContent content = new ClipboardContent();
        content.putString(str);
        Clipboard.getSystemClipboard().setContent(content);
    }

    public String getSeenBenchmarkData() {
        return (String)benchmarkSeenList.getUserData();
    }

    public String getLocatedBenchmarkData() {
        return (String)benchmarkLocatedList.getUserData();
    }
    public String getMaxSeen() {
        return seenTf.getText();
    }
    
    public String getMaxLocated() {
        return locatedTf.getText();
    }
}