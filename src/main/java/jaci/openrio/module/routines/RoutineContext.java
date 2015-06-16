package jaci.openrio.module.routines;

import edu.wpi.first.wpilibj.SpeedController;
import jaci.openrio.toast.core.thread.HeartbeatListener;

import java.io.*;

public class RoutineContext implements HeartbeatListener {

    SpeedController[] controllers;
    File file;
    int hb_count;
    RoutineSection current_section;

    boolean reading;
    BufferedReader reader;
    String last_section;

    boolean writing;
    OutputStream out;

    boolean closed;
    long startTime;

    public RoutineContext(File file) {
        this.file = file;
    }

    public void setMotors(SpeedController... controllers) {
        this.controllers = controllers;
    }

    public SpeedController[] getMotors() {
        return controllers;
    }

    public void startPlayback() {
        if (reading) throw new IllegalStateException("Already playing back!");
        try {
            reset();
            reader = new BufferedReader(new FileReader(file));
            reading = true;
            RoutineHeartbeat.add(this);
        } catch (Exception e) {
            RoutineHeartbeat.log().error("Could not find Routine");
        }
    }

    public void startRecording() {
        if (writing) throw new IllegalStateException("Already recording!");
        try {
            reset();
            out = new FileOutputStream(file);
            writing = true;
            RoutineHeartbeat.add(this);
        } catch (Exception e) {
        }
    }

    public boolean recorded() {
        return file.exists();
    }

    public void watchDog() {
        if (System.currentTimeMillis() - startTime >= 15 * 1000) {
            stop();
        }
    }

    public void stop() {
        reading = false;
        writing = false;
    }

    public void reset() {
        hb_count = 0;
        startTime = 0;
    }

    @Override
    public void onHeartbeat(int skipped) {
        try {
            if (reading || writing) {
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                    RoutineHeartbeat.log().info("Starting Routine");
                }
                watchDog();
                hb_count += 1 + skipped;
                if (reading)
                    readTick();
                if (writing)
                    writeTick();
            } else close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        try {
            out.close();
        } catch (Exception e) { }
        try {
            reader.close();
        } catch (Exception e) { }
        RoutineHeartbeat.remove(this);
        RoutineHeartbeat.log().info("Routine stopped");
    }

    public void readTick() throws IOException {
        if (last_section == null) {
            last_section = reader.readLine();
        } else if (last_section.equals("DONE")) {
            close();
            return;
        }
        current_section = new RoutineSection();
        current_section.parse(last_section);
        last_section = current_section.read(reader);
        if (current_section.heartbeat < hb_count) {
            readTick();
        } else if (current_section.heartbeat == hb_count) {
            current_section.execute(this);
        }
    }

    public void writeTick() throws IOException {
        RoutineSection newSection = new RoutineSection();
        newSection.load(this);
        newSection.write(hb_count, out, current_section);
        current_section = newSection;
    }

}
