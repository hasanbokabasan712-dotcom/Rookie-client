package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

// örümcek ağının içinde ileri yönde hız ekler, bypass seçilince rastgelelik eklenir
public final class NoWeb {
    private NoWeb() {}

    public static void tick(MinecraftClient mc) {
        Module m = Modules.NOWEB;
        if (!m.enabled || mc.player == null || mc.world == null) return;
        if (!mc.world.getBlockState(mc.player.getBlockPos()).isOf(Blocks.COBWEB)) return;

        float fwd = mc.player.input.movementForward;
        if (fwd == 0) return;

        double speed = m.num("Hız");
        if (m.on("Bypass")) {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            if (r.nextDouble() < 0.1) return;   // bazı tickleri atla, düzenli desen olmasın
            speed *= r.nextDouble(0.8, 1.1);
        }
        Vec3d dir = Vec3d.fromPolar(0f, mc.player.getYaw());
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.add(dir.multiply(speed * Math.signum(fwd))));
    }
}
