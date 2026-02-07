package me.emvoh.midviewcell.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiTexturedButton extends GuiButton {
    private final ResourceLocation tex;
    private final int texW, texH;
    private final int u;
    private final int vNormal;
    private final int vHover;

    // Text colors
    private final int textNormal;
    private final int textHover;
    private final int textDisabled;

    // Backward-compatible constructor, uses your preferred defaults
    public GuiTexturedButton(int id, int x, int y, int w, int h, String text,
                             ResourceLocation tex, int texW, int texH,
                             int u, int vNormal, int vHover) {
        this(id, x, y, w, h, text, tex, texW, texH, u, vNormal, vHover,
                0x505050, 0xFFFFA0, 0xB8B8B8);
    }

    // New constructor with custom colors
    public GuiTexturedButton(int id, int x, int y, int w, int h, String text,
                             ResourceLocation tex, int texW, int texH,
                             int u, int vNormal, int vHover,
                             int textNormal, int textHover, int textDisabled) {
        super(id, x, y, w, h, text);
        this.tex = tex;
        this.texW = texW;
        this.texH = texH;
        this.u = u;
        this.vNormal = vNormal;
        this.vHover = vHover;

        this.textNormal = textNormal;
        this.textHover = textHover;
        this.textDisabled = textDisabled;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        this.hovered = mouseX >= this.x && mouseY >= this.y
                && mouseX < this.x + this.width && mouseY < this.y + this.height;

        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(this.tex);

        int v = this.hovered ? this.vHover : this.vNormal;

        // draw the textured button
        this.drawModalRectWithCustomSizedTexture(
                this.x, this.y,
                this.u, v,
                this.width, this.height,
                this.texW, this.texH
        );


        // draw the text on top
        int color = !this.enabled ? 0xA0A0A0 : (this.hovered ? 0xFFFFA0 : 0x505050);

        int tx = this.x + (this.width - mc.fontRenderer.getStringWidth(this.displayString)) / 2;
        int ty = this.y + (this.height - 8) / 2;

        mc.fontRenderer.drawString(this.displayString, tx, ty, color, false); // no shadow

    }
}
