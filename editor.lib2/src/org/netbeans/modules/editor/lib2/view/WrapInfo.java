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
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.editor.util.GapList;


/**
 * Information about line wrapping that may be attached to {@link ParagraphViewChildren}.
 * 
 * @author Miloslav Metelka
 */

final class WrapInfo extends GapList<WrapLine> {

    // -J-Dorg.netbeans.modules.editor.lib2.view.WrapInfo.level=FINER
    private static final Logger LOG = Logger.getLogger(WrapInfo.class.getName());

    private static final long serialVersionUID  = 0L;

    double childrenWidth; // 32 bytes = 24-super + 8

    float childrenHeight; // 36 bytes = 32 + 4

    WrapInfo(double childrenWidth, float childrenHeight) {
        super(2);
        this.childrenWidth = childrenWidth;
        this.childrenHeight = childrenHeight;
    }

    float preferredHeight() {
        return size() * childrenHeight;
    }

    void paintWrapLines(ParagraphViewChildren children, ParagraphView paragraphView,
            int startIndex, int endIndex,
            Graphics2D g, Shape alloc, Rectangle clipBounds)
    {
        DocumentView docView = paragraphView.getDocumentView();
        if (docView == null) { // Not paint unless connected to hierarchy
            return;
        }
        TextLayout lineContinuationTextLayout = docView.getLineContinuationCharTextLayout();
        Rectangle2D.Double allocBounds = ViewUtils.shape2Bounds(alloc);
        double allocOrigX = allocBounds.x;
        allocBounds.y += startIndex * childrenHeight;
        allocBounds.height = childrenHeight; // Stays for whole rendering
        int lastWrapLineIndex = size() - 1;
        for (int i = startIndex; i < endIndex; i++) {
            WrapLine wrapLine = get(i);
            EditorView startViewPart = wrapLine.startViewPart;
            if (startViewPart != null) {
                float width = startViewPart.getPreferredSpan(View.X_AXIS);
                allocBounds.width = width;
                startViewPart.paint(g, allocBounds, clipBounds);
                allocBounds.x += width;
            }
            if (wrapLine.hasFullViews()) { // Render the views
                int j = wrapLine.startViewIndex;
                double nextVisualOffset = paragraphView.getViewVisualOffset(j);
                assert (wrapLine.endViewIndex <= children.size()) : "Invalid for endViewIndex=" + // NOI18N
                        wrapLine.endViewIndex + ", wrapInfo:\n" + // NOI18N
                        this.toString(paragraphView) + "\nParagraphView:\n" + paragraphView; // NOI18N
                for (;j < wrapLine.endViewIndex; j++) {
                    EditorView childView = children.get(j);
                    double visualOffset = nextVisualOffset;
                    nextVisualOffset = paragraphView.getViewVisualOffset(j + 1);
                    allocBounds.width = nextVisualOffset - visualOffset;
                    childView.paint(g, allocBounds, clipBounds);
                    allocBounds.x += allocBounds.width;
                }
            }
            EditorView endViewPart = wrapLine.endViewPart;
            if (endViewPart != null) {
                float width = endViewPart.getPreferredSpan(View.X_AXIS);
                allocBounds.width = width;
                endViewPart.paint(g, allocBounds, clipBounds);
                allocBounds.x += width;
            }
            // Paint wrap mark
            if (i != lastWrapLineIndex) { // but not on last wrap line
                PaintState paintState = PaintState.save(g);
                try {
                    HighlightsView.paintForeground(g, allocBounds, docView, lineContinuationTextLayout,
                            paragraphView.getAttributes());
                } finally {
                    paintState.restore();
                }
            }
            allocBounds.x = allocOrigX;
            allocBounds.y += childrenHeight;
        }
    }

    public void checkIntegrity(ParagraphView paragraphView) {
        if (LOG.isLoggable(Level.FINER)) {
            String err = findIntegrityError(paragraphView);
            if (err != null) {
                String msg = "WrapInfo INTEGRITY ERROR! - " + err;
                LOG.finer(msg + "\n");
                LOG.finer(toString(paragraphView)); // toString() impl should append newline
                // For finest level stop throw real ISE otherwise just log the stack
                if (LOG.isLoggable(Level.FINEST)) {
                    throw new IllegalStateException(msg);
                } else {
                    LOG.log(Level.INFO, msg, new Exception());
                }
            }
        }
    }

    public String findIntegrityError(ParagraphView paragraphView) {
        String err = null;
        int lastOffset = paragraphView.getStartOffset();
        for (int i = 0; i < size(); i++) {
            WrapLine wrapLine = get(i);
            EditorView startViewPart = wrapLine.startViewPart;
            boolean nonEmptyLine = false;
            if (startViewPart != null) {
                nonEmptyLine = true;
                if (startViewPart.getStartOffset() != lastOffset) {
                    err = "startViewPart.getStartOffset()=" + startViewPart.getStartOffset() +
                            " != lastOffset=" + lastOffset;
                }
                lastOffset = startViewPart.getEndOffset();
            }
            int startViewIndex = wrapLine.startViewIndex;
            int endViewIndex = wrapLine.endViewIndex;
            if (startViewIndex != endViewIndex) {
                nonEmptyLine = true;
                boolean validIndices = true;
                if (startViewIndex < 0) {
                    validIndices = false;
                    if (err == null) {
                        err = "startViewIndex=" + startViewIndex + " < 0";
                    }
                }
                if (endViewIndex < startViewIndex) {
                    validIndices = false;
                    if (err == null) {
                        err = "endViewIndex=" + endViewIndex + " < startViewIndex=" + startViewIndex;
                    }
                }
                if (endViewIndex > paragraphView.getViewCount()) {
                    validIndices = false;
                    if (err == null) {
                        err = "endViewIndex=" + endViewIndex + " > getViewCount()=" + paragraphView.getViewCount();
                    }
                }
                if (validIndices) {
                    EditorView childView = paragraphView.getEditorView(startViewIndex);
                    if (err == null && childView.getStartOffset() != lastOffset) {
                        err = "startChildView.getStartOffset()=" + childView.getStartOffset()
                                + " != lastOffset=" + lastOffset;
                    }
                    childView = paragraphView.getEditorView(endViewIndex - 1);
                    lastOffset = childView.getEndOffset();
                }
            }
            EditorView endViewPart = wrapLine.endViewPart;
            if (endViewPart != null) {
                nonEmptyLine = true;
                if (err == null && lastOffset != endViewPart.getStartOffset()) {
                    err = "endViewPart.getStartOffset()=" + endViewPart.getStartOffset() +
                            " != lastOffset=" + lastOffset;
                }
                lastOffset = endViewPart.getEndOffset();
            }
            if (!nonEmptyLine && err == null) {
                err = "Empty";
            }
            if (err != null) {
                err = "WrapLine[" + i + "]: " + err;
                break;
            }
        }
        return err;
    }

    String dumpWrapLine(EditorBoxView boxView, int wrapLineIndex) {
        return "Invalid wrapLine["  + wrapLineIndex + "]:\n" + toString((ParagraphView)boxView);
    }

    public String appendInfo(StringBuilder sb, ParagraphView paragraphView, int indent) { // Expected to append newline at end
        sb.append("\n");
        ArrayUtilities.appendSpaces(sb, indent);
        sb.append("childrenSpan:[").append(childrenWidth);
        sb.append(",").append(childrenHeight);
        sb.append("], realSpan:[");
        if (paragraphView != null) {
            sb.append(paragraphView.getMajorAxisSpan()).append(",");
            sb.append(paragraphView.getMinorAxisSpan());
        } else {
            sb.append("<NULL>");
        }
        sb.append("]");
        DocumentView docView;
        if (paragraphView != null && ((docView = paragraphView.getDocumentView()) != null)) {
            float visibleWidth = docView.getVisibleWidth();
            sb.append(" visibleWidth=").append(visibleWidth);
        }
        int wrapLineCount = size();
        int digitCount = ArrayUtilities.digitCount(wrapLineCount);
        for (int i = 0; i < wrapLineCount; i++) {
            sb.append("\n");
            ArrayUtilities.appendSpaces(sb, indent + 2);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            WrapLine wrapLine = get(i);
            sb.append("SV:");
            EditorView startViewPart = wrapLine.startViewPart;
            if (startViewPart != null) {
                sb.append("<").append(startViewPart.getStartOffset()).append(",");
                sb.append(startViewPart.getEndOffset()).append(">");
            } else {
                sb.append("NULL");
            }
            sb.append("; x=").append(wrapLine.startViewX);
            int startViewIndex = wrapLine.startViewIndex;
            int endViewIndex = wrapLine.endViewIndex;
            sb.append(" [").append(startViewIndex).append(",");
            sb.append(endViewIndex).append("] ");
            if (paragraphView != null && startViewIndex != endViewIndex) {
                if (startViewIndex > endViewIndex) {
                    sb.append("ERROR!!! startViewIndex=").append(startViewIndex);
                    sb.append(" > endViewIndex=").append(endViewIndex);
                } else {
                    int childCount = paragraphView.getViewCount();
                    if (startViewIndex == childCount) {
                        sb.append("<").append(paragraphView.getEndOffset()).append(">");
                    } else {
                        EditorView startChild = paragraphView.getEditorView(startViewIndex);
                        EditorView lastChild = paragraphView.getEditorView(endViewIndex - 1);
                        sb.append("<").append(startChild.getStartOffset());
                        sb.append(",").append(lastChild.getEndOffset()).append("> ");
                    }
                }
            }
            sb.append("EV:");
            EditorView endViewPart = wrapLine.endViewPart;
            if (endViewPart != null) {
                sb.append("<").append(endViewPart.getStartOffset()).append(",");
                sb.append(endViewPart.getEndOffset()).append(">");
            } else {
                sb.append("NULL");
            }
        }
        return sb.toString();
    }

    public String toString(ParagraphView paragraphView) {
        return appendInfo(new StringBuilder(200), paragraphView, 0);
    }

    @Override
    public String toString() {
        return toString(null);
    }

}
