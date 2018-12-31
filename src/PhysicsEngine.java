package dronelab;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.image.Image;
import javafx.scene.shape.*;

import dronelab.collidable.*;
import dronelab.utils.*;
public class PhysicsEngine {
    Scenario scenario;

    public PhysicsEngine(Scenario scen) {
        scenario = scen;
    }

    // These things should have an effect...
    public void applyGravity() {

    }

    public void applyWindSpeed() {

    }

    public void applyWindResistance() {

    }

    public void moveAll() {
        Shape intersectShape = null;
        ArrayList<Mobile> mobs = scenario.mobiles;
        ArrayList<Obstacle> obstacles = scenario.obstacles;
        ArrayList<Collision> collisions = scenario.collisions;

        for (Mobile mob : mobs) {
            // First adjust our speed as per parameters
            // Remember these are drones, distressed people and workers.
            mob.adjustSpeed();

            // False means we have no speed and are not moving.
	        if (mob.move() == false)
                continue;

            // Now that we have moved, we need to check and see if we need to update
            // our sector(s).
            scenario.updateSectors(mob);

            intersectShape = Physics.getCollisionShape(mob, obstacles);
            if (intersectShape != null) {
                // This is just one part of the collision, we could potentially create this
                // entire shape and just draw it for each collision, might be more interesting
                // than doing the X here.
                Collision c = new Collision(intersectShape.getBoundsInLocal().getMinX(), intersectShape.getBoundsInLocal().getMinY());
                collisions.add(c);

                // Take some damage if we're doing that
                mob.damage((int)Physics.getSpeed(mob.getXSpeed(), mob.getYSpeed()));

                // Now move back since we hit something, the first move
                // was sort of like a fake move.
                mob.undoMove();
                mob.stopMoving();
                mob.stopSeekingTarget();
            }
        }
    }
}