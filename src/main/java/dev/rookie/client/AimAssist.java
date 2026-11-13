package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class AimAssist {
    private AimAssist() {}

    private static final float MAX_STEP = 10f;   // derece/tick, insan gibi görünsün diye sınırlı
    private static PlayerEntity target;          // kilitli hedef, kapanana ya da yok olana kadar değişmez

    public static PlayerEntity getTarget() {
        return target;
    }

    // 0 = kafa, 1 = vücut, 2 = ayak
    public static Vec3d aimPoint(PlayerEntity p, int region) {
        switch (region) {
            case 0: return p.getPos().add(0, p.getHeight() * 0.9, 0);
            case 2: return p.getPos().add(0, 0.15, 0);
            default: return p.getPos().add(0, p.getHeight() * 0.55, 0);
        }
    }

    private static float[] delta(Vec3d eye, Vec3d aim, float yaw, float pitch) {
        double dx = aim.x - eye.x, dy = aim.y - eye.y, dz = aim.z - eye.z;
        float tYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float tPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        return new float[] { MathHelper.wrapDegrees(tYaw - yaw), tPitch - pitch };
    }

    public static double crosshairAngle(MinecraftClient mc, PlayerEntity p, int region) {
        float[] d = delta(mc.player.getEyePos(), aimPoint(p, region), mc.player.getYaw(), mc.player.getPitch());
        return Math.hypot(d[0], d[1]);
    }

    private static PlayerEntity pick(MinecraftClient mc, Module m) {
        double range = m.num("Menzil");
        boolean walls = m.on("Duvar Arkası");
        boolean skipTeam = m.on("Takım Atla");
        boolean skipInv = m.on("Görünmez Atla");
        int region = m.get("Bölge").index();

        PlayerEntity best = null;
        double bestAngle = Double.MAX_VALUE;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!Targets.valid(mc, p, range, skipTeam, skipInv)) continue;
            if (!walls && !mc.player.canSee(p)) continue;
            double a = crosshairAngle(mc, p, region);
            if (a < bestAngle) {
                bestAngle = a;
                best = p;
            }
        }
        return best;
    }

    public static void tick(MinecraftClient mc) {
        Module m = Modules.AIM;
        if (!m.enabled || mc.player == null || mc.world == null || mc.currentScreen != null) {
            target = null;
            return;
        }

        double range = m.num("Menzil");
        boolean skipTeam = m.on("Takım Atla");
        boolean skipInv = m.on("Görünmez Atla");

        // kilitli hedef sadece ölürse, menzil dışına çıkarsa ya da görünmez olursa bırakılır
        if (target != null && !(target.isAlive() && Targets.valid(mc, target, range, skipTeam, skipInv))) {
            target = null;
        }
        if (target == null) target = pick(mc, m);
        if (target == null) return;

        int region = m.get("Bölge").index();
        float[] d = delta(mc.player.getEyePos(), aimPoint(target, region), mc.player.getYaw(), mc.player.getPitch());

        float s = (float) m.num("Smooth");
        float stepYaw = d[0] * s;
        float stepPitch = d[1] * s;
        if (m.on("Bypass")) {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            s *= (float) r.nextDouble(0.8, 1.2);
            stepYaw = MathHelper.clamp(d[0] * s, -MAX_STEP, MAX_STEP) + (float) r.nextDouble(-0.3, 0.3);
            stepPitch = MathHelper.clamp(d[1] * s, -MAX_STEP, MAX_STEP) + (float) r.nextDouble(-0.15, 0.15);
        }

        mc.player.setYaw(mc.player.getYaw() + stepYaw);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + stepPitch, -90f, 90f));
    }
}
