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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.lib.editor.util.ArrayUtilities;


/**
 * View building support.
 * <br>
 * When building new views they must have "enough space" so the old view(s) that occupy
 * area of the new view must be removed. When fReplace (first replace) is non-null
 * then the views can be replaced locally in a paragraph view.
 * However if the replace exceeds a local replace then full paragraph views
 * are being removed and recreated. This is because otherwise the remaining
 * local views would have to be re-parented because new paragraph view instances
 * are being created and used.
 * 
 * @author Miloslav Metelka
 */

final class ViewBuilder {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewBuilder.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewBuilder.class.getName());

    private int prevViewEndOffset;

    private final int offsetDelta;

    private final Element lineRoot;

    private int lineIndex;

    private int lineEndOffset;
    
    private int paragraphViewEndOffset = Integer.MIN_VALUE;

    private int matchOffset = Integer.MIN_VALUE;

    private final FactoryState[] factoryStates;

    /**
     * Replace of paragraph views in a document view.
     */
    private final ViewReplace<DocumentView,ParagraphView> dReplace;

    /**
     * First replace or null if none.
     * It's in ParagraphView[dReplace.index-1].
     */
    private ViewReplace<ParagraphView,EditorView> fReplace;

    /**
     * Actual replace inside current paragraph.
     */
    private ViewReplace<ParagraphView,EditorView> pReplace;

    private boolean viewRemovalFinished;

    /**
     * List of all paragraph replaces done so far.
     */
    private List<ViewReplace<ParagraphView,EditorView>> pReplaceList;

    private int docViewEndOffset;

    private boolean createLocalViews; // Whether children of paragraph views are created
    
    private static boolean wrongStartOffsetReported; // TODO remove when ISE gets fixed
    
    /**
     * Construct view builder.
     * @param paragraphView paragraph view in which a first replace will occur.
     *  It may be null if no first replace is done (e.g. during full views rebuild).
     * @param documentView non-null doc view for which view building is performed.
     * @param paragraphViewIndex >=0 index of first paragraph where the rebuilding occurs.
     * @param viewFactories should be sorted with increasing priority.
     * @param startOffset start offset of the rebuild.
     * @param endOffset end offset of the rebuild. The rebuild may actually span further until
     *  the old and newly created views match their end offsets.
     *  If there was a document modification the endOffset is measured in after-mod offsets.
     * @param modOffset it must be endOffset for no-mod; insertOffset+insertLength for inserts
     *  and removeOffset+removeLength for removals.
     * @param offsetDelta it's 0 for no-mod; +insertLength for inserts; -removeLength for removals.
     */
    ViewBuilder(ParagraphView paragraphView, DocumentView documentView, int paragraphViewIndex,
            EditorViewFactory[] viewFactories, int startOffset, int endOffset,
            int modOffset, int offsetDelta, boolean createLocalViews)
    {
        Document doc = documentView.getDocument();
        docViewEndOffset = documentView.getEndOffset();
        assert (startOffset >= 0) : "startOffset=" + startOffset + " < 0"; // NOI18N
        assert (endOffset >= modOffset) : "endOffset=" + endOffset + " < modOffset=" + modOffset; // NOI18N
        this.createLocalViews = createLocalViews;
        // When not creating local views the possible passed paragraphView must be recreated
        if (!createLocalViews && paragraphView != null) {
            int pOffset = paragraphView.getStartOffset();
            assert (pOffset <= startOffset) : "pOffset=" + pOffset + " > startOffset=" + startOffset; // NOI18N
            startOffset = pOffset;
            // [TODO] Consider whether endOffset should be extended to pOffset+pView.getLength() or not
            paragraphView = null;
        }
        assert (paragraphView == null || createLocalViews) : "createLocalViews=" + createLocalViews + // NOI18N
                ", paragraphView=" + paragraphView; // NOI18N
        // Possibly do a first-replace in the given paragraphView
        if (paragraphView != null) {
            fReplace = new ViewReplace<ParagraphView, EditorView>(
                    paragraphView, paragraphView.getViewIndex(startOffset));
            this.pReplace = fReplace;
            paragraphViewIndex++; // dReplace will start from next paragraph view
        }
        // Always do document-replace since the built views can extend beyond fReplace even for very local changes
        dReplace = new ViewReplace<DocumentView, ParagraphView>(documentView, paragraphViewIndex);
        // Search for the views that need to be removed.
        // Must search in original offsets to after-mod offsets by using both childView.getLength()
        // and paragraphView.getLength() which returns textual span of existing views
        // (unaffected by possibly recently performed modification(s)).
        // First project endOffset to original offset space.
        int endAffectedOffset;
        if (offsetDelta >= 0) { // Insert
            endAffectedOffset = Math.max(endOffset, modOffset);
        } else { // Removal
            endAffectedOffset = Math.max(endOffset, modOffset - offsetDelta);
        }
        endAffectedOffset = Math.min(endAffectedOffset, docViewEndOffset - offsetDelta);
        if (fReplace != null) {
            int paragraphViewStartOffset = paragraphView.getStartOffset();
            if ((startOffset < paragraphViewStartOffset) && !wrongStartOffsetReported) {
                wrongStartOffsetReported = true;
                throw new IllegalStateException("startOffset=" + startOffset + // NOI18N
                        " < paragraphViewStartOffset=" + paragraphViewStartOffset + // NOI18N
                        "\ndocViewEndOffset=" + docViewEndOffset + ", paragraph-views-count=" + documentView.getViewCount() + // NOI18N
                        "\n" + documentView.toStringDetail()); // NOI18N
            }                
            EditorView childView = fReplace.childViewAtIndex();
            // Round start offset to child's start offset
            int childStartOffset = childView.getStartOffset();
            // Re-check updated startOffset
            if (childStartOffset > startOffset && !wrongStartOffsetReported) {
                wrongStartOffsetReported = true;
                throw new IllegalStateException("childStartOffset=" + childStartOffset + // NOI18N
                    " > startOffset=" + startOffset + "\ndocumentView:\n" + documentView.toStringDetail());
            }
            startOffset = childStartOffset;
            // Get paragraph end offset in original offset coordinates
            paragraphViewEndOffset = paragraphViewStartOffset + paragraphView.getLength();
            if (endAffectedOffset < paragraphViewEndOffset) {
                // Rebuild located inside fReplace's paragraph view
                matchOffset = startOffset; // childView's start offset
                while (matchOffset < endAffectedOffset) {
                    matchOffset += childView.getLength();
                    fReplace.removeCount++;
                    if (fReplace.removeEndIndex() == paragraphView.getViewCount()) { // When endAffectedOffset inside last child view
                        assert (matchOffset == paragraphViewEndOffset) : "matchOffset=" + // NOI18N
                                matchOffset + " != paragraphViewEndOffset=" + paragraphViewEndOffset; // NOI18N
                        break;
                    }
                    childView = paragraphView.getEditorView(fReplace.removeEndIndex());
                }
                assert (matchOffset >= endAffectedOffset);
            } else {
                fReplace.removeTillEnd(); // Remove all remaining child views
                matchOffset = paragraphViewEndOffset;
            }
        }
        if (matchOffset < endAffectedOffset) {
            int paragraphCount = documentView.getViewCount();
            if (dReplace.index < paragraphCount) { // dReplace.removeEndIndex() == dReplace.index
                EditorView pView = documentView.getEditorView(dReplace.index);
                if (paragraphViewEndOffset == Integer.MIN_VALUE) {
                    // Check for full rebuild
                    if (modOffset == endOffset && offsetDelta == 0 && endOffset == docViewEndOffset && dReplace.index == 0) {
                        assert (paragraphView == null) : "paragraphView=" + paragraphView + " != null"; // NOI18N
                        // Remove all paragraphs (skip individual removal of each paragraph in checkRemoveParagraphs())
                        // Current view hierarchy may be obsolete in full rebuild (e.g. after lengthy atomic operation)
                        dReplace.removeCount = paragraphCount;
                        viewRemovalFinished = true;
                        matchOffset = paragraphViewEndOffset = docViewEndOffset;
                    } else if (modOffset == 0 && offsetDelta > 0) {
                        // docView[0].getStartOffset() == 1 in case of undo()
                        paragraphViewEndOffset = 0;
                    } else {
                        paragraphViewEndOffset = pView.getStartOffset();
                    }
                }
                if (!viewRemovalFinished) {
                    paragraphViewEndOffset += pView.getLength();
                    matchOffset = paragraphViewEndOffset;
                    dReplace.removeCount++;
                    checkRemoveParagraphs(endAffectedOffset, false);
                }
            } else {
                viewRemovalFinished = true;
                matchOffset = paragraphViewEndOffset = docViewEndOffset;
            }
        }
        assert (matchOffset >= 0) : "matchOffset=" + matchOffset; // NOI18N
        assert (paragraphViewEndOffset >= 0) : "paragraphViewEndOffset=" + paragraphViewEndOffset; // NOI18N

        // Apply offsetDelta to operate in actual offset coordinates for removals
        if (!viewRemovalFinished && offsetDelta != 0) {
            matchOffset += offsetDelta;
            paragraphViewEndOffset += offsetDelta;
        }

        assert (matchOffset >= 0) : "matchOffset=" + matchOffset; // NOI18N
        assert (paragraphViewEndOffset >= 0) : "paragraphViewEndOffset=" + paragraphViewEndOffset; // NOI18N
        assert (matchOffset <= docViewEndOffset) : "matchOffset=" + matchOffset + // NOI18N
                " > docViewEndOffset=" + docViewEndOffset; // NOI18N

        this.prevViewEndOffset = startOffset;
        this.offsetDelta = offsetDelta;

        lineRoot = doc.getDefaultRootElement();
        lineIndex = lineRoot.getElementIndex(startOffset);
        Element line = lineRoot.getElement(lineIndex);
        lineEndOffset = line.getEndOffset();

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder: <").append(startOffset).append(",").append(endOffset); // NOI18N
            if (matchOffset != endOffset) {
                sb.append("=>").append(matchOffset); // NOI18N
            }
            sb.append(">, modOffset=").append(modOffset);
            sb.append(", docTextLength=").append(docViewEndOffset).append("\nfReplace=");
            if (fReplace != null) {
                sb.append(fReplace);
            } else {
                sb.append("<NULL>\n");
            }
            sb.append("dReplace=").append(dReplace);
            sb.append("lineIndex=").append(lineIndex);
            sb.append(", createLocalViews=").append(createLocalViews);
            sb.append('\n');

            LOG.fine(sb.toString());
        }
        this.factoryStates = new FactoryState[viewFactories.length];
        for (int i = 0; i < viewFactories.length; i++) {
            FactoryState state = new FactoryState(viewFactories[i], startOffset);
            state.init(startOffset, matchOffset);
            state.updateNextViewStartOffset(startOffset);
            factoryStates[i] = state;
        }
        pReplaceList = new ArrayList<ViewReplace<ParagraphView, EditorView>>(2);
    }

    void createViews() {
        if (prevViewEndOffset > matchOffset) {
            throw new IllegalStateException(
                "prevViewEndOffset=" + prevViewEndOffset + " > matchOffset=" + matchOffset); // NOI18N
        }

        boolean doCreateViews = (prevViewEndOffset < matchOffset);
        if (prevViewEndOffset == matchOffset) {
            if (fReplace != null) {
                assert (fReplace == pReplace);
                // Check if fReplace's view becomes empty (all child views removed)
                // and possibly remove it fully.
                if (fReplace.added == null && fReplace.removeCount == fReplace.view.getViewCount()) {
                    assert (fReplace.index == 0) : "Invalid full-remove fReplace: " + fReplace; // NOI18N
                    // Mark fReplace for removal
                    fReplace = null;
                    pReplace = null;
                    dReplace.index--;
                    dReplace.removeCount++;
                    // Leave (doCreateViews == false) => Do not create views
                } else { // otherwise just partial removal
                    // Check if last child view (NewlineView) is not removed.
                    // If it would be removed then remove the next paragraph view
                    // since otherwise the fReplace would not end with NewlineView.
                    boolean newlineViewRetained = (fReplace.removeEndIndex() < fReplace.view.getViewCount());
                    checkRemoveParagraphs(prevViewEndOffset, newlineViewRetained);
                }
            } // fReplace == null => leave (doCreateViews == false)
        }

        if (doCreateViews) {
            try {
                // Create all new views
                while (createNextView()) {
                }
            } catch (IllegalStateException ex) { // Re-throw with more info
                throw new IllegalStateException("ViewBuilder: Error in view creation: prevViewEndOffset=" +
                        prevViewEndOffset + ", matchOffset=" + matchOffset +
                        ", docViewEndOffset=" + docViewEndOffset + ", lineEndOffset=" + lineEndOffset +
                        ", viewRemovalFinished=" + viewRemovalFinished, ex);
            }
        }

        if (pReplace != null && pReplace != fReplace) { // Unfinished pReplace
            throw new IllegalStateException("Unfinished non-first replace - error during view replacement: view:\n" + // NOI18N
                    dReplace.view + "\n\npReplace:\n" + pReplace + "\nfReplace:\n" + fReplace); // NOI18N
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("ViewBuilder-creationEndOffset=" + prevViewEndOffset + "\n");
        }
        if (LOG.isLoggable(Level.FINER)) {
            if (LOG.isLoggable(Level.FINEST)) {
                // Log original docView state
                // Use separate string builder to at least log original state if anything goes wrong.
                LOG.finer("ViewBuilder-Original:\n" + dReplace.view.toStringDetail() + '\n');
            }
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder.createViews():\n");
            Document doc = dReplace.view.getDocument();
            sb.append("Creation for document: ").append(doc).append('\n');
            if (fReplace != null) {
                sb.append("fReplace:").append(fReplace);
            }
            sb.append("dReplace:").append(dReplace);
            sb.append("pReplaceList:\n");
            int digitCount = ArrayUtilities.digitCount(pReplaceList.size());
            for (int i = 0; i < pReplaceList.size(); i++) {
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(pReplaceList.get(i));
            }
            LOG.fine(sb.toString());
        }
    }

    /**
     * Create next view.
     * @return true if the creation of views should continue or false if it should end.
     */
    boolean createNextView() {
        int limitOffset = matchOffset;
        if (limitOffset > docViewEndOffset) {
            throw new IllegalStateException("matchOffset=" + matchOffset + // NOI18N
                    " > docViewEndOffset=" + docViewEndOffset + "\ndocView:\n" + dReplace.view); // NOI18N
        }
        for (int i = factoryStates.length - 1; i >= 0; i--) {
            FactoryState state = factoryStates[i];
            int cmp = state.nextViewStartOffset - prevViewEndOffset;
            if (cmp < 0) { // Next view starting below
                state.updateNextViewStartOffset(prevViewEndOffset);
                cmp = state.nextViewStartOffset - prevViewEndOffset;
            }
            if (cmp == 0) { // Candidate for the next view
                // Create new view. Note that the limitOffset is only a suggestion.
                // Only the bottommost highlights-view-factory should always respect the the limitOffset.
                if (prevViewEndOffset < 0) {
                    throw new IllegalStateException("prevViewEndOffset=" + prevViewEndOffset + " < 0"); // NOI18N
                }
                if (prevViewEndOffset >= limitOffset) {
                    throw new IllegalStateException("prevViewEndOffset=" + prevViewEndOffset + // NOI18N
                            " >= limitOffset=" + limitOffset + ", docTextLength=" + docViewEndOffset); // NOI18N
                }
                if (limitOffset > docViewEndOffset) {
                    throw new IllegalStateException("limitOffset=" + limitOffset + " > docTextLength=" + docViewEndOffset); // NOI18N
                }
                EditorView view = null;
                int createdViewEndOffset;
                if (createLocalViews) { // Regular views creation
                    view = state.factory.createView(prevViewEndOffset, limitOffset);
                    if (view == null) { // Refused => Use a next factory
                        continue;
                    }
                    createdViewEndOffset = prevViewEndOffset + view.getLength();
                } else {
                    createdViewEndOffset = state.factory.viewEndOffset(prevViewEndOffset, limitOffset);
                    if (createdViewEndOffset == -1) { // Refused => Use a next factory
                        continue;
                    }
                }

                if (createdViewEndOffset > docViewEndOffset) {
                    throw new IllegalStateException("View " + view + " produced by factory " + state.factory + // NOI18N
                            " has endOffset=" + createdViewEndOffset + " but docTextLength=" + docViewEndOffset); // NOI18N
                }

                updateLine(createdViewEndOffset);
                boolean eolView = (createdViewEndOffset == lineEndOffset);
                // Make space for new views by replacing old ones.
                // When fReplace is active then only local removals are done unless
                // a NewlineView gets created in which case the views till the end
                // of a fReplace's view must be removed (they would have to be re-parented otherwise).
                // If fReplace is not active then remove full paragraph views
                // (again to avoid re-parenting of local views to new paragraph views).
                if (!viewRemovalFinished) {
                    if (fReplace != null && fReplace == pReplace) { // Still replacing in fReplace
                        // Check if remove till end of paragraph
                        if (createdViewEndOffset > paragraphViewEndOffset || eolView) {
                            fReplace.removeTillEnd();
                            if (paragraphViewEndOffset > docViewEndOffset) {
                                throw new IllegalStateException("paragraphViewEndOffset=" + paragraphViewEndOffset + // NOI18N
                                        " > docViewEndOffset=" + docViewEndOffset + "\ndocView:\n" + dReplace.view); // NOI18N
                            }
                            matchOffset = paragraphViewEndOffset;
                            // Possibly need to remove next paragraph views
                            checkRemoveParagraphs(createdViewEndOffset, eolView);
                        } else if (createdViewEndOffset > matchOffset) {
                            // Remove single views and not go beyond paragraph view's end
                            int viewCount = fReplace.view.getViewCount();
                            int index;
                            while ((index = fReplace.removeEndIndex()) < viewCount) {
                                // Use getLength() instead of getEndOffset() since for intra-line mods
                                // with offsetDelta != 0 the views do not have updated offsets
                                matchOffset += pReplace.view.getEditorView(index).getLength();
                                if (matchOffset > docViewEndOffset) {
                                    throw new IllegalStateException("matchOffset=" + matchOffset + // NOI18N
                                            " > docViewEndOffset=" + docViewEndOffset + // NOI18N
                                            ", pReplace-view-length=" + pReplace.view.getEditorView(index).getLength() + // NOI18N
                                            "\ndocView:\n" + dReplace.view); // NOI18N
                                }
                                pReplace.removeCount++;
                                if (createdViewEndOffset <= matchOffset) {
                                    break;
                                }
                            }
                            assert (index < viewCount) : "Replace includes last local view; viewCount=" + // NOI18N
                                    viewCount + ", matchOffset=" + matchOffset + // NOI18N
                                    ", paragraphViewEndOffset=" + paragraphViewEndOffset + // NOI18N
                                    ", docTextLength=" + docViewEndOffset; // NOI18N
                        }
                    } else { // Remove whole paragraph(s)
                        checkRemoveParagraphs(createdViewEndOffset, eolView);
                    }
                }
                assert (viewRemovalFinished || createdViewEndOffset <= matchOffset) :
                    "createdViewEndOffset=" + createdViewEndOffset + " > matchOffset=" + matchOffset + // NOI18N
                    ", docTextLength=" + docViewEndOffset; // NOI18N

                if (pReplace == null) { // Finished a paragraph view previously
                    // TODO Could possibly grab the start pos from line element
                    Position startPos;
                    try {
                        startPos = dReplace.view.getDocument().createPosition(prevViewEndOffset);
                    } catch (BadLocationException e) {
                        throw new IllegalStateException("Cannot create position at offset=" + prevViewEndOffset);
                    }
                    ParagraphView paragraphView = new ParagraphView(startPos);
                    dReplace.add(paragraphView);
                    pReplace = new ViewReplace<ParagraphView, EditorView>(paragraphView, 0);
                    if (createLocalViews) {
                        pReplaceList.add(pReplace);
                    }
                }
                if (createLocalViews) {
                    pReplace.add(view);
                }

                if (eolView) {
                    // Init view's length except for first replace where it's updated by EBVChildren.replace()
                    if (fReplace != pReplace) {
                        int length = createdViewEndOffset - pReplace.view.getStartOffset();
                        pReplace.view.setLength(length);
                    }
                    pReplace = null;
                }

                prevViewEndOffset = createdViewEndOffset;
                // Continue creation until matchOffset is reached
                // but also in case when it was reached but the created views do not
                // finish a paragraph view (pReplace is non-null and it's not a first-replace
                // where it's allowed to finish without newline-view creation).
                return (prevViewEndOffset < matchOffset);

            } else { // cmp > 0 => next view starting somewhere above last view's end offset
                // Remember the nextViewStartOffset as a limit offset for factories
                // that lay below this factory
                if (state.nextViewStartOffset < docViewEndOffset) { // Can be Integer.MAX_VALUE
                    limitOffset = state.nextViewStartOffset;
                    if (limitOffset > docViewEndOffset) {
                        throw new IllegalStateException("state: limitOffset=" + limitOffset + // NOI18N
                                " > docViewEndOffset=" + docViewEndOffset + "\ndocView:\n" + dReplace.view); // NOI18N
                    }
                }
            }
        }
        // The code should not get there since the highlights-view-factory (at index 0)
        // should always provide a view.
        throw new IllegalStateException("No factory returned view for offset=" + prevViewEndOffset);
    }

    private void checkRemoveParagraphs(int createdViewEndOffset, boolean newlineViewCreated) {
        while (createdViewEndOffset > matchOffset ||
                (!newlineViewCreated && createdViewEndOffset == matchOffset))
        {
            int index = dReplace.removeEndIndex();
            if (index < dReplace.view.getViewCount()) {
                ParagraphView removeView = (ParagraphView) dReplace.view.getEditorView(index);
                dReplace.removeCount++;
                paragraphViewEndOffset += removeView.getLength();
                // Here the matchOffset > docViewEndOffset is allowed
                // since checkRemoveParagraphs() can be used from ViewBuilder's constructor
                // to compute paragraph views affected by just performed removal.
                matchOffset = paragraphViewEndOffset;
            } else { // No more views to remove
                viewRemovalFinished = true; // Allow to finish the loop
                matchOffset = paragraphViewEndOffset = docViewEndOffset;
                break;
            }
        }
    }

    void replaceAndRepaintViews() {
        // Compute repaint region as area of views being removed
        DocumentView docView = dReplace.view;
        final JTextComponent textComponent = docView.getTextComponent();
        final Rectangle repaintBounds = new Rectangle(0,0,-1,-1);
        assert (textComponent != null) : "Null textComponent"; // NOI18N
        boolean docViewHeightChanged = false;
        boolean docViewWidthChanged = false;
        Rectangle2D.Double docViewBounds = docView.getAllocation();
        TextLayoutCache textLayoutCache = docView.getTextLayoutCache();
        VisualUpdate<?> fUpdate = null;
        if (fReplace != null) {
            // Clear individual views from textLayoutCache
            fUpdate = fReplace.replaceViews(offsetDelta);
            if (fUpdate != null) {
                // fReplace is at (dReplace.index - 1)
                Shape childAlloc = docView.getChildAllocation(dReplace.index - 1, docViewBounds);
                fUpdate.updateSpansAndLayout(childAlloc);
                if (fUpdate.isPreferenceChanged()) {
                    docViewWidthChanged |= fUpdate.isWidthChanged();
                    docViewHeightChanged |= fUpdate.isHeightChanged();
                }
                if (!fUpdate.getRepaintBounds().isEmpty()) {
                    repaintBounds.add(fUpdate.getRepaintBounds());
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.fine("fReplace:REPAINT:" + ViewUtils.toString(fUpdate.getRepaintBounds()) + '\n');
                    }
                }
            }
        }

        // Remove paragraphs from text-layout-cache
        for (int i = 0; i < dReplace.removeCount; i++) {
            ParagraphView paragraphView = (ParagraphView) docView.getEditorView(dReplace.index + i);
            if (paragraphView.children != null) {
                textLayoutCache.remove(paragraphView);
            }
        }
//        String err = textLayoutCache.findIntegrityError(); if (err != null) throw new IllegalStateException(err);

        // Repaint removed paragraph views
        dReplace.retainSpans(); // Attempt to retain spans of paragraph views
        VisualUpdate<?> dUpdate = dReplace.replaceViews(0);
        // dUpdate.updateSpansAndLayout() will be done later once
        // all the paragraph-update.updateSpansAndLayout() get called.
        // This way the exact measurement of paragraph-views heights can only be done
        // in one iteration.

        for (int i = 0; i < pReplaceList.size(); i++) {
            ViewReplace<ParagraphView, EditorView> replace = pReplaceList.get(i);
            VisualUpdate<?> pUpdate = replace.replaceViews(0);
            if (pUpdate != null) {
                Shape childAlloc = docView.getChildAllocation(dReplace.index + i, docViewBounds);
                pUpdate.updateSpansAndLayout(childAlloc);
                if (pUpdate.isPreferenceChanged()) {
                    docViewWidthChanged |= pUpdate.isWidthChanged();
                    docViewHeightChanged |= pUpdate.isHeightChanged();
                }
                if (!pUpdate.getRepaintBounds().isEmpty()) {
                    repaintBounds.add(pUpdate.getRepaintBounds());
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.fine("pReplaceList[" + i + "]:REPAINT:" + // NOI18N
                                ViewUtils.toString(pUpdate.getRepaintBounds()) + '\n');
                    }
                }
            }
        }

        if (dUpdate != null) {
            dUpdate.updateSpansAndLayout(docViewBounds);
            if (dUpdate.isPreferenceChanged()) {
                docViewWidthChanged |= dUpdate.isWidthChanged();
                docViewHeightChanged |= dUpdate.isHeightChanged();
            }
            if (!dUpdate.getRepaintBounds().isEmpty()) {
                repaintBounds.add(dUpdate.getRepaintBounds());
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.fine("dReplace:REPAINT:" + ViewUtils.toString(dUpdate.getRepaintBounds()) + '\n');
                }
            }
        }
        
        // Since fUpdate is not visually included in dUpdate => use docView.preferenceChanged()
        if (fUpdate != null && fUpdate.isPreferenceChanged()) {
            // Explicitly call preferenceChanged() which will fix vertical span
            // in docView if the paragraph view's height changed
            docView.preferenceChanged(dReplace.index - 1,   
                    fUpdate.isWidthChanged(), fUpdate.isHeightChanged(), false);
        }

        if (!repaintBounds.isEmpty()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.fine("REPAINT-bounds:" + ViewUtils.toString(repaintBounds) + '\n');
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.repaint(textComponent, repaintBounds);
                }
            });
        }
        if (docViewWidthChanged || docViewHeightChanged) {
            docView.preferenceChanged(null, docViewWidthChanged, docViewHeightChanged);
        }
    }

    void finish() {
        // Finish factories
        for (FactoryState factoryState : factoryStates) {
            factoryState.factory.finish();
        }

        dReplace.view.checkIntegrity();
    }

    /**
     * Update line so that it "contains" the offset or the <code>offset == lineEndOffset</code>
     * @param offset
     */
    void updateLine(int offset) {
        while (offset > lineEndOffset) {
            lineIndex++;
            Element line = lineRoot.getElement(lineIndex);
            lineEndOffset = line.getEndOffset();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("-------- ViewBuilder dump -------\n");
        sb.append("prevViewEndOffset=").append(prevViewEndOffset).append('\n');
        sb.append("offsetDelta=").append(offsetDelta).append('\n');
        sb.append("docTextLength=").append(docViewEndOffset).append('\n');
        sb.append("lineIndex=").append(lineIndex).append('\n');
        sb.append("lineEndOffset=").append(lineEndOffset).append('\n');
        sb.append("paragraphViewEndOffset=").append(paragraphViewEndOffset).append('\n');
        sb.append("matchOffset=").append(matchOffset).append('\n');
        sb.append("fReplace=").append(fReplace).append('\n');
        sb.append("dReplace=").append(dReplace).append('\n');
        sb.append("pReplace=").append(pReplace).append('\n');
        sb.append("pReplaceList=").append(pReplaceList).append('\n');
        sb.append("viewRemovalFinished=").append(viewRemovalFinished).append('\n');
        sb.append("-------- End of ViewBuilder dump -------\n");
        return sb.toString();
    }

    private static final class FactoryState {

        final EditorViewFactory factory;

        int nextViewStartOffset;

        FactoryState(EditorViewFactory factory, int startOffset) {
            this.factory = factory;
        }

        void init(int startOffset, int matchOffset) {
            factory.restart(startOffset, matchOffset);
        }

        void updateNextViewStartOffset(int offset) {
            nextViewStartOffset = factory.nextViewStartOffset(offset);
            if (nextViewStartOffset < offset) {
                throw new IllegalStateException("Editor view factory " + factory + // NOI18N
                        " returned nextViewStartOffset=" + nextViewStartOffset + // NOI18N
                        " < offset=" + offset); // NOI18N
            }
        }

    }

}
