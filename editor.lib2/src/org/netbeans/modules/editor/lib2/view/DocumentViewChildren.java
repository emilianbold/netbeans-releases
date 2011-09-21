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
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsReader;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;

/**
 * Class that manages children of {@link EditorBoxView}.
 * <br/>
 * The class can manage offsets of children in case {@link #rawEndOffsetManaged()}
 * returns true. In such case each child must properly implement {@link EditorView#getRawEndOffset()}
 * and the maintained raw offsets are relative to corresponding box view's getStartOffset().
 * <br/>
 * Generally children of {@link #ParagraphView} manage their raw end offsets
 * while children of {@link #DocumentView} do not manage them (they use Position objects
 * to manage its start).
 * 
 * @author Miloslav Metelka
 */

public class DocumentViewChildren extends ViewChildren<ParagraphView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorBoxViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(DocumentViewChildren.class.getName());

    /**
     * Repaint bounds that extend to end of component. Using just MAX_VALUE
     * for width/height caused problems since it probably overflowed
     * inside AWT code when added to positive x/y so ">> 1" is done for now.
     */
    protected static final double EXTEND_TO_END = (double) (Integer.MAX_VALUE >> 1);

    private static final long serialVersionUID  = 0L;

    private ViewPaintHighlights viewPaintHighlights;
    
    private float childrenWidth;
    
    DocumentViewChildren(int capacity) {
        super(capacity);
    }
    
    float width() {
        return childrenWidth;
    }
    
    float height() {
        return (float) startVisualOffset(size());
    }

    /**
     * Replace paragraph views inside DocumentView.
     * <br/>
     * In case both removeCount == 0 and addedViews is empty this method does not need to be called.
     *
     * @param docView
     * @param index
     * @param removeCount
     * @param addedViews
     * @return array of three members consisting of startY, origEndY, deltaY corresponding to the change.
     */
    double[] replace(DocumentView docView, int index, int removeCount, View[] addedViews) {
        if (index + removeCount > size()) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" + // NOI18N
                    removeCount + ", viewCount=" + size()); // NOI18N
        }
        int endAddedIndex = index;
        int removeEndIndex = index + removeCount;
        double startY = startVisualOffset(index);
        // Leave update.visualDelta == 0d
        double origEndY = (removeCount == 0) ? startY : endVisualOffset(removeEndIndex - 1);
        double endY = startY;
        moveVisualGap(removeEndIndex, origEndY);
        // Assign visual offset BEFORE possible removal/addition of views is made
        // since the added views would NOT have the visual offset filled in yet.
        if (removeCount != 0) { // Removing at least one item => index < size
            TextLayoutCache tlCache = docView.op.getTextLayoutCache();
            for (int i = removeCount - 1; i >= 0; i--) {
                // Do not clear text layouts since the paragraph view will be GCed anyway
                tlCache.remove(get(index + i), false);
            }
            remove(index, removeCount);
        }
        if (addedViews != null && addedViews.length != 0) {
            endAddedIndex = index + addedViews.length;
            addArray(index, addedViews);
            for (int i = 0; i < addedViews.length; i++) {
                ParagraphView view = (ParagraphView) addedViews[i];
                // First assign parent to the view and then later ask for preferred span.
                // This way the view may get necessary info from its parent regarding its preferred span.
                view.setParent(docView);
                endY += view.getPreferredSpan(View.Y_AXIS);
                view.setRawEndVisualOffset(endY);
            }
        }
        double deltaY = endY - origEndY;
        // Always call heightChangeUpdate() to fix gapStorage.visualGapStart
        heightChangeUpdate(endAddedIndex, endY, deltaY);
        if (deltaY != 0d) {
            docView.op.notifyHeightChange();
        }
        return new double[] { startY, origEndY, deltaY };
    }
    
    private void heightChangeUpdate(int endIndex, double endY, double deltaY) {
        if (gapStorage != null) {
            gapStorage.visualGapStart = endY;
            gapStorage.visualGapLength -= deltaY;
            gapStorage.visualGapIndex = endIndex;
        } else { // No gapStorage
            if (deltaY != 0d) {
                int pCount = size();
                if (pCount > ViewGapStorage.GAP_STORAGE_THRESHOLD) {
                    gapStorage = new ViewGapStorage(); // Only for visual gap
                    gapStorage.initVisualGap(endIndex, endY);
                    deltaY += gapStorage.visualGapLength; // To shift above visual gap
                }
                for (;endIndex < pCount; endIndex++) {
                    EditorView view = get(endIndex);
                    view.setRawEndVisualOffset(view.getRawEndVisualOffset() + deltaY);
                }
            }
        }
    }
    
    /**
     * Check height change between start and end indexes and possibly update gap (or rest of views).
     * Also notify repaint of whole area between start and end indexes.
     *
     * @param docView
     * @param index
     * @param endIndex
     * @param docViewAlloc 
     */
    void checkChildrenSpanChange(DocumentView docView, int index) {
        if (docView.op.isChildWidthChange()) {
            docView.op.resetChildWidthChange();
            ParagraphView pView = get(index);
            float newWidth = pView.getPreferredSpan(View.X_AXIS);
            if (newWidth > childrenWidth) {
                childrenWidth = newWidth;
                docView.op.notifyWidthChange();
            }
        }
        if (docView.op.isChildHeightChange()) {
            docView.op.resetChildHeightChange();
            ParagraphView pView = get(index);
            double startY = startVisualOffset(index);
            double endY = endVisualOffset(index);
            float newHeight = pView.getPreferredSpan(View.Y_AXIS);
            double deltaY = newHeight - (endY - startY);
            if (deltaY != 0d) {
                index++; // Move to nex view
                moveVisualGap(index, endY);
                endY += deltaY;
                pView.setRawEndVisualOffset(endY);
                heightChangeUpdate(index, endY, deltaY);
                docView.op.notifyHeightChange();
            }
        }
    }
    
    Shape getChildAllocation(DocumentView docView, int index, Shape docViewAlloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(docViewAlloc);
        double startY = startVisualOffset(index);
        double endY = endVisualOffset(index);
        mutableBounds.y += startY;
        mutableBounds.height = endY - startY;
        // Leave mutableBounds.width
        return mutableBounds;
    }
    
    /**
     * Get view index of first view that "contains" the offset (starts with it or it's inside)
     * by examining child views' absolute start offsets.
     * <br/>
     * This is suitable for document view where start offsets of paragraph views
     * are maintained as positions.
     * 
     * @param offset absolute offset to search for.
     * @param low minimum index from which to start searching.
     * @return view index or -1.
     */
    int viewIndexFirstByStartOffset(int offset, int low) {
        int high = size() - 1;
        if (high == -1) { // No child views
            return -1;
        }
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midStartOffset = get(mid).getStartOffset();
            if (midStartOffset < offset) {
                low = mid + 1;
            } else if (midStartOffset > offset) {
                high = mid - 1;
            } else { // element starts at offset
                while (mid > 0) {
                    mid--;
                    midStartOffset = get(mid).getStartOffset();
                    if (midStartOffset < offset) {
                        mid++;
                        break;
                    }
                }
                high = mid;
                break;
            }
        }
        return Math.max(high, 0); // High could be -1 but should be zero for size() > 0.
    }

    public int viewIndexAtY(double y, Shape alloc) {
        Rectangle2D allocRect = ViewUtils.shapeAsRect(alloc);
        y -= allocRect.getY(); // Make relative
        return viewIndexFirstVisual(y, size());
    }

    /**
     * Get view at given index and if that view is a box view then make sure
     * its children are initialized.
     *
     * @param index
     * @return view with its children initialized.
     */
    ParagraphView getParagraphViewChildrenValid(DocumentView docView, int index) {
        ParagraphView pView = get(index);
        if (pView.children == null) {
            // Init the children; Do not init children before the index since it could in theory remove
            // the pView at index completely so the caller would have to do a lot of checks again.
            // Possibly init next 5 pViews after index since invoking ViewBuilder's has some overhead.
            docView.op.ensureChildrenValid(index, index + 1, 0, 5);
            // Reget the view since the rebuild could replace its instance
            pView = get(index);
            assert (pView.children != null);
        }
        return pView;
    }
    
    public Shape modelToViewChecked(DocumentView docView, int offset, Shape docViewAlloc, Position.Bias bias) {
        int pIndex = viewIndexFirstByStartOffset(offset, 0); // Ignore bias since these are paragraph views
        Shape ret = docViewAlloc;
        if (pIndex >= 0) { // When at least one child the index will fit one of them
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            docView.op.getTextLayoutCache().activate(pView);
            Shape childAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            // Update the bounds with child.modelToView()
            ret = pView.modelToViewChecked(offset, childAlloc, bias);
            checkChildrenSpanChange(docView, pIndex);
        }
        return ret;
    }

    public int viewToModelChecked(DocumentView docView, double x, double y, Shape docViewAlloc, Position.Bias[] biasReturn) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        int offset;
        if (pIndex >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            docView.op.getTextLayoutCache().activate(pView);
            Shape childAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            // forward to the child view
            offset = pView.viewToModelChecked(x, y, childAlloc, biasReturn);
            checkChildrenSpanChange(docView, pIndex);
        } else { // at the end
            offset = docView.getStartOffset();
        }
        return offset;
    }

    int getNextVisualPositionY(DocumentView docView, int offset, Bias bias, Shape alloc, boolean southDirection, Bias[] biasRet) {
        double x = HighlightsViewUtils.getMagicX(docView, docView, offset, bias, alloc);
        int pIndex = docView.getViewIndex(offset, bias);
        int viewCount = size();
        int increment = southDirection ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            // Get paragraph view with valid children
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            Shape pAlloc = getChildAllocation(docView, pIndex, alloc);
            if (pView.children.ensureIndexMeasured(pView, pView.getViewCount(), ViewUtils.shapeAsRect(pAlloc))) {
                checkChildrenSpanChange(docView, pIndex);
                pAlloc = getChildAllocation(docView, pIndex, alloc);
            }
            retOffset = pView.children.getNextVisualPositionY(pView, offset, bias, pAlloc, southDirection, biasRet, x);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    int getNextVisualPositionX(DocumentView docView, int offset, Bias bias, Shape alloc, boolean eastDirection, Bias[] biasRet) {
        int pIndex = docView.getViewIndex(offset, bias);
        int viewCount = size();
        int increment = eastDirection ? 1 : -1;
        int retOffset = -1;
        for (; retOffset == -1 && pIndex >= 0 && pIndex < viewCount; pIndex += increment) {
            // Get paragraph view with valid children
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            Shape pAlloc = getChildAllocation(docView, pIndex, alloc);
            if (pView.children.ensureIndexMeasured(pView, pView.getViewCount(), ViewUtils.shapeAsRect(pAlloc))) {
                checkChildrenSpanChange(docView, pIndex);
                pAlloc = getChildAllocation(docView, pIndex, alloc);
            }
            retOffset = pView.children.getNextVisualPositionX(pView, offset, bias, pAlloc, eastDirection, biasRet);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    public String getToolTipTextChecked(DocumentView docView, double x, double y, Shape docViewAlloc) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        String toolTipText = null;
        if (pIndex >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            docView.op.getTextLayoutCache().activate(pView);
            Shape childAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            // forward to the child view
            toolTipText = pView.getToolTipTextChecked(x, y, childAlloc);
            checkChildrenSpanChange(docView, pIndex);
        }
        return toolTipText;
    }

    public JComponent getToolTip(DocumentView docView, double x, double y, Shape docViewAlloc) {
        int pIndex = viewIndexAtY(y, docViewAlloc);
        JComponent toolTip = null;
        if (pIndex >= 0) {
            // First find valid child (can lead to change of child allocation bounds)
            ParagraphView pView = getParagraphViewChildrenValid(docView, pIndex);
            docView.op.getTextLayoutCache().activate(pView);
            Shape childAlloc = getChildAllocation(docView, pIndex, docViewAlloc);
            // forward to the child view
            toolTip = pView.getToolTip(x, y, childAlloc);
            checkChildrenSpanChange(docView, pIndex);
        }
        return toolTip;
    }

    protected void paint(DocumentView docView, Graphics2D g, Shape docViewAlloc, Rectangle clipBounds) {
        if (size() > 0) {
            double startY = clipBounds.y;
            double endY = clipBounds.getMaxY();
            int startIndex;
            int endIndex;
            if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE)) {
                ViewHierarchyImpl.PAINT_LOG.fine("\nDocumentViewChildren.paint(): START clipBounds: " + clipBounds + "\n"); // NOI18N
            }
            do {
                startIndex = viewIndexAtY(startY, docViewAlloc);
                endIndex = viewIndexAtY(endY, docViewAlloc) + 1;
                if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE)) {
                    ViewHierarchyImpl.PAINT_LOG.fine("  paint:docView:[" + startIndex + "," + endIndex + // NOI18N
                            "] for y:<" + startY + "," + endY + ">\n"); // NOI18N
                }
                // Ensure valid children
                // Possibly build extra 5 lines in each direction to speed up possible scrolling
                // If there was any update then recompute indices since rebuilding might change vertical spans
            } while (docView.op.ensureChildrenValid(startIndex, endIndex, 10, 10));

            // Ensure that the (inited) children are all measured
            // Text layout cache must be able to contain TLs for all painted views.
            // Otherwise firstly processed views would start to forget their TLs because of tlCache.activate()
            TextLayoutCache tlCache = docView.op.getTextLayoutCache();
            tlCache.ensureCapacity(endIndex - startIndex);
            endIndex = size(); // will likely be lowered inside the loop
            for (int i = startIndex; i < endIndex; i++) {
                ParagraphView pView = get(i);
                if (pView.children == null) { // Views shrinked their vertical span during ensureIndexMeasured()
                    // Init next batch of pViews
                    int extraEndIndex = Math.min(i + 20, size());
                    docView.op.ensureChildrenValid(i, extraEndIndex, 0, 0);
                    tlCache.ensureCapacity(extraEndIndex - startIndex);
                    endIndex = size(); // total number of pViews may change by ensureChildrenValid()
                    pView = get(i);
                }
                tlCache.activate(pView);
                Shape childAlloc = getChildAllocation(docView, i, docViewAlloc);
                Rectangle2D childRect = ViewUtils.shapeAsRect(childAlloc);
                if (pView.children.ensureYMeasured(pView, endY, childRect)) {
                    checkChildrenSpanChange(docView, i);
                }
                if (childRect.getMaxY() >= endY) { // Recompute of children height may make painting endIndex lower
                    endIndex = i + 1;
                    break;
                }
            }

            // Paint children in <startIndex,endIndex>
            boolean logPaintTime = ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINE);
            long nanoTime = 0L;
            if (logPaintTime) {
                nanoTime = System.nanoTime();
            }
            // Compute offset bounds to compute paint highlights
            int startOffset = get(startIndex).getStartOffset(); // startIndex < viewCount
            int endOffset = get(endIndex - 1).getEndOffset();
            // It must listen whether some layer does not change during reading
            // of the paint highlights. If there would be too many failures (likely
            // due to a layer that fires changes when asked for highlights) the paint
            // should succeed even without the painting highlights.
            HighlightsList paintHighlights = null;
            int maxPHReads = 10;
            do {
                HighlightsContainer phContainer = HighlightingManager.getInstance(docView.getTextComponent()).
                        getTopHighlights();
                final boolean[] phStale = new boolean[1];
                HighlightsChangeListener hChangeListener = new HighlightsChangeListener() {
                    @Override
                    public void highlightChanged(HighlightsChangeEvent event) {
                        phStale[0] = true;
                    }
                };
                phContainer.addHighlightsChangeListener(hChangeListener);
                HighlightsReader reader = new HighlightsReader(phContainer, startOffset, endOffset);
                reader.readUntil(endOffset);
                paintHighlights = reader.highlightsList();
                if (!phStale[0]) {
                    break;
                } else {
                    phStale[0] = false;
                }
                phContainer.removeHighlightsChangeListener(hChangeListener);
            } while (--maxPHReads >= 0);

            // Assert that paint highlight items cover the whole area being painted.
            int phEndOffset;
            assert ((phEndOffset = paintHighlights.endOffset()) == endOffset) :
                    "phEndOffset=" + phEndOffset + " != endOffset"; // NOI18N
            // Paint hilghlights will serve all the child views being painted
            viewPaintHighlights = new ViewPaintHighlights(paintHighlights);
            // Do children painting
            for (int i = startIndex; i < endIndex; i++) {
                ParagraphView pView = get(i);
                Shape childAlloc = getChildAllocation(docView, i, docViewAlloc);
                if (ViewHierarchyImpl.PAINT_LOG.isLoggable(Level.FINER)) {
                    ViewHierarchyImpl.PAINT_LOG.finer("    pView[" + i + "]: pAlloc=" + // NOI18N
                            ViewUtils.toString(childAlloc) + "\n"); // NOI18N
                }
                pView.paint(g, childAlloc, clipBounds);
            }
            viewPaintHighlights = null;
            if (logPaintTime) {
                nanoTime = System.nanoTime() - nanoTime;
                ViewHierarchyImpl.PAINT_LOG.fine("Painted " + (endIndex-startIndex) + // NOI18N
                        " lines <" + startIndex + "," + endIndex + // NOI18N
                        "> in " + (nanoTime/1000000d) + " ms\n"); // NOI18N
            }
            // [TODO] Since this portion was painted => exclude it from possibly scheduled paint
        }
    }
    
    ViewPaintHighlights getPaintHighlights(EditorView view, int shift) {
        assert (viewPaintHighlights != null) : "ViewPaintHighlights is null. Not in paint()?"; // NOI18N
        viewPaintHighlights.reset(view, shift);
        return viewPaintHighlights;
    }

    void recomputeChildrenWidths() {
        int viewCount = size();
        for (int i = 0; i < viewCount; i++) {
            ParagraphView pView = get(i);
            pView.resetWidth();
        }
    }

    public StringBuilder appendChildrenInfo(DocumentView docView, StringBuilder sb, int indent, int importantIndex) {
        return appendChildrenInfo(sb, indent, importantIndex);
    }

    @Override
    protected String getXYInfo(int index) {
        return new StringBuilder(10).append(" y=").append(startVisualOffset(index)).toString();
    }

}
