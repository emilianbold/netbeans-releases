/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingActivityInterceptor;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author Tomas Zezula
 */
public final class RepositoryUpdater implements PathRegistryListener, FileChangeListener, PropertyChangeListener, DocumentListener {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public static synchronized RepositoryUpdater getDefault() {
        if (instance == null) {
            instance = new RepositoryUpdater();
        }
        return instance;
    }

    public synchronized void start(boolean force) {
        if (state == State.CREATED) {
            state = State.STARTED;
            LOGGER.fine("Initializing..."); //NOI18N
            this.indexingActivityInterceptors = Lookup.getDefault().lookupResult(IndexingActivityInterceptor.class);
            PathRegistry.getDefault().addPathRegistryListener(this);
            FileUtil.addFileChangeListener(this);
            EditorRegistry.addPropertyChangeListener(this);

            if (force) {
                scheduleWork(null, false);
            }
        }
    }

    public synchronized void stop() {
        if (state != State.STOPPED) {
            state = State.STOPPED;
            LOGGER.fine("Closing..."); //NOI18N

            PathRegistry.getDefault().removePathRegistryListener(this);
            FileUtil.removeFileChangeListener(this);
            EditorRegistry.removePropertyChangeListener(this);

            getWorker().cancelAll();
        }
    }

    public boolean isScanInProgress() {
        return getWorker().isWorking();
    }

    public boolean waitUntilFinished(long timeout) throws InterruptedException {
        return getWorker().waitUntilFinished(timeout);
    }

    /**
     * Schedules new job for indexing files under a root. This method forcible
     * reindexes all files in the job without checking timestamps.
     *
     * @param rootUrl The root that should be reindexed.
     * @param fileUrls Files under the root. Files that are not under the <code>rootUrl</code>
     *   are ignored. Can be <code>null</code> in which case all files under the root
     *   will be reindexed.
     * @param followUpJob If <code>true</code> the indexers will be notified that
     *   they are indexing follow up files (ie. files that one of the indexers involved
     *   in earlier indexing job requested to reindex) in contrast to files that are
     *   being reindexed due to ordinary change events (eg. when classpath roots are
     *   added/removed, file is modified, editor tabs are switched, etc).
     */
    public void addIndexingJob(URL rootUrl, Collection<? extends URL> fileUrls, boolean followUpJob, boolean checkEditor, boolean wait) {
        assert rootUrl != null;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("addIndexingJob: rootUrl=" + rootUrl + ", fileUrls=" + fileUrls //NOI18N
                + ", followUpJob=" + followUpJob + ", checkEditor=" + checkEditor + ", wait=" + wait); //NOI18N
        }

        FileObject root = URLMapper.findFileObject(rootUrl);
        assert root != null : rootUrl + " can't be translated to FileObject"; //NOI18N
        if (root == null) {
            return;
        }

        FileListWork flw = null;
        if (fileUrls != null && fileUrls.size() > 0) {
            Set<FileObject> files = new HashSet<FileObject>();
            for(URL fileUrl : fileUrls) {
                FileObject file = URLMapper.findFileObject(fileUrl);
                if (file != null) {
                    if (FileUtil.isParentOf(root, file)) {
                        files.add(file);
                    } else {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.warning(file + " does not lie under " + root + ", not indexing it"); //NOI18N
                        }
                    }
                }
            }

            if (files.size() > 0) {
                flw = new FileListWork(rootUrl, files, followUpJob, checkEditor);
            }
        } else {
            flw = new FileListWork(rootUrl, followUpJob, checkEditor);
        }

        if (flw != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Scheduling index refreshing: root=" + rootUrl + ", files=" + fileUrls); //NOI18N
            }

            scheduleWork(flw, wait);
        }
    }

    /**
     * Schedules new job for refreshing all indexes created by the given indexer.
     *
     * @param indexerName The name of the indexer, which indexes should be refreshed.
     */
    public void addIndexingJob(String indexerName) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("addIndexingJob: indexerName=" + indexerName); //NOI18N
        }

        CustomIndexerFactory factory = null;
        Set<String> indexerMimeTypes = new HashSet<String>();
        
        for(String mimeType : Util.getAllMimeTypes()) {
            Collection<? extends CustomIndexerFactory> mimeTypeFactories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
            for(CustomIndexerFactory f : mimeTypeFactories) {
                if (f.getIndexerName().equals(indexerName)) {
                    if (factory != null && factory.getClass() != f.getClass()) {
                        LOGGER.warning("Different CustomIndexerFactory implementations using the same name: " //NOI18N
                            + factory.getClass().getName() + ", " + f.getClass().getName()); //NOI18N
                    } else {
                        factory = f;
                        indexerMimeTypes.add(mimeType);
                    }
                }
            }
        }

        if (factory == null) {
            throw new InvalidParameterException("No CustomIndexerFactory with name: '" + indexerName + "'"); //NOI18N
        } else {
            Work w = new RefreshIndices(indexerMimeTypes, factory, scannedRoots2Dependencies);
            scheduleWork(w, false);
        }
    }

    public void refreshAll() {
        scheduleWork(new RootsWork(scannedRoots2Dependencies, scannedBinaries), false);
    }

    public synchronized IndexingController getController() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    // -----------------------------------------------------------------------
    // PathRegistryListener implementation
    // -----------------------------------------------------------------------

    public void pathsChanged(PathRegistryEvent event) {
        assert event != null;
        if (LOGGER.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Paths changed:\n"); //NOI18N
            for(PathRegistryEvent.Change c : event.getChanges()) {
                sb.append(" event=").append(c.getEventKind()); //NOI18N
                sb.append(" pathKind=").append(c.getPathKind()); //NOI18N
                sb.append(" pathType=").append(c.getPathType()); //NOI18N
                sb.append(" affected paths:\n"); //NOI18N
                Collection<? extends ClassPath> paths = c.getAffectedPaths();
                if (paths != null) {
                    for(ClassPath cp : paths) {
                        sb.append("  \""); //NOI18N
                        sb.append(cp.toString(ClassPath.PathConversionMode.PRINT));
                        sb.append("\"\n"); //NOI18N
                    }
                }
                sb.append("--\n"); //NOI18N
            }
            sb.append("====\n"); //NOI18N
            LOGGER.fine(sb.toString());
        }
        scheduleWork(new RootsWork(scannedRoots2Dependencies, scannedBinaries), false);
    }

    // -----------------------------------------------------------------------
    // FileChangeListener implementation
    // -----------------------------------------------------------------------

    public void fileFolderCreated(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }
        
        //In ideal case this should do nothing,
        //but in Netbeans newlly created folder may
        //already contain files
        boolean processed = false;
        FileObject fo = fe.getFile();
        URL root = null;
        
        if (fo != null && fo.isValid() && VisibilityQuery.getDefault().isVisible(fo)) {
            root = getOwningSourceRoot(fo);

            if (root != null) {
                scheduleWork(new FileListWork(root, Collections.singleton(fo), false, false), false);
                processed = true;
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Folder created (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileDataCreated(FileEvent fe) {
        fileChanged(fe);
    }

    public void fileChanged(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }

        boolean processed = false;
        FileObject fo = fe.getFile();
        URL root = null;

        if (fo != null && fo.isValid() && VisibilityQuery.getDefault().isVisible(fo)) {
            root = getOwningSourceRoot (fo);
            if (root != null) {
                scheduleWork(new FileListWork(root, Collections.singleton(fo), false, false), false);
                processed = true;
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File modified (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileDeleted(FileEvent fe) {
        if (!authorize(fe)) {
            return;
        }

        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);
        boolean processed = false;

        if (root != null &&  VisibilityQuery.getDefault().isVisible(fo) && fo.isData()
            /*&& FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null*/) {
            String relativePath = FileUtil.getRelativePath(URLMapper.findFileObject(root), fo);
            assert relativePath != null : "FileObject not under root: f=" + fo + ", root=" + root; //NOI18N
            scheduleWork(new DeleteWork(root, relativePath), false);
            processed = true;
        }
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File deleted (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        if (!authorize(fe)) {
            return;
        }
        
        final FileObject newFile = fe.getFile();
        final String oldNameExt = fe.getExt().length() == 0 ? fe.getName() : fe.getName() + "." + fe.getExt(); //NOI18N
        final URL root = getOwningSourceRoot(newFile);
        boolean processed = false;

        if (root != null) {
            FileObject rootFo = URLMapper.findFileObject(root);
            String oldFilePath = FileUtil.getRelativePath(rootFo, newFile.getParent()) + "/" + oldNameExt; //NOI18N
            scheduleWork(new DeleteWork(root, oldFilePath.toString()), false);

            if (VisibilityQuery.getDefault().isVisible(newFile) && newFile.isData()) {
                // delaying of this task was just copied from the old java.source RepositoryUpdater
                RequestProcessor.getDefault().create(new Runnable() {
                    public void run() {
                        scheduleWork(new FileListWork(root, Collections.singleton(newFile), false, false), false);
                    }
                }).schedule(FILE_LOCKS_DELAY);
            }
            processed = true;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File renamed (" + (processed ? "processed" : "ignored") + "): " //NOI18N
                    + FileUtil.getFileDisplayName(newFile) + " Owner: " + root
                    + " Original Name: " + oldNameExt); //NOI18N
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // assuming attributes change does not mean change in a file type
    }

    // -----------------------------------------------------------------------
    // PropertyChangeListener implementation
    // -----------------------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        assert SwingUtilities.isEventDispatchThread() : "Changes in focused editor component should be delivered on AWT"; //NOI18N
        
        List<? extends JTextComponent> components = Collections.<JTextComponent>emptyList();

        if (evt.getPropertyName() == null) {
            components = EditorRegistry.componentList();

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
            if (evt.getOldValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getOldValue();
                components = Collections.singletonList(jtc);
                handleActiveDocumentChange(jtc.getDocument(), null);
            }
            
        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
            if (evt.getNewValue() instanceof JTextComponent) {
                JTextComponent jtc = (JTextComponent) evt.getNewValue();
                components = Collections.singletonList(jtc);
                handleActiveDocumentChange(null, jtc.getDocument());
            }

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUSED_DOCUMENT_PROPERTY)) {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc == null) {
                jtc = EditorRegistry.lastFocusedComponent();
            }
            if (jtc != null) {
                components = Collections.singletonList(jtc);
            }

            handleActiveDocumentChange((Document) evt.getOldValue(), (Document) evt.getNewValue());
        }

        if (components.size() > 0) {
            Map<URL, FileListWork> jobs = new HashMap<URL, FileListWork>();
            for(JTextComponent jtc : components) {
                Document d = jtc.getDocument();
                FileObject f = NbEditorUtilities.getFileObject(d);
                if (f != null) {
                    URL root = getOwningSourceRoot(f);
                    if (root != null) {
                        long version = DocumentUtilities.getDocumentVersion(d);
                        Long lastSeenVersion = (Long) d.getProperty(PROP_LAST_SEEN_VERSION);

                        // check if we've ever seen this document, if it supports versioning
                        // and if so then if the version seen last time is the same as the current one
                        if (lastSeenVersion == null || version == 0 || lastSeenVersion < version) {
                            d.putProperty(PROP_LAST_SEEN_VERSION, version);

                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Document modified: " + FileUtil.getFileDisplayName(f) + " Owner: " + root); //NOI18N
                            }

                            FileListWork job = jobs.get(root);
                            if (job == null) {
                                job = new FileListWork(root, Collections.singleton(f), false, true);
                                jobs.put(root, job);
                            } else {
                                job.addFile(f);
                            }
                        }
                    }
                }
            }

            for(FileListWork job : jobs.values()) {
                scheduleWork(job, false);
            }
        }
    }

    // -----------------------------------------------------------------------
    // DocumentListener implementation
    // -----------------------------------------------------------------------

    public void changedUpdate(DocumentEvent e) {
        // no document modification
    }

    public void insertUpdate(DocumentEvent e) {
        removeUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        final Reference<Document> ref = activeDocumentRef;
        Document activeDocument = ref == null ? null : ref.get();
        Document document = e.getDocument();

        FileObject f = NbEditorUtilities.getFileObject(document);
        if (f != null) {
            URL root = getOwningSourceRoot(f);
            if (root != null) {
                if (activeDocument == document) {
                    // An active document was modified, we've indexed that document berfore,
                    // so mark it dirty
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Active document modified: " + FileUtil.getFileDisplayName(f) + " Owner: " + root); //NOI18N
                    }

                    Collection<? extends Indexable> dirty = Collections.singleton(SPIAccessor.getInstance().create(new FileObjectIndexable(URLMapper.findFileObject(root), f)));
                    String mimeType = DocumentUtilities.getMimeType(document);
                    Collection<? extends CustomIndexerFactory> customIndexerFactories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    Collection<? extends EmbeddingIndexerFactory> embeddingIndexerFactories = MimeLookup.getLookup(mimeType).lookupAll(EmbeddingIndexerFactory.class);

                    for(CustomIndexerFactory factory : customIndexerFactories) {
                        try {
                            Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root), root,
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false, true);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }

                    for(EmbeddingIndexerFactory factory : embeddingIndexerFactories) {
                        try {
                            Context ctx = SPIAccessor.getInstance().createContext(CacheFolder.getDataFolder(root), root,
                                    factory.getIndexerName(), factory.getIndexVersion(), null, false, true);
                            factory.filesDirty(dirty, ctx);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                        }
                    }
                } else {
                    // an odd event, maybe we could just ignore it
                    try {
                        addIndexingJob(root, Collections.singleton(f.getURL()), false, true, false);
                    } catch (FileStateInvalidException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static RepositoryUpdater instance;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N
    private static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test"); //NOI18N
    private static final boolean noRootsScan = Boolean.getBoolean("netbeans.indexing.noRootsScan"); //NOI18N
    private static final int FILE_LOCKS_DELAY = org.openide.util.Utilities.isWindows() ? 2000 : 1000;
    private static final String PROP_LAST_SEEN_VERSION = RepositoryUpdater.class.getName() + "-last-seen-document-version"; //NOI18N
    
    private final Map<URL, List<URL>>scannedRoots2Dependencies = Collections.synchronizedMap(new HashMap<URL, List<URL>>());
    private final Set<URL>scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedUnknown = Collections.synchronizedSet(new HashSet<URL>());

    private volatile State state = State.CREATED;
    private volatile Task worker;

    private volatile Reference<Document> activeDocumentRef = null;
    private Lookup.Result<? extends IndexingActivityInterceptor> indexingActivityInterceptors = null;
    private IndexingController controller;

    private RepositoryUpdater () {
        // no-op
    }

    private void handleActiveDocumentChange(Document deactivated, Document activated) {
        Document activeDocument = activeDocumentRef == null ? null : activeDocumentRef.get();

        if (deactivated != null && deactivated == activeDocument) {
            activeDocument.removeDocumentListener(this);
            activeDocumentRef = null;
            LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }

        if (activated != null && activated != activeDocument) {
            if (activeDocument != null) {
                activeDocument.removeDocumentListener(this);
                LOGGER.log(Level.FINE, "Unregistering active document listener: activeDocument={0}", activeDocument); //NOI18N
            }

            activeDocument = activated;
            activeDocumentRef = new WeakReference<Document>(activeDocument);
            
            activeDocument.addDocumentListener(this);
            LOGGER.log(Level.FINE, "Registering active document listener: activeDocument={0}", activeDocument); //NOI18N
        }
    }

    /* test */ void scheduleWork(final Work work, boolean wait) {
        recordCaller();

        synchronized (this) {
            if (state == State.STARTED) {
                state = State.INITIAL_SCAN_RUNNING;
                getWorker().schedule(new RootsWork(scannedRoots2Dependencies, scannedBinaries) {
                    public @Override void getDone() {
                        try {
                            if (work != null) {
                                try {
                                    long tm = System.currentTimeMillis();
                                    LOGGER.fine("Initial scan waiting for projects"); //NOI18N
                                    OpenProjects.getDefault().openProjects().get();
                                    LOGGER.log(Level.FINE, "Initial scan blocked for {0} ms", System.currentTimeMillis() - tm); //NOI18N
                                } catch (Exception ex) {
                                    // ignore
                                    LOGGER.log(Level.FINE, "Waiting for projects before initial scan timed out", ex); // NOI18N
                                }
                            } // else forced (eg. from tests) so don't wait for projects

                            super.getDone();
                        } finally {
                            if (state == State.INITIAL_SCAN_RUNNING) {
                                synchronized (RepositoryUpdater.this) {
                                    if (state == State.INITIAL_SCAN_RUNNING) {
                                        state = State.ACTIVE;
                                    }
                                }
                            }
                        }
                    }
                }, false);
            }
        }

        if (work != null) {
            getWorker().schedule(work, wait);
        }
    }

    private Task getWorker () {
        Task t = this.worker;
        if (t == null) {
            synchronized (this) {
                if (this.worker == null) {
                    this.worker = new Task ();
                }
                t = this.worker;
            }
        }
        return t;
    }

    private URL getOwningSourceRoot(final FileObject fo) {
        if (fo == null) {
            return null;
        }
        List<URL> clone = new ArrayList<URL> (this.scannedRoots2Dependencies.keySet());
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }

    private boolean authorize(FileEvent event) {
        Collection<? extends IndexingActivityInterceptor> interceptors = indexingActivityInterceptors.allInstances();
        for(IndexingActivityInterceptor i : interceptors) {
            if (i.authorizeFileSystemEvent(event) == IndexingActivityInterceptor.Authorization.IGNORE) {
                return false;
            }
        }
        return true;
    }

    private static final Map<List<StackTraceElement>, Long> lastRecordedStackTraces = new HashMap<List<StackTraceElement>, Long>();
    private static long stackTraceId = 0;
    private static void recordCaller() {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }

        synchronized (lastRecordedStackTraces) {
            StackTraceElement []  stackTrace = Thread.currentThread().getStackTrace();
            List<StackTraceElement> stackTraceList = new ArrayList<StackTraceElement>(stackTrace.length);
            for(StackTraceElement e : stackTrace) {
                stackTraceList.add(e);
            }

            Long id = lastRecordedStackTraces.get(stackTraceList);
            if (id == null) {
                id = stackTraceId++;
                lastRecordedStackTraces.put(stackTraceList, id);
                StringBuilder sb = new StringBuilder();
                sb.append("RepositoryUpdater caller [id=").append(id).append("] :\n"); //NOI18N
                for(StackTraceElement e : stackTraceList) {
                    sb.append(e.toString());
                    sb.append("\n"); //NOI18N
                }
                LOGGER.fine(sb.toString());
            } else {
                StackTraceElement caller = Util.findCaller(stackTrace);
                LOGGER.fine("RepositoryUpdater caller [refid=" + id + "]: " + caller); //NOI18N
            }
        }
    }

// we have to handle *all* mime types because of eg. tasklist indexer or goto-file indexer
//    private static boolean isMonitoredMimeType(FileObject f, Set<String> mimeTypes) {
//        String mimeType = FileUtil.getMIMEType(f, mimeTypes.toArray(new String[mimeTypes.size()]));
//        return mimeType != null && mimeTypes.contains(mimeType);
//    }

    enum State {CREATED, STARTED, INITIAL_SCAN_RUNNING, ACTIVE, STOPPED};

    /* test */ static abstract class Work {

        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final boolean followUpJob;
        private final boolean checkEditor;
        private final CountDownLatch latch;
        private ProgressHandle progressHandle = null;

//        private int allLanguagesParsersCount = -1;
//        private int allLanguagesTasksCount = -1;

        protected Work(boolean followUpJob, boolean checkEditor) {
            this.followUpJob = followUpJob;
            this.checkEditor = checkEditor;
            this.latch = new CountDownLatch(1);
        }

        protected final boolean isFollowUpJob() {
            return followUpJob;
        }
        
        protected final boolean hasToCheckEditor() {
            return checkEditor;
        }

        protected final void updateProgress(String message) {
            assert message != null;
            if (progressHandle == null) {
                return;
            }
            progressHandle.progress(message);
        }

        protected final void updateProgress(URL currentlyScannedRoot) {
            assert currentlyScannedRoot != null;
            if (progressHandle == null) {
                return;
            }
            progressHandle.progress(urlForMessage(currentlyScannedRoot));
        }

        protected final void updateProgress(URL currentlyScannedRoot, int scannedFiles, int totalFiles) {
            assert currentlyScannedRoot != null;
            if (progressHandle == null) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(urlForMessage(currentlyScannedRoot));
            sb.append(" (").append(scannedFiles).append(" of ").append(totalFiles).append(")"); //NOI18N
            progressHandle.progress(sb.toString());
        }

        protected final void delete (final Collection<Indexable> deleted, final URL root) throws IOException {
            if (deleted == null || deleted.size() == 0) {
                return;
            }

            LinkedList<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                Set<CustomIndexerFactory> customIndexerFactories = new HashSet<CustomIndexerFactory>();
                Set<EmbeddingIndexerFactory> embeddingIndexerFactories = new HashSet<EmbeddingIndexerFactory>();
                for (String mimeType : Util.getAllMimeTypes()) {
                    Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    customIndexerFactories.addAll(factories);

                    Collection<? extends EmbeddingIndexerFactory> embeddingFactories = MimeLookup.getLookup(mimeType).lookupAll(EmbeddingIndexerFactory.class);
                    embeddingIndexerFactories.addAll(embeddingFactories);
                }

                for (CustomIndexerFactory factory : customIndexerFactories) {
                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor);
                    factory.filesDeleted(deleted, ctx);
                }

                for(EmbeddingIndexerFactory factory : embeddingIndexerFactories) {
                    final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor);
                    factory.filesDeleted(deleted, ctx);
                }
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }
        }

        protected final void index (final Map<String,Collection<Indexable>> resources, final URL root) throws IOException {
            LinkedList<Context> transactionContexts = new LinkedList<Context>();
            try {
                // determine the total number of files
                int scannedFilesCount = 0;
                int totalFilesCount = 0;
                for (String mimeType : resources.keySet()) {
                    final Collection<? extends Indexable> indexables = resources.get(mimeType);
                    if (indexables != null) {
                        totalFilesCount += indexables.size();
                    }
                }

                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                for (String mimeType : resources.keySet()) {
                    final Collection<? extends Indexable> indexables = resources.get(mimeType);
                    if (indexables == null) {
                        continue;
                    }
                    
                    scannedFilesCount += indexables.size();
                    updateProgress(root, scannedFilesCount, totalFilesCount);
                    
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("-- Indexing " + mimeType + " in " + root); //NOI18N
                    }

                    final Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    boolean supportsEmbeddings = true;
                    for (CustomIndexerFactory factory : factories) {
                        boolean b = factory.supportsEmbeddedIndexers();
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.fine("CustomIndexerFactory: " + factory + ", supportsEmbeddedIndexers=" + b); //NOI18N
                        }

                        supportsEmbeddings &= b;
                        final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null, followUpJob, checkEditor);
                        transactionContexts.add(ctx);

                        // some CustomIndexers (eg. java) need to know about roots even when there
                        // are no modified Inexables at the moment (eg. java checks source level in
                        // the associated project, etc)
                        final CustomIndexer indexer = factory.createIndexer();
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Indexing " + indexables.size() + " indexables; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                        }
                        try {
                            SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                        } catch (ThreadDeath td) {
                            throw td;
                        } catch (Throwable t) {
                            LOGGER.log(Level.WARNING, null, t);
                        }
                    }

                    if (supportsEmbeddings) {
                        if (Util.canBeParsed(mimeType)) {
                            //Then use slow gsf like indexers
                            LOGGER.log(Level.FINE, "Using EmbeddingIndexers for {0}", indexables); //NOI18N

                            final SourceIndexer si = new SourceIndexer(root, cacheRoot, followUpJob, checkEditor);
                            si.index(indexables, transactionContexts);
                        } else {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(mimeType + " has no Parser or EmbeddingProvider registered and won't be indexed by embedding indexers"); //NOI18N
                            }
                        }
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(mimeType + " files indexed by custom indexers, embedding indexers forbidden"); //NOI18N
                        }
                    }

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("-- Finished indexing " + mimeType + " in " + root); //NOI18N
                    }
                }
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }
        }

        protected abstract void getDone();

        protected boolean isCancelledBy(Work newWork) {
            return false;
        }

        public boolean absorb(Work newWork) {
            return false;
        }

        protected final boolean isCancelled() {
            return cancelled.get();
        }

        public final void doTheWork() {
            try {
                getDone();
            } finally {
                latch.countDown();
            }
        }

        public final void waitUntilDone() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }

        public final void cancel() {
            cancelled.set(true);
        }

        public final void cancelBy(Work newWork) {
            if (isCancelledBy(newWork)) {
                cancelled.set(true);
            }
        }

        private String urlForMessage(URL currentlyScannedRoot) {
            String msg = null;

            URL tmp = FileUtil.getArchiveFile(currentlyScannedRoot);
            if (tmp == null) {
                tmp = currentlyScannedRoot;
            }
            try {
                if ("file".equals(tmp.getProtocol())) { //NOI18N
                    final File file = new File(new URI(tmp.toString()));
                    msg = file.getAbsolutePath();
                }
            } catch (URISyntaxException ex) {
                // ignore
            }

            return msg == null ? tmp.toString() : msg;
        }
    } // End of Work class

    private static final class FileListWork extends Work {

        private final URL root;
        private final Collection<FileObject> files;

        public FileListWork (URL root, boolean followUpJob, boolean checkEditor) {
            super(followUpJob, checkEditor);

            assert root != null;
            this.root = root;
            this.files = null;
        }

        public FileListWork (URL root, Collection<FileObject> files, boolean followUpJob, boolean checkEditor) {
            super(followUpJob, checkEditor);
            
            assert root != null;
            assert files != null && files.size() > 0;
            this.root = root;
            this.files = new HashSet<FileObject>();
            this.files.addAll(files);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("FileListWork: root=" + root + ", file=" + files); //NOI18N
            }
        }

        public void addFile(FileObject f) {
            assert f != null;
            assert FileUtil.isParentOf(URLMapper.findFileObject(root), f) : "File " + f + " does not belong under the root: " + root; //NOI18N
            files.add(f);
        }

        protected @Override void getDone() {
//            updateProgress(root);
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                try {
                    final Crawler crawler = files == null ?
                        new FileObjectCrawler(rootFo, false, null) : // rescan the whole root (no timestamp check)
                        new FileObjectCrawler(rootFo, files.toArray(new FileObject[files.size()]), false, null); // rescan selected files (no timestamp check)

                    final Map<String,Collection<Indexable>> resources = crawler.getResources();
                    index (resources, root);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
            TEST_LOGGER.log(Level.FINEST, "filelist"); //NOI18N
        }

        public @Override boolean absorb(Work newWork) {
            if (newWork instanceof FileListWork) {
                FileListWork nflw = (FileListWork) newWork;
                if (nflw.root.equals(root)
                    && nflw.isFollowUpJob() == isFollowUpJob()
                    && nflw.hasToCheckEditor() == hasToCheckEditor()
                ) {
                    files.addAll(nflw.files);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(this + ", root=" + root + " absorbed: " + nflw.files); //NOI18N
                    }
                    return true;
                }
            }
            return false;
        }
    } // End of FileListWork

    private static final class DeleteWork extends Work {

        private final URL root;
        private final Set<String> relativePaths = new HashSet<String>();

        public DeleteWork (URL root, String relativePath) {
            super(false, false);
            
            Parameters.notNull("root", root);
            Parameters.notNull("relativePath", relativePath);
            
            this.root = root;
            this.relativePaths.add(relativePath);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("DeleteWork: root=" + root + ", files=" + relativePaths);
            }
        }

        public @Override void getDone() {
//            updateProgress(root);
            try {
                final Collection<Indexable> indexables = new LinkedList<Indexable>();
                for(String path : relativePaths) {
                    indexables.add(SPIAccessor.getInstance().create(new DeletedIndexable (root, path)));
                }
                delete(indexables, root);
                TEST_LOGGER.log(Level.FINEST, "delete"); //NOI18N
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        public @Override boolean absorb(Work newWork) {
            if (newWork instanceof DeleteWork) {
                DeleteWork ndw = (DeleteWork) newWork;
                if (ndw.root.equals(root)) {
                    relativePaths.addAll(ndw.relativePaths);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(this + ", root=" + root + " absorbed: " + ndw.relativePaths); //NOI18N
                    }
                    return true;
                }
            }
            return false;
        }

    } // End of DeleteWork class

    private static class RefreshIndices extends Work {

        private final Set<String> indexerMimeTypes;
        private final CustomIndexerFactory indexerFactory;
        private final Map<URL, List<URL>> scannedRoots2Dependencies;

        public RefreshIndices(Set<String> indexerMimeTypes, CustomIndexerFactory indexerFactory, Map<URL, List<URL>> scannedRoots2Depencencies) {
            super(false, false);
            this.indexerMimeTypes = indexerMimeTypes;
            this.indexerFactory = indexerFactory;
            this.scannedRoots2Dependencies = scannedRoots2Depencencies;
        }

        @Override
        protected void getDone() {
            for(URL root : scannedRoots2Dependencies.keySet()) {
                try {
                    final FileObject rootFo = URLMapper.findFileObject(root);
                    if (rootFo != null) {
                        Crawler crawler = new FileObjectCrawler(rootFo, false, indexerMimeTypes);
                        final Map<String, Collection<Indexable>> resources = crawler.getResources();
                        final Collection<Indexable> deleted = crawler.getDeletedResources();

                        if (deleted.size() > 0) {
                            delete(deleted, root);
                        }

                        final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                        LinkedList<Context> transactionContexts = new LinkedList<Context>();
                        try {
                            for(String mimeType : resources.keySet()) {
                                final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, indexerFactory.getIndexerName(), indexerFactory.getIndexVersion(), null, false, false);
                                transactionContexts.add(ctx);

                                // some CustomIndexers (eg. java) need to know about roots even when there
                                // are no modified Inexables at the moment (eg. java checks source level in
                                // the associated project, etc)
                                final Collection<? extends Indexable> indexables = resources.get(mimeType);
                                if (indexables != null) {
                                    final CustomIndexer indexer = indexerFactory.createIndexer();
                                    if (LOGGER.isLoggable(Level.FINE)) {
                                        LOGGER.fine("Reindexing " + indexables.size() + " indexables; using " + indexer + "; mimeType='" + mimeType + "'"); //NOI18N
                                    }
                                    try {
                                        SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                                    } catch (ThreadDeath td) {
                                        throw td;
                                    } catch (Throwable t) {
                                        LOGGER.log(Level.WARNING, null, t);
                                    }
                                }
                            }
                        } finally {
                            for(Context ctx : transactionContexts) {
                                IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                                if (support != null) {
                                    SupportAccessor.getInstance().store(support);
                                }
                            }
                        }
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                }
            }
        }
    } // End of RefreshIndices class

    private static class RootsWork extends Work {

        private final Map<URL, List<URL>> scannedRoots2Dependencies;
        private final Set<URL> scannedBinaries;

        public RootsWork (Map<URL, List<URL>> scannedRoots2Depencencies, Set<URL> scannedBinaries) {
            super(false, false);
            this.scannedRoots2Dependencies = scannedRoots2Depencencies;
            this.scannedBinaries = scannedBinaries;
        }

        public @Override void getDone() {
            try {
                updateProgress(NbBundle.getMessage(RepositoryUpdater.class, "MSG_ProjectDependencies")); //NOI18N
                final DependenciesContext ctx = new DependenciesContext(scannedRoots2Dependencies, scannedBinaries, true);
                final List<URL> newRoots = new LinkedList<URL>();
                newRoots.addAll(PathRegistry.getDefault().getSources());
                newRoots.addAll(PathRegistry.getDefault().getLibraries());

                ctx.newBinariesToScan.addAll(PathRegistry.getDefault().getBinaryLibraries());
                for (Iterator<URL> it = ctx.newBinariesToScan.iterator(); it.hasNext(); ) {
                    if (ctx.oldBinaries.remove(it.next())) {
                        it.remove();
                    }
                }
                newRoots.addAll(PathRegistry.getDefault().getUnknownRoots());

                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();

                for (URL url : newRoots) {
                    findDependencies (url, depGraph, ctx, PathRecognizerRegistry.getDefault().getLibraryIds(), PathRecognizerRegistry.getDefault().getBinaryLibraryIds());
                }
                
                ctx.newRootsToScan.addAll(org.openide.util.Utilities.topologicalSort(depGraph.keySet(), depGraph));
                Collections.reverse(ctx.newRootsToScan);

                scanBinaries(ctx);
                scanSources(ctx);

                depGraph.keySet().retainAll(ctx.scannedRoots);
                scannedRoots2Dependencies.putAll(depGraph);
                scannedRoots2Dependencies.keySet().removeAll(ctx.oldRoots);
                
                scannedBinaries.addAll(ctx.scannedBinaries);
                scannedBinaries.removeAll(ctx.oldBinaries);
            } catch (final TopologicalSortException tse) {
                final IllegalStateException ise = new IllegalStateException ();
                throw (IllegalStateException) ise.initCause(tse);
            }
        }

        protected @Override boolean isCancelledBy(Work newWork) {
            boolean b = newWork instanceof RootsWork;
            if (b && LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cancelling " + this + ", because of " + newWork); //NOI18N
            }
            return b;
        }

        private static void findDependencies(
                final URL rootURL,
                final Map<URL, List<URL>> depGraph,
                DependenciesContext ctx,
                final Set<String> libraryClassPathIds,
                final Set<String> binaryLibraryClassPathIds)
        {
            if (ctx.useInitialState && ctx.initialRoots2Deps.containsKey(rootURL)) {
                ctx.oldRoots.remove(rootURL);
                return;
            }
            if (depGraph.containsKey(rootURL)) {
                return;
            }
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {
                return;
            }

            final List<URL> deps = new LinkedList<URL>();
            ctx.cycleDetector.push(rootURL);
            try {
                { // libraries
                    final List<ClassPath> libraryPathToResolve = new ArrayList<ClassPath>(libraryClassPathIds.size());
                    for (String id : libraryClassPathIds) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            libraryPathToResolve.add(cp);
                        }
                    }

                    for (ClassPath cp : libraryPathToResolve) {
                        for (ClassPath.Entry entry : cp.entries()) {
                            final URL sourceRoot = entry.getURL();
                            if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                deps.add(sourceRoot);
                                findDependencies(sourceRoot, depGraph, ctx, libraryClassPathIds, binaryLibraryClassPathIds);
                            }
                        }
                    }
                }

                { // binary libraries
                    final List<ClassPath> binaryLibraryPathToResolve = new ArrayList<ClassPath>(binaryLibraryClassPathIds.size());
                    for (String id : binaryLibraryClassPathIds) {
                        ClassPath cp = ClassPath.getClassPath(rootFo, id);
                        if (cp != null) {
                            binaryLibraryPathToResolve.add(cp);
                        }
                    }

                    for (ClassPath cp : binaryLibraryPathToResolve) {
                        for (ClassPath.Entry entry : cp.entries()) {
                            final URL url = entry.getURL();
                            final URL[] sourceRoots = PathRegistry.getDefault().sourceForBinaryQuery(url, cp, false);
                            if (sourceRoots != null) {
                                for (URL sourceRoot : sourceRoots) {
                                    if (!sourceRoot.equals(rootURL) && !ctx.cycleDetector.contains(sourceRoot)) {
                                        deps.add(sourceRoot);
                                        findDependencies(sourceRoot, depGraph, ctx, libraryClassPathIds, binaryLibraryClassPathIds);
                                    }
                                }
                            }
                            else {
                                //What does it mean?
                                if (ctx.useInitialState) {
                                    if (!ctx.initialBinaries.contains(url)) {
                                        ctx.newBinariesToScan.add (url);
                                    }
                                    ctx.oldBinaries.remove(url);
                                }
                            }
                        }
                    }
                }
            } finally {
                ctx.cycleDetector.pop();
            }

            depGraph.put(rootURL, deps);
        }

        private void scanBinaries (final DependenciesContext ctx) {
            assert ctx != null;
            for (URL binary : ctx.newBinariesToScan) {
                if (isCancelled()) {
                    break;
                }
                
                final long tmStart = System.currentTimeMillis();
                try {
                    if (!noRootsScan) {
                        updateProgress(binary);
                        scanBinary (binary);
                    }
                    ctx.scannedBinaries.add(binary);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                } finally {
                    final long time = System.currentTimeMillis() - tmStart;
                    if (PERF_TEST) {
                        reportRootScan(binary, time);
                    }
                    LOGGER.fine(String.format("Indexing of: %s took: %d ms", binary.toExternalForm(), time)); //NOI18N
                }
            }
            TEST_LOGGER.log(Level.FINEST, "scanBinary", ctx.newBinariesToScan);       //NOI18N
        }

        private void scanBinary(URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning binary root: {0}", root); //NOI18N

            List<Context> transactionContexts = new LinkedList<Context>();
            try {
                final FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo != null) {
                    final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                    String mimeType = ""; //NOI18N

                    final File archiveOrDir = FileUtil.archiveOrDirForURL(root);
                    final FileObject archiveOrDirFo = archiveOrDir == null ? null : FileUtil.toFileObject(archiveOrDir);
                    if (archiveOrDirFo != null && archiveOrDirFo.isData()) {
                        mimeType = archiveOrDirFo.getMIMEType();
                    }

                    final Collection<? extends BinaryIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(BinaryIndexerFactory.class);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.fine("Using CustomIndexerFactories(" + mimeType + "): " + factories); //NOI18N
                    }

                    for(BinaryIndexerFactory f : factories) {
                        final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, f.getIndexerName(), f.getIndexVersion(), null, false, false);
                        transactionContexts.add(ctx);

                        final BinaryIndexer indexer = f.createIndexer();
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine("Indexing binary " + root + " using " + indexer); //NOI18N
                        }
                        SPIAccessor.getInstance().index(indexer, ctx);
                    }
                }
            } finally {
                for(Context ctx : transactionContexts) {
                    IndexingSupport support = SPIAccessor.getInstance().context_getAttachedIndexingSupport(ctx);
                    if (support != null) {
                        SupportAccessor.getInstance().store(support);
                    }
                }
            }
        }

        private void scanSources  (final DependenciesContext ctx) {
            assert ctx != null;
            for (URL source : ctx.newRootsToScan) {
                if (isCancelled()) {
                    break;
                }

                final long tmStart = System.currentTimeMillis();
                try {
                    if (!noRootsScan) {
                        updateProgress(source);
                        scanSource (source);
                    }
                    ctx.scannedRoots.add(source);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, null, ioe);
                } finally {
                    final long time = System.currentTimeMillis() - tmStart;
                    if (PERF_TEST) {
                        reportRootScan(source, time);
                    }
                    LOGGER.fine(String.format("Indexing of: %s took: %d ms", source.toExternalForm(), time)); //NOI18N
                }
            }
            TEST_LOGGER.log(Level.FINEST, "scanSources", ctx.newRootsToScan);         //NOI18N
        }

        private void scanSource (URL root) throws IOException {
            LOGGER.log(Level.FINE, "Scanning sources root: {0}", root); //NOI18N

            //todo: optimize for java.io.Files
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                final Crawler crawler = new FileObjectCrawler(rootFo, true, null);
                final Map<String,Collection<Indexable>> resources = crawler.getResources();
                final Collection<Indexable> deleted = crawler.getDeletedResources();
                delete(deleted, root);
                index(resources, root);
            }
        }

        private static void reportRootScan(URL root, long duration) {
            try {
                Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                m.invoke(c.newInstance(), new Object[] {root.toExternalForm(), new Long(duration)});
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, null, e);
            }
        }
    } // End of RootsWork class

    private static final class Task extends ParserResultTask {

        // -------------------------------------------------------------------
        // Public implementation
        // -------------------------------------------------------------------

        public void schedule (Work work, boolean wait) {
            boolean enforceWork = false;
            boolean waitForWork = false;

            synchronized (todo) {
                assert work != null;
                if (!allCancelled) {
                      if (wait && Utilities.holdsParserLock()) {
                        if (protectedMode == 0) {
                            enforceWork = true;
                        } else {
//                            LOGGER.log(Level.FINE, "Won't enforce {0} when in protected mode", work); //NOI18N
//                            wait = false;
                            // nobody should actually call schedule(work, true) from
                            // within a UserTask, SchedulerTask or Indexer
                            throw new IllegalStateException("Won't enforce " + work + " when in protected mode"); //NOI18N
                        }
                    }

                    if (!enforceWork) {
                        if (workInProgress != null) {
                            workInProgress.cancelBy(work);
                        }

                        // coalesce ordinary jobs
                        boolean absorbed = false;
                        if (!wait) {
                            for(Work w : todo) {
                                if (w.absorb(work)) {
                                    absorbed = true;
                                    break;
                                }
                            }
                        }

                        if (!absorbed) {
                            LOGGER.log(Level.FINE, "Scheduling {0}", work); //NOI18N
                            todo.add(work);
                        } else {
                            LOGGER.log(Level.FINE, "Work absorbed {0}", work); //NOI18N
                        }
                        
                        if (!scheduled && protectedMode == 0) {
                            scheduled = true;
                            Utilities.scheduleSpecialTask(this);
                        }
                        waitForWork = wait;
                    }
                }
            }

            if (enforceWork) {
                // XXX: this will not set the isWorking() flag, which is strictly speaking
                // wrong, but probably won't harm anything
                LOGGER.log(Level.FINE, "Enforcing {0}", work); //NOI18N
                work.doTheWork();
            } else if (waitForWork) {
                LOGGER.log(Level.FINE, "Waiting for {0}", work); //NOI18N
                work.waitUntilDone();
            }
        }

        public void cancelAll() {
            synchronized (todo) {
                if (!allCancelled) {
                    // stop accepting new work and clean the queue
                    allCancelled = true;
                    todo.clear();

                    // stop the work currently being done
                    final Work work = workInProgress;
                    if (work != null) {
                        work.cancel();
                    }

                    // wait for until the current work is finished
                    while (scheduled) {
                        try {
                            todo.wait(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            }
        }

        public boolean isWorking() {
            synchronized (todo) {
                return scheduled || protectedMode > 0;
            }
        }

        public void enterProtectedMode() {
            synchronized (todo) {
                protectedMode++;
                LOGGER.log(Level.FINE, "Entering protected mode: {0}", protectedMode); //NOI18N
            }
        }

        public void exitProtectedMode(Runnable followupTask) {
            synchronized (todo) {
                if (protectedMode <= 0) {
                    throw new IllegalStateException("Calling exitProtectedMode without enterProtectedMode"); //NOI18N
                }

                // stash the followup task, we will run all of them when exiting the protected mode
                if (followupTask != null) {
                    if (followupTasks == null) {
                        followupTasks = new LinkedList<Runnable>();
                    }
                    followupTasks.add(followupTask);
                }

                protectedMode--;
                LOGGER.log(Level.FINE, "Exiting protected mode: {0}", protectedMode); //NOI18N

                if (protectedMode == 0) {
                    // in normal mode again, restart all delayed jobs
                    final List<Runnable> tasks = followupTasks;

                    // delaying of these tasks was just copied from the old java.source RepositoryUpdater
                    RequestProcessor.getDefault().create(new Runnable() {
                        public void run() {
                            schedule(new Work(false, false) {
                                protected @Override void getDone() {
                                    if (tasks != null) {
                                        for(Runnable task : tasks) {
                                            try {
                                                task.run();
                                            } catch (ThreadDeath td) {
                                                throw td;
                                            } catch (Throwable t) {
                                                LOGGER.log(Level.WARNING, null, t);
                                            }
                                        }
                                    }
                                }
                            }, false);
                        }
                    }).schedule(FILE_LOCKS_DELAY);
                    LOGGER.log(Level.FINE, "Protected mode exited, scheduling postprocess tasks: {0}", tasks); //NOI18N
                }
            }
        }

        public boolean isInProtectedMode() {
            synchronized (todo) {
                return protectedMode > 0;
            }
        }

        // returns false when timed out
        public boolean waitUntilFinished(long timeout) throws InterruptedException {
            if (Utilities.holdsParserLock()) {
                throw new IllegalStateException("Can't wait for indexing to finish from inside a running parser task"); //NOI18N
            }

            synchronized (todo) {
                while (scheduled) {
                    if (timeout > 0) {
                        todo.wait(timeout);
                        return !scheduled;
                    } else {
                        todo.wait();
                    }
                }
            }

            return true;
        }

        // -------------------------------------------------------------------
        // ParserResultTask implementation
        // -------------------------------------------------------------------

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return null;
        }

        @Override
        public void cancel() {
            // this task is not cancellable by the Parsing infrastructure
        }

        @Override
        public void run(Result nil, final SchedulerEvent nothing) {
            try {
                _run();
            } finally {
                synchronized (todo) {
                    scheduled = false;
                    todo.notifyAll();
                }
            }
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private final List<Work> todo = new LinkedList<Work>();
        private volatile Work workInProgress = null;
        private boolean scheduled = false;
        private boolean allCancelled = false;
        private int protectedMode = 0;
        private List<Runnable> followupTasks = null;

        private void _run() {
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class, "MSG_BackgroundCompileStart")); //NOI18N
            progressHandle.start();

            try {
                for(Work work = getWork(); work != null; work = getWork()) {
                    work.progressHandle = progressHandle;
                    try {
                        work.doTheWork();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        LOGGER.log(Level.WARNING, null, t);
                    } finally {
                        work.progressHandle = null;
                    }
                }
            } finally {
                progressHandle.finish();
            }
        }
        
        private Work getWork () {
            synchronized (todo) {
                Work w;
                if (protectedMode == 0 && todo.size() > 0) {
                    w = todo.remove(0);
                } else {
                    w = null;
                }
                workInProgress = w;
                return w;
            }
        }
    } // End of Task class

    private static final class DependenciesContext {

        private final Map<URL, List<URL>> initialRoots2Deps;
        private final Set<URL> initialBinaries;

        private final Set<URL> oldRoots;
        private final Set<URL> oldBinaries;

        private final List<URL> newRootsToScan;
        private final Set<URL> newBinariesToScan;

        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;

        private final Stack<URL> cycleDetector;
        private final boolean useInitialState;

        public DependenciesContext (final Map<URL, List<URL>> scannedRoots2Deps, final Set<URL> scannedBinaries, boolean useInitialState) {
            assert scannedRoots2Deps != null;
            assert scannedBinaries != null;
            
            this.initialRoots2Deps = Collections.unmodifiableMap(scannedRoots2Deps);
            this.initialBinaries = Collections.unmodifiableSet(scannedBinaries);

            this.oldRoots = new HashSet<URL> (scannedRoots2Deps.keySet());
            this.oldBinaries = new HashSet<URL> (scannedBinaries);

            this.newRootsToScan = new ArrayList<URL>();
            this.newBinariesToScan = new HashSet<URL>();

            this.scannedRoots = new HashSet<URL>();
            this.scannedBinaries = new HashSet<URL>();

            this.useInitialState = useInitialState;
            cycleDetector = new Stack<URL>();
        }
    } // End of DependenciesContext class

    private final class Controller extends IndexingController {

        public Controller() {
            super();
            RepositoryUpdater.this.start(false);
        }
        
        @Override
        public void enterProtectedMode() {
            getWorker().enterProtectedMode();
        }

        @Override
        public void exitProtectedMode(Runnable followUpTask) {
            getWorker().exitProtectedMode(followUpTask);
        }

        @Override
        public boolean isInProtectedMode() {
            return getWorker().isInProtectedMode();
        }

        @Override
        public Map<URL, List<URL>> getRootDependencies() {
            return new HashMap<URL, List<URL>>(RepositoryUpdater.this.scannedRoots2Dependencies);
        }

    } // End of Controller class

    // -----------------------------------------------------------------------
    // Methods for tests
    // -----------------------------------------------------------------------

    /**
     * Used by unit tests
     * @return
     */
    /* test */ State getState () {
        return state;
    }

    //Unit test method
    /* test */ Set<URL> getScannedBinaries () {
        return this.scannedBinaries;
    }

    //Unit test method
    /* test */ Set<URL> getScannedSources () {
        return this.scannedRoots2Dependencies.keySet();
    }

    //Unit test method
    /* test */ Set<URL> getScannedUnknowns () {
        return this.scannedUnknown;
    }
}
