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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
 * Update paragraph views by document and view factory changes.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUpdates implements DocumentListener {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUpdates.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUpdates.class.getName());

    private final DocumentView documentView;

    private EditorViewFactory[] viewFactories;

    private FactoriesListener factoriesListener;

    private DocumentListener incomingModificationListener;

    private boolean incomingModification;

    int rebuildStartOffset;

    int rebuildEndOffset;

    public ViewUpdates(DocumentView documentView) {
        this.documentView = documentView;
        factoriesListener = new FactoriesListener();
        incomingModificationListener = new IncomingModificationListener();
        Document doc = documentView.getDocument();
        // View hierarchy uses a pair of its own document listeners and DocumentView ignores
        // document change notifications sent from BasicTextUI.RootView.
        // First listener - incomingModificationListener at DocumentListenerPriority.FIRST notifies the hierarchy
        // about incoming document modification.
        // Second listener is "this" at DocumentListenerPriority.VIEW updates the view hierarchy structure
        // according to the document modification.
        // These two listeners avoid situation when a document modification modifies line structure
        // and so the view hierarchy (which uses swing Positions for line view statrts) is inconsistent
        // since e.g. with insert there may be gaps between views and with removal there may be overlapping views
        // but the document listeners that are just being notified include a highlighting layer's document listener
        // BEFORE the BasicTextUI.RootView listener. At that point the highlighting layer would fire a highlighting
        // change and the view hierarchy would attempt to rebuild itself but that would fail.
        DocumentUtilities.addDocumentListener(doc, incomingModificationListener, DocumentListenerPriority.FIRST);
        DocumentUtilities.addDocumentListener(doc, this, DocumentListenerPriority.VIEW);
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
                null, documentView, 0, viewFactories, 0, doc.getLength() + 1, 0);
        try {
            viewBuilder.createViews();
            viewBuilder.repaintAndReplaceViews();
        } finally {
            viewBuilder.finish(); // Includes factory.finish() in each factory
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (!documentView.isActive()) {
                    return;
                }
                // Insert into document was performed -> update or rebuild views
                // First update factories since they may fire rebuilding
                checkFactoriesComponentInited();
                for (int i = 0; i < viewFactories.length; i++) {
                    EditorViewFactory editorViewFactory = viewFactories[i];
                    editorViewFactory.insertUpdate(evt);
                }

                // Check if the factories fired any changes
                int rStartOffset = rebuildStartOffset;
                int rEndOffset = rebuildEndOffset;
                boolean rebuildNecessary = isRebuildNecessary();
                int insertOffset = evt.getOffset();
                int insertLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-INSERT: offset=" + insertOffset + ", length=" + insertLength + '\n'); // NOI18N
                }
                rStartOffset = Math.min(rStartOffset, insertOffset);
                rEndOffset = Math.max(rEndOffset, insertOffset + insertLength);
                // If line elements were modified the views will be modified too
                Document doc = evt.getDocument();
                DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
                if (lineElementChange != null) {
                    Element[] removedLines = lineElementChange.getChildrenRemoved();
                    if (removedLines.length > 0) { // Insertion at line's begining
                        rebuildNecessary = true;
                        int firstRemovedLineStartOffset = removedLines[0].getStartOffset();
                        int lastRemovedLineEndOffset = removedLines[removedLines.length - 1].getEndOffset();
                        assert (insertOffset >= firstRemovedLineStartOffset && insertOffset <= lastRemovedLineEndOffset);
                        if (firstRemovedLineStartOffset < rStartOffset) {
                            rStartOffset = firstRemovedLineStartOffset;
                        }
                        if (lastRemovedLineEndOffset > rEndOffset) {
                            rEndOffset = lastRemovedLineEndOffset;
                        }
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    if (addedLines.length > 0) { // Insertion at line's begining
                        rebuildNecessary = true;
                        int firstAddedLineStartOffset = addedLines[0].getStartOffset();
                        int lastAddedLineEndOffset = addedLines[addedLines.length - 1].getEndOffset();
                        if (firstAddedLineStartOffset < rStartOffset) {
                            rStartOffset = firstAddedLineStartOffset;
                        }
                        if (lastAddedLineEndOffset > rEndOffset) {
                            rEndOffset = lastAddedLineEndOffset;
                        }
                    }
                    rebuildNecessary |= (addedLines.length > 0);
                }
                int paragraphViewIndex = documentView.getViewIndex(rStartOffset);
// XXX: #182559, temporary workaround, please fix properly!
//                assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
                ParagraphView paragraphView;
                if (paragraphViewIndex >= 0) {
                    paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);
                } else {
                    paragraphView = null;
                }

                if (paragraphView != null && !rebuildNecessary) { // Attempt to update just a single view locally
                    // Just inform the view at the offset to contain more data
                    // Use rebuildEndOffset if there was a move inside views
                    int childViewIndex = paragraphView.getViewIndex(rEndOffset);
                    EditorView childView = paragraphView.getEditorView(childViewIndex);
                    if (insertOffset == childView.getStartOffset()) { // View starting right at insertOffset => use previous
                        childViewIndex--;
                        if (childViewIndex < 0) {
                            rebuildNecessary = true;
                        } else {
                            childView = paragraphView.getEditorView(childViewIndex); // re-get childView at new index
                        }
                    }
                    if (!rebuildNecessary) {
                        // Possibly clear text layout for the child view
                        if (childView instanceof TextLayoutView) {
                            documentView.getTextLayoutCache().put(paragraphView, (TextLayoutView)childView, null);
                        }
                        // View may refuse length setting in which case it must be rebuilt
                        rebuildNecessary = !childView.setLength(childView.getLength() + insertLength);
                        // Update offsets of the views that follow the modified one
                        if (!rebuildNecessary) {
                            double visualDelta = childView.getPreferredSpan(paragraphView.getMajorAxis()) -
                                    paragraphView.getViewMajorAxisSpan(childViewIndex);
                            // [TODO] fix line wrap info
                            paragraphView.fixSpans(childViewIndex + 1, insertLength, visualDelta);
                        }
                    }
                }

                if (paragraphView != null && rebuildNecessary) {
                    ViewBuilder viewBuilder = new ViewBuilder(paragraphView, documentView, paragraphViewIndex,
                            viewFactories, rStartOffset, rEndOffset, insertLength);
                    try {
                        viewBuilder.createViews();
                        viewBuilder.repaintAndReplaceViews();
                    } finally {
                        viewBuilder.finish(); // Includes factory.finish() in each factory
                    }
                }

                checkIntegrity();
            } finally {
                incomingModification = false;
                mutex.unlock();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (!documentView.isActive()) {
                    return;
                }
                // Removal in document was performed -> update or rebuild views
                checkFactoriesComponentInited();
                for (int i = 0; i < viewFactories.length; i++) {
                    EditorViewFactory editorViewFactory = viewFactories[i];
                    editorViewFactory.removeUpdate(evt);
                }

                // Check if the factories fired any changes
                int rStartOffset = rebuildStartOffset;
                int rEndOffset = rebuildEndOffset;
                boolean rebuildNecessary = isRebuildNecessary();
                int removeOffset = evt.getOffset();
                int removeLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-REMOVE: offset=" + removeOffset + ", length=" + removeLength + '\n'); // NOI18N
                }
                rStartOffset = Math.min(rStartOffset, removeOffset);
                rEndOffset = Math.max(rEndOffset, removeOffset + removeLength);
                // If line elements were modified the views will be modified too
                Document doc = evt.getDocument();
                DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
                Element[] removedLines = null;
                if (lineElementChange != null) {
                    removedLines = lineElementChange.getChildrenRemoved();
                    if (removedLines.length > 0) { // Insertion at line's begining
                        rebuildNecessary = true;
                        int firstRemovedLineStartOffset = removedLines[0].getStartOffset();
                        int lastRemovedLineEndOffset = removedLines[removedLines.length - 1].getEndOffset();
                        if (firstRemovedLineStartOffset < rStartOffset) {
                            rStartOffset = firstRemovedLineStartOffset;
                        }
                        if (lastRemovedLineEndOffset > rEndOffset) {
                            rEndOffset = lastRemovedLineEndOffset;
                        }
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    if (addedLines.length > 0) { // Insertion at line's begining
                        rebuildNecessary = true;
                        int firstAddedLineStartOffset = addedLines[0].getStartOffset();
                        int lastAddedLineEndOffset = addedLines[addedLines.length - 1].getEndOffset();
                        if (firstAddedLineStartOffset < rStartOffset) {
                            rStartOffset = firstAddedLineStartOffset;
                        }
                        if (lastAddedLineEndOffset > rEndOffset) {
                            rEndOffset = lastAddedLineEndOffset;
                        }
                    }
                    rebuildNecessary |= (addedLines.length > 0);
                }
                // During remove the paragraph views (which are based on positions) may get fused.
                // Thus use getViewIndexFirst() to find for the first one.
                int paragraphViewIndex = documentView.getViewIndexFirst(rStartOffset);
                assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
                ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);

                if (!rebuildNecessary) {
                    // Just inform the view at the offset to contain more data
                    // Use rebuildEndOffset if there was a move inside views
                    int childViewIndex = paragraphView.getViewIndex(rEndOffset);
                    EditorView childView = paragraphView.getEditorView(childViewIndex);
                    int childStartOffset = childView.getStartOffset();
                    int childEndOffset = childView.getEndOffset();
                    boolean localEdit = ((removeOffset == childStartOffset && removeOffset + removeLength < childEndOffset) ||
                            (removeOffset > childStartOffset && removeOffset + removeLength <= childEndOffset));
                    rebuildNecessary = !localEdit;
                    if (!rebuildNecessary) {
                        // Possibly clear text layout for the child view
                        if (childView instanceof TextLayoutView) {
                            documentView.getTextLayoutCache().put(paragraphView, (TextLayoutView)childView, null);
                        }
                        // View may refuse length setting in which case it must be rebuilt
                        rebuildNecessary = !childView.setLength(childView.getLength() - removeLength);
                        // Update offsets of the views that follow the modified one
                        if (!rebuildNecessary) {
                            double visualDelta = childView.getPreferredSpan(paragraphView.getMajorAxis()) -
                                    paragraphView.getViewMajorAxisSpan(childViewIndex);
                            // [TODO] fix line wrap info
                            paragraphView.fixSpans(childViewIndex + 1, -removeLength, visualDelta);
                        }
                    }
                }

                if (rebuildNecessary) {
                    ViewBuilder viewBuilder = new ViewBuilder(paragraphView, documentView, paragraphViewIndex,
                            viewFactories, rStartOffset, rEndOffset, -removeLength);
                    try {
                        viewBuilder.createViews();
                        viewBuilder.repaintAndReplaceViews();
                    } finally {
                        viewBuilder.finish(); // Includes factory.finish() in each factory
                    }
                }
                resetRebuildInfo();
                checkIntegrity();
            } finally {
                incomingModification = false;
                mutex.unlock();
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (!documentView.isActive()) {
                    return;
                }
                checkFactoriesComponentInited();
                for (int i = 0; i < viewFactories.length; i++) {
                    EditorViewFactory editorViewFactory = viewFactories[i];
                    editorViewFactory.changedUpdate(evt);
                }
                // TODO finish
                resetRebuildInfo();
                checkIntegrity();
            } finally {
                incomingModification = false;
                mutex.unlock();
            }
        }
    }

    boolean isRebuildNecessary() {
        return rebuildStartOffset != Integer.MAX_VALUE;
    }

    void resetRebuildInfo() {
        rebuildStartOffset = Integer.MAX_VALUE;
        rebuildEndOffset = Integer.MIN_VALUE;
    }

    void extendRebuildInfo(int startOffset, int endOffset) {
        if (startOffset == Integer.MAX_VALUE) {
            rebuildStartOffset = startOffset;
            rebuildEndOffset = endOffset;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("ViewUpdates.Change set to <" + rebuildStartOffset
                        + "," + rebuildEndOffset + ">\n");
            }
        } else {
            boolean change = false;
            if (startOffset < rebuildStartOffset) {
                rebuildStartOffset = startOffset;
                change = true;
            }
            if (endOffset > rebuildEndOffset) {
                rebuildEndOffset = endOffset;
                change = true;
            }
            if (change && LOG.isLoggable(Level.FINE)) {
                LOG.fine("ViewUpdates.Change extended to <" + rebuildStartOffset
                        + "," + rebuildEndOffset + ">\n");
            }
        }
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
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                if (documentView.isActive() && !incomingModification) {
                    if (isRebuildNecessary()) {
                        int rStartOffset = rebuildStartOffset;
                        int rEndOffset = rebuildEndOffset;
                        int paragraphViewIndex = documentView.getViewIndex(rStartOffset);
                        assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
                        ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);
                        ViewBuilder viewBuilder = new ViewBuilder(paragraphView, documentView, paragraphViewIndex,
                                viewFactories, rStartOffset, rEndOffset, 0);
                        try {
                            viewBuilder.createViews();
                            viewBuilder.repaintAndReplaceViews();
                        } finally {
                            viewBuilder.finish(); // Includes factory.finish() in each factory
                        }
                        resetRebuildInfo();
                    }
                }
            } finally {
                mutex.unlock();
            }
        }
    }


    private final class FactoriesListener implements EditorViewFactoryListener {

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

    private final class IncomingModificationListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            incomingModification = true;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            incomingModification = true;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            incomingModification = true;
        }

    }

}
