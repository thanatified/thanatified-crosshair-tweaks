package com.crosshairtweaks.config;

/**
 * SamplingMode controls how many pixels are sampled behind the crosshair
 * for the environmental blend system.
 *
 * CENTER = sample only the center pixel (fastest)
 * SMALL  = sample a small region (e.g., 7x7)
 * LARGE  = sample a large region (e.g., 21x21)
 */
public enum SamplingMode {

    // Fastest, simplest — 1 pixel
    CENTER,

    // Medium sampling radius — small region
    SMALL,

    // Largest sampling radius — wide region
    LARGE;
}
