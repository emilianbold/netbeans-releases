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
        boolean loggable = LOG.isLoggable(Level.FINE);
        if (childCount > 0) {
            float visibleWidth = documentView.getVisibleWidth();
            TextLayout lineContinuationTextLayout = documentView.lineContinuationTextLayout();
            // Make reasonable minimum width so that the number of visual lines does not double suddenly
            // when user would minimize the width too much. Also have enough space for line continuation mark
            availableWidth = Math.max(visibleWidth - lineContinuationTextLayout.getAdvance(),
                    documentView.getDefaultCharWidth() * 4);
            double nextVisualOffset = 0d;
            logMsgBuilder = (loggable ? new StringBuilder(100) : null);

            for (; childIndex < childCount; childIndex++) {
                EditorView childView = children.get(childIndex);
                double visualOffset = nextVisualOffset;
                nextVisualOffset = paragraphView.getViewVisualOffset(childIndex + 1);
                double childWidth = (nextVisualOffset - visualOffset);
                if (loggable) {
                    logMsgBuilder.setLength(0);
                    logMsgBuilder.append("child[").append(childIndex).append("]:").append(childView);
                    logMsgBuilder.append(", W:").append(childWidth); // NOI18N
                    logMsgBuilder.append(": "); // NOI18N
                }

                if (x + childWidth <= availableWidth) { // Within available width
                    addChild(childWidth);
                    if (loggable) {
                        logMsgBuilder.append("added; x=").append(x); // NOI18N
                    }
                } else { // Exceeds available width => must break the child view
                    if (breakViewLineEnd(childView, visualOffset)) { // Successful break
                        if (loggable) {
                            logMsgBuilder.append("break at END; x=").append(x); // NOI18N
                        }
                        while (breakViewNext()) {
                            if (loggable) {
                                logMsgBuilder.append("; break at START; x=").append(x); // NOI18N
                            }
                        } // Once fully split continue to fetching next child
                    } else { // break could not be performed
                        boolean forceAdd = true;
                        if (wrapLineNonEmpty) {
                            if (loggable) {
                                logMsgBuilder.append("break at END FAILED => line finished"); // NOI18N
                            }
                            finishWrapLine();
                            // Retry add on empty line
                            if (x + childWidth <= availableWidth) {
                                addChild(childWidth);
                                if (loggable) {
                                    logMsgBuilder.append("; retry-on-empty added; x=").append(x); // NOI18N
                                }
                                forceAdd = false;
                            } else { // did not fit on empty line => attempt break
                                if (breakViewLineEnd(childView, visualOffset)) { // Successful break
                                    forceAdd = false;
                                    if (loggable) {
                                        logMsgBuilder.append("; retry-on-empty break at END; x=").append(x); // NOI18N
                                    }
                                    while (breakViewNext()) {
                                        if (loggable) {
                                            logMsgBuilder.append("; retry-on-empty break at START; x=").append(x); // NOI18N
                                        }
                                    } // Once fully split continue to fetching next child
                                }
                            }
                        }
                        if (forceAdd) { // Force adding on an empty line
                            addChild(childWidth);
                            if (loggable) {
                                logMsgBuilder.append("add FORCED; x=").append(x); // NOI18N
                            }
                            if (x > availableWidth) {
                                finishWrapLine();
                            }
                        }
                    }
                }


                if (loggable) {
                    logMsgBuilder.append('\n');
                    LOG.fine(logMsgBuilder.toString());
                }
            }

            finishWrapLine();
        } // no views

        wrapInfo.addAll(wrapLines);
        wrapInfo.checkIntegrity(paragraphView);
        if (loggable) {
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
            wrapLine = new WrapLine((breakView != null) ? childIndex + 1 : childIndex);
        }
        return wrapLine;
    }

    private void finishWrapLine() {
        if (wrapLine != null) {
            if (wrapLineNonEmpty) {
                if (x > maxLineWidth) {
                    maxLineWidth = x;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    logMsgBuilder.append(";WL[").append(wrapLines.size());
                    logMsgBuilder.append("] finished(W=").append(x).append(")");
                }
                wrapLines.add(wrapLine);
            }
            wrapLine = null;
            wrapLineNonEmpty = false;
            x = 0f;
        }
    }

    private void addChild(double childWidth) {
        wrapLine().endViewIndex = childIndex + 1;
        wrapLineNonEmpty = true;
        x += childWidth;
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
            finishWrapLine();
            return true;
        } else { // break failed
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
        if (width > availableWidth - x && (wrapLine().startViewPart = breakViewImpl()) != null) { // break succeeded
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
            x += width;
            wrapLine.startViewX = x;
            if (x >= availableWidth) {
                finishWrapLine();
            }
            return false;
        }
    }

    private EditorView breakViewImpl() {
        // Do breaking by first having a fragment starting at end offset of the previous broken part.
        // This is compatible with the FlowView way of views breaking.
        EditorView part = (EditorView) breakView.breakView(
                View.X_AXIS, breakOffset, breakVisualOffset, availableWidth - x);
        EditorView fragment;
        int partEndOffset;
        if (part != null && part != breakView && ((fragment = (EditorView) breakView.createFragment(
                (partEndOffset = part.getEndOffset()), breakView.getEndOffset())) != null) && fragment != breakView)
        { // Successful breaking
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
