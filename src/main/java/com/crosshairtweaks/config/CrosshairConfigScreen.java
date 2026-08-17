package com.crosshairtweaks.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;

public class CrosshairConfigScreen {

    public static Screen create(Screen parent) {
        CrosshairConfig cfg = CrosshairConfig.load();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle("Thanatified Crosshair Tweaks");

        ConfigEntryBuilder entry = builder.entryBuilder();

        // ---- Normal crosshair ----
        ConfigCategory normal = builder.getOrCreateCategory("Normal Crosshair");

        normal.addEntry(entry.startEnumSelector("Shape", CrosshairShape.class, cfg.shape)
                .setSaveConsumer(v -> cfg.shape = v)
                .build());

        normal.addEntry(entry.startIntField("Size", cfg.size)
                .setMin(1).setMax(100)
                .setSaveConsumer(v -> cfg.size = v)
                .build());

        normal.addEntry(entry.startIntField("Thickness", cfg.thickness)
                .setMin(1).setMax(20)
                .setSaveConsumer(v -> cfg.thickness = v)
                .build());

        normal.addEntry(entry.startIntField("Gap", cfg.gap)
                .setMin(0).setMax(50)
                .setSaveConsumer(v -> cfg.gap = v)
                .build());

        normal.addEntry(entry.startColorField("Color", cfg.color)
                .setSaveConsumer(v -> cfg.color = v)
                .build());

        normal.addEntry(entry.startBooleanToggle("Outline", cfg.outline)
                .setSaveConsumer(v -> cfg.outline = v)
                .build());

        normal.addEntry(entry.startIntField("Outline Thickness", cfg.outlineThickness)
                .setMin(0).setMax(10)
                .setSaveConsumer(v -> cfg.outlineThickness = v)
                .build());

        normal.addEntry(entry.startColorField("Outline Color", cfg.outlineColor)
                .setSaveConsumer(v -> cfg.outlineColor = v)
                .build());

        // ---- Player-target crosshair ----
        ConfigCategory player = builder.getOrCreateCategory("Player Target Crosshair");

        player.addEntry(entry.startBooleanToggle("Enabled", cfg.playerEnabled)
                .setSaveConsumer(v -> cfg.playerEnabled = v)
                .build());

        player.addEntry(entry.startEnumSelector("Shape", CrosshairShape.class, cfg.playerShape)
                .setSaveConsumer(v -> cfg.playerShape = v)
                .build());

        player.addEntry(entry.startColorField("Color", cfg.playerColor)
                .setSaveConsumer(v -> cfg.playerColor = v)
                .build());

        player.addEntry(entry.startIntField("Size", cfg.playerSize)
                .setMin(1).setMax(100)
                .setSaveConsumer(v -> cfg.playerSize = v)
                .build());

        player.addEntry(entry.startIntField("Thickness", cfg.playerThickness)
                .setMin(1).setMax(20)
                .setSaveConsumer(v -> cfg.playerThickness = v)
                .build());

        // ---- Environmental blend ----
        ConfigCategory blend = builder.getOrCreateCategory("Environmental Blend");

        blend.addEntry(entry.startBooleanToggle("Enabled", cfg.blendEnabled)
                .setSaveConsumer(v -> cfg.blendEnabled = v)
                .build());

        blend.addEntry(entry.startFloatField("Blend Strength", cfg.blendStrength)
                .setMin(0f).setMax(1f)
                .setSaveConsumer(v -> cfg.blendStrength = v)
                .build());

        blend.addEntry(entry.startBooleanToggle("Gray Fix", cfg.grayFix)
                .setSaveConsumer(v -> cfg.grayFix = v)
                .build());

        blend.addEntry(entry.startFloatField("Gray Threshold", cfg.grayThreshold)
                .setMin(0f).setMax(1f)
                .setSaveConsumer(v -> cfg.grayThreshold = v)
                .build());

        blend.addEntry(entry.startColorField("Dark Mode Color", cfg.darkModeColor)
                .setSaveConsumer(v -> cfg.darkModeColor = v)
                .build());

        blend.addEntry(entry.startColorField("Light Mode Color", cfg.lightModeColor)
                .setSaveConsumer(v -> cfg.lightModeColor = v)
                .build());

        // ⭐ NEW: Sampling Mode (CENTER / SMALL / LARGE)
        blend.addEntry(entry.startEnumSelector("Sampling Mode", SamplingMode.class, cfg.samplingMode)
                .setSaveConsumer(v -> cfg.samplingMode = v)
                .build());

        builder.setSavingRunnable(cfg::save);

        return builder.build();
    }
}
