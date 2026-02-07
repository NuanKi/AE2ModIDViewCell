package me.emvoh.midviewcell.packets;

import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.emvoh.midviewcell.items.ModItemViewCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ModIDViewCellPacket extends AppEngPacket {
    private static final int MAX_ENTRIES = 256;
    private static final int MAX_STR_LEN = 64;

    private final EnumHand hand;
    private final List<String> whitelist;
    private final List<String> blacklist;

    // decode (called by handler after packet id was read)
    public ModIDViewCellPacket(final ByteBuf stream) throws IOException {
        final PacketBuffer pb = new PacketBuffer(stream);

        int handOrdinal = pb.readByte() & 0xFF;
        this.hand = EnumHand.values()[Math.min(handOrdinal, EnumHand.values().length - 1)];

        this.whitelist = readStringList(pb);
        this.blacklist = readStringList(pb);
    }


    public ModIDViewCellPacket(final EnumHand hand, final List<String> whitelist, final List<String> blacklist) {
        this.hand = hand == null ? EnumHand.MAIN_HAND : hand;
        this.whitelist = whitelist == null ? new ArrayList<>() : whitelist;
        this.blacklist = blacklist == null ? new ArrayList<>() : blacklist;

        final ByteBuf data = Unpooled.buffer();
        final PacketBuffer pb = new PacketBuffer(data);

        pb.writeInt(this.getPacketID());
        pb.writeByte(this.hand.ordinal());

        writeStringList(pb, this.whitelist);
        writeStringList(pb, this.blacklist);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        final EntityPlayerMP sender = (EntityPlayerMP) player;

        final ItemStack held = sender.getHeldItem(this.hand);
        if (held.isEmpty() || !(held.getItem() instanceof ModItemViewCell)) {
            return;
        }

        final List<String> wl = sanitize(this.whitelist);
        final List<String> bl = sanitize(this.blacklist);

        ModItemViewCell.setTagFilters(held, wl, bl);

        sender.inventory.markDirty();
        sender.inventoryContainer.detectAndSendChanges();
        if (sender.openContainer != null) {
            sender.openContainer.detectAndSendChanges();
        }
    }

    private static void writeStringList(PacketBuffer pb, List<String> list) {
        int n = list == null ? 0 : Math.min(list.size(), MAX_ENTRIES);
        pb.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            String s = list.get(i);
            if (s == null) s = "";
            if (s.length() > MAX_STR_LEN) s = s.substring(0, MAX_STR_LEN);
            pb.writeString(s);
        }
    }

    private static List<String> readStringList(PacketBuffer pb) {
        int n = pb.readVarInt();
        if (n < 0) n = 0;
        if (n > MAX_ENTRIES) n = MAX_ENTRIES;

        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(pb.readString(MAX_STR_LEN));
        }
        return out;
    }

    private static List<String> sanitize(List<String> in) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (in == null) return new ArrayList<>();

        for (String s : in) {
            if (out.size() >= MAX_ENTRIES) break;
            if (s == null) continue;

            s = s.trim().toLowerCase();

            // allow "@modid" input, store as "modid"
            if (s.startsWith("@")) s = s.substring(1).trim();

            while (!s.isEmpty() && (s.endsWith(",") || s.endsWith(";"))) {
                s = s.substring(0, s.length() - 1).trim();
            }

            if (s.isEmpty()) continue;
            if (s.length() > MAX_STR_LEN) s = s.substring(0, MAX_STR_LEN);

            out.add(s);
        }

        return new ArrayList<>(out);
    }
}
