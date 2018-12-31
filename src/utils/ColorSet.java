
package dronelab.utils;

import javafx.scene.paint.Color;

public class ColorSet {
    Color strokeCol;
    Color fillCol;
    int defaultStrokeR = 0;
    int defaultStrokeG = 0;
    int defaultStrokeB = 0;
    int defaultFillR = 0;
    int defaultFillG = 0;
    int defaultFillB = 0;
    double defaultStrokeA = 1.0;
    double defaultFillA = 1.0;

    public Color getStroke() { return strokeCol; }
    public Color getFill() { return fillCol; }

    public void setDefaultColors(int r, int g, int b, double a) {
        setDefaultFill(r, g, b, a);
        setDefaultStroke(r, g, b, a);
    }

    public void setDefaultColors(int r, int g, int b, double strokeA, double fillA) {
        setDefaultStroke(r, g, b, strokeA);
        setDefaultFill(r, g, b, fillA);
    }

    public void setDefaultColors(int r, int g, int b) {
        setDefaultColors(r, g, b, 1.0, 1.0);
    }
    
    public void setDefaultFill(int r, int g, int b) {
        setDefaultFill(r, g, b, 1.0);
    }

    public void setDefaultFill(int r, int g, int b, double a) {
        defaultFillR = r;
    	defaultFillG = g;
    	defaultFillB = b;
        defaultFillA = a;
    }

    public void setDefaultStroke(int r, int g, int b) {
        setDefaultStroke(r, g, b, 1.0);
    }

    public void setDefaultStroke(int r, int g, int b, double a) {
        defaultStrokeR = r;
    	defaultStrokeG = g;
    	defaultStrokeB = b;
        defaultStrokeA = a;
    }

    public void setColorsToDefault() {
        setStrokeToDefault();
        setFillToDefault();
    }

    public void setStrokeToDefault() {
        setStroke(defaultStrokeR, defaultStrokeG, defaultStrokeB, defaultStrokeA);
    }

    public void setFillToDefault() {
        setFill(defaultFillR, defaultFillG, defaultFillB, defaultFillA);
    }

    public void setStroke(int r, int g, int b, double a) {
	    strokeCol = Color.rgb(r, g, b, a);
    }

    public void setFill(int r, int g, int b, double a) {
	    fillCol = Color.rgb(r, g, b, a);
    }

    public void setStroke(int r, int g, int b) {
	    strokeCol = Color.rgb(r, g, b);
    }

    public void setFill(int r, int g, int b) {
	    fillCol = Color.rgb(r, g, b);
    }

    private void setColors(int r, int g, int b, double a) {
	    setStroke(r, g, b, a);
	    setFill(r, g, b, a);
    }
}