package com.crosshairtweaks.config;

public enum CrosshairShape {
    DEFAULT("crosshairtweaks.shape.default"),
    CROSS("crosshairtweaks.shape.cross"),
    DOT("crosshairtweaks.shape.dot"),
    CIRCLE("crosshairtweaks.shape.circle"),
    SQUARE("crosshairtweaks.shape.square"),
    T_SHAPE("crosshairtweaks.shape.t_shape"),
    CHEVRON("crosshairtweaks.shape.chevron"),
    DIAMOND("crosshairtweaks.shape.diamond");

	private final String translationKey;

	CrosshairShape(String translationKey) {
		this.translationKey = translationKey;
	}

	public String translationKey() {
		return translationKey;
	}

	public CrosshairShape next() {
		CrosshairShape[] values = values();
		return values[(this.ordinal() + 1) % values.length];
	}

	public CrosshairShape previous() {
		CrosshairShape[] values = values();
		return values[(this.ordinal() - 1 + values.length) % values.length];
	}
}
