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
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.PriorityMutex;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Update paragraph views by document and view factory changes.
 * 
 * @author Miloslav Metelka
 */

public final class ViewUpdates implements DocumentListener, EditorViewFactoryListener {

    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewUpdates.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewUpdates.class.getName());
    
    /**
     * Delay between view factory reports a change and the actual view(s) rebuild.
     */
    private static final int REBUILD_DELAY = 0;
    
//    /**
//     * Maximum number of characters that will be synced directly when
//     * syncViewsRebuild() gets called.
//     */
//    private static final int MAX_DIRECT_SYNC_LENGTH = 2000;
    
    /**
     * How many lines should be initialized at once at minimum.
     */
    private static final int LAZY_CHILDREN_MIN_BATCH_LINES = 5; // How many lines init at once

    private static final RequestProcessor rebuildRegionRP = 
            new RequestProcessor("ViewHierarchy-Region-Rebuilding", 1, false, false); // NOI18N

    /** 
     * Whether views are rebuilt immediately when a view factory fires change.
     * Primarily used in tests to avoid their randomness. */
    private static boolean directViewRebuild;

    private final Object rebuildRegionLock = new String("rebuild-region-lock"); // NOI18N

    private final DocumentView docView;

    private EditorViewFactory[] viewFactories;

    private DocumentListener incomingModificationListener;

    private OffsetRegion rebuildRegion;
    
    /**
     * Whether currently building views. Some highlighting layers may fire changes
     * when asked for highlights but such changes must not influence current views building.
     */
    private boolean buildingViews;
    
    private DocumentEvent incomingEvent;
    
    private final RequestProcessor.Task rebuildRegionTask = rebuildRegionRP.create(new RebuildViews());
    
    private boolean listenerPriorityAwareDoc;
    
    public ViewUpdates(DocumentView docView) {
        this.docView = docView;
        incomingModificationListener = new IncomingModificationListener();
        Document doc = docView.getDocument();
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
        listenerPriorityAwareDoc = DocumentUtilities.addPriorityDocumentListener(doc, 
                WeakListeners.create(DocumentListener.class, incomingModificationListener, null),
                DocumentListenerPriority.FIRST);
        // Add the second listener in all cases.
        DocumentUtilities.addDocumentListener(doc,
                WeakListeners.create(DocumentListener.class, this, doc),
                DocumentListenerPriority.VIEW);

        // Init view factories
        assert (viewFactories == null);
        JTextComponent component = docView.getTextComponent();
        assert (component != null) : "Null component; doc=" + docView.getDocument(); // NOI18N
        List<EditorViewFactory.Factory> factoryFactories = EditorViewFactory.factories();
        viewFactories = new EditorViewFactory[factoryFactories.size()];
        for (int i = 0; i < factoryFactories.size(); i++) {
            viewFactories[i] = factoryFactories.get(i).createEditorViewFactory(component);
            viewFactories[i].addEditorViewFactoryListener(WeakListeners.create(
                    EditorViewFactoryListener.class, this, viewFactories[i]));
        }
    }
    
    /**
     * Start view building process (it must be followed by finishBuildViews() in try-finally).
     */
    private ViewBuilder startBuildViews() {
//        assert (DocumentUtilities.isReadLocked(documentView.getDocument())) :
//                "Document NOT READ-LOCKED: " + documentView.getDocument(); // NOI18N
        assert !isBuildingViews() : "Already building views"; // NOI18N
        ViewBuilder viewBuilder = new ViewBuilder(docView, viewFactories);
        docView.checkMutexAcquiredIfLogging();
        setBuildingViews(true);
        return viewBuilder;
    }
    
    private void finishBuildViews(ViewBuilder viewBuilder) {
        setBuildingViews(false);
        viewBuilder.finish(); // Includes factory.finish() in each factory
        docView.checkIntegrityIfLoggable();
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finer("ViewUpdates.buildViews(): UPDATED-DOC-VIEW:\n" + docView); // NOI18N
        }
    }

    private boolean isBuildingViews() {
        return buildingViews;
    }

    private void setBuildingViews(boolean buildingViews) {
        assert (buildingViews != this.buildingViews) : "buildingViews=" + buildingViews + // NOI18N
                " == this.buildingViews=" + this.buildingViews; // NOI18N
        this.buildingViews = buildingViews;
    }

    void reinitAllViews() {
        // Build views lazily; boundaries may differ from start/end of doc e.g. for fold preview
        ViewBuilder viewBuilder = startBuildViews();
        try {
            // Possibly clear rebuild region - all the views will be re-inited anyway
            fetchRebuildRegion();
            viewBuilder.initFullRebuild();
            viewBuilder.createReplaceAndRepaintViews();
        } finally {
            finishBuildViews(viewBuilder);
        }
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
        int viewCount = docView.getViewCount();
        if (viewCount == 0) {
            return;
        }
        startIndex = Math.max(startIndex, 0);
        endIndex = Math.min(endIndex, viewCount);

        assert (startIndex >= 0) : "startIndex=" + startIndex; // NOI18N
        assert (endIndex >= startIndex) : "endIndex=" + endIndex + " < startIndex=" + startIndex; // NOI18N
        
        // Possibly shrink the area if some part already built
        ParagraphView startChild = docView.getEditorView(startIndex);
        while (startChild.children != null && startIndex < endIndex - 1) {
            startChild = docView.getEditorView(++startIndex);
        }
        ParagraphView lastChild = null;
        while (endIndex > startIndex && (lastChild = docView.getEditorView(endIndex - 1)).children != null) {
            endIndex--;
        }
        // lastChild is paragraphView at (endIndex - 1)

        if (endIndex > startIndex) { // lastChild should be inited in this case
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Lazy-children init: [" + startIndex + "," + endIndex + "]\n"); // NOI18N
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.INFO, "Lazy creation cause", new Exception()); // NOI18N
                }
            }
            ViewBuilder viewBuilder = startBuildViews();
            try {
                viewBuilder.initParagraphs(startIndex, endIndex,
                        startChild.getStartOffset(), lastChild.getEndOffset());
                viewBuilder.createReplaceAndRepaintViews();
            } finally {
                finishBuildViews(viewBuilder);
            }
        }
    }

    @Override
    public void insertUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        PriorityMutex mutex = docView.getMutex();
        if (mutex != null) {
            mutex.lock();
            docView.checkDocumentLockedIfLogging();
            try { // No return prior this "try" to properly unset incomingModification
                if (!docView.isUpdatable() || docView.getViewCount() == 0) {
                    // For viewCount zero - it would later fail on paragraphViewIndex == -1
                    // Even for empty doc there should be a single paragraph view for extra ending '\n'
                    // so this should only happen when no views were created yet.
                    return;
                }
                Document doc = docView.getDocument();
                assert (doc == evt.getDocument()) : "Invalid document";
                int insertOffset = evt.getOffset();
                int insertLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-INSERT-evt: offset=" + insertOffset + ", length=" + insertLength + // NOI18N
                            ", current-docViewEndOffset=" + (evt.getDocument().getLength()+1) + '\n'); // NOI18N
                }

                ViewBuilder viewBuilder = startBuildViews();
                try {
                    // Possibly clear rebuild region - all the views will be re-inited anyway
                    if (viewBuilder.initModUpdate(insertOffset, insertLength, fetchRebuildRegion())) {
                        viewBuilder.createReplaceAndRepaintViews();
                    }
                } finally {
                    finishBuildViews(viewBuilder);
                }
                
            } finally {
                docView.setIncomingModification(false);
                mutex.unlock();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        PriorityMutex mutex = docView.getMutex();
        if (mutex != null) {
            mutex.lock();
            docView.checkDocumentLockedIfLogging();
            try { // No return prior this "try" to properly unset incomingModification
                if (!docView.isUpdatable() || docView.getViewCount() == 0) {
                    // For viewCount zero - it would later fail on paragraphViewIndex == -1
                    // Even for empty doc there should be a single paragraph view for extra ending '\n'
                    // so this should only happen when no views were created yet.
                    return;
                }
                Document doc = docView.getDocument();
                assert (doc == evt.getDocument()) : "Invalid document";
                int removeOffset = evt.getOffset();
                int removeLength = evt.getLength();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("\nDOCUMENT-REMOVE-evt: offset=" + removeOffset + ", length=" + removeLength + // NOI18N
                            ", current-docViewEndOffset=" + (evt.getDocument().getLength()+1) + '\n'); // NOI18N
                }
                
                ViewBuilder viewBuilder = startBuildViews();
                try {
                    // Possibly clear rebuild region - all the views will be re-inited anyway
                    if (viewBuilder.initModUpdate(removeOffset, -removeLength, fetchRebuildRegion())) {
                        viewBuilder.createReplaceAndRepaintViews();
                    }
                } finally {
                    finishBuildViews(viewBuilder);
                }
            } finally {
                docView.setIncomingModification(false);
                mutex.unlock();
            }
        }
    }

    @Override
    public void changedUpdate(DocumentEvent evt) {
        clearIncomingEvent(evt);
        PriorityMutex mutex = docView.getMutex();
        if (mutex != null) {
            mutex.lock();
            docView.checkDocumentLockedIfLogging();
            try {
                if (!docView.isUpdatable()) {
                    return;
                }
                // TODO finish
                docView.checkIntegrityIfLoggable();
            } finally {
                mutex.unlock();
            }
        }
    }

    @Override
    public void viewFactoryChanged(EditorViewFactoryEvent evt) {
        boolean postRebuildTask;
        boolean directRebuild = directViewRebuild;
        synchronized (rebuildRegionLock) {
            docView.checkDocumentLockedIfLogging();
            // Post the task only if the region is null (if it would be non-null
            // a pending task would be started for it previously)
            postRebuildTask = (rebuildRegion == null);
            directRebuild |= (evt.getPriority() > 0);
            if (isBuildingViews()) {
                // It may happen that somee highlighting layer when asked for highlights (during views rebuilding)
                // reports a highlights change so in such case the views rebuild must be delayed anyway.
                directRebuild = false;
            }
            List<EditorViewFactory.Change> changes = evt.getChanges();
            for (EditorViewFactory.Change change : changes) {
                int startOffset = change.getStartOffset();
                int endOffset = change.getEndOffset();
                // Do not ignore empty <startOffset,endOffset> regions - should we??
                Document doc = docView.getDocument();
                int docTextLen = doc.getLength() + 1;
                startOffset = Math.min(startOffset, docTextLen);
                endOffset = Math.min(endOffset, docTextLen);
                rebuildRegion = OffsetRegion.union(rebuildRegion, doc, startOffset, endOffset, false);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("ViewUpdates.viewFactoryChanged: <" + startOffset + "," + endOffset + ">\n"); // NOI18N
                }
            }
        }

        if (directRebuild) {
            new RebuildViews().run();
        } else {
            if (postRebuildTask) {
                rebuildRegionTask.schedule(REBUILD_DELAY);
            } // Otherwise the task is scheduled already
        }
    }

    void syncedViewsRebuild() {
        if (docView.isActive()) {
            OffsetRegion region = fetchRebuildRegion();
            if (region != null) {
                docView.checkDocumentLockedIfLogging();
                // Do nothing if docView is not active. Once becomes active a full rebuild will be done.
                ViewBuilder viewBuilder = startBuildViews();
                try {
                    if (viewBuilder.initRebuild(region)) {
                        viewBuilder.createReplaceAndRepaintViews();
                    }
                } finally {
                    finishBuildViews(viewBuilder);
                }
            }
        }
    }

    static void setDirectViewRebuild(boolean directRebuild) { // Used to increase determinancy in random tests
        directViewRebuild = directRebuild;
    }

    private OffsetRegion fetchRebuildRegion() {
        synchronized (rebuildRegionLock) {
            OffsetRegion region = rebuildRegion;
            rebuildRegion = null;
            return region;
        }
    }
    
    /*private*/ void incomingEvent(DocumentEvent evt) {
        if (incomingEvent != null) {
            // Rebuild the view hierarchy: temporary solution until the real cause is found.
            docView.releaseChildren();
            LOG.log(Level.INFO, "View hierarchy rebuild due to pending document event", // NOI18N
                    new Exception("Pending incoming event: " + incomingEvent)); // NOI18N
        }
        incomingEvent = evt;
    }
    
    private void clearIncomingEvent(DocumentEvent evt) {
        if (listenerPriorityAwareDoc) {
            if (incomingEvent == null) {
                throw new IllegalStateException("Incoming event already cleared"); // NOI18N
            }
            if (incomingEvent != evt) {
                throw new IllegalStateException("Invalid incomingEvent=" + incomingEvent + " != evt=" + evt); // NOI18N
            }
            incomingEvent = null;
        }
    }

    private final class IncomingModificationListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            incomingEvent(e);
            docView.setIncomingModification(true);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            incomingEvent(e);
            docView.setIncomingModification(true);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            incomingEvent(e);
        }

    }

    private final class RebuildViews implements Runnable {
        
        public @Override void run() {
            docView.syncViewsRebuild();
        }

    }
}
