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

    public static void tick(MinecraftClient mc) {
        Module m = Modules.TRIGGER;
        if (!m.enabled || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity p)) return;
        if (!Targets.valid(mc, p, m.num("Menzil"), m.on("Takım Atla"), m.on("Görünmez Atla"))) return;

        if (m.on("Critical") && !canCrit(mc)) return;

        // prehit fires slightly before the cooldown is full
        double ready = m.on("Prehit") ? 0.85 : 1.0;
        if (mc.player.getAttackCooldownProgress(0.5f) < ready) return;

        double ms = m.num("Attack MS");
        if (m.on("Bypass")) ms *= ThreadLocalRandom.current().nextDouble(0.8, 1.2);
        long now = System.currentTimeMillis();
        if (now - lastAttack < ms) return;
        lastAttack = now;

        mc.interactionManager.attackEntity(mc.player, p);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // only swing while a real critical is possible: falling, off ground, no water/ladder/blindness
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
