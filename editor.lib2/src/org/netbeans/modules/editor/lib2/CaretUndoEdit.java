/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.document.ComplexPositions;

/**
 * Undoable edit for caret.
 *
 * @author Miloslav Metelka
 */
class CaretUndoEdit extends AbstractUndoableEdit {
    
    final Document doc; // (16=super)+4=20 bytes
    
    /**
     * Offset of the dot to restore and last bit is a marker for undo/redo
     */
    private int dotOffset; // 24 bytes
    
    CaretUndoEdit(Document doc, int dotOffset) {
        if (doc == null) {
            throw new IllegalArgumentException("doc parameter must not be null"); // NOI18N
        }
        if (dotOffset < 0) {
            throw new IllegalStateException("Negative dotOffset=" + dotOffset + " not supported here"); // NOI18N
        }
        this.doc = doc;
        this.dotOffset = dotOffset;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        restoreCaret();
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        restoreCaret();
    }
    
    @Override
    public boolean isSignificant() {
        return super.isSignificant();
    }
    
    private void restoreCaret() {
        JTextComponent c = EditorRegistry.findComponent(doc);
        if (c != null) {
            Caret caret = c.getCaret();
            if (caret instanceof EditorCaret) {
                try {
                    restoreEditorCaret((EditorCaret) caret);
                } catch (BadLocationException ex) {
                    // Ignore caret restoration
                }
            } else {
                restoreLegacyCaret(caret);
            }
        }
    }

    protected void restoreEditorCaret(EditorCaret caret) throws BadLocationException {
        Position dotPos = doc.createPosition(getDotOffset());
        caret.replaceCarets(Arrays.asList(dotPos, dotPos));
    }
    
    protected void restoreLegacyCaret(Caret caret) {
        caret.setDot(getDotOffset());
    }

    int getDotOffset() {
        return dotOffset;
    }

    static final class ComplexEdit extends CaretUndoEdit {

        /**
         * Original offset of the mark to restore or -1 as a marker value
         * for the case when one or more positions are complex positions
         * - in such case the first item of the array is a split offset
         * for dotOffset and then (markOffset,markSplitOffset) etc.
         */
        int markOffset; // 28 bytes
        
        int[] extraDotAndMarkOffsets; // 32 bytes
        
        ComplexEdit(Document doc, int dotOffset, int markOffset, int[] extraDotAndMarkOffsets) {
            super(doc, dotOffset);
            this.markOffset = markOffset;
            this.extraDotAndMarkOffsets = extraDotAndMarkOffsets;
        }

        @Override
        protected void restoreEditorCaret(EditorCaret caret) throws BadLocationException {
            List<Position> dotAndMarkPosPairs;
            if (markOffset != -1) {
                Position dotPos = doc.createPosition(getDotOffset());
                Position markPos = doc.createPosition(markOffset);
                if (extraDotAndMarkOffsets != null) {
                    dotAndMarkPosPairs = new ArrayList<>(2 + extraDotAndMarkOffsets.length);
                    dotAndMarkPosPairs.add(dotPos);
                    dotAndMarkPosPairs.add(markPos);
                    for (int i = 0; i < extraDotAndMarkOffsets.length; i++) {
                        dotAndMarkPosPairs.add(doc.createPosition(extraDotAndMarkOffsets[i]));
                    }
                } else {
                    dotAndMarkPosPairs = Arrays.asList(dotPos, markPos);
                }
            } else { // one or more complex positions
                int offset = getDotOffset();
                int splitOffset = extraDotAndMarkOffsets[0];
                dotAndMarkPosPairs = new ArrayList((extraDotAndMarkOffsets.length + 1) >> 1);
                int i = 2;
                while (true) {
                    Position pos = doc.createPosition(offset);
                    pos = ComplexPositions.create(pos, splitOffset);
                    dotAndMarkPosPairs.add(pos);
                    if (i >= extraDotAndMarkOffsets.length) {
                        break;
                    }
                    offset = extraDotAndMarkOffsets[i++];
                    splitOffset = extraDotAndMarkOffsets[i++];
                }
            }
            caret.replaceCarets(dotAndMarkPosPairs);
        }

        @Override
        protected void restoreLegacyCaret(Caret caret) {
            int markOffset = this.markOffset;
            if (markOffset == -1) { // complex positions at time of undo edit creation
                markOffset = extraDotAndMarkOffsets[1];
            }
            caret.setDot(markOffset);
            caret.moveDot(getDotOffset());
        }
        
    }
    
}
