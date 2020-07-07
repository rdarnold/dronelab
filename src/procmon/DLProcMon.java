package dronelab.procmon;

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

public class DLProcMon extends Application {

    Stage stage;
    Scene scene;

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

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        Utils.init();
        Config.load();

        //border.setRight(addFlowPane());

        VBox root = new VBox();
        scene = new Scene(root, 200, 100);

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
                /*if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                    scenario.processDeleteKey();
                }*/
            }
        });

        stage.setScene(scene);
        stage.show();

        initMainLoop();
        
        mainLoop.start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    private static void checkDroneLab() {
        long prevTS = Config.getTimeStampLoaded();

        // Load the file
        Config.loadProcMonFile();

        // See if the timestamp changed from last time
        if (Config.getTimeStampLoaded() == prevTS) {
            // If it's the same, that means the program froze or something; first close the process
            try {
                Runtime rt = Runtime.getRuntime();
                rt.exec("taskkill /F /FI \"WindowTitle eq DroneLab*\" /T");
                
                // Now write out a new config file with the correct number of runs and info from the
                // original one we loaded
                Config.save(Config.getNumRunsLoaded(), true);
    
                // Now give it a few seconds to make sure it closed
                Thread.sleep(10000);
    
                // Now restart the sim
                rt.exec("cmd /c \"\"" + Constants.RESTART_BATCH_FILE_NAME_PATH);
            }
            catch (Exception e) {
                Utils.log("Exception: " + e.toString());
            }

            Utils.log("DroneLab restarted at " + Config.getNumRunsLoaded() + " runs.");
        }
    }

    private static long lastMS = 0;
    public void updateOneFrame() {
        // Write out the 'stay alive' file so external programs know if we're still running
        long currentMS = System.currentTimeMillis();
        // Load the file every 60 seconds
        if (currentMS - lastMS > 60000) {
            lastMS = currentMS;
            checkDroneLab();
        }
    }

    public void initMainLoop() {
        mainLoop = new AnimationTimer() {
            @Override
                public void handle(long now) {

                currentTime = now;
                fps++;
                delta += currentTime-lastTime;

                //if (skipCount >= desiredSkipCount) {
                updateOneFrame();
                //skipCount = 0;
                //}

                if (delta > Constants.ONE_SECOND_IN_NANOSECONDS) {
                    //VBoxCurrentData.updateFpsText(fps);
                    delta -= Constants.ONE_SECOND_IN_NANOSECONDS;
                    fps = 0;
                }
            }
        };
    }
    
}

