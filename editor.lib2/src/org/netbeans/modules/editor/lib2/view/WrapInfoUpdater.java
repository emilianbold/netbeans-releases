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

import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.View;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;


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

    private final ParagraphView pView;

    private final DocumentView docView;

    private List<WrapLine> wrapLines;

    /** Wrap line being currently built. */
    private WrapLine wrapLine;

    /** Index of child view being currently processed. */
    private int childIndex;
    
    /** Current X on a wrap-line being just built. */
    private float x;

    /** Child view being currently processed or its part if it was fragmented. */
    private EditorView childViewOrPart;

    /**
     * In case childView was fragmented by breakView() and createFragment()
     * this is the remaining part that is currently being processed.
     */
    private boolean childViewFragmented;
    
    /**
     * Equivalent of childViewPart.getPreferredSpan(X_AXIS).
     */
    private float childViewOrPartWidth;

    /**
     * Offset on x-coordinate of childView measured from the first child in ParagraphViewChildren (no wrapping involved).
     * This is useful to quickly get (non-wrapped) child views' width without calling child.getPreferredSpan(X_AXIS).
     */
    private double childX;
    
    /**
     * Offset on x-coordinate of a next childView measured from the first child (without wrapping).
     * (nextChildX - childX) is childWidth which should be equivalent to child.getPreferredSpan(X_AXIS).
     */
    private double nextChildX;

    /** Relative X of the current childViewPart (if any) against childX.
     * <br>
     * When creating a second and following parts of a child view then this is a value 'x' parameter to breakView().
     */
    private float childViewPartRelX;
    
    /** breakView() and createFragment() set the width of the start part of fragmenting. */
    private float startPartWidth;
    
    /** End part of the fragmenting set by breakView() and createFragment() methods (they return start part
     * and end part is set here).
     */
    private EditorView endPart;
    
    /**
     * Total width that may be occupied by wrap line's content.
     */
    private float availableWidth;
    
    /**
     * Maximum width from all created wrap lines.
     */
    private float maxWrapLineWidth;

    private boolean wrapLineNonEmpty;

    private boolean wrapTypeWords;

    private StringBuilder logMsgBuilder;
    
    
    WrapInfoUpdater(WrapInfo wrapInfo, ParagraphView paragraphView) {
        this.wrapInfo = wrapInfo;
        this.pView = paragraphView;
        this.docView = paragraphView.getDocumentView();
        assert (this.docView != null) : "Null documentView"; // NOI18N
    }

    void initWrapInfo() {
        this.wrapLines = new ArrayList<WrapLine>(2);
        wrapTypeWords = (docView.op.getLineWrapType() == LineWrapType.WORD_BOUND);
        float visibleWidth = docView.op.getVisibleRect().width;
        TextLayout lineContinuationTextLayout = docView.op.getLineContinuationCharTextLayout();
        // Make reasonable minimum width so that the number of visual lines does not double suddenly
        // when user would minimize the width too much. Also have enough space for line continuation mark
        availableWidth = Math.max(visibleWidth - lineContinuationTextLayout.getAdvance(),
                docView.op.getDefaultCharWidth() * 4);
        logMsgBuilder = LOG.isLoggable(Level.FINE) ? new StringBuilder(100) : null;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("Building wrapLines: availWidth=").append(availableWidth); // NOI18N
            logMsgBuilder.append(", lineContCharWidth=").append(lineContinuationTextLayout.getAdvance()); // NOI18N
            logMsgBuilder.append("\n"); // NOI18N
        }
        try {
            initChildVars(0, 0d); // At least one child should exist 
            do {
                if (x + childViewOrPartWidth <= availableWidth) { // Within available width
                    addCurrentAndFetchNextView();
                } else { // Exceeds available width => must break the child view
                    boolean regularBreak = false;
                    if (wrapTypeWords) {
                        int currentStartOffset = childViewOrPart.getStartOffset();
                        int currentEndOffset = childViewOrPart.getEndOffset();
                        int startOffset;
                        if (wrapLine != null) {
                            EditorView wrapLineStartView = (wrapLine.startPart != null)
                                    ? wrapLine.startPart
                                    : pView.getEditorView(wrapLine.firstViewIndex);
                            startOffset = wrapLineStartView.getStartOffset();
                        } else {
                            startOffset = currentStartOffset;
                        }
                        WordInfo wordInfo = getWordInfo(startOffset, currentStartOffset);
                        if (wordInfo != null) {
                            // Attempt to break the view (at word boundary) so that it fits.
                            EditorView startPart = breakView(false);
                            if (startPart != null) {
                                childViewFragmented = true;
                                childViewOrPart = startPart;
                                childViewOrPartWidth = startPartWidth;
                                addCurrentAndFetchNextView();
                                finishWrapLine();
                            } else { // Does not fit or cannot break
                                int wordEndOffset = wordInfo.wordEndOffset();
                                if (startOffset == wordInfo.wordStartOffset()) {
                                    // Do not attempt fragmenting since breakView() did not succeed
                                    addCurrentAndFetchNextView();
                                    while (childViewOrPart != null && wordEndOffset > currentEndOffset) {
                                        currentEndOffset = childViewOrPart.getEndOffset();
                                        addCurrentAndFetchNextView();
                                    } // Continue with next child
                                } else {
                                    removeViewsToWordStart(wordInfo.wordStartOffset());
                                }
                                finishWrapLine();
                            }
                        } else { // WordInfo == null
                            regularBreak = true;
                        }
                    } else { // Not wrapping at words boundary
                        regularBreak = true;
                    }
                    
                    if (regularBreak) {
                        EditorView startPart = breakView(false);
                        if (startPart != null) {
                            childViewFragmented = true;
                            childViewOrPart = startPart;
                            childViewOrPartWidth = startPartWidth;
                            addCurrentAndFetchNextView();
                            finishWrapLine();
                        } else { // break failed
                            if (!wrapLineNonEmpty) {
                                addCurrentAndFetchNextView();
                            }
                            finishWrapLine();
                        }
                    }
                }
            } while (childIndex < pView.getViewCount());
            finishWrapLine();
        } finally {
            if (logMsgBuilder != null) {
                logMsgBuilder.append('\n');
                LOG.fine(logMsgBuilder.toString());
            }
        }

        wrapInfo.addAll(wrapLines);
        wrapInfo.checkIntegrity(pView);
        if (logMsgBuilder != null) {
            LOG.fine("Resulting wrapInfo:" + wrapInfo.toString(pView) + "\n");
        }
        wrapInfo.setWidth(maxWrapLineWidth);
    }
    
    private WrapLine wrapLine() {
        if (wrapLine == null) {
            // If a view is being currently broken then it should not be included into the wrapLine
            wrapLine = new WrapLine();
        }
        return wrapLine;
    }

    private void finishWrapLine() {
        if (wrapLine != null) {
            if (wrapLineNonEmpty) {
                if (x > maxWrapLineWidth) {
                    maxWrapLineWidth = x;
                }
                wrapLines.add(wrapLine);
            }
            wrapLine = null;
            wrapLineNonEmpty = false;
            x = 0f;
        }
    }
    
    private void initChildVars(int childIndex, double childX) {
        this.childIndex = childIndex;
        this.childX = childX;
        assignChild();
    }
    
    /**
     * Move next child view into childViewOrPart variable (or set it to null if there's no more children).
     */
    private void fetchNextChild() {
        childIndex++; // Possibly get >view-count for multiple calls but does not matter
        if (childIndex < pView.getViewCount()) {
            childX = nextChildX;
            assignChild();
        } else {
            //not-necessary childViewFragmented = false;
            childViewOrPart = null;
        }
    }
    
    private void assignChild() {
        nextChildX = pView.children.startVisualOffset(childIndex + 1);
        childViewFragmented = false;
        childViewOrPart = pView.getEditorView(childIndex);
        childViewOrPartWidth = (float) (nextChildX - childX);
        checkLogChild();
    }
    
    private void checkLogChild() {
        if (logMsgBuilder != null) {
            logMsgBuilder.append("child[").append(childIndex).append("]:").append(childViewOrPart.getDumpId()); // NOI18N
            int startOffset = childViewOrPart.getStartOffset();
            logMsgBuilder.append(" <").append(startOffset).append(",").append(startOffset + childViewOrPart.getLength()); // NOI18N
            logMsgBuilder.append("> W=").append(childViewOrPartWidth); // NOI18N
            logMsgBuilder.append(":\n"); // NOI18N
        }
    }

    /**
     * Add current child view or its part to current wrap line and fetch next view.
     * @return 
     */
    private void addCurrentAndFetchNextView() {
        if (!childViewFragmented) {
            WrapLine wl = wrapLine();
            if (!wl.hasFullViews()) {
                wl.firstViewIndex = childIndex;
            }
            wl.endViewIndex = childIndex + 1;
            wrapLineNonEmpty = true;
            if (logMsgBuilder != null) {
                logMsgBuilder.append("  added"); // NOI18N
                logWrapLineAndX(x, x + childViewOrPartWidth);
            }
            x += childViewOrPartWidth;
            fetchNextChild();

        } else { // Fragmented
            if (wrapLineNonEmpty) {
                addCurrentAsEndPart();
            } else {
                addCurrentAsStartPart();
            }
            if (endPart != null) {
                childViewOrPart = endPart; // (Already fragmented childViewFragmented == true)
                childViewOrPartWidth = endPart.getPreferredSpan(View.X_AXIS);
                endPart = null;
            } else {
                fetchNextChild();
            }
        }
    }
    
    private void removeChildren(int startIndex) {
        assert (wrapLine.hasFullViews()) : "No full views"; // NOI18N
        assert (wrapLine.endPart == null);
        assert (wrapLine.firstViewIndex <= startIndex && startIndex < wrapLine.endViewIndex)
                : "startIndex=" + startIndex + " not in WL " + wrapLine;
        double startChildX = pView.children.startVisualOffset(startIndex);
        x -= (childX - startChildX);
        wrapLine.endViewIndex = startIndex;
        if (!wrapLine.hasFullViews()) {
            if (wrapLine.startPart == null) {
                wrapLineNonEmpty = false;
            }
        }
        initChildVars(startIndex, startChildX);
    }
    
    private void addCurrentAsStartPart() {
        assert (!wrapLineNonEmpty);
        assert (wrapLine().startPart == null);
        assert !wrapLine().hasFullViews();
        assert (x == 0f);
        wrapLine().startPart = childViewOrPart;
        wrapLine().firstViewX = childViewOrPartWidth;
        x += childViewOrPartWidth;
        childViewPartRelX += childViewOrPartWidth;
        wrapLineNonEmpty = true;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  WrapLine's startViewPart "); // NOI18N
            logWrapLineAndX(0f, x);
        }
    }
    
    /**
     * Clear wrapLine.startPart and return it into childViewOrPart.
     */
    private void undoStartPart() {
        assert (wrapLine.startPart != null);
        assert (!wrapLine.hasFullViews());
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  Removed startViewPart x=" + x + " => 0."); // NOI18N
            logWrapLineAndX(0f, x);
        }
        childViewFragmented = true;
        childViewOrPart = wrapLine.startPart;
        childViewOrPartWidth = wrapLine.firstViewX;
        childViewPartRelX -= wrapLine.firstViewX;
        wrapLine.startPart = null;
        wrapLine.firstViewX = 0f;
        x = 0f;
        wrapLineNonEmpty = false;
    }
    
    private void addCurrentAsEndPart() {
        assert (wrapLine().endPart == null);
        wrapLine().endPart = childViewOrPart;
        float oldX = x;
        x += childViewOrPartWidth;
        childViewPartRelX += childViewOrPartWidth;
        wrapLineNonEmpty = true;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  WrapLine's endViewPart "); // NOI18N
            logWrapLineAndX(oldX, x);
        }
    }
    
    /**
     * Remove existing views in wrapLine so that the views may be split at the word start
     * @param wordStartOffset
     */
    private void removeViewsToWordStart(int wordStartOffset) {
        assert (wrapLineNonEmpty) : "Empty wrap line"; // NOI18N
        assert (wrapLine.endPart == null);
        boolean removeInStartPart = false;
        if (wrapLine.hasFullViews()) {
            for (int i = wrapLine.endViewIndex - 1; i >= wrapLine.firstViewIndex; i--) {
                // Reuse the removeInStartPart flag
                int viewStartOffset = pView.getEditorView(i).getStartOffset();
                removeInStartPart = (wordStartOffset < viewStartOffset);
                if (!removeInStartPart) {
                    removeChildren(i);
                    if (wordStartOffset > viewStartOffset) { // Break inside child view
                        EditorView startPart = createFragment(wordStartOffset, true);
                        if (startPart != null) {
                            childViewFragmented = true;
                            childViewOrPart = startPart;
                            childViewOrPartWidth = startPartWidth;
                            addCurrentAndFetchNextView();
                        } else { // Fragmentation failed
                            // In order to avoid infinite loop add the complete child
                            addCurrentAndFetchNextView();
                        }
                    }
                    break;
                }
            }
        } else {
            removeInStartPart = true;
        }
        if (removeInStartPart) {
            undoStartPart();
            EditorView startPart = createFragment(wordStartOffset, true);
            if (startPart != null) {
                childViewFragmented = true;
                childViewOrPart = startPart;
                childViewOrPartWidth = startPartWidth;
                addCurrentAndFetchNextView();
            } else { // Fragmentation failed
                // In order to avoid infinite loop add the complete child
                addCurrentAndFetchNextView();
            }
        }
    }
    
    private EditorView breakView(boolean allowWider) {
        // Do breaking by first having a fragment starting at end offset of the previous broken part.
        // This is compatible with the FlowView way of views breaking
        assert (endPart == null) : "Non-null endPart";
        EditorView view = childViewOrPart;
        int viewStartOffset = view.getStartOffset();
        float breakViewX = (float) (childX + childViewPartRelX);
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  breakView<").append(viewStartOffset). // NOI18N
                    append(",").append(viewStartOffset + view.getLength()).append("> x="). // NOI18N
                    append(breakViewX).append(" W=").append(availableWidth - x).append(" => "); // NOI18N
        }
        EditorView startPart = (EditorView) view.breakView(View.X_AXIS, viewStartOffset,
                breakViewX,
                availableWidth - x);
        if (startPart != null && startPart != view) {
            assert (startPart.getStartOffset() == viewStartOffset) : "startPart.getStartOffset()=" + // NOI18N
                    startPart.getStartOffset() + " != viewStartOffset=" + viewStartOffset; // NOI18N
            int startPartLength = startPart.getLength();
            int viewLength = view.getLength();
            if (startPartLength != viewLength) { // Otherwise it was not a real break
                if (logMsgBuilder != null) {
                    logMsgBuilder.append("startPart<").append(startPart.getStartOffset()). // NOI18N
                            append(",").append(startPart.getEndOffset()).append(">"); // NOI18N
                }
                startPartWidth = startPart.getPreferredSpan(View.X_AXIS);
                if (allowWider || startPartWidth <= availableWidth - x) {
                    endPart = (EditorView) view.createFragment(viewStartOffset + startPartLength,
                            viewStartOffset + viewLength);
                    if (endPart != null && endPart != view && endPart.getLength() == viewLength - startPartLength) {
                        if (logMsgBuilder != null) {
                            logMsgBuilder.append("\n");
                        }
                    } else { // createFragment() failed
                        if (logMsgBuilder != null) {
                            logMsgBuilder.append("createFragment <" + (viewStartOffset+startPartLength) + // NOI18N
                                    "," + (viewStartOffset+viewLength) + "> not allowed by view\n"); // NOI18N
                                    
                        }
                        startPart = null;
                        endPart = null;
                    }
                } else {
                    if (logMsgBuilder != null) {
                        logMsgBuilder.append("Fragment too wide(pW=" + startPartWidth + // NOI18N
                                ">aW=" + availableWidth + "-x=" + x + ")\n"); // NOI18N
                    }
                    startPart = null;
                }
            } else {
                if (logMsgBuilder != null) {
                    logMsgBuilder.append("startPart same length as view\n"); // NOI18N
                }
                startPart = null;
            }
        } else {
            if (logMsgBuilder != null) {
                logMsgBuilder.append("Break not allowed by view\n"); // NOI18N
            }
            startPart = null;
        }
        return startPart;
    }
    
    /**
     * Attempt to fragment view.
     * @param breakOffset offset where the start fragment will end and end part fragment will start.
     * @return start fragment (end fragment will be in "endPart") or null.
     */
    private EditorView createFragment(int breakOffset, boolean allowWider) {
        EditorView view = childViewOrPart;
        int viewStartOffset = view.getStartOffset();
        int viewEndOffset = viewStartOffset + view.getLength();
        assert (viewStartOffset < breakOffset) : "viewStartOffset=" + viewStartOffset + // NOI18N
                " >= breakOffset" + breakOffset; // NOI18N
        assert (breakOffset < viewEndOffset) : "breakOffset=" + breakOffset + // NOI18N
                " >= viewEndOffset" + viewEndOffset; // NOI18N
        assert (endPart == null) : "Non-null endPart";
        EditorView startPart = (EditorView) view.createFragment(viewStartOffset, breakOffset);
        assert (startPart != null);
        if (startPart != view) {
            if (logMsgBuilder != null) {
                logMsgBuilder.append(" breakView<").append(startPart.getStartOffset()). // NOI18N
                        append(",").append(startPart.getEndOffset()).append(">"); // NOI18N
            }
            startPartWidth = startPart.getPreferredSpan(View.X_AXIS);
            if (allowWider || startPartWidth <= availableWidth - x) {
                endPart = (EditorView) view.createFragment(breakOffset, viewEndOffset);
                assert (endPart != null) : "EndPart == null"; // NOI18N
                if (endPart == view) {
                    startPart = null;
                    endPart = null;
                }
            } else {
                startPart = null;
            }
        } else {
            startPart = null;
        }
        return startPart;
    }
    
    /**
     * Get word info in case there's a word around boundaryOffset.
     * @param boundaryOffset there must be word's char before and after this offset
     *  to return non-null result.
     * @param startOffset start offset of inspected area.
     * @return word info or null.
     */
    private WordInfo getWordInfo(int boundaryOffset, int startOffset) {
        CharSequence docText = DocumentUtilities.getText(docView.getDocument());
        boolean prevCharIsWordPart = (boundaryOffset > startOffset)
                && Character.isLetterOrDigit(docText.charAt(boundaryOffset - 1));
        if (prevCharIsWordPart) {
            // Check if next char is word part as well
            // [TODO] Check surrogates
            boolean nextCharIsWordPart = Character.isLetterOrDigit(docText.charAt(boundaryOffset));
            if (nextCharIsWordPart) {
                int wordEndOffset;
                int docTextLength = docText.length();
                for (wordEndOffset = boundaryOffset + 1;
                        wordEndOffset < docTextLength; wordEndOffset++)
                {
                    // [TODO] Check surrogates
                    if (!Character.isLetterOrDigit(docText.charAt(wordEndOffset))) {
                        break;
                    }
                }
                return new WordInfo(docText, boundaryOffset, startOffset, wordEndOffset);
            }
        }
        return null;
    }

    private void logWrapLineAndX(double oldX, double newX) {
        logMsgBuilder.append(" to WL[").append(wrapLines.size()). // NOI18N
                append("] at x=").append(oldX).append(";newX=").append(newX).append('\n'); // NOI18N
    }

    private static final class WordInfo {
        
        private CharSequence docText;

        private int boundaryOffset;

        private int startOffset;

        private int wordEndOffset;

        private int wordStartOffset = -1;
        
        WordInfo(CharSequence docText, int boundaryOffset, int startOffset, int wordEndOffset) {
            this.docText = docText;
            this.boundaryOffset = boundaryOffset;
            this.startOffset = startOffset;
            this.wordEndOffset = wordEndOffset;
        }
        
        int wordEndOffset() {
            return wordEndOffset;
        }
        
        int wordStartOffset() {
            if (wordStartOffset == -1) {
                for (wordStartOffset = boundaryOffset - 2; // boundaryOffset-1 already checked for word-char
                        wordStartOffset >= startOffset; wordStartOffset--) {
                    // [TODO] Check surrogates
                    if (!Character.isLetterOrDigit(docText.charAt(wordStartOffset))) {
                        break;
                    }
                }
                wordStartOffset++;
            }
            return wordStartOffset;
        }

    }

}
