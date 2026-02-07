package me.emvoh.midviewcell.items;

import appeng.items.storage.ItemViewCell;
import appeng.util.Platform;
import me.emvoh.midviewcell.client.gui.GuiModViewCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ModItemViewCell extends ItemViewCell {

    private static final String TAG_WHITELIST = "ModViewCell_WhiteList";
    private static final String TAG_BLACKLIST = "ModViewCell_BlackList";

    public ModItemViewCell() {
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
        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        if (world.isRemote) {
            GuiModViewCell.open(stack, hand);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
