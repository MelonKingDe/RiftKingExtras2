package de.melonkingde.riftkingextras;

import de.melonkingde.riftkingextras.mixin.SlotAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class RiftInventoryScreen extends AbstractContainerScreen<InventoryMenu> {

    // ---- Layout (GUI-Pixel) ----
    private static final int W = 180, H = 176;
    private static final int ARMOR_X = 36, ARMOR_Y = 19;       // Slot-Item-Position, Box = -1
    private static final int EQUIP_X = W - 36 - 16, EQUIP_Y = 19;
    private static final int GRID_X = 10, GRID_Y = 95, HOTBAR_Y = 153;
    private static final int BANNER_W = 24, BANNER_BODY = 70, BANNER_TIP = 10, BANNER_Y = 4;
    private static final int LEFT_BANNER_X = 6, RIGHT_BANNER_X = W - 6 - BANNER_W;
    private static final int BTN = 16, BTN_STEP = 17;

    // ---- Farben ----
    private static final int PANEL_OUTER = 0xFF0F0B16, PANEL_EDGE = 0xFF7B3FC4, PANEL_BG = 0xFF3A3A40;
    private static final int SLOT_BG = 0xFF26262C, VIEW_BG = 0xFF22222A;
    private static final int BANNER_EDGE = 0xFF2A0F4A, BANNER_BODY_C = 0xFF6B2FB0, BANNER_LIGHT = 0xFF8E4FD8;
    private static final int TITLE_PURPLE = 0xB45CFF;

    private record Btn(int x, int y, ItemStack icon, String left, String right) {}

    private final int[] origX, origY;
    private final Btn[] buttons;

    public RiftInventoryScreen(LocalPlayer player) {
        super(player.inventoryMenu, player.getInventory(), Component.literal("RiftKingExtras"), W, H);

        List<Slot> slots = this.menu.slots;
        origX = new int[slots.size()];
        origY = new int[slots.size()];
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            origX[i] = s.x;
            origY[i] = s.y;
            int nx = -10000, ny = -10000; // Crafting-Grid und Offhand verstecken
            if (i >= 5 && i <= 8) {                // Ruestung
                nx = ARMOR_X; ny = ARMOR_Y + (i - 5) * 18;
            } else if (i >= 9 && i <= 35) {        // Inventar
                int n = i - 9;
                nx = GRID_X + (n % 9) * 18; ny = GRID_Y + (n / 9) * 18;
            } else if (i >= 36 && i <= 44) {       // Hotbar
                nx = GRID_X + (i - 36) * 18; ny = HOTBAR_Y;
            }
            SlotAccessor acc = (SlotAccessor) s;
            acc.riftkingextras$setX(nx);
            acc.riftkingextras$setY(ny);
        }

        buttons = new Btn[] {
                // linkes Banner
                btn(LEFT_BANNER_X, 0, Items.PAINTING, "collection", "recipes"),
                btn(LEFT_BANNER_X, 1, Items.WRITABLE_BOOK, "quests", "chapters"),
                btn(LEFT_BANNER_X, 2, Items.EXPERIENCE_BOTTLE, "skills", "levels"),
                btn(LEFT_BANNER_X, 3, Items.BOOK, "guides", null),
                // rechtes Banner
                btn(RIGHT_BANNER_X, 0, Items.CRAFTING_TABLE, "craft", null),
                btn(RIGHT_BANNER_X, 1, Items.CHEST, "storage", "sacks"),
                btn(RIGHT_BANNER_X, 2, Items.BONE, "pets", "loadouts"),
                btn(RIGHT_BANNER_X, 3, Items.ENDER_PEARL, "warps", "is"),
        };
    }

    private static Btn btn(int bannerX, int index, net.minecraft.world.item.Item icon, String l, String r) {
        return new Btn(bannerX + (BANNER_W - BTN) / 2, BANNER_Y + 3 + index * BTN_STEP,
                new ItemStack(icon), l, r);
    }

    @Override
    public void removed() {
        // Originalpositionen wiederherstellen
        List<Slot> slots = this.menu.slots;
        for (int i = 0; i < slots.size() && i < origX.length; i++) {
            SlotAccessor acc = (SlotAccessor) slots.get(i);
            acc.riftkingextras$setX(origX[i]);
            acc.riftkingextras$setY(origY[i]);
        }
        super.removed();
    }

    // ------------------------------------------------------------------ Rendering

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g, mouseX, mouseY, delta);
        int l = this.leftPos, t = this.topPos;

        // Panel
        g.fill(l, t, l + W, t + H, PANEL_OUTER);
        g.fill(l + 1, t + 1, l + W - 1, t + H - 1, PANEL_EDGE);
        g.fill(l + 2, t + 2, l + W - 2, t + H - 2, PANEL_BG);

        // Banner
        drawBanner(g, l + LEFT_BANNER_X, t + BANNER_Y);
        drawBanner(g, l + RIGHT_BANNER_X, t + BANNER_Y);

        // Titel: RiftKing (weiss) + Extras (lila)
        Component a = Component.literal("RiftKing").withStyle(s -> s.withColor(0xFFFFFF).withBold(true));
        Component b = Component.literal("Extras").withStyle(s -> s.withColor(TITLE_PURPLE).withBold(true));
        int wa = this.font.width(a), wb = this.font.width(b);
        int tx = l + (W - wa - wb) / 2;
        g.text(this.font, a, tx, t + 6, 0xFFFFFFFF, true);
        g.text(this.font, b, tx + wa, t + 6, 0xFFFFFFFF, true);

        // Spieler-Vorschau
        g.fill(l + 56, t + 18, l + 124, t + 90, VIEW_BG);
        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    g, l + 56, t + 18, l + 124, t + 90, 30, 0.0625F, mouseX, mouseY, this.minecraft.player);
        }

        // Slot-Boxen mit Rarity-Rahmen (1 px)
        for (int i = 5; i <= 44; i++) {
            Slot s = this.menu.slots.get(i);
            box(g, l + s.x - 1, t + s.y - 1, Rarity.borderColor(s.getItem()));
        }
        for (int k = 0; k < 4; k++) {
            box(g, l + EQUIP_X - 1, t + EQUIP_Y + k * 18 - 1, Rarity.borderColor(EquipmentCache.ITEMS[k]));
        }

        // Banner-Buttons: Icon + Hover
        for (Btn b2 : buttons) {
            g.item(b2.icon, l + b2.x, t + b2.y);
            if (hit(mouseX, mouseY, l + b2.x, t + b2.y, BTN, BTN)) {
                g.fill(l + b2.x, t + b2.y, l + b2.x + BTN, t + b2.y + BTN, 0x50FFFFFF);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int l = this.leftPos, t = this.topPos;

        // Equipment-Items (keine echten Slots)
        for (int k = 0; k < 4; k++) {
            ItemStack st = EquipmentCache.ITEMS[k];
            int x = l + EQUIP_X, y = t + EQUIP_Y + k * 18;
            if (!st.isEmpty()) {
                g.item(st, x, y);
                g.itemDecorations(this.font, st, x, y);
            }
            if (hit(mouseX, mouseY, x - 1, y - 1, 18, 18)) {
                g.fill(x, y, x + 16, y + 16, 0x40FFFFFF);
                if (!st.isEmpty()) {
                    g.setTooltipForNextFrame(this.font, st, mouseX, mouseY);
                } else {
                    g.setTooltipForNextFrame(this.font, Component.literal("Klick: /stats"), mouseX, mouseY);
                }
            }
        }

        // Button-Tooltips
        for (Btn b : buttons) {
            if (hit(mouseX, mouseY, l + b.x, t + b.y, BTN, BTN)) {
                String text = "Linksklick: /" + b.left + (b.right != null ? "   Rechtsklick: /" + b.right : "");
                g.setTooltipForNextFrame(this.font, Component.literal(text), mouseX, mouseY);
            }
        }

        this.extractTooltip(g, mouseX, mouseY);
    }

    /** Standard-Labels ("Inventory", "Crafting") ausblenden. */
    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    }

    private static void box(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + 18, y + 18, color);
        g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_BG);
    }

    private static void drawBanner(GuiGraphicsExtractor g, int x, int y) {
        int rows = BANNER_BODY + BANNER_TIP;
        for (int r = 0; r < rows; r++) {
            int inset = r < BANNER_BODY ? 0 : (r - BANNER_BODY + 1) * (BANNER_W / 2) / BANNER_TIP;
            int x0 = x + inset, x1 = x + BANNER_W - inset;
            if (x1 - x0 < 2) continue;
            g.fill(x0, y + r, x1, y + r + 1, BANNER_EDGE);
            if (x1 - x0 > 2) {
                g.fill(x0 + 1, y + r, x1 - 1, y + r + 1, BANNER_BODY_C);
                g.fill(x0 + 1, y + r, x0 + 2, y + r + 1, BANNER_LIGHT);
            }
        }
        g.fill(x, y, x + BANNER_W, y + 1, BANNER_EDGE);
        g.fill(x + 1, y + 1, x + BANNER_W - 1, y + 2, BANNER_LIGHT);
    }

    private static boolean hit(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ------------------------------------------------------------------ Eingabe

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int button = event.button(); // 0 = links, 1 = rechts
        int l = this.leftPos, t = this.topPos;

        for (Btn b : buttons) {
            if (hit(mx, my, l + b.x, t + b.y, BTN, BTN)) {
                String cmd = button == 0 ? b.left : (button == 1 ? b.right : null);
                if (cmd != null) send(cmd);
                return true;
            }
        }
        for (int k = 0; k < 4; k++) {
            if (hit(mx, my, l + EQUIP_X - 1, t + EQUIP_Y + k * 18 - 1, 18, 18)) {
                send("stats");
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static void send(String command) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) p.connection.sendCommand(command);
    }
}
