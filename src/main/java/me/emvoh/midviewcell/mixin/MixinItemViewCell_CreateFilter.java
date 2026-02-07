package me.emvoh.midviewcell.mixin;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.storage.ItemViewCell;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.MergedPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@Mixin(value = ItemViewCell.class, remap = false)
public abstract class MixinItemViewCell_CreateFilter {

    /**
     * Extends AE2 view cell filtering to also support midviewcwll ModItemViewCell.
     * This overwrite is marked remap=false because AE2 is not part of MCP mappings.
     */
    @Overwrite(remap = false)
    public static IPartitionList<IAEItemStack> createFilter(final ItemStack[] list) {
        IPartitionList<IAEItemStack> myPartitionList = null;
        final MergedPriorityList<IAEItemStack> myMergedList = new MergedPriorityList<>();

        if (list == null) {
            return null;
        }

        for (final ItemStack currentViewCell : list) {
            if (currentViewCell == null || currentViewCell.isEmpty()) {
                continue;
            }

            // Your custom view cell (modid whitelist/blacklist)
            if (currentViewCell.getItem() instanceof me.emvoh.midviewcell.items.ModItemViewCell) {

                final List<String> wl = me.emvoh.midviewcell.items.ModItemViewCell.getTagWhitelist(currentViewCell);
                final List<String> bl = me.emvoh.midviewcell.items.ModItemViewCell.getTagBlacklist(currentViewCell);

                boolean addedAnything = false;

                if (wl != null && !wl.isEmpty()) {
                    // Change this class path if your ModIdPriorityList is in a different package
                    myMergedList.addNewList(new me.emvoh.midviewcell.util.ModIdPriorityList<>(wl), true);
                    addedAnything = true;
                }

                if (bl != null && !bl.isEmpty()) {
                    myMergedList.addNewList(new me.emvoh.midviewcell.util.ModIdPriorityList<>(bl), false);
                    addedAnything = true;
                }

                if (addedAnything) {
                    myPartitionList = myMergedList;
                }

                continue;
            }

            // Original AE2 view cell behavior
            if (currentViewCell.getItem() instanceof ItemViewCell) {

                final IItemList<IAEItemStack> priorityList =
                        AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

                final ICellWorkbenchItem vc = (ICellWorkbenchItem) currentViewCell.getItem();
                final IItemHandler upgrades = vc.getUpgradesInventory(currentViewCell);
                final IItemHandler config = vc.getConfigInventory(currentViewCell);
                final FuzzyMode fzMode = vc.getFuzzyMode(currentViewCell);

                boolean hasInverter = false;
                boolean hasFuzzy = false;

                for (int x = 0; x < upgrades.getSlots(); x++) {
                    final ItemStack is = upgrades.getStackInSlot(x);
                    if (!is.isEmpty() && is.getItem() instanceof IUpgradeModule) {
                        final Upgrades u = ((IUpgradeModule) is.getItem()).getType(is);
                        if (u != null) {
                            switch (u) {
                                case FUZZY:
                                    hasFuzzy = true;
                                    break;
                                case INVERTER:
                                    hasInverter = true;
                                    break;
                                default:
                            }
                        }
                    }
                }

                for (int x = 0; x < config.getSlots(); x++) {
                    final ItemStack is = config.getStackInSlot(x);
                    if (!is.isEmpty()) {
                        priorityList.add(AEItemStack.fromItemStack(is));
                    }
                }

                if (!priorityList.isEmpty()) {
                    if (hasFuzzy) {
                        myMergedList.addNewList(new FuzzyPriorityList<>(priorityList, fzMode), !hasInverter);
                    } else {
                        myMergedList.addNewList(new PrecisePriorityList<>(priorityList), !hasInverter);
                    }
                    myPartitionList = myMergedList;
                }
            }
        }

        return myPartitionList;
    }
}
