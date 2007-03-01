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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * The layer for highlighting a caret row.
 * 
 * @author Vita Stejskal
 */
public abstract class CaretBasedBlockHighlighting extends AbstractHighlightsContainer implements ChangeListener, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private Caret caret;
    private ChangeListener caretListener;
    
    private final String coloringName;
    private final boolean extendsEOL;
    private final boolean extendsEmptyLine;
    
    private Position currentLineStart;
    private Position currentLineEnd;
    
    /** Creates a new instance of CaretSelectionLayer */
    protected CaretBasedBlockHighlighting(JTextComponent component, String coloringName, boolean extendsEOL, boolean extendsEmptyLine) {
        // Determine the mime type
        EditorKit kit = component.getUI().getEditorKit(component);
        String mimeType = kit == null ? null : kit.getContentType();
        this.mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);

        this.coloringName = coloringName;
        this.extendsEOL = extendsEOL;
        this.extendsEmptyLine = extendsEmptyLine;
        
        // Hook up the component
        this.component = component;
        this.component.addPropertyChangeListener(WeakListeners.propertyChange(this, this.component));

        // Hook up the caret
        this.caret = component.getCaret();
        if (this.caret != null) {
            this.caretListener = WeakListeners.change(this, this.caret);
            this.caret.addChangeListener(caretListener);
        }

        // Calculate the current line position
        updateLineInfo(false);
    }

    // ------------------------------------------------
    // AbstractHighlightsContainer implementation
    // ------------------------------------------------
    
    public final HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (currentLineStart != null && currentLineEnd != null &&
            endOffset >= currentLineStart.getOffset() && startOffset <= currentLineEnd.getOffset())
        {
            return new SimpleHighlightsSequence(
                Math.max(currentLineStart.getOffset(), startOffset), 
                Math.min(currentLineEnd.getOffset(), endOffset), 
                getAttribs()
            );
        } else {
            return HighlightsSequence.EMPTY;
        }
    }

    // ------------------------------------------------
    // PropertyChangeListener implementation
    // ------------------------------------------------
    
    public final void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || "caret".equals(evt.getPropertyName())) { //NOI18N
            if (caret != null) {
                caret.removeChangeListener(caretListener);
                caretListener = null;
            }
            
            caret = component.getCaret();
            
            if (caret != null) {
                caretListener = WeakListeners.change(this, caret);
                caret.addChangeListener(caretListener);
            }
            
            updateLineInfo(true);
        }
    }
    
    // ------------------------------------------------
    // ChangeListener implementation
    // ------------------------------------------------
    
    public final void stateChanged(ChangeEvent e) {
        updateLineInfo(true);
    }

    protected abstract Position [] getCurrentBlockPositions(JTextComponent component, Document document, int caretOffset);
    
    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------
    
    private final void updateLineInfo(boolean fire) {
        Document document = component.getDocument();
        int caretOffset = caret == null ? -1 : caret.getDot();
        Position [] currentLine = getCurrentBlockPositions(component, document, caretOffset);
        
        if (!comparePositions(currentLine[0], currentLineStart) ||
            !comparePositions(currentLine[1], currentLineEnd))
        {
            Position changeStart = getLowerPosition(currentLine[0], currentLineStart);
            Position changeEnd = getHigherPosition(currentLine[1], currentLineEnd);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Current row changed from [" //NOI18N
                    + currentLineStart.getOffset() + ", " + currentLineEnd.getOffset()+ "] to [" //NOI18N
                    + currentLine[0].getOffset() + ", " + currentLine[1].getOffset() + "]"); //NOI18N
            }
            
            currentLineStart = currentLine[0];
            currentLineEnd = currentLine[1];

            if (fire) {
                fireHighlightsChange(
                    changeStart == null ? 0 : changeStart.getOffset(),
                    changeEnd == null ? document.getLength() : changeEnd.getOffset()
                );
            }
        }
    }
    
    private AttributeSet getAttribs() {
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(coloringName);
        
        if (attribs == null) {
            attribs = SimpleAttributeSet.EMPTY;
        } else if (extendsEOL || extendsEmptyLine) {
            attribs = AttributesUtilities.createImmutable(
                attribs, 
                AttributesUtilities.createImmutable(
                    ATTR_EXTENDS_EOL, Boolean.valueOf(extendsEOL),
                    ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(extendsEmptyLine))
            );
        }
        
        return attribs;
    }
    
    private static boolean comparePositions(Position p1, Position p2) {
        return (p1 == null && p2 == null) || 
               (p1 != null && p2 != null && p1.getOffset() == p2.getOffset());
    }
    
    private static Position getLowerPosition(Position p1, Position p2) {
        if (p1 != null && p2 != null) {
            return p1.getOffset() < p2.getOffset() ? p1 : p2;
        } else if (p1 != null) {
            return p1;
        } else if (p2 != null) {
            return p2;
        } else {
            return null;
        }
    }
    
    private static Position getHigherPosition(Position p1, Position p2) {
        if (p1 != null && p2 != null) {
            return p1.getOffset() > p2.getOffset() ? p1 : p2;
        } else if (p1 != null) {
            return p1;
        } else if (p2 != null) {
            return p2;
        } else {
            return null;
        }
    }
    
    private static final class SimpleHighlightsSequence implements HighlightsSequence {
        
        private int startOffset;
        private int endOffset;
        private AttributeSet attribs;
        
        private boolean end = false;
        
        public SimpleHighlightsSequence(int startOffset, int endOffset, AttributeSet attribs) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attribs = attribs;
        }

        public boolean moveNext() {
            if (!end) {
                end = true;
                return true;
            } else {
                return false;
            }
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public AttributeSet getAttributes() {
            return attribs;
        }
    } // End of SimpleHighlightsSequence
    
    public static final class CaretRowHighlighting extends CaretBasedBlockHighlighting {
        
        public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.CaretRowHighlighting"; //NOI18N
        
        public CaretRowHighlighting(JTextComponent component) {
            super(component, FontColorNames.CARET_ROW_COLORING, true, false);
        }
        
        protected Position[] getCurrentBlockPositions(
            JTextComponent component,
            Document document,
            int caretOffset
        ) {
            if (document != null && caretOffset >= 0 && caretOffset <= document.getLength()) {
                try {
                    int startOffset = DocUtils.getRowStart(document, caretOffset, 0);
                    int endOffset = DocUtils.getRowEnd(document, caretOffset);

                    if (endOffset < document.getLength()) {
                        endOffset++; // include the new-line character
                    }

                    return new Position [] {
                        document.createPosition(startOffset),
                        document.createPosition(endOffset),
                    };
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }

            return new Position [] { null, null };
        }
    } // End of CaretRowHighlighting class
    
    public static final class TextSelectionHighlighting extends CaretBasedBlockHighlighting {
        
        public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.TextSelectionHighlighting"; //NOI18N
    
        public TextSelectionHighlighting(JTextComponent component) {
            super(component, FontColorNames.SELECTION_COLORING, false, true);
        }
    
        protected Position[] getCurrentBlockPositions(
            JTextComponent component,
            Document document,
            int caretOffset
        ) {
            if (document != null && component != null) {
                int startOffset = component.getSelectionStart();
                int endOffset = component.getSelectionEnd();

                try {
                    return new Position [] {
                        document.createPosition(startOffset),
                        document.createPosition(endOffset),
                    };
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
                }
            }
            
            return new Position [] { null, null };
        }
    } // End of TextSelectionHighlighting class
}
