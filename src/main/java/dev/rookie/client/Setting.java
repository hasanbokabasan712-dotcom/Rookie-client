package dev.rookie.client;

import java.util.Locale;

public final class Setting {
    public enum Type { BOOL, SLIDER, CHOICE }

    public final String name;
    public final Type type;
    public boolean on;
    public double value, min, max, step;
    public String[] options = new String[0];

    private Setting(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public static Setting bool(String name, boolean on) {
        Setting s = new Setting(name, Type.BOOL);
        s.on = on;
        return s;
    }

    public static Setting slider(String name, double def, double min, double max, double step) {
        Setting s = new Setting(name, Type.SLIDER);
        s.value = def;
        s.min = min;
        s.max = max;
        s.step = step;
        return s;
    }

    public static Setting choice(String name, int def, String... options) {
        Setting s = new Setting(name, Type.CHOICE);
        s.options = options;
        s.value = def;
        return s;
    }

    public void setRatio(double r) {
        r = Math.max(0, Math.min(1, r));
        double raw = min + r * (max - min);
        value = Math.max(min, Math.min(max, Math.round(raw / step) * step));
    }

    public double ratio() {
        return (value - min) / (max - min);
    }

    public int index() {
        return (int) value;
    }

    public void cycle() {
        value = (index() + 1) % options.length;
    }

    public String text() {
        if (type == Type.BOOL) return on ? "AÇIK" : "KAPALI";
        if (type == Type.CHOICE) return options[index()];
        return step < 1 ? String.format(Locale.ROOT, "%.2f", value) : String.valueOf((int) value);
    }
}
