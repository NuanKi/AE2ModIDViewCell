package me.emvoh.midviewcell.network;

import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import me.emvoh.midviewcell.Main;
import me.emvoh.midviewcell.ModIdFilterTarget;
import me.emvoh.midviewcell.items.ModIDViewCellItem;
import me.emvoh.midviewcell.access.MEStorageMenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateModIdFiltersPayload(
        ModIdFilterTarget target,
        List<String> whitelist,
        List<String> blacklist) implements CustomPacketPayload {
    private static final int MAX_FILTERS = 128;
    private static final int MAX_FILTER_LENGTH = 256;
    private static final int TARGET_HAND = 0;
    private static final int TARGET_SLOT = 1;
    private static final int TARGET_TERMINAL_ENTRY = 2;

    public static final Type<UpdateModIdFiltersPayload> TYPE = new Type<>(Main.id("update_mod_id_filters"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateModIdFiltersPayload> STREAM_CODEC =
            StreamCodec.ofMember(UpdateModIdFiltersPayload::write, UpdateModIdFiltersPayload::decode);

    public UpdateModIdFiltersPayload {
        whitelist = normalizeStringList(whitelist);
        blacklist = normalizeStringList(blacklist);
    }

    @Override
    public Type<UpdateModIdFiltersPayload> type() {
        return TYPE;
    }

    public static UpdateModIdFiltersPayload decode(RegistryFriendlyByteBuf buffer) {
        ModIdFilterTarget target = readTarget(buffer);
        List<String> whitelist = readStringList(buffer);
        List<String> blacklist = readStringList(buffer);
        return new UpdateModIdFiltersPayload(target, whitelist, blacklist);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        writeTarget(buffer, this.target);
        writeStringList(buffer, this.whitelist);
        writeStringList(buffer, this.blacklist);
    }

    public static void handle(UpdateModIdFiltersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                payload.handleOnServer(player);
            }
        });
    }

    private void handleOnServer(ServerPlayer player) {
        if (this.target.isHand()) {
            InteractionHand hand = this.target.hand();
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ModIDViewCellItem) {
                ModIDViewCellItem.setModIdFilters(stack, this.whitelist, this.blacklist);
                player.setItemInHand(hand, stack);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }
            return;
        }

        if (this.target.isTerminalEntry()) {
            AbstractContainerMenu menu = player.containerMenu;
            if (menu.containerId == this.target.containerId() && menu instanceof MEStorageMenuAccess access) {
                access.midviewcell$updateStoredModIdViewCell(
                        this.target.terminalSerial(),
                        this.whitelist,
                        this.blacklist);
                menu.broadcastChanges();
            }
            return;
        }

        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != this.target.containerId()) {
            return;
        }

        int slotIndex = this.target.slotIndex();
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return;
        }

        Slot slot = menu.getSlot(slotIndex);
        ItemStack stack = slot.getItem();
        if (stack.getItem() instanceof ModIDViewCellItem) {
            ModIDViewCellItem.setModIdFilters(stack, this.whitelist, this.blacklist);
            slot.set(stack);
            menu.broadcastChanges();
        }
    }

    private static void writeTarget(RegistryFriendlyByteBuf buffer, ModIdFilterTarget target) {
        if (target.isHand()) {
            buffer.writeByte(TARGET_HAND);
            buffer.writeEnum(target.hand());
        } else if (target.isTerminalEntry()) {
            buffer.writeByte(TARGET_TERMINAL_ENTRY);
            buffer.writeVarInt(target.containerId());
            buffer.writeVarLong(target.terminalSerial());
        } else {
            buffer.writeByte(TARGET_SLOT);
            buffer.writeVarInt(target.containerId());
            buffer.writeVarInt(target.slotIndex());
        }
    }

    private static ModIdFilterTarget readTarget(RegistryFriendlyByteBuf buffer) {
        int targetType = buffer.readByte();
        return switch (targetType) {
            case TARGET_HAND -> ModIdFilterTarget.forHand(buffer.readEnum(InteractionHand.class));
            case TARGET_SLOT -> ModIdFilterTarget.forSlot(buffer.readVarInt(), buffer.readVarInt());
            case TARGET_TERMINAL_ENTRY -> ModIdFilterTarget.forTerminalEntry(buffer.readVarInt(), buffer.readVarLong());
            default -> throw new DecoderException("Invalid Mod ID filter target: " + targetType);
        };
    }

    private static void writeStringList(RegistryFriendlyByteBuf buffer, List<String> values) {
        if (values.size() > MAX_FILTERS) {
            throw new DecoderException("Too many Mod ID filters: " + values.size());
        }
        buffer.writeVarInt(values.size());
        for (String value : values) {
            buffer.writeUtf(value, MAX_FILTER_LENGTH);
        }
    }

    private static List<String> readStringList(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_FILTERS) {
            throw new DecoderException("Invalid Mod ID filter count: " + count);
        }

        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(buffer.readUtf(MAX_FILTER_LENGTH));
        }
        return values;
    }

    private static List<String> normalizeStringList(List<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String cleaned = normalizeModId(value);
            if (!cleaned.isEmpty()) {
                normalized.add(cleaned);
            }
        }
        if (normalized.size() > MAX_FILTERS) {
            throw new DecoderException("Too many Mod ID filters: " + normalized.size());
        }
        return List.copyOf(normalized);
    }

    private static String normalizeModId(String value) {
        return getString(value);
    }

    @NotNull
    public static String getString(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1).trim();
        }
        while (!normalized.isEmpty() && (normalized.endsWith(",") || normalized.endsWith(";"))) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }
}
