package me.emvoh.midviewcell.items;

import appeng.api.config.FuzzyMode;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.items.storage.ViewCellItem;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.ConfigInventory;
import java.util.ArrayList;
import java.util.List;
import me.emvoh.midviewcell.ModIdFilterTarget;
import me.emvoh.midviewcell.client.hooks.ClientHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

public class ModIDViewCellItem extends ViewCellItem {
    private static final String TAG_WHITELIST = "ModIDViewCell_WhiteList";
    private static final String TAG_BLACKLIST = "ModIDViewCell_BlackList";

    public ModIDViewCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return ConfigInventory.emptyTypes();
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.empty();
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
    }

    public static List<String> getModIdWhitelist(ItemStack stack) {
        return getStringList(stack, TAG_WHITELIST);
    }

    public static List<String> getModIdBlacklist(ItemStack stack) {
        return getStringList(stack, TAG_BLACKLIST);
    }

    public static void setModIdFilters(ItemStack stack, List<String> whitelist, List<String> blacklist) {
        setStringList(stack, TAG_WHITELIST, whitelist);
        setStringList(stack, TAG_BLACKLIST, blacklist);
    }

    private static List<String> getStringList(ItemStack stack, String key) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }

        ListTag listTag = tag.getList(key, Tag.TAG_STRING);
        if (listTag.isEmpty()) {
            return List.of();
        }

        List<String> values = new ArrayList<>(listTag.size());
        for (int i = 0; i < listTag.size(); i++) {
            String value = listTag.getString(i);
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private static void setStringList(ItemStack stack, String key, List<String> values) {
        ListTag listTag = new ListTag();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                listTag.add(StringTag.valueOf(value.trim()));
            }
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (listTag.isEmpty()) {
                tag.remove(key);
            } else {
                tag.put(key, listTag);
            }
        });
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (usedHand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            openModIdFilterScreen(stack, ModIdFilterTarget.forHand(usedHand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            @NotNull ItemStack stack,
            @NotNull ItemStack other,
            @NotNull Slot slot,
            @NotNull ClickAction action,
            @NotNull Player player,
            @NotNull SlotAccess access) {
        if (action == ClickAction.SECONDARY && other.isEmpty() && isTerminalViewCellSlot(player, slot)) {
            if (player.level().isClientSide()) {
                openModIdFilterScreen(stack, ModIdFilterTarget.forSlot(player.containerMenu.containerId, slot.index));
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            Item.@NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag) {
        if (!isShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.appliedenergistics2.mod_view_cell.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        tooltipComponents.add(Component.empty());
        addFilterTooltipSection(
                tooltipComponents,
                "tooltip.appliedenergistics2.mod_view_cell.whitelist",
                getModIdWhitelist(stack),
                ChatFormatting.GREEN);
        addFilterTooltipSection(
                tooltipComponents,
                "tooltip.appliedenergistics2.mod_view_cell.blacklist",
                getModIdBlacklist(stack),
                ChatFormatting.RED);
    }

    private static void addFilterTooltipSection(
            List<Component> tooltipComponents,
            String titleKey,
            List<String> entries,
            ChatFormatting titleColor) {
        tooltipComponents.add(Component.translatable(titleKey).withStyle(titleColor));
        if (entries.isEmpty()) {
            tooltipComponents.add(Component.translatable("tooltip.appliedenergistics2.mod_view_cell.none")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        for (String entry : entries) {
            tooltipComponents.add(Component.literal("  - ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.translatable(
                            "tooltip.appliedenergistics2.mod_view_cell.entry", entry)
                            .withStyle(ChatFormatting.GRAY)));
        }
    }

    private static boolean isShiftDown() {
        return FMLEnvironment.dist == Dist.CLIENT && ClientHooks.hasShiftDown();
    }

    private static boolean isTerminalViewCellSlot(Player player, Slot slot) {
        return player.containerMenu instanceof MEStorageMenu menu
                && menu.getSlots(SlotSemantics.VIEW_CELL).contains(slot);
    }

    private static void openModIdFilterScreen(ItemStack stack, ModIdFilterTarget target) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientHooks.openModIdFilterScreen(stack, target);
        }
    }
}
