package me.emvoh.midviewcell.client.gui;


import appeng.core.sync.network.NetworkHandler;
import me.emvoh.midviewcell.client.gui.widgets.GuiTexturedButton;
import me.emvoh.midviewcell.items.ModItemViewCell;
import me.emvoh.midviewcell.packets.ModIDViewCellPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class GuiModViewCell extends GuiScreen {
    private final EnumHand hand;

    private static final ResourceLocation BG = new ResourceLocation("modidviewcell", "textures/guis/mod_view_cell.png");

    // Your atlas size (set HEIGHT to your real png height, 512 is common)
    private static final int TEX_W = 512;
    private static final int TEX_H = 512;

    // Atlas regions
    private static final int U_ENTRY = 0;
    private static final int V_ENTRY_IDLE = 275;
    private static final int V_ENTRY_FOCUSED = 289;

    private static final int U_LISTBOX = 0;
    private static final int V_LISTBOX = 305;

    // Pixel sizes (match your GUI)
    private static final int ENTRY_W = 232;
    private static final int ENTRY_H = 14;
    private int entryBoxX;
    private int entryBoxY;

    private static final int ENTRY_PAD_X = 4;
    private static final int ENTRY_PAD_Y = 3; // this matches your placeholder look


    private static final int MAX_ENTRY_LEN = 256;

    private final ItemStack stack;

    private GuiTextField entryField;

    private final List<String> whitelist = new ArrayList<>();
    private final List<String> blacklist = new ArrayList<>();

    private int wlSelected = -1;
    private int blSelected = -1;

    private int wlScroll = 0;
    private int blScroll = 0;

    // Layout
    private int guiLeft;
    private int guiTop;
    private int guiWidth = 256;
    private int guiHeight = 272;

    private int boxW;
    private int boxH;
    private int boxGap = 10;


    private int wlX, wlY;
    private int blX, blY;

    // Text colors
// UI colors (RGB for FontRenderer)
    private static final int COL_TITLE         = 0x505050;
    private static final int COL_LABEL         = 0x505050;
    private static final int COL_PLACEHOLDER   = 0xCFCFCF;
    private static final int COL_ENTRY_TEXT    = 0xF2F2F2;
    private static final int COL_LIST_TEXT     = 0xF2F2F2;
    private static final int COL_LIST_TEXT_SEL = 0xFFFFFF;
    private static final int COL_LIST_SEL_BG   = 0x7C81C6FF;

    public GuiModViewCell(ItemStack stack, EnumHand hand) {
        this.stack = stack.copy();
        this.hand = hand == null ? EnumHand.MAIN_HAND : hand;

        this.whitelist.addAll(ModItemViewCell.getTagWhitelist(this.stack));
        this.blacklist.addAll(ModItemViewCell.getTagBlacklist(this.stack));
    }


    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.guiLeft = (this.width - guiWidth) / 2;
        this.guiTop = (this.height - guiHeight) / 2;

        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visibleRows = 10;
        this.boxH = visibleRows * lineH + 4; // +4 for the 2px top/bottom padding you use


        final int margin = 12;

        entryBoxX = guiLeft + margin;
        entryBoxY = guiTop + 27;

        int entryTextX = entryBoxX + ENTRY_PAD_X;
        int entryTextY = entryBoxY + ENTRY_PAD_Y;
        int entryTextW = ENTRY_W - (ENTRY_PAD_X * 2);

        this.entryField = new GuiTextField(0, this.fontRenderer, entryTextX, entryTextY, entryTextW, ENTRY_H);
        this.entryField.setTextColor(COL_ENTRY_TEXT);
        this.entryField.setDisabledTextColour(COL_ENTRY_TEXT);
        this.entryField.setMaxStringLength(MAX_ENTRY_LEN);
        this.entryField.setFocused(false);
        this.entryField.setEnableBackgroundDrawing(false);
;

        // Boxes (push them down so labels do not collide with Add buttons)
        this.boxW = (guiWidth - margin * 2 - boxGap) / 2;

        wlX = guiLeft + margin;
        wlY = guiTop + 92;

        blX = wlX + boxW + boxGap;
        blY = wlY;

        this.buttonList.clear();

        // Add buttons row
        int addBtnY = guiTop + 50;
        this.buttonList.add(new GuiTexturedButton(
                2, wlX, addBtnY, 111, 18, "Add to Whitelist",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));
        this.buttonList.add(new GuiTexturedButton(
                3, blX, addBtnY, 111, 18, "Add to Blacklist",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));

        // Remove / Clear row (exactly same size as Add buttons: 111x18)
        int toolsY = wlY + boxH + 10;

        this.buttonList.add(new GuiTexturedButton(
                4, wlX, toolsY, 111, 18, "Remove Selected",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));
        this.buttonList.add(new GuiTexturedButton(
                5, blX, toolsY, 111, 18, "Clear All",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));

        // Save / Cancel row (anchored to bottom with margin)
        int bottomBtnH = 18;
        int bottomY = guiTop + guiHeight - margin - bottomBtnH;

        this.buttonList.add(new GuiTexturedButton(
                1, guiLeft + margin, bottomY, 90, bottomBtnH, "Cancel",
                BG, TEX_W, TEX_H, 0, 458, 477
        ));

        int cancelNudgeLeft = 1;
        this.buttonList.add(new GuiTexturedButton(
                0, guiLeft + guiWidth - margin - 90 - cancelNudgeLeft, bottomY, 90, bottomBtnH, "Save",
                BG, TEX_W, TEX_H, 0, 458, 477
        ));
        clampScrolls();

    }


    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.entryField != null) {
            this.entryField.updateCursorCounter();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: // Save
                normalizeInPlace(whitelist);
                normalizeInPlace(blacklist);
                // This is where you will send a packet client -> server later
                sendSaveToServer(new ArrayList<>(whitelist), new ArrayList<>(blacklist));
                this.mc.displayGuiScreen(null);
                break;

            case 1: // Cancel
                this.mc.displayGuiScreen(null);
                break;

            case 2: // Add -> WL
                addEntryTextToList(whitelist);
                clampScrolls();
                break;

            case 3: // Add -> BL
                addEntryTextToList(blacklist);
                clampScrolls();
                break;

            case 4: // Remove selected
                removeSelected();
                clampScrolls();
                break;

            case 5: // Clear all
                whitelist.clear();
                blacklist.clear();
                wlSelected = -1;
                blSelected = -1;
                wlScroll = 0;
                blScroll = 0;
                break;

            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Tab toggles focus on the entry field
        if (keyCode == Keyboard.KEY_TAB) {
            boolean newFocus = !this.entryField.isFocused();
            this.entryField.setFocused(newFocus);
            if (newFocus) {
                this.entryField.setCursorPositionEnd();
            }
            return;
        }

        // Let the text field consume typing when focused
        if (this.entryField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }

        // Convenience: Enter adds to active list
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (blSelected != -1 && wlSelected == -1) {
                addEntryTextToList(blacklist);
            } else {
                addEntryTextToList(whitelist);
            }
            clampScrolls();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.entryField != null) {
            this.entryField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        // Handle selecting entries in list boxes
        int wlIdx = clickSelectIndex(mouseX, mouseY, wlX, wlY, boxW, boxH, whitelist, wlScroll);
        if (wlIdx != -1) {
            wlSelected = wlIdx;
            blSelected = -1;
            return;
        }

        int blIdx = clickSelectIndex(mouseX, mouseY, blX, blY, boxW, boxH, blacklist, blScroll);
        if (blIdx != -1) {
            blSelected = blIdx;
            wlSelected = -1;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visible = Math.max(1, (boxH - 4) / lineH);

        if (isInside(mouseX, mouseY, wlX, wlY, boxW, boxH)) {
            wlScroll = applyScroll(wlScroll, wheel, whitelist, visible);
        } else if (isInside(mouseX, mouseY, blX, blY, boxW, boxH)) {
            blScroll = applyScroll(blScroll, wheel, blacklist, visible);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BG);

        // Draw the full 256x272 background
        drawModalRectWithCustomSizedTexture(
                guiLeft, guiTop,
                0, 0,
                guiWidth, guiHeight,
                TEX_W, TEX_H
        );

        // Title
        String title = "Mod View Cell Filters";
        int x = guiLeft + guiWidth / 2;
        int y = guiTop + 8;
        this.fontRenderer.drawString(
                title,
                x - this.fontRenderer.getStringWidth(title) / 2,
                y,
                COL_TITLE,
                false // no shadow
        );

        // Labels (these bind the font texture)
        this.fontRenderer.drawString("Whitelist", wlX, wlY - 12, COL_LABEL);
        this.fontRenderer.drawString("Blacklist", blX, blY - 12, COL_LABEL);

        // Entry field
        if (this.entryField != null) {
            // Re-bind GUI texture because font rendering changed it
            GlStateManager.color(1f, 1f, 1f, 1f);
            this.mc.getTextureManager().bindTexture(BG);

            int vEntry = this.entryField.isFocused() ? V_ENTRY_FOCUSED : V_ENTRY_IDLE;

            drawModalRectWithCustomSizedTexture(
                    entryBoxX, entryBoxY,
                    U_ENTRY, vEntry,
                    ENTRY_W, ENTRY_H,
                    TEX_W, TEX_H
            );


            // This draws the text + cursor (and binds font again, which is fine)
            this.entryField.drawTextBox();
            drawEntryPlaceholder();
        }


        // List boxes
        drawStringListBox(wlX, wlY, boxW, boxH, whitelist, wlSelected, wlScroll);
        drawStringListBox(blX, blY, boxW, boxH, blacklist, blSelected, blScroll);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /* =========================
       List box helpers
       ========================= */

    private void drawStringListBox(int x, int y, int w, int h, List<String> list, int selected, int scroll) {
        // border
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BG);

        drawModalRectWithCustomSizedTexture(
                x, y,
                U_LISTBOX, V_LISTBOX,
                w, h,
                TEX_W, TEX_H
        );


        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visible = Math.max(1, (h - 4) / lineH);

        int maxScroll = Math.max(0, list.size() - visible);
        int start = Math.max(0, Math.min(scroll, maxScroll));

        for (int i = 0; i < visible; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;

            int yy = y + 2 + i * lineH;

            if (idx == selected) {
                drawRect(x + 2, yy - 1, x + w - 2, yy + lineH - 1, COL_LIST_SEL_BG);
            }

            int textColor = (idx == selected) ? COL_LIST_TEXT_SEL : COL_LIST_TEXT;
            this.fontRenderer.drawString(trimToWidth(list.get(idx), w - 8), x + 4, yy, textColor);

        }
    }

    private int clickSelectIndex(int mouseX, int mouseY, int x, int y, int w, int h, List<String> list, int scroll) {
        if (!isInside(mouseX, mouseY, x, y, w, h)) return -1;

        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visible = Math.max(1, (h - 4) / lineH);

        int maxScroll = Math.max(0, list.size() - visible);
        int start = Math.max(0, Math.min(scroll, maxScroll));

        int relY = mouseY - (y + 2);
        if (relY < 0) return -1;

        int row = relY / lineH;
        int idx = start + row;

        if (idx >= 0 && idx < list.size()) return idx;
        return -1;
    }

    private int applyScroll(int scroll, int wheelDelta, List<String> list, int visibleRows) {
        int dir = wheelDelta > 0 ? -1 : 1; // wheel up means scroll up
        int maxScroll = Math.max(0, list.size() - visibleRows);
        int next = scroll + dir;
        if (next < 0) next = 0;
        if (next > maxScroll) next = maxScroll;
        return next;
    }

    private boolean isInside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String trimToWidth(String s, int widthPx) {
        if (s == null) return "";
        String out = s;
        while (this.fontRenderer.getStringWidth(out) > widthPx && out.length() > 0) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    /* =========================
       Actions
       ========================= */

    private void addEntryTextToList(List<String> target) {
        if (this.entryField == null) return;

        String raw = this.entryField.getText();
        if (raw == null) raw = "";
        raw = raw.trim();
        if (raw.isEmpty()) return;

        // Split on commas, semicolons, and whitespace
        String[] parts = raw.split("[,;\\s]+");

        LinkedHashSet<String> merged = new LinkedHashSet<>(target);
        for (String p : parts) {
            String norm = normalizeModId(p);
            if (!norm.isEmpty()) {
                merged.add(norm);
            }
        }

        target.clear();
        target.addAll(merged);

        this.entryField.setText("");
    }

    private void removeSelected() {
        if (wlSelected != -1) {
            if (wlSelected >= 0 && wlSelected < whitelist.size()) {
                whitelist.remove(wlSelected);
            }
            wlSelected = -1;
            return;
        }

        if (blSelected != -1) {
            if (blSelected >= 0 && blSelected < blacklist.size()) {
                blacklist.remove(blSelected);
            }
            blSelected = -1;
        }
    }

    private void clampScrolls() {
        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visible = Math.max(1, (boxH - 4) / lineH);

        wlScroll = Math.max(0, Math.min(wlScroll, Math.max(0, whitelist.size() - visible)));
        blScroll = Math.max(0, Math.min(blScroll, Math.max(0, blacklist.size() - visible)));

        if (wlSelected >= whitelist.size()) wlSelected = -1;
        if (blSelected >= blacklist.size()) blSelected = -1;
    }

    private void normalizeInPlace(List<String> list) {
        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String s : list) {
            String norm = normalizeModId(s);
            if (!norm.isEmpty()) cleaned.add(norm);
        }
        list.clear();
        list.addAll(cleaned);
    }

    private static String normalizeModId(String s) {
        if (s == null) return "";
        s = s.trim().toLowerCase();

        if (s.startsWith("@")) s = s.substring(1).trim();

        while (!s.isEmpty() && (s.endsWith(",") || s.endsWith(";"))) {
            s = s.substring(0, s.length() - 1).trim();
        }

        return s;
    }


    /**
     * Stub for now so this class compiles.
     * Later you will replace this with your actual packet send.
     *
     * Server-side should validate the player is still holding the same ModItemViewCell
     * and then call ModItemViewCell.setTagFilters(stack, whitelist, blacklist).
     */
    protected void sendSaveToServer(List<String> whitelist, List<String> blacklist) {
        NetworkHandler.instance().sendToServer(new ModIDViewCellPacket(this.hand, whitelist, blacklist));
    }


    public static void open(ItemStack stack, EnumHand hand) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiModViewCell(stack, hand));
    }

    private void drawEntryPlaceholder() {
        if (this.entryField == null) return;

        if (!this.entryField.getText().isEmpty()) return;


        if (this.entryField.isFocused()) return;

        int x = this.entryField.x;
        int y = this.entryField.y;
        this.fontRenderer.drawString("Enter ModIDs (comma or space)", x, y, COL_PLACEHOLDER);
    }

}
