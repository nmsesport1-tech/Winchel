package com.anglehud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public final class AngleScreens {
    private AngleScreens() {}

    private static float currentPitch() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getXRot() : 0f;
    }

    /** Gemeinsame Basis (damit das HUD nicht doppelt gezeichnet wird). */
    public abstract static class Base extends Screen {
        protected final Screen parent;

        protected Base(Component title, Screen parent) {
            super(title);
            this.parent = parent;
        }

        @Override
        public void onClose() {
            AngleConfig.save();
            this.minecraft.setScreen(parent);
        }
    }

    // ---------------------------------------------------------------- Hauptmenü
    public static class MainMenu extends Base {
        public MainMenu(Screen parent) {
            super(Component.literal("Angle HUD"), parent);
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int y = this.height / 4 + 20;
            this.addRenderableWidget(Button.builder(Component.literal("Design"),
                    b -> this.minecraft.setScreen(new DesignScreen(this)))
                    .bounds(cx - 100, y, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Display"),
                    b -> this.minecraft.setScreen(new DisplayScreen(this)))
                    .bounds(cx - 100, y + 26, 200, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 62, 200, 20).build());
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 4, 0xFFFFFFFF);
        }
    }

    // ------------------------------------------------------------------- Design
    private static final class ColorSlider extends AbstractSliderButton {
        private final String label;
        private final IntConsumer setter;

        ColorSlider(int x, int y, int w, int h, String label, int initial, IntConsumer setter) {
            super(x, y, w, h, Component.empty(), initial / 255.0);
            this.label = label;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            setMessage(Component.literal(label + ": " + (int) Math.round(this.value * 255)));
        }

        @Override
        protected void applyValue() {
            setter.accept((int) Math.round(this.value * 255));
        }
    }

    public static class DesignScreen extends Base {
        public DesignScreen(Screen parent) {
            super(Component.literal("Design"), parent);
        }

        @Override
        protected void init() {
            AngleConfig c = AngleConfig.get();
            int cx = this.width / 2;
            int y = this.height / 4 + 10;
            this.addRenderableWidget(new ColorSlider(cx - 100, y, 200, 20, "Red", c.r, v -> c.r = v));
            this.addRenderableWidget(new ColorSlider(cx - 100, y + 24, 200, 20, "Green", c.g, v -> c.g = v));
            this.addRenderableWidget(new ColorSlider(cx - 100, y + 48, 200, 20, "Blue", c.b, v -> c.b = v));

            this.addRenderableWidget(Button.builder(shadowLabel(), b -> {
                c.shadow = !c.shadow;
                b.setMessage(shadowLabel());
            }).bounds(cx - 100, y + 76, 200, 20).build());

            this.addRenderableWidget(Button.builder(decimalsLabel(), b -> {
                c.decimals = c.decimals > 0 ? 0 : 1;
                b.setMessage(decimalsLabel());
            }).bounds(cx - 100, y + 100, 200, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx - 100, y + 130, 200, 20).build());
        }

        private static Component shadowLabel() {
            return Component.literal("Shadow: " + (AngleConfig.get().shadow ? "ON" : "OFF"));
        }

        private static Component decimalsLabel() {
            return Component.literal("Decimals: " + (AngleConfig.get().decimals > 0 ? "1" : "0"));
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 10, 0xFFFFFFFF);
            // Vorschau
            String preview = AngleRenderer.text(-45f);
            int w = this.font.width(preview);
            g.drawString(this.font, preview, this.width / 2 - w / 2, this.height / 4 + 2,
                    0xFF000000 | AngleConfig.get().rgb(), AngleConfig.get().shadow);
        }
    }

    // ------------------------------------------------------------------ Display
    public static class DisplayScreen extends Base {
        private boolean dragging = false;
        private double grabDx = 0;
        private double grabDy = 0;

        public DisplayScreen(Screen parent) {
            super(Component.literal("Display"), parent);
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int y = this.height - 28;
            this.addRenderableWidget(Button.builder(Component.literal("Reset position"), b -> {
                AngleConfig.get().resetPosition();
            }).bounds(cx - 102, y, 100, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                    .bounds(cx + 2, y, 100, 20).build());
        }

        @Override
        public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
            g.fill(0, 0, this.width, this.height, 0x99202020);
        }

        private String sample() {
            return AngleRenderer.text(currentPitch());
        }

        private int boxLeft(String t) {
            return (int) Math.round(AngleConfig.get().x * this.width) - this.font.width(t) / 2 - 4;
        }

        private int boxTop() {
            return (int) Math.round(AngleConfig.get().y * this.height) - this.font.lineHeight / 2 - 3;
        }

        private boolean hit(double mx, double my) {
            String t = sample();
            int l = boxLeft(t);
            int tp = boxTop();
            return mx >= l && mx <= l + this.font.width(t) + 8 && my >= tp && my <= tp + this.font.lineHeight + 6;
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
            super.render(g, mouseX, mouseY, delta);
            g.drawCenteredString(this.font, Component.literal("Drag the angle display anywhere. Esc = done."),
                    this.width / 2, 12, 0xFFFFFFFF);
            String t = sample();
            int l = boxLeft(t);
            int tp = boxTop();
            g.fill(l, tp, l + this.font.width(t) + 8, tp + this.font.lineHeight + 6, dragging ? 0x66FFFFFF : 0x33FFFFFF);
            int w = this.font.width(t);
            int x = (int) Math.round(AngleConfig.get().x * this.width) - w / 2;
            int y = (int) Math.round(AngleConfig.get().y * this.height) - this.font.lineHeight / 2;
            g.drawString(this.font, t, x, y, 0xFF000000 | AngleConfig.get().rgb(), AngleConfig.get().shadow);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (super.mouseClicked(event, doubleClick)) return true;
            if (event.button() == 0 && hit(event.x(), event.y())) {
                dragging = true;
                grabDx = AngleConfig.get().x * this.width - event.x();
                grabDy = AngleConfig.get().y * this.height - event.y();
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
            if (dragging) {
                AngleConfig c = AngleConfig.get();
                c.x = Math.max(0.0, Math.min(1.0, (event.x() + grabDx) / this.width));
                c.y = Math.max(0.0, Math.min(1.0, (event.y() + grabDy) / this.height));
                return true;
            }
            return super.mouseDragged(event, dragX, dragY);
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent event) {
            dragging = false;
            return super.mouseReleased(event);
        }
    }
}
