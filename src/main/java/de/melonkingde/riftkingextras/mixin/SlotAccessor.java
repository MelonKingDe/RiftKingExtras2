package de.melonkingde.riftkingextras.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Macht die (finalen) Slot-Koordinaten schreibbar, damit wir das Layout umbauen koennen. */
@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("x")
    void riftkingextras$setX(int x);

    @Mutable
    @Accessor("y")
    void riftkingextras$setY(int y);
}
