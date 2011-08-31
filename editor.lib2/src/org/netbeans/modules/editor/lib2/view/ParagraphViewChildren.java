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
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.text.Position.Bias;
import javax.swing.text.TabExpander;
import javax.swing.text.TabableView;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;


/**
 * View of a visual line that is capable of doing word-wrapping.
 * 
 * @author Miloslav Metelka
 */

final class ParagraphViewChildren extends ViewChildren<EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphViewChildren.class.getName());

    private static final long serialVersionUID  = 0L;

    /**
     * Info about line wrap - initially null.
     */
    private WrapInfo wrapInfo; // 28=super + 4 = 32 bytes
    
    private int measuredEndIndex; // 32 + 4 = 36 bytes

    private float childrenHeight; // 36 + 4 = 40 bytes

    public ParagraphViewChildren(int capacity) {
        super(capacity);
    }
    
    float height() {
        return (wrapInfo == null) ? childrenHeight : wrapInfo.height(this);
    }
    
    float width() {
        return (wrapInfo == null) ? (float) childrenWidth() : wrapInfo.width();
    }

    double childrenWidth() {
        return startVisualOffset(measuredEndIndex);
    }
    
    float childrenHeight() {
        return childrenHeight;
    }
    
    int length() {
        return startOffset(size());
    }
    
    boolean isFullyMeasured() {
        return measuredEndIndex >= size();
    }
    
    Shape getChildAllocation(int index, Shape alloc) {
        Rectangle2D.Double mutableBounds = ViewUtils.shape2Bounds(alloc);
        double startX = startVisualOffset(index);
        double endX = endVisualOffset(index);
        mutableBounds.x += startX;
        mutableBounds.width = endX - startX;
        mutableBounds.height = childrenHeight; // Only works in non-wrapping case
        return mutableBounds;
    }

    int getViewIndex(ParagraphView pView, int offset) {
        offset -= pView.getStartOffset(); // Get relative offset
        return viewIndexFirst(offset); // Binary search through relative offsets
    }
    
    int getViewIndex(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        return indexAndAlloc.index;
    }

    int viewIndexNoWrap(ParagraphView pView, double x, Shape pAlloc) {
        Rectangle2D pViewRect = ViewUtils.shapeAsRect(pAlloc);
        ensurePointMeasured(pView, x, pViewRect.getY(), pViewRect);
        return viewIndexFirstVisual(x, measuredEndIndex); // Binary search through relative offsets
    }
    
    /**
     * Replace children of paragraph view.
     * 
     * @param pView
     * @param index
     * @param removeCount
     * @param addedViews
     * @param offsetDelta delta of offsets caused by insert/removal.
     */
    void replace(ParagraphView pView, int index, int removeCount, View[] addedViews, int offsetDelta) {
        if (index + removeCount > size()) {
            throw new IllegalArgumentException("index=" + index + ", removeCount=" + // NOI18N
                    removeCount + ", viewCount=" + size()); // NOI18N
        }
        int removeEndIndex = index + removeCount;
        int startRelOffset = 0;
        int endRelOffset = 0;
        startRelOffset = startOffset(index);
        endRelOffset = (removeCount == 0) ? startRelOffset : endOffset(removeEndIndex - 1);
        moveOffsetGap(removeEndIndex, endRelOffset);
        double x = startVisualOffset(index);
        // Move visual gap to index since everything above it will be visually recomputed later
        if (index < measuredEndIndex) {
            moveVisualGap(index, x);
            lowerMeasuredEndIndex(index);
        }
        // Assign visual offset BEFORE possible removal/addition of views is made
        // since the added views would NOT have the visual offset filled in yet.
        if (removeCount != 0) { // Removing at least one item => index < size
            remove(index, removeCount);
        }
        int endAddedIndex;
        if (addedViews != null && addedViews.length != 0) {
            endAddedIndex = index + addedViews.length;
            addArray(index, addedViews);
            int pViewStartOffset = pView.getStartOffset();
            for (int i = 0; i < addedViews.length; i++) {
                EditorView view = (EditorView) addedViews[i];
                int offset = view.getRawEndOffset();
                // Below gap => do not use offsetGapLength
                view.setRawEndOffset(offset - pViewStartOffset);
                view.setParent(pView);
                // Do not measure added view at this point
            }
        } else { // No added views
            endAddedIndex = index;
        }
        // Even if no views would be added (just removal) the tabbable views must be re-computed
        // Since making the measuredEndIndex smaller => do not need to recompute layout
        if (gapStorage != null) { // TODO - not used currently (no gap created)
            gapStorage.offsetGapStart = endRelOffset;
            gapStorage.offsetGapLength -= offsetDelta;
        } else { // Move the items one by one
            if (offsetDelta != 0) {
                int viewCount = size(); // Refresh (changed by 
                if (false && viewCount > ViewGapStorage.GAP_STORAGE_THRESHOLD) { // TODO enable when stable
                    gapStorage = new ViewGapStorage();
                    gapStorage.initOffsetGap(endRelOffset);
                    offsetDelta += gapStorage.offsetGapLength; // Move above gap will follow
                }
                for (int i = endAddedIndex; i < viewCount; i++) {
                    EditorView view = get(i);
                    view.setRawEndOffset(view.getRawEndOffset() + offsetDelta);
                }
            }
        }
        // Update paragraph view's length to actual textual length of children.
        // It cannot be done relatively by just adding offsetDelta to original length
        // since box views with unitialized children already have proper length
        // so later children initialization would double that length.
        int newLength = getLength();
        if (newLength != pView.getLength()) {
            if (ViewHierarchyImpl.SPAN_LOG.isLoggable(Level.FINER)) {
                ViewHierarchyImpl.SPAN_LOG.finer(pView.getDumpId() + ": update length: " + // NOI18N
                        pView.getLength() + " => " + newLength + "\n"); // NOI18N
            }
            pView.setLength(newLength);
        }
    }
    
    void lowerMeasuredEndIndex(int newMeasuredEndIndex) {
        assert (newMeasuredEndIndex >= 0) : "newMeasuredEndIndex=" + newMeasuredEndIndex + " < 0"; // NOI18N
        assert (newMeasuredEndIndex <= measuredEndIndex);
        while (measuredEndIndex > newMeasuredEndIndex) {
            EditorView view = get(--measuredEndIndex);
            if (view instanceof HighlightsView) {
                HighlightsView hView = (HighlightsView) view;
                assert (hView.getTextLayout() != null);
                hView.setTextLayout(null);
            }
        }
    }
    
    boolean ensureIndexMeasured(ParagraphView pView, int index, Rectangle2D pViewRect) {
        int viewCount = size();
        if (measuredEndIndex < viewCount) {
            if (measuredEndIndex <= index) { // <= to ensure next on inited too
                double measuredEndX = startVisualOffset(measuredEndIndex);
                fixSpansAndRepaint(pView, measuredEndX, viewCount, index, 0d, pViewRect);
                return true;
            }
        }
        return false;
    }
    
    boolean ensurePointMeasured(ParagraphView pView, double x, double y, Rectangle2D pViewRect) {
        int viewCount = size();
        if (measuredEndIndex < viewCount) {
            double relY = y - pViewRect.getY();
            if (relY >= childrenHeight) {
                // [TODO] Init just partially till relY
                ensureIndexMeasured(pView, viewCount, pViewRect);
                return true;
            }
            x -= pViewRect.getX(); // make relative
            double measuredEndRelX = startVisualOffset(measuredEndIndex);
            if (measuredEndRelX <= x) { // <= to ensure next one inited too
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ensurePointMeasured: x=" + x + ",y=" + y + ", mX=" + measuredEndRelX); // NOI18N
                }
                fixSpansAndRepaint(pView, measuredEndRelX, viewCount, -1, x, pViewRect);
                return true;
            }
        }
        return false;
    }

    /**
     * Ensure that all child views occupying the particular y coordinate will be measured.
     *
     * @param pView
     * @param y
     * @param pViewRect
     * @return 
     */
    boolean ensureYMeasured(ParagraphView pView, double y, Rectangle2D pViewRect) {
        // Use Integer.MAX_VALUE for reasonable subtracting
        return ensurePointMeasured(pView, (double) Integer.MAX_VALUE, y, pViewRect);
    }
    
    void fixSpansAndRepaint(ParagraphView pView, double mEndX,
            int viewCount, int targetIndex, double targetRelX, Rectangle2D pViewRect)
    {
        DocumentView docView = pView.getDocumentView();
        CharSequence docText = null;
        int mEndOffset = 0;
        TabExpander tabExpander = null;
        float origWidth = width();
        float origHeight = height();
        // Ensure that a repeated fixing of spans will not be called many times
        // since there is certain overhead - repaints and possible width preference change.
        int mEndIndex = measuredEndIndex;
        int stopIndex = mEndIndex << 1;
        boolean wrapInfoChecked = false;
        for (; mEndIndex < viewCount; mEndIndex++) {
            EditorView view = get(mEndIndex);
            // First assign parent to the view and then ask for preferred span.
            // This way the view may get necessary info from its parent regarding its preferred span.
            float width;
            if (view instanceof HighlightsView) {
                HighlightsView hView = (HighlightsView) view;
                // Fill in text layout if necessary
                if (hView.getTextLayout() == null) { // Fill in text layout
                    if (docText == null) {
                        mEndOffset = pView.getStartOffset() + startOffset(mEndIndex); // Needed for TextLayout creation
                        docText = DocumentUtilities.getText(docView.getDocument());
                    }
                    String text = docText.subSequence(mEndOffset, mEndOffset + view.getLength()).toString();
                    TextLayout textLayout = docView.op.createTextLayout(text, hView.getAttributes());
                    hView.setTextLayout(textLayout);
                 }
            }
            // Measure the view
            if (view instanceof TabableView) {
                if (tabExpander == null) {
                    tabExpander = docView.getTabExpander();
                }
                width = ((TabableView) view).getTabbedSpan((float) mEndX, tabExpander);
            } else {
                width = view.getPreferredSpan(View.X_AXIS);
            }
            mEndX += width;
            view.setRawEndVisualOffset(visualOffset2Raw(mEndX));
            // Check for possible height change
            float height = view.getPreferredSpan(View.Y_AXIS);
            if (height > childrenHeight) {
                childrenHeight = height;
            }
            mEndOffset += view.getLength();
            // Check whether stop now
            if (mEndIndex >= stopIndex &&
                    ((targetIndex != -1 && mEndIndex > targetIndex) ||
                    (targetIndex == -1 && mEndX > targetRelX))) // '>' so that the x is contained inside a view
            {
                if (!wrapInfoChecked) {
                    wrapInfoChecked = true;
                    checkCreateWrapInfo(docView, mEndX);
                    if (wrapInfo != null) {
                        stopIndex = viewCount; // Force measure till end
                        continue;
                    }
                }
                mEndIndex++;
                break;
            }
        }
        assert (mEndIndex >= measuredEndIndex);
        measuredEndIndex = mEndIndex;
        // [TODO] implement lazy incremental update
        if (!wrapInfoChecked) {
            checkCreateWrapInfo(docView, mEndX);
        }
        if (wrapInfo != null) {
            buildWrapLines(pView);
        } // Else no wrapping 
        boolean fullyMeasured = isFullyMeasured();
        float newWidth = width();
        float newHeight = height();
        if ((fullyMeasured && newWidth != origWidth) || (!fullyMeasured && newWidth > origWidth)) {
            pView.setWidth(newWidth);
            pView.notifyChildWidthChange();
        }
        boolean repaintHeightChange = false;
        double deltaY = newHeight - origHeight;
        if ((fullyMeasured && deltaY != 0d) || (!fullyMeasured && deltaY > 0d)) {
            repaintHeightChange = true;
            pView.setHeight(newHeight);
            pView.notifyChildHeightChange();
            docView.addChange(pViewRect.getY(), pViewRect.getMaxY(), deltaY);
        }
        // Repaint full pView [TODO] can be improved
        Rectangle visibleRect = docView.op.getVisibleRect();
        pView.notifyRepaint(pViewRect.getX(), pViewRect.getY(), visibleRect.getMaxX(),
                repaintHeightChange ? visibleRect.getMaxY() : pViewRect.getMaxY());
    }
    
    private void checkCreateWrapInfo(DocumentView docView, double childrenWidth) {
        // For existing wrapInfo tend to create wrap info too since it could just be truncated
        // but it contains up-to-date wrap line height.
        if (wrapInfo != null || childrenWidth > docView.op.getAvailableWidth()) {
            wrapInfo = new WrapInfo();
        }
    }
    
    private void buildWrapLines(ParagraphView pView) {
        wrapInfo.updater = new WrapInfoUpdater(wrapInfo, pView);
        wrapInfo.updater.initWrapInfo();
        wrapInfo.updater = null; // Finished [TODO] Lazy update
    }

    void resetWidth() {
        lowerMeasuredEndIndex(0);
        wrapInfo = null;
    }

    void preferenceChanged(ParagraphView pView, EditorView view, boolean widthChange, boolean heightChange) {
        int index = viewIndexFirst(raw2Offset(view.getRawEndOffset()));
        if (index >= 0 && get(index) == view) {
            if (widthChange) {
                if (index < measuredEndIndex) {
                    lowerMeasuredEndIndex(index);
                }
            }
            if (heightChange) {
                float newHeight = view.getPreferredSpan(View.Y_AXIS);
                if (newHeight > childrenHeight) {
                    childrenHeight = newHeight;
                } else {
                    heightChange = false; // Change in fact does not affect this view
                }
            }
            if (widthChange || heightChange) {
                pView.preferenceChanged(null, widthChange, heightChange);
            }
        }
    }

    void paint(ParagraphView pView, Graphics2D g, Shape pAlloc, Rectangle clipBounds) {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(pAlloc);
        // Ensure whole line gets inited (TODO - check available width for non-wrapping case)
        ensureIndexMeasured(pView, size(), allocBounds);
        if (wrapInfo != null) {
            int startWrapLineIndex;
            int endWrapLineIndex;
            double wrapY = clipBounds.y - allocBounds.y;
            float wrapLineHeight = wrapInfo.wrapLineHeight(this);
            if (wrapY < wrapLineHeight) {
                startWrapLineIndex = 0;
            } else {
                startWrapLineIndex = (int) (wrapY / wrapLineHeight);
            }
            // Find end index
            wrapY += clipBounds.height + (wrapLineHeight - 1);
            if (wrapY >= height()) {
                endWrapLineIndex = wrapInfo.wrapLineCount();
            } else {
                endWrapLineIndex = (int) (wrapY / wrapLineHeight) + 1;
            }
            wrapInfo.paintWrapLines(this, pView, startWrapLineIndex, endWrapLineIndex, g, pAlloc, clipBounds);

        } else { // Regular paint
            double startX = clipBounds.x - allocBounds.x;
            double endX = startX + clipBounds.width;
            if (size() > 0) {
                int startIndex = viewIndexNoWrap(pView, startX, pAlloc); // y ignored
                int endIndex = viewIndexNoWrap(pView, endX, pAlloc) + 1; // y ignored
                paintChildren(pView, g, pAlloc, clipBounds, startIndex, endIndex);
            }
        }
    }
    
    void paintChildren(ParagraphView pView, Graphics2D g, Shape pAlloc, Rectangle clipBounds,
            int startIndex, int endIndex)
    {
        while (startIndex < endIndex) {
            EditorView view = get(startIndex);
            Shape childAlloc = getChildAllocation(startIndex, pAlloc);
            if (view.getClass() == NewlineView.class) {
                // Extend till end of screen (docView's width)
                Rectangle2D.Double childRect = ViewUtils.shape2Bounds(childAlloc);
                DocumentView docView = pView.getDocumentView();
                childRect.width = docView.op.getVisibleRect().getMaxX() - childRect.getX();
                childAlloc = childRect;
            }
            view.paint(g, childAlloc, clipBounds);
            startIndex++;
        }
    }
    
    Shape modelToViewChecked(ParagraphView pView, int offset, Shape alloc, Bias bias) {
        int index = pView.getViewIndex(offset, bias);
        if (index < 0) {
            return alloc;
        }
        ensureIndexMeasured(pView, index, ViewUtils.shapeAsRect(alloc));
        if (wrapInfo != null) {
            int wrapLineIndex = findWrapLineIndex(pView, offset);
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            Rectangle2D wrapLineBounds = wrapLineAlloc(alloc, wrapLineIndex);
            Shape ret = null;
            StringBuilder logBuilder = null;
            if (LOG.isLoggable(Level.FINE)) {
                logBuilder = new StringBuilder(100);
                logBuilder.append("ParagraphViewChildren.modelToViewChecked(): offset="). // NOI18N
                        append(offset).append(", wrapLineIndex=").append(wrapLineIndex). // NOI18N
                        append(", orig-allocBounds=").append(ViewUtils.toString(alloc)).append("\n    "); // NOI18N
            }

            if (wrapLine.startPart != null && offset < wrapLine.startPart.getEndOffset()) {
                Shape startPartAlloc = startPartAlloc(wrapLineBounds, wrapLine);
                if (logBuilder != null) {
                    logBuilder.append("START-part:").append(ViewUtils.toString(startPartAlloc)); // NOI18N
                }
                ret = wrapLine.startPart.modelToViewChecked(offset, startPartAlloc, bias);
            } else if (wrapLine.endPart != null && offset >= wrapLine.endPart.getStartOffset()) {
                Shape endPartAlloc = endPartAlloc(wrapLineBounds, wrapLine, pView);
                if (logBuilder != null) {
                    logBuilder.append("END-part:").append(ViewUtils.toString(endPartAlloc)); // NOI18N
                }
                // getPreferredSpan() perf should be ok since part-view should cache the TextLayout
                ret = wrapLine.endPart.modelToViewChecked(offset, endPartAlloc, bias);
            } else {
                assert (wrapLine.hasFullViews()) : wrapInfo.dumpWrapLine(pView, wrapLineIndex);
                for (int i = wrapLine.firstViewIndex; i < wrapLine.endViewIndex; i++) {
                    EditorView view = pView.getEditorView(i);
                    if (offset < view.getEndOffset()) {
                        Shape viewAlloc = wrapAlloc(wrapLineBounds, wrapLine, i, pView);
                        ret = view.modelToViewChecked(offset, viewAlloc, bias);
                        assert (ret != null);
                        break;
                    }
                }
            }
            if (logBuilder != null) {
                logBuilder.append("\n    RET=").append(ViewUtils.toString(ret)).append('\n'); // NOI18N
                LOG.fine(logBuilder.toString());
            }
            return ret;

        } else { // No wrapping
            ensureIndexMeasured(pView, index, ViewUtils.shapeAsRect(alloc));
            // First find valid child (can lead to change of child allocation bounds)
            EditorView view = get(index);
            Shape childAlloc = getChildAllocation(index, alloc);
            // Update the bounds with child.modelToView()
            return view.modelToViewChecked(offset, childAlloc, bias);
        }
    }

    public int viewToModelChecked(ParagraphView pView, double x, double y, Shape pAlloc, Bias[] biasReturn) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        int offset = (indexAndAlloc != null)
                ? indexAndAlloc.viewOrPart.viewToModelChecked(x, y, indexAndAlloc.alloc, biasReturn)
                : pView.getStartOffset();
        return offset;
    }

    public String getToolTipTextChecked(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        String toolTipText = (indexAndAlloc != null)
                ? indexAndAlloc.viewOrPart.getToolTipTextChecked(x, y, indexAndAlloc.alloc)
                : null;
        return toolTipText;
    }

    public JComponent getToolTip(ParagraphView pView, double x, double y, Shape pAlloc) {
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, y, pAlloc);
        JComponent toolTip = (indexAndAlloc != null)
                ? indexAndAlloc.viewOrPart.getToolTip(x, y, indexAndAlloc.alloc)
                : null;
        return toolTip;
    }

    /**
     * Find next visual position in Y direction.
     * In case of no linewrap the method should return -1 for a given valid offset parameter.
     * For offset -1 the method should find position best corresponding to x parameter.
     * If linewrap is active the method should go through the particular wraplines.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     * @param x x-position corresponding to magic caret position.
     */
    int getNextVisualPositionY(ParagraphView pView,
            int offset, Bias bias, Shape pAlloc, boolean southDirection, Bias[] biasRet, double x)
    {
        // Children already ensured to be measured by parent
        int retOffset;
        if (offset == -1) {
            if (wrapInfo != null) { // Use last wrap line
                int wrapLine = southDirection ? 0 : wrapInfo.wrapLineCount() - 1;
                retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLine);
            } else { // wrapInfo == null; offset == -1
                retOffset = visualPositionNoWrap(pView, pAlloc, biasRet, x);
            }
        } else { // offset != -1
            if (wrapInfo != null) {
                int wrapLineIndex = findWrapLineIndex(pView, offset);
                if (!southDirection && wrapLineIndex > 0) {
                    retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLineIndex - 1);
                } else if (southDirection && wrapLineIndex < wrapInfo.wrapLineCount() - 1) {
                    retOffset = visualPositionOnWrapLine(pView, pAlloc, biasRet, x, wrapLineIndex + 1);
                } else {
                    retOffset = -1;
                }
            } else { // wrapInfo == null
                retOffset = -1;
            }
        }
        return retOffset;
    }
    
    /**
     * Find next visual position in Y direction.
     * In case of no line-wrap the method should return -1 for a given valid offset.
     * and a valid offset when -1 is given as parameter.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     */
    int getNextVisualPositionX(ParagraphView pView, int offset, Bias bias, Shape pAlloc, boolean eastDirection, Bias[] biasRet) {
        // Children already ensured to be measured by parent
        int viewCount = size();
        int index = (offset == -1)
                ? (eastDirection ? 0 : viewCount - 1)
                : getViewIndex(pView, offset);
        int increment = eastDirection ? 1 : -1;
        int retOffset = -1;
        // Cycle through individual views in left or right direction
        for (; retOffset == -1 && index >= 0 && index < viewCount; index += increment) {
            EditorView view = get(index); // Ensure valid children
            Shape viewAlloc = getChildAllocation(index, pAlloc);
            retOffset = view.getNextVisualPositionFromChecked(offset, bias, viewAlloc, 
                    eastDirection ? SwingConstants.EAST : SwingConstants.WEST, biasRet);
            if (retOffset == -1) {
                offset = -1; // Continue by entering the paragraph from outside
            }
        }
        return retOffset;
    }

    private int visualPositionNoWrap(ParagraphView pView, Shape alloc, Bias[] biasRet, double x) {
        int childIndex = viewIndexNoWrap(pView, x, alloc);
        EditorView child = pView.getEditorView(childIndex);
        Shape childAlloc = pView.getChildAllocation(childIndex, alloc);
        Rectangle2D r = ViewUtils.shapeAsRect(childAlloc);
        return child.viewToModelChecked(x, r.getY(), childAlloc, biasRet);
    }

    private int visualPositionOnWrapLine(ParagraphView pView,
            Shape alloc, Bias[] biasRet, double x, int wrapLineIndex)
    {
        WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
        Shape wrapLineAlloc = wrapLineAlloc(alloc, wrapLineIndex);
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(pView, x, wrapLineAlloc, wrapLine);
        double y = ViewUtils.shapeAsRect(indexAndAlloc.alloc).getY();
        return indexAndAlloc.viewOrPart.viewToModelChecked(x, y, indexAndAlloc.alloc, biasRet);
    }
    
    private int findWrapLineIndex(Rectangle2D pAllocRect, double y) {
        int wrapLineIndex;
        double relY = y - pAllocRect.getY();
        float wrapLineHeight = wrapInfo.wrapLineHeight(this);
        if (relY < wrapLineHeight) {
            wrapLineIndex = 0;
        } else {
            wrapLineIndex = (int) (relY / wrapLineHeight);
            int wrapLineCount = wrapInfo.wrapLineCount();
            if (wrapLineIndex >= wrapLineCount) {
                wrapLineIndex = wrapLineCount - 1;
            }
        }
        return wrapLineIndex;
    }
    
    private int findWrapLineIndex(ParagraphView pView, int offset) {
        int wrapLineCount = wrapInfo.wrapLineCount();
        int wrapLineIndex = 0;
        WrapLine wrapLine = null;
        while (++wrapLineIndex < wrapLineCount) {
            wrapLine = wrapInfo.get(wrapLineIndex);
            if (wrapLineStartOffset(pView, wrapLine) > offset) {
                break;
            }
        }
        wrapLineIndex--;
        return wrapLineIndex;
    }

    private Shape startPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine) {
        Rectangle2D.Double startPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        startPartBounds.width = wrapLine.firstViewX;
        return startPartBounds;
    }
    
    private Shape endPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine, ParagraphView pView) {
        Rectangle2D.Double endPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        endPartBounds.width = wrapLine.endPart.getPreferredSpan(View.X_AXIS);
        endPartBounds.x += wrapLine.firstViewX;
        if (wrapLine.hasFullViews()) {
            endPartBounds.x += (startVisualOffset(wrapLine.endViewIndex)
                    - startVisualOffset(wrapLine.firstViewIndex));
        }
        return endPartBounds;
    }
    
    private Shape wrapAlloc(Shape wrapLineAlloc, WrapLine wrapLine, int viewIndex, ParagraphView pView) {
        double startX = startVisualOffset(wrapLine.firstViewIndex);
        double x = (viewIndex != wrapLine.firstViewIndex)
                ? startVisualOffset(viewIndex)
                : startX;
        Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        viewBounds.x += wrapLine.firstViewX + (x - startX);
        viewBounds.width = endVisualOffset(viewIndex) - x;
        return viewBounds;
    }

    private IndexAndAlloc findIndexAndAlloc(ParagraphView pView, double x, double y, Shape pAlloc) {
        if (size() == 0) {
            return null;
        }
        Rectangle2D pRect = ViewUtils.shapeAsRect(pAlloc);
        ensurePointMeasured(pView, x, y, pRect);
        if (wrapInfo == null) { // Regular case
            IndexAndAlloc indexAndAlloc = new IndexAndAlloc();
            int index = viewIndexNoWrap(pView, x, pAlloc);
            indexAndAlloc.index = index;
            indexAndAlloc.viewOrPart = get(index);
            indexAndAlloc.alloc = getChildAllocation(index, pAlloc);
            return indexAndAlloc;
            
        } else { // Wrapping
            int wrapLineIndex = findWrapLineIndex(pRect, y);
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            Rectangle2D.Double wrapLineAlloc = wrapLineAlloc(pAlloc, wrapLineIndex);
            return findIndexAndAlloc(pView, x, wrapLineAlloc, wrapLine);
        }
    }

    private IndexAndAlloc findIndexAndAlloc(ParagraphView pView,
            double x, Shape wrapLineAlloc, WrapLine wrapLine)
    {
        IndexAndAlloc indexAndAlloc = new IndexAndAlloc();
        if (wrapLine.startPart != null && (x < wrapLine.firstViewX
                || (!wrapLine.hasFullViews() && wrapLine.endPart == null))) {
            indexAndAlloc.index = -1; // start part
            indexAndAlloc.viewOrPart = wrapLine.startPart;
            indexAndAlloc.alloc = startPartAlloc(wrapLineAlloc, wrapLine);
            return indexAndAlloc;
        }
        // Go through full views
        if (wrapLine.hasFullViews()) {
            Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
            viewBounds.x += wrapLine.firstViewX;
            double lastX = startVisualOffset(wrapLine.firstViewIndex);
            for (int i = wrapLine.firstViewIndex; i < wrapLine.endViewIndex; i++) {
                double nextX = startVisualOffset(i + 1);
                viewBounds.width = nextX - lastX;
                if (x < viewBounds.x + viewBounds.width || // Fits
                        (i == wrapLine.endViewIndex - 1 && wrapLine.endPart == null)) // Last part and no end part
                {
                    indexAndAlloc.index = i;
                    indexAndAlloc.viewOrPart = pView.getEditorView(i);
                    indexAndAlloc.alloc = viewBounds;
                    return indexAndAlloc;
                }
                viewBounds.x += viewBounds.width;
                lastX = nextX;
            }
            // Force last in case there is no end part
        }
        assert (wrapLine.endPart != null) : "Null endViewPart"; // NOI18N
        // getPreferredSpan() perf should be ok since part-view should cache the TextLayout
        indexAndAlloc.index = -2;
        indexAndAlloc.viewOrPart = wrapLine.endPart;
        indexAndAlloc.alloc = endPartAlloc(wrapLineAlloc, wrapLine, pView);
        return indexAndAlloc;
    }

    private Rectangle2D.Double wrapLineAlloc(Shape alloc, int wrapLineIndex) {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        float wrapLineHeight = wrapInfo.wrapLineHeight(this);
        allocBounds.y += wrapLineIndex * wrapLineHeight;
        allocBounds.height = wrapLineHeight;
        return allocBounds;
    }
    
    private int wrapLineStartOffset(ParagraphView pView, WrapLine wrapLine) {
        if (wrapLine.startPart != null) {
            return wrapLine.startPart.getStartOffset();
        } else if (wrapLine.hasFullViews()) {
            return pView.getView(wrapLine.firstViewIndex).getStartOffset();
        } else {
            assert (wrapLine.endPart != null) : "Invalid wrapLine: " + wrapLine;
            return wrapLine.endPart.getStartOffset();
        }
    }
    
    @Override
    protected String findIntegrityError(EditorView parent) {
        String err = super.findIntegrityError(parent);
        return err;
    }

    /**
     * Append pView-related info to string builder.
     *
     * @param pView
     */
    public StringBuilder appendViewInfo(ParagraphView pView, StringBuilder sb) {
        sb.append(", chWxH=").append(width()).append("x").append(height()); // NOI18N
        sb.append(", mI=").append(measuredEndIndex); // NOI18N
        if (wrapInfo != null) {
            sb.append(", Wrapped"); // NOI18N
        }
        return sb;
    }

    public StringBuilder appendChildrenInfo(ParagraphView pView, StringBuilder sb, int indent, int importantIndex) {
        if (wrapInfo != null) {
            wrapInfo.appendInfo(sb, pView, indent);
        }
        return appendChildrenInfo(sb, indent, importantIndex);
    }

    @Override
    protected StringBuilder appendChildInfo(StringBuilder sb, int index) {
        sb.append("x=").append(startVisualOffset(index)).append(": ");
        return sb;
    }

    private static final class IndexAndAlloc {
        
        /**
         * Between &lt;wrapLine.startViewIndex, wrapLine.endViewIndex&gt;
         * or -1 for startViewPart or -2 for endViewPart.
         */
        int index;
        
        /**
         * View (or part) correspond to given index;
         */
        EditorView viewOrPart;

        /**
         * Allocation corresponding to index.
         */
        Shape alloc;
        
    }

}
