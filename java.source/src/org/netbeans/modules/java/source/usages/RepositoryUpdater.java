/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.MissingPlatformError;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TooManyListenersException;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.*;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.classpath.GlobalSourcePath;
import org.netbeans.modules.java.source.parsing.*;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.tasklist.ErrorAnnotator;
import org.netbeans.modules.java.source.tasklist.JavaTaskProvider;
import org.netbeans.modules.java.source.tasklist.RebuildOraculum;
import org.netbeans.modules.java.source.tasklist.TaskCache;
import org.netbeans.modules.java.source.tasklist.TasklistSettings;
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
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class RepositoryUpdater implements PropertyChangeListener, FileChangeListener, ClassPathRootsListener.ClassPathRootsChangedListener {
        
    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());
    private static final Logger ACTIVITY_LOGGER = Logger.getLogger(RepositoryUpdater.class.getName()+".activity");  //NOI18N
    private static final Set<String> warnedIgnoredRoots = Collections.synchronizedSet(new HashSet<String>());
    private static final Set<String> ignoredDirectories = parseSet("org.netbeans.javacore.ignoreDirectories", "SCCS CVS .svn"); // NOI18N
    private static final boolean noscan = Boolean.getBoolean("netbeans.javacore.noscan");   //NOI18N
    private static final boolean PERF_TEST = Boolean.getBoolean("perf.refactoring.test");
    static final String GOING_TO_RECOMPILE = "Going to recompile: {0}"; //NOI18N
    static final String CONTAINS_TASKLIST_DATA = "containsTasklistData"; //NOI18N
    static final String CONTAINS_TASKLIST_DEPENDENCY_DATA = "containsTasklistDependencyData"; //NOI18N
    static final String DIRTY_ROOT = "dirty"; //NOI18N
    static final String SOURCE_LEVEL_ROOT = "sourceLevel"; //NOI18N
    static final String EXTRA_COMPILER_OPTIONS = "extraCompilerOptions"; //NOI18N
    static final String CLASSPATH_ATTRIBUTE = "classPath"; //NOI18N
    static final String DIGEST = "digest"; //NOI18N
    
    //non-final, non-private for tests...
    static int DELAY = Utilities.isWindows() ? 2000 : 1000;
    
    private static RepositoryUpdater instance;
    
    private final GlobalSourcePath cpImpl;
    private final ClassPath cp;
    private final ClassPath ucp;
    private final ClassPath binCp;
    private Set<URL> scannedRoots;
    private Set<URL> scannedBinaries;
    private Map<URL,List<URL>> deps;        //todo: may be shared with scannedRoots, may save some HashMap.Entry
    private static final RequestProcessor WORKER = new RequestProcessor(RepositoryUpdater.class.getName(),1);
    private Work currentWork;
    private boolean dirty;
    private volatile int noSubmited;
    private volatile boolean notInitialized;         //Transient state during IDE start
    private final AtomicBoolean closed;
    private Map<ClassPath, Set<URL>> classPath2Roots;
    
    //Preprocessor support
    private final Map<URL, JavaFileFilterImplementation> filters = Collections.synchronizedMap(new HashMap<URL, JavaFileFilterImplementation>());
    private final FilterListener filterListener = new FilterListener ();
    
    //recompile support:
    private final Map<URL, Collection<File>> url2Recompile = new HashMap<URL, Collection<File>>();
    private boolean recompileScheduled;
    private boolean recompileToBeScheduled;
    private final Set<URL> recompileFilesWithErrors = new HashSet<URL>();
    private boolean recompileFilesWithErrorsScheduled;
    private boolean recompileFilesWithErrorsToBeScheduled;
    private final Map<URL, Collection<File>> url2CompileWithDeps = new HashMap<URL, Collection<File>>();
    private final Map<URL, Reference<Task>> url2CompileWithDepsTask = new HashMap<URL, Reference<Task>>();
    private final Set<URL> rootsWithVirtualSource = new HashSet<URL>();
    private boolean compileWithDepsToBeScheduled;
    private int compileScheduled;
    
    private int lockRU;

    private final Map<URL, String> root2DebugData = new HashMap<URL, String>();
    
    /** Creates a new instance of RepositoryUpdater */
    private RepositoryUpdater() {        
        notInitialized = true;
        this.closed = new AtomicBoolean (false);
        this.scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
        this.scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());            
        this.deps = Collections.synchronizedMap(new HashMap<URL,List<URL>>());
        this.cpImpl = GlobalSourcePath.getDefault();            
        this.cp = ClassPathFactory.createClassPath (this.cpImpl.getSourcePath());
        this.ucp = ClassPathFactory.createClassPath (this.cpImpl.getUnknownSourcePath());
        this.binCp = ClassPathFactory.createClassPath(this.cpImpl.getBinaryPath());
        this.classPath2Roots = Collections.synchronizedMap(new WeakHashMap<ClassPath, Set<URL>>());
        this.open ();
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

    private synchronized void open () throws IllegalStateException {
        if (notInitialized) {
            try {
                this.cpImpl.setExcludesListener (this);
                this.cp.addPropertyChangeListener(this);
                this.registerFileSystemListener();
                submitBatch();
                notInitialized = false;
            } catch (TooManyListenersException e) {
                throw new IllegalStateException ();
            }
        }
    }
    
    public void close () {
        try {
            this.closed.set(true);
            this.cpImpl.setExcludesListener(null);       
            this.cp.removePropertyChangeListener(this);
            this.unregisterFileSystemListener();
        } catch (TooManyListenersException e) {
            throw new IllegalStateException ();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.log(Level.FINER, "propertyChange from ClassPath: evt={0}, property name={1}", new Object[] {evt.toString(), evt.getPropertyName()});
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
            if (evt.getSource() == this.cp) {
                submitBatch();
            }
        }
        
        if (GlobalSourcePath.PROP_INCLUDES.equals(evt.getPropertyName())
            && (evt.getSource() == this.cpImpl
                || /*XXX: jlahoda: should not be necessary, IMO*/evt.getSource() == this.cp)) {
            ClassPath changedCp = (ClassPath) evt.getNewValue();
            assert changedCp != null;
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "ClassPath change, cp={0}", changedCp.toString());
            for (ClassPath.Entry e : changedCp.entries()) {
                URL root = e.getURL();
                scheduleCompilation(root,root, true, false);
            }
            
            return ;
        }
        
        
    }
    
    public void rootsChanged(Collection<ClassPath> changedCp) {
        assert changedCp != null;
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "modified roots changedCp={0}", changedCp.toString());
        }
        
        List<URL> roots = new LinkedList<URL>();

        for (ClassPath cp : changedCp) {
            Set<URL> rootsToChange = classPath2Roots.get(cp);

            if (rootsToChange != null) {
                for (URL root : rootsToChange) {
                    List<URL> oldDeps = this.deps.get(root);
                    if (oldDeps != null) {
                        final FileObject rootFo = URLMapper.findFileObject(root);
                        if (rootFo != null) {
                            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
                            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
                            final ClassPath[] pathsToResolve = new ClassPath[]{bootPath, compilePath};
                            final List<URL> newDeps = new LinkedList<URL>();
                            for (int i = 0; i < pathsToResolve.length; i++) {
                                final ClassPath pathToResolve = pathsToResolve[i];
                                if (pathToResolve != null) {
                                    for (ClassPath.Entry entry : pathToResolve.entries()) {
                                        final URL url = entry.getURL();
                                        final URL[] sourceRoots = RepositoryUpdater.this.cpImpl.getSourceRootForBinaryRoot(url, pathToResolve, false);
                                        if (sourceRoots != null) {
                                            for (URL sourceRoot : sourceRoots) {
                                                if (!sourceRoot.equals(root)) {
                                                    newDeps.add(sourceRoot);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            this.deps.put(root, newDeps);
                        }
                    }

                    roots.add(root);
                }
            }
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "roots for filter change={0}", roots.toString());
        }
        submit(Work.filterChange(roots, false));
    }
    
    private void submitBatch () {
        Work _currentWork;
        synchronized (this) {
            if (this.currentWork == null) {                    
                this.currentWork = _currentWork = Work.batch();                
            }
            else {
                this.dirty = true;
                return;
            }
        }
        submit (_currentWork);
    }
    
    public synchronized boolean isScanInProgress() {
        return notInitialized || this.noSubmited > 0;
    }
    
    public synchronized void waitScanFinished () throws InterruptedException {
        while (isScanInProgress()) {
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
    
    private synchronized void resetDirty (final Work ticket) {
        assert ticket != null;
        if (ticket == this.currentWork) {
            this.dirty = false;
            this.currentWork = null;
        }
    }
    
    
    public void fileRenamed(FileRenameEvent fe) {
        final FileObject fo = fe.getFile();
        try {
            boolean vs = false;
            if ((isJava (fo) || fo.isFolder() || (vs=VirtualSourceProviderQuery.hasVirtualSource(fo))) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot(fo);
                if (root != null) {                    
                    final File parentFile = FileUtil.toFile(fo.getParent());
                    if (parentFile != null) {                        
                        final String originalExt = fe.getExt();
                        boolean origVs = false;
                        if (isJava (originalExt) || (origVs = VirtualSourceProviderQuery.hasVirtualSource(originalExt))) {
                            String originalName = fe.getName();
                            originalName = originalName+'.'+originalExt;  //NOI18N
                            final URL original = new File (parentFile,originalName).toURI().toURL();
                            submit(Work.delete(original,root,fo.isFolder(),origVs));
                            if (TasklistSettings.isTasklistEnabled()) {
                                Set<URL> toRefresh = TaskCache.getDefault().dumpErrors(root, original, FileUtil.toFile(fo), Collections.<Diagnostic>emptyList());
                                if (TasklistSettings.isBadgesEnabled()) {
                                    ErrorAnnotator an = ErrorAnnotator.getAnnotator();

                                    if (an != null) {
                                        an.updateInError(toRefresh);
                                    }
                                }
                            }
                        }
                        final Work work = Work.compile (fo,root,vs);
                        RepositoryUpdater.WORKER.post(new Runnable () {
                            public void run () {
                                submit(work);
                            }
                        },DELAY);                        
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
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Folder created: "+FileUtil.getFileDisplayName(fo)+" Owner: " + root);
                }
                scheduleCompilation(fo.getURL(),root,true,false);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public void fileDeleted(FileEvent fe) {
        final FileObject fo = fe.getFile();        
        final boolean isFolder = fo.isFolder();
        try {
            boolean vs = false;
            if ((isJava(fo) || isFolder || (vs=VirtualSourceProviderQuery.hasVirtualSource(fo))) && VisibilityQuery.getDefault().isVisible(fo)) {                
                final URL root = getOwningSourceRoot (fo);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Java file deleted: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root);
                }
                if (root != null) {                    
                    markRootTasklistDirty(root);
                    submit(Work.delete(fo,root,isFolder,vs));
                    if (TasklistSettings.isTasklistEnabled()) {
                        Set<URL> toRefresh = TaskCache.getDefault().dumpErrors(root, fo.getURL(), FileUtil.toFile(fo), Collections.<Diagnostic>emptyList());
                        if (TasklistSettings.isBadgesEnabled()) {
                            ErrorAnnotator an = ErrorAnnotator.getAnnotator();
                            
                            if (an != null) {
                                an.updateInError(toRefresh);
                            }
                        }
                        JavaTaskProvider.refresh(fo);
                    }
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
            if ((isJava(fo) || VirtualSourceProviderQuery.hasVirtualSource(fo)) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);        
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Java file created: " + FileUtil.getFileDisplayName(fo) + " Owner: " + root);
                }                
                if (root != null) {                    
                    markRootTasklistDirty(root);
                    File f = FileUtil.toFile(fo);
                    
                    assert f != null;
                    
                    assureCompiledWithDeps(root, f);
                    
                    if (TasklistSettings.isTasklistEnabled() && TasklistSettings.isDependencyTrackingEnabled()) {
                        //new file creation may cause/fix some errors
                        //not 100% correct (consider eg. a file that has two .* imports
                        //new file creation may cause new error in this case
                        assureFilesWithErrorsRecompiled(root);
                    }
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
            if ((isJava(fo) || VirtualSourceProviderQuery.hasVirtualSource(fo)) && VisibilityQuery.getDefault().isVisible(fo)) {
                final URL root = getOwningSourceRoot (fo);
                File file = FileUtil.toFile(fo);
                if (LOGGER.isLoggable(Level.FINE)) {                    
                    LOGGER.fine("Java file changed: " + FileUtil.getFileDisplayName(fo) + " Owner: "+root);
                }                                
                if (root != null && file != null) {                    
                    markRootTasklistDirty(root);
                    assureCompiledWithDeps(root, file);
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
        if (!cpImpl.isLibrary(root)) {
            assert "file".equals(foURL.getProtocol()) && "file".equals(rootURL.getProtocol());
            scheduleCompilation (foURL,rootURL,fo.isFolder(),!isJava(fo));
        }
    }      
    
    
    private final void scheduleCompilation (final URL file, final URL root, boolean isFolder, boolean virtual) {
        submit(Work.compile (file,root, isFolder,virtual));
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
        submit(Work.compile (folder,root.getURL(),latch,folder.equals(root)));
        open();
        return latch[0];
    }
    
    private void submit (final Work  work) {
        if (!noscan) {
            synchronized (this) {
                this.noSubmited++;
            }
            final CompileWorker cw = new CompileWorker (work);
            JavaSource.Priority p;
            
            switch (work.getType()) {
                case RECOMPILE:
                case RECOMPILE_FILES_WITH_ERRORS:
                    p = JavaSource.Priority.LOW;
                    break;
                default:
                    p = JavaSource.Priority.MAX;
                    break;
            }
            JavaSourceAccessor.getINSTANCE().runSpecialTask (cw, p);
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
        List<URL> clone = new ArrayList<URL> (this.scannedRoots);
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
    
    public void rebuildRoot(URL toRebuild, boolean forceClean) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Rebuild Root: " + toRebuild, new Exception());
        }

        submit(Work.filterChange(Collections.singletonList(toRebuild), forceClean));
    }
    
    public void rebuildAll(boolean forceClean) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Rebuild All." , new Exception());
        }
        
        List<URL> toRebuild = new LinkedList<URL>();
        
        for (FileObject file : getScannedSources().getRoots()) {
            try {
                toRebuild.add(file.getURL());
            } catch (FileStateInvalidException e) {
                Exceptions.printStackTrace(e);
            }
        }
        submit(Work.filterChange(toRebuild, forceClean));
    }
    
    /**For IncorrectErrorBadges
     */
    public static int getDelay() {
        return DELAY;
    }
    
    private void registerClassPath(URL root, ClassPath cp, ClasspathInfo.PathKind kind) {
        Set<URL> roots = classPath2Roots.get(cp);
        
        if (roots == null) {
            classPath2Roots.put(cp, roots = new HashSet<URL>());
            ClassPathRootsListener.getDefault().addClassPathRootsListener(cp, kind != ClasspathInfo.PathKind.SOURCE, RepositoryUpdater.this);
        }
        
        roots.add(root);
    }
    
    
    private synchronized void assureRecompiled(URL root, Collection<File> files) {
        if (files.isEmpty()) {
            //nothing to do:
            return ;
        }
        
        Collection<File> storedFiles = url2Recompile.get(root);
        
        if (storedFiles == null) {
            url2Recompile.put(root, storedFiles = new LinkedHashSet<File>());
        }
        
        storedFiles.addAll(files);
        
        if (!recompileScheduled) {
            if (lockRU > 0) {
                recompileToBeScheduled = true;
            } else {
                submit(Work.recompile());
                recompileScheduled = true;
            }
        }
    }
    
    private synchronized void assureFilesWithErrorsRecompiled(URL root) {
        recompileFilesWithErrors.add(root);
        
        if (!recompileFilesWithErrorsScheduled) {
            if (lockRU > 0) {
                recompileFilesWithErrorsToBeScheduled = true;
            } else {
                submit(Work.recompileFilesWithErrors());
                recompileFilesWithErrorsScheduled = true;
            }
        }
    }
    
    private synchronized void assureCompiledWithDeps(final URL root, File file) {
        Reference<Task> taskRef = url2CompileWithDepsTask.get(root);
        Task t = taskRef != null ? taskRef.get() : null;
        Collection<File> storedFiles;
        
        if (t == null || !t.cancel()) {
            if (lockRU == 0 || t != null) {
                storedFiles = new LinkedHashSet<File>();
                url2CompileWithDeps.put(root, storedFiles);
            } else {
                storedFiles = url2CompileWithDeps.get(root);
                
                if (storedFiles == null) {
                    url2CompileWithDeps.put(root, storedFiles = new  LinkedHashSet<File>());
                }
            }
            
            if (lockRU == 0) {
                //the task either does not exist, or has been already started - create new one:
                LOGGER.log(Level.FINER, "creating a new task for root: {0}", root.toExternalForm());
                final Collection<File> storedFilesFin = storedFiles;
                t = WORKER.create(new Runnable() {
                    public void run() {
                        synchronized (RepositoryUpdater.this) {
                            compileScheduled--;
                        }
                        submit(Work.compileWithDeps(root, storedFilesFin));
                        noSubmited--;
                    }
                });
                url2CompileWithDepsTask.put(root, new WeakReference<Task>(t));
                compileScheduled++;
                noSubmited++;
            } else {
                url2CompileWithDepsTask.remove(root);
            }
        } else {
            storedFiles = url2CompileWithDeps.get(root);
        }
        
        assert storedFiles != null;

        storedFiles.add(file);
        
        if (lockRU > 0) {
            compileWithDepsToBeScheduled = true;
        } else {
            t.schedule(DELAY);
        }
    }
    
    private void findDependencies(final URL rootURL, final Stack<URL> cycleDetector, final Map<URL, List<URL>> depGraph) {
        if (depGraph.containsKey(rootURL)) {
            return;
        }
        final FileObject rootFo = URLMapper.findFileObject(rootURL);
        if (rootFo == null) {
            return;
        }
        cycleDetector.push(rootURL);
        final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
        final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
        final ClassPath[] pathsToResolve = new ClassPath[]{bootPath, compilePath};
        final List<URL> deps = new LinkedList<URL>();
        for (int i = 0; i < pathsToResolve.length; i++) {
            final ClassPath pathToResolve = pathsToResolve[i];
            if (pathToResolve != null) {
                for (ClassPath.Entry entry : pathToResolve.entries()) {
                    final URL url = entry.getURL();
                    final URL[] sourceRoots = cpImpl.getSourceRootForBinaryRoot(url, pathToResolve, false);
                    if (sourceRoots != null) {
                        for (URL sourceRoot : sourceRoots) {
                            if (!sourceRoot.equals(rootURL) && !cycleDetector.contains(sourceRoot)) {
                                deps.add(sourceRoot);
                                findDependencies(sourceRoot, cycleDetector, depGraph);
                            }
                        }
                    }
                }
            }
        }
        depGraph.put(rootURL, deps);
        cycleDetector.pop();
    }
    
    public synchronized boolean isRULocked() {
        return lockRU > 0;
    }
    
    public synchronized void lockRU() {
        lockRU++;
    }
    
    public void unlockRU() {
        unlockRU(null);
    }
    
    public synchronized void unlockRU(final Runnable notifyFinished) {
        if (--lockRU > 0)
            return ;
        
        assert lockRU == 0;
        
        if (recompileToBeScheduled) {
            submit(Work.recompile());
            recompileScheduled = true;
            recompileToBeScheduled = false;
        }
        
        if (recompileFilesWithErrorsToBeScheduled) {
            submit(Work.recompileFilesWithErrors());
            recompileFilesWithErrorsScheduled = true;
            recompileFilesWithErrorsToBeScheduled = false;
        }
        
        if (compileWithDepsToBeScheduled) {
            final Map<URL, List<URL>> depGraph = new HashMap<URL, List<URL>>();
            for (URL rootURL : url2CompileWithDeps.keySet()) {
                findDependencies(rootURL, new Stack<URL>(), depGraph);
            }
            List<URL> rootsToCompile;

            try {
                rootsToCompile = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                Collections.reverse(rootsToCompile);
            } catch (TopologicalSortException ex) {
                Exceptions.printStackTrace(ex);
                rootsToCompile = new ArrayList<URL>(url2CompileWithDeps.keySet());
            }
            
            int index = 0;

            for (final URL root : rootsToCompile) {
                if (!url2CompileWithDeps.containsKey(root)) {
                    continue;
                }

                Reference<Task> taskRef = url2CompileWithDepsTask.get(root);
                Task t = taskRef != null ? taskRef.get() : null;

                if (t == null || !t.cancel()) {
                    final Collection<File> storedFilesFin = url2CompileWithDeps.get(root);
                    //the task either does not exist, or has been already started - create new one:
                    t = WORKER.create(new Runnable() {
                        public void run() {
                            submit(Work.compileWithDeps(root, storedFilesFin));
                            noSubmited--;
                        }
                    });
                    noSubmited++;
                    url2CompileWithDepsTask.put(root, new WeakReference<Task>(t));
                }

                t.schedule(DELAY + index++);
            }
            compileWithDepsToBeScheduled = false;
            
            if (notifyFinished != null) {
                WORKER.create(new Runnable() {
                    public void run() {
                        submit(Work.notifyFinished(notifyFinished));
                    }
                }).schedule(DELAY + index++);
            }
        }
    }
    
    private static void markRootTasklistDirty(URL root) throws IOException {
        if (TasklistSettings.isTasklistEnabled() && TasklistSettings.isDependencyTrackingEnabled()) {
            ensureAttributeValue(root, DIRTY_ROOT, "true", false); //NOI18N
        } else {
            ensureAttributeValue(root, CONTAINS_TASKLIST_DEPENDENCY_DATA, null, false); //NOI18N
            if (!TasklistSettings.isTasklistEnabled()) {
                ensureAttributeValue(root, CONTAINS_TASKLIST_DATA, null, false); //NOI18N
            }
        }
    }
    
    static boolean ensureAttributeValue(final URL root, final String attributeName, final String attributeValue, final boolean markDirty) throws IOException {
        Properties p = loadProperties(root);
        final String current = p.getProperty(attributeName);
        
        if (   (attributeValue != null && attributeValue.equals(current))
            || (attributeValue == null && current == null))
            return false;
        
        if (attributeValue != null) {
            p.setProperty(attributeName, attributeValue);
        } else {
            p.remove(attributeName);
        }
        
        if (markDirty) {
            p.setProperty(DIRTY_ROOT, "true"); //NOI18N
        }
        
        storeProperties(root, p);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine ("ensureAttributeValue attr: " + attributeName + " current: " +current+ " new: " + attributeValue+" markDirty: "+markDirty);
        }
        return true;
    }
    
    static void setAttribute(URL root, String attributeName, String attributeValue) throws IOException {
        Properties p = loadProperties(root);
        
        if (attributeValue != null) {
            p.setProperty(attributeName, attributeValue);
        } else {
            p.remove(attributeName);
        }
        
        storeProperties(root, p);
    }
    
    static String getAttribute(URL root, String attributeName, String defaultValue) throws IOException {
        Properties p = loadProperties(root);
        
        return p.getProperty(attributeName, defaultValue);
    }
    
    private static Properties loadProperties(URL root) throws IOException {
        File f = getAttributeFile(root);
        Properties result = new Properties();
        
        if (!f.exists())
            return result;
        
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        
        try {
            result.load(in);
        } catch (IllegalArgumentException iae) {
            //Issue #138704: Invalid unicode encoding in attribute file.
            //Return newly constructed Properties, the result
            //may already contain some pairs.
            LOGGER.warning("Broken attribute file: " + f.getAbsolutePath());    //NOI18N
            return new Properties();
        } finally {
            in.close();
        }
        
        return result;
    }
    
    private static void storeProperties(URL root, Properties p) throws IOException {
        File f = getAttributeFile(root);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        
        try {
            p.store(out, "");
        } finally {
            out.close();
        }
    }
    
    private static File getAttributeFile(URL root) throws IOException {
        File dirtyFile = Index.getClassFolder(root);
        
        return new File(dirtyFile.getParentFile(), "attributes.properties");
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
    
    private static boolean isJava (final String ext) {
        return ext != null && JavaDataLoader.JAVA_EXTENSION.equals(ext.toLowerCase());
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
        COMPILE_BATCH, COMPILE_CONT, COMPILE, DELETE, UPDATE_BINARY, FILTER_CHANGED, COMPILE_WITH_DEPENDENCIES, RECOMPILE, RECOMPILE_FILES_WITH_ERRORS, NOTIFY_FINISHED
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
        
        public static Work compile (final FileObject file, final URL root, boolean virtual) throws FileStateInvalidException {
            return compile (file.getURL(), root, file.isFolder(), virtual);
        }
        
        
        public static Work compile (final URL file, final URL root, boolean isFolder, boolean virtual) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.COMPILE, file, root, isFolder, virtual, null,false);
        }
        
        
        private static Work compile (final FileObject file, final URL root, CountDownLatch[] latch, boolean isInitialCompilation) throws FileStateInvalidException {
            assert file != null && root != null;
            assert latch != null && latch.length == 1 && latch[0] == null;
            latch[0] = new CountDownLatch (1);
            return new SingleRootWork (WorkType.COMPILE, file.getURL(), root, file.isFolder(), false, latch[0], isInitialCompilation);
        }
        
        public static Work delete (final FileObject file, final URL root, final boolean isFolder, final boolean isVirtual) throws FileStateInvalidException {
            return delete (file.getURL(), root,file.isFolder(), isVirtual);
        }
        
        public static Work delete (final URL file, final URL root, final boolean isFolder, final boolean isVirtual) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.DELETE, file, root, isFolder,isVirtual,null,false);
        }
        
        public static Work binary (final FileObject file, final URL root) throws FileStateInvalidException {
            return binary (file.getURL(), root, file.isFolder());
        }
        
        public static Work binary (final URL file, final URL root, boolean isFolder) {
            assert file != null && root != null;
            return new SingleRootWork (WorkType.UPDATE_BINARY, file, root, isFolder, false, null,false);
        }
        
        public static Work filterChange (final List<URL> roots, boolean forceClean) {
            assert roots != null;
            return new MultiRootsWork (WorkType.FILTER_CHANGED, roots, forceClean, null);
        }
        
        public static Work recompile() {
            return new RecompileWork(null);
        }
        
        public static Work recompileFilesWithErrors() {
            return new RecompileFilesWithErrorsWork(null);
        }
        
        public static Work compileWithDeps(URL root, Collection<File> filesToCompile) {
            return new CompileWithDepsWork(root, filesToCompile, null);
        }
        
        public static Work notifyFinished(Runnable r) {
            return new NotifyFinishedWork(r);
        }
        
    }
        
    private static class SingleRootWork extends Work {
        
        private final URL file;
        private final URL root;
        private final boolean isFolder;
        private final boolean isVirtual;
        private final boolean isInitialCompilation;
        
                
        public SingleRootWork (WorkType type, URL file, URL root,
                boolean isFolder, boolean isVirtual,
                CountDownLatch latch, boolean isInitialCompilation) {
            super (type, latch);           
            this.file = file;            
            this.root = root;
            this.isFolder = isFolder;
            this.isVirtual = isVirtual;
            this.isInitialCompilation = isInitialCompilation;
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
        
        public boolean isVirtual () {
            return this.isVirtual;
        }
        
        public boolean isInitialCompilation () {
            return this.isInitialCompilation;
        }
        
    }
    
    private static class MultiRootsWork extends Work {
        private List<URL> roots;
        private boolean forceClean;
        
        public MultiRootsWork (WorkType type, List<URL> roots, boolean forceClean, CountDownLatch latch) {
            super (type, latch);
            this.roots = roots;
            this.forceClean = forceClean;
        }
        
        public List<URL> getRoots () {
            return roots;
        }
        
        public boolean getForceClean() {
            return forceClean;
        }
    }          
    
    private static class RecompileWork extends Work {
        public RecompileWork(CountDownLatch latch) {
            super(WorkType.RECOMPILE, latch);
        }
    }
    
    private static class RecompileFilesWithErrorsWork extends Work {
        public RecompileFilesWithErrorsWork(CountDownLatch latch) {
            super(WorkType.RECOMPILE_FILES_WITH_ERRORS, latch);
        }
    }
    
    private static class CompileWithDepsWork extends Work {
        private URL root;
        private Collection<File> filesToCompile;
        public CompileWithDepsWork(URL root, Collection<File> filesToCompile, CountDownLatch latch) {
            super(WorkType.COMPILE_WITH_DEPENDENCIES, latch);
            this.root = root;
            this.filesToCompile = filesToCompile;
        }

        public URL getRoot() {
            return root;
        }

        public Collection<File> getFilesToCompile() {
            return filesToCompile;
        }
    }
    
    private static class NotifyFinishedWork extends Work {
        
        private Runnable r;

        public NotifyFinishedWork(Runnable r) {
            super(WorkType.NOTIFY_FINISHED, null);
            this.r = r;
        }
        
    }
    
    private final class CompileWorker implements CancellableTask<CompilationInfo> {
                
        /**
         * Used as a identity for resetDirty, which should reset currentWork only
         * when the currentWork == ticket. The ticket is the same as initial work,
         * unfortunately the work is not final and changes when the initial scan is interrupted,
         * so we need a new final field.
         */
        private final Work ticket;
        private Work work;
        private List<URL> state;
        private Set<URL> oldRoots;
        private Set<URL> oldBinaries;
        private Set<URL> newBinaries;                
        private ProgressHandle handle;
        private final Set<URI> dirtyCrossFiles;        
        private final Set<URL> ignoreExcludes;
        private final AtomicBoolean canceled;
        private BinaryAnalyser activeBinaryAnalyzer;
        
        //Logging
        private long cbst;      //Complete binary scan time
        private long csst;      //Complete source scan time
        
        public CompileWorker (Work work ) {
            assert work != null;
            this.work = work;
            this.ticket = work;
            this.canceled = new AtomicBoolean (false);
            this.dirtyCrossFiles = new HashSet<URI>();
            this.ignoreExcludes = new HashSet<URL>();
        }
        
        public void cancel () { 
            this.canceled.set(true);
        }
        
        public void run (final CompilationInfo nullInfo) throws IOException {
            ACTIVITY_LOGGER.finest("START");    //NOI18N
            try {
            ClassIndexManager.getDefault().writeLock (new ClassIndexManager.ExceptionAction<Void> () {
                
                @SuppressWarnings("fallthrough")
                public Void run () throws IOException {
                    boolean continuation = false;
                    try {
                    final WorkType type = work.getType();                        
                    LOGGER.finer ("Request for: " + type);      //NOI18N
                    switch (type) {
                        case FILTER_CHANGED:
                        {
                            //the global handle is always null here, isn't it?
                            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                            
                            handle.start();
                            try {
                                final MultiRootsWork mw = (MultiRootsWork) work;
                                final List<URL> roots = mw.getRoots();
                                if (roots.size() > 1) {
                                    final Map<URL,List<URL>> depGraph = new HashMap<URL,List<URL>> ();                                
                                    for (URL root: roots) {
                                        findDependencies (root, new Stack<URL>(), depGraph, null, false);
                                    }
                                    state = Utilities.topologicalSort(roots, depGraph);
                                } else {
                                    //if only one root is to be updated, no need to compute the dependencies:
                                    state = new LinkedList<URL>(roots);
                                }
                                for (java.util.ListIterator<URL> it = state.listIterator(state.size()); it.hasPrevious(); ) {                
                                    if (closed.get()) {
                                        return null;
                                    }
                                    final URL rootURL = it.previous();
                                    it.remove();
                                    updateFolder (rootURL,rootURL, true, mw.getForceClean());
                                }                                
                            } catch (final TopologicalSortException tse) {
                                    final IllegalStateException ise = new IllegalStateException ();                                
                                    throw (IllegalStateException) ise.initCause(tse);
                            } finally {
                                handle.finish();
                            }
                            break;
                        }
                        case COMPILE_BATCH:
                        {
                            cbst = csst = 0L;
                            assert handle == null;
                            handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                            handle.start();
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
                                CompileWorker.this.state = Utilities.topologicalSort(depGraph.keySet(), depGraph);
                                deps.putAll(depGraph);
                                completed = true;
                            } catch (final TopologicalSortException tse) {
                                final IllegalStateException ise = new IllegalStateException ();                                
                                throw (IllegalStateException) ise.initCause(tse);
                            } finally {
                                if (!completed) {
                                    resetDirty(ticket);
                                }
                            }
                        }
                        case COMPILE_CONT:
                            boolean completed = false;
                            try {
                                if (activeBinaryAnalyzer != null) {
                                    BinaryAnalyser.Result baFinished = null;
                                    try {
                                        baFinished = activeBinaryAnalyzer.resume();
                                    } finally {
                                        if (baFinished == null || baFinished == BinaryAnalyser.Result.FINISHED) {
                                            activeBinaryAnalyzer.finish();
                                            activeBinaryAnalyzer = null;
                                        }
                                        else if (baFinished == BinaryAnalyser.Result.CLOSED) {
                                            activeBinaryAnalyzer.clear();
                                            activeBinaryAnalyzer = null;
                                        }
                                        else {
                                            CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                            JavaSourceAccessor.getINSTANCE().runSpecialTask (CompileWorker.this,JavaSource.Priority.MAX);
                                            continuation = true;
                                            return null;
                                        }
                                    }
                                }
                                if (!scanRoots()) {
                                    CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                    JavaSourceAccessor.getINSTANCE().runSpecialTask (CompileWorker.this,JavaSource.Priority.MAX);
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
                                        throw (IllegalStateException) ise.initCause(tse);
                                    }
                                    if (!scanRoots ()) {
                                        CompileWorker.this.work = new Work (WorkType.COMPILE_CONT,null);
                                        JavaSourceAccessor.getINSTANCE().runSpecialTask (CompileWorker.this,JavaSource.Priority.MAX);
                                        continuation = true;
                                        return null;
                                    }
                                }
                                completed = true;

                                //UI Gestures:
                                Object[] data = new Object[2 * root2DebugData.size() + 1];
                                int index = 0;

                                data[index++] = Index.getCacheFolder().getName();
                                
                                for (Entry<URL, String> e : root2DebugData.entrySet()) {
                                    File cacheFile = Index.getClassFolder(e.getKey(), true);

                                    if (cacheFile != null) {
                                        data[index] = cacheFile.getParentFile().getName();
                                    }

                                    index++;
                                    data[index++] = e.getValue();
                                }

                                root2DebugData.clear();

                                LogRecord rec = new LogRecord(Level.CONFIG, RepositoryUpdater.class.getName());
                                rec.setParameters(data);

                                Logger.getLogger("org.netbeans.ui.java.RepositoryUpdater").log(rec);
                            } finally {
                                if (!completed && !continuation) {
                                    resetDirty (ticket);
                                }
                            }                            
                            final ClassIndexManager cim = ClassIndexManager.getDefault();
                            scannedRoots.removeAll(oldRoots);
                            deps.keySet().remove(oldRoots);
                            for (URL oldRoot : oldRoots) {
                                cim.removeRoot(oldRoot);
                                JavaFileFilterImplementation filter = filters.remove(oldRoot);
                                if (filter != null && !filters.values().contains(filter)) {
                                    filter.removeChangeListener(filterListener);
                                }
                            }
                            scannedBinaries.removeAll (oldBinaries);
                            final CachingArchiveProvider cap = CachingArchiveProvider.getDefault();
                            for (URL oldRoot : oldBinaries) {
                                cim.removeRoot(oldRoot);
                                cap.removeArchive(oldRoot);
                            }
                            final JTextComponent editor = EditorRegistry.lastFocusedComponent();
                            if (editor != null) {
                                final Document doc = editor.getDocument();
                                JavaSource js = doc == null ? null : JavaSource.forDocument(doc);
                                if (js != null) {
                                    JavaSourceAccessor.getINSTANCE().revalidate(js);
                                }
                            }
                            LOGGER.finer(String.format("Complete binary scan time: %d ms. Complete source scan time: %d ms.", cbst, csst));      //NOI18N
                            break;
                        case COMPILE:
                        {
                            try {
                                final SingleRootWork sw = (SingleRootWork) work;
                                final URL file = sw.getFile();
                                final URL root = sw.getRoot ();
                                if (sw.isFolder()) {
                                    handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_BackgroundCompileStart"));
                                    handle.start();
                                    try {
                                        updateFolder (file, root, sw.isInitialCompilation(), false);
                                    } finally {
                                        handle.finish();
                                    }
                                }
                                else {
                                    updateFile (file, root, sw.isVirtual(), null);
                                }
                            } catch (Abort abort) {
                                //Ignore abort
                            }                         
                            break;
                        }
                        case DELETE:
                        {
                            final SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot ();
                            final List<File> toRebuild = delete (file, root, sw.isFolder(), sw.isVirtual());
                            if (toRebuild != null)
                                assureRecompiled(root, toRebuild);
                            break;
                        }
                        case UPDATE_BINARY:
                        {
                            SingleRootWork sw = (SingleRootWork) work;
                            final URL file = sw.getFile();
                            final URL root = sw.getRoot();
                            updateBinary (file, root);
                            break;
                        }
                        case COMPILE_WITH_DEPENDENCIES:
                        {
                            try {
                                CompileWithDepsWork w = (CompileWithDepsWork) work;
                                
                                compileWithDeps(w.getRoot(), w.getFilesToCompile());
                            } catch (Abort abort) {
                                //Ignore abort
                            }
                            break;
                        }
                        case RECOMPILE_FILES_WITH_ERRORS:
                        {
                            Set<URL> toAnalyse = new HashSet<URL>();
                            synchronized (RepositoryUpdater.this) {
                                toAnalyse.addAll(recompileFilesWithErrors);
                                recompileFilesWithErrors.clear();
                                recompileFilesWithErrorsScheduled = false;
                            }
                            
                            for (URL root : toAnalyse) {
                                try {
                                    long s = System.currentTimeMillis();

                                    List<File> toReparse = new LinkedList<File>();
                                    for (URL u : TaskCache.getDefault().getAllFilesInError(root)) {
                                        toReparse.add(FileUtil.normalizeFile(new File(u.toURI())));
                                    }

                                    long e = System.currentTimeMillis();

                                    LOGGER.log(Level.FINER, "Enumerating files with errors for root: {0} took: {1} ms.", new Object[]{root, e - s});

                                    assureRecompiled(root, toReparse);
                                } catch (URISyntaxException e) {
                                    LOGGER.log(Level.WARNING, null, e);
                                }
                            }
                            break;
                        }
                        case RECOMPILE:
                        {
                            //XXX compileWithDeps should have priority ower recompile:
                            try {
                                recompile();
                            } catch (Abort abort) {
                                //Ignore abort
                            }
                            break;
                        }
                        case NOTIFY_FINISHED:
                        {
                            WORKER.post(new Runnable() {
                                public void run() {
                                    ((NotifyFinishedWork) work).r.run();
                                }
                            });
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
            } catch (InterruptedException e) {
                //Never thrown
                Exceptions.printStackTrace(e);
            }
            finally {
                ACTIVITY_LOGGER.finest("FINISHED");    //NOI18N
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

        private void gatherResourceForParseFilesFromRoot(Collection<? extends File> files, File rootFile, final File cacheRoot, Map<String, List<File>> resources) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "going to compute resources");
                LOGGER.log(Level.FINEST, "files={0}", files);
            }

            String rootName = cacheRoot.getAbsolutePath();
            int len = rootName.length();
            if (rootName.charAt(len - 1) != File.separatorChar) {
                len++;
            }

            for (File toProcess : files) {
                String relative = FileObjects.stripExtension(FileObjects.getRelativePath(rootFile, toProcess));

                LOGGER.log(Level.FINEST, "relative={0}", relative);

                File f = new File(cacheRoot, relative + '.' + FileObjects.RS);

                LOGGER.log(Level.FINEST, "f={0}, exists={1}", new Object[]{f.getAbsolutePath(), f.exists()});

                if (f.exists()) {
                    gatherResources(cacheRoot, f, len, resources);
                    continue;
                }

                f = new File(cacheRoot, relative + '.' + FileObjects.CLASS);

                LOGGER.log(Level.FINEST, "f={0}, exists={1}", new Object[]{f.getAbsolutePath(), f.exists()});

                if (f.exists()) {
                    gatherResources(cacheRoot, f, len, resources);

                    File folder = f.getParentFile();
                    File[] children = folder.listFiles();

                    if (children == null) {
                        LOGGER.info("IO error while listing folder: " + folder.getAbsolutePath() + " isDirectory: " + folder.isDirectory() + " canRead: " + folder.canRead()); //NOI18N
                        continue;
                    }

                    String prefix = FileObjects.stripExtension(f.getName()) + "$";

                    for (File child : children) {
                        if (child.getName().startsWith(prefix)) {
                            gatherResources(cacheRoot, child, len, resources);
                        }
                    }
                }
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "resources={0}", resources);
            }
        }
        
        private boolean scanRoots () {
            long cst = System.currentTimeMillis();
            try {
                for (Iterator<URL> it = this.newBinaries.iterator(); it.hasNext(); ) {
                    if (this.canceled.getAndSet(false)) {                    
                        return false;
                    }
                    if (closed.get()) {
                        return true;
                    }
                    final URL rootURL = it.next();
                    updateProgress (rootURL);
                    try {
                        it.remove();
                        final ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(rootURL,false);                                        
                        RepositoryUpdater.this.scannedBinaries.add (rootURL);                    
                        long time = 0;
                        BinaryAnalyser ba = ci.getBinaryAnalyser();
                        BinaryAnalyser.Result finished = null;
                        try {
                            finished = ba.start(rootURL, canceled, closed);
                        } finally {
                            if (finished == null || finished == BinaryAnalyser.Result.FINISHED) {                            
                                    time = ba.finish();
                            }
                            else if (finished == BinaryAnalyser.Result.CLOSED) {
                                    ba.clear();                            
                            }
                            else {
                                activeBinaryAnalyzer = ba;
                                return false;
                            }
                        }
                        if (PERF_TEST) {
                            try {
                                Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                                java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                                m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(time)});
                            } catch (Exception e) {
                                    Exceptions.printStackTrace(e);
                            }                            
                        }
                        LOGGER.finer (String.format("Indexing of: %s took: %d ms",rootURL.toExternalForm(),time));
                    } catch (Throwable e) {
                        if (e instanceof ThreadDeath) {
                            throw (ThreadDeath) e;
                        }
                        else {
                            Exceptions.attachMessage(e, "While scanning: " + rootURL);
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            } finally {
                cbst += System.currentTimeMillis() - cst;
            }
            cst = System.currentTimeMillis();
            try {
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
                            updateProgress (rootURL);
                            updateFolder (rootURL,rootURL, true, false);
                            long time = System.currentTimeMillis() - startT;                        
                            if (PERF_TEST) {
                                try {
                                    Class c = Class.forName("org.netbeans.performance.test.utilities.LoggingScanClasspath",true,Thread.currentThread().getContextClassLoader()); // NOI18N
                                    java.lang.reflect.Method m = c.getMethod("reportScanOfFile", new Class[] {String.class, Long.class}); // NOI18N
                                    m.invoke(c.newInstance(), new Object[] {rootURL.toExternalForm(), new Long(time)});
                                } catch (Exception e) {
                                        Exceptions.printStackTrace(e);
                                }
                            }
                            LOGGER.finer(String.format("Scannig of %s took %d ms.", rootURL.toExternalForm(), time));
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
            } finally {
                csst += System.currentTimeMillis() - cst;
            }
            return true;
        }
        
        private void updateProgress (final URL url) {
            assert url != null;
            if (handle == null) {
                return;
            }            
            URL tmp = FileUtil.getArchiveFile(url);
            if (tmp == null) {
                tmp = url;
            }
            try {
                if ("file".equals(tmp.getProtocol())) {
                    final File file = new File(new URI(tmp.toString()));                    
                    handle.progress(file.getAbsolutePath());
                }
                else {
                    handle.progress(tmp.toString());
                }
            } catch (URISyntaxException ex) {
                handle.progress(tmp.toString());
            }            
        }
        
        private void parseFiles(URL root, final File classCache, boolean isInitialCompilation,
                Iterable<? extends File> children, Iterable<? extends File> virtualChildren,
                boolean clean, JavaFileFilterImplementation filter,
                Map<String,List<File>> resources, Set<File> compiledFiles, Set<File> toRecompile,
                Map<URI, List<String>> misplacedSource2FQNs, boolean allowCancel, boolean generateVirtual)
                throws IOException 
        {
            parseFiles(root, classCache, isInitialCompilation, children,
                    virtualChildren, clean, filter, resources, compiledFiles,
                    toRecompile, misplacedSource2FQNs, allowCancel, generateVirtual,
                    null, null);
        }

        private void parseFiles(URL root, final File classCache, boolean isInitialCompilation,
                Iterable<? extends File> children, Iterable<? extends File> virtualChildren,
                boolean clean, JavaFileFilterImplementation filter,
                Map<String,List<File>> resources, Set<File> compiledFiles, Set<File> toRecompile,
                Map<URI, List<String>> misplacedSource2FQNs, boolean allowCancel, boolean generateVirtual,
                FileList digest, File folderFile) throws IOException {
        
            assert !allowCancel || compiledFiles != null;
            LOGGER.finer("parseFiles: " + root);            
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }
            if (!rootFo.isFolder()) {
                LOGGER.warning("Source root has to be a folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }
            
            final File rootFile = FileUtil.toFile(rootFo);
            
            final ClassPath sourcePath = ClassPath.getClassPath(rootFo,ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);                        
            
            if (sourcePath == null) {
                LOGGER.warning("No source path for folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }
            
            if (bootPath == null) {
                LOGGER.warning("No boot path for folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }
            
            if (compilePath == null) {
                LOGGER.warning("No compile path for folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }
            
            if (!Arrays.asList(sourcePath.getRoots()).contains(rootFo)) {
                LOGGER.warning("Source root: " +  FileUtil.getFileDisplayName(rootFo) + " not on its sourcepath."); // NOI18N
                return;
            }                        
            
            ClassPath.Entry entry = null;
            if (!this.ignoreExcludes.contains(root)) {
                entry = getClassPathEntry(sourcePath, root);
            }
            
            LOGGER.fine("Initial value of clean: "+clean);
            
            if (isInitialCompilation) {
                if (getAttribute(root, DIRTY_ROOT, null) != null) { //always do a clean rebuild if root dirty
                    LOGGER.fine("forcing clean due to dirty root");
                    clean = true;
                }
                
                String sourceLevel = SourceLevelQuery.getSourceLevel(rootFo);
                
                if (ensureAttributeValue(root, SOURCE_LEVEL_ROOT, sourceLevel, true)) {
                    LOGGER.fine("forcing clean due to source level change");
                    clean = true;
                }
                
                String extraCompilerOptions = CompilerSettings.getCommandLine();
                
                if (extraCompilerOptions.length() == 0) {
                    extraCompilerOptions = null;
                }
                
                if (ensureAttributeValue(root, EXTRA_COMPILER_OPTIONS, extraCompilerOptions, true)) {
                    LOGGER.fine("forcing clean due to extra compiler options change");
                    clean = true;
                }
                
                if (ensureAttributeValue(root, CLASSPATH_ATTRIBUTE, classPathToString(ClasspathInfoAccessor.getINSTANCE().create(bootPath, compilePath, sourcePath,null,true,false,false)), true)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("forcing clean due to difderent classpaths, original: "+
                                getAttribute(root, CLASSPATH_ATTRIBUTE, "")+
                                "new:"+
                                classPathToString(ClasspathInfoAccessor.getINSTANCE().create(bootPath, compilePath, sourcePath,null,true,false,false)));
                    }
                    clean = true;
                }

                if (TasklistSettings.isTasklistEnabled() && TasklistSettings.isDependencyTrackingEnabled()) {
                    if (ensureAttributeValue(root, CONTAINS_TASKLIST_DATA, "true", true)) {
                        LOGGER.fine("forcing clean because there are not tasklist data");
                        clean = true;
                    }
                    if (ensureAttributeValue(root, CONTAINS_TASKLIST_DEPENDENCY_DATA, "true", true)) {
                        LOGGER.fine("forcing clean because there are not tasklist dependency data");
                        clean = true;
                    }
                }
            }

            LinkedList<CompileTuple> toCompile = new LinkedList<CompileTuple>();
            ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
            if (uqImpl == null) {
                //IDE is exiting, indeces are already closed.
                return;
            }
            if (digest != null && !digest.resolveDigest(root) && !clean) {
                // no need to recompile whole root and no change detected
                // in source root.
                return;
            }
            if (resources == null && folderFile != null) {
                resources = getAllClassFiles(classCache, FileObjects.getRelativePath(rootFile, folderFile), true);
            }
            SourceAnalyser sa = uqImpl.getSourceAnalyser();
            assert sa != null;
            boolean invalidIndex = isInitialCompilation && !sa.isValid();
            LOGGER.fine("forcing clean because of invalid index");
            Set<File> rs = new HashSet<File> ();
            
            //XXX: getting encoding for the folder is (technically speaking) incorrect:
            Charset encoding = FileEncodingQuery.getEncoding(rootFo);
            Set<File> existingFilesInError = new HashSet<File>();
            if (isInitialCompilation) {
                if (TasklistSettings.isTasklistEnabled()) {
                    for (URL u : TaskCache.getDefault().getAllFilesWithRecord(root)) {
                        try {
                            existingFilesInError.add(FileUtil.normalizeFile(new File(u.toURI())));
                        } catch (URISyntaxException ex) {
                            LOGGER.log(Level.FINEST, null, ex);
                        }
                    }
                } else {
                    ensureAttributeValue(root, CONTAINS_TASKLIST_DATA, "false", false);
                }
            }
            Set<ElementHandle<TypeElement>> removed = isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
            Set<ElementHandle<TypeElement>> added =   isInitialCompilation ? null : new HashSet<ElementHandle<TypeElement>> ();
            Set<File>                       removedFiles = isInitialCompilation ? null : new HashSet<File>();
            Set<File>                       addedFiles   = isInitialCompilation ? null : new HashSet<File>();
            Set<URL> errorBadgesToRefresh = new HashSet<URL>();
            for (File child : children) {
                if (!child.canRead()) {
                    //the file is not readable, ignore it:
                    if (compiledFiles != null) {
                        compiledFiles.add(child);
                    }
                    continue;
                }
                if (!existingFilesInError.isEmpty()) {
                    existingFilesInError.remove(child);
                }
                final String relativePath = FileObjects.getRelativePath(rootFile,child);
                if (entry == null || entry.includes(relativePath.replace(File.separatorChar,'/'))) {
                    if (invalidIndex || clean || dirtyCrossFiles.remove(child.toURI())) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            String message = "Compiling " + child.getPath() + " due to ";       //NOI18N
                            if (invalidIndex) {
                                message+="invalidIndex";                                        //NOI18N
                            }
                            else if (clean) {
                                message+="clean";                                               //NOI18N
                            }
                            else {
                                message+="dirtyCrossFiles";                                     //NOI18N
                            }
                            LOGGER.finest(message);
                        }
                        toCompile.add(new CompileTuple(FileObjects.fileFileObject(child, rootFile, filter, encoding), child));
                    } else {                        
                        final int index = relativePath.lastIndexOf('.');  //NOI18N
                        final String offset = (index > -1) ? relativePath.substring(0,index) : relativePath;
                        List<File> files = resources.remove(offset);
                        if  (files==null) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.finest("Compiling " + child.getPath()+" no cache for it" );      //NOI18N
                            }
                            toCompile.add(new CompileTuple(FileObjects.fileFileObject(child, rootFile, filter, encoding), child));
                        } else {
                            boolean rsf = files.get(0).getName().endsWith(FileObjects.RS);
                            String sourceName = null;
                            if (files.get(0).lastModified() < child.lastModified()) {
                                if (LOGGER.isLoggable(Level.FINEST)) {
                                    LOGGER.finest("Compiling " + child.getPath()+ " timestamp (cache: "+files.get(0).lastModified()+" source: "+child.lastModified()+")" ); //NOI18N
                                }
                                toCompile.add(new CompileTuple(FileObjects.fileFileObject(child, rootFile, filter, encoding), child));
                                String rsFileBinaryName = null;
                                for (File toDelete : files) {
                                    toDelete.delete();
                                    if (rsf) {
                                        sourceName = relativePath;
                                        rsFileBinaryName = FileObjects.getBinaryName(toDelete,classCache);
                                        rsf = false;
                                    } else {
                                        String className = FileObjects.getBinaryName(toDelete,classCache);
                                        if (sourceName != null &&  !rsFileBinaryName.equals(className)) {
                                            sa.delete(className,sourceName);
                                        }
                                        else {
                                            sa.delete(className,null);
                                        }
                                        if (removed != null) {
                                            removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                                            removedFiles.add(toDelete);
                                        }
                                    }
                                }
                            } else if (rsf) {
                                files.remove(0);
                                rs.addAll(files);
                            }
                        }
                    }
                } else {
                    if (TasklistSettings.isTasklistEnabled()) {
                        //excluded file, make sure any errors attached to it are deleted:
                        errorBadgesToRefresh.addAll(TaskCache.getDefault().dumpErrors(root, child.toURI().toURL(), child, Collections.<Diagnostic>emptyList()));
                    }
                }
            }
            final List<File> virtualFilesToCompile = new LinkedList<File>();
            boolean hasVirtualChildren = false;
            for (File child : virtualChildren) {
                hasVirtualChildren = true;
                if (!child.canRead()) {
                    //the file is not readable, ignore it:
                    continue;
                }
                final String relativePath = FileObjects.getRelativePath(rootFile,child);
                if (entry == null || entry.includes(relativePath.replace(File.separatorChar,'/'))) {
                    if (invalidIndex || clean || dirtyCrossFiles.remove(child.toURI())) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            String message = "Compiling " + child.getPath() + " due to ";       //NOI18N
                            if (invalidIndex) {
                                message+="invalidIndex";                                        //NOI18N
                            }
                            else if (clean) {
                                message+="clean";                                               //NOI18N
                            }
                            else {
                                message+="dirtyCrossFiles";                                     //NOI18N
                            }
                            LOGGER.finest(message);
                        }
                        virtualFilesToCompile.add(child);
                    } else {                        
                        final int index = relativePath.lastIndexOf('.');  //NOI18N
                        final String offset = (index > -1) ? relativePath.substring(0,index) : relativePath;
                        List<File> files = resources.remove(offset);
                        if  (files==null) {
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.finest("Compiling " + child.getPath()+" no cache for it" );      //NOI18N
                            }
                            virtualFilesToCompile.add(child);
                        } else if (!files.isEmpty() && files.get(0).getName().endsWith(FileObjects.RX)) {
                            if (files.get(0).lastModified() < child.lastModified()) {
                                if (LOGGER.isLoggable(Level.FINEST)) {
                                    LOGGER.finest("Compiling " + child.getPath()+ " timestamp (cache: "+files.get(0).lastModified()+" source: "+child.lastModified()+")" ); //NOI18N
                                }
                                virtualFilesToCompile.add(child);
                                for (File toDelete : files) {
                                    toDelete.delete();                                   
                                    String className = FileObjects.getBinaryName(toDelete,classCache);                                        
                                    sa.delete(className,relativePath);
                                    if (removed != null) {
                                        removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                                    }                                    
                                }
                            } else {
                                files.remove(0);
                                rs.addAll(files);
                            }
                        }
                    }
                }
            }
            if (hasVirtualChildren) {
                rootsWithVirtualSource.add(root);
            }
            for (List<File> files : resources.values()) {
                for (File toDelete : files) {
                    if (!rs.contains(toDelete)) {
                        toDelete.delete();
                        if (toDelete.getName().endsWith(FileObjects.CLASS)) {
                            String className = FileObjects.getBinaryName(toDelete,classCache);
                            sa.delete(className,null);
                            if (removed != null) {
                                removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                                removedFiles.add(toDelete);
                            }
                        }
                    }
                }
            }            
            final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(bootPath,compilePath,sourcePath,
                    filter,true,this.ignoreExcludes.contains(root),!virtualFilesToCompile.isEmpty());
            if (!virtualFilesToCompile.isEmpty()) {
                final Iterable<VirtualSourceProviderQuery.Binding> fos = VirtualSourceProviderQuery.translate(virtualFilesToCompile, rootFile);
                for (VirtualSourceProviderQuery.Binding fo : fos) {
                    ClasspathInfoAccessor.getINSTANCE().registerVirtualSource(cpInfo, fo.virtual);
                    if (generateVirtual) {
                        toCompile.add(new CompileTuple(fo.virtual,fo.original,fo.index));
                    }
                }
            }
            if (!toCompile.isEmpty()) {                
                //System.err.println("toCompile=" + toCompile);
                errorBadgesToRefresh.addAll(batchCompile(toCompile, rootFo, cpInfo, sa, dirtyCrossFiles,
                        compiledFiles, allowCancel ? canceled : null, added,
                        isInitialCompilation ? RepositoryUpdater.this.closed:null, toRecompile, misplacedSource2FQNs, addedFiles));
            }
            Set<ElementHandle<TypeElement>> _at = null;
            Set<ElementHandle<TypeElement>> _rt = null;
            if (added != null && !RepositoryUpdater.this.closed.get()) {
                assert removed != null;
                assert addedFiles != null;
                assert removedFiles != null;
                _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
                _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
                _at.removeAll(removed);
                _rt.removeAll(added);
                added.retainAll(removed);                                                                   //Changed
                removedFiles.removeAll(addedFiles);
                if (toRecompile != null) {
                    toRecompile.addAll(RebuildOraculum.findAllDependent(rootFile, null, cpInfo.getClassIndex(), _rt));
                }
            }
            sa.store();
            synchronized (RepositoryUpdater.this) {
                if (url2Recompile.get(root) == null) {
                    setAttribute(root, DIRTY_ROOT, null); //NOI18N
                }
            }
            if (TasklistSettings.isTasklistEnabled()) {
                //delete .err files for files that were deleted while the IDE was stopped:
                for (File f : existingFilesInError) {
                    errorBadgesToRefresh.addAll(TaskCache.getDefault().dumpErrors(root, f.toURI().toURL(), f, Collections.<Diagnostic>emptyList()));
                }
                if (TasklistSettings.isBadgesEnabled() && !errorBadgesToRefresh.isEmpty()) {
                    ErrorAnnotator an = ErrorAnnotator.getAnnotator();

                    if (an != null) {
                        an.updateInError(errorBadgesToRefresh);
                    }
                }

                JavaTaskProvider.refresh(rootFo);
            }
            if (added != null && !RepositoryUpdater.this.closed.get()) {
                assert removed != null;
                assert _at != null;
                assert _rt != null;
                uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
                        _rt.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,_rt),
                        added.isEmpty() ? null : new ClassIndexImplEvent (uqImpl,added));
                BuildArtifactMapperImpl.classCacheUpdated(root, classCache, removedFiles, addedFiles);
            }            
        }
        
        private void updateFolder(final URL folder, final URL root, final boolean isInitialCompilation, boolean clean) throws IOException {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                return;
            }            
            if (!rootFo.isFolder()) {
                LOGGER.warning("Source root has to be a folder: " +  FileUtil.getFileDisplayName(rootFo)); // NOI18N
                return;
            }            
            final ClassPath sourcePath = ClassPath.getClassPath(rootFo,ClassPath.SOURCE);
            final ClassPath bootPath = ClassPath.getClassPath(rootFo, ClassPath.BOOT);
            final ClassPath compilePath = ClassPath.getClassPath(rootFo, ClassPath.COMPILE);
            if (sourcePath == null || bootPath == null || compilePath == null) {
                String rootName = FileUtil.getFileDisplayName(rootFo);
                if (warnedIgnoredRoots.add(rootName)) {
                    LOGGER.warning("Ignoring root with no ClassPath: " + rootName);    // NOI18N
                }
                return;
            }
            //listen on the particular boot&compile classpaths:
            registerClassPath(root, bootPath, ClasspathInfo.PathKind.BOOT);
            registerClassPath(root, compilePath, ClasspathInfo.PathKind.COMPILE);
            registerClassPath(root, sourcePath, ClasspathInfo.PathKind.SOURCE);
            try {                                
                final File rootFile = FileUtil.toFile(rootFo);
                if (rootFile == null) {
                    return ;
                }
                final File folderFile = isInitialCompilation ? rootFile : FileUtil.normalizeFile(new File (URI.create(folder.toExternalForm())));
                //Preprocessor support
                JavaFileFilterImplementation filter = filters.get(root);
                if (filter == null) {
                    filter = JavaFileFilterQuery.getFilter(rootFo);
                    if (filter != null) {
                        if (!filters.values().contains(filter)) {
                            filter.addChangeListener(filterListener);
                        }
                        filters.put(root, filter);
                    }
                }
                final File classCache = Index.getClassFolder(rootFile);
                final Map<URI, List<String>> misplacedSource2FQNs = new HashMap<URI, List<String>>();
                Map<String, List<File>> resources = Collections.<String, List<File>>emptyMap();
                final FileList children = new FileList(folderFile);
                Set<File> compiledFiles = new HashSet<File>();
                if (isInitialCompilation) {
                    root2DebugData.put(root, children.getJavaFiles().size() + ":" + children.getVirtualJavaFiles().size());
                }
                parseFiles(root, classCache, isInitialCompilation,
                        children.getJavaFiles(), children.getVirtualJavaFiles(),
                        clean, filter, null, compiledFiles, null, misplacedSource2FQNs,
                        false, true, children, folderFile);
                
                if (!misplacedSource2FQNs.isEmpty()) {
                    LOGGER.log(Level.FINE, "misplaces classes detected");
                    if (LOGGER.isLoggable(Level.FINEST)) {                        
                        LOGGER.log(Level.FINEST, "misplacedSource2FQNs={0}", misplacedSource2FQNs);
                    }
                    
                    resources = new HashMap<String, List<File>>();
                    
                    gatherResourceForParseFilesFromRoot(compiledFiles, rootFile, classCache, resources);
                    
                    parseFiles(root, classCache, isInitialCompilation,
                            compiledFiles, children.getVirtualJavaFiles(),
                            true, filter, resources, null, null, misplacedSource2FQNs, false,false);
                }
            } catch (OutputFileManager.InvalidSourcePath e) {
                //Deleted project, ignore
            }
            catch (MissingPlatformError mp) {
                //No platform ignore
            } finally {
                if (!clean && isInitialCompilation) {
                    RepositoryUpdater.this.scannedRoots.add(root);
                }
            }
        }
        
        private void updateFile (final URL file, final URL root, final boolean virtual, Collection<File> toRebuild) throws IOException {
            final FileObject fo = URLMapper.findFileObject(file);
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (fo == null || rootFo == null) {
                return ;
            }
            
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
            if (uqImpl != null) {
                try {
                    uqImpl.setDirty(null);
                    final JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(fo);
                    final File rootFile = FileUtil.toFile(rootFo);
                    final File fileFile = FileUtil.toFile(fo);
                    final File classCache = Index.getClassFolder (rootFile);
                    final Map <String,List<File>> resources = getAllClassFiles (classCache, FileObjects.getRelativePath(rootFile, fileFile.getParentFile()),false);
                    final String relativePath = FileObjects.getRelativePath (rootFile,fileFile);
                    final int index = relativePath.lastIndexOf('.');  //NOI18N
                    final String offset = (index > -1) ? relativePath.substring(0,index) : relativePath;                    
                    List<File> files = resources.remove (offset);                
                    SourceAnalyser sa = uqImpl.getSourceAnalyser();
                    assert sa != null;
                    Set<Pair<String,String>> classNamesToDelete = new HashSet<Pair<String,String>>();
                    final Set<ElementHandle<TypeElement>> added = new HashSet<ElementHandle<TypeElement>>();
                    final Set<ElementHandle<TypeElement>> removed = new HashSet <ElementHandle<TypeElement>> ();
                    final Set<File> addedFiles = new HashSet<File>();
                    final Set<File> removedFiles = new HashSet<File> ();
                    if (files != null) {
                        String sourceName = null;
                        String rsFileBinaryName = null;
                        for (File toDelete : files) {
                            toDelete.delete();
                            final String ext = FileObjects.getExtension(toDelete.getName());
                            if (FileObjects.CLASS.equals(ext)) {
                                String className = FileObjects.getBinaryName (toDelete,classCache);
                                if (sourceName != null && !rsFileBinaryName.equals(className)) {
                                    classNamesToDelete.add(Pair.<String,String>of(className,sourceName));
                                }
                                else {
                                    classNamesToDelete.add(Pair.<String,String>of(className,null));
                                }                                
                                removed.add (ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                                removedFiles.add(toDelete);
                            }
                            else if (FileObjects.RS.equals(ext) || FileObjects.RX.equals(ext)) {
                                //The RS files comes as first in toDelete
                                sourceName = relativePath;
                                rsFileBinaryName = FileObjects.getBinaryName (toDelete,classCache);
                            }
                        }
                    }
                    else {
                        classNamesToDelete.add(Pair.<String,String>of (FileObjects.convertFolder2Package(offset, '/'),null));  //NOI18N
                    }
                    final ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create (fo, filter, true, false, virtual);   //Todo: shouldn't use rather root for virtual files, does virtual source provide ClassPath?
                    ClassPath.Entry entry = getClassPathEntry (cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),root);
                    if (entry == null || entry.includes(fo)) {                        
                        final CompilerListener listener = new CompilerListener ();                        
                        final JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo);                
                        final String sourceLevel = SourceLevelQuery.getSourceLevel(fo); //Todo: shouldn't use rather root for virtual files, does virtual source provide source level?
                        final Collection<CompileTuple> active = new LinkedList<CompileTuple>();
                        if (virtual) {
                            final Iterable<VirtualSourceProviderQuery.Binding> jfos = VirtualSourceProviderQuery.translate(Collections.singleton(fileFile), rootFile);
                            for (VirtualSourceProviderQuery.Binding jfo : jfos) {
                                ClasspathInfoAccessor.getINSTANCE().registerVirtualSource(cpInfo, jfo.virtual);
                                active.add (new CompileTuple(jfo.virtual,jfo.original,jfo.index));
                            }

                        }
                        else {
                            active.add (new CompileTuple(FileObjects.nbFileObject(fo, rootFo, filter, false),fileFile));
                        }
                        if (!active.isEmpty()) {
                            JavacTaskImpl jt = JavaSourceAccessor.getINSTANCE().createJavacTask(cpInfo, listener, sourceLevel);                            
                            if (LOGGER.isLoggable(Level.FINEST)) {
                                LOGGER.finest("Created new javac for: " + FileUtil.getFileDisplayName(fo)+ " "+ cpInfo.toString());   //NOI18N
                            }                            
                            jt.setTaskListener(listener);
                            boolean hasMain = false;
                            for (CompileTuple tuple : active) {
                                Iterable<? extends CompilationUnitTree> trees = jt.parse(tuple.jfo);
                                Iterable<? extends TypeElement> classes = jt.enterTrees(trees);
                                if (toRebuild != null) {
                                    Map<ElementHandle, Collection<String>> members = RebuildOraculum.sortOut(jt.getElements(), classes);
                                    toRebuild.addAll(RebuildOraculum.get().findFilesToRebuild(rootFile, file, cpInfo, members));
                                }
                                jt.analyze ();
                                boolean[] main = new boolean[1];
                                sa.analyse(trees, jt, fm, tuple.virtual, tuple.indexable,
                                        tuple.virtual ? FileObjects.fileFileObject(tuple.file, rootFile, null) : tuple.jfo,
                                        added, main);
                                Log.instance(jt.getContext()).nerrors = 0;
                                hasMain |= main[0];                                
                            }
                            ExecutableFilesIndex.DEFAULT.setMainClass(root, fo.getURL(), hasMain);
                            for (Pair<String,String> s : classNamesToDelete) {
                                sa.delete(s);
                            }

                            if (!virtual) { //Don't report errors in virtual files
                                assert active.size() == 1;
                                
                                JavaFileObject jfo = active.iterator().next().jfo;
                                List<Diagnostic> diag = new ArrayList<Diagnostic>();
                                for (Diagnostic d : listener.errors) {
                                    if (jfo == d.getSource()) {
                                        diag.add(d);
                                    }
                                }
                                for (Diagnostic d : listener.warnings) {
                                    if (jfo == d.getSource()) {
                                        diag.add(d);
                                    }
                                }
                                if (TasklistSettings.isTasklistEnabled()) {
                                    Set<URL> toRefresh = TaskCache.getDefault().dumpErrors(root, file, fileFile, diag);

                                    if (TasklistSettings.isBadgesEnabled()) {
                                        //XXX: maybe move to the common path (to be used also in the else branch:
                                        ErrorAnnotator an = ErrorAnnotator.getAnnotator();

                                        if (an != null) {
                                            an.updateInError(toRefresh);
                                        }
                                    }

                                    JavaTaskProvider.refresh(fo);
                                }
                            }
                            for (JavaFileObject generated : jt.generate()) {
                                if (generated instanceof OutputFileObject) {
                                    addedFiles.add(((OutputFileObject) generated).getFile());
                                } else {
                                    //XXX: log (presumably should not happen)
                                }
                            }                            
                            listener.cleanDiagnostics();
                            jt.finish();
                        } else {
                            //Todo: clean up this repeated code
                            for (Pair<String,String> s : classNamesToDelete) {
                                sa.delete(s);
                            }
                        }
                    }

                    Set<ElementHandle<TypeElement>> _at = new HashSet<ElementHandle<TypeElement>> (added);      //Added
                    Set<ElementHandle<TypeElement>> _rt = new HashSet<ElementHandle<TypeElement>> (removed);    //Removed
                    _at.removeAll(removed);
                    _rt.removeAll(added);
                    added.retainAll(removed);                                                                   //Changed
                    
                    removedFiles.removeAll(addedFiles);
                    
                    if (toRebuild != null) {
                        toRebuild.addAll(RebuildOraculum.findAllDependent(rootFile, null, cpInfo.getClassIndex(), _rt));
                    }

                    sa.store();
                    
                    if (!closed.get()) {
                        uqImpl.typesEvent(_at.isEmpty() ? null : new ClassIndexImplEvent(uqImpl, _at),
                                _rt.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,_rt), 
                                added.isEmpty() ? null : new ClassIndexImplEvent(uqImpl,added));                
                        BuildArtifactMapperImpl.classCacheUpdated(root, classCache, removedFiles, addedFiles);
                    }
                } catch (OutputFileManager.InvalidSourcePath e) {
                    return ;
                }
                 catch (MissingPlatformError mp) {
                     //Broken platform, ignore
                     return;
                 }
            }
        }
        
        private List<File> delete (final URL file, final URL root, final boolean folder, final boolean virtual) throws IOException {
            List<File> toReparse = null;
            assert "file".equals(root.getProtocol()) : "Unexpected protocol of URL: " + root;   //NOI18N
            final File rootFile = FileUtil.normalizeFile(new File (URI.create(root.toExternalForm())));
            assert "file".equals(file.getProtocol()) : "Unexpected protocol of URL: " + file;   //NOI18N
            final File fileFile = FileUtil.normalizeFile(new File (URI.create(file.toExternalForm())));
            final String offset = FileObjects.getRelativePath (rootFile,fileFile);
            assert offset != null && offset.length() > 0 : String.format("File %s not under root %s ", fileFile.getAbsolutePath(), rootFile.getAbsolutePath());  // NOI18N                        
            final File classCache = Index.getClassFolder (rootFile);
            final File[] affectedFiles = getAffectedCacheFiles(offset, classCache, folder, virtual);            
            if (affectedFiles != null && affectedFiles.length > 0) {
                Set<ElementHandle<TypeElement>> removed = new HashSet<ElementHandle<TypeElement>>();
                final ClassIndexImpl uqImpl = ClassIndexManager.getDefault().createUsagesQuery(root, true);
                assert uqImpl != null;                
                final SourceAnalyser sa = uqImpl.getSourceAnalyser();
                assert sa != null;
                List<Pair<String,String>> names = new ArrayList<Pair<String,String>>();
                getFiles (affectedFiles, classCache, names, removed);
                //find dependent files:
                FileObject rootFO = FileUtil.toFileObject(rootFile);
                if (rootFO != null) {
                    final JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(rootFO);
                    ClasspathInfo cpInfo = ClasspathInfoAccessor.getINSTANCE().create(rootFO, filter, true, false, false);
                    toReparse = RebuildOraculum.findAllDependent(rootFile, null, cpInfo.getClassIndex(), removed);
                }
                //actually delete the sig files:
                for (Pair<String,String> s : names) {
                    sa.delete(s);
                }
                sa.store();
                if (!closed.get()) {
                    uqImpl.typesEvent(null,new ClassIndexImplEvent(uqImpl, removed), null);
                }
            }
            
            return toReparse;
        }
        
        private void getFiles (final File[] affectedFiles, final File classCache, final List<? super Pair<String,String>> names, Set<ElementHandle<TypeElement>> removed) throws IOException {
            for (File f : affectedFiles) {
                if (f.isDirectory()) {
                    getFiles (f.listFiles(),classCache, names, removed);
                }
                else {
                    final String ext = FileObjects.getExtension(f.getName());
                    if (FileObjects.RS.equals(ext)) {
                        final List<File> rsFiles = new LinkedList<File>();
                        readRSFile(f, classCache, rsFiles);
                        final String relativePath = FileObjects.getRelativePath (classCache,f);
                        final String rsFileBinaryName = FileObjects.getBinaryName(f, classCache);
                        final String sourceName = relativePath.substring(0, relativePath.length() - FileObjects.RS.length()) + FileObjects.JAVA;
                        for (File rsf : rsFiles) {
                            String className = FileObjects.getBinaryName (rsf,classCache);
                            if (!rsFileBinaryName.equals(className)) {
                                names.add(Pair.<String,String>of (className,sourceName));
                            }
                            else {
                                names.add(Pair.<String,String>of (className,null));
                            }
                            removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                            rsf.delete();
                        }
                    }
                    else if (FileObjects.RX.equals(ext)) {
                        final List<File> rxFiles = new LinkedList<File>();
                        readRSFile(f, classCache, rxFiles);
                        for (File rsf : rxFiles) {
                            String className = FileObjects.getBinaryName (rsf,classCache);                            
                            names.add(Pair.<String,String>of (className,null));                            
                            removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                            rsf.delete();
                        }
                    }
                    else {
                        String className = FileObjects.getBinaryName (f,classCache);                                                                        
                        names.add(Pair.<String,String>of (className,null));
                        removed.add(ElementHandleAccessor.INSTANCE.create(ElementKind.OTHER, className));
                    }
                }
                f.delete();                    
            }
        }
        
        
        private void updateBinary (final URL file, final URL root) throws IOException {            
            CachingArchiveProvider.getDefault().clearArchive(root);                       
            File cacheFolder = Index.getClassFolder(root);
            FileObjects.deleteRecursively(cacheFolder);
            ClassIndexImpl uq = ClassIndexManager.getDefault().createUsagesQuery(root, false);
            if (uq == null) {
                return ; //IDE is exiting, indeces are already closed.
            }
            final BinaryAnalyser ba = uq.getBinaryAnalyser();
            if (ba != null) {   //ba == null => IDE is exiting, indexing will be done on IDE restart
                //todo: may also need interruption.
                try {
                    BinaryAnalyser.Result finished = ba.start(root, new AtomicBoolean(false), new AtomicBoolean(false));
                    while (finished == BinaryAnalyser.Result.CANCELED) {
                        finished = ba.resume();
                    }
                } finally {
                    ba.finish();
                }
            }
        }                
        
        private void recompile() throws IOException {
            Map<URL, Collection<File>> toRecompile = new HashMap<URL, Collection<File>>();
            synchronized (RepositoryUpdater.this) {
                toRecompile.putAll(url2Recompile);
                url2Recompile.clear();
                recompileScheduled = false;
            }
            
            LOGGER.log(Level.FINEST, GOING_TO_RECOMPILE, toRecompile);
            
            toRecompile = compileFileFromRoots(toRecompile, true, false, null);
            
            if (!toRecompile.isEmpty()) {
                synchronized (RepositoryUpdater.this) {
                    for (URL root : toRecompile.keySet()) {
                        Collection<File> storedFiles = url2Recompile.get(root);
                        
                        if (storedFiles == null) {
                            url2Recompile.put(root, storedFiles = new LinkedHashSet<File>());
                        }
                        
                        storedFiles.addAll(toRecompile.get(root));
                    }
                    
                    if (!recompileScheduled) {
                        submit(Work.recompile());
                        recompileScheduled = true;
                    }
                }
            }
        }
        
        private void compileWithDeps(URL root, Collection<File> storedFiles) throws IOException {
            Map<URL, Collection<File>> depsToRecompile;
            
            if (TasklistSettings.isTasklistEnabled() && TasklistSettings.isDependencyTrackingEnabled()) {
                depsToRecompile = new HashMap<URL, Collection<File>>();
            } else {
                depsToRecompile = null;
            }
            
            Map<URL, Collection<File>> toCompile = new HashMap<URL, Collection<File>>();
            
            toCompile.put(root, storedFiles);
            
            if (storedFiles.size() == 1) {
                final File file = storedFiles.iterator().next();
                URL currentFile = file.toURI().toURL();
                List<File> toRebuild = depsToRecompile != null ? new LinkedList<File>() : null;
                
                updateFile(currentFile, root, !isJava(FileObjects.getExtension(file.getName())), toRebuild); 
                
                if (depsToRecompile != null) {
                    depsToRecompile.put(root, toRebuild);
                }
            } else {
                Map<URL, Collection<File>> result = compileFileFromRoots(toCompile, false, true, depsToRecompile);

                assert result.isEmpty(); //not cancellable
            }
            
            if (depsToRecompile != null && !depsToRecompile.isEmpty()) {
                assert depsToRecompile.containsKey(root);
                
                Set<File> thisFiles = new LinkedHashSet<File>(depsToRecompile.get(root));

                thisFiles.removeAll(storedFiles);

                if (!thisFiles.isEmpty()) {
                    assureRecompiled(root, thisFiles);
                } else {
                    setAttribute(root, DIRTY_ROOT, null); //NOI18N
                }
            } else {
                setAttribute(root, DIRTY_ROOT, null); //NOI18N
            }
        }
        
        private Map<URL, Collection<File>> compileFileFromRoots(Map<URL, Collection<File>> toRecompile, final boolean cancellable,  final boolean useVirtual, final Map<URL, Collection<File>> depsToRecompile) throws IOException {
            List<URL> handledRoots = new LinkedList<URL>();
            
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryUpdater.class,"MSG_RefreshingWorkspace"));
            
            handle.start();
            
            try {
                for (Iterator<URL> it = toRecompile.keySet().iterator(); it.hasNext(); ) {
                    URL root = it.next();
                    handledRoots.add(root);
                    FileObject rootFO = URLMapper.findFileObject(root);
                    if (rootFO == null) {
                        LOGGER.info("Root folder: " + root +" doesn't exist.");    //NOI18N
                        it.remove();
                        continue;
                    }
                    long start = System.currentTimeMillis();
                    final JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(rootFO);
                    final File cacheRoot = Index.getClassFolder(root);
                    Collection<File> files = toRecompile.get(root);

                    long cur = System.currentTimeMillis();

                    Set<File> compiledFiles = cancellable ? new HashSet<File>() : null;
                    Map<String, List<File>> resources = new HashMap<String, List<File>>();
                    File rootFile = FileUtil.toFile(rootFO);

                    try {
                        Set<File> thisDepsToRecompile;

                        if (depsToRecompile != null) {
                            thisDepsToRecompile = new LinkedHashSet<File>();
                        } else {
                            thisDepsToRecompile = null;
                        }

                        gatherResourceForParseFilesFromRoot(files, rootFile, cacheRoot, resources);
                        
                        final Map<URI, List<String>> misplacedSource2FQNs = new HashMap<URI, List<String>>();
                        List<? extends File> virtualFiles;
                        if (useVirtual && rootsWithVirtualSource.contains(root)) {
                            final FileList list = new FileList(rootFile);
                            virtualFiles = list.getVirtualJavaFiles();
                        }
                        else {
                            virtualFiles = Collections.<File>emptyList();
                        }
                        parseFiles(root, cacheRoot, false,
                                files, virtualFiles,
                                true, filter, resources, compiledFiles, thisDepsToRecompile, misplacedSource2FQNs, cancellable, true);

                        if (!misplacedSource2FQNs.isEmpty()) {
                            LOGGER.log(Level.FINE, "misplaces classes detected");
                            if (LOGGER.isLoggable(Level.FINEST)) {                                
                                LOGGER.log(Level.FINEST, "misplacedSource2FQNs={0}", misplacedSource2FQNs);
                            }

                            resources.clear();
                            gatherResourceForParseFilesFromRoot(files, rootFile, cacheRoot, resources);
                            
                            parseFiles(root, cacheRoot, false,
                                    files, virtualFiles,
                                    true, filter, resources, compiledFiles, thisDepsToRecompile, misplacedSource2FQNs, cancellable, false);
                        }
                        
                        if (thisDepsToRecompile != null && !thisDepsToRecompile.isEmpty()) {
                            depsToRecompile.put(root, thisDepsToRecompile);
                        }

                        if (compiledFiles != null) {
                            files.removeAll(compiledFiles);

                            if (!files.isEmpty()) {
                                toRecompile.put(root, files);
                                break;
                            } else {
                                it.remove();
                            }
                        } else {
                            it.remove();
                        }
                    } catch (OutputFileManager.InvalidSourcePath e) {
                        //Deleted project
                        it.remove();
                    }
                    catch (MissingPlatformError e) {
                        //Broken platform, ignore
                        it.remove();
                    }

                    Logger.getLogger("TIMER").log(Level.FINE, "Deps - Reparse",
                        new Object[] {rootFO, System.currentTimeMillis() - cur});
                    Logger.getLogger("TIMER").log(Level.FINE, "Deps - Total",
                        new Object[] {rootFO, System.currentTimeMillis() - start});
                }

                handle.finish();
                return toRecompile;
            } finally {
                handle.finish();
            }
        }
        
    }
    
     private static String classPathToString(ClasspathInfo info) throws FileStateInvalidException {
         ClassPath bootPath = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(info, ClasspathInfo.PathKind.BOOT);
         ClassPath compilePath = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(info, ClasspathInfo.PathKind.COMPILE);
         ClassPath sourcePath = info.getClassPath(ClasspathInfo.PathKind.SOURCE);

         StringBuilder sb = new StringBuilder();

         classPathToStringImpl(bootPath, sb);
         classPathToStringImpl(compilePath, sb);
         classPathToStringImpl(sourcePath, sb);

         return sb.toString();
     }
     
     private static void classPathToStringImpl(ClassPath cp, StringBuilder sb) {
         for (ClassPath.Entry e : cp.entries()) {
             URL u = e.getURL();
             
             sb.append(u.toExternalForm());
             
             File f = ClassPathRootsListener.fileForURL(u);
             
             if (f != null) {
                 sb.append("(");
                 if (f.isFile()) {
                     sb.append(f.lastModified());
                 } else {
                     sb.append("-1");
                 }
                 sb.append(")");
             }
             
             sb.append(":");
         }
     }

    public void verifySourceLevel(URL root, String sourceLevel) throws IOException {
        String existingSourceLevel = getAttribute(root, SOURCE_LEVEL_ROOT, null);
        
        if (sourceLevel != null && existingSourceLevel != null && !sourceLevel.equals(existingSourceLevel)) {
            LOGGER.log(Level.FINE, "source level change detected (provided={1}, existing={2}), refreshing whole source root ({0})", new Object[] {root.toExternalForm(), sourceLevel, existingSourceLevel});
            submit(Work.filterChange(Collections.singletonList(root), true));
            return ;
        }
    }
       
    
    static class FileList {
    
        private final File root;
        private final List<File> javaFiles = new LinkedList<File>();
        private final List<File> virtualJavaFiles = new LinkedList<File>();
        private boolean initialized;
        private String digest;

        public FileList (final File root) {
            assert root != null;
            this.root = root;
        }


        public List<? extends File> getJavaFiles () {
            init();
            return Collections.unmodifiableList(this.javaFiles);
        }        
        
        public List<? extends File> getVirtualJavaFiles () {
            init();
            return Collections.unmodifiableList(this.virtualJavaFiles);
        }
        
        private synchronized void init () {
            if (!initialized) {                
                collectFiles (root, javaFiles, virtualJavaFiles);
                computeDigest(root, javaFiles);
                initialized = true;
            }
        }

        private boolean resolveDigest(URL rootUrl) {
            try {
                return ensureAttributeValue(rootUrl, DIGEST, digest, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return true;
            }
        }

        private void computeDigest(File root, final List<File> javaFiles) {
            StringBuilder sb = new StringBuilder(200);
            for (File f : javaFiles) {
                sb.append(f.getPath()).append(f.lastModified());
            }
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
                byte[] b = sb.toString().getBytes();
                byte[] digest = md5.digest(b);
                this.digest = printDigest(digest);
            } catch (NoSuchAlgorithmException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private String printDigest(byte[] digest) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xFF & digest[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }
        
        private static void collectFiles (final File root, final List<? super File> javaFiles,
                final List<? super File> virtualJavaFiles) {
            final File[] content = root.listFiles();
            if (content != null) {
                for (File child : content) {
                    final String name = child.getName();
                    if (child.isDirectory() && !ignoredDirectories.contains(name)) {
                        collectFiles(child, javaFiles, virtualJavaFiles);
                    }
                    else if (name.endsWith('.'+JavaDataLoader.JAVA_EXTENSION)) { //NOI18N
                        javaFiles.add(child);
                    }
                    else if (VirtualSourceProviderQuery.hasVirtualSource(child)) {
                        virtualJavaFiles.add(child);
                    }
                }
            }
        }        
    }

    boolean waitWorkStarted() throws InterruptedException {
        boolean waited = false;
        while (true) {
            boolean sleep;
            synchronized (this) {
                sleep = compileScheduled > 0 || !url2Recompile.isEmpty() || !recompileFilesWithErrors.isEmpty();
            }
                
            if (sleep) {
                Thread.sleep(100);
                waited = true;
            } else {
                break;
                    }
                }
        return waited;
            }
            
    private static class CompilerListener implements DiagnosticListener<JavaFileObject>, LowMemoryListener, TaskListener {
                               
        final List<Diagnostic> errors = new LinkedList<Diagnostic> ();
        final List<Diagnostic> warnings = new LinkedList<Diagnostic> ();
        final List<ClassSymbol> justEntered = new LinkedList<ClassSymbol> ();
        final AtomicBoolean lowMemory = new AtomicBoolean ();
        
        void cleanDiagnostics () {
            if (!this.errors.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    for (Diagnostic msg : this.errors) {
                        LOGGER.finer(msg.toString());      //NOI18N
                    }
                }
                this.errors.clear();
            }
            if (!this.warnings.isEmpty()) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    for (Diagnostic msg: this.warnings) {
                        LOGGER.finer(msg.toString());      //NOI18N
                    }
                }
                this.warnings.clear();
            }
            this.justEntered.clear();
        }
        
        List<? extends ClassSymbol> getEnteredTypes () {
            List<ClassSymbol> result = new ArrayList<ClassSymbol>(this.justEntered);
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
                            if (!(sym.sourcefile instanceof FileObjects.ZipFileBase)) {
                                this.justEntered.add(sym);
                            }
                        }
                    }          
                }
            }
        }
        
        public void lowMemory (final LowMemoryEvent event) {
            this.lowMemory.set(true);
        }
    }    
    
    interface Callback {
        public void willCompile(JavaFileObject jfo);
    }
    
    
    static Callback CALLBACK = null;
    
    private static Set<URL> batchCompile (final LinkedList<CompileTuple> toCompile, final FileObject rootFo, final ClasspathInfo cpInfo, final SourceAnalyser sa,
        final Set<URI> dirtyFiles, Set<File> compiledFiles, AtomicBoolean canceled, final Set<? super ElementHandle<TypeElement>> added,
        final AtomicBoolean ideClosed, Set<File> toRecompile, Map<URI, List<String>> misplacedSource2FQNs, Set<File> addedFiles) throws IOException {
        assert toCompile != null;
        assert rootFo != null;
        assert cpInfo != null;
        File rootFile = FileUtil.toFile(rootFo);
        assert rootFile != null;
        CompileTuple activeTuple = null;
        final JavaFileManager fileManager = ClasspathInfoAccessor.getINSTANCE().getFileManager(cpInfo);
        final CompilerListener listener = new CompilerListener ();
        final Map<URI, List<String>> misplacedSource2FQNsLocal = new HashMap<URI, List<String>>(misplacedSource2FQNs);
        
        misplacedSource2FQNs.clear();
        
        Set<URL> toRefresh = new HashSet<URL>();
        LowMemoryNotifier.getDefault().addLowMemoryListener(listener);
        try {
            JavacTaskImpl jt = null;
            try {                
                List<CompileTuple> bigFiles = new LinkedList<CompileTuple>();
                int state = 0;
                boolean isBigFile = false;
                final String sourceLevel = SourceLevelQuery.getSourceLevel(rootFo);
                while (!toCompile.isEmpty() || !bigFiles.isEmpty() || activeTuple != null) {
                    if (canceled != null && canceled.getAndSet(false)) {
                        return toRefresh;
                    }
                    if (ideClosed != null && ideClosed.get()) {
                        return toRefresh;
                    }
                    try {
                        if (listener.lowMemory.getAndSet(false)) {
                            if (jt != null) {
                                jt.finish();
                            }
                            jt = null;
                            listener.cleanDiagnostics();
                            if (state == 1) {
                                break;
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        if (activeTuple == null) {
                            if (!toCompile.isEmpty()) {
                                activeTuple = toCompile.remove(0);
                                isBigFile = false;
                            } else {
                                activeTuple = bigFiles.remove(0);
                                isBigFile = true;
                            }
                            
                            if (CALLBACK != null) {
                                CALLBACK.willCompile(activeTuple.jfo);
                            }
                        }
                        if (jt == null) {
                            jt = JavaSourceAccessor.getINSTANCE().createJavacTask(cpInfo, listener, sourceLevel, new ClassNamesForFileOraculumImpl(misplacedSource2FQNsLocal));
                            jt.setTaskListener(listener);
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(rootFo) + " " + cpInfo.toString());    //NOI18N
                            }
                        }
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            LOGGER.finest("Parsing file: " + activeTuple.jfo.toUri());   //NOI18N
                        }
                        Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[] {activeTuple.jfo});
                        if (listener.lowMemory.getAndSet(false)) {
                            jt.finish();
                            jt = null;
                            listener.cleanDiagnostics();
                            trees = null;
                            if (state == 1) {
                                if (isBigFile) {
                                    break;
                                } else {
                                    bigFiles.add(activeTuple);
                                    activeTuple = null;
                                    state = 0;
                                }
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        Iterable<? extends TypeElement> types = jt.enterTrees(trees);
                        if (!activeTuple.virtual && activeTuple.file != null) {
                            //When the active file is not set (generated virtual source) ignore top level check
                            String expectedTopLevelClassName = activeTuple.file.getName();

                            expectedTopLevelClassName = expectedTopLevelClassName.substring(0, expectedTopLevelClassName.length() - ".java".length());

                            //check for classes living elsewhere:
                            for (TypeElement topLevel : types) {
                                if (topLevel==null) {
                                    //workaround for 6443073
                                    //see Symbol.java:601
                                    //see JavacTaskImpl.java:367
                                    //see also #144315
                                    continue;
                                }

                                if (!expectedTopLevelClassName.equals(topLevel.getSimpleName().toString())) {
                                    List<String> classes = new LinkedList<String>();
                                    JavacElements elements = JavacElements.instance(jt.getContext());

                                    for (TypeElement e : types) {
                                        classes.add(elements.getBinaryName(e).toString());
                                    }

                                    misplacedSource2FQNs.put(activeTuple.file.toURI(), classes);

                                    break;
                                }
                            }
                        }
                        
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
                                    bigFiles.add(activeTuple);
                                    activeTuple = null;
                                    state = 0;
                                }
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        jt.analyze();
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
                                    bigFiles.add(activeTuple);
                                    activeTuple = null;
                                    state = 0;
                                }
                            } else {
                                state = 1;
                            }
                            System.gc();
                            continue;
                        }
                        if (sa != null) {
                            boolean[] main = new boolean[1];
                            sa.analyse(trees, jt, fileManager, activeTuple.virtual, activeTuple.indexable,
                                    activeTuple.virtual ? FileObjects.fileFileObject(activeTuple.file, rootFile, null) : activeTuple.jfo,
                                    added, main);
                            if (activeTuple.file != null) {
                                //When the active file is not set (generated virtual source) ignore executable flag
                                ExecutableFilesIndex.DEFAULT.setMainClass(rootFo.getURL(), activeTuple.file.toURI().toURL(), main[0]);
                            }
                        }                                
                        List<Diagnostic> diag = new ArrayList<Diagnostic>();
                        URI u = activeTuple.jfo.toUri();
                        for (Iterator<Diagnostic> it = listener.errors.iterator(); it.hasNext(); ) {
                            Diagnostic d = it.next();
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer(d.toString());
                            }
                            if (activeTuple.jfo.equals(d.getSource())) {
                                diag.add(d);
                                it.remove();
                            }
                        }
                        
                        for (Iterator<Diagnostic> it = listener.warnings.iterator(); it.hasNext(); ) {
                            Diagnostic d = it.next();
                            if (LOGGER.isLoggable(Level.FINER)) {
                                LOGGER.finer(d.toString());
                            }
                            if (activeTuple.jfo.equals(d.getSource())) {
                                diag.add(d);
                                it.remove();
                            }
                        }
                        
                        if (TasklistSettings.isTasklistEnabled() && activeTuple.file != null && !activeTuple.virtual) {
                            toRefresh.addAll(TaskCache.getDefault().dumpErrors(rootFo.getURL(), u.toURL(), activeTuple.file, diag));
                        }
                        Log.instance(jt.getContext()).nerrors = 0;
                        if (compiledFiles != null && !activeTuple.virtual) {
                            //compiledFiles are not tracked for virtual sources
                            compiledFiles.add(activeTuple.file);
                        }
                        if (toRecompile != null && !activeTuple.virtual) {
                            //todo: enable rebuild oraculum for virtual files
                            Map<ElementHandle, Collection<String>> members = RebuildOraculum.sortOut(jt.getElements(), types);
                            toRecompile.addAll(RebuildOraculum.get().findFilesToRebuild(rootFile, activeTuple.file.toURI().toURL(), cpInfo, members));
                        }
                        for (JavaFileObject generated : jt.generate(types)) {   //Analyzing genlist may be a bit faster
                            if (generated instanceof OutputFileObject) {
                                if (addedFiles != null) {
                                    addedFiles.add(((OutputFileObject) generated).getFile());
                                }
                            } else {
                                //XXX: log (presumably should not happen)
                            }
                        }
                        activeTuple = null;
                        state  = 0;
                    } catch (CouplingAbort a) {
                        //coupling error
                        //TODO: check if the source sig file ~ the source java file:
                        couplingAbort(a, activeTuple.jfo);
                        if (jt != null) {
                            jt.finish();
                        }
                        jt = null;
                        listener.cleanDiagnostics();
                        state = 0;
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.FINEST)) {
                            final ClassPath bootPath   = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
                            final ClassPath classPath  = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
                            final ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
                            final String message = String.format("batchCompile caused an exception Root: %s File: %s Bootpath: %s Classpath: %s Sourcepath: %s",
                                        FileUtil.getFileDisplayName(rootFo),
                                        activeTuple.jfo.toUri().toString(),
                                        bootPath == null   ? null : bootPath.toString(),
                                        classPath == null  ? null : classPath.toString(),
                                        sourcePath == null ? null : sourcePath.toString()
                                        );
                            LOGGER.log(Level.FINEST, message, t);  //NOI18N
                        }
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }
                        else if (t instanceof OutputFileManager.InvalidSourcePath) {
                            //Handled above
                            throw (OutputFileManager.InvalidSourcePath) t;
                        }
                        else if (t instanceof MissingPlatformError) {
                            //Handled above
                            throw (MissingPlatformError) t;
                        }
                        else {
                            if (jt != null) {
                                jt.finish();
                            }
                            //When a javac failed with the Exception mark a file
                            //causing this exceptin as compiled
                            //otherwise tasklist will reschedule the parse again
                            //and the RepositoryUpdater ends in infinite loop of reparse.
                            if (compiledFiles != null) {
                                compiledFiles.add(activeTuple.file);
                            }
                            final URI activeURI = activeTuple.jfo.toUri();
                            jt = null;
                            activeTuple = null;                            
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
                    LOGGER.warning("Not enough memory to compile folder: " + FileUtil.getFileDisplayName(rootFo));    // NOI18N
                }
            } finally {
                if (jt != null) {
                    jt.finish();
                }
            }
        } finally {
            LowMemoryNotifier.getDefault().removeLowMemoryListener(listener);
        }
        
        return toRefresh;
    }
    
    private static Set<String> parseSet(String propertyName, String defaultValue) {
        StringTokenizer st = new StringTokenizer(System.getProperty(propertyName, defaultValue), " \t\n\r\f,-:+!");
        Set<String> result = new HashSet<String>();
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
    
    public static Set<File> getAffectedCacheFiles (final FileObject file, final FileObject root) {
        assert file != null;
        assert root != null;
        File[] res = null;
        File classCache = null;
        try {
            classCache = Index.getClassFolder (root.getURL());
            if (classCache != null) {
                String offset = FileUtil.getRelativePath(root, file);
                if (offset != null) {
                    res = getAffectedCacheFiles(offset.replace('/', File.separatorChar), classCache, false, false);    //NOI18N
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        //never return null unlike getAffectedCacheFiles(Str,File,bool) which has null as a special ret type
        final Set<File> af = new HashSet<File>();
        if (res != null) {
            for (File f : res) {
                try {                                        
                    if (f.getName().endsWith(FileObjects.RS)) {
                        List<File> rsFiles = new LinkedList<File>();
                        readRSFile(f, classCache, rsFiles);                        
                        for (File rsf : rsFiles) {
                            af.add(rsf);
                        }
                    }
                    else {                                        
                        af.add(f);                
                    }
                } catch (IOException e) {                    
                    Exceptions.printStackTrace(e);
                }
            }
        }   
        return af;
    }
    
    private static File[] getAffectedCacheFiles (final String offset, final File classCache,
            final boolean folder, final boolean virtual) {        
        File[] affectedFiles = null;
        if (folder) {
            final File container = new File (classCache, offset);
            affectedFiles = container.listFiles();
        }
        else {
            int slashIndex = offset.lastIndexOf (File.separatorChar);
            int dotIndex = offset.lastIndexOf('.');     //NOI18N
            final File container = slashIndex == -1 ? classCache : new File (classCache,offset.substring(0,slashIndex));
            String[] patterns;
            if (virtual) {
                patterns = new String[] {
                    offset.substring(slashIndex+1) + '.' + FileObjects.RX       //NOI18N
                };
            }
            else {
                final String name = offset.substring(slashIndex+1, dotIndex);
                patterns = new String[] {
                    name + '.',     //NOI18N
                    name + '$'      //NOI18N
                };
            }
            final File[] content  = container.listFiles();
            if (content != null) {
                final List<File> result = new ArrayList<File>(content.length);
                for (File f : content) {
                    final String fname = f.getName();
                    for (int i=0; i< patterns.length; i++) {
                        if (fname.startsWith(patterns[i])) {
                            result.add(f);
                            break;
                        }
                    }
                }
                affectedFiles = result.toArray(new File[result.size()]);
            }
        }
        return affectedFiles;
    }
        
    private static void gatherResources(final File root, final File f, final int oi, final Map<String,List<File>> result) {
        String path = f.getAbsolutePath();
        int extIndex = path.lastIndexOf('.');  //NO18N
        if (extIndex+1+FileObjects.RS.length() == path.length() && path.endsWith(FileObjects.RS)) {
            path = path.substring(oi,extIndex);
            List<File> files = result.get(path);
            if (files == null) {
                files = new LinkedList<File>();
                result.put(path,files);
            }
            files.add(0,f); //the rs file has to be the first
            try {
                readRSFile(f,root, files);
            } catch (IOException ioe) {
                //The signature file is broken, report it but don't stop scanning
                Exceptions.printStackTrace(ioe);
            }
        } else if (extIndex+1+FileObjects.RX.length() == path.length() && path.endsWith(FileObjects.RX)) {
            //Todo: The RX file format is: pkg/name.origext.rx
            extIndex = path.lastIndexOf('.',extIndex-1);
            if (extIndex>0) {
                path = path.substring(oi,extIndex);
                List<File> files = result.get(path);
                if (files == null) {
                    files = new LinkedList<File>();
                    result.put(path,files);
                }
                files.add(0,f); //the rs file has to be the first
                try {
                    readRSFile(f,root, files);
                } catch (IOException ioe) {
                    //The signature file is broken, report it but don't stop scanning
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
        else if (extIndex+1+FileObjects.CLASS.length() == path.length() && path.endsWith(FileObjects.CLASS)) {
            int index = path.indexOf('$',oi);  //NOI18N
            if (index == -1) {
                path = path.substring(oi,extIndex);
            } else {
                path = path.substring(oi,index);
            }
            List<File> files = result.get(path);
            if (files == null) {
                files = new LinkedList<File>();
                result.put(path,files);
            }
            files.add(f);
        }
    }
    
    private static void getAllClassFilesImpl (final File folder, final File root, final int oi, final Map<String,List<File>> result, final boolean recursive) {
        final File[] content = folder.listFiles();
        if (content == null) {
            LOGGER.info("IO error while listing folder: " + folder.getAbsolutePath() +" isDirectory: " + folder.isDirectory() +" canRead: " + folder.canRead());    //NOI18N
            return;
        }
        for (File f: content) {
            if (f.isDirectory() && recursive) {
                getAllClassFilesImpl(f, root, oi,result, recursive);                    
            }
            else {
                gatherResources(root, f, oi, result);
            }
        }
    }
        
    private static void readRSFile (final File f, final File root, final List<? super File> files) throws IOException {
        BufferedReader in = new BufferedReader (new InputStreamReader ( new FileInputStream (f), "UTF-8"));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                File sf = new File (root, FileObjects.convertPackage2Folder(binaryName)+'.'+FileObjects.CLASS);
                files.add(sf);                                        
            }
        } finally {
            in.close();
        }
    }
    
    private static Collection<? extends ElementHandle<TypeElement>> readRefFile (final File f) throws IOException {
        final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
        BufferedReader in = new BufferedReader (new InputStreamReader ( new FileInputStream (f), "UTF-8"));
        try {
            String binaryName;
            while ((binaryName=in.readLine())!=null) {
                result.add(ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, binaryName));
            }
        } finally {
            in.close();
        }
        return result;
    }
    
    public static Collection<? extends ElementHandle<TypeElement>> getRelatedFiles (final File source, final File root) throws IOException {
        assert source != null;
        assert root != null;
        List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
        String path = FileObjects.getRelativePath(root, source);
        File cache = Index.getClassFolder(root.toURI().toURL());
        File f = new File (cache,path+'.'+FileObjects.RX);      //NOI18N
        boolean rf = false;
        if (f.exists()) {
            rf = true;                                   
            result.addAll(RepositoryUpdater.readRefFile(f));
        }
        int index = path.lastIndexOf('.');                      //NOI18N
        if (index>0) {
            path = path.substring(0, index);
        }
        f = new File (cache,path+'.'+FileObjects.RS);           //NOI18N
        if (f.exists()) {
            rf = true;
            result.addAll(RepositoryUpdater.readRefFile(f));

        }
        if (!rf) {            
            index = path.lastIndexOf (File.separatorChar);
            final String parentPath = index == -1 ? "" : path.substring(0,index);
            final String parentPackage = FileObjects.convertFolder2Package(parentPath,File.separatorChar);
            final File container = new File (cache,parentPath);
            final String name = path.substring(index+1);
            String[] patterns = new String[] {
                name + '.',     //NOI18N
                name + '$'      //NOI18N
            };
            final File[] content  = container.listFiles();
            if (content != null) {
                for (File file : content) {
                    String fname = file.getName();
                    for (int i=0; i< patterns.length; i++) {
                        if (fname.startsWith(patterns[i])) {
                            index = fname.lastIndexOf('.');
                            if (index > 0) {
                                fname = fname.substring(0,index);
                            }
                            String binName = parentPackage.length() == 0 ? fname : parentPackage + '.' + fname; //NOI18N
                            result.add (ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, binName));
                            break;
                        }
                    }
                }                
            }                                    
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
    
    
    private class FilterListener implements ChangeListener {
            
        public void stateChanged(ChangeEvent event) {
            Object source = event.getSource();
            if (source instanceof JavaFileFilterImplementation) {
                List<URL> dirtyRoots = new LinkedList<URL> ();
                synchronized (filters) {
                    for (Map.Entry<URL,JavaFileFilterImplementation> e : filters.entrySet()) {
                        if (e.getValue() == source) {
                            dirtyRoots.add(e.getKey());
                        }
                    }
                }
                submit(Work.filterChange(dirtyRoots, true));
            }
        }
    }
    
    public static synchronized RepositoryUpdater getDefault () {
        if (instance == null) {
            instance = new RepositoryUpdater ();
        }
        return instance;
    }        
    
    private static final int MAX_DUMPS = 255;
    
    /**
     * Dumps the source code to the file. Used for parser debugging. Only a limited number
     * of dump files is used. If the last file exists, this method doesn't dump anything.
     *
     * @param  info  CompilationInfo for which the error occurred.
     * @param  exc  exception to write to the end of dump file
     */
    public static void couplingAbort(CouplingAbort a, JavaFileObject source) {
        String dumpDir = System.getProperty("netbeans.user") + "/var/log/"; //NOI18N
        JavaFileObject classSource = a.getClassFile();
        String uri = classSource != null ? classSource.toUri().toASCIIString() : "<unknown>";
        String origName = classSource != null ? classSource.getName() : "unknown";
        File f = new File(dumpDir + origName + ".dump"); // NOI18N
        boolean dumpSucceeded = false;
        int i = 1;
        while (i < MAX_DUMPS) {
            if (!f.exists())
                break;
            f = new File(dumpDir + origName + '_' + i + ".dump"); // NOI18N
            i++;
        }
        if (!f.exists()) {
            try {
                OutputStream os = new FileOutputStream(f);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8")); // NOI18N
                try {
                    writer.println(String.format("Coupling error: class file %s, source file %s", uri, source.toUri().toASCIIString()));
                    writer.println("----- Sig file content: -------------------------------------------"); // NOI18N
                    if (classSource == null) {
                        writer.println("no content"); //NOI18N
                    } else {
                        if (classSource.getName().toLowerCase().endsWith('.'+FileObjects.SIG)) { // NOI18N
                            writer.println(classSource.getCharContent(true));
                        } else {
                            writer.println("not a sig file"); // NOI18N
                        }
                    }
                    writer.println("----- Source file content: ----------------------------------------"); // NOI18N
                    writer.println(source.getCharContent(true));
                    writer.println("----- Tree: -------------------------------------------------------"); // NOI18N
                    writer.println(a.getTree().toString());
                    writer.println("----- Coupling Error: ---------------------------------------------"); // NOI18N
                    a.printStackTrace(writer);
                } finally {
                    writer.close();
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing coupling dump file!", ioe); // NOI18N
            }
        }
        if (dumpSucceeded) {
            LOGGER.log(Level.SEVERE, "Coupling error: class file {0}, source file {1}", new Object[] {uri, source.toUri().toASCIIString()});
        } else {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }
    
    private static class CompileTuple {
        public final JavaFileObject jfo;
        public final File file;
        public final boolean virtual;
        public final boolean indexable;
        
        public CompileTuple (final JavaFileObject jfo, final File file) {
            this.jfo = jfo;
            this.file = file;
            this.virtual = false;
            this.indexable = true;
        }
        
        public CompileTuple (final JavaFileObject jfo, final File file, boolean indexable) {
            this.jfo = jfo;
            this.file = file;
            this.virtual = true;
            this.indexable = indexable;
        }
    }

}
