package me.emvoh.midviewcell.client.models;

import me.emvoh.midviewcell.Main;
import me.emvoh.midviewcell.Tags;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public final class ClientModels {

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                Main.MOD_VIEW_CELL,
                0,
                new ModelResourceLocation(Tags.MODID + ":mod_view_cell", "inventory")
        );
    }

    private ClientModels() {}
}