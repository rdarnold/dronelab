package dronelab.collidable.equipment;

import java.util.ArrayList;
import java.util.Random;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import dronelab.utils.*;
import dronelab.collidable.*;
import dronelab.collidable.behaviors.BehaviorModule;
import dronelab.Scenario;

public class WiFiCommunicator {
    Drone drone;
    private ArrayList<String> queue = new ArrayList<String>();

    // The Solo from 3DR has a Wi Fi HD range of half a mile.
    // We can use that as a guide here.
    private double range = Distance.pixelsFromMeters(800); 

    public double getRange() { return range; }
    public void setRangeMeters(double nMeters) {
        range = Distance.pixelsFromMeters(nMeters); 
    }

    public WiFiCommunicator(Drone d) {
        drone = d;
    } 

    public void setRange(int nNew) {
        range = nNew;
    }

    public void broadcastBehaviorMsg(BehaviorModule mod, String contents) {
        /*String msg = mod.getName() + ":"; 
        msg += contents;
        broadcast(msg);*/

        StringBuilder msg = new StringBuilder(mod.getName());
        msg.append(":");
        msg.append(contents);
        broadcast(msg.toString());
    }

    public ArrayList<String> getQueue() { return queue; }
    public void clearQueue() {
        queue.clear();
    }

    public String dequeue() {
        if (queue.size() <= 0) {
            return null;
        }
        return queue.remove(0);
        //String msg = queue.get(0);
        //queue.remove(0);
        //return msg;
    }

    public void broadcast(String msg) {
        //Utils.log("broadcast: " + msg);
        // So first check to make sure we don't have the same kind of message in the queue already; if we do,
        // just overwrite it.  This can happen if the system is delayed to the point where the number of outgoing
        // messages cannot be consumed fast enough by the broadcast system
        if (queue.size() > 0) {
            String strType = "";
            for (int i = queue.size()-1; i >= 0; i--) {
                strType = queue.get(i).substring(0, 3);
                if (strType.equals("POS") == true) {
                    // Remove any dupes for position messages and just broadcast our latest one
                    Utils.log("REMOVED");
                    queue.remove(i);
                }
            }
        }

        // Slap it onto some kind of outgoing message queue
        queue.add(msg);
    }

    // This could be made a lot more efficient by not doing
    // all the string operations.
    public void receive(String msg) {
        // Parse the message
        String strType = "";
        int index = msg.indexOf(":");
        if (index != -1) {
            strType = msg.substring(0, index);
        }

	    //Utils.log("receive: " + msg);
	    //Utils.log("strType: " + strType);
        if (strType.equals("POS")) {
            //String contents = msg.substring(index + 1, msg.length());
	        //Utils.log("contents: " + contents);

            //String filename = "abc.def.ghi";     // full file name
            //String[] parts = contents.split("\\:"); // String array, each element is text between dots

            //Utils.log("id: " + parts[0]);
            //Utils.log("x: " + parts[1]);
            //Utils.log("y: " + parts[2]);
            /*int id = Utils.tryParseInt(parts[0];
            double x = Utils.tryParseDouble(parts[1]);
            double y = Utils.tryParseDouble(parts[2]);*/
        
            // The new way using indexOf because it's much more
            // efficient than split, even though done like this it's a little
            // hard to read.  But this area was a bottleneck so I needed to optimize.
            // In the future this could be refactored to look nicer.
            //Utils.log(msg);
            int start = index + 1;
            index = msg.indexOf(":", start);
            int id = Utils.tryParseInt(msg.substring(start, index));
            //Utils.log(id);
            start = index + 1;
            index = msg.indexOf(":", start);
            double x = Utils.tryParseDouble(msg.substring(start, index));
            //Utils.log(x);
            start = index + 1;
            double y = Utils.tryParseDouble(msg.substring(start, msg.length()));
            //Utils.log(y);
            drone.updateDroneDataXY(id, x, y); 
        } 
        else if (strType.equals("PSN")) {
            /*String contents = msg.substring(index + 1, msg.length());
	        //Utils.log("contents: " + contents);

            //String filename = "abc.def.ghi";     // full file name
            String[] parts = contents.split("\\:"); // String array, each element is text between dots

            //Utils.log("id: " + parts[0]);
            //Utils.log("x: " + parts[1]);
            //Utils.log("y: " + parts[2]);
            double x = Utils.tryParseDouble(parts[1]);
            double y = Utils.tryParseDouble(parts[2]);
            drone.updatePersonList(x, y);*/
            //Utils.log(msg);
            // We skip over the first entry, it's the id and we don't care about that here.
            int start = index + 1;
            index = msg.indexOf(":", start);
            start = index + 1;
            index = msg.indexOf(":", start);
            double x = Utils.tryParseDouble(msg.substring(start, index));
            //Utils.log(x);
            start = index + 1;
            double y = Utils.tryParseDouble(msg.substring(start, msg.length()));
            //Utils.log(y);
            drone.updatePersonList(x, y); 
        } 
        else {
            // See if this message is directed to a behavior module
            // They are able to receive messages sent to their names
            for (BehaviorModule mod : drone.behaviors) {
                if (strType.equals(mod.getName()) == true) {
                    mod.receive(msg.substring(mod.getName().length() + 1, msg.length()));
                    return;
                }
            }
        }
    }

    public String buildPersonMsg(double x, double y) {
        StringBuilder msg = new StringBuilder("PSN:");
        //msg = "PSN:";  // Of course ideally this would just be a byte code.
        /*msg += drone.getId() + ":";
        msg += x + ":";
        msg += y;*/
        msg.append(drone.getId());
        msg.append(":");
        msg.append(x);
        msg.append(":");
        msg.append(y);
        return msg.toString();
    }

    public void broadcastPerson(Person person) {
        if (person == null)
            return;

        broadcast(buildPersonMsg(person.getX(), person.getY()));
    }

    public String buildPosMsg(double x, double y) {
        // Realistically we should have things in here like a checksum/md5,
        // timestamp, msg id number, length, start/end characters, etc.  I'm going
        // to hold off on that for now as I dont know that the simulation needs it
        // yet.  But it might become interesting if we want to simulate things like
        // packet loss or content damage.
        /*String msg = "POS:";  // Of course ideally this would just be a byte code.
        msg += drone.getId() + ":";
        msg += x + ":";
        msg += y;*/
        StringBuilder msg = new StringBuilder("POS:");
        msg.append(drone.getId());
        msg.append(":");
        msg.append(x);
        msg.append(":");
        msg.append(y);
        return msg.toString();
    }

    public void broadcastPosition() {
        if (drone.ls == null)
            return;

        // Take from the location sensor, not necessarily the "real" position
        double x = drone.ls.x();
        double y = drone.ls.y();

        broadcast(buildPosMsg(x, y));
    }

    int currentDrawWidth = 0;
    public boolean draw(GraphicsContext gc) {
        // Draw some kind of range fan or something like that, maybe outwardly
        // moving circles almost like the wifi symbol
        
        currentDrawWidth += 3;
        if (currentDrawWidth > range * 2) {
            currentDrawWidth = 0;
        }
        gc.setStroke(Color.rgb(0, 0, 0, 0.6));
        gc.setLineWidth(2);
        gc.strokeOval(drone.x() - currentDrawWidth/2, drone.y() - currentDrawWidth/2, currentDrawWidth, currentDrawWidth);
        
        // Also draw the rest of it so we can see it properly
        gc.setFill(Color.rgb(125, 125, 125, 0.3));
        gc.fillOval(drone.x() - range, drone.y() - range, range * 2, range * 2);
        return true;
    }
}