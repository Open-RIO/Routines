package jaci.openrio.module.routines.addon;

import jaci.openrio.module.android.tile.Tile;
import jaci.openrio.module.android.tile.TileTicker;
import jaci.openrio.module.routines.Routines;

import java.io.IOException;

public class ToastDroidHandler {

    static Tile tile;
    static int selection;

    public static void toast_droid() {
        setIndex();
        tile = new Tile("routines_module", "Routines") {
            public String[] getSubtitles() {
                String[] subs;
                String[] routines = Routines.getAvailableRoutines();
                if (routines.length > 0) {
                    subs = new String[2];
                    subs[0] = "Active: " + routines[selection];
                    subs[1] = "Available: " + String.join(", ", routines);
                } else {
                    subs = new String[1];
                    subs[0] = "No Routines Available";
                }
                return subs;
            }

            public void onTouch() {
                cycle();
                try {
                    String[] routines = Routines.getAvailableRoutines();
                    if (routines.length > 0)
                        Routines.setDefault(routines[selection]);
                } catch (Exception e) {}
            }
        };
        TileTicker.register(tile);
    }

    public static void setIndex() {
        try {
            String def = Routines.getDefault();
            String[] routines = Routines.getAvailableRoutines();
            for (int i = 0; i < routines.length; i++)
                if (routines[i].equals(def))
                    selection = i;
        } catch (IOException e) { }
    }

    public static void cycle() {
        selection++;
        if (selection >= Routines.getAvailableRoutines().length)
            selection = 0;
        tile.update();
    }

}
