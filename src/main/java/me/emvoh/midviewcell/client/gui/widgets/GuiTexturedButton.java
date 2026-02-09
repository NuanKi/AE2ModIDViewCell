package me.emvoh.midviewcell.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiTexturedButton extends GuiButton {
    private final ResourceLocation tex;
    private final int texW, texH;

    private final int uNormal;
    private final int uHover;   // NEW
    private final int vNormal;
    private final int vHover;

    // Text colors
    private final int textNormal;
    private final int textHover;
    private final int textDisabled;

    // Old constructor (backwards compatible): hover uses same U
    public GuiTexturedButton(int id, int x, int y, int w, int h, String text,
                             ResourceLocation tex, int texW, int texH,
                             int u, int vNormal, int vHover) {
        this(id, x, y, w, h, text, tex, texW, texH,
                u, u, vNormal, vHover,
                0x505050, 0xFFFFA0, 0xB8B8B8);
    }

    // NEW constructor: separate U for hover
    public GuiTexturedButton(int id, int x, int y, int w, int h, String text,
                             ResourceLocation tex, int texW, int texH,
                             int uNormal, int uHover, int vNormal, int vHover) {
        this(id, x, y, w, h, text, tex, texW, texH,
                uNormal, uHover, vNormal, vHover,
                0x505050, 0xFFFFA0, 0xB8B8B8);
    }

    // Full constructor with custom colors
    public GuiTexturedButton(int id, int x, int y, int w, int h, String text,
                             ResourceLocation tex, int texW, int texH,
                             int uNormal, int uHover, int vNormal, int vHover,
                             int textNormal, int textHover, int textDisabled) {
        super(id, x, y, w, h, text);
        this.tex = tex;
        this.texW = texW;
        this.texH = texH;

        this.uNormal = uNormal;
        this.uHover = uHover;
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

        boolean useHover = this.enabled && this.hovered;

        int u = useHover ? this.uHover : this.uNormal;   // <-- NEW
        int v = useHover ? this.vHover : this.vNormal;

        this.drawModalRectWithCustomSizedTexture(
                this.x, this.y,
                u, v,
                this.width, this.height,
                this.texW, this.texH
        );

        // draw the text on top
        int color = !this.enabled ? this.textDisabled : (useHover ? this.textHover : this.textNormal);

        int tx = this.x + (this.width - mc.fontRenderer.getStringWidth(this.displayString)) / 2;
        int ty = this.y + (this.height - mc.fontRenderer.FONT_HEIGHT) / 2;

        mc.fontRenderer.drawString(this.displayString, tx, ty, color, false);
    }
}
