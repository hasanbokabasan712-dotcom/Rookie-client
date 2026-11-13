package dev.rookie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public final class Draw {
    private Draw() {}

    public static void rect(MatrixStack m, int x1, int y1, int x2, int y2, int argb) {
        Matrix4f mat = m.peek().getPositionMatrix();
        int a = (argb >>> 24) & 0xFF, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        bb.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bb.vertex(mat, x1, y1, 0).color(r, g, b, a).next();
        bb.vertex(mat, x1, y2, 0).color(r, g, b, a).next();
        bb.vertex(mat, x2, y2, 0).color(r, g, b, a).next();
        bb.vertex(mat, x2, y1, 0).color(r, g, b, a).next();
        Tessellator.getInstance().draw();
        RenderSystem.disableBlend();
    }
}
