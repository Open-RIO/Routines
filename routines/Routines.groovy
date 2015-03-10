import edu.wpi.first.wpilibj.SpeedController
import jaci.openrio.toast.core.StateTracker
import jaci.openrio.toast.core.ToastBootstrap
import jaci.openrio.toast.lib.module.GroovyScript
import jaci.openrio.toast.lib.state.RobotState
import jaci.openrio.toast.lib.state.StateListener
import jaci.openrio.toast.lib.log.Logger
import jaci.openrio.toast.core.shared.*

/**
 * The Autonomous Tracker module for Toast.
 * This module allows for Autonomous patterns (routines) to be recorded and
 * played back during competition
 *
 * @author Jaci
 */
public class Routines extends GroovyScript implements ModuleEventListener {

    static File patternHome
    static Logger logger
    static HashMap<String, AutonomousContext> allContexts
    static Routines instance

    public void loadScript() {
        patternHome = new File(ToastBootstrap.robotHome, "autonomous/")
        patternHome.mkdirs()
        allContexts = new HashMap<String, AutonomousContext>()
        logger = new Logger("Routines", Logger.ATTR_DEFAULT)
        ModuleEventBus.registerListener(this)
        instance = this
    }

    /**
     * Get the Routines instance
     */
    public static Routines getRoutines() {
      return instance
    }

    /**
     * Get the Context for the Given ID (Routine Name)
     */
    public AutonomousContext getContext(String id) {
        AutonomousContext context
        if (!allContexts.containsKey(id)) {
          context = new AutonomousContext(id)
          allContexts.put(id, context)
        } else
          context = allContexts.get(id)
        return context
    }

    /**
     * Halt Playback of all Routines
     */
    public void stopAll() {
        for (Map.Entry<String, AutonomousContext> context : allContexts) {
          context.getValue().stopPlayback()
        }
    }

    public void onModuleEvent(String sender, String event_type, Object... data) {
      if (event_type.startsWith("routines_")) {
        String func = event_type.replace("routines_", "")
        if (func.equals("create")) {
          getContext(data[0])
        } else if (func.equals("setControllers")) {
          getContext(data[0]).setControllers(data[1])
        } else if (func.equals("startRecording")) {
          getContext(data[0]).beginRecording()
        } else if (func.equals("stopRecording")) {
          getContext(data[0]).stopRecording()
        } else if (func.equals("stopPlayback")) {
          getContext(data[0]).stopPlayback()
        } else if (func.equals("startPlayback")) {
          getContext(data[0]).startPlayback()
        } else if (func.equals("stopAll")) {
          stopAll()
        }
      }
    }

    public static class AutonomousContext implements StateListener.Ticker {

        String identifier
        SpeedController[] controllers
        StringBuilder builder
        long startTime
        boolean recording
        boolean playback
        File saveFile
        ArrayList<String> lineData
        int lineIndex

        public AutonomousContext(String id) {
            this.identifier = id
            StateTracker.addTicker(this)
            saveFile = new File(patternHome, "${id}.routine")
        }

        /**
         * Set the SpeedControllers (motors) for this Routine
         */
        public void setControllers(SpeedController... controllers) {
            this.controllers = controllers
        }

        /**
         * Start recording for this Routine. Will automatically stop in 15 seconds
         */
        public void startRecording() {
            builder = new StringBuilder()
            recording = true
            startTime = System.currentTimeMillis()
        }

        /**
         * Manually stop recording for this routine
         */
        public void stopRecording() {
            recording = false
            saveFile.createNewFile()
            saveFile.withWriter {
                it.write(builder.toString())
            }
            builder = new StringBuilder()
            logger.info("Autonomous Recording Stopped: ${identifier}")
        }

        /**
         * Start playback for this routine. Will automatically stop in 15 seconds, or when the routine has ended
         */
        public void startPlayback() {
            saveFile.withReader {
                lineData = it.readLines()
                startTime = System.currentTimeMillis()
                lineIndex = 0
                playback = true
            }
        }

        /**
         * Manually stop playback for this routine
         */
        public void stopPlayback() {
            playback = false
            logger.info("Playback stopped: ${identifier}")
        }

        public static String format(int id, double pwm) {
            return String.format("%s:%s", id, pwm)
        }

        @Override
        void tickState(RobotState state) {
            if (recording) {
                if (System.currentTimeMillis() - startTime > 15 * 1000) {
                    stopRecording()
                    return
                }

                for (int i = 0; i < controllers.length; i++)
                    builder.append(format(i, controllers[i].get()) + "\n")
            }
            if (playback) {
                for (SpeedController controller : controllers) {
                    String[] split = lineData.get(lineIndex).split(":")
                    double pwm = Double.parseDouble(split[1])
                    controller.set(pwm)
                    lineIndex++
                }
                if (lineIndex >= lineData.size())
                    stopPlayback()
            }
        }
    }

}
