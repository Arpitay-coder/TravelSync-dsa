package com.travelsync.ui.page;

import com.travelsync.ui.TabbedAlgorithmPage;
import com.travelsync.ui.panel.suffix.LcpPanel;
import com.travelsync.ui.panel.suffix.SuffixArrayPanel;
import com.travelsync.ui.panel.suffix.SuffixAutomatonPanel;

/** M2 page: suffix structures for indexing and comparing travel documents. */
public final class DocumentIntelligencePage extends TabbedAlgorithmPage {

    public DocumentIntelligencePage() {
        addPanel("Suffix Array / SA-IS", new SuffixArrayPanel(), "Suffix Array", "SA-IS");
        addPanel("LCP / Kasai", new LcpPanel(), "LCP / Kasai");
        addPanel("Suffix Automaton", new SuffixAutomatonPanel(), "Suffix Automaton");
    }

    @Override
    public String title() { return "Document Intelligence"; }

    @Override
    public String subtitle() { return "M2 \u2013 Suffix Array, SA-IS, LCP/Kasai and Suffix Automaton over travel descriptions"; }
}
