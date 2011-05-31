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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextHitInfo;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;


/**
 * View of a visual line that is capable of doing word-wrapping.
 * 
 * @author Miloslav Metelka
 */

public final class ParagraphViewChildren extends EditorBoxViewChildren<EditorView> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ParagraphViewChildren.level=FINE
    private static final Logger LOG = Logger.getLogger(ParagraphViewChildren.class.getName());

    private static final long serialVersionUID  = 0L;

    /**
     * Info about line wrap - initially null.null
     */
    private WrapInfo wrapInfo; // 32 bytes = 28-super + 4

    public ParagraphViewChildren(int capacity) {
        super(capacity);
    }

    @Override
    protected boolean rawOffsetUpdate() {
        return true;
    }

    @Override
    protected boolean handleTabableViews() {
        return true;
    }

    @Override
    protected double getMajorAxisChildrenSpan(EditorBoxView<EditorView> boxView) {
        // When wrapInfo is active the box view's width and height reflect the multi-line wrap
        // (and the children' width/height are stored in wrapInfo).
        // This is because the children may be dropped but the view must retain its spans
        // after it as well.
        return (wrapInfo != null) ? wrapInfo.childrenWidth : super.getMajorAxisChildrenSpan(boxView);
    }

    @Override
    protected void setMajorAxisChildrenSpan(EditorBoxView<EditorView> boxView, double majorAxisSpan) {
        if (wrapInfo != null) {
            wrapInfo.childrenWidth = majorAxisSpan;
        } else {
            super.setMajorAxisChildrenSpan(boxView, majorAxisSpan);
        }
    }

    @Override
    protected float getMinorAxisChildrenSpan(EditorBoxView<EditorView> boxView) {
        return (wrapInfo != null) ? wrapInfo.childrenHeight : super.getMinorAxisChildrenSpan(boxView);
    }

    @Override
    protected void setMinorAxisChildrenSpan(EditorBoxView<EditorView> boxView, float minorAxisSpan) {
        if (wrapInfo != null) {
            wrapInfo.childrenHeight = minorAxisSpan;
        } else {
            super.setMinorAxisChildrenSpan(boxView, minorAxisSpan);
        }
    }

    @Override
    protected EditorView getEditorViewChildrenValid(EditorBoxView<EditorView> boxView, int index) {
        return get(index);
    }

    @Override
    protected void updateLayout(EditorBoxView<EditorView> boxView, VisualUpdate<EditorView> visualUpdate, Shape alloc) {
        double origWidth = boxView.getMajorAxisSpan();
        float origHeight = boxView.getMinorAxisSpan();
        
        recomputeLayout(boxView); // Recompute wrap lines

        double width = boxView.getMajorAxisSpan();
        float height = boxView.getMinorAxisSpan();
        boolean widthChanged = (origWidth != width);
        boolean heightChanged = (origHeight != height);

        if (alloc != null) {
            if (wrapInfo == null) {
                super.updateLayout(boxView, visualUpdate, alloc);
            } else {
                Rectangle2D.Double repaintBounds = ViewUtils.shape2Bounds(alloc);
                if (wrapInfo == null) {
                    repaintBounds.x += visualUpdate.visualOffset;
                    repaintBounds.width -= visualUpdate.visualOffset;
                } else {
                    // Possibly improve by computing exact bounds
                }
                if (widthChanged) {
                    visualUpdate.markWidthChanged();
                    repaintBounds.width = EXTEND_TO_END;
                } else { // Just repaint the modified area (of the same size)
                    // Leave the whole visible width for repaint
                    //repaintBounds.width = removedSpan;
                }
                if (heightChanged) {
                    visualUpdate.markHeightChanged();
                    repaintBounds.height = EXTEND_TO_END;
                } // else: leave the repaintBounds.height set to alloc's height
                visualUpdate.repaintBounds = ViewUtils.toRect(repaintBounds);
            }

        } else { // Null alloc => compatible operation
            if (widthChanged || heightChanged) {
                boxView.preferenceChanged(null, widthChanged, heightChanged);
            }
        }
    }

    /**
     * Recompute spans and possibly do wrap or vice versa (remove wrap).
     * This is used once a component's width gets changed.
     *
     * @param boxView non-null box view.
     */
    void recomputeLayout(EditorBoxView<EditorView> boxView) {
        ParagraphView paragraphView = (ParagraphView) boxView;
        DocumentView docView = paragraphView.getDocumentView();
        if (docView != null) {
            boolean wrapDone = false;
            double childrenWidth = getMajorAxisChildrenSpan(boxView);
            float childrenHeight = getMinorAxisChildrenSpan(boxView);
            // Do no word wrap in case there's RTL text anywhere in paragraph view
            if (docView.getLineWrapType() != DocumentView.LineWrapType.NONE && !paragraphView.isRTL()) {
                wrapInfo = null;
                float visibleWidth = docView.getVisibleWidth();
                // Check if major axis span (should already be updated) exceeds scrollpane width.
                if (visibleWidth > docView.getDefaultCharWidth() && childrenWidth > visibleWidth) {
                    wrapDone = true;
                    // Get current minor axis span before wrapInfo gets inited
                    // since the method behavior would get modified.
                    wrapInfo = new WrapInfo(childrenWidth, childrenHeight);
                    float prefWidth = new WrapInfoUpdater(wrapInfo, paragraphView).initWrapInfo();
                    float prefHeight = wrapInfo.preferredHeight();
                    boxView.setMajorAxisSpan(prefWidth);
                    boxView.setMinorAxisSpan(prefHeight);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("WrapInfo.init(): pref[" + prefWidth + "," + prefHeight + "] "
                                + wrapInfo.toString(paragraphView));
                    }
                }
            }
            if (!wrapDone) {
                boxView.setMajorAxisSpan(childrenWidth);
                boxView.setMinorAxisSpan(childrenHeight);
            }
        }
    }

    /**
     * Get view index corresponding to given visualOffset depending on present
     * TextLayout instances covering the line's views.
     * <br/>
     * For non-TextLayout views it gives regular results.
     *
     * @param boxView
     * @param visualOffset
     * @param last if set to false return first text-layout's view index.
     *   If true return last text-layout's view index.
     * @return structure with index and TextLayoutPart if any.
     */
    private ViewSearchResult getTextLayoutViewIndex(EditorBoxView<EditorView> boxView, double visualOffset, boolean last) {
        ViewSearchResult result = new ViewSearchResult(boxView, visualOffset);
        int high = size() - 1;
        if (high == -1) {
            result.index = -1;
            return result;
        }
        int low = 0;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            EditorView view = boxView.getEditorView(mid);
            if (view instanceof HighlightsView) {
                HighlightsView hView = (HighlightsView) view;
                Object layout = hView.layout();
                if (layout instanceof TextLayoutPart) {
                    TextLayoutPart part = (TextLayoutPart) layout;
                    int tlStartIndex = mid - part.index();
                    double tlStartVisualOffset = getViewVisualOffset(tlStartIndex);
                    double tlEndVisualOffset = getViewVisualOffset(tlStartIndex + part.viewCount());
                    if (visualOffset >= tlStartVisualOffset && visualOffset <= tlEndVisualOffset) {
                        // Found right text layout
                        result.index =  last ? tlStartIndex + part.viewCount() - 1 : tlStartIndex;
                        result.textLayoutPart = (TextLayoutPart) ((HighlightsView)boxView.getEditorView(
                                result.index)).layout();
                    }
                }
            }
            double midVisualOffset = getViewVisualOffset(mid);
            if (midVisualOffset < visualOffset) {
                low = mid + 1;
            } else if (midVisualOffset > visualOffset) {
                high = mid - 1;
            } else {
                // view starting exactly at the given visual offset found
                high = mid;
                break;
            }
        }
        result.index = Math.max(high, 0);
        result.view = boxView.getEditorView(result.index);
        return result;
    }

    private int getOffset(ViewSearchResult result) {
        assert (result.textLayoutPart.index() == 0); // Start part
        TextHitInfo hitInfo = HighlightsViewUtils.x2Index(result.textLayout(), (float) result.visualOffset);
        return result.view.getStartOffset() + hitInfo.getCharIndex();
    }

    private void updatePartViewIndex(ViewSearchResult result) {
        if (result.textLayoutPart != null) {
            int offset = getOffset(result);
            result.index = getViewIndex(offset, result.index, result.index + result.textLayoutPart.viewCount());
            result.view = result.boxView.getEditorView(result.index);
        }
    }

    @Override
    protected void paint(EditorBoxView<EditorView> boxView, Graphics2D g, Shape alloc, Rectangle clipBounds) {
        if (wrapInfo != null) {
            Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
            int startWrapLineIndex;
            int endWrapLineIndex;
            double relY = clipBounds.y - allocBounds.y;
            float wrapLineHeight = wrapInfo.childrenHeight;
            if (relY < wrapLineHeight) {
                startWrapLineIndex = 0;
            } else {
                startWrapLineIndex = (int) (relY / wrapLineHeight);
            }
            // Find end index
            relY += clipBounds.height + (wrapLineHeight - 1);
            if (relY >= boxView.getMinorAxisSpan()) {
                endWrapLineIndex = wrapInfo.wrapLineCount();
            } else {
                endWrapLineIndex = (int) (relY / wrapLineHeight);
            }
            wrapInfo.paintWrapLines(this, (ParagraphView)boxView,
                    startWrapLineIndex, endWrapLineIndex, g, alloc, clipBounds);
        } else {
            super.paint(boxView, g, alloc, clipBounds);
        }
    }

    @Override
    protected void paintChildren(EditorBoxView<EditorView> boxView, Graphics2D g, Shape alloc,
            Rectangle clipBounds, double startVisualOffset, double endVisualOffset)
    {
        if (((ParagraphView)boxView).isRTL()) {
            // Calculate bounds of whole text layouts since for RTL text latter index is more left visually
            int startIndex = Math.max(getTextLayoutViewIndex(boxView, startVisualOffset, false).index, 0); // Cover no-children case
            int endIndex = getTextLayoutViewIndex(boxView, endVisualOffset, true).index + 1;
            paintChildren(boxView, g, alloc, clipBounds, startIndex, endIndex);
        } else {
            super.paintChildren(boxView, g, alloc, clipBounds, startVisualOffset, endVisualOffset);
        }
    }

    @Override
    protected void paintChildren(EditorBoxView<EditorView> boxView, Graphics2D g, Shape alloc,
            Rectangle clipBounds, int startIndex, int endIndex)
    {
        // Paint backward to properly paint text layout parts
        for (int i = startIndex; i < endIndex; i++) {
            EditorView view = getEditorViewChildrenValid(boxView, i);
            Shape childAlloc = getChildAllocation(boxView, i, i + 1, alloc);

            if (view instanceof HighlightsView) {
                HighlightsView hView = (HighlightsView) view;
                Object layout = hView.layout();
                if (layout instanceof TextLayoutPart) {
                    // Always paint whole text layout (at least due to possible RTL text)
                    TextLayoutPart part = (TextLayoutPart) layout;
                    TextLayout textLayout = part.textLayout();
                    DocumentView docView = ((ParagraphView)boxView).getDocumentView();
                    int layoutStartViewIndex = i - part.index();
                    Shape textLayoutAlloc = getChildAllocation(boxView, layoutStartViewIndex,
                            layoutStartViewIndex + part.viewCount(), alloc);
                    Rectangle2D.Double textLayoutBounds = ViewUtils.shape2Bounds(textLayoutAlloc);
                    int endPartRelIndex = Math.min(layoutStartViewIndex + part.viewCount(), endIndex) -
                            layoutStartViewIndex;
                    TextHitInfo startHit = TextHitInfo.leading(part.offsetShift());
                    TextHitInfo endHit = TextLayoutUtils.endHit(boxView, part, layoutStartViewIndex,
                            endPartRelIndex);
                    // Since only a whole text layout can be rendered by its TL.draw() (in a single color)
                    // do the rendering in the following way:
                    // 1. All parts' backgrounds.
                    // 2. Whole text layout's text in "global" foreground color.
                    // 3. All parts' foregrounds (for part's "extra" foreground color
                    //        render whole TL in part's "extra" color clipped to part's bounds).
                    //
                    // In adition in step 3 the TL was already rendered in "global" color (in step 2)
                    // but this would interfere with TL's rendering in "extra" color (it looks blurry on screen).
                    // Therefore in step 3 the part's background must be cleared first.
                    //
                    // Updated steps:
                    // 1. All parts' backgrounds where (part.foreground() == null).
                    // 2. Whole text layout in "global" color.
                    // 3. Backgrounds of (part.foreground() != null) parts and all parts' foregrounds.
                    //
                    for (int j = part.index(); j < endPartRelIndex; j++) {
                        HighlightsView partView = (HighlightsView) boxView.getEditorView(layoutStartViewIndex + j);
                        TextLayoutPart paintPart = (TextLayoutPart) partView.layout();
                        if (paintPart.foreground() == null) {
                            int shift = paintPart.offsetShift();
                            int length = partView.getLength();
                            TextHitInfo shiftHit = TextHitInfo.leading(shift);
                            TextHitInfo shiftLengthHit = TextHitInfo.leading(shift + length);
                            Shape partAlloc = TextLayoutUtils.getRealAlloc(textLayout, textLayoutBounds,
                                    shiftHit, shiftLengthHit);
                            partView.partPaintBackground(g, partAlloc, textLayoutBounds, clipBounds);
                        }
                    }

                    // Render textLayout
                    Shape origClip = g.getClip();
                    Color origColor = g.getColor();
                    try {
                        Shape renderTextLayoutAlloc = TextLayoutUtils.getRealAlloc(
                                textLayout, textLayoutBounds,
                                startHit, endHit);
                        Color foreColor = part.textLayoutForeground();
                        g.setColor(foreColor);
                        g.clip(renderTextLayoutAlloc);
                        HighlightsViewUtils.paintTextLayout(g, textLayoutBounds, textLayout, docView);
                    } finally {
                        g.setColor(origColor);
                        g.setClip(origClip);
                    }

                    // Render foregrounds
                    for (int j = part.index(); j < endPartRelIndex; j++) {
                        HighlightsView partView = (HighlightsView) boxView.getEditorView(layoutStartViewIndex + j);
                        TextLayoutPart paintPart = (TextLayoutPart) partView.layout();
                        int shift = paintPart.offsetShift();
                        int length = partView.getLength();
                        TextHitInfo shiftHit = TextHitInfo.leading(shift);
                        TextHitInfo shiftLengthHit = TextHitInfo.leading(shift + length);
                        Shape partAlloc = TextLayoutUtils.getRealAlloc(textLayout, textLayoutBounds,
                                shiftHit, shiftLengthHit);
                        if (paintPart.foreground() != null) {
                            partView.partPaintBackground(g, partAlloc, textLayoutBounds, clipBounds);
                        }
                        partView.partPaintForeground(g, partAlloc, textLayoutBounds, clipBounds);
                    }

                    i = layoutStartViewIndex + endPartRelIndex - 1;
                    continue;
                } // For TextLayout use regular painting (below)
            }

            view.paint(g, childAlloc, clipBounds);
        }
    }
    
    @Override
    public Shape modelToViewChecked(EditorBoxView<EditorView> boxView, int offset, Shape alloc, Bias bias) {
        if (wrapInfo != null) {
            int wrapLineIndex = findWrapLineIndex(boxView, offset);
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

            if (wrapLine.startViewPart != null && offset < wrapLine.startViewPart.getEndOffset()) {
                Shape startPartAlloc = startPartAlloc(wrapLineBounds, wrapLine);
                if (logBuilder != null) {
                    logBuilder.append("START-part:").append(ViewUtils.toString(startPartAlloc)); // NOI18N
                }
                ret = wrapLine.startViewPart.modelToViewChecked(offset, startPartAlloc, bias);
            } else if (wrapLine.endViewPart != null && offset >= wrapLine.endViewPart.getStartOffset()) {
                Shape endPartAlloc = endPartAlloc(wrapLineBounds, wrapLine, boxView);
                if (logBuilder != null) {
                    logBuilder.append("END-part:").append(ViewUtils.toString(endPartAlloc)); // NOI18N
                }
                // getPreferredSpan() perf should be ok since part-view should cache the TextLayout
                ret = wrapLine.endViewPart.modelToViewChecked(offset, endPartAlloc, bias);
            } else {
                assert (wrapLine.hasFullViews()) : wrapInfo.dumpWrapLine(boxView, wrapLineIndex);
                for (int i = wrapLine.startViewIndex; i < wrapLine.endViewIndex; i++) {
                    EditorView view = boxView.getEditorView(i);
                    if (offset < view.getEndOffset()) {
                        Shape viewAlloc = viewAlloc(wrapLineBounds, wrapLine, i, boxView);
                        if (view instanceof HighlightsView) {
                            // Give index hint
                            ret = ((HighlightsView)view).modelToViewChecked(offset, viewAlloc, bias, i);
                        } else {
                            ret = view.modelToViewChecked(offset, viewAlloc, bias);
                        }
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

        } else {
            // [TODO] Once modified to HighlightsView == single TextLayout => use super call again
            int index = getViewIndex(offset, bias);
            if (index >= 0) { // When at least one child the index will fit one of them
                // First find valid child (can lead to change of child allocation bounds)
                EditorView view = getEditorViewChildrenValid(boxView, index);
                Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
                // Update the bounds with child.modelToView()
                if (view instanceof HighlightsView) {
                    // Give index hint
                    return ((HighlightsView) view).modelToViewChecked(offset, childAlloc, bias, index);
                } else {
                    return view.modelToViewChecked(offset, childAlloc, bias);
                }
            } else { // No children => fallback by leaving the given bounds
                return alloc;
            }
        }
    }

    @Override
    public int viewToModelChecked(EditorBoxView<EditorView> boxView, double x, double y, Shape alloc, Bias[] biasReturn) {
        if (wrapInfo != null) {
            int wrapLineIndex = findWrapLineIndex(alloc, y);
            Shape wrapLineAlloc = wrapLineAlloc(alloc, wrapLineIndex);
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            IndexAndAlloc indexAndAlloc = findIndexAndAlloc(boxView, x, wrapLineAlloc, wrapLine);
            return indexAndAlloc.viewOrPart.viewToModelChecked(x, y, indexAndAlloc.alloc, biasReturn);
        } else {
            if (((ParagraphView)boxView).isRTL()) {
                Rectangle2D.Double relXY = ViewUtils.shape2RelBounds(alloc, x, y);
                ViewSearchResult result = getTextLayoutViewIndex(boxView, relXY.getX(), false);
                if (result.textLayoutPart != null) { // first part
                    return getOffset(result);
                }
            }
            // [TODO] Once modified to HighlightsView == single TextLayout => use super call again
            int index = getViewIndexAtPoint(boxView, x, y, alloc);
            int offset;
            if (index >= 0) {
                // First find valid child (can lead to change of child allocation bounds)
                EditorView view = getEditorViewChildrenValid(boxView, index);
                Shape childAlloc = getChildAllocation(boxView, index, index + 1, alloc);
                // forward to the child view
                if (view instanceof HighlightsView) {
                    // Give index hint
                    offset = ((HighlightsView)view).viewToModelChecked(x, y, childAlloc, biasReturn, index);
                } else {
                    offset = view.viewToModelChecked(x, y, childAlloc, biasReturn);
                }
            } else { // at the end
                offset = boxView.getStartOffset();
            }
            return offset;
        }
    }

    /**
     * Find next visual position in Y direction.
     * In case of no linewrap the method should return -1 for a given valid offset parameter.
     * For offset -1 the method should find position best corresponding to x parameter.
     * If linewrap is active the method should go through the particular wraplines.
     * @param offset offset inside line or -1 to "enter" a line at the given x.
     * @param x x-position corresponding to magic caret position.
     */
    int getNextVisualPositionY(EditorBoxView<EditorView> boxView,
            int offset, Bias bias, Shape alloc, int direction, Bias[] biasRet, double x)
    {
        switch (direction) {
            case SwingConstants.NORTH:
                int retOffsetNorth;
                if (offset == -1) {
                    if (wrapInfo != null) { // Use last wrap line
                        int lastWrapLineIndex = wrapInfo.wrapLineCount() - 1;
                        retOffsetNorth = visualPositionOnWrapLine(boxView, alloc, biasRet, x, lastWrapLineIndex);
                    } else { // wrapInfo == null; offset == -1
                        retOffsetNorth = visualPosition(boxView, alloc, biasRet, x);
                    }
                } else { // offset != -1
                    if (wrapInfo != null) {
                        int wrapLineIndex = findWrapLineIndex(boxView, offset);
                        if (wrapLineIndex > 0) {
                            retOffsetNorth = visualPositionOnWrapLine(boxView, alloc, biasRet, x, wrapLineIndex - 1);
                        } else {
                            retOffsetNorth = -1;
                        }
                    } else { // wrapInfo == null
                        retOffsetNorth = -1;
                    }
                }
                return retOffsetNorth;

            case SwingConstants.SOUTH:
                int retOffsetSouth;
                if (offset == -1) {
                    if (wrapInfo != null) { // Use first wrap line
                        retOffsetSouth = visualPositionOnWrapLine(boxView, alloc, biasRet, x, 0);
                    } else { // wrapInfo == null; offset == -1
                        retOffsetSouth = visualPosition(boxView, alloc, biasRet, x);
                    }
                } else { // offset != -1
                    if (wrapInfo != null) {
                        int wrapLineIndex = findWrapLineIndex(boxView, offset);
                        if (wrapLineIndex < wrapInfo.wrapLineCount() - 1) {
                            retOffsetSouth = visualPositionOnWrapLine(boxView, alloc, biasRet, x, wrapLineIndex + 1);
                        } else {
                            retOffsetSouth = -1;
                        }
                    } else { // wrapInfo == null
                        retOffsetSouth = -1;
                    }
                }
                return retOffsetSouth;

            case SwingConstants.EAST: // Should be handled elsewhere
            case SwingConstants.WEST: // Should be handled elsewhere
                throw new IllegalStateException("Not intended to handle EAST and WEST directions"); // NOI18N
            default:
                throw new IllegalArgumentException("Bad direction: " + direction); // NOI18N
        }
    }
    
    private int visualPositionOnWrapLine(EditorBoxView<EditorView> boxView,
            Shape alloc, Bias[] biasRet, double x, int wrapLineIndex)
    {
        WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
        Shape wrapLineAlloc = wrapLineAlloc(alloc, wrapLineIndex);
        IndexAndAlloc indexAndAlloc = findIndexAndAlloc(boxView, x, wrapLineAlloc, wrapLine);
        double y = ViewUtils.shapeAsRect(indexAndAlloc.alloc).getY();
        return indexAndAlloc.viewOrPart.viewToModelChecked(x, y, indexAndAlloc.alloc, biasRet);
    }
    
    private int visualPosition(EditorBoxView<EditorView> boxView, Shape alloc, Bias[] biasRet, double x) {
        Rectangle2D allocRect = ViewUtils.shapeAsRect(alloc);
        double y = allocRect.getY();
        int childIndex;
        if (((ParagraphView)boxView).isRTL()) {
            ViewSearchResult result = getTextLayoutViewIndex(boxView, x - allocRect.getX(), false);
            if (result.textLayoutPart != null) {
                return getOffset(result);
            }
            childIndex = result.index;
        } else {
            childIndex = getViewIndexAtPoint(boxView, x, y, alloc);
        }
        EditorView child = boxView.getEditorView(childIndex);
        Shape childAlloc = boxView.getChildAllocation(childIndex, alloc);
        return child.viewToModelChecked(x, y, childAlloc, biasRet);
    }

    private IndexAndAlloc findIndexAndAlloc(EditorBoxView<EditorView> boxView,
            double x, Shape wrapLineAlloc, WrapLine wrapLine)
    {
        IndexAndAlloc indexAndAlloc = new IndexAndAlloc();
        if (wrapLine.startViewPart != null && (x < wrapLine.startViewX ||
                (!wrapLine.hasFullViews() && wrapLine.endViewPart == null)))
        {
            indexAndAlloc.index = -1; // start part
            indexAndAlloc.viewOrPart = wrapLine.startViewPart;
            indexAndAlloc.alloc = startPartAlloc(wrapLineAlloc, wrapLine);
            return indexAndAlloc;
        }
        // Go through full views
        if (wrapLine.hasFullViews()) {
            Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
            viewBounds.x += wrapLine.startViewX;
            double lastVisualOffset = boxView.getViewVisualOffset(wrapLine.startViewIndex);
            for (int i = wrapLine.startViewIndex; i < wrapLine.endViewIndex; i++) {
                double nextVisualOffset = boxView.getViewVisualOffset(i + 1);
                viewBounds.width = nextVisualOffset - lastVisualOffset;
                if (x < viewBounds.x + viewBounds.width || // Fits
                        (i == wrapLine.endViewIndex - 1 && wrapLine.endViewPart == null)) // Last part and no end part
                {
                    indexAndAlloc.index = i;
                    indexAndAlloc.viewOrPart = boxView.getEditorView(i);
                    indexAndAlloc.alloc = viewBounds;
                    return indexAndAlloc;
                }
                viewBounds.x += viewBounds.width;
                lastVisualOffset = nextVisualOffset;
            }
            // Force last in case there is no end part
        }
        assert (wrapLine.endViewPart != null) : "Null endViewPart"; // NOI18N
        // getPreferredSpan() perf should be ok since part-view should cache the TextLayout
        indexAndAlloc.index = -2;
        indexAndAlloc.viewOrPart = wrapLine.endViewPart;
        indexAndAlloc.alloc = endPartAlloc(wrapLineAlloc, wrapLine, boxView);
        return indexAndAlloc;
    }

    private int findWrapLineIndex(Shape alloc, double y) {
        Rectangle2D allocRect = ViewUtils.shapeAsRect(alloc);
        int wrapLineIndex;
        double relY = y - allocRect.getY();
        float wrapLineHeight = wrapInfo.childrenHeight;
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
    
    private int findWrapLineIndex(EditorBoxView<EditorView> boxView, int offset) {
        int wrapLineCount = wrapInfo.wrapLineCount();
        int wrapLineIndex = 0;
        WrapLine wrapLine = null;
        while (++wrapLineIndex < wrapLineCount) {
            wrapLine = wrapInfo.get(wrapLineIndex);
            if (getWrapLineStartOffset(boxView, wrapLine) > offset) {
                break;
            }
        }
        wrapLineIndex--;
        return wrapLineIndex;
    }

    private Rectangle2D.Double wrapLineAlloc(Shape alloc, int wrapLineIndex) {
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        allocBounds.y += wrapLineIndex * wrapInfo.childrenHeight;
        allocBounds.height = wrapInfo.childrenHeight;
        return allocBounds;
    }
    
    private Shape startPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine) {
        Rectangle2D.Double startPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        startPartBounds.width = wrapLine.startViewX;
        return startPartBounds;
    }
    
    private Shape endPartAlloc(Shape wrapLineAlloc, WrapLine wrapLine, EditorBoxView<EditorView> boxView) {
        Rectangle2D.Double endPartBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        endPartBounds.width = wrapLine.endViewPart.getPreferredSpan(View.X_AXIS);
        endPartBounds.x += wrapLine.startViewX;
        if (wrapLine.hasFullViews()) {
            endPartBounds.x += (boxView.getViewVisualOffset(wrapLine.endViewIndex)
                    - boxView.getViewVisualOffset(wrapLine.startViewIndex));
        }
        return endPartBounds;
    }
    
    private Shape viewAlloc(Shape wrapLineAlloc, WrapLine wrapLine, int viewIndex, EditorBoxView<EditorView> boxView) {
        double startViewVisualOffset = boxView.getViewVisualOffset(wrapLine.startViewIndex);
        double visualOffset = (viewIndex != wrapLine.startViewIndex)
                ? boxView.getViewVisualOffset(viewIndex)
                : startViewVisualOffset;
        Rectangle2D.Double viewBounds = ViewUtils.shape2Bounds(wrapLineAlloc);
        viewBounds.x += wrapLine.startViewX + (visualOffset - startViewVisualOffset);
        viewBounds.width = (boxView.getViewVisualOffset(viewIndex + 1) - visualOffset);
        return viewBounds;
    }

    private int getWrapLineStartOffset(EditorBoxView<EditorView> boxView, WrapLine wrapLine) {
        if (wrapLine.startViewPart != null) {
            return wrapLine.startViewPart.getStartOffset();
        } else if (wrapLine.hasFullViews()) {
            return boxView.getView(wrapLine.startViewIndex).getStartOffset();
        } else {
            assert (wrapLine.endViewPart != null) : "Invalid wrapLine: " + wrapLine;
            return wrapLine.endViewPart.getStartOffset();
        }
    }

    @Override
    public StringBuilder appendChildrenInfo(EditorBoxView<EditorView> boxView,
            StringBuilder sb, int indent, int importantIndex)
    {
        super.appendChildrenInfo(boxView, sb, indent, importantIndex);
        if (wrapInfo != null) {
            wrapInfo.appendInfo(sb, (ParagraphView)boxView, indent);
        }
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

    private static final class ViewSearchResult {

        /**
         * Box View.
         */
        final EditorBoxView<EditorView> boxView;

        /**
         * Visual x-offset of the search.
         */
        final double visualOffset;
        
        /**
         * View index.
         */
        int index;
        
        /**
         * View at index or null for invalid index.
         */
        EditorView view;

        /**
         * TextLayoutPart or null if the view at the index is not a text layout part.
         */
        TextLayoutPart textLayoutPart;

        public ViewSearchResult(EditorBoxView<EditorView> boxView, double visualOffset) {
            this.boxView = boxView;
            this.visualOffset = visualOffset;
        }
        
        TextLayout textLayout() {
            return textLayoutPart.textLayout();
        }
        
    }

}
