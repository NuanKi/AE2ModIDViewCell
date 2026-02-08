package me.emvoh.midviewcell.mixin;

import appeng.container.slot.SlotRestrictedInput;
import appeng.items.storage.ItemViewCell;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SlotRestrictedInput.class, remap = false)
public abstract class MixinSlotRestrictedInput_ViewCell {

    @Inject(
            method = { "isItemValid(Lnet/minecraft/item/ItemStack;)Z",
                       "func_75214_a(Lnet/minecraft/item/ItemStack;)Z" },
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void midviewcell$acceptAnyItemViewCell(final ItemStack stack, final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        final SlotRestrictedInput self = (SlotRestrictedInput) (Object) this;

        if (self.getPlaceableItemType() == SlotRestrictedInput.PlacableItemType.VIEW_CELL
                && !stack.isEmpty()
                && stack.getItem() instanceof ItemViewCell) {
            cir.setReturnValue(true);
        }
    }
}
