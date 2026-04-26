package me.emvoh.midviewcell.network;

import me.emvoh.midviewcell.Main;
import me.emvoh.midviewcell.access.MEStorageMenuAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InsertViewCellIntoNetworkPayload(int containerId, int slotIndex) implements CustomPacketPayload {
    public static final Type<InsertViewCellIntoNetworkPayload> TYPE =
            new Type<>(Main.id("insert_view_cell_into_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertViewCellIntoNetworkPayload> STREAM_CODEC =
            StreamCodec.ofMember(InsertViewCellIntoNetworkPayload::write, InsertViewCellIntoNetworkPayload::decode);

    public static InsertViewCellIntoNetworkPayload decode(RegistryFriendlyByteBuf buffer) {
        return new InsertViewCellIntoNetworkPayload(buffer.readVarInt(), buffer.readVarInt());
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.containerId);
        buffer.writeVarInt(this.slotIndex);
    }

    @Override
    public Type<InsertViewCellIntoNetworkPayload> type() {
        return TYPE;
    }

    public static void handle(InsertViewCellIntoNetworkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AbstractContainerMenu menu = player.containerMenu;
                if (menu.containerId == payload.containerId && menu instanceof MEStorageMenuAccess access) {
                    access.midviewcell$insertPlayerSlotIntoNetwork(payload.slotIndex);
                }
            }
        });
    }
}
