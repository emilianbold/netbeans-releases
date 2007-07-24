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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Abort;
import com.sun.tools.javac.util.CancelAbort;
import com.sun.tools.javac.util.CancelService;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javadoc.JavadocEnter;
import com.sun.tools.javadoc.JavadocMemberEnter;
import com.sun.tools.javadoc.Messager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.Scanner;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavadocEnv;
import org.netbeans.modules.java.source.engine.ReattributionException;
import org.netbeans.modules.java.source.engine.RootTree;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.builder.DefaultEnvironment;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.java.source.usages.SymbolClassReader;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
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
    
    /**Constants for JavaSource.flags*/
    private static final int INVALID = 1;
    private static final int CHANGE_EXPECTED = INVALID<<1;
    private static final int RESCHEDULE_FINISHED_TASKS = CHANGE_EXPECTED<<1;
    private static final int UPDATE_INDEX = RESCHEDULE_FINISHED_TASKS<<1;
    private static final int IS_CLASS_FILE = UPDATE_INDEX<<1;
    
    private static final Pattern excludedTasks;
    private static final Pattern includedTasks;
    /**Limit for task to be marked as a slow one, in ms*/
    private static final int SLOW_CANCEL_LIMIT = 50;
    private static final PrintWriter DEV_NULL = new PrintWriter(new DevNullWriter(), false);
    

    private static final int REPARSE_DELAY = 500;
    private int reparseDelay;
    
    /**Used by unit tests*/
    static JavaFileObjectProvider jfoProvider = new DefaultJavaFileObjectProvider (); 
    
    /**
     * Helper map mapping the {@link Phase} to message for performance logger
     */
    private static Map<Phase, String> phase2Message = new HashMap<Phase,String> ();
    
    private static final Object INTERNAL_LOCK = new Object ();
    
    /**
     * Init the maps
     */
    static {
        JavaSourceAccessor.INSTANCE = new JavaSourceAccessorImpl ();
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N
        
        //Initialize the excludedTasks
        Pattern _excludedTasks = null;
        try {
            String excludedValue= System.getProperty("org.netbeans.api.java.source.JavaSource.excludedTasks");      //NOI18N
            if (excludedValue != null) {
                _excludedTasks = Pattern.compile(excludedValue);
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        excludedTasks = _excludedTasks;
        Pattern _includedTasks = null;
        try {
            String includedValue= System.getProperty("org.netbeans.api.java.source.JavaSource.includedTasks");      //NOI18N
            if (includedValue != null) {
                _includedTasks = Pattern.compile(includedValue);
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        includedTasks = _includedTasks;
        
    }    
                            
    private final static PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<Request> (10, new RequestComparator());
    private final static Map<JavaSource,Collection<Request>> finishedRequests = new WeakHashMap<JavaSource,Collection<Request>>();
    private final static Map<JavaSource,Collection<Request>> waitingRequests = new WeakHashMap<JavaSource,Collection<Request>>();
    private final static Collection<CancellableTask> toRemove = new LinkedList<CancellableTask> ();
    private final static SingleThreadFactory factory = new SingleThreadFactory ();
    private final static CurrentRequestReference currentRequest = new CurrentRequestReference ();
    private final static EditorRegistryListener editorRegistryListener = new EditorRegistryListener ();
    private final static List<DeferredTask> todo = Collections.synchronizedList(new LinkedList<DeferredTask>());
    //Only single thread can operate on the single javac
    private final static ReentrantLock javacLock = new ReentrantLock (true);
    
    private final Collection<FileObject> files;
    final FileObject rootFo;
    private final FileChangeListener fileChangeListener;
    private DocListener listener;
    private DataObjectListener dataObjectListener;
    
    private final ClasspathInfo classpathInfo;    
    private CompilationInfo currentInfo;
    private java.util.Stack<CompilationInfo> infoStack = new java.util.Stack<CompilationInfo> ();
            
    private int flags = 0;        
    
    //Preprocessor support
    private FilterListener filterListener;
    
    private PositionConverter binding;
    
    private static final Logger LOGGER = Logger.getLogger(JavaSource.class.getName());
        
    static {
        Executors.newSingleThreadExecutor(factory).submit (new CompilationJob());
    }
    
    
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
            DataObject dObj = (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
            if (dObj != null)
                js = forFileObject(dObj.getPrimaryFile());
        }
        return js;
    }
    
    /**
     * Creates a new instance of JavaSource
     * @param files to create JavaSource for
     * @param cpInfo classpath info
     */
    private JavaSource (ClasspathInfo cpInfo, PositionConverter binding, Collection<? extends FileObject> files) throws IOException {
        this.reparseDelay = REPARSE_DELAY;
        this.files = Collections.unmodifiableList(new ArrayList<FileObject>(files));   //Create a defensive copy, prevent modification
        this.fileChangeListener = new FileChangeListenerImpl ();
        this.binding = binding;
        boolean multipleSources = this.files.size() > 1, filterAssigned = false;
        for (Iterator<? extends FileObject> it = this.files.iterator(); it.hasNext();) {
            FileObject file = it.next();
            try {
                Logger.getLogger("TIMER").log(Level.FINE, "JavaSource",
                    new Object[] {file, this});
                if (!multipleSources) {
                    file.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,file));
                    this.assignDocumentListener(file);
                    this.dataObjectListener = new DataObjectListener(file);                                        
                }
                if (!filterAssigned) {
                    filterAssigned = true;
                    if (this.binding == null) {
                        this.binding = new PositionConverter(file, JavaFileFilterQuery.getFilter(file));
                    }
                    JavaFileFilterImplementation filter = this.binding.getFilter();
                    if (filter != null) {
                        this.filterListener = new FilterListener (filter);
                    }
                }
            } catch (DataObjectNotFoundException donf) {
                if (multipleSources) {
                    LOGGER.warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(file));     //NOI18N
                    it.remove();
                }
                else {
                    throw donf;
                }
            }
        }
        this.classpathInfo = cpInfo;
        if (this.files.size() == 1) {
            this.rootFo = classpathInfo.getClassPath(PathKind.SOURCE).findOwnerRoot(this.files.iterator().next());
        }
        else {
            this.rootFo = null;
        }
        this.classpathInfo.addChangeListener(WeakListeners.change(this.listener, this.classpathInfo));
    }
    
    private JavaSource (final ClasspathInfo info, final FileObject classFileObject, final FileObject root) throws IOException {
        assert info != null;
        assert classFileObject != null;
        assert root != null;
        this.reparseDelay = REPARSE_DELAY;
        this.files = Collections.<FileObject>singletonList(classFileObject);
        this.fileChangeListener = new FileChangeListenerImpl ();
        classFileObject.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,classFileObject));
        this.dataObjectListener = new DataObjectListener(classFileObject);
        this.classpathInfo =  info;
        this.rootFo = root;
        this.classpathInfo.addChangeListener(WeakListeners.change(this.listener, this.classpathInfo));
        this.flags|= IS_CLASS_FILE;
        this.binding = new PositionConverter(classFileObject, null);
    }
       
    private static final Set<StackTraceElement> warnedAboutRunInEQ = new HashSet<StackTraceElement>();
    /** Runs a task which permits for controlling phases of the parsing process.
     * You probably do not want to call this method unless you are reacting to
     * some user's GUI input which requires immediate action (e.g. code completion popup). 
     * In all other cases use {@link JavaSourceTaskFactory}.<BR>
     * Call to this method will cancel processig of all the phase completion tasks until
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
        
        assert !holdsDocumentWriteLock(files) : "JavaSource.runCompileControlTask called under Document write lock.";    //NOI18N
        
        boolean a = false;
        assert a = true;
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
            if (warnedAboutRunInEQ.add(stackTraceElement)) {
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
                    CompilationInfo currentInfo = null;
                    synchronized (this) {                        
                        if (this.currentInfo != null && (this.flags & INVALID)==0) {
                            currentInfo = this.currentInfo;
                        }
                        if (!shared) {
                            this.flags|=INVALID;
                        }                        
                    }
                    if (currentInfo == null) {
                        currentInfo = createCurrentInfo(this, binding, null);
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
                        task.run (new CompilationController (currentInfo));
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
                        CompilationInfo ci = createCurrentInfo(this, new PositionConverter(activeFile, null), jt);
                        task.run(new CompilationController(ci));
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
                boolean isScanner = false;
                if (request[0] == null) {
                    isScanner = currentRequest.getUserTaskToCancel(request);
                }
                try {
                    if (isScanner) {
                        return sync;
                    }
                    if (request[0] != null) {
                        request[0].task.cancel();
                    }
                    if (javacLock.tryLock()) {
                        try {
                            if (todo.remove(r)) {
                                try {
                                    runUserActionTask(task, shared);
                                    return sync;
                                } finally {
                                    sync.taskFinished();
                                }
                            }
                        } finally {
                            javacLock.unlock();
                        }
                    }
                } finally {
                    currentRequest.cancelCompleted(request[0]);
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
     * Call to this method will cancel processig of all the phase completion tasks until
     * this task does not finish.<BR>
     * @see Task for information about implementation requirements
     * @param task The task which.
     */    
    public ModificationResult runModificationTask(Task<WorkingCopy> task) throws IOException {        
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        
        assert !holdsDocumentWriteLock(files) : "JavaSource.runModificationTask called under Document write lock.";    //NOI18N
        
        boolean a = false;
        assert a = true;        
        if (a && javax.swing.SwingUtilities.isEventDispatchThread()) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            LOGGER.warning("JavaSource.runModificationTask called in AWT event thread by: " + stackTrace[2]);     //NOI18N
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
                    CompilationInfo currentInfo = null;
                    synchronized (this) {
                        if (this.currentInfo != null &&  (this.flags & INVALID) == 0) {
                            currentInfo = this.currentInfo;
                        }
                    }
                    if (currentInfo == null) {
                        currentInfo = createCurrentInfo(this, binding, null);
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
                        CompilationInfo ci = createCurrentInfo(this, new PositionConverter(activeFile, null), jt);
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
       
    /** Adds a task to given compilation phase. The tasks will run sequentially by
     * priorty after given phase is reached.
     * @see CancellableTask for information about implementation requirements 
     * @task The task to run.
     * @phase In which phase should the task run
     * @priority Priority of the task.
     */
    void addPhaseCompletionTask( CancellableTask<CompilationInfo> task, Phase phase, Priority priority ) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        if (phase == null || phase == Phase.MODIFIED) { 
            throw new IllegalArgumentException (String.format("The %s is not a legal value of phase",phase));   //NOI18N
        }
        if (priority == null) {
            throw new IllegalArgumentException ("The priority cannot be null");    //NOI18N
        }
        final String taskClassName = task.getClass().getName();
        if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
            if (includedTasks == null || !includedTasks.matcher(taskClassName).matches())
            return;
        }
        CompilationInfo currentInfo;
        synchronized (this) {
            currentInfo = this.currentInfo;
        }
        if (currentInfo == null) {
            currentInfo = createCurrentInfo (this, binding, null);
        }
        synchronized (this) {
            if (this.currentInfo == null) {
                this.currentInfo = currentInfo;
            }
        }
        handleAddRequest (new Request (task, this, phase, priority, true));
    }
    
    /** Removes the task from the phase queue.
     * @task The task to remove.
     */
    void removePhaseCompletionTask( CancellableTask<CompilationInfo> task ) {
        final String taskClassName = task.getClass().getName();
        if (excludedTasks != null && excludedTasks.matcher(taskClassName).matches()) {
            if (includedTasks == null || !includedTasks.matcher(taskClassName).matches()) {
                return;
            }
        }
        synchronized (INTERNAL_LOCK) {
            toRemove.add (task);
        }
    }
    
    /**Rerun the task in case it was already run. Does nothing if the task was not already run.
     *
     * @task to reschedule
     */
    void rescheduleTask(CancellableTask<CompilationInfo> task) {
        synchronized (INTERNAL_LOCK) {
            JavaSource.Request request = this.currentRequest.getTaskToCancel (task);
            if ( request == null) {                
out:            for (Iterator<Collection<Request>> it = finishedRequests.values().iterator(); it.hasNext();) {
                    Collection<Request> cr = it.next ();
                    for (Iterator<Request> it2 = cr.iterator(); it2.hasNext();) {
                        Request fr = it2.next();
                        if (task == fr.task) {
                            it2.remove();
                            JavaSource.requests.add(fr);
                            if (cr.size()==0) {
                                it.remove();
                            }
                            break out;
                        }
                    }
                }
            }
            else {
                currentRequest.cancelCompleted(request);
            }
        }        
    }
        
    /**
     * Marks this {@link JavaSource} as modified, causes that the cached information are
     * cleared and all the PhaseCompletionTasks are restarted.    
     * The only client of this method should be the JavaDataObject or other DataObjects
     * providing the {@link JavaSource}. If you call this method in another case you are
     * probably doing something incorrect.
     */
    void revalidate () {
        this.resetState(true, false);
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
    
    JavacTaskImpl createJavacTask(final DiagnosticListener<? super JavaFileObject> diagnosticListener) {
        String sourceLevel = null;
        if (!this.files.isEmpty()) {
            FileObject file = files.iterator().next();
            
            sourceLevel = SourceLevelQuery.getSourceLevel(file);
            
            FileObject root = getClasspathInfo().getClassPath(PathKind.SOURCE).findOwnerRoot(file);
            
            if (root != null && sourceLevel != null) {
                try {
                    RepositoryUpdater.getDefault().verifySourceLevel(root.getURL(), sourceLevel);
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        }
        if (sourceLevel == null) {
            sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
        }
        JavacTaskImpl javacTask = createJavacTask(getClasspathInfo(), diagnosticListener, sourceLevel, false);
        Context context = javacTask.getContext();
        JSCancelService.preRegister(context);
        TreeLoader.preRegister(context, getClasspathInfo());
        Messager.preRegister(context, null, DEV_NULL, DEV_NULL, DEV_NULL);
        ErrorHandlingJavadocEnter.preRegister(context);
        JavadocMemberEnter.preRegister(context);       
        JavadocEnv.preRegister(context, getClasspathInfo());
        Scanner.Factory.instance(context);
        //Builder2.instance(context).keepComments = true;
        com.sun.tools.javac.main.JavaCompiler.instance(context).keepComments = true;
        return javacTask;
    }
    
    private static JavacTaskImpl createJavacTask(final ClasspathInfo cpInfo, final DiagnosticListener<? super JavaFileObject> diagnosticListener, final String sourceLevel, final boolean backgroundCompilation) {
        ArrayList<String> options = new ArrayList<String>();
        String lintOptions = CompilerSettings.getCommandLine();
        
        if (lintOptions.length() > 0) {
            options.addAll(Arrays.asList(lintOptions.split(" ")));
        }
        if (!backgroundCompilation) {
            options.add("-Xjcov"); //NOI18N, Make the compiler store end positions
            options.add("-XDdisableStringFolding"); //NOI18N
        } else {
            options.add("-XDbackgroundCompilation");    //NOI18N
            options.add("-XDcompilePolicy=byfile");     //NOI18N
        }
        options.add("-XDide");   // NOI18N, javac runs inside the IDE
        options.add("-g:");      // NOI18N, Enable some debug info
        options.add("-g:lines"); // NOI18N, Make the compiler to maintain line table
        options.add("-g:vars");  // NOI18N, Make the compiler to maintain local variables table
        options.add("-source");  // NOI18N
        options.add(validateSourceLevel(sourceLevel));

        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        try {            
            //The ToolProvider.defaultJavaCompiler will use the context classloader to load the javac implementation
            //it should be load by the current module's classloader (should delegate to other module's classloaders as necessary)
            Thread.currentThread().setContextClassLoader(ClasspathInfo.class.getClassLoader());
            JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
            JavacTaskImpl task = (JavacTaskImpl)tool.getTask(null, cpInfo.getFileManager(), diagnosticListener, options, null, Collections.<JavaFileObject>emptySet());
            Context context = task.getContext();
            
            if (backgroundCompilation) {
                SymbolClassReader.preRegister(context, false);
            } else {
                SymbolClassReader.preRegister(context, true);
            }
            
            return task;
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
        }
    }    
     
    private static class ErrorHandlingJavadocEnter extends JavadocEnter {
        
        private Messager messager;
        
        public static void preRegister(final Context context) {
            context.put(enterKey, new Context.Factory<Enter>() {
                public Enter make() {
                    return new ErrorHandlingJavadocEnter(context);
                }
            });
        }
        
        protected ErrorHandlingJavadocEnter(Context context) {
            super(context);
            messager = Messager.instance0(context);
        }
        
        public @Override void main(com.sun.tools.javac.util.List<JCCompilationUnit> trees) {
            //Todo: Check everytime after the java update that JavaDocEnter.main or Enter.main
            //are not changed.
            this.complete(trees, null);
        }
    }

    /**
     * Not synchronized, only the CompilationJob's thread can call it!!!!
     *
     */
    static Phase moveToPhase (final Phase phase, final CompilationInfo currentInfo, final boolean cancellable) throws IOException {
        Phase currentPhase = currentInfo.getPhase();                
        final boolean isMultiFiles = currentInfo.getJavaSource().files.size()>1;
        LowMemoryNotifier lm = null;
        LMListener lmListener = null;
        if (isMultiFiles) {
            lm = LowMemoryNotifier.getDefault();
            assert lm != null;
            lmListener = new LMListener ();
            lm.addLowMemoryListener (lmListener);
        }                                
        try {
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase.compareTo(Phase.PARSED)<0 && phase.compareTo(Phase.PARSED)>=0) {
                if (cancellable && currentRequest.isCanceled()) {
                    //Keep the currentPhase unchanged, it may happen that an userActionTask
                    //runnig after the phace completion task may still use it.
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                // XXX - this might be with wrong encoding
                Iterable<? extends CompilationUnitTree> trees = currentInfo.getJavacTask().parse(new JavaFileObject[] {currentInfo.jfo});
                assert trees != null : "Did not parse anything";        //NOI18N
                Iterator<? extends CompilationUnitTree> it = trees.iterator();
                assert it.hasNext();
                CompilationUnitTree unit = it.next();
                currentInfo.setCompilationUnit(unit);
                JCCompilationUnit tree = (JCCompilationUnit) unit;
                ASTService s = ASTService.instance(currentInfo.getJavacTask().getContext());
                if (tree.endPositions != null) {
                    s.setEndPosTable(tree.sourcefile, tree.endPositions);
                }
                List<CompilationUnitTree> units = new ArrayList<CompilationUnitTree>();
                units.add(tree);
                s.setRoot(new RootTree(units));
                assert !it.hasNext();
                currentPhase = Phase.PARSED;
                long end = System.currentTimeMillis();
                FileObject file = currentInfo.getFileObject();
                Logger.getLogger("TIMER").log(Level.FINE, "Compilation Unit",
                    new Object[] {file, unit});

                logTime (file,currentPhase,(end-start));
            }                
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.PARSED && phase.compareTo(Phase.ELEMENTS_RESOLVED)>=0) {
                if (cancellable && currentRequest.isCanceled()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                currentInfo.getJavacTask().enter();
                currentPhase = Phase.ELEMENTS_RESOLVED;
                long end = System.currentTimeMillis();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
           }
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.ELEMENTS_RESOLVED && phase.compareTo(Phase.RESOLVED)>=0) {
                if (cancellable && currentRequest.isCanceled()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis ();
                currentInfo.getJavacTask().analyze();
                currentPhase = Phase.RESOLVED;
                long end = System.currentTimeMillis ();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
            }
            if (lmListener != null && lmListener.lowMemory.getAndSet(false)) {
                currentInfo.needsRestart = true;
                return currentPhase;
            }
            if (currentPhase == Phase.RESOLVED && phase.compareTo(Phase.UP_TO_DATE)>=0) {
                currentPhase = Phase.UP_TO_DATE;
            }
        } catch (CouplingAbort a) {
            RepositoryUpdater.couplingAbort(a, currentInfo.jfo);
            currentInfo.needsRestart = true;
            return currentPhase;            
        } catch (CancelAbort ca) {
            return Phase.MODIFIED;
        } catch (Abort abort) {
            currentPhase = Phase.UP_TO_DATE;
        } catch (IOException ex) {
            currentPhase = Phase.MODIFIED;
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (ReattributionException ex) {
            currentPhase = Phase.MODIFIED;
            dumpSource(currentInfo, ex);
            throw new RuntimeException(ex);
        } catch (RuntimeException ex) {
            currentPhase = Phase.MODIFIED;
            dumpSource(currentInfo, ex);
            throw ex;        
        } catch (Error ex) {
            currentPhase = Phase.MODIFIED;
            dumpSource(currentInfo, ex);
            throw ex;
        }

        finally {
            if (isMultiFiles) {
                assert lm != null;
                assert lmListener != null;
                lm.removeLowMemoryListener (lmListener);
            }
            currentInfo.setPhase(currentPhase);
        }
        return currentPhase;
    }
    
    static void logTime (FileObject source, Phase phase, long time) {
        assert source != null && phase != null;
        String message = phase2Message.get(phase);
        assert message != null;
        Logger.getLogger("TIMER").log(Level.FINE, message, new Object[] {source, time});
    }
    
    boolean isClassFile () {
        return (this.flags & IS_CLASS_FILE) != 0;
    }
    
    private static final RequestProcessor RP = new RequestProcessor ("JavaSource-event-collector",1);       //NOI18N
    
    private final RequestProcessor.Task resetTask = RP.create(new Runnable() {
        public void run() {
            resetStateImpl();
        }
    });
        
    private void resetState(boolean invalidate, boolean updateIndex) {
        boolean invalid;
        synchronized (this) {
            invalid = (this.flags & INVALID) != 0;
            this.flags|=CHANGE_EXPECTED;
            if (invalidate) {
                this.flags|=(INVALID|RESCHEDULE_FINISHED_TASKS);
            }
            if (updateIndex) {
                this.flags|=UPDATE_INDEX;
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
    
    private void assignDocumentListener(FileObject fo) throws IOException {
        DataObject od = DataObject.find(fo);
        EditorCookie.Observable ec = od.getCookie(EditorCookie.Observable.class);            
        if (ec != null) {
            this.listener = new DocListener (ec);
        } else {
            LOGGER.log(Level.WARNING,String.format("File: %s has no EditorCookie.Observable", FileUtil.getFileDisplayName (fo)));      //NOI18N
        }
    }
    
    private static class Request {
        private final CancellableTask<? extends CompilationInfo> task;
        private final JavaSource javaSource;        //XXX: Maybe week, depends on the semantics
        private final Phase phase;
        private final Priority priority;
        private final boolean reschedule;
        
        public Request (final CancellableTask<? extends CompilationInfo> task, final JavaSource javaSource,
            final Phase phase, final Priority priority, final boolean reschedule) {
            assert task != null;
            this.task = task;
            this.javaSource = javaSource;
            this.phase = phase;
            this.priority = priority;
            this.reschedule = reschedule;
        }
        
        public @Override String toString () {            
            if (reschedule) {
                return String.format("Periodic request for phase: %s with priority: %s to perform: %s", phase.name(), priority, task.toString());   //NOI18N
            }
            else {
                return String.format("One time request for phase: %s with priority: %s to perform: %s", phase != null ? phase.name() : "<null>", priority, task.toString());   //NOI18N
            }
        }
        
        public @Override int hashCode () {
            return this.priority.ordinal();
        }
        
        public @Override boolean equals (Object other) {
            if (other instanceof Request) {
                Request otherRequest = (Request) other;
                return priority == otherRequest.priority
                    && reschedule == otherRequest.reschedule
                    && (phase == null ? otherRequest.phase == null : phase.equals (otherRequest.phase))
                    && task.equals(otherRequest.task);                       
            }
            else {
                return false;
            }
        }        
    }
    
    private static class RequestComparator implements Comparator<Request> {
        public int compare (Request r1, Request r2) {
            assert r1 != null && r2 != null;
            return r1.priority.compareTo (r2.priority);
        }
    }
    
    private static class CompilationJob implements Runnable {        
        
        @SuppressWarnings ("unchecked") //NOI18N
        public void run () {
            try {
                while (true) {                   
                    try {
                        synchronized (INTERNAL_LOCK) {
                            //Clean up toRemove tasks
                            if (!toRemove.isEmpty()) {
                                for (Iterator<Collection<Request>> it = finishedRequests.values().iterator(); it.hasNext();) {
                                    Collection<Request> cr = it.next ();
                                    for (Iterator<Request> it2 = cr.iterator(); it2.hasNext();) {
                                        Request fr = it2.next();
                                        if (toRemove.remove(fr.task)) {
                                            it2.remove();
                                        }
                                    }
                                    if (cr.size()==0) {
                                        it.remove();
                                    }
                                }
                            }
                        }
                        Request r = JavaSource.requests.poll(2,TimeUnit.SECONDS);
                        if (r != null) {
                            currentRequest.setCurrentTask(r);
                            try {                            
                                JavaSource js = r.javaSource;
                                if (js == null) {
                                    assert r.phase == null;
                                    assert r.reschedule == false;
                                    javacLock.lock ();
                                    try {
                                        try {
                                            r.task.run (null);
                                        } finally {
                                            currentRequest.clearCurrentTask();
                                            boolean cancelled = requests.contains(r);
                                            if (!cancelled) {
                                                DeferredTask[] _todo;
                                                synchronized (todo) {
                                                    _todo = todo.toArray(new DeferredTask[todo.size()]);
                                                    todo.clear();
                                                }
                                                for (DeferredTask rq : _todo) {
                                                    try {
                                                        rq.js.runUserActionTask(rq.task, rq.shared);                                                        
                                                    } finally {
                                                        rq.sync.taskFinished();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (RuntimeException re) {
                                        Exceptions.printStackTrace(re);
                                    }
                                    finally {
                                        javacLock.unlock();
                                    }
                                }
                                else {
                                    assert js.files.size() <= 1;
                                    boolean jsInvalid;
                                    CompilationInfo ci;
                                    synchronized (INTERNAL_LOCK) {
                                        //jl:what does this comment mean?
                                        //Not only the finishedRequests for the current request.javaSource should be cleaned,
                                        //it will cause a starvation
                                        if (toRemove.remove(r.task)) {
                                            continue;
                                        }
                                        synchronized (js) {                     
                                            boolean changeExpected = (js.flags & CHANGE_EXPECTED) != 0;
                                            if (changeExpected) {
                                                //Skeep the task, another invalidation is comming
                                                Collection<Request> rc = JavaSource.waitingRequests.get (r.javaSource);
                                                if (rc == null) {
                                                    rc = new LinkedList<Request> ();
                                                    JavaSource.waitingRequests.put (r.javaSource, rc);
                                                }
                                                rc.add(r);
                                                continue;
                                            }
                                            jsInvalid = (js.flags & INVALID)!=0;
                                            ci = js.currentInfo;
                                        }
                                    }
                                    try {
                                        //createCurrentInfo has to be out of synchronized block, it aquires an editor lock                                    
                                        if (jsInvalid) {
                                            ci = createCurrentInfo (js, js.binding, null);
                                            synchronized (js) {
                                                if ((js.flags & INVALID) != 0) {
                                                    js.currentInfo = ci;
                                                    js.flags &= ~INVALID;
                                                }
                                                else {
                                                    ci = js.currentInfo;
                                                }
                                            }
                                        }                                    
                                        assert ci != null;
                                        javacLock.lock();
                                        try {
                                            boolean shouldCall;
                                            final JSCancelService cancelService = JSCancelService.instance(ci.getJavacTask().getContext());
                                            if (cancelService != null) {
                                                cancelService.active = true;
                                            }

                                            try {
                                                final Phase phase = JavaSource.moveToPhase (r.phase, ci, true);
                                                shouldCall = phase.compareTo(r.phase)>=0;
                                            } finally {
                                                if (cancelService != null) {
                                                    cancelService.active = false;
                                                }
                                            }                                            
                                            if (shouldCall) {
                                                synchronized (js) {
                                                    shouldCall &= (js.flags & INVALID)==0;
                                                }
                                                if (shouldCall) {
                                                    //The state (or greater) was reached and document was not modified during moveToPhase
                                                    try {
                                                        final long startTime = System.currentTimeMillis();
                                                        Index.cancel.set(currentRequest.getCanceledRef());
                                                        try {
                                                            ((CancellableTask<CompilationInfo>)r.task).run (ci); //XXX: How to do it in save way?
                                                        } finally {
                                                            Index.cancel.remove();
                                                        }
                                                        final long endTime = System.currentTimeMillis();
                                                        if (LOGGER.isLoggable(Level.FINEST)) {
                                                            LOGGER.finest(String.format("executed task: %s in %d ms.",  //NOI18N
                                                                r.task.getClass().toString(), (endTime-startTime)));
                                                        }
                                                        if (LOGGER.isLoggable(Level.FINER)) {
                                                            final long cancelTime = currentRequest.getCancelTime();
                                                            if (cancelTime >= startTime && (endTime - cancelTime) > SLOW_CANCEL_LIMIT) {
                                                                LOGGER.finer(String.format("Task: %s ignored cancel for %d ms.",  //NOI18N
                                                                    r.task.getClass().toString(), (endTime-cancelTime)));
                                                            }
                                                        }
                                                    } catch (CancelAbort ca) {
                                                        //Handled below by: canceled = currentRequest.setCurrentTask(null);
                                                    } catch (Exception re) {
                                                        Exceptions.printStackTrace (re);
                                                    }
                                                }
                                            }
                                        } finally {
                                            javacLock.unlock();
                                        }

                                        if (r.reschedule) {                                            
                                            synchronized (INTERNAL_LOCK) {
                                                boolean canceled = currentRequest.setCurrentTask(null);
                                                synchronized (js) {
                                                    if ((js.flags & INVALID)!=0 || canceled) {
                                                        //The JavaSource was changed or canceled rechedule it now
                                                        JavaSource.requests.add(r);
                                                    }
                                                    else {
                                                        //Up to date JavaSource add it to the finishedRequests
                                                        Collection<Request> rc = JavaSource.finishedRequests.get (r.javaSource);
                                                        if (rc == null) {
                                                            rc = new LinkedList<Request> ();
                                                            JavaSource.finishedRequests.put (r.javaSource, rc);
                                                        }
                                                        rc.add(r);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (final FileObjects.InvalidFileException invalidFile) {
                                        //Ideally the requests should be removed by JavaSourceTaskFactory and task should be put to finishedRequests,
                                        //but the reality is different, the task cannot be put to finished request because of possible memory leak
                                    }
                                }
                            } finally {
                                currentRequest.setCurrentTask(null);                   
                            }
                        } 
                    } catch (Throwable e) {
                        if (e instanceof InterruptedException) {
                            throw (InterruptedException)e;
                        }
                        else if (e instanceof ThreadDeath) {
                            throw (ThreadDeath)e;
                        }
                        else {
                            Exceptions.printStackTrace(e);
                        }
                    }                    
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                // stop the service.
            }
        }                        
    }
    
    private class DocListener implements DocumentListener, PropertyChangeListener, ChangeListener {
        
        private EditorCookie.Observable ec;
        private DocumentListener docListener;
        
        public DocListener (EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            Document doc = ec.getDocument();            
            if (doc != null) {
                doc.addDocumentListener(docListener = WeakListeners.create(DocumentListener.class, this, doc));
            }
        }
        
        public void insertUpdate(DocumentEvent e) {
            //Has to reset cache asynchronously
            //the callback cannot be in synchronized section
            //since NbDocument.runAtomic fires under lock
            JavaSource.this.resetState(true, true);
        }

        public void removeUpdate(DocumentEvent e) {
            //Has to reset cache asynchronously
            //the callback cannot be in synchronized section
            //since NbDocument.runAtomic fires under lock
            JavaSource.this.resetState(true, true);
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();                
                if (old instanceof Document && docListener != null) {
                    ((Document) old).removeDocumentListener(docListener);
                    docListener = null;
                }                
                Document doc = ec.getDocument();                
                if (doc != null) {
                    doc.addDocumentListener(docListener = WeakListeners.create(DocumentListener.class, this, doc));
                    resetState(true, false);
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            JavaSource.this.resetState(true, false);
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
            if (DataObject.PROP_VALID.equals(pce.getPropertyName())) {
                handleInvalidDataObject(invalidDO);
            } else if (pce.getPropertyName() == null && !dobj.isValid()) {
                handleInvalidDataObject(invalidDO);
            }
        }
        
        private void handleInvalidDataObject(final DataObject invalidDO) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    handleInvalidDataObjectImpl(invalidDO);
                }
            });
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
                    assignDocumentListener(fobj);
                    resetState(true, true);
                } catch (IOException ex) {
                    // should not occur
                    LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                }
            }
        }
        
    }
    
    private final class FilterListener implements ChangeListener {        
        
        public FilterListener (final JavaFileFilterImplementation filter) {
            filter.addChangeListener(WeakListeners.change(this, filter));
        }
        
        public void stateChanged(ChangeEvent event) {
            JavaSource.this.resetState(true, false);
        }
    }
        
    private static CompilationInfo createCurrentInfo (final JavaSource js, final PositionConverter binding, final JavacTaskImpl javac) throws IOException {        
        CompilationInfo info = new CompilationInfo (js, binding, javac);
        if (binding != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "CompilationInfo",
                    new Object[] {binding.getFileObject(), info});
        }
        return info;
    }

    private static void handleAddRequest (final Request nr) {
        assert nr != null;
        requests.add (nr);
        JavaSource.Request request = currentRequest.getTaskToCancel(nr.priority);
        try {
            if (request != null) {
                request.task.cancel();
            }
        } finally {
            currentRequest.cancelCompleted(request);
        }
    }    
    
    private static class SingleThreadFactory implements ThreadFactory {
        
        private Thread t;
        
        public Thread newThread(Runnable r) {
            assert this.t == null;
            this.t = new Thread (r,"Java Source Worker Thread");     //NOI18N
            return this.t;
        }
        
        public boolean isDispatchThread (Thread t) {
            assert t != null;
            return this.t == t;
        }
    }
    
    private static class JavaSourceAccessorImpl extends JavaSourceAccessor {
        
        protected @Override void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, Priority priority) {
            handleAddRequest(new Request (task, null, null, priority, false));
        }                

        @Override
        public JavacTaskImpl createJavacTask(ClasspathInfo cpInfo, DiagnosticListener<? super JavaFileObject> diagnosticListener, String sourceLevel) {
            if (sourceLevel == null)
                sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
            return JavaSource.createJavacTask(cpInfo, diagnosticListener, sourceLevel, true);
        }
        
        @Override
        public JavacTaskImpl getJavacTask (final CompilationInfo compilationInfo) {
            assert compilationInfo != null;
            return compilationInfo.getJavacTask();
        }
        
        @Override
        public DefaultEnvironment getCommandEnvironment(WorkingCopy copy) {
            assert copy != null;
            return copy.getCommandEnvironment();
        }
        
        @Override
        public CompilationInfo getCurrentCompilationInfo (final JavaSource js, final Phase phase) throws IOException {
            assert js != null;
            assert isDispatchThread();
            CompilationInfo info = null;
            synchronized (js) {                     
                if ((js.flags & INVALID)==0) {
                    info = js.currentInfo;
                }
            }
            if (info == null) {
                return null;
            }
            Phase currentPhase = moveToPhase(phase, info, true);                
            return currentPhase.compareTo(phase)<0 ? null : info;
        }

        @Override
        public void revalidate(JavaSource js) {
            js.revalidate();
        }
        
        @Override
        public boolean isDispatchThread () {
            return factory.isDispatchThread(Thread.currentThread());
        }

        public JavaSource create(ClasspathInfo cpInfo, PositionConverter binding, Collection<? extends FileObject> files) throws IllegalArgumentException {
            return JavaSource.create(cpInfo, binding, files);
        }

        public PositionConverter create(FileObject fo, int offset, int length, JTextComponent component) {
            return new PositionConverter(fo, offset, length, component);
        }
    }
    
    
    /**
     *  Only encapsulates current request. May be trasformed into 
     *  JavaSource private static methods, but it may be less readable.
     */
    private static final class CurrentRequestReference {                        
        
        private static JavaSource.Request DUMMY_RQ = new JavaSource.Request (new CancellableTask<CompilationInfo>() { public void cancel (){}; public void run (CompilationInfo info){}},null,null,null,false);
        
        private JavaSource.Request reference;
        private JavaSource.Request canceledReference;
        private long cancelTime;
        private final AtomicBoolean canceled;
        private boolean mayCancelJavac;
        
        CurrentRequestReference () {
            this.canceled = new AtomicBoolean();
        }
        
        boolean setCurrentTask (JavaSource.Request reference) throws InterruptedException {
            boolean result = false;
            synchronized (INTERNAL_LOCK) {
                while (this.canceledReference!=null) {
                    INTERNAL_LOCK.wait();
                }
                result = this.canceled.getAndSet(false);
                this.mayCancelJavac = false;
                this.cancelTime = 0;
                this.reference = reference;                
            }
            return result;
        }
        
        /**
         * Prevents rececondition in runWhenScanFinished. This method may be called only from
         * the Java-Source-Worker-Thread right after the initial scan finished. The problem was
         * that the task was added into the todo after the todo was drained into the list of pending
         * tasks but the getTaskToCancel thought that the task is still the RepositoryUpdater. So the
         * Java-Source-Worker-Thread has to clean the task after calling RU.run but before draining the
         * pending tasks into the array, it cannot use setCurrentTaks (null) since it is under javac lock
         * and the setCurrentTaks methods may block the caller thread => deadlock.
         */ 
        void clearCurrentTask () {
            synchronized (INTERNAL_LOCK) {
                this.reference = null;
            }
        }
        
        JavaSource.Request getTaskToCancel (final Priority priority) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (INTERNAL_LOCK) {
                    if (this.reference != null && priority.compareTo(this.reference.priority) < 0) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled.set(true);                    
                        this.cancelTime = System.currentTimeMillis();
                    }
                }
            }
            return request;
        }
        
        JavaSource.Request getTaskToCancel (final boolean mayCancelJavac) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (INTERNAL_LOCK) {
                    if (this.reference != null) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled.set(true);
                        this.mayCancelJavac = mayCancelJavac;
                        this.cancelTime = System.currentTimeMillis();
                    }
                    else if (canceledReference == null)  {
                        request = DUMMY_RQ;
                        this.canceledReference = request;
                        this.mayCancelJavac = mayCancelJavac;
                        this.cancelTime = System.currentTimeMillis();
                    }
                }
            }
            return request;
        }
        
        JavaSource.Request getTaskToCancel (final CancellableTask task) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (INTERNAL_LOCK) {
                    if (this.reference != null && task == this.reference.task) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled.set(true);
                    }
                }
            }
            return request;
        }
        
        JavaSource.Request getTaskToCancel () {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {                
                synchronized (INTERNAL_LOCK) {
                     request = this.reference;
                    if (request != null) {
                        assert this.canceledReference == null;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled.set(true);
                        this.cancelTime = System.currentTimeMillis();
                    }
                }
            }
            return request;
        }
        
        /**
         * Called by {@link JavaSource#runWhenScanFinished} to find out which
         * task is currently running. Returns true when the running task in backgroud
         * scan otherwise returns false. The caller is expected not to call cancel on
         * the background scanner, so this method do not reset reference and do not set
         * cancelled flag when running task is background scan. But it sets the canceledReference
         * to prevent java source thread to dispatch next queued task.
         * @param request is filled by currently running task or null when there is no running task.
         * @return true when running task is background scan
         */
        boolean getUserTaskToCancel (JavaSource.Request[] request) {
            assert request != null;
            assert request.length == 1;
            boolean result = false;
            if (!factory.isDispatchThread(Thread.currentThread())) {                
                synchronized (INTERNAL_LOCK) {
                     request[0] = this.reference;
                    if (request[0] != null) {
                        result = request[0].phase == null;
                        assert this.canceledReference == null || result;
                        this.canceledReference = request[0];
                        if (!result) {
                            this.reference = null;                        
                        }
                        this.canceled.set(result);
                        this.cancelTime = System.currentTimeMillis();
                    }
                }
            }
            return result;
        }
        
        boolean isCanceled () {
            synchronized (INTERNAL_LOCK) {
                return this.canceled.get();
            }
        }
        
        AtomicBoolean getCanceledRef () {
            return this.canceled;
        }
        
        boolean isInterruptJavac () {
            synchronized (INTERNAL_LOCK) {
                boolean ret = this.mayCancelJavac && 
                        this.canceledReference != null &&
                        this.canceledReference.javaSource != null &&
                        (this.canceledReference.javaSource.flags & INVALID) != 0;
                return ret;
            }
        }
        
        long getCancelTime () {
            synchronized (INTERNAL_LOCK) {
                return this.cancelTime;
            }
        }
        
        void cancelCompleted (final JavaSource.Request request) {
            if (request != null) {
                synchronized (INTERNAL_LOCK) {
                    assert request == this.canceledReference;
                    this.canceledReference = null;
                    INTERNAL_LOCK.notify();
                }
            }
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
    
    /**
     * Only for unit tests
     */
    static interface JavaFileObjectProvider {
        public JavaFileObject createJavaFileObject (FileObject fo, JavaFileFilterImplementation filter) throws IOException;
    }
    
    static final class DefaultJavaFileObjectProvider implements JavaFileObjectProvider {
        public JavaFileObject createJavaFileObject (FileObject fo, JavaFileFilterImplementation filter) throws IOException {
            return FileObjects.nbFileObject(fo, filter, true);
        }
    }
    
    private static class LMListener implements LowMemoryListener {                
        private AtomicBoolean lowMemory = new AtomicBoolean (false);
        
        public void lowMemory(LowMemoryEvent event) {
            lowMemory.set(true);
        }        
    }
    
    private static class JSCancelService extends CancelService {
                        
        boolean active;
        
        public static JSCancelService instance (final Context context) {
            final CancelService cancelService = CancelService.instance(context);
            return (cancelService instanceof JSCancelService) ? (JSCancelService) cancelService : null;
        }
        
        static void preRegister(final Context context) {
            context.put(cancelServiceKey, new JSCancelService());
        }
        
        @Override
        public boolean isCanceled () {
            final boolean res =  active && currentRequest.isInterruptJavac();
            return res;
        }
               
    }
    
    /**
     *Ugly and slow, called only when -ea
     *
     */
    private static boolean holdsDocumentWriteLock (Iterable<FileObject> files) {
        final Class<AbstractDocument> docClass = AbstractDocument.class;
        try {
            final Method method = docClass.getDeclaredMethod("getCurrentWriter"); //NOI18N
            method.setAccessible(true);
            final Thread currentThread = Thread.currentThread();
            for (FileObject fo : files) {
                try {
                final DataObject dobj = DataObject.find(fo);
                final EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    final StyledDocument doc = ec.getDocument();
                    if (doc instanceof AbstractDocument) {
                        Object result = method.invoke(doc);
                        if (result == currentThread) {
                            return true;
                        }
                    }
                }
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }            
            }
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
        }
        return false;
    }    
    
    private static String validateSourceLevel (String sourceLevel) {        
        Source[] sources = Source.values();
        if (sourceLevel == null) {
            //Should never happen but for sure
            return sources[sources.length-1].name;
        }
        for (Source source : sources) {
            if (source.name.equals(sourceLevel)) {
                return sourceLevel;
            }
        }
        SpecificationVersion specVer = new SpecificationVersion (sourceLevel);
        SpecificationVersion JAVA_12 = new SpecificationVersion ("1.2");   //NOI18N
        if (JAVA_12.compareTo(specVer)>0) {
            //Some SourceLevelQueries return 1.1 source level which is invalid, use 1.2
            return sources[0].name;
        }
        else {
            return sources[sources.length-1].name;
        }
    }
    
    private static final int MAX_DUMPS = 255;
    
    /**
     * Dumps the source code to the file. Used for parser debugging. Only a limited number
     * of dump files is used. If the last file exists, this method doesn't dump anything.
     *
     * @param  info  CompilationInfo for which the error occurred.
     * @param  exc  exception to write to the end of dump file
     */
    private static void dumpSource(CompilationInfo info, Throwable exc) {
        String userDir = System.getProperty("netbeans.user");
        if (userDir == null) {
            return;
        }
        String dumpDir =  userDir + "/var/log/"; //NOI18N
        String src = info.getText();
        FileObject file = info.getFileObject();
        String fileName = FileUtil.getFileDisplayName(file);
        String origName = file.getName();
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
                    writer.println(src);
                    writer.println("----- Classpath: ---------------------------------------------"); // NOI18N
                    
                    final ClassPath bootPath   = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = info.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);

                    writer.println("bootPath: " + (bootPath != null ? bootPath.toString() : "null"));
                    writer.println("classPath: " + (classPath != null ? classPath.toString() : "null"));
                    writer.println("sourcePath: " + (sourcePath != null ? sourcePath.toString() : "null"));
                    
                    writer.println("----- Original exception ---------------------------------------------"); // NOI18N
                    exc.printStackTrace(writer);
                } finally {
                    writer.close();
                    dumpSucceeded = true;
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "Error when writing parser dump file!", ioe); // NOI18N
            }
        }
        if (dumpSucceeded) {
            Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against java/source and attach dump file '"  // NOI18N
                    + f.getAbsolutePath() + "'."); // NOI18N
            Exceptions.printStackTrace(t);
        } else {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }
    
    private static final class DevNullWriter extends Writer {
        public void write(char[] cbuf, int off, int len) throws IOException {
        }
        public void flush() throws IOException {
        }
        public void close() throws IOException {
        }
    }
    
    //Package private test utility methods
    int getReparseDelay () {
        return this.reparseDelay;
    }
    
    void setReparseDelay (int reparseDelay, boolean reset) {
        this.reparseDelay = reparseDelay;
        if (reset) {
            resetState(true, false);
        }
    }
    
}
