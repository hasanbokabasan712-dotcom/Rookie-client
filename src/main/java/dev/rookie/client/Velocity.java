package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

// darbe aldıktan hemen sonraki birkaç tickte yatay savurmayı yüzdeye göre söndürür
public final class Velocity {
    private Velocity() {}

    public static void tick(MinecraftClient mc) {
        Module m = Modules.VELOCITY;
        if (!m.enabled || mc.player == null) return;
        if (mc.player.hurtTime <= 0 || mc.player.hurtTime < mc.player.maxHurtTime - 2) return;

        double pct = m.num("Yatay %") / 100.0;
        pct = Math.max(0, Math.min(1, pct + ThreadLocalRandom.current().nextDouble(-0.03, 0.03)));

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * pct, v.y, v.z * pct);
    }
}
