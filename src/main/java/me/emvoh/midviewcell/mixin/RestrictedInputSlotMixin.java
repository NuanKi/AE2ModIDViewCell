package me.emvoh.midviewcell.mixin;

import appeng.items.storage.ViewCellItem;
import appeng.menu.slot.RestrictedInputSlot;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = RestrictedInputSlot.class, remap = false)
public abstract class RestrictedInputSlotMixin {
    @ModifyExpressionValue(
            method = "mayPlace",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/definitions/ItemDefinition;is(Lnet/minecraft/world/item/ItemStack;)Z",
                    ordinal = 0),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            target = "Lappeng/core/definitions/AEItems;VIEW_CELL:Lappeng/core/definitions/ItemDefinition;")))
    private boolean midviewcell$mayPlaceAllowViewCellItem(boolean original, ItemStack stack) {
        return original || stack.getItem() instanceof ViewCellItem;
    }
}
