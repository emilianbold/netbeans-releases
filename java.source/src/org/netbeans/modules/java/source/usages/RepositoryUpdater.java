/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTaskImpl.Filter;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Abort;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.*;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.classpath.GlobalSourcePath;
import org.netbeans.modules.java.source.parsing.*;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class RepositoryUpdater implements PropertyChangeListener, FileChangeListener {
        
    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Set<String> ignoredDirectories = parseSet("org.netbeans.javacore.ignoreDirectories", "SCCS CVS .svn"); // NOI18N
    private static final boolean noscan = Boolean.getBoolean("netbeans.javacore.noscan");   //NOI18N
    public static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test");
    private static final String PACKAGE_INFO = "package-info.java";  //NOI18N
    
    private static final int DELAY = Utilities.isWindows() ? 2000 : 1000;
    
    private static RepositoryUpdater instance;
    
    private final GlobalSourcePath cpImpl;
    private final ClassPath cp;
    private final ClassPath binCp;
    private Set<URL> scannedRoots;
    private Set<URL> scannedBinaries;
    private Delay delay;
    private Work currentWork;
    private boolean dirty;
    private int noSubmited;
    
    /** Creates a new instance of RepositoryUpdater */
    private RepositoryUpdater() {
        this.delay = new Delay();
        this.cpImpl = GlobalSourcePath.getDefault();
        this.cp = ClassPathFactory.createClassPath (this.cpImpl.getSourcePath());
        this.cp.addPropertyChangeListener(this);
        this.binCp = ClassPathFactory.createClassPath(this.cpImpl.getBinaryPath());
        this.registerFileSystemListener();
        this.scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
        this.scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
        submitBatch();
    }
    
    public ClassPath getScannedSources () {
        return this.cp;
    }
    
    public ClassPath getScannedBinaries() {
        return this.binCp;
    }
    
    public void close () {                
        this.cp.removePropertyChangeListener(this);
        this.unregisterFileSystemListener();
        this.delay.cancel();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            submitBatch();
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
            if ((isJava (fo) || fo.isFolder()) && VisibilityQuery.getDefault().isVisible(fo)) {
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
        //but in Netbeans newlly created folder may
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
            if ((isJava(fo) || isFolder) && VisibilityQuery.getDefault().isVisible(fo)) {
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
            if (isJava(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
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
            if (isJava(fo) && VisibilityQuery.getDefault().isVisible(fo)) {
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
            JavaSourceAccessor.INSTANCE.runSpecialTask (cw,JavaSource.Priority.MAX);
        }
    }
    
    
    private void registerFileSystemListener  () {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fss = new HashSet<FileSystem> ();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject (root);
            if (fo == null) {
                Logger.getLogger("global").warning("No MasterFS for file system root: " + root.getAbsolutePath());          // NOI18N
            }
            else {
                try {
                    final FileSystem fs = fo.getFileSystem();
                    if (!fss.contains(fs)) {
                        fs.addFileChangeListener (this);
                        fss.add(fs);
                    }
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
    
    private void unregisterFileSystemListener () {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fss = new HashSet<FileSystem> ();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject (root);
            if (fo == null) {
                Logger.getLogger("global").warning("No MasterFS for file system root: " + root.getAbsolutePath());          // NOI18N
            }
            else {
                try {
                    final FileSystem fs = fo.getFileSystem();
                    if (!fss.contains(fs)) {
                        fs.removeFileChangeListener (this);
                        fss.add(fs);
                    }
                } catch (FileStateInvalidException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }
    
    private URL getOwningSourceRoot (final FileObject fo) {
        if (fo == null) {
            return null;
        }
        synchronized (this.scannedRoots) {
            for (URL root : this.scannedRoots) {
                FileObject rootFo = URLMapper.findFileObject(root);
                if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                    return root;
                }
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
    private static boolean isJava (final FileObject fo) {
        if (fo.isFolder()) {
            return false;
        }
        else if (JavaDataLoader.JAVA_EXTENSION.equals(fo.getExt().toLowerCase())) {
            return true;
        }    
        else {
            return JavaDataLoader.JAVA_MIME_TYPE.equals(fo.getMIMEType());
        }
    }
    
    private static boolean isBinary (final FileObject fo) {
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
    }
    
    private static enum WorkType {
        COMPILE_BATCH, COMPILE_CONT, COMPILE, DELETE, UPDATE_BINARY
    };
    
    
    private final static class Work {
        private final WorkType workType;
        private final URL file;
        private final URL root;
        private final boolean isFolder;
        private final CountDownLatch latch;
                        
        private Work (final WorkType type, final URL file, final URL root, final boolean isFolder, CountDownLatch latch) {
            this.workType = type;
            this.file = file;
            this.root = root;
            this.isFolder = isFolder;
            this.latch = latch;
        }
        
        private Work (final WorkType type) {
            assert type == WorkType.COMPILE_BATCH || type == WorkType.COMPILE_CONT;
            this.workType = type;
            this.file = null;
            this.root = null;
            this.isFolder = false;
            this.latch = null;
        }
        
        public WorkType getType () {
            return this.workType;
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
        
        public void finished () {
            if (this.latch != null) {
                this.latch.countDown();
            }
        }                
        
        public static Work batch () {
            return new Work (WorkType.COMPILE_BATCH);
        }
        
        public static Work compile (final FileObject file, final URL root) throws FileStateInvalidException {
            return compile (file.getURL(), root, file.isFolder());
        }
        
        public static Work compile (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new Work (WorkType.COMPILE, file, root, isFolder, null);
        }
        
        public static Work compile (final FileObject file, final URL root, CountDownLatch[] latch) throws FileStateInvalidException {
            assert file != null && root != null;
            assert latch != null && latch.length == 1 && latch[0] == null;
            latch[0] = new CountDownLatch (1);
            return new Work (WorkType.COMPILE, file.getURL(), root, file.isFolder(),latch[0]);
        }
        
        public static Work delete (final FileObject file, final URL root, final boolean isFolder) throws FileStateInvalidException {
            return delete (file.getURL(), root,file.isFolder());
        }
        
        public static Work delete (final URL file, final URL root, final boolean isFolder) {
            assert file != null && root != null;
            return new Work (WorkType.DELETE, file, root, isFolder,null);
        }
        
        public static Work binary (final FileObject file, final URL root) throws FileStateInvalidException {
            return binary (file.getURL(), root, file.isFolder());
        }
        
        public static Work binary (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new Work (WorkType.UPDATE_BINARY, file, root, isFolder, null);
        }
        
    };        
    
    private final class CompileWorker implements CancellableTask<CompilationInfo> {
                
        private Work work;
        private List<URL> state;
        private Set<URL> oldRoots;
        private Set<URL> oldBinaries;
        private Set<URL> newBinaries;
        private ProgressHandle handle;
        private final AtomicBoolean canceled;
        
        public CompileWorker (Work work ) {
            assert work != null;
            this.work = work;
            this.canceled = new AtomicBoolean (false);
        }
        
        public void cancel () {            
            this.canceled.set(true);
        }
        
        public void run (final CompilationInfo nullInfo) throws IOException {
            ClassIndexManager.getDefault().writeLock (new ClassIndexManager.ExceptionAction<Void> () {
                public Void run () throws IOException {
                    boolean continuation = false;
                    try {
                    final WorkType type = work.getType();                        
                    switch (type) {
                        case COMPILE_BATCH:
                        {
                            assert handle == null;
                            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                            handle.start();
                            boolean completed = false;
                            try {
                                oldRoots = new HashSet<URL> (scannedRoots);
                                oldBinaries = new HashSet<URL> (scannedBinaries);
                                final List<ClassPath.Entry> entries = cp.entries();                                                                                                
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
                                    final URL rootURL = entry.getURL();
                                    findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries);
                                }                                
                                CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                completed = true;
                            } catch (final TopologicalSortException tse) {
                                final IllegalStateException ise = new IllegalStateException ();                                
                                throw (IllegalStateException) ise.initCause(tse);
                            } finally {
                                if (!completed) {
                                    resetDirty();
                                }
                            }
                        }
                        case COMPILE_CONT:
                            boolean completed = false;
                            try {
                                if (!scanRoots()) {
                                    CompileWorker.this.work = new Work (WorkType.COMPILE_CONT);
                                    JavaSourceAccessor.INSTANCE.runSpecialTask (CompileWorker.this,JavaSource.Priority.MAX);
                                    continuation = true;
                                    return null;
                                }
                                while (isDirty()) {
                                    assert CompileWorker.this.state.isEmpty();
                                    final List<ClassPath.Entry> entries = cp.entries();
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
                                        final URL rootURL = entry.getURL();
                                        findDependencies (rootURL, new Stack<URL>(), depGraph, newBinaries);
                                    }
                                    try {
                                        CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                    } catch (final TopologicalSortException tse) {
                                        final IllegalStateException ise = new IllegalStateException ();
                                        throw (IllegalStateException) ise.initCause(tse);
                                    }
                                    if (!scanRoots ()) {
                                        CompileWorker.this.work = new Work (WorkType.COMPILE_CONT);
                                        JavaSourceAccessor.INSTANCE.runSpecialTask (CompileWorker.this,JavaSource.Priority.MAX);
                                        continuation = true;
                                        return null;
                                    }
                                }
                                completed = true;
                            } finally {
                                if (!completed && !continuation) {
                                    resetDirty ();
                                }
                            }                            
                            final ClassIndexManager cim = ClassIndexManager.getDefault();
                            scannedRoots.removeAll(oldRoots);
                            for (URL oldRoot : oldRoots) {
                                cim.removeRoot(oldRoot);
                            }
                            scannedBinaries.removeAll (oldBinaries);
                            final CachingArchiveProvider cap = CachingArchiveProvider.getDefault();
                            for (URL oldRoot : oldBinaries) {
                                cim.removeRoot(oldRoot);
                                cap.removeArchive(FileObjects.getRootFile(oldRoot));
                            }
                            break;
                        case COMPILE:
                        {
                            try {
                                final URL file = work.getFile();
                                final URL root = work.getRoot ();
                                if (work.isFolder()) {
                                    updateFolder (file, root, handle);
                                }
                                else {
                                    updateFile (file,root);
                                }
                            } catch (Abort abort) {
                                //Ignore abort
                            }                         
                            break;
                        }
                        case DELETE:
                        {
                            final URL file = work.getFile();
                            final URL root = work.getRoot ();
                            delete (file, root, work.isFolder());
                            break;                                 
                        }
                        case UPDATE_BINARY:
                        {
                            final URL file = work.getFile();
                            final URL root = work.getRoot();
                            updateBinary (file, root);
                            break;
                        }
                    }                                                
                    return null;                    
                } finally {
                    if (!continuation) {
                        synchronized (RepositoryUpdater.this) {
                            RepositoryUpdater.this.noSubmited--;
                            if (RepositoryUpdater.this.noSubmited == 0) {
                                RepositoryUpdater.this.notifyAll();
                            }
                        }
                        work.finished ();
                        if (handle != null) {
                            handle.finish ();
                        }
                    }
                }
            }});
        }
        
        private void findDependencies (final URL rootURL, final Stack<URL> cycleDetector, final Map<URL,List<URL>> depGraph, final Set<URL> binaries) {           
            if (RepositoryUpdater.this.scannedRoots.contains(rootURL)) {                                
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
            cycleDetector.push (rootURL);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
            final ClassPath[] pathsToResolve = new ClassPath[] {bootPath,compilePath};
            final List<URL> deps = new LinkedList<URL> ();
            for (int i=0; i< pathsToResolve.length; i++) {
                final ClassPath pathToResolve = pathsToResolve[i];
                if (pathToResolve != null) {
                    for (ClassPath.Entry entry : pathToResolve.entries()) {
                        final URL url = entry.getURL();
                        final URL[] sourceRoots = RepositoryUpdater.this.cpImpl.getSourceRootForBinaryRoot(url, pathToResolve, false);
                        if (sourceRoots != null) {
                            for (URL sourceRoot : sourceRoots) {
                                if (!cycleDetector.contains(sourceRoot)) {
                                    deps.add (sourceRoot);
                                    findDependencies(sourceRoot, cycleDetector,depGraph, binaries);
                                }
                            }
                        }
                        else {
                            if (!RepositoryUpdater.this.scannedBinaries.contains(url)) {
                                binaries.add (url);
                            }
                            oldBinaries.remove(url);
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
                try {
                    final URL rootURL = it.next();
                    it.remove();
                    final ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(rootURL,false);
                    final File rootFile = FileObjects.getRootFile(rootURL);
                    final String message = String.format (NbBundle.getMessage(RepositoryUpdater.class,"MSG_Scannig"),rootFile.getAbsolutePath());
                    handle.setDisplayName(message);
                    RepositoryUpdater.this.scannedBinaries.add (rootURL);
                    if (rootFile.canRead()) {                        
                        long startT = System.currentTimeMillis();
                        ci.getBinaryAnalyser().analyse(rootFile, handle);
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
                    }
                } catch (Throwable e) {
                    if (e instanceof ThreadDeath) {
                        throw (ThreadDeath) e;
                    }
                    else {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
            for (java.util.ListIterator<URL> it = this.state.listIterator(this.state.size()); it.hasPrevious(); ) {
                if (this.canceled.getAndSet(false)) {                    
                    return false;
                }
                try {
                    final URL rootURL = it.previous();
                    it.remove();                                                                                
                    if (!oldRoots.remove(rootURL)) {
                        long startT = System.currentTimeMillis();
                        updateFolder (rootURL,rootURL, handle);
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
                    }
                } catch (Throwable e) {
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
        
        private void updateFolder(final URL folder, final URL root, final ProgressHandle handle) throws IOException {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }            
            if (!rootFo.isFolder()) {
                Logger.getLogger("global").warning("Source root has to be a folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }            
            final ClassPath sourcePath = ClassPath.getClassPath(rootFo,ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);            
            final boolean isInitialCompilation = folder.equals(root);            
            if (sourcePath == null || bootPath == null || compilePath == null) {
                Logger.getLogger("global").warning("Ignoring root with no ClassPath: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                return;
            }
            if (isInitialCompilation) {
                //Initial compilation  debug messages
                if (RepositoryUpdater.this.scannedRoots.contains(root)) {
                    return;
                }                
                LOGGER.fine("Scanning Root: " + FileUtil.getFileDisplayName(rootFo));    //NOI18N
            }
            try {                                
                final File rootFile = FileUtil.toFile(rootFo);
                final File folderFile = isInitialCompilation ? rootFile : FileUtil.normalizeFile(new File (URI.create(folder.toExternalForm())));
                if (handle != null) {
                    final String message = String.format (NbBundle.getMessage(RepositoryUpdater.class,"MSG_Scannig"),rootFile.getAbsolutePath());
                    handle.setDisplayName(message);
                }
                final ClasspathInfo cpInfo = ClasspathInfoAccessor.INSTANCE.create(CacheClassPath.forClassPath(bootPath),CacheClassPath.forClassPath(compilePath),sourcePath,isInitialCompilation);
                List<JavaFileObject> toCompile = new LinkedList<JavaFileObject>();
                final File classCache = Index.getClassFolder(rootFile);
                final Map <String,List<File>> resources = getAllClassFiles(classCache, FileObjects.getRelativePath(rootFile,folderFile),true);
                final LazyFileList children = new LazyFileList(folderFile);
                ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
                assert uqImpl != null;
                SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                Set<File> rs = new HashSet<File> ();
                for (File child : children) {                    
                    String offset = FileObjects.getRelativePath(rootFile,child);
                    final int index = offset.lastIndexOf('.');  //NOI18N
                    if (index > -1) {
                        offset = offset.substring(0,index);
                    }
                    List<File> files = resources.remove(offset);
                    if  (files==null) {
                        toCompile.add(FileObjects.fileFileObject(child, rootFile));
                    } else {
                        boolean rsf = files.get(0).getName().endsWith(FileObjects.RS);
                        if (files.get(0).lastModified() < child.lastModified()) {
                            toCompile.add(FileObjects.fileFileObject(child, rootFile));
                            for (File toDelete : files) {                            
                                toDelete.delete();
                                if (rsf) {                                    
                                    rsf = false;
                                }
                                else {
                                    String className = FileObjects.getBinaryName(toDelete,classCache);
                                    sa.delete(className);
                                }                            
                            }
                        }
                        else if (rsf) {
                            files.remove(0);
                            rs.addAll(files);
                        }                        
                    }
                }
                for (List<File> files : resources.values()) {
                    for (File toDelete : files) {
                        if (!rs.contains(toDelete)) {
                            toDelete.delete();
                            if (toDelete.getName().endsWith(FileObjects.SIG)) {
                                String className = FileObjects.getBinaryName(toDelete,classCache);                        
                                sa.delete(className);
                            }
                        }
                    }
                }
                if (!toCompile.isEmpty()) {
                    if (handle != null) {
                        final String message = String.format (NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompile"),rootFile.getAbsolutePath());
                        handle.setDisplayName(message);
                    }
                    batchCompile(toCompile, rootFo, cpInfo, sa);
                }
                sa.store();
            } finally {
                if (isInitialCompilation) {
                    RepositoryUpdater.this.scannedRoots.add(root);
                }
            }
        }
        
        private void updateFile (final URL file, final URL root) throws IOException {
            final FileObject fo = URLMapper.findFileObject(file);
            if (fo == null) {
                return;
            }
            
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
            if (uqImpl != null) {
                uqImpl.setDirty(null);
                ClasspathInfo cpInfo = ClasspathInfo.create (fo);
                final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
                final File fileFile = FileUtil.toFile(fo);
                final File classCache = Index.getClassFolder (rootFile);
                final Map <String,List<File>> resources = getAllClassFiles (classCache, FileObjects.getRelativePath(rootFile, fileFile.getParentFile()),false);
                String offset = FileObjects.getRelativePath (rootFile,fileFile);
                final int index = offset.lastIndexOf('.');  //NOI18N
                if (index > -1) {
                    offset = offset.substring(0,index);
                }
                List<File> files = resources.remove (offset);                
                SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                if (files != null) {
                    for (File toDelete : files) {
                        toDelete.delete();
                        if (toDelete.getName().endsWith(FileObjects.SIG)) {
                            String className = FileObjects.getBinaryName (toDelete,classCache);                                                           
                            sa.delete (className);
                        }
                    }
                }
                else {
                    sa.delete(FileObjects.convertFolder2Package(offset, '/'));  //NOI18N
                }
                assert fo != null;
                String sourceLevel = SourceLevelQuery.getSourceLevel(fo);
                final CompilerListener listener = new CompilerListener ();
                final JavaFileManager fm = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);
                JavaFileObject active = SourceFileObject.create(fo);
                JavacTaskImpl jt = JavaSourceAccessor.INSTANCE.createJavacTask(cpInfo, listener, sourceLevel);
                jt.setTaskListener(listener);
                Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {active});
                jt.enter();            
                jt.analyze ();
                dumpClasses(listener.getEnteredTypes(), fm, com.sun.tools.javac.code.Types.instance(jt.getContext()),
                    com.sun.tools.javac.util.Name.Table.instance(jt.getContext()));
                sa.analyse (trees, jt, fm, active);
                listener.cleanDiagnostics();
                sa.store();
            }
        }
        
        private void delete (final URL file, final URL root, final boolean folder) throws IOException {
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
            assert "file".equals(file.getProtocol()) : "Unexpected protocol of URL: " + file;   //NOI18N
            final File fileFile = FileUtil.normalizeFile(new File (URI.create(file.toExternalForm())));
            final String offset = FileObjects.getRelativePath (rootFile,fileFile);
            assert offset != null && offset.length() > 0 : String.format("File %s not under root %s ", fileFile.getAbsolutePath(), rootFile.getAbsolutePath());  // NOI18N                        
            final File classCache = Index.getClassFolder (rootFile);
            File[] affectedFiles = null;
            if (folder) {
                final File container = new File (classCache, offset);
                affectedFiles = container.listFiles();
            }
            else {
                int slashIndex = offset.lastIndexOf (File.separatorChar);
                int dotIndex = offset.lastIndexOf('.');     //NOI18N
                final File container = slashIndex == -1 ? classCache : new File (classCache,offset.substring(0,slashIndex));
                final String name = offset.substring(slashIndex+1, dotIndex);
                final String[] patterns = new String[] {
                  name + '.',
                  name + '$'
                };
                final File[] content  = container.listFiles();
                if (content != null) {
                    final List<File> result = new ArrayList<File>(content.length);
                    for (File f : content) {
                        final String fname = f.getName();
                        if (fname.startsWith(patterns[0]) || fname.startsWith(patterns[1])) {
                            result.add(f);
                        }
                    }
                    affectedFiles = result.toArray(new File[result.size()]);
                }
            }
            if (affectedFiles != null && affectedFiles.length > 0) {
                final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
                assert uqImpl != null;                
                final SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                for (File f : affectedFiles) {
                    if (f.getName().endsWith(FileObjects.RS)) {
                        List<File> rsFiles = new LinkedList<File>();
                        readRSFile(f, classCache, rsFiles);
                        for (File rsf : rsFiles) {
                            String className = FileObjects.getBinaryName (rsf,classCache);                                                                        
                            sa.delete (className);
                            rsf.delete();
                        }
                    }
                    else {
                        String className = FileObjects.getBinaryName (f,classCache);                                                                        
                        sa.delete (className);
                    }
                    f.delete();                    
                }
                sa.store();
            }
        }
        
        private void updateBinary (final URL file, final URL root) throws IOException {
            File rootFile = FileObjects.getRootFile(root);
            CachingArchiveProvider.getDefault().clearArchive(rootFile);            
            if (rootFile.exists()) {
                final BinaryAnalyser ba = ClassIndexManager.getDefault().createUsagesQuery(root, false).getBinaryAnalyser();
                ba.analyse(rootFile, handle);
            }
        }                
    }        
    
    static class LazyFileList implements Iterable<File> {
    
        private File root;

        public LazyFileList (final File root) {
            assert root != null && root.isDirectory();
            this.root = root;
        }

        public Iterator<File> iterator() {
            return new It (this.root);
        }


        private class It implements Iterator<File> {

            private final List<File> toDo = new LinkedList<File> ();

            public It (File root) {
                this.toDo.add (root);
            }

            public boolean hasNext() {
                while (!toDo.isEmpty()) {
                    File f = toDo.remove (0);   
                    final String name = f.getName();
                    if (f.isDirectory() && !ignoredDirectories.contains(name) && Utilities.isJavaIdentifier(name)) {
                        File[] content = f.listFiles();
                            for (int i=0,j=0;i<content.length;i++) {
                                f = content[i];
                                if (f.isFile()) {
                                    this.toDo.add(j++,f);
                                }
                                else {
                                    this.toDo.add(f);
                                }
                            }
                    }                    
                    else if (name.endsWith('.'+JavaDataLoader.JAVA_EXTENSION) && !PACKAGE_INFO.equals(name) && f.length()>0) { //NOI18N
                        toDo.add(0,f);
                        return true;
                    }                                        
                }
                return false;
            }

            public File next() {
                return toDo.remove (0);
            }

            public void remove() {
                throw new UnsupportedOperationException ();
            }

        }            
    }
    
    
    private final class Delay {
        
        private final Timer timer;
        private final List<Work> tasks;
        
        public Delay () {
            this.timer = new Timer ();
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
                    w = new Work (WorkType.DELETE,w.file,w.root,w.isFolder,w.latch);
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
    
    private static class CompilerListener implements DiagnosticListener<JavaFileObject>, LowMemoryListener, TaskListener {
                               
        final List<Diagnostic> errors = new LinkedList<Diagnostic> ();
        final List<Diagnostic> warnings = new LinkedList<Diagnostic> ();
        final List<ClassSymbol> justEntered = new LinkedList<ClassSymbol> ();
        final AtomicBoolean lowMemory = new AtomicBoolean ();
        
        void cleanDiagnostics () {
            if (!this.errors.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Diagnostic msg : this.errors) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.errors.clear();
            }
            if (!this.warnings.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    for (Diagnostic msg: this.warnings) {
                        LOGGER.fine(msg.toString());      //NOI18N
                    }
                }
                this.warnings.clear();
            }
            this.justEntered.clear();
        }
        
        List<? extends ClassSymbol> getEnteredTypes () {
            List<ClassSymbol> result = new ArrayList(this.justEntered);
            this.justEntered.clear();
            return result;
        }
        
        

        public void report(final Diagnostic diagnosticMessage) {
            Diagnostic.Kind kind = diagnosticMessage.getKind();
            if ( kind == Diagnostic.Kind.ERROR) {
                this.errors.add (diagnosticMessage);
            }
            else if (kind == Diagnostic.Kind.WARNING
                   ||kind == Diagnostic.Kind.MANDATORY_WARNING) {
                this.warnings.add (diagnosticMessage);
            }
        }
        
        public void started(TaskEvent event) {        
        }        
        
        public void finished(final TaskEvent event) {
            if (event.getKind() == TaskEvent.Kind.ENTER) {                
                final CompilationUnitTree unit = event.getCompilationUnit();
                for (Tree typeTree : unit.getTypeDecls()) {
                    if (typeTree instanceof JCTree.JCClassDecl) {       //May be a JCTree.JCExpressionStatement in case of an error
                        ClassSymbol sym = ((JCTree.JCClassDecl)typeTree).sym;
                        if (sym != null) {
                            if (sym.sourcefile == null) {
                                sym.sourcefile = event.getSourceFile();
                            }
                            this.justEntered.add(sym);
                        }
                    }          
                }
            }
        }
        
        public void lowMemory (final LowMemoryEvent event) {
            this.lowMemory.set(true);
        }
    }
    
    public static void batchCompile (final List<JavaFileObject> toCompile, final FileObject rootFo, final ClasspathInfo cpInfo, final SourceAnalyser sa) throws IOException {
        assert toCompile != null;
        assert rootFo != null;
        assert cpInfo != null;
        JavaFileObject active = null;
        final JavaFileManager fileManager = ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo);
        final CompilerListener listener = new CompilerListener ();        
        LowMemoryNotifier.getDefault().addLowMemoryListener(listener);
        try {
            JavacTaskImpl jt = null;
            try {                
                List<JavaFileObject> bigFiles = new LinkedList<JavaFileObject>();
                int state = 0;
                boolean isBigFile = false;
                final String sourceLevel = SourceLevelQuery.getSourceLevel(rootFo);
                while (!toCompile.isEmpty() || !bigFiles.isEmpty() || active != null) {
                    try {
                        if (listener.lowMemory.getAndSet(false)) {
                            if (jt != null) {
                                jt.finish();
                            }
                            jt = null;
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
                        if (jt == null) {
                            jt = JavaSourceAccessor.INSTANCE.createJavacTask(cpInfo, listener, sourceLevel);
                            jt.setTaskListener(listener);
                            LOGGER.fine("Created new JavacTask for: " + FileUtil.getFileDisplayName(rootFo));    //NOI18N
                        }
                        Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {active});
                        if (listener.lowMemory.getAndSet(false)) {
                            jt.finish();
                            jt = null;
                            listener.cleanDiagnostics();
                            trees = null;
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
                        Iterable<? extends TypeElement> types = jt.enterTrees(trees);
                        dumpClasses (listener.getEnteredTypes(),fileManager, 
                                com.sun.tools.javac.code.Types.instance(jt.getContext()),
                                com.sun.tools.javac.util.Name.Table.instance(jt.getContext()));
                        if (listener.lowMemory.getAndSet(false)) {
                            jt.finish();
                            jt = null;
                            listener.cleanDiagnostics();
                            trees = null;
                            types = null;
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
                        final JavaCompiler jc = JavaCompiler.instance(jt.getContext());
                        final JavaFileObject finalActive = active;
                        Filter f = new Filter() {
                            public void process(Env<AttrContext> env) {
                                try {
                                    jc.attribute(env);
                                } catch (Throwable t) {
                                    if (finalActive.toUri().getPath().contains("org/openide/loaders/OpenSupport.java")) {
                                        Exceptions.printStackTrace(t);
                                    }
                                }
                            }
                        };
                        f.run(jc.todo, types);
                        dumpClasses (listener.getEnteredTypes(), fileManager,
                                com.sun.tools.javac.code.Types.instance(jt.getContext()),
                                com.sun.tools.javac.util.Name.Table.instance(jt.getContext()));
                        if (listener.lowMemory.getAndSet(false)) {
                            jt.finish();
                            jt = null;
                            listener.cleanDiagnostics();
                            trees = null;
                            types = null;
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
                        if (sa != null) {
                            sa.analyse(trees,jt, ClasspathInfoAccessor.INSTANCE.getFileManager(cpInfo), active);
                        }                                        
                        if (!listener.errors.isEmpty()) {
                            Log.instance(jt.getContext()).nerrors = 0;
                            listener.cleanDiagnostics();
                        }
                        active = null;
                        state  = 0;
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }
                        else {
                            if (jt != null) {
                                jt.finish();
                            }
                            final URI activeURI = active.toUri();
                            jt = null;
                            active = null;                            
                            listener.cleanDiagnostics();
                            if (!(t instanceof Abort)) {                                
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
                            }
                        }
                    }
                }
                if (state == 1) {
                    Logger.getLogger("global").warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                }
            } finally {
                if (jt != null) {
                    jt.finish();
                }
            }
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(listener);
        }
    }
    
    
    private static void dumpClasses (final List<? extends ClassSymbol> entered, final JavaFileManager fileManager, final com.sun.tools.javac.code.Types javacTypes,
        final com.sun.tools.javac.util.Name.Table nameTable) throws IOException {
        for (ClassSymbol classSym : entered) {
            JavaFileObject source = classSym.sourcefile;            
            dumpTopLevel(classSym, fileManager, source, javacTypes, nameTable);
        }
    }
    
    private static void dumpTopLevel (final ClassSymbol classSym, final JavaFileManager fileManager, final JavaFileObject source, final com.sun.tools.javac.code.Types types,
        com.sun.tools.javac.util.Name.Table nameTable) throws IOException {
        if (classSym.getSimpleName() != nameTable.error) {
            final String sourceName = fileManager.inferBinaryName(StandardLocation.SOURCE_PATH, source);
            final StringBuilder classNameBuilder = new StringBuilder ();
            ClassFileUtil.encodeClassName(classSym, classNameBuilder, '.');  //NOI18N
            final String binaryName = classNameBuilder.toString();
            Set<String> rsList = null;
            if (!sourceName.equals(binaryName)) {            
                rsList = new HashSet<String>();
            }
            final JavaFileObject fobj = fileManager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, binaryName, JavaFileObject.Kind.CLASS, source);
            final PrintWriter out = new PrintWriter (new OutputStreamWriter(fobj.openOutputStream()));
            try {               
                SymbolDumper.dump(out,types,classSym,null);
            } finally {
                out.close();
            }
            if (rsList != null) {
                rsList.add(binaryName);
            }
            final List<Symbol> enclosedElements = classSym.getEnclosedElements();
            for (Symbol ee : enclosedElements) {
                if (ee.getKind().isClass() || ee.getKind().isInterface()) {
                    dumpClass ((ClassSymbol)ee,fileManager, source, types, nameTable, rsList);
                }
            }
            if (rsList != null) {
                final int index = sourceName.lastIndexOf('.');              //NOI18N
                final String pkg = index == -1 ? "" : sourceName.substring(0,index);    //NOI18N
                final String rsName = (index == -1 ? sourceName : sourceName.substring(index+1)) + '.' + FileObjects.RS;    //NOI18N
                javax.tools.FileObject fo = fileManager.getFileForOutput(StandardLocation.CLASS_OUTPUT, pkg, rsName, source);
                assert fo != null;
                PrintWriter rsOut = new PrintWriter(fo.openWriter());
                try {
                    for (String sig : rsList) {
                        rsOut.println(sig);                    
                    }
                } finally {
                    rsOut.close();
                }
            }
        }
    }
            
    private static void dumpClass (final ClassSymbol classSym, final JavaFileManager fileManager, final JavaFileObject source, final com.sun.tools.javac.code.Types types,
            final com.sun.tools.javac.util.Name.Table nameTable, final Set<? super String> rsList) throws IOException {
        if (classSym.getSimpleName() != nameTable.error) {
            final StringBuilder classNameBuilder = new StringBuilder ();
            ClassFileUtil.encodeClassName(classSym, classNameBuilder, '.');  //NOI18N
            final String binaryName = classNameBuilder.toString();
            final JavaFileObject fobj = fileManager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, binaryName, JavaFileObject.Kind.CLASS, source);
            final PrintWriter out = new PrintWriter (new OutputStreamWriter(fobj.openOutputStream()));
            try {               
                SymbolDumper.dump(out,types,classSym,null);
            } finally {
                out.close();
            }
            if (rsList != null) {
                rsList.add(binaryName);
            }
            final List<Symbol> enclosedElements = classSym.getEnclosedElements();
            for (Symbol ee : enclosedElements) {
                if (ee.getKind().isClass() || ee.getKind().isInterface()) {
                    dumpClass ((ClassSymbol)ee,fileManager, source, types, nameTable, rsList);
                }
            }
        }
    }
    
    private static HashSet parseSet(String propertyName, String defaultValue) {
        StringTokenizer st = new StringTokenizer(System.getProperty(propertyName, defaultValue), " \t\n\r\f,-:+!");
        HashSet result = new HashSet();
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }
    
    
    public static Map<String,List<File>> getAllClassFiles (final File root, final String offset, boolean recursive) {
        assert root != null;
        Map<String,List<File>> result = new HashMap<String,List<File>> ();
        String rootName = root.getAbsolutePath();
        int len = rootName.length();
        if (rootName.charAt(len-1)!=File.separatorChar) {
            len++;
        }
        File folder = root;
        if (offset.length() > 0) {
            folder = new File (folder,offset);  //NOI18N
            if (!folder.exists() || !folder.isDirectory()) {
                return result;
            }
        }
        getAllClassFilesImpl (folder, root,len,result, recursive);
        return result;
    }
        
    private static void getAllClassFilesImpl (final File folder, final File root, final int oi, final Map<String,List<File>> result, final boolean recursive) {
        final File[] content = folder.listFiles();
        for (File f: content) {
            if (f.isDirectory() && recursive) {
                getAllClassFilesImpl(f, root, oi,result, recursive);                    
            }
            else {
                String path = f.getAbsolutePath();
                int extIndex = path.lastIndexOf('.');  //NO18N
                if (extIndex+1+FileObjects.RS.length() == path.length() && path.endsWith(FileObjects.RS)) {
                    path = path.substring (oi,extIndex);
                    List<File> files = result.get (path);
                    if (files == null) {
                        files = new LinkedList<File>();
                        result.put (path,files);
                    }
                    files.add(0,f); //the rs file has to be the first
                    try {
                        readRSFile (f,root, files);
                    } catch (IOException ioe) {
                        //The signature file is broken, report it but don't stop scanning
                        Exceptions.printStackTrace(ioe);
                    }
                }
                else if (extIndex+1+FileObjects.SIG.length() == path.length() && path.endsWith(FileObjects.SIG)) {
                    int index = path.indexOf('$',oi);  //NOI18N                    
                    if (index == -1) {
                        path = path.substring (oi,extIndex);
                    }
                    else {
                        path = path.substring (oi,index);
                    }                    
                    List<File> files = result.get (path);
                    if (files == null) {
                        files = new LinkedList<File>();
                        result.put (path,files);
                    }
                    files.add (f);
                }
            }
        }
    }
        
    private static void readRSFile (final File f, final File root, final List<? super File> files) throws IOException {
        BufferedReader in = new BufferedReader (new FileReader (f));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                File sf = new File (root, FileObjects.convertPackage2Folder(binaryName)+'.'+FileObjects.SIG);
                files.add(sf);                                        
            }
        } finally {
            in.close();
        }
    }
    
    public static synchronized RepositoryUpdater getDefault () {
        if (instance == null) {
            instance = new RepositoryUpdater ();
        }
        return instance;
    }        
    
}
