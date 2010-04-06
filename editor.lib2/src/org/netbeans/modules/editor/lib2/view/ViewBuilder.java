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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private int lastCreatedViewEndOffset;

    private final int offsetDelta;

    private final Element lineRoot;

    private int lineIndex;

    private int lineStartOffset;

    private int lineEndOffset;
    
    private int paragraphViewEndOffset = Integer.MIN_VALUE;

    private int matchOffset = Integer.MIN_VALUE;

    private final FactoryState[] factoryStates;

    private final ViewReplace<DocumentView,ParagraphView> dReplace;

    /**
     * First replace or null if none.
     * It's in ParagraphView[dReplace.index-1].
     */
    private ViewReplace<ParagraphView,EditorView> fReplace;

    private ViewReplace<ParagraphView,EditorView> pReplace;

    private boolean viewRemovalFinished;

    private List<ViewReplace<ParagraphView,EditorView>> pReplaceList;

    private int docTextLength; // doc.getLength()+1 which includes an extra newline

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
     * @param endModOffset it must be endOffset for no-mod; insertOffset+insertLength for inserts
     *  and removeOffset+removeLength for removals.
     * @param offsetDelta it's 0 for no-mod; +insertLength for inserts; -removeLength for removals.
     */
    ViewBuilder(ParagraphView paragraphView, DocumentView documentView, int paragraphViewIndex,
            EditorViewFactory[] viewFactories, int startOffset, int endOffset,
            int endModOffset, int offsetDelta)
    {
        Document doc = documentView.getDocument();
        docTextLength = doc.getLength() + 1;
        if (paragraphView != null) {
            fReplace = new ViewReplace<ParagraphView, EditorView>(
                    paragraphView, paragraphView.getViewIndex(startOffset));
            this.pReplace = fReplace;
            paragraphViewIndex++; // dReplace will start from next paragraph view
        }
        dReplace = new ViewReplace<DocumentView, ParagraphView>(documentView, paragraphViewIndex);
        // Search for the views that need to be removed.
        // Must search in original offsets to after-insert offsets by using both childView.getLength()
        // and paragraphView.getLength() which return textual span of existing views
        // (unaffected by possibly just performed modification(s)).
        int endAffectedOffset = Math.max(endModOffset, endOffset);
        if (fReplace != null) {
            int paragraphViewStartOffset = paragraphView.getStartOffset();
            assert (paragraphViewStartOffset <= startOffset);
            EditorView childView = fReplace.childViewAtIndex();
            // Round start offset to child's start offset
            startOffset = childView.getStartOffset();
            assert (paragraphViewStartOffset <= startOffset);
            // Get paragraph end offset in original offset coordinates
            paragraphViewEndOffset = paragraphViewStartOffset + paragraphView.getLength();
            if (endAffectedOffset < paragraphViewEndOffset) {
                // Rebuild located inside fReplace's paragraph view
                matchOffset = startOffset; // childView's start offset
                while (matchOffset < endAffectedOffset) {
                    matchOffset += childView.getLength();
                    fReplace.removeCount++;
                    int index = fReplace.removeEndIndex(); // Should be within PV's bounds
                    childView = paragraphView.getEditorView(index);
                }
                assert (matchOffset >= endAffectedOffset);
            } else {
                fReplace.removeTillEnd(); // Remove all remaining child views
                matchOffset = paragraphViewEndOffset;
            }
        }
        if (matchOffset < endAffectedOffset) {
            int paragraphCount = documentView.getViewCount();
            int index = dReplace.removeEndIndex();
            if (index < paragraphCount) {
                EditorView nextParagraphView = documentView.getEditorView(index);
                if (paragraphViewEndOffset == Integer.MIN_VALUE) {
                    paragraphViewEndOffset = nextParagraphView.getStartOffset();
                }
                paragraphViewEndOffset += nextParagraphView.getLength();
                matchOffset = paragraphViewEndOffset;
                dReplace.removeCount++;
                checkRemoveParagraphs(endAffectedOffset, false);
            } else {
                viewRemovalFinished = true;
                matchOffset = paragraphViewEndOffset = docTextLength;
            }
        }
        assert (matchOffset >= 0) : "matchOffset=" + matchOffset; // NOI18N
        assert (paragraphViewEndOffset >= 0) : "paragraphViewEndOffset=" + paragraphViewEndOffset; // NOI18N

        // Apply offsetDelta to operate in actual offset coordinates
        if (!viewRemovalFinished && offsetDelta != 0) {
            matchOffset += offsetDelta;
            paragraphViewEndOffset += offsetDelta;
        }

        assert (matchOffset >= 0) : "matchOffset=" + matchOffset; // NOI18N
        assert (paragraphViewEndOffset >= 0) : "paragraphViewEndOffset=" + paragraphViewEndOffset; // NOI18N

        this.lastCreatedViewEndOffset = startOffset;
        this.offsetDelta = offsetDelta;

        lineRoot = doc.getDefaultRootElement();
        lineIndex = lineRoot.getElementIndex(startOffset);
        Element line = lineRoot.getElement(lineIndex);
        lineStartOffset = line.getStartOffset();
        lineEndOffset = line.getEndOffset();

        if (LOG.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder: <").append(startOffset).append(",").append(endOffset); // NOI18N
            if (matchOffset != endOffset) {
                sb.append("=>").append(matchOffset); // NOI18N
            }
            sb.append(">, endModOffset=").append(endModOffset);
            sb.append(", docTextLength=").append(docTextLength).append("\nfReplace=");
            if (fReplace != null) {
                sb.append(fReplace);
            } else {
                sb.append("<NULL>\n");
            }
            sb.append("dReplace=").append(dReplace);
            sb.append("lineIndex=").append(lineIndex);
            sb.append(", lineStartOffset=").append(lineStartOffset).append('\n');

            LOG.fine(sb.toString());
        }
        this.factoryStates = new FactoryState[viewFactories.length];
        for (int i = 0; i < viewFactories.length; i++) {
            FactoryState state = new FactoryState(viewFactories[i], startOffset);
            state.init(startOffset);
            state.updateNextViewStartOffset(startOffset);
            factoryStates[i] = state;
        }
        pReplaceList = new ArrayList<ViewReplace<ParagraphView, EditorView>>(2);
    }

    void createViews() {
        assert (lastCreatedViewEndOffset <= matchOffset) :
            "lastCreatedViewEndOffset=" + lastCreatedViewEndOffset + " > matchOffset=" + // NOI18N
            matchOffset;

        boolean doCreateViews = (lastCreatedViewEndOffset < matchOffset);
        if (lastCreatedViewEndOffset == matchOffset) {
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
                    checkRemoveParagraphs(lastCreatedViewEndOffset, newlineViewRetained);
                }
            } // fReplace == null => leave (doCreateViews == false)
        }

        if (doCreateViews) {
            // Create all new views
            while (createNextView()) {
            }
        }

        if (pReplace != null && pReplace != fReplace) { // Unfinished pReplace
            throw new IllegalStateException("Unfinished non-first replace pReplace=" + pReplace);
        }

        if (LOG.isLoggable(Level.FINE)) {
            if (LOG.isLoggable(Level.FINER)) {
                // Log original docView state
                // Use separate string builder to at least log original state if anything goes wrong.
                LOG.finer("ViewBuilder-Original:\n" + dReplace.view.toStringDetail() + '\n');
            }
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder.createViews():\n");
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
        for (int i = factoryStates.length - 1; i >= 0; i--) {
            FactoryState state = factoryStates[i];
            int cmp = state.nextViewStartOffset - lastCreatedViewEndOffset;
            if (cmp < 0) { // Next view starting below
                state.updateNextViewStartOffset(lastCreatedViewEndOffset);
                cmp = state.nextViewStartOffset - lastCreatedViewEndOffset;
            }
            if (cmp == 0) { // Candidate for the next view
                // Create new view. Note that the limitOffset is only a suggestion.
                // Only the bottommost highlights-view-factory should always respect the the limitOffset.
                assert (lastCreatedViewEndOffset >= 0) :
                    "lastCreatedViewEndOffset=" + lastCreatedViewEndOffset + " < 0"; // NOI18N
                assert (lastCreatedViewEndOffset < limitOffset) :
                    "lastCreatedViewEndOffset=" + lastCreatedViewEndOffset + // NOI18N
                    " >= limitOffset=" + limitOffset + ", docTextLength=" + docTextLength; // NOI18N
                assert (limitOffset <= docTextLength) :
                    "limitOffset=" + limitOffset + " > docTextLength=" + docTextLength; // NOI18N
                EditorView view = state.factory.createView(lastCreatedViewEndOffset, limitOffset);
                boolean newlineViewCreated = (view instanceof NewlineView);
                int createdViewEndOffset = lastCreatedViewEndOffset + view.getLength();
                if (createdViewEndOffset > docTextLength) {
                    throw new IllegalStateException("View " + view + " produced by factory " + state.factory + // NOI18N
                            " has endOffset=" + createdViewEndOffset + " but docTextLength=" + docTextLength); // NOI18N
                }

                // Make space for new views by replacing old ones.
                // When fReplace is active then only local removals are done unless
                // a NewlineView gets created in which case the views till the end
                // of a fReplace's view must be removed (they would have to be re-parented otherwise).
                // If fReplace is not active then remove full paragraph views
                // (again to avoid re-parenting of local views to new paragraph views).
                if (!viewRemovalFinished) {
                    if (fReplace != null && fReplace == pReplace) { // Still replacing in fReplace
                        // Check if remove till end of paragraph
                        if (createdViewEndOffset > paragraphViewEndOffset || newlineViewCreated) {
                            fReplace.removeTillEnd();
                            matchOffset = paragraphViewEndOffset;
                            // Possibly need to remove next paragraph views
                            checkRemoveParagraphs(createdViewEndOffset, newlineViewCreated);
                        } else if (createdViewEndOffset > matchOffset) {
                            // Remove single views and not go beyond paragraph view's end
                            int viewCount = fReplace.view.getViewCount();
                            int index;
                            while ((index = fReplace.removeEndIndex()) < viewCount) {
                                // Use getLength() instead of getEndOffset() since for intra-line mods
                                // with offsetDelta != 0 the views do not have updated offsets
                                matchOffset += pReplace.view.getEditorView(index).getLength();
                                pReplace.removeCount++;
                                if (createdViewEndOffset <= matchOffset) {
                                    break;
                                }
                            }
                            assert (index < viewCount) : "Replace includes last local view; viewCount=" + // NOI18N
                                    viewCount + ", matchOffset=" + matchOffset + // NOI18N
                                    ", paragraphViewEndOffset=" + paragraphViewEndOffset + // NOI18N
                                    ", docTextLength=" + docTextLength; // NOI18N
                        }
                    } else { // Remove whole paragraph(s)
                        checkRemoveParagraphs(createdViewEndOffset, newlineViewCreated);
                    }
                }
                assert (viewRemovalFinished || createdViewEndOffset <= matchOffset) :
                    "createdViewEndOffset=" + createdViewEndOffset + " > matchOffset=" + matchOffset + // NOI18N
                    ", docTextLength=" + docTextLength; // NOI18N

                if (pReplace == null) { // Finished a paragraph view previously
                    updateLine();
                    if (lastCreatedViewEndOffset != lineStartOffset) {
                        LOG.info(toString());
                        throw new IllegalStateException("lastCreatedViewEndOffset=" + lastCreatedViewEndOffset + // NOI18N
                        " != lineStartOffset=" + lineStartOffset); // NOI18N
                    }
                    // TODO Could possibly grab the start pos from line element
                    Position startPos;
                    try {
                        startPos = dReplace.view.getDocument().createPosition(lineStartOffset);
                    } catch (BadLocationException e) {
                        throw new IllegalStateException("Cannot create position at offset=" + lineStartOffset);
                    }
                    ParagraphView paragraphView = new ParagraphView(startPos);
                    dReplace.add(paragraphView);
                    pReplace = new ViewReplace<ParagraphView,EditorView>(paragraphView, 0);
                    pReplaceList.add(pReplace);
                }
                pReplace.add(view);


                if (newlineViewCreated) {
                    pReplace = null;
                }

                lastCreatedViewEndOffset = createdViewEndOffset;
                // Continue creation until matchOffset is reached
                // but also in case when it was reached but the created views do not
                // finish a paragraph view (pReplace is non-null and it's not a first-replace
                // where it's allowed to finish without newline-view creation).
                return (lastCreatedViewEndOffset < matchOffset);

            } else { // cmp > 0 => next view starting somewhere above last view's end offset
                // Remember the nextViewStartOffset as a limit offset for factories
                // that lay below this factory
                if (state.nextViewStartOffset < docTextLength) { // Can be Integer.MAX_VALUE
                    limitOffset = state.nextViewStartOffset;
                }
            }
        }
        // The code should not get there since the highlights-view-factory (at index 0)
        // should always provide a view.
        throw new IllegalStateException("No factory returned view for offset=" + lastCreatedViewEndOffset);
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
                // Do not remove individual views
                matchOffset = paragraphViewEndOffset;
            } else { // No more views to remove
                viewRemovalFinished = true; // Allow to finish the loop
                matchOffset = paragraphViewEndOffset = docTextLength;
                break;
            }
        }
    }

    void repaintAndReplaceViews() {
        // Compute repaint region as area of views being removed
        DocumentView docView = dReplace.view;
        JTextComponent textComponent = docView.getTextComponent();
        assert (textComponent != null) : "Null textComponent"; // NOI18N
        boolean docViewHeightChanged = false;
        boolean docViewWidthChanged = false;
        Rectangle repaintBounds = new Rectangle(0,0,-1,-1);
        Rectangle2D.Double docViewBounds = docView.getAllocation();
        TextLayoutCache textLayoutCache = docView.getTextLayoutCache();
        if (fReplace != null) {
            if (fReplace.removeCount > 0) {
                textLayoutCache.remove(fReplace.view, fReplace.index, fReplace.removeCount);
            }
            // fReplace is at (dReplace.index - 1)
            Shape childAlloc = docView.getChildAllocation(dReplace.index - 1, docViewBounds);
            // Clear individual views from textLayoutCache
            EditorBoxView.ReplaceResult fResult = fReplace.replaceViews(offsetDelta, childAlloc);
            if (fResult != null) {
                if (fResult.isPreferenceChanged()) {
                    docView.preferenceChanged(dReplace.index - 1,
                            fResult.isWidthChanged(), fResult.isHeightChanged(), false);
                    docViewWidthChanged |= fResult.isWidthChanged();
                    docViewHeightChanged |= fResult.isHeightChanged();
                }
                if (!fResult.getRepaintBounds().isEmpty()) {
                    repaintBounds.add(fResult.getRepaintBounds());
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("fReplace:REPAINT:" + ViewUtils.toString(fResult.getRepaintBounds()) + '\n');
                    }
                }
            }
        }

        // Remove paragraphs from text-layout-cache
        for (int i = 0; i < dReplace.removeCount; i++) {
            ParagraphView paragraphView = (ParagraphView) docView.getEditorView(dReplace.index + i);
            textLayoutCache.removeParagraph(paragraphView);
        }

        // Repaint removed paragraph views
        EditorBoxView.ReplaceResult dResult = dReplace.replaceViews(0, docViewBounds);
        if (dResult != null) {
            if (dResult.isPreferenceChanged()) {
                docViewWidthChanged |= dResult.isWidthChanged();
                docViewHeightChanged |= dResult.isHeightChanged();
            }
            if (!dResult.getRepaintBounds().isEmpty()) {
                repaintBounds.add(dResult.getRepaintBounds());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("dReplace:REPAINT:" + ViewUtils.toString(dResult.getRepaintBounds()) + '\n');
                }
            }
        }
        for (int i = 0; i < pReplaceList.size(); i++) {
            ViewReplace<ParagraphView, EditorView> replace = pReplaceList.get(i);
            Shape childAlloc = docView.getChildAllocation(dReplace.index + i, docViewBounds);
            EditorBoxView.ReplaceResult pResult = replace.replaceViews(0, childAlloc);
            if (pResult != null) {
                if (pResult.isPreferenceChanged()) {
                    docView.preferenceChanged(dReplace.index + i,
                            pResult.isWidthChanged(), pResult.isHeightChanged(), false);
                    docViewWidthChanged |= pResult.isWidthChanged();
                    docViewHeightChanged |= pResult.isHeightChanged();
                }
                if (!pResult.getRepaintBounds().isEmpty()) {
                    repaintBounds.add(pResult.getRepaintBounds());
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("pReplaceList[" + i + "]:REPAINT:" + // NOI18N
                                ViewUtils.toString(pResult.getRepaintBounds()) + '\n');
                    }
                }
            }
        }
        if (!repaintBounds.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("REPAINT-bounds:" + ViewUtils.toString(repaintBounds) + '\n');
            }
            ViewUtils.repaint(textComponent, repaintBounds);
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

    void updateLine() {
        while (lastCreatedViewEndOffset >= lineEndOffset) {
            lineIndex++;
            Element line = lineRoot.getElement(lineIndex);
            lineStartOffset = line.getStartOffset();
            lineEndOffset = line.getEndOffset();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("-------- ViewBuilder dump -------\n");
        sb.append("lastCreatedViewEndOffset=").append(lastCreatedViewEndOffset).append('\n');
        sb.append("offsetDelta=").append(offsetDelta).append('\n');
        sb.append("docTextLength=").append(docTextLength).append('\n');
        sb.append("lineIndex=").append(lineIndex).append('\n');
        sb.append("lineStartOffset=").append(lineStartOffset).append('\n');
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

        void init(int startOffset) {
            factory.restart(startOffset);
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
