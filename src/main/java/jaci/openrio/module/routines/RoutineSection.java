package jaci.openrio.module.routines;

import edu.wpi.first.wpilibj.SpeedController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutineSection {

    static Pattern motorPattern = Pattern.compile("(\\d+):[-+]?((\\d*[.])?\\d+)");      //Gr1: Motor ID, Gr2: Value Full
    static Pattern sectionPattern = Pattern.compile("S:(\\d+)");                      //Gr1: Section Hb ID

    int heartbeat;
    List<MotorChangeValue> values;

    public RoutineSection() {
        values = new LinkedList<>();
    }

    public void parse(String line) {
        Matcher match = sectionPattern.matcher(line);
        match.matches();
        this.heartbeat = Integer.parseInt(match.group(1));
    }

    public void load(RoutineContext context) {
        SpeedController[] controllers = context.getMotors();
        for (int i = 0; i < controllers.length; i++) {
            MotorChangeValue val = new MotorChangeValue();
            val.motorid = i;
            val.value = (float) controllers[i].get();
            values.add(val);
        }
    }

    public void write(int hearbeat_val, OutputStream stream, RoutineSection previousSection) throws IOException {
        this.heartbeat = hearbeat_val;
        stream.write(("S:" + hearbeat_val + "\n").getBytes());
        for (MotorChangeValue myval : values) {
            int motor_id = myval.motorid;
            boolean contains = false;
            float cont_val = 0F;
            if (previousSection != null) {
                for (MotorChangeValue theirval : previousSection.values) {
                    if (theirval.motorid == motor_id) {
                        contains = true;
                        cont_val = theirval.value;
                    }
                }
            }

            if (!contains || cont_val != myval.value) {
                stream.write((motor_id + ":" + myval.value + "\n").getBytes());
            }
        }
    }

    public String read(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null) {
            if (line.equals("")) continue;
            Matcher m = motorPattern.matcher(line);
            if (m.matches()) {
                MotorChangeValue change = new MotorChangeValue();
                change.motorid = Integer.valueOf(m.group(1));
                change.value = Float.valueOf(m.group(2));
                values.add(change);
            } else if (sectionPattern.matcher(line).matches()) {
                return line;
            } else {
                break;
            }
            line = reader.readLine();
        }

        return "DONE";
    }

    public void execute(RoutineContext context) {
        SpeedController[] controllers = context.getMotors();
        for (MotorChangeValue change : values)
            controllers[change.motorid].set(change.value);
    }

    public static class MotorChangeValue {
        public int motorid;
        public float value;
    }

}
