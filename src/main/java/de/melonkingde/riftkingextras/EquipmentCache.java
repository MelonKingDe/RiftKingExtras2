package de.melonkingde.riftkingextras;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.Slot;
import de.melonkingde.riftkingextras.mixin.AbstractContainerScreenAccessor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Merkt sich die 4 Equipment-Items (Necklace, Cloak, Belt, Gloves) aus dem /stats-Menue. */
public final class EquipmentCache {
    private EquipmentCache() {}

    public static final ItemStack[] ITEMS = {
            ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY
    };

    /** Slot-Indizes im "Your Equipment and Stats"-Menue. */
    private static final int[] MENU_SLOTS = {10, 19, 28, 37};

    public static void tick(Minecraft mc) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        if (screen instanceof RiftInventoryScreen) return;
        if (!screen.getTitle().getString().contains("Equipment")) return;

        List<Slot> slots = ((AbstractContainerScreenAccessor) screen).riftkingextras$getMenu().slots;
        if (slots.size() <= MENU_SLOTS[MENU_SLOTS.length - 1]) return;

        // Erst auslesen, wenn der Server die Items geschickt hat
        boolean loaded = false;
        for (int i = 0; i < 27; i++) {
            if (!slots.get(i).getItem().isEmpty()) { loaded = true; break; }
        }
        if (!loaded) return;

        for (int k = 0; k < MENU_SLOTS.length; k++) {
            ItemStack st = slots.get(MENU_SLOTS[k]).getItem();
            ITEMS[k] = (st.isEmpty() || isPlaceholder(st)) ? ItemStack.EMPTY : st.copy();
        }
    }

    /** Leere Equipment-Slots werden von Skyblock als Glasscheibe dargestellt. */
    private static boolean isPlaceholder(ItemStack st) {
        return BuiltInRegistries.ITEM.getKey(st.getItem()).getPath().endsWith("glass_pane");
    }
}
