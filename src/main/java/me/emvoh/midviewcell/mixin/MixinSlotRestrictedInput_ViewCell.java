package me.emvoh.midviewcell.mixin;

import appeng.api.definitions.IItemDefinition;
import appeng.container.slot.SlotRestrictedInput;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SlotRestrictedInput.class, remap = false)
public abstract class MixinSlotRestrictedInput_ViewCell {

    @Redirect(
            method = {
                    "isItemValid(Lnet/minecraft/item/ItemStack;)Z",   // dev (deobf)
                    "func_75214_a(Lnet/minecraft/item/ItemStack;)Z"   // prod (reobf/SRG override of Slot#isItemValid)
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/definitions/IItemDefinition;isSameAs(Lnet/minecraft/item/ItemStack;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean midviewcell$viewCellAcceptMyItem(IItemDefinition def, ItemStack stack) {
        if (def.isSameAs(stack)) {
            return true;
        }

        SlotRestrictedInput self = (SlotRestrictedInput) (Object) this;
        return self.getPlaceableItemType() == SlotRestrictedInput.PlacableItemType.VIEW_CELL
                && stack.getItem() instanceof me.emvoh.midviewcell.items.ModItemViewCell;
    }
}
