package me.emvoh.midviewcell.client.hooks;

import me.emvoh.midviewcell.ModIdFilterTarget;
import me.emvoh.midviewcell.client.guis.implementations.ModIdFilterScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openModIdFilterScreen(ItemStack stack, ModIdFilterTarget target) {
        Minecraft.getInstance().setScreen(new ModIdFilterScreen(stack, target));
    }

    public static boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }
}
