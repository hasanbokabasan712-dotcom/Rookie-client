package dev.rookie.client;

import java.util.List;

public final class Modules {
    private Modules() {}

    public static final Module AIM = new Module("Aim Assist", "Combat")
            .add(Setting.choice("Bölge", 1, "Kafa", "Vücut", "Ayak"))
            .add(Setting.bool("Duvar Arkası", false))
            .add(Setting.bool("Takım Atla", true))
            .add(Setting.bool("Görünmez Atla", true))
            .add(Setting.slider("Menzil", 32, 3, 64, 1))
            .add(Setting.slider("Smooth", 0.25, 0.05, 1.0, 0.05))
            .add(Setting.bool("Bypass", true));

    public static final Module TRIGGER = new Module("Trigger Bot", "Combat")
            .add(Setting.bool("Critical", false))
            .add(Setting.bool("Prehit", false))
            .add(Setting.bool("Takım Atla", true))
            .add(Setting.bool("Görünmez Atla", true))
            .add(Setting.slider("Attack MS", 60, 0, 1000, 10))
            .add(Setting.slider("Menzil", 3.0, 1.0, 6.0, 0.1))
            .add(Setting.bool("Bypass", true));

    public static final Module VELOCITY = new Module("Velocity", "Combat")
            .add(Setting.slider("Yatay %", 0, 0, 100, 5));

    public static final Module ESP = new Module("ESP", "Render")
            .add(Setting.bool("Oyuncular", true))
            .add(Setting.bool("Moblar", false))
            .add(Setting.bool("Duvar Arkası", true))
            .add(Setting.slider("Menzil", 64, 8, 128, 1));

    public static final Module TARGET_ESP = new Module("Target ESP", "Render")
            .add(Setting.bool("Köşe Kutu", true))
            .add(Setting.bool("Zemin Halka", true))
            .add(Setting.bool("Duvar Arkası", true));

    public static final Module ELYTRA = new Module("Elytra", "Movement")
            .add(Setting.slider("Hız", 0.06, 0.02, 0.15, 0.01));

    public static final Module NOWEB = new Module("NoWeb", "Movement")
            .add(Setting.slider("Hız", 0.08, 0.02, 0.2, 0.01))
            .add(Setting.bool("Bypass", true));

    public static final Module AUTO_TOTEM = new Module("Auto Totem", "Combat")
            .add(Setting.slider("HP", 13, 0, 20, 1));

    public static final List<Module> ALL = List.of(AIM, TRIGGER, VELOCITY, AUTO_TOTEM, ESP, TARGET_ESP, ELYTRA, NOWEB);
}
