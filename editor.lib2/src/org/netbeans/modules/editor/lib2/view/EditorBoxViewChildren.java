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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.text.Position;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;

/**
 * Class that manages children of {@link EditorBoxView}.
 * <br/>
 * 
 * @author Miloslav Metelka
 */

public class EditorBoxViewChildren extends GapList<EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorBoxViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorBoxViewChildren.class.getName());

    /**
     * Number of child views above which they will start to be managed
     * in a gap-storage way upon modification.
     * Below the threshold the views are updated without gap creation.
     */
    private static final int GAP_STORAGE_THRESHOLD = 10;

    private static final long serialVersionUID  = 0L;

    GapStorage gapStorage; // 28 bytes = 24-super + 4
    
    EditorBoxViewChildren(int capacity) {
        super(capacity);
    }

    /**
     * @return true if the code should update view's raw start offsets (highlights views)
     *  or false if not (paragraph views).
     */
    protected boolean rawOffsetUpdate() {
        return false;
    }

    /**
     * Whether TabableView instances should be handled specially.
     *
     * @return true if TabableView children should be treated specially.
     */
    protected boolean handleTabableViews() {
        return false;
    }

    /**
     * @see {@link EditorBoxView#replace(int, int, javax.swing.text.View[], int, java.awt.Shape, float)}
     */
    public EditorBoxView.ReplaceResult replace(EditorBoxView boxView, EditorBoxView.ReplaceResult result,
            int index, int removeCount, EditorView[] addedViews,
            int offsetDelta, Shape alloc)
    {
        boolean modified = false;
        int viewCount = size();
        if (index < 0) {
            throw new IllegalArgumentException("index=" + index + " < 0"); // NOI18N
        }
        if (removeCount < 0) {
            throw new IllegalArgumentException("removeCount=" + removeCount + " < 0"); // NOI18N
        }
        if (index + removeCount > viewCount) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" +
                    removeCount + ", viewCount=" + viewCount); // NOI18N
        }
        boolean removedTillEnd = (index + removeCount == viewCount);
        moveStorage(index + removeCount); // Includes moveGap()
        double visualOffset = getViewVisualOffset(boxView, index);
        double removedSpan;
        int gapIndexDelta = removeCount;
        if (removeCount != 0) { // Removing at least one item => index < size
            // Update visual offsets
            removedSpan = getViewVisualOffset(boxView, index + removeCount) - visualOffset;
            remove(index, removeCount);
            modified = true;
        } else {
            removedSpan = 0d;
        }
        double addedSpan = 0d;
        float minorAxisChildrenSpan = getMinorAxisChildrenSpan(boxView);
        boolean minorAxisChildrenSpanChange = false;
        int majorAxis = boxView.getMajorAxis();
        if (addedViews != null && addedViews.length != 0) {
            modified = true;
            gapIndexDelta += addedViews.length;
            addArray(index, addedViews);
            int boxViewStartOffset = boxView.getStartOffset();
            int minorAxis = ViewUtils.getOtherAxis(majorAxis);
            boolean supportsRawOffsetUpdate = rawOffsetUpdate();
            double viewVisualOffset = visualOffset;
            TabExpander tabExpander = boxView.getTabExpander();
            for (int i = 0; i < addedViews.length; i++) {
                EditorView view = addedViews[i];
                if (supportsRawOffsetUpdate) {
                    int offset = view.getRawOffset();
                    // Below gap => do not use offsetGapLength
                    view.setRawOffset(offset - boxViewStartOffset);
                }
                // First assign parent to the view and then ask for preferred span.
                // This way the view may get necessary info from its parent regarding its preferred span.
                view.setParent(boxView);
                float majorSpan;
                if (handleTabableViews() && view instanceof TabableView) {
                    majorSpan = ((TabableView)view).getTabbedSpan((float)viewVisualOffset, tabExpander);
                } else {
                    majorSpan = view.getPreferredSpan(majorAxis);
                }
                float minorSpan = view.getPreferredSpan(minorAxis);
                // Below gap => do not use visualGapLength
                view.setRawVisualOffset(viewVisualOffset);
                viewVisualOffset += majorSpan;
                addedSpan += majorSpan;
                if (minorSpan > minorAxisChildrenSpan) {
                    minorAxisChildrenSpan = minorSpan;
                    minorAxisChildrenSpanChange = true;
                }
            }
        }
        if (gapStorage != null) {
            gapStorage.gapIndex += gapIndexDelta;
        }
        boolean majorAxisChildrenSpanChange = (addedSpan != removedSpan);
        if (majorAxisChildrenSpanChange || offsetDelta != 0) {
            // Fix both visual and textual offsets in one iteration through children
            fixOffsetsAndMajorSpan(boxView, index + addedViews.length, offsetDelta, addedSpan - removedSpan);
        }
        if (minorAxisChildrenSpanChange) {
            setMinorAxisChildrenSpan(boxView, minorAxisChildrenSpan);
        }
        if (modified) {
            updateSpans(boxView, result, index, removeCount, addedViews.length,
                    majorAxisChildrenSpanChange, visualOffset,
                    addedSpan, removedSpan, removedTillEnd,
                    minorAxisChildrenSpanChange, alloc
            );
        } // Otherwise the repaint bounds and other vars in result stay unfilled
        return result;
    }

    protected double getMajorAxisChildrenSpan(EditorBoxView boxView) {
        return boxView.getMajorAxisSpan();
    }

    protected void setMajorAxisChildrenSpan(EditorBoxView boxView, double majorAxisSpan) {
        boxView.setMajorAxisSpan(majorAxisSpan);
    }

    protected float getMinorAxisChildrenSpan(EditorBoxView boxView) {
        return boxView.getMinorAxisSpan();
    }

    protected void setMinorAxisChildrenSpan(EditorBoxView boxView, float minorAxisSpan) {
        boxView.setMinorAxisSpan(minorAxisSpan);
    }

    protected void updateSpans(EditorBoxView boxView, EditorBoxView.ReplaceResult result,
            int index, int removedCount, int addedCount,
            boolean majorAxisSpanChange, double visualOffset,
            double addedSpan, double removedSpan, boolean removedTillEnd,
            boolean minorAxisSpanChange, Shape alloc)
    {
        int majorAxis = boxView.getMajorAxis();
        if (alloc != null) {
            Rectangle2D.Double repaintBounds = ViewUtils.shape2Bounds(alloc);
            assert (repaintBounds.width >= 0) : "repaintBounds.width=" + repaintBounds.width;
            assert (repaintBounds.height >= 0) : "repaintBounds.height=" + repaintBounds.height + "; boxView=" + boxView +
                    ", addedSpan=" + addedSpan + ", removedSpan="  + removedSpan + ", visualOffset=" + visualOffset;
            if (majorAxis == View.X_AXIS) {
                repaintBounds.x += visualOffset;
                if (majorAxisSpanChange || removedTillEnd) {
                    result.widthChanged = true;
                    repaintBounds.width = (double) Integer.MAX_VALUE; // Extend to end
                } else { // Just repaint the modified area (of the same size)
                    repaintBounds.width = removedSpan;
                }
                if (minorAxisSpanChange) {
                    result.heightChanged = true;
                    repaintBounds.height = (double) Integer.MAX_VALUE; // Extend to end
                } // else: leave the repaintBounds.height set to alloc's height

            } else { // Y_AXIS is major axis
                repaintBounds.y += visualOffset;
                if (majorAxisSpanChange || removedTillEnd) {
                    result.heightChanged = true;
                    repaintBounds.height = (double) Integer.MAX_VALUE; // Extend to end
                } else { // Just repaint the modified area (of the same size)
                    repaintBounds.height = removedSpan;
                }
                if (minorAxisSpanChange) {
                    result.widthChanged = true;
                    repaintBounds.width = (double) Integer.MAX_VALUE; // Extend to end
                } // else: leave the repaintBounds.width set to alloc's width
            }
            result.repaintBounds = ViewUtils.toRect(repaintBounds);

        } else { // Null alloc => compatible operation
            if (majorAxisSpanChange || minorAxisSpanChange) {
                if (majorAxis == View.X_AXIS) {
                    boxView.preferenceChanged(null, majorAxisSpanChange, minorAxisSpanChange);
                } else {
                    boxView.preferenceChanged(null, minorAxisSpanChange, majorAxisSpanChange);
                }
            }
        }
    }

    int getViewIndex(int offset, Position.Bias bias) {
	if(bias == Position.Bias.Backward) {
	    offset -= 1;
	}
        return getViewIndex(offset);
    }

    int getViewIndex(int offset) {
        int high = size() - 1;
        if (high == -1) {
            return -1;
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = get(mid).getStartOffset();
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else { // element starts at offset
                return mid;
            }
        }
        return Math.max(high, 0);
    }

    int raw2RelOffset(int rawOffset) {
        return (gapStorage == null || rawOffset < gapStorage.offsetGapStart)
                ? rawOffset
                : rawOffset - gapStorage.offsetGapLength;
    }

    int relOffset2Raw(int offset) {
        return (gapStorage == null || offset < gapStorage.offsetGapStart)
                ? offset
                : offset + gapStorage.offsetGapLength;
    }

    int getLength() { // Total length of contained child views
        int size = size();
        if (size > 0) {
            EditorView lastChildView = get(size - 1);
            return raw2RelOffset(lastChildView.getRawOffset()) + lastChildView.getLength();
        } else {
            return 0;
        }
    }

    private double raw2VisualOffset(double rawVisualOffset) {
        return (gapStorage == null || rawVisualOffset < gapStorage.visualGapStart)
                ? rawVisualOffset
                : rawVisualOffset + gapStorage.visualGapLength;
    }

    final double getViewVisualOffset(EditorBoxView boxView, int index) {
        return (index == size())
                ? getMajorAxisChildrenSpan(boxView)
                : getViewVisualOffset(index);
    }

    final double getViewVisualOffset(int index) {
        return getViewVisualOffset(get(index).getRawVisualOffset());
    }

    final double getViewVisualOffset(double rawVisualOffset) {
        return raw2VisualOffset(rawVisualOffset);
    }

    final double getViewMajorAxisSpan(EditorBoxView boxView, int index) {
        return (index == size() - 1)
                ? getMajorAxisChildrenSpan(boxView) - getViewVisualOffset(index)
                : getViewVisualOffset(index + 1) - getViewVisualOffset(index);
    }

    Shape getChildAllocation(EditorBoxView boxView, int startIndex, int endIndex, Shape alloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        double visualOffset = getViewVisualOffset(startIndex);
        double endVisualOffset = (endIndex == size()) ? getMajorAxisChildrenSpan(boxView) : getViewVisualOffset(endIndex);
        if (boxView.getMajorAxis() == View.X_AXIS) {
            mutableBounds.x += visualOffset;
            mutableBounds.width = endVisualOffset - visualOffset;
            mutableBounds.height = boxView.getMinorAxisSpan();
        } else { // y is major axis
            mutableBounds.y += visualOffset;
            mutableBounds.height = endVisualOffset - visualOffset;
            mutableBounds.width = boxView.getMinorAxisSpan();
        }
        return mutableBounds;
    }

    /**
     * Find index of first view containing the given offset.
     *
     * @param offset offset of the element
     * @return index of the element. If there is no element with that
     *  index then the index of the next element (with the greater offset)
     *  (or size of the list) will be returned.
     *  <br>
     *  If there are multiple items with the same offset then the first one of them
     *  will be returned.
     */
    int getViewIndexFirst(int offset) {
        int high = size() - 1;
        if (high == -1) {
            return -1; // No items
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            int viewStartOffset = get(mid).getStartOffset();
            if (viewStartOffset < offset) {
                low = mid + 1;
            } else if (viewStartOffset > offset) {
                high = mid - 1;
            } else { // exact offset found at index
                while (mid > 0) {
                    mid--;
                    viewStartOffset = get(mid).getStartOffset();
                    if (viewStartOffset < offset) {
                        mid++;
                        break;
                    }
                }
                high = mid;
                break;
            }
        }
        return Math.max(high, 0);
    }

    private int getViewIndexFirst(double visualOffset) {
        int high = size() - 1;
        if (high == -1) {
            return -1; // No items
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            double viewVisualOffset = getViewVisualOffset(mid);
            if (viewVisualOffset < visualOffset) {
                low = mid + 1;
            } else if (viewVisualOffset > visualOffset) {
                high = mid - 1;
            } else { // exact offset found at index
                while (mid > 0) {
                    mid--;
                    viewVisualOffset = getViewVisualOffset(mid);
                    if (viewVisualOffset < visualOffset) {
                        mid++;
                        break;
                    }
                }
                high = mid;
                break;
            }
        }
        return Math.max(high, 0);
    }

    final void moveStorage(int index) {
        if (gapStorage == null) {
            if (false && size() > GAP_STORAGE_THRESHOLD) { // [TODO] Enable gap behavior
                gapStorage = new GapStorage(size());
                moveGap(index);
            }
        } else { // Existing gap storage
            moveGap(index);
        }
    }

    final void fixOffsetsAndMajorSpan(EditorBoxView boxView, int index, int offsetDelta, double visualDelta) {
        // Expects moveGap(index) was called already before calling of this method
        boolean visualUpdate = (visualDelta != 0d);
        if (gapStorage != null) {
            gapStorage.offsetGapLength -= offsetDelta;
            gapStorage.visualGapLength -= visualDelta;
            if (handleTabableViews()) {
                TabExpander tabExpander = boxView.getTabExpander();
                // Go though the rest of views and check if their span has changed
                int viewCount = size();
                for (int i = index; i < viewCount; i++) {
                    EditorView view = get(i);
                    if (visualUpdate) {
                        view.setRawVisualOffset(view.getRawVisualOffset() + visualDelta);
                    }
                    if (view instanceof TabableView) {
                        double origMajorSpan = boxView.getViewMajorAxisSpan(index);
                        float visualOffset = (float) boxView.getViewVisualOffset(index);
                        double majorSpan = ((TabableView)view).getTabbedSpan(visualOffset, tabExpander);
                        if (majorSpan != origMajorSpan) {
                            visualDelta += (majorSpan - origMajorSpan);
                            visualUpdate = true;
                        }
                    }
                }

            }
        } else { // Move the items one by one
            boolean offsetUpdate = rawOffsetUpdate() && (offsetDelta != 0);
            int viewCount = size();
            for (int i = index; i < viewCount; i++) {
                EditorView view = get(i);
                if (offsetUpdate) {
                    view.setRawOffset(view.getRawOffset() + offsetDelta);
                }
                if (visualUpdate) {
                    view.setRawVisualOffset(view.getRawVisualOffset() + visualDelta);
                }
            }
        }
        if (visualUpdate) {
            setMajorAxisChildrenSpan(boxView, getMajorAxisChildrenSpan(boxView) + visualDelta);
        }
    }

    private void moveGap(int index) {
        if (index != gapStorage.gapIndex) {
            boolean supportsRawOffsetUpdate = rawOffsetUpdate();
            if (index < gapStorage.gapIndex) {
                int lastOffset = 0;
                double lastVisualOffset = 0d;
                for (int i = gapStorage.gapIndex - 1; i >= index; i--) {
                    EditorView view = get(i);
                    if (supportsRawOffsetUpdate) {
                        lastOffset = view.getRawOffset() + gapStorage.offsetGapLength;
                        view.setRawOffset(lastOffset);
                    }
                    lastVisualOffset = view.getRawVisualOffset() + gapStorage.visualGapLength;
                    view.setRawVisualOffset(lastVisualOffset);
                }
                if (supportsRawOffsetUpdate) {
                    gapStorage.offsetGapStart = lastOffset;
                }
                gapStorage.visualGapStart = lastVisualOffset;

            } else { // index > gapStorage.gapIndex
                for (int i = gapStorage.gapIndex; i < index; i++) {
                    EditorView view = get(i);
                    view.setRawVisualOffset(view.getRawVisualOffset() - gapStorage.visualGapLength);
                    if (supportsRawOffsetUpdate) {
                        view.setRawOffset(view.getRawOffset() - gapStorage.offsetGapLength);
                    }
                }
                if (index < size()) { // Gap moved to existing view
                    EditorView view = get(index);
                    if (supportsRawOffsetUpdate) {
                        gapStorage.offsetGapStart = view.getRawOffset() - gapStorage.offsetGapLength;
                    }
                    gapStorage.visualGapStart = view.getRawVisualOffset() - gapStorage.visualGapLength;
                } else { // Gap above at end of all existing views => make gap starts high enough to eliminate translation
                    assert (index == size()) : "Invalid requested index=" + index + // NOI18N
                            ", size()=" + size() + ", gapIndex=" + gapStorage.gapIndex; // NOI18N
                    if (supportsRawOffsetUpdate) {
                        gapStorage.offsetGapStart = GapStorage.INITIAL_OFFSET_GAP_LENGTH;
                    }
                    gapStorage.visualGapStart = GapStorage.INITIAL_VISUAL_GAP_LENGTH;
                }
            }
            gapStorage.gapIndex = index;
        }
    }


    public int getViewIndexAtPoint(EditorBoxView boxView, double x, double y, Shape alloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        x -= mutableBounds.x;
        y -= mutableBounds.y;

        int high = size() - 1;
        if (high == -1) {
            return -1;
        }
        int low = 0;
        double visualOffset = (boxView.getMajorAxis() == View.X_AXIS) ? x : y;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVisualOffset = getViewVisualOffset(mid);
            if (midVisualOffset < visualOffset) {
                low = mid + 1;
            } else if (midVisualOffset > visualOffset) {
                high = mid - 1;
            } else {
                // view starting exactly at the given visual offset found
                return mid;
            }
        }
        return Math.max(high, 0);
    }

    public Shape modelToViewChecked(EditorBoxView boxView, int offset, Shape alloc, Position.Bias bias) {
        int index = getViewIndex(offset, bias);
        if (index >= 0) { // When at least one child the index will fit one of them
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // Forward to the child view
            EditorView child = get(index);
            // Update the bounds with child.modelToView()
            return child.modelToViewChecked(offset, childAlloc, bias);
        } else { // No children => fallback by leaving the given bounds
            return alloc;
        }
    }

    public int viewToModelChecked(EditorBoxView boxView, double x, double y, Shape alloc, Position.Bias[] biasReturn) {
        int index = getViewIndexAtPoint(boxView, x, y, alloc);
        int offset;
        if (index >= 0) {
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // forward to the child view
            EditorView view = get(index);
            offset = view.viewToModelChecked(x, y, childAlloc, biasReturn);
        } else { // at the end
            offset = boxView.getStartOffset();
        }
        return offset;
    }

    protected void paint(EditorBoxView boxView, Graphics2D g, Shape alloc, Rectangle clipBounds) {
        int index;
        int endIndex;
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        if (clipBounds.contains(allocBounds)) { // Full rendering
            index = 0;
            endIndex = size();
        } else { // Only portion
            Rectangle2D.Double mutableBounds = (Rectangle2D.Double) allocBounds.clone();
            Rectangle2D.intersect(mutableBounds, clipBounds, mutableBounds);
            if (!mutableBounds.isEmpty()) {
                // Compute lower and higher bounds
                int majorAxis = boxView.getMajorAxis();
                double visualOffset;
                double endVisualOffset;
                if (majorAxis == View.X_AXIS) {
                    visualOffset = mutableBounds.x;
                    endVisualOffset = visualOffset + mutableBounds.width;
                } else {
                    visualOffset = mutableBounds.y;
                    endVisualOffset = visualOffset + mutableBounds.height;
                }
                index = Math.max(getViewIndexFirst(visualOffset), 0); // Cover no-children case
                endIndex = getViewIndexFirst(endVisualOffset) + 1;
            } else {
                index = 0;
                endIndex = 0;
            }
        }

        while (index < endIndex) {
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            EditorView view = get(index);
            view.paint(g, childAlloc, clipBounds);
            index++;
        }
    }

    /**
     * Append debugging info.
     *
     * @param sb non-null string builder
     * @param indent &gt;=0 indentation in spaces.
     * @param importantIndex either an index of child that is important to describe in the output
     *  (Initial and ending two displayed plus two before and after the important index).
     *  Or -1 to display just starting and ending two. Or -2 to display all children.
     * @return
     */
    public StringBuilder appendChildrenInfo(EditorBoxView boxView, StringBuilder sb, int indent, int importantIndex) {
        int viewCount = size();
        int digitCount = ArrayUtilities.digitCount(viewCount);
        int importantLastIndex = -1; // just be < 0
        int childImportantIndex = (importantIndex == -2) ? -2 : -1;
        for (int i = 0; i < viewCount; i++) {
            sb.append('\n');
            ArrayUtilities.appendSpaces(sb, indent);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            EditorView view = get(i);
            view.appendViewInfo(sb, indent, childImportantIndex);
            boolean appendDots = false;
            if (i == 4) { // After showing first 5 items => possibly skip to important index
                if (importantIndex == -1) { // Display initial five
                    if (i < viewCount - 6) { // -6 since i++ will follow
                        appendDots = true;
                        i = viewCount - 6;
                    }
                } else if (importantIndex >= 0) {
                    importantLastIndex = importantIndex + 3;
                    importantIndex = importantIndex - 3;
                    if (i < importantIndex - 1) {
                        appendDots = true;
                        i = importantIndex - 1;
                    }
                } // otherwise importantIndex == -2 to display every child
            } else if (i == importantLastIndex) {
                if (i < viewCount - 6) { // -6 since i++ will follow
                    appendDots = true;
                    i = viewCount - 6;
                }
            }
            if (appendDots) {
                sb.append('\n');
                ArrayUtilities.appendSpaces(sb, indent);
                sb.append("...");
            }
        }
        return sb;
    }

    /**
     * Gap storage speeds up operations when a number of children views exceeds
     */
    static final class GapStorage {

        /**
         * Length of the visual gap in child view infos along their major axis.
         * Initial length of the gap is Integer.MAX_VALUE which assuming 20pt font height
         * should allow for sufficient 107,374,182 lines.
         */
        static final double INITIAL_VISUAL_GAP_LENGTH = Integer.MAX_VALUE;

        static final int INITIAL_OFFSET_GAP_LENGTH = (Integer.MAX_VALUE >> 1);

        GapStorage(int gapIndex) {
            this.gapIndex = gapIndex;
        }

        /**
         * Start of the visual gap in child views along their major axis.
         */
        double visualGapStart = INITIAL_VISUAL_GAP_LENGTH; // 8-super + 8 = 16 bytes

        double visualGapLength = INITIAL_VISUAL_GAP_LENGTH; // 16 + 8 = 24 bytes

        /**
         * Start of the offset gap used for managing start offsets of HighlightsView views.
         * It is not used for paragraph views.
         */
        int offsetGapStart = INITIAL_OFFSET_GAP_LENGTH; // 24 + 4 = 28 bytes

        int offsetGapLength = INITIAL_OFFSET_GAP_LENGTH; // 28 + 4 = 32 bytes

        /**
         * Index of the gap in the contained children.
         */
        int gapIndex;

    }

}
