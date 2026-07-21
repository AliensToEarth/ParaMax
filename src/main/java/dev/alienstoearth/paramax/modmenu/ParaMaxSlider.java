package dev.alienstoearth.paramax.modmenu;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

final class ParaMaxSlider extends SliderWidget {

    enum Format {
        INT,
        INT_OR_AUTO,
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
        super(x, y, width, height, Text.empty(),
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
        Text shown = switch (this.format) {
            case INT -> Text.literal(String.valueOf((int) v));
            case INT_OR_AUTO -> (int) v == 0
                    ? Text.translatable("paramax.value.auto")
                    : Text.literal(String.valueOf((int) v));
            case PERCENT -> Text.translatable("paramax.unit.percent", (int) Math.round(v * 100.0));
            case BLOCKS -> Text.translatable("paramax.unit.blocks", (int) v);
            case MILLISECONDS -> Text.translatable("paramax.unit.milliseconds", (int) v);
            case FPS -> Text.translatable("paramax.unit.fps", (int) v);
        };
        this.setMessage(Text.translatable("paramax.slider.entry", Text.translatable(this.labelKey), shown));
    }

    @Override
    protected void applyValue() {
        this.setter.accept(this.realValue());
    }
}
