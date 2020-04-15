package dronelab;

import java.util.ArrayList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.transform.Rotate;

import dronelab.collidable.*;
import dronelab.utils.*;

// Allows various parts of the environment to be generated in specific
// ways
public class EnvironmentGenerator  {
    Scenario scen;

    // Do we want to distribute people completely at random, or based
    // on pre-conceived notions?
    public static enum PersonDistribution {
        Random, Buildings, TallBuildings;
    }

    public static PersonDistribution getDistributionForString(String str) {
        for (PersonDistribution dist : PersonDistribution.values()) {
            if (dist.name().equals(str)) {
                return dist;
            }
        }
        return PersonDistribution.Random;
    }

    public EnvironmentGenerator(Scenario s) {
        scen = s;
    }
    
    public void generateVictims(int nNum) {
        generateVictims(nNum, nNum, PersonDistribution.Random);
    }

    public void generateVictims(int nMin, int nMax) {
        generateVictims(nMin, nMax, PersonDistribution.Random);
    }

    public void generateVictims(int nMin, int nMax, PersonDistribution distro) {
        int nNumVictims = Utils.number(nMin, nMax);

        switch (distro) {
            case Random: 
                generateRandomVictims(nNumVictims);
                break;
            case Buildings: 
                generateBuildingsVictims(nNumVictims);
                break;
            case TallBuildings: 
                generateTallBuildingsVictims(nNumVictims);
                break;
        }
    }

    private void generateRandomVictims(int nNum) {
        int minX = 0;
        int maxX = (int)scen.currentWidth - 10; // Just so we arent right on the edge
        int minY = 0;
        int maxY = (int)scen.currentHeight - 10; // Just so we arent right on the edge
        int elevation = 0;

        // Now based on the distribution, decide where to try to put people.
        // If it's totally random, that's easy, just pick completely at random.
        for (int i = 0; i < nNum; i++) {
            int x = Utils.number(minX, maxX);
            int y = Utils.number(minY, maxY);
            scen.addVictim(x, y, elevation);
        }
    }

    private void generateBuildingsVictims(int nNum) {
        Utils.log("generateBuildingsVictims not done");
    }

    private void generateTallBuildingsVictims(int nNum) {
        Utils.log("generateTallBuildingsVictims not done");
    }
}