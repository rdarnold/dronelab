package dronelab.gui;

import java.util.function.DoubleConsumer;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

public class AnimatedZoomOperator {
    private final Timeline timeline = new Timeline(60);

    public void zoom(double from, double to, DoubleConsumer applyScale, Runnable finished) {
        stop();
        DoubleProperty scale = new SimpleDoubleProperty(from);
        scale.addListener((property, previous, current) -> applyScale.accept(current.doubleValue()));
        timeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(50), new KeyValue(scale, to)));
        timeline.setOnFinished(event -> finished.run());
        timeline.playFromStart();
    }

    public void stop() {
        timeline.stop();
    }
}
