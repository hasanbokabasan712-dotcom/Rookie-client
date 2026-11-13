package dev.rookie.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class RookieClient implements ClientModInitializer {
    private static boolean menuWasDown;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            menuKey(mc);
            moduleKeybinds(mc);
            AimAssist.tick(mc);
            Velocity.tick(mc);
            AutoTotem.tick(mc);
            Elytra.tick(mc);
            NoWeb.tick(mc);
            TriggerBot.tick(mc);
        });
        WorldRenderEvents.LAST.register(TargetEsp::render);
        HudRenderCallback.EVENT.register(EspHud::render);
    }

    private static void menuKey(MinecraftClient mc) {
        boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_F12) == GLFW.GLFW_PRESS;
        if (down && !menuWasDown) {
            if (mc.currentScreen == null) mc.setScreen(new ClickGui());
            else if (mc.currentScreen instanceof ClickGui) mc.setScreen(null);
        }
        menuWasDown = down;
    }

    private static void moduleKeybinds(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        for (Module m : Modules.ALL) {
            if (m.bind < 0) {
                m.wasDown = false;
                continue;
            }
            boolean down = GLFW.glfwGetKey(handle, m.bind) == GLFW.GLFW_PRESS;
            if (down && !m.wasDown && mc.currentScreen == null) {
                m.enabled = !m.enabled;
            }
            m.wasDown = down;
        }
    }
}
