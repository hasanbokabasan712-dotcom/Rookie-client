package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class TriggerBot {
    private TriggerBot() {}

    private static long lastAttack;
    private static final double PREHIT_CONE = 12.0;  // derece

    public static void tick(MinecraftClient mc) {
        Module m = Modules.TRIGGER;
        if (!m.enabled || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;

        ThreadLocalRandom r = ThreadLocalRandom.current();
        double range = m.num("Menzil");
        boolean skipTeam = m.on("Takım Atla");
        boolean skipInv = m.on("Görünmez Atla");

        PlayerEntity p;
        double ready;
        if (m.on("Prehit")) {
            // prehit: kilitli hedef ya da imlece yakın oyuncu, imleç tam üstünde olmasa da vurur
            p = AimAssist.getTarget();
            if (p == null) p = nearestInCone(mc, range, skipTeam, skipInv);
            ready = m.on("Bypass") ? r.nextDouble(0.82, 0.9) : 0.85;
        } else {
            if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
            if (!(hit.getEntity() instanceof PlayerEntity pe)) return;
            p = pe;
            ready = m.on("Bypass") ? r.nextDouble(0.93, 1.0) : 1.0;
        }

        if (p == null || !Targets.valid(mc, p, range, skipTeam, skipInv)) return;
        if (m.on("Critical") && !canCrit(mc)) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < ready) return;
        double ms = m.num("Attack MS");
        if (m.on("Bypass")) {
            if (r.nextDouble() < 0.06) return;  // düzenli ritim olmasın diye bazı hazır vuruşları atla
            ms *= r.nextDouble(0.75, 1.35);
        }
        long now = System.currentTimeMillis();
        if (now - lastAttack < ms) return;
        lastAttack = now;

        mc.interactionManager.attackEntity(mc.player, p);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private static PlayerEntity nearestInCone(MinecraftClient mc, double range, boolean skipTeam, boolean skipInv) {
        int region = Modules.AIM.get("Bölge").index();
        PlayerEntity best = null;
        double bestAngle = PREHIT_CONE;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!Targets.valid(mc, p, range, skipTeam, skipInv)) continue;
            double a = AimAssist.crosshairAngle(mc, p, region);
            if (a < bestAngle) {
                bestAngle = a;
                best = p;
            }
        }
        return best;
    }

    // sadece gerçek kritik olabilecekken vur: düşüyor, yerde değil, su/merdiven/körlük yok
    private static boolean canCrit(MinecraftClient mc) {
        PlayerEntity pl = mc.player;
        return pl.fallDistance > 0f
                && !pl.isOnGround()
                && !pl.isClimbing()
                && !pl.isTouchingWater()
                && !pl.hasVehicle()
                && !pl.hasStatusEffect(StatusEffects.BLINDNESS);
    }
}
