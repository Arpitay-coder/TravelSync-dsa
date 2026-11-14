package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.string.AhoCorasickPanel;
import com.travelsync.ui.panel.string.KmpPanel;
import com.travelsync.ui.panel.string.RabinKarpPanel;
import com.travelsync.ui.panel.string.ZFunctionPanel;

/** M1 page: string algorithms applied to destination, hotel and booking-code search. */
public final class StringIntelligencePage extends TabbedAlgorithmPage {

    public StringIntelligencePage() {
        addPanel("KMP", new KmpPanel(), "KMP");
        addPanel("Z-Function", new ZFunctionPanel(), "Z-Function");
        addPanel("Rabin-Karp", new RabinKarpPanel(), "Rabin-Karp");
        addPanel("Aho-Corasick", new AhoCorasickPanel(), "Aho-Corasick");
    }

    @Override
    public String title() { return "String Intelligence"; }

    @Override
    public String subtitle() { return "M1 \u2013 KMP, Z-Function, Rabin-Karp and Aho-Corasick over travel data"; }
}
