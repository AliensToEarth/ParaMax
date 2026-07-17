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

    private final String label;
    private final double min;
    private final double max;
    private final double step;
    private final Format format;
    private final DoubleConsumer setter;

    ParaMaxSlider(int x, int y, int width, int height, String label,
                  double min, double max, double step, Format format,
                  DoubleSupplier getter, DoubleConsumer setter) {
        super(x, y, width, height, Text.empty(),
                (clamp(getter.getAsDouble(), min, max) - min) / (max - min));
        this.label = label;
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
        String shown = switch (this.format) {
            case INT -> String.valueOf((int) v);
            case INT_OR_AUTO -> (int) v == 0 ? "auto" : String.valueOf((int) v);
            case PERCENT -> (int) Math.round(v * 100.0) + "%";
            case BLOCKS -> (int) v + " blocks";
            case MILLISECONDS -> (int) v + " ms";
            case FPS -> (int) v + " FPS";
        };
        this.setMessage(Text.literal(this.label + ": " + shown));
    }

    @Override
    protected void applyValue() {
        this.setter.accept(this.realValue());
    }
}
