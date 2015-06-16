package jaci.openrio.module.routines.command;

import jaci.openrio.module.routines.RoutineContext;
import jaci.openrio.module.routines.RoutineHeartbeat;
import jaci.openrio.module.routines.Routines;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.IHelpable;

import java.io.IOException;

public class CommandRecord extends AbstractCommand implements IHelpable {
    @Override
    public String getCommandName() {
        return "rrecord";
    }

    @Override
    public String getHelp() {
        return "Use this to record a routine to playback. Will give a 5 second countdown.";
    }

    @Override
    public void invokeCommand(int argLength, String[] args, String command) {
        String name = null;
        try {
            name = args.length == 1 ? args[0] : Routines.getDefault();
            final RoutineContext context = Routines.getRoutine(name);
            context.setMotors(Routines.getAllControllers());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        RoutineHeartbeat.log().info("5...");
                        Thread.sleep(1000);
                        RoutineHeartbeat.log().info("4..");
                        Thread.sleep(1000);
                        RoutineHeartbeat.log().info("3..");
                        Thread.sleep(1000);
                        RoutineHeartbeat.log().info("2.");
                        Thread.sleep(1000);
                        RoutineHeartbeat.log().info("1.");
                        Thread.sleep(1000);
                        context.startRecording();
                    } catch (Exception e) { }
                }
            }).start();
        } catch (IOException e) { }
    }
}
