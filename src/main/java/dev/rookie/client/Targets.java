package dev.rookie.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public final class Targets {
    private Targets() {}

    public static boolean valid(MinecraftClient mc, PlayerEntity p, double range, boolean skipTeam, boolean skipInvisible) {
        if (p == mc.player || !p.isAlive() || p.isSpectator()) return false;
        if (skipTeam && mc.player.isTeammate(p)) return false;
        if (skipInvisible && p.isInvisible()) return false;
        return mc.player.squaredDistanceTo(p) <= range * range;
    }
}
