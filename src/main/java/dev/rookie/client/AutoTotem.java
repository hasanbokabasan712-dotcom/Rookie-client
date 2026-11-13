package dev.rookie.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

// can eşiğinin altına düşünce totemi sol ele alır. Envanter ekranı açılmaz,
// tıklama doğrudan oyuncunun kendi ekran handler'ı üzerinden yapılır.
public final class AutoTotem {
    private AutoTotem() {}

    private static final int OFFHAND_BUTTON = 40;
    private static long lastSwap;
    private static long nextDelay = 150;

    public static void tick(MinecraftClient mc) {
        Module m = Modules.AUTO_TOTEM;
        if (!m.enabled || mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (mc.player.getHealth() > m.num("HP")) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        long now = System.currentTimeMillis();
        if (now - lastSwap < nextDelay) return;

        int source = findTotem(mc.player.getInventory());
        if (source < 0) return;

        // hotbar (0-8) -> handler slot 36-44, ana envanter (9-35) aynı indeks
        int handlerSlot = source < 9 ? 36 + source : source;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, handlerSlot, OFFHAND_BUTTON, SlotActionType.SWAP, mc.player);

        lastSwap = now;
        nextDelay = ThreadLocalRandom.current().nextLong(80, 220);  // sabit ritim olmasın
    }

    // önce hotbar, sonra ana envanter
    private static int findTotem(PlayerInventory inv) {
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
