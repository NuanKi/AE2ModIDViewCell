package me.emvoh.midviewcell;

import appeng.api.stacks.AEKey;
import appeng.api.storage.AEKeyFilter;
import appeng.util.prioritylist.IPartitionList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import me.emvoh.midviewcell.items.ModIDViewCellItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModIdPartitionList implements IPartitionList {
    private final AEKeyFilter filter;
    private final List<Pattern> patterns;

    private ModIdPartitionList(AEKeyFilter filter, List<Pattern> patterns) {
        this.filter = filter;
        this.patterns = List.copyOf(patterns);
    }

    @Nullable
    public static ModIdPartitionList fromViewCells(
            AEKeyFilter filter,
            Collection<ItemStack> viewCells,
            boolean whitelist) {
        List<String> entries = new ArrayList<>();
        for (ItemStack stack : viewCells) {
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof ModIDViewCellItem) {
                entries.addAll(whitelist
                        ? ModIDViewCellItem.getModIdWhitelist(stack)
                        : ModIDViewCellItem.getModIdBlacklist(stack));
            }
        }
        return fromEntries(filter, entries);
    }

    @Nullable
    private static ModIdPartitionList fromEntries(AEKeyFilter filter, List<String> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        List<Pattern> compiled = new ArrayList<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }

            String normalized = entry.trim();
            if (normalized.startsWith("@")) {
                normalized = normalized.substring(1);
            }
            if (normalized.isBlank()) {
                continue;
            }

            try {
                compiled.add(Pattern.compile(normalized));
            } catch (PatternSyntaxException exception) {
                Main.LOGGER.warn("Ignoring invalid mod id regex '{}': {}", normalized, exception.getMessage());
            }
        }

        return compiled.isEmpty() ? null : new ModIdPartitionList(filter, compiled);
    }

    @Override
    public boolean isListed(AEKey key) {
        return filter.matches(key) && matchesModId(key.getModId());
    }

    @Override
    public boolean isEmpty() {
        return patterns.isEmpty();
    }

    @Override
    public Iterable<AEKey> getItems() {
        return List.of();
    }

    private boolean matchesModId(String modId) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(modId).matches()) {
                return true;
            }
        }
        return false;
    }
}
