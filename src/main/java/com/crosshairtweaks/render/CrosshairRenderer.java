package com.crosshairtweaks.render;

import com.crosshairtweaks.config.CrosshairShape;
import net.minecraft.client.gui.DrawContext;

/**
 * Draws a crosshair shape centered on (centerX, centerY).
 * All shapes are drawn with axis-aligned fills (and Bresenham line-plotting
 * for diagonals), so no textures are needed - color/size/thickness can be
 * changed freely without needing new art assets.
 */
public final class CrosshairRenderer {

	private CrosshairRenderer() {
	}

	public static void draw(DrawContext context, int centerX, int centerY, CrosshairShape shape,
	                         int size, int thickness, int gap, int color,
	                         boolean outline, int outlineThickness, int outlineColor) {

		if (outline) {
			// Draw the shape slightly larger, in the outline color, first -
			// this reads as a border around the real crosshair and is a
			// cheap extra contrast guarantee on top of the smart blend.
			int pad = outlineThickness;
			drawShape(context, centerX, centerY, shape, size + pad, thickness + pad * 2, Math.max(0, gap - pad), outlineColor);
		}

		drawShape(context, centerX, centerY, shape, size, thickness, gap, color);
	}

	private static void drawShape(DrawContext context, int cx, int cy, CrosshairShape shape,
	                               int size, int thickness, int gap, int color) {
		switch (shape) {
			case CROSS -> drawCross(context, cx, cy, size, thickness, gap, color);
			case DOT -> drawDot(context, cx, cy, size, color);
			case CIRCLE -> drawCircleOutline(context, cx, cy, size, thickness, color);
			case SQUARE -> drawSquareOutline(context, cx, cy, size, thickness, color);
			case T_SHAPE -> drawTShape(context, cx, cy, size, thickness, gap, color);
			case CHEVRON -> drawChevron(context, cx, cy, size, thickness, gap, color);
			case DIAMOND -> drawDiamondOutline(context, cx, cy, size, thickness, color);
		}
	}

	private static void drawCross(DrawContext context, int cx, int cy, int size, int thickness, int gap, int color) {
		int half = thickness / 2;
		// left arm
		context.fill(cx - gap - size, cy - half, cx - gap, cy - half + thickness, color);
		// right arm
		context.fill(cx + gap, cy - half, cx + gap + size, cy - half + thickness, color);
		// top arm
		context.fill(cx - half, cy - gap - size, cx - half + thickness, cy - gap, color);
		// bottom arm
		context.fill(cx - half, cy + gap, cx - half + thickness, cy + gap + size, color);
	}

	private static void drawTShape(DrawContext context, int cx, int cy, int size, int thickness, int gap, int color) {
		int half = thickness / 2;
		// horizontal bar through the center
		context.fill(cx - size, cy - half, cx + size, cy - half + thickness, color);
		// single arm going down only
		context.fill(cx - half, cy + gap, cx - half + thickness, cy + gap + size, color);
	}

	private static void drawDot(DrawContext context, int cx, int cy, int size, int color) {
		int r = Math.max(1, size / 3);
		context.fill(cx - r, cy - r, cx + r, cy + r, color);
	}

	private static void drawSquareOutline(DrawContext context, int cx, int cy, int size, int thickness, int color) {
		int x0 = cx - size, x1 = cx + size, y0 = cy - size, y1 = cy + size;
		context.fill(x0, y0, x1, y0 + thickness, color); // top
		context.fill(x0, y1 - thickness, x1, y1, color); // bottom
		context.fill(x0, y0, x0 + thickness, y1, color); // left
		context.fill(x1 - thickness, y0, x1, y1, color); // right
	}

	private static void drawCircleOutline(DrawContext context, int cx, int cy, int radius, int thickness, int color) {
		int steps = Math.max(24, radius * 6);
		for (int i = 0; i < steps; i++) {
			double angle = (2 * Math.PI * i) / steps;
			int px = cx + (int) Math.round(Math.cos(angle) * radius);
			int py = cy + (int) Math.round(Math.sin(angle) * radius);
			context.fill(px - thickness / 2, py - thickness / 2,
					px - thickness / 2 + thickness, py - thickness / 2 + thickness, color);
		}
	}

	private static void drawDiamondOutline(DrawContext context, int cx, int cy, int size, int thickness, int color) {
		int top_x = cx, top_y = cy - size;
		int right_x = cx + size, right_y = cy;
		int bottom_x = cx, bottom_y = cy + size;
		int left_x = cx - size, left_y = cy;

		drawLine(context, top_x, top_y, right_x, right_y, thickness, color);
		drawLine(context, right_x, right_y, bottom_x, bottom_y, thickness, color);
		drawLine(context, bottom_x, bottom_y, left_x, left_y, thickness, color);
		drawLine(context, left_x, left_y, top_x, top_y, thickness, color);
	}

	private static void drawChevron(DrawContext context, int cx, int cy, int size, int thickness, int gap, int color) {
		// A "^" shape pointing up, offset below center by the gap - handy for
		// a minimal always-on-target style indicator.
		int apexY = cy + gap;
		drawLine(context, cx, apexY, cx - size, apexY + size, thickness, color);
		drawLine(context, cx, apexY, cx + size, apexY + size, thickness, color);
	}

	/** Bresenham line plotting, stamping a thickness x thickness square per step. */
	private static void drawLine(DrawContext context, int x0, int y0, int x1, int y1, int thickness, int color) {
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy;
		int half = Math.max(1, thickness) / 2;

		int x = x0, y = y0;
		while (true) {
			context.fill(x - half, y - half, x - half + Math.max(1, thickness), y - half + Math.max(1, thickness), color);
			if (x == x1 && y == y1) break;
			int e2 = 2 * err;
			if (e2 >= dy) {
				err += dy;
				x += sx;
			}
			if (e2 <= dx) {
				err += dx;
				y += sy;
			}
		}
	}
}
