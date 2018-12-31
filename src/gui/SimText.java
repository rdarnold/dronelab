package dronelab.gui;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.animation.Animation;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.text.TextAlignment;
import dronelab.DroneLab;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class SimText extends Text {
    DropShadow dsTextEffect;
    FadeTransition fadeOut; 
    FadeTransition fadeIn; 
    final Animation typingAnimation;
    String content = "...";

    public SimText() {
        super();

        setFont(Font.font ("Verdana", 40));
        setFill(Color.rgb(0, 0, 0));

        dsTextEffect = new DropShadow(25, Color.WHITE);
        dsTextEffect.setSpread(0.75);
        setEffect(dsTextEffect);

        fadeOut = new FadeTransition(Duration.millis(1000), this);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hide();
            }
        });

        fadeIn = new FadeTransition(Duration.millis(250), this);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        typingAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(2000));
            }
        
            protected void interpolate(double frac) {
                final int length = 3; //content.length();
                final int n = Math.round(length * (float) frac);
                setText("Loading" + content.substring(0, n));
            }
        };
    }
     
    public void clear() {
        show(null);
    }

    public void hide() {
        setText("");
        setVisible(false);
    }

    public void fadeOut() {
        fadeOut.play();
    }

    public void fadeIn() {
	    setVisible(true);
        toFront();
        fadeIn.play();
    }

    public void fadeInText(String text) {
        setText(text);
        fadeIn();
    }

    public void show() {
        fadeIn();
        typingAnimation.play();
    }

    public void show(String text) {
        setText(text);
        show();
    }
}