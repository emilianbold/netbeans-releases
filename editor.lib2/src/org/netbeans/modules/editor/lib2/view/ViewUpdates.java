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
import org.openide.util.WeakListeners;

/**
 * Update paragraph views by document and view factory changes.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUpdates implements DocumentListener {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUpdates.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUpdates.class.getName());
    
    /**
     * Maximum number of characters where the view rebuilds still produce local intra-paragraph views.
     */
    private static final int MAX_LOCAL_VIEWS_REBUILD_LENGTH = 200;
    
    /**
     * How many lines should be inited at once at minimum.
     */
    private static final int LAZY_CHILDREN_MIN_BATCH_LINES = 5; // How many lines init at once

    private final DocumentView documentView;

    private EditorViewFactory[] viewFactories;

    private FactoriesListener factoriesListener;

    private DocumentListener incomingModificationListener;

    OffsetRegion rebuildRegion = OffsetRegion.empty();

    private boolean buildingViews;

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
        DocumentUtilities.addDocumentListener(doc, 
                WeakListeners.create(DocumentListener.class, incomingModificationListener, null),
                DocumentListenerPriority.FIRST);
        DocumentUtilities.addDocumentListener(doc,
                WeakListeners.create(DocumentListener.class, this, doc),
                DocumentListenerPriority.VIEW);
    }

    private void initFactories() {
        assert (viewFactories == null);
        JTextComponent component = documentView.getTextComponent();
        assert (component != null) : "Null component; doc=" + documentView.getDocument(); // NOI18N
        List<EditorViewFactory.Factory> factoryFactories = EditorViewFactory.factories();
        viewFactories = new EditorViewFactory[factoryFactories.size()];
        for (int i = 0; i < factoryFactories.size(); i++) {
            viewFactories[i] = factoryFactories.get(i).createEditorViewFactory(component);
            viewFactories[i].addEditorViewFactoryListener(WeakListeners.create(
                    EditorViewFactoryListener.class, factoriesListener, viewFactories[i]));
        }
    }
    
    private void buildViews(ParagraphView paragraphView, int paragraphViewIndex,
        int startOffset, int endOffset,
        int modOffset, int offsetDelta, boolean createLocalViews)
    {
        assert (DocumentUtilities.isReadLocked(documentView.getDocument())) :
                "Document NOT READ-LOCKED: " + documentView.getDocument(); // NOI18N
        assert documentView.isMutexAcquired() : "View hierarchy mutex not acquired";
        assert !buildingViews : "Already building views"; // NOI18N
        ViewBuilder viewBuilder = new ViewBuilder(paragraphView, documentView,
                paragraphViewIndex, viewFactories, startOffset, endOffset,
                modOffset, offsetDelta, createLocalViews
        );
        buildingViews = true;
        try {
            viewBuilder.createViews();
            viewBuilder.replaceAndRepaintViews();
        } finally {
            buildingViews = false;
            viewBuilder.finish(); // Includes factory.finish() in each factory
        }
        // Fire change of views
        documentView.viewHierarchy.fireViewHierarchyEvent(new ViewHierarchyEvent(documentView.viewHierarchy(),
                startOffset));
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finer("ViewUpdates.buildViews(): UPDATED-DOC-VIEW:\n" + documentView); // NOI18N
        }
    }

    void reinitViews() {
        // Insert into document was performed -> update or rebuild views
        // First update factories since they may fire rebuilding
        checkFactoriesComponentInited();
        // Build views lazily; boundaries may differ from start/end of doc e.g. for fold preview
        int startOffset = documentView.getStartOffset();
        int endOffset = documentView.getEndOffset();
        buildViews(null, 0, startOffset, endOffset, endOffset, 0, documentView.isAccurateSpan());
    }

    /**
     * Init children of views in given range.
     *
     * @param startIndex lower bound (can possibly be < 0)
     * @param endIndex upper bound (can possibly be >= viewCount).
     */
    void initChildren(int startIndex, int endIndex) {
        if (endIndex - startIndex < LAZY_CHILDREN_MIN_BATCH_LINES) {
            // Build views around too
            startIndex -= (LAZY_CHILDREN_MIN_BATCH_LINES >> 1);
            endIndex += (LAZY_CHILDREN_MIN_BATCH_LINES >> 1);
        }
        startIndex = Math.max(startIndex, 0);
        endIndex = Math.min(endIndex, documentView.getViewCount());

        assert (startIndex >= 0) : "startIndex=" + startIndex; // NOI18N
        assert (endIndex >= startIndex) : "endIndex=" + endIndex + " < startIndex=" + startIndex; // NOI18N
        
        // Possibly shrink the area if some part already built
        ParagraphView startChild = documentView.getEditorView(startIndex);
        while (startChild.children != null && startIndex < endIndex - 1) {
            startChild = documentView.getEditorView(++startIndex);
        }
        ParagraphView lastChild = null;
        while (endIndex > startIndex && (lastChild = documentView.getEditorView(endIndex - 1)).children != null) {
            endIndex--;
        }

        if (endIndex > startIndex) { // lastChild should be inited in this case
            int docTextLength = documentView.getDocument().getLength() + 1;
            int startOffset = startChild.getStartOffset();
            int endOffset = lastChild.getEndOffset();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Lazy-children init: [" + startIndex + "," + endIndex + "]\n"); // NOI18N
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.INFO, "Lazy creation cause", new Exception()); // NOI18N
                }
            }
            assert (startOffset <= endOffset) : "startOffset=" + startOffset + // NOI18N
                    " > endOffset=" + endOffset + "\n" + documentView.toStringDetail(); // NOI18N
            assert (endOffset <= docTextLength) : "endOffset=" + endOffset + // NOI18N
                    " > docTextLength=" + docTextLength + "\n" + documentView.toStringDetail(); // NOI18N
            // Build views with forced creation of local views
            buildViews(null, startIndex, startOffset, endOffset, endOffset, 0, true);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            documentView.checkDocumentLocked();
            try {
                if (!documentView.isUpdatable()) {
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
                OffsetRegion rRegion = rebuildRegion;
                resetRebuildInfo();
                if (documentView.getViewCount() == 0) {
                    return; // It would later fail on paragraphViewIndex == -1
                }

                int insertOffset = evt.getOffset();
                int insertLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-INSERT-evt: offset=" + insertOffset + ", length=" + insertLength + // NOI18N
                            ", docLen=" + evt.getDocument().getLength() + '\n'); // NOI18N
                }
                rRegion = rRegion.union(insertOffset, insertOffset + insertLength);

                // If line elements were modified the views will be modified too
                Document doc = evt.getDocument();
                DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
                if (lineElementChange != null) {
                    Element[] removedLines = lineElementChange.getChildrenRemoved();
                    if (removedLines.length > 0) { // Insertion at line's begining
                        int firstRemovedLineStartOffset = removedLines[0].getStartOffset();
                        int lastRemovedLineEndOffset = removedLines[removedLines.length - 1].getEndOffset();
                        assert (insertOffset >= firstRemovedLineStartOffset && insertOffset <= lastRemovedLineEndOffset);
                        rRegion = rRegion.union(firstRemovedLineStartOffset, lastRemovedLineEndOffset);
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    if (addedLines.length > 0) { // Insertion at line's begining
                        int firstAddedLineStartOffset = addedLines[0].getStartOffset();
                        int lastAddedLineEndOffset = addedLines[addedLines.length - 1].getEndOffset();
                        rRegion = rRegion.union(firstAddedLineStartOffset, lastAddedLineEndOffset);
                    }
                }
                int docViewStartOffset = documentView.getStartOffset();
                int docViewEndOffset = documentView.getEndOffset();
                rRegion.intersection(docViewStartOffset, docViewEndOffset);
                if (rRegion.isEmpty()) {
                    // Outside of area covered by document view
                    return;
                }
                int paragraphViewIndex;
                ParagraphView paragraphView;
                int paragraphViewLength;
                if (insertOffset == 0) {
                    // Insert may be in fact an undo of a previous removal at insertOffset=0
                    // in which case the regular code with paragraphView!=null
                    // would fail since the paragraphView.getStartOffset() would be
                    // 0+insertLength and the ViewBuilder would retain the paragraphView.
                    // So with paragraphView==null do a full rebuild of the first paragraph view.
                    paragraphViewIndex = 0;
                    paragraphView = null;
                    paragraphViewLength = 0;
                } else {
                    paragraphViewIndex = documentView.getViewIndex(rRegion.startOffset());
                    assert (paragraphViewIndex >= 0) : "paragraphViewIndex=" + paragraphViewIndex + // NOI18N
                            ", docLen=" + evt.getDocument().getLength(); // NOI18N
                    paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);
                    paragraphViewLength = paragraphView.getLength();
                }

                // Decide whether create local views - reflect paragraphView length since
                // a local rebuild inside even a long paragraphView should create local views.
                boolean createLocalViews = (rRegion.length() <=
                        MAX_LOCAL_VIEWS_REBUILD_LENGTH + paragraphViewLength);
                if (paragraphView != null) {
                    if (paragraphView.children == null) {
                        int paragraphStartOffset = paragraphView.getStartOffset();
                        assert (paragraphStartOffset <= rRegion.startOffset()) :
                                "paragraphStartOffset=" + paragraphStartOffset + " > rStartOffset=" + rRegion.startOffset(); // NOI18N
                        rRegion = rRegion.union(paragraphStartOffset, paragraphStartOffset + paragraphView.getLength());
                        paragraphView = null;
                        // When the original area had null children then it should be fine
                        // to rebuild without local views creation
                        createLocalViews = false;
                    }
                }
                createLocalViews |= documentView.isAccurateSpan();

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ViewUpdates.insertUpdate-buildViews(): " + rRegion + // NOI18N
                            " createLocalViews=" + createLocalViews + "\n"); // NOI18N
                }
                buildViews(paragraphView, paragraphViewIndex,
                        rRegion.startOffset(), rRegion.endOffset(),
                        insertOffset, insertLength, createLocalViews);
            } finally {
                documentView.setIncomingModification(false);
                mutex.unlock();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            documentView.checkDocumentLocked();
            try {
                if (!documentView.isUpdatable()) {
                    return;
                }
                // Removal in document was performed -> update or rebuild views
                checkFactoriesComponentInited();
                for (int i = 0; i < viewFactories.length; i++) {
                    EditorViewFactory editorViewFactory = viewFactories[i];
                    editorViewFactory.removeUpdate(evt);
                }

                // Check if the factories fired any changes
                OffsetRegion rRegion = rebuildRegion;
                resetRebuildInfo();
                if (documentView.getViewCount() == 0) {
                    return; // It would later fail on paragraphViewIndex == -1
                }

                int removeOffset = evt.getOffset();
                int removeLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-REMOVE-evt: offset=" + removeOffset + ", length=" + removeLength + // NOI18N
                            ", docLen=" + evt.getDocument().getLength() + '\n'); // NOI18N
                }
                rRegion = rRegion.union(removeOffset, removeOffset + removeLength);
                int docViewStartOffset = documentView.getStartOffset();
                int docViewOrigEndOffset = documentView.getEndOffset();
                if (docViewOrigEndOffset >= removeOffset) {
                    docViewOrigEndOffset += removeLength;
                }
                rRegion.intersection(docViewStartOffset, docViewOrigEndOffset);
                if (rRegion.isEmpty()) {
                    // Outside of area covered by document view
                    return;
                }


                // If line elements were modified the views will be modified too
                Document doc = evt.getDocument();
                DocumentEvent.ElementChange lineElementChange = evt.getChange(doc.getDefaultRootElement());
                Element[] removedLines = null;
                if (lineElementChange != null) {
                    removedLines = lineElementChange.getChildrenRemoved();
                    if (removedLines.length > 0) { // Insertion at line's begining
                        int firstRemovedLineStartOffset = removedLines[0].getStartOffset();
                        int lastRemovedLineEndOffset = removedLines[removedLines.length - 1].getEndOffset();
                        rRegion = rRegion.union(firstRemovedLineStartOffset, lastRemovedLineEndOffset);
                    }
                    Element[] addedLines = lineElementChange.getChildrenAdded();
                    if (addedLines.length > 0) { // Insertion at line's begining
                        int firstAddedLineStartOffset = addedLines[0].getStartOffset();
                        int lastAddedLineEndOffset = addedLines[addedLines.length - 1].getEndOffset();
                        rRegion = rRegion.union(firstAddedLineStartOffset, lastAddedLineEndOffset);
                    }
                }
                // During remove the paragraph views (which are based on positions) may get fused.
                // Thus use getViewIndexFirst() to find for the first one.
                int paragraphViewIndex = documentView.getViewIndexFirst(rRegion.startOffset());
                assert (paragraphViewIndex >= 0) : "Line view index is " + paragraphViewIndex; // NOI18N
                ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);

                // Decide whether create local views - reflect paragraphView length since
                // a local rebuild inside even a long paragraphView should create local views.
                boolean createLocalViews = (rRegion.length() <=
                        MAX_LOCAL_VIEWS_REBUILD_LENGTH + paragraphView.getLength());
                if (paragraphView.children == null) { // Cannot do local rebuild in such case
                    int paragraphStartOffset = paragraphView.getStartOffset();
                    assert (paragraphStartOffset <= rRegion.startOffset()) :
                            "paragraphStartOffset=" + paragraphStartOffset + " > rStartOffset=" + rRegion.startOffset(); // NOI18N
                    rRegion = rRegion.union(paragraphStartOffset, paragraphStartOffset + paragraphView.getLength());
                    paragraphView = null;
                    // When the original area had null children then it should be fine
                    // to rebuild without local views creation
                    createLocalViews = false;
                }
                createLocalViews |= documentView.isAccurateSpan();

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ViewUpdates.removeUpdate-buildViews(): " + rRegion + // NOI18N
                            " createLocalViews=" + createLocalViews + "\n"); // NOI18N
                }
                buildViews(paragraphView, paragraphViewIndex,
                        rRegion.startOffset(), rRegion.endOffset(), 
                        removeOffset, -removeLength, createLocalViews);
            } finally {
                documentView.setIncomingModification(false);
                mutex.unlock();
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            documentView.checkDocumentLocked();
            try {
                if (!documentView.isUpdatable()) {
                    return;
                }
                checkFactoriesComponentInited();
                for (int i = 0; i < viewFactories.length; i++) {
                    EditorViewFactory editorViewFactory = viewFactories[i];
                    editorViewFactory.changedUpdate(evt);
                }
                // TODO finish
                resetRebuildInfo();
                documentView.checkIntegrity();
            } finally {
                documentView.setIncomingModification(false);
                mutex.unlock();
            }
        }
    }

    boolean isRebuildNecessary() {
        return !rebuildRegion.isEmpty();
    }

    void resetRebuildInfo() {
        rebuildRegion = OffsetRegion.empty();
    }

    void extendRebuildInfo(int startOffset, int endOffset) {
        OffsetRegion oldRegion = rebuildRegion;
        rebuildRegion = rebuildRegion.union(startOffset, endOffset);
        if (rebuildRegion != oldRegion) {
            LOG.fine("ViewUpdates.Change extended to " + rebuildRegion + "\n");
        }
    }

    private void checkFactoriesComponentInited() {
        if (viewFactories == null) {
            initFactories();
        }
    }

    /*private*/ void checkRebuild(OffsetRegion region) {
        PriorityMutex mutex = documentView.getMutex();
        if (mutex != null) {
            mutex.lock();
            try {
                documentView.checkDocumentLocked();
                // Check buildingViews flag since it's possible that asking for a highlight
                // triggered firing of a highlight change resulting in checkRebuild() call
                rebuildRegion = rebuildRegion.union(region);
                if (!buildingViews && documentView.isActive()) {
                    if (isRebuildNecessary()) {
                        OffsetRegion rRegion = rebuildRegion;
                        resetRebuildInfo();
                        int docViewStartOffset = documentView.getStartOffset();
                        int docViewEndOffset = documentView.getEndOffset();
                        rRegion = rRegion.intersection(docViewStartOffset, docViewEndOffset);
                        if (rRegion.isEmpty()) {
                            // Outside of area covered by document view
                            return;
                        }
                        documentView.checkIntegrity();
                        int paragraphViewIndex = documentView.getViewIndexFirst(rRegion.startOffset());
                        assert (paragraphViewIndex >= 0) : "Paragraph view index is " + paragraphViewIndex + // NOI18N
                                " for " + rRegion; // NOI18N
                        ParagraphView paragraphView = (ParagraphView) documentView.getEditorView(paragraphViewIndex);
                        // Decide whether create local views - reflect paragraphView length since
                        // a local rebuild inside even a long paragraphView should create local views.
                        boolean createLocalViews = (rRegion.length()
                                <= MAX_LOCAL_VIEWS_REBUILD_LENGTH + paragraphView.getLength());
                        if (paragraphView.children == null) { // Rebuild must include whole paragraphView
                            int paragraphStartOffset = paragraphView.getStartOffset();
                            assert (paragraphStartOffset <= rRegion.startOffset()) :
                                "paragraphStartOffset=" + paragraphStartOffset + " > rRegion.startOffset=" + rRegion.startOffset(); // NOI18N
                            rRegion = rRegion.union(paragraphStartOffset, paragraphStartOffset + paragraphView.getLength());
                            paragraphView = null;
                            // When the original area had null children then it should be fine
                            // to rebuild without local views creation
                            createLocalViews = false;
                        }
                        createLocalViews |= documentView.isAccurateSpan();

                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("ViewUpdates.checkRebuild-buildViews(): " + rRegion + // NOI18N
                                    " createLocalViews=" + createLocalViews + "\n"); // NOI18N
                        }
                        buildViews(paragraphView, paragraphViewIndex,
                                rRegion.startOffset(), rRegion.endOffset(),
                                rRegion.endOffset(), 0, createLocalViews);
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
            OffsetRegion region = OffsetRegion.empty();
            List<EditorViewFactory.Change> changes = evt.getChanges();
            for (EditorViewFactory.Change change : changes) {
                region = region.union(change.getStartOffset(), change.getEndOffset());
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ViewUpdates.viewFactoryChanged: <" + change.getStartOffset() +
                            "," + change.getEndOffset() + ">\n");
                }
            }
            if (!region.isEmpty()) {
                checkRebuild(region);
            }
        }

    }

    private final class IncomingModificationListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            documentView.setIncomingModification(true);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentView.setIncomingModification(true);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            documentView.setIncomingModification(true);
        }

    }

}
