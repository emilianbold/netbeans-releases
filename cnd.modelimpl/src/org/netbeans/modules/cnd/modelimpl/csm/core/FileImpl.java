/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import org.netbeans.modules.cnd.antlr.Parser;
import org.netbeans.modules.cnd.antlr.RecognitionException;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.FortranParserEx;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.fsm.core.DataRenderer;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc.ChangedSegment;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.DefaultCache;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * CsmFile implementations
 * @author Vladimir Kvashin
 */
public final class FileImpl implements CsmFile, MutableDeclarationsContainer,
        Disposable, Persistent, SelfPersistent, CsmIdentifiable {

    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | TraceFlags.DEBUG;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    // the next flag(s) make sense only in the casew reportParse is true
    private static final boolean logState = Boolean.getBoolean("parser.log.state");
//    private static final boolean logEmptyTokenStream = Boolean.getBoolean("parser.log.empty");
    private static final boolean emptyAstStatictics = Boolean.getBoolean("parser.empty.ast.statistics");

    public static enum FileType {
        UNDEFINED_FILE,
        SOURCE_FILE,
        SOURCE_C_FILE,
        SOURCE_CPP_FILE,
        SOURCE_FORTRAN_FILE,
        HEADER_FILE,
    };
    public static final int UNDEFINED_FILE = 0;
    public static final int SOURCE_FILE = 1;
    public static final int SOURCE_C_FILE = 2;
    public static final int SOURCE_CPP_FILE = 3;
    public static final int HEADER_FILE = 4;
    private static volatile AtomicLong parseCount = new AtomicLong(1);

    public static void incParseCount() {
        parseCount.incrementAndGet();
    }

    public static int getParseCount() {
        return (int) (parseCount.get() & 0xFFFFFFFFL);
    }
    private FileBuffer fileBuffer;
    /**
     * DUMMY_STATE and DUMMY_HANDLERS are used when we need to ensure that the file will be arsed.
     * Typucally this happens when user edited buffer (after a delay), but also by clents request, etc. -
     * i.e. when we do not know the state to put in the parsing queue
     *
     * The issue here is that adding this file with default states (from container) does not suite,
     * since we don't know what is being done with the queue, file container and this file itself,
     * so there are a lot of sync issues on this way.
     *
     * Previously, null value was used instead; using null is much less clear an visible
     *
     * So, putting DUMMY_STATE into the queue
     *
     * 1) does not harm states that are in queue or will be put there (see ParserQueue code)
     *
     * 2) in the case DUMMY_STATE is popped from queue by the ParserThread,
     * it invokes ensureParsed(DUMMY_HANDLERS), which parses the file with all valid states from container.
     * This (2) might hapen only when there are NO other states in queue
     */
    public static final Collection<APTPreprocHandler> DUMMY_HANDLERS = new EmptyCollection<APTPreprocHandler>();
    public static final APTPreprocHandler.State DUMMY_STATE = new APTPreprocHandler.State() {

        @Override
        public boolean isCleaned() {
            return true;
        }

        @Override
        public boolean isCompileContext() {
            return false;
        }

        @Override
        public boolean isValid() {
            return false;
        }
    };
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private Object projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    /**
     * It's a map since we need to eliminate duplications
     */
    private final TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> declarations;
    private WeakReference<Map<CsmDeclaration.Kind,SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>> sortedDeclarations;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();
    private Set<CsmUID<CsmInclude>> includes = createIncludes();
    private Set<CsmUID<CsmInclude>> brokenIncludes = new LinkedHashSet<CsmUID<CsmInclude>>(0);
    private final ReadWriteLock includesLock = new ReentrantReadWriteLock();
    private final Set<ErrorDirectiveImpl> errors = createErrors();
    private final ReadWriteLock errorsLock = new ReentrantReadWriteLock();
    private final TreeMap<NameSortedKey, CsmUID<CsmMacro>> macros;
    private final ReadWriteLock macrosLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    private int errorCount = 0;
    private int lastParseTime;

    public static enum State {

        /** The file has never been parsed */
        INITIAL,
        /** The file has been completely parsed */
        PARSED,
        /** The file is parsed in one preprocessor state,
        but should be parsed in one or several other states */
        PARTIAL,
        /** The file is modified and needs to be reparsed */
        MODIFIED,
    }

    private static enum ParsingState {
        /** The file is not in parsing phase */
        NOT_BEING_PARSED,
        /** The file is modified during parsing */
        MODIFIED_WHILE_BEING_PARSED,
        /** The file is now being parsed */
        BEING_PARSED
    }
    private volatile State state;
    private volatile ParsingState parsingState;
    private FileType fileType = FileType.UNDEFINED_FILE;
    private static final class StateLock {}
    private final Object stateLock = new StateLock();
    private final List<CsmUID<FunctionImplEx>> fakeRegistrationPairs = new CopyOnWriteArrayList<CsmUID<FunctionImplEx>>();
    private FileSnapshot fileSnapshot;
    private final Object snapShotLock = new Object();

    private long lastParsed = Long.MIN_VALUE;
    /** Cache the hash code */
    private int hash = 0; // Default to 0
    /**
     * Stores the UIDs of the static functions declarations (not definitions)
     * This is necessary for finding definitions/declarations
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    private final Collection<CsmUID<CsmFunction>> staticFunctionDeclarationUIDs;
    private final Collection<CsmUID<CsmVariable>> staticVariableUIDs;
    private final ReadWriteLock staticLock = new ReentrantReadWriteLock();
    private Reference<List<CsmReference>> lastMacroUsages = null;
    private ChangeListener fileBufferChangeListener = new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
            FileImpl.this.markReparseNeeded(false);
        }
    };

    /** For test purposes only */
    public interface Hook {

        void parsingFinished(CsmFile file, APTPreprocHandler preprocHandler);
    }
    private static Hook hook = null;

    public FileImpl(FileBuffer fileBuffer, ProjectBase project, FileType fileType, NativeFileItem nativeFileItem) {
        state = State.INITIAL;
        parsingState = ParsingState.NOT_BEING_PARSED;
        declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
        macros = createMacros();
        staticFunctionDeclarationUIDs = new ArrayList<CsmUID<CsmFunction>>(0);
        staticVariableUIDs = new ArrayList<CsmUID<CsmVariable>>(0);
        setBuffer(fileBuffer);
        this.projectUID = UIDCsmConverter.projectToUID(project);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(FileImpl.this) + " of prj@"  + System.identityHashCode(project) + ":UID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        this.projectRef = new WeakReference<ProjectBase>(project); // Suppress Warnings
        this.fileType = fileType;
        if (nativeFileItem != null) {
            project.putNativeFileItem(getUID(), nativeFileItem);
        }
        Notificator.instance().registerNewFile(FileImpl.this);
    }

    /** For test purposes only */
    public static void setHook(Hook aHook) {
        hook = aHook;
    }

    public final NativeFileItem getNativeFileItem() {
        return getProjectImpl(true).getNativeFileItem(getUID());
    }

    private ProjectBase _getProject(boolean assertNotNull) {
        Object o = projectRef;
        if (o instanceof ProjectBase) {
            return (ProjectBase) o;
        } else if (o instanceof Reference) {
            ProjectBase prj = (ProjectBase)((Reference) o).get();
            if (prj != null) {
                return prj;
            }
        }
        projectLock.readLock().lock();
        try {
            ProjectBase prj = null;
            if (projectRef instanceof ProjectBase) {
                prj = (ProjectBase) projectRef;
            } else if (projectRef instanceof Reference) {
                prj = (ProjectBase)((Reference) projectRef).get();
            }
            if (prj == null) {
                prj = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                if (assertNotNull) {
                    assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
                }
                projectRef = new WeakReference<ProjectBase>(prj);
            }
            return prj;
        } finally {
            projectLock.readLock().unlock();
        }
    }

    @Override
    public final boolean isSourceFile() {
        return isSourceFileType(fileType);
    }

    public static boolean isSourceFileType(FileType fileType) {
        switch (fileType) {
            case SOURCE_CPP_FILE:
            case SOURCE_C_FILE:
            case SOURCE_FILE:
            case SOURCE_FORTRAN_FILE:
                return true;
        }
        return false;
    }

    public boolean isCppFile() {
        return fileType == FileType.SOURCE_CPP_FILE;
    }

    /*package local*/ void setSourceFile() {
        if (!(fileType == FileType.SOURCE_C_FILE || fileType == FileType.SOURCE_CPP_FILE || fileType == FileType.SOURCE_FORTRAN_FILE)) {
            fileType = FileType.SOURCE_FILE;
        }
    }

    @Override
    public boolean isHeaderFile() {
        return fileType == FileType.HEADER_FILE;
    }

    /*package local*/ void setHeaderFile() {
        if (fileType == FileType.UNDEFINED_FILE) {
            fileType = FileType.HEADER_FILE;
        }
    }

    // TODO: consider using macro map and __cplusplus here instead of just checking file name
    public APTLanguageFilter getLanguageFilter(APTPreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : ProjectBase.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getLanguageFilter(null);
        } else {
            String lang;
            if (fileType == FileType.SOURCE_CPP_FILE) {
                lang = APTLanguageSupport.GNU_CPP;
            } else if (fileType == FileType.SOURCE_C_FILE) {
                lang = APTLanguageSupport.GNU_C;
            } else if (fileType == FileType.SOURCE_FORTRAN_FILE) {
                lang = APTLanguageSupport.FORTRAN;
            } else {
                lang = APTLanguageSupport.GNU_CPP;
                String name = getName().toString();
                if (name.length() > 2 && name.endsWith(".c")) { // NOI18N
                    lang = APTLanguageSupport.GNU_C;
                }
            }
            return APTLanguageSupport.getInstance().getFilter(lang);
        }
    }

    //@Deprecated
    private APTPreprocHandler getPreprocHandler() {
        return getProjectImpl(true) == null ? null : getProjectImpl(true).getPreprocHandler(fileBuffer.getFile());
    }

    public APTPreprocHandler getPreprocHandler(int offset) {
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(offset, offset);
        return getPreprocHandler(bestStatePair);
    }

    private APTPreprocHandler getPreprocHandler(PreprocessorStatePair statePair) {
        return getProjectImpl(true) == null ? null : getProjectImpl(true).getPreprocHandler(fileBuffer.getFile(), statePair);
    }

    public Collection<APTPreprocHandler> getPreprocHandlers() {
        return getProjectImpl(true) == null ? Collections.<APTPreprocHandler>emptyList() : getProjectImpl(true).getPreprocHandlers(this.getFile());
    }

    public Collection<PreprocessorStatePair> getPreprocStatePairs() {
      ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return Collections.<PreprocessorStatePair>emptyList();
        }
        return projectImpl.getPreprocessorStatePairs(this.getFile());
    }

    private PreprocessorStatePair getContextPreprocStatePair(int startContext, int endContext) {
        ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return null;
        }
        if (startContext == 0) {
            // zero is in all => no the best state
            return null;
        }
        Collection<PreprocessorStatePair> preprocStatePairs = projectImpl.getPreprocessorStatePairs(this.getFile());
        // select the best based on context offsets
        for (PreprocessorStatePair statePair : preprocStatePairs) {
            if (statePair.pcState.isInActiveBlock(startContext, endContext)) {
                return statePair;
            }
        }
        return null;
    }

//    private Collection<APTPreprocHandler.State> getPreprocStates() {
//        ProjectBase project = getProjectImpl(true);
//        return (project == null) ? Collections.<APTPreprocHandler.State>emptyList() : project.getPreprocStates(this);
//    }
    public void setBuffer(FileBuffer fileBuffer) {
        synchronized (changeStateLock) {
            if (this.fileBuffer != null) {
                this.fileBuffer.removeChangeListener(fileBufferChangeListener);
            }
            this.fileBuffer = fileBuffer;
//            if (traceFile(getAbsolutePath())) {
//                new Exception("setBuffer: " + fileBuffer).printStackTrace(System.err);
//            }
            // we do not need listener for non-document based buffer
            // all state invalidations are made through:
            //  - external file change event
            //  - or "end file edit" action in deep reparsing utils
            if (fileBuffer != null && !fileBuffer.isFileBased()) {
                if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                    if (reportParse || logState || TraceFlags.DEBUG) {
                        System.err.printf("#setBuffer changing to MODIFIED %s is %s with current state %s %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                    }
                    state = State.MODIFIED;
                    postMarkedAsModified();
                }
                this.fileBuffer.addChangeListener(fileBufferChangeListener);
            }
        }
    }

    /** must be called only changeStateLock */
    private void postMarkedAsModified() {
        tsRef.clear();
        if (parsingState == ParsingState.BEING_PARSED) {
            parsingState = ParsingState.MODIFIED_WHILE_BEING_PARSED;
        }
    }

    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }

    // ONLY FOR PARSER THREAD USAGE
    // Parser Queue ensures that the same file can be parsed at the same time
    // only by one thread.
    /*package*/ void ensureParsed(Collection<APTPreprocHandler> handlers) {
        boolean wasDummy = false;
        if (handlers == DUMMY_HANDLERS) {
            wasDummy = true;
            handlers = getPreprocHandlers();
        }
        long time;
        synchronized (stateLock) {
            try {
                State curState;
                synchronized (changeStateLock) {
                    curState = state;
                    parsingState = ParsingState.BEING_PARSED;
                }
                if (reportParse || logState || TraceFlags.DEBUG) {
                    if (traceFile(getAbsolutePath())) {
                        System.err.printf("#ensureParsed %s is %s, has %d handlers, state %s %s dummy=%s\n", getAbsolutePath(), fileType, handlers.size(), curState, parsingState, wasDummy); // NOI18N
                        int i = 0;
                        for (APTPreprocHandler aPTPreprocHandler : handlers) {
                            logParse("EnsureParsed handler " + (i++), aPTPreprocHandler); // NOI18N
                        }
                    }
                }
                APTFile fullAPT = getFullAPT();
                if (fullAPT == null) {
                    // probably file was removed
                    return;
                }
                switch (curState) {
                    case PARSED: // even if it was parsed, but there was entry in queue with handler => need additional parse
                    case INITIAL:
                    case PARTIAL:
                        if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT && curState == State.PARSED) {
                            System.err.printf("additional parse with PARSED state " + parsingState + "for %s\n", getAbsolutePath()); // NOI18N
                        }
                        time = System.currentTimeMillis();
                        try {
                            for (APTPreprocHandler preprocHandler : handlers) {
                                _parse(preprocHandler, fullAPT);
                                if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                    break; // does not make sense parsing old data
                                }
                            }
                        } finally {
                            postParse();
                            synchronized (changeStateLock) {
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    state = State.PARSED;
                                }  // if not, someone marked it with new state
                            }
                            stateLock.notifyAll();
                            lastParseTime = (int)(System.currentTimeMillis() - time);
                            //System.err.println("Parse of "+getAbsolutePath()+" took "+lastParseTime+"ms");
                        }
                        if (TraceFlags.DUMP_PARSE_RESULTS) {
                            new CsmTracer().dumpModel(this);
                        }
                        break;
                    case MODIFIED:
                        boolean first = true;
                        time = System.currentTimeMillis();
                        try {
                            for (APTPreprocHandler preprocHandler : handlers) {
                                if (first) {
                                    _reparse(preprocHandler, fullAPT);
                                    first = false;
                                } else {
                                    _parse(preprocHandler, fullAPT);
                                }
                                if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                    break; // does not make sense parsing old data
                                }
                            }
                        } finally {
                            synchronized (changeStateLock) {
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    state = State.PARSED;
                                } // if not, someone marked it with new state
                            }
                            postParse();
                            stateLock.notifyAll();
                            lastParseTime = (int)(System.currentTimeMillis() - time);
                            //System.err.println("Parse of "+getAbsolutePath()+" took "+lastParseTime+"ms");
                        }
                        if (TraceFlags.DUMP_PARSE_RESULTS || TraceFlags.DUMP_REPARSE_RESULTS) {
                            new CsmTracer().dumpModel(this);
                        }
                        break;
                    default:
                        System.err.println("unexpected state in ensureParsed " + curState); // NOI18N
                }
            } finally {
                synchronized (changeStateLock) {
                    parsingState = ParsingState.NOT_BEING_PARSED;
                }
            }
        }
    }

    private void postParse() {
        // do not call fix fakes after file parsed
        // if something is not resolved, postpone till project parse finished
//        fixFakeRegistrations(false);
        if (isValid()) {   // FIXUP: use a special lock here
            RepositoryUtils.put(this);
        }
        if (isValid()) {	// FIXUP: use a special lock here
            getProjectImpl(true).getGraph().putFile(this);
        }
        if (isValid()) {   // FIXUP: use a special lock here
            Notificator.instance().registerChangedFile(this);
            Notificator.instance().flush();
        } else {
            // FIXUP: there should be a notificator per project instead!
            Notificator.instance().reset();
        }
    }

    /*package*/ void onProjectParseFinished(boolean prjLibsAlreadyParsed) {
        if (fixFakeRegistrations(true)) {
            if (isValid()) {   // FIXUP: use a special lock here
                RepositoryUtils.put(this);
            }
            if (isValid()) {   // FIXUP: use a special lock here
                Notificator.instance().registerChangedFile(this);
                Notificator.instance().flush();
                ProgressSupport.instance().fireFileParsingFinished(this);
            } else {
                // FIXUP: there should be a notificator per project instead!
                Notificator.instance().reset();
            }
        }
    }

    // returns parse/rearse time in milliseconds.
    int getLastParseTime(){
        return lastParseTime;
    }

    public boolean validate() {
        synchronized (changeStateLock) {
            if (state == State.PARSED) {
                long lastModified = getBuffer().lastModified();
                if (lastModified > lastParsed) {
                    if (TraceFlags.TRACE_VALIDATION) {
                        System.err.printf("VALIDATED %s\n\t lastModified=%d\n\t   lastParsed=%d\n", getAbsolutePath(), lastModified, lastParsed);
                    }
                    if (reportParse || logState || TraceFlags.DEBUG) {
                        System.err.printf("#validate changing to MODIFIED %s is %s with current state %s %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                    }
                    state = State.MODIFIED;
                    postMarkedAsModified();
                    return false;
                }
            }
            return true;
        }
    }
    private static final class ChangeStateLock {}
    private final Object changeStateLock = new ChangeStateLock();

    public final void markReparseNeeded(boolean invalidateCache) {
        synchronized (changeStateLock) {
            if (reportParse || logState || TraceFlags.DEBUG) {
                System.err.printf("#markReparseNeeded %s is %s with current state %s, %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
            }
            if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                state = State.MODIFIED;
                postMarkedAsModified();
            }
            if (invalidateCache) {
                APTDriver.getInstance().invalidateAPT(this.getBuffer());
                APTFileCacheManager.invalidate(this.getBuffer());
            }
        }
    }

    public final void markMoreParseNeeded() {
        synchronized (changeStateLock) {
            if (reportParse || logState || TraceFlags.DEBUG) {
                System.err.printf("#markMoreParseNeeded %s is %s with current state %s, %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
            }
            switch (state) {
                case PARSED:
                    state = State.PARTIAL;
                    break;
                case INITIAL:
                case MODIFIED:
                case PARTIAL:
                // nothing
            }
        }
    }

    public final int getErrorCount() {
        return errorCount;
    }

    private APTFile getFullAPT() {
        APTFile aptFull = null;
        ChangedSegment changedSegment = null;
        try {
            aptFull = APTDriver.getInstance().findAPT(this.getBuffer());
            if (getBuffer() instanceof FileBufferDoc) {
                changedSegment = ((FileBufferDoc) getBuffer()).getLastChangedSegment();
            }
        } catch (FileNotFoundException ex) {
            APTUtils.LOG.log(Level.WARNING, "FileImpl: file {0} not found, probably removed", new Object[]{getBuffer().getFile().getAbsolutePath()});// NOI18N
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
        return aptFull;
    }

    private void _reparse(APTPreprocHandler preprocHandler, APTFile aptFull) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------ reparsing " + fileBuffer.getFile().getName()); // NOI18N
        }
        synchronized(snapShotLock) {
            fileSnapshot = new FileSnapshot(this);
        }
        _clearIncludes();
        _clearMacros();
        _clearErrors();
        if (reportParse || logState || TraceFlags.DEBUG) {
            logParse("ReParsing", preprocHandler); //NOI18N
        }
        Parsing parsing = doParse(preprocHandler, aptFull);
        if (parsing != null) {
            if (isValid()) {
                disposeAll(false);
                parsing.stageTwo(this);
            }
        } else {
            //System.err.println("null ast for file " + getAbsolutePath());
        }
        fileSnapshot = null;
    }

    CsmFile getSnapshot(){
        synchronized(snapShotLock) {
            FileSnapshot res = fileSnapshot;
            if (res != null) {
                return res;
            }
            return new FileSnapshot(this);
        }
    }

    @Override
    public void dispose() {
        onDispose();
        Notificator.instance().registerRemovedFile(this);
        disposeAll(true);
    }

    public void onProjectClose() {
        onDispose();
    }

    private void onDispose() {
        RepositoryUtils.disposeUID(uid, this);
        projectLock.writeLock().lock();
        try {
            if (projectRef == null) {
                // restore container from it's UID
                this.projectRef = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
            }
        } finally {
            projectLock.writeLock().unlock();
        }
    }

    private void disposeAll(boolean clearNonDisposable) {
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        //for( Iterator iter = declarations.values().iterator(); iter.hasNext(); ) {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        try {
            declarationsLock.writeLock().lock();
            uids = new ArrayList<CsmUID<CsmOffsetableDeclaration>>(declarations.values());
            declarations.clear();
            //declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        try {
            staticLock.writeLock().lock();
            staticFunctionDeclarationUIDs.clear();
            staticVariableUIDs.clear();
        } finally {
            staticLock.writeLock().unlock();
        }
        clearFakeRegistrations();

        if (clearNonDisposable) {
            clearStateCache();
            _clearIncludes();
            _clearMacros();
            _clearErrors();
        }
        Collection<CsmOffsetableDeclaration> arr = UIDCsmConverter.UIDsToDeclarations(uids);
        Utils.disposeAll(arr);
        RepositoryUtils.remove(uids);
    }

    private void _clearMacros() {
        Collection<CsmUID<CsmMacro>> copy;
        try {
            macrosLock.writeLock().lock();
            copy = new ArrayList<CsmUID<CsmMacro>>(macros.values());
            macros.clear();
        } finally {
            macrosLock.writeLock().unlock();
        }
        RepositoryUtils.remove(copy);
    }

    private TreeMap<NameSortedKey, CsmUID<CsmMacro>> createMacros() {
        return new TreeMap<NameSortedKey, CsmUID<CsmMacro>>();
    }

    private void _clearIncludes() {
        try {
            includesLock.writeLock().lock();
            RepositoryUtils.remove(includes);
            brokenIncludes.clear();
            includes = createIncludes();
        } finally {
            includesLock.writeLock().unlock();
        }
    }

    private void _clearErrors() {
        try {
            errorsLock.writeLock().lock();
            errors.clear();
        } finally {
            errorsLock.writeLock().unlock();
        }
    }

    private Set<CsmUID<CsmInclude>> createIncludes() {
        return new TreeSet<CsmUID<CsmInclude>>(UID_START_OFFSET_COMPARATOR);
    }

    private Set<ErrorDirectiveImpl> createErrors() {
        return new TreeSet<ErrorDirectiveImpl>(START_OFFSET_COMPARATOR);
    }

    /** for debugging/tracing purposes only */
    public AST debugParse() {
        synchronized (stateLock) {
            return _parse(getPreprocHandler(), getFullAPT());
        }
    }


    private AST _parse(APTPreprocHandler preprocHandler, APTFile aptFull) {

        Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
        if (reportParse || logState || TraceFlags.DEBUG) {
            logParse("Parsing", preprocHandler); //NOI18N
        }
        Parsing parsing = doParse(preprocHandler, aptFull);
        if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
            sw.stopAndReport("Parsing of " + fileBuffer.getFile().getName() + " took \t"); // NOI18N
        }
        if (parsing != null) {
            Diagnostic.StopWatch sw2 = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
            if (isValid()) {   // FIXUP: use a special lock here
                parsing.stageTwo(this);
                if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
                    sw2.stopAndReport("Rendering of " + fileBuffer.getFile().getName() + " took \t"); // NOI18N
                }
            }
            Object ast = parsing.getAST();
            if(ast instanceof AST) {
                return (AST)ast;
            }
        }
        return null;
    }

    private void logParse(String title, APTPreprocHandler preprocHandler) {
        if (reportParse || logState || TraceFlags.DEBUG) {
            System.err.printf("# %s %s \n#\t(%s %s %s) \n#\t(Thread=%s)\n", //NOI18N
                    title, fileBuffer.getFile().getPath(),
                    TraceUtils.getPreprocStateString(preprocHandler.getState()),
                    TraceUtils.getMacroString(preprocHandler, TraceFlags.logMacros),
                    TraceUtils.getPreprocStartEntryString(preprocHandler.getState()),
                    Thread.currentThread().getName());
            if (logState) {
                System.err.printf("%s\n\n", preprocHandler.getState()); //NOI18N
            }
        }
    }

    // called under tokStreamLock
    private boolean createAndCacheFullTokenStream(int startContext, int endContext, /*in-out*/FileTokenStreamCache tsCache) {
        APTFile apt = getFullAPT();
        if (apt == null) {
            return false;
        }
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(startContext, endContext);
        APTPreprocHandler preprocHandler = getPreprocHandler(bestStatePair);
        if (preprocHandler == null) {
            return false;
        }
        APTPreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = ProjectBase.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while getting TS of file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
            return false;
        }
        APTLanguageFilter languageFilter = getLanguageFilter(ppState);
        FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(getAbsolutePath());
        // ask for concurrent entry if absent
        APTFileCacheEntry cacheEntry = getAPTCacheEntry(preprocHandler, Boolean.FALSE);
        APTParseFileWalker walker = new APTParseFileWalker(startProject, apt, this, preprocHandler, false, pcBuilder,cacheEntry);
        tsCache.addNewPair(pcBuilder, walker.getTokenStream(false), languageFilter);
        // remember walk info
        setAPTCacheEntry(preprocHandler, cacheEntry, false);
        return true;
    }
    private static final class TokenStreamLock {}
    private final Object tokStreamLock = new TokenStreamLock();
    private Reference<FileTokenStreamCache> tsRef = new SoftReference<FileTokenStreamCache>(null);
    /**
     *
     * @param startOffset
     * @param endOffset
     * @param firstTokenIDIfExpandMacros pass 0 if not interested in particular token type
     * @param filtered
     * @return
     */
    public final TokenStream getTokenStream(int startContextOffset, int endContextOffset, int/*CPPTokenTypes*/ firstTokenIDIfExpandMacros, boolean filtered) {
        boolean trace = false;
        FileTokenStreamCache cache = tsRef.get();
        TokenStream stream;
        if (cache == null) {
            stream = null;
        } else {
            stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
        }
        if (stream != null) {
            if (trace) {
                System.err.printf("found for %s %s stream [%d-%d]\n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
            }
        } else {
            // we need to build new full token stream
            synchronized (tokStreamLock) {
                cache = tsRef.get();
                if (cache == null) {
                    cache = new FileTokenStreamCache();
                    tsRef = new SoftReference<FileTokenStreamCache>(cache);
                } else {
                    // could be already created by parallel thread
                    stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
                }
                if (stream == null) {
                    if (trace) {
                        System.err.printf("creating for %s %s stream [%d-%d]\n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
                    }
                    if (createAndCacheFullTokenStream(startContextOffset, endContextOffset, cache)) {
                        stream = cache.getTokenStreamInActiveBlock(filtered, startContextOffset, endContextOffset, firstTokenIDIfExpandMacros);
                    }
                } else {
                    if (trace) {
                        System.err.printf("found for just cached %s %s stream [%d-%d]\n", getAbsolutePath(), (filtered ? "filtered" : ""), startContextOffset, endContextOffset); // NOI18N
                    }
                }
            }
        }
        return stream;
    }

    /** For test purposes only */
    public interface ErrorListener {

        void error(String text, int line, int column);
    }

    /** For test purposes only */
    public void getErrors(ErrorListener errorListener) {
        Collection<RecognitionException> parserErrors = new ArrayList<RecognitionException>();
        getErrors(parserErrors);
        for (RecognitionException e : parserErrors) {
            errorListener.error(e.getMessage(), e.getLine(), e.getColumn());
        }
    }

    private static class ParserBasedTokenBuffer implements ReadOnlyTokenBuffer {

        Parser parser;

        public ParserBasedTokenBuffer(Parser parser) {
            this.parser = parser;
        }

        @Override
        public int LA(int i) {
            return parser.LA(i);
        }

        @Override
        public Token LT(int i) {
            return parser.LT(i);
        }
    }

    public final APTFileCacheEntry getAPTCacheEntry(APTPreprocHandler preprocHandler, Boolean createExclusiveIfAbsent) {
        if (!TraceFlags.APT_FILE_CACHE_ENTRY) {
            return null;
        }
        APTFileCacheEntry out = APTFileCacheManager.getEntry(getAbsolutePath(), preprocHandler, createExclusiveIfAbsent);
        assert createExclusiveIfAbsent == null || out != null;
        return out;
    }

    public final void setAPTCacheEntry(APTPreprocHandler preprocHandler, APTFileCacheEntry entry, boolean cleanOthers) {
        if (TraceFlags.APT_FILE_CACHE_ENTRY) {
            APTFileCacheManager.setAPTCacheEntry(getAbsolutePath(), preprocHandler, entry, cleanOthers);
        }
    }

    public ReadOnlyTokenBuffer getErrors(final Collection<RecognitionException> result) {
        CPPParserEx.ErrorDelegate delegate = new CPPParserEx.ErrorDelegate() {

            @Override
            public void onError(RecognitionException e) {
                result.add(e);
            }
        };
        // FIXUP (up to the end of the function)
        // should be changed with setting appropriate flag and using common parsing mechanism
        // (Now doParse performs too many actions that should NOT be performed if parsing just for getting errors;
        // making this actions conditional will make doParse code spaghetty-like. That's why I use this fixup)
        // Another issue to be solved is threading and cancellation
        if (TraceFlags.TRACE_ERROR_PROVIDER) {
            System.err.printf("\n\n>>> Start parsing (getting errors) %s \n", getName());
        }
        long time = TraceFlags.TRACE_ERROR_PROVIDER ? System.currentTimeMillis() : 0;
//        APTPreprocHandler preprocHandler = getPreprocHandler();
//        APTPreprocHandler.State ppState = preprocHandler.getState();
//        ProjectBase startProject = ProjectBase.getStartProject(ppState);
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if (!TraceFlags.TRACE_ERROR_PROVIDER) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }
        try {
            // use cached TS
            TokenStream tokenStream = getTokenStream(0, Integer.MAX_VALUE, 0, true);
            if (tokenStream != null) {
                CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile().getName(), tokenStream, flags);
                parser.setErrorDelegate(delegate);
                parser.setLazyCompound(false);
                parser.translation_unit();
                return new ParserBasedTokenBuffer(parser);
            }
        } catch (Error ex) {
            System.err.println(ex.getClass().getName() + " at parsing file " + fileBuffer.getFile().getAbsolutePath()); // NOI18N
            throw ex;
        } finally {
            if (TraceFlags.TRACE_ERROR_PROVIDER) {
                System.err.printf("<<< Done parsing (getting errors) %s %d ms\n\n\n", getName(), System.currentTimeMillis() - time);
            }
        }
        return null;
    }

    private interface Parsing {
        void stageOne(TokenStream ts);
        void stageTwo(FileImpl file);

        Object getAST();
        int getErrorCount();
    }

    private static class CppParsing implements Parsing {

        private File file;
        private int flags;
        private CPPParserEx parser;

        public CppParsing(File file, int flags) {
            this.file = file;
            this.flags = flags;
        }

        @Override
        public void stageOne(TokenStream ts) {
            parser = CPPParserEx.getInstance(file.getName(), ts, flags);
            try {
                parser.translation_unit();
            } catch (Error ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
                throw ex;
            }
        }

        @Override
        public void stageTwo(FileImpl file) {
            AST ast = parser.getAST();
            if(ast != null) {
                new AstRenderer(file).render(ast);
                incParseCount();
            }
        }

        @Override
        public Object getAST() {
            return parser.getAST();
        }

        @Override
        public int getErrorCount() {
            return parser.getErrorCount();
        }
    }

    private static class FortranParsing implements Parsing {

        private File file;
        private FortranParserEx parser;
        private FortranParserEx.program_return ret;

        public FortranParsing(File file) {
            this.file = file;
        }

        @Override
        public void stageOne(TokenStream ts) {
//                FortranParserEx.MyTokenStream ts2 = new FortranParserEx.MyTokenStream(new org.netbeans.modules.cnd.antlr.TokenBuffer(filteredTokenStream));
//                while(ts2.LA(1) != -1) {
//                    System.out.println(ts2.LT(1).getText() + " " + ts2.LT(1).getType());
//                    ts2.consume();
//                }
//                System.out.println(ts2.LT(1).getText());
            parser = new FortranParserEx(ts);
            try {
                ret = parser.program();
//                CommonTree tree = (CommonTree) ret.getTree();
//                System.out.println(tree);
//                System.out.println(tree.getChildren());
            } catch (org.antlr.runtime.RecognitionException ex) {
                System.err.println(ex.getClass().getName() + " at parsing file " + file.getAbsolutePath()); // NOI18N
            } catch (Exception ex) {
                System.err.println("Fortran parser error at parsing file " + file.getAbsolutePath()); // NOI18N
            }
        }

        @Override
        public void stageTwo(FileImpl file) {
            new DataRenderer(file).render(parser.parsedObjects);
            incParseCount();
        }

        @Override
        public Object getAST() {
            return ret.getTree();
        }

        @Override
        public int getErrorCount() {
            return parser.getNumberOfSyntaxErrors();
        }
    }


    private Parsing doParse(APTPreprocHandler preprocHandler, APTFile aptFull) {

        if (reportErrors) {
            if (!ParserThreadManager.instance().isParserThread() && !ParserThreadManager.instance().isStandalone()) {
                String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
                Diagnostic.trace(text);
                new Throwable(text).printStackTrace(System.err);
            }
        }
        assert preprocHandler != null;
        if (preprocHandler == null) {
            return null;
        }

        ParseStatistics.getInstance().fileParsed(this, preprocHandler);

        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if (!reportErrors) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }

//        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
//            if (getAbsolutePath().toString().endsWith(".h")) { // NOI18N
//                try {
//                    Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
        Parsing parsing = null;
        if (aptFull != null) {
            // use full APT for generating token stream
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: parsing using full APT for " + getAbsolutePath());
            }
            // make real parse
            APTPreprocHandler.State ppState = preprocHandler.getState();
            ProjectBase startProject = ProjectBase.getStartProject(ppState);
            if (startProject == null) {
                System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                        "\n while parsing file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
                return null;
            }
            // We gather conditional state here as well, because sources are not included anywhere
            FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(getAbsolutePath());
            // ask for concurrent entry if absent
            APTFileCacheEntry aptCacheEntry = getAPTCacheEntry(preprocHandler, Boolean.FALSE);
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFull, this, preprocHandler, true, pcBuilder,aptCacheEntry);
            walker.addMacroAndIncludes(true);
            if (TraceFlags.DEBUG) {
                System.err.println("doParse " + getAbsolutePath() + " with " + ParserQueue.tracePreprocState(ppState));
            }

            TokenStream filteredTokenStream = walker.getFilteredTokenStream(getLanguageFilter(ppState));

            long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;

            if(fileType == FileType.SOURCE_FORTRAN_FILE) {
                //System.out.println("Prasing fortran file " + getName());
                parsing = new FortranParsing(fileBuffer.getFile());
            } else {
                //System.out.println("Prasing cpp file " + getName());
                parsing = new CppParsing(fileBuffer.getFile(), flags);
            }

            parsing.stageOne(filteredTokenStream);

            FilePreprocessorConditionState pcState = pcBuilder.build();
            if (false) {
                setAPTCacheEntry(preprocHandler, aptCacheEntry, false);
            }
            startProject.setParsedPCState(this, ppState, pcState);

            if (emptyAstStatictics) {
                time = System.currentTimeMillis() - time;
                final Object ast = parsing.getAST();
                if(ast instanceof AST) {
                    System.err.println("PARSED FILE " + getAbsolutePath() + (AstUtil.isEmpty((AST)ast, true) ? " EMPTY" : "") + ' ' + time + " ms");
                } else {
                    System.err.print("ast is not instance of AST");
                }
            }
            if (TraceFlags.DUMP_AST) {
                System.err.println("\n");
                System.err.print("AST: ");
                System.err.print(getAbsolutePath());
                System.err.print(' ');
                final Object ast = parsing.getAST();
                if(ast instanceof AST) {
                    AstUtil.toStream((AST)ast, System.err);
                } else {
                    System.err.print("ast is not instance of AST");
                }
                System.err.println("\n");

            }
            errorCount = parsing.getErrorCount();
            if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                parsing = null;
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: not save cache for file modified during parsing" + getAbsolutePath());
                }
            }
        }
        clearStateCache();
        lastParsed = Math.max(System.currentTimeMillis(), fileBuffer.lastModified());
        lastMacroUsages = null;
        if (TraceFlags.TRACE_VALIDATION) {
            System.err.printf("PARSED    %s \n\tlastModified=%d\n\t  lastParsed=%d  diff=%d\n",
                    getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified() - lastParsed);
        }
        Hook aHook = hook;
        if (aHook != null) {
            aHook.parsingFinished(this, preprocHandler);
        }
//        parseCount++;
        return parsing;
    }

    public List<CsmReference> getLastMacroUsages() {
        Reference<List<CsmReference>> ref = lastMacroUsages;
        return ref != null ? ref.get() : null;
    }

    public void setLastMacroUsages(List<CsmReference> res) {
        lastMacroUsages = new SoftReference<List<CsmReference>>(Collections.unmodifiableList(res));
    }

    public long getLastParsedTime() {
        return lastParsed;
    }

    public void addInclude(IncludeImpl includeImpl, boolean broken) {
        CsmUID<CsmInclude> inclUID = RepositoryUtils.put((CsmInclude)includeImpl);
        assert inclUID != null;
        try {
            includesLock.writeLock().lock();
            includes.add(inclUID);
            if (broken) {
                brokenIncludes.add(inclUID);
            } else {
                brokenIncludes.remove(inclUID);
            }
        } finally {
            includesLock.writeLock().unlock();
        }
    }
    public static final Comparator<CsmOffsetable> START_OFFSET_COMPARATOR = new Comparator<CsmOffsetable>() {

        @Override
        public int compare(CsmOffsetable o1, CsmOffsetable o2) {
            if (o1 == o2) {
                return 0;
            }
            int ofs1 = o1.getStartOffset();
            int ofs2 = o2.getStartOffset();
            if (ofs1 == ofs2) {
                return 0;
            } else {
                return (ofs1 - ofs2);
            }
        }
    };
    static final private Comparator<CsmUID> UID_START_OFFSET_COMPARATOR = new Comparator<CsmUID>() {

        @SuppressWarnings("unchecked")
        @Override
        public int compare(CsmUID o1, CsmUID o2) {
            if (o1 == o2) {
                return 0;
            }
            Comparable<CsmUID> i1 = (Comparable<CsmUID>) o1;
            assert i1 != null;
            return i1.compareTo(o2);
        }
    };

    @Override
    public String getText(int start, int end) {
        try {
            return fileBuffer.getText(start, end);
        } catch (IOException e) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    @Override
    public String getText() {
        try {
            return fileBuffer.getText();
        } catch (IOException e) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    @Override
    public CsmProject getProject() {
        return _getProject(false);
    }

    public CsmUID<CsmProject> getProjectUID() {
        return projectUID;
    }

    /** Just a convenient shortcut to eliminate casts */
    public ProjectBase getProjectImpl(boolean assertNotNull) {
        return _getProject(assertNotNull);
    }

    @Override
    public CharSequence getName() {
        return CharSequenceKey.create(fileBuffer.getFile().getName());
    }

    @Override
    public Collection<CsmInclude> getIncludes() {
        Collection<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(includes);
        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    @Override
    public Collection<CsmErrorDirective> getErrors() {
        Collection<CsmErrorDirective> out = new ArrayList<CsmErrorDirective>(0);
        try {
            errorsLock.readLock().lock();
            out.addAll(errors);
        } finally {
            errorsLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmInclude> getIncludes(CsmFilter filter) {
        Iterator<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(includes, filter);

        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmInclude> getBrokenIncludes() {
        Collection<CsmInclude> out;
        try {
            includesLock.readLock().lock();
            out = UIDCsmConverter.UIDsToIncludes(brokenIncludes);
        } finally {
            includesLock.readLock().unlock();
        }
        return out;
    }

    public boolean hasBrokenIncludes() {
        try {
            includesLock.readLock().lock();
            return !brokenIncludes.isEmpty();
        } finally {
            includesLock.readLock().unlock();
        }
    }

    public boolean hasDeclarations() {
        // due to unblocking size() - use it
        return declarations.size() != 0;
//        try {
//            declarationsLock.readLock().lock();
//            return !declarations.isEmpty();
//        } finally {
//            declarationsLock.readLock().unlock();
//        }
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        try {
            declarationsLock.readLock().lock();
            Collection<CsmUID<CsmOffsetableDeclaration>> uids = declarations.values();
            decls = UIDCsmConverter.UIDsToDeclarations(uids);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        Iterator<CsmOffsetableDeclaration> out;
        try {
            declarationsLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations.values(), filter);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    /**
     * Returns number of declarations.
     * Does not fixFakeRegistrations, so this size could be inaccurate
     *
     * @return number of declarations
     */
    public int getDeclarationsSize(){
//        try {
//            declarationsLock.readLock().lock();
        // NOTE: in the current implementation declarations is TreeMap based
        // no need to syn here
        return declarations.size();
//        } finally {
//            declarationsLock.readLock().unlock();
//        }
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findDeclarations(CsmDeclaration.Kind[] kinds, CharSequence prefix) {
        Collection<CsmUID<CsmOffsetableDeclaration>> out = null;
        try {
            declarationsLock.readLock().lock();
            Map<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>> map = null;
            if (sortedDeclarations != null) {
                map = sortedDeclarations.get();
            }
            if (map == null) {
                map = new EnumMap<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>(CsmDeclaration.Kind.class);
                for(CsmUID<CsmOffsetableDeclaration> anUid : declarations.values()){
                    CsmDeclaration.Kind kind = UIDUtilities.getKind(anUid);
                    SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                    if (val == null){
                        val = new TreeMap<NameKey, CsmUID<CsmOffsetableDeclaration>>();
                        map.put(kind, val);
                    }
                    val.put(new NameKey(anUid), anUid);
                }
                sortedDeclarations = new WeakReference<Map<CsmDeclaration.Kind, SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>>>>(map);
            }
            out = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
            for(CsmDeclaration.Kind kind : kinds) {
                 SortedMap<NameKey, CsmUID<CsmOffsetableDeclaration>> val = map.get(kind);
                 if (val != null) {
                     if (prefix == null) {
                         out.addAll(val.values());
                     } else {
                         NameKey fromKey = new NameKey(prefix, 0);
                         NameKey toKey = new NameKey(prefix, Integer.MAX_VALUE);
                         out.addAll(val.subMap(fromKey, toKey).values());
                     }
                 }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(int startOffset, int endOffset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(startOffset-1);
            OffsetSortedKey fromKey = new OffsetSortedKey(startOffset,""); // NOI18N
            OffsetSortedKey toKey = new OffsetSortedKey(endOffset,""); // NOI18N
            SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> map = declarations.subMap(fromKey, toKey);
            for(Map.Entry<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> entry : map.entrySet()){
                CsmUID<CsmOffsetableDeclaration> anUid = entry.getValue();
                int start = UIDUtilities.getStartOffset(anUid);
                int end = UIDUtilities.getEndOffset(anUid);
                if (start >= endOffset) {
                    break;
                }
                if(end >= startOffset && start < endOffset) {
                    res.add(anUid);
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(int offset) {
        List<CsmUID<CsmOffsetableDeclaration>> res;
        try {
            declarationsLock.readLock().lock();
            res = getDeclarationsByOffset(offset);
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.UIDsToDeclarations(res).iterator();
    }

    // call under read lock
    private List<CsmUID<CsmOffsetableDeclaration>> getDeclarationsByOffset(int offset){
        List<CsmUID<CsmOffsetableDeclaration>> res = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        OffsetSortedKey key = new OffsetSortedKey(offset+1,""); // NOI18N
        while(true) {
            SortedMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> head = declarations.headMap(key);
            if (head.isEmpty()) {
                break;
            }
            OffsetSortedKey last = head.lastKey();
            if (last == null) {
                break;
            }
            CsmUID<CsmOffsetableDeclaration> aUid = declarations.get(last);
            int from = UIDUtilities.getStartOffset(aUid);
            int to = UIDUtilities.getEndOffset(aUid);
            if (from <= offset && offset <= to) {
                res.add(0, aUid);
                key = last;
            } else {
                break;
            }
        }
        return res;
    }

    public void addMacro(CsmMacro macro) {
        CsmUID<CsmMacro> macroUID = RepositoryUtils.put(macro);
        NameSortedKey key = new NameSortedKey(macro);
        assert macroUID != null;
        try {
            macrosLock.writeLock().lock();
            macros.put(key, macroUID);
        } finally {
            macrosLock.writeLock().unlock();
        }
    }

    public void addError(ErrorDirectiveImpl error) {
        try {
            errorsLock.writeLock().lock();
            errors.add(error);
        } finally {
            errorsLock.writeLock().unlock();
        }
    }

    @Override
    public Collection<CsmMacro> getMacros() {
        Collection<CsmMacro> out;
        try {
            macrosLock.readLock().lock();
            out = UIDCsmConverter.UIDsToMacros(macros.values());
        } finally {
            macrosLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmMacro> getMacros(CsmFilter filter) {
        Iterator<CsmMacro> out;
        try {
            macrosLock.readLock().lock();
            out = UIDCsmConverter.UIDsToMacros(macros.values(), filter);
        } finally {
            macrosLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmUID<CsmMacro>> findMacroUids(CharSequence name) {
        Collection<CsmUID<CsmMacro>> uids = new ArrayList<CsmUID<CsmMacro>>(2);
        NameSortedKey from = NameSortedKey.getStartKey(name);
        NameSortedKey to = NameSortedKey.getEndKey(name);
        try {
            macrosLock.readLock().lock();
            for (Map.Entry<NameSortedKey, CsmUID<CsmMacro>> entry : macros.subMap(from, to).entrySet()) {
                uids.add(entry.getValue());
            }
        } finally {
            macrosLock.readLock().unlock();
        }
        return uids;
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int startOffset, int endOffset, CharSequence name) {
        OffsetSortedKey key = new OffsetSortedKey(startOffset, name);
        CsmUID<CsmOffsetableDeclaration> anUid = null;
        try {
            declarationsLock.readLock().lock();
            anUid = declarations.get(key);
            sortedDeclarations = null;
//            if (traceFile(this.getAbsolutePath())) {
//                System.err.printf("%s found %s [%d-%d] in \n\t%s\n", (anUid == null) ? "NOT " : "", name, startOffset, endOffset, declarations);
//            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        if (anUid != null && UIDUtilities.getEndOffset(anUid) != endOffset) {
            anUid = null;
        }
        return UIDCsmConverter.UIDtoDeclaration(anUid);
    }

    @Override
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uidDecl = RepositoryUtils.put(decl);
        try {
            declarationsLock.writeLock().lock();
            declarations.put(getOffsetSortKey(decl), uidDecl);
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        // TODO: remove this dirty hack!
        if (decl instanceof VariableImpl) {
            VariableImpl v = (VariableImpl) decl;
            if (!NamespaceImpl.isNamespaceScope(v, true)) {
                v.setScope(this, true);
                addStaticVariableDeclaration(uidDecl);
            }
        }
        if (CsmKindUtilities.isFunctionDeclaration(decl)) {
            if (decl instanceof FunctionImpl) {
                FunctionImpl fi = (FunctionImpl) decl;
                if (!NamespaceImpl.isNamespaceScope(fi)) {
                    fi.setScope(this);
                    addStaticFunctionDeclaration(uidDecl);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addStaticFunctionDeclaration(CsmUID uidDecl) {
        try {
            staticLock.writeLock().lock();
            staticFunctionDeclarationUIDs.add(uidDecl);
        } finally {
            staticLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private void addStaticVariableDeclaration(CsmUID uidDecl) {
        try {
            staticLock.writeLock().lock();
            staticVariableUIDs.add(uidDecl);
        } finally {
            staticLock.writeLock().unlock();
        }
    }

    /**
     * Gets the list of the static functions declarations (not definitions)
     * This is necessary for finding definitions/declarations
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    public Collection<CsmFunction> getStaticFunctionDeclarations() {
        Collection<CsmFunction> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarations(staticFunctionDeclarationUIDs);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmFunction> getStaticFunctionDeclarations(CsmFilter filter) {
        Iterator<CsmFunction> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(staticFunctionDeclarationUIDs, filter);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Collection<CsmVariable> getStaticVariableDeclarations() {
        Collection<CsmVariable> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarations(staticVariableUIDs);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    public Iterator<CsmVariable> getStaticVariableDeclarations(CsmFilter filter) {
        Iterator<CsmVariable> out;
        try {
            staticLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(staticVariableUIDs, filter);
        } finally {
            staticLock.readLock().unlock();
        }
        return out;
    }

    @Override
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        _removeDeclaration(declaration);
    }

    private void _removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uidDecl;
        try {
            declarationsLock.writeLock().lock();
            uidDecl = declarations.remove(getOffsetSortKey(declaration));
            sortedDeclarations = null;
        } finally {
            declarationsLock.writeLock().unlock();
        }
        RepositoryUtils.remove(uidDecl, declaration);
        // update repository
        RepositoryUtils.put(this);
    }

    private OffsetSortedKey getOffsetSortKey(CsmOffsetableDeclaration declaration) {
        return new OffsetSortedKey(declaration);
    }

    @Override
    public CharSequence getAbsolutePath() {
        return fileBuffer.getAbsolutePath();
    }

    public File getFile() {
        return fileBuffer.getFile();
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        l.addAll(getStaticVariableDeclarations());
        l.addAll(getStaticFunctionDeclarations());
        return l;
    }

    @Override
    public boolean isValid() {
        CsmProject project = _getProject(false);
        return project != null && project.isValid();
    }

    @Override
    public boolean isParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED;
        }
    }

    public final State getState() {
        synchronized (changeStateLock) {
            return state;
        }
    }

    public boolean isParsingOrParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED || parsingState != ParsingState.NOT_BEING_PARSED;
        }
    }

    private static final boolean TRACE_SCHUDULE_PARSING = Boolean.getBoolean("cnd.trace.schedule.parsing"); // NOI18N
    @Override
    public void scheduleParsing(boolean wait) throws InterruptedException {
        synchronized (stateLock) {
            while (!isParsed()) {
                String oldName = wait ? Thread.currentThread().getName() : "";
                try {
                    if (wait) {
                        StringBuilder name = new StringBuilder(oldName);
                        name.append(": scheduleParsing ").append(getAbsolutePath()); // NOI18N
                        name.append(" in states ").append(state).append(", ").append(parsingState); // NOI18N
                        Thread.currentThread().setName(name.toString());
                    }
                    if (!isParsingOrParsed()) {
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: enqueue %s in states %s, %s\n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                        boolean added = ParserQueue.instance().add(this, Collections.singleton(DUMMY_STATE),
                                ParserQueue.Position.HEAD, false, ParserQueue.FileAction.NOTHING);
                        if (!added) {
                            return;
                        }
                    }
                    if (wait) {
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: waiting for %s in states %s, %s\n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                        stateLock.wait();
                        if (TRACE_SCHUDULE_PARSING) {
                            System.err.printf("scheduleParsing: lock notified for %s in states %s, %s\n", getAbsolutePath(), state, parsingState); // NOI18N
                        }
                    } else {
                        return;
                    }
                } finally {
                    if (wait) {
                        Thread.currentThread().setName(oldName);
                    }
                }
            }
        }
    }

    public final void onFakeRegisration(FunctionImplEx decl, AST fakeRegistrationAst) {
        synchronized (fakeRegistrationPairs) {
            CsmUID<FunctionImplEx> uidDecl = UIDCsmConverter.declarationToUID(decl);
            fakeRegistrationPairs.add(uidDecl);
            getProjectImpl(true).trackFakeFunctionAST(getUID(), uidDecl, fakeRegistrationAst);
        }
    }

    private void clearFakeRegistrations() {
        synchronized (fakeRegistrationPairs) {
            getProjectImpl(true).cleanAllFakeFunctionAST(getUID());
            fakeRegistrationPairs.clear();
        }
    }

    private volatile boolean alreadyInFixFakeRegistrations = false;

    /**
     * Fixes ambiguities.
     *
     * @param clearFakes - indicates that we should clear list of fake registrations (all have been parsed and we have no chance to fix them in future)
     */
    private boolean fixFakeRegistrations(boolean projectParsedMode) {
        boolean wereFakes = false;
        synchronized (fakeRegistrationPairs) {
            if (!alreadyInFixFakeRegistrations) {
                alreadyInFixFakeRegistrations = true;
                if (fakeRegistrationPairs.isEmpty() || !isValid()) {
                    alreadyInFixFakeRegistrations = false;
                    return false;
                }
                if (fakeRegistrationPairs.size() > 0) {
                    for (int i = 0; i < fakeRegistrationPairs.size(); i++) {
                        CsmUID<FunctionImplEx> fakeUid = fakeRegistrationPairs.get(i);
                        AST fakeAST = getProjectImpl(true).getFakeFunctionAST(getUID(), fakeUid);
                        CsmDeclaration curElem = fakeUid.getObject();
                        if (curElem != null) {
                            if (curElem instanceof FunctionImplEx) {
                                wereFakes = true;
                                incParseCount();
                                if (((FunctionImplEx) curElem).fixFakeRegistration(projectParsedMode, fakeAST)) {
                                    getProjectImpl(true).trackFakeFunctionAST(getUID(), fakeUid, null);
                                }
                                incParseCount();
                            } else {
                                DiagnosticExceptoins.register(new Exception("Incorrect fake registration class: " + curElem.getClass() + " for fake UID:" + fakeUid)); // NOI18N
                            }
                        }
                    }
                }
                alreadyInFixFakeRegistrations = false;
            }
        }
        return wereFakes;
    }

    public
    @Override
    String toString() {
        return "FileImpl @" + hashCode() + ":" + super.hashCode() + ' ' + getAbsolutePath() + " prj:" + System.identityHashCode(this.projectUID) + this.projectUID; // NOI18N
    }

    @Override
    public final CsmUID<CsmFile> getUID() {
        CsmUID<CsmFile> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = out = UIDUtilities.createFileUID(this);
                }
            }
        }
        return uid;
    }
    private CsmUID<CsmFile> uid = null;

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(DataOutput output) throws IOException {
        PersistentUtils.writeBuffer(this.fileBuffer, output);

        PersistentUtils.writeErrorDirectives(this.errors, output);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        try {
            declarationsLock.readLock().lock();
            factory.writeOffsetSortedToUIDMap(this.declarations, output, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
        try {
            includesLock.readLock().lock();
            factory.writeUIDCollection(this.includes, output, false);
            factory.writeUIDCollection(this.brokenIncludes, output, false);
        } finally {
            includesLock.readLock().unlock();
        }
        try {
            macrosLock.readLock().lock();
            factory.writeNameSortedToUIDMap(this.macros, output, false);
        } finally {
            macrosLock.readLock().unlock();
        }
        factory.writeUIDCollection(this.fakeRegistrationPairs, output, false);
        //output.writeUTF(state.toString());
        output.writeByte(fileType.ordinal());

        // not null UID
        assert this.projectUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        output.writeLong(lastParsed);
        output.writeInt(lastParseTime);
        State curState = state;
        if (curState != State.PARSED && curState != State.INITIAL) {
            if (TraceFlags.TIMING) {
                System.err.printf("file is written in intermediate state %s, switching to PARSED: %s \n", curState, getAbsolutePath());
                //if (CndUtils.isDebugMode() && !firstDump) {
                //    firstDump = true;
                //    CndUtils.threadsDump();
                //}
            }
            curState = State.PARSED;
        }
        output.writeByte(curState.ordinal());
        try {
            staticLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticFunctionDeclarationUIDs, output, false);
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticVariableUIDs, output, false);
        } finally {
            staticLock.readLock().unlock();
        }
    }
    //private static boolean firstDump = false;

    public FileImpl(DataInput input) throws IOException {
        this.fileBuffer = PersistentUtils.readBuffer(input);

        PersistentUtils.readErrorDirectives(this.errors, input);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.declarations = factory.readOffsetSortedToUIDMap(input, null);
        factory.readUIDCollection(this.includes, input);
        factory.readUIDCollection(this.brokenIncludes, input);
        this.macros = factory.readNameSortedToUIDMap(input, DefaultCache.getManager());
        factory.readUIDCollection(this.fakeRegistrationPairs, input);
        fileType = FileType.values()[input.readByte()];

        this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(FileImpl.this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        // not null UID
        assert this.projectUID != null;
        this.projectRef = null;

        assert fileBuffer != null;
        assert fileBuffer.isFileBased();
        lastParsed = input.readLong();
        lastParseTime = input.readInt();
        state = State.values()[input.readByte()];
        parsingState = ParsingState.NOT_BEING_PARSED;
        int collSize = input.readInt();
        if (collSize <= 0) {
            staticFunctionDeclarationUIDs = new ArrayList<CsmUID<CsmFunction>>(0);
        } else {
            staticFunctionDeclarationUIDs = new ArrayList<CsmUID<CsmFunction>>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticFunctionDeclarationUIDs, input, collSize);
        collSize = input.readInt();
        if (collSize <= 0) {
            staticVariableUIDs = new ArrayList<CsmUID<CsmVariable>>(0);
        } else {
            staticVariableUIDs = new ArrayList<CsmUID<CsmVariable>>(collSize);
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticVariableUIDs, input, collSize);
    }

    public
    @Override
    int hashCode() {
        if (hash == 0) {   // we don't need sync here - at worst, we'll calculate the same value twice
            String identityHashPath = getProjectImpl(true).getUniqueName() + "*" + getAbsolutePath(); // NOI18N
            hash = identityHashPath.hashCode();
        }
        return hash;
    }

    public
    @Override
    boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileImpl)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FileImpl other = (FileImpl) obj;
        if (this.getAbsolutePath().equals(other.getAbsolutePath())) {
            return this.getProjectImpl(true).getUniqueName().equals(other.getProjectImpl(true).getUniqueName());
        }
        return false;
    }

    public int getOffset(int line, int column) {
        if (line <= 0 || column <= 0) {
            throw new IllegalArgumentException("line and column are 1-based"); // NOI18N
        }
        int offset = 0;
        int curLine = 1;
        String text = getText();
        // find line
        for (; offset < text.length() && curLine < line; offset++) {
            if (text.charAt(offset) == '\n') {
                curLine++;
            }
        }
        // check line
        if (curLine < line) {
            throw new IllegalStateException("no line with index " + line + " in file " + getAbsolutePath()); // NOI18N
        }
        int outOffset = offset + (column - 1);
        // check that column is valid: not on the next line
        if (text.length() < outOffset || (text.substring(offset, outOffset).indexOf('\n') >= 0)) { // NOI18N
            throw new IllegalStateException("no column with index " + column + " in file " + getAbsolutePath()); // NOI18N
        }
        return outOffset;
    }

    /**
     * returns 1-based line and column associated with offset
     * @param offset interested offset in file
     * @return returns pair {line, column}
     */
    public int[] getLineColumn(int offset) {
        int[] lineCol = new int[]{1, 1};
        String text = getText();
        if (offset == Integer.MAX_VALUE) {
            offset = text.length();
        }
        if (text.length() < offset) {
            throw new IllegalArgumentException("offset is out of file length; " + // NOI18N
                    (getBuffer().isFileBased() ? "file based" : "document based") + // NOI18N
                    " file=" + this.getAbsolutePath() + // NOI18N
                    ";length=" + text.length() + "; offset=" + offset); // NOI18N
        }
        final int TABSIZE = ModelSupport.getTabSize();
        // find line and column
        for (int curOffset = 0; curOffset < offset; curOffset++) {
            char curChar = text.charAt(curOffset);
            if (curChar == '\n') {
                // just increase line number
                lineCol[0] = lineCol[0] + 1;
                lineCol[1] = 1;
            } else if (curChar == '\t') {
                int col = lineCol[1];
                int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                lineCol[1] = newCol;
            } else {
                lineCol[1]++;
            }
        }
        return lineCol;
    }

    private final FileStateCache stateCache = new FileStateCache(this);
    /*package-local*/ void cacheVisitedState(APTPreprocHandler.State inputState, APTPreprocHandler outputHandler, FilePreprocessorConditionState pcState) {
        stateCache.cacheVisitedState(inputState, outputHandler, pcState);
    }

    /*package-local*/ PreprocessorStatePair getCachedVisitedState(APTPreprocHandler.State inputState) {
        return stateCache.getCachedVisitedState(inputState);
    }

    /*package-local*/ void clearStateCache() {
        tsRef.clear();
        stateCache.clearStateCache();
        APTFileCacheManager.invalidate(this.getBuffer());
    }

    public static class NameKey implements Comparable<NameKey> {
        private int start = 0;
        private CharSequence name;
        private NameKey(CsmUID<CsmOffsetableDeclaration> anUid) {
            name = UIDUtilities.getName(anUid);
            start = UIDUtilities.getStartOffset(anUid);
        }

        private NameKey(CharSequence name, int offset) {
            this.name = name;
            start = offset;
        }

        @Override
        public int compareTo(NameKey o) {
            int res = CharSequenceKey.Comparator.compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }
    }

    public static final class OffsetSortedKey implements Comparable<OffsetSortedKey>, Persistent, SelfPersistent {

        private int start = 0;
        private CharSequence name;

        private OffsetSortedKey(CsmOffsetableDeclaration declaration) {
            start = ((CsmOffsetable) declaration).getStartOffset();
            name = declaration.getName();
        }

        private OffsetSortedKey(int offset, CharSequence name) {
            start = offset;
            this.name = NameCache.getManager().getString(name);
        }

        @Override
        public int compareTo(OffsetSortedKey o) {
            int res = start - o.start;
            if (res == 0) {
                res = CharSequenceKey.Comparator.compare(name, o.name);
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OffsetSortedKey) {
                OffsetSortedKey key = (OffsetSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "OffsetSortedKey: " + this.name + "[" + this.start; // NOI18N
        }

        @Override
        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            PersistentUtils.writeUTF(name, output);
        }

        public OffsetSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = PersistentUtils.readUTF(input, NameCache.getManager());
        }
    }

    public static class NameSortedKey implements Comparable<NameSortedKey>, Persistent, SelfPersistent {

        private int start = 0;
        private CharSequence name;

        private NameSortedKey(CsmMacro macro) {
            this(macro.getName(), macro.getStartOffset());
        }

        private NameSortedKey(CharSequence name, int start) {
            this.start = start;
            this.name = NameCache.getManager().getString(name);
        }

        @Override
        public int compareTo(NameSortedKey o) {
            int res = CharSequenceKey.Comparator.compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NameSortedKey) {
                NameSortedKey key = (NameSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "NameSortedKey: " + this.name + "[" + this.start; // NOI18N
        }

        public static NameSortedKey getStartKey(CharSequence name) {
            return new NameSortedKey(name, 0);
        }

        public static NameSortedKey getEndKey(CharSequence name) {
            return new NameSortedKey(name, Integer.MAX_VALUE);
        }

        @Override
        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            PersistentUtils.writeUTF(name, output);
        }

        public NameSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = PersistentUtils.readUTF(input, NameCache.getManager());
        }
    }

    private static class EmptyCollection<T> extends AbstractCollection<T> {

        @Override
        public int size() {
            return 0;
        }

        public
        @Override
        boolean contains(Object obj) {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.<T>emptyList().iterator();
        }
    }

    public static boolean traceFile(CharSequence file) {
        if (TraceFlags.TRACE_FILE_NAME != null) {
            if (TraceFlags.TRACE_FILE_NAME.length() == 0) {
                // trace all files
                return true;
            }
            return file.toString().endsWith(TraceFlags.TRACE_FILE_NAME);
        }
        return false;
    }
}
