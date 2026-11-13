package dev.rookie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

// ESP: oyuncu/mob kutuları. Target ESP: sadece kilitli hedef için mor köşe + zemin halkası.
public final class TargetEsp {
    private TargetEsp() {}

    private static final int PLAYER_RGB = 0x3BFFD0;
    private static final int MOB_RGB = 0xFFE04D;
    private static final int TARGET_RGB = 0xB84DFF;

    public static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        Module esp = Modules.ESP;
        Module tgt = Modules.TARGET_ESP;
        if (!esp.enabled && !tgt.enabled) return;

        List<Entity> boxes = new ArrayList<>();
        if (esp.enabled) {
            double range = esp.num("Menzil");
            for (Entity e : mc.world.getEntities()) {
                if (e == mc.player || !(e instanceof LivingEntity)) continue;
                if (!((LivingEntity) e).isAlive()) continue;
                boolean isPlayer = e instanceof PlayerEntity;
                if (isPlayer ? !esp.on("Oyuncular") : !esp.on("Moblar")) continue;
                if (mc.player.squaredDistanceTo(e) > range * range) continue;
                boxes.add(e);
            }
        }

        Entity focus = null;
        if (tgt.enabled) {
            focus = AimAssist.getTarget();
            if (focus == null && mc.crosshairTarget instanceof EntityHitResult hit) focus = hit.getEntity();
            if (focus != null && (focus == mc.player || !(focus instanceof LivingEntity) || !((LivingEntity) focus).isAlive())) {
                focus = null;
            }
        }
        if (boxes.isEmpty() && focus == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();
        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = ms.peek().getPositionMatrix();

        boolean through = (esp.enabled && esp.on("Duvar Arkası")) || (tgt.enabled && tgt.on("Duvar Arkası"));
        float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 180.0);
        int alpha = (int) (pulse * 255);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        if (through) RenderSystem.disableDepthTest();

        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (Entity e : boxes) {
            boolean isPlayer = e instanceof PlayerEntity;
            boxLines(bb, mat, e.getBoundingBox(), isPlayer ? PLAYER_RGB : MOB_RGB, 255);
        }
        if (focus != null) {
            if (tgt.on("Köşe Kutu")) brackets(bb, mat, focus, TARGET_RGB, alpha);
            if (tgt.on("Zemin Halka")) circle(bb, mat, focus, TARGET_RGB, alpha);
        }

        Tessellator.getInstance().draw();
        if (through) RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    private static void boxLines(BufferBuilder bb, Matrix4f mat, Box b, int rgb, int a) {
        double x1 = b.minX, y1 = b.minY, z1 = b.minZ, x2 = b.maxX, y2 = b.maxY, z2 = b.maxZ;
        line(bb, mat, x1, y1, z1, x2, y1, z1, rgb, a);
        line(bb, mat, x2, y1, z1, x2, y1, z2, rgb, a);
        line(bb, mat, x2, y1, z2, x1, y1, z2, rgb, a);
        line(bb, mat, x1, y1, z2, x1, y1, z1, rgb, a);
        line(bb, mat, x1, y2, z1, x2, y2, z1, rgb, a);
        line(bb, mat, x2, y2, z1, x2, y2, z2, rgb, a);
        line(bb, mat, x2, y2, z2, x1, y2, z2, rgb, a);
        line(bb, mat, x1, y2, z2, x1, y2, z1, rgb, a);
        line(bb, mat, x1, y1, z1, x1, y2, z1, rgb, a);
        line(bb, mat, x2, y1, z1, x2, y2, z1, rgb, a);
        line(bb, mat, x2, y1, z2, x2, y2, z2, rgb, a);
        line(bb, mat, x1, y1, z2, x1, y2, z2, rgb, a);
    }

    // 8 köşe, her köşeden içe doğru 3 kısa kol
    private static void brackets(BufferBuilder bb, Matrix4f mat, Entity p, int rgb, int a) {
        double w = p.getWidth() / 2 + 0.1;
        double h = p.getHeight();
        double len = Math.min(w, h) * 0.45;
        double x0 = p.getX(), y0 = p.getY(), z0 = p.getZ();

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int top = 0; top <= 1; top++) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    double cx = x0 + sx * w;
                    double cy = y0 + (top == 1 ? h : 0);
                    double cz = z0 + sz * w;
                    double dy = top == 1 ? -len : len;
                    line(bb, mat, cx, cy, cz, cx - sx * len, cy, cz, rgb, a);
                    line(bb, mat, cx, cy, cz, cx, cy + dy, cz, rgb, a);
                    line(bb, mat, cx, cy, cz, cx, cy, cz - sz * len, rgb, a);
                }
            }
        }
    }

    private static void circle(BufferBuilder bb, Matrix4f mat, Entity p, int rgb, int a) {
        double rad = p.getWidth() * 0.9;
        double y = p.getY() + 0.05;
        int n = 32;
        for (int i = 0; i < n; i++) {
            double t1 = i * Math.PI * 2 / n;
            double t2 = (i + 1) * Math.PI * 2 / n;
            line(bb, mat,
                    p.getX() + Math.cos(t1) * rad, y, p.getZ() + Math.sin(t1) * rad,
                    p.getX() + Math.cos(t2) * rad, y, p.getZ() + Math.sin(t2) * rad,
                    rgb, a);
        }
    }

    private static void line(BufferBuilder bb, Matrix4f mat,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             int rgb, int a) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        bb.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        bb.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
    }
}
