package hu.bme.aut.smeyelframework.timing;

import android.util.Log;

import org.opencv.core.Core;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class for (CPU) tick based time related operations.
 * <p>
 * Manages a measurement name -- metadata mapping, and calculates run time of measurements.
 * <p>
 * Created on 2014.09.21..
 * @author Ákos Pap
 */
public class Timing {

    private static Timing instance;

    public static Timing instance() {
        return instance;
    }

    public static void init() {
        instance = new Timing();
    }

    private Timing() {
        tickFrequency = Core.getTickFrequency();
        log = new MeasurementLog();
    }

    /** ################## END OF SINGLETON #################################################### **/

    public static final String TAG = "Timing";

    /** Ticks per second */
    private final double tickFrequency;
    private final MeasurementLog log;

    /**
     * Stores the {@link hu.bme.aut.smeyelframework.timing.Timing.Metadata} instances,
     * indexed by the name of the measurement.
     */
    private Map<String, Metadata> map = new HashMap<>();

    public static long getCurrentTickstamp() {
        return Core.getTickCount();
    }

    public static long getTickStampAtDelta(long deltaMs) {
        return Core.getTickCount() + (long)(deltaMs/1000.0 * Core.getTickFrequency());
    }

    public static long asMillis(long ticks) {
        return (long) (ticks / Core.getTickFrequency() * 1000);
    }

    /**
     * Container to store data related to a specific measurement.
     */
    private static class Metadata {
        public long startTick = 0;
        public boolean isRunning = false;

        public double sum = 0;
        public int count = 0;
    }

    /**
     * Starts a measurement with the given name.
     * @param measurementName The name of the measurement.
     */
    public synchronized void start(String measurementName) {
        if (! map.containsKey(measurementName)) {
            map.put(measurementName, new Metadata());
        }

        Metadata md = map.get(measurementName);
        md.startTick = Core.getTickCount(); // TickCount must be used instead of CPUTickCount! See docs.
        md.isRunning = true;
    }

    /**
     * Calculates the run time of a measurement, and returns it in milliseconds.
     * Side effect: logs the measurementName and runtime (in ms) to {@link #log}.
     *
     * @param measurementName The name of the measurement.
     * @return Run time of the measurement in milliseconds.
     */
    public synchronized double stop(String measurementName) {
        long stopTick = Core.getTickCount(); // NOT CPUTickCount! See docs!

        if (! map.containsKey(measurementName)) {
            Log.w(TAG, "Tried to stop a measurement that doesn't exist: " + measurementName);
            return -1;
        }

        Metadata md = map.get(measurementName);

        if (! md.isRunning) {
            Log.w(TAG, "Tried to stop a measurement that isn't started: " + measurementName);
            return -1;
        }

        double runTimeSec = (stopTick - md.startTick) / tickFrequency;
        double runTimeMs = ((int)(runTimeSec * 1000 * 1000)) / 1000.0;

        md.sum += runTimeSec;
        md.count++;
        md.isRunning = false;

        log.push(new ElapsedTimeLogItem(measurementName, runTimeMs));

        return runTimeMs;
    }

    public MeasurementLog getLog() {
        return log;
    }

}
