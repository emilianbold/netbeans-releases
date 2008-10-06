/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
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
        String mimeType = BlockHighlighting.getMimeType(component);
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

    protected abstract Position [] getCurrentBlockPositions(Document document, Caret caret);
    
    // ------------------------------------------------
    // private implementation
    // ------------------------------------------------
    
    private final void updateLineInfo(boolean fire) {
        ((AbstractDocument) component.getDocument()).readLock();
        try {
            Position [] currentLine = getCurrentBlockPositions(component.getDocument(), caret);

            if (!comparePositions(currentLine[0], currentLineStart) ||
                !comparePositions(currentLine[1], currentLineEnd))
            {
                Position changeStart = getLowerPosition(currentLine[0], currentLineStart);
                Position changeEnd = getHigherPosition(currentLine[1], currentLineEnd);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Current row changed from [" //NOI18N
                        + positionToString(currentLineStart) + ", " + positionToString(currentLineEnd) + "] to [" //NOI18N
                        + positionToString(currentLine[0]) + ", " + positionToString(currentLine[1]) + "]"); //NOI18N
                }

                currentLineStart = currentLine[0];
                currentLineEnd = currentLine[1];

                if (fire) {
                    fireHighlightsChange(
                        changeStart == null ? 0 : changeStart.getOffset(),
                        changeEnd == null ? Integer.MAX_VALUE : changeEnd.getOffset()
                    );
                }
            }
        } finally {
            ((AbstractDocument) component.getDocument()).readUnlock();
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
    
    private static String positionToString(Position p) {
        return p == null ? "null" : Integer.toString(p.getOffset()); //NOI18N
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
        
        protected Position[] getCurrentBlockPositions(Document document, Caret caret) {
            if (document != null && caret != null) {
                int caretOffset = caret.getDot();
                try {
                    int startOffset = DocUtils.getRowStart(document, caretOffset, 0);
                    int endOffset = DocUtils.getRowEnd(document, caretOffset);

                    // include the new-line character or the end of the document
                    endOffset++;

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
            super(component, FontColorNames.SELECTION_COLORING, true, true);
        }
    
        protected Position[] getCurrentBlockPositions(Document document, Caret caret) {
            if (document != null && caret != null) {
                int caretOffset = caret.getDot();
                int markOffset = caret.getMark();

                if (caretOffset != markOffset) {
                    try {
                        return new Position [] {
                            document.createPosition(Math.min(caretOffset, markOffset)),
                            document.createPosition(Math.max(caretOffset, markOffset)),
                        };
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            
            return new Position [] { null, null };
        }
    } // End of TextSelectionHighlighting class
}
