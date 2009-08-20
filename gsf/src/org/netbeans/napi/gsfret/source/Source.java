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

package org.netbeans.napi.gsfret.source;

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
import java.util.IdentityHashMap;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParseEvent;
import org.netbeans.modules.gsf.api.ParseListener;
import org.netbeans.modules.gsf.api.Parser;
import org.netbeans.modules.gsf.api.SourceFileReader;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.EmbeddingModel;
import org.netbeans.napi.gsfret.source.ClasspathInfo.PathKind;
import org.netbeans.napi.gsfret.source.ModificationResult.Difference;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsf.api.DataLoadersBridge;
import org.netbeans.modules.gsf.api.EditHistory;
import org.netbeans.modules.gsf.api.IncrementalEmbeddingModel;
import org.netbeans.modules.gsf.api.IncrementalParser;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsfret.source.SourceAccessor;
import org.netbeans.modules.gsfret.source.usages.ClassIndexImpl;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.netbeans.modules.gsfret.source.util.LowMemoryEvent;
import org.netbeans.modules.gsfret.source.util.LowMemoryListener;
import org.netbeans.modules.gsfret.source.util.LowMemoryNotifier;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * This file is based on the JavaSource class in Retouche's org.netbeans.modules.gsfpath.api.source package.
 * It represents an open source file in the editor.
 *
 * @author Petr Hrebejk
 * @author Tomas Zezula
 * @author Tor Norbye
 */
public final class Source {

    public static enum Priority {
        MAX,
        HIGH,
        ABOVE_NORMAL,
        NORMAL,
        BELOW_NORMAL,
        LOW,
        MIN
    }

    /**
     * This specialization of {@link IOException} signals that a {@link Source#runUserActionTask}
     * or {@link Source#runModificationTask} failed due to lack of memory. The {@link InsufficientMemoryException#getFile}
     * method returns a file which cannot be processed.
     */
    public static final class InsufficientMemoryException extends IOException {

        private FileObject fo;

        private InsufficientMemoryException (final String message, final FileObject fo) {
            super (message);
            this.fo = fo;
        }

        private InsufficientMemoryException (FileObject fo) {
            this (NbBundle.getMessage(Source.class, "MSG_UnsufficientMemoryException", FileUtil.getFileDisplayName (fo)),fo);
        }


        /**
         * Returns file which cannot be processed due to lack of memory.
         * @return {@link FileObject}
         */
        public FileObject getFile () {
            return this.fo;
        }
    }

    /**
     * Constants for Source.flags
     */
    private static final int INVALID = 1;
    private static final int CHANGE_EXPECTED = INVALID<<1;
    private static final int RESCHEDULE_FINISHED_TASKS = CHANGE_EXPECTED<<1;
    private static final int UPDATE_INDEX = RESCHEDULE_FINISHED_TASKS<<1;
    
    /**Slow task reporting*/
    private static final boolean reportSlowTasks = Boolean.getBoolean("org.netbeans.napi.gsfret.source.Source.reportSlowTasks");   //NOI18N
    /**Limit for task to be marked as a slow one, in ms*/
    private static final int SLOW_TASK_LIMIT = 250;
    private static final int SLOW_CANCEL_LIMIT = 50;

    /**Not final for tests.*/
    static int REPARSE_DELAY = 500;

    /**
     * Helper maps mapping the {@link Phase} to key and message for
     * the {@link TimesCollector}
     */
    private static Map<Phase, String> phase2Key = new HashMap<Phase, String> ();
    private static Map<Phase, String> phase2Message = new HashMap<Phase, String> ();

    /**
     * Init the maps
     */
    static {
        SourceAccessor.setINSTANCE (new JavaSourceAccessorImpl ());
        phase2Key.put(Phase.PARSED,"parsed");                                   //NOI18N
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N

        phase2Key.put(Phase.ELEMENTS_RESOLVED,"sig-attributed");                //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N

        phase2Key.put(Phase.RESOLVED, "attributed");                            //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N

    }

    private final static PriorityBlockingQueue<Request> requests = new PriorityBlockingQueue<Request> (10, new RequestComparator());
    private final static Map<Source, Collection<Request>> finishedRequests = new WeakHashMap<Source,Collection<Request>>();
    private final static Map<Source,Collection<Request>> waitingRequests = new WeakHashMap<Source,Collection<Request>>();
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
    private PropertyChangeListener dataObjectListener;

    private final ClasspathInfo classpathInfo;
    private CompilationInfo currentInfo;
    private Map<String,ParserResult> recentParseResult = new HashMap<String,ParserResult>();
    private Map<EmbeddingModel,Collection<? extends TranslatedSource>> recentEmbeddingTranslations = 
            new IdentityHashMap<EmbeddingModel,Collection<? extends TranslatedSource>>();
    private EditHistory editHistory = new EditHistory();
    private java.util.Stack<CompilationInfo> infoStack = new java.util.Stack<CompilationInfo> ();

    private int flags = 0;

    //Preprocessor support
    private Object/*FilterListener*/ filterListener;
    private boolean possiblyIncremental;

    static {
        Executors.newSingleThreadExecutor(factory).submit (new CompilationJob());
    }


    /**
     * Returns a {@link Source} instance representing given {@link org.openide.filesystems.FileObject}s
     * and classpath represented by given {@link ClasspathInfo}.
     *
     *
     * @param cpInfo the classpaths to be used.
     * @param files for which the {@link Source} should be created
     * @return a new {@link Source}
     * @throws IllegalArgumentException if fileObject or cpInfo is null
     */
    public static Source create(final ClasspathInfo cpInfo, final Collection<? extends FileObject> files) throws IllegalArgumentException {
        if (files == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        try {
            return new Source(cpInfo, files);
        } catch (DataObjectNotFoundException donf) {
            Logger.getLogger("global").warning("Ignoring non existent file: " + FileUtil.getFileDisplayName(donf.getFileObject()));     //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    /**
     * Returns a {@link Source} instance representing given {@link org.openide.filesystems.FileObject}s
     * and classpath represented by given {@link ClasspathInfo}.
     *
     *
     * @param cpInfo the classpaths to be used.
     * @param files for which the {@link Source} should be created
     * @return a new {@link Source}
     * @throws IllegalArgumentException if fileObject or cpInfo is null
     */
    public static Source create(final ClasspathInfo cpInfo, final FileObject... files) throws IllegalArgumentException {
        if (files == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        return create(cpInfo, Arrays.asList(files));
    }

    private static Map<FileObject, Reference<Source>> file2JavaSource = new WeakHashMap<FileObject, Reference<Source>>();

    public static void clearSourceCache() {
        file2JavaSource.clear();
    }
    
    /**
     * Returns a {@link Source} instance associated to given {@link org.openide.filesystems.FileObject},
     * it returns null if the {@link Document} is not associanted with data type providing the {@link Source}.
     *
     *
     * @param fileObject for which the {@link Source} should be found/created.
     * @return {@link Source} or null
     * @throws IllegalArgumentException if fileObject is null
     */
    public static Source forFileObject(FileObject fileObject) throws IllegalArgumentException {
        if (fileObject == null) {
            throw new IllegalArgumentException ("fileObject == null");  //NOI18N
        }
        if (!fileObject.isValid() || fileObject.isFolder()) {
            return null;
        }
        if (!LanguageRegistry.getInstance().isSupported(fileObject.getMIMEType())) {
            return null;
        }

        Reference<Source> ref = file2JavaSource.get(fileObject);
        Source js = ref != null ? ref.get() : null;

        if (js == null) {
            file2JavaSource.put(fileObject, new WeakReference(js = create(ClasspathInfo.create(fileObject), fileObject)));
        }

        return js;
    }

    /**
     * Returns a {@link Source} instance associated to {@link org.openide.filesystems.FileObject}
     * the {@link Document} was created from, it returns null if the {@link Document} is not
     * associanted with data type providing the {@link Source}.
     *
     *
     * @param doc {@link Document} for which the {@link Source} should be found/created.
     * @return {@link Source} or null
     * @throws IllegalArgumentException if doc is null
     */
    public static Source forDocument(Document doc) throws IllegalArgumentException {
        if (doc == null) {
            throw new IllegalArgumentException ("doc == null");  //NOI18N
        }
        Reference<Source> ref = (Reference<Source>)doc.getProperty(Source.class);
        Source js = ref != null ? ref.get() : null;
        if (js == null) {
            FileObject fo = DataLoadersBridge.getDefault().getFileObject(doc);
            if (fo != null)
                js = forFileObject(fo);
        }
        return js;
    }
    

    /**
     * Creates a new instance of Source
     *
     *
     * @param files to create Source for
     * @param cpInfo classpath info
     */
    private Source (ClasspathInfo cpInfo, Collection<? extends FileObject> files) throws IOException {
        this.files = Collections.unmodifiableList(new ArrayList (files));   //Create a defensive copy, prevent modification
        this.fileChangeListener = new FileChangeListenerImpl ();
        boolean multipleSources = this.files.size() > 1, filterAssigned = false;
        for (Iterator<? extends FileObject> it = this.files.iterator(); it.hasNext();) {
            FileObject file = it.next();
            try {
                //TimesCollector.getDefault().reportReference( file, Source.class.toString(), "[M] Source", this );       //NOI18N
                if (!multipleSources) {
                    this.possiblyIncremental = LanguageRegistry.getInstance().isIncremental(file.getMIMEType());
                    file.addFileChangeListener(FileUtil.weakFileChangeListener(this.fileChangeListener,file));
                    this.assignDocumentListener(file);
                    this.dataObjectListener = DataLoadersBridge.getDefault().getDataObjectListener(file, new FileChangeAdapter() {
                        @Override
                        public void fileChanged(FileEvent fe) {
                            try {
                                assignDocumentListener(fe.getFile());
                                resetState(true, true);
                            } catch (IOException ex) {
                                // should not occur
                                Logger.getLogger(Source.class.getName()).log(Level.SEVERE,
                                        ex.getMessage(),
                                        ex);
                            }
                        }
                    });
                }
                //if (!filterAssigned) {
                //    filterAssigned = true;
                //    JavaFileFilterImplementation filter = JavaFileFilterQuery.getFilter(file);
                //    if (filter != null) {
                //        this.filterListener = new FilterListener (filter);
                //    }
                //}
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
        if (files.size() == 1) {
            this.rootFo = classpathInfo.getClassPath(PathKind.SOURCE).findOwnerRoot(files.iterator().next());
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
     * Call to this method will cancel processing of all the phase completion tasks until
     * this task does not finish.<BR>
     * @see org.netbeans.napi.gsfret.source.CancellableTask for information about implementation requirements
     * @param task The task which.
     * @param shared if true the java compiler may be reused by other {@link org.netbeans.napi.gsfret.source.CancellableTasks},
     * the value false may have negative impact on the IDE performance.
     */
    public void runUserActionTask( final CancellableTask<CompilationController> task, final boolean shared) throws IOException {
        if (task == null) {
            throw new IllegalArgumentException ("Task cannot be null");     //NOI18N
        }

        boolean assertionsEnabled = false;
        assert assertionsEnabled = true;
        if (assertionsEnabled) {
            if (!(javacLock.isHeldByCurrentThread() || !holdsDocumentWriteLock(files))) {
                String msg = "Source.runCompileControlTask called under Document write lock."; // NOI18N
                Logger.getLogger(Source.class.getName()).log(Level.INFO, msg,
                        new IllegalStateException(msg));
            }
        }
        
        if (this.files.size()<=1) {                        
            final Source.Request request = currentRequest.getTaskToCancel();
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
            final Source.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }
                this.javacLock.lock();
                try {
                    ParserTaskImpl jt = null;
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
                            jt = ci.getParserTask();
//                            Log.instance(jt.getContext()).nerrors = 0;
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
        
        assert javacLock.isHeldByCurrentThread() || !holdsDocumentWriteLock(files) : "Source.runCompileControlTask called under Document write lock.";    //NOI18N
        
        ModificationResult result = new ModificationResult(this);
        if (this.files.size()<=1) {
            //long start = System.currentTimeMillis();            
            final Source.Request request = currentRequest.getTaskToCancel();
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
                    WorkingCopy copy = new WorkingCopy (currentInfo);
                    task.run (copy);
                    List<Difference> diffs = copy.getChanges();
                    if (diffs != null && diffs.size() > 0)
                        result.diffs.put(currentInfo.getFileObject(), diffs);
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
            //TimesCollector.getDefault().reportTime(currentInfo.getFileObject(),  "gsf-source-modification-task",   //NOI18N
            //  "Modification Task", System.currentTimeMillis() - start);   //NOI18N
        }
        else {
            final Source.Request request = currentRequest.getTaskToCancel();
            try {
                if (request != null) {
                    request.task.cancel();
                }
                this.javacLock.lock();
                try {
                    ParserTaskImpl jt = null;
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
                            jt = ci.getParserTask();
                            //jt = ci.getParserTask();
//                            Log.instance(jt.getContext()).nerrors = 0;
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
        synchronized (Source.class) {
            toRemove.add (task);
        }
    }

    /**Rerun the task in case it was already run. Does nothing if the task was not already run.
     *
     * @task to reschedule
     */
    void rescheduleTask(CancellableTask<CompilationInfo> task) {
        synchronized (Source.class) {
            Source.Request request = this.currentRequest.getTaskToCancel (task);
            if ( request == null) {
out:            for (Iterator<Collection<Request>> it = finishedRequests.values().iterator(); it.hasNext();) {
                    Collection<Request> cr = it.next ();
                    for (Iterator<Request> it2 = cr.iterator(); it2.hasNext();) {
                        Request fr = it2.next();
                        if (task == fr.task) {
                            it2.remove();
                            Source.requests.add(fr);
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
     * Marks this {@link Source} as modified, causes that the cached information are
     * cleared and all the PhaseCompletionTasks are restarted.
     * The only client of this method should be the JavaDataObject or other DataObjects
     * providing the {@link Source}. If you call this method in another case you are
     * probably doing something incorrect.
     */
    void revalidate () {
        this.resetState(true, false);
    }

    /**
     * Returns the classpaths ({@link ClasspathInfo}) used by this
     * {@link Source}
     *
     *
     * @return {@link ClasspathInfo}, never returns null.
     */
    public ClasspathInfo getClasspathInfo() {
        return classpathInfo;
    }

    /**
     * Returns unmodifiable {@link Collection} of {@link FileObject}s used by this {@link Source}
     * @return the {@link FileObject}s
     */
    public Collection<FileObject> getFileObjects() {
        return files;
    }

    ParserTaskImpl createParserTask(/*final DiagnosticListener<? super SourceFileObject> diagnosticListener,*/ CompilationInfo compilationInfo) {
        //assert diagnosticListener == null;
        Language language = compilationInfo.getLanguage();
        assert language != null;
        ParserTaskImpl javacTask = createParserTask(language, compilationInfo, getClasspathInfo(), /*diagnosticListener,*/ false);
//        Context context = javacTask.getContext();
//        Messager.preRegister(context, null);
//        ErrorHandlingJavadocEnter.preRegister(context);
//        JavadocMemberEnter.preRegister(context);
//        SourceUtils.JavaDocEnv.preRegister(context, getClasspathInfo());
//        Scanner.Factory.instance(context);
//        Builder2.instance(context).keepComments = true;
        return javacTask;
    }

    private static ParserTaskImpl createParserTask(Language language, final CompilationInfo currentInfo, final ClasspathInfo cpInfo, /*final DiagnosticListener<? super SourceFileObject> diagnosticListener,*/ final boolean backgroundCompilation) {
        ParserTaskImpl jti = new ParserTaskImpl(language);

        return jti;
    }

    /**
     * Not synchronized, only the CompilationJob's thread can call it!!!!
     *
     */
    static Phase moveToPhase (final Phase phase, final CompilationInfo currentInfo, final boolean cancellable) throws IOException {
        Phase currentPhase = currentInfo.getPhase();
        final boolean isMultiFiles = currentInfo.getSource().files.size()>1;
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

                ParserFile file = new DefaultParserFile(currentInfo.getFileObject(), null, false);
                List<ParserFile> sourceFiles = Collections.singletonList(file);
                final FileObject bufferFo = currentInfo.getFileObject();

                final ParserResult[] resultHolder = new ParserResult[1];
                ParseListener listener = new ParseListener() {
                    final List<Error> errors = new ArrayList<Error>();

                    public void started(ParseEvent e) {
                        errors.clear();
                    }

                    public void error(Error error) {
                        errors.add(error);
                    }

                    public void exception(Exception exception) {
                        Exceptions.printStackTrace(exception);
                    }

                    public void finished(ParseEvent e) {
                        // TODO - check state
                        if (e.getKind() == ParseEvent.Kind.PARSE) {
                            ParserResult result = e.getResult();
                            for (Error error : errors) {
                                result.addError(error);
                            }
                            resultHolder[0] = result;
                        }
                    }
                };
                
                LanguageRegistry registry = LanguageRegistry.getInstance();
                String mimeType = currentInfo.getFileObject().getMIMEType();
                List<Language> languages = registry.getApplicableLanguages(mimeType);
                Source source = currentInfo.getSource();
                currentInfo.setHistory(source.editHistory);
                
            for (Language language : languages) {
                EmbeddingModel model = registry.getEmbedding(language.getMimeType(), mimeType);
                assert language != null;
                Parser parser = language.getParser(); // Todo - call createParserTask here?
                IncrementalParser incrementalParser = parser instanceof IncrementalParser ? (IncrementalParser)parser : null;

if (cancellable && currentRequest.isCanceled()) {
    // Keep the currentPhase unchanged, it may happen that an userActionTask
    // running after the phace completion task may still use it.
    return Phase.MODIFIED;
}
// <editor-fold defaultstate="collapsed" desc="Peformance">                
long vsTime = -1;
long parseTime = -1;
// </editor-fold>
                if (parser != null) {
                    if (model != null) {
                        Document document = currentInfo.getDocument();
                            
                        if (document == null) {
                            // Ensure document is forced open such that info.getDocument() will not yield null
                            GsfUtilities.getDocument(currentInfo.getFileObject(), true);
                            document = currentInfo.getDocument();
                        } else {
                            // If you have a plain text file and try to assign a new mime type
                            // to it, we end up in a scenario where the file mime type and the
                            // document mime type do not (at least not yet) match - with bad results
                            // later on (mime type lookup, TokenHierarchy lookup etc) all return
                            // objects with a wrong/unexpected text/plain mimetype.
                            // See http://www.netbeans.org/issues/show_bug.cgi?id=138948 for details.
                            // It looks like the infrastructure closes the file shortly after this,
                            // so presumably live-changing files like this isn't supported..
                            if ("text/plain".equals(document.getProperty("mimeType"))) { // NOI18N
                                return Phase.MODIFIED;
                            }
                        }
                        
                        if (document == null) {
                            // TODO - log problem
                            continue;
                        }
if (cancellable && currentRequest.isCanceled()) {
    // Keep the currentPhase unchanged, it may happen that an userActionTask
    // running after the phace completion task may still use it.
    return Phase.MODIFIED;
}
// <editor-fold defaultstate="collapsed" desc="Peformance">                     
long vsStart = System.currentTimeMillis();  
// </editor-fold>

                        Collection<? extends TranslatedSource> translations = null;

                        boolean incremental = false;
                        if (model instanceof IncrementalEmbeddingModel && (source.files == null || source.files.size() <= 1)) {
                            incremental = true;
                            IncrementalEmbeddingModel incrementalModel = (IncrementalEmbeddingModel)model;
                            translations = source.recentEmbeddingTranslations.get(model);
                            if (translations != null) {
                                EditHistory history = source.editHistory;
                                if (translations.size() > 0) {
                                    history = EditHistory.getCombinedEdits(translations.iterator().next().getEditVersion(), source.editHistory);
                                }
                                if (history != null && history.isValid()) {
                                    IncrementalEmbeddingModel.UpdateState updated = incrementalModel.update(history, translations);
                                    if (updated == IncrementalEmbeddingModel.UpdateState.COMPLETED) {
                                        // No need to parse - nothing else to be done for this mime type
                                        ParserResult result = source.recentParseResult.get(language.getMimeType());
                                        for (TranslatedSource translated : translations) {
                                            translated.setEditVersion(source.editHistory.getVersion());
                                        }
                                        if (result != null) {
                                            currentInfo.addEmbeddingResult(language.getMimeType(), result);
                                            result.setUpdateState(ParserResult.UpdateState.NO_CHANGE);
                                            continue;
                                        }
                                    } else if (updated == IncrementalEmbeddingModel.UpdateState.FAILED) {
                                        // Force update!
                                        translations = null;
                                    } else {
                                        assert updated == IncrementalEmbeddingModel.UpdateState.UPDATED;
                                        // Continue to parse below
                                        for (TranslatedSource translated : translations) {
                                            translated.setEditVersion(source.editHistory.getVersion());
                                        }
                                    }
                                } else {
                                    // Force update
                                    translations = null;
                                }
                            }
                        }

                        if (translations == null) {
                            // No incremental support or previous result
                            translations = model.translate(document);
                            if (incremental) {
                                source.recentEmbeddingTranslations.put(model, translations);
                                for (TranslatedSource translated : translations) {
                                    translated.setEditVersion(source.editHistory.getVersion());
                                }
                            }
                        }
if (cancellable && currentRequest.isCanceled()) {
    // Keep the currentPhase unchanged, it may happen that an userActionTask
    // running after the phace completion task may still use it.
    return Phase.MODIFIED;
}
// <editor-fold defaultstate="collapsed" desc="Peformance">                                        
vsTime = System.currentTimeMillis() - vsStart; //some impls. may compute and cache the results just on model.translate(document)
parseTime = 0;
// </editor-fold>
                        for (TranslatedSource translatedSource : translations) {
// <editor-fold defaultstate="collapsed" desc="Peformance">                                                 
vsStart = System.currentTimeMillis();
// </editor-fold>
                            String buffer = translatedSource.getSource();
// <editor-fold defaultstate="collapsed" desc="Peformance">                                                 
vsTime += System.currentTimeMillis() - vsStart;
long parseStart = System.currentTimeMillis();
// </editor-fold>
                            SourceFileReader reader = new StringSourceFileReader(buffer, bufferFo);
                            ParserResult result = null;
                            if (incrementalParser != null && (source.files == null || source.files.size() <= 1)) {
                                ParserResult previousResult = source.recentParseResult.get(language.getMimeType());
                                if (previousResult != null) {
                                    EditHistory history = EditHistory.getCombinedEdits(previousResult.getEditVersion(), source.editHistory);
                                    if (history != null && history.isValid()) {
                                        ParserResult ir = incrementalParser.parse(file, reader, translatedSource, history, previousResult);
                                        if (ir != null) {
                                            ParserResult.UpdateState state = ir.getUpdateState();
                                            if (state != ParserResult.UpdateState.FAILED) {
                                                result = ir;
                                            }
                                        }
                                    }
                                }
                            }
                            if (result == null) {
                                Parser.Job job = new Parser.Job(sourceFiles, listener, reader, translatedSource);
                                parser.parseFiles(job);
                                result = resultHolder[0];
                            }
                            if (incrementalParser != null || (incremental && translations != null)) {
                                // Hmm, this will only work correctly for the FIRST element if the collections are > 1
                                source.recentParseResult.put(language.getMimeType(), result);
                                result.setEditVersion(source.editHistory.getVersion());
                            }
// <editor-fold defaultstate="collapsed" desc="Peformance">                                                 
parseTime += System.currentTimeMillis() - parseStart;     
// </editor-fold>
if (cancellable && currentRequest.isCanceled()) {
    // Keep the currentPhase unchanged, it may happen that an userActionTask
    // running after the phace completion task may still use it.
    return Phase.MODIFIED;
}
                            result.setTranslatedSource(translatedSource);
                            assert result != null;
                            currentInfo.addEmbeddingResult(language.getMimeType(), result);
                        }
                    } else {
// <editor-fold defaultstate="collapsed" desc="Peformance">                                             
long parseStart = System.currentTimeMillis();    
// </editor-fold>
                        String buffer = currentInfo.getText();
                        SourceFileReader reader = new StringSourceFileReader(buffer, bufferFo);

                        ParserResult result = null;
                        if (incrementalParser != null && (source.files == null || source.files.size() <= 1)) {
                            ParserResult previousResult = source.recentParseResult.get(language.getMimeType());
                            if (previousResult != null) {
                                EditHistory history = EditHistory.getCombinedEdits(previousResult.getEditVersion(), source.editHistory);
                                if (history != null && history.isValid()) {
                                    result = incrementalParser.parse(file, reader, null, history, previousResult);
                                    if (result != null) {
                                        ParserResult.UpdateState state = result.getUpdateState();
                                        if (state == ParserResult.UpdateState.FAILED) {
                                            result = null;
                                        }
                                    }
                                }
                            }
                        }
                        if (result == null) {
                            Parser.Job job = new Parser.Job(sourceFiles, listener, reader, null);
                            parser.parseFiles(job);
                            result = resultHolder[0];
                        }
                        if (incrementalParser != null) {
                            source.recentParseResult.put(language.getMimeType(), result);
                            result.setEditVersion(source.editHistory.getVersion());
                        }
// <editor-fold defaultstate="collapsed" desc="Peformance">                     
parseTime = System.currentTimeMillis() - parseStart;
// </editor-fold>
if (cancellable && currentRequest.isCanceled()) {
    // Keep the currentPhase unchanged, it may happen that an userActionTask
    // running after the phace completion task may still use it.
    return Phase.MODIFIED;
}
                        assert result != null;
                        currentInfo.addEmbeddingResult(language.getMimeType(), result);
                    }
                }
// <editor-fold defaultstate="collapsed" desc="Peformance">                     

if(vsTime > 0) {                
    Logger.getLogger("TIMER").log(Level.FINE, "Virtual Source (" + language.getMimeType() + ")", 
        new Object[] {currentInfo.getFileObject(), vsTime});
}
if(parseTime > 0) {                
    Logger.getLogger("TIMER").log(Level.FINE, "Parsing (" + language.getMimeType() + ")",
        new Object[] {currentInfo.getFileObject(), parseTime});
}
// </editor-fold>
            }
                currentPhase = Phase.PARSED;

                if (source.possiblyIncremental) {
                    EditHistory oldHistory = source.editHistory;
                    source.editHistory = new EditHistory();
                    oldHistory.add(source.editHistory);
                }

//                long end = System.currentTimeMillis();
//                FileObject file = currentInfo.getFileObject();
//                //TimesCollector.getDefault().reportReference(file, "compilationUnit", "[M] Compilation Unit", unit);     //NOI18N
//                logTime (file,currentPhase,(end-start));
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
                // Noop right now - revisit when I add a parser which needs it (groovy?)
                //currentInfo.getParserTask().enter();
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
                // Noop right now - revisit when I add a parser which needs it (groovy?)
                //currentInfo.getParserTask().analyze();
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
        //} catch (Error abort) { // Abort in com.sun.tools is not here
        //    currentPhase = Phase.UP_TO_DATE;
//        } catch (IOException ex) {
//            dumpSource(currentInfo, ex);
//            throw ex;
        } catch (RuntimeException ex) {
            dumpSource(currentInfo, ex); 
            throw ex;
        } catch (java.lang.Error ex) {
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
        //TimesCollector.getDefault().reportTime (source,key,message,time);
    }

    private final RequestProcessor.Task resetTask = RequestProcessor.getDefault().create(new Runnable() {
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
        synchronized (Source.class) {
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
                if ((cr=Source.finishedRequests.remove(this)) != null && cr.size()>0)  {
                    Source.requests.addAll(cr);
                }
            }
            if ((cr=Source.waitingRequests.remove(this)) != null && cr.size()>0)  {
                Source.requests.addAll(cr);
            }
        }
    }

    private void updateIndex () {
        if (this.rootFo != null) {
            try {
                for (Language language : LanguageRegistry.getInstance()) {
                    if (language.getIndexer() != null) {
                        ClassIndexImpl ciImpl = ClassIndexManager.get(language).getUsagesQuery(this.rootFo.getURL());
                        if (ciImpl != null) {
                            ciImpl.setDirty(this);
                        }
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    /** For test framework only */
    public void testUpdateIndex() {
        updateIndex();
    }

    private void assignDocumentListener(FileObject fo) throws IOException {
        EditorCookie.Observable ec =  DataLoadersBridge.getDefault().getCookie(fo,EditorCookie.Observable.class);
        if (ec != null) {
            this.listener = new DocListener (ec);
        } else {
            Logger.getLogger("global").log(Level.WARNING,String.format("File: %s has no EditorCookie.Observable", FileUtil.getFileDisplayName (fo)));      //NOI18N
        }
    }

    private static class Request {
        private final CancellableTask<? extends CompilationInfo> task;
        private final Source javaSource;        //XXX: Maybe week, depends on the semantics
        private final Phase phase;
        private final Priority priority;
        private final boolean reschedule;

        public Request (final CancellableTask<? extends CompilationInfo> task, final Source javaSource,
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
                return String.format("One time request for phase: %s with priority: %d to perform: %s", phase != null ? phase.name() : "<null>", priority, task.toString());   //NOI18N
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
                        synchronized (Source.class) {
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
                        Request r = Source.requests.poll(2,TimeUnit.SECONDS);
                        if (r != null) {
                            currentRequest.setCurrentTask(r);
                            try {
                                Source js = r.javaSource;
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
                                    synchronized (Source.class) {
                                        //jl:what does this comment mean?
                                        //Not only the finishedRequests for the current request.javaSource should be cleaned,
                                        //it will cause a starvation
                                        if (toRemove.remove(r.task)) {
                                            continue;
                                        }
                                        synchronized (js) {
                                            boolean changeExpected = (js.flags & CHANGE_EXPECTED) != 0;
                                            if (changeExpected) {
                                                //Skip the task, another invalidation is comming
                                                Collection<Request> rc = Source.waitingRequests.get (r.javaSource);
                                                if (rc == null) {
                                                    rc = new LinkedList<Request> ();
                                                    Source.waitingRequests.put (r.javaSource, rc);
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
                                            final Phase phase = Source.moveToPhase (r.phase, ci, true);
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
                                                                Logger.getLogger("global").log(Level.INFO,String.format("Source executed a slow task: %s in %d ms.",  //NOI18N
                                                                    r.task.getClass().toString(), (endTime-startTime)));
                                                            }
                                                            final long cancelTime = currentRequest.getCancelTime();
                                                            if (cancelTime >= startTime && (endTime - cancelTime) > SLOW_CANCEL_LIMIT) {
                                                                Logger.getLogger("global").log(Level.INFO,String.format("Task: %s ignored cancel for %d ms.",  //NOI18N
                                                                    r.task.getClass().toString(), (endTime-cancelTime)));
                                                            }
                                                        }
                                                    } catch (RuntimeException re) {
                                                        Exceptions.printStackTrace (re);
                                                    }
                                                }
                                            }
                                        } finally {
                                            javacLock.unlock();
                                        }

                                        if (r.reschedule) {
                                            synchronized (Source.class) {
                                                boolean canceled = currentRequest.setCurrentTask(null);
                                                synchronized (js) {
                                                    if ((js.flags & INVALID)!=0 || canceled) {
                                                        //The Source was changed or canceled rechedule it now
                                                        Source.requests.add(r);
                                                    }
                                                    else {
                                                        //Up to date Source add it to the finishedRequests
                                                        Collection<Request> rc = Source.finishedRequests.get (r.javaSource);
                                                        if (rc == null) {
                                                            rc = new LinkedList<Request> ();
                                                            Source.finishedRequests.put (r.javaSource, rc);
                                                        }
                                                        rc.add(r);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (final IOException invalidFile) {
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

    private class DocListener implements DocumentListener, PropertyChangeListener, ChangeListener, TokenHierarchyListener {

        private EditorCookie.Observable ec;
        private DocumentListener docListener;
        private TokenHierarchyListener lexListener;

        public DocListener (EditorCookie.Observable ec) {
            assert ec != null;
            this.ec = ec;
            this.ec.addPropertyChangeListener(WeakListeners.propertyChange(this, this.ec));
            Document doc = ec.getDocument();
            if (doc != null) {
                doc.addDocumentListener(docListener = WeakListeners.create(DocumentListener.class, this, doc));
                if (possiblyIncremental) {
                    TokenHierarchy th = TokenHierarchy.get(doc);
                    th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                }
            }
        }

        public void insertUpdate(DocumentEvent e) {
            //Has to reset cache asynchronously
            //the callback cannot be in synchronized section
            //since NbDocument.runAtomic fires under lock
            Source.this.resetState(true, true);
            if (possiblyIncremental) {
                editHistory.insertUpdate(e);
            }
        }

        public void removeUpdate(DocumentEvent e) {
            //Has to reset cache asynchronously
            //the callback cannot be in synchronized section
            //since NbDocument.runAtomic fires under lock
            Source.this.resetState(true, true);
            if (possiblyIncremental) {
                editHistory.removeUpdate(e);
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                Object old = evt.getOldValue();
                if (old instanceof Document) {
                    if (lexListener != null) {
                        TokenHierarchy th = TokenHierarchy.get((Document) old);
                        th.removeTokenHierarchyListener(lexListener);
                        lexListener = null;
                    }
                    if (docListener != null) {
                        ((Document) old).removeDocumentListener(docListener);
                        docListener = null;
                        // Document closed - don't hang on to parse results
                        recentParseResult.clear();
                        recentEmbeddingTranslations.clear();
                    }
                }
                Document doc = ec.getDocument();
                if (doc != null) {
                    doc.addDocumentListener(docListener = WeakListeners.create(DocumentListener.class, this, doc));
                    if (possiblyIncremental) {
                        TokenHierarchy th = TokenHierarchy.get(doc);
                        th.addTokenHierarchyListener(lexListener = WeakListeners.create(TokenHierarchyListener.class, this,th));
                    }
                    resetState(true, false);
                }
            }
        }

        public void stateChanged(ChangeEvent e) {
            Source.this.resetState(true, false);
        }

        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            assert possiblyIncremental;
            editHistory.tokenHierarchyChanged(evt);
            if (evt.type() == TokenHierarchyEventType.REBUILD) {
                Source.this.resetState(true, true);
            }
        }
    }

    private static class EditorRegistryListener implements CaretListener/*, PropertyChangeListener*/ {
                        
        //private Request request;
        private Reference<JTextComponent> lastEditorRef;
        
        public EditorRegistryListener() {
            EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    editorRegistryChanged();
                }
            });
            editorRegistryChanged();
        }
                
        public void editorRegistryChanged() {
            final JTextComponent editor = EditorRegistry.lastFocusedComponent();
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != editor) {
                if (lastEditor != null) {
                    lastEditor.removeCaretListener(this);
                    //lastEditor.removePropertyChangeListener(this);
                    final Document doc = lastEditor.getDocument();
                    Source js = null;
                    if (doc != null) {
                        js = forDocument(doc);
                    }
                    //if (js != null) {
                    //    js.k24 = false;
                    //}                   
                }
                lastEditorRef = new WeakReference<JTextComponent>(editor);
                if (editor != null) {
                    editor.addCaretListener(this);
                    //lastEditor.addPropertyChangeListener(this);
                }
            }
        }
        
        public void caretUpdate(CaretEvent event) {
            final JTextComponent lastEditor = lastEditorRef == null ? null : lastEditorRef.get();
            if (lastEditor != null) {
                Document doc = lastEditor.getDocument();
                if (doc != null) {
                    Source js = forDocument(doc);
                    if (js != null) {
                        js.resetState(false, false);
                    }
                }
            }
        }

//        public void propertyChange(final PropertyChangeEvent evt) {
//            String propName = evt.getPropertyName();
//            if ("completion-active".equals(propName)) {
//                Source js = null;
//                final Document doc = lastEditor.getDocument();
//                if (doc != null) {
//                    js = forDocument(doc);
//                }
//                if (js != null) {
//                    Object rawValue = evt.getNewValue();
//                    assert rawValue instanceof Boolean;
//                    if (rawValue instanceof Boolean) {
//                        final boolean value = (Boolean)rawValue;
//                        if (value) {
//                            assert this.request == null;
//                            this.request = currentRequest.getTaskToCancel(false);
//                            if (this.request != null) {
//                                this.request.task.cancel();
//                            }
//                            js.k24 = true;
//                        }
//                        else {                    
//                            Request _request = this.request;
//                            this.request = null;                            
//                            js.k24 = false;
//                            js.resetTask.schedule(js.reparseDelay);
//                            currentRequest.cancelCompleted(_request);
//                        }
//                    }
//                }
//            }
//        }
    }

    private class FileChangeListenerImpl extends FileChangeAdapter {

        public @Override void fileChanged(final FileEvent fe) {
            Source.this.resetState(true, false);
        }

        public @Override void fileRenamed(FileRenameEvent fe) {
            Source.this.resetState(true, false);
        }
    }

    private static CompilationInfo createCurrentInfo (final Source js, final FileObject fo, final Object/*FilterListener*/ filterListener, final ParserTaskImpl javac) throws IOException {
        CompilationInfo info = new CompilationInfo (js, fo, javac);

        //TimesCollector.getDefault().reportReference(fo, CompilationInfo.class.toString(), "[M] CompilationInfo", info);     //NOI18N
        return info;
    }

    private static void handleAddRequest (final Request nr) {
        assert nr != null;
        requests.add (nr);
        Source.Request request = currentRequest.getTaskToCancel(nr.priority);
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
            this.t = new Thread (r,"GSF Source Worker Thread");     //NOI18N
            return this.t;
        }

        public boolean isDispatchThread (Thread t) {
            assert t != null;
            return this.t == t;
        }
    }

    private static class JavaSourceAccessorImpl extends SourceAccessor {
        private StackTraceElement[] javacLockedStackTrace;

        protected @Override void runSpecialTaskImpl (CancellableTask<CompilationInfo> task, Priority priority) {
            handleAddRequest(new Request (task, null, null, priority, false));
        }

        @Override
        public ParserTaskImpl createParserTask(Language language, ClasspathInfo cpInfo) {
            boolean backgroundCompilation = true; // Is this called from anywhere else?
            return Source.createParserTask(language, null, cpInfo, backgroundCompilation);
        }

        @Override
        public ParserTaskImpl/*JavacTaskImpl*/ getParserTask (final CompilationInfo compilationInfo) {
            assert compilationInfo != null;
            return compilationInfo.getParserTask();
        }
        
        @Override
        public CompilationInfo getCurrentCompilationInfo (final Source js, final Phase phase) throws IOException {
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
        public void revalidate(Source js) {
            js.revalidate();
        }

        @Override
        public boolean isDispatchThread () {
            return factory.isDispatchThread(Thread.currentThread());
         }

        @Override
        public void lockParser() {            
            javacLock.lock();
            try {
                this.javacLockedStackTrace = Thread.currentThread().getStackTrace();
            } catch (RuntimeException e) {
                //Not important, thrown by logging code
            }
        }
        
        @Override
        public void unlockParser() {
            try {
                this.javacLockedStackTrace = null;
            } finally {
                javacLock.unlock();
            }
        }
        
        @Override
        public boolean isParserLocked() {
            return javacLock.isLocked();
        }
    }

    private static class CurrentRequestReference {


        private Source.Request reference;
        private Source.Request canceledReference;
        private long cancelTime;
        private boolean canceled;

        public CurrentRequestReference () {
        }

        public boolean setCurrentTask (Source.Request reference) throws InterruptedException {
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

        public Source.Request getTaskToCancel (final Priority priority) {
            Source.Request request = null;
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

        public Source.Request getTaskToCancel (final Source js) {
            Source.Request request = null;
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

        public Source.Request getTaskToCancel (final CancellableTask task) {
            Source.Request request = null;
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

        public Source.Request getTaskToCancel () {
            Source.Request request = null;
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

        public void cancelCompleted (final Source.Request request) {
            if (request != null) {
                synchronized (this) {
                    assert request == this.canceledReference;
                    this.canceledReference = null;
                    this.notify();
                }
            }
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
                    final StyledDocument doc = DataLoadersBridge.getDefault().getDocument(fo);
                    if (doc instanceof AbstractDocument) {
                        Object result = method.invoke(doc);
                        if (result == currentThread) {
                            return true;
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
        String src = null;
        try {
            src = info.getText();
        } catch (IllegalStateException ise) {
            Document doc = info.getDocument();
            if (doc != null) {
                try {
                    src = doc.getText(0, doc.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (src == null) {
            src = "";
        }
        FileObject file = info.getFileObject();
        String origName = "unknown";
        String fileName = origName;
        if (file != null) {
            fileName = FileUtil.getFileDisplayName(file);
            origName = file.getNameExt();
        }
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
        String language = "ruby";
        if (info.getLanguage() != null) {
            language = info.getLanguage().getDisplayName();
        }
        if (dumpSucceeded) {
            Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against " + language + " and attach dump file '"  // NOI18N
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
