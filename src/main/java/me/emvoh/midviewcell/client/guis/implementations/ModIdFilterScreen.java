package me.emvoh.midviewcell.client.guis.implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.BackgroundGenerator;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.widgets.AE2Button;
import appeng.client.gui.widgets.OpenGuideButton;
import appeng.menu.AEBaseMenu;
import guideme.PageAnchor;
import me.emvoh.midviewcell.Main;
import me.emvoh.midviewcell.ModIdFilterTarget;
import me.emvoh.midviewcell.client.guis.widgets.WideAETextField;
import me.emvoh.midviewcell.items.ModIDViewCellItem;
import me.emvoh.midviewcell.network.UpdateModIdFiltersPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static me.emvoh.midviewcell.network.UpdateModIdFiltersPayload.getString;

@OnlyIn(Dist.CLIENT)
public class ModIdFilterScreen extends AEBaseScreen<ModIdFilterScreen.ModIdFilterMenu> {
    private static final String AE2_TEXT_FIELD_STYLE = "/screens/common/palette.json";
    private static final PageAnchor GUIDE_PAGE = new PageAnchor(Main.id("mod_id_view_cell.md"), null);
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 272;
    private static final int MARGIN = 12;
    private static final int ENTRY_HEIGHT = 14;
    private static final int ACTION_BUTTON_WIDTH = 111;
    private static final int MAX_ENTRY_LENGTH = 256;
    private static final int LIST_GAP = 12;
    private static final int VISIBLE_ROWS = 10;
    private static final int SCROLL_WIDTH = 4;
    private static final int SCROLL_MIN_THUMB_HEIGHT = 8;

    private static final int COLOR_TITLE = 0xFF505050;
    private static final int COLOR_LABEL = 0xFF505050;
    private static final int COLOR_ENTRY_FOCUSED = 0xFF8AB4FF;
    private static final int COLOR_LIST_BACKGROUND = 0xFF9A9FB4;
    private static final int COLOR_LIST_BORDER = 0xFF696D88;
    private static final int COLOR_LIST_HIGHLIGHT = 0xFFF2F2F2;
    private static final int COLOR_LIST_TEXT = 0xFF413F54;
    private static final int COLOR_LIST_EMPTY = 0xFF878FA5;
    private static final int COLOR_LIST_SELECTED = 0x88ACE9FF;
    private static final int COLOR_SCROLL_TRACK = 0x55696D88;
    private static final int COLOR_SCROLL_THUMB = 0xCC696D88;
    private static final int COLOR_SCROLL_THUMB_ACTIVE = 0xFF413F54;

    private final ItemStack stack;
    private final ModIdFilterTarget target;
    private final List<String> whitelist = new ArrayList<>();
    private final List<String> blacklist = new ArrayList<>();
    private final LinkedHashSet<Integer> whitelistSelected = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> blacklistSelected = new LinkedHashSet<>();
    private final List<String> allModIds = new ArrayList<>();

    private int guiLeft;
    private int guiTop;
    private int entryBoxX;
    private int entryBoxY;
    private int entryBoxWidth;
    private int listWidth;
    private int listHeight;
    private int whitelistX;
    private int whitelistY;
    private int blacklistX;
    private int blacklistY;
    private int whitelistScroll;
    private int blacklistScroll;
    private int whitelistPrimary = -1;
    private int blacklistPrimary = -1;
    private int whitelistAnchor = -1;
    private int blacklistAnchor = -1;

    private WideAETextField entryField;
    private OpenGuideButton guideButton;
    private Button moveWhitelistToBlacklistButton;
    private Button moveBlacklistToWhitelistButton;
    private Button removeSelectedButton;

    private ActiveList activeList = ActiveList.WHITELIST;
    private ActiveList lastAddedList = ActiveList.WHITELIST;
    private String lastAddedValue;
    private List<String> tabMatches = List.of();
    private int tabMatchIndex;
    private String tabSessionPrefix;
    private int tabSessionStart = -1;
    private int tabSessionEnd = -1;

    public ModIdFilterScreen(ItemStack stack, ModIdFilterTarget target) {
        this(stack, target, Objects.requireNonNull(Minecraft.getInstance().player).getInventory());
    }

    private ModIdFilterScreen(ItemStack stack, ModIdFilterTarget target, Inventory playerInventory) {
        super(
                new ModIdFilterMenu(playerInventory),
                playerInventory,
                Component.translatable("screen.midviewcell.mod_id_filters.title"),
                StyleManager.loadStyleDoc(AE2_TEXT_FIELD_STYLE));
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.stack = stack.copy();
        this.target = target;
        this.whitelist.addAll(ModIDViewCellItem.getModIdWhitelist(this.stack));
        this.blacklist.addAll(ModIDViewCellItem.getModIdBlacklist(this.stack));
    }

    @Override
    protected void init() {
        super.init();

        this.guiLeft = this.leftPos;
        this.guiTop = this.topPos;

        int lineHeight = this.lineHeight();
        this.listHeight = VISIBLE_ROWS * lineHeight + 4;
        this.listWidth = (GUI_WIDTH - MARGIN * 2 - LIST_GAP) / 2;

        this.entryBoxX = this.guiLeft + MARGIN;
        this.entryBoxY = this.guiTop + 27;
        this.whitelistX = this.guiLeft + MARGIN;
        this.whitelistY = this.guiTop + 92;
        this.blacklistX = this.whitelistX + this.listWidth + LIST_GAP;
        this.blacklistY = this.whitelistY;
        this.entryBoxWidth = this.blacklistX + ACTION_BUTTON_WIDTH - this.entryBoxX;

        buildModIdIndex();

        this.entryField = new WideAETextField(
                StyleManager.loadStyleDoc(AE2_TEXT_FIELD_STYLE),
                this.font,
                this.entryBoxX,
                this.entryBoxY,
                this.entryBoxWidth,
                ENTRY_HEIGHT);
        this.entryField.setMaxLength(MAX_ENTRY_LENGTH);
        this.entryField.setTextShadow(false);
        this.entryField.setPlaceholder(Component.translatable("screen.midviewcell.mod_id_filters.placeholder"));
        this.addRenderableWidget(this.entryField);

        this.guideButton = this.addRenderableWidget(new OpenGuideButton(button -> openHelp()));
        this.guideButton.setX(this.guiLeft + GUI_WIDTH - MARGIN - 16);
        this.guideButton.setY(this.guiTop + 6);

        int addButtonY = this.guiTop + 50;
        addClassicButton(this.whitelistX, addButtonY, ACTION_BUTTON_WIDTH, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.add_whitelist"),
                button -> {
                    this.activeList = ActiveList.WHITELIST;
                    addEntryTextToList(this.whitelist);
                });
        addClassicButton(this.blacklistX, addButtonY, ACTION_BUTTON_WIDTH, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.add_blacklist"),
                button -> {
                    this.activeList = ActiveList.BLACKLIST;
                    addEntryTextToList(this.blacklist);
                });

        int moveButtonWidth = 12;
        int moveButtonHeight = 14;
        int moveX = this.whitelistX + this.listWidth + (LIST_GAP - moveButtonWidth) / 2;
        int centerY = this.whitelistY + this.listHeight / 2;
        this.moveWhitelistToBlacklistButton = addClassicButton(moveX, centerY - moveButtonHeight - 2,
                moveButtonWidth, moveButtonHeight, Component.literal(">"),
                button -> moveSelectedBetweenLists(ActiveList.WHITELIST, ActiveList.BLACKLIST));
        this.moveBlacklistToWhitelistButton = addClassicButton(moveX, centerY + 2,
                moveButtonWidth, moveButtonHeight, Component.literal("<"),
                button -> moveSelectedBetweenLists(ActiveList.BLACKLIST, ActiveList.WHITELIST));

        int toolsY = this.whitelistY + this.listHeight + 10;
        this.removeSelectedButton = addClassicButton(this.whitelistX, toolsY, ACTION_BUTTON_WIDTH, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.remove_selected"),
                button -> removeSelected());
        addClassicButton(this.blacklistX, toolsY, ACTION_BUTTON_WIDTH, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.clear_all"),
                button -> clearAll());

        int bottomY = this.guiTop + GUI_HEIGHT - MARGIN - 18;
        addClassicButton(this.guiLeft + MARGIN, bottomY, 90, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.cancel"),
                button -> this.onClose());
        addClassicButton(this.guiLeft + GUI_WIDTH - MARGIN - 90, bottomY, 90, 18,
                Component.translatable("screen.midviewcell.mod_id_filters.save"),
                button -> saveAndClose());

        clampScrolls();
        refreshButtonStates();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        refreshButtonStates();
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderMenuBackground(graphics);
        this.drawBG(graphics, this.leftPos, this.topPos, mouseX, mouseY, partialTick);
    }

    @Override
    public void drawBG(GuiGraphics graphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        drawScreenFrame(graphics);
        drawStringListBox(
                graphics,
                this.whitelistX,
                this.whitelistY,
                this.whitelist,
                this.whitelistSelected,
                this.whitelistScroll,
                this.activeList == ActiveList.WHITELIST && !this.whitelistSelected.isEmpty());
        drawStringListBox(
                graphics,
                this.blacklistX,
                this.blacklistY,
                this.blacklist,
                this.blacklistSelected,
                this.blacklistScroll,
                this.activeList == ActiveList.BLACKLIST && !this.blacklistSelected.isEmpty());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && clickList(mouseX, mouseY)) {
            this.entryField.setFocused(false);
            this.setFocused(null);
            return true;
        }

        if (isMouseOverChild(mouseX, mouseY)) {
            boolean handled = clickChild(mouseX, mouseY, button);
            if (handled && this.entryField != null && this.entryField.isFocused()) {
                clearSelectionsForTextInput();
            }
            return handled;
        }

        if (isInside(mouseX, mouseY, this.guiLeft, this.guiTop, GUI_WIDTH, GUI_HEIGHT)) {
            if (this.entryField != null) {
                this.entryField.setFocused(false);
            }
            this.setFocused(null);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isDragging()) {
            this.setDragging(false);
            var focused = this.getFocused();
            return focused != null && focused.mouseReleased(mouseX, mouseY, button);
        }

        for (var child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY) && child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        var focused = this.getFocused();
        return focused != null
                && this.isDragging()
                && button == 0
                && focused.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visibleRows = getVisibleRows();
        if (isInside(mouseX, mouseY, this.whitelistX, this.whitelistY, this.listWidth, this.listHeight)) {
            this.activeList = ActiveList.WHITELIST;
            this.whitelistScroll = applyScroll(this.whitelistScroll, scrollY, this.whitelist, visibleRows);
            return true;
        }
        if (isInside(mouseX, mouseY, this.blacklistX, this.blacklistY, this.listWidth, this.listHeight)) {
            this.activeList = ActiveList.BLACKLIST;
            this.blacklistScroll = applyScroll(this.blacklistScroll, scrollY, this.blacklist, visibleRows);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean entryFocused = this.entryField != null && this.entryField.isFocused();

        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_S) {
            saveAndClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            if (entryFocused) {
                if (!doTabComplete(Screen.hasShiftDown())) {
                    blurEntryField();
                }
            } else if (this.entryField != null) {
                clearSelectionsForTextInput();
                this.entryField.setFocused(true);
                this.entryField.moveCursorToEnd(false);
                this.setFocused(this.entryField);
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (entryFocused && !this.entryField.getValue().trim().isEmpty()) {
                addEntryTextToList(this.activeList == ActiveList.BLACKLIST ? this.blacklist : this.whitelist);
                return true;
            }
            saveAndClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!entryFocused) {
                removeSelectedOrLastAdded();
                return true;
            }
        }

        if (!entryFocused) {
            if (hasListKeyboardFocus() && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
                switchActiveListWithArrow(keyCode == GLFW.GLFW_KEY_RIGHT
                        ? ActiveList.BLACKLIST
                        : ActiveList.WHITELIST);
                return true;
            }
            if (hasListKeyboardFocus() && keyCode == GLFW.GLFW_KEY_UP) {
                moveSelection(-1, Screen.hasControlDown(), Screen.hasShiftDown());
                return true;
            }
            if (hasListKeyboardFocus() && keyCode == GLFW.GLFW_KEY_DOWN) {
                moveSelection(1, Screen.hasControlDown(), Screen.hasShiftDown());
                return true;
            }
        }

        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled) {
            clearTabSession();
        }
        return handled;
    }

    private void blurEntryField() {
        if (this.entryField != null) {
            this.entryField.setFocused(false);
        }
        this.setFocused(null);
        clearTabSession();
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean handled = super.charTyped(codePoint, modifiers);
        if (handled) {
            if (this.entryField != null && this.entryField.isFocused()) {
                clearSelectionsForTextInput();
            }
            clearTabSession();
        }
        return handled;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private AE2Button addClassicButton(int x, int y, int width, int height, Component message, Button.OnPress onPress) {
        return this.addRenderableWidget(new AE2Button(x, y, width, height, message, onPress));
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    @Override
    protected PageAnchor getHelpTopic() {
        return GUIDE_PAGE;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }

    private void drawScreenFrame(GuiGraphics graphics) {
        BackgroundGenerator.draw(GUI_WIDTH, GUI_HEIGHT, graphics, this.guiLeft, this.guiTop);

        drawCenteredStringNoShadow(graphics, this.title, this.guiLeft + GUI_WIDTH / 2, this.guiTop + 8, COLOR_TITLE);
        graphics.drawString(this.font, Component.translatable("screen.midviewcell.mod_id_filters.whitelist"),
                this.whitelistX, this.whitelistY - 12, COLOR_LABEL, false);
        graphics.drawString(this.font, Component.translatable("screen.midviewcell.mod_id_filters.blacklist"),
                this.blacklistX, this.blacklistY - 12, COLOR_LABEL, false);
    }

    private void drawCenteredStringNoShadow(GuiGraphics graphics, Component text, int x, int y, int color) {
        FormattedCharSequence formatted = text.getVisualOrderText();
        graphics.drawString(this.font, formatted, x - this.font.width(formatted) / 2, y, color, false);
    }

    private void drawStringListBox(
            GuiGraphics graphics,
            int x,
            int y,
            List<String> list,
            LinkedHashSet<Integer> selectedSet,
            int scroll,
            boolean active) {
        int lineHeight = lineHeight();
        int visibleRows = getVisibleRows();
        drawListPanel(graphics, x, y, active);

        int start = Mth.clamp(scroll, 0, Math.max(0, list.size() - visibleRows));
        int textMaxWidth = this.listWidth - 8 - SCROLL_WIDTH - 4;

        if (list.isEmpty()) {
            graphics.drawString(
                    this.font,
                    Component.translatable("tooltip.appliedenergistics2.mod_view_cell.none")
                            .withStyle(ChatFormatting.ITALIC),
                    x + 4,
                    y + 4,
                    COLOR_LIST_EMPTY,
                    false);
            return;
        }

        for (int row = 0; row < visibleRows; row++) {
            int index = start + row;
            if (index >= list.size()) {
                break;
            }

            int rowY = y + 3 + row * lineHeight;
            if (selectedSet.contains(index)) {
                graphics.fill(x + 2, rowY - 1, x + this.listWidth - 2, rowY + lineHeight - 1, COLOR_LIST_SELECTED);
            }

            String text = this.font.plainSubstrByWidth(list.get(index), textMaxWidth);
            graphics.drawString(this.font, text, x + 4, rowY, COLOR_LIST_TEXT, false);
        }

        drawScrollbar(graphics, x, y, list.size(), start, visibleRows, active);
    }

    private void drawListPanel(GuiGraphics graphics, int x, int y, boolean active) {
        graphics.fill(x, y, x + this.listWidth, y + this.listHeight, COLOR_LIST_BACKGROUND);
        graphics.hLine(x, x + this.listWidth - 1, y, COLOR_LIST_HIGHLIGHT);
        graphics.vLine(x, y, y + this.listHeight - 1, COLOR_LIST_HIGHLIGHT);
        graphics.hLine(x + 1, x + this.listWidth - 1, y + 1, COLOR_LIST_BORDER);
        graphics.vLine(x + 1, y + 1, y + this.listHeight - 1, COLOR_LIST_BORDER);
        graphics.hLine(x + 1, x + this.listWidth - 1, y + this.listHeight - 1, COLOR_LIST_BORDER);
        graphics.vLine(x + this.listWidth - 1, y + 1, y + this.listHeight - 1, COLOR_LIST_BORDER);

        if (active) {
            graphics.renderOutline(x, y, this.listWidth, this.listHeight, COLOR_ENTRY_FOCUSED);
        }
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int listSize, int scroll, int visibleRows, boolean active) {
        int maxScroll = Math.max(0, listSize - visibleRows);
        if (maxScroll <= 0) {
            return;
        }

        int innerY = y + 2;
        int innerHeight = this.listHeight - 4;
        int thumbHeight = Math.max(SCROLL_MIN_THUMB_HEIGHT, innerHeight * visibleRows / listSize);
        int travel = innerHeight - thumbHeight;
        int thumbY = innerY + Math.round(travel * (scroll / (float) maxScroll));
        int barX = x + this.listWidth - 7;

        graphics.fill(barX, innerY, barX + SCROLL_WIDTH, innerY + innerHeight, COLOR_SCROLL_TRACK);
        graphics.fill(barX, thumbY, barX + SCROLL_WIDTH, thumbY + thumbHeight,
                active ? COLOR_SCROLL_THUMB_ACTIVE : COLOR_SCROLL_THUMB);
    }

    private boolean clickList(double mouseX, double mouseY) {
        int whitelistIndex = clickSelectIndex(mouseX, mouseY, this.whitelistX, this.whitelistY, this.whitelist, this.whitelistScroll);
        if (whitelistIndex != -1) {
            this.activeList = ActiveList.WHITELIST;
            this.blacklistSelected.clear();
            this.blacklistPrimary = -1;
            this.blacklistAnchor = -1;
            applyMultiSelectClick(ActiveList.WHITELIST, whitelistIndex, Screen.hasControlDown(), Screen.hasShiftDown());
            return true;
        }

        int blacklistIndex = clickSelectIndex(mouseX, mouseY, this.blacklistX, this.blacklistY, this.blacklist, this.blacklistScroll);
        if (blacklistIndex != -1) {
            this.activeList = ActiveList.BLACKLIST;
            this.whitelistSelected.clear();
            this.whitelistPrimary = -1;
            this.whitelistAnchor = -1;
            applyMultiSelectClick(ActiveList.BLACKLIST, blacklistIndex, Screen.hasControlDown(), Screen.hasShiftDown());
            return true;
        }

        return false;
    }

    private int clickSelectIndex(double mouseX, double mouseY, int x, int y, List<String> list, int scroll) {
        if (!isInside(mouseX, mouseY, x, y, this.listWidth, this.listHeight) || list.isEmpty()) {
            return -1;
        }

        int row = ((int) mouseY - (y + 3)) / lineHeight();
        int index = Mth.clamp(scroll, 0, Math.max(0, list.size() - getVisibleRows())) + row;
        return index >= 0 && index < list.size() ? index : -1;
    }

    private int applyScroll(int scroll, double scrollY, List<String> list, int visibleRows) {
        int direction = scrollY > 0 ? -1 : 1;
        int maxScroll = Math.max(0, list.size() - visibleRows);
        return Mth.clamp(scroll + direction, 0, maxScroll);
    }

    private void addEntryTextToList(List<String> target) {
        if (this.entryField == null) {
            return;
        }

        String raw = this.entryField.getValue();
        if (raw == null || raw.trim().isEmpty()) {
            return;
        }

        LinkedHashSet<String> merged = new LinkedHashSet<>(target);
        String lastNew = null;
        for (String part : raw.split("[,;\\s]+")) {
            String normalized = normalizeModId(part);
            if (!normalized.isEmpty() && merged.add(normalized)) {
                lastNew = normalized;
            }
        }

        target.clear();
        target.addAll(merged);
        if (lastNew != null) {
            this.lastAddedValue = lastNew;
            this.lastAddedList = target == this.blacklist ? ActiveList.BLACKLIST : ActiveList.WHITELIST;
        }

        this.entryField.setValue("");
        clearTabSession();
        clampScrolls();
    }

    private void removeSelected() {
        if (!this.whitelistSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.WHITELIST, this.whitelist, this.whitelistSelected);
        } else if (!this.blacklistSelected.isEmpty()) {
            removeIndicesFromList(ActiveList.BLACKLIST, this.blacklist, this.blacklistSelected);
        }
        clampScrolls();
    }

    private void removeSelectedOrLastAdded() {
        if (!this.whitelistSelected.isEmpty() || !this.blacklistSelected.isEmpty()) {
            removeSelected();
            return;
        }

        if (this.lastAddedValue != null) {
            List<String> list = this.lastAddedList == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
            if (list.remove(this.lastAddedValue)) {
                this.lastAddedValue = list.isEmpty() ? null : list.get(list.size() - 1);
                clampScrolls();
                return;
            }
            this.lastAddedValue = null;
        }

        List<String> list = this.activeList == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
        if (!list.isEmpty()) {
            String removed = list.remove(list.size() - 1);
            onRemoved(this.activeList, removed);
            clampScrolls();
        }
    }

    private void clearAll() {
        this.whitelist.clear();
        this.blacklist.clear();
        this.whitelistSelected.clear();
        this.blacklistSelected.clear();
        this.whitelistPrimary = -1;
        this.blacklistPrimary = -1;
        this.whitelistAnchor = -1;
        this.blacklistAnchor = -1;
        this.whitelistScroll = 0;
        this.blacklistScroll = 0;
        this.lastAddedValue = null;
    }

    private void moveSelectedBetweenLists(ActiveList from, ActiveList to) {
        List<String> source = from == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
        List<String> destination = to == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
        LinkedHashSet<Integer> sourceSelection = from == ActiveList.WHITELIST ? this.whitelistSelected : this.blacklistSelected;
        LinkedHashSet<Integer> destinationSelection = to == ActiveList.WHITELIST ? this.whitelistSelected : this.blacklistSelected;

        if (sourceSelection.isEmpty()) {
            return;
        }

        ArrayList<Integer> ascending = new ArrayList<>(sourceSelection);
        Collections.sort(ascending);
        LinkedHashSet<String> moved = new LinkedHashSet<>();
        for (int index : ascending) {
            if (index >= 0 && index < source.size()) {
                String normalized = normalizeModId(source.get(index));
                if (!normalized.isEmpty()) {
                    moved.add(normalized);
                }
            }
        }

        ArrayList<Integer> descending = new ArrayList<>(sourceSelection);
        descending.sort(Collections.reverseOrder());
        for (int index : descending) {
            if (index >= 0 && index < source.size()) {
                source.remove(index);
            }
        }

        sourceSelection.clear();
        destinationSelection.clear();
        ArrayList<Integer> newSelection = new ArrayList<>();
        String lastMoved = null;
        for (String value : moved) {
            int destinationIndex = destination.indexOf(value);
            if (destinationIndex == -1) {
                destination.add(value);
                destinationIndex = destination.size() - 1;
            }
            destinationSelection.add(destinationIndex);
            newSelection.add(destinationIndex);
            lastMoved = value;
        }

        this.activeList = to;
        setPrimarySelection(to, newSelection.isEmpty() ? -1 : newSelection.get(newSelection.size() - 1));
        setPrimarySelection(from, -1);
        if (lastMoved != null) {
            this.lastAddedValue = lastMoved;
            this.lastAddedList = to;
        }
        clampScrolls();
    }

    private void moveSelection(int delta, boolean control, boolean shift) {
        List<String> list = this.activeList == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
        if (list.isEmpty()) {
            clearSelection(this.activeList);
            return;
        }

        int current = primaryFor(this.activeList);
        int next = current < 0
                ? (delta > 0 ? 0 : list.size() - 1)
                : Mth.clamp(current + delta, 0, list.size() - 1);

        clearSelection(otherList(this.activeList));
        LinkedHashSet<Integer> selected = selectionFor(this.activeList);
        if (shift) {
            int anchor = anchorFor(this.activeList);
            if (anchor < 0) {
                anchor = current >= 0 ? current : next;
            }
            selected.clear();
            int start = Math.min(anchor, next);
            int end = Math.max(anchor, next);
            for (int i = start; i <= end; i++) {
                selected.add(i);
            }
            setAnchorSelection(this.activeList, anchor);
        } else if (control) {
            selected.add(next);
            setAnchorSelection(this.activeList, next);
        } else {
            selected.clear();
            selected.add(next);
            setAnchorSelection(this.activeList, next);
        }
        setPrimarySelection(this.activeList, next);
        ensureSelectionVisible(this.activeList);
        this.setFocused(null);
    }

    private void switchActiveListWithArrow(ActiveList targetList) {
        if (targetList == this.activeList) {
            this.setFocused(null);
            return;
        }

        List<String> target = targetList == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
        if (target.isEmpty()) {
            this.setFocused(null);
            return;
        }

        int sourcePrimary = primaryFor(this.activeList);
        int targetIndex = Mth.clamp(sourcePrimary < 0 ? 0 : sourcePrimary, 0, target.size() - 1);
        clearSelection(this.activeList);

        LinkedHashSet<Integer> selected = selectionFor(targetList);
        selected.clear();
        selected.add(targetIndex);
        this.activeList = targetList;
        setPrimarySelection(targetList, targetIndex);
        setAnchorSelection(targetList, targetIndex);
        ensureSelectionVisible(targetList);
        this.setFocused(null);
    }

    private boolean hasActiveSelection() {
        return !selectionFor(this.activeList).isEmpty();
    }

    private boolean hasListKeyboardFocus() {
        return hasActiveSelection() && this.getFocused() == null;
    }

    private void clearSelectionsForTextInput() {
        clearSelection(ActiveList.WHITELIST);
        clearSelection(ActiveList.BLACKLIST);
    }

    private void applyMultiSelectClick(ActiveList list, int index, boolean control, boolean shift) {
        LinkedHashSet<Integer> selected = selectionFor(list);
        if (shift) {
            int anchor = anchorFor(list);
            if (anchor < 0) {
                anchor = primaryFor(list) >= 0 ? primaryFor(list) : index;
            }
            selected.clear();
            int start = Math.min(anchor, index);
            int end = Math.max(anchor, index);
            for (int i = start; i <= end; i++) {
                selected.add(i);
            }
            setAnchorSelection(list, anchor);
            setPrimarySelection(list, index);
            ensureSelectionVisible(list);
            return;
        }

        if (control) {
            if (selected.contains(index)) {
                selected.remove(index);
            } else {
                selected.add(index);
            }
            setPrimarySelection(list, selected.isEmpty() ? -1 : index);
            setAnchorSelection(list, selected.isEmpty() ? -1 : index);
            ensureSelectionVisible(list);
            return;
        }

        selected.clear();
        selected.add(index);
        setPrimarySelection(list, index);
        setAnchorSelection(list, index);
        ensureSelectionVisible(list);
    }

    private void removeIndicesFromList(ActiveList activeList, List<String> list, LinkedHashSet<Integer> selectedSet) {
        ArrayList<Integer> indices = new ArrayList<>(selectedSet);
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            if (index >= 0 && index < list.size()) {
                onRemoved(activeList, list.remove(index));
            }
        }
        selectedSet.clear();
        setPrimarySelection(activeList, -1);
        setAnchorSelection(activeList, -1);
    }

    private void clampScrolls() {
        int visibleRows = getVisibleRows();
        this.whitelistScroll = Mth.clamp(this.whitelistScroll, 0, Math.max(0, this.whitelist.size() - visibleRows));
        this.blacklistScroll = Mth.clamp(this.blacklistScroll, 0, Math.max(0, this.blacklist.size() - visibleRows));
        clampSelectionSet(this.whitelistSelected, this.whitelist.size());
        clampSelectionSet(this.blacklistSelected, this.blacklist.size());
    }

    private void clampSelectionSet(LinkedHashSet<Integer> selection, int size) {
        selection.removeIf(index -> index < 0 || index >= size);
    }

    private void refreshButtonStates() {
        boolean hasWhitelistSelection = !this.whitelistSelected.isEmpty();
        boolean hasBlacklistSelection = !this.blacklistSelected.isEmpty();
        if (this.moveWhitelistToBlacklistButton != null) {
            this.moveWhitelistToBlacklistButton.active = hasWhitelistSelection;
        }
        if (this.moveBlacklistToWhitelistButton != null) {
            this.moveBlacklistToWhitelistButton.active = hasBlacklistSelection;
        }
        if (this.removeSelectedButton != null) {
            this.removeSelectedButton.active = hasWhitelistSelection || hasBlacklistSelection;
        }
    }

    private void ensureSelectionVisible(ActiveList list) {
        int primary = primaryFor(list);
        if (primary < 0) {
            return;
        }

        int visibleRows = getVisibleRows();
        if (list == ActiveList.WHITELIST) {
            this.whitelistScroll = scrollToMakeVisible(this.whitelistScroll, primary, this.whitelist.size(), visibleRows);
        } else {
            this.blacklistScroll = scrollToMakeVisible(this.blacklistScroll, primary, this.blacklist.size(), visibleRows);
        }
    }

    private int scrollToMakeVisible(int scroll, int selected, int listSize, int visibleRows) {
        if (selected < scroll) {
            scroll = selected;
        } else if (selected >= scroll + visibleRows) {
            scroll = selected - visibleRows + 1;
        }
        return Mth.clamp(scroll, 0, Math.max(0, listSize - visibleRows));
    }

    private void saveAndClose() {
        addPendingEntryTextToActiveList();
        normalizeInPlace(this.whitelist);
        normalizeInPlace(this.blacklist);
        PacketDistributor.sendToServer(new UpdateModIdFiltersPayload(
                this.target,
                List.copyOf(this.whitelist),
                List.copyOf(this.blacklist)));
        this.onClose();
    }

    private void addPendingEntryTextToActiveList() {
        if (this.entryField == null || this.entryField.getValue().trim().isEmpty()) {
            return;
        }

        addEntryTextToList(this.activeList == ActiveList.BLACKLIST ? this.blacklist : this.whitelist);
    }

    private void normalizeInPlace(List<String> list) {
        LinkedHashSet<String> cleaned = new LinkedHashSet<>();
        for (String value : list) {
            String normalized = normalizeModId(value);
            if (!normalized.isEmpty()) {
                cleaned.add(normalized);
            }
        }
        list.clear();
        list.addAll(cleaned);
    }

    private static String normalizeModId(String value) {
        return getString(value);
    }

    private void buildModIdIndex() {
        this.allModIds.clear();
        for (var modInfo : ModList.get().getMods()) {
            String modId = modInfo.getModId();
            if (modId != null && !modId.isBlank()) {
                this.allModIds.add(modId.trim().toLowerCase(Locale.ROOT));
            }
        }
        if (!this.allModIds.contains("minecraft")) {
            this.allModIds.add("minecraft");
        }
        if (!this.allModIds.contains("neoforge")) {
            this.allModIds.add("neoforge");
        }
        Collections.sort(this.allModIds);
    }

    private boolean doTabComplete(boolean backwards) {
        String text = this.entryField.getValue();
        int cursor = this.entryField.getCursorPosition();
        int start = cursor;
        while (start > 0 && !isSeparator(text.charAt(start - 1))) {
            start--;
        }
        int end = cursor;
        while (end < text.length() && !isSeparator(text.charAt(end))) {
            end++;
        }
        if (start >= end) {
            return false;
        }

        String token = text.substring(start, end);
        boolean hadAt = token.startsWith("@");
        String prefix = (hadAt ? token.substring(1) : token).trim().toLowerCase(Locale.ROOT);
        if (prefix.isEmpty()) {
            return false;
        }

        boolean sameSession = this.tabSessionPrefix != null
                && this.tabSessionStart == start
                && !this.tabMatches.isEmpty()
                && prefix.startsWith(this.tabSessionPrefix);

        if (!sameSession) {
            ArrayList<String> matches = new ArrayList<>();
            for (String modId : this.allModIds) {
                if (modId.startsWith(prefix)) {
                    matches.add(modId);
                }
            }
            if (matches.isEmpty()) {
                clearTabSession();
                return false;
            }

            this.tabMatches = matches;
            this.tabMatchIndex = -1;
            this.tabSessionPrefix = prefix;
            this.tabSessionStart = start;
            this.tabSessionEnd = end;
            applyTokenReplacement(text, start, end, hadAt, commonPrefix(matches, prefix));
            return true;
        }

        this.tabMatchIndex += backwards ? -1 : 1;
        if (this.tabMatchIndex < 0) {
            this.tabMatchIndex = this.tabMatches.size() - 1;
        } else if (this.tabMatchIndex >= this.tabMatches.size()) {
            this.tabMatchIndex = 0;
        }
        applyTokenReplacement(text, this.tabSessionStart, this.tabSessionEnd, hadAt, this.tabMatches.get(this.tabMatchIndex));
        return true;
    }

    private void applyTokenReplacement(String fullText, int start, int end, boolean hadAt, String replacementCore) {
        String replacement = hadAt ? "@" + replacementCore : replacementCore;
        String newText = fullText.substring(0, start) + replacement + fullText.substring(end);
        this.entryField.setValue(newText);
        int cursor = start + replacement.length();
        this.entryField.setCursorPosition(cursor);
        this.tabSessionStart = start;
        this.tabSessionEnd = cursor;
    }

    private void clearTabSession() {
        this.tabMatches = List.of();
        this.tabMatchIndex = 0;
        this.tabSessionPrefix = null;
        this.tabSessionStart = -1;
        this.tabSessionEnd = -1;
    }

    private static String commonPrefix(List<String> values, String fallback) {
        if (values.isEmpty()) {
            return fallback;
        }
        String prefix = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            String value = values.get(i);
            int length = Math.min(prefix.length(), value.length());
            int index = 0;
            while (index < length && prefix.charAt(index) == value.charAt(index)) {
                index++;
            }
            prefix = prefix.substring(0, index);
            if (prefix.isEmpty()) {
                return fallback;
            }
        }
        return prefix.length() > fallback.length() ? prefix : values.get(0);
    }

    private static boolean isSeparator(char c) {
        return c == ',' || c == ';' || Character.isWhitespace(c);
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isMouseOverChild(double mouseX, double mouseY) {
        for (var child : this.children()) {
            if (child.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private boolean clickChild(double mouseX, double mouseY, int button) {
        for (var child : this.children()) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(child);
                if (button == 0) {
                    this.setDragging(true);
                }
                return true;
            }
        }
        return false;
    }

    private int lineHeight() {
        return this.font.lineHeight + 2;
    }

    private int getVisibleRows() {
        return Math.max(1, (this.listHeight - 4) / lineHeight());
    }

    private void onRemoved(ActiveList list, String removed) {
        if (removed != null && removed.equals(this.lastAddedValue) && list == this.lastAddedList) {
            List<String> current = list == ActiveList.WHITELIST ? this.whitelist : this.blacklist;
            this.lastAddedValue = current.isEmpty() ? null : current.get(current.size() - 1);
        }
    }

    private ActiveList otherList(ActiveList list) {
        return list == ActiveList.WHITELIST ? ActiveList.BLACKLIST : ActiveList.WHITELIST;
    }

    private LinkedHashSet<Integer> selectionFor(ActiveList list) {
        return list == ActiveList.WHITELIST ? this.whitelistSelected : this.blacklistSelected;
    }

    private int primaryFor(ActiveList list) {
        return list == ActiveList.WHITELIST ? this.whitelistPrimary : this.blacklistPrimary;
    }

    private int anchorFor(ActiveList list) {
        return list == ActiveList.WHITELIST ? this.whitelistAnchor : this.blacklistAnchor;
    }

    private void setPrimarySelection(ActiveList list, int index) {
        if (list == ActiveList.WHITELIST) {
            this.whitelistPrimary = index;
        } else {
            this.blacklistPrimary = index;
        }
    }

    private void setAnchorSelection(ActiveList list, int index) {
        if (list == ActiveList.WHITELIST) {
            this.whitelistAnchor = index;
        } else {
            this.blacklistAnchor = index;
        }
    }

    private void clearSelection(ActiveList list) {
        selectionFor(list).clear();
        setPrimarySelection(list, -1);
        setAnchorSelection(list, -1);
    }

    private enum ActiveList {
        WHITELIST,
        BLACKLIST
    }

    public static final class ModIdFilterMenu extends AEBaseMenu {
        private ModIdFilterMenu(Inventory playerInventory) {
            super(null, -1, playerInventory, null);
        }
    }
}
