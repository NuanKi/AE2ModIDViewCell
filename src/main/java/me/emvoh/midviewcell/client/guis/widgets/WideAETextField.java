package me.emvoh.midviewcell.client.guis.widgets;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class WideAETextField extends EditBox {
    private static final Blitter TEXTURE = Blitter.texture("guis/text_field.png", 128, 128);
    private static final int HORIZONTAL_PADDING = 2;
    private static final int TOP_PADDING = 4;
    private static final int BOTTOM_PADDING = 1;
    private static final int TEXTURE_HEIGHT = 12;
    private static final int MIDDLE_TEXTURE_WIDTH = 126;

    private final int fontPad;
    private final ScreenStyle style;
    private Component placeholder;
    private boolean editable = true;

    public WideAETextField(ScreenStyle style, Font font, int x, int y, int width, int height) {
        super(font,
                x + HORIZONTAL_PADDING,
                y + TOP_PADDING,
                width - 2 * HORIZONTAL_PADDING - font.width("_"),
                height - TOP_PADDING - BOTTOM_PADDING,
                Component.empty());
        this.style = style;
        this.fontPad = font.width("_");
        this.setBordered(false);
        this.setTextColor(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
        this.setTextColorUneditable(style.getColor(PaletteColor.TEXTFIELD_TEXT).toARGB());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        Bounds bounds = getVisualBounds();
        return mouseX >= bounds.left && mouseX < bounds.right
                && mouseY >= bounds.top && mouseY < bounds.bottom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            mouseX = Mth.clamp(mouseX, getX(), getX() + getWidth() - 1);
            mouseY = Mth.clamp(mouseY, getY(), getY() + getHeight() - 1);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return isFocused() && canConsumeInput() && keyCode != GLFW.GLFW_KEY_TAB && keyCode != GLFW.GLFW_KEY_ESCAPE;
    }

    @Override
    public void setEditable(boolean enabled) {
        super.setEditable(enabled);
        this.editable = enabled;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        int textureY = 0;
        if (!this.editable) {
            textureY = TEXTURE_HEIGHT;
        } else if (this.isFocused()) {
            textureY = TEXTURE_HEIGHT * 2;
        }

        drawWideBackground(graphics, textureY);
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (this.placeholder != null && !this.isFocused() && this.getValue().isEmpty()) {
            graphics.drawString(
                    Minecraft.getInstance().font,
                    this.placeholder,
                    getX(),
                    getY(),
                    this.style.getColor(PaletteColor.TEXTFIELD_PLACEHOLDER).toARGB(),
                    false);
        }
    }

    public void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    private void drawWideBackground(GuiGraphics graphics, int textureY) {
        Bounds bounds = getVisualBounds();
        int height = bounds.bottom - bounds.top;
        TEXTURE.src(0, textureY, 1, TEXTURE_HEIGHT).dest(bounds.left, bounds.top, 1, height).blit(graphics);

        int x = bounds.left + 1;
        int remaining = Math.max(0, bounds.right - bounds.left - 2);
        while (remaining > 0) {
            int segmentWidth = Math.min(MIDDLE_TEXTURE_WIDTH, remaining);
            TEXTURE.src(1, textureY, segmentWidth, TEXTURE_HEIGHT)
                    .dest(x, bounds.top, segmentWidth, height)
                    .blit(graphics);
            x += segmentWidth;
            remaining -= segmentWidth;
        }

        TEXTURE.src(127, textureY, 1, TEXTURE_HEIGHT).dest(bounds.right - 1, bounds.top, 1, height).blit(graphics);
    }

    private Bounds getVisualBounds() {
        int left = getX() - HORIZONTAL_PADDING;
        int top = getY() - TOP_PADDING;
        int right = left + getWidth() + 2 * HORIZONTAL_PADDING + this.fontPad;
        int bottom = top + getHeight() + TOP_PADDING + BOTTOM_PADDING;
        return new Bounds(left, top, right, bottom);
    }

    private record Bounds(int left, int top, int right, int bottom) {
    }
}
