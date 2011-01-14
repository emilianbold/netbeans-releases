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

package org.netbeans.modules.editor.lib2.view;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
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

public class EditorBoxViewChildren<V extends EditorView> extends GapList<V> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorBoxViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorBoxViewChildren.class.getName());

    /**
     * Repaint bounds that extend to end of component. Using just MAX_VALUE
     * for width/height caused problems since it probably overflowed
     * inside AWT code when added to positive x/y so ">> 1" is done for now.
     */
    protected static final double EXTEND_TO_END = (double) (Integer.MAX_VALUE >> 1);
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
     * @see {@link EditorBoxView#replace(int, int, javax.swing.text.View[], int) }
     */
    VisualUpdate<V> replace(EditorBoxView<V> boxView,
            int index, int removeCount, View[] addedViews, int offsetDelta)
    {
        VisualUpdate<V> visualUpdate = new VisualUpdate<V>(boxView);
        if (index < 0) {
            throw new IllegalArgumentException("index=" + index + " < 0"); // NOI18N
        }
        if (removeCount < 0) {
            throw new IllegalArgumentException("removeCount=" + removeCount + " < 0"); // NOI18N
        }
        if (index + removeCount > size()) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" +
                    removeCount + ", viewCount=" + size()); // NOI18N
        }
        moveOffsetGap(index + removeCount);
        moveVisualGap(index + removeCount);
        // Assign visual offset BEFORE possible removal/addition of views is made
        // since the added views would NOT have the visual offset filled in yet.
        setVisualIndexAndOffset(boxView, visualUpdate, index);
        int gapIndexDelta = -removeCount;
        if (removeCount != 0) { // Removing at least one item => index < size
            remove(index, removeCount);
        }
        if (addedViews != null && addedViews.length != 0) {
            gapIndexDelta += addedViews.length;
            visualUpdate.endVisualIndex = index + addedViews.length;
            addArray(index, addedViews);
            int boxViewStartOffset = boxView.getStartOffset();
            boolean supportsRawOffsetUpdate = rawOffsetUpdate();
            for (int i = 0; i < addedViews.length; i++) {
                @SuppressWarnings("unchecked")
                V view = (V) addedViews[i];
                if (supportsRawOffsetUpdate) {
                    int offset = view.getRawOffset();
                    // Below gap => do not use offsetGapLength
                    view.setRawOffset(offset - boxViewStartOffset);
                }
                // First assign parent to the view and then ask for preferred span.
                // This way the view may get necessary info from its parent regarding its preferred span.
                view.setParent(boxView);
            }
        } else { // No added views
            visualUpdate.endVisualIndex = index;
        }
        if (gapStorage != null) {
            gapStorage.offsetGapIndex += gapIndexDelta;
            int relEndOffset;
            if (gapStorage.offsetGapIndex > 0) {
                EditorView lastView = boxView.getEditorView(gapStorage.offsetGapIndex - 1);
                relEndOffset = lastView.getRawOffset() + lastView.getLength();
            } else {
                relEndOffset = 0;
            }
            gapStorage.offsetGapStart = relEndOffset;
            gapStorage.visualGapIndex += gapIndexDelta;
            gapStorage.offsetGapLength -= offsetDelta;
        } else { // Move the items one by one
            if (rawOffsetUpdate() && (offsetDelta != 0)) {
                int viewCount = size(); // Refresh (changed by 
                for (int i = visualUpdate.endVisualIndex; i < viewCount; i++) {
                    V view = get(i);
                    view.setRawOffset(view.getRawOffset() + offsetDelta);
                }
            }
        }
        // Update boxView's length to actual length of children.
        // It cannot be done relatively by just adding offsetDelta to original boxView's length
        // since box views with unitialized children already have proper length
        // so later children initialization would double the boxView's length.
        int newLength = getLength();
        if (newLength != boxView.getLength()) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(boxView.getDumpId() + ": update length: " + // NOI18N
                        boxView.getLength() + " => " + newLength + "\n"); // NOI18N
            }
            boxView.setLength(newLength);
        }
        return visualUpdate;
    }
    
    void updateSpansAndLayout(EditorBoxView<V> boxView, VisualUpdate<V> visualUpdate, Shape alloc) {
        fixSpans(boxView, visualUpdate);
        // Update various spans affected by preceding changes.
        updateLayout(boxView, visualUpdate, alloc);
    }
    
    VisualUpdate<V> updateViews(EditorBoxView<V> boxView, int index, int count, Shape alloc) {
        VisualUpdate<V> visualUpdate = new VisualUpdate<V>(boxView);
        moveVisualGap(index + count);
        setVisualIndexAndOffset(boxView, visualUpdate, index);
        visualUpdate.endVisualIndex = index + count;
        updateSpansAndLayout(boxView, visualUpdate, alloc);
        return visualUpdate;
    }

    private void setVisualIndexAndOffset(EditorBoxView<V> boxView, VisualUpdate<V> visualUpdate, int index) {
        // Adjacent child views on a line (highlight views) can be affected
        // if they share the same text layout with the removed child views.
        // Final determination can only be done once the added views are in-place and their font can be examined.
        // Check handleTabableViews() to determine whether handle joined text layouts (paragraph views)
        if (handleTabableViews()) {
            index = HighlightsViewUtils.findAffectedLayoutIndex(boxView, visualUpdate, index);
        }
        visualUpdate.visualIndex = index;
        visualUpdate.visualOffset = getViewVisualOffset(boxView, index);
    }
        
    private void fixSpans(EditorBoxView<V> boxView, VisualUpdate<V> visualUpdate) {
        // Go through affected children and recompute spans and visual offsets.
        // For lines the only affected children are the added ones but for intra-line views
        // a TextLayout may span multiple views so affected children may precede/follow the added ones.
        boolean handleTabableViews = handleTabableViews();
        int majorAxis = boxView.getMajorAxis();
        int minorAxis = ViewUtils.getOtherAxis(majorAxis);
        TabExpander tabExpander = handleTabableViews ? boxView.getTabExpander() : null;
        float minorAxisChildrenSpan = getMinorAxisChildrenSpan(boxView);
        double visualOffset = visualUpdate.visualOffset;
        assert (visualUpdate.visualIndex <= visualUpdate.endVisualIndex) :
            "visualIndex=" + visualUpdate.visualIndex +
            " > endVisualIndex=" + visualUpdate.endVisualIndex; // NOI18N
        if (handleTabableViews) {
            HighlightsViewUtils.fixLayouts(boxView, visualUpdate);
        }
        
        for (int i = visualUpdate.visualIndex; i < visualUpdate.endVisualIndex; i++) {
            V view = get(i);
            // First assign parent to the view and then ask for preferred span.
            // This way the view may get necessary info from its parent regarding its preferred span.
            float majorSpan;
            if (handleTabableViews) {
                if (view instanceof TabableView) {
                    majorSpan = ((TabableView) view).getTabbedSpan((float) visualOffset, tabExpander);
                } else {
                    majorSpan = view.getPreferredSpan(majorAxis);
                }
            } else { // Not tabable view
                majorSpan = view.getPreferredSpan(majorAxis);
            }
            // Below gap => do not use visualGapLength
            view.setRawVisualOffset(visualOffset);
            visualOffset += majorSpan;
            
            float viewMinorAxisSpan = view.getPreferredSpan(minorAxis);
            if (viewMinorAxisSpan > minorAxisChildrenSpan) {
                visualUpdate.markMinorChildrenSpanChanged();
                minorAxisChildrenSpan = viewMinorAxisSpan;
            }
        }
        if (visualUpdate.isMinorChildrenSpanChanged()) {
            setMinorAxisChildrenSpan(boxView, minorAxisChildrenSpan);
        }
        visualUpdate.endVisualOffset = visualOffset;
        // Add span of created views to visual delta
        double visualDelta = visualOffset - boxView.getViewVisualOffset(visualUpdate.endVisualIndex);
        if (visualDelta != 0d) {
            visualUpdate.markMajorChildrenSpanChanged();
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer(boxView.getDumpId() + "=>fixSpans(): vUpdate: " + visualUpdate +
                    "\nvDelta=" + visualDelta + " = vOffset=" + visualOffset + // NOI18N
                    " - vOffset[" + visualUpdate.endVisualIndex + "]=" + // NOI18N
                    boxView.getViewVisualOffset(visualUpdate.endVisualIndex) +
                    "\n"); // NOI18N
        }
        assert (visualUpdate.visualIndex <= visualUpdate.endVisualIndex) :
            "visualIndex=" + visualUpdate.visualIndex +
            " > endVisualIndex=" + visualUpdate.endVisualIndex; // NOI18N

        // Fix upper visual offsets
        boolean tabsChanged = false;
        int viewCount = size();
        if (gapStorage != null) {
            gapStorage.visualGapIndex = visualUpdate.endVisualIndex;
            gapStorage.visualGapStart = visualOffset;
            gapStorage.visualGapLength -= visualDelta;
            // When using gap storage fix the total span now since by updating visualGapLength
            // all the visual offsets get updated so the total span should be fixed now too
            // for boxView.getViewVisualOffset() to work properly.
            setMajorAxisChildrenSpan(boxView, getMajorAxisChildrenSpan(boxView) + visualDelta);
            if (handleTabableViews()) {
                double tabVisualDelta = 0d;
                // Go though the rest of views and check if their span has changed
                for (int i = visualUpdate.endVisualIndex; i < viewCount; i++) {
                    V view = get(i);
                    if (tabsChanged) {
                        view.setRawVisualOffset(view.getRawVisualOffset() + tabVisualDelta);
                    }
                    if (view instanceof TabableView) { // Must re-measure tab-view's span since it depends on x-coordinate.
                        // All indices are above visual gap (index is right above gap).
                        // visualGapLength is already updated so the x-coordinate reflects the update.
                        // visualOffset was just shifted by tabVisualDelta (but view[i+1] not yet).
                        double viewVisualOffset = view.getRawVisualOffset() - gapStorage.visualGapLength;
                        // add tabVisualDelta to next-view-VisualOffset since visualOffset already includes tabVisualDelta
                        double origMajorSpan = (boxView.getViewVisualOffset(i + 1) + tabVisualDelta) - viewVisualOffset;
                        double majorSpan = ((TabableView) view).getTabbedSpan((float) viewVisualOffset, tabExpander);
                        double majorSpanDelta = majorSpan - origMajorSpan;
                        if (majorSpanDelta != 0d) {
                            tabVisualDelta += majorSpanDelta;
                            tabsChanged = true;
                        }
                    }
                }
                if (tabsChanged) {
                    setMajorAxisChildrenSpan(boxView, getMajorAxisChildrenSpan(boxView) + tabVisualDelta);
                }
            }
        } else { // Move the items one by one
            for (int i = visualUpdate.endVisualIndex; i < viewCount; i++) {
                V view = get(i);
                view.setRawVisualOffset(view.getRawVisualOffset() + visualDelta);
                // Must possibly re-measure tab-view's span since it depends on x-coordinate.
                // Unlike when gap is active here both visualDelta shift and tab-remeasure shifts are joined
                if (handleTabableViews() && view instanceof TabableView) {
                    visualOffset = view.getRawVisualOffset();
                    // Use difference of visual offsets since it is most precise (doubles subtracting)
                    // and getPreferredSpan() could be expensive.
                    // add visualDelta to nextViewVisualOffset since visualOffset already includes visualDelta
                    double origMajorSpan = (boxView.getViewVisualOffset(i + 1) + visualDelta) - visualOffset;
                    double majorSpan = ((TabableView) view).getTabbedSpan((float) visualOffset, tabExpander);
                    double majorSpanDelta = majorSpan - origMajorSpan;
                    if (majorSpanDelta != 0d) {
                        visualDelta += majorSpanDelta;
                        tabsChanged = true;
                    }
                }
            }
            if (visualDelta != 0d) {
                setMajorAxisChildrenSpan(boxView, getMajorAxisChildrenSpan(boxView) + visualDelta);
                visualUpdate.markMajorChildrenSpanChanged();
            }
        }

        if (tabsChanged) {
            visualUpdate.markTabsChanged();
            visualUpdate.markMajorChildrenSpanChanged();
        }
    }

    protected double getMajorAxisChildrenSpan(EditorBoxView<V> boxView) {
        return boxView.getMajorAxisSpan();
    }

    protected void setMajorAxisChildrenSpan(EditorBoxView<V> boxView, double majorAxisSpan) {
        boxView.setMajorAxisSpan(majorAxisSpan);
    }

    protected float getMinorAxisChildrenSpan(EditorBoxView<V> boxView) {
        return boxView.getMinorAxisSpan();
    }

    protected void setMinorAxisChildrenSpan(EditorBoxView<V> boxView, float minorAxisSpan) {
        boxView.setMinorAxisSpan(minorAxisSpan);
    }

    protected void updateLayout(EditorBoxView<V> boxView, VisualUpdate<V> visualUpdate, Shape alloc) {
        int majorAxis = boxView.getMajorAxis();
        if (alloc != null) {
            Rectangle2D.Double repaintBounds = ViewUtils.shape2Bounds(alloc);
            assert (repaintBounds.width >= 0) : "repaintBounds.width=" + repaintBounds.width;
            assert (repaintBounds.height >= 0) : "repaintBounds.height=" + repaintBounds.height + "; boxView=" + boxView;
            if (majorAxis == View.X_AXIS) {
                repaintBounds.x += visualUpdate.visualOffset;
                if (visualUpdate.isMajorChildrenSpanChanged()) {
                    visualUpdate.markWidthChanged();
                    repaintBounds.width = EXTEND_TO_END;
                } else {
                    if (visualUpdate.isTabsChanged()) {
                        // Can happen even without total children span change.
                        // Repaint till end.
                        repaintBounds.width = getMajorAxisChildrenSpan(boxView) - visualUpdate.visualOffset;
                    } else { // Only repaint recomputed area
                        repaintBounds.width = visualUpdate.changedMajorSpan();
                    }
                }
                if (visualUpdate.isMinorChildrenSpanChanged()) {
                    visualUpdate.markHeightChanged();
                    repaintBounds.height = EXTEND_TO_END;
                } // else: leave the repaintBounds.height set to alloc's height

            } else { // Y_AXIS is major axis
                repaintBounds.y += visualUpdate.visualOffset;
                if (visualUpdate.isMajorChildrenSpanChanged()) {
                    visualUpdate.markHeightChanged();
                    repaintBounds.height = EXTEND_TO_END;
                } else { // Just repaint the modified area (of the same size)
                    repaintBounds.height = visualUpdate.changedMajorSpan();
                }
                if (visualUpdate.isMinorChildrenSpanChanged()) {
                    visualUpdate.markWidthChanged();
                    repaintBounds.width = EXTEND_TO_END;
                } // else: leave the repaintBounds.width set to alloc's width
            }
            visualUpdate.repaintBounds = ViewUtils.toRect(repaintBounds);

        } else { // Null alloc => compatible operation
            if (visualUpdate.isPreferenceChanged()) {
                if (majorAxis == View.X_AXIS) {
                    if (visualUpdate.isMajorChildrenSpanChanged())
                        visualUpdate.markWidthChanged();
                    if (visualUpdate.isMinorChildrenSpanChanged())
                        visualUpdate.markMinorChildrenSpanChanged();
                } else {
                    if (visualUpdate.isMinorChildrenSpanChanged())
                        visualUpdate.markWidthChanged();
                    if (visualUpdate.isMajorChildrenSpanChanged())
                        visualUpdate.markMinorChildrenSpanChanged();
                }
                boxView.preferenceChanged(null, visualUpdate.isWidthChanged(), visualUpdate.isHeightChanged());
            }
        }
    }

    /**
     * Get view at given index and if that view is a box view then make sure
     * its children are initialized.
     *
     * @param index
     * @return view with its children initialized.
     */
    protected V getEditorViewChildrenValid(EditorBoxView<V> boxView, int index) {
        V child = get(index);
        if (child instanceof EditorBoxView) {
            EditorBoxView<?> boxChild = (EditorBoxView<?>) child;
            if (boxChild.children == null) {
                boxView.initChildren(index, index + 1);
                // Reget the view since the rebuild could replace it
                child = get(index);
                assert (((EditorBoxView<?>)child).children != null);
            }
        }
        return child;
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
            V lastChildView = get(size - 1);
            return raw2RelOffset(lastChildView.getRawOffset()) + lastChildView.getLength();
        } else {
            return 0;
        }
    }

    private double raw2VisualOffset(double rawVisualOffset) {
        return (gapStorage == null || rawVisualOffset < gapStorage.visualGapStart)
                ? rawVisualOffset
                : rawVisualOffset - gapStorage.visualGapLength;
    }

    final double getViewVisualOffset(EditorBoxView<V> boxView, int index) {
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

    final double getViewMajorAxisSpan(EditorBoxView<V> boxView, int index) {
        return (index == size() - 1)
                ? getMajorAxisChildrenSpan(boxView) - getViewVisualOffset(index)
                : getViewVisualOffset(index + 1) - getViewVisualOffset(index);
    }

    Shape getChildAllocation(EditorBoxView<V> boxView, int startIndex, int endIndex, Shape alloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        double visualOffset = getViewVisualOffset(startIndex);
        double endVisualOffset = (endIndex == size())
                ? getMajorAxisChildrenSpan(boxView)
                : getViewVisualOffset(endIndex);
        if (boxView.getMajorAxis() == View.X_AXIS) {
            mutableBounds.x += visualOffset;
            mutableBounds.width = endVisualOffset - visualOffset;
            mutableBounds.height = getMinorAxisChildrenSpan(boxView);
        } else { // y is major axis
            mutableBounds.y += visualOffset;
            mutableBounds.height = endVisualOffset - visualOffset;
            mutableBounds.width = getMinorAxisChildrenSpan(boxView);
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

    void moveOffsetGap(int index) {
        if (!rawOffsetUpdate() || size() == 0) {
            return;
        }
        if (gapStorage == null) {
            if (size() > GAP_STORAGE_THRESHOLD) {
                gapStorage = new GapStorage(size());
            } else { // Not necessary to use gap-storage for small sizes
                return;
            }
        }
        checkGap();
        if (index != gapStorage.offsetGapIndex) {
            if (index < gapStorage.offsetGapIndex) {
                int lastOffset = 0;
                for (int i = gapStorage.offsetGapIndex - 1; i >= index; i--) {
                    V view = get(i);
                    lastOffset = view.getRawOffset();
                    view.setRawOffset(lastOffset + gapStorage.offsetGapLength);
                }
                gapStorage.offsetGapStart = lastOffset;

            } else { // index > gapStorage.offsetGapIndex
                for (int i = gapStorage.offsetGapIndex; i < index; i++) {
                    V view = get(i);
                    view.setRawOffset(view.getRawOffset() - gapStorage.offsetGapLength);
                }
                if (index < size()) { // Gap moved to existing view - the view is right above gap => subtract gap-lengths
                    V view = get(index);
                    gapStorage.offsetGapStart = view.getRawOffset() - gapStorage.offsetGapLength;
                } else {
                    // Gap above at end of all existing views => make gap starts high enough
                    // so that no offset/visual-offset is >= offsetGapStart/visualGapStart (no translation occurs)
                    assert (index == size()) : "Invalid requested index=" + index + // NOI18N
                            ", size()=" + size() + ", offsetGapIndex=" + gapStorage.offsetGapIndex; // NOI18N
                    gapStorage.offsetGapStart = GapStorage.INITIAL_OFFSET_GAP_LENGTH;
                }
            }
            gapStorage.offsetGapIndex = index;
        }
        checkGap();
    }
    
    private void moveVisualGap(int index) {
        checkGap();
        if (gapStorage != null && index != gapStorage.visualGapIndex) {
            if (index < gapStorage.visualGapIndex) {
                double lastVisualOffset = 0d;
                for (int i = gapStorage.visualGapIndex - 1; i >= index; i--) {
                    V view = get(i);
                    lastVisualOffset = view.getRawVisualOffset();
                    view.setRawVisualOffset(lastVisualOffset + gapStorage.visualGapLength);
                }
                gapStorage.visualGapStart = lastVisualOffset;

            } else { // index > gapStorage.visualGapIndex
                for (int i = gapStorage.visualGapIndex; i < index; i++) {
                    V view = get(i);
                    view.setRawVisualOffset(view.getRawVisualOffset() - gapStorage.visualGapLength);
                }
                if (index < size()) { // Gap moved to existing view - the view is right above gap => subtract gap-lengths
                    V view = get(index);
                    gapStorage.visualGapStart = view.getRawVisualOffset() - gapStorage.visualGapLength;
                } else {
                    // Gap above at end of all existing views => make gap starts high enough
                    // so that no offset/visual-offset is >= offsetGapStart/visualGapStart (no translation occurs)
                    assert (index == size()) : "Invalid requested index=" + index + // NOI18N
                            ", size()=" + size() + ", visualGapIndex=" + gapStorage.visualGapIndex; // NOI18N
                    gapStorage.visualGapStart = GapStorage.INITIAL_VISUAL_GAP_LENGTH;
                }
            }
            gapStorage.visualGapIndex = index;
        }
        checkGap();
    }

    private void checkGap() {
        if (gapStorage != null && LOG.isLoggable(Level.FINE)) {
            String error = null;
            int offsetGapIndex = gapStorage.offsetGapIndex;
            int visualGapIndex = gapStorage.visualGapIndex;
            if (offsetGapIndex > size()) {
                error = "offsetGapIndex=" + offsetGapIndex + " > size()=" + size(); // NOI18N
            } else {
                for (int i = 0; i < size(); i++) {
                    V view = get(i);
                    int rawOffset = view.getRawOffset();
                    int relOffset = raw2RelOffset(rawOffset);
                    double rawVisualOffset = view.getRawVisualOffset();
                    double visualOffset = raw2VisualOffset(rawVisualOffset);
                    // Check textual offset
                    if (rawOffsetUpdate()) {
                        if (i < offsetGapIndex) {
                            if (rawOffset >= gapStorage.offsetGapStart) {
                                error = "Not below offset-gap: rawOffset=" + rawOffset + // NOI18N
                                        " >= offsetGapStart=" + gapStorage.offsetGapStart; // NOI18N
                            }
                        } else { // Above gap
                            if (rawOffset < gapStorage.offsetGapStart) {
                                error = "Not above offset-gap: rawOffset=" + rawOffset + // NOI18N
                                        " < offsetGapStart=" + gapStorage.offsetGapStart; // NOI18N
                            }
                            if (i == offsetGapIndex) {
                                if (relOffset != gapStorage.offsetGapStart) {
                                    error = "relOffset=" + relOffset + " != gapStorage.offsetGapStart=" + // NOI18N
                                            gapStorage.offsetGapStart;
                                }
                            }

                        }
                    }
                    // Check visual offset
                    if (i < visualGapIndex) {
                        if (rawVisualOffset >= gapStorage.visualGapStart) {
                            error = "Not below visual-gap: rawVisualOffset=" + rawVisualOffset + // NOI18N
                                    " >= visualGapStart=" + gapStorage.visualGapStart; // NOI18N
                        }
                    } else { // Above gap
                        if (rawVisualOffset < gapStorage.visualGapStart) {
                            error = "Not above visual-gap: rawVisualOffset=" + rawVisualOffset + // NOI18N
                                    " < visualGapStart=" + gapStorage.visualGapStart; // NOI18N
                        }
                        if (i == visualGapIndex) {
                            if (visualOffset != gapStorage.visualGapStart) {
                                error = "visualOffset=" + visualOffset + " != gapStorage.visualGapStart=" + // NOI18N
                                        gapStorage.visualGapStart;
                            }
                        }

                    }
                    if (error != null) {
                        break;
                    }
                }
            }
            if (error != null) {
                throw new IllegalStateException("gapStorage INTEGRITY ERROR!!!\n" + error);
            }
        }
    }


    public int getViewIndexAtPoint(EditorBoxView<V> boxView, double x, double y, Shape alloc) {
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

    public Shape modelToViewChecked(EditorBoxView<V> boxView, int offset, Shape alloc, Position.Bias bias) {
        int index = getViewIndex(offset, bias);
        if (index >= 0) { // When at least one child the index will fit one of them
            // First find valid child (can lead to change of child allocation bounds)
            V view = getEditorViewChildrenValid(boxView, index);
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // Update the bounds with child.modelToView()
            return view.modelToViewChecked(offset, childAlloc, bias);
        } else { // No children => fallback by leaving the given bounds
            return alloc;
        }
    }

    public int viewToModelChecked(EditorBoxView<V> boxView, double x, double y, Shape alloc,
            Position.Bias[] biasReturn)
    {
        int index = getViewIndexAtPoint(boxView, x, y, alloc);
        int offset;
        if (index >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            V view = getEditorViewChildrenValid(boxView, index);
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // forward to the child view
            offset = view.viewToModelChecked(x, y, childAlloc, biasReturn);
        } else { // at the end
            offset = boxView.getStartOffset();
        }
        return offset;
    }

    public String getToolTipTextChecked(EditorBoxView<V> boxView, double x, double y, Shape alloc) {
        int index = getViewIndexAtPoint(boxView, x, y, alloc);
        if (index >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            V view = getEditorViewChildrenValid(boxView, index);
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // forward to the child view
            return view.getToolTipTextChecked(x, y, childAlloc);
        } else { // at the end
            return null;
        }
    }

    public JComponent getToolTip(EditorBoxView<V> boxView, double x, double y, Shape alloc) {
        int index = getViewIndexAtPoint(boxView, x, y, alloc);
        if (index >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            V view = getEditorViewChildrenValid(boxView, index);
            Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
            // forward to the child view
            return view.getToolTip(x, y, childAlloc);
        } else { // at the end
            return null;
        }
    }

    protected void paint(EditorBoxView<V> boxView, Graphics2D g, Shape alloc, Rectangle clipBounds) {
        int index;
        int endIndex;
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        // Cannot use (clipBounds.contains(allocBounds)) to check for full rendering
        // since sometimes the allocBounds come equal to the visible area size which would then lead
        // to initChildren() for all the line paragraph views in the document.
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
            paintChildren(boxView, g, alloc, clipBounds, index, endIndex);
        }
    }
    
    protected void paintChildren(EditorBoxView<V> boxView, Graphics2D g, Shape alloc, Rectangle clipBounds,
            int startIndex, int endIndex)
    {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("EBView.paintChildren(): <" + startIndex + "," + endIndex + ">\n");
        }
        boxView.initChildren(startIndex - 1, endIndex + 1); // Ensure valid children (plus extra 2 lines)
        while (startIndex < endIndex) {
            V view = getEditorViewChildrenValid(boxView, startIndex);
            Shape childAlloc = getChildAllocation(boxView, startIndex, startIndex + 1, alloc);
            view.paint(g, childAlloc, clipBounds);
            startIndex++;
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
    public StringBuilder appendChildrenInfo(EditorBoxView<V> boxView, StringBuilder sb, int indent, int importantIndex) {
        int viewCount = size();
        int digitCount = ArrayUtilities.digitCount(viewCount);
        int importantLastIndex = -1; // just be < 0
        int childImportantIndex = (importantIndex == -2) ? -2 : -1;
        for (int i = 0; i < viewCount; i++) {
            sb.append('\n');
            ArrayUtilities.appendSpaces(sb, indent);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            V view = get(i);
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

    protected StringBuilder appendViewInfoCore(StringBuilder sb, int indent, int importantChildIndex) {
        if (gapStorage != null) {
            gapStorage.appendInfo(sb);
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
            this.offsetGapIndex = gapIndex;
            this.visualGapIndex = gapIndex;
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
        int offsetGapIndex; // 32 + 4 = 36 bytes
        
        /**
         * Index of the visual gap in the contained children.
         */
        int visualGapIndex; // 36 + 4 = 40 bytes

        StringBuilder appendInfo(StringBuilder sb) {
            sb.append("<").append(offsetGapStart).append("|").append(offsetGapLength);
            sb.append(", vis<").append(visualGapStart).append("|").append(visualGapLength);
            return sb;
        }

        @Override
        public String toString() {
            return appendInfo(new StringBuilder(100)).toString();
        }

    }

}
