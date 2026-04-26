package me.emvoh.midviewcell.network;

import me.emvoh.midviewcell.Main;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Main.MODID)
                .playToServer(
                        UpdateModIdFiltersPayload.TYPE,
                        UpdateModIdFiltersPayload.STREAM_CODEC,
                        UpdateModIdFiltersPayload::handle)
                .playToServer(
                        InsertViewCellIntoNetworkPayload.TYPE,
                        InsertViewCellIntoNetworkPayload.STREAM_CODEC,
                        InsertViewCellIntoNetworkPayload::handle);
    }
}
