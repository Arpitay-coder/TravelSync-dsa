package com.travelsync.util;

import java.util.Objects;

/** Locates the two theme stylesheets. See {@link ThemeManager} for switching between them. */
public final class Theme {

    public static final String LIGHT_STYLESHEET =
            Objects.requireNonNull(Theme.class.getResource("/com/travelsync/css/theme-light.css"), "theme-light.css missing")
                    .toExternalForm();

    public static final String DARK_STYLESHEET =
            Objects.requireNonNull(Theme.class.getResource("/com/travelsync/css/theme-dark.css"), "theme-dark.css missing")
                    .toExternalForm();

    /** Kept for existing call sites; points at the default (light) stylesheet. */
    public static final String STYLESHEET = LIGHT_STYLESHEET;

    private Theme() { }
}
