package me.obbyaddons.client.util;

import java.util.ArrayList;
import java.util.List;

public final class ServerTickTracker {

    private static final List<Runnable> LISTENERS = new ArrayList<>();

    private ServerTickTracker() {
    }

    public static void init() {
        System.out.println("[ObbyAddons] ServerTickTracker initialized.");
    }

    public static void register(Runnable listener) {
        LISTENERS.add(listener);
    }

    public static void fireServerTick() {
        for (Runnable listener : LISTENERS) {
            listener.run();
        }
    }
}