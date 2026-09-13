package me.obbyaddons.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ObbyConfig {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("obbyaddons.json");

    private static ObbyConfig instance = new ObbyConfig();

    // =========================
    // CHAT CLEANER
    // =========================

    public boolean chatCleanerEnabled = true;
    public boolean dungeonSpamEnabled = true;
    public boolean m7BossSpamEnabled = true;

    // =========================
    // STORM TIMER
    // =========================

    public int stormTimerX = 100;
    public int stormTimerY = 60;
    public float stormTimerScale = 1.0f;

    public int stormTimerColor = 0xFF7C3AED;

    public boolean stormTickTimerEnabled = true;
    public boolean stormTickDownFrom5 = true;

    // =========================
    // FIRST DEATH TIMER
    // =========================

    public boolean stormFirstDeathTime = true;

    public int stormDeathTimerX = 100;
    public int stormDeathTimerY = 80;
    public float stormDeathTimerScale = 1.0f;

    public int stormDeathTimerColor = 0xFFFFFF55;

    // =========================
    // SPIRIT MASK
    // =========================

    public boolean stormSpiritMaskWarning = true;

    // =========================
    // STORM LAST BREATH
    // =========================

    public boolean stormLbEnabled = true;

    public int stormLbTickOffset = 0;
    public int stormLbColor = 0xFF7C3AED;

    public int stormLbX = 100;
    public int stormLbY = 100;
    public float stormLbScale = 1.0f;

    // =========================
    // EXPLOSIVE ARROW
    // =========================

    // Main feature toggle
    public boolean explosiveArrowEnabled = true;

    // Only controls whether the HUD is visible
    public boolean explosiveArrowDamageTrackerHudEnabled = true;

    // HUD color
    public int explosiveArrowDamageTrackerColor = 0xFF7C3AED;

    public int explosiveArrowDamageTrackerX = 10;
    public int explosiveArrowDamageTrackerY = 10;
    public float explosiveArrowDamageTrackerScale = 1.0f;

    // =========================
    // TERMINATOR OVERLAY
    // =========================

    public boolean terminatorOverlayEnabled = true;
    public int terminatorOverlayColor = 0xFFFFFFFF;

    // =========================
    // CLICK PROT DISPLAY
    // =========================

    public boolean terminalClickTimerEnabled = true;

    public int terminalClickDelayMs = 400;

    public int terminalClickTimerColor = 0xFFFFFFFF;

    public int terminalClickTimerX = 100;
    public int terminalClickTimerY = 30;

    public float terminalClickTimerScale = 1.0f;

    // =========================
    // DUNGEON RUN TRACKER
    // =========================

    public boolean dungeonRunTrackerEnabled = true;
    public boolean dungeonRunTrackerHudEnabled = true;

    public int dungeonRunTrackerX = 10;
    public int dungeonRunTrackerY = 120;

    public float dungeonRunTrackerScale = 1.0f;

    public int dungeonRunTrackerColor = 0xFFFFFFFF;

    // =========================
    // ITEM HIGHLIGHT
    // =========================

    public boolean dungeonItemHighlightEnabled = false;

    // =========================
    // PEARL TRAJECTORY
    // =========================

    public boolean pearlTrajectoryEnabled =
            false;

    public int pearlTrajectoryColor =
            0xFF7C3AED;

    private ObbyConfig() {
    }

    public static ObbyConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ObbyConfig loaded =
                    GSON.fromJson(reader, ObbyConfig.class);

            if (loaded != null) {
                instance = loaded;
            }

        } catch (IOException exception) {
            System.err.println(
                    "[ObbyAddons] Failed to load config."
            );

            exception.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(
                    CONFIG_PATH.getParent()
            );

            try (Writer writer =
                         Files.newBufferedWriter(CONFIG_PATH)) {

                GSON.toJson(
                        instance,
                        writer
                );
            }

        } catch (IOException exception) {
            System.err.println(
                    "[ObbyAddons] Failed to save config."
            );

            exception.printStackTrace();
        }
    }
}