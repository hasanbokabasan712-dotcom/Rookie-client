package dev.rookie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tessellator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public final class TargetEsp {
    private TargetEsp() {}

    public static void render(WorldRenderContext ctx) {
        Module m = Modules.ESP;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!m.enabled || mc.player == null || mc.world == null) return;

        boolean box = m.on("Köşe Kutu");
        boolean ring = m.on("Zemin Halka");
        boolean through = m.on("Duvar Arkası");
        if (!box && !ring) return;

        List<PlayerEntity> targets = new ArrayList<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (Targets.valid(mc, p, m.num("Menzil"), false, false)) targets.add(p);
        }
        if (targets.isEmpty()) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();
        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = ms.peek().getPositionMatrix();

        // purple, pulsing alpha like the reference
        float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 180.0);
        int alpha = (int) (pulse * 255);
        int r = 0xB8, g = 0x4D, b = 0xFF;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0f);
        if (through) RenderSystem.disableDepthTest();

        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        for (PlayerEntity p : targets) {
            if (box) brackets(bb, mat, p, r, g, b, alpha);
            if (ring) circle(bb, mat, p, r, g, b, alpha);
        }
        Tessellator.getInstance().draw();

        if (through) RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    // 8 corners, 3 short inward bracket arms each
    private static void brackets(BufferBuilder bb, Matrix4f mat, PlayerEntity p, int r, int g, int b, int a) {
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
                    line(bb, mat, cx, cy, cz, cx - sx * len, cy, cz, r, g, b, a);
                    line(bb, mat, cx, cy, cz, cx, cy + dy, cz, r, g, b, a);
                    line(bb, mat, cx, cy, cz, cx, cy, cz - sz * len, r, g, b, a);
                }
            }
        }
    }

    // ground ring under the feet
    private static void circle(BufferBuilder bb, Matrix4f mat, PlayerEntity p, int r, int g, int b, int a) {
        double rad = p.getWidth() * 0.9;
        double y = p.getY() + 0.05;
        int n = 32;
        for (int i = 0; i < n; i++) {
            double t1 = i * Math.PI * 2 / n;
            double t2 = (i + 1) * Math.PI * 2 / n;
            line(bb, mat,
                    p.getX() + Math.cos(t1) * rad, y, p.getZ() + Math.sin(t1) * rad,
                    p.getX() + Math.cos(t2) * rad, y, p.getZ() + Math.sin(t2) * rad,
                    r, g, b, a);
        }
    }

    private static void line(BufferBuilder bb, Matrix4f mat,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             int r, int g, int b, int a) {
        bb.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        bb.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
    }
}
