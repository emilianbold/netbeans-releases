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
 * area of the new view must be removed. When firstReplace is non-null
 * then the views can be replaced locally in a paragraph view.
 * However if the replace exceeds a local replace then full paragraph views
 * are being removed and recreated. This is because otherwise the remaining
 * local views would have to be re-parented because new paragraph view instances
 * are being created and used.
 * 
 * @author Miloslav Metelka
 */

final class ViewBuilder {

    /**
     * Maximum number of scanned characters for which the view building will produce local views.
     */
    private static final int MAX_CHARS_FOR_CREATE_LOCAL_VIEWS = 2000;

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewBuilder.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewBuilder.class.getName());

    /**
     * Replace of paragraph views in a document view.
     */
    private final ViewReplace<DocumentView,ParagraphView> docReplace;

    private FactoryState[] factoryStates;

    private boolean createLocalViews; // Whether children of paragraph views are created

    private int creationOffset;

    private int matchOffset;
    
    private int modLength;
    
    private int docViewStartOffset;

    /**
     * Remember docView's end offset. View factories should generally
     * not create views above this boundary although it can happen if e.g.
     * there's a collapsed fold view around that offset.
     */
    private final int docViewEndBoundOffset;
    
    private Element lineRoot;

    private int lineIndex;

    private int lineEndOffset;
    
    /**
     * Line element to be possibly used for a paragraph view's start position.
     */
    private Element lineForParagraphView;
    
    /**
     * First local replace or null if none.
     * It's in ParagraphView[docReplace.index-1] and it is attempted in certain cases
     * (see checkLocalRebuild()) to make the view rebuilds as small as possible.
     * However if the rebuild continues across lines then the granularity extends to whole
     * paragraph views (matchOffset is increased by whole paragraph views).
     */
    private ViewReplace<ParagraphView,EditorView> firstReplace;

    /**
     * Actual local views replace inside currently served paragraph view.
     * <br/>
     * It may be equal to firstReplace when replacing inside first paragraph view.
     */
    private ViewReplace<ParagraphView,EditorView> localReplace;

    /**
     * List of all paragraph replaces done so far in docReplace (firstReplace is not part
     * of allReplaces).
     */
    private List<ViewReplace<ParagraphView,EditorView>> allReplaces;
    
    private volatile boolean staleCreation;
    
    private int startCreationOffset;
    
    private static enum RebuildCause {
        FULL_REBUILD, // Full rebuild of all paragraphs
        CHANGED_REGION, // Rebuild a document region that has changed (e.g. highlights changed or fold collapsed/expanded)
        MOD_UPDATE, // Update after modification in the document
        INIT_PARAGRAPHS // Initialize children of one or more paragraphs
    }
    
    /** Cause of the rebuild for logging purposes. */
    private RebuildCause rebuildCause;

    /**
     * Construct view builder.
     * @param docView non-null doc view for which view building is performed.
     * @param viewFactories view factories that should be sorted with increasing priority.
     */
    ViewBuilder(DocumentView docView, EditorViewFactory[] viewFactories) {
        // Always do document-replace since the built views can extend beyond firstReplace even for very local changes
        this.docReplace = new ViewReplace<DocumentView, ParagraphView>(docView);
        this.factoryStates = new FactoryState[viewFactories.length];
        for (int i = 0; i < viewFactories.length; i++) {
            factoryStates[i] = new FactoryState(viewFactories[i]);
        }
        this.docViewStartOffset = docView.getStartOffset();
        this.docViewEndBoundOffset = docView.getEndBoundOffset();
        this.createLocalViews = docView.op.isAccurateSpan();
    }
    
    /**
     * Init for build/rebuild of all paragraph views in the document view.
     */
    void initFullRebuild() {
        docReplace.removeTillEnd();
        creationOffset = docViewStartOffset;
        matchOffset = docViewEndBoundOffset;
        rebuildCause = RebuildCause.FULL_REBUILD;
        // No local rebuild => leave firstReplace == null
    }

    /**
     * Init requested paragraphs views.
     * @param paragraphViewIndex index of first paragraph where the rebuilding occurs.
     * @param endOffset ending offset of the rebuild.
     */
    void initParagraphs(int startRebuildIndex, int endRebuildIndex, int startOffset, int endOffset) {
        this.createLocalViews = true; // Force local views creation
        docReplace.index = startRebuildIndex;
        docReplace.removeCount = endRebuildIndex - startRebuildIndex;
        creationOffset = startOffset;
        matchOffset = endOffset;
        rebuildCause = RebuildCause.INIT_PARAGRAPHS;
    }

    /**
     * Initialize rebuild of a given changed region.
     *
     * @param rRegion non-null changed region
     * @return true if the change affects the view hierarchy and the createViews()
     *  method should be called. If false is returned createViews() should not be called
     *  and just finish() should be called.
     */
    boolean initChangedRegionRebuild(OffsetRegion rRegion) {
        DocumentView docView = docReplace.view;
        int startAffectedOffset = rRegion.startOffset();
        int endAffectedOffset = rRegion.endOffset();
        int startRebuildIndex = -1; // Index of first paragraph view to be rebuilt
        if (docView.hasExtraStartBound() && endAffectedOffset < docViewStartOffset) {
            // Affected area completely below docView's start
        } else if (docView.hasExtraEndBound() && startAffectedOffset >= docViewEndBoundOffset) {
            // Affected area completely above docView's end
        } else {
            startRebuildIndex = docView.getViewIndex(startAffectedOffset);
            // would be -1 for pViewCount == 0
        }
        if (startRebuildIndex == -1) {
            // Signal that factories were not restarted
            factoryStates = null;
            return false;
        }
        updateRebuildIndexes(startRebuildIndex, endAffectedOffset);
        checkCreateLocalViews(startAffectedOffset, endAffectedOffset);
        checkLocalRebuild(startAffectedOffset, endAffectedOffset, 0);
        rebuildCause = RebuildCause.CHANGED_REGION;
        return true;
    }

    /**
     * Initialize view builder after document modification.
     *
     * @param modOffset it must be endOffset for no-mod; insertOffset for inserts
     *  and removeOffset for removals.
     * @param modLength it's 0 for no-mod; +insertLength for inserts; -removeLength for removals.
     * @param rRegion rebuild region in after-mod offset space or null if no extra
     *  affected area.
     * @return true if the change affects the view hierarchy and the createViews()
     *  method should be called. If false is returned createViews() should not be called
     *  and just finish() should be called.
     */
    boolean initModUpdate(int modOffset, int modLength, OffsetRegion rRegion) {
        this.modLength = modLength;
        DocumentView docView = docReplace.view;
        int startAffectedOffset = modOffset;
        // For removal use modOffset+1 so that for removal that shifted several paragraph's
        // starting positions back to modOffset would find proper last paragraph view
        int endAffectedOffset = modOffset + Math.max(modLength, 1);
        if (rRegion != null) { // Possibly union with rebuild region
            startAffectedOffset = Math.min(startAffectedOffset, rRegion.startOffset());
            endAffectedOffset = Math.max(endAffectedOffset, rRegion.endOffset());
        }
        int startRebuildIndex = -1; // Index of first paragraph view to be rebuilt
        boolean allowLocalRebuild = true;
        // [TESTING] For ViewHierarchyTest.testRandom() to identify right TestRootView:
        // Cond.breakpoint exp here: docReplace.view.getTextComponent().getClientProperty("id").equals(6)
        if (docView.hasExtraStartBound() && endAffectedOffset < docViewStartOffset) {
            // Affected area completely below docView's start
        } else if (docView.hasExtraEndBound() && startAffectedOffset > docViewEndBoundOffset) {
            // Affected area completely above docView's end
        } else if (docView.hasExtraStartBound() && modLength > 0 && modOffset + modLength == docViewStartOffset) {
            // Insert right at docView's start position but it moved forward with insertion
            // Note that positions at offset == 0 do not move so this would not happen for docViewStartOffset==0
            // Inserted right at the start-bound position => move bound back to insertion start
            try {
                docView.setStartPosition(docView.getDocument().createPosition(modOffset));
                docViewStartOffset = modOffset;
            } catch (BadLocationException ex) {
                throw new IllegalStateException("Unexpected BadLocationException", ex);
            }
            startRebuildIndex = 0;
            // Prohibit local rebuild since the docView has just updated its start pos
            // but the first paragraph view still carries the original (moved) position
            allowLocalRebuild = false;
        } else {
            startRebuildIndex = docView.getViewIndex(startAffectedOffset);
            if (modLength > 0) { // Insert
                // Check for insert right at pView's begining => should rebuild next pView since
                // it's affected by the insertion
                if (endAffectedOffset == modOffset + modLength) {
                    if (startRebuildIndex + 1 < docView.getViewCount() &&
                            docView.getParagraphView(startRebuildIndex + 1).getStartOffset() == modOffset + modLength)
                    { // Insert at begining of pView => force rebuild of next pView
                        endAffectedOffset++;
                        allowLocalRebuild = false;
                    }
                }
            } else { // Removal
                // If pView starts right at modOffset it might get moved back due to removal
                // in which case it's necessary to rebuild previous view
                if (startAffectedOffset == modOffset) {
                    if (startRebuildIndex < docView.getViewCount() &&
                            docView.getParagraphView(startRebuildIndex).getStartOffset() == modOffset)
                    {
                        if (startRebuildIndex > 0) {
                            startRebuildIndex--; // Rebuild previous view
                            // getViewIndexFirst() ensures that the startRebuildIndex pointed to first affected pView
                        } else {
                            allowLocalRebuild = false; // Ensure real rebuild
                        }
                    }
                }
            }
        }

        if (startRebuildIndex == -1) {
            // Signal that factories were not restarted
            factoryStates = null;
            return false;
        }
        updateRebuildIndexes(startRebuildIndex, endAffectedOffset);
        checkCreateLocalViews(startAffectedOffset, endAffectedOffset);
        if (allowLocalRebuild) {
            checkLocalRebuild(startAffectedOffset, endAffectedOffset, modLength);
        }
        rebuildCause = RebuildCause.MOD_UPDATE;
        return true;
    }
    
    boolean createReplaceRepaintViews(boolean force) {
        if (!createViews(force)) {
            return false;
        }
        replaceRepaintViews();
        return true;
    }
    
    private void updateRebuildIndexes(int startRebuildIndex, int endAffectedOffset) {
        DocumentView docView = docReplace.view;
        docReplace.index = startRebuildIndex;
        creationOffset = (startRebuildIndex != 0)
                ? docView.getParagraphView(startRebuildIndex).getStartOffset()
                : docViewStartOffset;
        int pViewCount = docView.getViewCount();
        // Find ending paragraph index for rebuild; look to next paragraph start to possibly save one binary search
        int endRebuildIndex = docReplace.index; // Start value (there cna possibly be no pViews at all)
        matchOffset = docViewEndBoundOffset;
        if (endRebuildIndex < pViewCount) {
            endRebuildIndex++; // Check if change till next paragraph would suffice
            if (endRebuildIndex < pViewCount) {
                int nextParagraphViewOffset = docView.getView(endRebuildIndex).getStartOffset();
                if (endAffectedOffset > nextParagraphViewOffset) {
                    endRebuildIndex = docView.getViewIndex(endAffectedOffset) + 1;
                    if (endRebuildIndex < pViewCount) {
                        matchOffset = docView.getView(endRebuildIndex).getStartOffset();
                    } // Leave matchOffset == docViewEndBoundOffset
                } else { // nextParagraphViewOffset >= endAffectedOffset
                    matchOffset = nextParagraphViewOffset;
                }
            } // endRebuildIndex == pViewCount => Leave matchOffset == docViewEndBoundOffset
        } // Leave matchOffset == docViewEndBoundOffset
        docReplace.removeCount = endRebuildIndex - docReplace.index;
    }
    
    private void checkCreateLocalViews(int startAffectedOffset, int endAffectedOffset) {
        // If not creating local views decide to do so for small rebuilds
        if (!createLocalViews) {
            createLocalViews = (endAffectedOffset - startAffectedOffset < MAX_CHARS_FOR_CREATE_LOCAL_VIEWS);
        }
    }
        
    private void checkLocalRebuild(int startAffectedOffset, int endAffectedOffset, int modLength) {
        // Check whether local single-paragraph update should be attempted
        if (docReplace.removeCount == 1) {
            ParagraphView pView = (ParagraphView) docReplace.view.getParagraphView(docReplace.index);
            if (pView.getViewCount() > 0) { // Also requires (pView.children != null)
                // For local rebuild use (startAffectedOffset-1) since otherwise
                // the rebuild could be done right at boundary of an existing local view
                // so two adjacent views could be created instead of a single one
                // that would naturally be created when rebuilding entire paragraph views.
                int startLocalIndex = pView.getViewIndex(startAffectedOffset - 1);
                EditorView startLocalView = pView.getEditorView(startLocalIndex);
                int localViewCount = pView.getViewCount();
                int endLocalIndex;
                // The endAffectedOffset in case of removal is always at least modOffset
                // or possibly >modOffset if there is a valid rRegion.
                // Similarly for inserts it's (modOffset+modLength) or higher.
                // Subtract inserted length or add removed length i.e. -modLength in both cases
                int origEndAffectedOffset = endAffectedOffset - modLength;
                // pView: startOffset by pos; endOffset = startOffset + getLength()
                int pViewStartOffset = pView.getStartOffset();
                // Local rebuild for intra-line mods only. Otherwise tests fail:
                // For before-first-line removal which "touches" start of first pView there would be
                // valid firstReplace created but the code counts that mod is in fact inside line (e.g.
                // origEndAffectedOffset is computed that way etc.). Therefore that constraint is applied.
                if (startAffectedOffset > pViewStartOffset && origEndAffectedOffset <= pView.getEndOffset()) {
                    EditorView localView = pView.getEditorView(startLocalIndex);
                    if (origEndAffectedOffset <= localView.getEndOffset()) {
                        endLocalIndex = startLocalIndex + 1;
                    } else {
                        endLocalIndex = Math.min(pView.getViewIndex(origEndAffectedOffset) + 1, localViewCount);
                    }
                    if (startLocalIndex > 0 || endLocalIndex < localViewCount) { // Not all children removed
                        firstReplace = new ViewReplace<ParagraphView, EditorView>(pView);
                        firstReplace.index = startLocalIndex;
                        firstReplace.removeCount = endLocalIndex - startLocalIndex;
                        creationOffset = startLocalView.getStartOffset();
                        if (endLocalIndex < localViewCount) {
                            matchOffset = pView.getView(endLocalIndex).getStartOffset() + modLength;
                        } else { // Removal includes last view (newline-view)
                            // Leave matchOffset at its original value which was begining of next paragraph view
                            // or docViewEndBoundOffset
                        }
                        localReplace = firstReplace;
                        // Skip current paragraph view from removal
                        docReplace.index++;
                        docReplace.removeCount--;
                    }
                }
            }
        }
    }
    
    boolean createViews(boolean force) {
        startCreationOffset = creationOffset; // Remember for logging and firing
        if (creationOffset > matchOffset) {
            throw new IllegalStateException(
                "creationOffset=" + creationOffset + " > matchOffset=" + matchOffset); // NOI18N
        }

        Document doc = docReplace.view.getDocument();
        lineRoot = doc.getDefaultRootElement();
        lineIndex = lineRoot.getElementIndex(creationOffset);
        Element line = lineRoot.getElement(lineIndex);
        lineEndOffset = line.getEndOffset();
        lineForParagraphView = line;

        for (int i = 0; i < factoryStates.length; i++) {
            FactoryState state = factoryStates[i];
            state.init(this, creationOffset, matchOffset);
        }
        allReplaces = new ArrayList<ViewReplace<ParagraphView, EditorView>>(2);

        if (creationOffset < matchOffset) {
            // Create all new views
            while (createNextView()) {
                if (staleCreation && !force) {
                    ViewStats.incrementStaleViewCreations();
                    if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
                        ViewHierarchyImpl.BUILD_LOG.fine("STALE-CREATION notified => View Rebuild Terminated\n"); // NOI18N
                    }
                    return false;
                }
            }
        }

        if (localReplace != null && localReplace != firstReplace) {
            // Unfinished paragraph replace which is however not a replace inside a first paragraph
            // This should only happen if the document view is created with explicit ending bound.
            // It means that the last paragraph view will not have a NewlineView as its last view.
            assert (docReplace.view.hasExtraEndBound()) :
                    "No ending newline view for document view without explicit start bound.";
            // The "unclosed" pReplace already added to pReplaceList.
            // Set total length of the pReplace's paragraph view
            int length = creationOffset - localReplace.view.getStartOffset();
            localReplace.view.setLength(length);
            localReplace = null;
        }

        // Check whether firstReplace replaces all views in the paragraph view with no added views.
        // In such case remove whole pView since it would otherwise stay empty which would be wrong.
        if (firstReplace != null && firstReplace.isMakingViewEmpty()) {
            // Remove whole pView
            docReplace.index--;
            docReplace.removeCount++;
            firstReplace = null;
        }

        if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
            if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINEST)) {
                // Log original docView state
                // Use separate string builder to at least log original state if anything goes wrong.
                ViewHierarchyImpl.BUILD_LOG.finer("ViewBuilder: DocView-Original-Content:\n" + // NOI18N
                        docReplace.view.toStringDetailUnlocked() + '\n'); // NOI18N
            }
            StringBuilder sb = new StringBuilder(200);
            sb.append("ViewBuilder.createViews(): in <").append(startCreationOffset); // NOI18N
            sb.append(",").append(creationOffset).append("> cause:").append(rebuildCause).append("\n"); // NOI18N
            sb.append("Document:").append(doc).append('\n'); // NOI18N
            if (firstReplace != null) {
                sb.append("FirstReplace:\n").append(firstReplace); // NOI18N
            } else {
                sb.append("No-FirstReplace\n"); // NOI18N
            }
            sb.append("DocReplace:\n").append(docReplace); // NOI18N
            sb.append("pReplaceList:\n"); // NOI18N
            int digitCount = ArrayUtilities.digitCount(allReplaces.size());
            for (int i = 0; i < allReplaces.size(); i++) {
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(allReplaces.get(i));
            }
            sb.append("-------------END-OF-VIEW-REBUILD-------------\n"); // NOI18N
            ViewUtils.log(ViewHierarchyImpl.BUILD_LOG, sb.toString());
        }
        return true;
    }

    /**
     * Create next view.
     * @return true if the creation of views should continue or false if it should end.
     */
    boolean createNextView() {
        int limitOffset = matchOffset;
        for (int i = factoryStates.length - 1; i >= 0; i--) {
            FactoryState state = factoryStates[i];
            int cmp = state.nextViewStartOffset - creationOffset;
            if (cmp < 0) { // Next view starting below
                state.updateNextViewStartOffset(creationOffset);
                cmp = state.nextViewStartOffset - creationOffset;
            }
            if (cmp == 0) { // Candidate for the next view
                // Create new view. Note that the limitOffset is only a suggestion.
                // Only the bottommost highlights-view-factory should always respect the the limitOffset.
                EditorView view = null;
                int createdViewEndOffset;
                if (createLocalViews) { // Regular views creation
                    view = state.factory.createView(creationOffset, limitOffset);
                    if (view == null) { // Refused => Use a next factory
                        continue;
                    }
                    int viewLength = view.getLength();
                    createdViewEndOffset = creationOffset + viewLength;
                    assert (viewLength > 0) : "viewLength=" + viewLength + " < 0"; // NOI18N
                } else {
                    createdViewEndOffset = state.factory.viewEndOffset(creationOffset, limitOffset);
                    if (createdViewEndOffset == -1) { // Refused => Use a next factory
                        continue;
                    }
                }

                updateLine(createdViewEndOffset);
                boolean eolView = (createdViewEndOffset == lineEndOffset);
                boolean inFirstReplace = (localReplace == firstReplace && firstReplace != null);
                // Make space for new views by replacing old ones.
                // When firstReplace is active then only local removals are done unless
                // a NewlineView gets created in which case the views till the end
                // of a firstReplace's view must be removed (they would have to be re-parented otherwise).
                // If firstReplace is not active then remove full paragraph views
                // (again to avoid re-parenting of local views to new paragraph views).
                if (eolView && inFirstReplace) { // Rest of views on first pagaraph view will be thrown away
                    // Remove local views till end of first paragraph view
                    firstReplace.removeCount = firstReplace.view.getViewCount() - firstReplace.index;
                    int index = docReplace.removeEndIndex();
                    if (index < docReplace.view.getViewCount()) {
                        matchOffset = docReplace.view.getParagraphView(index).getStartOffset();
                    } else {
                        matchOffset = docViewEndBoundOffset;
                    }
                }
                if (createdViewEndOffset > matchOffset) {
                    if (inFirstReplace) { // Replacing in firstReplace
                        int index;
                        int localViewCount = firstReplace.view.getViewCount();
                        while ((index = firstReplace.removeEndIndex()) < localViewCount) {
                            // Use getLength() instead of getEndOffset() since for intra-line mods
                            // with modLength != 0 the views do not have updated offsets
                            matchOffset += localReplace.view.getEditorView(index).getLength();
                            localReplace.removeCount++;
                            // For eolView remove all till end; otherwise only until matchOffset is ok
                            if (createdViewEndOffset <= matchOffset) {
                                break;
                            }
                        }
                    } else { // Remove whole paragraph(s)
                        int pViewCount = docReplace.view.getViewCount();
                        if (docReplace.removeEndIndex() < pViewCount) {
                            do {
                                int index = docReplace.removeNext();
                                if (index < pViewCount) {
                                    matchOffset = docReplace.view.getParagraphView(index).getStartOffset();
                                } else {
                                    matchOffset = docViewEndBoundOffset;
                                    break;
                                }
                            } while (createdViewEndOffset > matchOffset);
                        } // matchOffset already assigned correctly
                    }
                }

                if (localReplace == null) { // Finished a paragraph view previously
                    Position startPos;
                    if (creationOffset == docViewStartOffset && docViewStartOffset != 0) { // Custom start bound
                        // Reuse start position of the document view. This is important since
                        // otherwise when undoing a removal that spanned custom start of document view
                        // the docView.startPos could undo to another offset than firstParagraphView.startPos
                        startPos = docReplace.view.getStartPosition();
                    } else if (lineForParagraphView instanceof Position &&
                            creationOffset == lineForParagraphView.getStartOffset())
                    { // Reuse element as position
                        startPos = (Position) lineForParagraphView;
                    } else { // Create pos
                        try {
                            startPos = docReplace.view.getDocument().createPosition(creationOffset);
                        } catch (BadLocationException e) {
                            throw new IllegalStateException("Cannot create position at offset=" + creationOffset, e);
                        }
                    }
                    ParagraphView paragraphView = new ParagraphView(startPos);
                    docReplace.add(paragraphView);
                    localReplace = new ViewReplace<ParagraphView, EditorView>(paragraphView);
                    // pReplace.index = 0;   <= already set by constructor
                    if (createLocalViews) {
                        allReplaces.add(localReplace);
                    }
                }
                if (createLocalViews) {
                    localReplace.add(view);
                }

                if (eolView) {
                    // Init view's length except for first replace where it's updated by EBVChildren.replace()
                    if (localReplace != firstReplace) {
                        int length = createdViewEndOffset - localReplace.view.getStartOffset();
                        localReplace.view.setLength(length);
                    }
                    localReplace = null;
                    // Attempt to reuse line element as a start position for paragraph view
                    lineForParagraphView = (lineIndex + 1 < lineRoot.getElementCount())
                            ? lineRoot.getElement(lineIndex + 1)
                            : null;
                }

                creationOffset = createdViewEndOffset;
                // Continue creation until matchOffset is reached
                // but also in case when it was reached but the created views do not
                // finish a paragraph view (pReplace is non-null and it's not a first-replace
                // where it's allowed to finish without newline-view creation).
                return (creationOffset < matchOffset);

            } else { // cmp > 0 => next view starting somewhere above last view's end offset
                // Remember the nextViewStartOffset as a limit offset for factories
                // that lay below this factory
                if (state.nextViewStartOffset < limitOffset) { // Can be Integer.MAX_VALUE
                    limitOffset = state.nextViewStartOffset;
                }
            }
        }
        // The code should not get there since the highlights-view-factory (at index 0)
        // should always provide a view.
        throw new IllegalStateException("No factory returned view for offset=" + creationOffset);
    }
    
    private void transcribe(ParagraphView origPView, ParagraphView newPView) {
        float origWidth = origPView.getWidth();
        newPView.setWidth(origWidth);
        float origHeight = origPView.getHeight();
        newPView.setHeight(origHeight);
    }

    private void replaceRepaintViews() {
        // Compute repaint region as area of views being removed
        DocumentView docView = docReplace.view;
        final JTextComponent textComponent = docView.getTextComponent();
        assert (textComponent != null) : "Null textComponent"; // NOI18N
        // Check firstReplace (in PV at (docReplace.index - 1))
        boolean firstReplaceValid = firstReplace != null && firstReplace.isChanged();
        if (firstReplaceValid) {
            // This generally does not affect layout (will be computed later and possibly fire a VH change)
            firstReplace.view.replace(firstReplace.index,
                    firstReplace.removeCount, firstReplace.addedViews(), modLength);
        }
        // Remove paragraphs from text-layout-cache
        TextLayoutCache textLayoutCache = docView.op.getTextLayoutCache();
        for (int i = 0; i < docReplace.removeCount; i++) {
            ParagraphView pView = docView.getParagraphView(docReplace.index + i);
            if (pView.children != null) {
                textLayoutCache.remove(pView, false);
            }
        }
        
        // Possibly retain vertical spans from original views
        // Algorithm first goes from first replaced pView checking whether new pView has the same offset span
        // (and if it does then its width and height are copied from original pView)
        // until first pView with different span is found.
        // If there are any changed pView(s) the algorithm goes from last pView back doing the same thing.
        // The remaining really "new" pView(s) are assigned default row height
        // and their y-span is reported as a changed area into the view hierarchy listener.
        // Since the pViews are based on swing positions it make sense to go from the end back
        // since for document modifications the original position-based pViews will be shifted forward/back
        // accordingly.
        List<ParagraphView> addedPViews = docReplace.added;
        int addedCount = docReplace.addedSize();
        int removeCount = docReplace.removeCount; // Number of pViews to be removed from docView
        int index = docReplace.index;
        // Area between i0 and i1 will be set to default row height
        int i0 = 0; // first changed pView
        int i1 = addedCount; // next after last changed view among added views
        int i1Orig = removeCount; // i1 in current pViews
        int commonCount = Math.min(addedCount, removeCount);
        if (commonCount > 0) {
            ParagraphView origPView = docView.getParagraphView(index);
            ParagraphView newPView = addedPViews.get(0);
            if (origPView.getStartOffset() == newPView.getStartOffset()) {
                while (true) {
                    if (origPView.getLength() != newPView.getLength()) {
                        break;
                    }
                    transcribe(origPView, newPView);
                    i0++;
                    if (i0 >= commonCount) {
                        break;
                    }
                    origPView = docView.getParagraphView(index + i0);
                    newPView = addedPViews.get(i0);
                }
            }
            int endCount = commonCount - i0;
            if (endCount > 0) {
                int i = 1; // subtract index
                origPView = docView.getParagraphView(index + removeCount - i);
                newPView = addedPViews.get(addedCount - i);
                if (origPView.getEndOffset() == newPView.getEndOffset()) { // could in fact compare start offsets too
                    while (true) {
                        if (origPView.getLength() != newPView.getLength()) {
                            i--;
                            break;
                        }
                        transcribe(origPView, newPView);
                        if (i >= commonCount - i0) {
                            break;
                        }
                        i++;
                        origPView = docView.getParagraphView(index + removeCount - i);
                        newPView = addedPViews.get(addedCount - i);
                    }
                    i1 = addedCount - i;
                    i1Orig = removeCount - i;
                }
            }
        }
        // Fill the rest with default row height
        if (i0 != i1) {
            float defaultRowHeight = docView.op.getDefaultRowHeight();
            for (int i = i0; i < i1; i++) {
                ParagraphView addedPView = addedPViews.get(i);
                addedPView.setHeight(defaultRowHeight);
                // Do not set initial width (let it be computed) since it also requires
                // possible notifying of width change. And there also may be line wrapping
                // involved so the estimated width would not comply.
            }
        }
        if (ViewHierarchyImpl.BUILD_LOG.isLoggable(Level.FINE)) {
            ViewHierarchyImpl.BUILD_LOG.fine("Non-Retained Views: " + // NOI18N
                    ((i0 != i1) ? "<" + i0 + "," + i1 + ">" : "NONE") + " of " + addedCount + " new pViews\n"); // NOI18N
        }
        
        
        // New paragraph views are currently not measured (they use spans
        // that were retained from old views or they use defaults).
        ViewHierarchyChange change = docView.validChange();
        // When change is local inside pView then report change till end of pView
        // since the views that follow modification will shift very likely.
        // It's important that the firstReplace replace was already processed
        // so the firstReplace.view has correct end offset.
        int changeEndOffset = (firstReplaceValid && !docReplace.isChanged())
                ? firstReplace.view.getEndOffset()
                : matchOffset; // replace ends at pView boundary
        change.addChange(startCreationOffset, changeEndOffset);

        // firstReplace generally does not affect y although it can but it's determined later
        double startY;
        double endY;
        double deltaY;
        if (docReplace.isChanged()) {
            boolean changeY = false;
            double y0 = 0d;
            double y1 = 0d;
            if (removeCount != addedCount || i0 != i1) {
                changeY = true;
                // Do actual replace AFTER determining y coordinates in order to get original y values
                // Children may be uninitialized (subsequent docView.replace() will init them.
                y0 = (docView.children != null) ? docView.getY(index + i0) : 0d;
                y1 = (i0 != i1Orig) ? docView.getY(index + i1Orig) : y0;
            }
            // Replace views in docView (includes possible call to notifyHeightChange())
            double[] startEndDeltaY = docView.replaceViews(
                    docReplace.index, docReplace.removeCount, docReplace.addedViews());
            startY = startEndDeltaY[0];
            endY = startEndDeltaY[1];
            deltaY = startEndDeltaY[2];
            // Replace contents of each added paragraph view (if the contents are built too).
            for (int i = 0; i < allReplaces.size(); i++) {
                ViewReplace<ParagraphView, EditorView> replace = allReplaces.get(i);
                if (replace.isChanged()) {
                    replace.view.replace(replace.index, replace.removeCount, replace.addedViews());
                }
            }
            // Add y change if necessary
            if (changeY) {
                boolean realChange = true;
                if (deltaY == 0d) { // Attempt extra check
                    // There might be a modification in a single line and since
                    // newPView.getLength() != origPView.getLength() the line was not transcribed.
                    // However if (i1 - i0) == 1 and deltaY == 0d then there was not a physical change in fact.
                    if (i1 - i0 == 1) {
                        realChange = false;
                    }
                }
                if (realChange) {
                    change.addChangeY(y0, y1, deltaY);
                }
            }
        } else { // docReplace empty
            assert firstReplaceValid : "Invalid state - no updates done";
            startY = docView.getY(docReplace.index - 1);
            endY = docView.getY(docReplace.index);
            deltaY = 0d;
        }
        
        // For accurate span force computation of text layouts
        Rectangle2D.Double docViewRect = docView.getAllocationMutable();
        if (docView.op.isAccurateSpan()) {
            int pIndex = docReplace.index;
            int endIndex = docReplace.addEndIndex();
            if (firstReplaceValid) {
                pIndex--;
            }
            for (; pIndex < endIndex; pIndex++) {
                ParagraphView pView = docView.getParagraphView(pIndex);
                Shape childAlloc = docView.getChildAllocation(pIndex, docViewRect);
                if (pView.children != null) {
                    pView.children.ensureIndexMeasured(pView, pView.getViewCount(), ViewUtils.shapeAsRect(childAlloc));
                    docView.children.checkChildrenSpanChange(docView, pIndex);
                }
            }
        }
        
        // Schedule repaints based on current docView allocation.
        // For valid firstReplace the current impl repaints whole line.
        docViewRect.y = startY;
        double endRepaintY = (deltaY != 0d) 
                ? docViewRect.getMaxY() 
                : endY;
        docViewRect.height = endRepaintY - docViewRect.y;
        docView.op.notifyRepaint(docView.op.extendToVisibleWidth(docViewRect));
    }
    
    void finish() {
        // Finish factories
        if (factoryStates != null) {
            for (FactoryState factoryState : factoryStates) {
                factoryState.finish();
            }
        }
        docReplace.view.checkIntegrityIfLoggable();
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
    
    void notifyStaleCreation() {
        staleCreation = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("-------- ViewBuilder dump -------\n");
        sb.append("creationOffset=").append(creationOffset).append('\n');
        sb.append("docViewEndBoundOffset=").append(docViewEndBoundOffset).append('\n');
        sb.append("lineIndex=").append(lineIndex).append('\n');
        sb.append("lineEndOffset=").append(lineEndOffset).append('\n');
        sb.append("matchOffset=").append(matchOffset).append('\n');
        sb.append("modLength=").append(modLength).append('\n');
        sb.append("firstReplace=").append(firstReplace).append('\n');
        sb.append("docReplace=").append(docReplace).append('\n');
        sb.append("pReplace=").append(localReplace).append('\n');
        sb.append("pReplaceList=").append(allReplaces).append('\n');
        sb.append("-------- End of ViewBuilder dump -------\n");
        return sb.toString();
    }

    private static final class FactoryState {

        final EditorViewFactory factory;

        int nextViewStartOffset;

        FactoryState(EditorViewFactory factory) {
            this.factory = factory;
        }

        void init(ViewBuilder viewBuilder, int startOffset, int matchOffset) {
            factory.setViewBuilder(viewBuilder);
            factory.restart(startOffset, matchOffset);
            updateNextViewStartOffset(startOffset);
        }

        void updateNextViewStartOffset(int offset) {
            nextViewStartOffset = factory.nextViewStartOffset(offset);
            if (nextViewStartOffset < offset) {
                throw new IllegalStateException("Editor view factory " + factory + // NOI18N
                        " returned nextViewStartOffset=" + nextViewStartOffset + // NOI18N
                        " < offset=" + offset); // NOI18N
            }
        }

        void finish() {
            factory.finishCreation();
            factory.setViewBuilder(null);
        }

    }

}
