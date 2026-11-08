package com.anglehud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AngleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("angle_hud.json");

    public int r = 255;
    public int g = 255;
    public int b = 255;
    public boolean shadow = true;
    public int decimals = 1;
    /** Mitte der Anzeige als Bruchteil der GUI-Breite/-Höhe. */
    public double x = 0.5;
    public double y = 0.545;

    private static AngleConfig instance = new AngleConfig();

    public static AngleConfig get() {
        return instance;
    }

    public int rgb() {
        return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public void resetPosition() {
        x = 0.5;
        y = 0.545;
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            AngleConfig c = GSON.fromJson(reader, AngleConfig.class);
            if (c != null) instance = c;
        } catch (Exception e) {
            AngleHudClient.LOGGER.error("Konnte Config nicht laden", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(instance, w);
            }
        } catch (Exception e) {
            AngleHudClient.LOGGER.error("Konnte Config nicht speichern", e);
        }
    }
}
