package com.travelsync.util;

import com.travelsync.visualization.Palette;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/** Switches TravelSync between its light and dark theme, remembers the choice and tells canvases to repaint. */
public final class ThemeManager {

    private static final String PREF_KEY = "lightTheme";
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    private static Scene scene;
    private static boolean light = true; // light is TravelSync's default theme

    private ThemeManager() { }

    /** Attaches the manager to the main scene and restores the saved preference (default: light). */
    public static void init(Scene mainScene) {
        scene = mainScene;
        try {
            light = Preferences.userNodeForPackage(ThemeManager.class).getBoolean(PREF_KEY, true);
        } catch (RuntimeException e) {
            light = true; // preferences unavailable: fall back to the default light theme
        }
        apply();
    }

    public static boolean isLight() { return light; }

    /** Toggles the theme, persists it and notifies listeners (used by canvases to repaint). */
    public static void toggle() {
        light = !light;
        try {
            Preferences.userNodeForPackage(ThemeManager.class).putBoolean(PREF_KEY, light);
        } catch (RuntimeException e) {
            // not persisting is harmless
        }
        apply();
        for (Runnable listener : List.copyOf(LISTENERS)) {
            listener.run();
        }
    }

    /** Registers a callback executed after every theme change (used by algorithm panels to repaint their canvas). */
    public static void addListener(Runnable listener) {
        LISTENERS.add(listener);
    }

    /** Applies the current theme to a dialog (dialogs open in their own stage and need the stylesheet explicitly). */
    public static void style(DialogPane pane) {
        pane.getStylesheets().add(light ? Theme.LIGHT_STYLESHEET : Theme.DARK_STYLESHEET);
    }

    private static void apply() {
        Palette.setLight(light);
        if (scene == null) return;
        scene.getStylesheets().setAll(light ? Theme.LIGHT_STYLESHEET : Theme.DARK_STYLESHEET);
    }
}
