package net.lugo.spawnproofer.util;


import net.lugo.spawnproofer.config.ModConfig;

import java.awt.*;

public class ColorHelper {
    public static Color getOverlayColor(int lightLevel) {
        return lightLevel >= ModConfig.lightLevelThreshold ? Color.GREEN : Color.RED;
    }

    public static float[] getOverlayColorFloats(int lightLevel) {
        Color color = getOverlayColor(lightLevel);

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        return new float[]{r, g, b};
    }
}
