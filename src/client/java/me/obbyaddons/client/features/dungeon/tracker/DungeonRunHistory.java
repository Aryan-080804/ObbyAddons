package me.obbyaddons.client.features.dungeon.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DungeonRunHistory {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type LIST_TYPE =
            new TypeToken<List<DungeonRunRecord>>() {
            }.getType();

    private static final Path FILE =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("obbyaddons")
                    .resolve("dungeon_runs.json");

    private static final List<DungeonRunRecord> runs =
            new ArrayList<>();

    private DungeonRunHistory() {
    }

    public static void load() {

        runs.clear();

        if (!Files.exists(FILE)) {
            return;
        }

        try {

            String json =
                    Files.readString(FILE);

            List<DungeonRunRecord> loaded =
                    GSON.fromJson(
                            json,
                            LIST_TYPE
                    );

            if (loaded != null) {
                runs.addAll(loaded);
            }

        } catch (Exception exception) {

            System.err.println(
                    "[ObbyAddons] Failed to load dungeon run history."
            );

            exception.printStackTrace();
        }
    }

    public static void save() {

        try {

            Files.createDirectories(
                    FILE.getParent()
            );

            Files.writeString(
                    FILE,
                    GSON.toJson(
                            runs,
                            LIST_TYPE
                    )
            );

        } catch (IOException exception) {

            System.err.println(
                    "[ObbyAddons] Failed to save dungeon run history."
            );

            exception.printStackTrace();
        }
    }

    public static void addRun(
            DungeonRunRecord run
    ) {

        if (run == null) {
            return;
        }

        runs.add(run);

        save();
    }

    public static List<DungeonRunRecord> getRuns() {

        return Collections.unmodifiableList(
                runs
        );
    }

    public static DungeonRunRecord getById(
            String id
    ) {

        if (id == null) {
            return null;
        }

        for (DungeonRunRecord run : runs) {

            if (id.equals(run.getId())) {
                return run;
            }
        }

        return null;
    }

    public static DungeonRunRecord getLatestRun() {

        if (runs.isEmpty()) {
            return null;
        }

        return runs.get(
                runs.size() - 1
        );
    }

    public static int getTotalRuns() {
        return runs.size();
    }

    public static void clear() {

        runs.clear();

        save();
    }
}
