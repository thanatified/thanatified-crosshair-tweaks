package com.crosshairtweaks.gui;

import com.crosshairtweaks.CrosshairTweaksClient;
import com.crosshairtweaks.config.CrosshairConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class CrosshairConfigScreen extends Screen {

    private final Screen parent;

    // Local copies of config values
    private boolean highlightPlayers;
    private double crosshairSize;
    private boolean environmentBlend;

    public CrosshairConfigScreen(Screen parent) {
        super(Text.literal("Crosshair Tweaks Settings"));
        this.parent = parent;

        // Load current config values
        CrosshairConfig cfg = CrosshairTweaksClient.CONFIG;
        this.highlightPlayers = cfg.highlightPlayers;
        this.crosshairSize = cfg.crosshairSize;
        this.environmentBlend = cfg.environmentBlend;
    }

    @Override
    protected void init() {

        // Title
        this.addDrawableChild(new TextWidget(
                this.width / 2 - 80, 20, 160, 20,
                Text.literal("Crosshair Tweaks Settings"),
                this.textRenderer
        ));

        // Toggle: Highlight Players
        CheckboxWidget highlightToggle = CheckboxWidget.builder(
                Text.literal("Highlight Players"), this.textRenderer)
                .pos(this.width / 2 - 100, 60)
                .checked(highlightPlayers)
                .build();

        highlightToggle.onPress();
        this.addDrawableChild(highlightToggle);

        // Slider: Crosshair Size
        SliderWidget sizeSlider = new SliderWidget(
                this.width / 2 - 100, 100, 200, 20,
                Text.literal("Crosshair Size: " + (int) crosshairSize),
                crosshairSize / 10.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Crosshair Size: " + (int) (this.value * 10)));
            }

            @Override
            protected void applyValue() {
                crosshairSize = this.value * 10;
            }
        };

        this.addDrawableChild(sizeSlider);

        // Toggle: Environment Blend
        CheckboxWidget blendToggle = CheckboxWidget.builder(
                Text.literal("Environment Blend"), this.textRenderer)
                .pos(this.width / 2 - 100, 140)
                .checked(environmentBlend)
                .build();

        blendToggle.onPress();
        this.addDrawableChild(blendToggle);

        // Save Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {

            // Write values back into config
            CrosshairConfig cfg = CrosshairTweaksClient.CONFIG;
            cfg.highlightPlayers = highlightToggle.isChecked();
            cfg.crosshairSize = crosshairSize;
            cfg.environmentBlend = blendToggle.isChecked();

            cfg.save();

            this.client.setScreen(parent);

        }).dimensions(this.width / 2 - 100, 180, 90, 20).build());

        // Cancel Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 10, 180, 90, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }
}
