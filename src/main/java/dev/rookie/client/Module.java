package dev.rookie.client;

import java.util.ArrayList;
import java.util.List;

public final class Module {
    public final String name;
    public final String category;
    public boolean enabled;
    public boolean expanded;
    public int bind = -1;        // GLFW key code, -1 = none
    public boolean wasDown;      // edge detection for keybind
    public final List<Setting> settings = new ArrayList<>();

    public Module(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public Module add(Setting s) {
        settings.add(s);
        return this;
    }

    public Setting get(String n) {
        for (Setting s : settings) if (s.name.equals(n)) return s;
        throw new IllegalArgumentException("no setting: " + n);
    }

    public boolean on(String n) { return get(n).on; }
    public double num(String n) { return get(n).value; }
}
