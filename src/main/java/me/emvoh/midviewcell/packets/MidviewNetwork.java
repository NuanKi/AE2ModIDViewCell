package me.emvoh.midviewcell.packets;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class MidviewNetwork {

    public static final String CHANNEL = "ae2modidviewcell";
    public static final SimpleNetworkWrapper NET = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);

    private static int id = 0;

    public static void init() {
        NET.registerMessage(
                ModIDViewCellPacket.Handler.class,
                ModIDViewCellPacket.class,
                id++,
                Side.SERVER
        );
    }

    private MidviewNetwork() {}
}
