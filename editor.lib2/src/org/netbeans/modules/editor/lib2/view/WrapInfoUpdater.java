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

import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.View;


/**
 * Information about line wrapping that may be attached to {@link ParagraphViewChildren}.
 * 
 * @author Miloslav Metelka
 */

final class WrapInfoUpdater {

    // -J-Dorg.netbeans.modules.editor.lib2.view.WrapInfoUpdater.level=FINER
    private static final Logger LOG = Logger.getLogger(WrapInfoUpdater.class.getName());

    private static final long serialVersionUID  = 0L;

    private final WrapInfo wrapInfo;

    private final ParagraphViewChildren children;

    private final ParagraphView paragraphView;

    private final DocumentView documentView;

    private List<WrapLine> wrapLines;

    /** Wrap line being currently built. */
    private WrapLine wrapLine;

    /** Index of child view being currently processed. */
    private int childIndex;

    private float x;

    private EditorView breakView;

    /** Current x coordinate on the wrap line. */
    private float breakVisualOffset;

    private int breakOffset;

    private float availableWidth;

    private float maxLineWidth;

    private boolean wrapLineNonEmpty;

    private StringBuilder logMsgBuilder;

    WrapInfoUpdater(WrapInfo wrapInfo, ParagraphViewChildren children, ParagraphView paragraphView) {
        this.wrapInfo = wrapInfo;
        this.children = children;
        this.paragraphView = paragraphView;
        this.documentView = paragraphView.getDocumentView();
        assert (this.documentView != null) : "Null documentView"; // NOI18N
    }

    float initWrapInfo() {
        this.wrapLines = new ArrayList<WrapLine>(2);
        int childCount = children.size();
        if (childCount > 0) {
            float visibleWidth = documentView.getVisibleWidth();
            TextLayout lineContinuationTextLayout = documentView.getLineContinuationCharTextLayout();
            // Make reasonable minimum width so that the number of visual lines does not double suddenly
            // when user would minimize the width too much. Also have enough space for line continuation mark
            availableWidth = Math.max(visibleWidth - lineContinuationTextLayout.getAdvance(),
                    documentView.getDefaultCharWidth() * 4);
            double nextVisualOffset = 0d;
            logMsgBuilder = LOG.isLoggable(Level.FINE) ? new StringBuilder(100) : null;
            if (logMsgBuilder != null) {
                logMsgBuilder.append("lineContCharW:").append(lineContinuationTextLayout.getAdvance());
                logMsgBuilder.append(", availW:").append(availableWidth);
                logMsgBuilder.append("\n");
            }

            try {
                for (; childIndex < childCount; childIndex++) {
                    EditorView childView = children.get(childIndex);
                    double visualOffset = nextVisualOffset;
                    nextVisualOffset = paragraphView.getViewVisualOffset(childIndex + 1);
                    double childWidth = (nextVisualOffset - visualOffset);
                    if (logMsgBuilder != null) {
                        logMsgBuilder.append("child[").append(childIndex).append("]:").append(childView);
                        logMsgBuilder.append(",W=").append(childWidth); // NOI18N
                        logMsgBuilder.append(": "); // NOI18N
                    }

                    if (x + childWidth <= availableWidth) { // Within available width
                        addChild(childWidth);
                    } else { // Exceeds available width => must break the child view
                        if (breakViewLineEnd(childView, visualOffset)) { // Successful break
                            while (breakViewNext()) { }
                        } else { // break could not be performed
                            boolean forceAdd = true;
                            if (wrapLineNonEmpty) {
                                finishWrapLine();
                                // Retry On Empty Line -> retry adding
                                if (logMsgBuilder != null) {
                                    logMsgBuilder.append("ROEL;");
                                }
                                if (x + childWidth <= availableWidth) {
                                    addChild(childWidth);
                                    forceAdd = false;
                                } else { // did not fit on empty line => attempt break
                                    if (breakViewLineEnd(childView, visualOffset)) { // Successful break
                                        forceAdd = false;
                                        while (breakViewNext()) { }
                                    }
                                }
                            }
                            if (forceAdd) { // Force adding on an empty line
                                if (logMsgBuilder != null) {
                                    logMsgBuilder.append("addFORCED;"); // NOI18N
                                }
                                addChild(childWidth);
                            }
                        }
                    }
                    if (logMsgBuilder != null) {
                        logMsgBuilder.append('\n');
                    }
                }
                finishWrapLine();
            } finally {
                if (logMsgBuilder != null) {
                    logMsgBuilder.append('\n');
                    LOG.fine(logMsgBuilder.toString());
                }
            }
        } // no views

        wrapInfo.addAll(wrapLines);
        wrapInfo.checkIntegrity(paragraphView);
        if (logMsgBuilder != null) {
            LOG.fine("Inited wrapInfo:\n" + wrapInfo.toString(paragraphView) + "\n");
        }
        return maxLineWidth;
    }
    
    float preferredWidth() {
        return maxLineWidth;
    }

    private WrapLine wrapLine() {
        if (wrapLine == null) {
            // If a view is being currently broken then it should not be included in the wrapLine
            wrapLine = new WrapLine();
            if (logMsgBuilder != null) {
                logMsgBuilder.append("WL[").append(wrapLines.size());
                logMsgBuilder.append("]{");
            }
        }
        return wrapLine;
    }

    private void finishWrapLine() {
        if (wrapLine != null) {
            if (wrapLineNonEmpty) {
                if (x > maxLineWidth) {
                    maxLineWidth = x;
                }
                if (logMsgBuilder != null) {
                    logMsgBuilder.append("};");
                }
                wrapLines.add(wrapLine);
            }
            wrapLine = null;
            wrapLineNonEmpty = false;
            x = 0f;
        }
    }

    private void addChild(double childWidth) {
        WrapLine wl = wrapLine();
        if (!wl.isInited()) {
            wl.startViewIndex = childIndex;
        }
        wl.endViewIndex = childIndex + 1;
        wrapLineNonEmpty = true;
        x += childWidth;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("added,x=").append(x).append(';'); // NOI18N
        }
    }

    /**
     * Start breaking the child view at the wrap line's end.
     * @param childView
     * @param childVisualOffset
     * @return true for successful break.
     */
    private boolean breakViewLineEnd(EditorView childView, double childVisualOffset) {
        assert (breakView == null);
        breakView = childView;
        breakOffset = childView.getStartOffset();
        // Use visual offset as "x" for breaking so that '\t' behavior is not affected by wrapping
        breakVisualOffset = (float) childVisualOffset;
        if ((wrapLine().endViewPart = breakViewImpl()) != null) { // break succeeded
            if (logMsgBuilder != null) {
                logMsgBuilder.append("brEND,x=").append(x).append(';'); // NOI18N
            }
            finishWrapLine();
            return true;
        } else { // break failed
            if (logMsgBuilder != null) {
                logMsgBuilder.append("brEND-FAIL;"); // NOI18N
            }
            breakView = null;
            return false;
        }
    }

    /**
     * Continue breaking at line's begining.
     * @return true if the remaining child view part did not fit on the line
     *  and further breaking needs to be done.
     */
    private boolean breakViewNext() {
        float width = breakView.getPreferredSpan(View.X_AXIS);
        boolean fits = (width <= availableWidth - x);
        if (!fits && (wrapLine().startViewPart = breakViewImpl()) != null) { // break succeeded
            if (logMsgBuilder != null) {
                logMsgBuilder.append("brSTART,x=").append(x).append(';'); // NOI18N
            }
            wrapLine.startViewX = x;
            wrapLineNonEmpty = true;
            if (wrapLine.startViewPart.getEndOffset() == breakView.getEndOffset()) {
                breakView = null; // Fully broken
                return false;
            } else { // The next part was created but did not fit to line => finish this line and break further
                finishWrapLine();
                return true;
            }
        } else { // breakView width fits or break failed
            wrapLine().startViewPart = breakView;
            breakView = null;
            wrapLineNonEmpty = true;
            x += width;
            wrapLine.startViewX = x;
            if (logMsgBuilder != null) {
                logMsgBuilder.append("brSTART-"); // NOI18N
                logMsgBuilder.append(fits ? "FITS" : "FAIL"); // NOI18N
                logMsgBuilder.append(",x=").append(x).append(';'); // NOI18N
            }
            if (x >= availableWidth) {
                finishWrapLine();
            }
            return false;
        }
    }

    private EditorView breakViewImpl() {
        // Do breaking by first having a fragment starting at end offset of the previous broken part.
        // This is compatible with the FlowView way of views breaking
        if (logMsgBuilder != null) {
            int breakViewStartOffset = breakView.getStartOffset();
            if (breakViewStartOffset != breakOffset) {
                logMsgBuilder.append("\nERROR!: breakViewStartOffset=").append(breakViewStartOffset).
                        append(" != breakOffset=").append(breakOffset); // NOI18N
            }
        }
        EditorView part = (EditorView) breakView.breakView(
                View.X_AXIS, breakOffset, breakVisualOffset, availableWidth - x);
        EditorView fragment;
        int partEndOffset;
        if (part != null && part != breakView && ((fragment = (EditorView) breakView.createFragment(
                (partEndOffset = part.getEndOffset()), breakView.getEndOffset())) != null) && fragment != breakView)
        { // Successful breaking
            if (logMsgBuilder != null) {
                int breakViewStartOffset = breakView.getStartOffset();
                if (part.getStartOffset() != breakViewStartOffset) {
                    logMsgBuilder.append("\nbreakView() ERROR!: partStartOffset=").append(part.getStartOffset()).
                            append(" != breakViewStartOffset=").append(breakViewStartOffset); // NOI18N
                }
                if (partEndOffset != fragment.getStartOffset()) {
                    logMsgBuilder.append("\nERROR!: partEndOffset=").append(partEndOffset).
                            append(" !=  fragmentStartOffset=").append(fragment.getStartOffset()); // NOI18N
                }
                if (fragment.getEndOffset() != breakView.getEndOffset()) {
                    logMsgBuilder.append("\ncreateFragment() ERROR!: fragmentEndOffset=").append(fragment.getEndOffset()).
                            append(" != breakViewEndOffset=").append(breakView.getEndOffset());
                }
            }
            wrapLineNonEmpty = true;
            float width = part.getPreferredSpan(View.X_AXIS);
            breakView = fragment;
            breakVisualOffset += width;
            breakOffset = partEndOffset;
            x += width;
        } else { // Can't break
            part = null;
        }
        return part;
    }

}
