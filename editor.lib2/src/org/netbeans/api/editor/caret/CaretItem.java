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
package org.netbeans.api.editor.caret;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Logger;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.CheckForNull;

/**
 * A single caret inside {@link EditorCaret} handled internally by EditorCaret.
 * <br>
 * For API client methods {@link CaretInfo} class is used. There is one-to-one reference
 * between caret item and caret info. But the info is immutable so once the caret item
 * gets mutated its corresponding caret info becomes obsolete and new info gets created
 * lazily.
 *
 * @author Miloslav Metelka
 */
final class CaretItem implements Comparable {
    
    // -J-Dorg.netbeans.modules.editor.lib2.CaretItem.level=FINEST
    private static final Logger LOG = Logger.getLogger(CaretItem.class.getName());

    private static final int TRANSACTION_MARK_REMOVED = 1;
    
    private static final int UPDATE_VISUAL_BOUNDS = 2;

    private final EditorCaret editorCaret;
    
    private Position dotPos;

    private Position markPos;

    private Point magicCaretPosition;
    
    /**
     * Last info or null if info became obsolete and should be recomputed.
     */
    private CaretInfo info;
    
    private Rectangle caretBounds;
    
    /**
     * Hint of index of this caret item in replaceCaretItems in transaction context.
     */
    private int transactionIndexHint;
    
    /**
     * Transaction uses this flag to mark this item for removal.
     */
    private int statusBits;

    CaretItem(EditorCaret editorCaret, Position dotPos, Position markPos) {
        this.editorCaret = editorCaret;
        this.dotPos = dotPos;
        this.markPos = markPos;
        this.statusBits = UPDATE_VISUAL_BOUNDS; // Request visual bounds updating automatically
    }
    
    EditorCaret editorCaret() {
        return editorCaret;
    }
    
    void ensureValidInfo() {
        // No explicit locking - locking managed by EditorCaret
        if (info == null) {
            info = new CaretInfo(this);
        }
    }
    
    void clearInfo() {
        // No explicit locking - locking managed by EditorCaret
        this.info = null;
    }

    CaretInfo getValidInfo() {
        ensureValidInfo();
        return info;
    }

//    void clearTransactionInfo() {
//        // No explicit locking - locking managed by EditorCaret
//        this.transactionInfo = null;
//    }
//
//    CaretInfo getValidTransactionInfo() {
//        if (transactionInfo == null) {
//            transactionInfo = new CaretInfo(this);
//        }
//        return transactionInfo;
//    }
    
    /**
     * Get position of the caret itself.
     *
     * @return non-null position of the caret placement. The position may be
     * virtual so methods in {@link org.netbeans.api.editor.document.ShiftPositions} may be used if necessary.
     */
    @CheckForNull
    Position getDotPosition() {
        return dotPos;
    }

    /**
     * Return either the same object like {@link #getDotPosition()} if there's
     * no selection or return position denoting the other end of an existing
     * selection (which is either before or after the dot position depending of
     * how the selection was created).
     *
     * @return non-null position of the caret placement. The position may be
     * virtual so methods in {@link org.netbeans.api.editor.document.ShiftPositions} may be used if necessary.
     */
    @CheckForNull
    Position getMarkPosition() {
        return markPos;
    }

    int getDot() {
        return (dotPos != null) ? dotPos.getOffset() : 0;
    }

    int getMark() {
        return (markPos != null) ? markPos.getOffset() : 0;
    }

    /**
     * @return true if there's a selection or false if there's no selection for
     * this caret.
     */
    boolean isSelection() {
        return (dotPos != null && markPos != null &&
                markPos != dotPos && dotPos.getOffset() != markPos.getOffset());
    }
    
    boolean isSelectionShowing() {
        return editorCaret.isSelectionVisible() && isSelection();
    }

    Position getSelectionStart() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }

    Position getSelectionEnd() {
        return dotPos; // TBD - possibly inspect virtual columns etc.
    }

    Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    void setDotPos(Position dotPos) {
        this.dotPos = dotPos;
    }

    void setMarkPos(Position markPos) {
        this.markPos = markPos;
    }

    void setMagicCaretPosition(Point newMagicCaretPosition) { // [TODO] move to transaction context
        this.magicCaretPosition = newMagicCaretPosition;
    }

    void setCaretBounds(Rectangle newCaretBounds) {
        this.caretBounds = newCaretBounds;
    }

    Rectangle getCaretBounds() {
        return this.caretBounds;
    }

    int getTransactionIndexHint() {
        return transactionIndexHint;
    }

    void setTransactionIndexHint(int transactionIndexHint) {
        this.transactionIndexHint = transactionIndexHint;
    }

    void markTransactionMarkRemoved() {
        this.statusBits |= TRANSACTION_MARK_REMOVED;
    }

    boolean isTransactionMarkRemoved() {
        return (this.statusBits & TRANSACTION_MARK_REMOVED) != 0;
    }

    void clearTransactionMarkRemoved() {
        this.statusBits &= ~TRANSACTION_MARK_REMOVED;
    }
    
    void markUpdateVisualBounds() {
        this.statusBits |= UPDATE_VISUAL_BOUNDS;
    }
    
    boolean isUpdateVisualBounds() {
        return (this.statusBits & UPDATE_VISUAL_BOUNDS) != 0;
    }
    
    void clearUpdateVisualBounds() {
        this.statusBits &= ~UPDATE_VISUAL_BOUNDS;
    }
    
    @Override
    public int compareTo(Object o) {
        return getDot() - ((CaretItem)o).getDot();
    }

    @Override
    public String toString() {
        return "dotPos=" + dotPos + ", markPos=" + markPos + ", magicCaretPosition=" + magicCaretPosition; // NOI18N
    }

}
