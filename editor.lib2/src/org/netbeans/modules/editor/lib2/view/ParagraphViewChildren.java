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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;


/**
 * View of a visual line that is capable of doing word-wrapping.
 * 
 * @author Miloslav Metelka
 */

public final class ParagraphViewChildren extends EditorBoxViewChildren {

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
    protected boolean extendLastViewAllocation() {
        return true;
    }

    @Override
    protected double getMajorAxisChildrenSpan(EditorBoxView boxView) {
        return (wrapInfo != null) ? wrapInfo.majorAxisChildrenSpan : super.getMajorAxisChildrenSpan(boxView);
    }

    @Override
    protected void setMajorAxisChildrenSpan(EditorBoxView boxView, double majorAxisSpan) {
        if (wrapInfo != null) {
            wrapInfo.majorAxisChildrenSpan = majorAxisSpan;
        } else {
            super.setMajorAxisChildrenSpan(boxView, majorAxisSpan);
        }
    }

    @Override
    protected float getMinorAxisChildrenSpan(EditorBoxView boxView) {
        return (wrapInfo != null) ? wrapInfo.minorAxisChildrenSpan : super.getMinorAxisChildrenSpan(boxView);
    }

    @Override
    protected void setMinorAxisChildrenSpan(EditorBoxView boxView, float minorAxisSpan) {
        if (wrapInfo != null) {
            wrapInfo.minorAxisChildrenSpan = minorAxisSpan;
        } else {
            super.setMinorAxisChildrenSpan(boxView, minorAxisSpan);
        }
    }

    @Override
    protected void updateSpans(EditorBoxView boxView, EditorBoxView.ReplaceResult result,
            int index, int removedCount, int addedCount,
            boolean majorAxisSpanChange, double visualOffset,
            double addedVisualSpan, double removedVisualSpan, boolean removedTillEnd,
            boolean minorAxisSpanChange, Shape alloc)
    {
        ParagraphView paragraphView = (ParagraphView) boxView;
        DocumentView docView = paragraphView.getDocumentView();
        if (docView != null) {
            boolean regular = true;
            switch (docView.getLineWrapType()) {
                case NONE:

                case CHARACTER_BOUND:
                case WORD_BOUND:
                    double origWidth = boxView.getMajorAxisSpan();
                    float origHeight = boxView.getMinorAxisSpan();
                    if (wrapInfo != null) {
                        boxView.setMajorAxisSpan(wrapInfo.majorAxisChildrenSpan);
                        boxView.setMinorAxisSpan(wrapInfo.minorAxisChildrenSpan);
                        wrapInfo = null; // Currently always recreate wrapInfo [TODO] also allow update of existing
                    }
                    float visibleWidth = docView.getVisibleWidth();
                    // Check if major axis span (should already be updated) exceeds scrollpane width.
                    double majorAxisChildrenSpan = getMajorAxisChildrenSpan(boxView);
                    if (visibleWidth > docView.getDefaultCharWidth() && majorAxisChildrenSpan > visibleWidth) {
                        regular = false;
                        // Get current minor axis span before wrapInfo gets inited
                        // since the method behavior would get modified.
                        float minorAxisChildrenSpan = getMinorAxisChildrenSpan(boxView);
                        wrapInfo = new WrapInfo(majorAxisChildrenSpan, minorAxisChildrenSpan);
                        float prefWidth = new WrapInfoUpdater(wrapInfo, this, paragraphView).initWrapInfo();
                        float prefHeight = wrapInfo.preferredHeight();
                        boxView.setMajorAxisSpan(prefWidth);
                        boxView.setMinorAxisSpan(prefHeight);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("WrapInfo.init(): pref[" + prefWidth + "," + prefHeight + "] "
                                    + wrapInfo.toString(paragraphView));
                        }

                        majorAxisSpanChange = (origWidth != boxView.getMajorAxisSpan());
                        minorAxisSpanChange = (origHeight != boxView.getMinorAxisSpan());
                        if (alloc != null) {
                            Rectangle2D.Double repaintBounds = ViewUtils.shape2Bounds(alloc);
                            repaintBounds.x += visualOffset;
                            if (majorAxisSpanChange || removedTillEnd) {
                                result.widthChanged = true;
                                repaintBounds.width = (double) Integer.MAX_VALUE; // Extend to end
                            } else { // Just repaint the modified area (of the same size)
                                // Leave the whole visible width for repaint
                                //repaintBounds.width = removedSpan;
                            }
                            if (minorAxisSpanChange) {
                                result.heightChanged = true;
                                repaintBounds.height = (double) Integer.MAX_VALUE; // Extend to end
                            } // else: leave the repaintBounds.height set to alloc's height
                            result.repaintBounds = ViewUtils.toRect(repaintBounds);

                        } else { // Null alloc => compatible operation
                            if (majorAxisSpanChange || minorAxisSpanChange) {
                                boxView.preferenceChanged(null, majorAxisSpanChange, minorAxisSpanChange);
                            }
                        }
                    }
                    break;

                default:
                    throw new AssertionError();
            }

            if (regular) {
                super.updateSpans(boxView, result, index, removedCount, addedCount,
                        majorAxisSpanChange, visualOffset, addedVisualSpan, removedVisualSpan, removedTillEnd,
                        minorAxisSpanChange, alloc);
            }
        }
    }

    @Override
    protected void paint(EditorBoxView boxView, Graphics2D g, Shape alloc, Rectangle clipBounds) {
        if (wrapInfo != null) {
            Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
            int startWrapLineIndex;
            int endWrapLineIndex;
            double relY = clipBounds.y - allocBounds.y;
            float wrapLineHeight = wrapInfo.minorAxisChildrenSpan;
            if (relY < wrapLineHeight) {
                startWrapLineIndex = 0;
            } else {
                startWrapLineIndex = (int) (relY / wrapLineHeight);
            }
            // Find end index
            relY += clipBounds.height + (wrapLineHeight - 1);
            if (relY >= boxView.getMinorAxisSpan()) {
                endWrapLineIndex = wrapInfo.size();
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
    public Shape modelToViewChecked(EditorBoxView boxView, int offset, Shape alloc, Bias bias) {
        if (wrapInfo != null) {
            Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
            int wrapLineCount = wrapInfo.size();
            int wrapLineIndex = 0;
            WrapLine wrapLine = null;
            while (++wrapLineIndex < wrapLineCount) {
                wrapLine = wrapInfo.get(wrapLineIndex);
                if (getWrapLineStartOffset(boxView, wrapLine) > offset) {
                    break;
                }
            }
            wrapLineIndex--;
            wrapLine = wrapInfo.get(wrapLineIndex);
            allocBounds.y += wrapLineIndex * wrapInfo.minorAxisChildrenSpan;
            allocBounds.height = wrapInfo.minorAxisChildrenSpan;
            Shape ret = null;

            if (wrapLine.startViewPart != null && offset < wrapLine.startViewPart.getEndOffset()) {
                ret = wrapLine.startViewPart.modelToViewChecked(offset, allocBounds, bias);
                allocBounds.width = wrapLine.startViewX;
            } else if (wrapLine.endViewPart != null && offset >= wrapLine.endViewPart.getStartOffset()) {
                assert (wrapLine.endViewPart != null) : "Invalid wrapLine: " + wrapLine; // NOI18N
                allocBounds.x += wrapLine.startViewX;
                if (wrapLine.hasFullViews()) {
                    allocBounds.x += (boxView.getViewVisualOffset(wrapLine.endViewIndex) -
                        boxView.getViewVisualOffset(wrapLine.startViewIndex));
                }
                allocBounds.width = wrapLine.endViewPart.getPreferredSpan(View.X_AXIS);
                ret = wrapLine.endViewPart.modelToViewChecked(offset, allocBounds, bias);
            } else {
                assert (wrapLine.hasFullViews()) : wrapInfo.dumpWrapLine(boxView, wrapLineIndex);
                for (int i = wrapLine.startViewIndex; i < wrapLine.endViewIndex; i++) {
                    if (offset < boxView.getEditorView(i).getEndOffset()) {
                        double startVisualOffset = boxView.getViewVisualOffset(wrapLine.startViewIndex);
                        double visualOffset = (i != wrapLine.startViewIndex)
                                ? boxView.getViewVisualOffset(i)
                                : startVisualOffset;
                        allocBounds.x += wrapLine.startViewX + (visualOffset - startVisualOffset);
                        allocBounds.width = (boxView.getViewVisualOffset(i + 1) - visualOffset);
                        ret = boxView.getEditorView(i).modelToViewChecked(offset, allocBounds, bias);
                        assert (ret != null);
                        break;
                    }
                }
            }
            return ret;

        } else {
            return super.modelToViewChecked(boxView, offset, alloc, bias);
        }
    }

    private int getWrapLineStartOffset(EditorBoxView boxView, WrapLine wrapLine) {
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
    public int viewToModelChecked(EditorBoxView boxView, double x, double y, Shape alloc, Bias[] biasReturn) {
        if (wrapInfo != null) {
            Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
            int wrapLineIndex;
            double relY = y - allocBounds.y;
            float wrapLineHeight = wrapInfo.minorAxisChildrenSpan;
            if (relY < wrapLineHeight) {
                wrapLineIndex = 0;
            } else {
                wrapLineIndex = (int) (relY / wrapLineHeight);
            }
            allocBounds.y += relY;
            allocBounds.height = wrapInfo.minorAxisChildrenSpan;
            WrapLine wrapLine = wrapInfo.get(wrapLineIndex);
            if (wrapLine.startViewPart != null && (x < wrapLine.startViewX ||
                    (!wrapLine.hasFullViews() && wrapLine.endViewPart == null)))
            {
                allocBounds.width = wrapLine.startViewX;
                return wrapLine.startViewPart.viewToModelChecked(x, y, allocBounds, biasReturn);
            }
            allocBounds.x += wrapLine.startViewX;
            if (wrapLine.hasFullViews()) {
                double lastVisualOffset = boxView.getViewVisualOffset(wrapLine.startViewIndex);
                for (int i = wrapLine.startViewIndex; i < wrapLine.endViewIndex; i++) {
                    double nextVisualOffset = boxView.getViewVisualOffset(i + 1);
                    allocBounds.width = nextVisualOffset - lastVisualOffset;
                    if (x < allocBounds.x + allocBounds.width) {
                        return boxView.getEditorView(i).viewToModelChecked(x, y, allocBounds, biasReturn);
                    }
                    allocBounds.x += allocBounds.width;
                    lastVisualOffset = nextVisualOffset;
                }
                // Force last in case there is no end part
                if (wrapLine.endViewPart == null) {
                    allocBounds.x -= allocBounds.width; // go back and retain last child's width
                    return boxView.getEditorView(wrapLine.endViewIndex - 1).viewToModelChecked(
                            x, y, allocBounds, biasReturn);
                }
            }
            assert (wrapLine.endViewPart != null) : "Null endViewPart"; // NOI18N
            allocBounds.width = wrapLine.endViewPart.getPreferredSpan(View.X_AXIS);
            return wrapLine.endViewPart.viewToModelChecked(x, y, allocBounds, biasReturn);
        } else {
            return super.viewToModelChecked(boxView, x, y, alloc, biasReturn);
        }
    }

    @Override
    public StringBuilder appendChildrenInfo(EditorBoxView boxView, StringBuilder sb, int indent, int importantIndex) {
        super.appendChildrenInfo(boxView, sb, indent, importantIndex);
        if (wrapInfo != null) {
            wrapInfo.appendInfo(sb, (ParagraphView)boxView, indent);
        }
        return sb;
    }

}
