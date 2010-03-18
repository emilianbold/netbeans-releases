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

    /**
     * @param viewFactories should be sorted with increasing priority.
     */
    ViewBuilder(ParagraphView paragraphView, DocumentView documentView, int paragraphViewIndex,
            EditorViewFactory[] viewFactories, int startOffset, int endOffset, int offsetDelta)
    {
        Document doc = documentView.getDocument();
        if (paragraphView != null) {
            fReplace = new ViewReplace<ParagraphView, EditorView>(
                    paragraphView, paragraphView.getViewIndex(startOffset));
            this.pReplace = fReplace;
            paragraphViewIndex++; // dReplace will start from next paragraph view
        }
        dReplace = new ViewReplace<DocumentView, ParagraphView>(documentView, paragraphViewIndex);
        // Search for the views that need to be removed
        // Map original offsets to after-insert offsets by using both childView.getLength()
        // and paragraphView.getLength() which return textual span of views

        if (fReplace != null) {
            int paragraphViewStartOffset = paragraphView.getStartOffset();
            assert (paragraphViewStartOffset <= startOffset);
            EditorView childView = fReplace.childViewAtIndex();
            startOffset = childView.getStartOffset();
            assert (paragraphViewStartOffset <= startOffset);
            paragraphViewEndOffset = paragraphViewStartOffset + paragraphView.getLength()
                    + offsetDelta; // Updated end offset
            if (paragraphViewEndOffset >= endOffset) { // Rebuild located inside fReplace's paragraph view
                matchOffset = startOffset + offsetDelta; // childView's start offset + offsetDelta
                while (matchOffset < endOffset) {
                    matchOffset += childView.getLength();
                    fReplace.removeCount++;
                    int index = fReplace.removeEndIndex();
                    if (index < paragraphView.getViewCount()) {
                        childView = paragraphView.getEditorView(index);
                    } else {
                        assert (matchOffset >= endOffset);
                        break;
                    }
                }
            } else {
                // Leave matchOffset == -1
                fReplace.removeTillEnd(); // Remove all remaining child views
            }
        }
        if (matchOffset < endOffset) {
            int paragraphCount = documentView.getViewCount();
            int index = dReplace.removeEndIndex();
            if (index < paragraphCount) {
                EditorView nextParagraphView = documentView.getEditorView(index);
                // Use startOffset + getLength() to get proper original length and update by offsetDelta
                // If paragraphViewEndOffset already initialized only update it since for valid fReplace
                // the nextParagraphView's startOffset could already be above the modification point.
                if (paragraphViewEndOffset == Integer.MIN_VALUE) {
                    paragraphViewEndOffset = nextParagraphView.getStartOffset() + offsetDelta;
                }
                paragraphViewEndOffset += nextParagraphView.getLength();
                dReplace.removeCount++;
                while (matchOffset < endOffset) {
                    index = dReplace.removeEndIndex();
                    if (index >= paragraphCount) {
                        viewRemovalFinished = true;
                        matchOffset = doc.getLength() + 1;
                        break;
                    }
                    nextParagraphView = dReplace.view.getEditorView(index);
                    paragraphViewEndOffset += nextParagraphView.getLength();
                    matchOffset = paragraphViewEndOffset;
                    dReplace.removeCount++;
                }
            } else {
                viewRemovalFinished = true;
                matchOffset = paragraphViewEndOffset = doc.getLength() + 1;
            }
        }
        assert (matchOffset != -1);

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
            sb.append(">\nfReplace=");
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
            factoryStates[i] = new FactoryState(viewFactories[i], startOffset);
            factoryStates[i].factory.restart(startOffset);
        }
        pReplaceList = new ArrayList<ViewReplace<ParagraphView, EditorView>>(2);
    }

    void createViews() {
        // Create all new views
        while (createNextView()) { }

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

    boolean createNextView() {
        int limitOffset = matchOffset;
        for (int i = factoryStates.length - 1; i >= 0; i--) {
            FactoryState state = factoryStates[i];
            int cmp = state.nextViewStartOffset - lastCreatedViewEndOffset;
            if (cmp < 0) { // Next view starting below
                state.nextViewStartOffset = state.factory.nextViewStartOffset(lastCreatedViewEndOffset);
                cmp = state.nextViewStartOffset - lastCreatedViewEndOffset;
                if (cmp < 0) {
                    throw new IllegalStateException("EditorViewFactory " + state.factory + // NOI18N
                            " returned nextViewStartOffset=" + state.nextViewStartOffset + // NOI18N
                            " for offset=" + lastCreatedViewEndOffset); // NOI18N

                }
            }
            if (cmp == 0) { // Candidate for the next view
                // Create new view. Note that the limitOffset is only a suggestion.
                // Only the bottommost highlights-view-factory should always respect the the limitOffset.
                EditorView view = state.factory.createView(lastCreatedViewEndOffset, limitOffset);
                boolean newlineViewCreated = (view instanceof NewlineView);
                int createdViewEndOffset = lastCreatedViewEndOffset + view.getLength();

                while (!viewRemovalFinished && createdViewEndOffset > matchOffset) {
                    if (fReplace != null && dReplace.removeCount == 0) { // Still replacing in fReplace
                        // Check if remove till end of paragraph
                        if (createdViewEndOffset > paragraphViewEndOffset || newlineViewCreated) {
                            fReplace.removeTillEnd();
                            matchOffset = paragraphViewEndOffset;
                        } else { // Remove single views and not go beyond paragraph view's end
                            int viewCount = fReplace.view.getViewCount();
                            int index;
                            while ((index = fReplace.removeEndIndex()) < viewCount) {
                                // Use getLength() instead of getEndOffset() since for intra-line mods
                                // with offsetDelta != 0 the views do not have updated offsets
                                matchOffset += pReplace.view.getEditorView(index).getLength();
                                pReplace.removeCount++;
                                if (matchOffset >= createdViewEndOffset) {
                                    break;
                                }
                            }
                        }
                    } else { // Remove whole paragraph(s)
                        int pIndex = dReplace.removeEndIndex();
                        if (pIndex < dReplace.view.getViewCount()) {
                            ParagraphView removeView = (ParagraphView) dReplace.view.getEditorView(pIndex);
                            dReplace.removeCount++;
                            paragraphViewEndOffset = removeView.getEndOffset();
                            // Do not remove individual views
                            matchOffset = paragraphViewEndOffset;
                        } else { // No more views to remove
                            viewRemovalFinished = true; // Allow to finish the loop
                        }
                    }
                }

                if (pReplace == null) { // Finished a paragraph view previously
                    updateLine();
                    assert (lastCreatedViewEndOffset == lineStartOffset) :
                        "lastCreatedViewEndOffset=" + lastCreatedViewEndOffset + // NOI18N
                        " != lineStartOffset=" + lineStartOffset; // NOI18N
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
                return (lastCreatedViewEndOffset < matchOffset);

            } else { // cmp > 0 => next view starting somewhere above last view's end offset
                // Remember the nextViewStartOffset as a limit offset for factories
                // that lay below this factory
                limitOffset = state.nextViewStartOffset;
            }
        }
        // The code should not get there since the highlights-view-factory (at index 0)
        // should always provide a view.
        throw new IllegalStateException("No factory returned view for offset=" + lastCreatedViewEndOffset);
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
                        LOG.fine("fReplace:REPAINT:" + ViewUtils.toString(fResult.getRepaintBounds()));
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
                    LOG.fine("dReplace:REPAINT:" + ViewUtils.toString(dResult.getRepaintBounds()));
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
                                ViewUtils.toString(pResult.getRepaintBounds()));
                    }
                }
            }
        }
        if (!repaintBounds.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("REPAINT:" + ViewUtils.toString(repaintBounds));
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

    private static final class FactoryState {

        final EditorViewFactory factory;

        int nextViewStartOffset;

        FactoryState(EditorViewFactory factory, int startOffset) {
            this.factory = factory;
            this.nextViewStartOffset = factory.nextViewStartOffset(startOffset);
        }

    }

}
