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

import java.awt.Shape;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.ViewFactory;

/**
 * Update paragraph views by document and view factory changes.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUpdates {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUpdates.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUpdates.class.getName());

    private final DocumentView documentView;

    private EditorViewFactory[] viewFactories;

    private FactoriesListener factoriesListener;

    private boolean delayedRebuild;

    public ViewUpdates(DocumentView documentView) {
        this.documentView = documentView;
        factoriesListener = new FactoriesListener();
    }

    private void reinitFactories() {
        if (viewFactories != null) {
            for (int i = 0; i < viewFactories.length; i++) {
                viewFactories[i].removeEditorViewFactoryListener(factoriesListener);
            }
            viewFactories = null;
        }
        initFactories();
    }

    private void initFactories() {
        assert (viewFactories == null);
        JTextComponent component = documentView.getTextComponent();
        assert (component != null) : "Null component; doc=" + documentView.getDocument(); // NOI18N
        List<EditorViewFactory.Factory> factoryFactories = EditorViewFactory.factories();
        viewFactories = new EditorViewFactory[factoryFactories.size()];
        for (int i = 0; i < factoryFactories.size(); i++) {
            viewFactories[i] = factoryFactories.get(i).createEditorViewFactory(component);
            viewFactories[i].addEditorViewFactoryListener(factoriesListener);
        }
    }

    void reinitViews() {
        // Insert into document was performed -> update or rebuild views
        // First update factories since they may fire rebuilding
        Document doc = documentView.getDocument();
        checkFactoriesComponentInited();
        ViewBuilder viewBuilder = new ViewBuilder(
                new ViewReplace<DocumentView,ParagraphView>(documentView, 0),
                null, viewFactories, 0, doc.getLength() + 1, 0);
        try {
            viewBuilder.createViews();
            viewBuilder.repaintAndReplaceViews();
        } finally {
            viewBuilder.finish(); // Includes factory.finish() in each factory
        }
    }

    public void insertUpdate(DocumentEvent evt, Shape alloc, ViewFactory viewFactory) {
        // Insert into document was performed -> update or rebuild views
        // First update factories since they may fire rebuilding
        checkFactoriesComponentInited();
        delayedRebuild = true;
        try {
            for (int i = 0; i < viewFactories.length; i++) {
                EditorViewFactory editorViewFactory = viewFactories[i];
                editorViewFactory.insertUpdate(evt);
            }
        } finally {
            delayedRebuild = false;
        }

        // Check if the factories fired any changes
        int rebuildStartOffset = factoriesListener.rebuildStartOffset;;
        int rebuildEndOffset = factoriesListener.rebuildEndOffset;
        boolean noRebuild = (rebuildStartOffset == Integer.MAX_VALUE);
        int insertOffset = evt.getOffset();
        int insertLength = evt.getLength();
        rebuildStartOffset = Math.min(rebuildStartOffset, insertOffset);
        rebuildEndOffset = Math.max(rebuildEndOffset, insertOffset + insertLength);
        Element[] addedLines = null;
        // If line elements were modified the views will be modified too
        Document doc = evt.getDocument();
        DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
        if (lineElementChange != null) {
            Element[] removedLines = lineElementChange.getChildrenRemoved();
            if (removedLines.length > 0) { // Insertion at line's begining
                noRebuild = false;
                assert (removedLines.length == 1) : "Expected 1 removed line during insert only"; // NOI18N
                Element removedLine = removedLines[0];
                int removedLineStartOffset = removedLine.getStartOffset();
                int removedLineEndOffset = removedLine.getEndOffset();
                assert (insertOffset >= removedLineStartOffset && insertOffset <= removedLineEndOffset);
                if (removedLineStartOffset < rebuildStartOffset) {
                    rebuildStartOffset = removedLineStartOffset;
                }
                if (removedLineEndOffset > rebuildEndOffset) {
                    rebuildEndOffset = removedLineEndOffset;
                }
            }
            addedLines = lineElementChange.getChildrenAdded();
            noRebuild &= (addedLines == null || addedLines.length == 0);
        }
        int paragraphViewIndex = documentView.getViewIndex(rebuildStartOffset);
        assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
        ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);

        if (noRebuild) {
            // Just inform the view at the offset to contain more data
            // Use rebuildEndOffset if there was a move inside views
            int childViewIndex = paragraphView.getViewIndex(rebuildEndOffset);
            EditorView childView = paragraphView.getEditorView(childViewIndex);
            if (insertOffset == childView.getStartOffset()) { // View starting right at insertOffset => use previous
                childViewIndex--;
                if (childViewIndex < 0) {
                    noRebuild = false;
                } else {
                    childView = paragraphView.getEditorView(childViewIndex); // re-get childView at new index
                }
            }
            if (noRebuild) {
                // View may refuse length setting in which case it must be rebuilt
                noRebuild = childView.setLength(childView.getLength() + insertLength);
                // Update offsets of the views that follow the modified one
                if (noRebuild) {
                    double visualDelta = childView.getPreferredSpan(paragraphView.getMajorAxis()) -
                            paragraphView.getViewMajorAxisSpan(childViewIndex);
                    // [TODO] fix line wrap info
                    paragraphView.fixSpans(childViewIndex + 1, insertLength, visualDelta);
                }
            }
        }

        if (!noRebuild) {
            ViewReplace<DocumentView, ParagraphView> docViewReplace =
                    new ViewReplace<DocumentView, ParagraphView>(documentView, paragraphViewIndex);
            ViewReplace<ParagraphView,EditorView> localReplace = new ViewReplace<ParagraphView,EditorView>(
                    paragraphView, paragraphView.getViewIndex(rebuildStartOffset));
            docViewReplace.index++; // Increase index since paragraph view of local replace won't be removed
            // Views contained in paragraph view are not updated yet by the inserted length
            int paragraphViewEndOffset = paragraphView.getEndOffset() + insertLength;
            if (rebuildEndOffset < paragraphViewEndOffset) {
                // Rebuild till end of line so that the local views do not need to be updated
                rebuildEndOffset = paragraphViewEndOffset;
            }
            ViewBuilder viewBuilder = new ViewBuilder(docViewReplace, localReplace,
                    viewFactories, rebuildStartOffset, rebuildEndOffset, insertLength);
            try {
                viewBuilder.createViews();
                viewBuilder.repaintAndReplaceViews();
            } finally {
                viewBuilder.finish(); // Includes factory.finish() in each factory
            }
        }

        checkIntegrity();
    }

    public void removeUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        // Removal in document was performed -> update or rebuild views
        checkFactoriesComponentInited();
        delayedRebuild = true;
        try {
            for (int i = 0; i < viewFactories.length; i++) {
                EditorViewFactory editorViewFactory = viewFactories[i];
                editorViewFactory.removeUpdate(evt);
            }
        } finally {
            delayedRebuild = false;
        }

        // Check if the factories fired any changes
        int rebuildStartOffset = factoriesListener.rebuildStartOffset;;
        int rebuildEndOffset = factoriesListener.rebuildEndOffset;
        boolean noRebuild = (rebuildStartOffset == Integer.MAX_VALUE);
        int removeOffset = evt.getOffset();
        int removeLength = evt.getLength();
        rebuildStartOffset = Math.min(rebuildStartOffset, removeOffset);
        rebuildEndOffset = Math.max(rebuildEndOffset, removeOffset + removeLength);
        // If line elements were modified the views will be modified too
        Document doc = evt.getDocument();
        DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
        Element[] removedLines = null;
        if (lineElementChange != null) {
            removedLines = lineElementChange.getChildrenRemoved();
            if (removedLines.length > 0) { // Insertion at line's begining
                noRebuild = false;
                int removedLineStartOffset = removedLines[0].getStartOffset();
                int removedLineEndOffset = removedLines[removedLines.length - 1].getEndOffset();
                if (removedLineStartOffset < rebuildStartOffset) {
                    rebuildStartOffset = removedLineStartOffset;
                }
                if (removedLineEndOffset > rebuildEndOffset) {
                    rebuildEndOffset = removedLineEndOffset;
                }
            }
        }
        int paragraphViewIndex = documentView.getViewIndex(rebuildStartOffset);
        assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
        ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);

        if (noRebuild) {
            // Just inform the view at the offset to contain more data
            // Use rebuildEndOffset if there was a move inside views
            int childViewIndex = paragraphView.getViewIndex(rebuildEndOffset);
            EditorView childView = paragraphView.getEditorView(childViewIndex);
            int childStartOffset = childView.getStartOffset();
            int childEndOffset = childView.getEndOffset();
            noRebuild = ((removeOffset == childStartOffset && removeOffset + removeLength < childEndOffset) ||
                    (removeOffset > childStartOffset && removeOffset + removeLength <= childEndOffset));
            if (noRebuild) {
                // View may refuse length setting in which case it must be rebuilt
                noRebuild = childView.setLength(childView.getLength() - removeLength);
                // Update offsets of the views that follow the modified one
                if (noRebuild) {
                    double visualDelta = childView.getPreferredSpan(paragraphView.getMajorAxis()) -
                            paragraphView.getViewMajorAxisSpan(childViewIndex);
                    // [TODO] fix line wrap info
                    paragraphView.fixSpans(childViewIndex + 1, -removeLength, visualDelta);
                }
            }
        }

        if (!noRebuild) {
            ViewReplace<DocumentView, ParagraphView> docViewReplace =
                    new ViewReplace<DocumentView, ParagraphView>(documentView, paragraphViewIndex);
            ViewReplace<ParagraphView,EditorView> localReplace = new ViewReplace<ParagraphView,EditorView>(
                    paragraphView, paragraphView.getViewIndex(rebuildStartOffset));
            docViewReplace.index++; // Increase index since paragraph view of local replace won't be removed
            // Views contained in paragraph view are not updated yet by the inserted length
            int paragraphViewEndOffset = paragraphView.getEndOffset() - removeLength;
            if (rebuildEndOffset < paragraphViewEndOffset) {
                // Rebuild till end of line so that the local views do not need to be updated
                rebuildEndOffset = paragraphViewEndOffset;
            }
            ViewBuilder viewBuilder = new ViewBuilder(docViewReplace, localReplace,
                    viewFactories, rebuildStartOffset, rebuildEndOffset, removeLength);
            try {
                viewBuilder.createViews();
                viewBuilder.repaintAndReplaceViews();
            } finally {
                viewBuilder.finish(); // Includes factory.finish() in each factory
            }
        }

        checkIntegrity();
    }

    public void changedUpdate(DocumentEvent evt, Shape a, ViewFactory f) {
        checkFactoriesComponentInited();
        delayedRebuild = true;
        try {
            for (int i = 0; i < viewFactories.length; i++) {
                EditorViewFactory editorViewFactory = viewFactories[i];
                editorViewFactory.changedUpdate(evt);
            }
        } finally {
            delayedRebuild = false;
        }

        // TODO finish
        checkIntegrity();
    }

    private void checkIntegrity() {
        if (LOG.isLoggable(Level.FINE)) {
            String err = documentView.findIntegrityError();
            LOG.fine(err);
        }
    }

    private void checkFactoriesComponentInited() {
        if (viewFactories == null) {
            initFactories();
        }
    }

    /*private*/ void checkRebuild() {
        synchronized (documentView.getMonitor()) {
            if (!delayedRebuild) {
                if (factoriesListener.isModified()) {
                    int rebuildStartOffset = factoriesListener.rebuildStartOffset;
                    int rebuildEndOffset = factoriesListener.rebuildEndOffset;
                    int paragraphViewIndex = documentView.getViewIndex(rebuildStartOffset);
                    assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
                    EditorView child = documentView.getEditorView(paragraphViewIndex);
                    ParagraphView paragraphView = (ParagraphView) child;
                    // Use paragraphViewIndex+1 since localReplace's paragraph view won't be removed
                    ViewReplace<DocumentView,ParagraphView> docViewReplace =
                            new ViewReplace<DocumentView,ParagraphView>(documentView, paragraphViewIndex + 1);
                    ViewReplace<ParagraphView,EditorView> localReplace = new ViewReplace<ParagraphView,EditorView>(
                            paragraphView, paragraphView.getViewIndex(rebuildStartOffset));
                    ViewBuilder viewBuilder = new ViewBuilder(docViewReplace, localReplace,
                            viewFactories, rebuildStartOffset, rebuildEndOffset, 0);
                    try {
                        viewBuilder.createViews();
                        viewBuilder.repaintAndReplaceViews();
                    } finally {
                        viewBuilder.finish(); // Includes factory.finish() in each factory
                    }

                    factoriesListener.reset();
                }
            }
        }
    }


    private final class FactoriesListener implements EditorViewFactoryListener {

        int rebuildStartOffset;

        int rebuildEndOffset;

        FactoriesListener() {
            reset();
        }

        boolean isModified() {
            return rebuildStartOffset != Integer.MAX_VALUE;
        }

        void reset() {
            rebuildStartOffset = Integer.MAX_VALUE;
            rebuildEndOffset = Integer.MIN_VALUE;
        }

        @Override
        public void viewFactoryChanged(EditorViewFactoryEvent evt) {
            List<EditorViewFactory.Change> changes = evt.getChanges();
            for (EditorViewFactory.Change change : changes) {
                if (change.getStartOffset() < rebuildStartOffset) {
                    rebuildStartOffset = change.getStartOffset();
                }
                if (change.getEndOffset() > rebuildEndOffset) {
                    rebuildEndOffset = change.getEndOffset();
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ViewUpdates.Change <" + change.getStartOffset() +
                            "," + change.getEndOffset() + ">"
                            );
                }
                checkRebuild();
            }
        }

    }

}
