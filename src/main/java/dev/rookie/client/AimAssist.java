package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssist {
    private AimAssist() {}

    public static void tick(MinecraftClient mc) {
        Module m = Modules.AIM;
        if (!m.enabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        boolean bypass = m.on("Bypass");
        boolean throughWalls = m.on("Duvar Arkası");
        boolean skipTeam = m.on("Takım Atla");
        boolean skipInv = m.on("Görünmez Atla");
        double range = m.num("Menzil");
        float smooth = (float) m.num("Smooth");

        Vec3d eye = mc.player.getEyePos();
        float curYaw = mc.player.getYaw();
        float curPitch = mc.player.getPitch();

        double bestAngle = Double.MAX_VALUE;
        float bestYaw = 0f, bestPitch = 0f;
        boolean found = false;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!Targets.valid(mc, p, range, skipTeam, skipInv)) continue;
            if (!throughWalls && !mc.player.canSee(p)) continue;

            Vec3d aim = p.getEyePos();
            double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
            float tYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float tPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));

            float dYaw = MathHelper.wrapDegrees(tYaw - curYaw);
            float dPitch = tPitch - curPitch;
            double angle = Math.hypot(dYaw, dPitch);

            if (angle >= bestAngle) continue;
            bestAngle = angle;
            bestYaw = dYaw;
            bestPitch = dPitch;
            found = true;
        }

        if (!found) return;

        float stepYaw = bestYaw * smooth;
        float stepPitch = bestPitch * smooth;
        if (bypass) {
            // tiny random wobble so the path isn't a perfect lock
            stepYaw += (float) ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
            stepPitch += (float) ThreadLocalRandom.current().nextDouble(-0.2, 0.2);
        }
        mc.player.setYaw(curYaw + stepYaw);
        mc.player.setPitch(MathHelper.clamp(curPitch + stepPitch, -90f, 90f));
    }
}
