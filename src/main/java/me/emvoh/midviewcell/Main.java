package me.emvoh.midviewcell;

import appeng.api.ids.AECreativeTabIds;
import appeng.core.definitions.AEItems;
import com.mojang.logging.LogUtils;
import me.emvoh.midviewcell.items.ModIDViewCellItem;
import me.emvoh.midviewcell.network.ModNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "ae2modidviewcell";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredItem<ModIDViewCellItem> MOD_ID_VIEW_CELL = ITEMS.register(
            "mod_id_view_cell",
            () -> new ModIDViewCellItem(new Item.Properties().stacksTo(1)));

    public Main(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModNetwork::registerPayloads);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(AECreativeTabIds.MAIN)) {
            return;
        }

        ItemStack viewCell = AEItems.VIEW_CELL.stack();
        ItemStack modIdViewCell = MOD_ID_VIEW_CELL.toStack();
        if (event.getParentEntries().contains(viewCell) && event.getSearchEntries().contains(viewCell)) {
            event.insertAfter(viewCell, modIdViewCell, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else {
            event.accept(modIdViewCell, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
