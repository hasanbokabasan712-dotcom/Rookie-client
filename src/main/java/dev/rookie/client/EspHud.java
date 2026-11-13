package dev.rookie.client;

import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

// ESP'nin ekran üstü kısmı: üstünde can + zırh barı, altında isim
public final class EspHud {
    private EspHud() {}

    private static final int PLAYER_RGB = 0xFF3BFFD0;
    private static final int MOB_RGB = 0xFFFFE04D;
    private static final int BAR_BG = 0xAA000000;
    private static final int ARMOR_RGB = 0xFFBFBFBF;

    public static void render(MatrixStack m, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!Modules.ESP.enabled || mc.player == null || mc.world == null || mc.currentScreen != null) return;

        double range = Modules.ESP.num("Menzil");
        TextRenderer tr = mc.textRenderer;

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player || !(e instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity) e;
            if (!le.isAlive()) continue;

            boolean isPlayer = e instanceof PlayerEntity;
            if (isPlayer ? !Modules.ESP.on("Oyuncular") : !Modules.ESP.on("Moblar")) continue;
            if (mc.player.squaredDistanceTo(e) > range * range) continue;

            double[] head = Projection.toScreen(mc, e.getPos().add(0, e.getHeight() + 0.3, 0));
            double[] feet = Projection.toScreen(mc, e.getPos());
            if (head == null || feet == null) continue;
            if (feet[1] - head[1] <= 4) continue;

            int cx = (int) feet[0];
            int barW = 40;
            int x1 = cx - barW / 2, x2 = cx + barW / 2;
            int hy = (int) head[1];

            // can barı
            float hp = (float) Math.max(0, Math.min(1, le.getHealth() / le.getMaxHealth()));
            Draw.rect(m, x1 - 1, hy - 1, x2 + 1, hy + 4, BAR_BG);
            Draw.rect(m, x1, hy, x1 + (int) (barW * hp), hy + 3, healthColor(hp));

            // zırh barı (20 = tam zırh)
            int armor = le.getArmor();
            float ar = Math.max(0, Math.min(1, armor / 20f));
            Draw.rect(m, x1 - 1, hy + 5, x2 + 1, hy + 8, BAR_BG);
            if (armor > 0) Draw.rect(m, x1, hy + 6, x1 + (int) (barW * ar), hy + 7, ARMOR_RGB);

            // sayısal değer
            String info = String.format(Locale.ROOT, "%.0f HP  ZIRH %d", le.getHealth(), armor);
            tr.draw(m, info, cx - tr.getWidth(info) / 2f, hy - 10, 0xFFFFFFFF);

            // isim, ayakların altında
            String name = e.getName().getString();
            tr.draw(m, name, cx - tr.getWidth(name) / 2f, (float) feet[1] + 2, isPlayer ? PLAYER_RGB : MOB_RGB);
        }
    }

    private static int healthColor(float hp) {
        if (hp > 0.5f) return 0xFF3BE05A;
        if (hp > 0.25f) return 0xFFFFC83B;
        return 0xFFFF3B3B;
    }
}
