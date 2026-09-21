package dev.alienstoearth.paramax.modmenu;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

final class ParaMaxSlider extends AbstractSliderButton {

    enum Format {
        INT,
        PERCENT,
        BLOCKS,
        MILLISECONDS,
        FPS
    }

    private final String labelKey;
    private final double min;
    private final double max;
    private final double step;
    private final Format format;
    private final DoubleConsumer setter;

    ParaMaxSlider(int x, int y, int width, int height, String labelKey,
                  double min, double max, double step, Format format,
                  DoubleSupplier getter, DoubleConsumer setter) {
        super(x, y, width, height, Component.empty(),
                (clamp(getter.getAsDouble(), min, max) - min) / (max - min));
        this.labelKey = labelKey;
        this.min = min;
        this.max = max;
        this.step = step;
        this.format = format;
        this.setter = setter;
        this.updateMessage();
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private double realValue() {
        double raw = this.min + this.value * (this.max - this.min);
        return Math.round(raw / this.step) * this.step;
    }

    @Override
    protected void updateMessage() {
        double v = this.realValue();
        Component shown = switch (this.format) {
            case INT -> Component.literal(String.valueOf((int) v));
            case PERCENT -> Component.translatable("paramax.unit.percent", (int) Math.round(v * 100.0));
            case BLOCKS -> Component.translatable("paramax.unit.blocks", (int) v);
            case MILLISECONDS -> Component.translatable("paramax.unit.milliseconds", (int) v);
            case FPS -> Component.translatable("paramax.unit.fps", (int) v);
        };
        this.setMessage(Component.translatable("paramax.slider.entry", Component.translatable(this.labelKey), shown));
    }

    @Override
    protected void applyValue() {
        this.setter.accept(this.realValue());
    }
}
