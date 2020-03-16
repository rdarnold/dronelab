package dronelab;

import java.util.ArrayList;

import dronelab.*;
import dronelab.utils.*;

// Try to keep this decoupled from the UI
public class SimRunner {
    DroneLab sim;
    int numRuns = 0;  // Set this to the run number you want to start from (120, 840, etc.) 
    int firstLine = numRuns / 10;  /// So if we start from run 320, we start on line 32, this is just for loading from the matrix
    int currentRepetition = 0;
    SimParams.AlgorithmFlag currentAlgorithm = SimParams.AlgorithmFlag.STANDARD;
    boolean runningFullSet = false; // Are we running all scenarios back to back or not?
    int timeLimitHours = 4;
    long startTime = 0;
    long endTime = 0;
    long lastRunStartTime = 0;
    long lastRunEndTime = 0;

    public int getCurrentRepetitionNum() { return currentRepetition; }

    // This is basically our class that allows us to run multiple
    // back to back simulations and things like that.  It can run
    // a sim, then adjust parameters, run another, etc.
    public SimRunner(DroneLab theSim) {
        sim = theSim;
    }

    public void signalSimComplete() {
        numRuns++;

        // Now print out some data for us, if we are doing full runs
        if (runningFullSet == true && lastRunStartTime > 0) {
            lastRunEndTime = System.currentTimeMillis();
            long millis = lastRunEndTime - lastRunStartTime;
            String strTimePassed = TimeData.printHMSFromMillis(millis);
            Utils.log("Run " + numRuns + " complete.  Total time taken: " + strTimePassed);
        }

        // We could put other types of runs in here if we wanted.
        if (sim.simMatrix.isEmpty() == false) {
            if (performRunFromMatrix() == false) {
                sim.stop();
            }
        } 
        else {
            if (performRunsDefault() == false) {
                sim.stop();
            }
        }
    }

    // These are run from the simulation matrix
    public void startRunsFromMatrix() {
        startTime = System.currentTimeMillis();
        runningFullSet = true; // Yes we are doing the full set here.
        sim.stop();

        // Now set up the params based on whatever we want to do
        currentAlgorithm = SimParams.AlgorithmFlag.MIX_SRA;
        SimParams params = sim.scenario.simParams;
        params.setSimMatrix(sim.simMatrix);
        params.setSimMatrixItem(null); // We will set this correctly in a moment
        params.setNumRepetitions(10);
        params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        params.setAlgorithmFlag(currentAlgorithm);
        params.setup(firstLine); // Start from the first line in our matrix
        performRunFromMatrix();
    }

    
    // These are our runs as loaded from the simulation matrix
    private boolean performRunFromMatrix() {
        // Get the next line from the matrix
        SimParams params = sim.scenario.simParams;
        SimMatrix mat = params.getSimMatrix();

        if (numRuns >= (mat.size() * params.getNumRepetitions())) {
            sim.draw = true;
            runningFullSet = false;
            Utils.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            Utils.log("All runs complete.");

            // Keep upping the end time each time a run ends so that we are looking at
            // the total time from the beginning of the simulation.
            /*endTime = System.currentTimeMillis();
            long milliSecondsPassed = endTime - startTime;
            String strTimePassed = TimeData.printHMSFromMillis(milliSecondsPassed);

            // Done.
            Utils.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            if (!nextAlgorithm()) {
                // Basically reset things in case the person wants to actually
                // use the system now.  Then print out some data and finish.
                sim.draw = true;
                runningFullSet = false;
                Utils.log("All runs complete.  Total time taken: " + strTimePassed);
                return false;
            }
            Utils.log("One set of algorithm runs complete.  Total time taken: " + strTimePassed);
            Utils.log("Proceeding to next set.");
            scenario.applyAlgorithm();
            numRuns = 0;
            params.setNumDrones(1);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);*/
            return false; // Done
        }

        // There are X number of reps within each ONE run
        currentRepetition++;

        //sim.draw = false; // Change this to true if we want to watch it in action.

        if (currentRepetition == (params.getNumRepetitions()+1)) {
            currentRepetition = 1;
            // Now set up the params based on the matrix item
            if (params.setup(numRuns/params.getNumRepetitions()) == false) {
                return false;
            }

            // And now redeploy
            Utils.log("One set of sim runs complete.");
            Utils.log("Proceeding to next set.");

            // Re-apply because we've adjusted the proportion of drone roles
            sim.scenario.applyAlgorithm();
        } 

        // Set a new start time for this run
        lastRunStartTime = System.currentTimeMillis();

        sim.reset();  // This is actually where "deploy" is called
        sim.start();  // This basically just starts up the animation timer again
        return true;
    }

    // Start our sim runs based off of hard-coded and/or loaded parameters
    public void startRuns() {
        currentRepetition = 0;

        if (sim.simMatrix.isEmpty() == false) {
            // If we loaded simulation matrix, run from that
            startRunsFromMatrix();
        }
        else {
            // Otherwise do our default runs - these were the final ones for the Japan dronelab work
            startRunsDefault();
        }
    }

    public void startRunsDefault() {
        startTime = System.currentTimeMillis();
        runningFullSet = true; // Yes we are doing the full set here.
        sim.stop();

        // Now set up the params based on whatever we want to do
        currentAlgorithm = SimParams.AlgorithmFlag.STANDARD;
        SimParams params = sim.scenario.simParams;
        params.setSimMatrixItem(null);
        params.setNumDrones(1);
        params.setNumRepetitions(5);
        params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        params.setAlgorithmFlag(currentAlgorithm);
        performRunsDefault();
    }

    // These are our standard runs, we'll do 1, 5, 10, and 20 drones
    // with the half/half distribution, and we'll do each one x times
    // so that we get a really nice distribution curve
    //private int repetitions = 5;
    private boolean performRunsDefault() {
        
        // Do we want to run a new one or not? 
        if (runningFullSet == false) {
            return false;
        }

        Scenario scenario = sim.scenario;
        sim.draw = true; // Change this to true if we want to watch it in action.

        SimParams params = scenario.simParams;
        if (numRuns == params.getNumRepetitions()) {
            params.setNumDrones(5);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns == params.getNumRepetitions() * 2) {
            params.setNumDrones(10);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns == params.getNumRepetitions() * 3) {
            params.setNumDrones(20);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns >= params.getNumRepetitions() * 4) {
            // Keep upping the end time each time a run ends so that we are looking at
            // the total time from the beginning of the simulation.
            endTime = System.currentTimeMillis();
            long milliSecondsPassed = endTime - startTime;
            String strTimePassed = TimeData.printHMSFromMillis(milliSecondsPassed);

            // Done.
            Utils.print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            if (!nextAlgorithm()) {
                // Basically reset things in case the person wants to actually
                // use the system now.  Then print out some data and finish.
                sim.draw = true;
                runningFullSet = false;
                Utils.log("All runs complete.  Total time taken: " + strTimePassed);
                return false;
            }
            Utils.log("One set of algorithm runs complete.  Total time taken: " + strTimePassed);
            Utils.log("Proceeding to next set.");
            scenario.applyAlgorithm();
            numRuns = 0;
            params.setNumDrones(1);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        }

        // Set a new start time for this run
        lastRunStartTime = System.currentTimeMillis();

        sim.reset();
        sim.start();
        return true;
    }

    private boolean nextAlgorithm() {
        SimParams params = sim.scenario.simParams;
        params.setAlgorithmFlag(currentAlgorithm);
        if (currentAlgorithm == SimParams.AlgorithmFlag.STANDARD) {
            currentAlgorithm = SimParams.AlgorithmFlag.SPIRAL;
            params.setAlgorithmFlag(currentAlgorithm);
            return true;
        }
        else if (currentAlgorithm == SimParams.AlgorithmFlag.SPIRAL) {
            currentAlgorithm = SimParams.AlgorithmFlag.SCATTER;
            params.setAlgorithmFlag(currentAlgorithm);
            return true;
        }

        // Done
        return false;
    }
}