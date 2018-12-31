package dronelab;

import java.util.ArrayList;
import java.util.EnumSet;
import javafx.scene.shape.Rectangle;

import dronelab.collidable.*;
import dronelab.utils.*;


// Sectors are used for collision detection ONLY.  They contain
// pointers to any stuff within them.
public class Sector extends Rectangle {

    private int m_nId = 0;
    public int id() { return m_nId; }

    // They have pointers to all mobiles, obstacles, and victims
    // within their limits.
    public ArrayList<Mobile> mobiles = new ArrayList<Mobile>();
    public ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
    public ArrayList<Person> people = new ArrayList<Person>();

    // Where it is in the grid.
    public int row = 0;
    public int col = 0;

    public Sector(int id) { 
        super();
        m_nId = id;
    }

    public Sector(int id, int wid, int hgt) { 
        super(wid, hgt);
        m_nId = id;
    }

    public Sector(int id, int x, int y, int wid, int hgt) { 
        super(x, y, wid, hgt);
        m_nId = id;
        /*Utils.log(x);
        Utils.log(y);
        Utils.log(wid);
        Utils.log(hgt);*/
    }

    // The "add" functions all assume that the thing is not already
    // in the respective list.  So that check must be done elsewhere.
    public void add(Mobile mob) {
        mobiles.add(mob);
        mob.sectors.add(this);
    }
    
    public void add(Obstacle obs) {
        obstacles.add(obs);
    }

    public void add(Person pers) {
        people.add(pers);
    }

    public void remove(Mobile mob) {
        mobiles.remove(mob);
        mob.sectors.remove(this);
    }
    
    public void remove(Obstacle obs) {
        obstacles.remove(obs);
    }

    public void remove(Person pers) {
        people.remove(pers);
    }

    public void clear() {
        mobiles.clear();
        obstacles.clear();
        people.clear();
    }

    // Check to see if the coordinates passed in are inside this sector's
    // boundaries or not.
    public boolean isInsideBounds(double x, double y, double wid, double hgt) {
        return Physics.rectanglesOverlap(x, y, wid, hgt, this);
    }
    
    public boolean isInsideBounds(Rectangle rect) {
        return Physics.rectanglesOverlap(this, rect);
    }

    public boolean isInsideBounds(Collidable col) {
        return isInsideBounds(col.getBoundingRect());
    }
}

