/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import com.sun.tools.javac.api.DuplicateClassChecker;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.util.Abort;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position.LineMapImpl;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.Processor;
import javax.swing.event.ChangeEvent;
import  javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.JavadocEnv;
import org.netbeans.modules.java.source.PostFlowAnalysis;
import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.indexing.APTUtils;
import org.netbeans.modules.java.source.indexing.FQN2Files;
import org.netbeans.lib.nbjavac.services.NBAttr;
import org.netbeans.lib.nbjavac.services.NBClassReader;
import org.netbeans.lib.nbjavac.services.NBEnter;
import org.netbeans.lib.nbjavac.services.NBJavadocEnter;
import org.netbeans.lib.nbjavac.services.NBJavadocMemberEnter;
import org.netbeans.lib.nbjavac.services.NBMemberEnter;
import org.netbeans.lib.nbjavac.services.NBParserFactory;
import org.netbeans.lib.nbjavac.services.NBClassWriter;
import org.netbeans.lib.nbjavac.services.NBJavacTrees;
import org.netbeans.lib.nbjavac.services.NBMessager;
import org.netbeans.lib.nbjavac.services.NBResolve;
import org.netbeans.lib.nbjavac.services.NBTreeMaker;
import org.netbeans.lib.nbjavac.services.PartialReparser;
import org.netbeans.modules.java.source.tasklist.CompilerSettings;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.WeakListeners;

/**
 * Provides Parsing API parser built atop Javac (using JSR 199).
 * @author Tomas Zezula
 */
//@NotThreadSafe
public class JavacParser extends Parser {
    //Timer logger
    private static final Logger TIME_LOGGER = Logger.getLogger("TIMER");        //NOI18N
    //Debug logger
    private static final Logger LOGGER = Logger.getLogger(JavacParser.class.getName());
    //Java Mime Type
    public static final String MIME_TYPE = "text/x-java";
    //JavaFileObjectProvider used by the JavacParser - may be overriden by unit test
    static JavaFileObjectProvider jfoProvider = new DefaultJavaFileObjectProvider ();
    //No output writer like /dev/null
    private static final PrintWriter DEV_NULL = new PrintWriter(new NullWriter(), false);
    //Max number of dump files
    private static final int MAX_DUMPS = Integer.getInteger("org.netbeans.modules.java.source.parsing.JavacParser.maxDumps", 255);  //NOI18N
    //Command line switch disabling partial reparse
    private static final boolean DISABLE_PARTIAL_REPARSE = Boolean.getBoolean("org.netbeans.modules.java.source.parsing.JavacParser.no_reparse");   //NOI18N
    private static final String LOMBOK_DETECTED = "lombokDetected";

    /**
     * Helper map mapping the {@link Phase} to message for performance logger
     */
    private static final Map<Phase, String> phase2Message = new EnumMap<Phase,String> (Phase.class);

    static {
        phase2Message.put (Phase.PARSED,"Parsed");                              //NOI18N
        phase2Message.put (Phase.ELEMENTS_RESOLVED,"Signatures Attributed");    //NOI18N
        phase2Message.put (Phase.RESOLVED, "Attributed");                       //NOI18N
    }

    //Listener support
    private final ChangeSupport listeners = new ChangeSupport(this);
    //Cancelling of parser
    private final AtomicBoolean parserCanceled = new AtomicBoolean();
    //Cancelling of index
    private final AtomicBoolean indexCanceled = new AtomicBoolean();
    
    //When true the parser is a private copy not used by the parsing API, see JavaSourceAccessor.createCompilationController
    private final boolean privateParser;
    //File processed by this javac
    private FileObject file;
    //Root owning the file
    private FileObject root;
    //ClassPaths used by the parser
    private ClasspathInfo cpInfo;
    //Count of files the parser was created for
    private final int sourceCount;
    //Incremental parsing support
    private final boolean supportsReparse;
    //Incremental parsing support
    private final List<Pair<DocPositionRegion,MethodTree>> positions =
            Collections.synchronizedList(new LinkedList<Pair<DocPositionRegion,MethodTree>>());
    //Incremental parsing support
    private final AtomicReference<Pair<DocPositionRegion,MethodTree>> changedMethod =
            new AtomicReference<Pair<DocPositionRegion, MethodTree>>();
    //Incremental parsing support
    private final DocListener listener;
    //J2ME preprocessor support
    private final FilterListener filterListener;
    //ClasspathInfo Listener
    private final ChangeListener cpInfoListener;
    //Cached javac impl
    private CompilationInfoImpl ciImpl;
    //State of the parser, used only for single source parser, otherwise don't care.
    private boolean initialized;
    //Parser is invalidated, new parser impl need to be created, but keeps current classpath info.
    private boolean invalid;
    //Last used snapshot
    private Snapshot cachedSnapShot;
    //Lamport clock of parse calls
    private long parseId;
    //Weak Change listener on ClasspathInfo, created by init
    private ChangeListener weakCpListener;
    //Current source for parse optmalization of task with no Source (identity)
    private Reference<JavaSource> currentSource;

    JavacParser (final Collection<Snapshot> snapshots, boolean privateParser) {
        this.privateParser = privateParser;
        this.sourceCount = snapshots.size();
        final boolean singleJavaFile = this.sourceCount == 1 && MIME_TYPE.equals(snapshots.iterator().next().getSource().getMimeType());
        this.supportsReparse = singleJavaFile && !DISABLE_PARTIAL_REPARSE;
        EditorCookie.Observable ec = null;
        JavaFileFilterImplementation filter = null;
        if (singleJavaFile) {
            final Source source = snapshots.iterator().next().getSource();
            FileObject fo = source.getFileObject();
            if (fo != null) {
                //fileless Source -- ie. debugger watch CC etc
                filter = JavaFileFilterQuery.getFilter(fo);
                try {
                    final DataObject dobj = DataObject.find(fo);
                    ec = dobj.getCookie(EditorCookie.Observable.class);
                    if (ec == null) {
                        LOGGER.log(Level.FINE,
                            String.format("File: %s has no EditorCookie.Observable", //NOI18N
                            FileUtil.getFileDisplayName (fo)));
                    }
                } catch (DataObjectNotFoundException e) {
                    LOGGER.log(Level.FINE,"Invalid DataObject",e);
                }
            }
        }
        this.filterListener = filter != null ? new FilterListener (filter) : null;
        this.listener = ec != null ? new DocListener(ec) : null;
        this.cpInfoListener = new ClasspathInfoListener (
            listeners,
            new Runnable() {
                @Override
                public void run() {
                    if (sourceCount == 0) {
                        invalidate(true);
                    }
                }
            });
    }

    private void init (final Snapshot snapshot, final Task task, final boolean singleSource) {
        final boolean explicitCpInfo = (task instanceof ClasspathInfoProvider) && ((ClasspathInfoProvider)task).getClasspathInfo() != null;
        if (!initialized) {
            final Source source = snapshot.getSource();
            final FileObject sourceFile = source.getFileObject();
            assert sourceFile != null;
            this.file = sourceFile;
            final ClasspathInfo oldInfo = this.cpInfo;
            if (explicitCpInfo) {
                cpInfo = ((ClasspathInfoProvider)task).getClasspathInfo();
            }
            else {
                cpInfo = ClasspathInfo.create(sourceFile);
            }
            final ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
            assert cp != null;
            this.root = cp.findOwnerRoot(sourceFile);
            if (singleSource) {
                if (oldInfo != null && weakCpListener != null) {
                    oldInfo.removeChangeListener(weakCpListener);
                    this.weakCpListener = null;
                }
                if (!explicitCpInfo) {      //Don't listen on artificial classpahs
                    this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                    cpInfo.addChangeListener (this.weakCpListener);
                }
                initialized = true;
            }
        } else if (singleSource && !explicitCpInfo) {     //tzezula: MultiSource should ever be explicitCpInfo, but JavaSource.create(CpInfo, List<Fo>) allows null!
            //Recheck ClasspathInfo if still valid
            assert this.file != null;
            assert cpInfo != null;
            final ClassPath scp = ClassPath.getClassPath(this.file, ClassPath.SOURCE);
            if (scp != cpInfo.getClassPath(PathKind.SOURCE)) {
                //Revalidate
                final Project owner = FileOwnerQuery.getOwner(this.file);
                LOGGER.log(Level.WARNING, "ClassPath identity changed for {0}, class path owner: {1} original sourcePath: {2} new sourcePath: {3}", //NOI18N
                        new Object[]{
                            this.file,
                            owner == null ? "null" : (FileUtil.getFileDisplayName(owner.getProjectDirectory())+" ("+owner.getClass()+")"), cpInfo.getClassPath(PathKind.SOURCE),    //NOI18N
                            scp
                });       
                if (this.weakCpListener != null) {
                    cpInfo.removeChangeListener(weakCpListener);
                }
                cpInfo = ClasspathInfo.create(this.file);
                final ClassPath cp = cpInfo.getClassPath(PathKind.SOURCE);
                assert cp != null;
                this.root = cp.findOwnerRoot(this.file);
                this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                cpInfo.addChangeListener (this.weakCpListener);
                JavaSourceAccessor.getINSTANCE().invalidateCachedClasspathInfo(this.file);
            }
        }
    }

    private void init(final Task task) {
        if (!initialized) {
            ClasspathInfo _tmpInfo = null;
            if (task instanceof ClasspathInfoProvider &&
                (_tmpInfo = ((ClasspathInfoProvider)task).getClasspathInfo()) != null) {
                if (cpInfo != null && weakCpListener != null) {
                    cpInfo.removeChangeListener(weakCpListener);
                    this.weakCpListener = null;
                }
                cpInfo = _tmpInfo;
                this.weakCpListener = WeakListeners.change(cpInfoListener, cpInfo);
                cpInfo.addChangeListener (this.weakCpListener);                
            } else {
                throw new IllegalArgumentException("No classpath provided by task: " + task);
            }
            initialized = true;
        }
    }

    private void invalidate (final boolean reinit) {
        this.invalid = true;
        if (reinit) {
            this.initialized = false;
        }
    }

    //@GuardedBy (org.netbeans.modules.parsing.impl.TaskProcessor.parserLock)
    @Override
    public void parse(final Snapshot snapshot, final Task task, SourceModificationEvent event) throws ParseException {
        try {            
            parseImpl(snapshot, task);
        } catch (FileObjects.InvalidFileException ife) {
            //pass - already invalidated in parseImpl
        } catch (IOException ioe) {
            throw new ParseException ("JavacParser failure", ioe); //NOI18N
        }
    }
    
    
    private boolean shouldParse(@NonNull Task task) {                
        if (!(task instanceof MimeTask)) {
            currentSource = null;
            return true;
        }
        final JavaSource newSource = ((MimeTask)task).getJavaSource();
        if (invalid) {
            currentSource = new WeakReference<JavaSource>(newSource);
            return true;
        }
        final JavaSource oldSource = currentSource == null ?
                null :
                currentSource.get();
        if (oldSource == null) {
            currentSource = new WeakReference<JavaSource>(newSource);
            return true;
        }
        if (newSource.equals(oldSource)) {            
            return false;
        } else {
            currentSource = new WeakReference<JavaSource>(newSource);
            return true;
        }
    }
    
    private void parseImpl(
            final Snapshot snapshot,
            final Task task) throws IOException {
        assert task != null;
        assert privateParser || Utilities.holdsParserLock();
        parseId++;
        parserCanceled.set(false);
        indexCanceled.set(false);
        cachedSnapShot = snapshot;
        LOGGER.log(Level.FINE, "parse: task: {0}\n{1}", new Object[]{   //NOI18N
            task.toString(),
            snapshot == null ? "null" : snapshot.getText()});      //NOI18N
        final CompilationInfoImpl oldInfo = ciImpl;
        boolean success = false;
        try {
            switch (this.sourceCount) {
                case 0:
                    if (shouldParse(task)) {
                        init(task);
                        ciImpl = new CompilationInfoImpl(cpInfo);
                    }
                    break;
                case 1:
                    init (snapshot, task, true);
                    boolean needsFullReparse = true;
                    if (supportsReparse) {
                        final Pair<DocPositionRegion,MethodTree> _changedMethod = changedMethod.getAndSet(null);
                        if (_changedMethod != null && ciImpl != null) {
                            LOGGER.log(Level.FINE, "\t:trying partial reparse:\n{0}", _changedMethod.first().getText());                           //NOI18N
                            needsFullReparse = !reparseMethod(ciImpl, snapshot, _changedMethod.second(), _changedMethod.first().getText());
                            if (!needsFullReparse) {
                                ciImpl.setChangedMethod(_changedMethod);
                            }
                        }
                    }
                    if (needsFullReparse) {
                        positions.clear();
                        ciImpl = createCurrentInfo (this, file, root, snapshot, null, null);
                        LOGGER.fine("\t:created new javac");                                    //NOI18N
                    }
                    break;
                default:
                    init (snapshot, task, false);
                    ciImpl = createCurrentInfo(this, file, root, snapshot,
                        ciImpl == null ? null : ciImpl.getJavacTask(),
                        ciImpl == null ? null : ciImpl.getDiagnosticListener());
            }
            success = true;
        } finally {
            invalid = !success;
            if (oldInfo != ciImpl && oldInfo != null) {
                oldInfo.dispose();
            }
        }
    }

    //@GuardedBy (org.netbeans.modules.parsing.impl.TaskProcessor.parserLock)
    @Override
    public JavacParserResult getResult (final Task task) throws ParseException {        
        assert privateParser || Utilities.holdsParserLock();
        if (ciImpl == null && !invalid) {
            throw new IllegalStateException("No CompilationInfoImpl in valid parser");      //NOI18N
        }
        LOGGER.log (Level.FINE, "getResult: task:{0}", task.toString());                     //NOI18N

        final boolean isJavaParserResultTask = task instanceof JavaParserResultTask;
        final boolean isParserResultTask = task instanceof ParserResultTask;
        final boolean isUserTask = task instanceof UserTask;
        final boolean isClasspathInfoProvider = task instanceof ClasspathInfoProvider;

        //Assumes that caller is synchronized by the Parsing API lock
        if (invalid || isClasspathInfoProvider) {
            if (invalid) {
                LOGGER.fine ("Invalid, reparse");    //NOI18N
            }
            if (isClasspathInfoProvider) {
                final ClasspathInfo providedInfo = ((ClasspathInfoProvider)task).getClasspathInfo();
                if (providedInfo != null && !providedInfo.equals(cpInfo)) {
                    if (sourceCount != 0) {
                        LOGGER.log (Level.FINE, "Task {0} has changed ClasspathInfo form: {1} to:{2}", new Object[]{task, cpInfo, providedInfo}); //NOI18N
                    }
                    invalidate(true); //Reset initialized, world has changed.
                }
            }            
            if (invalid) {
                assert cachedSnapShot != null || sourceCount == 0;
                try {                    
                    parseImpl(cachedSnapShot, task);
                } catch (FileObjects.InvalidFileException ife) {
                    //Deleted file
                    LOGGER.warning(ife.getMessage());
                    return null;
                } catch (IOException ioe) {
                    throw new ParseException ("JavacParser failure", ioe); //NOI18N
                }
            }
        }
        JavacParserResult result = null;
        if (isParserResultTask) {
            Phase requiredPhase;
            if (isJavaParserResultTask) {
                requiredPhase = ((JavaParserResultTask)task).getPhase();
            }
            else {
                requiredPhase = JavaSource.Phase.RESOLVED;
                LOGGER.log(Level.WARNING, "ParserResultTask: {0} doesn''t provide phase, assuming RESOLVED", task);                   //NOI18N
            }
            Phase reachedPhase;
            final DefaultCancelService cancelService = DefaultCancelService.instance(ciImpl.getJavacTask().getContext());
            if (cancelService != null) {
                cancelService.mayCancel.set(true);
            }
            try {
                reachedPhase = moveToPhase(requiredPhase, ciImpl, true);
            } catch (IOException ioe) {
                throw new ParseException ("JavacParser failure", ioe);      //NOI18N
            } finally {
                if (cancelService != null) {
                    cancelService.mayCancel.set(false);
                }
            }
            if (reachedPhase.compareTo(requiredPhase)>=0) {
                ClassIndexImpl.cancel.set(indexCanceled);
                result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationInfo(ciImpl));
            }
        }
        else if (isUserTask) {
            result = new JavacParserResult(JavaSourceAccessor.getINSTANCE().createCompilationController(ciImpl));
        }
        else {
            LOGGER.log(Level.WARNING, "Ignoring unknown task: {0}", task);                   //NOI18N
        }
        //Todo: shared = false should replace this
        //for now it creates a new parser and passes it outside the infrastructure
        //used by debugger private api
        if (task instanceof NewComilerTask) {
            NewComilerTask nct = (NewComilerTask)task;
            if (nct.getCompilationController() == null || nct.getTimeStamp() != parseId) {
                try {
                    nct.setCompilationController(
                        JavaSourceAccessor.getINSTANCE().createCompilationController(new CompilationInfoImpl(this, file, root, null, null, cachedSnapShot, true)),
                        parseId);
                } catch (IOException ioe) {
                    throw new ParseException ("Javac Failure", ioe);
                }
            }
        }
        return result;
    }

    @Override
    public void cancel (final @NonNull CancelReason reason, final @NonNull SourceModificationEvent event) {
        indexCanceled.set(true);
        if (reason == CancelReason.SOURCE_MODIFICATION_EVENT && event.sourceChanged()) {
            parserCanceled.set(true);
        }
    }

    public void resultFinished (boolean isCancelable) {
        if (isCancelable) {
            ClassIndexImpl.cancel.remove();
            indexCanceled.set(false);
        }
    }


    @Override
    public void addChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.addChangeListener(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        assert changeListener != null;
        this.listeners.removeChangeListener(changeListener);
    }


    /**
     * Returns {@link ClasspathInfo} used by this javac
     * @return the ClasspathInfo
     */
    ClasspathInfo getClasspathInfo () {
        return this.cpInfo;
    }

    /**
     * Moves the Javac into the required {@link JavaSource#Phase}
     * Not synchronized, has to be called under Parsing API lock.
     * @param the required {@link JavaSource#Phase}
     * @parma currentInfo - the javac
     * @param cancellable when true the method checks cancels
     * @return the reached phase
     * @throws IOException when the javac throws an exception
     */
    Phase moveToPhase (final Phase phase, final CompilationInfoImpl currentInfo,
            final boolean cancellable) throws IOException {
        JavaSource.Phase parserError = currentInfo.parserCrashed;
        assert parserError != null;
        Phase currentPhase = currentInfo.getPhase();
        try {
            if (currentPhase.compareTo(Phase.PARSED)<0 && phase.compareTo(Phase.PARSED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && parserCanceled.get()) {
                    //Keep the currentPhase unchanged, it may happen that an userActionTask
                    //runnig after the phace completion task may still use it.
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                // XXX - this might be with wrong encoding
                Iterable<? extends CompilationUnitTree> trees = currentInfo.getJavacTask().parse(new JavaFileObject[] {currentInfo.jfo});
                if (trees == null) {
                    LOGGER.log( Level.INFO, "Did not parse anything for: {0}", currentInfo.jfo.toUri()); //NOI18N
                    return Phase.MODIFIED;
                }
                Iterator<? extends CompilationUnitTree> it = trees.iterator();
                if (!it.hasNext()) {
                    LOGGER.log( Level.INFO, "Did not parse anything for: {0}", currentInfo.jfo.toUri()); //NOI18N
                    return Phase.MODIFIED;
                }
                CompilationUnitTree unit = it.next();
                currentInfo.setCompilationUnit(unit);
                assert !it.hasNext();
                final Document doc = listener == null ? null : listener.document;
                if (doc != null && supportsReparse) {
                    FindMethodRegionsVisitor v = new FindMethodRegionsVisitor(doc,Trees.instance(currentInfo.getJavacTask()).getSourcePositions(),this.parserCanceled);
                    v.visit(unit, null);
                    synchronized (positions) {
                        positions.clear();
                        positions.addAll(v.getResult());
                    }
                }
                currentPhase = Phase.PARSED;
                long end = System.currentTimeMillis();
                FileObject currentFile = currentInfo.getFileObject();
                TIME_LOGGER.log(Level.FINE, "Compilation Unit",
                    new Object[] {currentFile, unit});

                logTime (currentFile,currentPhase,(end-start));
            }
            if (currentPhase == Phase.PARSED && phase.compareTo(Phase.ELEMENTS_RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && parserCanceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis();
                currentInfo.getJavacTask().enter();
                currentPhase = Phase.ELEMENTS_RESOLVED;
                long end = System.currentTimeMillis();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
           }
           if (currentPhase == Phase.ELEMENTS_RESOLVED && phase.compareTo(Phase.RESOLVED)>=0 && phase.compareTo(parserError)<=0) {
                if (cancellable && parserCanceled.get()) {
                    return Phase.MODIFIED;
                }
                long start = System.currentTimeMillis ();
                JavacTaskImpl jti = currentInfo.getJavacTask();
                PostFlowAnalysis.analyze(jti.analyze(), jti.getContext());
                currentPhase = Phase.RESOLVED;
                long end = System.currentTimeMillis ();
                logTime(currentInfo.getFileObject(),currentPhase,(end-start));
            }
            if (currentPhase == Phase.RESOLVED && phase.compareTo(Phase.UP_TO_DATE)>=0) {
                currentPhase = Phase.UP_TO_DATE;
            }
        } catch (CouplingAbort a) {
            TreeLoader.dumpCouplingAbort(a, null);
            return currentPhase;
        } catch (CancelAbort ca) {
            currentPhase = Phase.MODIFIED;
            invalidate(false);
        } catch (Abort abort) {
            parserError = currentPhase;
        } catch (IOException ex) {
            currentInfo.parserCrashed = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (RuntimeException ex) {
            parserError = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;
        } catch (Error ex) {
            parserError = currentPhase;
            dumpSource(currentInfo, ex);
            throw ex;        
        } finally {
            currentInfo.setPhase(currentPhase);
            currentInfo.parserCrashed = parserError;
        }
        return currentPhase;
    }

    private static CompilationInfoImpl createCurrentInfo (final JavacParser parser,
            final FileObject file,
            final FileObject root,
            final Snapshot snapshot,
            final JavacTaskImpl javac,
            final DiagnosticListener<JavaFileObject> diagnosticListener) throws IOException {
        CompilationInfoImpl info = new CompilationInfoImpl(parser, file, root, javac, diagnosticListener, snapshot, false);
        if (file != null) {
            Logger.getLogger("TIMER").log(Level.FINE, "CompilationInfo",    //NOI18N
                    new Object[] {file, info});
        }
        return info;
    }

    static JavacTaskImpl createJavacTask(
            final FileObject file,
            final FileObject root,
            final ClasspathInfo cpInfo,
            final JavacParser parser,
            final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            final ClassNamesForFileOraculum oraculum,
            final boolean detached) {
        SourceLevelQuery.Result sourceLevel = null;
        if (file != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Created new JavacTask for: {0}", FileUtil.getFileDisplayName(file));
            }
            sourceLevel = SourceLevelQuery.getSourceLevel2(file);
        }
        FQN2Files dcc = null;
        if (root != null) {
            try {
                dcc = FQN2Files.forRoot(root.toURL());
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        final JavacTaskImpl javacTask = createJavacTask(
                cpInfo,
                diagnosticListener,
                sourceLevel != null ? sourceLevel.getSourceLevel() : null,
                sourceLevel != null ? sourceLevel.getProfile() : null,
                false,
                oraculum,
                dcc,
                parser == null ? null : new DefaultCancelService(parser),
                APTUtils.get(root));
        Context context = javacTask.getContext();
        TreeLoader.preRegister(context, cpInfo, detached);
        return javacTask;
    }

    public static JavacTaskImpl createJavacTask (
            @NonNull final ClasspathInfo cpInfo,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            @NullAllowed final String sourceLevel,
            @NullAllowed final SourceLevelQuery.Profile sourceProfile,
            @NullAllowed final ClassNamesForFileOraculum cnih,
            @NullAllowed final DuplicateClassChecker dcc,
            @NullAllowed final CancelService cancelService,
            @NullAllowed final APTUtils aptUtils) {
        return createJavacTask(cpInfo, diagnosticListener, sourceLevel, sourceProfile, true, cnih, dcc, cancelService, aptUtils);
    }

    private static JavacTaskImpl createJavacTask(
            @NonNull final ClasspathInfo cpInfo,
            @NullAllowed final DiagnosticListener<? super JavaFileObject> diagnosticListener,
            @NullAllowed final String sourceLevel,
            @NullAllowed SourceLevelQuery.Profile sourceProfile,
            final boolean backgroundCompilation,
            @NullAllowed final ClassNamesForFileOraculum cnih,
            @NullAllowed final DuplicateClassChecker dcc,
            @NullAllowed final CancelService cancelService,
            @NullAllowed final APTUtils aptUtils) {
        final List<String> options = new ArrayList<String>();
        String lintOptions = CompilerSettings.getCommandLine(cpInfo);
        com.sun.tools.javac.code.Source validatedSourceLevel = validateSourceLevel(sourceLevel, cpInfo);
        if (lintOptions.length() > 0) {
            options.addAll(Arrays.asList(lintOptions.split(" ")));
        }
        if (!backgroundCompilation) {
            options.add("-Xjcov"); //NOI18N, Make the compiler store end positions
            options.add("-XDallowStringFolding=false"); //NOI18N
            options.add("-XDkeepComments=true"); //NOI18N
            assert options.add("-XDdev") || true; //NOI18N
        } else {
            options.add("-XDbackgroundCompilation");    //NOI18N
            options.add("-XDcompilePolicy=byfile");     //NOI18N
            options.add("-XD-Xprefer=source");     //NOI18N
            options.add("-target");                     //NOI18N
            options.add(validatedSourceLevel.requiredTarget().name);
        }
        options.add("-XDide");   // NOI18N, javac runs inside the IDE
        options.add("-XDsave-parameter-names");   // NOI18N, javac runs inside the IDE
        options.add("-XDsuppressAbortOnBadClassFile");   // NOI18N, when a class file cannot be read, produce an error type instead of failing with an exception
        options.add("-XDshouldStopPolicy=GENERATE");   // NOI18N, parsing should not stop in phase where an error is found
        options.add("-g:source"); // NOI18N, Make the compiler to maintian source file info
        options.add("-g:lines"); // NOI18N, Make the compiler to maintain line table
        options.add("-g:vars");  // NOI18N, Make the compiler to maintain local variables table
        options.add("-source");  // NOI18N
        options.add(validatedSourceLevel.name);
        if (sourceProfile != null &&
            sourceProfile != SourceLevelQuery.Profile.DEFAULT) {
            options.add("-profile");    //NOI18N, Limit JRE to required compact profile
            options.add(sourceProfile.getName());
        }
        options.add("-XDdiags=-source");  // NOI18N
        options.add("-XDdiagsFormat=%L%m|%L%m|%L%m");  // NOI18N
        options.add("-XDbreakDocCommentParsingOnError=false");  // NOI18N
        boolean aptEnabled = aptUtils != null && aptUtils.aptEnabledOnScan() && (backgroundCompilation || aptUtils.aptEnabledInEditor())
                && !ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, PathKind.SOURCE).entries().isEmpty();
        Collection<? extends Processor> processors = null;
        if (aptEnabled) {
            processors = aptUtils.resolveProcessors(backgroundCompilation);
            if (processors.isEmpty()) {
                aptEnabled = false;
            } else {
                for (Processor p : processors) {
                    if ("lombok.core.AnnotationProcessor".equals(p.getClass().getName())) {
                        options.add("-XD" + LOMBOK_DETECTED);
                        break;
                    }
                }
            }
        }
        if (aptEnabled) {
            for (Map.Entry<? extends String, ? extends String> entry : aptUtils.processorOptions().entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append("-A").append(entry.getKey()); //NOI18N
                if (entry.getValue() != null) {
                    sb.append('=').append(entry.getValue()); //NOI18N
                }
                options.add(sb.toString());
            }
        } else {
            options.add("-proc:none"); // NOI18N, Disable annotation processors
        }

        Context context = new Context();
        //need to preregister the Messages here, because the getTask below requires Log instance:
        NBMessager.preRegister(context, null, DEV_NULL, DEV_NULL, DEV_NULL);
        JavacTaskImpl task = (JavacTaskImpl)JavacTool.create().getTask(null, 
                ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo),
                diagnosticListener, options, null, Collections.<JavaFileObject>emptySet(),
                context);
        if (aptEnabled) {
            task.setProcessors(processors);
        }
        NBClassReader.preRegister(context, !backgroundCompilation);
        if (cnih != null) {
            context.put(ClassNamesForFileOraculum.class, cnih);
        }
        if (dcc != null) {
            context.put(DuplicateClassChecker.class, dcc);
        }                    
        if (cancelService != null) {
            DefaultCancelService.preRegister(context, cancelService);
        }
        NBAttr.preRegister(context);
        NBClassWriter.preRegister(context);
        NBParserFactory.preRegister(context);
        NBTreeMaker.preRegister(context);
        NBJavacTrees.preRegister(context);
        if (!backgroundCompilation) {
            JavacFlowListener.preRegister(context, task);
            NBJavadocEnter.preRegister(context);
            NBJavadocMemberEnter.preRegister(context);
            JavadocEnv.preRegister(context, cpInfo);
            NBResolve.preRegister(context);
        } else {
            NBEnter.preRegister(context);
            NBMemberEnter.preRegister(context);
        }
        TIME_LOGGER.log(Level.FINE, "JavaC", context);
        return task;
    }

    public static boolean DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
    static @NonNull com.sun.tools.javac.code.Source validateSourceLevel(
            @NullAllowed String sourceLevel,
            @NonNull final ClasspathInfo cpInfo) {
        ClassPath bootClassPath = cpInfo.getClassPath(PathKind.BOOT);
        ClassPath classPath = null;
        ClassPath srcClassPath = cpInfo.getClassPath(PathKind.SOURCE);
        com.sun.tools.javac.code.Source[] sources = com.sun.tools.javac.code.Source.values();
        Level warnLevel;
        if (sourceLevel == null) {
            //automatically use highest source level that is satisfied by the given boot classpath:
            sourceLevel = sources[sources.length-1].name;
            warnLevel = Level.FINE;
        } else {
            warnLevel = Level.WARNING;
        }
        for (com.sun.tools.javac.code.Source source : sources) {
            if (source.name.equals(sourceLevel)) {
                if (DISABLE_SOURCE_LEVEL_DOWNGRADE) return source;
                if (source.compareTo(com.sun.tools.javac.code.Source.JDK1_4) >= 0) {
                    if (bootClassPath != null && bootClassPath.findResource("java/lang/AssertionError.class") == null) { //NOI18N
                        if (bootClassPath.findResource("java/lang/Object.class") == null) { // NOI18N
                            // empty bootClassPath also check classpath
                            classPath = cpInfo.getClassPath(PathKind.COMPILE);
                        }
                        if (!hasResource("java/lang/AssertionError", ClassPath.EMPTY, classPath, srcClassPath)) { // NOI18N
                            LOGGER.log(warnLevel,
                                       "Even though the source level of {0} is set to: {1}, java.lang.AssertionError cannot be found on the bootclasspath: {2}\n" +
                                       "Changing source level to 1.3",
                                       new Object[]{cpInfo.getClassPath(PathKind.SOURCE), sourceLevel, bootClassPath}); //NOI18N
                            return com.sun.tools.javac.code.Source.JDK1_3;
                        }
                    }
                }
                if (source.compareTo(com.sun.tools.javac.code.Source.JDK1_5) >= 0 &&
                    !hasResource("java/lang/StringBuilder", bootClassPath, classPath, srcClassPath)) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.StringBuilder cannot be found on the bootclasspath: {2}\n" +
                               "Changing source level to 1.4",
                               new Object[]{cpInfo.getClassPath(PathKind.SOURCE), sourceLevel, bootClassPath}); //NOI18N
                    return com.sun.tools.javac.code.Source.JDK1_4;
                }
                if (source.compareTo(com.sun.tools.javac.code.Source.JDK1_7) >= 0 &&
                    !hasResource("java/lang/AutoCloseable", bootClassPath, classPath, srcClassPath)) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.lang.AutoCloseable cannot be found on the bootclasspath: {2}\n" +   //NOI18N
                               "Changing source level to 1.6",  //NOI18N
                               new Object[]{cpInfo.getClassPath(PathKind.SOURCE), sourceLevel, bootClassPath}); //NOI18N
                    return com.sun.tools.javac.code.Source.JDK1_6;
                }
                if (source.compareTo(com.sun.tools.javac.code.Source.JDK1_8) >= 0 &&
                    !hasResource("java/util/stream/Streams", bootClassPath, classPath, srcClassPath)) { //NOI18N
                    LOGGER.log(warnLevel,
                               "Even though the source level of {0} is set to: {1}, java.util.stream.Streams cannot be found on the bootclasspath: {2}\n" +   //NOI18N
                               "Changing source level to 1.7",  //NOI18N
                               new Object[]{cpInfo.getClassPath(PathKind.SOURCE), sourceLevel, bootClassPath}); //NOI18N
                    return com.sun.tools.javac.code.Source.JDK1_7;                                                                        
                }
                return source;
            }
        }
        SpecificationVersion specVer = new SpecificationVersion (sourceLevel);
        SpecificationVersion JAVA_12 = new SpecificationVersion ("1.2");   //NOI18N
        if (JAVA_12.compareTo(specVer)>0) {
            //Some SourceLevelQueries return 1.1 source level which is invalid, use 1.2
            return sources[0];
        }
        else {
            return sources[sources.length-1];
        }
    }

    private static boolean hasResource(
        @NonNull String resourceBase,
        @NullAllowed ClassPath boot,
        @NullAllowed ClassPath compile,
        @NullAllowed ClassPath source) {
        final String resourceClass = String.format("%s.class", resourceBase);
        final String resourceJava = String.format("%s.java", resourceBase);
        if (boot != null && boot.findResource(resourceClass) == null) { //NOI18N
            if (source != null && source.findResource(resourceJava) == null) {   //NOI18N
                if (compile == null || compile.findResource(resourceClass) == null) { // NOI18N
                    return false;
                }
            }
        }
        return true;
    }
    
    private static void logTime (FileObject source, Phase phase, long time) {
        assert source != null && phase != null;
        String message = phase2Message.get(phase);
        assert message != null;
        TIME_LOGGER.log(Level.FINE, message, new Object[] {source, time});
    }

    /**
     * Dumps the source code to the file. Used for parser debugging. Only a limited number
     * of dump files is used. If the last file exists, this method doesn't dump anything.
     *
     * @param  info  CompilationInfo for which the error occurred.
     * @param  exc  exception to write to the end of dump file
     */
    private static void dumpSource(CompilationInfoImpl info, Throwable exc) {
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
            try {
                Throwable t = Exceptions.attachMessage(exc, "An error occurred during parsing of \'" + fileName + "\'. Please report a bug against java/source and attach dump file '"  // NOI18N
                        + f.getAbsolutePath() + "'."); // NOI18N
                Exceptions.printStackTrace(t);
            } catch (RuntimeException re) {
                //There was already a call exc.initCause(null) which causes RE when a another initCause() is called.
                //Print at least the original exception
                Exceptions.printStackTrace(exc);
            }
        } else {
            LOGGER.log(Level.WARNING,
                    "Dump could not be written. Either dump file could not " + // NOI18N
                    "be created or all dump files were already used. Please " + // NOI18N
                    "check that you have write permission to '" + dumpDir + "' and " + // NOI18N
                    "clean all *.dump files in that directory."); // NOI18N
        }
    }

    private static boolean reparseMethod (final CompilationInfoImpl ci,
            final Snapshot snapshot,
            final MethodTree orig,
            final String newBody) throws IOException {
        assert ci != null;
        final FileObject fo = ci.getFileObject();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Reparse method in: {0}", fo);          //NOI18N
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
            if (Options.instance(task.getContext()).isSet(LOMBOK_DETECTED)) return false;
            PartialReparser pr = PartialReparser.instance(task.getContext());
            final JavacTrees jt = JavacTrees.instance(task);
            final int origStartPos = (int) jt.getSourcePositions().getStartPosition(cu, orig.getBody());
            final int origEndPos = (int) jt.getSourcePositions().getEndPosition(cu, orig.getBody());
            if (origStartPos > origEndPos) {
                LOGGER.log(Level.WARNING, "Javac returned startpos: {0} > endpos: {1}", new Object[]{origStartPos, origEndPos});  //NOI18N
                return false;
            }
            final FindAnonymousVisitor fav = new FindAnonymousVisitor();
            fav.scan(orig.getBody(), null);
            if (fav.hasLocalClass) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Skeep reparse method (old local classes): {0}", fo);   //NOI18N
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
                    DiagnosticListener dl = ci.getDiagnosticListener();
                    assert dl instanceof CompilationInfoImpl.DiagnosticListenerImpl;
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).startPartialReparse(origStartPos, origEndPos);
                    long start = System.currentTimeMillis();
                    Map<JCTree,LazyDocCommentTable.Entry> docComments = new HashMap<JCTree, LazyDocCommentTable.Entry>();
                    block = pr.reparseMethodBody(cu, orig, newBody, firstInner, docComments);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "Reparsed method in: {0}", fo);     //NOI18N
                    }
                    assert block != null;
                    fav.reset();
                    fav.scan(block, null);
                    final int newNoInner = fav.noInner;
                    if (fav.hasLocalClass || noInner != newNoInner) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Skeep reparse method (new local classes): {0}", fo);   //NOI18N
                        }
                        return false;
                    }
                    ((LazyDocCommentTable) ((JCTree.JCCompilationUnit)cu).docComments).table.keySet().removeAll(fav.docOwners);
                    ((LazyDocCommentTable) ((JCTree.JCCompilationUnit)cu).docComments).table.putAll(docComments);
                    long end = System.currentTimeMillis();
                    if (fo != null) {
                        logTime (fo,Phase.PARSED,(end-start));
                    }
                    final int newEndPos = (int) jt.getSourcePositions().getEndPosition(cu, block);
                    final int delta = newEndPos - origEndPos;
                    final EndPosTable endPos = ((JCCompilationUnit)cu).endPositions;
                    final TranslatePositionsVisitor tpv = new TranslatePositionsVisitor(orig, endPos, delta);
                    tpv.scan(cu, null);
                    ((JCMethodDecl)orig).body = block;
                    if (Phase.RESOLVED.compareTo(currentPhase)<=0) {
                        start = System.currentTimeMillis();
                        pr.reattrMethodBody(orig, block);
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Resolved method in: {0}", fo);     //NOI18N
                        }
                        if (!((CompilationInfoImpl.DiagnosticListenerImpl)dl).hasPartialReparseErrors()) {
                            final JavacFlowListener fl = JavacFlowListener.instance(ctx);
                            if (fl != null && fl.hasFlowCompleted(fo)) {
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    final List<? extends Diagnostic> diag = ci.getDiagnostics();
                                    if (!diag.isEmpty()) {
                                        LOGGER.log(Level.FINER, "Reflow with errors: {0} {1}", new Object[]{fo, diag});     //NOI18N
                                    }
                                }
                                TreePath tp = TreePath.getPath(cu, orig);       //todo: store treepath in changed method => improve speed
                                Tree t = tp.getParentPath().getLeaf();
                                pr.reflowMethodBody(cu, (ClassTree) t, orig);
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    LOGGER.log(Level.FINER, "Reflowed method in: {0}", fo); //NOI18N
                                }
                            }
                        }
                        end = System.currentTimeMillis();
                        if (fo != null) {
                            logTime (fo, Phase.ELEMENTS_RESOLVED,0L);
                            logTime (fo,Phase.RESOLVED,(end-start));
                        }
                    }

                    //fix CompilationUnitTree.getLineMap:
                    long startM = System.currentTimeMillis();
                    char[] chars = snapshot.getText().toString().toCharArray();
                    ((LineMapImpl) cu.getLineMap()).build(chars, chars.length);
                    LOGGER.log(Level.FINER, "Rebuilding LineMap took: {0}", System.currentTimeMillis() - startM);

                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).endPartialReparse (delta);
                } finally {
                    l.endPartialReparse();
                    l.useSource(prevLogged);
                }
                ci.update(snapshot);
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

    //Helper classes
    private static class DefaultCancelService extends CancelService {

        //May be the parser canceled inside javac?
        final AtomicBoolean mayCancel = new AtomicBoolean();
        private final JavacParser parser;

        private DefaultCancelService(final JavacParser parser) {
            this.parser = parser;
        }

        public static void preRegister(Context context, CancelService cancelServiceToRegister) {
            context.put(cancelServiceKey, cancelServiceToRegister);
        }

        public static DefaultCancelService instance(final Context ctx) {
            assert ctx != null;
            final CancelService cancelService = CancelService.instance(ctx);
            return (cancelService instanceof DefaultCancelService) ? (DefaultCancelService) cancelService : null;
        }

        @Override
        public boolean isCanceled() {
            return mayCancel.get() && parser.parserCanceled.get();
        }
    }

    /**
     * Lexer listener used to detect partial reparse
     * todo: should be replaced by parsing API events when available
     */
    private class DocListener implements PropertyChangeListener, TokenHierarchyListener {

        private EditorCookie.Observable ec;
        private TokenHierarchyListener lexListener;
        private volatile Document document;

        @SuppressWarnings("LeakingThisInConstructor")
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
            EditorRegistry.addPropertyChangeListener(new EditorRegistryWeakListener(this));
        }

        @Override
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
                }
                else {
                    //reset document
                    this.document = doc;
                }
            } else if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(evt.getPropertyName())) {
                final Document doc = document;
                final JTextComponent focused = EditorRegistry.focusedComponent();
                final Document focusedDoc = focused == null ? null : focused.getDocument();
                if (doc != null && doc == focusedDoc) {
                    positions.clear();
                }
            }
        }

        @Override
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            Pair<DocPositionRegion,MethodTree> changedMethod = null;
            if (evt.type() == TokenHierarchyEventType.MODIFICATION) {
                if (supportsReparse) {
                    int start = evt.affectedStartOffset();
                    int end = evt.affectedEndOffset();
                    synchronized (positions) {
                        for (Pair<DocPositionRegion,MethodTree> pe : positions) {
                            PositionRegion p = pe.first();
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
                        JavacParser.this.changedMethod.set(changedMethod);
                    }
                }
            } else if (evt.type() == TokenHierarchyEventType.ACTIVITY) {
                positions.clear();
                JavacParser.this.changedMethod.set(null);
            }
        }
    }

    private static final class EditorRegistryWeakListener extends WeakReference<PropertyChangeListener> implements PropertyChangeListener, Runnable {
        private Reference<JTextComponent> last;

        private EditorRegistryWeakListener(final @NonNull PropertyChangeListener delegate) {
            super(delegate,org.openide.util.Utilities.activeReferenceQueue());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final PropertyChangeListener delegate = get();
            final JTextComponent lastCandidate = EditorRegistry.focusedComponent();
            if (delegate != null && lastCandidate != null && lastCandidate != getLast()) {
                setLast(lastCandidate);
                delegate.propertyChange(evt);
            }
        }

        @Override
        public void run() {
            EditorRegistry.removePropertyChangeListener(this);
        }

        /**
         * @return the last
         */
        private JTextComponent getLast() {
            return last == null ? null : last.get();
        }

        /**
         * @param last the last to set
         */
        private void setLast(JTextComponent last) {
            this.last = new WeakReference<JTextComponent>(last);
        }
    }

    /**
     * For unit tests only
     * Used by JavaSourceTest.testIncrementalReparse
     * @param changedMethod
     */
    public synchronized void setChangedMethod (final Pair<DocPositionRegion,MethodTree> changedMethod) {
        assert changedMethod != null;
        this.changedMethod.set(changedMethod);
    }

    /**
     * Filter listener to listen on j2me preprocessor
     */
    private final class FilterListener implements ChangeListener {

        @SuppressWarnings("LeakingThisInConstructor")
        public FilterListener (final JavaFileFilterImplementation filter) {
            filter.addChangeListener(WeakListeners.change(this, filter));
        }

        @Override
        public void stateChanged(ChangeEvent event) {
            listeners.fireChange();
        }
    }
}
