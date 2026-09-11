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

    public boolean chatCleanerEnabled = true;
    public boolean dungeonSpamEnabled = true;
    public boolean m7BossSpamEnabled = true;

    public int stormTimerX = 100;
    public int stormTimerY = 60;
    public float stormTimerScale = 1.0f;
    public int stormDeathTimerX = 100;
    public int stormDeathTimerY = 80;
    public float stormDeathTimerScale = 1.0f;
    public int stormTimerColor = 0xFF7C3AED;
    public int stormDeathTimerColor = 0xFFFFFF55;
    public boolean stormSpiritMaskWarning = true;

    public boolean stormTickTimerEnabled = true;
    public boolean stormTickDownFrom5 = true;
    public boolean stormFirstDeathTime = true;

    public boolean stormLbEnabled = true;
    public int stormLbTickOffset = 0;
    public int stormLbColor = 0xFF7C3AED;

    public int stormLbX = 100;
    public int stormLbY = 100;
    public float stormLbScale = 1.0f;

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
            ObbyConfig loaded = GSON.fromJson(reader, ObbyConfig.class);

            if (loaded != null) {
                instance = loaded;
            }
        } catch (IOException exception) {
            System.err.println("[ObbyAddons] Failed to load config.");
            exception.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException exception) {
            System.err.println("[ObbyAddons] Failed to save config.");
            exception.printStackTrace();
        }
    }
}