package me.obbyaddons.client.gui;

import me.obbyaddons.client.config.ObbyConfig;
import me.obbyaddons.client.features.dungeon.ExplosiveArrowSettings;
import me.obbyaddons.client.features.dungeon.StormSettings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {

    private static final String STORM_PREVIEW_TEXT = "25.35";
    private static final String DEATH_PREVIEW_TEXT = "41.30";
    private static final String LB_PREVIEW_TEXT = "5.00";

    private static final String EXPLOSIVE_ARROW_PREVIEW_TEXT =
            "Explosive Arrow: 74,586,293";

    private static final int PURPLE = 0xFF7C3AED;
    private static final int SELECTED_BACKGROUND = 0x553B0764;

    private boolean draggingStormTimer = false;
    private boolean draggingDeathTimer = false;
    private boolean draggingLbTimer = false;
    private boolean draggingExplosiveArrow = false;

    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen() {
        super(Component.literal("ObbyAddons HUD Editor"));
    }

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

        drawStormTimerPreview(
                graphics,
                mouseX,
                mouseY
        );

        drawDeathTimerPreview(
                graphics,
                mouseX,
                mouseY
        );

        drawLbTimerPreview(
                graphics,
                mouseX,
                mouseY
        );

        drawExplosiveArrowPreview(
                graphics,
                mouseX,
                mouseY
        );

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
    // PREVIEWS
    // =========================

    private void drawStormTimerPreview(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        drawHudPreview(
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
    }

    private void drawDeathTimerPreview(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        drawHudPreview(
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
    }

    private void drawLbTimerPreview(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        drawHudPreview(
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
    }

    private void drawExplosiveArrowPreview(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        drawHudPreview(
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
    }

    private void drawHudPreview(
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
        int normalWidth =
                this.font.width(text);

        int normalHeight =
                this.font.lineHeight;

        int scaledWidth =
                Math.round(
                        normalWidth * scale
                );

        int scaledHeight =
                Math.round(
                        normalHeight * scale
                );

        boolean hovered =
                mouseX >= x &&
                mouseX <= x + scaledWidth &&
                mouseY >= y &&
                mouseY <= y + scaledHeight;

        if (hovered || dragging) {
            graphics.fill(
                    x - 3,
                    y - 3,
                    x + scaledWidth + 3,
                    y + scaledHeight + 3,
                    SELECTED_BACKGROUND
            );
        }

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

        if (hovered || dragging) {
            graphics.fill(
                    x - 3,
                    y - 3,
                    x + scaledWidth + 3,
                    y - 2,
                    PURPLE
            );
        }
    }

    // =========================
    // HOVER CHECKS
    // =========================

    private boolean isStormTimerHovered(
            double mouseX,
            double mouseY
    ) {
        return isHudHovered(
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
        return isHudHovered(
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
        return isHudHovered(
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
        return isHudHovered(
                EXPLOSIVE_ARROW_PREVIEW_TEXT,
                ExplosiveArrowSettings.damageTrackerX,
                ExplosiveArrowSettings.damageTrackerY,
                ExplosiveArrowSettings.damageTrackerScale,
                mouseX,
                mouseY
        );
    }

    private boolean isHudHovered(
            String text,
            int x,
            int y,
            float scale,
            double mouseX,
            double mouseY
    ) {
        int scaledWidth =
                Math.round(
                        this.font.width(text) * scale
                );

        int scaledHeight =
                Math.round(
                        this.font.lineHeight * scale
                );

        return mouseX >= x &&
                mouseX <= x + scaledWidth &&
                mouseY >= y &&
                mouseY <= y + scaledHeight;
    }

    // =========================
    // CLICK / DRAG
    // =========================

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0) {

            if (isStormTimerHovered(
                    event.x(),
                    event.y()
            )) {
                draggingStormTimer = true;

                dragOffsetX =
                        event.x()
                                - StormSettings.stormTimerX;

                dragOffsetY =
                        event.y()
                                - StormSettings.stormTimerY;

                return true;
            }

            if (isDeathTimerHovered(
                    event.x(),
                    event.y()
            )) {
                draggingDeathTimer = true;

                dragOffsetX =
                        event.x()
                                - StormSettings.stormDeathTimerX;

                dragOffsetY =
                        event.y()
                                - StormSettings.stormDeathTimerY;

                return true;
            }

            if (isLbTimerHovered(
                    event.x(),
                    event.y()
            )) {
                draggingLbTimer = true;

                dragOffsetX =
                        event.x()
                                - StormSettings.stormLbX;

                dragOffsetY =
                        event.y()
                                - StormSettings.stormLbY;

                return true;
            }

            if (isExplosiveArrowHovered(
                    event.x(),
                    event.y()
            )) {
                draggingExplosiveArrow = true;

                dragOffsetX =
                        event.x()
                                - ExplosiveArrowSettings.damageTrackerX;

                dragOffsetY =
                        event.y()
                                - ExplosiveArrowSettings.damageTrackerY;

                return true;
            }
        }

        return super.mouseClicked(
                event,
                doubleClick
        );
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        if (draggingStormTimer) {

            StormSettings.stormTimerX =
                    (int) (
                            event.x() - dragOffsetX
                    );

            StormSettings.stormTimerY =
                    (int) (
                            event.y() - dragOffsetY
                    );

            clampStormTimerPosition();

            return true;
        }

        if (draggingDeathTimer) {

            StormSettings.stormDeathTimerX =
                    (int) (
                            event.x() - dragOffsetX
                    );

            StormSettings.stormDeathTimerY =
                    (int) (
                            event.y() - dragOffsetY
                    );

            clampDeathTimerPosition();

            return true;
        }

        if (draggingLbTimer) {

            StormSettings.stormLbX =
                    (int) (
                            event.x() - dragOffsetX
                    );

            StormSettings.stormLbY =
                    (int) (
                            event.y() - dragOffsetY
                    );

            clampLbTimerPosition();

            return true;
        }

        if (draggingExplosiveArrow) {

            ExplosiveArrowSettings.damageTrackerX =
                    (int) (
                            event.x() - dragOffsetX
                    );

            ExplosiveArrowSettings.damageTrackerY =
                    (int) (
                            event.y() - dragOffsetY
                    );

            clampExplosiveArrowPosition();

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

        return super.mouseReleased(event);
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
            StormSettings.stormTimerScale +=
                    change;

            StormSettings.stormTimerScale =
                    clampScale(
                            StormSettings.stormTimerScale
                    );

            clampStormTimerPosition();

            saveStormTimerLayout();

            return true;
        }

        if (isDeathTimerHovered(
                mouseX,
                mouseY
        )) {
            StormSettings.stormDeathTimerScale +=
                    change;

            StormSettings.stormDeathTimerScale =
                    clampScale(
                            StormSettings.stormDeathTimerScale
                    );

            clampDeathTimerPosition();

            saveStormDeathTimerLayout();

            return true;
        }

        if (isLbTimerHovered(
                mouseX,
                mouseY
        )) {
            StormSettings.stormLbScale +=
                    change;

            StormSettings.stormLbScale =
                    clampScale(
                            StormSettings.stormLbScale
                    );

            clampLbTimerPosition();

            saveStormLbLayout();

            return true;
        }

        if (isExplosiveArrowHovered(
                mouseX,
                mouseY
        )) {
            ExplosiveArrowSettings.damageTrackerScale +=
                    change;

            ExplosiveArrowSettings.damageTrackerScale =
                    clampScale(
                            ExplosiveArrowSettings.damageTrackerScale
                    );

            clampExplosiveArrowPosition();

            saveExplosiveArrowLayout();

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

    private int getScaledWidth(
            String text,
            float scale
    ) {
        return Math.round(
                this.font.width(text) * scale
        );
    }

    private int getScaledHeight(
            float scale
    ) {
        return Math.round(
                this.font.lineHeight * scale
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
}