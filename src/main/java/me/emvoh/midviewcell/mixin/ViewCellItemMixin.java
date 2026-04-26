package me.emvoh.midviewcell.mixin;

import appeng.api.storage.AEKeyFilter;
import appeng.items.storage.ViewCellItem;
import appeng.util.prioritylist.IPartitionList;
import appeng.util.prioritylist.MergedPriorityList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.Collection;
import me.emvoh.midviewcell.ModIdPartitionList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ViewCellItem.class, remap = false)
public class ViewCellItemMixin {
    @ModifyReturnValue(method = "createFilter", at = @At("RETURN"))
    private static IPartitionList midviewcell$applyModIdFilters(
            IPartitionList original,
            AEKeyFilter filter,
            Collection<ItemStack> viewCells) {
        ModIdPartitionList whitelist = ModIdPartitionList.fromViewCells(filter, viewCells, true);
        ModIdPartitionList blacklist = ModIdPartitionList.fromViewCells(filter, viewCells, false);
        if (whitelist == null && blacklist == null) {
            return original;
        }

        MergedPriorityList merged;
        if (original instanceof MergedPriorityList existing) {
            merged = existing;
        } else {
            merged = new MergedPriorityList();
            if (original != null) {
                merged.addNewList(original, true);
            }
        }

        if (whitelist != null) {
            merged.addNewList(whitelist, true);
        }
        if (blacklist != null) {
            merged.addNewList(blacklist, false);
        }
        return merged;
    }
}
