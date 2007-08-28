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
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
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

    private static final Logger LOG = Logger.getLogger(ComposedTextHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.impl.highlighting.ComposedTextHighlighting"; //NOI18N
    private static final String PROP_COMPLETION_ACTIVE = "completion-active"; //NOI18N
    
    private final JTextComponent component;
    private final Document document;
    private final OffsetsBag bag;
    private final AttributeSet highlightInverse;
    private final AttributeSet highlightUnderlined;
    
    public ComposedTextHighlighting(JTextComponent component, Document document, String mimeType) {
        // Prepare the highlight
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(FontColorSettings.class);
        AttributeSet dc = fcs.getFontColors(FontColorNames.DEFAULT_COLORING);
        Color background = (Color) dc.getAttribute(StyleConstants.Background);
        Color foreground = (Color) dc.getAttribute(StyleConstants.Foreground);
        highlightInverse = AttributesUtilities.createImmutable(StyleConstants.Background, foreground, StyleConstants.Foreground, background);
        highlightUnderlined = AttributesUtilities.createImmutable(StyleConstants.Underline, foreground);
        
        // Create the highlights container
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        // Start listening on the document
        this.document = document;
        this.document.addDocumentListener(WeakListeners.document(this, this.document));
        
        this.component = component;
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
        AttributedString composedText = getComposedTextAttribute(e);
        
        if (composedText != null) {
            enableParsing(component, false);
            
            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder();
                AttributedCharacterIterator aci = composedText.getIterator();

                sb.append("\nInsertUpdate: \n"); //NOI18N
                for(char c = aci.first(); c != AttributedCharacterIterator.DONE; c = aci.next()) {
                    sb.append("'").append(c).append("' = {"); //NOI18N
                    Map<AttributedCharacterIterator.Attribute, ?> attributes = aci.getAttributes();
                    for(AttributedCharacterIterator.Attribute key : attributes.keySet()) {
                        Object value = attributes.get(key);
                        if (value instanceof InputMethodHighlight) {
                            sb.append("'").append(key).append("' = {"); //NOI18N
                            Map<TextAttribute, ?> style = ((InputMethodHighlight) value).getStyle();
                            if (style == null) {
                                style = Toolkit.getDefaultToolkit().mapInputMethodHighlight((InputMethodHighlight) value);
                            }
                            if (style != null) {
                                for(TextAttribute ta : style.keySet()) {
                                    Object tav = style.get(ta);
                                    sb.append("'").append(ta).append("' = '").append(tav).append("', "); //NOI18N
                                }
                            } else {
                                sb.append("null"); //NOI18N
                            }
                            sb.append("}, "); //NOI18N
                        } else {
                            sb.append("'").append(key).append("' = '").append(value).append("', "); //NOI18N
                        }
                    }
                    sb.append("}\n"); //NOI18N
                }
                sb.append("-------------------------------------\n"); //NOI18N
                LOG.fine(sb.toString());
            }
            
            AttributedCharacterIterator aci = composedText.getIterator();
            bag.clear();
            
            int offset = e.getOffset();
            for(char c = aci.first(); c != AttributedCharacterIterator.DONE; c = aci.next()) {
                Map<AttributedCharacterIterator.Attribute, ?> attributes = aci.getAttributes();
                AttributeSet attrs = translateAttributes(attributes);
                bag.addHighlight(offset, offset + 1, attrs);
                offset++;
            }
        } else {
            enableParsing(component, true);
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
    
    private static AttributedString getComposedTextAttribute(DocumentEvent e) {
        if (e instanceof BaseDocumentEvent) {
            AttributeSet attribs = ((BaseDocumentEvent) e).getChangeAttributes();
            if (attribs != null) {
                Object value = attribs.getAttribute(StyleConstants.ComposedTextAttribute);
                if (value instanceof AttributedString) {
                    return (AttributedString) value;
                }
            }
        }
        return null;
    }

    private AttributeSet translateAttributes(Map<AttributedCharacterIterator.Attribute, ?> source) {
        for(AttributedCharacterIterator.Attribute sourceKey : source.keySet()) {
            Object sourceValue = source.get(sourceKey);
            
            // Ignore any non-input method related highlights
            if (!(sourceValue instanceof InputMethodHighlight)) {
                continue;
            }
            
            InputMethodHighlight imh = (InputMethodHighlight) sourceValue;
            
            // Get the style attributes
            Map<TextAttribute, ?> style = imh.getStyle();
            if (style == null) {
                style = Toolkit.getDefaultToolkit().mapInputMethodHighlight(imh);
                if (style == null) {
                    style = Collections.emptyMap();
                }
            }
            
            for(TextAttribute styleAttrKey : style.keySet()) {
                Object styleAttrValue = style.get(styleAttrKey);
                if (styleAttrKey == TextAttribute.INPUT_METHOD_UNDERLINE) {
                    LOG.fine("Underline ... " + imh); //NOI18N
                    return highlightUnderlined;
                } else if (styleAttrKey == TextAttribute.SWAP_COLORS) {
                    LOG.fine("Inverse ... " + imh); //NOI18N
                    return highlightInverse;
                }
            }
        }
        
        LOG.fine("No translation for " + source);
        return SimpleAttributeSet.EMPTY;
    }
    
    private static void enableParsing(JTextComponent component, boolean enable) {
        boolean newCompletionActive = !enable;
        Boolean oldCompletionActive = (Boolean) component.getClientProperty(PROP_COMPLETION_ACTIVE);
        
        if (oldCompletionActive == null || oldCompletionActive != newCompletionActive) {
            component.putClientProperty(PROP_COMPLETION_ACTIVE, newCompletionActive);
        }
    }
}
