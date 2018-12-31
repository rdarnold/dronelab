package dronelab;

import dronelab.utils.*;
public class BroadcastMessage {
    long fromId;
    String msg;
    Constants.CommType commType = Constants.CommType.WIFI;
    double x = 0;
    double y = 0;
    double range = 0;

    public BroadcastMessage(long from, double sentX, double sentY, double rng, String msgContents, Constants.CommType type) {
        fromId = from;
        x = sentX;
        y = sentY;
        range = rng;
        msg = msgContents;
        commType = type;
    }
}