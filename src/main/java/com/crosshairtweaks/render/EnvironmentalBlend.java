package com.crosshairtweaks.render;

import com.crosshairtweaks.config.CrosshairConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public final class EnvironmentalBlend {

    private EnvironmentalBlend() {}

    private static final int SAMPLE_RADIUS = 2;

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

    private static float[] sampleBackground() {
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        if (window == null) {
            return null;
        }

        Framebuffer fb = client.getFramebuffer();
        if (fb == null || fb.getColorAttachment() == null) {
            return null;
        }

        int texWidth = fb.textureWidth;
        int texHeight = fb.textureHeight;

        if (texWidth <= 0 || texHeight <= 0) {
            return null;
        }

        int centerX = texWidth / 2;
        int centerY = texHeight / 2;

        int size = SAMPLE_RADIUS * 2 + 1;
        int startX = Math.max(0, centerX - SAMPLE_RADIUS);
        int startY = Math.max(0, centerY - SAMPLE_RADIUS);

        int pixelCount = texWidth * texHeight;
        ByteBuffer fullBuffer = BufferUtils.createByteBuffer(4 * pixelCount);

        // ⭐ Correct API for MC 1.21.11 — GpuTexture has the GL ID
        int texId = fb.getColorAttachment().getGlId();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, fullBuffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        long sumR = 0, sumG = 0, sumB = 0;
        int sampleCount = 0;

        for (int y = 0; y < size; y++) {
            int py = startY + y;
            if (py < 0 || py >= texHeight) continue;

            for (int x = 0; x < size; x++) {
                int px = startX + x;
                if (px < 0 || px >= texWidth) continue;

                int index = (py * texWidth + px) * 4;
                int r = fullBuffer.get(index) & 0xFF;
                int g = fullBuffer.get(index + 1) & 0xFF;
                int b = fullBuffer.get(index + 2) & 0xFF;

                sumR += r;
                sumG += g;
                sumB += b;
                sampleCount++;
            }
        }

        if (sampleCount == 0) {
            return null;
        }

        return new float[]{
                (sumR / (float) sampleCount) / 255f,
                (sumG / (float) sampleCount) / 255f,
                (sumB / (float) sampleCount) / 255f
        };
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
