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

/**
 * Simple JSON-backed config for Crosshair Tweaks.
 * No Cloth Config required.
 */
public class CrosshairConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("crosshair_tweaks.json");

    // ---- Your config values ----
    public boolean highlightPlayers = true;
    public double crosshairSize = 5.0;
    public boolean environmentBlend = true;

    // ---- Load config ----
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

        // If file missing or broken → create fresh config
        CrosshairConfig fresh = new CrosshairConfig();
        fresh.save();
        return fresh;
    }

    // ---- Save config ----
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
