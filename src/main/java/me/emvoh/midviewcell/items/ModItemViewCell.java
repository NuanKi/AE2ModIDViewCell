package me.emvoh.midviewcell.items;

import appeng.items.storage.ItemViewCell;
import appeng.util.Platform;
import me.emvoh.midviewcell.client.gui.GuiModViewCell;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ModItemViewCell extends ItemViewCell {

    private static final String TAG_WHITELIST = "ModViewCell_WhiteList";
    private static final String TAG_BLACKLIST = "ModViewCell_BlackList";

    public ModItemViewCell() {
        super();
        this.setMaxStackSize(1);
    }

    public static List<String> getTagWhitelist(ItemStack stack) {
        return getTagList(stack, TAG_WHITELIST);
    }

    public static List<String> getTagBlacklist(ItemStack stack) {
        return getTagList(stack, TAG_BLACKLIST);
    }

    public static void setTagFilters(ItemStack stack, List<String> whitelist, List<String> blacklist) {
        setTagList(stack, TAG_WHITELIST, whitelist);
        setTagList(stack, TAG_BLACKLIST, blacklist);
    }

    private static List<String> getTagList(ItemStack stack, String tagName) {
        final List<String> out = new ArrayList<>();
        if (stack == null || stack.isEmpty()) {
            return out;
        }

        final NBTTagCompound tag = Platform.openNbtData(stack);

        if (!tag.hasKey(tagName, Constants.NBT.TAG_LIST)) {
            return out;
        }

        final NBTTagList list = tag.getTagList(tagName, Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            String s = list.getStringTagAt(i);
            s = normalizeModId(s);
            if (!s.isEmpty()) {
                out.add(s);
            }
        }

        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    private static void setTagList(ItemStack stack, String tagName, List<String> values) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        final NBTTagCompound tag = Platform.openNbtData(stack);

        if (values == null || values.isEmpty()) {
            tag.removeTag(tagName);
            return;
        }

        final LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String s : values) {
            s = normalizeModId(s);
            if (!s.isEmpty()) {
                cleaned.add(s);
            }
        }

        if (cleaned.isEmpty()) {
            tag.removeTag(tagName);
            return;
        }

        final NBTTagList list = new NBTTagList();
        for (String s : cleaned) {
            list.appendTag(new NBTTagString(s));
        }
        tag.setTag(tagName, list);
    }

    private static String normalizeModId(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();

        while (!s.isEmpty() && (s.endsWith(",") || s.endsWith(";"))) {
            s = s.substring(0, s.length() - 1).trim();
        }

        return s;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        final ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            if (world.isRemote) {
                GuiModViewCell.open(stack, hand);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        if (!GuiScreen.isShiftKeyDown()) {
            lines.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format("tooltip.appliedenergistics2.mod_view_cell.hold_shift"));
            return;
        }

        final List<String> wl = getTagWhitelist(stack);
        final List<String> bl = getTagBlacklist(stack);

        lines.add("");

        lines.add(TextFormatting.GREEN + I18n.format("tooltip.appliedenergistics2.mod_view_cell.whitelist"));
        if (wl.isEmpty()) {
            lines.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format("tooltip.appliedenergistics2.mod_view_cell.none"));
        } else {
            for (final String s : wl) {
                lines.add(TextFormatting.DARK_GRAY + "  - " + TextFormatting.GRAY + I18n.format("tooltip.appliedenergistics2.mod_view_cell.entry", s));
            }
        }

        lines.add(TextFormatting.RED + I18n.format("tooltip.appliedenergistics2.mod_view_cell.blacklist"));
        if (bl.isEmpty()) {
            lines.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format("tooltip.appliedenergistics2.mod_view_cell.none"));
        } else {
            for (final String s : bl) {
                lines.add(TextFormatting.DARK_GRAY + "  - " + TextFormatting.GRAY + I18n.format("tooltip.appliedenergistics2.mod_view_cell.entry", s));
            }
        }
    }
}
