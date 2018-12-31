package dronelab.gui;

import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

// A ScrollPane that allows proper scaling of its content and appropriately
// resizes its "insides" to fit the content (actually these two things are
// mutually exclusive - so I chose good zooming over insides refitting)
public class ScalableScrollPane extends ScrollPane {
    Group scaleGroup;
    Node currentContent = null;
    //Scale scaleTransform;
    //double scaleFactor;
    AnimatedZoomOperator zoomOperator = new AnimatedZoomOperator();

    public void resetContent(Node content)
    {
        if (currentContent != null) {
            //scaleGroup.getChildren().remove(currentContent);
            scaleGroup.getChildren().removeAll();
        }
        currentContent = content;
        scaleGroup.getChildren().add(content);
        //setContent(scaleGroup);
        //scaleTransform = new Scale(1.0, 1.0, 0, 0);
        //scaleGroup.getTransforms().add(scaleTransform);
    }

    public ScalableScrollPane(Node content)
    {
        scaleGroup = new Group();
        //currentContent = content;
        //scaleGroup.getChildren().add(content);
        resetContent(content);
        setContent(scaleGroup);
        //scaleTransform = new Scale(1.0, 1.0, 0, 0);
        //scaleGroup.getTransforms().add(scaleTransform);
    }

    public void scale(double factor, double sceneX, double sceneY) {
        //scaleFactor = factor;
        //scaleGroup.setScaleX(scaleFactor);
        //scaleGroup.setScaleY(scaleFactor);

        //scaleTransform.setX(scaleFactor);
        //scaleTransform.setY(scaleFactor);
	    zoomOperator.zoom(scaleGroup, factor, sceneX, sceneY);
    }

    public double getScale() {
        return zoomOperator.getScale();
    }
}