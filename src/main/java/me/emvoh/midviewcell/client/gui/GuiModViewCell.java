package me.emvoh.midviewcell.client.gui;

import me.emvoh.midviewcell.Tags;
import me.emvoh.midviewcell.client.gui.widgets.GuiTexturedButton;
import me.emvoh.midviewcell.items.ModItemViewCell;
import me.emvoh.midviewcell.packets.MidviewNetwork;
import me.emvoh.midviewcell.packets.ModIDViewCellPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class GuiModViewCell extends GuiScreen {
    private final EnumHand hand;

    private static final ResourceLocation BG = new ResourceLocation(Tags.MODID, "textures/guis/mod_view_cell.png");

    private static final int TEX_W = 512;
    private static final int TEX_H = 512;

    // Atlas regions
    private static final int U_ENTRY = 0;
    private static final int V_ENTRY_IDLE = 275;
    private static final int V_ENTRY_FOCUSED = 289;

    private static final int U_LISTBOX = 0;
    private static final int V_LISTBOX = 305;

    // Pixel sizes
    private static final int ENTRY_W = 232;
    private static final int ENTRY_H = 14;
    private int entryBoxX;
    private int entryBoxY;

    private static final int ENTRY_PAD_X = 4;
    private static final int ENTRY_PAD_Y = 3;


    private static final int MAX_ENTRY_LEN = 256;

    private final ItemStack stack;

    private GuiTextField entryField;

    private final List<String> whitelist = new ArrayList<>();
    private final List<String> blacklist = new ArrayList<>();

    // Multi-select support
    private final LinkedHashSet<Integer> wlSelected = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> blSelected = new LinkedHashSet<>();

    // Primary selection (used for keyboard up/down and scrolling)
    private int wlPrimary = -1;
    private int blPrimary = -1;

    // Anchor for shift-range selection
    private int wlAnchor = -1;
    private int blAnchor = -1;

    private int wlScroll = 0;
    private int blScroll = 0;

    // Layout
    private int guiLeft;
    private int guiTop;
    private int guiWidth = 256;
    private int guiHeight = 272;

    private int boxW;
    private int boxH;
    private int boxGap = 12;


    private int wlX, wlY;
    private int blX, blY;

    // Scrollbar
    private static final int SCROLL_W = 4;          // thumb width
    private static final int SCROLL_PAD_RIGHT = 2;  // padding from inner right edge
    private static final int SCROLL_MIN_THUMB_H = 8;

    private static final int COL_SCROLL_TRACK = 0x40000000; // semi-transparent black
    private static final int COL_SCROLL_THUMB = 0x80FFFFFF; // semi-transparent white
    private static final int COL_SCROLL_THUMB_ACTIVE = 0xB0FFFFFF;


    // Text colors
    private static final int COL_TITLE         = 0x505050;
    private static final int COL_LABEL         = 0x505050;
    private static final int COL_PLACEHOLDER   = 0xCFCFCF;
    private static final int COL_ENTRY_TEXT    = 0xF2F2F2;
    private static final int COL_LIST_TEXT     = 0xF2F2F2;
    private static final int COL_LIST_TEXT_SEL = 0xFFFFFF;
    private static final int COL_LIST_SEL_BG   = 0x7C81C6FF;

    private enum ActiveList { WL, BL }
    private ActiveList activeList = ActiveList.WL;
    private ActiveList lastAddedList = ActiveList.WL;
    private String lastAddedValue = null;

    // Move buttons
    private GuiButton moveWlToBlBtn;
    private GuiButton moveBlToWlBtn;

    private static final int BTN_MOVE_WL_TO_BL = 6;
    private static final int BTN_MOVE_BL_TO_WL = 7;

    // Tab completion
    private final List<String> allModIds = new ArrayList<>();
    private List<String> tabMatches = new ArrayList<>();
    private int tabMatchIndex = 0;

    private String tabSessionPrefix = null;
    private int tabSessionStart = -1;
    private int tabSessionEnd = -1;


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
        this.boxW = (guiWidth - margin * 2 - boxGap) / 2;
        buildModIdIndex();

        wlX = guiLeft + margin;
        wlY = guiTop + 92;

        blX = wlX + boxW + boxGap;
        blY = wlY;

        this.buttonList.clear();

        // Move buttons between the list boxes (stacked)
        int moveBtnW = 10;
        int moveBtnH = 12;

        // center inside the gap (will overlap a tiny bit if boxGap < moveBtnW, which is usually fine)
        int moveX = wlX + boxW + (boxGap - moveBtnW) / 2;

        // vertically centered in the listbox area
        int centerY = wlY + boxH / 2;
        int moveTopY = centerY - moveBtnH - 2;
        int moveBottomY = centerY + 2;

        this.moveWlToBlBtn = new GuiTexturedButton(
                BTN_MOVE_WL_TO_BL, moveX, moveTopY, moveBtnW, moveBtnH, "",
                BG, TEX_W, TEX_H,
                0, 11, 497, 497
        );

        this.moveBlToWlBtn = new GuiTexturedButton(
                BTN_MOVE_BL_TO_WL, moveX, moveBottomY, moveBtnW, moveBtnH, "",
                BG, TEX_W, TEX_H,
                22, 33, 497, 497
        );


        this.buttonList.add(this.moveWlToBlBtn);
        this.buttonList.add(this.moveBlToWlBtn);

        // Add to Whitelist / Blacklist buttons row
        int addBtnY = guiTop + 50;
        this.buttonList.add(new GuiTexturedButton(
                2, wlX, addBtnY, 111, 18, "Add to Whitelist",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));
        this.buttonList.add(new GuiTexturedButton(
                3, blX, addBtnY, 111, 18, "Add to Blacklist",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));

        // Remove / Clear buttons row
        int toolsY = wlY + boxH + 10;

        this.buttonList.add(new GuiTexturedButton(
                4, wlX, toolsY, 111, 18, "Remove Selected",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));
        this.buttonList.add(new GuiTexturedButton(
                5, blX, toolsY, 111, 18, "Clear All",
                BG, TEX_W, TEX_H, 0, 420, 439
        ));

        // Save / Cancel buttons row
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

        if (moveWlToBlBtn != null) moveWlToBlBtn.enabled = !wlSelected.isEmpty();
        if (moveBlToWlBtn != null) moveBlToWlBtn.enabled = !blSelected.isEmpty();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0: // Save
                saveAndClose();
                break;


            case 1: // Cancel
                this.mc.displayGuiScreen(null);
                break;

            case 2: // Add -> WL
                activeList = ActiveList.WL;
                addEntryTextToList(whitelist);
                clampScrolls();
                break;

            case 3: // Add -> BL
                activeList = ActiveList.BL;
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

                wlSelected.clear();
                blSelected.clear();
                wlPrimary = -1;
                blPrimary = -1;
                wlAnchor = -1;
                blAnchor = -1;

                wlScroll = 0;
                blScroll = 0;

                lastAddedValue = null;
                lastAddedList = ActiveList.WL;
                break;

            case BTN_MOVE_WL_TO_BL:
                moveSelectedBetweenLists(ActiveList.WL, ActiveList.BL);
                clampScrolls();
                break;

            case BTN_MOVE_BL_TO_WL:
                moveSelectedBetweenLists(ActiveList.BL, ActiveList.WL);
                clampScrolls();
                break;


            default:
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // CTRL+S saves
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_S) {
            saveAndClose();
            return;
        }

        // TAB behavior:
        // - If entry not focused: focus it
        // - If entry focused: tab-complete current token (Shift+Tab cycles backwards)
        if (keyCode == Keyboard.KEY_TAB) {
            if (this.entryField != null && this.entryField.isFocused()) {
                boolean backwards = isShiftKeyDown();
                doTabComplete(backwards);
            } else if (this.entryField != null) {
                this.entryField.setFocused(true);
                this.entryField.setCursorPositionEnd();
            }
            return;
        }

        boolean entryFocused = (this.entryField != null && this.entryField.isFocused());

        if (!entryFocused) {
            if (keyCode == Keyboard.KEY_DELETE) {
                removeSelectedOrLastAdded();
                clampScrolls();
                return;
            }

            if (keyCode == Keyboard.KEY_UP) {
                moveSelection(-1);
                clampScrolls();
                return;
            }

            if (keyCode == Keyboard.KEY_DOWN) {
                moveSelection(1);
                clampScrolls();
                return;
            }
        }

        // Enter behavior:
        // - If entry focused and has text: add to active list
        // - If entry focused and empty: save
        // - If entry not focused: save
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (entryFocused) {
                String raw = this.entryField.getText();
                if (raw != null && !raw.trim().isEmpty()) {
                    if (activeList == ActiveList.BL) {
                        addEntryTextToList(blacklist);
                    } else {
                        addEntryTextToList(whitelist);
                    }
                    clampScrolls();
                } else {
                    saveAndClose();
                }
                return;
            }

            saveAndClose();
            return;
        }

        if (this.entryField != null && this.entryField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }

        if (this.entryField != null && this.entryField.textboxKeyTyped(typedChar, keyCode)) {
            clearTabSession();
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

        boolean ctrl = isCtrlKeyDown();
        boolean shift = isShiftKeyDown();

        int wlIdx = clickSelectIndex(mouseX, mouseY, wlX, wlY, boxW, boxH, whitelist, wlScroll);
        if (wlIdx != -1) {
            activeList = ActiveList.WL;

            blSelected.clear();
            blPrimary = -1;
            blAnchor = -1;

            applyMultiSelectClick(ActiveList.WL, wlIdx, ctrl, shift);
            return;
        }

        int blIdx = clickSelectIndex(mouseX, mouseY, blX, blY, boxW, boxH, blacklist, blScroll);
        if (blIdx != -1) {
            activeList = ActiveList.BL;

            wlSelected.clear();
            wlPrimary = -1;
            wlAnchor = -1;

            applyMultiSelectClick(ActiveList.BL, blIdx, ctrl, shift);
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
            activeList = ActiveList.WL;
            wlScroll = applyScroll(wlScroll, wheel, whitelist, visible);
        } else if (isInside(mouseX, mouseY, blX, blY, boxW, boxH)) {
            activeList = ActiveList.BL;
            blScroll = applyScroll(blScroll, wheel, blacklist, visible);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(BG);

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

        // Labels
        this.fontRenderer.drawString("Whitelist", wlX, wlY - 12, COL_LABEL);
        this.fontRenderer.drawString("Blacklist", blX, blY - 12, COL_LABEL);

        // Entry field
        if (this.entryField != null) {
            GlStateManager.color(1f, 1f, 1f, 1f);
            this.mc.getTextureManager().bindTexture(BG);

            int vEntry = this.entryField.isFocused() ? V_ENTRY_FOCUSED : V_ENTRY_IDLE;

            drawModalRectWithCustomSizedTexture(
                    entryBoxX, entryBoxY,
                    U_ENTRY, vEntry,
                    ENTRY_W, ENTRY_H,
                    TEX_W, TEX_H
            );


            this.entryField.drawTextBox();
            drawEntryPlaceholder();
        }


        // List boxes
        drawStringListBox(wlX, wlY, boxW, boxH, whitelist, wlSelected, wlPrimary, wlScroll);
        drawStringListBox(blX, blY, boxW, boxH, blacklist, blSelected, blPrimary, blScroll);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /* =========================
       List box helpers
       ========================= */

    private void drawStringListBox(int x, int y, int w, int h, List<String> list, LinkedHashSet<Integer> selectedSet, int primary, int scroll) {
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

        int textMaxWidth = w - 8 - (SCROLL_W + SCROLL_PAD_RIGHT + 2);

        for (int i = 0; i < visible; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;

            int yy = y + 2 + i * lineH;

            boolean selected = selectedSet.contains(idx);
            if (selected) {
                drawRect(x + 2, yy - 1, x + w - 2, yy + lineH - 1, COL_LIST_SEL_BG);
            }

            int textColor = selected ? COL_LIST_TEXT_SEL : COL_LIST_TEXT;
            this.fontRenderer.drawString(trimToWidth(list.get(idx), textMaxWidth), x + 4, yy, textColor);
        }

        boolean active = (activeList == ActiveList.WL && list == whitelist) || (activeList == ActiveList.BL && list == blacklist);
        drawScrollbar(x, y, w, h, list.size(), start, visible, active);
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
        return s == null ? "" : this.fontRenderer.trimStringToWidth(s, widthPx);
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

        String lastNew = null;
        for (String p : parts) {
            String norm = normalizeModId(p);
            if (!norm.isEmpty()) {
                boolean added = merged.add(norm);
                if (added) lastNew = norm;
            }
        }

        target.clear();
        target.addAll(merged);

        if (lastNew != null) {
            lastAddedValue = lastNew;
            lastAddedList = (target == whitelist) ? ActiveList.WL : ActiveList.BL;
        }

        this.entryField.setText("");
    }

    private void removeSelected() {
        if (!wlSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.WL, whitelist, wlSelected);
            wlPrimary = -1;
            wlAnchor = -1;
            return;
        }

        if (!blSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.BL, blacklist, blSelected);
            blPrimary = -1;
            blAnchor = -1;
        }
    }

    private void clampScrolls() {
        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        int visible = Math.max(1, (boxH - 4) / lineH);

        wlScroll = Math.max(0, Math.min(wlScroll, Math.max(0, whitelist.size() - visible)));
        blScroll = Math.max(0, Math.min(blScroll, Math.max(0, blacklist.size() - visible)));

        // drop out-of-range selected indices
        clampSelectionSet(wlSelected, whitelist.size());
        clampSelectionSet(blSelected, blacklist.size());

        if (!wlSelected.contains(wlPrimary)) wlPrimary = -1;
        if (!blSelected.contains(blPrimary)) blPrimary = -1;

        if (wlAnchor >= whitelist.size()) wlAnchor = -1;
        if (blAnchor >= blacklist.size()) blAnchor = -1;
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

    protected void sendSaveToServer(List<String> whitelist, List<String> blacklist) {
        MidviewNetwork.NET.sendToServer(new ModIDViewCellPacket(hand, whitelist, blacklist));
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

    private void saveAndClose() {
        normalizeInPlace(whitelist);
        normalizeInPlace(blacklist);
        sendSaveToServer(new ArrayList<>(whitelist), new ArrayList<>(blacklist));
        this.mc.displayGuiScreen(null);
    }

    private int getVisibleRows() {
        int lineH = this.fontRenderer.FONT_HEIGHT + 2;
        return Math.max(1, (boxH - 4) / lineH);
    }

    private int scrollToMakeVisible(int scroll, int selected, int listSize) {
        int visible = getVisibleRows();
        int maxScroll = Math.max(0, listSize - visible);

        if (selected < scroll) {
            scroll = selected;
        } else if (selected >= scroll + visible) {
            scroll = selected - visible + 1;
        }

        if (scroll < 0) scroll = 0;
        if (scroll > maxScroll) scroll = maxScroll;
        return scroll;
    }

    private void moveSelection(int delta) {
        List<String> list = (activeList == ActiveList.WL) ? whitelist : blacklist;
        if (list.isEmpty()) {
            wlSelected.clear();
            blSelected.clear();
            wlPrimary = -1;
            blPrimary = -1;
            wlAnchor = -1;
            blAnchor = -1;
            return;
        }

        if (activeList == ActiveList.WL) {
            int selected = wlPrimary;

            if (selected < 0) selected = (delta > 0) ? 0 : (list.size() - 1);
            else selected = Math.max(0, Math.min(list.size() - 1, selected + delta));

            wlSelected.clear();
            wlSelected.add(selected);
            wlPrimary = selected;
            wlAnchor = selected;

            blSelected.clear();
            blPrimary = -1;
            blAnchor = -1;

            wlScroll = scrollToMakeVisible(wlScroll, wlPrimary, whitelist.size());
        } else {
            int selected = blPrimary;

            if (selected < 0) selected = (delta > 0) ? 0 : (list.size() - 1);
            else selected = Math.max(0, Math.min(list.size() - 1, selected + delta));

            blSelected.clear();
            blSelected.add(selected);
            blPrimary = selected;
            blAnchor = selected;

            wlSelected.clear();
            wlPrimary = -1;
            wlAnchor = -1;

            blScroll = scrollToMakeVisible(blScroll, blPrimary, blacklist.size());
        }
    }

    private void removeSelectedOrLastAdded() {
        if (!wlSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.WL, whitelist, wlSelected);
            wlPrimary = -1;
            wlAnchor = -1;
            return;
        }

        if (!blSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.BL, blacklist, blSelected);
            blPrimary = -1;
            blAnchor = -1;
            return;
        }

        if (lastAddedValue != null) {
            List<String> list = (lastAddedList == ActiveList.WL) ? whitelist : blacklist;

            if (list.remove(lastAddedValue)) {
                lastAddedValue = list.isEmpty() ? null : list.get(list.size() - 1);
                return;
            } else {
                lastAddedValue = null;
            }
        }

        List<String> list = (activeList == ActiveList.WL) ? whitelist : blacklist;
        if (!list.isEmpty()) {
            ActiveList which = activeList;
            String removed = list.remove(list.size() - 1);
            onRemoved(which, removed);
        }
    }

    private void onRemoved(ActiveList list, String removed) {
        if (removed == null) return;

        if (removed.equals(lastAddedValue) && list == lastAddedList) {
            List<String> l = (list == ActiveList.WL) ? whitelist : blacklist;
            lastAddedValue = l.isEmpty() ? null : l.get(l.size() - 1);
        }
    }

    private void drawScrollbar(int x, int y, int w, int h, int listSize, int scroll, int visibleRows, boolean active) {
        if (listSize <= 0) return;

        int maxScroll = Math.max(0, listSize - visibleRows);
        if (maxScroll <= 0) return; // no scrollbar needed

        int innerY = y + 2;
        int innerH = h - 4;

        float ratio = (float) visibleRows / (float) listSize;
        int thumbH = Math.max(SCROLL_MIN_THUMB_H, (int) (innerH * ratio));

        int travel = innerH - thumbH;
        int thumbY = innerY + (int) (travel * (scroll / (float) maxScroll));

        int barX2 = x + w - 2 - SCROLL_PAD_RIGHT;
        int barX1 = barX2 - SCROLL_W;

        // track
        drawRect(barX1, innerY, barX2, innerY + innerH, COL_SCROLL_TRACK);

        // thumb
        int thumbCol = active ? COL_SCROLL_THUMB_ACTIVE : COL_SCROLL_THUMB;
        drawRect(barX1, thumbY, barX2, thumbY + thumbH, thumbCol);
    }

    private void moveSelectedBetweenLists(ActiveList from, ActiveList to) {
        List<String> src = (from == ActiveList.WL) ? whitelist : blacklist;
        List<String> dst = (to == ActiveList.WL) ? whitelist : blacklist;

        LinkedHashSet<Integer> srcSel = (from == ActiveList.WL) ? wlSelected : blSelected;
        LinkedHashSet<Integer> dstSel = (to == ActiveList.WL) ? wlSelected : blSelected;

        if (srcSel.isEmpty()) return;

        ArrayList<Integer> idxsAsc = new ArrayList<>(srcSel);
        Collections.sort(idxsAsc);

        // Collect normalized values in visible order, de-dup
        LinkedHashSet<String> movedIds = new LinkedHashSet<>();
        for (int idx : idxsAsc) {
            if (idx < 0 || idx >= src.size()) continue;
            String v = normalizeModId(src.get(idx));
            if (!v.isEmpty()) movedIds.add(v);
        }

        // Remove from source, descending
        ArrayList<Integer> idxsDesc = new ArrayList<>(srcSel);
        Collections.sort(idxsDesc, Collections.reverseOrder());
        for (int idx : idxsDesc) {
            if (idx >= 0 && idx < src.size()) {
                src.remove(idx);
            }
        }

        // Clear source selection state
        srcSel.clear();
        if (from == ActiveList.WL) {
            wlPrimary = -1;
            wlAnchor = -1;
        } else {
            blPrimary = -1;
            blAnchor = -1;
        }

        // Add to destination if missing, keep order
        ArrayList<Integer> newDstIdxs = new ArrayList<>();
        String lastMoved = null;

        for (String id : movedIds) {
            int existing = dst.indexOf(id);
            if (existing == -1) {
                dst.add(id);
                existing = dst.size() - 1;
            }
            newDstIdxs.add(existing);
            lastMoved = id;
        }

        // Select all moved entries in destination
        dstSel.clear();
        for (int di : newDstIdxs) dstSel.add(di);

        activeList = to;

        // Set primary selection to the last moved entry for scrolling
        if (!newDstIdxs.isEmpty()) {
            int primary = newDstIdxs.get(newDstIdxs.size() - 1);
            if (to == ActiveList.WL) {
                wlPrimary = primary;
                wlAnchor = primary;
                wlScroll = scrollToMakeVisible(wlScroll, wlPrimary, whitelist.size());
                blPrimary = -1;
                blAnchor = -1;
                blSelected.clear();
            } else {
                blPrimary = primary;
                blAnchor = primary;
                blScroll = scrollToMakeVisible(blScroll, blPrimary, blacklist.size());
                wlPrimary = -1;
                wlAnchor = -1;
                wlSelected.clear();
            }
        }

        if (lastMoved != null) {
            lastAddedValue = lastMoved;
            lastAddedList = to;
        }
    }

    private void buildModIdIndex() {
        this.allModIds.clear();

        for (ModContainer c : Loader.instance().getActiveModList()) {
            String id = c.getModId();
            if (id != null && !id.trim().isEmpty()) {
                this.allModIds.add(id.trim().toLowerCase());
            }
        }

        if (!this.allModIds.contains("minecraft")) this.allModIds.add("minecraft");
        if (!this.allModIds.contains("forge")) this.allModIds.add("forge");

        Collections.sort(this.allModIds);
    }

    private void clearTabSession() {
        tabMatches = new ArrayList<>();
        tabMatchIndex = 0;
        tabSessionPrefix = null;
        tabSessionStart = -1;
        tabSessionEnd = -1;
    }

    private static boolean isSep(char c) {
        return c == ',' || c == ';' || Character.isWhitespace(c);
    }

    private static String commonPrefix(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        String p = items.get(0);
        for (int i = 1; i < items.size(); i++) {
            String s = items.get(i);
            int n = Math.min(p.length(), s.length());
            int j = 0;
            while (j < n && p.charAt(j) == s.charAt(j)) j++;
            p = p.substring(0, j);
            if (p.isEmpty()) break;
        }
        return p;
    }

    private void doTabComplete(boolean backwards) {
        if (entryField == null) return;

        String text = entryField.getText();
        if (text == null) text = "";

        int caret = entryField.getCursorPosition();
        caret = Math.max(0, Math.min(caret, text.length()));

        int start = caret;
        while (start > 0 && !isSep(text.charAt(start - 1))) start--;

        int end = caret;
        while (end < text.length() && !isSep(text.charAt(end))) end++;

        if (start >= end) return;

        String token = text.substring(start, end);
        boolean hadAt = token.startsWith("@");
        String tokenCore = hadAt ? token.substring(1) : token;

        String current = tokenCore.trim().toLowerCase();
        if (current.isEmpty()) return;

        boolean sameSession =
                tabSessionPrefix != null &&
                        tabSessionStart == start &&
                        tabMatches != null &&
                        !tabMatches.isEmpty() &&
                        current.startsWith(tabSessionPrefix);

        if (!sameSession) {
            ArrayList<String> matches = new ArrayList<>();
            for (String id : allModIds) {
                if (id.startsWith(current)) matches.add(id);
            }

            if (matches.isEmpty()) {
                clearTabSession();
                return;
            }

            tabMatches = matches;
            tabMatchIndex = -1;
            tabSessionPrefix = current;
            tabSessionStart = start;
            tabSessionEnd = end;

            String cp = commonPrefix(tabMatches);
            String chosen = (cp.length() > current.length()) ? cp : tabMatches.get(0);

            int exactIdx = tabMatches.indexOf(chosen);
            if (exactIdx >= 0) tabMatchIndex = exactIdx;

            applyTokenReplacement(text, start, end, hadAt, chosen);
            return;
        }

        int curIdx = tabMatches.indexOf(current);
        if (curIdx >= 0) tabMatchIndex = curIdx;

        if (backwards) {
            tabMatchIndex--;
            if (tabMatchIndex < 0) tabMatchIndex = tabMatches.size() - 1;
        } else {
            tabMatchIndex++;
            if (tabMatchIndex >= tabMatches.size()) tabMatchIndex = 0;
        }

        String chosen = tabMatches.get(tabMatchIndex);
        applyTokenReplacement(text, tabSessionStart, tabSessionEnd, hadAt, chosen);
    }

    private void applyTokenReplacement(String fullText, int start, int end, boolean hadAt, String replacementCore) {
        String replacement = hadAt ? ("@" + replacementCore) : replacementCore;

        String newText = fullText.substring(0, start) + replacement + fullText.substring(end);

        entryField.setText(newText);
        int newCaret = start + replacement.length();
        entryField.setCursorPosition(newCaret);
        entryField.setSelectionPos(newCaret);

        tabSessionStart = start;
        tabSessionEnd = start + replacement.length();
    }

    private void applyMultiSelectClick(ActiveList which, int idx, boolean ctrl, boolean shift) {
        LinkedHashSet<Integer> sel = (which == ActiveList.WL) ? wlSelected : blSelected;
        List<String> list = (which == ActiveList.WL) ? whitelist : blacklist;

        if (idx < 0 || idx >= list.size()) return;

        if (shift) {
            int anchor = (which == ActiveList.WL) ? wlAnchor : blAnchor;
            int primary = (which == ActiveList.WL) ? wlPrimary : blPrimary;

            if (anchor < 0) anchor = (primary >= 0) ? primary : idx;

            sel.clear();
            int a = Math.min(anchor, idx);
            int b = Math.max(anchor, idx);
            for (int i = a; i <= b; i++) sel.add(i);

            if (which == ActiveList.WL) {
                wlPrimary = idx;
                wlAnchor = anchor;
                wlScroll = scrollToMakeVisible(wlScroll, wlPrimary, whitelist.size());
            } else {
                blPrimary = idx;
                blAnchor = anchor;
                blScroll = scrollToMakeVisible(blScroll, blPrimary, blacklist.size());
            }
            return;
        }

        if (ctrl) {
            if (sel.contains(idx)) {
                sel.remove(idx);
            } else {
                sel.add(idx);
            }

            if (which == ActiveList.WL) {
                wlPrimary = sel.isEmpty() ? -1 : idx;
                wlAnchor = sel.isEmpty() ? -1 : idx;
                if (wlPrimary != -1) wlScroll = scrollToMakeVisible(wlScroll, wlPrimary, whitelist.size());
            } else {
                blPrimary = sel.isEmpty() ? -1 : idx;
                blAnchor = sel.isEmpty() ? -1 : idx;
                if (blPrimary != -1) blScroll = scrollToMakeVisible(blScroll, blPrimary, blacklist.size());
            }
            return;
        }

        // normal click, single select
        sel.clear();
        sel.add(idx);

        if (which == ActiveList.WL) {
            wlPrimary = idx;
            wlAnchor = idx;
            wlScroll = scrollToMakeVisible(wlScroll, wlPrimary, whitelist.size());
        } else {
            blPrimary = idx;
            blAnchor = idx;
            blScroll = scrollToMakeVisible(blScroll, blPrimary, blacklist.size());
        }
    }

    private void removeIndicesFromList(ActiveList which, List<String> list, LinkedHashSet<Integer> selectedSet) {
        if (selectedSet.isEmpty()) return;

        ArrayList<Integer> idxs = new ArrayList<>(selectedSet);
        Collections.sort(idxs, Collections.reverseOrder()); // remove from end to start

        for (int idx : idxs) {
            if (idx >= 0 && idx < list.size()) {
                String removed = list.remove(idx);
                onRemoved(which, removed);
            }
        }

        selectedSet.clear();
    }

    private void clampSelectionSet(LinkedHashSet<Integer> sel, int size) {
        if (sel.isEmpty()) return;

        LinkedHashSet<Integer> kept = new LinkedHashSet<>();
        for (int i : sel) {
            if (i >= 0 && i < size) kept.add(i);
        }
        sel.clear();
        sel.addAll(kept);
    }
}
