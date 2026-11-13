package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

// elytra ile süzülürken bakış yönüne ivme ekler, yatay hız tavanı var
public final class Elytra {
    private Elytra() {}

    private static final double SPEED_CAP = 2.6;

    public static void tick(MinecraftClient mc) {
        Module m = Modules.ELYTRA;
        if (!m.enabled || mc.player == null || !mc.player.isFallFlying()) return;

        Vec3d v = mc.player.getVelocity();
        if (Math.hypot(v.x, v.z) > SPEED_CAP) return;

        double boost = m.num("Hız") * ThreadLocalRandom.current().nextDouble(0.85, 1.15);
        mc.player.setVelocity(v.add(mc.player.getRotationVector().multiply(boost)));
    }
}
