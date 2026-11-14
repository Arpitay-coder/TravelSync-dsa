package com.travelsync.ui.page;

import com.travelsync.ui.Page;
import javafx.scene.Node;

import java.util.function.Supplier;

/** Wraps an existing node-based view as a {@link Page}. */
public final class ViewPage implements Page {

    private final String title;
    private final String subtitle;
    private final Supplier<Node> factory;

    public ViewPage(String title, String subtitle, Supplier<Node> factory) {
        this.title = title;
        this.subtitle = subtitle;
        this.factory = factory;
    }

    @Override
    public String title() { return title; }

    @Override
    public String subtitle() { return subtitle; }

    @Override
    public Node content() { return factory.get(); }
}
