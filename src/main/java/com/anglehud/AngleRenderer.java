package com.anglehud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Locale;

public final class AngleRenderer {
    private AngleRenderer() {}

    /** Hoch = positive Grad, runter = negative Grad. */
    public static String text(float xRot) {
        AngleConfig c = AngleConfig.get();
        double deg = -xRot + 0.0;
        if (Math.abs(deg) < 0.05 && c.decimals > 0) deg = 0.0;
        if (c.decimals <= 0) {
            long v = Math.round(deg);
            return v + "\u00B0";
        }
        return String.format(Locale.ROOT, "%.1f\u00B0", deg);
    }

    public static int centerX(GuiGraphics g) {
        return (int) Math.round(AngleConfig.get().x * g.guiWidth());
    }

    public static int centerY(GuiGraphics g) {
        return (int) Math.round(AngleConfig.get().y * g.guiHeight());
    }

    public static void draw(GuiGraphics g, Minecraft mc, String text) {
        AngleConfig c = AngleConfig.get();
        int w = mc.font.width(text);
        int x = centerX(g) - w / 2;
        int y = centerY(g) - mc.font.lineHeight / 2;
        g.drawString(mc.font, text, x, y, 0xFF000000 | c.rgb(), c.shadow);
    }
}
