package jaci.openrio.module.routines;

import jaci.openrio.toast.core.thread.HeartbeatListener;
import jaci.openrio.toast.lib.log.Logger;
import jaci.openrio.toast.lib.state.ConcurrentVector;

/**
 * A faster-running version of the Heartbeat suitable for Routines recording.
 *
 * @author Jaci
 */
public class RoutineHeartbeat implements Runnable {

    static ConcurrentVector<HeartbeatListener> aorta = new ConcurrentVector<>();
    static long heart_rate = 20;
    static Logger nervous_system;
    int skipped_beats;
    static boolean running;
    int consecutive;

    @Override
    public void run() {
        while (true) {
            try {
                running = true;
                long start = System.currentTimeMillis();
                aorta.tick();

                for (HeartbeatListener artery : aorta)
                    artery.onHeartbeat(skipped_beats);

                long end = System.currentTimeMillis();
                long tick_time = end - start;

                skipped_beats = 0;
                if (tick_time < heart_rate) {
                    Thread.sleep(heart_rate - tick_time);
                    consecutive = 0;
                } else if (tick_time > heart_rate) {
                    skipped_beats = (int)(tick_time / heart_rate);
                    consecutive++;
                    if (consecutive < 3) {
                        log().warn(String.format("Heartbeat skipped %s beats, (took %sms of max %sms)", skipped_beats, tick_time, heart_rate));
                    } else if (consecutive == 3) {
                        log().warn("Too many consecutive skipped Heartbeats, suppressing log.");
                    }
                    Thread.sleep(tick_time % heart_rate);
                }

                if (aorta.size() == 0) {
                    running = false;
                    return;
                }
            } catch (Exception e) {}
        }
    }

    /**
     * Get the logger for the Heartbeat
     */
    public static Logger log() {
        if (nervous_system == null)
            nervous_system = new Logger("Routines", Logger.ATTR_TIME);
        return nervous_system;
    }

    /**
     * Add a listener to the Heartbeat. This can be done at any time
     */
    public static void add(HeartbeatListener artery) {
        aorta.addConcurrent(artery);
        checkActive();
    }

    /**
     * Remove a listener from the Heartbeat. This can be done at any time.
     */
    public static void remove(HeartbeatListener artery) {
        aorta.removeConcurrent(artery);
    }

    static Thread heart;

    static void checkActive() {
        if (!running) {
            heart = new Thread(new RoutineHeartbeat());
            heart.setName("Routines");
            heart.start();
        }
    }
}
