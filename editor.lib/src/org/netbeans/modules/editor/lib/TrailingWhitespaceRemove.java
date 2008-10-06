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

package org.netbeans.modules.editor.lib;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;

/**
 * Removal of trailing whitespace
 *
 * @author Miloslav Metelka
 * @since 1.27
 */
public final class TrailingWhitespaceRemove implements BeforeSaveTasks.Task, DocumentListener {

    // -J-Dorg.netbeans.modules.editor.lib2.TrailingWhitespaceRemove.level=FINE
    static final Logger LOG = Logger.getLogger(TrailingWhitespaceRemove.class.getName());

    static final int GET_ELEMENT_INDEX_THRESHOLD = 100;

    public static synchronized TrailingWhitespaceRemove install(BaseDocument doc) {
        TrailingWhitespaceRemove twr = (TrailingWhitespaceRemove) doc.getProperty(TrailingWhitespaceRemove.class);
        if (twr == null) {
            twr = new TrailingWhitespaceRemove(doc);
            BeforeSaveTasks beforeSaveTasks = BeforeSaveTasks.get(doc);
            beforeSaveTasks.addTask(twr);
            doc.putProperty(TrailingWhitespaceRemove.class, twr);
        }
        return twr;
    }

    Document doc;

    CharSequence docText;

    GapList<MutablePositionRegion> modRegions;

    private int lastRegionIndex;
    
    private boolean inWhitespaceRemove;

    private TrailingWhitespaceRemove(BaseDocument doc) {
        this.doc = doc;
        this.docText = DocumentUtilities.getText(doc); // Persists for doc's lifetime
        this.modRegions = emptyModRegions();
        doc.addUpdateDocumentListener(this);
    }

    public synchronized void run(CompoundEdit compoundEdit) {
        inWhitespaceRemove = true;
        try {
            new ModsProcessor().removeWhitespace();
            NewModRegionsEdit edit = new NewModRegionsEdit();
            compoundEdit.addEdit(edit);
            edit.run();
        } finally {
            inWhitespaceRemove = false;
        }
    }

    public void resetModRegions() {
        this.modRegions = emptyModRegions();
    }
    
    GapList<MutablePositionRegion> emptyModRegions() {
        return new GapList<MutablePositionRegion>(3);
    }

    public void insertUpdate(DocumentEvent evt) {
        CompoundEdit compoundEdit = (CompoundEdit) evt;
        int offset = evt.getOffset();
        int length = evt.getLength();
        boolean covered = false;
        if (lastRegionIndex >= 0 && lastRegionIndex < modRegions.size()) {
            covered = isCovered(offset, length);
        }
        if (!covered) {
            // Find by binary search
            lastRegionIndex = findRegionIndex(offset, false);
            if (lastRegionIndex >= 0) {
                covered = isCovered(offset, length);
            }
        }
        if (!covered) {
            addRegion(compoundEdit, offset, offset + length);
            // lastRegionIndex populated by index of addition
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        // Currently do not handle in any special way but
        // Since there's a mod on the line there will be a diff
        // so it should not matter much if the ending WS is removed too.
        if (inWhitespaceRemove) { // Removals of extra whitespace
            DocumentUtilities.putEventProperty(evt, "caretIgnore", Boolean.TRUE);
        }
    }

    public void changedUpdate(DocumentEvent evt) {
    }

    private boolean isCovered(int offset, int length) {
        PositionRegion region = modRegions.get(lastRegionIndex);
        if (region.getStartOffset() <= offset &&
                offset + length <= region.getEndOffset()) {
            return true;
        }
        return false;
    }

    private MutablePositionRegion addRegion(CompoundEdit compoundEdit, int startOffset, int endOffset) {
        try {
            MutablePositionRegion region = new MutablePositionRegion(doc, startOffset, endOffset);
            lastRegionIndex = findRegionIndex(startOffset, true);
            AddRegionEdit edit = new AddRegionEdit(lastRegionIndex, region);
            edit.run();
            compoundEdit.addEdit(edit);
            return region;
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    void addRegion(int index, MutablePositionRegion region) {
        modRegions.add(index, region);
    }
    
    private int findRegionIndex(int offset, boolean forInsert) {
        int low = 0;
        int high = modRegions.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = modRegions.get(mid).getStartOffset();
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else {
                // offset == region.getStartOffset()
                while (++mid < modRegions.size()) {
                    if (modRegions.get(mid).getStartOffset() != offset)
                        break;
                }
                mid--;
                if (forInsert)
                    low = mid + 1;
                else
                    high = mid;
                break;
            }
        }
        return forInsert ? low : high;
    }

    public void checkConsistency() {
        int lastOffset = 0;
        for (int i = 0; i < modRegions.size(); i++) {
            PositionRegion region = modRegions.get(i);
            int offset = region.getStartOffset();
            if (offset < lastOffset) {
                throw new IllegalStateException("region[" + i + "].getStartOffset()=" + // NOI18N
                        offset + " < lastOffset=" + lastOffset); // NOI18N
            }
            lastOffset = offset;
            offset = region.getEndOffset();
            if (offset < lastOffset) {
                throw new IllegalStateException("region[" + i + "].getEndOffset()=" + // NOI18N
                        offset + " < region.getStartOffset()=" + lastOffset); // NOI18N
            }
            lastOffset = offset;
        }
    }

    @Override
    public String toString() {
        int size = modRegions.size();
        int digitCount = String.valueOf(size).length();
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < size; i++) {
            PositionRegion region = modRegions.get(i);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(region.toString(doc));
            sb.append('\n');
        }
        return sb.toString();
    }

    private final class ModsProcessor {

        private final Element lineElementRoot;

        /** Index of current region. */
        private int regionIndex;

        /** Start offset of the current region. */
        private int regionStartOffset;

        /** End offset of the current region. */
        private int regionEndOffset;

        /** Index of current line. */
        private int lineIndex;

        /** Start offset of the current line. */
        private int lineStartOffset;

        /** Offset of '\n' on the current line. */
        private int lineLastOffset;

        /**
         * Line index that should be excluded from whitespace removal (line with caret)
         * or -1 for none.
         */
        private final int caretLineIndex;

        /**
         * Shift offset of the caret relative to caretLineIndex's line begining.
         */
        private final int caretRelativeOffset;

        ModsProcessor() {
            lineElementRoot = DocumentUtilities.getParagraphRootElement(doc);
            JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
            if (lastFocusedComponent != null && lastFocusedComponent.getDocument() == doc) {
                int caretOffset = lastFocusedComponent.getCaretPosition();
                caretLineIndex = lineElementRoot.getElementIndex(caretOffset);
                // Assign the relativeCaretOffset since the subsequent modifications
                // done by physical whitespace removal would make the absolute offsets unusable.
                caretRelativeOffset = caretOffset - lineElementRoot.getElement(caretLineIndex).getStartOffset();
            } else {
                caretLineIndex = -1;
                caretRelativeOffset = 0;
            }
        }

        void removeWhitespace() {
            regionIndex = modRegions.size();
            lineStartOffset = Integer.MAX_VALUE; // Will cause line's bin-search
            while (fetchPreviousNonEmptyRegion()) {
                // Use last offset since someone may paste "blah \n" so the last offset point to '\n' here
                int regionLastOffset = regionEndOffset - 1;
                int lastLineIndex = lineIndex;
                if (regionLastOffset + GET_ELEMENT_INDEX_THRESHOLD < lineStartOffset) {
                    // Too below - use binary search
                    lineIndex = lineElementRoot.getElementIndex(regionEndOffset - 1);
                    fetchLineElement();
                } else { // Within threshold - try to search sequentially
                    while (lineStartOffset > regionLastOffset) {
                        lineIndex--;
                        fetchLineElement();
                    }
                }

                if (lastLineIndex != lineIndex) {
                    removeWhitespaceOnLine();
                    while (regionStartOffset < lineStartOffset) {
                        lineIndex--;
                        fetchLineElement();
                        removeWhitespaceOnLine();
                    }
                }
            }
        }

        private boolean fetchPreviousNonEmptyRegion() {
            while (--regionIndex >= 0) {
                PositionRegion region = modRegions.get(regionIndex);
                regionStartOffset = region.getStartOffset();
                regionEndOffset = region.getEndOffset();
                if (regionStartOffset == regionEndOffset)// Empty region - continue
                    continue;
                return true;
            }
            return false;
        }

        private void fetchLineElement() {
            Element lineElement = lineElementRoot.getElement(lineIndex);
            lineStartOffset = lineElement.getStartOffset();
            lineLastOffset = lineElement.getEndOffset() - 1;
        }

        private void removeWhitespaceOnLine() {
            int startOffset = lineStartOffset; // lowest offset where WS can be removed
            if (lineIndex == caretLineIndex) {
                startOffset += caretRelativeOffset;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Line index " + lineIndex + " contains caret at relative offset " + // NOI18N
                            caretRelativeOffset + ".\n"); // NOI18N
                }
            }
            int offset;
            for (offset = lineLastOffset - 1; offset >= startOffset; offset--) {
                char c = docText.charAt(offset);
                // Currently only remove ' ' and '\t' - may be revised
                if (c != ' ' && c != '\t') {
                    break;
                }
            }
            // Increase offset (either below lineStartOffset or on non-white char)
            offset++;
            if (offset < lineLastOffset) {
                BadLocationException ble = null;
                try {
                    doc.remove(offset, lineLastOffset - offset);
                } catch (BadLocationException e) {
                    ble = e;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Remove between " + DocumentUtilities.debugOffset(doc, offset) + // NOI18N
                            " and " + DocumentUtilities.debugOffset(doc, lineLastOffset) + // NOI18N
                            (ble == null ? " succeeded." : " failed.") + // NOI18N
                            '\n'
                    );
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.INFO, "Exception thrown during removal:", ble); // NOI18N
                    }
                }
            }
        }
    }

    private final class AddRegionEdit extends AbstractUndoableEdit {

        private int index;

        private MutablePositionRegion region;

        public AddRegionEdit(int index, MutablePositionRegion region) {
            this.index = index;
            this.region = region;
        }
        
        public void run() {
            addRegion(index, region);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Added region " + region + " at index=" + index + '\n'); // NOI18N
                LOG.fine("Regions:\n" + modRegions + '\n'); // NOI18N
            }
        }
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (modRegions.get(index) != region) {
                index = findRegionIndex(region.getStartOffset(), false);
            }
            if (modRegions.get(index) == region) {
                modRegions.remove(index);
            } else { // Safety fallback
                modRegions.remove(region);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Removed region " + region + " at index=" + index + '\n'); // NOI18N
                LOG.fine("Regions:\n" + modRegions + '\n'); // NOI18N
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            // #145588 - must recompute index according to current modRegions state
            index = findRegionIndex(region.getStartOffset(), true);
            run();
        }

    }

    private final class NewModRegionsEdit extends AbstractUndoableEdit {
        
        private GapList<MutablePositionRegion> oldModRegions;
        
        private GapList<MutablePositionRegion> newModRegions;

        public NewModRegionsEdit() {
            this.oldModRegions = modRegions;
            this.newModRegions = emptyModRegions();
        }

        public void run() {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Abandoning old regions\n" + modRegions); // NOI18N
            }
            modRegions = newModRegions;
        }    
        
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            modRegions = oldModRegions;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Restored old regions\n" + modRegions); // NOI18N
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            run();
        }
        
    }
}
