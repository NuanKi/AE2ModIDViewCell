package me.emvoh.midviewcell.access;

import java.util.List;

public interface MEStorageMenuAccess {
    void midviewcell$updateStoredModIdViewCell(
            long serial,
            List<String> whitelist,
            List<String> blacklist
    );

    void midviewcell$insertPlayerSlotIntoNetwork(int slotIndex);
}
