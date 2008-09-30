/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.gsfret.source.usages;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
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
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.DateTools;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.Severity;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.gsf.GsfTaskProvider;
import org.netbeans.napi.gsfret.source.ClasspathInfo;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.ParserTaskImpl;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfret.source.GlobalSourcePath;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import org.netbeans.modules.gsfret.source.parsing.FileObjects;
import org.netbeans.modules.gsfret.source.util.LowMemoryEvent;
import org.netbeans.modules.gsfret.source.util.LowMemoryListener;
import org.netbeans.modules.gsfret.source.util.LowMemoryNotifier;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathFactory;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 * RepositoryUpdater is in charge of maintaining indices of the various classes in 
 * the system, for use by code completion, go to declaration, etc.  The classes include
 * not only the user's source and test directories, but jars from the boot class path etc.
 * The RepositoryUpdater schedules indexing jobs, watches filesystems for modifications,
 * determines whether an index is out of date, etc.
 * 
 * This class is originally from Retouche, under the java/source module. Since it's
 * an important and fairly complicated piece of logic, I am trying my best to keep
 * my copy in sync with the java one. Therefore, I have left the original formatting
 * in place as much as possible. Please don't make gratuitous formatting changes that
 * makes diffing harder.
 * 
 * There are some important changes. Obviously, the various javac-specific setup code
 * has changed, and I also need to -iterate- over files to be indexed to let the
 * potentially multiple language indexers each have a chance to index the file.
 * I have also changed references to other parts that have been renamed, such
 * as JavaSource => Source, etc.
 * 
 * @author Tomas Zezula
 * @author Tor Norbye
 */
public class RepositoryUpdater implements PropertyChangeListener, FileChangeListener {
    // Flag for controlling last-minute workaround for issue #120231
//    private static final boolean CLOSE_INDICES = Boolean.getBoolean("gsf.closeindices");
    
    private static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    static boolean haveIndexed = false;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger BUG_LOGGER = Logger.getLogger("ruby.indexerbug");
    private static final Set<String> ignoredDirectories = parseSet("org.netbeans.javacore.ignoreDirectories", "SCCS CVS .svn .hg"); // NOI18N
    private static final boolean noscan = Boolean.getBoolean("netbeans.javacore.noscan");   //NOI18N
    private static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test");
    //private static final String PACKAGE_INFO = "package-info.java";  //NOI18N
    
    private static final long STARTED = System.currentTimeMillis();
    private static String getElapsedTime() {
        StringBuilder sb = new StringBuilder();
        long now = System.currentTimeMillis();
        long elapsed = now-STARTED;
        long seconds = elapsed/1000;
        long minutes = seconds/60;
        if (seconds > 400) {
            seconds -= minutes*60;
            sb.append(minutes + " minutes, " + seconds + " seconds");
        } else {
            sb.append(seconds + " seconds");
        }
        sb.append(": ");
        return sb.toString();
    }
    
    // TODO - make delay configurable?
    private static final int DELAY = Utilities.isWindows() ? 2000 : 1000;
    
    private static RepositoryUpdater instance;
    
    private final GlobalSourcePath cpImpl;
    private final ClassPath cp;
    private final ClassPath ucp;
    private final ClassPath binCp;
    private Set<URL> scannedRoots;
    private Set<URL> scannedBinaries;
    private Map<URL,List<URL>> deps;        //todo: may be shared with scannedRoots, may save some HashMap.Entry
    private Delay delay;
    private Work currentWork;
    private boolean dirty;
    private int noSubmited;
    private final AtomicBoolean closed;
    
    //Preprocessor support
    //private final Map<URL, JavaFileFilterImplementation> filters = Collections.synchronizedMap(new HashMap<URL, JavaFileFilterImplementation>());
    //private final FilterListener filterListener = new FilterListener ();
    
    /** Creates a new instance of RepositoryUpdater */
    private RepositoryUpdater() {
        try {
            this.closed = new AtomicBoolean (false);
            this.scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
            this.scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());            
            this.deps = Collections.synchronizedMap(new HashMap<URL,List<URL>>());
            this.delay = new Delay();
            this.cpImpl = GlobalSourcePath.getDefault();
            this.cpImpl.setExcludesListener (this);
            this.cp = ClassPathFactory.createClassPath (this.cpImpl.getSourcePath());
            this.cp.addPropertyChangeListener(this);
            this.ucp = ClassPathFactory.createClassPath (this.cpImpl.getUnknownSourcePath());
            this.binCp = ClassPathFactory.createClassPath(this.cpImpl.getBinaryPath());
            this.registerFileSystemListener();
            submitBatch();
        } catch (TooManyListenersException e) {
            throw new IllegalStateException ();
        }
    }
    
    public ClassPath getScannedSources () {
        return this.cp;
    }
    
    public ClassPath getScannedBinaries() {
        return this.binCp;
    }
    
    public Map<URL,List<URL>> getDependencies () {
        return new HashMap<URL,List<URL>> (this.deps);
    }
    
    public void close () {                
        this.closed.set(true);
        this.cp.removePropertyChangeListener(this);
        this.unregisterFileSystemListener();
        this.delay.cancel();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            submitBatch();
        }
        else if (GlobalSourcePath.PROP_INCLUDES.equals(evt.getPropertyName())) {            
            ClassPath changedCp = (ClassPath) evt.getNewValue();
            assert changedCp != null;
            for (ClassPath.Entry e : changedCp.entries()) {
                URL root = e.getURL();
                scheduleCompilation(root,root, true);
            }
        }
    }
    
    private synchronized void submitBatch () {
        if (this.currentWork == null) {                    
            this.currentWork = Work.batch();
            submit (this.currentWork);
        }
        else {
            this.dirty = true;
        }
    }
    
    public synchronized boolean isScanInProgress() {
        return this.noSubmited > 0;
    }
    
    public synchronized void waitScanFinished () throws InterruptedException {
        while (this.noSubmited > 0 ) {
            this.wait();
        }
    }
    
    
    private synchronized boolean isDirty () {
        if (this.dirty) {
            this.dirty = false;
            return true;
        }
        else {
            this.currentWork = null;
            return false;                        
        }
    }
    
    private synchronized void resetDirty () {
        this.dirty = false;
        this.currentWork = null;
    }
    
    
    public void fileRenamed(FileRenameEvent fe) {
        final FileObject fo = fe.getFile();
        try {
            if ((isRelevantSource (fo) || fo.isFolder()) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot(fo);
                if (root != null) {                
                    String originalName = fe.getName();
                    final String originalExt = fe.getExt();
                    if (originalExt.length()>0) {
                        originalName = originalName+'.'+originalExt;  //NOI18N
                    }
                    final File parentFile = FileUtil.toFile(fo.getParent());
                    if (parentFile != null) {
                        final URL original = new File (parentFile,originalName).toURI().toURL();
                        submit(Work.delete(original,root,fo.isFolder()));
                        delay.post(Work.compile (fo,root));
                    }
                }
            }
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    String originalName = fe.getName();
                    final String originalExt = fe.getExt();
                    if (originalExt.length()>0) {
                        originalName = originalName+'.'+originalExt;    //NOI18N
                    }
                    final File parentFile = FileUtil.toFile(fo.getParent());
                    if (parentFile != null) {
                        final URL original = new File (parentFile,originalName).toURI().toURL();
                        submit(Work.binary(original, root, fo.isFolder()));
                        submit(Work.binary(fo, root));
                    }
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        //Not interesting, do nothing
    }

    public void fileFolderCreated(FileEvent fe) {
        //In ideal case this should do nothing,
        //but in Netbeans newly created folder may
        //already contain files
        final FileObject fo = fe.getFile();
        try {
            final URL root = getOwningSourceRoot(fo);
            if ( root != null && VisibilityQuery.getDefault().isVisible(fo)) {
                scheduleCompilation(fo,root);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();        
        final boolean isFolder = fo.isFolder();
        try {
            boolean relevantSource = isRelevantSource(fo);
            if (!relevantSource && "content/unknown".equals(fo.getMIMEType())) { // NOI18N
                // When deleted, file objects lose their mimetypes...
                relevantSource = true;
            }
            if ((relevantSource || isFolder) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);
                if (root != null) {                
                    submit(Work.delete(fo,root,isFolder));
                }
            }
            else if ((isBinary(fo) || isFolder) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root !=null) {
                    submit(Work.binary(fo,root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();        
        try {
            if (isRelevantSource(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);        
                if (root != null) {
                    postCompilation(fo, root);
                }
            }
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    submit(Work.binary(fo, root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        try {
            if (isRelevantSource(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);
                if (root != null) {
                    postCompilation(fo, root);
                }
            }        
            else if (isBinary(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningBinaryRoot(fo);
                if (root != null) {
                    submit(Work.binary(fo, root));
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }  
    
    
    public final void scheduleCompilation (final FileObject fo, final FileObject root) throws IOException {
        URL foURL = fo.getURL();
        URL rootURL = root.getURL();
        assert "file".equals(foURL.getProtocol()) && "file".equals(rootURL.getProtocol());
        scheduleCompilation (foURL,rootURL,fo.isFolder());
    }      
    
    private final void scheduleCompilation (final FileObject fo, final URL root) throws IOException {
        scheduleCompilation (fo.getURL(),root,fo.isFolder());
    }
    
    private final void scheduleCompilation (final URL file, final URL root, boolean isFolder) {
        submit(Work.compile (file,root, isFolder));
    }
    
    private final void postCompilation (final FileObject file, final URL root) throws FileStateInvalidException {
        delay.post (Work.compile (file,root));
    }
    
    
    /**
     * This method is only for unit tests.
     * Test can schedule compilation and wait on the returned {@link CountDownLatch}
     * until the compilation is finished.
     * @param folder to be compiled
     * @param root the source root. The folder has to be either under the root or
     * equal to the root.
     * @return {@link CountDownLatch} to wait on.
     */
    public final CountDownLatch scheduleCompilationAndWait (final FileObject folder, final FileObject root) throws IOException {
        CountDownLatch[] latch = new CountDownLatch[1];
        submit(Work.compile (folder,root.getURL(),latch));
        return latch[0];
    }
    
    private void submit (final Work  work) {
        if (!noscan) {
            synchronized (this) {
                this.noSubmited++;
            }
            final CompileWorker cw = new CompileWorker (work);
            SourceAccessor.getINSTANCE().runSpecialTask (cw, Source.Priority.MAX);
        }
    }
    
    
    private void registerFileSystemListener  () {
        FileUtil.addFileChangeListener(this);
    }
    
    private void unregisterFileSystemListener () {
        FileUtil.removeFileChangeListener(this);
    }
    
    private URL getOwningSourceRoot (final FileObject fo) {
        if (fo == null) {
            return null;
        }
        List<URL> clone = new ArrayList<URL>(this.scannedRoots);
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }        
    
    private URL getOwningBinaryRoot (final FileObject fo){
        if (fo == null) {
            return null;
        }
        try {
            synchronized (this.scannedBinaries) {
                URL foURL = fo.getURL();
                for (URL root : this.scannedBinaries) {
                    URL fileURL = FileUtil.getArchiveFile(root);
                    boolean archive = true;
                    if (fileURL == null) {
                        fileURL = root;
                        archive = false;
                    }
                    String filePath = fileURL.getPath();
                    String foPath = foURL.getPath();
                    if (filePath.equals(foPath)) {
                        return root;
                    }                    
                    if (!archive && foPath.startsWith(filePath)) {                        
                        return root;
                    }
                }
            }
        } catch (FileStateInvalidException fsi) {
            Exceptions.printStackTrace(fsi);
        }
        return null;
    }
    
    /**
     * Temporary implementation which does not care about
     * extended mime types like text/x-something+x-java
     */
    public static boolean isRelevantSource (final FileObject fo) {
        if (fo.isFolder()) {
            return false;
        }

        if (LanguageRegistry.getInstance().isSupported(fo.getMIMEType())) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isBinary (final FileObject fo) {
        return false;
        /* XXX TODO no support for binary persistence files yet, such as compiled ruby files etc.
        if (fo.isFolder()) {
            return false;
        }
        String ext = fo.getExt().toLowerCase();
        if (FileObjects.CLASS.equals(ext) ||
            FileObjects.JAR.equals(ext) ||
            FileObjects.ZIP.equals(ext)) { 
                return true;
        }        
        return false;
         */
    }
    
    private static enum WorkType {
        COMPILE_BATCH, COMPILE_CONT, COMPILE, DELETE, UPDATE_BINARY, FILTER_CHANGED
    };
    
    
    private static class Work {
        private final WorkType workType;
        private final CountDownLatch latch;
        
        protected Work (WorkType workType, CountDownLatch latch) {
            assert workType != null;
            this.workType = workType;
            this.latch = latch;
        }
        
        public WorkType getType () {
            return this.workType;
        }
        
        public void finished () {
            if (this.latch != null) {
                this.latch.countDown();
            }
        }
        
        
        public static Work batch () {
            return new Work (WorkType.COMPILE_BATCH, null);
        }
        
        public static Work compile (final FileObject file, final URL root) throws FileStateInvalidException {
            return compile (file.getURL(), root, file.isFolder());
        }
        
        public static Work compile (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.COMPILE, file, root, isFolder, null);
        }
        
        public static Work compile (final FileObject file, final URL root, CountDownLatch[] latch) throws FileStateInvalidException {
            assert file != null && root != null;
            assert latch != null && latch.length == 1 && latch[0] == null;
            latch[0] = new CountDownLatch (1);
            return new SingleRootWork (WorkType.COMPILE, file.getURL(), root, file.isFolder(),latch[0]);
        }
        
        public static Work delete (final FileObject file, final URL root, final boolean isFolder) throws FileStateInvalidException {
            return delete (file.getURL(), root,file.isFolder());
        }
        
        public static Work delete (final URL file, final URL root, final boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.DELETE, file, root, isFolder,null);
        }
        
        public static Work binary (final FileObject file, final URL root) throws FileStateInvalidException {
            return binary (file.getURL(), root, file.isFolder());
        }
        
        public static Work binary (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.UPDATE_BINARY, file, root, isFolder, null);
        }
        
        public static Work filterChange (final List<URL> roots) {
            assert roots != null;
            return new MultiRootsWork (WorkType.FILTER_CHANGED, roots, null);
        }
        
    }
        
    private static class SingleRootWork extends Work {
        
        private URL file;
        private URL root;
        private boolean isFolder;
        
                
        public SingleRootWork (WorkType type, URL file, URL root, boolean isFolder, CountDownLatch latch) {
            super (type, latch);           
            this.file = file;            
            this.root = root;
            this.isFolder = isFolder;
        }
        
        public URL getFile () {
            return this.file;
        }
        
        public URL getRoot () {
            return this.root;
        }
        
        public boolean isFolder () {            
            return this.isFolder;            
        }
        
    }
    
    private static class MultiRootsWork extends Work {
        private List<URL> roots;
        
        public MultiRootsWork (WorkType type, List<URL> roots, CountDownLatch latch) {
            super (type, latch);
            this.roots = roots;
        }
        
        public List<URL> getRoots () {
            return roots;
        }
    }  
    
    private final class CompileWorker implements CancellableTask<CompilationInfo> {
                
        private Work work;
        private List<URL> state;
        private Set<URL> oldRoots;
        private Set<URL> oldBinaries;
        private Set<URL> newBinaries;                
        private ProgressHandle handle;
        private final Set<URI> dirtyCrossFiles;        
        private final Set<URL> ignoreExcludes;
        private final AtomicBoolean canceled;
        
        public CompileWorker (Work work ) {
            assert work != null;
            this.work = work;
            this.canceled = new AtomicBoolean (false);
            this.dirtyCrossFiles = new HashSet<URI>();
            this.ignoreExcludes = new HashSet<URL>();
        }

        public void cancel () {            
            this.canceled.set(true);
        }
        
        public void run (final CompilationInfo nullInfo) throws IOException {
            ClassIndexManager./*getDefault().*/writeLock (new ClassIndexManager.ExceptionAction<Void> () {
                
                @SuppressWarnings("fallthrough")
                public Void run () throws IOException {
                    
                    boolean continuation = false;
                    try {
                    final WorkType type = work.getType();                        
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +  "CompilerWorker.run - type=" + type);
}
                    switch (type) {
                        case FILTER_CHANGED:                            
                            try {
                                final MultiRootsWork mw = (MultiRootsWork) work;
                                final List<URL> roots = mw.getRoots();
                                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();                                
                                for (URL root: roots) {
                                    findDependencies (root, new Stack<URL>(), depGraph, null, false);
                                }
                                state = Utilities.topologicalSort(roots, depGraph);                                                                             
                                for (java.util.ListIterator<URL> it = state.listIterator(state.size()); it.hasPrevious(); ) {                
                                    if (closed.get()) {
                                        return null;
                                    }
                                    final URL rootURL = it.previous();
                                    it.remove();
                                    updateFolder (rootURL,rootURL, true, handle);
                                }                                
                            } catch (final TopologicalSortException tse) {
                                    final IllegalStateException ise = new IllegalStateException ();                                
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +  "CompilerWorker *** IllegalStateException ", tse);
}
                                    throw (IllegalStateException) ise.initCause(tse);
                            }
                            break;
                        case COMPILE_BATCH:
                        {
                            assert handle == null;
                            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                            handle.start();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() + "CompilerWorker.run.COMPILE_BATCH - created and started handle " + handle);
}
                            boolean completed = false;
                            try {
                                oldRoots = new HashSet<URL> (scannedRoots);
                                oldBinaries = new HashSet<URL> (scannedBinaries);
                                final List<ClassPath.Entry> entries = new LinkedList<ClassPath.Entry>();
                                entries.addAll (cp.entries());
                                entries.addAll (ucp.entries());
                                final List<ClassPath.Entry> binaryEntries = binCp.entries();
                                newBinaries = new HashSet<URL> ();
                                for (ClassPath.Entry entry : binaryEntries) {
                                    URL binRoot = entry.getURL();
                                    if (!oldBinaries.remove(binRoot)) {
                                        newBinaries.add (binRoot);
                                    }
                                }

                                final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();
                                for (ClassPath.Entry entry : entries) {
                                    if (closed.get()) {
                                        return null;
                                    }
                                    final URL rootURL = entry.getURL();
                                    findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries, true);
                                }                                
                                
                                if (PREINDEXING && depGraph.size() > 0) {
                                    for (Language language : LanguageRegistry.getInstance()) {
                                        Collection<FileObject> coreLibraries = language.getGsfLanguage().getCoreLibraries();
                                        Indexer indexer = language.getIndexer();
                                        if (indexer == null) {
                                            continue;
                                        }
                                        if (coreLibraries != null) {
                                            for (FileObject libFo : coreLibraries) {
                                                URL binRoot = libFo.getURL();
                                                if (indexer.acceptQueryPath(binRoot.toExternalForm())) {
                                                    //newBinaries.add(binRoot);
                                                    depGraph.put(binRoot, Collections.<URL>emptyList());
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                deps.putAll(depGraph);
                                completed = true;
                            } catch (final TopologicalSortException tse) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - THREW EXCEPTION!", tse);
}
                                final IllegalStateException ise = new IllegalStateException ();                                
                                throw (IllegalStateException) ise.initCause(tse);
                            } finally {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - completed=" + completed);
}
                                if (!completed) {
                                    resetDirty();
                                }
                            }
                        }
                        case COMPILE_CONT:
                            boolean completed = false;
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_CONT - about to scan roots");
}
                            try {
                                if (!scanRoots()) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH -failed - doing continuation!");
}
                                    CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                    SourceAccessor.getINSTANCE().runSpecialTask (CompileWorker.this, Source.Priority.MAX);
                                    continuation = true;
                                    return null;
                                }
                                while (isDirty()) {
                                    if (closed.get()) {
                                        return null;
                                    }
                                    assert CompileWorker.this.state.isEmpty();                                    
                                    final List<ClassPath.Entry> entries = new LinkedList<ClassPath.Entry>();
                                    entries.addAll (cp.entries());
                                    entries.addAll (ucp.entries());                                    
                                    final List<ClassPath.Entry> binaryEntries = binCp.entries();
                                    newBinaries = new HashSet<URL> ();
                                    for (ClassPath.Entry entry : binaryEntries) {
                                        URL binRoot = entry.getURL();
                                        if (!scannedBinaries.contains(binRoot)) {
                                            newBinaries.add(binRoot);
                                        }
                                        else {
                                            oldBinaries.remove(binRoot);
                                        }
                                    }
                                    final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();
                                    for (ClassPath.Entry entry : entries) {
                                        if (closed.get()) {
                                            return null;
                                        }
                                        final URL rootURL = entry.getURL();
                                        findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries, true);
                                    }
                                    try {
                                        CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                        deps.putAll(depGraph);
                                    } catch (final TopologicalSortException tse) {
                                        final IllegalStateException ise = new IllegalStateException ();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker *** IllegalStateException ", ise);
}
                                        throw (IllegalStateException) ise.initCause(tse);
                                    }
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - tryihng scanRoots again - state=" + state + ",newBinaries=" + newBinaries + ", oldBinaries=" + oldBinaries);
}
                                    if (!scanRoots ()) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - scanRoots failed AGAIN!");
}
                                        CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                        SourceAccessor.getINSTANCE().runSpecialTask (CompileWorker.this, Source.Priority.MAX);
                                        continuation = true;
                                        return null;
                                    }
                                }
                                completed = true;
                            } finally {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - finally: completed=" + completed + ", continuation=" + continuation);
}
                                if (!completed && !continuation) {
                                    resetDirty ();
                                }
                            }                            
//                            final ClassIndexManager cim = ClassIndexManager.getDefault();
                            scannedRoots.removeAll(oldRoots);
                            deps.keySet().remove(oldRoots);
//if (CLOSE_INDICES) {    // HACK - see #120231                        
//                            for (URL oldRoot : oldRoots) {
//                                cim.removeRoot(oldRoot);
////                                JavaFileFilterImplementation filter = filters.remove(oldRoot);
////                                if (filter != null && !filters.values().contains(filter)) {
////                                    filter.removeChangeListener(filterListener);
////                                }
//                            }
//}
                            scannedBinaries.removeAll (oldBinaries);
//                            final CachingArchiveProvider cap = CachingArchiveProvider.getDefault();
//if (CLOSE_INDICES) {                            
//                            for (URL oldRoot : oldBinaries) {
//                                cim.removeRoot(oldRoot);
////                                cap.removeArchive(oldRoot);
//                            }
//}
                            break;
                        case COMPILE:
                        {
                            try {
                                final SingleRootWork sw = (SingleRootWork) work;
                                final URL file = sw.getFile();
                                final URL root = sw.getRoot ();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE; file=" + file +", root=" + root);
}
                                if (sw.isFolder()) {
                                    handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_Updating"));
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE - created handle - " + handle);
}
                                    handle.start();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE - started handle - " + handle);
}
                                    try {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE - updating file " + file + ", root=" + root);
}
                                        updateFolder (file, root, false, handle);
                                    } finally {
                                        handle.finish();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE - finished handle - " + handle);
}
                                    }
                                }
                                else {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE - updating file");
}
                                    updateFile (file,root);
                                }
                            //} catch (Abort abort) {
                            } catch (Exception abort) {
                                //Ignore abort
                            }                         
                            break;
                        }
                        case DELETE:
                        {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.DELETE");
}
                            final SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot ();
                            delete (file, root, sw.isFolder());
                            break;                                 
                        }
                        case UPDATE_BINARY:
                        {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.BINARY");
}
                            SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot();
                            updateBinary (file, root);
                            break;
                        }
                    }                                                
                    return null;                    
                } finally {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.finally: continuation=" + continuation);
}
                    if (!continuation) {
                        synchronized (RepositoryUpdater.this) {
                            RepositoryUpdater.this.noSubmited--;
                            if (RepositoryUpdater.this.noSubmited == 0) {
                                RepositoryUpdater.this.notifyAll();
                                
                                if (PREINDEXING) {
                                    if (haveIndexed) {
                                        LifecycleManager.getDefault().saveAll();
                                        LifecycleManager.getDefault().exit();
                                    }
                                }
                            }
                        }
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.finally.after submission noSubmitted=" + RepositoryUpdater.this.noSubmited);
}
                        work.finished ();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.finally.finished -- handle=" + handle);
}
                        if (handle != null) {
                            handle.finish ();
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.run.COMPILE_BATCH - finished handle " + handle);
}
                        }
                    }
                }
            }});
        }
        
        private Map<FileObject, WeakReference<ClassPath>> sourceClassPathsCache =
                new WeakHashMap<FileObject, WeakReference<ClassPath>>();

        private ClassPath getBootClassPaths(FileObject file, String type/*, FileObject systemRoot*/) {
            // Default provider - do this for things like Ruby library files
            synchronized (this) {
                ClassPath cp = null;
                if (!file.isFolder()) {
                    //file = systemRoot;
                    assert false : file;
                }
                if (file.isFolder()) {
                    Reference ref = (Reference) this.sourceClassPathsCache.get (file);
                    if (ref == null || (cp = (ClassPath)ref.get()) == null ) {
                        cp = ClassPathSupport.createClassPath(new FileObject[] {file});
                        this.sourceClassPathsCache.put(file, new WeakReference<ClassPath>(cp));
                    }
                }
                return cp;                                        
            }
        }
        
        
        private void findDependencies (final URL rootURL, final Stack<URL> cycleDetector, final Map<URL,List<URL>> depGraph,
            final Set<URL> binaries, final boolean useInitialState) {           
            if (useInitialState && RepositoryUpdater.this.scannedRoots.contains(rootURL)) {                                
                this.oldRoots.remove(rootURL);                        
                return;
            }
            if (depGraph.containsKey(rootURL)) {
                return;
            }                                                
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {               
                return;
            }
            
            // I don't want to start asking for the ClassPath of directories in the libraries
            // since these start yielding Java jars etc.
            if (true) {
                //if (useInitialState) {
                   depGraph.put(rootURL,new LinkedList<URL> ());

                    if (!RepositoryUpdater.this.scannedBinaries.contains(rootURL)) {
                        binaries.add (rootURL);
                    }
                    oldBinaries.remove(rootURL);
                //}
                return;
            }
            
            cycleDetector.push (rootURL);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
            final ClassPath[] pathsToResolve = new ClassPath[] {bootPath,compilePath};
//            ClassPath libraryPath = LanguageRegistry.getInstance().getLibraryPaths();
//            final ClassPath[] pathsToResolve =  libraryPath != null ?
//                new ClassPath[] {bootPath, compilePath, libraryPath} :
//                new ClassPath[] {bootPath,compilePath};
            final List<URL> deps = new LinkedList<URL> ();
            for (int i=0; i< pathsToResolve.length; i++) {
                final ClassPath pathToResolve = pathsToResolve[i];
                if (pathToResolve != null) {
                    for (ClassPath.Entry entry : pathToResolve.entries()) {
                        final URL url = entry.getURL();
                        final URL[] sourceRoots = RepositoryUpdater.this.cpImpl.getSourceRootForBinaryRoot(url, pathToResolve, false);
                        if (sourceRoots != null) {
                            for (URL sourceRoot : sourceRoots) {
                                if (sourceRoot.equals (rootURL)) {
                                    this.ignoreExcludes.add (rootURL);
                                }
                                else if (!cycleDetector.contains(sourceRoot)) {
                                    deps.add (sourceRoot);
                                    findDependencies(sourceRoot, cycleDetector,depGraph, binaries, useInitialState);
                                }
                            }
                        }
                        else {
                            if (useInitialState) {
                                if (!RepositoryUpdater.this.scannedBinaries.contains(url)) {
                                    binaries.add (url);
                                }
                                oldBinaries.remove(url);
                            }
                        }
                    }
                }
            }
            depGraph.put(rootURL,deps);
            cycleDetector.pop ();
        }
        
        private boolean scanRoots () {
            
            for (Iterator<URL> it = this.newBinaries.iterator(); it.hasNext(); ) {
                if (this.canceled.getAndSet(false)) {                    
                    return false;
                }
                if (closed.get()) {
                    return true;
                }
                final URL rootURL = it.next();
                try {
                    it.remove();
                    String urlString = rootURL.toExternalForm();
                    for (IndexerEntry entry : getIndexers()) {
                        Language language = entry.getLanguage();
                        if (entry.indexer.acceptQueryPath(urlString)) {
                            final ClassIndexImpl ci = ClassIndexManager.get(language).createUsagesQuery(rootURL,false);                                        
                        }
                    }
                    RepositoryUpdater.this.scannedBinaries.add (rootURL);                    
//                    long startT = System.currentTimeMillis();
//                    ci.getBinaryAnalyser().analyse(rootURL, handle);
//                    long endT = System.currentTimeMillis();
//                    if (PERF_TEST) {
//                        try {
//                            Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
//                            java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
//                            m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(endT - startT)});
//                        } catch (Exception e) {
//                                Exceptions.printStackTrace(e);
//                        }                            
//                    }
                } catch (Throwable e) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker *** caught exception " , e);
}
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath) e;
                    }
                    else {
                        Exceptions.attachMessage(e, "While scanning: " + rootURL);
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            for (java.util.ListIterator<URL> it = this.state.listIterator(this.state.size()); it.hasPrevious(); ) {
                if (this.canceled.getAndSet(false)) {                    
                    return false;
                }
                if (closed.get()) {
                    return true;
                }
                try {
                    final URL rootURL = it.previous();
                    it.remove();                                                                                
                    if (!oldRoots.remove(rootURL) && !RepositoryUpdater.this.scannedRoots.contains(rootURL)) {
                        long startT = System.currentTimeMillis();                        
                        updateFolder (rootURL,rootURL, false, handle);
                        long endT = System.currentTimeMillis();
                        if (PERF_TEST) {
                            try {
                                Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                                java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                                m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(endT - startT)});
                            } catch (Exception e) {
                                    Exceptions.printStackTrace(e);
                            }
                        }
                        if (PREINDEXING) {
                            // How do I obtain the data folder for this puppy?
                            for (Language language : LanguageRegistry.getInstance()) {
                                if (language.getIndexer() != null) {
                                    Index.preindex(language, rootURL);
                                }
                            }
                            haveIndexed = true;
                        }
                    }
                } catch (Throwable e) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker *** caught exception 3 " , e);
}
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath) e;
                    }
                    else {
                        Exceptions.printStackTrace (e);
                    }
                }
            }
            return true;
        }

        private boolean isBoot(ClassPath bootPath, FileObject rootFo) {
            for (FileObject fo : bootPath.getRoots()) {
                if (fo == rootFo) {
                    return true;
                }
            }

            return false;
        }
        
        private void updateFolder(final URL folder, final URL root, boolean clean, final ProgressHandle handle) throws IOException {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }            
            if (!rootFo.isFolder()) {
                LOGGER.warning("Source root has to be a folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }            
            ClassPath sourcePath = ClassPath.getClassPath(rootFo,ClassPath.SOURCE);
            ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);            
            final boolean isInitialCompilation = folder.equals(root);            
            if (sourcePath == null || bootPath == null || compilePath == null) {
                //LOGGER.warning("Ignoring root with no ClassPath: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                //return;
                ClassPath cp = getBootClassPaths(rootFo, ClassPath.SOURCE);
                if (sourcePath == null) {
                    sourcePath = cp;
                }
                if (bootPath == null) {
                    bootPath = cp;
                }
                if (compilePath == null) {
                    compilePath = cp;
                }
            }            
            //boolean isBoot = isInitialCompilation && ClassIndexManager.isBootRoot(root);
            boolean isBoot = isInitialCompilation && isBoot(bootPath, rootFo);
            if (!isBoot) {
                String urlString = root.toExternalForm();
                if (urlString.indexOf("/vendor/") != -1) {
                    isBoot = true;
                }
            }
/*
                // XXX This is suspicious
            if (!clean && isInitialCompilation) {
                //Initial compilation  debug messages
                if (RepositoryUpdater.this.scannedRoots.contains(root)) {
                    return;
                }                
                LOGGER.fine("Scanning Root: " + FileUtil.getFileDisplayName(rootFo));    //NOI18N
            }
*/
            try {     
                File rootFile = FileUtil.toFile(rootFo);
                if (rootFile == null) {
                    FileObject jar = FileUtil.getArchiveFile(rootFo);
                    rootFile = FileUtil.toFile(jar);
                    if (rootFile == null) {
                        // Probably a jar, which sometimes ends up in my updateFolder now because the
                        // isBinary stuff etc. isn't working yet
                        return;
                    }
                }
                final File folderFile = isInitialCompilation ? rootFile : FileUtil.normalizeFile(new File (URI.create(folder.toExternalForm())));
                
//                //Preprocessor support
                Object filter = null;
//                JavaFileFilterImplementation filter = filters.get(root);
//                if (filter == null) {
//                    filter = JavaFileFilterQuery.getFilter(rootFo);
//                    if (filter != null) {
//                        if (!filters.values().contains(filter)) {
//                            filter.addChangeListener(filterListener);
//                        }
//                        filters.put(root, filter);
//                    }
//                }
                List<ParserFile> toCompile = new LinkedList<ParserFile>();
                final Map <String,List<File>> resources = Collections.emptyMap();
                final LazyFileList children = new LazyFileList(folderFile);
                
                // If this is a boot class path, don't update it
                boolean isRootFolder = isBoot;
                // Known Rails user-project exceptions (not recorded as boot roots since 
                // they are in the user's project directories)
                if (folderFile.getName().equals("vendor") || folderFile.getName().equals("lib")) { // NOI18N
                    // lib? Won't that mess up my user projects?
                    isRootFolder = true;
                }

                boolean checkUpToDate = false;
                if (isRootFolder) {
                    if (folderFile.exists() && folderFile.canRead()) {
                        checkUpToDate = true;
                    }
                }
                boolean allUpToDate = checkUpToDate;

                Map<Language,Map<String,String>> timeStamps = new HashMap<Language,Map<String,String>>();

                boolean invalidIndex = false;
                String rootString = root.toExternalForm();
                for (IndexerEntry entry : getIndexers()) {
                    Language language = entry.getLanguage();
                    if (!entry.indexer.acceptQueryPath(rootString)) {
                        continue;
                    }

                    ClassIndexImpl uqImpl = ClassIndexManager.get(language).createUsagesQuery(root, true);
                    assert uqImpl != null;
                    SourceAnalyser sa = uqImpl.getSourceAnalyser();
                    assert sa != null;
                    if (checkUpToDate && !sa.isUpToDate(null, folderFile.lastModified())) {
                        allUpToDate = false;
                    } else if (isInitialCompilation) {
                        if (!sa.isValid()) {
                            invalidIndex = true;
                            allUpToDate = false;
                        } else { //if (!isBoot) {
                            final ClassIndexImpl ci = ClassIndexManager.get(language).getUsagesQuery(root);   
                            if (ci != null) {
                                // I should only do this if allUpToDate is false!
                                Map<String,String> ts = ci.getTimeStamps();
                                if (ts != null && ts.size() > 0) {
                                    timeStamps.put(language, ts);
                                }
                            }
                        }
                    }
                }
                
                if (allUpToDate) {
                    return;
                }
                
                if (handle != null) {
                    final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_Scannig",rootFile.getAbsolutePath());
                    handle.setDisplayName(message);
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.updateFolder - updating handle " + handle + " to " + message + " + folderFile");
}
                }

                if (timeStamps.size() == 0) {
                    timeStamps = null;
                }
                
                
//                Set<File> rs = new HashSet<File> ();
                ClassPath.Entry entry = null;
                final ClasspathInfo cpInfo;
                if (!this.ignoreExcludes.contains(root)) {
                    entry = getClassPathEntry(sourcePath, root);
                    cpInfo = ClasspathInfoAccessor.getInstance().create(bootPath,compilePath,sourcePath, filter, true,false);
                }
                else {
                    cpInfo = ClasspathInfoAccessor.getInstance().create(bootPath,compilePath,sourcePath, filter, true,true);
                }
                
//                Set<ElementHandle<TypeElement>> removed = isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
//                Set<ElementHandle<TypeElement>> added =   isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
Set removed = null;
Set added = null;
                for (File child : children) {       
                    String offset = FileObjects.getRelativePath(rootFile,child);                    
                    if (entry == null || entry.includes(offset.replace(File.separatorChar,'/'))) {                                                
                        if (invalidIndex || clean || dirtyCrossFiles.remove(child.toURI())) {
                            toCompile.add (FileObjects.fileFileObject(child, rootFile, isBoot, null/* filter*/));
                        }
                        else {
                            final int index = offset.lastIndexOf('.');  //NOI18N
                            if (index > -1) {
                                offset = offset.substring(0,index);
                            }
                            List<File> files = resources.remove(offset);                        
                            if  (files==null) {
                                toCompile.add(FileObjects.fileFileObject(child, rootFile, isBoot, null/*filter*/));
                            } else {
//                                boolean rsf = files.get(0).getName().endsWith(FileObjects.RS);
                                if (files.get(0).lastModified() < child.lastModified()) {
                                    toCompile.add(FileObjects.fileFileObject(child, rootFile, isBoot, null/*filter*/));
                                    for (File toDelete : files) {                            
                                        toDelete.delete();
//                                        if (rsf) {                                    
//                                            rsf = false;
//                                        }
//                                        else {
//                                            String className = FileObjects.getBinaryName(toDelete,classCache);
//                                            sa.delete(toDelete);
//                                            if (removed != null) {
//                                                removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                                            }
                                        }                            
//                                    }
//                                }
//                                else if (rsf) {
//                                    files.remove(0);
//                                    rs.addAll(files);
                                }                        
                            }
                        }
                    }
                }
                for (List<File> files : resources.values()) {
                    for (File toDelete : files) {
  //                      if (!rs.contains(toDelete)) {
                            toDelete.delete();
                            if (toDelete.getName().endsWith(FileObjects.SIG)) {
                                //String className = FileObjects.getBinaryName(toDelete,classCache);                        
                                //sa.delete(className);
//                                sa.delete(toDelete);
//                                if (removed != null) {
//                                    removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                                }
                            }
//                        }
                    }
                }
                if (!toCompile.isEmpty()) {
                    if (handle != null) {
                        // Show message for "indexing" rather than compiling since I'm not keeping trees around etc - it's
                        // all used to populate Lucene at this point.
                        //final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompile",rootFile.getAbsolutePath());
                        String path = rootFile.getAbsolutePath();
                        // Shorten path by prefix to ruby location if possible
                        final String message = NbBundle.getMessage(RepositoryUpdater.class,"MSG_Analyzing",path);
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker.updateFolder2 - updating handle " + handle + " to " + message);
}
                        handle.setDisplayName(message);
                    }

                    CachingIndexer cachingIndexer = CachingIndexer.get(root, toCompile.size());
                    
                    Map<Language,List<File>> seenTimestampedFiles = new HashMap<Language, List<File>>();

                    batchCompile(toCompile, rootFo, cpInfo, cachingIndexer, root, dirtyCrossFiles, added, timeStamps, seenTimestampedFiles);

                    if (timeStamps != null) {
                        deleteRemovedFiles(cachingIndexer, timeStamps, seenTimestampedFiles);
                    }

                    if (cachingIndexer != null) {
                        cachingIndexer.flush();
                    }
                }
// store is a noop anyway                
//                sa.store();
                
//                if (added != null) {
//                    assert removed != null;
//                    Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
//                    Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
//                    _at.removeAll(removed);
//                    _rt.removeAll(added);
//                    added.retainAll(removed);                                                                   //Changed
//                    uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
//                            _rt.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,_rt), 
//                            added.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,added));
//                }
            } finally {
                if (!clean && isInitialCompilation) {
                    RepositoryUpdater.this.scannedRoots.add(root);
                }
            }
        }

        /** Delete any files from the index that we no longer see on disk. */
        private void deleteRemovedFiles(CachingIndexer cachingIndexer, Map<Language, Map<String, String>> timeStamps, Map<Language, List<File>> seenTimestampedFiles) {
            for (Language language : timeStamps.keySet()) {
                List<File> seen = seenTimestampedFiles.get(language);
                int seenCount = seen != null ? seen.size() : 0;
                Map<String,String> stamps = timeStamps.get(language);
                // TODO - do I really need to pull out the keySet() here?
                int indexedCount = stamps != null ? stamps.keySet().size() : 0;
                if (seenCount != indexedCount) {
                    // Special case: We generate some extra index entries for things
                    // in jar files -- those shouldn't count
                    int jarFileCount = 0;
                    for (String url : stamps.keySet()) {
                        if (url.startsWith("jar:")) { // NOI18N
                            jarFileCount++;
                        }
                    }

                    if (seenCount+jarFileCount == indexedCount) {
                        // Yes, the discrepancy was just because of files in jar files
                        continue;
                    }

                    // We only count files that we've timestamped, thus we can
                    // never get a greater seen count than the number of files in
                    // the index.
                    if (seenCount > indexedCount) {
                        LOGGER.warning("Unexpectedly encountered more timestamped files (" + seenCount + ") than indexed (" + indexedCount + ")"); // NOI18N
                        if (seenCount < 50) {
                            LOGGER.warning(" Details: seen=" + seen + "; stamps=" + stamps);
                        }
                    }

                    // Now we have to figure out which files were deleted. Those
                    // are the files we have in the index that weren't encountered
                    // on disk.

                    // First translate the files into URLs such that we can do proper
                    // comparisons
                    //List<String> seenUrls = new ArrayList<String>(seenCount);
                    Set<String> seenUrls = new HashSet<String>(2*seenCount);
                    if (seen != null) {
                        assert stamps != null;
                        for (File f : seen) {
                            Indexer indexer = language.getIndexer();
                            assert indexer != null;
                            String url = indexer.getPersistentUrl(f);
                            seenUrls.add(url);
                        }

                    }

                    Set<String> removed = new HashSet<String>(stamps.keySet());
                    removed.removeAll(seenUrls);

                    for (String url : removed) {
                        try {
                            if (url.startsWith("jar:")) { // NOI18N
                                continue;
                            }
                            cachingIndexer.remove(language, url);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
        
        private List<Language> getApplicableIndexers(String mimeType) {
            List<Language> languages = null;
            for (Language language : LanguageRegistry.getInstance().getApplicableLanguages(mimeType)) {
                if (language.getIndexer() == null) {
                    continue;
                }
                
                if (languages == null) {
                    languages = new ArrayList<Language>(5);
                }
                languages.add(language);
            }
            
            return languages != null ? languages : Collections.<Language>emptyList();
        }
        
        private void updateFile (final URL file, final URL root) throws IOException {
            final FileObject fo = URLMapper.findFileObject(file);
            if (fo == null) {
                return;
            }

        List<Language> languages = getApplicableIndexers(fo.getMIMEType());
        if (languages.size() > 0) {
            final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
            final File fileFile = FileUtil.toFile(fo);
            ParserFile active = FileObjects.fileFileObject(fileFile, rootFile, false, null/*filter*/);
            ParserFile[] activeList = new ParserFile[]{active};
            ClasspathInfo cpInfo = ClasspathInfoAccessor.getInstance().create (fo, null/*filter*/, true, false);
            ClassPath.Entry entry = getClassPathEntry (cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),root);
            boolean scan = (entry == null || entry.includes(fo));
            String rootString = root.toExternalForm();
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
        for (Language language : languages) {
            if (language.getIndexer() != null && !language.getIndexer().acceptQueryPath(rootString)) {
                // TODO - shouldn't I also skip this for-iteration if indexer is null?
                continue;
            }
            
            final ClassIndexImpl uqImpl = ClassIndexManager.get(language).createUsagesQuery(root, true);
            if (uqImpl != null) {
                uqImpl.setDirty(null);
//                final JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(fo);
                //final File classCache = Index.getClassFolder (rootFile);
                //final Map <String,List<File>> resources = getAllClassFiles (classCache, FileObjects.getRelativePath(rootFile, fileFile.getParentFile()),false);
//                String offset = FileObjects.getRelativePath (rootFile,fileFile);
//                final int index = offset.lastIndexOf('.');  //NOI18N
//                if (index > -1) {
//                    offset = offset.substring(0,index);
//                }
                //List<File> files = resources.remove (offset);                
                SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                //final Set<ElementHandle<TypeElement>> added = new HashSet<ElementHandle<TypeElement>>();
                //final Set<ElementHandle<TypeElement>> removed = new HashSet <ElementHandle<TypeElement>> ();
                // TODO: Handle deletions; is this only used for th sig files?
//                if (files != null) {
//                    for (File toDelete : files) {
//                        toDelete.delete();
//                        if (toDelete.getName().endsWith(FileObjects.SIG)) {
//                            String className = FileObjects.getBinaryName (toDelete,classCache);                                                           
//                            sa.delete (className);
//                            removed.add (ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
//                        }
//                    }
//                }
//                else {
//                    sa.delete(FileObjects.convertFolder2Package(offset, '/'));  //NOI18N
//                }
                if (scan) {
                    final CompilerListener listener = new CompilerListener ();
                    //final JavaFileManager fm = ClasspathInfoAccessor.getInstance().getFileManager(cpInfo);                
                    //JavaFileObject active = FileObjects.fileFileObject(fileFile, rootFile, filter);
                    ParserTaskImpl jt = SourceAccessor.getINSTANCE().createParserTask(language, cpInfo);
                    //jt.setTaskListener(listener);
                    jt.setParseListener(listener);
                    //Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {active});
                    Iterable<ParserResult> trees = jt.parse(activeList);
                    //jt.enter();            
                    //jt.analyze ();
                    //dumpClasses(listener.getEnteredTypes(), fm, root.toExternalForm(), null, ...
                    //sa.analyse (trees, jt, fm, active, added);
                    sa.analyse (language, trees);
                    
                    listener.cleanDiagnostics();                    
                }
//                sa.store();
//                Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
//                Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
//                _at.removeAll(removed);
//                _rt.removeAll(added);
//                added.retainAll(removed);                                                                   //Changed
//                uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
//                        _rt.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,_rt), 
//                        added.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,added));                
            }
          }
          
          GsfTaskProvider.refresh(fo);
        }
        }
        
        private void delete (final URL file, final URL root, final boolean folder) throws IOException {
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
            assert "file".equals(file.getProtocol()) : "Unexpected protocol of URL: " + file;   //NOI18N
            final File fileFile = FileUtil.normalizeFile(new File (URI.create(file.toExternalForm())));
            final String offset = FileObjects.getRelativePath (rootFile,fileFile);
            assert offset != null && offset.length() > 0 : String.format("File %s not under root %s ", fileFile.getAbsolutePath(), rootFile.getAbsolutePath());  // NOI18N                        

            boolean platform = false;
            ParserFile parserFile = FileObjects.fileFileObject(fileFile, rootFile, platform, null);
            
        for (Language language : LanguageRegistry.getInstance()) {
            // I have to iterate over all indexer languages here - I can't call
            // getApplicableIndexers() since I don't have the mime type for 
            // deleted files (and asking for it just returns content/unknown)
            if (language.getIndexer() == null) {
                continue;
            }
            if (!language.getIndexer().acceptQueryPath(root.toExternalForm())) {
                continue;
            }
            final ClassIndexImpl uqImpl = ClassIndexManager.get(language).createUsagesQuery(root, true);
            assert uqImpl != null;                
            final SourceAnalyser sa = uqImpl.getSourceAnalyser();
            assert sa != null;
            sa.delete(parserFile, language);
        }
//            
//            
//            final File classCache = Index.getClassFolder (rootFile);
//            File[] affectedFiles = null;
//            if (folder) {
//                final File container = new File (classCache, offset);
//                affectedFiles = container.listFiles();
//            }
//            else {
//                int slashIndex = offset.lastIndexOf (File.separatorChar);
//                int dotIndex = offset.lastIndexOf('.');     //NOI18N
//                final File container = slashIndex == -1 ? classCache : new File (classCache,offset.substring(0,slashIndex));
//                final String name = offset.substring(slashIndex+1, dotIndex);
//                final String[] patterns = new String[] {
//                  name + '.',
//                  name + '$'
//                };
//                final File[] content  = container.listFiles();
//                if (content != null) {
//                    final List<File> result = new ArrayList<File>(content.length);
//                    for (File f : content) {
//                        final String fname = f.getName();
//                        if (fname.startsWith(patterns[0]) || fname.startsWith(patterns[1])) {
//                            result.add(f);
//                        }
//                    }
//                    affectedFiles = result.toArray(new File[result.size()]);
//                }
//            }
//            if (affectedFiles != null && affectedFiles.length > 0) {
////                Set<ElementHandle<TypeElement>> removed = new HashSet<ElementHandle<TypeElement>>();
//                final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
//                assert uqImpl != null;                
//                final SourceAnalyser sa = uqImpl.getSourceAnalyser();
//                assert sa != null;
//                for (File f : affectedFiles) {
////                    if (f.getName().endsWith(FileObjects.RS)) {
////                        List<File> rsFiles = new LinkedList<File>();
////                        readRSFile(f, classCache, rsFiles);
////                        for (File rsf : rsFiles) {
////                            String className = FileObjects.getBinaryName (rsf,classCache);                                                                        
////                            sa.delete (className);
////                            removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
////                            rsf.delete();
////                        }
////                    }
////                    else {
////                        String className = FileObjects.getBinaryName (f,classCache);                                                                        
////                        sa.delete (className);
//                    sa.delete(f);
////                        removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
////                    }
//                    f.delete();                    
//                }
//                sa.store();                
////                uqImpl.typesEvent(null,new ClassIndexImplEvent(uqImpl, removed), null);
//            }
        }
        
        private void updateBinary (final URL file, final URL root) throws IOException {            
////            CachingArchiveProvider.getDefault().clearArchive(root);                       
//            File cacheFolder = Index.getClassFolder(root);
//            FileObjects.deleteRecursively(cacheFolder);
//            final BinaryAnalyser ba = ClassIndexManager.getDefault().createUsagesQuery(root, false).getBinaryAnalyser();
//            ba.analyse(root, handle);
        }                
    }        
    
    static class LazyFileList implements Iterable<File> {
    
        private File root;

        public LazyFileList (final File root) {
            assert root != null;
            this.root = root;
        }

        public Iterator<File> iterator() {
            if (!root.exists()) {
                return Collections.<File>emptySet().iterator();
            }
            return new It (this.root);
        }


        private class It implements Iterator<File> {

            private final LinkedList<File> toDo = new LinkedList<File>();

            public It(File root) {
                File[] files = root.listFiles();
                if (files != null && files.length > 0) {
                    this.toDo.addAll(java.util.Arrays.asList(files));
                }
            }

            public boolean hasNext() {
                while (!toDo.isEmpty()) {
                    File f = toDo.peek();
                    final String name = f.getName();
                    if (f.isDirectory() && !ignoredDirectories.contains(name)) {
                        f = toDo.removeFirst();
                        assert f.isDirectory();
                        File[] content = f.listFiles();
                        if (content != null) {
                            for (int i = 0; i < content.length; i++) {
                                f = content[i];
                                if (f == null) {
                                    continue;
                                }
                                if (f.isFile()) {
                                    this.toDo.addFirst(f);
                                } else {
                                    this.toDo.addLast(f);
                                }
                            }
                        }
                    } else {
                        return true;
                    }
                }
                return false;
            }

            public File next() {
                return toDo.removeFirst();
            }

            public void remove() {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker throwing exception 1 ");
}
                throw new UnsupportedOperationException();
            }
        }
    }
    
    
    private final class Delay {
        
        private final Timer timer;
        private final List<Work> tasks;
        
        public Delay () {
            this.timer = new Timer(RepositoryUpdater.class.getName());
            this.tasks = new LinkedList<Work> ();
        }
        
        public synchronized void post (final Work work) {
            assert work != null;
            this.tasks.add (work);
            this.timer.schedule(new DelayTask (work),DELAY);
        }
        
        public void cancel () {
            Work[] toCancel;
            synchronized (this) {
                toCancel = this.tasks.toArray (new Work[this.tasks.size()]);
            }
            for (Work w : toCancel) {
                if (w.workType == WorkType.COMPILE) {
                    w = new SingleRootWork (WorkType.DELETE,((SingleRootWork)w).file,
                        ((SingleRootWork)w).root,((SingleRootWork)w).isFolder,
                        w.latch);
                }
                CompileWorker cw = new CompileWorker (w);
                try {
                    cw.run (null);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        
        
        private class DelayTask extends TimerTask {
            
            final Work work;
            
            public DelayTask (final Work work) {
                this.work = work;
            }
            
            public void run() {
                submit(work);
                synchronized (Delay.this) {
                    Delay.this.tasks.remove (work);
                }                
            }

            public @Override boolean cancel() {
                boolean retValue = super.cancel();
                if (retValue) {
                    synchronized (Delay.this) {
                        Delay.this.tasks.remove (work);
                    }
                }
                return retValue;
            }                        
        }                
    }
    
    private static class CompilerListener implements /*DiagnosticListener<JavaFileObject>,*/LowMemoryListener, ParseListener {
                               
        final List<Error> errors = new LinkedList<Error> ();
        final List<Error> warnings = new LinkedList<Error> ();
//        final List<ClassSymbol> justEntered = new LinkedList<ClassSymbol> ();
        final List<ParserResult> justEntered = new LinkedList<ParserResult> ();
        final AtomicBoolean lowMemory = new AtomicBoolean ();
//        
        void cleanDiagnostics () {
            if (!this.errors.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Error msg : this.errors) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.errors.clear();
            }
            if (!this.warnings.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Error msg: this.warnings) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.warnings.clear();
            }
            this.justEntered.clear();
        }
        
        List<ParserResult> getEnteredTypes () {
            List<ParserResult> result = new ArrayList<ParserResult>(this.justEntered);
            this.justEntered.clear();
            return result;
        }
        
        public void error(Error error) {
            if (error.getSeverity() == Severity.ERROR) {
                this.errors.add(error);
            } else {
                this.warnings.add(error);
            }
        }
        
        public void exception(Exception exception) {
        }
        
//
//        public void report(final Diagnostic diagnosticMessage) {
//            Diagnostic.Kind kind = diagnosticMessage.getKind();
//            if ( kind == Diagnostic.Kind.ERROR) {
//                this.errors.add (diagnosticMessage);
//            }
//            else if (kind == Diagnostic.Kind.WARNING
//                   ||kind == Diagnostic.Kind.MANDATORY_WARNING) {
//                this.warnings.add (diagnosticMessage);
//            }
//        }
//        
        public void started(ParseEvent e) {
            
        }

        public void finished(ParseEvent event) {
            if (event.getKind() == ParseEvent.Kind.PARSE /*ENTER*/) {
                //final CompilationUnitTree unit = event.getCompilationUnit();
                final ParserResult result = event.getResult();
                if (result != null) {
                    this.justEntered.add(result);
                }
//                
//                for (Tree typeTree : unit.getTypeDecls()) {
//                    if (typeTree instanceof JCTree.JCClassDecl) {       //May be a JCTree.JCExpressionStatement in case of an error
//                        ClassSymbol sym = ((JCTree.JCClassDecl)typeTree).sym;
//                        if (sym != null) {
//                            if (sym.sourcefile == null) {
//                                sym.sourcefile = event.getSourceFile();
//                            }
//                            this.justEntered.add(sym);
//                        }
//                    }          
//                }
            }
        }
        
        public void lowMemory (final LowMemoryEvent event) {
            this.lowMemory.set(true);
        }
    }
    
    public static void batchCompile (final List<ParserFile> toCompile, final FileObject rootFo, 
             ClasspathInfo cpInfo, CachingIndexer cachingIndexer, URL root,
        final Set<URI> dirtyFiles, final Set/*<? super ElementHandle<TypeElement>>*/ added,
                        Map<Language,Map<String,String>> timeStamps, Map<Language,List<File>> seenTimestampedFiles) throws IOException {
        assert toCompile != null;
        assert rootFo != null;
        assert cpInfo != null;
        ParserFile active = null;
        //final JavaFileManager fileManager = ClasspathInfoAccessor.getInstance().getFileManager(cpInfo);
        final CompilerListener listener = new CompilerListener ();        

        // Compute applicable indexers: Reduce the number of indexers to be queried during file interrogation
        List<IndexerEntry> applicableIndexers = new ArrayList<IndexerEntry>(indexers.size());
        String urlString = root.toExternalForm();
        for (IndexerEntry entry : getIndexers()) {
            if (!entry.indexer.acceptQueryPath(urlString)) {
                continue;
            }
            applicableIndexers.add(entry);
        }
        
        LowMemoryNotifier.getDefault().addLowMemoryListener(listener);
        try {
            try {
                List<ParserFile> bigFiles = new LinkedList<ParserFile>();
                int state = 0; // TODO: Document what these states mean
                boolean isBigFile = false;
          allFiles:
                while (!toCompile.isEmpty() || !bigFiles.isEmpty() || active != null) {
                    try {
                        if (listener.lowMemory.getAndSet(false)) {
                            if (state == 1) {
                                break;
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        if (active == null) {
                            if (!toCompile.isEmpty()) {
                                active = toCompile.remove(0);
                                isBigFile = false;
                            } else {
                                active = bigFiles.remove(0);
                                isBigFile = true;
                            }
                        }

                        // See 131671 for example -- this may be a file like
                        // ".#foo.rb" which is technically a Ruby file, but a shortlived
                        // one that we don't want to bother with. .# files tend to be
                        // shortlived backup files.
                        if (active.getNameExt().startsWith(".#")) { // NOI18N
                            state  = 0;
                            active = null;
                            continue;
                        }
                        
                        // Change from what's going on in the Retouche updater:
                        // We could have many language implementations that want to index this root;
                        // we need to iterate through them and let each one of them index if they
                        // want to.
                        // We're gonna do this for every file in the filesystem - do cheaper iteration
                        // using indices rather than iterators
                        for (int in = 0; in < applicableIndexers.size(); in++) {
                            IndexerEntry entry = applicableIndexers.get(in);
                            Indexer indexer = entry.getIndexer();
                            if (!indexer.isIndexable(active)) {
                                continue;
                            }

                            Language language = entry.getLanguage();
                            if (timeStamps != null) {
                                Map<String,String> ts = timeStamps.get(language);
                                if (ts != null) {
                                    File file = active.getFile();
                                    String url = indexer.getPersistentUrl(file);
                                    String timeStampString = ts.get(url);
                                    if (timeStampString != null) {

                                        // Keep track of timestamped files we've seen such
                                        // that I can delete entries that have been deleted
                                        // outside of the IDE.
                                        List<File> list = seenTimestampedFiles.get(language);
                                        if (list == null) {
                                            list = new ArrayList<File>(toCompile.size());
                                            seenTimestampedFiles.put(language, list);
                                        }
                                        list.add(file);

                                        try {
                                            long timeStamp = DateTools.stringToTime(timeStampString);
                                            if (file.lastModified() <= timeStamp) {
                                                continue;
                                            }
                                        } catch (ParseException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                }
                            }

                            ParserTaskImpl jt = new ParserTaskImpl(language);
                            jt.setParseListener(listener);
                            Iterable<ParserResult> trees = jt.parse(new ParserFile[] { active });
                            if (trees != null) {
                                if (cachingIndexer != null) {
                                    cachingIndexer.index(language, active.getFile(), trees);
                                } else {
                                    ClassIndexImpl uqImpl = ClassIndexManager.get(language).createUsagesQuery(root, true);
                                    assert uqImpl != null;
                                    SourceAnalyser sa = uqImpl.getSourceAnalyser();
                                    if (sa != null) {
                                        sa.analyse(language, trees);
                                    }
                                }
                            }
                        }
                        if (listener.lowMemory.getAndSet(false)) {
                            listener.cleanDiagnostics();
                            if (state == 1) {
                                if (isBigFile) {
                                    break;
                                } else {
                                    bigFiles.add(active);
                                    active = null;
                                    state = 0;
                                }
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        if (!listener.errors.isEmpty()) {
                            //Log.instance(jt.getContext()).nerrors = 0;
                            listener.cleanDiagnostics();
                        }
                        active = null;
                        state  = 0;
                    } catch (Throwable t) {
if (BUG_LOGGER.isLoggable(Level.FINE)) {
    BUG_LOGGER.log(Level.FINE, getElapsedTime() +"CompilerWorker *** caught exception 4 " , t);
}
                        if (PREINDEXING) {
                            Exceptions.attachMessage(t, "Parsing " + active.getFile().getPath());
                            Exceptions.printStackTrace(t);
                            Toolkit.getDefaultToolkit().beep();
                            //System.exit(-1);
                        }                        
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }
                        else {
                            String activeURI;
                            if (active != null) {
                                activeURI = active.getNameExt();
                            } else {
                                activeURI = "unknown";
                            }
                            active = null;                            
                            listener.cleanDiagnostics();
                            //if (!(t instanceof Abort)) { // a javac Throwable                                
                                final ClassPath bootPath   = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                                final ClassPath classPath  = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                                final ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                                t = Exceptions.attachMessage(t,String.format("Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s",
                                        FileUtil.getFileDisplayName(rootFo),
                                        activeURI.toString(),
                                        bootPath == null   ? null : bootPath.toString(),
                                        classPath == null  ? null : classPath.toString(),
                                        sourcePath == null ? null : sourcePath.toString()
                                        ));
                                Exceptions.printStackTrace(t);
                            //}
                        }
                    }
                }
                if (state == 1) {
                    LOGGER.warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                }
            } finally {
            }
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(listener);
        }
    }
    
    private static Set<String> parseSet(String propertyName, String defaultValue) {
        StringTokenizer st = new StringTokenizer(System.getProperty(propertyName, defaultValue), " \t\n\r\f,-:+!");
        Set<String> result = new HashSet<String>();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }
    
    
    private static ClassPath.Entry getClassPathEntry (final ClassPath cp, final URL root) {
        assert cp != null;
        assert root != null;
        for (ClassPath.Entry e : cp.entries()) {
            if (root.equals(e.getURL())) {
                return e;
            }
        }
        return null;
    }
    
    
//    private class FilterListener implements ChangeListener {
//
//        public void stateChanged(ChangeEvent event) {
//            Object source = event.getSource();
//            if (source instanceof JavaFileFilterImplementation) {
//                List<URL> dirtyRoots = new LinkedList<URL> ();
//                synchronized (filters) {
//                    for (Map.Entry<URL,JavaFileFilterImplementation> e : filters.entrySet()) {
//                        if (e.getValue() == source) {
//                            dirtyRoots.add(e.getKey());
//                        }
//                    }
//                }
//                submit(Work.filterChange(dirtyRoots));
//            }
//        }
//    }
    
    public static synchronized RepositoryUpdater getDefault () {
        if (instance == null) {
            instance = new RepositoryUpdater ();
        }
        return instance;
    }        
  
    // There could be multiple indexers (for different languages) that want
    // to index a given file. I will iterate over the indexers and let each
    // indexer have a chance to index every file. To do this I compute
    // a list of indexers in advance - and provide a place where we can
    // cache the parser tasks such that only one is created per indexer.
    
    private static List<IndexerEntry> indexers;
    
    private static List<IndexerEntry> getIndexers() {
        if (indexers == null) {
            indexers = new ArrayList<IndexerEntry>();
            for (Language language : LanguageRegistry.getInstance()) {
                Indexer indexer = language.getIndexer();
                if (indexer != null) {
                    IndexerEntry entry = new IndexerEntry(language, indexer);
                    indexers.add(entry);
                }
            }
        }
        
        return indexers;
    }
    
    private static class IndexerEntry {
        private Language language;
        private Indexer indexer;
        
        IndexerEntry(Language language, Indexer indexer) {
            this.language = language;
            this.indexer = indexer;
        }
        
        Indexer getIndexer() {
            return indexer;
        }
        
        Language getLanguage() {
            return language;
        }
    } 
}
