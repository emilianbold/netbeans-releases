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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/** Class representing Java source file opened in the editor.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public final class JavaSource {
    
    
    public static enum Phase {
        MODIFIED,
    
        PARSED,
        
        ELEMENTS_RESOLVED,
    
        RESOLVED,   
    
        UP_TO_DATE;
    
    };
    
    public static enum Priority {
        MAX,
        HIGH,
        ABOVE_NORMAL,
        NORMAL,
        BELOW_NORMAL,
        LOW,
        MIN
    };
    
    
    
    /**
     * This specialization of {@link IOException} signals that a {@link JavaSource#runUserActionTask}
     * or {@link JavaSource#runModificationTask} failed due to lack of memory. The {@link InsufficientMemoryException#getFile}
     * method returns a file which cannot be processed.
     */
    public static final class InsufficientMemoryException extends IOException {        
        
        private FileObject fo;
        
        private InsufficientMemoryException (final String message, final FileObject fo) {
            super (message);
            this.fo = fo;
        }
        
        private InsufficientMemoryException (FileObject fo) {
            this (NbBundle.getMessage(JavaSource.class, "MSG_UnsufficientMemoryException", FileUtil.getFileDisplayName (fo)),fo);
        }
        
        
        /**
         * Returns file which cannot be processed due to lack of memory.
         * @return {@link FileObject}
         */
        public FileObject getFile () {
            return this.fo;
        }        
    }
    
    static {
        JavaSourceAccessor.setINSTANCE (new JavaSourceAccessorImpl ());
     }
    private final Collection<FileObject> files;    
    private final ClasspathInfo classpathInfo;    
    private CompilationInfoImpl currentInfo;
    private java.util.Stack<CompilationInfoImpl> infoStack = new java.util.Stack<CompilationInfoImpl> ();
    
    private static final Logger LOGGER = Logger.getLogger(JavaSource.class.getName());
    /**
     * Returns a {@link JavaSource} instance representing given {@link org.openide.filesystems.FileObject}s
     * and classpath represented by given {@link ClasspathInfo}.
     * @param cpInfo the classpaths to be used.
     * @param files for which the {@link JavaSource} should be created
     * @return a new {@link JavaSource}
     * @throws {@link IllegalArgumentException} if fileObject or cpInfo is null
     */
    public static JavaSource create(final ClasspathInfo cpInfo, final Collection<? extends FileObject> files) throws IllegalArgumentException {
        if (files == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        return create(cpInfo, null, files);
    }
    
    
    /**
     * Returns a {@link JavaSource} instance representing given {@link org.openide.filesystems.FileObject}s
     * and classpath represented by given {@link ClasspathInfo}.
     * @param cpInfo the classpaths to be used.
     * @param files for which the {@link JavaSource} should be created
     * @return a new {@link JavaSource}
     * @throws {@link IllegalArgumentException} if fileObject or cpInfo is null        
     */
    public static JavaSource create(final ClasspathInfo cpInfo, final FileObject... files) throws IllegalArgumentException {
        if (files == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        return create(cpInfo, null, Arrays.asList(files));
    }
    
    private static JavaSource create(final ClasspathInfo cpInfo, final PositionConverter binding, final Collection<? extends FileObject> files) throws IllegalArgumentException {
        try {
            return new JavaSource(cpInfo, binding, files);
        } catch (DataObjectNotFoundException donf) {
            Logger.getLogger("global").warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(donf.getFileObject()));     //NOI18N
        } catch (IOException ex) {            
            Exceptions.printStackTrace(ex);
        }        
        return null;
    }
    
    private static Map<FileObject, Reference<JavaSource>> file2JavaSource = new WeakHashMap<FileObject, Reference<JavaSource>>();
    
    /**
     * Returns a {@link JavaSource} instance associated to given {@link org.openide.filesystems.FileObject},
     * it returns null if the {@link Document} is not associanted with data type providing the {@link JavaSource}.
     * @param fileObject for which the {@link JavaSource} should be found/created.
     * @return {@link JavaSource} or null
     * @throws {@link IllegalArgumentException} if fileObject is null
     */
    public static JavaSource forFileObject(FileObject fileObject) throws IllegalArgumentException {
        if (fileObject == null) {
            throw new IllegalArgumentException ("fileObject == null");  //NOI18N
        }
        if (!fileObject.isValid()) {
            return null;
        }

        try {
            if (   fileObject.getFileSystem().isDefault()
                && fileObject.getAttribute("javax.script.ScriptEngine") != null
                && fileObject.getAttribute("template") == Boolean.TRUE) {
                return null;
            }
            DataObject od = DataObject.find(fileObject);
            
            EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
            
            if (!(ec instanceof CloneableEditorSupport)) {
                //allow creation of JavaSource for .class files:
                if (!("application/x-class-file".equals(FileUtil.getMIMEType(fileObject)) || "class".equals(fileObject.getExt()))) {
                    return null;
                }
            }
        } catch (FileStateInvalidException ex) {
            LOGGER.log(Level.FINE, null, ex);
            return null;
        } catch (DataObjectNotFoundException ex) {
            LOGGER.log(Level.FINE, null, ex);
            return null;
        }
        
        Reference<JavaSource> ref = file2JavaSource.get(fileObject);
        JavaSource js = ref != null ? ref.get() : null;
        if (js == null) {
            if ("application/x-class-file".equals(FileUtil.getMIMEType(fileObject)) || "class".equals(fileObject.getExt())) {   //NOI18N
                ClassPath bootPath = ClassPath.getClassPath(fileObject, ClassPath.BOOT);
                ClassPath compilePath = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
                if (compilePath == null) {
                    compilePath = ClassPathSupport.createClassPath(new URL[0]);
                }
                ClassPath srcPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
                if (srcPath == null) {
                    srcPath = ClassPathSupport.createClassPath(new URL[0]);
                }
                ClassPath execPath = ClassPath.getClassPath(fileObject, ClassPath.EXECUTE);
                if (execPath != null) {
                    bootPath = ClassPathSupport.createProxyClassPath(execPath, bootPath);
                }
                final ClasspathInfo info = ClasspathInfo.create(bootPath, compilePath, srcPath);
                FileObject root = ClassPathSupport.createProxyClassPath(bootPath,compilePath,srcPath).findOwnerRoot(fileObject);
                if (root == null) {
                    return null;
                }
                try {
                    js = new JavaSource (info,fileObject,root);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            } else {
            PositionConverter binding = null;
            if (!"text/x-java".equals(FileUtil.getMIMEType(fileObject)) && !"java".equals(fileObject.getExt())) {  //NOI18N
                for (JavaSourceProvider provider : Lookup.getDefault().lookupAll(JavaSourceProvider.class)) {
                    JavaFileFilterImplementation filter = provider.forFileObject(fileObject);
                    if (filter != null) {
                        binding = new PositionConverter(fileObject, filter);
                        break;
                    }
                }
                if (binding == null)
                    return null;
            }
            js = create(ClasspathInfo.create(fileObject), binding, Collections.singletonList(fileObject));
            }
            file2JavaSource.put(fileObject, new WeakReference<JavaSource>(js));
        }
        return js;
    }
    
    /**
     * Returns a {@link JavaSource} instance associated to the given {@link javax.swing.Document},
     * it returns null if the {@link Document} is not
     * associated with data type providing the {@link JavaSource}.
     * @param doc {@link Document} for which the {@link JavaSource} should be found/created.
     * @return {@link JavaSource} or null
     * @throws {@link IllegalArgumentException} if doc is null
     */
    public static JavaSource forDocument(Document doc) throws IllegalArgumentException {
        if (doc == null) {
            throw new IllegalArgumentException ("doc == null");  //NOI18N
        }
        Reference<?> ref = (Reference<?>) doc.getProperty(JavaSource.class);
        JavaSource js = ref != null ? (JavaSource) ref.get() : null;
        if (js == null) {
            Object source = doc.getProperty(Document.StreamDescriptionProperty);
            
            if (source instanceof DataObject) {
                DataObject dObj = (DataObject) source;
                if (dObj != null) {
                    js = forFileObject(dObj.getPrimaryFile());
                }
            }
        }
        return js;
    }
    
    /**
     * Creates a new instance of JavaSource
     * @param files to create JavaSource for
     * @param cpInfo classpath info
     */
    private JavaSource (ClasspathInfo cpInfo, PositionConverter binding, Collection<? extends FileObject> files) throws IOException {
        this.files = Collections.unmodifiableList(new ArrayList<FileObject>(files));   //Create a defensive copy, prevent modification
        boolean multipleSources = this.files.size() > 1;
        for (Iterator<? extends FileObject> it = this.files.iterator(); it.hasNext();) {
            FileObject file = it.next();
            Logger.getLogger("TIMER").log(Level.FINE, "JavaSource",
                new Object[] {file, this});                
            if (!file.isValid()) {
                if (multipleSources) {
                    LOGGER.warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(file));     //NOI18N
                    it.remove();
                }
                else {
                    DataObject.find(file);  //throws IOE
                }
            }                
        }
        this.classpathInfo = cpInfo;        
    }
    
    private JavaSource (final ClasspathInfo info, final FileObject classFileObject, final FileObject root) throws IOException {
        assert info != null;
        assert classFileObject != null;
        assert root != null;
        this.files = Collections.<FileObject>singletonList(classFileObject);
        this.classpathInfo =  info;
    }
       
    /** Runs a task which permits for controlling phases of the parsing process.
     * You probably do not want to call this method unless you are reacting to
     * some user's GUI input which requires immediate action (e.g. code completion popup). 
     * In all other cases use {@link JavaSourceTaskFactory}.<BR>
     * Call to this method will cancel processing of all the phase completion tasks until
     * this task does not finish.<BR>
     * @see org.netbeans.api.java.source.CancellableTask for information about implementation requirements
     * @param task The task which.
     * @param shared if true the java compiler may be reused by other {@link org.netbeans.api.java.source.CancellableTasks},
     * the value false may have negative impact on the IDE performance.     
     * <div class="nonnormative">
     * <p>
     * It's legal to nest the {@link JavaSource#runUserActionTask} into another {@link JavaSource#runUserActionTask}.
     * It's also legal to nest the {@link JavaSource#runModificationTask} into {@link JavaSource#runUserActionTask},
     * the outer {@link JavaSource#runUserActionTask} does not see changes caused by nested {@link JavaSource#runModificationTask},
     * but the following nested task see them. 
     * </p>
     * </div>
     */
    public void runUserActionTask( final Task<CompilationController> task, final boolean shared) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        
        assert javacLock.isHeldByCurrentThread() || !holdsDocumentWriteLock(files) : "JavaSource.runCompileControlTask called under Document write lock.";    //NOI18N
        
        boolean a = false;
        assert a = true;
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement stackTraceElement = findCaller(Thread.currentThread().getStackTrace());
            if (stackTraceElement != null && warnedAboutRunInEQ.add(stackTraceElement)) {
                LOGGER.warning("JavaSource.runUserActionTask called in AWT event thread by: " + stackTraceElement); // NOI18N
            }
        }
        
        if (this.files.size()<=1) {                        
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }            
                this.javacLock.lock();                                
                try {
                    CompilationInfoImpl currentInfo = null;
                    boolean jsInvalid;
                    Pair<DocPositionRegion, MethodTree> changedMethod = null;
                    synchronized (this) {                        
                        jsInvalid = this.currentInfo == null || (this.flags & INVALID)!=0;
                        currentInfo = this.currentInfo;
                        changedMethod = (currentInfo == null ? null : this.currentInfo.getChangedTree());
                        if (!shared) {
                            this.flags|=INVALID;
                        }                        
                    }                    
                    if (jsInvalid) {
                        boolean needsFullReparse = true;
                        if (changedMethod != null && currentInfo != null) {
                            needsFullReparse = !reparseMethod(currentInfo,
                                    changedMethod.second,
                                    changedMethod.first.getText());
                        }
                        if (needsFullReparse) {
                            currentInfo = createCurrentInfo(this, binding, null);
                        }
                        if (shared) {
                            synchronized (this) {                        
                                if (this.currentInfo == null || (this.flags & INVALID) != 0) {
                                    this.currentInfo = currentInfo;
                                    this.flags&=~INVALID;
                                }
                                else {
                                    currentInfo = this.currentInfo;
                                }
                            }
                        }
                    }
                    assert currentInfo != null;
                    if (shared) {
                        if (!infoStack.isEmpty()) {
                            currentInfo = infoStack.peek();
                        }
                    }
                    else {
                        infoStack.push (currentInfo);
                    }
                    try {
                        final CompilationController clientController = new CompilationController (currentInfo);
                        try {
                            task.run (clientController);
                        } finally {
                            if (shared) {
                                clientController.invalidate();
                            }
                        }
                    } finally {
                        if (!shared) {
                            infoStack.pop ();
                        }
                    }                    
                }
                catch (CompletionFailure e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {                    
                    this.javacLock.unlock();
                }
            } finally {
                currentRequest.cancelCompleted (request);
            }            
        }
        else {
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }
                this.javacLock.lock();
                try {
                    JavacTaskImpl jt = null;
                    FileObject activeFile = null;
                    Iterator<FileObject> files = this.files.iterator();                    
                    while (files.hasNext() || activeFile != null) {
                        boolean restarted;
                        if (activeFile == null) {
                            activeFile = files.next();                            
                            restarted = false;
                        }
                        else {
                            restarted = true;
                        }
                        CompilationInfoImpl ci = createCurrentInfo(this, new PositionConverter(activeFile, null), jt);
                        CompilationController clientController = new CompilationController(ci);
                        try {
                            task.run (clientController);
                        } finally {
                            if (shared) {
                                clientController.invalidate();
                            }
                        }
                        if (!ci.needsRestart) {
                            jt = ci.getJavacTask();
                            Log.instance(jt.getContext()).nerrors = 0;
                            activeFile = null;
                        }
                        else {                            
                            jt = null;
                            ci = null;
                            System.gc();
                            if (restarted) {
                                throw new InsufficientMemoryException (activeFile);
                            }
                        }
                    }                
                } 
                catch (CompletionFailure e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {
                    this.javacLock.unlock();
                }
            } finally {
                currentRequest.cancelCompleted(request);
            }
        }
    }

    private void runUserActionTask( final CancellableTask<CompilationController> task, final boolean shared) throws IOException {
        final Task<CompilationController> _task = task;
        this.runUserActionTask (_task, shared);
    }
    
    
    /**
     * Performs the given task when the scan finished. When no background scan is running
     * it performs the given task synchronously. When the background scan is active it queues
     * the given task and returns, the task is performed when the background scan completes by
     * the thread doing the background scan.
     * @param task to be performed
     * @param shared if true the java compiler may be reused by other {@link org.netbeans.api.java.source.CancellableTasks},
     * the value false may have negative impact on the IDE performance.
     * @return {@link Future} which can be used to find out the sate of the task {@link Future#isDone} or {@link Future#isCancelled}.
     * The caller may cancel the task using {@link Future#cancel} or wait until the task is performed {@link Future#get}.
     * @throws IOException encapsulating the exception thrown by {@link CancellableTasks#run}
     * @since 0.12
     */
    public Future<Void> runWhenScanFinished (final Task<CompilationController> task, final boolean shared) throws IOException {
        assert task != null;
        final ScanSync sync = new ScanSync (task);
        final DeferredTask r = new DeferredTask (this,task,shared,sync);
        //0) Add speculatively task to be performed at the end of background scan
        todo.add (r);
        if (RepositoryUpdater.getDefault().isScanInProgress()) {
            return sync;
        }
        //1) Try to aquire javac lock, if successfull no task is running
        //   perform the given taks synchronously if it wasn't already performed
        //   by background scan.
        final boolean locked = javacLock.tryLock();
        if (locked) {
            try {
                if (todo.remove(r)) {
                    try {
                        runUserActionTask(task, shared);
                    } finally {
                        sync.taskFinished();
                    }
                }
            } finally {
                javacLock.unlock();
            }
        }
        else {
            //Otherwise interrupt currently running task and try to aquire lock
            do {
                final JavaSource.Request[] request = new JavaSource.Request[1];
                boolean isScanner = currentRequest.getUserTaskToCancel(request);
                try {
                    if (isScanner) {
                        return sync;
                    }
                    if (request[0] != null) {
                        request[0].task.cancel();
                    }
                    if (javacLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                        try {
                            if (todo.remove(r)) {
                                try {
                                    runUserActionTask(task, shared);
                                    return sync;
                                } finally {
                                    sync.taskFinished();
                                }
                            }
                            else {
                                return sync;
                            }
                        } finally {
                            javacLock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    throw (InterruptedIOException) new InterruptedIOException ().initCause(e);
                }
                finally {
                    if (!isScanner) {
                        currentRequest.cancelCompleted(request[0]);
                    }
                }
            } while (true);            
        }
        return sync;
    }

    private Future<Void> runWhenScanFinished (final CancellableTask<CompilationController> task, final boolean shared) throws IOException {
        final Task<CompilationController> _task = task;
        return this.runWhenScanFinished (_task, shared);
    }
       
    /** Runs a task which permits for modifying the sources.
     * Call to this method will cancel processing of all the phase completion tasks until
     * this task does not finish.<BR>
     * @see Task for information about implementation requirements
     * @param task The task which.
     */    
    public ModificationResult runModificationTask(Task<WorkingCopy> task) throws IOException {        
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        
        assert javacLock.isHeldByCurrentThread() || !holdsDocumentWriteLock(files) : "JavaSource.runModificationTask called under Document write lock.";    //NOI18N
        
        boolean a = false;
        assert a = true;        
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement stackTraceElement = findCaller(Thread.currentThread().getStackTrace());
            if (stackTraceElement != null && warnedAboutRunInEQ.add(stackTraceElement)) {
                LOGGER.warning("JavaSource.runModificationTask called in AWT event thread by: " + stackTraceElement);     //NOI18N
            }
        }
        
        ModificationResult result = new ModificationResult(this);
        if (this.files.size()<=1) {
            long start = System.currentTimeMillis();            
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }            
                this.javacLock.lock();                                
                try {
                    CompilationInfoImpl currentInfo = null;
                    boolean jsInvalid;
                    Pair<DocPositionRegion,MethodTree> changedMethod;
                    synchronized (this) {
                        jsInvalid = this.currentInfo == null ||  (this.flags & INVALID) != 0;
                        currentInfo = this.currentInfo;
                        changedMethod = currentInfo == null ? null : currentInfo.getChangedTree();
                    }
                    if (jsInvalid) {
                        boolean needsFullReparse = true;
                        if (changedMethod != null && currentInfo != null) {
                            needsFullReparse = !reparseMethod(currentInfo,
                                    changedMethod.second,
                                    changedMethod.first.getText());
                        }
                        if (needsFullReparse) {
                            currentInfo = createCurrentInfo(this, binding, null);
                        }
                        synchronized (this) {
                            if (this.currentInfo == null || (this.flags & INVALID) != 0) {
                                this.currentInfo = currentInfo;
                                this.flags&=~INVALID;
                            }
                            else {
                                currentInfo = this.currentInfo;
                            }
                        }
                    }
                    assert currentInfo != null;                    
                    WorkingCopy copy = new WorkingCopy (currentInfo);
                    task.run (copy);
                    List<Difference> diffs = copy.getChanges();
                    if (diffs != null && diffs.size() > 0)
                        result.diffs.put(currentInfo.getFileObject(), diffs);
                }
                catch (CompletionFailure e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {
                    this.javacLock.unlock();
                }
            } finally {
                currentRequest.cancelCompleted (request);
            }
            Logger.getLogger("TIMER").log(Level.FINE, "Modification Task",
                    new Object[] {currentInfo.getFileObject(), System.currentTimeMillis() - start});
        }
        else {
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }
                this.javacLock.lock();
                try {
                    JavacTaskImpl jt = null;
                    FileObject activeFile = null;
                    Iterator<FileObject> files = this.files.iterator();
                    while (files.hasNext() || activeFile != null) {
                        boolean restarted;
                        if (activeFile == null) {
                            activeFile = files.next();
                            restarted = false;
                        }
                        else {
                            restarted = true;
                        }
                        CompilationInfoImpl ci = createCurrentInfo(this, new PositionConverter(activeFile, null), jt);
                        WorkingCopy copy = new WorkingCopy(ci);
                        task.run(copy);
                        if (!ci.needsRestart) {
                            jt = ci.getJavacTask();
                            Log.instance(jt.getContext()).nerrors = 0;
                            List<Difference> diffs = copy.getChanges();
                            if (diffs != null && diffs.size() > 0)
                                result.diffs.put(ci.getFileObject(), diffs);
                            activeFile = null;
                        }
                        else {
                            jt = null;
                            ci = null;
                            System.gc();
                            if (restarted) {
                                throw new InsufficientMemoryException (activeFile);
                            }
                        }
                    }
                }
                catch (CompletionFailure e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (Exception e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {
                    this.javacLock.unlock();
                }
            } finally {
                currentRequest.cancelCompleted(request);
            }
        }        
        synchronized (this) {
            this.flags|=INVALID;
        }
        return result;
    }


    private ModificationResult runModificationTask(CancellableTask<WorkingCopy> task) throws IOException { 
        final Task<WorkingCopy> _task = task;
        return this.runModificationTask (_task);
    }
         
    /**
     * Returns the classpaths ({@link ClasspathInfo}) used by this
     * {@link JavaSource}
     * @return {@link ClasspathInfo}, never returns null.
     */
    public ClasspathInfo getClasspathInfo() {
	return classpathInfo;
    }
    
    /**
     * Returns unmodifiable {@link Collection} of {@link FileObject}s used by this {@link JavaSource}
     * @return the {@link FileObject}s
     */
    public Collection<FileObject> getFileObjects() {
        return files;
    }
                                                 
    private void resetState (boolean invalidate, boolean updateIndex) {
        resetState (invalidate, updateIndex, false, null);
    }
               
    private void resetState(boolean invalidate, boolean updateIndex, boolean filterOutFileManager, Pair<DocPositionRegion,MethodTree> changedMethod) {
        boolean invalid;
        synchronized (this) {
            invalid = (this.flags & INVALID) != 0;
            this.flags|=CHANGE_EXPECTED;
            if (invalidate) {
                this.flags|=(INVALID|RESCHEDULE_FINISHED_TASKS);
                if (this.currentInfo != null) {
                    this.currentInfo.setChangedMethod (changedMethod);
                }
            }
            if (updateIndex) {
                this.flags|=UPDATE_INDEX;
            }
            if (filterOutFileManager && binding != null && rootFo != null) {
                //No need to be fully synchronized OutputFileManager.setFilteredFiles is idempotent
                OutputFileManager ofm = this.classpathInfo.getOutputFileManager();
                if (ofm != null && !ofm.isFiltered()) {
                    Set<File> files = RepositoryUpdater.getAffectedCacheFiles(binding.getFileObject(), rootFo);
                    ofm.setFilteredFiles(files);                    
                }
            }
        }
        if (updateIndex && !invalid) {
            //First change set the index as dirty
            updateIndex ();
        }
        Request r = currentRequest.getTaskToCancel (invalidate);
        if (r != null) {
            r.task.cancel();
            Request oldR = rst.getAndSet(r);
            assert oldR == null;
        }
        if (!k24) {
            resetTask.schedule(reparseDelay);
        }
    }
    
    private final AtomicReference<Request> rst = new AtomicReference<JavaSource.Request> ();
    private volatile boolean k24;
    
    /**
     * Not synchronized, only sets the atomic state and clears the listeners
     *
     */
    private void resetStateImpl() {
        if (!k24) {
            Request r = rst.getAndSet(null);
            currentRequest.cancelCompleted(r);
            synchronized (INTERNAL_LOCK) {
                boolean reschedule, updateIndex;
                synchronized (this) {
                    reschedule = (this.flags & RESCHEDULE_FINISHED_TASKS) != 0;
                    updateIndex = (this.flags & UPDATE_INDEX) != 0;
                    this.flags&=~(RESCHEDULE_FINISHED_TASKS|CHANGE_EXPECTED|UPDATE_INDEX);
                }            
                if (updateIndex) {
                    //Last change set the index as dirty
                    updateIndex ();
                }
                Collection<Request> cr;            
                if (reschedule) {                
                    if ((cr=JavaSource.finishedRequests.remove(this)) != null && cr.size()>0)  {
                        JavaSource.requests.addAll(cr);
                    }
                }
                if ((cr=JavaSource.waitingRequests.remove(this)) != null && cr.size()>0)  {
                    JavaSource.requests.addAll(cr);
                }
            }          
        }
    }
    
    private void updateIndex () {
        if (this.rootFo != null) {
            try {
                ClassIndexImpl ciImpl = ClassIndexManager.getDefault().getUsagesQuery(this.rootFo.getURL());
                if (ciImpl != null) {
                    ciImpl.setDirty(this);
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
                            
    private class DocListener implements PropertyChangeListener, ChangeListener, TokenHierarchyListener {
        
        private EditorCookie.Observable ec;
        private TokenHierarchyListener lexListener;
        private volatile Document document;
        
        public DocListener (EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            Document doc = ec.getDocument();            
            if (doc != null) {
                TokenHierarchy th = TokenHierarchy.get(doc);
                th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                document = doc;
            }            
        }
                                   
        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();                
                if (old instanceof Document && lexListener != null) {
                    TokenHierarchy th = TokenHierarchy.get((Document) old);
                    th.removeTokenHierarchyListener(lexListener);
                    lexListener = null;
                }                
                Document doc = ec.getDocument();                
                if (doc != null) {
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                    this.document = doc;    //set before rescheduling task to avoid race condition
                    resetState(true, false);
                }
                else {
                    //reset document
                    this.document = doc;
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            JavaSource.this.resetState(true, false);
        }
        
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            Pair<DocPositionRegion,MethodTree> changedMethod = null;
            if (evt.type() == TokenHierarchyEventType.MODIFICATION) {
                if (supportsReparse) {
                    int start = evt.affectedStartOffset();
                    int end = evt.affectedEndOffset();                                                                
                    synchronized (positions) {
                        for (Pair<DocPositionRegion,MethodTree> pe : positions) {
                            PositionRegion p = pe.first;
                            if (start > p.getStartOffset() && end < p.getEndOffset()) {
                                changedMethod = pe;
                                break;
                            }
                        }
                        if (changedMethod != null) {
                            TokenChange<JavaTokenId> change = evt.tokenChange(JavaTokenId.language());
                            if (change != null) {
                                TokenSequence<JavaTokenId> ts = change.removedTokenSequence();
                                if (ts != null) {
                                    while (ts.moveNext()) {
                                        switch (ts.token().id()) {
                                            case LBRACE:
                                            case RBRACE:
                                                changedMethod = null;
                                                break;
                                        }
                                    }
                                }
                                if (changedMethod != null) {
                                    TokenSequence<JavaTokenId> current = change.currentTokenSequence();                
                                    current.moveIndex(change.index());
                                    for (int i=0; i< change.addedTokenCount(); i++) {
                                        current.moveNext();
                                        switch (current.token().id()) {
                                            case LBRACE:
                                            case RBRACE:
                                                changedMethod = null;
                                                break;
                                            }
                                    }
                                }
                            }
                        }
                        positions.clear();
                        if (changedMethod!=null) {
                            positions.add (changedMethod);
                        }
                    }
                }
            }
            JavaSource.this.resetState(true, changedMethod==null, true, changedMethod);
        }        
    }
    
    private static class EditorRegistryListener implements CaretListener, PropertyChangeListener {
                        
        private Request request;
        private JTextComponent lastEditor;
        
        public EditorRegistryListener () {
            EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    editorRegistryChanged();
                }
            });
            editorRegistryChanged();
        }
                
        public void editorRegistryChanged() {
            final JTextComponent editor = EditorRegistry.lastFocusedComponent();
            if (lastEditor != editor) {
                if (lastEditor != null) {
                    lastEditor.removeCaretListener(this);
                    lastEditor.removePropertyChangeListener(this);
                    final Document doc = lastEditor.getDocument();
                    JavaSource js = null;
                    if (doc != null) {
                        js = forDocument(doc);
                    }
                    if (js != null) {
                        js.k24 = false;
                    }                   
                }
                lastEditor = editor;
                if (lastEditor != null) {                    
                    lastEditor.addCaretListener(this);
                    lastEditor.addPropertyChangeListener(this);
                }
            }
        }
        
        public void caretUpdate(CaretEvent event) {
            if (lastEditor != null) {
                Document doc = lastEditor.getDocument();
                if (doc != null) {
                    JavaSource js = forDocument(doc);
                    if (js != null) {
                        js.resetState(false, false);
                    }
                }
            }
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if ("completion-active".equals(propName)) {
                JavaSource js = null;
                final Document doc = lastEditor.getDocument();
                if (doc != null) {
                    js = forDocument(doc);
                }
                if (js != null) {
                    Object rawValue = evt.getNewValue();
                    assert rawValue instanceof Boolean;
                    if (rawValue instanceof Boolean) {
                        final boolean value = (Boolean)rawValue;
                        if (value) {
                            assert this.request == null;
                            this.request = currentRequest.getTaskToCancel(false);
                            if (this.request != null) {
                                this.request.task.cancel();
                            }
                            js.k24 = true;
                        }
                        else {                    
                            Request _request = this.request;
                            this.request = null;                            
                            js.k24 = false;
                            js.resetTask.schedule(js.reparseDelay);
                            currentRequest.cancelCompleted(_request);
                        }
                    }
                }
            }
        }
        
    }
    
    private class FileChangeListenerImpl extends FileChangeAdapter {                
        
        public @Override void fileChanged(final FileEvent fe) {
            JavaSource.this.resetState(true, false);
        }        

        public @Override void fileRenamed(FileRenameEvent fe) {
            JavaSource.this.resetState(true, false);
        }        
    }
    
    private final class DataObjectListener implements PropertyChangeListener {
        
        private DataObject dobj;
        private final FileObject fobj;
        private PropertyChangeListener wlistener;
        
        public DataObjectListener(FileObject fo) throws DataObjectNotFoundException {
            this.fobj = fo;
            this.dobj = DataObject.find(fo);
            wlistener = WeakListeners.propertyChange(this, dobj);
            this.dobj.addPropertyChangeListener(wlistener);
        }
        
        public void propertyChange(PropertyChangeEvent pce) {
            DataObject invalidDO = (DataObject) pce.getSource();
            if (invalidDO != dobj)
                return;
            final String propName = pce.getPropertyName();
            if (DataObject.PROP_VALID.equals(propName)) {
                handleInvalidDataObject(invalidDO);
                resetOutputFileManagerFilter();
            } else if (DataObject.PROP_MODIFIED.equals(propName) && !invalidDO.isModified()) {
                resetOutputFileManagerFilter();
            } else if (pce.getPropertyName() == null && !dobj.isValid()) {
                handleInvalidDataObject(invalidDO);
                resetOutputFileManagerFilter();
            }            
        }
        
        private void handleInvalidDataObject(final DataObject invalidDO) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    handleInvalidDataObjectImpl(invalidDO);
                }
            });
        }
        
        private void resetOutputFileManagerFilter () {
            if (binding != null) {
                OutputFileManager ofm = classpathInfo.getOutputFileManager();
                if (ofm != null) {                    
                    ofm.clearFilteredFiles();                    
                }
            }
        }
        
        private void handleInvalidDataObjectImpl(DataObject invalidDO) {
            invalidDO.removePropertyChangeListener(wlistener);
            if (fobj.isValid()) {
                // file object still exists try to find new data object
                try {
                    DataObject dobjNew = DataObject.find(fobj);
                    synchronized (DataObjectListener.this) {
                        if (dobjNew == dobj) {
                            return;
                        }
                        dobj = dobjNew;
                        dobj.addPropertyChangeListener(wlistener);
                    }
                    assignDocumentListener(dobjNew);
                    resetState(true, true);
                } catch (DataObjectNotFoundException e) {
                    //Ignore - invalidated after fobj.isValid () was called
                } catch (IOException ex) {
                    // should not occur
                    LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                }
            }
        }
        
    }
    
    private static class JavaSourceAccessorImpl extends JavaSourceAccessor {
        
        private StackTraceElement[] javacLockedStackTrace;
        
        protected @Override void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, Priority priority) {
            handleAddRequest(new Request (task, null, null, priority, false));
        }                

        @Override
        public JavacTaskImpl createJavacTask(ClasspathInfo cpInfo, DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel, ClassNamesForFileOraculum oraculum) {
            if (sourceLevel == null)
                sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
            return JavaSource.createJavacTask(cpInfo, diagnosticListener, sourceLevel, true, oraculum);
        }
                
        @Override
        public JavacTaskImpl getJavacTask (final CompilationInfo compilationInfo) {
            assert compilationInfo != null;
            return compilationInfo.impl.getJavacTask();
        }
        
        @Override
        public CompilationInfo getCurrentCompilationInfo (final JavaSource js, final Phase phase) throws IOException {
            assert js != null;
            assert isDispatchThread();
            CompilationInfoImpl info = null;
            synchronized (js) {                     
                if ((js.flags & INVALID)==0) {
                    info = js.currentInfo;
                }
            }
            if (info == null) {
                return null;
            }
            Phase currentPhase = moveToPhase(phase, info, true);                
            return currentPhase.compareTo(phase)<0 ? null : new CompilationInfo(info);
        }

        @Override
        public void revalidate(JavaSource js) {
            js.revalidate();
        }
        
        @Override
        public boolean isDispatchThread () {
            return factory.isDispatchThread(Thread.currentThread());
        }
        
        @Override
        public void lockJavaCompiler () {            
            javacLock.lock();
            try {
                this.javacLockedStackTrace = Thread.currentThread().getStackTrace();
            } catch (RuntimeException e) {
                //Not important, thrown by logging code
            }
        }
        
        @Override
        public void unlockJavaCompiler () {
            try {
                this.javacLockedStackTrace = null;
            } finally {
                javacLock.unlock();
            }
        }
        
        @Override
        public boolean isJavaCompilerLocked() {
            return javacLock.isLocked();
        }

        public JavaSource create(ClasspathInfo cpInfo, PositionConverter binding, Collection<? extends FileObject> files) throws IllegalArgumentException {
            return JavaSource.create(cpInfo, binding, files);
        }

        public PositionConverter create(FileObject fo, int offset, int length, JTextComponent component) {
            return new PositionConverter(fo, offset, length, component);
        }
        
        public CompilationInfo createCompilationInfo (final CompilationInfoImpl impl) {
            return new CompilationInfo(impl);
        }
        
        public CompilationController createCompilationController (final CompilationInfoImpl impl) {
            return new CompilationController(impl);
        }
    }
    
                
    static final class ScanSync implements Future<Void> {
        
        private Task<CompilationController> task;
        private final CountDownLatch sync;
        private final AtomicBoolean canceled;
        
        public ScanSync (final Task<CompilationController> task) {
            assert task != null;
            this.task = task;
            this.sync = new CountDownLatch (1);
            this.canceled = new AtomicBoolean (false);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            if (this.sync.getCount() == 0) {
                return false;
            }
            synchronized (todo) {
                boolean _canceled = canceled.getAndSet(true);
                if (!_canceled) {
                    for (Iterator<DeferredTask> it = todo.iterator(); it.hasNext();) {
                        DeferredTask task = it.next();
                        if (task.task == this.task) {
                            it.remove();
                            return true;
                        }
                    }
                }
            }            
            return false;
        }

        public boolean isCancelled() {
            return this.canceled.get();
        }

        public synchronized boolean isDone() {
            return this.sync.getCount() == 0;
        }

        public Void get() throws InterruptedException, ExecutionException {
            this.sync.await();
            return null;
        }

        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            this.sync.await(timeout, unit);
            return null;
        }
        
        private void taskFinished () {
            this.sync.countDown();
        }            
    }
    
    static final class DeferredTask {
        final JavaSource js;
        final Task<CompilationController> task;
        final boolean shared;
        final ScanSync sync;
        
        public DeferredTask (final JavaSource js, final Task<CompilationController> task, final boolean shared, final ScanSync sync) {
            assert js != null;
            assert task != null;
            assert sync != null;
            
            this.js = js;
            this.task = task;
            this.shared = shared;
            this.sync = sync;
        }
    }
                            
    private static boolean reparseMethod (final CompilationInfoImpl ci, final MethodTree orig, final String newBody) throws IOException {        
        assert ci != null;         
        final FileObject fo = ci.getFileObject();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Reparse method in: " + fo);          //NOI18N
        }
        if (!ci.getJavaSource().supportsReparse) {
            return false;
        }
        if (((JCMethodDecl)orig).localEnv == null) {
            //We are seeing interface method or abstract or native method with body.
            //Don't do any optimalization of this broken code - has no attr env.
            return false;
        }
        final Phase currentPhase = ci.getPhase();
        if (Phase.PARSED.compareTo(currentPhase) > 0) {
            return false;
        }
        try {
            final CompilationUnitTree cu = ci.getCompilationUnit();
            if (cu == null || newBody == null) {
                return false;
            }
            final JavacTaskImpl task = ci.getJavacTask();
            final JavacTrees jt = JavacTrees.instance(task);
            final int origStartPos = (int) jt.getSourcePositions().getStartPosition(cu, orig.getBody());
            final int origEndPos = (int) jt.getSourcePositions().getEndPosition(cu, orig.getBody());
            if (origStartPos > origEndPos) {
                LOGGER.warning("Javac returned startpos: "+origStartPos+" > endpos: "+origEndPos);  //NOI18N
                return false;
            }
            final FindAnnonVisitor fav = new FindAnnonVisitor();
            fav.scan(orig.getBody(), null);
            if (fav.hasLocalClass) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("Skeep reparse method (old local classes): " + fo);   //NOI18N
                }
                return false;
            }
            final int firstInner = fav.firstInner;
            final int noInner = fav.noInner;
            final Context ctx = task.getContext();
            final TreeLoader treeLoader = TreeLoader.instance(ctx);
            if (treeLoader != null) {
                treeLoader.startPartialReparse();
            }
            try {
                final Log l = Log.instance(ctx);
                l.startPartialReparse();
                final JavaFileObject prevLogged = l.useSource(cu.getSourceFile());
                JCBlock block;
                try {
                    DiagnosticListener dl = ctx.get(DiagnosticListener.class);
                    assert dl instanceof CompilationInfoImpl.DiagnosticListenerImpl;
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).startPartialReparse(origStartPos, origEndPos);
                    long start = System.currentTimeMillis();
                    block = task.reparseMethodBody(cu, orig, newBody, firstInner);                
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("Reparsed method in: " + fo);     //NOI18N
                    }
                    assert block != null;
                    fav.reset();
                    fav.scan(block, null);
                    final int newNoInner = fav.noInner;
                    if (fav.hasLocalClass || noInner != newNoInner) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Skeep reparse method (new local classes): " + fo);   //NOI18N
                        }
                        return false;
                    }
                    long end = System.currentTimeMillis();                
                    if (fo != null) {
                        logTime (fo,Phase.PARSED,(end-start));
                    }
                    final int newEndPos = (int) jt.getSourcePositions().getEndPosition(cu, block);
                    final int delta = newEndPos - origEndPos;
                    final Map<JCTree,Integer> endPos = ((JCCompilationUnit)cu).endPositions;
                    final TranslatePosVisitor tpv = new TranslatePosVisitor(orig, endPos, delta);
                    tpv.scan(cu, null);
                    ((JCMethodDecl)orig).body = block;
                    if (Phase.RESOLVED.compareTo(currentPhase)<=0) {
                        start = System.currentTimeMillis();
                        task.reattrMethodBody(orig, block);
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Resolved method in: " + fo);     //NOI18N
                        }
                        if (!((CompilationInfoImpl.DiagnosticListenerImpl)dl).hasPartialReparseErrors()) {
                            final JSFlowListener fl = JSFlowListener.instance(ctx);
                            if (fl != null && fl.hasFlowCompleted(fo)) {
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    final List<? extends Diagnostic> diag = ci.getDiagnostics();
                                    if (!diag.isEmpty()) {
                                        LOGGER.finer("Reflow with errors: " + fo + " " + diag);     //NOI18N
                                    }                            
                                }
                                TreePath tp = TreePath.getPath(cu, orig);       //todo: store treepath in changed method => improve speed
                                Tree t = tp.getParentPath().getLeaf();
                                task.reflowMethodBody(cu, (ClassTree) t, orig);
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    LOGGER.finer("Reflowed method in: " + fo); //NOI18N
                                }
                            }
                        }
                        end = System.currentTimeMillis();
                        if (fo != null) {
                            logTime (fo, Phase.ELEMENTS_RESOLVED,0L);
                            logTime (fo,Phase.RESOLVED,(end-start));
                        }
                    }
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).endPartialReparse (delta);
                } finally {
                    l.endPartialReparse();
                    l.useSource(prevLogged);
                }
                jfoProvider.update(ci.jfo);
            } finally {
              if (treeLoader != null) {
                  treeLoader.endPartialReparse();
              }  
            }
        } catch (CouplingAbort ca) {
            //Needs full reparse
            return false;
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            boolean a = false;
            assert a = true; 
            if (a) {
                dumpSource(ci, t);
            }
            return false;
        }
        return true;
    }
}