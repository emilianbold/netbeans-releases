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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.PositionsBag;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class BlockHighlighting extends AbstractHighlightsContainer implements HighlightsChangeListener {

    private static final Logger LOG = Logger.getLogger(BlockHighlighting.class.getName());

    private String layerId;
    private JTextComponent component;
    private Document document;
    private PositionsBag bag;
    
    public BlockHighlighting(String layerId, JTextComponent component) {
        this.layerId = layerId;
        this.component = component;
        this.document = component.getDocument();
        
        this.bag = new PositionsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        EditorFindSupport.getInstance().hookLayer(this, component);
    }

    public String getLayerTypeId() {
        return layerId;
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }

    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }
    
    public void highlightBlock(final int startOffset, final int endOffset, final String coloringName) {
        document.render(new Runnable() {
            public void run() {
                if (startOffset < endOffset) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Highlighting block: [" + startOffset + ", " + endOffset + "]; " + getLayerTypeId());
                    }

                    try {
                        PositionsBag newBag = new PositionsBag(document);
                        newBag.addHighlight(
                            document.createPosition(startOffset), 
                            document.createPosition(endOffset),
                            getAttribs(coloringName)
                        );
                        bag.setHighlights(newBag);
                    } catch (BadLocationException e) {
                        LOG.log(Level.FINE, "Can't add highlight <" + startOffset + 
                            ", " + endOffset + ", " + coloringName + ">", e);
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Reseting block highlighs; " + getLayerTypeId());
                    }

                    bag.clear();
                }
            }
        });
    }

    public int [] gethighlightedBlock() {
        HighlightsSequence sequence = bag.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (sequence.moveNext()) {
            return new int [] { sequence.getStartOffset(), sequence.getEndOffset() };
        } else {
            return null;
        }
    }
    
    private AttributeSet getAttribs(String coloringName) {
        FontColorSettings fcs = MimeLookup.getLookup(
            MimePath.parse(getMimeType())).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(coloringName);
        return attribs == null ? SimpleAttributeSet.EMPTY : attribs;
    }
    
    private String getMimeType() {
        return component.getUI().getEditorKit(component).getContentType();
    }
}
