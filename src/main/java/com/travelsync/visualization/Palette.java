package com.travelsync.visualization;

import javafx.scene.paint.Color;

/**
 * Colour design tokens shared by all canvas visualisers. Every colour has a light-theme and a
 * dark-theme variant; {@link #setLight(boolean)} is called by {@code ThemeManager} whenever the
 * person toggles the theme, and every visualiser re-reads these on its next repaint.
 */
public final class Palette {

    private static boolean light = true; // TravelSync opens in its light theme by default

    private Palette() { }

    /** Switches every visualiser between the light (true) and dark (false) variant. */
    public static void setLight(boolean value) {
        light = value;
    }

    public static boolean isLight() { return light; }

    public static Color mutedText() {
        return light ? Color.web("#64748B") : Color.web("#8FA0C7");
    }

    public static Color ink() {
        return light ? Color.web("#1E293B") : Color.web("#E8ECF7");
    }

    public static Color background() {
        return light ? Color.web("#FFFFFF") : Color.web("#111832");
    }

    public static Color link() {
        return light ? Color.web("#C77D0A") : Color.web("#F5A623");
    }

    public static Color treeEdge() {
        return light ? Color.web("#9AA9CC") : Color.web("#3B4A75");
    }

    public static Color fill(CellState state) {
        return switch (state) {
            case NORMAL -> light ? Color.web("#F0F4F9") : Color.web("#1C2544");
            case ACTIVE -> Color.web("#3B82F6");
            case MATCH -> Color.web("#14B8A6");
            case MISMATCH -> Color.web("#EF4444");
            case WINDOW -> light ? Color.web("#FEF3C7") : Color.web("#7A4E0A");
            case DONE -> light ? Color.web("#CCFBF1") : Color.web("#164E45");
            case MUTED -> light ? Color.web("#F7F9FC") : Color.web("#141B33");
            case HIGHLIGHT -> Color.web("#8B5CF6");
        };
    }

    public static Color stroke(CellState state) {
        return switch (state) {
            case NORMAL -> light ? Color.web("#94A3B8") : Color.web("#3A4870");
            case ACTIVE -> light ? Color.web("#1D4ED8") : Color.web("#7FADFF");
            case MATCH -> light ? Color.web("#0F766E") : Color.web("#5DF0D4");
            case MISMATCH -> light ? Color.web("#B91C1C") : Color.web("#FF8FA3");
            case WINDOW -> light ? Color.web("#B45309") : Color.web("#F5C15A");
            case DONE -> light ? Color.web("#0D9488") : Color.web("#3ADCBB");
            case MUTED -> light ? Color.web("#CBD5E1") : Color.web("#2A3355");
            case HIGHLIGHT -> light ? Color.web("#6D28D9") : Color.web("#B79BFF");
        };
    }

    /** Text colour readable on top of {@link #fill}. */
    public static Color text(CellState state) {
        if (state == CellState.MUTED) return mutedText();
        if (light && (state == CellState.NORMAL || state == CellState.DONE || state == CellState.WINDOW)) {
            return Color.web("#1E293B");
        }
        if (!light && (state == CellState.NORMAL || state == CellState.MUTED)) {
            return Color.web("#E8ECF7");
        }
        return Color.WHITE;
    }

    /** @return the fill colour of a state as a CSS hex string (for row styling). */
    public static String css(CellState state) {
        Color c = fill(state);
        return String.format("#%02x%02x%02x", (int) Math.round(c.getRed() * 255), (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    /** Edge colour of a state; medium-contrast so thin lines are visible on both backgrounds. */
    public static Color edge(CellState state) {
        return switch (state) {
            case NORMAL -> light ? Color.web("#8A9BC4") : Color.web("#5A6DA0");
            case ACTIVE -> light ? Color.web("#2F6BEA") : Color.web("#7FADFF");
            case MATCH -> light ? Color.web("#0E9F78") : Color.web("#5DF0D4");
            case MISMATCH -> light ? Color.web("#D6304D") : Color.web("#FF8FA3");
            case WINDOW -> light ? Color.web("#C98F10") : Color.web("#F5C15A");
            case DONE -> light ? Color.web("#2C9A7C") : Color.web("#3AA88E");
            case MUTED -> light ? Color.web("#CDD6EA") : Color.web("#2E3860");
            case HIGHLIGHT -> light ? Color.web("#7C4FE0") : Color.web("#B79BFF");
        };
    }
}
