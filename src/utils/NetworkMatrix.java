package dronelab.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import java.io.*;
import java.nio.file.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;


import dronelab.*;
import dronelab.collidable.*;
import dronelab.utils.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// This class saves out the current connectivity of all the drones periodically as either a text file,
// or an Excel file
// and as a DynetML xml file
public final class NetworkMatrix {
    private NetworkMatrix () {} // private constructor

    public static boolean[][] netMatrix; 
    public static boolean[][] prevMatrix; 
    public static String[][] attrMatrix; 

    public static int simSecondsBetweenSaves = 60; // Much faster than real-time when fast-forwarding

    public static int simSecondsBetweenXYZSaves = 1;

    public static boolean checkChanges = false; // Only save out if the network connectivity is different than last time


    public static Document doc = null;
    public static DocumentBuilderFactory docFactory;
    public static DocumentBuilder docBuilder;
    private static Element rootElement;
    private static int networkCount = 0;

    public static void reset() {
        netMatrix = null;
        prevMatrix = null;
    }

    private static void buildMatrix() {
        netMatrix = new boolean[DroneLab.scenario.drones.size()][DroneLab.scenario.drones.size()];
        attrMatrix = new String[DroneLab.scenario.drones.size()][6];

        int row = 0;
        int col = 0;
        double range = 0;
        for (Drone d : DroneLab.scenario.drones) {
            //create drone attribute matrix used for XML doc
            //attrMatrix[row][0] = Integer.toString(d.getDroneType().getValue());
            attrMatrix[row][1] = Integer.toString(d.getDroneRole().getValue());
            /*attrMatrix[row][2] = Double.toString(d.getMaxAscentSpeed());
            attrMatrix[row][3] = Double.toString(d.getMaxDescentSpeed());
            attrMatrix[row][4] = Double.toString(d.getMaxAccelerationRate());
            attrMatrix[row][5] = Double.toString(d.getMaxVerticalAccelerationRate());*/

            //populate network adjacency matrix
            col = 0;
            range = d.getWiFiRangePixels();
            for (Drone d2 : DroneLab.scenario.drones) {
                if (Physics.withinDistance(d.getX(), d.getY(), d2.getX(), d2.getY(), range) == true) {
                    netMatrix[row][col] = true;
                }
                col++;
            }
            row++;
        }
    }

    // Is current matrix different from previous one?
    private static boolean checkChanged() {
        // If we're not supposed to check, just always return true; i.e. always save
        if (checkChanges == false) {
            return true;
        }

        // If nothing to compare to, it's new and therefore changed / different
        if (netMatrix == null || prevMatrix == null) {
            return true;
        }

        // Works for 2-dimensional arrays
        return (Arrays.deepEquals(netMatrix, prevMatrix) == false);
        
        /*if (netMatrix.length != prevMatrix.length) {
            return true;
        }

        for (int i = 0; i < netMatrix.length; i++) {
            if (!Arrays.equals(netMatrix[i], prevMatrix[i])) {
                return true;
            }
        }
        return false;*/
        //return Arrays.equals(netMatrix, prevMatrix);
        
        /*for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (netMatrix[row][col] != prevMatrix[row][col]) {
                    return true;
                }
            }
        }
        return false;*/
    }

    public static void saveXYZ() {
        ArrayList<Drone> drones = DroneLab.scenario.drones;
        int size = drones.size();

        for (int i = 0; i < size; i++) {
            //time(s),x,y,z\r\n
            StringBuilder str = new StringBuilder();
            str.append("" + DroneLab.scenario.simTime.getTotalSeconds());
            str.append(",");
            str.append("" + drones.get(i).getCurrentXYZ());
            str.append("\r\n");
            // Format is Run_#_Drone_###_Type_#.csv
            String fname = "Run_" + (DroneLab.runner.getCurrentRunNum() + 1) + 
                        "_Drone_" + drones.get(i).getDataId() + 
                        "_Type_" + drones.get(i).getDroneRole().getValue() + ".csv";
            Utils.appendFile(str.toString(), "output/xyzData/"+fname);
        }
    }

    // Save out the current state of all the drones in the scenario in terms of which ones are within
    // communication range of which other ones.
    public static void save() {
        buildMatrix();

        if (checkChanged() == false) {
            return;
        }
        prevMatrix = netMatrix;

        // Now save it out to whatever file format.  For now it is CSV.
        StringBuilder str = new StringBuilder();

        ArrayList<Drone> drones = DroneLab.scenario.drones;
        int size = drones.size();

        // First output the row as 1,2,3,etc. just to make it clear to the person
        // looking at the file, which is which
        str.append(" ,"); // Append a blank space to start in the top left corner
        for (int i = 0; i < size; i++) {
            str.append("" + (i + 1));
            if (i < size-1) {
                str.append(",");
            }
        }
        str.append("\r\n");


        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                // Start with the A, B, C, etc., for the column
                if (col == 0) {
                    str.append("" + (row + 1));
                    str.append(",");
                }
                if (netMatrix[row][col] == true) {
                    str.append("1");
                }
                else {
                    str.append("0");
                }
                if (col < size-1) {
                    str.append(",");
                }
            }
            str.append("\r\n");
        }

        StringBuilder attrStr = new StringBuilder();
        // Now write out the attributes matrix, which is just to say, which of the drones is which role
        for (int i = 0; i < size; i++) {
            attrStr.append("" + (i + 1));
            attrStr.append(",");
            attrStr.append("" + drones.get(i).getDroneRole().getValue());
            attrStr.append(",");
            attrStr.append("" + drones.get(i).getDroneRole().toString());
            attrStr.append("\r\n");
        }


        // Format is runNumber_timeStampInSeconds.csv
        String fname =  "Run-" + (DroneLab.runner.getCurrentRunNum() + 1) + 
                        "_Time-" + DroneLab.scenario.simTime.getTotalSeconds() + 
                        "_Located-" + DroneLab.scenario.getNumVictimsSeen();
        
        // Now write two files, one is the association matrix, the other is the attributes matrix
        Utils.writeFile(str.toString(), Constants.NET_MATRIX_DATA_SAVE_PATH + fname + "_Assoc.csv");
        Utils.writeFile(attrStr.toString(), Constants.NET_MATRIX_DATA_SAVE_PATH + fname + "_Attr.csv");
    }
    
    
    public static void resetDyNetML() {
        networkCount=0;
        // Generate xml root elements
        // initialize DynetXML
        // http://www.casos.cs.cmu.edu/projects/dynetml/
        // http://www.casos.cs.cmu.edu/projects/dynetml/dynetml_2_0-schema.xml
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
            rootElement = doc.createElement("DynamicMetaNetwork");
            doc.appendChild(rootElement);
        } catch(Exception e) {
            e.printStackTrace();
        }

    } 

     public static void appendDyNetML() {
        buildMatrix();

        if (checkChanged() == false) {
            return;
        }
        prevMatrix = netMatrix;

        //System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
        
        // Now save it out to whatever file format.  For now it is CSV.
        StringBuilder str = new StringBuilder();

        int size = DroneLab.scenario.drones.size();

        //system time
        LocalDateTime dateObj = LocalDateTime.now();
        DateTimeFormatter formatDateObj = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH:mm:ss");
        String  formattedDT = dateObj.format(formatDateObj);

        try {  
            Element MetaNetwork =  doc.createElement("MetaNetwork");
            MetaNetwork.setAttribute("id",Integer.toString(networkCount));
            networkCount++;
            //MetaNetwork.setAttribute("date",formattedDT);
            MetaNetwork.setAttribute("timePeriod", DroneLab.scenario.simTime.toString());
            rootElement.appendChild(MetaNetwork);
            
            //XML static elements & attributes
            Element nodes = doc.createElement("nodes");
            MetaNetwork.appendChild(nodes);
            Element nodeClass = doc.createElement("nodeclass");
            nodeClass.setAttribute("type","agent");
            nodeClass.setAttribute("id","drone");
            nodes.appendChild(nodeClass);
            Element propertyIdentities = doc.createElement("propertyIdentities");
            nodeClass.appendChild(propertyIdentities);
            /*Element propertyIdentity0 = doc.createElement("propertyIdentity");
            propertyIdentity0.setAttribute("id","droneType");
            propertyIdentity0.setAttribute("type","number");
            propertyIdentity0.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity0);*/
            Element propertyIdentity1 = doc.createElement("propertyIdentity");
            propertyIdentity1.setAttribute("id","droneRole");
            propertyIdentity1.setAttribute("type","number");
            propertyIdentity1.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity1);
            /*Element propertyIdentity2 = doc.createElement("propertyIdentity");
            propertyIdentity2.setAttribute("id","maxAscentSpeed");
            propertyIdentity2.setAttribute("type","number");
            propertyIdentity2.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity2);
            Element propertyIdentity3 = doc.createElement("propertyIdentity");
            propertyIdentity3.setAttribute("id","maxDescentSpeed");
            propertyIdentity3.setAttribute("type","number");
            propertyIdentity3.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity3);
            Element propertyIdentity4 = doc.createElement("propertyIdentity");
            propertyIdentity4.setAttribute("id","maxAccelerationRate");
            propertyIdentity4.setAttribute("type","number");
            propertyIdentity4.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity4);
            Element propertyIdentity5 = doc.createElement("propertyIdentity");
            propertyIdentity5.setAttribute("id","maxVerticalAccelerationRate");
            propertyIdentity5.setAttribute("type","number");
            propertyIdentity5.setAttribute("singleValued","false");
            propertyIdentities.appendChild(propertyIdentity5);*/

            //Create network elements
            Element networks = doc.createElement("networks");
            MetaNetwork.appendChild(networks);
            Element network = doc.createElement("network");
            network.setAttribute("source","drone");
            network.setAttribute("sourceType","Agent");
            network.setAttribute("target","drone");
            network.setAttribute("targetType","Agent");
            network.setAttribute("id","drone x drone");
            network.setAttribute("isDirected", "false");
            network.setAttribute("allowSelfLoops", "false");
            networks.appendChild(network);
                        
            for (int row = 0; row < size; row++) {
                //Create node for each drone & add properties
                Element droneNode = doc.createElement("node");
                droneNode.setAttribute("id",Integer.toString(row));
                nodeClass.appendChild(droneNode);
                //Element propertyDroneType = doc.createElement("property");
                Element propertyDroneRole = doc.createElement("property");
                /*Element propertyMaxAscentSpeed = doc.createElement("property");
                Element propertyMaxDescentSpeed = doc.createElement("property");
                Element propertyMaxAccelerationRate = doc.createElement("property");
                Element propertyMaxVerticalAccelerationRate = doc.createElement("property");*/
                //propertyDroneType.setAttribute("id","droneType");
                //propertyDroneType.setAttribute("value",attrMatrix[row][0]);
                propertyDroneRole.setAttribute("id","droneRole");
                propertyDroneRole.setAttribute("value",attrMatrix[row][1]);
                /*propertyMaxAscentSpeed.setAttribute("id","maxAscentSpeed");
                propertyMaxAscentSpeed.setAttribute("value",attrMatrix[row][2]);
                propertyMaxDescentSpeed.setAttribute("id","maxDescentSpeed");
                propertyMaxDescentSpeed.setAttribute("value",attrMatrix[row][3]);
                propertyMaxAccelerationRate.setAttribute("id","maxAccelerationRate");
                propertyMaxAccelerationRate.setAttribute("value",attrMatrix[row][4]);
                propertyMaxVerticalAccelerationRate.setAttribute("id","maxVerticalAccelerationRate");
                propertyMaxVerticalAccelerationRate.setAttribute("value",attrMatrix[row][5]);*/
                //droneNode.appendChild(propertyDroneType);
                droneNode.appendChild(propertyDroneRole);
                /*droneNode.appendChild(propertyMaxAscentSpeed);
                droneNode.appendChild(propertyMaxDescentSpeed);
                droneNode.appendChild(propertyMaxAccelerationRate);
                droneNode.appendChild(propertyMaxVerticalAccelerationRate);*/

                //iterate through adjacency matrix
                for (int col = 0; col < size; col++) {
                    //create edge but dont create duplicate
                    int edgeCreated = 0;
                    Element link = doc.createElement("link");
                    if (col >= row) {
                        link.setAttribute("source", Integer.toString(row));
                        link.setAttribute("target", Integer.toString(col));
                        edgeCreated = 1;
                    }

                    if (netMatrix[row][col] == true) {
                        str.append("1");    
                        if (edgeCreated == 1) {               
                            link.setAttribute("value", "1");
                        }
                    }
                    else {
                        str.append("0");
                        if (edgeCreated == 1) {
                            link.setAttribute("value", "0");
                        }
                    }
                    if (col < size-1) {
                        str.append(",");
                    }

                    if (edgeCreated == 1) {
                        network.appendChild(link);
                    }
                    
                }
                str.append("\r\n");
            }
        } 
        catch(Exception e) {
            e.printStackTrace();
        }    
    }   

    public static void saveDyNetML() {
    
        // Now save it out to whatever file format.  For now it is CSV.

        String xmlfname =  "DyNetML_Run-" + (DroneLab.runner.getCurrentRunNum() + 1) + 
                    "_Time-" + DroneLab.scenario.simTime.getTotalSeconds() + 
                    "_Located-" + DroneLab.scenario.getNumVictimsSeen() + 
                    ".xml";
        File xmlFilePath = new File(Constants.NET_MATRIX_DATA_SAVE_PATH + xmlfname);
        
        try {
            FileOutputStream output = new FileOutputStream(xmlFilePath);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(output);

            transformer.transform(source, result);
        } catch(Exception e) {
            e.printStackTrace();
        }
                
    } 
}