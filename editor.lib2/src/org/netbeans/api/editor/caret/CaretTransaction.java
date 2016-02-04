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
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.ShiftPositions;
import org.netbeans.lib.editor.util.GapList;

/**
 * Context passed to getCaret transaction allowing to create/remove/modify the carets during the transaction.
 *
 * @author Miloslav Metelka
 */
final class CaretTransaction {
    
    private final EditorCaret editorCaret;
    
    private final JTextComponent component;
    
    private final Document doc;
    
    /**
     * Original items held here mainly due to latter repaints of the removed items.
     */
    private final GapList<CaretItem> origCaretItems;
    
    private int modIndex;
    
    /**
     * For MOVE_HANDLER and CARETS_REMOVE this is end index of modified area.
     * For CARETS_ADD and MIXED this is not filled.
     */
    private int modEndIndex;
    
    private CaretItem[] addCaretItems;
    
    private int affectedStartIndex = Integer.MAX_VALUE;

    private int affectedEndIndex;

    private int affectedStartOffset = Integer.MAX_VALUE;
    
    private int affectedEndOffset;
    
    private boolean addOrRemove;
    
    private boolean dotOrMarkChanged;
    
    private GapList<CaretItem> replaceItems;
    
    private GapList<CaretItem> replaceSortedItems;
    
    private GapList<CaretItem> extraRemovedItems;
    
    private int[] indexes;
    
    private int indexesLength;
    
    /**
     * End of area where index hints were updated.
     */
    private int indexHintEnd;
    
    private boolean fullResort;
    
    
    CaretTransaction(EditorCaret caret, JTextComponent component, Document doc) {
        this.editorCaret = caret;
        this.component = component;
        this.doc = doc;
        this.origCaretItems = editorCaret.getCaretItems();
    }
    

    @NonNull EditorCaret getCaret() {
        return editorCaret;
    }
    
    @NonNull JTextComponent getComponent() {
        return component;
    }
    
    @NonNull Document getDocument() {
        return doc;
    }
    
    boolean isAnyChange() {
        return addOrRemove || dotOrMarkChanged;
    }
    
    boolean moveDot(@NonNull CaretItem caret, @NonNull Position dotPos) {
        return setDotAndMark(caret, dotPos, caret.getMarkPosition());
    }

    boolean setDotAndMark(@NonNull CaretItem caretItem, @NonNull Position dotPos, @NonNull Position markPos) {
        assert (dotPos != null) : "dotPos must not be null";
        assert (markPos != null) : "markPos must not be null";
        int index = findCaretItemIndex(origCaretItems, caretItem);
        if (index != -1) {
            Position origDotPos = caretItem.getDotPosition();
            Position origMarkPos = caretItem.getMarkPosition();
            boolean changed = false;
            if (origDotPos == null || ShiftPositions.compare(dotPos, origDotPos) != 0) {
                caretItem.setDotPos(dotPos);
                changed = true;
            }
            if (origMarkPos == null || ShiftPositions.compare(markPos, origMarkPos) != 0) {
                caretItem.setMarkPos(markPos);
                changed = true;
            }
            if (changed) {
                updateAffectedIndexes(index, index + 1);
                caretItem.markUpdateVisualBounds();
                caretItem.clearInfo();
                dotOrMarkChanged = true;
            }
            return changed;
        }
        return false;
        //caret.setDotCaret(offset, this, true);
    }
    
    boolean setMagicCaretPosition(@NonNull CaretItem caretItem, Point p) {
        int index = findCaretItemIndex(origCaretItems, caretItem);
        if (index != -1) {
            caretItem.setMagicCaretPosition(p);
            caretItem.clearInfo();
            updateAffectedIndexes(index, index + 1);
            return true;
        }
        return false;
    }
    
    void documentInsertAtZeroOffset(int insertEndOffset) {
        // Nested insert inside active transaction for caret moving
        // Since carets may already be moved - do the operations with CaretItems directly
        Position insertEndPos = null;
        for (CaretItem caretItem : editorCaret.getSortedCaretItems()) {
            Position dotPos = caretItem.getDotPosition();
            boolean modifyDot = (dotPos == null || dotPos.getOffset() == 0);
            Position markPos = caretItem.getMarkPosition();
            boolean modifyMark = (markPos == null || markPos.getOffset() == 0);
            if (modifyDot || modifyMark) {
                if (insertEndPos == null) {
                    try {
                        insertEndPos = doc.createPosition(insertEndOffset);
                    } catch (BadLocationException ex) {
                        // Should never happen
                        return;
                    }
                }
                if (modifyDot) {
                    dotPos = insertEndPos;
                }
                if (modifyMark) {
                    markPos = insertEndPos;
                }
                setDotAndMark(caretItem, dotPos, markPos);
            }
            // Do not break the loop when caret's pos is above zero offset
            // since the carets may be already moved during the transaction
            // - possibly to offset zero. But there could be optimization
            // at least scan position of only the caret items that were modified and not others.
        }
        if (insertEndPos != null) {
            updateAffectedOffsets(0, insertEndOffset); // TODO isn't this extra work that setDotAndMark() already did??
            fullResort = true;
        }
    }
    
    void documentRemove(int offset) {
        fullResort = true; // TODO modify to more specific update
    }

    private void setSelectionStartEnd(@NonNull CaretItemInfo info, @NonNull Position pos, boolean start) {
        int index = findCaretItemIndex(origCaretItems, info.caretItem);
        assert (index != -1) : "Index=" + index + " should be valid";
        if (start == info.dotAtStart) {
            info.caretItem.setDotPos(pos);
        } else {
            info.caretItem.setMarkPos(pos);
        }
        updateAffectedIndexes(index, index + 1);
    }
    
    void handleCaretRemove(@NonNull CaretInfo caret) {
        
    }

    GapList<CaretItem> getReplaceItems() {
        return replaceItems;
    }
    
    GapList<CaretItem> getSortedCaretItems() {
        return replaceSortedItems;
    }
    
    List<CaretInfo> getOriginalCarets() {
        // Current impl ignores possible replaceItems content since client transactions only operate over
        // original infos from editorCaret. Internal transactions know the type of transaction
        // that was performed so they will skip carets possibly removed by the transaction.
        return editorCaret.getCarets();
    }

    List<CaretInfo> getOriginalSortedCarets() {
        // Current impl ignores possible replaceItems content - see getOriginalCarets()
        return editorCaret.getSortedCarets();
    }

    void replaceCarets(RemoveType removeType, int offset, CaretItem[] addCaretItems) {
        int size = origCaretItems.size();
        switch (removeType) {
            case NO_REMOVE:
                break;
            case REMOVE_LAST_CARET:
                if (size > 1) {
                    modIndex = size - 1;
                    modEndIndex = size;
                    addOrRemove = true;
                }
                break;
            case RETAIN_LAST_CARET:
                if (size > 1) {
                    modEndIndex = size - 1;
                    addOrRemove = true;
                }
                break;
            case REMOVE_ALL_CARETS:
                if (size > 0) {
                    modEndIndex = size;
                    addOrRemove = true;
                }
                break;
            case DOCUMENT_REMOVE:
                break;
            case DOCUMENT_INSERT_ZERO_OFFSET:
                documentInsertAtZeroOffset(offset);
                break;

            default:
                throw new AssertionError("Unhandled removeType=" + removeType); // NOI18N
        }
        if (addCaretItems != null) {
            this.addCaretItems = addCaretItems;
            addOrRemove = true;
        }
    }
    
    void runCaretMoveHandler(CaretMoveHandler handler) {
        CaretMoveContext context = new CaretMoveContext(this);
        handler.moveCarets(context);
    }
    
    private static int getOffset(Position pos) {
        return (pos != null) ? pos.getOffset() : 0;
    }
    
    void removeOverlappingRegions() {
        removeOverlappingRegions(0, Integer.MAX_VALUE);
    }

    void removeOverlappingRegions(int removeOffset) {
        removeOverlappingRegions(0, removeOffset); // TODO compute startIndex by binary search
    }
    
    void removeOverlappingRegions(int startIndex, int stopOffset) {
        if (addOrRemove) {
            initReplaceItems();
        } else if (dotOrMarkChanged) {
            initReplaceItems(); // TODO optimize for low number of changed items
        }
        GapList<CaretItem> origSortedItems = (replaceSortedItems != null)
                ? replaceSortedItems
                : editorCaret.getSortedCaretItems();
        int origSortedItemsSize = origSortedItems.size();
        GapList<CaretItem> nonOverlappingItems = null;
        int copyStartIndex = 0;
        int i = startIndex - 1;
        boolean itemsRemoved = false;
        CaretItemInfo lastInfo = new CaretItemInfo();
        if (i >= 0) {
            lastInfo.update(origSortedItems.get(i));
        } // Otherwise leave the default zeros in lastInfo
        
        CaretItemInfo itemInfo = new CaretItemInfo();
        while (++i < origSortedItemsSize) {
            itemInfo.update(origSortedItems.get(i));
            if (itemInfo.overlapsAtStart(lastInfo)) {
                if (nonOverlappingItems == null) {
                    nonOverlappingItems = new GapList<CaretItem>(origSortedItemsSize - 1); // At least one will be skipped
                }
                itemsRemoved = true;
                // Determine type of overlap
                if (!lastInfo.dotAtStart) { // Caret of lastInfo moved into next block
                    if (lastInfo.startsBelow(itemInfo)) {
                        // Extend selection of itemInfo to start of lastInfo
                        updateAffectedOffsets(lastInfo.startOffset, itemInfo.startOffset);
                        setSelectionStartEnd(itemInfo, lastInfo.startPos, true);
                    }
                    // Remove lastInfo's getCaret item
                    lastInfo.caretItem.markTransactionMarkRemoved();
                    origSortedItems.copyElements(copyStartIndex, i - 1, nonOverlappingItems);
                    copyStartIndex = i;
                    
                } else { // Remove itemInfo and set selection of lastInfo to end of itemInfo
                    if (itemInfo.endsAbove(lastInfo)) {
                        updateAffectedOffsets(lastInfo.endOffset, itemInfo.endOffset);
                        setSelectionStartEnd(lastInfo, itemInfo.endPos, false);
                    }
                    // Remove itemInfo's getCaret item
                    itemInfo.caretItem.markTransactionMarkRemoved();
                    origSortedItems.copyElements(copyStartIndex, i, nonOverlappingItems);
                    copyStartIndex = i + 1;
                }
            } else { // No overlapping
                // Swap the items to reuse original lastInfo
                CaretItemInfo tmp = lastInfo;
                lastInfo = itemInfo;
                itemInfo = tmp;
                if (lastInfo.endOffset > stopOffset) {
                    break;
                }
            }
        }

        if (itemsRemoved) { // At least one item removed
            if (copyStartIndex < origSortedItemsSize) {
                origSortedItems.copyElements(copyStartIndex, origSortedItemsSize, nonOverlappingItems);
            }
            GapList<CaretItem> origItems = resultItems();
            int origItemsSize = origItems.size();
            replaceItems = new GapList<>(origItemsSize);
            for (i = 0; i < origItemsSize; i++) {
                CaretItem caretItem = origItems.get(i);
                if (caretItem.isTransactionMarkRemoved()) {
                    caretItem.clearTransactionMarkRemoved();
                    if (extraRemovedItems == null) {
                        extraRemovedItems = new GapList<>();
                    }
                    extraRemovedItems.add(caretItem);
                } else {
                    replaceItems.add(caretItem);
                }
            }
            replaceSortedItems = nonOverlappingItems;
        }
    }
    
    GapList<CaretItem> addRemovedItems(GapList<CaretItem> toItems) {
        int removeSize = modEndIndex - modIndex;
        int extraRemovedSize = (extraRemovedItems != null) ? extraRemovedItems.size() : 0;
        if (removeSize + extraRemovedSize > 0) {
            if (toItems == null) {
                toItems = new GapList<>(removeSize + extraRemovedSize);
            }
            if (removeSize > 0) {
                toItems.addAll(origCaretItems, modIndex, removeSize);
            }
            if (extraRemovedSize > 0) {
                toItems.addAll(extraRemovedItems);
            }
        }
        return toItems;
    }
    
    GapList<CaretItem> addUpdateVisualBoundsItems(GapList<CaretItem> toItems) {
        GapList<CaretItem> items = resultItems();
        int size = items.size();
        for (int i = 0; i < size; i++) {
            CaretItem caretItem = items.get(i);
            if (caretItem.isUpdateVisualBounds()) {
                caretItem.clearUpdateVisualBounds();
                if (toItems == null) {
                    toItems = new GapList<>();
                }
                toItems.add(caretItem);
            }
        }
        return toItems;
    }

    private GapList<CaretItem> resultItems() {
        return (replaceItems != null) ? replaceItems : origCaretItems;
    }

    private void initReplaceItems() {
        assert (replaceItems == null) : "replaceItems already inited to " + replaceItems; // NOI18N
        int size = origCaretItems.size();
        int removeSize = modEndIndex - modIndex;
        int addSize = (addCaretItems != null) ? addCaretItems.length : 0;
        int newSize = size - removeSize + addSize;
        replaceItems = new GapList<>(newSize);
        if (removeSize > 0) {
            replaceItems.addAll(origCaretItems, 0, modIndex);
            replaceItems.addAll(origCaretItems, modEndIndex, size - modEndIndex);
        } else {
            replaceItems.addAll(origCaretItems);
        }
        if (addCaretItems != null) {
            replaceItems.addArray(replaceItems.size(), addCaretItems);
        }

        assert (replaceItems.size() == newSize);
        boolean updateIndividual = (removeSize + addSize) < (newSize >> 2); // Threshold 1/4 of total size for full resort
        if (fullResort || true) { // Force full resort
            replaceSortedItems = replaceItems.copy();
            if (newSize > 1) {
                
            }
        } else { // Partial resort TODO
            
        }
    }

    private void resetIndexes() {
        indexesLength = 0;
    }
    
    private void addToIndexes(int index) {
        if (indexes == null) {
            indexes = new int[8];
        } else if (indexesLength == indexes.length) {
            int[] orig = indexes;
            indexes = new int[indexesLength << 1];
            System.arraycopy(orig, 0, indexes, 0, indexesLength);
        }
        indexes[indexesLength++] = index;
    }
    
    
    private int findCaretItemIndex(GapList<CaretItem> caretItems, CaretItem caretItem) {
        // Method only resolves existing items not added items
        int i = caretItem.getTransactionIndexHint();
        int size = caretItems.size();
        if (i >= size || caretItems.get(i) != caretItem) {
            while (indexHintEnd < size) {
                CaretItem c = caretItems.get(indexHintEnd);
                c.setTransactionIndexHint(indexHintEnd++);
                if (c == caretItem) {
                    return indexHintEnd - 1;
                }
            }
            return -1;
        }
        return i;
    }
    
    private void updateAffectedIndexes(int startIndex, int endIndex) {
        if (affectedStartIndex == Integer.MAX_VALUE) {
            affectedStartIndex = startIndex;
            affectedEndIndex = endIndex;
        } else {
            affectedStartIndex = Math.min(affectedStartIndex, startIndex);
            affectedEndIndex = Math.max(affectedEndIndex, endIndex);
        }
    }

    private void updateAffectedOffsets(int startOffset, int endOffset) {
        if (affectedStartOffset == Integer.MAX_VALUE) { // Affected range not inited yet
            affectedStartOffset = startOffset;
            affectedEndOffset = endOffset;
        } else { // Affected range already inited
            if (startOffset < affectedStartOffset) {
                affectedStartOffset = startOffset;
            }
            if (endOffset > affectedEndOffset) {
                affectedEndOffset = endOffset;
            }
        }
    }

    static CaretItem[] asCaretItems(EditorCaret caret, @NonNull List<Position> dotAndSelectionStartPosPairs) {
        int size = dotAndSelectionStartPosPairs.size();
        if ((size & 1) != 0) {
            throw new IllegalStateException("Passed list has size=" + size + " which is not an even number.");
        }
        CaretItem[] addedCarets = new CaretItem[size >> 1];
        int listIndex = 0;
        for (int j = 0; j < addedCarets.length; j++) {
            Position dotPos = dotAndSelectionStartPosPairs.get(listIndex++);
            Position selectionStartPos = dotAndSelectionStartPosPairs.get(listIndex++);
            CaretItem caretItem = new CaretItem(caret, dotPos, selectionStartPos);
            addedCarets[j] = caretItem;
        }
        return addedCarets;
    }

    enum RemoveType {
        NO_REMOVE,
        REMOVE_LAST_CARET,
        RETAIN_LAST_CARET,
        REMOVE_ALL_CARETS,
        DOCUMENT_REMOVE,
        DOCUMENT_INSERT_ZERO_OFFSET
    }
    
    /**
     * Helper class for resolving overlapping getCaret selections.
     */
    private static final class CaretItemInfo {
        
        CaretItem caretItem;
        
        Position startPos;
        
        Position endPos;

        int startOffset;

        int startShift;
        
        int endOffset;

        int endShift;
        
        boolean dotAtStart;
        
        void update(CaretItem caret) {
            this.caretItem = caret;
            Position dotPos = caret.getDotPosition();
            if (dotPos != null) {
                int dotOffset = dotPos.getOffset();
                int dotShift = ShiftPositions.getShift(dotPos);
                Position markPos = caret.getMarkPosition();
                if (markPos != null && markPos != dotPos) { // Still they may be equal which means no selection
                    int markOffset = markPos.getOffset();
                    int markShift = ShiftPositions.getShift(markPos);
                    if (markOffset < dotOffset || (markOffset == dotOffset && markShift <= dotShift)) {
                        startPos = markPos;
                        endPos = dotPos;
                        startOffset = markOffset;
                        startShift = markShift;
                        endOffset = dotOffset;
                        endShift = dotShift;
                        dotAtStart = false;
                    } else {
                        startPos = dotPos;
                        endPos = markPos;
                        startOffset = dotOffset;
                        startShift = dotShift;
                        endOffset = markOffset;
                        endShift = markShift;
                        dotAtStart = true;
                    }
                } else {
                    startPos = endPos = dotPos;
                    startOffset = endOffset = dotOffset;
                    startShift = startShift = dotShift;
                    dotAtStart = false;
                }
            } else {
                clear();
            }
        }

        private void clear() {
            caretItem = null;
            startPos = endPos = null;
            startOffset = endOffset = 0;
            startShift = startShift = 0;
            dotAtStart = false;
        }
        
        private boolean overlapsAtStart(CaretItemInfo info) {
            return (ShiftPositions.compare(info.endOffset, info.endShift,
                    startOffset, startShift) > 0);
        }
        
        private boolean startsBelow(CaretItemInfo info) {
            return (ShiftPositions.compare(startOffset, startShift,
                    info.startOffset, info.startShift) < 0);
        }
        
        private boolean endsAbove(CaretItemInfo info) {
            return (ShiftPositions.compare(endOffset, endShift,
                    info.endOffset, info.endShift) > 0);
        }
        
    }

}
