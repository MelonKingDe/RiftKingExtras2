package de.melonkingde.riftkingextras;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class RiftKingExtrasClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(RiftKingExtrasClient::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null) return;

        // Normales Inventar (E) immer durch unseres ersetzen
        if (mc.gui.screen() instanceof InventoryScreen) {
            mc.gui.setScreen(new RiftInventoryScreen(mc.player));
        }

        // Equipment aus dem /stats-Menue mitlesen
        EquipmentCache.tick(mc);
    }
}
