package dronelab;

import java.util.Date;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import javafx.util.Duration;
//import java.awt.Robot; // To move the mouse after scaling the canvas
//import java.awt.AWTException; // To move the mouse after scaling the canvas
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.Animation;
//import javafx.beans.InvalidationListener;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.beans.Observable;
//import javafx.beans.binding.DoubleBinding;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
//import javafx.geometry.VPos;
//import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.Scene;
import javafx.scene.Node;
//import javafx.scene.Group;
//import javafx.scene.transform.Scale;
//import javafx.scene.transform.Translate;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.shape.ArcType;
import javafx.scene.control.Button;
//import javafx.scene.control.Hyperlink;
//import javafx.scene.control.TextField;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.ChoiceBox;
//import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.Tooltip;
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
//import javafx.scene.transform.Transform;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
//import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.TitledPane;
//import javafx.scene.layout.FlowPane;
//import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
//import javafx.scene.layout.TilePane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.DropShadow;
import javafx.concurrent.Task;
import javafx.scene.SubScene;
import javafx.scene.control.Accordion;
 
import dronelab._3d.*;
import dronelab.collidable.*;
import dronelab.gui.*;
import dronelab.utils.*;

/*import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.converter.NumberStringConverter;*/

public class DroneLab extends Application {

    int wid = 1000;
    int hgt = 900;

    public double clickedX = 0;
    public double clickedY = 0;
    public double canvasMouseX = 0;
    public double canvasMouseY = 0;
    public double spMouseX = 0;
    public double spMouseY = 0;
    public double screenMouseX = 0;
    public double screenMouseY = 0;
    public double sceneMouseX = 0;
    public double sceneMouseY = 0;

    // Canvas visible area.
    Rectangle canvasVisibleRect = new Rectangle();
    double canvasVisibleX = 0;
    double canvasVisibleY = 0;
    double canvasVisibleWid = 0;
    double canvasVisibleHgt = 0;

    String strVersion = "v1.2";

    // Are we ctually running anything or not.  Wish we could just
    // check the animation timer.
    boolean running = false;
    public boolean draw = true; // If false, we don't draw, we are grinding sim results

    //used to store the current time to calculate fps
    private long currentTime = 0;
    //used to store the last time to calculate fps
    private long lastTime = 0;
    private long lastUpdate = 0;
    //fps counter
    private int fps = 0;//text to display fps
    //acumulated difference between current time and last time
    private double delta = 0;
    private AnimationTimer mainLoop;

    //private int skipCount = 0;
    //int desiredSkipCount = 0; //1;
    
    // Just a list of the timestamps each survivor was found during this sim run, this is
    // part of our updated simulation for SNA ILIR.  It is a little weird to have it here
    // but this is where we are recording the data for now.  This stuff would be better suited
    // to a separate class.
    private String survivorFoundTimes = "";

    Stage stage;
    Scene scene;

	//Robot robot = null;

    public static Scenario scenario = null;
    public static DroneLab sim = null;

    // A construct simulating a human operator of the swarm
    public static ControllingEntity operator = new ControllingEntity();

    public Constants.Language language = Constants.Language.ENGLISH;

    ////////////////////
    /// GUI controls ///
    ////////////////////

    int canvasWid = 800;//wid - canvasXOffset;
    int canvasHgt = 500;//hgt - canvasYOffset;

    SimCanvas canvas;
    ScalableScrollPane spCanvasContainer;
    BorderPane border = new BorderPane();
    StackPane centerStack = new StackPane();

    SimText displayText = new SimText();

    // Top bar
    Text numRunText = new Text();
    Text fpsText = new Text();
    Text timeText = new Text();
    Button btnStart = new Button();
    //Button btnPause = new Button();
    Button btnReset = new Button();
    Button btnBuild = new Button();
    Button btnPause = new Button();

    // Sim Config 
    ArrayList<VBoxCustom> vBoxCustomList;

    VBox leftVBox = null;
    VBox builderVBox = null;
    VBoxBuilderOptions vBoxBuilderOptions = null;
    VBoxConfiguration vBoxConfiguration = null;
    VBoxDroneConfig vBoxDroneConfig = null;
    VBoxBehavior vBoxBehavior = null;
    VBoxSensor vBoxSensor = null;
    VBoxCurrentData vBoxCurrentData = null;
    VBoxSimulation vBoxSimulation = null;

    PerspectiveViewer perspectiveViewer = null;
    Gui3D gui3D = null;
    public static SimRunner runner = null;

    public SimMatrix simMatrix = null;

    ////////////////////////
    /// End GUI controls ///
    ////////////////////////

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        sim = this;

        /*try {
            robot = new Robot();
        }
        catch (AWTException ex) {
            ex.printStackTrace();;
        }*/
        Utils.init();
        BehaviorLoader.load();
        Config.load();

        simMatrix = new SimMatrix();
        simMatrix.load(Constants.INPUT_LOAD_PATH + Config.getSimMatrixFilename());

        draw = Config.getDrawLoaded();

        setupGenericGui();
        
        scenario = new Scenario();
        //scenario.init(this);

        // If we auto-loaded from a prior sim run, reload the last scenario including
        // generated survivors
        if (Config.getAutoLoaded() == true && Config.getStartNew() == false) {
            scenario.init(this, Constants.SCENARIO_CURRENT_FILE_NAME);
        }
        else {
            scenario.init(this, Config.getScenarioName());
        }

        // The gui elements that are specific to the scenario, need sizing, need populating, etc.
        setupScenarioSpecificGui();
        //border.setRight(addFlowPane());

        // Controls are now all created, so populate whatever we need to from config, like the
        // number of random survivors that we loaded
        applyLoadedConfig();

        //StackPane root = new StackPane();
        scene = new Scene(border, wid, hgt);

        /*scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                Utils.log("KeyPress: " + keyEvent.getCharacter().charAt(0));
            }
        });*/
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                // Screw it, just do them both ...
                perspectiveViewer.processKeyRelease(keyEvent.getCode());
                scenario.processKeyRelease(keyEvent.getCode());
                /*if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                    scenario.processDeleteKey();
                }*/
            }
        });

        setLanguage(Constants.Language.ENGLISH);
        stage.setScene(scene);
        stage.show();

        // Actually lets start in a paused mode.
        //handlePauseButton();
        initMainLoop();
        reset();

        // Do this last, because it auto-starts the sim in some circumstances
        runner = new SimRunner(this);

        // And then let's actually start but we are paused
        //start();

        //canvas.getGraphicsContext2D().translate(-100, -100);
        //moveCanvas(canvas, -100, -100);
        //mainLoop.start();
        /*"primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(final WindowEvent event) {
                    mainLoop.stop();
                    System.exit(0);
                }
        });*/
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    private static long lastMS = 0;
    public void updateOneFrame() {
        // Write out the 'stay alive' file so external programs know if we're still running
        long currentMS = System.currentTimeMillis();
        // Write the file out every 30 seconds
        if (currentMS - lastMS > 30000) {
            lastMS = currentMS;
            Config.saveProcMonFile(runner.getCurrentRunNum());
        }

        // Now do the rest
        if (scenario.update() == false) {
            return;
        }
        if (draw == true) {
            perspectiveViewer.update();
        }
        // Update all our GUI stuff as needed
        for (VBoxCustom box : vBoxCustomList) {
            box.update();
        }
        if (draw == true) {
            redrawCanvas();
        }
    }

    public void initMainLoop() {
        lastTime = System.nanoTime();
        mainLoop = new AnimationTimer() {
            @Override
                public void handle(long now) {
                /*skipCount++;
                if (skipCount <= desiredSkipCount) {
                return;
                }
                skipCount = 0;*/

                currentTime = now;
                fps++;
                delta += currentTime-lastTime;

                //if (skipCount >= desiredSkipCount) {
                updateOneFrame();
                //skipCount = 0;
                //}

                if (delta > Constants.ONE_SECOND_IN_NANOSECONDS) {
                    //VBoxCurrentData.updateFpsText(fps);
                    updateFpsText(fps);
                    delta -= Constants.ONE_SECOND_IN_NANOSECONDS;
                    fps = 0;
                }

                updateTimeText();
                lastTime = currentTime;
            }
        };
    }

    //////////////////////
    //// Start Drawing ///
    //////////////////////

    public void updateNumRunText(int num) {
        if (num <= 0) {
            numRunText.setText("");
        }
        else {
            numRunText.setText("Run: " + num);
        }
    }

    public void updateFpsText(int newFps) {
        fpsText.setText("FPS: " + newFps);
    }

    public void updateTimeText() {
        if (scenario == null || scenario.simTime == null) {
            return;
        }
        timeText.setText(scenario.simTime.toString());
    }

    public void updateCanvasVisibleArea() {
        // Update our local canvas coordinates, really we only need to do this when the viewport changes.
        Point2D coordinates = canvas.sceneToLocal(centerStack.getLayoutX(), centerStack.getLayoutY());
        canvasVisibleX = coordinates.getX();
        canvasVisibleY = coordinates.getY();

        // Width and height are close enough.  I really want the layoutwidth and layout height but those arent
        // gettable for some reason.  I'd have to do getLayoutBounds and I dont really want to do that.
        coordinates = canvas.sceneToLocal(
            centerStack.getLayoutX() + centerStack.getWidth(), 
            centerStack.getLayoutY() + centerStack.getHeight());

        canvasVisibleWid = coordinates.getX() - canvasVisibleX;
        canvasVisibleHgt = coordinates.getY() - canvasVisibleY;

        canvasVisibleRect.setX(canvasVisibleX);
        canvasVisibleRect.setY(canvasVisibleY);
        canvasVisibleRect.setWidth(canvasVisibleWid);
        canvasVisibleRect.setHeight(canvasVisibleHgt);
        //Utils.log(canvasVisibleX + ", " + canvasVisibleY+ ", " + canvasVisibleWid + ", " + canvasVisibleHgt);
    }

    public void signalRedraw() {
        // If we arent in regular looping mode, redraw.  Otherwise we dont
        // need to.
        if (running == false && draw == true) {
	        redrawCanvas();

            // And throw it up again later for good measure, sometimes
            // we need it to draw after the event has occurred so the drawing
            // doesnt work right if we dont do this.
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    redrawCanvas();
                }
            });
        }
    }

    public void redrawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        //gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // We really only need to do this when we move or resize the viewport.
        updateCanvasVisibleArea();
        scenario.draw(gc, canvasVisibleRect);
    }
    //////////////////////
    ///// End Drawing ////
    //////////////////////

    ///////////////////////
    /// Misc Processing ///
    ///////////////////////

    public void reset() {
        //scenario.reset();
        survivorFoundTimes = "";
        scenario.deployAll();
        vBoxCurrentData.clear();
        vBoxCurrentData.update();
        spCanvasContainer.setHvalue(scenario.getStartingHvalue()); // 0 is top, 1.0 is bottom 
        spCanvasContainer.setVvalue(scenario.getStartingVvalue()); // 0 is left, 1.0 is right
        //spCanvasContainer.scale(scenario.zoomLevel, scenario.startingX, scenario.startingY);
        updateCanvasVisibleArea();
        updateDroneParameterFields();
        redrawCanvas();
        perspectiveViewer.reset();
    }

    public void handleStartButton() {
        if (running == false) {
            start();
        } else {
            stop();
        }
    }

    public void start() {
        running = true;
        mainLoop.start();
        if (scenario.buildMode == true) {
            stopBuilding();
        }
	    btnStart.setText("Stop");
    }

    public void stop() {
        running = false;
	    mainLoop.stop();
        if (scenario.buildMode == true) {
            stopBuilding();
        }
	    btnStart.setText("Start");
    }

    public void handleBuildStopButton()
    {
        if (scenario.buildMode == false) {
            build();
        } else {
            stopBuilding();
        }
    }    

    public void handlePauseButton() {
	    scenario.togglePause();
        if (scenario.paused == true) {
            btnPause.setText("Resume");
        } else {
            btnPause.setText("Pause");
        }
    }

    public void build() {
        stop();
        btnBuild.setText("Stop");
        border.setLeft(builderVBox);
        scenario.startBuilding();
    }

    public void stopBuilding()
    {
        if (scenario.buildMode == true) {
            scenario.stopBuilding();
            btnBuild.setText("Build");
            border.setLeft(leftVBox);
        }
    }             

    String newScenario = null;
    public void selectScenario(String newScen) {
        newScenario = newScen;

        // Attempt to load the new scenario
        stop();
        drawLoading();
        redrawCanvas();

        Task task = new Task<Void>() {
            @Override public Void call() {
                scenario.load(newScenario);
                return null;
            }
        };

        //ProgressBar bar = new ProgressBar();
        //bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    public void setLanguage(Constants.Language newLang) { 
	    language = newLang;
        setTextLanguage(language);
    }

    public void setLanguage(String newLang) {
        if (newLang == "English") {
            setLanguage(Constants.Language.ENGLISH);
        } else if (newLang == "日本語") {
            setLanguage(Constants.Language.JAPANESE);
        } else {
            setLanguage(Constants.Language.BOTH);
        }
    }

    public void updateAllCurrentDroneInfo() {
        updateAllChoiceBoxText();
        updateDroneParameterFields();
    }

    public void updateAllChoiceBoxText() {
        for (VBoxCustom box : vBoxCustomList) {
            box.updateChoiceBoxText();
        }
    }

    public void updateDroneParameterFields() {
        for (VBoxCustom box : vBoxCustomList) {
            box.updateOnDemand();
        }
    }

    public void setTextLanguage(Constants.Language lang) {
        for (VBoxCustom box : vBoxCustomList) {
            box.setLanguage(lang);
        }

        // Update all of our labels and text strings
        switch (lang) {
            case ENGLISH:
                stage.setTitle("DroneLab " + strVersion);

                // Top bar
                btnStart.setText("Start");
                btnReset.setText("Reset");
                btnBuild.setText("Build");
                btnPause.setText("Pause");
                break;

            case JAPANESE:
                stage.setTitle("ドローンラブ " + strVersion);

                // Top bar
                btnStart.setText("開始");
                btnReset.setText("リセット");
                btnBuild.setText("ビルドする");
                btnPause.setText("休止する");
                break;

            case BOTH:
                stage.setTitle("ドローンラブ DroneLab " + strVersion);

                // Top bar
                btnStart.setText("開始 Start");
                btnReset.setText("リセット Reset");
                btnBuild.setText("ビルドする Build");
                btnPause.setText("休止する Pause");
                break;
        }

        setTopBarButtonSizes();
        updateAllChoiceBoxText();
    }

    public String getSimDataFilename() {
        String filename = "";

        String strDate = new SimpleDateFormat("'('yyyy-MM-dd'_'HH-mm-ss').txt'").format(new Date());

        // Filename based on various parameters
        SimParams params = scenario.simParams;

        // Start with the algorithm so we can easily sort by name.
        filename += params.getAlgorithmFlag().getFilePrefix();
        /*if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD)
            filename += "st_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL)
            filename += "sp_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER)
            filename += "sc_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.MIX_SRA)
            filename += "mix_"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.MIX_SRA_C)
            filename += "mixc_"; 
        else
            filename += "nd_"; */

        filename += params.getNumDrones() + "uav_"; 
        filename += scenario.getNumVictims() + "ppl_"; 
        filename += params.getTimeLimitMinutes() + "min_"; 
        filename += strDate;
        return filename;
    }

    // ************************************************************
    // NOTE TO SELF THIS DATA RECORDING SHOULD MOVE ELSEWHERE

    // This is treated differently and is not part of the regular "package" of data, rather,
    // we just write out all survivor timestamps as a list to one single file when we finish
    // a simulation run.
    public String ls = System.getProperty("line.separator");
    public void addSurvivorTimestamp() {
        survivorFoundTimes += "" + scenario.simTime.getTotalSeconds() + ls;
    }

    public void recordSimMatrixData() {
        SimParams params = scenario.simParams;
        SimMatrixItem item = params.getSimMatrixItem();
        // Only register it if we got to 90% or above
        //if (strMaxSeen)
        // These were set already when the benchmark was reached
        int seen90Perc = vBoxCurrentData.getSeen90PercSeconds();
        int loc90Perc = vBoxCurrentData.getLocated90PercSeconds();
        if (seen90Perc > 0) {
            Utils.log("Reached 90% seen (" + sim.scenario.getNumVictimsSeen() + "/" + sim.scenario.getNumVictims() + " survivors)");
            item.setSecondsTakenCamera(runner.getCurrentRepetitionNum()-1, seen90Perc);
        }
        if (loc90Perc > 0) {
            item.setSecondsTakenFINDER(runner.getCurrentRepetitionNum()-1, loc90Perc);
        }

        String str = "";

        for (SimMatrixItem item2 : params.getSimMatrix().getItems()) {
            for (int i = 0; i < params.getNumRepetitions(); i++) {
                str += item2.getSecondsTakenCamera(i);
                if (i != params.getNumRepetitions()-1) {
                    str += ", ";
                } 
            }
            str += "\r\n";
        }

        // Write out the excel spreadsheet now too, or just a text file with all the data
        Utils.writeFile(str, Constants.DATA_SAVE_PATH + Constants.DATA_FILE_CAMERA);

        str = "";

        for (SimMatrixItem item2 : params.getSimMatrix().getItems()) {
            for (int i = 0; i < params.getNumRepetitions(); i++) {
                str += item2.getSecondsTakenFINDER(i);
                if (i != params.getNumRepetitions()-1) {
                    str += ", ";
                } 
            }
            str += "\r\n";
        }

        // Write out the excel spreadsheet now too, or just a text file with all the data
        Utils.writeFile(str, Constants.DATA_SAVE_PATH + Constants.DATA_FILE_FINDER);
    }

    // Record the data for one sim run
    public void recordSimData() {
        String filename = getSimDataFilename();
        String strSeen = vBoxCurrentData.getSeenBenchmarkData();
        String strLocated = vBoxCurrentData.getLocatedBenchmarkData();
        String strMaxSeen = vBoxCurrentData.getMaxSeen();
        String strMaxLocated = vBoxCurrentData.getMaxLocated();

        SimParams params = scenario.simParams;
        String str = "";

        // Save out the data in a format that works for our simulation matrix
        if (params.getSimMatrixItem() != null) {
            recordSimMatrixData();
        }
        
        str += "Algorithm: " + params.getAlgorithmFlag().toString();
        /*if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.STANDARD)
            str += "Algorithm: Standard"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SPIRAL)
            str += "Algorithm: Spiral"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.SCATTER)
            str += "Algorithm: Scatter"; 
        else if (params.getAlgorithmFlag() == SimParams.AlgorithmFlag.MIX_SRA)
            str += "Algorithm: MixSRA"; 
        else
            str += "Algorithm: NotDefined"; */
        str += "\r\n"; 
        
        str += "RealSeconds: " + scenario.getLastRunSeconds();
        str += "\r\n"; 
        str += "Drones: " + params.getNumDrones();
        str += "\r\n"; 
        str += "Survivors: " + scenario.getNumVictims();
        str += "\r\n"; 
        str += "SimTimeLimit: " + params.getTimeLimitSeconds();
        str += "\r\n"; 
        str += "SimTimeStoppedAt: " + scenario.simTime.toString();
        str += "\r\n"; 
        str += "MaxCameraCoverage: " + strMaxSeen;
        str += "\r\n"; 
        str += "MaxFINDERCoverage: " + strMaxLocated;
        str += "\r\n"; 
        str += "CameraBenchmarks: ";
        str += "\r\n"; 
        str += strSeen;
        str += "\r\n"; 
        str += "FINDERBenchmarks: ";
        str += "\r\n"; 
        str += strLocated;

        // Log it so we can see too.
        Utils.print("---------------------------------");
        Utils.log("Filename: " + filename);
        Utils.log(str);

        // Comment this out for now; I don't want to write 1000 files for all the simulation runs
        //Utils.writeFile(str, Constants.DATA_SAVE_PATH + filename);

        // Now write out the survivor time data as just a plain text file
        String fname = "" + (runner.getCurrentRunNum() + 1) + ".txt";
        Utils.writeFile(survivorFoundTimes, Constants.SEEN_DATA_SAVE_PATH + fname);
    }
    
    // END NOTE TO SELF THE ABOVE DATA RECORDING SHOULD MOVE ELSEWHERE
    // ************************************************************

    public boolean signalPercentComplete(int perc) {
        if (perc >= 89) {
            // We are going to end at 90% for now to save time.
            scenario.endRun();
            return true;
        }

        return false;
    }

    public void signalComplete() {
        stop();

        // We should record any data at this point.  We have all the
        // benchmarks saved on the vBoxCurrentData screen.  Somehow
        // we should name the file intuitively and timestamp it.
        recordSimData();

        // Maybe we do more stuff, maybe not, whatever we want really.
        runner.signalSimComplete();
    }

    ///////////////////////////
    /// End Misc Processing ///
    //////////////////////////

    ///////////////////////////
    //// Start Gui Creation ///
    ///////////////////////////

    private void setupGenericGui() {
        HBox hbox = createTopHBox();
        addStackPane(hbox);         // Add stack to HBox in top region
        border.setTop(hbox);
        border.setLeft(createLeftVBox());

        centerStack.setAlignment(Pos.TOP_LEFT);
        //createMainGraphicalArea();
        /*createCanvasPane();
        centerStack.getChildren().add(spCanvasContainer);
        centerStack.getChildren().add(displayText);
        border.setCenter(centerStack);*/
    }

    private void setupScenarioSpecificGui() {
        // Create it now but we dont do anything with it until we hit the build button
        createBuilderVBox();

        // Do this after the scenario is created since we need the background dimensions
        // to create our canvas properly.  Although we really should be able to resize it
        // like we do when we load a new scenario.
        createMainGraphicalArea();

        setupCustomVBoxesForScenario();

        // Must be done after we create the scenario since this population is
        // based on various scenario flags and settings.
        vBoxConfiguration.populateCheckBoxes();

        updateAllCurrentDroneInfo();
    }

    private void create3dViewer() {
        perspectiveViewer = new PerspectiveViewer(scenario);
        gui3D = new Gui3D(perspectiveViewer);
    }

    private void createMainGraphicalArea() {
        create3dViewer();
        createCanvasPane();
        VBox centerVB = new VBox();
        centerStack.getChildren().add(spCanvasContainer);
        centerStack.getChildren().add(displayText);
        border.setCenter(centerStack);

        StackPane bottomStack = new StackPane();
        bottomStack.getChildren().addAll(perspectiveViewer.getSubScene(), gui3D);
        border.setBottom(bottomStack);
    }

    private void setupCustomVBoxesForScenario() {
        vBoxConfiguration.setScenario(scenario);
        vBoxDroneConfig.setScenario(scenario);
        vBoxBehavior.setScenario(scenario);
        vBoxSensor.setScenario(scenario);
        vBoxCurrentData.setScenario(scenario);
        vBoxSimulation.setScenario(scenario);
    }
/*
    private void moveCanvas(Canvas canvas, int x, int y) {
        canvas.setTranslateX(x);
        canvas.setTranslateY(y);
    }*/

    public void updateCanvasMouseCoordinates(Point2D coordinates) {
        updateCanvasMouseCoordinates(coordinates.getX(), coordinates.getY());
    }

    public void updateCanvasMouseCoordinates(double x, double y) {
        canvasMouseX = x;
        canvasMouseY = y;
        scenario.updateCanvasMouseCoordinates(canvasMouseX, canvasMouseY);
    }

    //public ScalableScrollPane createCanvasPane() {
    public ScalableScrollPane createCanvasPane() {
        canvas = new SimCanvas(this, scenario.backGround.getWidth(), scenario.backGround.getHeight());
        /*InvalidationListener listener = new InvalidationListener() {
            @Override
            public void invalidated(Observable o) {
                redrawCanvas();       
            }           
        };
        canvas.widthProperty().addListener(listener);
        canvas.heightProperty().addListener(listener);*/
        //Pane canvasPane = new Pane();

                    // add scale transform
        //canvas.scaleXProperty().bind(myScale);
        //canvas.scaleYProperty().bind(myScale);

        spCanvasContainer = new ScalableScrollPane(canvas);
        //group.getChildren().add(canvas); 
        //spCanvasContainer.setContent(group);
        spCanvasContainer.setPannable(true);

        /*canvas.setFocusTraversable(true);
        canvas.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                System.out.println("Handled");
            }
        });*/

        //spCanvasContainer.setHmax(canvas.getWidth());
        //spCanvasContainer.setVmax(canvas.getHeight());
        EventHandler filter = 
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        if (scenario.buildMode == true) {
                            scenario.handleScrollEvent(event.getDeltaY());
                        } else {
                            double zoomFactor = 1.5;

                            if (event.getDeltaY() <= 0) {
                                // zoom out
                                zoomFactor = 1 / zoomFactor;
                            }

                            spCanvasContainer.scale(zoomFactor, event.getSceneX(), event.getSceneY());
                            scenario.zoomFactor = spCanvasContainer.getScale();
                            signalRedraw();
                        }
 	                    event.consume();

                /*
                    // I'm leaving all this in case I want to try to do that zoom + fitting behavior
                    // again.
                        //Bounds bounds = canvas.localToScene(canvas.getBoundsInLocal());
                        sceneMouseX = event.getSceneX();
                        sceneMouseY = event.getSceneY();
                        Point2D coordinates = canvas.sceneToLocal(sceneMouseX, sceneMouseY);
                        updateCanvasMouseCoordinates(coordinates);

                        double oldZoom = scenario.zoomLevel;
                        scenario.handleScrollEvent(event.getDeltaY());

                        spCanvasContainer.scale(scenario.zoomLevel);

                        // Basically the sceneToLocal on the canvas doesnt "work yet" for some reason so
                        // I have to do the calculations myself to figure out where the mouse pointer is now.
                        double newCanvasMouseX = canvasMouseX - (canvasMouseX * ((scenario.zoomLevel - oldZoom) / scenario.zoomLevel));
                        double newCanvasMouseY = canvasMouseY - (canvasMouseY * ((scenario.zoomLevel - oldZoom) / scenario.zoomLevel));

                        // newCanvasMouseX and Y are correct,
                        double xDiff = (newCanvasMouseX - canvasMouseX);
                        double yDiff = (newCanvasMouseY - canvasMouseY);

                        // The differences are correct, but how much does that mean I want to scroll?
                        // We want to move the viewing pane so that our
                        // previous canvasX and canvasY are where the mouse is.  That gives me a ratio
                        // of how big the diffs are compared to the canvas size. But what does that mean
                        // for the size of the container?  It's like saying, I know I was at this position
                        // in the canvas before.  Now I know after we scaled it down, I'm at this other
                        // position.  I want to get back to my original position, I know the difference between
                        // the two in canvas coordinates, but what does that mean in container scroll values?
                        // How much do I need to change the scroll?  Its really hard to say because the ScrollPane
                        // seems to do its own set of things here, and even if we do what feels intuitive mathematically
                        // the result is not correct.  So I just tweak it using values I know work pretty well.
                        // These values actually give us really good results so they must be pretty close to replicating
                        // whatever crap the ScrollPane does internally.
                        double xBase = 0.25; // Based on a regular size map scenario
                        double yBase = 0.35;

                        // If we have an image with a regular size but we add it to the scenario at an increased
                        // size these numbers have to change accordingly. This should be a formula which
                        // divides by the ratio between the original image size and the image size actually used
                        // in the scenario.  Or, maybe just say the regular base is on a 2500 x 1850 image so
                        // everything else should be scaled accordingly.
                        xBase /= (scenario.backGround.getWidth() / scenario.nativeWidth);
                        yBase /= (scenario.backGround.getHeight() / scenario.nativeHeight);

                        double xVar = 1.25;// + (xBase / scenario.zoomLevel);
                        double yVar = 1.65;// + (yBase / scenario.zoomLevel);
                        double xRatio = ((xDiff * xVar) / canvas.getWidth());
                        double yRatio = ((yDiff * yVar) / canvas.getHeight());

                        spCanvasContainer.setHvalue(spCanvasContainer.getHvalue() - xRatio);
                        spCanvasContainer.setVvalue(spCanvasContainer.getVvalue() - yRatio);
                        event.consume();*/
                    }
                };
        spCanvasContainer.addEventFilter(ScrollEvent.ANY, filter);

        /*final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
            }
        };
        spCanvasContainer.scaleGroup.scaleXProperty().addListener(changeListener);*/

        // This is what I need.
       /* EventHandler transformFilter = 
                new EventHandler<TransformChangedEvent>() {
                    @Override
                    public void handle(TransformChangedEvent event) {
                Point2D coordinates = canvas.sceneToLocal(sceneMouseX, sceneMouseY);
                System.out.println("coordinates.getX() SHOULD MATCH NEXT MOVE: " + coordinates.getX());
                    }
                };
        spCanvasContainer.scaleTransform.addEventFilter(TransformChangedEvent.ANY, transformFilter);*/

        spCanvasContainer.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                spMouseX = event.getX();
                spMouseY = event.getY();
                screenMouseX = event.getScreenX();
                screenMouseY = event.getScreenY();
                sceneMouseX = event.getSceneX();
                sceneMouseY = event.getSceneY();
            }
        });

        //return canvasPane;
        return spCanvasContainer;
    }

    public void addStackPane(HBox hb) {
        StackPane stack = new StackPane();
        Rectangle helpIcon = new Rectangle(30.0, 25.0);
        helpIcon.setFill(new LinearGradient(0,0,0,1, true, CycleMethod.NO_CYCLE,
            new Stop[]{
            new Stop(0,Color.web("#4977A3")),
            new Stop(0.5, Color.web("#B0C6DA")),
            new Stop(1,Color.web("#9CB6CF")),}));
        helpIcon.setStroke(Color.web("#D0E6FA"));
        helpIcon.setArcHeight(3.5);
        helpIcon.setArcWidth(3.5);

        Text helpText = new Text("?");
        helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
        helpText.setFill(Color.WHITE);
        helpText.setStroke(Color.web("#7080A0")); 

        stack.getChildren().addAll(helpIcon, helpText);
        stack.setAlignment(Pos.CENTER_RIGHT);     // Right-justify nodes in stack
        StackPane.setMargin(helpText, new Insets(0, 10, 0, 0)); // Center "?"

        //hb.getChildren().add(stack);            // Add to HBox from Example 1-2
        //HBox.setHgrow(stack, Priority.ALWAYS);    // Give stack any extra space

        HBox newHb = new HBox();
        //newHb.setPadding(new Insets(15, 12, 15, 12));
        newHb.setSpacing(10);
        newHb.setAlignment(Pos.CENTER_RIGHT);     // Right-justify nodes in hbox
        numRunText.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        numRunText.setFill(Color.WHITE);
        newHb.getChildren().addAll(numRunText); //, stack);
        timeText.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        timeText.setFill(Color.WHITE);
        newHb.getChildren().addAll(timeText); //, stack);
        fpsText.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        fpsText.setFill(Color.WHITE);
        //fpsText.setStroke(Color.web("#7080A0")); 
        newHb.getChildren().addAll(fpsText); //, stack);
        HBox.setHgrow(newHb, Priority.ALWAYS);    // Give hbox any extra space

        hb.getChildren().add(newHb);   
    }

    public void setTopBarButtonSizes() {
        int prefWid = 100;
        int prefHgt = 20;

        if (language == Constants.Language.BOTH) {
            prefWid = 150;
        }

        btnStart.setPrefSize(prefWid, prefHgt);
        //btnPause.setPrefSize(prefWid, prefHgt);
        btnReset.setPrefSize(prefWid, prefHgt);
        btnBuild.setPrefSize(prefWid, prefHgt);
        btnPause.setPrefSize(prefWid, prefHgt);
    }

    public HBox createTopHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699;");

        btnStart.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleStartButton();
            }
        });

        /*btnPause.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                stop();
            }
        });*/

        btnReset.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                reset();
            }
        });

        btnBuild.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handleBuildStopButton();
            }
        });

        btnPause.setOnAction(new EventHandler<ActionEvent>() {    
            @Override
            public void handle(ActionEvent event) {
                handlePauseButton();
            }
        });

        hbox.getChildren().addAll(btnStart, btnPause, btnReset, btnBuild);
        return hbox;
    }

    public TitledPane addTitledPane(Accordion addTo, VBoxCustom vbox, String text) {
        TitledPane tp = new TitledPane();
        //System.out.println(Font.getDefault().getName());
        //tp.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD, 12));
        tp.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        //tp.setStyle("-fx-font-weight: bold"); 
        tp.setText(text);
        //tp.lookup(".title").setStyle("-fx-font-weight: bold;");
        tp.setContent(vbox);
        addTo.getPanes().add(tp);
        return tp;
    }

    public VBox createLeftVBox() {
        vBoxConfiguration = new VBoxConfiguration(this);
        vBoxBehavior = new VBoxBehavior(this);
        vBoxSensor = new VBoxSensor(this);
        vBoxDroneConfig = new VBoxDroneConfig(this);
        vBoxCurrentData = new VBoxCurrentData(this);
        vBoxSimulation = new VBoxSimulation(this);

        vBoxCustomList = new ArrayList<VBoxCustom>();
        vBoxCustomList.add(vBoxConfiguration);
        vBoxCustomList.add(vBoxDroneConfig);
        vBoxCustomList.add(vBoxBehavior);
        vBoxCustomList.add(vBoxSensor);
        vBoxCustomList.add(vBoxCurrentData);
        vBoxCustomList.add(vBoxSimulation);

        //leftVBox = new VBox();
        Accordion accordion = new Accordion();
        VBox.setVgrow(accordion, Priority.ALWAYS);
        //accordion.getPanes().add(titledPane);

        addTitledPane(accordion, vBoxConfiguration, "Settings");
        TitledPane startingPane = addTitledPane(accordion, vBoxSimulation, "Simulation");
        addTitledPane(accordion, vBoxDroneConfig, "Drone");
        //addTitledPane(accordion, vBoxBehavior, "Behaviors");
        //addTitledPane(accordion, vBoxSensor, "Sensors");
        addTitledPane(accordion, vBoxCurrentData, "Data");

        // We'll default to this one opened...
        accordion.setExpandedPane(startingPane);
        //tp.getChildren().addAll(VBoxCustomList);

        vBoxDroneConfig.getChildren().addAll(vBoxBehavior, vBoxSensor);

        //vbox.getChildren().add(VBoxConfiguration);
        //vbox.getChildren().add(VBoxDroneConfig);
        //vbox.getChildren().add(VBoxBehavior);
        //vbox.getChildren().add(VBoxSensor);
        //vbox.getChildren().add(VBoxCurrentData);
        //vbox.getChildren().addAll(VBoxCustomList);
        leftVBox = new VBox();
        leftVBox.getChildren().add(accordion);
        return leftVBox;
    }

    public void createBuilderVBox() {
        builderVBox = new VBox();
        vBoxBuilderOptions = new VBoxBuilderOptions(this);
        TitledPane tp = new TitledPane();
        tp.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        tp.setText("Build Options");
        tp.setContent(vBoxBuilderOptions);
        builderVBox.getChildren().add(tp);
    }

    public void applyLoadedConfig() {
        // This should be set because it gets pulled later
        vBoxSimulation.setNumRandomSurvivorsFromConfig();

        // This does'nt necessarily have to be set because we automatically run at
        // 200x speed when auto-loading but just makes it more intuitive when looking
        // at the GUI if it's set to the right value
        if (Config.getAutoLoaded() == true) {
            vBoxSimulation.setFFW(200);
        }
    }

    // To set our fields in the GUI so that htey match what we auto-loaded
    public void resetVBoxSimulationFields() {
        vBoxSimulation.resetFields();
    }

    /////////////////////////
    //// End Gui Creation ///
    /////////////////////////

    //////////////////////////////
    //// Start Misc Processing ///
    //////////////////////////////

    public void signalNewScenarioLoaded() {
        // Loaded scenario before we even created the canvas.
        if (canvas == null) {
            return;
        }

        hideLoading();

        // Remake the canvas.
        canvas.setWidth(scenario.backGround.getWidth());
        canvas.setHeight(scenario.backGround.getHeight());

        // And reset/redraw everything.
 	    reset();

        // Call update once just so that everything updates and redraws properly.
        updateOneFrame();

        // Try yet again for a redraw because it still sometimes hits some kind of
        // race condition and doesnt redraw.  Doing it this way does appear to work.
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                redrawCanvas();
            }
        });
    }
    
    public void drawLoading() {
        displayText.show("Loading...");
    }

    public void hideLoading() {
        displayText.fadeOut();
    }

    ////////////////////////////
    //// End Misc Processing ///
    ////////////////////////////

    /*
    private void drawDShape(GraphicsContext gc) {
        gc.beginPath();
        gc.moveTo(50, 50);
        gc.bezierCurveTo(150, 20, 150, 150, 75, 150);
        gc.closePath();
    }

    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 10, 10, 40);
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[]{10, 40, 10, 40},
                       new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                         new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                          new double[]{210, 210, 240, 240}, 4);
    }*/
}

