package com.crosshairtweaks.render;

import com.crosshairtweaks.config.CrosshairConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

/**
 * Vanilla's crosshair "blend" is just an inverted GL_COLOR_LOGIC_OP.
 * Inverting a color close to middle gray (~0.5, 0.5, 0.5) produces another
 * color close to middle gray, so on desaturated stone/deepslate/concrete
 * the crosshair all but disappears. This class fixes that by:
 *
 *  1. Reading the actual rendered pixels behind the crosshair.
 *  2. Detecting when the background is "gray" (low saturation) and, if so,
 *     snapping straight to pure black or white instead of inverting - this
 *     is the fix that specifically targets the gray-block problem.
 *  3. For colorful backgrounds, inverting but pushing the result further
 *     from mid-gray ("sharper" / more saturated) by a configurable amount.
 */
public final class EnvironmentalBlend {

	private EnvironmentalBlend() {
	}

	// small sample box so a single noisy/edge pixel doesn't flicker the result
	private static final int SAMPLE_RADIUS = 2;

	private static final ByteBuffer PIXEL_BUFFER =
			BufferUtils.createByteBuffer(4 * (SAMPLE_RADIUS * 2 + 1) * (SAMPLE_RADIUS * 2 + 1));

	/**
	 * @return an ARGB int (alpha forced to 0xFF) representing what the crosshair
	 *         color should be, given the configured base color and the pixels
	 *         currently on screen behind it.
	 */
	public static int computeColor(CrosshairConfig config, int baseArgb) {
		if (!config.blendEnabled) {
			return baseArgb;
		}

		float[] bg = sampleBackground();
		if (bg == null) {
			return baseArgb;
		}

		float r = bg[0], g = bg[1], b = bg[2];
		float luminance = 0.299f * r + 0.587f * g + 0.114f * b;
		float maxC = Math.max(r, Math.max(g, b));
		float minC = Math.min(r, Math.min(g, b));
		float chroma = maxC - minC; // 0 = perfectly gray, 1 = fully saturated

		float targetR, targetG, targetB;

		if (config.grayFix && chroma < config.grayThreshold) {
			// Background is effectively gray - snap to whichever extreme
			// contrasts hardest instead of producing another mid-gray.
			int snapColor = luminance < 0.5f ? config.darkModeColor : config.lightModeColor;
			targetR = ((snapColor >> 16) & 0xFF) / 255f;
			targetG = ((snapColor >> 8) & 0xFF) / 255f;
			targetB = (snapColor & 0xFF) / 255f;
		} else {
			// Colorful background: invert, then boost saturation/contrast so
			// the result reads as a "sharper" color rather than a washed-out
			// complementary tone.
			float invR = 1f - r;
			float invG = 1f - g;
			float invB = 1f - b;

			float boost = 1.4f;
			targetR = clamp01(0.5f + (invR - 0.5f) * boost);
			targetG = clamp01(0.5f + (invG - 0.5f) * boost);
			targetB = clamp01(0.5f + (invB - 0.5f) * boost);
		}

		float baseR = ((baseArgb >> 16) & 0xFF) / 255f;
		float baseG = ((baseArgb >> 8) & 0xFF) / 255f;
		float baseB = (baseArgb & 0xFF) / 255f;

		float strength = clamp01(config.blendStrength);
		int finalR = Math.round(lerp(baseR, targetR, strength) * 255f);
		int finalG = Math.round(lerp(baseG, targetG, strength) * 255f);
		int finalB = Math.round(lerp(baseB, targetB, strength) * 255f);

		int alpha = (baseArgb >> 24) & 0xFF;
		return (alpha << 24) | (finalR << 16) | (finalG << 8) | finalB;
	}

	/**
	 * Reads a small box of pixels from the main framebuffer, centered on
	 * where the crosshair is drawn, and returns the average color as
	 * normalized floats, or null if it couldn't be read (e.g. no window yet).
	 */
	private static float[] sampleBackground() {
		MinecraftClient client = MinecraftClient.getInstance();
		Window window = client.getWindow();
		if (window == null) {
			return null;
		}

		Framebuffer fb = client.getFramebuffer();
		if (fb == null) {
			return null;
		}

		int fbWidth = window.getFramebufferWidth();
		int fbHeight = window.getFramebufferHeight();

		int centerX = fbWidth / 2;
		// OpenGL's glReadPixels origin is bottom-left, screen space is top-left,
		// but since we want the exact vertical center this flip is a no-op -
		// it's kept explicit here so the math stays correct if an offset is
		// ever added above/below center.
		int centerY = fbHeight - (fbHeight / 2);

		int size = SAMPLE_RADIUS * 2 + 1;
		int startX = Math.max(0, centerX - SAMPLE_RADIUS);
		int startY = Math.max(0, centerY - SAMPLE_RADIUS);

		PIXEL_BUFFER.clear();

		int previousReadFb = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, fb.fbo);
		GL11.glReadPixels(startX, startY, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, PIXEL_BUFFER);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFb);

		long sumR = 0, sumG = 0, sumB = 0;
		int count = size * size;
		for (int i = 0; i < count; i++) {
			int base = i * 4;
			sumR += PIXEL_BUFFER.get(base) & 0xFF;
			sumG += PIXEL_BUFFER.get(base + 1) & 0xFF;
			sumB += PIXEL_BUFFER.get(base + 2) & 0xFF;
		}

		return new float[]{
				(sumR / (float) count) / 255f,
				(sumG / (float) count) / 255f,
				(sumB / (float) count) / 255f
		};
	}

	private static float clamp01(float v) {
		return Math.max(0f, Math.min(1f, v));
	}

	private static float lerp(float a, float b, float t) {
		return a + (b - a) * t;
	}
}
