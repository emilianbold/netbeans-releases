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
    
    /** X on a current wrap-line */
    private float x;

    private EditorView childView;
    
    /** Visual offset of the childView. */
    private double visualOffset;
    
    /** Visual offset of next childView that follows childView. */
    private double nextVisualOffset;

    private EditorView childViewPart;
    
    private float childViewPartWidth;

    /** Relative shift of the current childViewPart against visualOffset of childView. */
    private float visualOffsetPartShift;
    
    /** Assigned by breakView() and createFragment(). */
    private float startPartWidth;
    
    /** End part of the fragmenting returned from certain methods. */
    private EditorView endPart;
    
    private float availableWidth;
    
    private float maxLineWidth;

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
        availableWidth = Math.max(visibleWidth - TextLayoutUtils.getWidth(lineContinuationTextLayout),
                docView.op.getDefaultCharWidth() * 4);
        logMsgBuilder = LOG.isLoggable(Level.FINE) ? new StringBuilder(100) : null;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("availableWidth:").append(availableWidth); // NOI18N
            logMsgBuilder.append(", lineContCharWidth:").append(lineContinuationTextLayout.getAdvance()); // NOI18N
            logMsgBuilder.append("\n"); // NOI18N
        }
        try {
            initChildVars(0, 0d); // At least one child should exist 
            do {
                if (widthFits()) { // Within available width
                    addAndFetchNext();
                } else { // Exceeds available width => must break the child view
                    boolean regularBreak = false;
                    if (wrapTypeWords) {
                        EditorView wrapLineStartView = wrapLineStartView();
                        int wrapLineStartOffset = wrapLineStartView.getStartOffset();
                        int childStartOffset = childView.getStartOffset();
                        int childEndOffset = childView.getEndOffset();
                        WordInfo wordInfo = getWordInfo(childStartOffset, wrapLineStartOffset);
                        if (wordInfo != null) {
                            // Attempt to break the view (at word boundary) so that it fits.
                            EditorView startPart = breakView(false);
                            if (startPart != null) {
                                useStartPart(startPart);
                                addAndFetchNext();
                                finishWrapLine();
                            } else { // Does not fit or cannot break
                                int wordEndOffset = wordInfo.wordEndOffset();
                                if (wrapLineStartOffset == wordInfo.wordStartOffset()) {
                                    // Do not attempt fragmenting since breakView() did not succeed
                                    while (addAndFetchNext() && wordEndOffset > childEndOffset) {
                                        childEndOffset = childView.getEndOffset();
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
                            useStartPart(startPart);
                            addAndFetchNext();
                            finishWrapLine();
                        } else { // break failed
                            if (!wrapLineNonEmpty) {
                                addAndFetchNext();
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
        wrapInfo.setWidth(maxLineWidth);
    }
    
    private void initChildVars(int childIndex, double visualOffset) {
        this.childIndex = childIndex;
        this.visualOffset = visualOffset;
        nextVisualOffset = pView.children.startVisualOffset(childIndex + 1);
        childView = pView.getEditorView(childIndex);
        childViewPart = null;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("child[").append(childIndex).append("]:").append(childView);
            logMsgBuilder.append(",W=").append(width()); // NOI18N
            logMsgBuilder.append(": "); // NOI18N
        }
    }
    
    private boolean fetchNextChild() {
        childViewPart = null;
        childIndex++; // Possibly get >view-count for multiple calls but does not matter
        if (childIndex < pView.getViewCount()) {
            visualOffset = nextVisualOffset;
            nextVisualOffset = pView.children.startVisualOffset(childIndex + 1);
            childView = pView.getEditorView(childIndex);
            if (logMsgBuilder != null) {
                logMsgBuilder.append("child[").append(childIndex).append("]:").append(childView);
                logMsgBuilder.append(",W=").append(width()); // NOI18N
                logMsgBuilder.append(": "); // NOI18N
            }
            return false;
        } else {
            return true; // Finished
        }
    }

    private WrapLine wrapLine() {
        if (wrapLine == null) {
            // If a view is being currently broken then it should not be included in the wrapLine
            wrapLine = new WrapLine();
//            if (logMsgBuilder != null) {
//                logMsgBuilder.append("WL[").append(wrapLines.size());
//                logMsgBuilder.append("]{");
//            }
        }
        return wrapLine;
    }

    private void finishWrapLine() {
        if (wrapLine != null) {
            if (wrapLineNonEmpty) {
                if (x > maxLineWidth) {
                    maxLineWidth = x;
                }
//                if (logMsgBuilder != null) {
//                    logMsgBuilder.append("};");
//                }
                wrapLines.add(wrapLine);
            }
            wrapLine = null;
            wrapLineNonEmpty = false;
            x = 0f;
        }
    }
    
    private boolean addAndFetchNext() {
        if (childViewPart == null) {
            double childWidth = nextVisualOffset - visualOffset;
            WrapLine wl = wrapLine();
            if (!wl.hasFullViews()) {
                wl.firstViewIndex = childIndex;
            }
            wl.endViewIndex = childIndex + 1;
            wrapLineNonEmpty = true;
            if (logMsgBuilder != null) {
                logMsgBuilder.append("added"); // NOI18N
                logWrapLineAndX(x, x + childWidth);
            }
            x += childWidth;
            return fetchNextChild();
        } else { // A part is active
            if (wrapLineNonEmpty) {
                addEndPart(childViewPart);
            } else {
                addStartPart(childViewPart);
            }
            if (endPart != null) {
                useEndPart();
                return false;
            } else {
                return fetchNextChild();
            }
        }
    }
    
    private boolean widthFits() {
        return x + width() <= availableWidth;
    }
    
    private double width() {
        double width = (childViewPart != null) ? childViewPartWidth : (nextVisualOffset - visualOffset);
        return width;
    }
    
    private void removeChildren(int startIndex) {
        assert (wrapLine.hasFullViews()) : "No full views"; // NOI18N
        assert (wrapLine.endPart == null);
        assert (wrapLine.firstViewIndex <= startIndex && startIndex < wrapLine.endViewIndex)
                : "startIndex=" + startIndex + " not in WL " + wrapLine;
        double startVisualOffset = pView.children.startVisualOffset(startIndex);
        x -= (visualOffset - startVisualOffset);
        wrapLine.endViewIndex = startIndex;
        if (!wrapLine.hasFullViews()) {
            if (wrapLine.startPart == null) {
                wrapLineNonEmpty = false;
            }
        }
        initChildVars(startIndex, startVisualOffset);
    }
    
    private void addStartPart(EditorView newStartPart) {
        assert (!wrapLineNonEmpty);
        assert (wrapLine().startPart == null);
        assert !wrapLine().hasFullViews();
        assert (x == 0f);
        wrapLine().startPart = newStartPart;
        wrapLine().firstViewX = childViewPartWidth;
        x += childViewPartWidth;
        visualOffsetPartShift += childViewPartWidth;
        wrapLineNonEmpty = true;
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  WrapLine's startViewPart "); // NOI18N
            logWrapLineAndX(0f, x);
        }
    }
    
    private void removeStartPart() {
        assert (wrapLine.startPart != null);
        assert (!wrapLine.hasFullViews());
        if (logMsgBuilder != null) {
            logMsgBuilder.append("  Removed startViewPart x=" + x + " => 0."); // NOI18N
            logWrapLineAndX(0f, x);
        }
        childViewPart = wrapLine.startPart;
        childViewPartWidth = wrapLine.firstViewX;
        visualOffsetPartShift -= wrapLine.firstViewX;
        wrapLine.startPart = null;
        wrapLine.firstViewX = 0f;
        x = 0f;
        wrapLineNonEmpty = false;
    }
    
    private void addEndPart(EditorView endPart) {
        assert (wrapLine().endPart == null);
        wrapLine().endPart = endPart;
        float oldX = x;
        x += childViewPartWidth;
        visualOffsetPartShift += childViewPartWidth;
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
                            useStartPart(startPart);
                            addAndFetchNext();
                        } else { // Fragmentation failed
                            // In order to avoid infinite loop add the complete child
                            addAndFetchNext();
                        }
                    }
                    break;
                }
            }
        } else {
            removeInStartPart = true;
        }
        if (removeInStartPart) {
            removeStartPart();
            EditorView startPart = createFragment(wordStartOffset, true);
            if (startPart != null) {
                useStartPart(startPart);
                addAndFetchNext();
            } else { // Fragmentation failed
                // In order to avoid infinite loop add the complete child
                addAndFetchNext();
            }
        }
    }
    
    private EditorView wrapLineStartView() {
        EditorView startView = (wrapLine != null)
            ? ((wrapLine.startPart != null)
                ? wrapLine.startPart
                : pView.getEditorView(wrapLine.firstViewIndex))
            : currentView();
        return startView;
    }

    private EditorView currentView() {
        EditorView view = (childViewPart != null) ? childViewPart : childView;
        return view;
    }
    
    private void useStartPart(EditorView startPart) {
        childViewPart = startPart;
        childViewPartWidth = startPartWidth;
    }
    
    private void useEndPart() {
        childViewPart = endPart;
        childViewPartWidth = childViewPart.getPreferredSpan(View.X_AXIS);
        endPart = null;
    }
    
    private EditorView breakView(boolean allowWider) {
        // Do breaking by first having a fragment starting at end offset of the previous broken part.
        // This is compatible with the FlowView way of views breaking
        assert (endPart == null) : "Non-null endPart";
        EditorView view = currentView();
        int viewStartOffset = view.getStartOffset();
        if (logMsgBuilder != null) {
        }
        // Use visual offset as "x" for breaking so that '\t' behavior is not affected by wrapping
        EditorView startPart = (EditorView) view.breakView(View.X_AXIS, viewStartOffset,
                (float) (visualOffset + visualOffsetPartShift),
                availableWidth - x);
        assert (startPart != null);
        if (startPart != view) {
            startPartWidth = startPart.getPreferredSpan(View.X_AXIS);
            if (allowWider || startPartWidth <= availableWidth - x) {
                int partEndOffset = startPart.getEndOffset();
                endPart = (EditorView) view.createFragment(partEndOffset, view.getEndOffset());
                assert (endPart != null) : "endPart is null"; // NOI18N
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
     * Attempt to fragment view. Successful 
     * @param breakOffset offset where the start fragment will end and end part fragment will start.
     * @return start fragment (end fragment will be in "endPart") or null.
     */
    private EditorView createFragment(int breakOffset, boolean allowWider) {
        EditorView view = currentView();
        int viewStartOffset = view.getStartOffset();
        int viewEndOffset = view.getEndOffset();
        assert (viewStartOffset < breakOffset) : "viewStartOffset=" + viewStartOffset + // NOI18N
                " >= breakOffset" + breakOffset; // NOI18N
        assert (breakOffset < viewEndOffset) : "breakOffset=" + breakOffset + // NOI18N
                " >= viewEndOffset" + viewEndOffset; // NOI18N
        assert (endPart == null) : "Non-null endPart";
        EditorView startPart = (EditorView) view.createFragment(viewStartOffset, breakOffset);
        assert (startPart != null);
        if (startPart != view) {
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
     * @param endOffset end offset of inspected area.
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
