
package dronelab.collidable;

import java.util.ArrayList;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.ClassLoader;
import java.lang.reflect.InvocationTargetException;

import dronelab.Scenario;
import dronelab.collidable.behaviors.*;
import dronelab.collidable.equipment.*;
import dronelab.utils.*;

public final class BehaviorLoader {

    private static ArrayList<BehaviorModule> modules;

    private BehaviorLoader () { // private constructor
    }

    public static void setBehaviors(Drone drone) {
        // We only create behaviors from the ones that are listed in the drone's
        // behavior order.  Otherwise we dont know how to order them, which would still
        // technically "work" but the behavior could be really squirrely depending on 
        // how the modules were ordered.  Doing it this way, we "force" a drone maker
        // to specify the order of behaviors otherwise the behavior won't be processed.
        drone.behaviors = createBehaviorModuleList(drone.behaviorOrder.toArray(new String[0]));
        //drone.behaviors = createBehaviorModuleList(Constants.STR_BEHAVIORS);
        orderBehaviors(drone.behaviors, drone.behaviorOrder);

        for (BehaviorModule mod : drone.behaviors) {
            mod.assign(drone);
        }
    }

    public static ArrayList<String> getBehaviorFileNames() {
        ArrayList<String> list = new ArrayList<String>();
        File folder = new File("./bin/dronelab/collidable/behaviors");
        File[] listOfFiles = folder.listFiles();
        //Utils.log(folder.getName());

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                list.add(listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                // Maybe I should have like a custom behaviors folder.
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }
        return list;
    }

    public static BehaviorModule createModuleForClassName(String strFullyQualifiedClassName) {
        BehaviorModule newModule = null;
        try {
            Object newObj = Class.forName(strFullyQualifiedClassName).newInstance();
            if (newObj == null) {
                // This is not supposed to happen according to the documentation.
                Utils.err("Null class " + strFullyQualifiedClassName);
            }
            newModule = (BehaviorModule)newObj;
        }
        catch (ClassNotFoundException e) {
            Utils.err(e.toString());
            Utils.err("Class not found: " + strFullyQualifiedClassName);
        }
        catch (InstantiationException e) {
            Utils.err(e.toString());
            Utils.err("Could not create an instance of class " + strFullyQualifiedClassName);
        }
        catch (IllegalAccessException e) {
            Utils.err(e.toString());
            Utils.err("Could not invoke getFileExtension()"
            + " method from class " + strFullyQualifiedClassName);
        }
        return newModule;
    }

    // Dynamically load up all our behavior classes from the specified
    // folder.  This separates out our behavior modules and allows us to
    // create them outside of this program.
    // This does in fact work ... I appear to be able to dynamically
    // load classes dropped in the behaviors folder at runtime and plunk them
    // onto the list of drone behaviors.
    public static ArrayList<BehaviorModule> load() {
        modules = new ArrayList<BehaviorModule>();
        
        String strPackageName = "dronelab.collidable.behaviors."; 

        // Basically I want to build a list of filenames and use those as
        // the class names and try to load them all.  Inside a jar I'll have to
        // use streams somehow.
        ArrayList<String> fileNames = getBehaviorFileNames();
        for (String str : fileNames) {
            if (str.equals(BehaviorModule.class.getSimpleName() + ".class") == true) {
                // We don't want to load up the base module as it's abstract and already
                // loaded.
                continue;
            }
            String strFullyQualifiedClassName = strPackageName + Utils.removeFileExtension(str);
            //String strFullyQualifiedClassName = AvoidModule.class.getCanonicalName();
            // This will probably not work from within the jar if I want to
            // include behaviors in the jar.
            // Also I dont seem to need this - the Class.forName seems to load them
            // just fine.
            /*File file = new File("./bin/dronelab/collidable/behaviors");

            try {
                // Convert File to a URL
                URL url = file.toURL();          // file:/c:/myclasses/
                URL[] urls = new URL[]{url};

                // Create a new class loader with the directory
                ClassLoader cl = new URLClassLoader(urls);

                // Load in the class; MyClass.class should be located in
                // the directory file:/c:/myclasses/com/mycompany
                Class cls = cl.loadClass(strFullyQualifiedClassName);
            } 
            catch (Exception e) {
                Utils.err(e.toString());
            }*/
            BehaviorModule newModule = createModuleForClassName(strFullyQualifiedClassName);
            if (newModule != null) {
                modules.add(newModule);
            }
        }
        return modules;
    }

    public static ArrayList<BehaviorModule> createBehaviorModuleList(String[] strList) {
        ArrayList<BehaviorModule> list = new ArrayList<BehaviorModule>();

        for (String str : strList) {
            addModule(list, str);
        }
        return list;
    }

    public static BehaviorModule getModule(ArrayList<BehaviorModule> list, String strName) {
        if (list == null) {
            return null;
        }
        for (BehaviorModule mod : list) {
            if (mod.getName() == strName) {
                return mod;
            }
        }
        return null;
    }
    
    public static void addModuleToList(ArrayList<BehaviorModule> fromList,
                                       ArrayList<BehaviorModule> toList, 
                                       String strName) {
        BehaviorModule mod = getModule(fromList, strName);
        if (mod != null) {
            toList.add(mod);
        }
    }

    public static void updateBehaviorDrawPriorities(ArrayList<BehaviorModule> list) {
        int i = 1;
        for (BehaviorModule mod : list) {
            if (mod.usesDrawLocation() == true) {
                mod.setDrawPriority(i);
                i++;
            }
        }
    }

    // Properly order the behavior/subsumption list, otherwise we could get squirrely stuff.
    public static void orderBehaviors(ArrayList<BehaviorModule> list, ArrayList<String> order) {
        ArrayList<BehaviorModule> tempList = new ArrayList<BehaviorModule>();

        // How do I really want to order these, I need to provide some like external
        // ordering scheme.
        for (String str : order) {
            addModuleToList(list, tempList, str);
        }

        list.clear();
        list.addAll(tempList);

        // Now assign them all their correct priorities for drawing
        // locations, so that we can see 1, 2, 3, etc., for the locations
        // instead of all these different colors and symbols
        updateBehaviorDrawPriorities(list);
    }

    public static void removeModule(ArrayList<BehaviorModule> list, String strName) {
        BehaviorModule mod = getModule(list, strName);
            
        if (mod != null) {
            list.remove(mod);
        } 
    }

    public static BehaviorModule addModule(ArrayList<BehaviorModule> list, String strName) {
            return addModule(list, null, strName);
    }

    public static BehaviorModule addModule(ArrayList<BehaviorModule> list, 
                                           ArrayList<String> order, 
                                           String strName) {
        // Look through our list of loaded behaviors, find one with a matching name,
        // and add it.
        BehaviorModule added = null;
        for (BehaviorModule mod : modules) {
            if (strName.equals(mod.getName())) {
                added = createModuleForClassName(mod.getClass().getCanonicalName());
                if (added != null) {
                    list.add(added);
                    break;
                }
            }
        }

        // Then, if we can, also order it.
        if (order != null) {
            orderBehaviors(list, order);
        }
        return added;
    }
}