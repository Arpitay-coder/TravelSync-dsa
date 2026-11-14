package com.travelsync.data;

import javafx.beans.property.*;

public final class AppSettings {

    public enum Density { COMFORTABLE, COMPACT }

    private static final AppSettings INSTANCE = new AppSettings();
    public static AppSettings getInstance() { return INSTANCE; }

    // Milliseconds per visualization step at "Normal" speed; Slow = x2, Fast = x0.4
    private final DoubleProperty stepDurationMillis = new SimpleDoubleProperty(600);
    private final ObjectProperty<Density> density = new SimpleObjectProperty<>(Density.COMFORTABLE);
    private final BooleanProperty autoRefresh = new SimpleBooleanProperty(true);

    private AppSettings() { }

    public DoubleProperty stepDurationMillisProperty() { return stepDurationMillis; }
    public double getStepDurationMillis() { return stepDurationMillis.get(); }
    public void setStepDurationMillis(double v) { stepDurationMillis.set(v); }

    public ObjectProperty<Density> densityProperty() { return density; }
    public Density getDensity() { return density.get(); }
    public void setDensity(Density d) { density.set(d); }

    public BooleanProperty autoRefreshProperty() { return autoRefresh; }
    public boolean isAutoRefresh() { return autoRefresh.get(); }
    public void setAutoRefresh(boolean v) { autoRefresh.set(v); }
}
