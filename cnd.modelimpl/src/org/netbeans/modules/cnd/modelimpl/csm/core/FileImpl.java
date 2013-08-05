/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.antlr.Parser;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;
import org.netbeans.modules.cnd.modelimpl.content.file.FakeIncludePair;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentDeclarations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentIncludes;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentInstantiations;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentMacros;
import org.netbeans.modules.cnd.modelimpl.content.file.FileComponentReferences;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContentSignature;
import org.netbeans.modules.cnd.modelimpl.csm.ClassImpl;
import org.netbeans.modules.cnd.modelimpl.csm.EnumImpl;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImplEx;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceDefinitionImpl;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTIndexingWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParser;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.CsmParserResult;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider.ParserError;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc.ChangedSegment;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;
import org.netbeans.modules.cnd.modelimpl.trace.TraceUtils;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.modelutil.Tracer;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;

/**
 * CsmFile implementations
 * @author Vladimir Kvashin
 */
public final class FileImpl implements CsmFile,
        Disposable, Persistent, SelfPersistent, CsmIdentifiable {

    private final ThreadLocal<AtomicReference<FileContent>> parsingFileContentRef = new ThreadLocal<AtomicReference<FileContent>>() {

        @Override
        protected AtomicReference<FileContent> initialValue() {
            return new AtomicReference<FileContent>(null);
        }
    };

    public static boolean isFileBeingParsedInCurrentThread(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).getParsingFileContent() != null;
        }
        return false;
    }

    /*package*/ FileContent prepareLazyStatementParsingContent() {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        assert parsingFileContentRef.get().get() == null;
        FileContent out = FileContent.getHardReferenceBasedCopy(this.currentFileContent, false);
        parsingFileContentRef.get().set(out);
        return out;
    }
    
    /*package*/ void releaseLazyStatementParsingContent(FileContent tmpFileContent) {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        FileContent cur = parsingFileContentRef.get().get();
        if (cur == tmpFileContent) {
            // TODO: merge parse errors?
            parsingFileContentRef.get().set(null);
        }
    }

    public FileContent getParsingFileContent() {
        return parsingFileContentRef.get().get();
    }
    
    public FileContent prepareIncludedFileParsingContent() {
        assert TraceFlags.PARSE_HEADERS_WITH_SOURCES;
        if (getParsingFileContent() == null) {
            parsingFileContentRef.get().set(FileContent.getHardReferenceBasedCopy(this.currentFileContent, false));
        }
        return getParsingFileContent();
    }
    
    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | TraceFlags.DEBUG;
    public static final int PARSE_FILE_TIMEOUT = 30;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    // the next flag(s) make sense only in the casew reportParse is true
    private static final boolean logState = Boolean.getBoolean("parser.log.state");
//    private static final boolean logEmptyTokenStream = Boolean.getBoolean("parser.log.empty");
    private static final boolean emptyAstStatictics = Boolean.getBoolean("parser.empty.ast.statistics");

    public static final int UNDEFINED_FILE = 0;
    public static final int SOURCE_FILE = 1;
    public static final int SOURCE_C_FILE = 2;
    public static final int SOURCE_CPP_FILE = 3;
    public static final int HEADER_FILE = 4;
    private static volatile AtomicLong parseCount = new AtomicLong(1);

    private Collection<ParserError> parsingErrors;
    
    public static void incParseCount() {
        parseCount.incrementAndGet();
    }

    public static int getParseCount() {
        return (int) (parseCount.get() & 0xFFFFFFFFL);
    }

    public static long getLongParseCount() {
        return parseCount.get();
    }

    private FileBuffer fileBuffer;
    /**
     * DUMMY_STATE and DUMMY_HANDLERS are used when we need to ensure that the file will be parsed.
     * Typically this happens when user edited buffer (after a delay), but also by clients request, etc. -
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
     * This (2) might happen only when there are NO other states in queue
     */
    static final Collection<APTPreprocHandler> DUMMY_HANDLERS = new EmptyCollection<APTPreprocHandler>();
    static final APTPreprocHandler.State DUMMY_STATE = new SpecialStateImpl();
    static final APTPreprocHandler.State PARTIAL_REPARSE_STATE = new SpecialStateImpl();
    static final Collection<APTPreprocHandler> PARTIAL_REPARSE_HANDLERS = new EmptyCollection<APTPreprocHandler>();
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private Object projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    /**
     * It's a map since we need to eliminate duplications
     */
    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    private int lastParseTime;
    
    FileContentSignature getSignature() {
        return FileContentSignature.create(this);
    }
    
    /*tests-only*/void debugInvalidate() {
        this.state = State.INITIAL;
    }

    void attachToProject(final ProjectBase project) {
        projectLock.writeLock().lock();
        try {
            if (projectRef == project) {
                return;
            }
            if (projectRef instanceof Reference<?> && ((Reference) projectRef).get() == project) {
                return;
            }
            projectRef = new WeakReference<ProjectBase>(project);
        } finally {
            projectLock.writeLock().unlock();
        }
    }

    /*package*/static enum State {

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
    private volatile FileContent currentFileContent;
    private FileContentSignature lastFileBasedSignature;
    private FileSnapshot fileSnapshot;
    private final Object snapShotLock = new Object();

    private volatile boolean disposed = false; // convert to flag field as soon as new flags appear

    private long lastParsed = Long.MIN_VALUE;
    private long lastParsedBufferCRC;
    private long lastParsedCompilationUnitCRC;

    /** Cache the hash code */
    private int hash = 0; // Default to 0
    private Reference<List<CsmReference>> lastMacroUsages = null;

    /** For test purposes only */
    private static TraceModel.TestHook hook = null;

    public FileImpl(FileBuffer fileBuffer, ProjectBase project, FileType fileType, NativeFileItem nativeFileItem) {
        state = State.INITIAL;
        parsingState = ParsingState.NOT_BEING_PARSED;
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert (projectUID instanceof KeyBasedUID); // this fact is used in write() and getInitId()
        this.fileBuffer = fileBuffer;
        
        hasBrokenIncludes = new AtomicBoolean(false);
        this.currentFileContent = FileContent.createFileContent(FileImpl.this, project);
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
    /*package*/static void setTestHook(TraceModel.TestHook aHook) {
        hook = aHook;
    }

    public final NativeFileItem getNativeFileItem() {
        return getProjectImpl(true).getNativeFileItem(getUID());
    }

    private ProjectBase _getProject(boolean assertNotNull) {
        Object o = projectRef;
        if (o instanceof ProjectBase) {
            return (ProjectBase) o;
        } else if (o instanceof Reference<?>) {
            ProjectBase prj = (ProjectBase)((Reference<?>) o).get();
            if (prj != null) {
                return prj;
            }
        }
        projectLock.readLock().lock();
        try {
            ProjectBase prj = null;
            if (projectRef instanceof ProjectBase) {
                prj = (ProjectBase) projectRef;
            } else if (projectRef instanceof Reference<?>) {
                prj = (ProjectBase)((Reference<?>) projectRef).get();
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

    @Override
    public FileType getFileType() {
        return fileType;
    }


    /*package local*/ void setHeaderFile() {
        if (fileType == FileType.UNDEFINED_FILE) {
            fileType = FileType.HEADER_FILE;
        }
    }

    // TODO: consider using macro map and __cplusplus here instead of just checking file name
    public APTLanguageFilter getLanguageFilter(APTPreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : Utils.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getLanguageFilter(null);
        } else {
            return APTLanguageSupport.getInstance().getFilter(getFileLanguage(), getFileLanguageFlavor());
        }
    }
    
    // Returns language for current context (compilation unit)
    public String getContextLanguage(APTPreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : Utils.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getFileLanguage();
        } else {
            return getFileLanguage();
        }
    }

    public String getFileLanguage() {
        return Utils.getLanguage(fileType, getAbsolutePath().toString());
    }

    public String getFileLanguageFlavor() {
        if(APTLanguageSupport.FORTRAN.equals(getFileLanguage())) {
            try {
                return CndLexerUtilities.detectFortranFormat(getBuffer().getText()) == CndLexerUtilities.FortranFormat.FIXED ? 
                            APTLanguageSupport.FLAVOR_FORTRAN_FIXED : 
                            APTLanguageSupport.FLAVOR_FORTRAN_FREE;
            } catch (IOException ex) {
                return APTLanguageSupport.FLAVOR_FORTRAN_FREE;
            }
        } else {
            if(CndTraceFlags.LANGUAGE_FLAVOR_CPP11) {
                return APTLanguageSupport.FLAVOR_CPP11;
            }
            NativeFileItem nativeFileItem = getNativeFileItem();
            if(nativeFileItem != null) {
                return Utils.getLanguageFlavor(nativeFileItem.getLanguageFlavor());
            }
        }
        return APTLanguageSupport.FLAVOR_UNKNOWN;        
    }
    
    public APTPreprocHandler getPreprocHandler(int offset) {
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(offset, offset);
        return getPreprocHandler(bestStatePair);
    }

    private APTPreprocHandler getPreprocHandler(PreprocessorStatePair statePair) {
        if (statePair == null) {
            return null;
        }
        final ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return null;
        }
        return projectImpl.getPreprocHandler(fileBuffer.getAbsolutePath(), statePair);
    }

    public Collection<APTPreprocHandler> getPreprocHandlersForParse() {
        final ProjectBase projectImpl = getProjectImpl(true);
        return projectImpl == null ? Collections.<APTPreprocHandler>emptyList() : projectImpl.getPreprocHandlersForParse(this);
    }

    public Collection<PreprocessorStatePair> getPreprocStatePairs() {
      ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return Collections.<PreprocessorStatePair>emptyList();
        }
        return projectImpl.getPreprocessorStatePairs(this);
    }

    public Collection<APTPreprocHandler> getFileContainerOwnPreprocHandlersToDump() {
        final ProjectBase projectImpl = getProjectImpl(true);
        return projectImpl == null ? Collections.<APTPreprocHandler>emptyList() : projectImpl.getFileContainerPreprocHandlersToDump(this.getAbsolutePath());
    }

    public Collection<PreprocessorStatePair> getFileContainerOwnPreprocessorStatePairsToDump() {
        ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return Collections.<PreprocessorStatePair>emptyList();
        }
        return projectImpl.getFileContainerStatePairsToDump(this.getAbsolutePath());
    }

    private PreprocessorStatePair getContextPreprocStatePair(int startContext, int endContext) {
        ProjectBase projectImpl = getProjectImpl(true);
        if (projectImpl == null) {
            return null;
        }
        Collection<PreprocessorStatePair> preprocStatePairs = projectImpl.getPreprocessorStatePairs(this);
        // select the best based on context offsets
        for (PreprocessorStatePair statePair : preprocStatePairs) {
            if (statePair.pcState.isInActiveBlock(startContext, endContext)) {
                return statePair;
            }
        }
        return null;
    }

    public void setBuffer(FileBuffer fileBuffer) {
        synchronized (changeStateLock) {
            this.fileBuffer = fileBuffer;
//            if (traceFile(getAbsolutePath())) {
//                new Exception("setBuffer: " + fileBuffer).printStackTrace(System.err);
//            }
            if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                if (reportParse || logState || TraceFlags.DEBUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.printf("#setBuffer changing to MODIFIED %s is %s with current state %s %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                }
                state = State.MODIFIED;
                postMarkedAsModified();
            }
        }
    }

    private void postMarkedAsModified() {
        // must be called only changeStateLock
        assert Thread.holdsLock(changeStateLock) : "must be called under changeStateLock";
        tsRef.clear();
        if (parsingState == ParsingState.BEING_PARSED) {
            parsingState = ParsingState.MODIFIED_WHILE_BEING_PARSED;
        }
    }

    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }

    private final AtomicInteger inEnsureParsed = new AtomicInteger(0);
    // ONLY FOR PARSER THREAD USAGE
    // Parser Queue ensures that the same file can be parsed at the same time
    // only by one thread.
    /*package*/ void ensureParsed(Collection<APTPreprocHandler> handlers) {
        if (TraceFlags.PARSE_HEADERS_WITH_SOURCES && this.isHeaderFile()) {
            System.err.printf("HEADERS_WITH_SOURCES: ensureParsed: %s\n", this.getAbsolutePath());
        }
        try {
            if (inEnsureParsed.incrementAndGet() != 1) {
                assert false : "concurrent ensureParsed in file " + getAbsolutePath() + parsingState + state; 
            }
            CsmModelState modelState = ModelImpl.instance().getState();
            if (modelState == CsmModelState.CLOSING || modelState == CsmModelState.OFF) {
                if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("ensureParsed: %s file is interrupted on closing model\n", this.getAbsolutePath());
                }                
                synchronized (changeStateLock) {
                    state = State.INITIAL;
                }         
                RepositoryUtils.put(this);
                return;
            }
            FileContentSignature newSignature = null;
            FileContentSignature oldSignature = null;
            boolean tryPartialReparse = (handlers == PARTIAL_REPARSE_HANDLERS);
            boolean triggerParsingActivity = (handlers != DUMMY_HANDLERS);
            if (handlers == DUMMY_HANDLERS || handlers == PARTIAL_REPARSE_HANDLERS) {
                handlers = getPreprocHandlersForParse();
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
                            System.err.printf("#ensureParsed %s is %s, has %d handlers, state %s %s triggerParsingActivity=%s\n", getAbsolutePath(), fileType, handlers.size(), curState, parsingState, triggerParsingActivity); // NOI18N
                            int i = 0;
                            for (APTPreprocHandler aPTPreprocHandler : handlers) {
                                logParse("EnsureParsed handler " + (i++), aPTPreprocHandler); // NOI18N
                            }
                        }
                    }
                    APTFile fullAPT = getFileAPT(true);
                    if (fullAPT == null) {
                        // probably file was removed
                        return;
                    }
                    
                    if (CndTraceFlags.TEXT_INDEX) {
                        APTIndexingWalker aptIndexingWalker = new APTIndexingWalker(fullAPT, getTextIndexKey(), getProjectImpl(true).getCacheLocation());
                        aptIndexingWalker.index();
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
                                ParseDescriptor parseParams = new ParseDescriptor(this, fullAPT, null, false, triggerParsingActivity);
                                long compUnitCRC = 0;
                                for (APTPreprocHandler preprocHandler : handlers) {
                                    compUnitCRC = APTHandlersSupport.getCompilationUnitCRC(preprocHandler);
                                    parseParams.setCurrentPreprocHandler(preprocHandler);
                                    parseParams.setLanguage(getContextLanguage(preprocHandler.getState()));
                                    _parse(parseParams);
                                    if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                        break; // does not make sense parsing old data
                                    }
                                }
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    updateModelAfterParsing(parseParams.content, compUnitCRC);
                                }
                            } finally {
                                postParse();
                                synchronized (changeStateLock) {
                                    if (parsingState == ParsingState.BEING_PARSED) {
                                        state = State.PARSED;
                                    }  // if not, someone marked it with new state
                                }
                                postParseNotify();
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
                                ParseDescriptor parseParams = new ParseDescriptor(this, fullAPT, null, true, triggerParsingActivity);
                                if (lastFileBasedSignature == null) {
                                    if (tryPartialReparse ||  !fileBuffer.isFileBased()) {
                                        // initialize file-based content signature
                                        lastFileBasedSignature = FileContentSignature.create(this);
                                    }
                                }
                                long compUnitCRC = 0;
                                for (APTPreprocHandler preprocHandler : handlers) {
                                    parseParams.setCurrentPreprocHandler(preprocHandler);
                                    parseParams.setLanguage(getContextLanguage(preprocHandler.getState()));
                                    if (first) {
                                        compUnitCRC = APTHandlersSupport.getCompilationUnitCRC(preprocHandler);
                                        _reparse(parseParams);
                                        first = false;
                                    } else {
                                        _parse(parseParams);
                                    }
                                    if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                                        break; // does not make sense parsing old data
                                    }
                                }
                                if (parsingState == ParsingState.BEING_PARSED) {
                                    updateModelAfterParsing(parseParams.content, compUnitCRC);
                                    if (tryPartialReparse) {
                                        assert lastFileBasedSignature != null;
                                        newSignature = FileContentSignature.create(this);
                                        oldSignature = lastFileBasedSignature;
                                        lastFileBasedSignature = null;
                                    }
                                }
                            } finally {
                                postParse();
                                synchronized (changeStateLock) {
                                    if (parsingState == ParsingState.BEING_PARSED) {
                                        state = State.PARSED;
                                    } // if not, someone marked it with new state
                                }
                                postParseNotify();
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
            // check state at the end as well, because there could be interruption during parse of file
            modelState = ModelImpl.instance().getState();
            if (modelState == CsmModelState.CLOSING || modelState == CsmModelState.OFF) {
                if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("after ensureParsed: %s file is interrupted on closing model\n", this.getAbsolutePath());
                }
                synchronized (changeStateLock) {
                    state = State.INITIAL;
                }
                RepositoryUtils.put(this);
            } else {
                // if was request for partial reparse and file state was not modified during parse
                if (tryPartialReparse && newSignature != null) {
                    assert oldSignature != null;
                    DeepReparsingUtils.finishPartialReparse(this, oldSignature, newSignature);
                }
            }
        } finally {
            if (inEnsureParsed.decrementAndGet() != 0) {
                CndUtils.assertTrueInConsole(false, "broken state in file " + getAbsolutePath() + parsingState + state);
            }
            // all exist points must have state change notifcation
            synchronized (stateLock) {
                stateLock.notifyAll();
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
    }

    private void postParseNotify() {
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
    /*package*/int getLastParseTime(){
        return lastParseTime;
    }
    
    /*package*/boolean validate() {
        synchronized (changeStateLock) {
            if (state == State.PARSED) {
                long lastModified = getBuffer().lastModified();
                // using "==" when comparison disallows offline index: in most cases timestamps differ
                if (TraceFlags.USE_CURR_PARSE_TIME ? (lastModified > lastParsed) : (lastModified != lastParsed)) {
                    if (lastParsedBufferCRC != getBuffer().getCRC()) {
                        if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_191307_BUG) {
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
            }
            return true;
        }
    }

    private static final class ChangeStateLock {}
    private final Object changeStateLock = new ChangeStateLock();

    public final void markReparseNeeded(boolean invalidateCache) {
        synchronized (changeStateLock) {
            if (reportParse || logState || TraceFlags.DEBUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("#markReparseNeeded %s is %s with current state %s, %s\n", getAbsolutePath(), fileType, state, parsingState); // NOI18N
                if (TraceFlags.TRACE_191307_BUG) {
                    new Exception("markReparseNeeded is called").printStackTrace(System.err);// NOI18N
                }// NOI18N
            }
            if (state != State.INITIAL || parsingState != ParsingState.NOT_BEING_PARSED) {
                state = State.MODIFIED;
                postMarkedAsModified();
            }
            if (invalidateCache) {
                final FileBuffer buf = this.getBuffer();
                APTDriver.invalidateAPT(buf);
                APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
            }
        }
    }

    /*package*/final void markMoreParseNeeded() {
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

    /*package*/final int getErrorCount() {
        checkNotInParsingThreadImpl();
        return currentFileContent.getErrorCount();
    }
    
    /*package*/APTFile getFileAPT(boolean full) {
        APTFile fileAPT = null;
        ChangedSegment changedSegment = null;
        try {
            if (full) {
                fileAPT = APTDriver.findAPT(this.getBuffer(), getFileLanguage(), getFileLanguageFlavor());
            } else {
                fileAPT = APTDriver.findAPTLight(this.getBuffer());
            }
            if (getBuffer() instanceof FileBufferDoc) {
                changedSegment = ((FileBufferDoc) getBuffer()).getLastChangedSegment();
            }
        } catch (FileNotFoundException ex) {
            APTUtils.LOG.log(Level.WARNING, "FileImpl: file {0} not found, probably removed", new Object[]{getBuffer().getAbsolutePath()});// NOI18N
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
        if (fileAPT != null && APTUtils.LOG.isLoggable(Level.FINE)) {
            CharSequence guardMacro = fileAPT.getGuardMacro();
            if (guardMacro.length() == 0 && !isSourceFile()) {
                APTUtils.LOG.log(Level.FINE, "FileImpl: file {0} does not have guard", new Object[]{getBuffer().getAbsolutePath()});// NOI18N
            }
        }
        return fileAPT;
    }

    private void _reparse(ParseDescriptor parseParams) {
        parsingFileContentRef.get().set(parseParams.content);
        try {
            if (TraceFlags.DEBUG) {
                Diagnostic.trace("------ reparsing " + fileBuffer.getUrl()); // NOI18N
            }
            synchronized(snapShotLock) {
                fileSnapshot = new FileSnapshot(this);
            }
            if (reportParse || logState || TraceFlags.DEBUG) {
                logParse("ReParsing", parseParams.getCurrentPreprocHandler()); //NOI18N
            }
            disposeAll(false);
            CsmParserResult parsing = doParse(parseParams);
            if (parsing != null) {
                if (isValid()) {
                    parsing.render(parseParams);
                }
            } else {
                //System.err.println("null ast for file " + getAbsolutePath());
            }
            fileSnapshot = null;
        } finally {
            parsingFileContentRef.get().set(null);
        }
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
        disposed = true;
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
            if (projectRef instanceof Reference<?>) {
                projectRef = ((Reference)projectRef).get();
            }
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
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = currentFileContent.cleanDeclarations();
        clearFakeRegistrations();

        hasBrokenIncludes.set(false);
        if (clearNonDisposable) {
            currentFileContent.cleanOther();
        }
        currentFileContent.put();
        Collection<CsmOffsetableDeclaration> arr = UIDCsmConverter.UIDsToDeclarations(uids);
        Utils.disposeAll(arr);
        RepositoryUtils.remove(uids);
    }

    /**enapsulates all parameters which should be used during parse or reparse of the file */
    /*package*/static final class ParseDescriptor implements CsmParserProvider.CsmParserParameters {

        private final CsmParserProvider.CsmParseCallback callback;
        private final FileContent content;
        private final boolean lazyCompound;
        private final APTFile fullAPT;
        private APTPreprocHandler curPreprocHandler;
        private final FileImpl fileImpl;
        private final boolean triggerParsingActivity;
        // FIXME: it's worth to remember states before parse and reuse after
        private final long lastParsed;
        private final long lastParsedCRC;
        private String language = APTLanguageSupport.GNU_CPP;

        public ParseDescriptor(FileImpl fileImpl, APTFile fullAPT, CsmParserProvider.CsmParseCallback callback, boolean emptyFileContent, boolean triggerParsingActivity) {
            this(fileImpl, fullAPT, callback, TraceFlags.EXCLUDE_COMPOUND, emptyFileContent, triggerParsingActivity);
        }

        public ParseDescriptor(FileImpl fileImpl, APTFile fullAPT,
                CsmParserProvider.CsmParseCallback callback,
                boolean lazyCompound, boolean emptyFileContent, boolean triggerParsingActivity) {
            assert fileImpl != null : "null file is not allowed";
            assert fullAPT != null : "null APTFile is not allowed";
            this.fileImpl = fileImpl;
            this.content = FileContent.getHardReferenceBasedCopy(fileImpl.currentFileContent, emptyFileContent);
            this.fullAPT = fullAPT;
            this.callback = callback;
            this.lazyCompound = lazyCompound;
            this.triggerParsingActivity = triggerParsingActivity;
            this.lastParsed = fileImpl.fileBuffer.lastModified();
            this.lastParsedCRC = fileImpl.fileBuffer.getCRC();
        }

        private void setCurrentPreprocHandler(APTPreprocHandler preprocHandler) {
            assert preprocHandler != null : "null preprocHandler is not allowed";
            this.curPreprocHandler = preprocHandler;
        }
        
        private void setLanguage(String language) {
            assert language != null : "null language is not allowed";
            this.language = language;
        }
        
        private APTPreprocHandler getCurrentPreprocHandler() {
            assert curPreprocHandler != null : "null preprocHandler is not allowed";
            return curPreprocHandler;
        }

        public FileContent getFileContent() {
            return content;
        }
        
        @Override
        public String getLanguage() {
            return language;
        }        

        @Override
        public CsmFile getMainFile() {
            return fileImpl;
        }
    }
    
    /** for debugging/tracing purposes only */
    public AST debugParse() {
        Collection<APTPreprocHandler> handlers = getFileContainerOwnPreprocHandlersToDump();
        if (handlers.isEmpty()) {
            return null;
        }
        final APTFile fullAPT = getFileAPT(true);
        ParseDescriptor params = new ParseDescriptor(this, fullAPT, null, false, false, false);
        params.setCurrentPreprocHandler(handlers.iterator().next());
        synchronized (stateLock) {
            CsmParserResult parsing = _parse(params);
            Object ast = parsing.getAST();
            if (ast instanceof AST) {
                return (AST) ast;
            }
        }
        return null;
    }

    private CsmParserResult _parse(ParseDescriptor parseParams) {
        parsingFileContentRef.get().set(parseParams.content);
        PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(Tracer.PARSE_FILE_PERFORMANCE_EVENT, getFileObject());
        try {
            performanceEvent.setTimeOut(FileImpl.PARSE_FILE_TIMEOUT);
            Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
            if (reportParse || logState || TraceFlags.DEBUG) {
                logParse("Parsing", parseParams.getCurrentPreprocHandler()); //NOI18N
            }
            CsmParserResult parsing = doParse(parseParams);
            if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
                sw.stopAndReport("Parsing of " + fileBuffer.getUrl() + " took \t"); // NOI18N
            }
            if (parsing != null) {
                Diagnostic.StopWatch sw2 = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
                if (isValid()) {   // FIXUP: use a special lock here
                    parsing.render(parseParams);
                    if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) {
                        sw2.stopAndReport("Rendering of " + fileBuffer.getUrl() + " took \t"); // NOI18N
                    }
                }
            }
            return parsing;
        } finally {
            ProjectBase projectImpl = getProjectImpl(false);
            if (projectImpl != null) {
                if (projectImpl.isArtificial()) {
                    Collection<ProjectBase> dependentProjects = ((LibProjectImpl)projectImpl).getDependentProjects();
                    if (dependentProjects.size() > 0) {
                        projectImpl = dependentProjects.iterator().next();
                    }
                }
            }
            Object platformProject = null;
            if (projectImpl != null) {
                platformProject = projectImpl.getPlatformProject();
            }
            int lines = 0;
            try {
                lines = getBuffer().getLineCount();
            } catch (IOException ex) {
            }
            if (platformProject instanceof NativeProject) {
                performanceEvent.log(lines, ((NativeProject)platformProject).getProject());
            } else {
                performanceEvent.log(lines);
            }
            parsingFileContentRef.get().set(null);
        }
    }

    private void logParse(String title, APTPreprocHandler preprocHandler) {
        if (reportParse || logState || TraceFlags.DEBUG) {
            System.err.printf("# %s %s \n#\t(%s %s %s) \n#\t(Thread=%s)\n", //NOI18N
                    title, fileBuffer.getUrl(),
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
        PreprocessorStatePair bestStatePair = getContextPreprocStatePair(startContext, endContext);
        APTPreprocHandler preprocHandler = getPreprocHandler(bestStatePair);
        if (preprocHandler == null) {
            return false;
        }
        APTPreprocHandler.State ppState = preprocHandler.getState();
        // ask for cache and pcBuilder as well
        AtomicReference<APTFileCacheEntry> cacheEntry = new AtomicReference<APTFileCacheEntry>(null);
        AtomicReference<FilePreprocessorConditionState.Builder> pcBuilder = new AtomicReference<FilePreprocessorConditionState.Builder>(null);
        TokenStream tokenStream = createParsingTokenStreamForHandler(preprocHandler, false, cacheEntry, pcBuilder);
        if (tokenStream == null) {
            return false;
        }
        APTLanguageFilter languageFilter = getLanguageFilter(ppState);
        tsCache.addNewPair(pcBuilder.get(), tokenStream, languageFilter);
        // remember walk info
        setAPTCacheEntry(ppState, cacheEntry.get(), false);
        return true;
    }
    
    private TokenStream createParsingTokenStreamForHandler(APTPreprocHandler preprocHandler, boolean filtered, 
            AtomicReference<APTFileCacheEntry> cacheOut, AtomicReference<FilePreprocessorConditionState.Builder> pcBuilderOut) {
        APTFile apt = getFileAPT(true);
        if (apt == null) {
            return null;
        }                
        if (preprocHandler == null) {
            return null;
        }
        APTPreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = Utils.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while getting TS of file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
            return null;
        }
        FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(getAbsolutePath());
        if (pcBuilderOut != null) {
            pcBuilderOut.set(pcBuilder);
        }
        // ask for concurrent entry if absent
        APTFileCacheEntry cacheEntry = getAPTCacheEntry(ppState, Boolean.FALSE);
        if (cacheOut != null) {
            cacheOut.set(cacheEntry);
        }
        APTParseFileWalker walker = new APTParseFileWalker(startProject, apt, this, preprocHandler, false, pcBuilder,cacheEntry);
        return walker.getTokenStream(filtered);
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
                    tsRef = new WeakReference<FileTokenStreamCache>(cache);
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

    private TokenStream getTokenStreamOfIncludedFile(final CsmInclude include) {
        FileImpl file = (FileImpl) include.getIncludeFile();
        if (file != null && file.isValid()) {
            // create ppHandler till #include directive
            PreprocessorStatePair includeContextPair = this.getContextPreprocStatePair(include.getStartOffset(), include.getEndOffset());
            if (includeContextPair == null) {
                return file.getTokenStream(0, Integer.MAX_VALUE, 0, true);
            }
            APTPreprocHandler.State thisFileStartState = includeContextPair.state;
            LinkedList<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(thisFileStartState);
            reverseInclStack.addLast(new IncludeInfoImpl(include, file.getAbsolutePath()));
            ProjectBase projectImpl = getProjectImpl(true);
            if (projectImpl == null) {
                return file.getTokenStream(0, Integer.MAX_VALUE, 0, true);
            }
            APTPreprocHandler preprocHandler = projectImpl.createEmptyPreprocHandler(getAbsolutePath());
            APTPreprocHandler restorePreprocHandlerFromIncludeStack = projectImpl.restorePreprocHandlerFromIncludeStack(reverseInclStack, getAbsolutePath(), preprocHandler, thisFileStartState);
            // using restored preprocessor handler, ask included file for parsing token stream filtered by language          
            TokenStream includedFileTS = file.createParsingTokenStreamForHandler(restorePreprocHandlerFromIncludeStack, true, null, null);
            if(includedFileTS != null) {
                APTLanguageFilter languageFilter = file.getLanguageFilter(thisFileStartState);
                return languageFilter.getFilteredStream(includedFileTS);
            }
        }
        return null;
    }
    
    private static class IncludeInfoImpl implements APTIncludeHandler.IncludeInfo {

        private final int line;
        private final CsmInclude include;
        private final CharSequence path;

        IncludeInfoImpl(CsmInclude include, CharSequence path) {
            this.line = include.getStartPosition().getLine();
            this.include = include;
            this.path = path;
        }

        @Override
        public CharSequence getIncludedPath() {
            return path;
        }

        @Override
        public int getIncludeDirectiveLine() {
            return line;
        }

        @Override
        public int getIncludeDirectiveOffset() {
            return include.getStartOffset();
        }

        @Override
        public int getIncludedDirIndex() {
            return 0;
        }

        @Override
        public String toString() {
            return "restore " + include + " from line " + line + " in file " + include.getContainingFile(); // NOI18N
        }
    }

    /** For test purposes only */
    /*package*/ void testErrors(TraceModel.ErrorListener errorListener) {
        Collection<ParserError> parserErrors = new ArrayList<ParserError>();
        getErrors(parserErrors);
        for (ParserError e : parserErrors) {
            errorListener.error(e.message, e.line, e.column);
        }
    }

    private static class ParserBasedTokenBuffer implements ReadOnlyTokenBuffer {

        private final Parser parser;

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

    public final APTFileCacheEntry getAPTCacheEntry(APTPreprocHandler.State ppState, Boolean createExclusiveIfAbsent) {
        if (!TraceFlags.APT_FILE_CACHE_ENTRY) {
            return null;
        }
        APTFileCacheEntry out = APTFileCacheManager.getInstance(getBuffer().getFileSystem()).getEntry(getAbsolutePath(), ppState, createExclusiveIfAbsent);
        assert createExclusiveIfAbsent == null || out != null;
        return out;
    }

    public final void setAPTCacheEntry(APTPreprocHandler.State ppState, APTFileCacheEntry entry, boolean cleanOthers) {
        if (TraceFlags.APT_FILE_CACHE_ENTRY) {
            final FileBuffer buf = getBuffer();
            APTFileCacheManager.getInstance(buf.getFileSystem()).setAPTCacheEntry(buf.getAbsolutePath(), ppState, entry, cleanOthers);
        }
    }

    public ReadOnlyTokenBuffer getErrors(final Collection<ParserError> result) {
        CsmParserProvider.ParserErrorDelegate delegate = new CsmParserProvider.ParserErrorDelegate() {

            @Override
            public void onError(ParserError e) {
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
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if (!TraceFlags.TRACE_ERROR_PROVIDER) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }
        try {
            // use cached TS
            TokenStream tokenStream = getTokenStream(0, Integer.MAX_VALUE, 0, true);
            
            if (tokenStream != null) {
                if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                    CsmProject project = getProject();
                    if(parsingErrors != null) {
                        result.addAll(parsingErrors);
                    }
                    return new ParserBasedTokenBuffer(null);
                } else {
                    CPPParserEx parser = CPPParserEx.getInstance(this, tokenStream, flags);
                    parser.setErrorDelegate(delegate);
                    parser.setLazyCompound(false);
                    parser.translation_unit();
                    return new ParserBasedTokenBuffer(parser);
                }
            }
        } catch (Throwable ex) {
            System.err.println(ex.getClass().getName() + " at parsing file " + fileBuffer.getAbsolutePath()); // NOI18N
        } finally {
            if (TraceFlags.TRACE_ERROR_PROVIDER) {
                System.err.printf("<<< Done parsing (getting errors) %s %d ms\n\n\n", getName(), System.currentTimeMillis() - time);
            }
        }
        return null;
    }

    private CsmParserResult doParse(ParseDescriptor parseParams) {

        if (reportErrors) {
            if (!ParserThreadManager.instance().isParserThread() && !ParserThreadManager.instance().isStandalone()) {
                String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
                Diagnostic.trace(text);
                new Throwable(text).printStackTrace(System.err);
            }
        }
        APTPreprocHandler preprocHandler = parseParams.getCurrentPreprocHandler();
        APTFile aptFull = parseParams.fullAPT; 
        assert preprocHandler != null;
        if (preprocHandler == null) {
            return null;
        }

        ParseStatistics.getInstance().fileParsed(this, preprocHandler);

//        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
//            if (getAbsolutePath().toString().endsWith(".h")) { // NOI18N
//                try {
//                    Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME * 1000);
//                } catch (InterruptedException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
        CsmParserResult parseResult = null;
        if (aptFull != null) {
            // use full APT for generating token stream
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: parsing using full APT for " + getAbsolutePath());
            }
            // make real parse
            APTPreprocHandler.State ppState = preprocHandler.getState();
            ProjectBase startProject = Utils.getStartProject(ppState);
            if (startProject == null) {
                System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                        "\n while parsing file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
                return null;
            }
            // We gather conditional state here as well, because sources are not included anywhere
            FilePreprocessorConditionState.Builder pcBuilder = new FilePreprocessorConditionState.Builder(getAbsolutePath());
            // ask for concurrent entry if absent
            APTFileCacheEntry aptCacheEntry = getAPTCacheEntry(ppState, Boolean.FALSE);
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFull, this, preprocHandler, parseParams.triggerParsingActivity, pcBuilder,aptCacheEntry);
            walker.setFileContent(parseParams.content);
            if (TraceFlags.DEBUG) {
                System.err.println("doParse " + getAbsolutePath() + " with " + ParserQueue.tracePreprocState(ppState));
            }

            TokenStream filteredTokenStream = walker.getFilteredTokenStream(getLanguageFilter(ppState));

            long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
            CsmParser parser = CsmParserProvider.createParser(parseParams);
            assert parser != null : "no parser for " + this;

            parser.init(this, filteredTokenStream, parseParams.callback);
            
            parseResult = parser.parse(parseParams.lazyCompound ? CsmParser.ConstructionKind.TRANSLATION_UNIT : CsmParser.ConstructionKind.TRANSLATION_UNIT_WITH_COMPOUND);
            FilePreprocessorConditionState pcState = pcBuilder.build();
            if (false) {
                setAPTCacheEntry(ppState, aptCacheEntry, false);
            }
            startProject.setParsedPCState(this, ppState, pcState);

            if (emptyAstStatictics) {
                time = System.currentTimeMillis() - time;
                boolean empty = parseResult.isEmptyAST();
                if(empty) {
                    System.err.println("PARSED FILE " + getAbsolutePath() + " HAS EMPTY AST" + ' ' + time + " ms");
                }
            }
            if (TraceFlags.DUMP_AST) {
                parseResult.dumpAST();
            }
            parseParams.content.setErrorCount(parseResult.getErrorCount());
            if (parsingState == ParsingState.MODIFIED_WHILE_BEING_PARSED) {
                parseResult = null;
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: not save cache for file modified during parsing" + getAbsolutePath());
                }
            }
        }
        clearStateCache();
        lastMacroUsages = null;
        TraceModel.TestHook aHook = hook;
        if (aHook != null) {
            aHook.parsingFinished(this, preprocHandler);
        }
//        parseCount++;
        return parseResult;
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

    long getLastParsedCompilationUnitCRC() {
        return lastParsedCompilationUnitCRC;
    }

    private void updateModelAfterParsing(FileContent fileContent, long compUnitCRC) {
        Map<CsmUID<FunctionImplEx<?>>, AST> fakeASTs = fileContent.getFakeASTs();
        ProjectBase projectImpl = getProjectImpl(true);
        CsmUID<CsmFile> thisFileUID = getUID();
        for (Map.Entry<CsmUID<FunctionImplEx<?>>, AST> entry : fakeASTs.entrySet()) {
            projectImpl.trackFakeFunctionAST(thisFileUID, entry.getKey(), entry.getValue());
        }
        hasBrokenIncludes.set(fileContent.hasBrokenIncludes());
        // handle file content
        currentFileContent = fileContent.toWeakReferenceBasedCopy();
        currentFileContent.put();
        lastParsed = fileBuffer.lastModified();
        lastParsedBufferCRC = fileBuffer.getCRC();
        lastParsedCompilationUnitCRC = compUnitCRC;
        // using file time as parse time disallows offline index: in most cases timestamps differ
        if (TraceFlags.USE_CURR_PARSE_TIME) {
            lastParsed = Math.max(System.currentTimeMillis(), fileBuffer.lastModified());
        }
        if (TraceFlags.TRACE_VALIDATION) {
            System.err.printf("PARSED    %s \n\tlastModified=%d\n\t  lastParsed=%d  diff=%d\n",
                    getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified() - lastParsed);
        }
        RepositoryUtils.put(this);
        if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
            if(parsingErrors == null) {
                parsingErrors = new ArrayList<ParserError>();
            }
            parsingErrors.clear();
            if(currentFileContent != null) {
                parsingErrors.addAll(currentFileContent.getParserErrors());
            }
        }
        if (TraceFlags.PARSE_HEADERS_WITH_SOURCES) {
            for (FileContent includedFileContent : fileContent.getIncludedFileContents()) {
                FileImpl fileImplIncluded = includedFileContent.getFile();
                fileImplIncluded.updateModelAfterParsing(includedFileContent, compUnitCRC);
                fileImplIncluded.parsingFileContentRef.get().set(null);
                if(TraceFlags.CPP_PARSER_NEW_GRAMMAR) {
                    if(fileImplIncluded.parsingErrors == null) {
                        fileImplIncluded.parsingErrors = new ArrayList<ParserError>();
                    }
                    fileImplIncluded.parsingErrors.clear();
                    if(includedFileContent != null) {
                        fileImplIncluded.parsingErrors.addAll(includedFileContent.getParserErrors());
                    }
                }
                synchronized (fileImplIncluded.changeStateLock) {
                    fileImplIncluded.state = State.PARSED;
                }
            }
        }
    }

    public void addInstantiation(CsmInstantiation inst) {
        getFileInstantiations().addInstantiation(inst);
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
    public CharSequence getText() {
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
        return CharSequences.create(PathUtilities.getBaseName(getAbsolutePath().toString()));
    }

    @Override
    public Collection<CsmInclude> getIncludes() {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getIncludes();
    }

    @Override
    public Collection<CsmErrorDirective> getErrors() {
        checkNotInParsingThreadImpl();
        return new ArrayList<CsmErrorDirective>(currentFileContent.getErrors());
    }

    public Iterator<CsmInclude> getIncludes(CsmFilter filter) {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getIncludes(filter);
    }

    public Collection<CsmInclude> getBrokenIncludes() {
        checkNotInParsingThreadImpl();
        return getFileIncludes().getBrokenIncludes();
    }

    public boolean hasBrokenIncludes() {
        checkNotInParsingThreadImpl();
        return hasBrokenIncludes.get();
    }

    /**
     * Gets the list of the static functions declarations (not definitions) This
     * is necessary for finding definitions/declarations since file-level static
     * functions (i.e. c-style static functions) aren't registered in project
     */
    public Collection<CsmFunction> getStaticFunctionDeclarations() {
        return getFileDeclarations().getStaticFunctionDeclarations();
    }

    public Iterator<CsmFunction> getStaticFunctionDeclarations(CsmFilter filter) {
        return getFileDeclarations().getStaticFunctionDeclarations(filter);
    }

    public Collection<CsmVariable> getStaticVariableDeclarations() {
        return getFileDeclarations().getStaticVariableDeclarations();
    }

    public Iterator<CsmVariable> getStaticVariableDeclarations(CsmFilter filter) {
        return getFileDeclarations().getStaticVariableDeclarations(filter);
    }

    public boolean hasDeclarations() {
        return getFileDeclarations().hasDeclarations();
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        return getFileDeclarations().getDeclarations();
    }

    /**
     * Returns number of declarations.
     * Does not fixFakeRegistrations, so this size could be inaccurate
     *
     * @return number of declarations
     */
    public int getDeclarationsSize(){
        return getFileDeclarations().getDeclarationsSize();
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        return getFileDeclarations().getDeclarations(filter);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(CsmDeclaration.Kind[] kinds, CharSequence prefix) {
        return getFileDeclarations().getDeclarations(kinds, prefix);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarations(int startOffset, int endOffset) {
        return getFileDeclarations().getDeclarations(startOffset, endOffset);
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(int offset) {
        return getFileDeclarations().getDeclarations(offset);
    }

    public Collection<CsmReference> getReferences() {
        return getFileReferences().getReferences();
    }

    public Collection<CsmReference> getReferences(Collection<CsmObject> objects) {
        return getFileReferences().getReferences(objects);
    }

    public boolean addReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addReference(ref, referencedObject);
    }

    public CsmReference getReference(int offset) {
        return getFileReferences().getReference(offset);
    }

    public boolean addResolvedReference(CsmReference ref, CsmObject referencedObject) {
        return getFileReferences().addResolvedReference(ref, referencedObject);
    }

    public void removeResolvedReference(CsmReference ref) {
        getFileReferences().removeResolvedReference(ref);
    }

    public CsmReference getResolvedReference(CsmReference ref) {
        return getFileReferences().getResolvedReference(ref);
    }

    @Override
    public Collection<CsmMacro> getMacros() {
        checkNotInParsingThreadImpl();
        return getFileMacros().getMacros();
    }

    public Iterator<CsmMacro> getMacros(CsmFilter filter) {
        checkNotInParsingThreadImpl();
        return getFileMacros().getMacros(filter);
    }

    public Collection<CsmUID<CsmMacro>> findMacroUids(CharSequence name) {
        checkNotInParsingThreadImpl();
        return getFileMacros().findMacroUids(name);
    }

    @Override
    public CharSequence getAbsolutePath() {
        return fileBuffer.getAbsolutePath();
    }

    @Override
    public FileObject getFileObject() {
        return fileBuffer.getFileObject();
    }
        
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        checkNotInParsingThreadImpl();
        return currentFileContent.getScopeElements();
    }

    @Override
    public boolean isValid() {
        if (disposed) {
            return false;
        }
        CsmProject project = _getProject(false);
        return project != null && project.isValid();
    }

    @Override
    public boolean isParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED;
        }
    }

    public void setLwmReady() {
        synchronized (changeStateLock) {
             state = State.PARSED;
             postParse();
        }
    }

    /*package*/final State getState() {
        synchronized (changeStateLock) {
            return state;
        }
    }

    public final String getStateFromTest() {
        assert CndUtils.isUnitTestMode();
        return state.toString();
    }
    
    public final String getParsingStateFromTest() {
        assert CndUtils.isUnitTestMode();
        return parsingState.toString();
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
                        boolean added = ParserQueue.instance().addToBeParsedNext(this);
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

    private void clearFakeRegistrations() {
        getProjectImpl(true).cleanAllFakeFunctionAST(getUID());
    }

    private volatile boolean alreadyInFixFakeRegistrations = false;

    /**
     * Fixes ambiguities.
     *
     * @param clearFakes - indicates that we should clear list of fake registrations (all have been parsed and we have no chance to fix them in future)
     */
    private boolean fixFakeRegistrations(boolean projectParsedMode) {
        checkNotInParsingThreadImpl();
        boolean result = false;
        result |= fixFakeFunctionRegistrations(projectParsedMode);
        result |= fixFakeIncludeRegistrations(projectParsedMode);
        return result;
    }

    private boolean fixFakeFunctionRegistrations(boolean projectParsedMode) {
        checkNotInParsingThreadImpl();
        boolean wereFakes = false;
        FileContent curContent = currentFileContent;
        List<CsmUID<FunctionImplEx<?>>> fakeFunctionRegistrations = curContent.getFakeFunctionRegistrations();
        synchronized (fakeFunctionRegistrations) {
            if (!alreadyInFixFakeRegistrations) {
                alreadyInFixFakeRegistrations = true;
                if (fakeFunctionRegistrations.isEmpty() || !isValid()) {
                    alreadyInFixFakeRegistrations = false;
                    return false;
                }
                if (fakeFunctionRegistrations.size() > 0) {
                    for (int i = 0; i < fakeFunctionRegistrations.size(); i++) {
                        CsmUID<FunctionImplEx<?>> fakeUid = fakeFunctionRegistrations.get(i);
                        AST fakeAST = getProjectImpl(true).getFakeFunctionAST(getUID(), fakeUid);
                        CsmDeclaration curElem = fakeUid.getObject();
                        if (curElem != null) {
                            if (curElem instanceof FunctionImplEx<?>) {
                                wereFakes = true;
                                incParseCount();
                                if (((FunctionImplEx<?>) curElem).fixFakeRegistration(curContent, projectParsedMode, fakeAST)) {
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

    private boolean fixFakeIncludeRegistrations(boolean projectParsedMode) {
        checkNotInParsingThreadImpl();
        boolean wereFakes = false;
        FileContent fileContent = currentFileContent;
        for (FakeIncludePair fakeIncludePair : fileContent.getFakeIncludeRegistrations()) {
            synchronized (fakeIncludePair) {
                if (!fakeIncludePair.isFixed()) {
                    CsmInclude include = UIDCsmConverter.UIDtoIdentifiable(fakeIncludePair.getIncludeUid());
                    if (include != null) {
                        CsmOffsetableDeclaration container = UIDCsmConverter.UIDtoDeclaration(fakeIncludePair.getContainerUid());
                        if (container != null && container.isValid()) {
                            FileImpl includedFile = (FileImpl) include.getIncludeFile();
                            if (includedFile != null && includedFile.isValid()) {
                                FileContent includedFileContent = includedFile.currentFileContent;
                                TokenStream ts = this.getTokenStreamOfIncludedFile(include);                               
                                if (ts != null) {
                                    CsmParser parser = CsmParserProvider.createParser(includedFile);
                                    assert parser != null : "no parser for " + this;
                                    parser.init(this, ts, null);       
                                    if (container instanceof EnumImpl) {
                                        EnumImpl enumImpl = (EnumImpl) container;
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.ENUM_BODY);
                                        result.render(includedFileContent, enumImpl, Boolean.FALSE);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    } else if (container instanceof ClassImpl) {
                                        ClassImpl cls = (ClassImpl) container;
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.CLASS_BODY);
                                        CsmDeclaration.Kind kind = cls.getKind();
                                        CsmVisibility visibility = CsmVisibility.PRIVATE;
                                        if(kind == CsmDeclaration.Kind.CLASS) {
                                            // FIXUP: it's better to have extra items in completion list
                                            // for crazy classes, than fail on resolving included
                                            // public methods
                                            // IZ#204951 - The c++ parser does not follow/parse #includes which are textually nested within a class definition
                                            visibility = CsmVisibility.PUBLIC;
                                        } else if( kind == CsmDeclaration.Kind.STRUCT ||
                                                kind == CsmDeclaration.Kind.UNION) {
                                            visibility = CsmVisibility.PUBLIC;
                                        }
                                        result.render(includedFileContent, cls, visibility, Boolean.FALSE);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    } else if (container instanceof NamespaceDefinitionImpl) {
                                        CsmParserResult result = parser.parse(CsmParser.ConstructionKind.NAMESPACE_DEFINITION_BODY);
                                        result.render(includedFileContent, (NamespaceDefinitionImpl) container);
                                        fakeIncludePair.markFixed();
                                        wereFakes = true;
                                    }
                                } else {
                                    APTUtils.LOG.log(Level.WARNING, "fixFakeIncludeRegistrations: file {0} has not tokens, probably empty or removed?", new Object[]{getBuffer().getUrl()});// NOI18N                            
                                }
                            }
                        }
                    }
                }
            }
        }
        return wereFakes;
    }

    public
    @Override
    String toString() {
        return "" + this.state + " FileImpl @" + hashCode() + ":" + super.hashCode() + ' ' + getAbsolutePath() + " prj:" + System.identityHashCode(this.projectUID) + this.projectUID + " " + this.parsingState; // NOI18N
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
    
    CndTextIndexKey getTextIndexKey() {
        return new CndTextIndexKey(getUnitId(), getFileId());
    }
    
    public int getFileId() {
        return KeyUtilities.getProjectFileIndex(((KeyBasedUID)getUID()).getKey());
    }

    public int getUnitId() {
        return ((KeyBasedUID)projectUID).getKey().getUnitId();
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        // not null UID
        assert this.projectUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        PersistentUtils.writeBuffer(this.fileBuffer, output, getUnitId());
        output.writeBoolean(hasBrokenIncludes.get());
        currentFileContent.write(output);

        output.writeByte(fileType.ordinal());

        output.writeLong(lastParsed);
        output.writeInt(lastParseTime);
        output.writeLong(lastParsedBufferCRC);
        output.writeLong(lastParsedCompilationUnitCRC);
        State curState = state;
        if (curState != State.PARSED && curState != State.INITIAL) {
            if (TraceFlags.TIMING) {
                System.err.printf("file is written in intermediate state %s, switching to INITIAL: %s \n", curState, getAbsolutePath());
            }
            curState = State.INITIAL;
        }
        output.writeByte(curState.ordinal());
    }
    //private static boolean firstDump = false;

    public FileImpl(RepositoryDataInput input) throws IOException {
        this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        if (TraceFlags.TRACE_CPU_CPP && getAbsolutePath().toString().endsWith("cpu.cc")) { // NOI18N
            new Exception("cpu.cc file@" + System.identityHashCode(FileImpl.this) + " of prjUID@" + System.identityHashCode(this.projectUID) + this.projectUID).printStackTrace(System.err); // NOI18N
        }
        // not null UID
        assert this.projectUID != null;
        this.projectRef = null;

        this.fileBuffer = PersistentUtils.readBuffer(input, getUnitId());

        hasBrokenIncludes = new AtomicBoolean(input.readBoolean());
        currentFileContent = new FileContent(this, this._getProject(false), input);

        fileType = FileType.values()[input.readByte()];

        assert fileBuffer != null;
        lastParsed = input.readLong();
        lastParseTime = input.readInt();
        lastParsedBufferCRC = input.readLong();
        lastParsedCompilationUnitCRC = input.readLong();
        state = State.values()[input.readByte()];
        parsingState = ParsingState.NOT_BEING_PARSED;
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

    // for testing only
    public int getOffset(int line, int column) {
        if (line <= 0 || column <= 0) {
            throw new IllegalArgumentException("line and column are 1-based"); // NOI18N
        }
        try {
            return fileBuffer.getOffsetByLineColumn(line, column);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }

    /**
     * returns 1-based line and column associated with offset
     * @param offset interested offset in file
     * @return returns pair {line, column}
     */
    public int[] getLineColumn(int offset) {
        if (offset == Integer.MAX_VALUE) {
            try {
                offset = fileBuffer.getCharBuffer().length;
            } catch (IOException e) {
                DiagnosticExceptoins.register(e);
                offset = 0;
            }
        }
        try {
            return fileBuffer.getLineColumnByOffset(offset);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return new int[]{0, 0};
        }
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
        final FileBuffer buf = this.getBuffer();
        APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
        
    }

    private FileComponentDeclarations getFileDeclarations() {
        FileContent contentImpl = getThreadSensitiveContentImpl();
        FileComponentDeclarations fd = contentImpl.getFileDeclarations();
        return fd != null ? fd : FileComponentDeclarations.empty();
    }

    private FileComponentMacros getFileMacros() {
        checkNotInParsingThreadImpl();
        FileComponentMacros fd = currentFileContent.getFileMacros();
        return fd != null ? fd : FileComponentMacros.empty();
    }

    private final AtomicBoolean hasBrokenIncludes;
    private FileComponentIncludes getFileIncludes() {
        checkNotInParsingThreadImpl();
        FileComponentIncludes fd = currentFileContent.getFileIncludes();
        return fd != null ? fd : FileComponentIncludes.empty();
    }

    private FileComponentReferences getFileReferences() {
        FileContent contentImpl = getThreadSensitiveContentImpl();
        FileComponentReferences fd = contentImpl.getFileReferences();
        return fd != null ? fd : FileComponentReferences.empty();
    }

    private FileComponentInstantiations getFileInstantiations() {
        checkNotInParsingThreadImpl();
        FileComponentInstantiations fd = currentFileContent.getFileInstantiations();
        return fd != null ? fd : FileComponentInstantiations.empty();
    }

    private FileContent getThreadSensitiveContentImpl() {
        // in parse context we use current parsing FileContent
        // otherwise currentFileContent
        FileContent contentImpl = getParsingFileContent();
        if (contentImpl == null) {
            contentImpl = currentFileContent;
        }
        return contentImpl;
    }

    private void checkNotInParsingThreadImpl() {
        if (true) {
            return;
        }
        assert getParsingFileContent() == null;
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

    /*package*/static boolean traceFile(CharSequence file) {
        if (TraceFlags.TRACE_FILE_NAME != null) {
            if (TraceFlags.TRACE_FILE_NAME.length() == 0) {
                // trace all files
                return true;
            }
            return file.toString().endsWith(TraceFlags.TRACE_FILE_NAME);
        }
        return false;
    }
    
    public void dumpInfo(PrintWriter printOut) {
        ProjectBase projectImpl = this.getProjectImpl(false);
        printOut.printf("FI: %s, of %s prj=%s disposing=%s (%d)\n\tprjUID=(%d) %s\n\tfileType=%s, hasSnap=%s hasBroken=%s\n", getName(), // NOI18N 
                projectImpl.getClass().getSimpleName(), projectImpl.getName(), projectImpl.isDisposing(), System.identityHashCode(projectImpl), 
                System.identityHashCode(projectUID), projectUID,
                this.fileType, toYesNo(this.fileSnapshot!=null), toYesNo(hasBrokenIncludes()));
        printOut.printf("\tlastParsedTime=%d, lastParsed=%d %s %s\n", this.lastParseTime, this.lastParsed, this.parsingState, this.state);// NOI18N 
        FileBuffer buffer = getBuffer();
        printOut.printf("\tfileBuf=%s lastModified=%d\n", toYesNo(buffer.isFileBased()), buffer.lastModified());// NOI18N 
    }

    public void dumpIndex(PrintWriter printOut) {
        getFileReferences().dump(printOut);
    }

    public void dumpPPStates(PrintWriter printOut) {
        int i = 0;
        final Collection<PreprocessorStatePair> preprocStatePairs = this.getFileContainerOwnPreprocessorStatePairsToDump();
        printOut.printf("Has %d ppStatePairs:\n", preprocStatePairs.size());// NOI18N 
        for (PreprocessorStatePair pair : preprocStatePairs) {
            printOut.printf("----------------Pair[%d]------------------------\n", ++i);// NOI18N 
            printOut.printf("pc=%s\nstate=%s\n", pair.pcState, pair.state);// NOI18N 
        }
        Collection<APTPreprocHandler> preprocHandlers = this.getFileContainerOwnPreprocHandlersToDump();
        printOut.printf("Converted into %d Handlers:\n", preprocHandlers.size());// NOI18N 
        i = 0;
        for (APTPreprocHandler ppHandler : preprocHandlers) {
            printOut.printf("----------------Handler[%d]------------------------\n", ++i);// NOI18N 
            printOut.printf("handler=%s\n", ppHandler);// NOI18N 
        }
    }

    public void dumpIncludePPStates(PrintWriter printOut) {
        int i = 0;
        final Collection<PreprocessorStatePair> preprocStatePairs = this.getFileContainerOwnPreprocessorStatePairsToDump();
        printOut.printf("Has %d OWNED ppStatePairs:\n", preprocStatePairs.size());// NOI18N
        for (PreprocessorStatePair pair : preprocStatePairs) {
            printOut.printf("----------------Own Pair[%d]------------------------\n", ++i);// NOI18N
            printOut.printf("pc=%s\nstate=%s\n", pair.pcState, pair.state);// NOI18N
        }
        Collection<CsmProject> projects = CsmModelAccessor.getModel().projects();
        i = 0;
        for (CsmProject csmProject : projects) {
            if (csmProject instanceof ProjectBase) {
                ProjectBase prj = (ProjectBase) csmProject;
                Map<CsmUID<CsmProject> , Collection<PreprocessorStatePair>> includedStates = prj.getIncludedPreprocStatePairs(this);
                if (includedStates.size() > 1) {
                    printOut.printf("ALARM! the same file %s is included as library of %d projects\n", this.getAbsolutePath(), includedStates.size()); // NOI18N
                }
                for (Map.Entry<CsmUID<CsmProject>, Collection<PreprocessorStatePair>> entry : includedStates.entrySet()) {
                    Collection<PreprocessorStatePair> pairs = entry.getValue();
                    printOut.printf("in project %s included %s\n", prj, UIDUtilities.getProjectName(entry.getKey()));// NOI18N
                    for (PreprocessorStatePair pair : pairs) {
                        printOut.printf("----------------Included Pair[%d]------------------------\n", ++i);// NOI18N
                        printOut.printf("with pc=%s\nstate=%s\n", pair.pcState, pair.state);// NOI18N
                    }
                }
            }
        }
    }

    static String toYesNo(boolean b) {
        return b ? "yes" : "no"; // NOI18N
    }

    private static class SpecialStateImpl implements APTPreprocHandler.State {

        public SpecialStateImpl() {
        }

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

        @Override
        public CharSequence getLanguage() {
            return CharSequences.empty();
        }

        @Override
        public CharSequence getLanguageFlavor() {
            return CharSequences.empty();
        }
    }
}
