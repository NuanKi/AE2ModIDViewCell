package me.emvoh.midviewcell;

import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;

public record ModIdFilterTarget(@Nullable InteractionHand hand, int containerId, int slotIndex, long terminalSerial) {
    public static ModIdFilterTarget forHand(InteractionHand hand) {
        return new ModIdFilterTarget(hand, -1, -1, -1);
    }

    public static ModIdFilterTarget forSlot(int containerId, int slotIndex) {
        return new ModIdFilterTarget(null, containerId, slotIndex, -1);
    }

    public static ModIdFilterTarget forTerminalEntry(int containerId, long terminalSerial) {
        return new ModIdFilterTarget(null, containerId, -1, terminalSerial);
    }

    public boolean isHand() {
        return hand != null;
    }

    public boolean isTerminalEntry() {
        return hand == null && terminalSerial >= 0;
    }
}
