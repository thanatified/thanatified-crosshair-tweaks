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

    private boolean playerEnabled;
    private int playerSize;
    private int size;
    private int thickness;
    private int gap;
    private boolean outline;

    public CrosshairConfigScreen(Screen parent) {
        super(Text.literal("Crosshair Tweaks Settings"));
        this.parent = parent;

        CrosshairConfig cfg = CrosshairTweaksClient.CONFIG;
        this.playerEnabled = cfg.playerEnabled;
        this.playerSize = cfg.playerSize;
        this.size = cfg.size;
        this.thickness = cfg.thickness;
        this.gap = cfg.gap;
        this.outline = cfg.outline;
    }

    @Override
    protected void init() {

        this.addDrawableChild(new TextWidget(
                this.width / 2 - 80, 20, 160, 20,
                Text.literal("Crosshair Tweaks Settings"),
                this.textRenderer
        ));

        CheckboxWidget playerToggle = CheckboxWidget.builder(
                Text.literal("Player Highlight"), this.textRenderer)
                .pos(this.width / 2 - 100, 60)
                .checked(playerEnabled)
                .build();
        this.addDrawableChild(playerToggle);

        SliderWidget sizeSlider = new SliderWidget(
                this.width / 2 - 100, 100, 200, 20,
                Text.literal("Crosshair Size: " + size),
                size / 20.0
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Crosshair Size: " + (int) (this.value * 20)));
            }

            @Override
            protected void applyValue() {
                size = (int) (this.value * 20);
            }
        };
        this.addDrawableChild(sizeSlider);

        CheckboxWidget outlineToggle = CheckboxWidget.builder(
                Text.literal("Outline"), this.textRenderer)
                .pos(this.width / 2 - 100, 140)
                .checked(outline)
                .build();
        this.addDrawableChild(outlineToggle);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            CrosshairConfig cfg = CrosshairTweaksClient.CONFIG;

            cfg.playerEnabled = playerToggle.isChecked();
            cfg.size = size;
            cfg.outline = outlineToggle.isChecked();

            cfg.save();
            this.client.setScreen(parent);

        }).dimensions(this.width / 2 - 100, 180, 90, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 10, 180, 90, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}
