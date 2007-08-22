/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.impl.highlighting;

import java.awt.Color;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocumentEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class ComposedTextHighlighting extends AbstractHighlightsContainer implements DocumentListener, HighlightsChangeListener {

    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.impl.highlighting.ComposedTextHighlighting"; //NOI18N
    
    private final OffsetsBag bag;
    private final Document document;
    private final AttributeSet highlight;
    
    public ComposedTextHighlighting(Document document, String mimeType) {
        // Prepare the highlight
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(FontColorSettings.class);
        AttributeSet dc = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Color background = (Color) dc.getAttribute(StyleConstants.Background);
        Color foreground = (Color) dc.getAttribute(StyleConstants.Foreground);
        highlight = AttributesUtilities.createImmutable(StyleConstants.Background, foreground, StyleConstants.Foreground, background);
        
        // Create the highlights container
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        // Start listening on the document
        this.document = document;
        this.document.addDocumentListener(WeakListeners.document(this, this.document));
    }

    // ----------------------------------------------------------------------
    //  AbstractHighlightsContainer implementation
    // ----------------------------------------------------------------------
    
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    // ----------------------------------------------------------------------
    //  HighlightsChangeListener implementation
    // ----------------------------------------------------------------------
    
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }

    // ----------------------------------------------------------------------
    //  DocumentListener implementation
    // ----------------------------------------------------------------------
    
    public void insertUpdate(DocumentEvent e) {
        if (isCompositeText(e)) {
            bag.addHighlight(e.getOffset(), e.getOffset() + e.getLength(), highlight);
        } else {
            bag.clear();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        // ignore
    }

    public void changedUpdate(DocumentEvent e) {
        // ignore
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private static boolean isCompositeText(DocumentEvent e) {
        if (e instanceof BaseDocumentEvent) {
            AttributeSet attribs = ((BaseDocumentEvent) e).getChangeAttributes();
            if (attribs != null) {
                return attribs.isDefined(StyleConstants.ComposedTextAttribute);
            }
        }
        return false;
    }
    
}
