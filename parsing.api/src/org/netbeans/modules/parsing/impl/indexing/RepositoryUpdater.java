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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;

/**
 *
 * @author Tomas Zezula
 */
public final class RepositoryUpdater implements PathRegistryListener, FileChangeListener, PropertyChangeListener {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    public static synchronized RepositoryUpdater getDefault() {
        if (instance == null) {
            instance = new RepositoryUpdater();
        }
        return instance;
    }

    public synchronized void close () {
        state = State.CLOSED;
        LOGGER.fine("Closing..."); //NOI18N

        PathRegistry.getDefault().removePathRegistryListener(this);
        FileUtil.removeFileChangeListener(this);
        EditorRegistry.removePropertyChangeListener(this);
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
                for(ClassPath cp : paths) {
                    sb.append("  \""); //NOI18N
                    sb.append(cp.toString(ClassPath.PathConversionMode.PRINT));
                    sb.append("\"\n"); //NOI18N
                }
                sb.append("--\n"); //NOI18N
            }
            sb.append("====\n"); //NOI18N
            LOGGER.fine(sb.toString());
        }
        submit(new RootsWork(scannedRoots, scannedBinaries));
    }

    // -----------------------------------------------------------------------
    // FileChangeListener implementation
    // -----------------------------------------------------------------------

    public void fileFolderCreated(FileEvent fe) {
        //In ideal case this should do nothing,
        //but in Netbeans newlly created folder may
        //already contain files
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot(fo);
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Folder created: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if ( root != null && VisibilityQuery.getDefault().isVisible(fo)) {
            submit(new FileListWork(root, fo));
        }
    }

    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File created: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null && VisibilityQuery.getDefault().isVisible(fo) &&
            FileUtil.getMIMEType(fo, PathRecognizerRegistry.getDefault().getMimeTypesAsArray()) != null)
        {
            submit(new FileListWork(root, fo));
        }
    }

    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File modified: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null && VisibilityQuery.getDefault().isVisible(fo) &&
            FileUtil.getMIMEType(fo, PathRecognizerRegistry.getDefault().getMimeTypesAsArray()) != null)
        {
            submit(new FileListWork(root, fo));
        }
    }

    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("File deleted: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root); //NOI18N
        }

        if (root != null &&  VisibilityQuery.getDefault().isVisible(fo)
            /*&& FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null*/) {
            submit(new DeleteWork(root, fo));
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        // XXX: what should we do here? Mimic fileDeleted() followed by fileDataCreated()?
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        // assuming attributes change does not mean change in a file type
    }

    // -----------------------------------------------------------------------
    // PropertyChangeListener implementation
    // -----------------------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        List<? extends JTextComponent> components = Collections.<JTextComponent>emptyList();

        if (evt.getPropertyName() == null) {
            components = EditorRegistry.componentList();

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_LOST_PROPERTY)) {
            if (evt.getOldValue() instanceof JTextComponent) {
                components = Collections.singletonList((JTextComponent) evt.getOldValue());
            }
            
        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUS_GAINED_PROPERTY)) {
            if (evt.getNewValue() instanceof JTextComponent) {
                components = Collections.singletonList((JTextComponent) evt.getNewValue());
            }

        } else if (evt.getPropertyName().equals(EditorRegistry.FOCUSED_DOCUMENT_PROPERTY)) {
            JTextComponent jtc = EditorRegistry.focusedComponent();
            if (jtc == null) {
                jtc = EditorRegistry.lastFocusedComponent();
            }
            if (jtc != null) {
                components = Collections.singletonList(jtc);
            }
        }

        if (components.size() > 0) {
            Map<URL, FileListWork> jobs = new HashMap<URL, FileListWork>();
            for(JTextComponent jtc : components) {
                FileObject f = NbEditorUtilities.getFileObject(jtc.getDocument());
                if (f != null) {
                    URL root = getOwningSourceRoot(f);
                    if (root != null) {
                        FileListWork job = jobs.get(root);
                        if (job == null) {
                            job = new FileListWork(root, f);
                            jobs.put(root, job);
                        } else {
                            job.addFile(f);
                        }
                    }
                }
            }

            for(FileListWork job : jobs.values()) {
                submit(job);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private implementation
    // -----------------------------------------------------------------------

    private static RepositoryUpdater instance;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger TEST_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests"); //NOI18N

    private final Set<URL>scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedUnknown = Collections.synchronizedSet(new HashSet<URL>());

    private volatile State state = State.CREATED;
    private volatile Task worker;

    private RepositoryUpdater () {
        init ();
    }

    private synchronized void init () {
        if (state == State.CREATED) {
            LOGGER.fine("Initializing..."); //NOI18N
            PathRegistry.getDefault().addPathRegistryListener(this);
            FileUtil.addFileChangeListener(this);
            EditorRegistry.addPropertyChangeListener(this);

            state = State.INITIALIZED;
            submit(new RootsWork(scannedRoots, scannedBinaries) {
                public @Override void getDone() {
                    try {
                        super.getDone();
                    } finally {
                        if (state == State.INITIALIZED) {
                            synchronized (RepositoryUpdater.this) {
                                if (state == State.INITIALIZED) {
                                    state = State.INITIALIZED_AFTER_FIRST_SCAN;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void submit (final Work  work) {
        Task t = getWorker ();
        assert t != null;
        LOGGER.log(Level.FINE, "Scheduling {0}", work);
        t.schedule (work);
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
        List<URL> clone = new ArrayList<URL> (this.scannedRoots);
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }

    enum State {CREATED, INITIALIZED, INITIALIZED_AFTER_FIRST_SCAN, CLOSED};

    private static abstract class Work {

        private ProgressHandle progressHandle = null;
        
        protected Work() {
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
            URL tmp = FileUtil.getArchiveFile(currentlyScannedRoot);
            if (tmp == null) {
                tmp = currentlyScannedRoot;
            }
            try {
                if ("file".equals(tmp.getProtocol())) { //NOI18N
                    final File file = new File(new URI(tmp.toString()));
                    progressHandle.progress(file.getAbsolutePath());
                }
                else {
                    progressHandle.progress(tmp.toString());
                }
            } catch (URISyntaxException ex) {
                progressHandle.progress(tmp.toString());
            }
        }

        protected final void index (final Map<String,Collection<Indexable>> resources, final Collection<Indexable> deleted, final URL root) throws IOException {
            SupportAccessor.getInstance().beginTrans();
            try {
                final FileObject cacheRoot = CacheFolder.getDataFolder(root);
                //First use all custom indexers
                Set<String> allMimeTypes = Util.getAllMimeTypes();
                for (String mimeType : allMimeTypes) {
                    final Collection<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(mimeType).lookupAll(CustomIndexerFactory.class);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Using CustomIndexerFactories(" + mimeType + "): " + factories); //NOI18N
                    }

                    boolean supportsEmbeddings = true;
                    try {
                        for (CustomIndexerFactory factory : factories) {
                            boolean b = factory.supportsEmbeddedIndexers();
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("CustomIndexerFactory: " + factory + ", supportsEmbeddedIndexers=" + b); //NOI18N
                            }

                            supportsEmbeddings &= b;
                            final Context ctx = SPIAccessor.getInstance().createContext(cacheRoot, root, factory.getIndexerName(), factory.getIndexVersion(), null);
                            factory.filesDeleted(deleted, ctx);
                            final Collection<? extends Indexable> indexables = resources.get(mimeType);
                            if (indexables != null) {
                                final CustomIndexer indexer = factory.createIndexer();
                                SPIAccessor.getInstance().index(indexer, Collections.unmodifiableCollection(indexables), ctx);
                            }
                        }
                    } finally {
                        if (!supportsEmbeddings) {
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine("Removing roots for " + mimeType + ", indexed by custom indexers, embedded indexers forbidden"); //NOI18N
                            }
                            resources.remove(mimeType);
                        }
                    }
                }
                //Then use slow gsf like indexers
                final List<Indexable> toIndex = new LinkedList<Indexable>();
                for (Collection<Indexable> data : resources.values()) {
                    toIndex.addAll(data);
                }

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Using EmbeddingIndexers for " + toIndex); //NOI18N
                }

                final SourceIndexer si = new SourceIndexer(root,cacheRoot);
                si.index(toIndex, deleted);
            } finally {
                SupportAccessor.getInstance().endTrans();
            }
        }

        public abstract void getDone();

    } // End of Work class

    private static final class FileListWork extends Work {

        private final URL root;
        private final Collection<FileObject> files;

        public FileListWork (URL root, FileObject file) {
            assert root != null;
            assert file != null;
            this.root = root;
            this.files = new HashSet<FileObject>();
            this.files.add(file);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("FileListWork: root=" + root + ", file=" + file); //NOI18N
            }
        }

        public void addFile(FileObject f) {
            assert f != null;
            assert FileUtil.isParentOf(URLMapper.findFileObject(root), f) : "File " + f + " does not belong under the root: " + root; //NOI18N
            files.add(f);
        }

        public void getDone() {
            updateProgress(root);
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }
            try {
                final Crawler crawler = new FileObjectCrawler(rootFo, files.toArray(new FileObject[files.size()]));
                final Map<String,Collection<Indexable>> resources = crawler.getResources();
                index (resources, Collections.<Indexable>emptyList(), root);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

    } // End of FileListWork

    private static final class DeleteWork extends Work {

        private final URL root;
        private final FileObject[] files;

        public DeleteWork (URL root, FileObject file) {
            assert root != null;
            assert file != null;
            this.root = root;
            this.files = new FileObject[] { file };
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("DeleteWork: root=" + root + ", file=" + file);
            }
        }

        public void getDone() {
            updateProgress(root);
            try {
                final ArrayList<Indexable> indexables = new ArrayList<Indexable>(files.length);
                final FileObject rootFo = URLMapper.findFileObject(root);
                for (int i=0; i< files.length; i++) {
                    indexables.add(SPIAccessor.getInstance().create(new DeletedIndexable (root, FileUtil.getRelativePath(rootFo, files[i]))));
                }
                index(Collections.<String,Collection<Indexable>>emptyMap(), indexables, root);
                TEST_LOGGER.log(Level.FINEST, "delete");         //NOI18N
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

    } // End of FileListWork

    private static class RootsWork extends Work {

        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;

        public RootsWork (Set<URL> scannedRoots, Set<URL> scannedBinaries) {
            this.scannedRoots = scannedRoots;
            this.scannedBinaries = scannedBinaries;
        }

        public void getDone() {
            try {
                updateProgress(NbBundle.getMessage(RepositoryUpdater.class, "MSG_ProjectDependencies")); //NOI18N
                final DependenciesContext ctx = new DependenciesContext(scannedRoots, scannedBinaries, true);
                final List<URL> newRoots = new LinkedList<URL>();
                newRoots.addAll(PathRegistry.getDefault().getSources());
                newRoots.addAll(PathRegistry.getDefault().getLibraries());
                newRoots.addAll(PathRegistry.getDefault().getUnknownRoots());

                ctx.newBinaries.addAll(PathRegistry.getDefault().getBinaryLibraries());
                for (Iterator<URL> it = ctx.newBinaries.iterator(); it.hasNext();) {
                    if (ctx.oldBinaries.remove(it.next())) {
                        it.remove();
                    }
                }
                ctx.newBinaries.removeAll(ctx.oldBinaries);
                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();

                for (URL url : newRoots) {
                    findDependencies (url, depGraph, ctx, PathRecognizerRegistry.getDefault().getLibraryIds(), PathRecognizerRegistry.getDefault().getBinaryLibraryIds());
                }
                ctx.newRoots.addAll(org.openide.util.Utilities.topologicalSort(depGraph.keySet(), depGraph));
                scanBinaries(ctx);
                scanSources(ctx);
                ctx.scannedRoots.removeAll(ctx.oldRoots);
                ctx.scannedBinaries.removeAll(ctx.oldBinaries);
            } catch (final TopologicalSortException tse) {
                final IllegalStateException ise = new IllegalStateException ();
                throw (IllegalStateException) ise.initCause(tse);
            }
        }
        
        private void findDependencies(
                final URL rootURL,
                final Map<URL,
                List<URL>> depGraph,
                DependenciesContext ctx,
                final Set<String> libraryClassPathIds,
                final Set<String> binaryLibraryClassPathIds)
        {
            if (ctx.useInitialState && ctx.scannedRoots.contains(rootURL)) {
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
                                    if (!ctx.scannedBinaries.contains(url)) {
                                        ctx.newBinaries.add (url);
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
            for (URL binary : ctx.newBinaries) {
                updateProgress(binary);
                scanBinary (binary);
                ctx.scannedBinaries.add(binary);
            }
            TEST_LOGGER.log(Level.FINEST, "scanBinary", ctx.newBinaries);       //NOI18N
        }

        private void scanBinary (URL root) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Scanning binary root: " + root); //NOI18N
            }
        }

        private void scanSources  (final DependenciesContext ctx) {
            assert ctx != null;
            for (URL source : ctx.newRoots) {
                try {
                    updateProgress(source);
                    scanSource (source);
                    ctx.scannedRoots.add(source);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }

            }
            TEST_LOGGER.log(Level.FINEST, "scanSources", ctx.newRoots);         //NOI18N
        }

        private void scanSource (URL root) throws IOException {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Scanning sources root: " + root); //NOI18N
            }

            //todo: optimize for java.io.Files
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null) {
                final Crawler crawler = new FileObjectCrawler(rootFo);
                final Map<String,Collection<Indexable>> resources = crawler.getResources();
                final Collection<Indexable> deleted = crawler.getDeletedResources();
                index (resources, deleted, root);
            }
        }
    } // End of RootsWork class

    private static final class Task extends ParserResultTask {

        // -------------------------------------------------------------------
        // Public implementation
        // -------------------------------------------------------------------

        public void schedule (Work work) {
            synchronized (todo) {
                assert work != null;
                if (!allCancelled) {
                    todo.add(work);
                    if (!scheduled) {
                        scheduled = true;
                        Utilities.scheduleSpecialTask(this);
                    }
                }
            }
        }

        public void cancelAll() {
            synchronized (todo) {
                allCancelled = true;
                todo.clear();
                
                while (scheduled) {
                    try {
                        todo.wait(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
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
                }
            }
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------

        private final List<Work> todo = new LinkedList<Work>();
        private boolean scheduled = false;
        private boolean allCancelled = false;

        private void _run() {
            ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class, "MSG_BackgroundCompileStart")); //NOI18N
            progressHandle.start();

            try {
                for(Work work = getWork(); work != null; work = getWork()) {
                    work.progressHandle = progressHandle;
                    try {
                        work.getDone();
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
                if (todo.size() > 0) {
                    return todo.remove(0);
                } else {
                    return null;
                }
            }
        }

    } // End of Task class

    private static final class DependenciesContext {

        private final Set<URL> oldRoots;
        private final Set<URL> oldBinaries;
        private final Set<URL> scannedRoots;
        private final Set<URL> scannedBinaries;
        private final Stack<URL> cycleDetector;
        private final List<URL> newRoots;
        private final Set<URL> newBinaries;
        private final boolean useInitialState;

        public DependenciesContext (final Set<URL> scannedRoots, final Set<URL> scannedBinaries, boolean useInitialState) {
            assert scannedRoots != null;
            assert scannedBinaries != null;
            this.scannedRoots = scannedRoots;
            this.scannedBinaries = scannedBinaries;
            this.useInitialState = useInitialState;
            cycleDetector = new Stack<URL>();
            oldRoots = new HashSet<URL> (scannedRoots);
            oldBinaries = new HashSet<URL> (scannedBinaries);
            this.newRoots = new ArrayList<URL>();
            this.newBinaries = new HashSet<URL>();
        }
    } // End of DependenciesContext class

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
        return this.scannedRoots;
    }

    //Unit test method
    /* test */ Set<URL> getScannedUnknowns () {
        return this.scannedUnknown;
    }
}
