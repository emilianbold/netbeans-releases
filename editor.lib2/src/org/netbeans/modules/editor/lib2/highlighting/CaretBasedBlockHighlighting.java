/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.lib.editor.util.swing.BlockCompare;
import org.netbeans.modules.editor.lib2.DocUtils;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * The layer for highlighting a caret row.
 * 
 * @author Vita Stejskal
 */
public abstract class CaretBasedBlockHighlighting extends AbstractHighlightsContainer implements ChangeListener, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(CaretBasedBlockHighlighting.class.getName());
    
    private boolean inited;
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private Caret caret;
    private ChangeListener caretListener;
    
    private final String coloringName;
    private final boolean extendsEOL;
    private final boolean extendsEmptyLine;
    
    private Position currentBlockStart;
    private Position currentBlockEnd;
    
    private AttributeSet attribs;
    
    private LookupListener lookupListener;

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
    }
    
    private void init() {
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
        if (!inited) {
            inited = true;
            init();
        }

        if (currentBlockStart != null && currentBlockEnd != null &&
            endOffset >= currentBlockStart.getOffset() && startOffset <= currentBlockEnd.getOffset())
        {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Queried for highlights in [" //NOI18N
                    + startOffset + ", " + endOffset + "], returning [" //NOI18N
                    + positionToString(currentBlockStart) + ", " + positionToString(currentBlockEnd) + "]" //NOI18N
                    + ", layer=" + s2s(this)); //NOI18N
            }

            return new SimpleHighlightsSequence(
                Math.max(currentBlockStart.getOffset(), startOffset), 
                Math.min(currentBlockEnd.getOffset(), endOffset), 
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
            Position newStart;
            Position newEnd;
            Position changeStart;
            Position changeEnd;
            Position[] newBlock = getCurrentBlockPositions(component.getDocument(), caret);
            if (newBlock != null) {
                newStart = newBlock[0];
                newEnd = newBlock[1];
                if (currentBlockStart == null) { // not valid yet
                    changeStart = currentBlockStart;
                    changeEnd = currentBlockEnd;
                } else { // Valid current start and end blocks
                    // Compare new block to old one
                    BlockCompare compare = BlockCompare.get(
                            newStart.getOffset(), newEnd.getOffset(),
                            currentBlockStart.getOffset(), currentBlockEnd.getOffset());
                    if (compare.equal()) { // Same blocks
                        changeStart = null; // No firing
                        changeEnd = null;
                    } else if (compare.equalStart()) {
                        if (compare.containsStrict()) {
                            changeStart = currentBlockEnd;
                            changeEnd = newEnd;
                        } else {
                            assert (compare.insideStrict());
                            changeStart = newEnd;
                            changeEnd = currentBlockEnd;
                        }
                    } else if (compare.equalEnd()) {
                        if (compare.containsStrict()) {
                            changeStart = newStart;
                            changeEnd = currentBlockStart;
                        } else {
                            assert (compare.insideStrict());
                            changeStart = currentBlockStart;
                            changeEnd = newStart;
                        }
                    } else {
                        changeStart = (compare.lowerStart()) ? newStart : currentBlockStart;
                        changeEnd = (compare.lowerEnd()) ? currentBlockEnd : newEnd;
                    }
                }

            } else { // newBlock is null => selection removed
                newStart = null;
                newEnd = null;
                changeStart = currentBlockStart;
                changeEnd = currentBlockEnd;
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Current block changed from [" //NOI18N
                        + positionToString(currentBlockStart) + ", " + positionToString(currentBlockEnd) + "] to [" //NOI18N
                        + positionToString(newStart) + ", " + positionToString(newEnd) + "]" //NOI18N
                        + ", layer=" + s2s(this)); //NOI18N
            }
            currentBlockStart = newStart;
            currentBlockEnd = newEnd;

            if (changeStart != null) {
                if (fire) {
                    fireHighlightsChange(changeStart.getOffset(), changeEnd.getOffset());
                }
            }
        } finally {
            ((AbstractDocument) component.getDocument()).readUnlock();
        }
    }
    
    private AttributeSet getAttribs() {
        if (lookupListener == null) {
            lookupListener = new LookupListener() {
                @Override
                public void resultChanged(LookupEvent ev) {
                    @SuppressWarnings("unchecked")
                    final Lookup.Result<FontColorSettings> result = (Lookup.Result<FontColorSettings>) ev.getSource();
                    setAttrs(result);
                }
            };
            Lookup lookup = MimeLookup.getLookup(mimePath);
            Lookup.Result<FontColorSettings> result = lookup.lookupResult(FontColorSettings.class);
            setAttrs(result);
            result.addLookupListener(WeakListeners.create(LookupListener.class,
                    lookupListener, result));
        }
        return attribs;
    }
        
    /*private*/ void setAttrs(Lookup.Result<FontColorSettings> result) {
        FontColorSettings fcs = result.allInstances().iterator().next();
        attribs = fcs.getFontColors(coloringName);
        if (attribs == null) {
            attribs = SimpleAttributeSet.EMPTY;
        } else if (extendsEOL || extendsEmptyLine) {
            attribs = AttributesUtilities.createImmutable(
                    attribs,
                    AttributesUtilities.createImmutable(
                    ATTR_EXTENDS_EOL, Boolean.valueOf(extendsEOL),
                    ATTR_EXTENDS_EMPTY_LINE, Boolean.valueOf(extendsEmptyLine)));
        }
    }
    
    private static String positionToString(Position p) {
        return p == null ? "null" : Integer.toString(p.getOffset()); //NOI18N
    }

    private static String s2s(Object o) {
        return o == null ? "null" : o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o)); //NOI18N
    }

    public static final class SimpleHighlightsSequence implements HighlightsSequence {
        
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

            return null;
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
            
            return null;
        }
    } // End of TextSelectionHighlighting class
}
