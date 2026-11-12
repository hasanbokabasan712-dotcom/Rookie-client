package dev.rookie.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.screen.Screen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class ClickGui extends Screen {
    private static final int ACCENT = 0xFFE8741E;
    private static final int ROW_OFF = 0xE0141414;
    private static final int ROW_SUB = 0xE01F1F1F;
    private static final int TRACK = 0xFF333333;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFFAAAAAA;
    private static final int COL_W = 130;
    private static final int ROW_H = 14;
    private static final List<String> CATS = List.of("Combat", "Render");

    private enum Kind { ROW, BOOL, SLIDER, BIND }
    private record Hit(int x, int y, int w, int h, Kind kind, Module mod, Setting set) {}

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private Module binding;

    public ClickGui() {
        super(Text.literal("Rookie"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(MatrixStack m, int mx, int my, float delta) {
        hits.clear();
        rect(m, 0, 0, width, height, 0x66000000);
        for (int i = 0; i < CATS.size(); i++) {
            column(m, CATS.get(i), 20 + i * (COL_W + 8), 20);
        }
        String hint = binding != null
                ? "Bir tuşa bas (ESC temizler, tık iptal)"
                : "Sol tık: aç/kapat   Sağ tık: ayarlar   F12: menü";
        textRenderer.draw(m, hint, 8, height - 14, GREY);
    }

    private void column(MatrixStack m, String cat, int x, int top) {
        rect(m, x, top, x + COL_W, top + ROW_H + 4, ACCENT);
        textRenderer.draw(m, cat, x + 6, top + 4, WHITE);
        int y = top + ROW_H + 4;

        for (Module mod : Modules.ALL) {
            if (!mod.category.equals(cat)) continue;
            int bg = mod.enabled ? ACCENT : ROW_OFF;
            rect(m, x, y, x + COL_W, y + ROW_H, bg);
            textRenderer.draw(m, mod.name, x + 5, y + 3, WHITE);
            String key = mod.bind < 0 ? "" : "[" + keyName(mod.bind) + "]";
            textRenderer.draw(m, key, x + COL_W - 5 - textRenderer.getWidth(key), y + 3, GREY);
            hits.add(new Hit(x, y, COL_W, ROW_H, Kind.ROW, mod, null));
            y += ROW_H;

            if (mod.expanded) {
                for (Setting s : mod.settings) {
                    int h = s.type == Setting.Type.SLIDER ? 18 : ROW_H;
                    subRow(m, s, mod, x, y, h);
                    y += h;
                }
                bindRow(m, mod, x, y);
                y += ROW_H + 4;
            }
        }
    }

    private void subRow(MatrixStack m, Setting s, Module mod, int x, int y, int h) {
        rect(m, x, y, x + COL_W, y + h, ROW_SUB);
        textRenderer.draw(m, s.name, x + 5, y + 3, GREY);
        String v = s.text();
        boolean lit = s.type == Setting.Type.BOOL && s.on;
        textRenderer.draw(m, v, x + COL_W - 5 - textRenderer.getWidth(v), y + 3, lit ? ACCENT : WHITE);

        if (s.type == Setting.Type.SLIDER) {
            int bx = x + 5, bw = COL_W - 10, by = y + 12;
            rect(m, bx, by, bx + bw, by + 3, TRACK);
            rect(m, bx, by, bx + (int) (bw * s.ratio()), by + 3, ACCENT);
            hits.add(new Hit(x, y, COL_W, h, Kind.SLIDER, mod, s));
        } else {
            hits.add(new Hit(x, y, COL_W, h, Kind.BOOL, mod, s));
        }
    }

    private void bindRow(MatrixStack m, Module mod, int x, int y) {
        rect(m, x, y, x + COL_W, y + ROW_H, ROW_SUB);
        textRenderer.draw(m, "Tuş", x + 5, y + 3, GREY);
        String v = binding == mod ? "Bekle..." : keyName(mod.bind);
        textRenderer.draw(m, v, x + COL_W - 5 - textRenderer.getWidth(v), y + 3, binding == mod ? ACCENT : WHITE);
        hits.add(new Hit(x, y, COL_W, ROW_H, Kind.BIND, mod, null));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (binding != null) {
            binding = null;
            return true;
        }
        for (Hit h : hits) {
            if (!inside(h, mx, my)) continue;
            switch (h.kind()) {
                case ROW -> {
                    if (button == 0) h.mod().enabled = !h.mod().enabled;
                    else if (button == 1) h.mod().expanded = !h.mod().expanded;
                }
                case BOOL -> h.set().on = !h.set().on;
                case SLIDER -> {
                    dragging = h;
                    setSlider(h, mx);
                }
                case BIND -> binding = h.mod();
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            setSlider(dragging, mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (binding != null) {
            binding.bind = key == GLFW.GLFW_KEY_ESCAPE ? -1 : key;
            binding = null;
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    private static void setSlider(Hit h, double mx) {
        h.set().setRatio((mx - h.x()) / h.w());
    }

    private static boolean inside(Hit h, double mx, double my) {
        return mx >= h.x() && mx < h.x() + h.w() && my >= h.y() && my < h.y() + h.h();
    }

    private static void rect(MatrixStack m, int x1, int y1, int x2, int y2, int argb) {
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

    static String keyName(int code) {
        if (code < 0) return "Yok";
        String n = GLFW.glfwGetKeyName(code, 0);
        return n != null ? n.toUpperCase(Locale.ROOT) : "K" + code;
    }
}
