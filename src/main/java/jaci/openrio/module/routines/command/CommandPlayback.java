package jaci.openrio.module.routines.command;

import jaci.openrio.module.routines.RoutineContext;
import jaci.openrio.module.routines.Routines;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.IHelpable;

import java.io.IOException;

public class CommandPlayback extends AbstractCommand implements IHelpable {
    @Override
    public String getCommandName() {
        return "rplay";
    }

    @Override
    public String getHelp() {
        return "Use this to playback a pre-recorded routine";
    }

    @Override
    public void invokeCommand(int argLength, String[] args, String command) {
        String name = null;
        try {
            name = args.length == 1 ? args[0] : Routines.getDefault();
            RoutineContext context = Routines.getRoutine(name);
            context.setMotors(Routines.getAllControllers());
            context.startPlayback();
        } catch (IOException e) { }

    }
}
