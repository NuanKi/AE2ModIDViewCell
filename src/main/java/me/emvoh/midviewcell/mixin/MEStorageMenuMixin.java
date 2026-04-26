package me.emvoh.midviewcell.mixin;

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.core.definitions.AEItems;
import appeng.items.storage.ViewCellItem;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.slot.RestrictedInputSlot;
import me.emvoh.midviewcell.items.ModIDViewCellItem;
import me.emvoh.midviewcell.access.MEStorageMenuAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MEStorageMenuMixin implements MEStorageMenuAccess {
    @Shadow
    @Final
    private List<RestrictedInputSlot> viewCellSlots;

    @Shadow
    @Final
    protected MEStorage storage;

    @Shadow
    @Final
    protected IEnergySource energySource;

    @Shadow
    @Nullable
    protected abstract AEKey getStackBySerial(long serial);

    @Inject(method = "transferStackToMenu", at = @At("HEAD"), cancellable = true)
    private void midviewcell$moveConfiguredViewCellsToSlotsFirst(
            ItemStack input,
            CallbackInfoReturnable<Integer> cir) {
        if (!midviewcell$shouldPreferViewCellSlot(input)) {
            return;
        }

        int originalCount = input.getCount();
        for (RestrictedInputSlot slot : this.viewCellSlots) {
            if (slot.hasItem() || !slot.mayPlace(input)) {
                continue;
            }

            input = slot.safeInsert(input);
            int moved = originalCount - input.getCount();
            if (moved > 0) {
                cir.setReturnValue(moved);
                return;
            }
        }
    }

    @Unique
    private boolean midviewcell$shouldPreferViewCellSlot(ItemStack stack) {
        if (!(stack.getItem() instanceof ViewCellItem)) {
            return false;
        }

        if (stack.getItem() instanceof ModIDViewCellItem) {
            return !ModIDViewCellItem.getModIdWhitelist(stack).isEmpty()
                    || !ModIDViewCellItem.getModIdBlacklist(stack).isEmpty();
        }

        if (AEItems.VIEW_CELL.is(stack)) {
            return midviewcell$hasCellWorkbenchConfig(stack);
        }

        return true;
    }

    @Override
    public void midviewcell$updateStoredModIdViewCell(long serial, List<String> whitelist, List<String> blacklist) {
        AEKey key = getStackBySerial(serial);
        if (!(key instanceof AEItemKey itemKey)) {
            return;
        }

        ItemStack updatedStack = itemKey.toStack();
        if (!(updatedStack.getItem() instanceof ModIDViewCellItem)) {
            return;
        }

        ModIDViewCellItem.setModIdFilters(updatedStack, whitelist, blacklist);
        AEItemKey updatedKey = AEItemKey.of(updatedStack);
        if (updatedKey == null) {
            return;
        }
        if (updatedKey.equals(itemKey)) {
            return;
        }

        IActionSource actionSource = ((MEStorageMenu) (Object) this).getActionSource();
        long extracted = StorageHelper.poweredExtraction(
                this.energySource,
                this.storage,
                itemKey,
                1,
                actionSource);
        if (extracted <= 0) {
            return;
        }

        long inserted = StorageHelper.poweredInsert(
                this.energySource,
                this.storage,
                updatedKey,
                1,
                actionSource);
        if (inserted >= 1) {
            return;
        }

        StorageHelper.poweredInsert(this.energySource, this.storage, itemKey, extracted, actionSource);
    }

    @Override
    public void midviewcell$insertPlayerSlotIntoNetwork(int slotIndex) {
        MEStorageMenu menu = (MEStorageMenu) (Object) this;
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return;
        }

        Slot slot = menu.getSlot(slotIndex);
        if (!(slot.container instanceof Inventory) || !slot.mayPickup(menu.getPlayer())) {
            return;
        }

        ItemStack stack = slot.getItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof ViewCellItem)) {
            return;
        }

        AEItemKey key = AEItemKey.of(stack);
        if (key == null) {
            return;
        }

        long inserted = StorageHelper.poweredInsert(
                this.energySource,
                this.storage,
                key,
                stack.getCount(),
                menu.getActionSource());
        if (inserted <= 0) {
            return;
        }

        slot.remove((int) Math.min(inserted, stack.getCount()));
        slot.setChanged();
        menu.broadcastChanges();
    }

    @Unique
    private boolean midviewcell$hasCellWorkbenchConfig(ItemStack stack) {
        ICellWorkbenchItem viewCell = (ICellWorkbenchItem) stack.getItem();
        var config = viewCell.getConfigInventory(stack);
        for (int i = 0; i < config.size(); i++) {
            if (config.getKey(i) != null) {
                return true;
            }
        }
        return false;
    }
}
