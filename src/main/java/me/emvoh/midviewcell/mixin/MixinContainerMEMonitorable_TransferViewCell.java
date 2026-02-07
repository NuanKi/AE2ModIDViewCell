package me.emvoh.midviewcell.mixin;

import appeng.api.definitions.IItemDefinition;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.items.storage.ItemViewCell;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ContainerMEMonitorable.class, remap = false)
public abstract class MixinContainerMEMonitorable_TransferViewCell {

    @Redirect(
            method = "transferStackInSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/api/definitions/IItemDefinition;isSameAs(Lnet/minecraft/item/ItemStack;)Z",
                    remap = false
            ),
            remap = false
    )
    private boolean ae2modidviewcell$acceptAnyItemViewCell(IItemDefinition def, ItemStack stack) {
        // Equivalent to: !(stack.isEmpty() || !(stack.getItem() instanceof ItemViewCell))
        return !stack.isEmpty() && stack.getItem() instanceof ItemViewCell;
    }
}
