package me.emvoh.midviewcell.util;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.prioritylist.IPartitionList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModIdPriorityList<T extends IAEStack<T>> implements IPartitionList<T> {

    private final Set<String> modIds;

    public ModIdPriorityList(List<String> modIds) {
        this.modIds = normalizeToSet(modIds);
    }

    @Override
    public boolean isListed(final T input) {
        if (!(input instanceof IAEItemStack)) {
            return false;
        }

        final ItemStack stack = ((IAEItemStack) input).createItemStack();
        if (stack.isEmpty()) {
            return false;
        }

        final ResourceLocation rl = stack.getItem().getRegistryName();
        if (rl == null) {
            return false;
        }

        final String modid = rl.getNamespace().toLowerCase();
        return this.modIds.contains(modid);
    }

    @Override
    public boolean isEmpty() {
        return this.modIds.isEmpty();
    }

    @Override
    public Iterable<T> getItems() {
        return new ArrayList<>();
    }

    private static Set<String> normalizeToSet(List<String> values) {
        final Set<String> out = new HashSet<>();
        if (values == null) return out;

        for (String s : values) {
            if (s == null) continue;
            s = s.trim().toLowerCase();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }
}
