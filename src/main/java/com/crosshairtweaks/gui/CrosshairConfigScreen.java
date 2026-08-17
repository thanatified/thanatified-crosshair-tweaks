package com.crosshairtweaks.gui;

import com.crosshairtweaks.config.CrosshairConfig;
import com.crosshairtweaks.config.CrosshairShape;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * A self-contained config screen - no external mod menu / config-lib
 * dependency needed. Open with the keybind (default: comma) or by adding a
 * button to it from another screen if you like.
 */
public class CrosshairConfigScreen extends Screen {

	private static final int LABEL_COLOR = 0xFFFFFFFF;
	private static final int LEFT_X = 20;
	private static final int RIGHT_X = 260;
	private static final int ROW_H = 22;
	private static final int WIDGET_W = 200;
	private static final int WIDGET_H = 18;

	private final Screen parent;
	private final CrosshairConfig config;

	public CrosshairConfigScreen(Screen parent, CrosshairConfig config) {
		super(Text.translatable("crosshairtweaks.title"));
		this.parent = parent;
		this.config = config;
	}

	@Override
	protected void init() {
		int leftY = 30;
		int rightY = 30;

		// ---------------- Normal crosshair (left column) ----------------
		leftY = addSectionLabel(leftY);

		leftY = addShapeCycleButton(LEFT_X, leftY, Text.translatable("crosshairtweaks.shape"),
				() -> config.shape, s -> config.shape = s);

		leftY = addIntSlider(LEFT_X, leftY, "crosshairtweaks.size", 1, 40, config.size, v -> config.size = v);
		leftY = addIntSlider(LEFT_X, leftY, "crosshairtweaks.thickness", 1, 12, config.thickness, v -> config.thickness = v);
		leftY = addIntSlider(LEFT_X, leftY, "crosshairtweaks.gap", 0, 20, config.gap, v -> config.gap = v);
		leftY = addColorField(LEFT_X, leftY, "crosshairtweaks.color", config.color, v -> config.color = v);

		leftY = addBoolButton(LEFT_X, leftY, "crosshairtweaks.outline", config.outline, v -> config.outline = v);
		leftY = addIntSlider(LEFT_X, leftY, "crosshairtweaks.outline_thickness", 0, 4, config.outlineThickness, v -> config.outlineThickness = v);
		leftY = addColorField(LEFT_X, leftY, "crosshairtweaks.outline", config.outlineColor, v -> config.outlineColor = v);

		leftY += 10;
		leftY = addBoolButton(LEFT_X, leftY, "crosshairtweaks.player.enabled", config.playerEnabled, v -> config.playerEnabled = v);
		leftY = addShapeCycleButton(LEFT_X, leftY, Text.translatable("crosshairtweaks.player.shape"),
				() -> config.playerShape, s -> config.playerShape = s);
		leftY = addColorField(LEFT_X, leftY, "crosshairtweaks.player.color", config.playerColor, v -> config.playerColor = v);
		leftY = addIntSlider(LEFT_X, leftY, "crosshairtweaks.player.size", 1, 40, config.playerSize, v -> config.playerSize = v);

		// ---------------- Environmental blend (right column) ----------------
		rightY = addBoolButton(RIGHT_X, rightY, "crosshairtweaks.blend.enabled", config.blendEnabled, v -> config.blendEnabled = v);
		rightY = addFloatSlider(RIGHT_X, rightY, "crosshairtweaks.blend.strength", 0f, 1f, config.blendStrength, v -> config.blendStrength = v);
		rightY = addBoolButton(RIGHT_X, rightY, "crosshairtweaks.blend.gray_fix", config.grayFix, v -> config.grayFix = v);
		rightY = addFloatSlider(RIGHT_X, rightY, "crosshairtweaks.blend.gray_threshold", 0f, 0.6f, config.grayThreshold, v -> config.grayThreshold = v);
		rightY = addColorField(RIGHT_X, rightY, "crosshairtweaks.blend.dark_color", config.darkModeColor, v -> config.darkModeColor = v);
		rightY = addColorField(RIGHT_X, rightY, "crosshairtweaks.blend.light_color", config.lightModeColor, v -> config.lightModeColor = v);

		// ---------------- Bottom buttons ----------------
		int bottomY = this.height - 30;
		this.addDrawableChild(ButtonWidget.builder(Text.translatable("crosshairtweaks.button.reset"), b -> {
			config.resetToDefaults();
			this.clearAndInit();
		}).dimensions(LEFT_X, bottomY, 100, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.translatable("crosshairtweaks.button.done"), b -> this.close())
				.dimensions(LEFT_X + 110, bottomY, 100, 20).build());
	}

	private int addSectionLabel(int y) {
		return y; // room reserved above the first widget for the title, drawn in render()
	}

	// -------- widget helpers --------

	private interface IntSetter { void set(int value); }
	private interface FloatSetter { void set(float value); }
	private interface BoolSetter { void set(boolean value); }
	private interface ShapeGetter { CrosshairShape get(); }
	private interface ShapeSetter { void set(CrosshairShape shape); }

	private int addIntSlider(int x, int y, String key, int min, int max, int current, IntSetter setter) {
		double initial = (current - min) / (double) (max - min);
		this.addDrawableChild(new SliderWidget(x, y, WIDGET_W, WIDGET_H,
				sliderText(key, current), clamp01(initial)) {
			@Override
			protected void updateMessage() {
				int value = min + (int) Math.round(this.value * (max - min));
				this.setMessage(sliderText(key, value));
			}

			@Override
			protected void applyValue() {
				int value = min + (int) Math.round(this.value * (max - min));
				setter.set(value);
			}
		});
		return y + ROW_H;
	}

	private int addFloatSlider(int x, int y, String key, float min, float max, float current, FloatSetter setter) {
		double initial = (current - min) / (double) (max - min);
		this.addDrawableChild(new SliderWidget(x, y, WIDGET_W, WIDGET_H,
				sliderTextF(key, current), clamp01(initial)) {
			@Override
			protected void updateMessage() {
				float value = min + (float) (this.value * (max - min));
				this.setMessage(sliderTextF(key, value));
			}

			@Override
			protected void applyValue() {
				float value = min + (float) (this.value * (max - min));
				setter.set(value);
			}
		});
		return y + ROW_H;
	}

	private int addBoolButton(int x, int y, String key, boolean current, BoolSetter setter) {
		ButtonWidget[] holder = new ButtonWidget[1];
		ButtonWidget button = ButtonWidget.builder(boolText(key, current), b -> {
			boolean now = !isOn(holder[0]);
			setter.set(now);
			holder[0].setMessage(boolText(key, now));
		}).dimensions(x, y, WIDGET_W, WIDGET_H).build();
		holder[0] = button;
		button.setMessage(boolText(key, current));
		this.addDrawableChild(button);
		return y + ROW_H;
	}

	// stash current bool state in the button's message text since ButtonWidget
	// has no generic user-data slot - cheap and avoids an extra field per row.
	private boolean isOn(ButtonWidget button) {
		return button.getMessage().getString().endsWith("ON");
	}

	private Text boolText(String key, boolean value) {
		return Text.translatable(key).append(": " + (value ? "ON" : "OFF"));
	}

	private Text sliderText(String key, int value) {
		return Text.translatable(key).append(": " + value);
	}

	private Text sliderTextF(String key, float value) {
		return Text.translatable(key).append(String.format(": %.2f", value));
	}

	private int addShapeCycleButton(int x, int y, Text label, ShapeGetter getter, ShapeSetter setter) {
		ButtonWidget button = ButtonWidget.builder(
				label.copy().append(": ").append(Text.translatable(getter.get().translationKey())),
				b -> {
					CrosshairShape next = getter.get().next();
					setter.set(next);
					b.setMessage(label.copy().append(": ").append(Text.translatable(next.translationKey())));
				}
		).dimensions(x, y, WIDGET_W, WIDGET_H).build();
		this.addDrawableChild(button);
		return y + ROW_H;
	}

	private int addColorField(int x, int y, String key, int currentArgb, IntSetter setterAsArgb) {
		String hex = String.format("%06X", currentArgb & 0xFFFFFF);
		TextFieldWidget field = new TextFieldWidget(this.textRenderer, x + 90, y, WIDGET_W - 90, WIDGET_H,
				Text.translatable(key));
		field.setMaxLength(6);
		field.setText(hex);
		field.setChangedListener(text -> {
			String clean = text.replace("#", "").trim();
			if (clean.matches("[0-9a-fA-F]{6}")) {
				int rgb = Integer.parseInt(clean, 16);
				int alpha = (currentArgb >> 24) & 0xFF;
				if (alpha == 0) alpha = 0xFF;
				setterAsArgb.set((alpha << 24) | rgb);
			}
		});
		this.addDrawableChild(field);
		return y + ROW_H;
	}

	private static double clamp01(double v) {
		return Math.max(0.0, Math.min(1.0, v));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, LABEL_COLOR);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("crosshairtweaks.section.normal"), LEFT_X, 20, 0xFFAAAAAA);
		context.drawTextWithShadow(this.textRenderer, Text.translatable("crosshairtweaks.section.blend"), RIGHT_X, 20, 0xFFAAAAAA);

		// color field labels (drawn separately since TextFieldWidget has no built-in label)
		context.drawTextWithShadow(this.textRenderer, Text.translatable("crosshairtweaks.color"), LEFT_X, 20 + 4 * ROW_H + 6, 0xFFFFFFFF);
	}

	@Override
	public void close() {
		config.save();
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
