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
 * Holds every user-configurable value for the mod and knows how to
 * load/save itself as JSON in the game's config directory.
 *
 * All colors are stored as 0xAARRGGBB ints (alpha in the top byte) so they
 * can be fed straight into DrawContext's fill/text methods.
 */
public class CrosshairConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("thanatifiedcrosshairtweaks.json");

	// ---- Normal crosshair ----
	public CrosshairShape shape = CrosshairShape.CROSS;
	public int size = 10;            // arm length in pixels, at GUI scale 1
	public int thickness = 2;        // arm thickness in pixels
	public int gap = 3;              // gap from the exact center
	public int color = 0xFFFFFFFF;   // white, fully opaque
	public boolean outline = true;
	public int outlineThickness = 1;
	public int outlineColor = 0xFF000000; // black

	// ---- Player-target crosshair ----
	public boolean playerEnabled = true;
	public CrosshairShape playerShape = CrosshairShape.DOT;
	public int playerColor = 0xFFFF3B30; // red
	public int playerSize = 10;
	public int playerThickness = 2;

	// ---- Environmental blend ----
	public boolean blendEnabled = true;
	public float blendStrength = 1.0f;   // 0 = off, 1 = full contrast correction
	public boolean grayFix = true;       // force pure black/white instead of an inverted gray
	public float grayThreshold = 0.18f;  // how "flat" a background needs to be to count as gray
	public int darkModeColor = 0xFFFFFFFF; // color used over dark backgrounds
	public int lightModeColor = 0xFF000000; // color used over light backgrounds

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

	public void resetToDefaults() {
		CrosshairConfig fresh = new CrosshairConfig();
		this.shape = fresh.shape;
		this.size = fresh.size;
		this.thickness = fresh.thickness;
		this.gap = fresh.gap;
		this.color = fresh.color;
		this.outline = fresh.outline;
		this.outlineThickness = fresh.outlineThickness;
		this.outlineColor = fresh.outlineColor;
		this.playerEnabled = fresh.playerEnabled;
		this.playerShape = fresh.playerShape;
		this.playerColor = fresh.playerColor;
		this.playerSize = fresh.playerSize;
		this.playerThickness = fresh.playerThickness;
		this.blendEnabled = fresh.blendEnabled;
		this.blendStrength = fresh.blendStrength;
		this.grayFix = fresh.grayFix;
		this.grayThreshold = fresh.grayThreshold;
		this.darkModeColor = fresh.darkModeColor;
		this.lightModeColor = fresh.lightModeColor;
	}
}
