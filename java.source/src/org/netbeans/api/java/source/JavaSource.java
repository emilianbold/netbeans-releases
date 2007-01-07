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

package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Abort;
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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.timers.TimesCollector;
import org.netbeans.editor.Registry;
import org.netbeans.jackpot.engine.CommandEnvironment;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.Scanner;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavadocEnv;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.java.source.util.LowMemoryEvent;
import org.netbeans.modules.java.source.util.LowMemoryListener;
import org.netbeans.modules.java.source.util.LowMemoryNotifier;
import org.netbeans.modules.java.source.usages.SymbolClassReader;
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
    
    /**Slow task reporting*/
    private static final boolean reportSlowTasks = Boolean.getBoolean("org.netbeans.api.java.source.JavaSource.reportSlowTasks");   //NOI18N
    /**Limit for task to be marked as a slow one, in ms*/
    private static final int SLOW_TASK_LIMIT = 250;
    private static final int SLOW_CANCEL_LIMIT = 50;
    
    /**Not final for tests.*/
    static int REPARSE_DELAY = 500;
    
    /**Used by unit tests*/
    static JavaFileObjectProvider jfoProvider = new DefaultJavaFileObjectProvider (); 
    
    /**
     * Helper maps mapping the {@link Phase} to key and message for
     * the {@link TimesCollector}
     */
    private static Map<Phase, String> phase2Key = new HashMap<Phase,String> ();
    private static Map<Phase, String> phase2Message = new HashMap<Phase,String> ();
    
    /**
     * Init the maps
     */
    static {
        JavaSourceAccessor.INSTANCE = new JavaSourceAccessorImpl ();
        phase2Key.put(Phase.PARSED,"parsed");                                   //NOI18N
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N
        
        phase2Key.put(Phase.ELEMENTS_RESOLVED,"sig-attributed");                //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N
        
        phase2Key.put(Phase.RESOLVED, "attributed");                            //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N
        
    }    
                            
    private final static PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<Request> (10, new RequestComparator());
    private final static Map<JavaSource,Collection<Request>> finishedRequests = new WeakHashMap<JavaSource,Collection<Request>>();
    private final static Map<JavaSource,Collection<Request>> waitingRequests = new WeakHashMap<JavaSource,Collection<Request>>();
    private final static Collection<CancellableTask> toRemove = new LinkedList<CancellableTask> ();
    private final static SingleThreadFactory factory = new SingleThreadFactory ();
    private final static CurrentRequestReference currentRequest = new CurrentRequestReference ();
    private final static EditorRegistryListener editorRegistryListener = new EditorRegistryListener ();
    //Only single thread can operate on the single javac
    private final static ReentrantLock javacLock = new ReentrantLock (true);
    
    private final Collection<FileObject> files;
    private final FileObject rootFo;
    private final FileChangeListener fileChangeListener;
    private DocListener listener;
    private DataObjectListener dataObjectListener;
    
    private final ClasspathInfo classpathInfo;    
    private CompilationInfo currentInfo;
    private java.util.Stack<CompilationInfo> infoStack = new java.util.Stack<CompilationInfo> ();
            
    private int flags = 0;        
    
    //Preprocessor support
    private FilterListener filterListener;
    
        
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
        try {
            return new JavaSource(cpInfo, files);
        } catch (DataObjectNotFoundException donf) {
            Logger.getLogger("global").warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(donf.getFileObject()));     //NOI18N
        } catch (IOException ex) {            
            Exceptions.printStackTrace(ex);
        }        
        return null;
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
        return create(cpInfo, Arrays.asList(files));
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
        if (!"text/x-java".equals(FileUtil.getMIMEType(fileObject)) && !"java".equals(fileObject.getExt())) {  //NOI18N
            //TODO: JavaSource cannot be created for all kinds of files, but text/x-java is too restrictive:
            return null;
        }        

        Reference<JavaSource> ref = file2JavaSource.get(fileObject);
        JavaSource js = ref != null ? ref.get() : null;

        if (js == null) {
            file2JavaSource.put(fileObject, new WeakReference<JavaSource>(js = create(ClasspathInfo.create(fileObject), fileObject)));
        }

        return js;
    }
    
    /**
     * Returns a {@link JavaSource} instance associated to {@link org.openide.filesystems.FileObject}
     * the {@link Document} was created from, it returns null if the {@link Document} is not
     * associanted with data type providing the {@link JavaSource}.
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
    private JavaSource (ClasspathInfo cpInfo, Collection<? extends FileObject> files) throws IOException {
        this.files = Collections.unmodifiableList(new ArrayList<FileObject>(files));   //Create a defensive copy, prevent modification
        this.fileChangeListener = new FileChangeListenerImpl ();
        boolean multipleSources = this.files.size() > 1;
        for (Iterator<? extends FileObject> it = this.files.iterator(); it.hasNext();) {
            FileObject file = it.next();
            try {
                TimesCollector.getDefault().reportReference( file, JavaSource.class.toString(), "[M] JavaSource", this );       //NOI18N
                if (!multipleSources) {
                    file.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,file));
                    this.assignDocumentListener(file);
                    this.dataObjectListener = new DataObjectListener(file);
                    JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(file);
                    if (filter != null) {
                        this.filterListener = new FilterListener (filter);
                    }                    
                }
            } catch (DataObjectNotFoundException donf) {
                if (multipleSources) {
                    Logger.getLogger("global").warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(file));     //NOI18N
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
     */    
    public void runUserActionTask( final CancellableTask<CompilationController> task, final boolean shared) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        
        assert !holdsDocumentWriteLock(files) : "JavaSource.runCompileControlTask called under Document write lock.";    //NOI18N
        
        if (this.files.size()<=1) {            
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
                currentInfo = createCurrentInfo(this,this.files.isEmpty() ? null : this.files.iterator().next(), filterListener, null);                
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
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }            
                this.javacLock.lock();
                try {
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
                } catch (Exception e) {
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
                        CompilationInfo ci = createCurrentInfo(this,activeFile,filterListener,jt);
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
                } catch (Exception e) {
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
       
    /** Runs a task which permits for modifying the sources.
     * Call to this method will cancel processig of all the phase completion tasks until
     * this task does not finish.<BR>
     * @see CancellableTask for information about implementation requirements
     * @param task The task which.
     */    
    public ModificationResult runModificationTask(CancellableTask<WorkingCopy> task) throws IOException {        
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }
        
        assert !holdsDocumentWriteLock(files) : "JavaSource.runModificationTask called under Document write lock.";    //NOI18N
        
        ModificationResult result = new ModificationResult();
        if (this.files.size()<=1) {
            long start = System.currentTimeMillis();
            CompilationInfo currentInfo = null;
            synchronized (this) {
                if (this.currentInfo != null &&  (this.flags & INVALID) == 0) {
                    currentInfo = this.currentInfo;
                }
            }
            if (currentInfo == null) {
                currentInfo = createCurrentInfo(this,this.files.isEmpty() ? null : this.files.iterator().next(), filterListener, null);
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
            final JavaSource.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }            
                this.javacLock.lock();
                try {
                    WorkingCopy copy = new WorkingCopy (currentInfo);
                    task.run (copy);
                    List<Difference> diffs = copy.getChanges();
                    if (diffs != null && diffs.size() > 0)
                        result.diffs.put(currentInfo.getFileObject(), diffs);
                } catch (Exception e) {
                    IOException ioe = new IOException ();
                    ioe.initCause(e);
                    throw ioe;
                } finally {
                    this.javacLock.unlock();
                }
            } finally {
                currentRequest.cancelCompleted (request);
            }
            TimesCollector.getDefault().reportTime(currentInfo.getFileObject(),  "java-source-modification-task",   //NOI18N
            "Modification Task", System.currentTimeMillis() - start);   //NOI18N
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
                        CompilationInfo ci = createCurrentInfo(this,activeFile, filterListener, jt);
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
                } catch (Exception e) {
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
        return result;
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
        CompilationInfo currentInfo;
        synchronized (this) {
            currentInfo = this.currentInfo;
        }
        if (currentInfo == null) {
            currentInfo = createCurrentInfo (this, this.files.isEmpty() ? null : this.files.iterator().next(), filterListener, null);
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
        synchronized (JavaSource.class) {
            toRemove.add (task);
        }
    }
    
    /**Rerun the task in case it was already run. Does nothing if the task was not already run.
     *
     * @task to reschedule
     */
    void rescheduleTask(CancellableTask<CompilationInfo> task) {
        synchronized (JavaSource.class) {
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
            sourceLevel = SourceLevelQuery.getSourceLevel(files.iterator().next());
        }
        if (sourceLevel == null) {
            sourceLevel = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion().toString();
        }        
        JavacTaskImpl javacTask = createJavacTask(getClasspathInfo(), diagnosticListener, sourceLevel, false);
        Context context = javacTask.getContext();
        Messager.preRegister(context, null);
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
        if (!backgroundCompilation) {
            if (Boolean.getBoolean("org.netbeans.api.java.source.JavaSource.USE_COMPILER_LINT")) { // XXX temp workaround for #76702
                options.add("-Xlint");
                options.add("-Xlint:-serial");
            }
            options.add("-Xjcov"); //NOI18N, Make the compiler store end positions
        } else {
            options.add("-XDbackgroundCompilation");    //NOI18N
            options.add("-XDcompilePolicy=byfile");     //NOI18N
        }
        options.add("-g:"); // NOI18N, Enable some debug info
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
                assert !it.hasNext();
                currentPhase = Phase.PARSED;
                long end = System.currentTimeMillis();
                FileObject file = currentInfo.getFileObject();
                TimesCollector.getDefault().reportReference(file, "compilationUnit", "[M] Compilation Unit", unit);     //NOI18N
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
        } catch (Abort abort) {
            currentPhase = Phase.UP_TO_DATE;
        } catch (IOException ex) {
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (RuntimeException ex) {
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (Error ex) {
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
        String key = phase2Key.get(phase);
        String message = phase2Message.get(phase);
        assert key != null && message != null;
        TimesCollector.getDefault().reportTime (source,key,message,time);
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
        Request r = currentRequest.getTaskToCancel (this);
        try {
            if (r != null) {
                r.task.cancel();
            }
        }
        finally {
            currentRequest.cancelCompleted(r);
        }
        resetTask.schedule(REPARSE_DELAY);
    }
    
    /**
     * Not synchronized, only sets the atomic state and clears the listeners
     *
     */
    private void resetStateImpl() {
        synchronized (JavaSource.class) {
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
            Logger.getLogger("global").log(Level.WARNING,String.format("File: %s has no EditorCookie.Observable", FileUtil.getFileDisplayName (fo)));      //NOI18N
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
                    && phase.equals (otherRequest.phase)
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
                        synchronized (JavaSource.class) {
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
                                        r.task.run (null);
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
                                    synchronized (JavaSource.class) {
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
                                            ci = createCurrentInfo (js,js.files.isEmpty() ? null : js.files.iterator().next(), js.filterListener, null);
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
                                            final Phase phase = JavaSource.moveToPhase (r.phase, ci, true);
                                            boolean shouldCall = phase.compareTo(r.phase)>=0;
                                            if (shouldCall) {
                                                synchronized (js) {
                                                    shouldCall &= (js.flags & INVALID)==0;
                                                }
                                                if (shouldCall) {
                                                    //The state (or greater) was reached and document was not modified during moveToPhase
                                                    try {
                                                        final long startTime = System.currentTimeMillis();
                                                        ((CancellableTask<CompilationInfo>)r.task).run (ci); //XXX: How to do it in save way?
                                                        final long endTime = System.currentTimeMillis();
                                                        if (reportSlowTasks) {
                                                            if ((endTime - startTime) > SLOW_TASK_LIMIT) {
                                                                Logger.getLogger("global").log(Level.INFO,String.format("JavaSource executed a slow task: %s in %d ms.",  //NOI18N
                                                                    r.task.getClass().toString(), (endTime-startTime)));
                                                            }
                                                            final long cancelTime = currentRequest.getCancelTime();
                                                            if (cancelTime >= startTime && (endTime - cancelTime) > SLOW_CANCEL_LIMIT) {
                                                                Logger.getLogger("global").log(Level.INFO,String.format("Task: %s ignored cancel for %d ms.",  //NOI18N
                                                                    r.task.getClass().toString(), (endTime-cancelTime)));
                                                            }
                                                        }
                                                    } catch (Exception re) {
                                                        Exceptions.printStackTrace (re);
                                                    }
                                                }
                                            }
                                        } finally {
                                            javacLock.unlock();
                                        }

                                        if (r.reschedule) {                                            
                                            synchronized (JavaSource.class) {
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
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            JavaSource.this.resetState(true, false);
        }
        
    }
    
    private static class EditorRegistryListener implements ChangeListener, CaretListener {
                        
        private JTextComponent lastEditor;
        
        public EditorRegistryListener () {
            Registry.addChangeListener(this);
        }
                
        public void stateChanged(ChangeEvent event) {
            final JTextComponent editor = Registry.getMostActiveComponent();
            if (lastEditor != editor) {
                if (lastEditor != null) {
                    lastEditor.removeCaretListener(this);
                }
                lastEditor = editor;
                if (lastEditor != null) {                    
                    lastEditor.addCaretListener(this);
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
        
        private void handleInvalidDataObject(DataObject invalidDO) {
            invalidDO.removePropertyChangeListener(wlistener);
            if (fobj.isValid()) {
                // file object still exists try to find new data object
                try {
                    dobj = DataObject.find(fobj);
                    dobj.addPropertyChangeListener(wlistener);
                    assignDocumentListener(fobj);
                    resetState(true, true);
                } catch (IOException ex) {
                    // should not occur
                    Logger.getLogger(JavaSource.class.getName()).log(Level.SEVERE,
                                                                     ex.getMessage(),
                                                                     ex);
                }
            }
        }
        
    }
    
    private final class FilterListener implements ChangeListener {        
        
        private final JavaFileFilterImplementation filter;
        
        public FilterListener (final JavaFileFilterImplementation filter) {
            this.filter = filter;
            this.filter.addChangeListener(WeakListeners.change(this, this.filter));
        }
        
        public void stateChanged(ChangeEvent event) {
            JavaSource.this.resetState(true, false);
        }
    }
        
    private static CompilationInfo createCurrentInfo (final JavaSource js, final FileObject fo, final FilterListener filterListener, final JavacTaskImpl javac) throws IOException {        
        CompilationInfo info = new CompilationInfo (js, fo, filterListener == null ? null : filterListener.filter, javac);
        TimesCollector.getDefault().reportReference(fo, CompilationInfo.class.toString(), "[M] CompilationInfo", info);     //NOI18N
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
        public CommandEnvironment getCommandEnvironment(WorkingCopy copy) {
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
    }
    
    private static class CurrentRequestReference {                
        
        
        private JavaSource.Request reference;
        private JavaSource.Request canceledReference;
        private long cancelTime;
        private boolean canceled;
        
        public CurrentRequestReference () {
        }
        
        public boolean setCurrentTask (JavaSource.Request reference) throws InterruptedException {
            boolean result = false;
            synchronized (this) {
                while (this.canceledReference!=null) {
                    this.wait();
                }
                result = this.canceled;
                this.canceled = false;
                this.cancelTime = 0;
                this.reference = reference;                
            }
            return result;
        }
        
        public JavaSource.Request getTaskToCancel (final Priority priority) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (this) {
                    if (this.reference != null && priority.compareTo(this.reference.priority) < 0) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;                    
                        if (reportSlowTasks) {
                            cancelTime = System.currentTimeMillis();
                        }
                    }
                }
            }
            return request;
        }
        
        public JavaSource.Request getTaskToCancel (final JavaSource js) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (this) {
                    if (this.reference != null && js.equals(this.reference.javaSource)) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        if (reportSlowTasks) {
                            cancelTime = System.currentTimeMillis();
                        }
                    }
                }
            }
            return request;
        }
        
        public JavaSource.Request getTaskToCancel (final CancellableTask task) {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {
                synchronized (this) {
                    if (this.reference != null && task == this.reference.task) {
                        assert this.canceledReference == null;
                        request = this.reference;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                    }
                }
            }
            return request;
        }
        
        public JavaSource.Request getTaskToCancel () {
            JavaSource.Request request = null;
            if (!factory.isDispatchThread(Thread.currentThread())) {                
                synchronized (this) {
                     request = this.reference;
                    if (request != null) {
                        assert this.canceledReference == null;
                        this.canceledReference = request;
                        this.reference = null;
                        this.canceled = true;
                        if (reportSlowTasks) {
                            cancelTime = System.currentTimeMillis();
                        }
                    }
                }
            }
            return request;
        }
        
        public synchronized boolean isCanceled () {
            return this.canceled;
        }
        
        public synchronized long getCancelTime () {
            return this.cancelTime;
        }
        
        public void cancelCompleted (final JavaSource.Request request) {
            if (request != null) {
                synchronized (this) {
                    assert request == this.canceledReference;
                    this.canceledReference = null;
                    this.notify();
                }
            }
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
        String dumpDir = System.getProperty("netbeans.user") + "/var/log/"; //NOI18N
        String src = info.getText();
        FileObject file = info.getFileObject();
        String fileName = FileUtil.getFileDisplayName(info.getFileObject());
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
                Logger.getLogger("global").log(Level.INFO, "Error when writing parser dump file!", ioe); // NOI18N
            }
        }
        if (dumpSucceeded) {
            Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against java/source and attach dump file '"  // NOI18N
                    + f.getAbsolutePath() + "'."); // NOI18N
            Exceptions.printStackTrace(t);
        } else {
            Logger.getLogger("global").log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }
    
}
