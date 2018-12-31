package dronelab;

import java.util.ArrayList;

import dronelab.*;
import dronelab.utils.*;

// Try to keep this decoupled from the UI
public class SimRunner {
    DroneLab sim;
    int numRuns = 0;
    SimParams.AlgorithmFlag currentAlgorithm = SimParams.AlgorithmFlag.STANDARD;
    boolean runningFullSet = false; // Are we running all scenarios back to back or not?
    int timeLimitHours = 4;
    long startTime = 0;
    long endTime = 0;
    long lastRunStartTime = 0;
    long lastRunEndTime = 0;

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
            Utils.log("One run complete.  Total time taken: " + strTimePassed);
        }

        // We could put other types of runs in here if we wanted.
        if (performRuns() == false) {
            sim.stop();
        }
    }

    // Start our sim runs based off of hard-coded parameters
    public void startRuns() {
	    startTime = System.currentTimeMillis();
        runningFullSet = true; // Yes we are doing the full set here.
        sim.stop();
        currentAlgorithm = SimParams.AlgorithmFlag.STANDARD;
        SimParams params = sim.scenario.simParams;
        params.setNumDrones(1);
        params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        params.setAlgorithmFlag(currentAlgorithm);
        performRuns();
    }

    // These are our standard runs, we'll do 1, 5, 10, and 20 drones
    // with the half/half distribution, and we'll do each one x times
    // so that we get a really nice distribution curve
    private int repetitions = 5;
    private boolean performRuns() {
        
        // Do we want to run a new one or not? 
        if (runningFullSet == false) {
            return false;
        }

        Scenario scenario = sim.scenario;
        sim.draw = false; // Change this to true if we want to watch it in action.

        SimParams params = scenario.simParams;
        if (numRuns == repetitions) {
            params.setNumDrones(5);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns == repetitions * 2) {
            params.setNumDrones(10);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns == repetitions * 3) {
            params.setNumDrones(20);
            params.setTimeLimitSeconds(TimeData.ONE_HOUR_IN_SECONDS * timeLimitHours);
        } else if (numRuns >= repetitions * 4) {
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