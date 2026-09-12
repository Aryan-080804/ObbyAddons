package me.obbyaddons.client.gui;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.client.features.dungeon.ExplosiveArrowSettings;
import me.obbyaddons.client.features.dungeon.StormSettings;
import me.obbyaddons.client.features.dungeon.TerminalClickTimerSettings;
import me.obbyaddons.client.features.dungeon.tracker.DungeonRunTrackerSettings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {

    // =========================
    // PREVIEW TEXT
    // =========================

    private static final String STORM_PREVIEW_TEXT =
            "25.35";

    private static final String DEATH_PREVIEW_TEXT =
            "35.15";

    private static final String LB_PREVIEW_TEXT =
            "5.00";

    private static final String EXPLOSIVE_ARROW_PREVIEW_TEXT =
            "Explosive Arrow: 74,586,293";

    private static final String CLICK_PROT_PREVIEW_TEXT =
            "400ms";

    private static final String[] DUNGEON_RUN_TRACKER_PREVIEW = {
            "M7",
            "Runs: 8",
            "Last: 5:12",
            "Avg: 5:21",
            "Best: 4:48"
    };

    // =========================
    // COLORS
    // =========================

    private static final int PURPLE =
            0xFF7C3AED;

    private static final int SELECTED_BACKGROUND =
            0x553B0764;

    // =========================
    // DRAG STATE
    // =========================

    private boolean draggingStormTimer = false;
    private boolean draggingDeathTimer = false;
    private boolean draggingLbTimer = false;
    private boolean draggingExplosiveArrow = false;
    private boolean draggingClickProt = false;
    private boolean draggingDungeonRunTracker = false;

    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen() {
        super(
                Component.literal(
                        "ObbyAddons HUD Editor"
                )
        );
    }

    // =========================
    // RENDER
    // =========================

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        drawSingleLinePreview(
                graphics,
                STORM_PREVIEW_TEXT,
                StormSettings.stormTimerX,
                StormSettings.stormTimerY,
                StormSettings.stormTimerScale,
                StormSettings.stormTimerColor,
                mouseX,
                mouseY,
                draggingStormTimer
        );

        drawSingleLinePreview(
                graphics,
                DEATH_PREVIEW_TEXT,
                StormSettings.stormDeathTimerX,
                StormSettings.stormDeathTimerY,
                StormSettings.stormDeathTimerScale,
                StormSettings.stormDeathTimerColor,
                mouseX,
                mouseY,
                draggingDeathTimer
        );

        drawSingleLinePreview(
                graphics,
                LB_PREVIEW_TEXT,
                StormSettings.stormLbX,
                StormSettings.stormLbY,
                StormSettings.stormLbScale,
                StormSettings.stormLbColor,
                mouseX,
                mouseY,
                draggingLbTimer
        );

        drawSingleLinePreview(
                graphics,
                EXPLOSIVE_ARROW_PREVIEW_TEXT,
                ExplosiveArrowSettings.damageTrackerX,
                ExplosiveArrowSettings.damageTrackerY,
                ExplosiveArrowSettings.damageTrackerScale,
                ExplosiveArrowSettings.damageTrackerColor,
                mouseX,
                mouseY,
                draggingExplosiveArrow
        );

        drawSingleLinePreview(
                graphics,
                CLICK_PROT_PREVIEW_TEXT,
                TerminalClickTimerSettings.x,
                TerminalClickTimerSettings.y,
                TerminalClickTimerSettings.scale,
                TerminalClickTimerSettings.color,
                mouseX,
                mouseY,
                draggingClickProt
        );

        drawMultiLinePreview(
                graphics,
                DUNGEON_RUN_TRACKER_PREVIEW,
                DungeonRunTrackerSettings.x,
                DungeonRunTrackerSettings.y,
                DungeonRunTrackerSettings.scale,
                DungeonRunTrackerSettings.color,
                mouseX,
                mouseY,
                draggingDungeonRunTracker
        );

        drawHelpText(
                graphics
        );
    }

    // =========================
    // PREVIEW DRAWING
    // =========================

    private void drawSingleLinePreview(
            GuiGraphicsExtractor graphics,
            String text,
            int x,
            int y,
            float scale,
            int color,
            int mouseX,
            int mouseY,
            boolean dragging
    ) {
        int scaledWidth =
                getScaledWidth(
                        text,
                        scale
                );

        int scaledHeight =
                getScaledHeight(
                        scale
                );

        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        scaledWidth,
                        scaledHeight
                );

        drawSelectionBackground(
                graphics,
                x,
                y,
                scaledWidth,
                scaledHeight,
                hovered || dragging
        );

        graphics.pose().pushMatrix();

        graphics.pose().translate(
                x,
                y
        );

        graphics.pose().scale(
                scale,
                scale
        );

        graphics.text(
                this.font,
                text,
                0,
                0,
                color,
                true
        );

        graphics.pose().popMatrix();

        drawSelectionTopBar(
                graphics,
                x,
                y,
                scaledWidth,
                hovered || dragging
        );
    }

    private void drawMultiLinePreview(
            GuiGraphicsExtractor graphics,
            String[] lines,
            int x,
            int y,
            float scale,
            int color,
            int mouseX,
            int mouseY,
            boolean dragging
    ) {
        int scaledWidth =
                getMultiLineScaledWidth(
                        lines,
                        scale
                );

        int scaledHeight =
                getMultiLineScaledHeight(
                        lines,
                        scale
                );

        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        scaledWidth,
                        scaledHeight
                );

        drawSelectionBackground(
                graphics,
                x,
                y,
                scaledWidth,
                scaledHeight,
                hovered || dragging
        );

        graphics.pose().pushMatrix();

        graphics.pose().translate(
                x,
                y
        );

        graphics.pose().scale(
                scale,
                scale
        );

        int yOffset = 0;

        for (String line : lines) {

            graphics.text(
                    this.font,
                    line,
                    0,
                    yOffset,
                    color,
                    true
            );

            yOffset +=
                    this.font.lineHeight + 1;
        }

        graphics.pose().popMatrix();

        drawSelectionTopBar(
                graphics,
                x,
                y,
                scaledWidth,
                hovered || dragging
        );
    }

    private void drawSelectionBackground(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            boolean selected
    ) {
        if (!selected) {
            return;
        }

        graphics.fill(
                x - 3,
                y - 3,
                x + width + 3,
                y + height + 3,
                SELECTED_BACKGROUND
        );
    }

    private void drawSelectionTopBar(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            boolean selected
    ) {
        if (!selected) {
            return;
        }

        graphics.fill(
                x - 3,
                y - 3,
                x + width + 3,
                y - 2,
                PURPLE
        );
    }

    private void drawHelpText(
            GuiGraphicsExtractor graphics
    ) {
        String help =
                "Drag to move • Scroll to resize";

        graphics.text(
                this.font,
                help,
                (this.width - this.font.width(help)) / 2,
                this.height - 20,
                0xFFFFFFFF,
                true
        );
    }

    // =========================
    // HOVER CHECKS
    // =========================

    private boolean isStormTimerHovered(
            double mouseX,
            double mouseY
    ) {
        return isSingleLineHovered(
                STORM_PREVIEW_TEXT,
                StormSettings.stormTimerX,
                StormSettings.stormTimerY,
                StormSettings.stormTimerScale,
                mouseX,
                mouseY
        );
    }

    private boolean isDeathTimerHovered(
            double mouseX,
            double mouseY
    ) {
        return isSingleLineHovered(
                DEATH_PREVIEW_TEXT,
                StormSettings.stormDeathTimerX,
                StormSettings.stormDeathTimerY,
                StormSettings.stormDeathTimerScale,
                mouseX,
                mouseY
        );
    }

    private boolean isLbTimerHovered(
            double mouseX,
            double mouseY
    ) {
        return isSingleLineHovered(
                LB_PREVIEW_TEXT,
                StormSettings.stormLbX,
                StormSettings.stormLbY,
                StormSettings.stormLbScale,
                mouseX,
                mouseY
        );
    }

    private boolean isExplosiveArrowHovered(
            double mouseX,
            double mouseY
    ) {
        return isSingleLineHovered(
                EXPLOSIVE_ARROW_PREVIEW_TEXT,
                ExplosiveArrowSettings.damageTrackerX,
                ExplosiveArrowSettings.damageTrackerY,
                ExplosiveArrowSettings.damageTrackerScale,
                mouseX,
                mouseY
        );
    }

    private boolean isClickProtHovered(
            double mouseX,
            double mouseY
    ) {
        return isSingleLineHovered(
                CLICK_PROT_PREVIEW_TEXT,
                TerminalClickTimerSettings.x,
                TerminalClickTimerSettings.y,
                TerminalClickTimerSettings.scale,
                mouseX,
                mouseY
        );
    }

    private boolean isDungeonRunTrackerHovered(
            double mouseX,
            double mouseY
    ) {
        int width =
                getMultiLineScaledWidth(
                        DUNGEON_RUN_TRACKER_PREVIEW,
                        DungeonRunTrackerSettings.scale
                );

        int height =
                getMultiLineScaledHeight(
                        DUNGEON_RUN_TRACKER_PREVIEW,
                        DungeonRunTrackerSettings.scale
                );

        return isInside(
                mouseX,
                mouseY,
                DungeonRunTrackerSettings.x,
                DungeonRunTrackerSettings.y,
                width,
                height
        );
    }

    private boolean isSingleLineHovered(
            String text,
            int x,
            int y,
            float scale,
            double mouseX,
            double mouseY
    ) {
        int width =
                getScaledWidth(
                        text,
                        scale
                );

        int height =
                getScaledHeight(
                        scale
                );

        return isInside(
                mouseX,
                mouseY,
                x,
                y,
                width,
                height
        );
    }

    private boolean isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;
    }

    // =========================
    // CLICK
    // =========================

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(
                    event,
                    doubleClick
            );
        }

        if (isStormTimerHovered(
                event.x(),
                event.y()
        )) {
            draggingStormTimer = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    StormSettings.stormTimerX,
                    StormSettings.stormTimerY
            );

            return true;
        }

        if (isDeathTimerHovered(
                event.x(),
                event.y()
        )) {
            draggingDeathTimer = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    StormSettings.stormDeathTimerX,
                    StormSettings.stormDeathTimerY
            );

            return true;
        }

        if (isLbTimerHovered(
                event.x(),
                event.y()
        )) {
            draggingLbTimer = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    StormSettings.stormLbX,
                    StormSettings.stormLbY
            );

            return true;
        }

        if (isExplosiveArrowHovered(
                event.x(),
                event.y()
        )) {
            draggingExplosiveArrow = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    ExplosiveArrowSettings.damageTrackerX,
                    ExplosiveArrowSettings.damageTrackerY
            );

            return true;
        }

        if (isClickProtHovered(
                event.x(),
                event.y()
        )) {
            draggingClickProt = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    TerminalClickTimerSettings.x,
                    TerminalClickTimerSettings.y
            );

            return true;
        }

        if (isDungeonRunTrackerHovered(
                event.x(),
                event.y()
        )) {
            draggingDungeonRunTracker = true;

            setDragOffset(
                    event.x(),
                    event.y(),
                    DungeonRunTrackerSettings.x,
                    DungeonRunTrackerSettings.y
            );

            return true;
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    private void setDragOffset(
            double mouseX,
            double mouseY,
            int hudX,
            int hudY
    ) {
        dragOffsetX =
                mouseX - hudX;

        dragOffsetY =
                mouseY - hudY;
    }

    // =========================
    // DRAG
    // =========================

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        int newX =
                (int) (
                        event.x()
                                - dragOffsetX
                );

        int newY =
                (int) (
                        event.y()
                                - dragOffsetY
                );

        if (draggingStormTimer) {

            StormSettings.stormTimerX =
                    newX;

            StormSettings.stormTimerY =
                    newY;

            clampStormTimerPosition();

            return true;
        }

        if (draggingDeathTimer) {

            StormSettings.stormDeathTimerX =
                    newX;

            StormSettings.stormDeathTimerY =
                    newY;

            clampDeathTimerPosition();

            return true;
        }

        if (draggingLbTimer) {

            StormSettings.stormLbX =
                    newX;

            StormSettings.stormLbY =
                    newY;

            clampLbTimerPosition();

            return true;
        }

        if (draggingExplosiveArrow) {

            ExplosiveArrowSettings.damageTrackerX =
                    newX;

            ExplosiveArrowSettings.damageTrackerY =
                    newY;

            clampExplosiveArrowPosition();

            return true;
        }

        if (draggingClickProt) {

            TerminalClickTimerSettings.x =
                    newX;

            TerminalClickTimerSettings.y =
                    newY;

            clampClickProtPosition();

            return true;
        }

        if (draggingDungeonRunTracker) {

            DungeonRunTrackerSettings.x =
                    newX;

            DungeonRunTrackerSettings.y =
                    newY;

            clampDungeonRunTrackerPosition();

            return true;
        }

        return super.mouseDragged(
                event,
                dragX,
                dragY
        );
    }

    // =========================
    // RELEASE
    // =========================

    @Override
    public boolean mouseReleased(
            MouseButtonEvent event
    ) {
        if (draggingStormTimer) {

            draggingStormTimer = false;

            saveStormTimerLayout();

            return true;
        }

        if (draggingDeathTimer) {

            draggingDeathTimer = false;

            saveStormDeathTimerLayout();

            return true;
        }

        if (draggingLbTimer) {

            draggingLbTimer = false;

            saveStormLbLayout();

            return true;
        }

        if (draggingExplosiveArrow) {

            draggingExplosiveArrow = false;

            saveExplosiveArrowLayout();

            return true;
        }

        if (draggingClickProt) {

            draggingClickProt = false;

            saveClickProtLayout();

            return true;
        }

        if (draggingDungeonRunTracker) {

            draggingDungeonRunTracker = false;

            saveDungeonRunTrackerLayout();

            return true;
        }

        return super.mouseReleased(
                event
        );
    }

    // =========================
    // SCROLL TO RESIZE
    // =========================

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        float change =
                verticalAmount > 0
                        ? 0.1f
                        : -0.1f;

        if (isStormTimerHovered(
                mouseX,
                mouseY
        )) {
            StormSettings.stormTimerScale =
                    clampScale(
                            StormSettings.stormTimerScale
                                    + change
                    );

            clampStormTimerPosition();
            saveStormTimerLayout();

            return true;
        }

        if (isDeathTimerHovered(
                mouseX,
                mouseY
        )) {
            StormSettings.stormDeathTimerScale =
                    clampScale(
                            StormSettings.stormDeathTimerScale
                                    + change
                    );

            clampDeathTimerPosition();
            saveStormDeathTimerLayout();

            return true;
        }

        if (isLbTimerHovered(
                mouseX,
                mouseY
        )) {
            StormSettings.stormLbScale =
                    clampScale(
                            StormSettings.stormLbScale
                                    + change
                    );

            clampLbTimerPosition();
            saveStormLbLayout();

            return true;
        }

        if (isExplosiveArrowHovered(
                mouseX,
                mouseY
        )) {
            ExplosiveArrowSettings.damageTrackerScale =
                    clampScale(
                            ExplosiveArrowSettings.damageTrackerScale
                                    + change
                    );

            clampExplosiveArrowPosition();
            saveExplosiveArrowLayout();

            return true;
        }

        if (isClickProtHovered(
                mouseX,
                mouseY
        )) {
            TerminalClickTimerSettings.scale =
                    clampScale(
                            TerminalClickTimerSettings.scale
                                    + change
                    );

            clampClickProtPosition();
            saveClickProtLayout();

            return true;
        }

        if (isDungeonRunTrackerHovered(
                mouseX,
                mouseY
        )) {
            DungeonRunTrackerSettings.scale =
                    clampScale(
                            DungeonRunTrackerSettings.scale
                                    + change
                    );

            clampDungeonRunTrackerPosition();
            saveDungeonRunTrackerLayout();

            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontalAmount,
                verticalAmount
        );
    }

    private float clampScale(
            float scale
    ) {
        return Math.max(
                0.5f,
                Math.min(
                        3.0f,
                        scale
                )
        );
    }

    // =========================
    // POSITION CLAMPING
    // =========================

    private void clampStormTimerPosition() {

        int width =
                getScaledWidth(
                        STORM_PREVIEW_TEXT,
                        StormSettings.stormTimerScale
                );

        int height =
                getScaledHeight(
                        StormSettings.stormTimerScale
                );

        StormSettings.stormTimerX =
                clampX(
                        StormSettings.stormTimerX,
                        width
                );

        StormSettings.stormTimerY =
                clampY(
                        StormSettings.stormTimerY,
                        height
                );
    }

    private void clampDeathTimerPosition() {

        int width =
                getScaledWidth(
                        DEATH_PREVIEW_TEXT,
                        StormSettings.stormDeathTimerScale
                );

        int height =
                getScaledHeight(
                        StormSettings.stormDeathTimerScale
                );

        StormSettings.stormDeathTimerX =
                clampX(
                        StormSettings.stormDeathTimerX,
                        width
                );

        StormSettings.stormDeathTimerY =
                clampY(
                        StormSettings.stormDeathTimerY,
                        height
                );
    }

    private void clampLbTimerPosition() {

        int width =
                getScaledWidth(
                        LB_PREVIEW_TEXT,
                        StormSettings.stormLbScale
                );

        int height =
                getScaledHeight(
                        StormSettings.stormLbScale
                );

        StormSettings.stormLbX =
                clampX(
                        StormSettings.stormLbX,
                        width
                );

        StormSettings.stormLbY =
                clampY(
                        StormSettings.stormLbY,
                        height
                );
    }

    private void clampExplosiveArrowPosition() {

        int width =
                getScaledWidth(
                        EXPLOSIVE_ARROW_PREVIEW_TEXT,
                        ExplosiveArrowSettings.damageTrackerScale
                );

        int height =
                getScaledHeight(
                        ExplosiveArrowSettings.damageTrackerScale
                );

        ExplosiveArrowSettings.damageTrackerX =
                clampX(
                        ExplosiveArrowSettings.damageTrackerX,
                        width
                );

        ExplosiveArrowSettings.damageTrackerY =
                clampY(
                        ExplosiveArrowSettings.damageTrackerY,
                        height
                );
    }

    private void clampClickProtPosition() {

        int width =
                getScaledWidth(
                        CLICK_PROT_PREVIEW_TEXT,
                        TerminalClickTimerSettings.scale
                );

        int height =
                getScaledHeight(
                        TerminalClickTimerSettings.scale
                );

        TerminalClickTimerSettings.x =
                clampX(
                        TerminalClickTimerSettings.x,
                        width
                );

        TerminalClickTimerSettings.y =
                clampY(
                        TerminalClickTimerSettings.y,
                        height
                );
    }

    private void clampDungeonRunTrackerPosition() {

        int width =
                getMultiLineScaledWidth(
                        DUNGEON_RUN_TRACKER_PREVIEW,
                        DungeonRunTrackerSettings.scale
                );

        int height =
                getMultiLineScaledHeight(
                        DUNGEON_RUN_TRACKER_PREVIEW,
                        DungeonRunTrackerSettings.scale
                );

        DungeonRunTrackerSettings.x =
                clampX(
                        DungeonRunTrackerSettings.x,
                        width
                );

        DungeonRunTrackerSettings.y =
                clampY(
                        DungeonRunTrackerSettings.y,
                        height
                );
    }

    // =========================
    // SIZE HELPERS
    // =========================

    private int getScaledWidth(
            String text,
            float scale
    ) {
        return Math.round(
                this.font.width(text)
                        * scale
        );
    }

    private int getScaledHeight(
            float scale
    ) {
        return Math.round(
                this.font.lineHeight
                        * scale
        );
    }

    private int getMultiLineScaledWidth(
            String[] lines,
            float scale
    ) {
        int normalWidth = 0;

        for (String line : lines) {

            normalWidth =
                    Math.max(
                            normalWidth,
                            this.font.width(line)
                    );
        }

        return Math.round(
                normalWidth * scale
        );
    }

    private int getMultiLineScaledHeight(
            String[] lines,
            float scale
    ) {
        int normalHeight =
                lines.length
                        * (this.font.lineHeight + 1);

        return Math.round(
                normalHeight * scale
        );
    }

    private int clampX(
            int x,
            int width
    ) {
        return Math.max(
                0,
                Math.min(
                        x,
                        this.width - width
                )
        );
    }

    private int clampY(
            int y,
            int height
    ) {
        return Math.max(
                0,
                Math.min(
                        y,
                        this.height - height
                )
        );
    }

    // =========================
    // SAVE LAYOUT
    // =========================

    private void saveStormTimerLayout() {

        ObbyConfig.get().stormTimerX =
                StormSettings.stormTimerX;

        ObbyConfig.get().stormTimerY =
                StormSettings.stormTimerY;

        ObbyConfig.get().stormTimerScale =
                StormSettings.stormTimerScale;

        ObbyConfig.save();
    }

    private void saveStormDeathTimerLayout() {

        ObbyConfig.get().stormDeathTimerX =
                StormSettings.stormDeathTimerX;

        ObbyConfig.get().stormDeathTimerY =
                StormSettings.stormDeathTimerY;

        ObbyConfig.get().stormDeathTimerScale =
                StormSettings.stormDeathTimerScale;

        ObbyConfig.save();
    }

    private void saveStormLbLayout() {

        ObbyConfig.get().stormLbX =
                StormSettings.stormLbX;

        ObbyConfig.get().stormLbY =
                StormSettings.stormLbY;

        ObbyConfig.get().stormLbScale =
                StormSettings.stormLbScale;

        ObbyConfig.save();
    }

    private void saveExplosiveArrowLayout() {

        ObbyConfig.get().explosiveArrowDamageTrackerX =
                ExplosiveArrowSettings.damageTrackerX;

        ObbyConfig.get().explosiveArrowDamageTrackerY =
                ExplosiveArrowSettings.damageTrackerY;

        ObbyConfig.get().explosiveArrowDamageTrackerScale =
                ExplosiveArrowSettings.damageTrackerScale;

        ObbyConfig.save();
    }

    private void saveClickProtLayout() {

        ObbyConfig.get().terminalClickTimerX =
                TerminalClickTimerSettings.x;

        ObbyConfig.get().terminalClickTimerY =
                TerminalClickTimerSettings.y;

        ObbyConfig.get().terminalClickTimerScale =
                TerminalClickTimerSettings.scale;

        ObbyConfig.save();
    }

    private void saveDungeonRunTrackerLayout() {

        ObbyConfig.get().dungeonRunTrackerX =
                DungeonRunTrackerSettings.x;

        ObbyConfig.get().dungeonRunTrackerY =
                DungeonRunTrackerSettings.y;

        ObbyConfig.get().dungeonRunTrackerScale =
                DungeonRunTrackerSettings.scale;

        ObbyConfig.save();
    }
}