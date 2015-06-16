package jaci.openrio.module.routines.command;

import jaci.openrio.module.routines.Routines;
import jaci.openrio.toast.core.command.AbstractCommand;
import jaci.openrio.toast.core.command.IHelpable;
import jaci.openrio.toast.core.command.UsageException;

import java.io.IOException;

public class CommandSet extends AbstractCommand implements IHelpable {
    @Override
    public String getCommandName() {
        return "rset";
    }

    @Override
    public String getHelp() {
        return "Sets the default routine to use during autonomous.";
    }

    @Override
    public void invokeCommand(int argLength, String[] args, String command) {
        if (args.length != 1) throw new UsageException("rset <routine name>");
        try {
            Routines.setDefault(args[0]);
        } catch (IOException e) { }
    }
}
