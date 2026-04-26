package me.emvoh.midviewcell.mixin;

import appeng.client.gui.AEBaseScreen;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.common.MEStorageMenu;
import me.emvoh.midviewcell.network.InsertViewCellIntoNetworkPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AEBaseScreen.class, remap = false)
public abstract class AEBaseScreenMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void midviewcell$shiftRightClickViewCellToNetwork(
            Slot slot,
            int slotIdx,
            int mouseButton,
            ClickType clickType,
            CallbackInfo ci) {
        if (slot == null
                || mouseButton != 1
                || clickType != ClickType.QUICK_MOVE
                || !(slot.container instanceof Inventory)
                || !(slot.getItem().getItem() instanceof ViewCellItem)) {
            return;
        }

        AEBaseScreen<?> screen = (AEBaseScreen<?>) (Object) this;
        if (!(screen.getMenu() instanceof MEStorageMenu menu)) {
            return;
        }

        PacketDistributor.sendToServer(new InsertViewCellIntoNetworkPayload(menu.containerId, slotIdx));
        ci.cancel();
    }
}
