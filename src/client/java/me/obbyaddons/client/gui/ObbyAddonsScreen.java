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
    private ExplosiveArrowSettingsWindow explosiveArrowSettingsWindow;

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

        int textX =
                frameX + (frameWidth - textBoxWidth) / 2;

        int textY =
                frameY + (frameHeight - textBoxHeight) / 2;

        searchBox = new EditBox(
                this.font,
                textX,
                textY,
                textBoxWidth,
                textBoxHeight,
                Component.literal("Search")
        );

        searchBox.setHint(
                Component.literal("Search features...")
        );

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
        drawSearchBackground(graphics);

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        hoveredFeature = null;

        Feature chatCleaner =
                FeatureManager.getFeature("Chat Cleaner");

        Feature stormFeatures =
                FeatureManager.getFeature("Storm Features");

        Feature explosiveArrow =
                FeatureManager.getFeature("Explosive Arrow");

        String search = getSearchText();

        boolean showChatCleaner =
                matchesSearch(chatCleaner, search);

        boolean showStormFeatures =
                matchesSearch(stormFeatures, search);

        boolean showExplosiveArrow =
                matchesSearch(explosiveArrow, search) ||
                        (
                                explosiveArrow != null &&
                                "damage tracker".contains(search)
                        );

        drawGeneralPanel(
                graphics,
                mouseX,
                mouseY,
                chatCleaner,
                showChatCleaner
        );

        drawDungeonPanel(
                graphics,
                mouseX,
                mouseY,
                stormFeatures,
                explosiveArrow,
                showStormFeatures,
                showExplosiveArrow
        );

        drawSettingsWindows(
                graphics,
                mouseX,
                mouseY
        );
    }

    private void drawSearchBackground(
            GuiGraphicsExtractor graphics
    ) {
        if (searchBox == null) {
            return;
        }

        int frameWidth = 220;
        int frameHeight = 26;

        int frameX =
                (this.width - frameWidth) / 2;

        int frameY =
                this.height - 34;

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

    private String getSearchText() {
        if (searchBox == null) {
            return "";
        }

        return searchBox
                .getValue()
                .trim()
                .toLowerCase();
    }

    private boolean matchesSearch(
            Feature feature,
            String search
    ) {
        return feature != null &&
                (
                        search.isEmpty() ||
                        feature
                                .getName()
                                .toLowerCase()
                                .contains(search)
                );
    }

    private void drawGeneralPanel(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            Feature chatCleaner,
            boolean showChatCleaner
    ) {
        int featureCount =
                showChatCleaner ? 1 : 0;

        int panelHeight =
                HEADER_HEIGHT +
                        (featureCount * ROW_HEIGHT);

        drawPanelBase(
                graphics,
                PANEL_X,
                PANEL_Y,
                PANEL_WIDTH,
                panelHeight,
                "GENERAL"
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
    }

    private void drawDungeonPanel(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            Feature stormFeatures,
            Feature explosiveArrow,
            boolean showStormFeatures,
            boolean showExplosiveArrow
    ) {
        int featureCount = 0;

        if (showStormFeatures) {
            featureCount++;
        }

        if (showExplosiveArrow) {
            featureCount++;
        }

        int panelHeight =
                HEADER_HEIGHT +
                        (featureCount * ROW_HEIGHT);

        drawPanelBase(
                graphics,
                DUNGEON_X,
                PANEL_Y,
                PANEL_WIDTH,
                panelHeight,
                "DUNGEONS"
        );

        int rowY =
                PANEL_Y + HEADER_HEIGHT;

        if (showStormFeatures) {
            drawFeatureRow(
                    graphics,
                    stormFeatures,
                    DUNGEON_X,
                    rowY,
                    mouseX,
                    mouseY
            );

            rowY += ROW_HEIGHT;
        }

        if (showExplosiveArrow) {
            drawFeatureRow(
                    graphics,
                    explosiveArrow,
                    DUNGEON_X,
                    rowY,
                    mouseX,
                    mouseY
            );
        }
    }

    private void drawPanelBase(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            String title
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                PANEL_BACKGROUND
        );

        graphics.fill(
                x,
                y,
                x + width,
                y + 2,
                PURPLE
        );

        graphics.fill(
                x,
                y + 2,
                x + width,
                y + HEADER_HEIGHT,
                HEADER_BACKGROUND
        );

        graphics.text(
                this.font,
                title,
                x + (width - this.font.width(title)) / 2,
                y + 6,
                TEXT,
                true
        );
    }

    private void drawFeatureRow(
            GuiGraphicsExtractor graphics,
            Feature feature,
            int x,
            int y,
            int mouseX,
            int mouseY
    ) {
        if (feature == null) {
            return;
        }

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

    private void drawSettingsWindows(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
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

        if (explosiveArrowSettingsWindow != null) {
            explosiveArrowSettingsWindow.render(
                    this,
                    graphics,
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (handleChatCleanerWindowClick(event)) {
            return true;
        }

        if (handleStormWindowClick(event)) {
            return true;
        }

        if (handleExplosiveArrowWindowClick(event)) {
            return true;
        }

        if (hoveredFeature != null) {

            // Left click = toggle entire feature
            if (event.button() == 0) {
                hoveredFeature.toggle();
                return true;
            }

            // Right click = open settings
            if (event.button() == 1) {

                String name =
                        hoveredFeature.getName();

                if (name.equals("Chat Cleaner")) {
                    openChatCleanerWindow();
                    return true;
                }

                if (name.equals("Storm Features")) {
                    openStormWindow();
                    return true;
                }

                if (name.equals("Explosive Arrow")) {
                    openExplosiveArrowWindow();
                    return true;
                }
            }
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private boolean handleChatCleanerWindowClick(
            MouseButtonEvent event
    ) {
        if (chatCleanerSettingsWindow == null) {
            return false;
        }

        if (event.button() == 0 &&
                chatCleanerSettingsWindow
                        .isCloseButtonHovered(
                                event.x(),
                                event.y()
                        )) {

            chatCleanerSettingsWindow = null;
            return true;
        }

        if (chatCleanerSettingsWindow.handleClick(
                event.x(),
                event.y(),
                event.button()
        )) {
            return true;
        }

        if (event.button() == 0 &&
                chatCleanerSettingsWindow
                        .isHeaderHovered(
                                event.x(),
                                event.y()
                        )) {

            chatCleanerSettingsWindow.startDragging(
                    event.x(),
                    event.y()
            );

            return true;
        }

        return false;
    }

    private boolean handleStormWindowClick(
            MouseButtonEvent event
    ) {
        if (stormSettingsWindow == null) {
            return false;
        }

        if (event.button() == 0 &&
                stormSettingsWindow
                        .isCloseButtonHovered(
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
                stormSettingsWindow
                        .isHeaderHovered(
                                event.x(),
                                event.y()
                        )) {

            stormSettingsWindow.startDragging(
                    event.x(),
                    event.y()
            );

            return true;
        }

        return false;
    }

    private boolean handleExplosiveArrowWindowClick(
            MouseButtonEvent event
    ) {
        if (explosiveArrowSettingsWindow == null) {
            return false;
        }

        if (event.button() == 0 &&
                explosiveArrowSettingsWindow
                        .isCloseButtonHovered(
                                event.x(),
                                event.y()
                        )) {

            explosiveArrowSettingsWindow = null;
            return true;
        }

        if (explosiveArrowSettingsWindow.handleClick(
                event.x(),
                event.y(),
                event.button()
        )) {
            return true;
        }

        if (event.button() == 0 &&
                explosiveArrowSettingsWindow
                        .isHeaderHovered(
                                event.x(),
                                event.y()
                        )) {

            explosiveArrowSettingsWindow.startDragging(
                    event.x(),
                    event.y()
            );

            return true;
        }

        return false;
    }

    private void openChatCleanerWindow() {
        int windowWidth = 145;
        int windowHeight = 82;

        int centerX =
                (this.width - windowWidth) / 2;

        int centerY =
                (this.height - windowHeight) / 2;

        chatCleanerSettingsWindow =
                new ChatCleanerSettingsWindow(
                        centerX,
                        centerY
                );
    }

    private void openStormWindow() {
        int windowWidth = 190;
        int windowHeight = 200;

        int centerX =
                (this.width - windowWidth) / 2;

        int centerY =
                (this.height - windowHeight) / 2;

        stormSettingsWindow =
                new StormSettingsWindow(
                        centerX,
                        centerY
                );
    }

    private void openExplosiveArrowWindow() {
        int windowWidth = 190;
        int windowHeight = 60;

        int centerX =
                (this.width - windowWidth) / 2;

        int centerY =
                (this.height - windowHeight) / 2;

        explosiveArrowSettingsWindow =
                new ExplosiveArrowSettingsWindow(
                        centerX,
                        centerY
                );
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

        if (stormSettingsWindow != null &&
                stormSettingsWindow.isSliderDragging()) {

            stormSettingsWindow.dragSlider(
                    event.x()
            );

            return true;
        }

        if (stormSettingsWindow != null &&
                stormSettingsWindow.isDragging()) {

            stormSettingsWindow.dragTo(
                    event.x(),
                    event.y()
            );

            return true;
        }

        if (explosiveArrowSettingsWindow != null &&
                explosiveArrowSettingsWindow.isDragging()) {

            explosiveArrowSettingsWindow.dragTo(
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
    public boolean mouseReleased(
            MouseButtonEvent event
    ) {
        if (chatCleanerSettingsWindow != null &&
                chatCleanerSettingsWindow.isDragging()) {

            chatCleanerSettingsWindow.stopDragging();
            return true;
        }

        if (stormSettingsWindow != null &&
                stormSettingsWindow.isSliderDragging()) {

            stormSettingsWindow.stopSliderDragging();
            return true;
        }

        if (stormSettingsWindow != null &&
                stormSettingsWindow.isDragging()) {

            stormSettingsWindow.stopDragging();
            return true;
        }

        if (explosiveArrowSettingsWindow != null &&
                explosiveArrowSettingsWindow.isDragging()) {

            explosiveArrowSettingsWindow.stopDragging();
            return true;
        }

        return super.mouseReleased(event);
    }
}