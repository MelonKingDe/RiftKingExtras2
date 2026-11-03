package de.melonkingde.riftkingextras;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Liest die Skyblock-Rarity aus der Lore (letzte Zeilen) und liefert die Rahmenfarbe. */
public final class Rarity {
    private Rarity() {}

    public static final int NONE = 0;
    public static final int FALLBACK = 0xFF9A9A9A; // Item ohne Rarity
    public static final int EMPTY = 0xFF3A3A44;    // leerer Slot

    private static final Map<String, Integer> COLORS = Map.ofEntries(
            Map.entry("COMMON", 0xFFFFFFFF),
            Map.entry("UNCOMMON", 0xFF55FF55),
            Map.entry("RARE", 0xFF5555FF),
            Map.entry("EPIC", 0xFFAA00AA),
            Map.entry("LEGENDARY", 0xFFFFAA00),
            Map.entry("MYTHIC", 0xFFFF55FF),
            Map.entry("DIVINE", 0xFF55FFFF),
            Map.entry("ULTIMATE", 0xFFAA0000),
            Map.entry("SPECIAL", 0xFFFF5555),
            Map.entry("VERY SPECIAL", 0xFFAA0000),
            Map.entry("SUPREME", 0xFFAA0000)
    );

    private static final Pattern PATTERN = Pattern.compile(
            "\\b(VERY SPECIAL|SPECIAL|ULTIMATE|DIVINE|MYTHIC|LEGENDARY|EPIC|UNCOMMON|COMMON|RARE|SUPREME)\\b");

    /** Rahmenfarbe (ARGB) fuer den Slot-Inhalt. */
    public static int borderColor(ItemStack stack) {
        if (stack.isEmpty()) return EMPTY;
        int c = of(stack);
        return c == NONE ? FALLBACK : c;
    }

    private static int of(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return NONE;
        List<Component> lines = lore.lines();
        int checked = 0;
        for (int i = lines.size() - 1; i >= 0 && checked < 5; i--, checked++) {
            Matcher m = PATTERN.matcher(lines.get(i).getString());
            if (m.find()) {
                Integer col = COLORS.get(m.group(1));
                if (col != null) return col;
            }
        }
        return NONE;
    }
}
