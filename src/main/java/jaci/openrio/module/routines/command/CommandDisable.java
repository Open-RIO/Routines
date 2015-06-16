package jaci.openrio.module.routines.command;

import jaci.openrio.module.routines.Routines;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.IHelpable;
import jaci.openrio.toast.core.command.UsageException;

import java.io.IOException;

public class CommandDisable extends AbstractCommand implements IHelpable {
    @Override
    public String getCommandName() {
        return "rdisable";
    }

    @Override
    public String getHelp() {
        return "Turns off Routines during autonomous";
    }

    @Override
    public void invokeCommand(int argLength, String[] args, String command) {
        if (args.length != 0) throw new UsageException("rdisable");
        try {
            Routines.setDefault("--disabled");
        } catch (IOException e) { }
    }
}
