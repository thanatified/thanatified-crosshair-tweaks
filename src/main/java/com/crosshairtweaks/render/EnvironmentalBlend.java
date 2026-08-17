package com.crosshairtweaks.render;

import com.crosshairtweaks.config.CrosshairConfig;
import com.crosshairtweaks.config.SamplingMode;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public final class EnvironmentalBlend {

    private EnvironmentalBlend() {}

    public static int computeColor(CrosshairConfig config, int baseArgb) {
        if (!config.blendEnabled) {
            return baseArgb;
        }

        float[] bg = sampleBackground(config);
        if (bg == null) {
            return baseArgb;
        }

        float r = bg[0], g = bg[1], b = bg[2];
        float luminance = 0.299f * r + 0.587f * g + 0.114f * b;
        float maxC = Math.max(r, Math.max(g, b));
        float minC = Math.min(r, Math.min(g, b));
        float chroma = maxC - minC;

        float targetR, targetG, targetB;

        if (config.grayFix && chroma < config.grayThreshold) {
            int snapColor = luminance < 0.5f ? config.darkModeColor : config.lightModeColor;
            targetR = ((snapColor >> 16) & 0xFF) / 255f;
            targetG = ((snapColor >> 8) & 0xFF) / 255f;
            targetB = (snapColor & 0xFF) / 255f;
        } else {
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

    private static float[] sampleBackground(CrosshairConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (window == null) {
            return null;
        }

        int fbWidth = window.getFramebufferWidth();
        int fbHeight = window.getFramebufferHeight();
        if (fbWidth <= 0 || fbHeight <= 0) {
            return null;
        }

        int radius;
        SamplingMode mode = config.samplingMode;
        if (mode == null) {
            mode = SamplingMode.SMALL;
        }

        switch (mode) {
            case CENTER:
                radius = 0;
                break;
            case SMALL:
                radius = 3;  // 7x7
                break;
            case LARGE:
                radius = 10; // 21x21
                break;
            default:
                radius = 3;
                break;
        }

        int size = radius * 2 + 1;

        int centerX = fbWidth / 2;
        int centerY = fbHeight / 2;

        int startX = Math.max(0, centerX - radius);
        int startY = Math.max(0, centerY - radius);

        ByteBuffer pixelBuffer = BufferUtils.createByteBuffer(4 * size * size);

        int previousReadFb = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        // Read from the currently bound framebuffer (Minecraft manages this internally)
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, 0);

        GL11.glReadPixels(startX, startY, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);

        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousReadFb);

        long sumR = 0, sumG = 0, sumB = 0;
        int count = size * size;

        for (int i = 0; i < count; i++) {
            int base = i * 4;
            int r = pixelBuffer.get(base) & 0xFF;
            int g = pixelBuffer.get(base + 1) & 0xFF;
            int b = pixelBuffer.get(base + 2) & 0xFF;

            sumR += r;
            sumG += g;
            sumB += b;
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
