package com.crosshairtweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CrosshairConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("crosshair_tweaks.json");

    // ---- Crosshair base ----
    public boolean playerEnabled = true;
    public CrosshairShape playerShape = CrosshairShape.DEFAULT;
    public int playerColor = 0xFFFFFFFF;
    public int playerSize = 5;
    public int playerThickness = 2;

    public CrosshairShape shape = CrosshairShape.DEFAULT;
    public int color = 0xFFFFFFFF;
    public int size = 5;
    public int thickness = 2;
    public int gap = 2;

    public boolean outline = false;
    public int outlineThickness = 1;
    public int outlineColor = 0xFF000000;

    // ---- Environmental blend ----
    public boolean blendEnabled = true;
    public float blendStrength = 1.0f;

    public boolean grayFix = false;
    public float grayThreshold = 0.2f;

    public int darkModeColor = 0xFF000000;
    public int lightModeColor = 0xFFFFFFFF;

    public SamplingMode samplingMode = SamplingMode.BASIC;

    public static CrosshairConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
                CrosshairConfig cfg = GSON.fromJson(reader, CrosshairConfig.class);
                if (cfg != null) {
                    return cfg;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("[Crosshair Tweaks] Failed to load config, using defaults: " + e);
            }
        }

        CrosshairConfig fresh = new CrosshairConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[Crosshair Tweaks] Failed to save config: " + e);
        }
    }
}
