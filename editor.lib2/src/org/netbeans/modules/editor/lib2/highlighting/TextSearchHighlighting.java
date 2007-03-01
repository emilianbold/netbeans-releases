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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
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
public class TextSearchHighlighting extends AbstractHighlightsContainer implements PropertyChangeListener, HighlightsChangeListener {

    private static final Logger LOG = Logger.getLogger(TextSearchHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.TextSearchHighlighting"; //NOI18N
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private final Document document;
    private final OffsetsBag bag;
    
    /** Creates a new instance of TextSearchHighlighting */
    public TextSearchHighlighting(JTextComponent component) {
        // Determine the mime type
        EditorKit kit = component.getUI().getEditorKit(component);
        String mimeType = kit == null ? null : kit.getContentType();
        this.mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);
        
        this.component = component;
        this.document = component.getDocument();
        
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        EditorFindSupport.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(this, EditorFindSupport.getInstance())
        );
        
        fillInTheBag();
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }
    
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null ||
            EditorFindSupport.FIND_WHAT.equals(evt.getPropertyName()) ||
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH.equals(evt.getPropertyName()))
        {
            fillInTheBag();
        }
    }

    private void fillInTheBag() {
        document.render(new Runnable() {
            public void run() {
                OffsetsBag newBag = new OffsetsBag(document);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("TSH: filling the bag; enabled = " + isEnabled());
                }

                if (isEnabled()) {
                    try {
                        int [] blocks = EditorFindSupport.getInstance().getBlocks(
                            new int [] {-1, -1}, document, 0, document.getLength());

                        assert blocks.length % 2 == 0 : "Wrong number of block offsets";

                        AttributeSet attribs = getAttribs();
                        for (int i = 0; i < blocks.length / 2; i++) {
                            newBag.addHighlight(blocks[2 * i], blocks[2 * i + 1], attribs);
                        }
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }

                bag.setHighlights(newBag);
            }
        });
    }
    
    private boolean isEnabled() {
        Object prop = EditorFindSupport.getInstance().getFindProperty(
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
        return (prop instanceof Boolean) && ((Boolean) prop).booleanValue();
    }
    
    private AttributeSet getAttribs() {
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(FontColorNames.HIGHLIGHT_SEARCH_COLORING);
        return attribs == null ? SimpleAttributeSet.EMPTY : attribs;
    }
}
