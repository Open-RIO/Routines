package jaci.openrio.module.routines;

import edu.wpi.first.wpilibj.SpeedController;
import jaci.openrio.module.routines.command.CommandDisable;
import jaci.openrio.module.routines.command.CommandPlayback;
import jaci.openrio.module.routines.command.CommandRecord;
import jaci.openrio.module.routines.command.CommandSet;
import jaci.openrio.toast.core.ToastBootstrap;
import jaci.openrio.toast.core.command.CommandBus;
import jaci.openrio.toast.core.loader.annotation.Branch;
import jaci.openrio.toast.lib.module.ModuleConfig;
import jaci.openrio.toast.lib.module.ToastStateModule;
import jaci.openrio.toast.lib.registry.Registrar;
import jaci.openrio.toast.lib.state.RobotState;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


@Branch(branch = "jaci.openrio.module.routines.addon.ToastDroidHandler", dependency = "ToastDroid", method = "toast_droid")
public class Routines extends ToastStateModule {

    static Routines instance;
    static File home;

    @Override
    public String getModuleName() {
        return "Routines";
    }

    @Override
    public String getModuleVersion() {
        return "0.3.0";
    }

    @Override
    public void prestart() {
        instance = this;
        home = new File(ToastBootstrap.toastHome, "system/routines");
        home.mkdirs();

        ModuleConfig prefs = new ModuleConfig("Routines");
        RoutineHeartbeat.heart_rate = prefs.getInt("heartbeat.length", 30);

        CommandBus.registerCommand(new CommandPlayback());
        CommandBus.registerCommand(new CommandRecord());
        CommandBus.registerCommand(new CommandSet());
        CommandBus.registerCommand(new CommandDisable());
    }

    public static SpeedController[] getAllControllers() {
        return (SpeedController[]) Stream.concat(Registrar.pwmRegistrar.stream(), Registrar.canRegistrar.stream())
                .filter(a -> { return a instanceof SpeedController; }).toArray();
    }

    @Override
    public void start() { }

    static LinkedList<RoutineContext> contexts = new LinkedList<>();

    public static Routines getInstance() {
        return instance;
    }

    public static RoutineContext getRoutine(String name) {
        File file = new File(home, name + ".routine");
        RoutineContext context = new RoutineContext(file);
        contexts.add(context);
        return context;
    }

    public static void setDefault(String routine) throws IOException {
        FileWriter writer = new FileWriter(new File(home, "user.config"));
        writer.write(routine);
        writer.close();
    }

    public static String getDefault() throws IOException {
        File file = new File(home, "user.config");
        if (!file.exists())
            setDefault("default");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String ln = reader.readLine();
        reader.close();
        return ln;
    }

    public static void stopAll() {
        for (RoutineContext cont : contexts) {
            try {
                cont.stop();
                for (SpeedController c : getAllControllers()) c.set(0);
            } catch (Exception e) {
            }
        }
    }

    public static String[] getAvailableRoutines() {
        File[] files = home.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".routine");
            }
        });
        if (files == null) return new String[0];
        String[] fn = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            fn[i] = files[i].getName().replace(".routine", "");
        }
        return fn;
    }

    @Override
    public void tickState(RobotState state) { }

    @Override
    public void transitionState(RobotState state, RobotState oldState) {
        if (state == RobotState.AUTONOMOUS) {
            try {
                String def = getDefault();
                if (!def.equals("") && !def.equals("--disabled")) {
                    RoutineContext context = getRoutine(def);
                    context.setMotors(getAllControllers());
                    if (context.recorded())
                        context.startPlayback();
                }
            } catch (Exception e) {}
        } else
            stopAll();
    }
}
