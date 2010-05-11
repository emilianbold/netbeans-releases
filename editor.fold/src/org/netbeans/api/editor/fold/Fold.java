/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.api.editor.fold;

import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.modules.editor.fold.FoldOperationImpl;
import org.netbeans.modules.editor.fold.FoldChildren;
import org.netbeans.modules.editor.fold.FoldUtilitiesImpl;
import org.openide.ErrorManager;

/**
 * Fold is a building block of the code folding tree-based hierarchy.
 * <br>
 * Folds cannot overlap but they can be nested arbitrarily.
 * <br>
 * It's possible to determine the fold's type, description
 * and whether it is collapsed or expanded
 * and explore the nested (children) folds.
 * <br>
 * There are various useful utility methods for folds in the {@link FoldUtilities}.
 *
 * <p>
 * There is one <i>root fold</i> at the top of the code folding hierarchy.
 * <br>
 * The root fold is special uncollapsable fold covering the whole document.
 * <br>
 * It can be obtained by using {@link FoldHierarchy#getRootFold()}.
 * <br>
 * The regular top-level folds are children of the root fold.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class Fold {
    
    private static final Fold[] EMPTY_FOLD_ARRAY = new Fold[0];
    
    private static final String DEFAULT_DESCRIPTION = "..."; // NOI18N
    
    private final FoldOperationImpl operation;

    private final FoldType type;
    
    private boolean collapsed;
    
    private String description;
    
    private Fold parent;
    
    private FoldChildren children;
    
    private int rawIndex;
    
    private int startGuardedLength;
    private int endGuardedLength;
    
    private Position startPos;
    private Position endPos;
    
    private Position guardedEndPos;
    private Position guardedStartPos;
    
    private Object extraInfo;
    

    Fold(FoldOperationImpl operation,
    FoldType type, String description, boolean collapsed,
    Document doc, int startOffset, int endOffset,
    int startGuardedLength, int endGuardedLength,
    Object extraInfo)
    throws BadLocationException {

        if (startGuardedLength < 0) {
            throw new IllegalArgumentException("startGuardedLength=" // NOI18N
                + startGuardedLength + " < 0"); // NOI18N
        }
        if (endGuardedLength < 0) {
            throw new IllegalArgumentException("endGuardedLength=" // NOI18N
                + endGuardedLength + " < 0"); // NOI18N
        }
        if (startOffset >= endOffset) {
            throw new IllegalArgumentException("startOffset=" + startOffset + " >= endOffset=" + endOffset); // NOI18N
        }
        if ((endOffset - startOffset) < (startGuardedLength + endGuardedLength)) {
            throw new IllegalArgumentException("(endOffset=" + endOffset // NOI18N
                + " - startOffset=" + startOffset + ") < " // NOI18N
                + "(startGuardedLength=" + startGuardedLength // NOI18N
                + " + endGuardedLength=" + endGuardedLength + ")" // NOI18N
            ); // NOI18N
        }
        
        this.operation = operation;
        this.type = type;

        this.collapsed = collapsed;
        this.description = description;

        this.startPos = doc.createPosition(startOffset);
        this.endPos = doc.createPosition(endOffset);

        this.startGuardedLength = startGuardedLength;
        this.endGuardedLength = endGuardedLength;
        
        this.extraInfo = extraInfo;
        
        // Must assign guarded areas positions
        // Even if the particular guarded area is zero the particular inner area
        // has at least 1 character to detect changes leading
        // to automatic forced expanding of the fold.
        updateGuardedStartPos(doc, startOffset);
        updateGuardedEndPos(doc, endOffset);

    }

    /**
     * Get type of this fold.
     *
     * @return non-null type identification of this fold.
     */
    public FoldType getType() {
        return type;
    }
    
    /**
     * Get parent fold of this fold.
     *
     * @return parent fold of this fold or null if this is root fold or if this
     *  fold was removed from the code folding hierarchy.
     *  <br>
     *  {@link FoldUtilities#isRootFold(Fold)} can be used to check
     *  whether this is root fold.
     */
    public Fold getParent() {
    	return parent;
    }
    
    void setParent(Fold parent) {
        if (isRootFold()) {
            throw new IllegalArgumentException("Cannot set parent on root"); // NOI18N
        } else {
            this.parent = parent;
        }
    }
    /**
     * Get the code folding hierarchy for which this fold was created.
     *
     * @return non-null code folding hierarchy for which this fold was constructed.
     */
    public FoldHierarchy getHierarchy() {
    	return operation.getHierarchy();
    }
    
    FoldOperationImpl getOperation() {
        return operation;
    }
    
    /**
     * Check whether this fold is currently a part of the hierarchy.
     * <br>
     * The fold may be temporarily removed from the hierarchy because
     * it became blocked by another fold. Once the blocking fold gets
     * removed the original fold becomes a part of the hierarchy again.
     *
     * @return true if the fold is actively a part of the hierarchy.
     */
/*    public boolean isHierarchyPart() {
    	return (getParent() != null) || isRootFold();
    }
 */
    
    boolean isRootFold() {
        return (operation.getManager() == null);
    }
    
    /**
     * Get an absolute starting offset of this fold in the associated document.
     * <br>
     * The starting offset is expected to track possible changes in the underlying
     * document (i.e. it's maintained
     * in {@link javax.swing.text.Position}-like form).
     *
     * @return &gt;=0 absolute starting offset of this fold in the document.
     */
    public int getStartOffset() {
        return (isRootFold()) ? 0 : startPos.getOffset();
    }
    
    void setStartOffset(Document doc, int startOffset)
    throws BadLocationException {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set endOffset of root fold"); // NOI18N
        } else {
            this.startPos = doc.createPosition(startOffset);
            updateGuardedStartPos(doc, startOffset);
        }
    }

    /**
     * Get an absolute ending offset of this fold in the associated document.
     * <br>
     * The ending offset is expected to track possible changes in the underlying
     * document (i.e. it's maintained
     * in {@link javax.swing.text.Position}-like form).
     *
     * @return <code>&gt;=getStartOffset()</code>
     *  offset of the first character in the document that is not part
     *  of this fold.
     */
    public int getEndOffset() {
        return isRootFold()
            ? operation.getHierarchy().getComponent().getDocument().getLength()
            : endPos.getOffset();
    }
    
    void setEndOffset(Document doc, int endOffset)
    throws BadLocationException {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set endOffset of root fold"); // NOI18N
        } else {
            this.endPos = doc.createPosition(endOffset);
            updateGuardedEndPos(doc, endOffset);
        }
    }

    /**
     * Return whether this fold is collapsed or expanded.
     * <br>
     * To collapse fold {@link FoldHierarchy#collapse(Fold)}
     * can be used.
     *
     * @return true if this fold is collapsed or false if it's expanded.
     */
    public boolean isCollapsed() {
    	return collapsed;
    }
    
    void setCollapsed(boolean collapsed) {
        if (isRootFold()) {
            throw new IllegalStateException("Cannot set collapsed flag on root fold."); // NOI18N
        }
        this.collapsed = collapsed;
    }

    /**
     * Get text description that should be displayed when the fold
     * is collapsed instead of the text contained in the fold.
     * <br>
     * If there is no specific description the "..." is returned.
     *
     * @return non-null description of the fold.
     */
    public String getDescription() {
    	return (description != null) ? description : DEFAULT_DESCRIPTION;
    }
    
    void setDescription(String description) {
    	this.description = description;
    }

    /**
     * Get total count of child folds contained in this fold.
     *
     * @return count of child folds contained in this fold.
     *  Zero means there are no children folds under this fold.
     */
    public int getFoldCount() {
    	return (children != null) ? children.getFoldCount() : 0;
    }

    /**
     * Get child fold of this fold at the given index.
     *
     * @param index &gt;=0 &amp;&amp; &lt;{@link #getFoldCount()}
     *  index of child of this fold.
     */
    public Fold getFold(int index) {
        if (children != null) {
            return children.getFold(index);
        } else { // no children exist
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
            + " but no children exist."); // NOI18N
        }
    }
    
    Fold[] foldsToArray(int index, int count) {
        if (children != null) {
            return children.foldsToArray(index, count);
        } else { // no children
            if (count == 0) {
                return EMPTY_FOLD_ARRAY;
            } else { // invalid count
                throw new IndexOutOfBoundsException("No children but count=" // NOI18N
                    + count);
            }
        }
    }

    /**
     * Remove the given folds and insert them as children
     * of the given fold which will be put to their place.
     *
     * @param index index at which the starts the area of child folds to wrap.
     * @param length number of child folds to wrap.
     * @param fold fold to insert at place of children. The removed children
     *  become children of the fold.
     */
    void extractToChildren(int index, int length, Fold fold) {
        if (fold.getFoldCount() != 0 || fold.getParent() != null) {
            throw new IllegalStateException();
        }
        if (length != 0) { // create FoldChildren instance for the extracted folds
            fold.setChildren(children.extractToChildren(index, length, fold));
        } else { // no children to extract -> insert the single child
            if (children == null) {
                children = new FoldChildren(this);
            }
            children.insert(index, fold); // insert the single child fold
        }
    }

    /**
     * Remove the fold at the given index
     * and put its children at its place.
     *
     * @param index index at which the child should be removed
     * @return the removed child at the index.
     */
    Fold replaceByChildren(int index) {
        Fold fold = getFold(index);
        FoldChildren foldChildren = fold.getChildren();
        fold.setChildren(null);
        children.replaceByChildren(index, foldChildren);
        return fold;
    }
    
    private FoldChildren getChildren() {
        return children;
    }
    
    private void setChildren(FoldChildren children) {
        this.children = children;
    }
    
    Object getExtraInfo() {
        return extraInfo;
    }

    /**
     * Get index of the given child fold in this fold.
     * <br>
     * The method has constant-time performance.
     *
     * @param child non-null child fold of this fold but in general
     *  it can be any non-null fold (see return value).
     * @return &gt;=0 index of the child fold in this fold
     *  or -1 if the given child fold is not a child of this fold.
     */
    public int getFoldIndex(Fold child) {
        return (children != null) ? children.getFoldIndex(child) : -1;
    }
    
    private void updateGuardedStartPos(Document doc, int startOffset) throws BadLocationException {
        if (!isRootFold()) {
            int guardedStartOffset = isZeroStartGuardedLength()
                ? startOffset + 1
                : startOffset + startGuardedLength;
            this.guardedStartPos = doc.createPosition(guardedStartOffset);
        }
    }

    private void updateGuardedEndPos(Document doc, int endOffset) throws BadLocationException {
        if (!isRootFold()) {
            int guardedEndOffset = isZeroEndGuardedLength()
                ? endOffset - 1
                : endOffset - endGuardedLength;
            this.guardedEndPos = doc.createPosition(guardedEndOffset);
        }
    }

    private boolean isZeroStartGuardedLength() {
        return (startGuardedLength == 0);
    }
    
    private boolean isZeroEndGuardedLength() {
        return (endGuardedLength == 0);
    }
    
    private int getGuardedStartOffset() {
        return isRootFold() ? getStartOffset() : guardedStartPos.getOffset();
    }
    
    private int getGuardedEndOffset() {
        return isRootFold() ? getEndOffset() : guardedEndPos.getOffset();
    }
    
    void insertUpdate(DocumentEvent evt) {
        if (evt.getOffset() + evt.getLength() == getGuardedStartOffset()) {
             // inserted right at the end of the guarded area
            try {
                updateGuardedStartPos(evt.getDocument(), getStartOffset());
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    void removeUpdate(DocumentEvent evt) {
        try {
            if (getStartOffset() == getGuardedStartOffset()) {
                updateGuardedStartPos(evt.getDocument(), getStartOffset());
            }
            if (getEndOffset() == getGuardedEndOffset()) {
                updateGuardedEndPos(evt.getDocument(), getEndOffset());
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Return true if the starting guarded area is damaged by a document modification.
     */
    boolean isStartDamaged() {
        return (!isZeroStartGuardedLength() // no additional check if zero guarded length
                && (getInnerStartOffset() - getStartOffset() != startGuardedLength));
    }
    
    /**
     * Return true if the ending guarded area is damaged by a document modification.
     */
    boolean isEndDamaged() {
        return (!isZeroEndGuardedLength() // no additional check if zero guarded length
                && (getEndOffset() - getInnerEndOffset() != endGuardedLength));
    }
    
    boolean isExpandNecessary() {
        // Only operate in case when isZero*() methods return true
        // because if not the fold would already be marked as damaged
        // and removed (isDamaged*() gets asked prior to this one).
        return (isZeroStartGuardedLength() && (getStartOffset() == getGuardedStartOffset()))
            || (isZeroEndGuardedLength() && (getEndOffset() == getGuardedEndOffset()));
    }
    
    /**
     * Get the position where the inner part of the fold starts
     * (and the initial guarded area ends).
     */
    private int getInnerStartOffset() {
        return isZeroStartGuardedLength() ? getStartOffset() : getGuardedStartOffset();
    }

    /**
     * Get the position where the inner part of the fold ends
     * (and the ending guarded area starts).
     */
    private int getInnerEndOffset() {
        return isZeroEndGuardedLength() ? getEndOffset() : getGuardedEndOffset();
    }

    /**
     * Get the raw index of this fold in the parent.
     * <br>
     * The SPI clients should never call this method.
     */
    int getRawIndex() {
        return rawIndex;
    }
    
    /**
     * Set the raw index of this fold in the parent.
     * <br>
     * The SPI clients should never call this method.
     */
    void setRawIndex(int rawIndex) {
        this.rawIndex = rawIndex;
    }
    
    /**
     * Update the raw index of this fold in the parent by a given delta.
     * <br>
     * The SPI clients should never call this method.
     */
    void updateRawIndex(int rawIndexDelta) {
        this.rawIndex += rawIndexDelta;
    }
    

    public String toString() {
        return FoldUtilitiesImpl.foldToString(this) + ", [" + getInnerStartOffset() // NOI18N
            + ", " + getInnerEndOffset() + "] {" // NOI18N
            + getGuardedStartOffset() + ", " // NOI18N
            + getGuardedEndOffset() + '}'; // NOI18N
    }
    
}
