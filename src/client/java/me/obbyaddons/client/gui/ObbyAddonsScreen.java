package me.obbyaddons.client.gui;

import me.obbyaddons.feature.Feature;
import me.obbyaddons.feature.FeatureManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ObbyAddonsScreen extends Screen {

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 8;
    private static final int PANEL_WIDTH = 110;

    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 17;

    private static final int PANEL_GAP = 8;

    private static final int DUNGEON_X =
            PANEL_X + PANEL_WIDTH + PANEL_GAP;

    private static final int PURPLE = 0xFF7C3AED;
    private static final int PURPLE_DARK = 0xFF3B0764;

    private static final int PANEL_BACKGROUND = 0xE6141414;
    private static final int HEADER_BACKGROUND = 0xF0202020;
    private static final int ROW_BACKGROUND = 0xE61A1A1A;
    private static final int ROW_HOVER = 0xE62A2A2A;

    private static final int TEXT = 0xFFFFFFFF;

    private Feature hoveredFeature;
    private ChatCleanerSettingsWindow chatCleanerSettingsWindow;
    private StormSettingsWindow stormSettingsWindow;
    private EditBox searchBox;

    public ObbyAddonsScreen() {
        super(Component.literal("ObbyAddons"));
    }

    @Override
    protected void init() {
        int frameWidth = 220;
        int frameHeight = 26;

        int frameX = (this.width - frameWidth) / 2;
        int frameY = this.height - 34;

        int textBoxWidth = 200;
        int textBoxHeight = this.font.lineHeight;

        int textX = frameX + (frameWidth - textBoxWidth) / 2;
        int textY = frameY + (frameHeight - textBoxHeight) / 2;

        searchBox = new EditBox(
                this.font,
                textX,
                textY,
                textBoxWidth,
                textBoxHeight,
                Component.literal("Search")
        );

        searchBox.setHint(Component.literal("Search features..."));
        searchBox.setMaxLength(50);
        searchBox.setBordered(false);
        searchBox.setTextColor(0xFFFFFFFF);
        searchBox.setCentered(true);

        this.addRenderableWidget(searchBox);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        // Draw themed search background BEFORE Minecraft draws the EditBox
        if (searchBox != null) {
            int frameWidth = 220;
            int frameHeight = 26;

            int frameX = (this.width - frameWidth) / 2;
            int frameY = this.height - 34;

            graphics.fill(
                    frameX - 3,
                    frameY,
                    frameX + frameWidth + 3,
                    frameY + frameHeight,
                    0xF0181818
            );

            graphics.fill(
                    frameX - 3,
                    frameY,
                    frameX + frameWidth + 3,
                    frameY + 3,
                    PURPLE
            );
        }

        // Now Minecraft draws the search text on TOP
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        hoveredFeature = null;

        Feature chatCleaner = FeatureManager.getFeature("Chat Cleaner");
        Feature stormFeatures = FeatureManager.getFeature("Storm Features");

        String search = "";

        if (searchBox != null) {
            search = searchBox.getValue().trim().toLowerCase();
        }

        boolean showChatCleaner =
                chatCleaner != null &&
                (
                        search.isEmpty() ||
                        chatCleaner.getName()
                                .toLowerCase()
                                .contains(search)
                );

        int featureCount = showChatCleaner ? 1 : 0;
        int panelHeight = HEADER_HEIGHT + (featureCount * ROW_HEIGHT);

        // Main panel
        graphics.fill(
                PANEL_X,
                PANEL_Y,
                PANEL_X + PANEL_WIDTH,
                PANEL_Y + panelHeight,
                PANEL_BACKGROUND
        );

        // Purple top accent
        graphics.fill(
                PANEL_X,
                PANEL_Y,
                PANEL_X + PANEL_WIDTH,
                PANEL_Y + 2,
                PURPLE
        );

        // Header
        graphics.fill(
                PANEL_X,
                PANEL_Y + 2,
                PANEL_X + PANEL_WIDTH,
                PANEL_Y + HEADER_HEIGHT,
                HEADER_BACKGROUND
        );

        String title = "GENERAL";

        graphics.text(
                this.font,
                title,
                PANEL_X + (PANEL_WIDTH - this.font.width(title)) / 2,
                PANEL_Y + 6,
                TEXT,
                true
        );

        if (showChatCleaner) {
            drawFeatureRow(
                    graphics,
                    chatCleaner,
                    PANEL_X,
                    PANEL_Y + HEADER_HEIGHT,
                    mouseX,
                    mouseY
            );
        }
        // Dungeons panel
        int dungeonFeatureCount = stormFeatures != null ? 1 : 0;
        int dungeonPanelHeight =
                HEADER_HEIGHT + (dungeonFeatureCount * ROW_HEIGHT);

        graphics.fill(
                DUNGEON_X,
                PANEL_Y,
                DUNGEON_X + PANEL_WIDTH,
                PANEL_Y + dungeonPanelHeight,
                PANEL_BACKGROUND
        );

        // Purple accent line
        graphics.fill(
                DUNGEON_X,
                PANEL_Y,
                DUNGEON_X + PANEL_WIDTH,
                PANEL_Y + 2,
                PURPLE
        );

        // Header
        graphics.fill(
                DUNGEON_X,
                PANEL_Y + 2,
                DUNGEON_X + PANEL_WIDTH,
                PANEL_Y + HEADER_HEIGHT,
                HEADER_BACKGROUND
        );

        String dungeonTitle = "DUNGEONS";

        graphics.text(
                this.font,
                dungeonTitle,
                DUNGEON_X + (PANEL_WIDTH - this.font.width(dungeonTitle)) / 2,
                PANEL_Y + 6,
                TEXT,
                true
        );

        if (chatCleanerSettingsWindow != null) {
            chatCleanerSettingsWindow.render(
                    this,
                    graphics,
                    mouseX,
                    mouseY
            );
        }
        
        if (stormSettingsWindow != null) {
            stormSettingsWindow.render(
                    this,
                    graphics,
                    mouseX,
                    mouseY
            );
        }

        if (stormFeatures != null) {
            drawFeatureRow(
                    graphics,
                    stormFeatures,
                    DUNGEON_X,
                    PANEL_Y + HEADER_HEIGHT,
                    mouseX,
                    mouseY
            );
        }

        
    }

    private void drawFeatureRow(
            GuiGraphicsExtractor graphics,
            Feature feature,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        boolean hovered =
                mouseX >= x &&
                mouseX < x + PANEL_WIDTH &&
                mouseY >= y &&
                mouseY < y + ROW_HEIGHT;

        int background;

        if (feature.isEnabled()) {
            background = PURPLE_DARK;
        } else if (hovered) {
            background = ROW_HOVER;
        } else {
            background = ROW_BACKGROUND;
        }

        graphics.fill(
                x,
                y,
                x + PANEL_WIDTH,
                y + ROW_HEIGHT,
                background
        );

        if (feature.isEnabled()) {
            graphics.fill(
                    x,
                    y,
                    x + 2,
                    y + ROW_HEIGHT,
                    PURPLE
            );
        }

        graphics.text(
                this.font,
                feature.getName(),
                x + 6,
                y + 5,
                TEXT,
                true
        );

        if (hovered) {
            hoveredFeature = feature;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        if (chatCleanerSettingsWindow != null) {

            // Close button
            if (event.button() == 0 &&
                    chatCleanerSettingsWindow.isCloseButtonHovered(
                            event.x(),
                            event.y()
                    )) {

                chatCleanerSettingsWindow = null;
                return true;
            }

            // Click settings like Dungeon Spam / M7 Boss Spam
            if (chatCleanerSettingsWindow.handleClick(
                    event.x(),
                    event.y(),
                    event.button()
            )) {
                return true;
            }

            // Start dragging when clicking the header
            if (event.button() == 0 &&
                    chatCleanerSettingsWindow.isHeaderHovered(
                            event.x(),
                            event.y()
                    )) {

                chatCleanerSettingsWindow.startDragging(
                        event.x(),
                        event.y()
                );

                return true;
            }
        }

        if (stormSettingsWindow != null) {

            if (event.button() == 0 &&
                    stormSettingsWindow.isCloseButtonHovered(
                            event.x(),
                            event.y()
                    )) {

                stormSettingsWindow = null;
                return true;
            }

            if (stormSettingsWindow.handleClick(
                    event.x(),
                    event.y(),
                    event.button()
            )) {
                return true;
            }

            if (event.button() == 0 &&
                    stormSettingsWindow.isHeaderHovered(
                            event.x(),
                            event.y()
                    )) {

                stormSettingsWindow.startDragging(
                        event.x(),
                        event.y()
                );

                return true;
            }
        }   

        if (hoveredFeature != null) {

            if (event.button() == 0) {
                hoveredFeature.toggle();
                return true;
            }

            if (event.button() == 1 &&
                    hoveredFeature.getName().equals("Chat Cleaner")) {

                int windowWidth = 145;
                int windowHeight = 82;

                int centerX = (this.width - windowWidth) / 2;
                int centerY = (this.height - windowHeight) / 2;

                chatCleanerSettingsWindow =
                        new ChatCleanerSettingsWindow(centerX, centerY);

                return true;
            }

            if (event.button() == 1 &&
                    hoveredFeature.getName().equals("Storm Features")) {

                int windowWidth = 190;
                int windowHeight = 260;

                int centerX = (this.width - windowWidth) / 2;
                int centerY = (this.height - windowHeight) / 2;

                stormSettingsWindow =
                        new StormSettingsWindow(centerX, centerY);

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        if (chatCleanerSettingsWindow != null &&
                chatCleanerSettingsWindow.isDragging()) {

            chatCleanerSettingsWindow.dragTo(
                    event.x(),
                    event.y()
            );

            return true;
        }

        // Drag Storm LB slider
        if (stormSettingsWindow != null &&
                stormSettingsWindow.isSliderDragging()) {

            stormSettingsWindow.dragSlider(
                    event.x()
            );

            return true;
        }

        // Drag Storm settings window
        if (stormSettingsWindow != null &&
                stormSettingsWindow.isDragging()) {

            stormSettingsWindow.dragTo(
                    event.x(),
                    event.y()
            );

            return true;
        }

        return super.mouseDragged(
                event,
                dragX,
                dragY
        );
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {

        if (chatCleanerSettingsWindow != null &&
                chatCleanerSettingsWindow.isDragging()) {

            chatCleanerSettingsWindow.stopDragging();
            return true;
        }

        // Stop dragging the Storm LB slider
        if (stormSettingsWindow != null &&
                stormSettingsWindow.isSliderDragging()) {

            stormSettingsWindow.stopSliderDragging();
            return true;
        }

        // Stop dragging the Storm settings window
        if (stormSettingsWindow != null &&
                stormSettingsWindow.isDragging()) {

            stormSettingsWindow.stopDragging();
            return true;
        }

        return super.mouseReleased(event);
    }
}