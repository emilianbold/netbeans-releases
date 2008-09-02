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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import javax.swing.event.ChangeEvent;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;
import antlr.Parser;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.collections.AST;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;

import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.cache.impl.FileCacheImpl;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.util.NotImplementedException;

/**
 * CsmFile implementations
 * @author Vladimir Kvashin
 */
public class FileImpl implements CsmFile, MutableDeclarationsContainer, 
        Disposable, Persistent, SelfPersistent {
    
    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | TraceFlags.DEBUG;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    private static final boolean reportState = Boolean.getBoolean("parser.log.state");
    
    private static final boolean emptyAstStatictics = Boolean.getBoolean("parser.empty.ast.statistics");

    private static final boolean SKIP_UNNECESSARY_FAKE_FIXES = false;
    
    public static final int UNDEFINED_FILE = 0;
    public static final int SOURCE_FILE = 1;
    public static final int SOURCE_C_FILE = 2;
    public static final int SOURCE_CPP_FILE = 3;
    public static final int HEADER_FILE = 4;

    private FileBuffer fileBuffer;
    
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)  
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;

    /** 
     * It's a map since we need to eliminate duplications 
     */
    private Map<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>> declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
    private final ReadWriteLock  declarationsLock = new ReentrantReadWriteLock();

    private Set<CsmUID<CsmInclude>> includes = createIncludes();
    private final ReadWriteLock includesLock = new ReentrantReadWriteLock();

    private Map<NameSortedKey, CsmUID<CsmMacro>> macros = createMacros();
    private final ReadWriteLock macrosLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    
    private int errorCount = 0;
    
    private enum State {

        /** The file has never been parsed */
	INITIAL,

        /** The file has been completely parsed */
	PARSED, 
        
        /** The file is parsed in one preprocessor state,
            but should be parsed in one or several other states */
        PARTIAL,

        /** The file is modified and needs to be reparsed */
	MODIFIED,

        /** The file is now being parsed */
	BEING_PARSED 
    }
    
    private State state;

    private int fileType = UNDEFINED_FILE;
    
    private final Object stateLock = new Object();
    
    private final Collection<CsmUID<FunctionImplEx>> fakeRegistrationUIDs = new CopyOnWriteArrayList<CsmUID<FunctionImplEx>>();
    
    private long lastParsed = Long.MIN_VALUE;
    
    /** Cache the hash code */
    private int hash; // Default to 0
    
    /** 
     * Stores the UIDs of the static functions declarations (not definitions) 
     * This is necessary for finding definitions/declarations 
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    private final Collection<CsmUID<CsmFunction>> staticFunctionDeclarationUIDs = new ArrayList<CsmUID<CsmFunction>>();
    private final Collection<CsmUID<CsmVariable>> staticVariableUIDs = new ArrayList<CsmUID<CsmVariable>>();
    private final ReadWriteLock  staticLock = new ReentrantReadWriteLock();
    
    private List<CsmReference> lastMacroUsages;
    
    private ChangeListener fileBufferChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
            FileImpl.this.markReparseNeeded(false);
        }        
    };
    
    /** For test purposes only */
    public interface Hook {
	void parsingFinished(CsmFile file, APTPreprocHandler preprocHandler);
    }
    
    private static Hook hook = null;
    
    public FileImpl(FileBuffer fileBuffer, ProjectBase project, int fileType, NativeFileItem nativeFileItem) {
	state = State.INITIAL;
        setBuffer(fileBuffer);
        this.projectUID = UIDCsmConverter.projectToUID(project);
        this.projectRef = null;
        this.fileType = fileType;
        if (nativeFileItem != null){
            project.putNativeFileItem(getUID(), nativeFileItem);
        }
        Notificator.instance().registerNewFile(this);
    }
    
    /** For test purposes only */
    public static void setHook(Hook aHook) {
	hook = aHook;
    }
    
    public final NativeFileItem getNativeFileItem() {
        return getProjectImpl(true).getNativeFileItem(getUID());
    }
    private ProjectBase _getProject(boolean assertNotNull) {
        projectLock.readLock().lock();
        try {
            ProjectBase prj = this.projectRef;
            if (prj == null) {
                prj = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                if (assertNotNull) {
                    assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
                }
            }
            return prj;
        } finally {
            projectLock.readLock().unlock();
        }
    }
    
    public boolean isSourceFile(){
        return fileType == SOURCE_FILE || fileType == SOURCE_C_FILE || fileType == SOURCE_CPP_FILE;
    }
    
    public boolean isCppFile(){
        return fileType == SOURCE_CPP_FILE;
    }
    
    /*package local*/ void setSourceFile(){
        if (!(fileType == SOURCE_C_FILE || fileType == SOURCE_CPP_FILE)) {
            fileType = SOURCE_FILE;
        }
    }

    public boolean isHeaderFile(){
        return fileType == HEADER_FILE;
    }

    /*package local*/ void setHeaderFile(){
        if (fileType == UNDEFINED_FILE) {
            fileType = HEADER_FILE;
        }
    }
    
    // TODO: consider using macro map and __cplusplus here instead of just checking file name
    public APTLanguageFilter getLanguageFilter(APTPreprocHandler.State ppState) {
        FileImpl startFile = ppState == null ? null : ProjectBase.getStartFile(ppState);
        if (startFile != null && startFile != this) {
            return startFile.getLanguageFilter(null);
        } else {
            String lang;
            if (fileType == SOURCE_CPP_FILE) {
                lang = APTLanguageSupport.GNU_CPP;
            } else if (fileType == SOURCE_C_FILE) {
                lang = APTLanguageSupport.GNU_C;
            } else {
                lang  = APTLanguageSupport.GNU_CPP;
                String name =  getName().toString();
                if (name.length() > 2 && name.endsWith(".c")) { // NOI18N
                    lang = APTLanguageSupport.GNU_C;
                }
            }
            return APTLanguageSupport.getInstance().getFilter(lang);
        }
    }
    
    @Deprecated
public APTPreprocHandler getPreprocHandler() {
        return getProjectImpl(true)==null ? null : getProjectImpl(true).getPreprocHandler(fileBuffer.getFile());
    }
    
    public Collection<APTPreprocHandler> getPreprocHandlers() {
        throw new NotImplementedException();
    }
    
    public void setBuffer(FileBuffer fileBuffer) {
        synchronized (changeStateLock) {
            if( this.fileBuffer != null ) {
                this.fileBuffer.removeChangeListener(fileBufferChangeListener);
            }
            this.fileBuffer = fileBuffer;
            if( state != State.INITIAL ) {
                state = State.MODIFIED;
            }
            this.fileBuffer.addChangeListener(fileBufferChangeListener);
        }
    }
    
    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }

    public void ensureParsed(Collection<APTPreprocHandler> handlers) {
	synchronized( stateLock ) {
	    switch( state ) {
		case INITIAL:
                case PARTIAL:
                    state = State.BEING_PARSED;
                    try {
                        for (APTPreprocHandler preprocHandler : handlers) {
                            _parse(preprocHandler);
                            if (state == State.MODIFIED) {
                                break; // does not make sense parsing old data
                            }
                        }
                    } finally {
                        synchronized (changeStateLock) {
                            if (state == State.BEING_PARSED) {
                                state = State.PARSED;
                            }  // if not, someone marked it with new state
                        }
                        stateLock.notifyAll();
                    }
		    if( TraceFlags.DUMP_PARSE_RESULTS ) new CsmTracer().dumpModel(this);
		    break;
		case MODIFIED:
                    state = State.BEING_PARSED;
                    boolean first = true;
                    try {
                        for (APTPreprocHandler preprocHandler : handlers) {
                            if (first) {
                                _reparse(preprocHandler);
                                first = false;
                            } else {
                                _parse(preprocHandler);
                            }
                            if (state == State.MODIFIED) {
                                break; // does not make sense parsing old data
                            }
                        }
                    } finally {
                        synchronized (changeStateLock) {
                            if (state == State.BEING_PARSED) {
                                state = State.PARSED;
                            } // if not, someone marked it with new state
                        }
                        stateLock.notifyAll();
                    }
		    if( TraceFlags.DUMP_PARSE_RESULTS || TraceFlags.DUMP_REPARSE_RESULTS ) new CsmTracer().dumpModel(this);
		    break;
		case PARSED:
		    break;
	    }
	}
    }   
    
    public boolean validate() {
	synchronized (changeStateLock) {
	    if( state == State.PARSED ) {
		long lastModified = getBuffer().lastModified();
		if( lastModified > lastParsed ) {
		    if( TraceFlags.TRACE_VALIDATION ) System.err.printf("VALIDATED %s\n\t lastModified=%d\n\t   lastParsed=%d\n", getAbsolutePath(), lastModified, lastParsed);
		    state = State.MODIFIED;
		    return false;
		}
	    }
	    return true;
	}
    }
    
    private final Object changeStateLock = new Object();

    public void markReparseNeeded(boolean invalidateCache) {
        synchronized (changeStateLock) {
	    if( state != State.INITIAL ) {
		state = State.MODIFIED;
	    }
            if (invalidateCache) {
                synchronized (tokStreamLock) {
                   ref = null;
                }                
                if (TraceFlags.USE_AST_CACHE) {
                    CacheManager.getInstance().invalidate(this);
                } else {
                    APTDriver.getInstance().invalidateAPT(this.getBuffer());
                }
            }
        }
    }
    
    public void markMoreParseNeeded() {
        synchronized (changeStateLock) {
            switch (state) {
                case BEING_PARSED:
                case PARSED:
                    state = State.PARTIAL;
                case INITIAL:
                case MODIFIED:
                case PARTIAL:
                    // nothing
            }
        }
    }
    
    public int getErrorCount() {    
        return errorCount;
    }

    /** 
     * sometimes called externally
     * by some (cached) project implementations, etc
     */
    public void render(AST tree) {
        new AstRenderer(this).render(tree);
    }
        
    private void _reparse(APTPreprocHandler preprocHandler) {
        if (! ParserThreadManager.instance().isParserThread() && ! ParserThreadManager.instance().isStandalone()) {
            String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
            Diagnostic.trace(text);
            new Throwable(text).printStackTrace(System.err);
        }
        if( TraceFlags.DEBUG ) Diagnostic.trace("------ reparsing " + fileBuffer.getFile().getName()); // NOI18N
	//Notificator.instance().startTransaction();
	try {
            _clearIncludes();
            _clearMacros();
            AST ast = doParse(preprocHandler);
            if (ast != null) {
                disposeAll(false);
                render(ast);
            } else {
                //System.err.println("null ast for file " + getAbsolutePath());
            }
	}
	finally {
	    //Notificator.instance().endTransaction();
            // update this file and it's project     
            RepositoryUtils.put(this);
            if (TraceFlags.USE_DEEP_REPARSING) {
                getProjectImpl(true).getGraph().putFile(this);
            }
            Notificator.instance().registerChangedFile(this);
            Notificator.instance().flush();
	}
	    
    }

    public void dispose() {
        onDispose();
        Notificator.instance().registerRemovedFile(this);
	disposeAll(true);
    }
    
    public void onProjectClose(){
        onDispose();
    }
    
    private void onDispose() {
        projectLock.writeLock().lock();
        try {
            if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
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
            uids = declarations.values();
            declarations = new TreeMap<OffsetSortedKey, CsmUID<CsmOffsetableDeclaration>>();
        }   finally {
            declarationsLock.writeLock().unlock();
        }

        if (clearNonDisposable) {
            _clearIncludes();
            _clearMacros();
        }
        Collection<CsmOffsetableDeclaration> arr = UIDCsmConverter.UIDsToDeclarations(uids);
        Utils.disposeAll(arr);          
        RepositoryUtils.remove(uids);
    }
        
    private void _clearMacros() {
        Collection<CsmUID<CsmMacro>> copy = macros.values();
        macros = createMacros();
        RepositoryUtils.remove(copy);
    }
    
    private Map<NameSortedKey, CsmUID<CsmMacro>> createMacros() {
        return new TreeMap<NameSortedKey, CsmUID<CsmMacro>>();
    }
    
    private void _clearIncludes() {
        try {
            includesLock.writeLock().lock();
            RepositoryUtils.remove(includes);
            includes = createIncludes();
        } finally {
            includesLock.writeLock().unlock();
        }
    }
    
    private Set<CsmUID<CsmInclude>> createIncludes() {
        return new TreeSet<CsmUID<CsmInclude>>(UID_START_OFFSET_COMPARATOR);
    }
    
    /** for debugging/tracing purposes only */
    public AST debugParse() {
        synchronized( stateLock ) {
            return _parse(getPreprocHandler());
        }
    }
    
    private AST _parse(APTPreprocHandler preprocHandler) {
        
        if (reportErrors) {
	    if (! ParserThreadManager.instance().isParserThread()  && ! ParserThreadManager.instance().isStandalone()) {
		String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
		Diagnostic.trace(text);
		new Throwable(text).printStackTrace(System.err);
	    }
        }        
	
	Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
	
        try {
            AST ast = doParse((preprocHandler == null) ?  getPreprocHandler() : preprocHandler);
            if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) sw.stopAndReport("Parsing of " + fileBuffer.getFile().getName() + " took \t"); // NOI18N
            
            if( ast != null ) {
                Diagnostic.StopWatch sw2 = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
                //Notificator.instance().startTransaction();
		if( isValid() ) {   // FIXUP: use a special lock here
		    render(ast);
		    if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) sw2.stopAndReport("Rendering of " + fileBuffer.getFile().getName() + " took \t"); // NOI18N
		}
                return ast;
            }
        } finally {
            if (isValid()) {   // FIXUP: use a special lock here
                RepositoryUtils.put(this);
            }
            if (TraceFlags.USE_DEEP_REPARSING && isValid()) {	// FIXUP: use a special lock here
                getProjectImpl(true).getGraph().putFile(this);
            }
            if( isValid() ) {   // FIXUP: use a special lock here
		Notificator.instance().registerChangedFile(this);
		Notificator.instance().flush();
	    }
	    else {
		// FIXUP: there should be a notificator per project instead!
		Notificator.instance().reset();
	    }
        }
        return null;
    }

    private TokenStream createFullTokenStream() {
        APTPreprocHandler preprocHandler = getPreprocHandler();
        APTFile apt = null;
        if (TraceFlags.USE_AST_CACHE) {
            apt = CacheManager.getInstance().findAPT(this);
        } else {
            try {
                apt = APTDriver.getInstance().findAPT(fileBuffer);
            } catch (IOException ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
        if (apt == null) {
            return null;
        }
        APTPreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = ProjectBase.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while getting TS of file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
            return null;
        }
        APTParseFileWalker walker = new APTParseFileWalker(startProject, apt, this, preprocHandler);
        return walker.getFilteredTokenStream(getLanguageFilter(ppState));        
    }
    
    private final String tokStreamLock = new String("TokenStream lock"); // NOI18N
    private Reference<OffsetTokenStream> ref = new SoftReference<OffsetTokenStream>(null);
    
    public TokenStream getTokenStream(int startOffset, int endOffset) {
        try {
            OffsetTokenStream stream;
            synchronized (tokStreamLock) {
                stream = ref != null ? ref.get() : null;
                ref = new SoftReference<OffsetTokenStream>(null);
            }
            if (stream == null || stream.getStartOffset() > startOffset) {
                if (stream == null) {
//                    System.err.println("new stream created for " + startOffset);
                } else {
//                    System.err.println("new stream created, because prev stream was finished on " + stream.getStartOffset() + " now asked for " + startOffset);
                }
                stream = new OffsetTokenStream(createFullTokenStream());
            } else {
//                System.err.println("use cached stream finished previously on " + stream.getStartOffset() + " now asked for " + startOffset);
            }
            stream.moveTo(startOffset, endOffset);
            return stream;
        } catch (TokenStreamException ex) {
            Utils.LOG.severe("Can't create compound statement: " + ex.getMessage());
            DiagnosticExceptoins.register(ex);
            return null;
        }
    }
    
    public void releaseTokenStream(TokenStream ts) {
        if (ts instanceof OffsetTokenStream) {
            OffsetTokenStream offsTS = (OffsetTokenStream)ts;
            synchronized (tokStreamLock) {
                if (ref != null && ref.get() == null) {
                    ref = new SoftReference<OffsetTokenStream>(offsTS);
//                    System.err.println("caching stream finished on " + offsTS.getStartOffset());                    
                }
            }
        }
    }
    
    private static class OffsetTokenStream implements TokenStream {

        private final TokenStream stream;
        private Token next;
        private int endOffset;

        public OffsetTokenStream(TokenStream stream) {
            this.stream = stream;
        }

        public Token nextToken() throws TokenStreamException {
            Token out = next;
            
            if (out == null || out.getType() == CPPTokenTypes.EOF ||
                  (((APTToken)out).getOffset() > endOffset)  ) {
                out = APTUtils.EOF_TOKEN;
            } else {
                next = stream.nextToken();
            }
            return out;
        }
        
        public int getStartOffset() {
            return next == null || (next.getType() == CPPTokenTypes.EOF) ? Integer.MAX_VALUE : ((APTToken)next).getOffset();
        }
        
        public void moveTo(int startOffset, int endOffset) throws TokenStreamException {
            this.endOffset = endOffset;
            assert this.endOffset >= startOffset;
            for (next = stream.nextToken(); next != null && next.getType() != CPPTokenTypes.EOF; next = stream.nextToken()) {
                assert (next instanceof APTToken) : "we have only APTTokens in token stream";
                int currOffset = ((APTToken) next).getOffset();
                if (currOffset == startOffset) {
                    break;
                }
            }            
        }
    };
    
    
    /** For text purposes only */
    public interface ErrorListener {
        void error(String text, int line, int column);
    }
    
    /** For text purposes only */
    public void getErrors(ErrorListener errorListener) {
        Collection<RecognitionException> errors = new ArrayList<RecognitionException>();
        getErrors(errors);
        for (RecognitionException e : errors) {
            errorListener.error(e.getMessage(), e.getLine(), e.getColumn());
        }
    }

    private static class ParserBasedTokenBuffer implements ReadOnlyTokenBuffer {
        Parser parser;
        public ParserBasedTokenBuffer(Parser parser) {
            this.parser = parser;
        }
        public int LA(int i) {
            return parser.LA(i);
        }
        public Token LT(int i) {
            return parser.LT(i);
        }
    }
    
    public ReadOnlyTokenBuffer getErrors(final Collection<RecognitionException> result) {
        CPPParserEx.ErrorDelegate delegate = new CPPParserEx.ErrorDelegate() {
            public void onError(RecognitionException e) {
                result.add(e);
            }
        };
        // FIXUP (up to the end of the function)
        // should be changed with setting appropriate flag and using common parsing mechanism
        // (Now doParse performs too many actions that should NOT be performed if parsing just for getting errors;
        // making this actions conditional will make doParse code spaghetty-like. That's why I use this fixup)
        // Another issue to be solved is threading and cancellation
        if( TraceFlags.TRACE_ERROR_PROVIDER ) System.err.printf("\n\n>>> Start parsing (getting errors) %s \n", getName());
        long time = TraceFlags.TRACE_ERROR_PROVIDER ? System.currentTimeMillis() : 0;
        APTPreprocHandler preprocHandler = getPreprocHandler();
        APTPreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = ProjectBase.getStartProject(ppState);
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if( ! TraceFlags.TRACE_ERROR_PROVIDER ) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }
        try {
            APTFile aptFull = APTDriver.getInstance().findAPT(this.getBuffer());
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFull, this, preprocHandler);
            CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile().getName(), walker.getFilteredTokenStream(getLanguageFilter(ppState)), flags);
            parser.setErrorDelegate(delegate);
            parser.setLazyCompound(false);
            parser.translation_unit();
            return new ParserBasedTokenBuffer(parser);
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
            return null;
        } catch (Error ex){
            System.err.println(ex.getClass().getName()+" at parsing file "+fileBuffer.getFile().getAbsolutePath()); // NOI18N
            throw ex;
        } finally {
            if( TraceFlags.TRACE_ERROR_PROVIDER ) System.err.printf("<<< Done parsing (getting errors) %s %d ms\n\n\n", getName(), System.currentTimeMillis() - time);
        }
    }
    
    private AST doParse(APTPreprocHandler preprocHandler) {
//        if( "cursor.hpp".equals(fileBuffer.getFile().getName()) ) {
//            System.err.println("cursor.hpp");
//        }  
        if( reportParse || TraceFlags.DEBUG ) {
            APTPreprocHandler.State state = preprocHandler.getState();
            System.err.printf("# APT-based AST-cached Parsing %s (valid=%b, compile-context=%b) (Thread=%s)\n", fileBuffer.getFile().getPath(),
                    state.isValid(), state.isCompileContext(), Thread.currentThread().getName());
            if (reportState) {
                System.err.printf("%s\n\n", preprocHandler.getState());
            }
        }
        
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if( ! reportErrors ) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }

        APTPreprocHandler.State oldState = preprocHandler.getState();

        // 1. get cache with AST
        // 2a if cache has AST => use AST and APTLight
        // 2b otherwise if cache has APT full => use APT full to generate parser's
        //     token stream and save in cache
        AST ast = null;
        APTFile aptLight = null;
        APTFile aptFull = null;
        if (TraceFlags.USE_AST_CACHE) {
            FileCache cacheWithAST = CacheManager.getInstance().findCacheWithAST(this, preprocHandler);
            assert (cacheWithAST != null);
            ast  = cacheWithAST.getAST(preprocHandler);
            aptLight = cacheWithAST.getAPTLight();
            aptFull = cacheWithAST.getAPT();        
        } else {
            try {
                aptFull = APTDriver.getInstance().findAPT(this.getBuffer());
            } catch(FileNotFoundException ex){
                APTUtils.LOG.log(Level.WARNING, "FileImpl: file {0} not found", new Object[] {getBuffer().getFile().getAbsolutePath()});// NOI18N
		DiagnosticExceptoins.register(ex);
            } catch (IOException ex) {
		DiagnosticExceptoins.register(ex);
            }
        }
        if (ast != null) {
            if (TraceFlags.TRACE_CACHE) {
                System.err.println("CACHE: parsing using AST and APTLight for " + getAbsolutePath());
            }             
            // use light for visiting and return ast as result
            assert (aptLight != null);
            boolean skip = TraceFlags.CACHE_SKIP_APT_VISIT;
            if (!skip) {
                APTParseFileWalker walker = new APTParseFileWalker(ProjectBase.getStartProject(preprocHandler.getState()), aptLight, this, preprocHandler);
                walker.addMacroAndIncludes(true);
                walker.visit();          
            } else {
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: skipped APTLight visiting");
                }
            }
        } else if (aptFull != null) {
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
            // We don't need to remember conditional state here - we do this in ProjectBase.onInclude
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFull, this, preprocHandler);
            walker.addMacroAndIncludes(true);
            if (TraceFlags.DEBUG) {
                System.err.println("doParse " + getAbsolutePath() + " with " + ParserQueue.tracePreprocState(oldState));
            }
            clearFakeRegistrations();
            CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile().getName(), walker.getFilteredTokenStream(getLanguageFilter(ppState)), flags);
            long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
            try {
                parser.translation_unit();
            } catch (Error ex){
                System.err.println(ex.getClass().getName()+" at parsing file "+fileBuffer.getFile().getAbsolutePath()); // NOI18N
                throw ex;
            }
            
            if( emptyAstStatictics ) {
                time = System.currentTimeMillis() - time;
                System.err.println("PARSED FILE " + getAbsolutePath() + (AstUtil.isEmpty(parser.getAST(), true) ? " EMPTY" : "") + ' ' + time + " ms");
            }
            if( TraceFlags.DUMP_AST ) {
                System.err.println("\n");
                System.err.print("AST: ");
                System.err.print(getAbsolutePath());
                System.err.print(' ');
                AstUtil.toStream(parser.getAST(), System.err);
                System.err.println("\n");

            }
            errorCount = parser.getErrorCount();
            ast = parser.getAST();            
            // save all in cache
            if (state != State.MODIFIED) {
                if (TraceFlags.USE_AST_CACHE) {
                    if (getBuffer().isFileBased() && !TraceFlags.CACHE_SKIP_SAVE) {
                        CacheManager.getInstance().saveCache(this, new FileCacheImpl(aptLight, aptFull, ast));
                    } else {
                        if (TraceFlags.TRACE_CACHE) {
                            System.err.println("CACHE: not save cache for document based file " + getAbsolutePath());
                        }
                    }
                }
            } else {
                ast = null;
                if (TraceFlags.TRACE_CACHE) {
                    System.err.println("CACHE: not save cache for file modified during parsing" + getAbsolutePath());
                }
            }
        }
	lastParsed = Math.max(System.currentTimeMillis(), fileBuffer.lastModified());
        lastMacroUsages = null;
	if( TraceFlags.TRACE_VALIDATION ) System.err.printf("PARSED    %s \n\tlastModified=%d\n\t  lastParsed=%d  diff=%d\n", 
		getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified()-lastParsed);
	Hook aHook = hook;
	if( aHook != null ) {
	    aHook.parsingFinished(this, preprocHandler);
	}
        return ast;
    }
    
    public List<CsmReference> getLastMacroUsages(){
        List<CsmReference> res = lastMacroUsages;
        if (res != null) {
            return new ArrayList<CsmReference>(res);
        }
        return res;
    }

    public void setLastMacroUsages(List<CsmReference> res){
        lastMacroUsages = new ArrayList<CsmReference>(res);
    }

    public long getLastParsedTime(){
        return lastParsed;
    }
    
    @SuppressWarnings("unchecked")
    public void addInclude(IncludeImpl includeImpl) {
        CsmUID<CsmInclude> inclUID = RepositoryUtils.put(includeImpl);
        assert inclUID != null;
        try {
            includesLock.writeLock().lock();
            includes.add(inclUID);
        } finally {
            includesLock.writeLock().unlock();
        }
    }
    
    public static final Comparator<CsmOffsetable> START_OFFSET_COMPARATOR = new Comparator<CsmOffsetable>() {
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
        public int compare(CsmUID o1, CsmUID o2) {
            if (o1 == o2) {
                return 0;
            }
            Comparable<CsmUID> i1 = (Comparable<CsmUID>)o1;
            assert i1 != null;
            return i1.compareTo(o2);
        }   
    };
    
    public String getText(int start, int end) {
        try {
            return fileBuffer.getText(start, end);
        }
        catch( IOException e ) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }

    public String getText() {
        try {
            return fileBuffer.getText();
        }
        catch( IOException e ) {
            DiagnosticExceptoins.register(e);
            return "";
        }
    }
    
    public CsmProject getProject() {
        return _getProject(false);
    }

    /** Just a convenient shortcut to eliminate casts */
    public ProjectBase getProjectImpl(boolean assertNotNull) {
        return _getProject(assertNotNull);
    }

    public CharSequence getName() {
        return CharSequenceKey.create(fileBuffer.getFile().getName());
    }

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

    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        if (!SKIP_UNNECESSARY_FAKE_FIXES) {
            fixFakeRegistrations();
        }
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
        if (!SKIP_UNNECESSARY_FAKE_FIXES) {
            fixFakeRegistrations();
        }
        Iterator<CsmOffsetableDeclaration> out;
        try {
            declarationsLock.readLock().lock();
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations.values(), filter);
         } finally {
            declarationsLock.readLock().unlock();
         }
         return out;
    }

    @SuppressWarnings("unchecked")
    public void addMacro(CsmMacro macro) {
        CsmUID<CsmMacro> macroUID = RepositoryUtils.put(macro);
        assert macroUID != null;
        try {
            macrosLock.writeLock().lock();
            macros.put(new NameSortedKey(macro), macroUID);
        } finally {
            macrosLock.writeLock().unlock();
        }
    }
    
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

    public Collection<CsmUID<CsmMacro>> findMacroUids(String name) {
        Collection<CsmUID<CsmMacro>> uids = new ArrayList<CsmUID<CsmMacro>>(2);
        NameSortedKey from = NameSortedKey.getStartKey(name);
        NameSortedKey to = NameSortedKey.getEndKey(name);
        try {
            macrosLock.readLock().lock();
            for (Map.Entry<NameSortedKey, CsmUID<CsmMacro>> entry : ((TreeMap<NameSortedKey, CsmUID<CsmMacro>>)macros).subMap(from, to).entrySet()){
                uids.add(entry.getValue());
            }
        } finally {
            macrosLock.readLock().unlock();
        }
        return uids;
    }

    @SuppressWarnings("unchecked")
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uidDecl = RepositoryUtils.put(decl);
        try {
            declarationsLock.writeLock().lock();
            declarations.put(getOffsetSortKey(decl), uidDecl);
        } finally {
            declarationsLock.writeLock().unlock();
        }
        // TODO: remove this dirty hack!
	if( decl instanceof VariableImpl ) {
            VariableImpl v = (VariableImpl) decl;
	    if( ! NamespaceImpl.isNamespaceScope(v, true) ) {
		v.setScope(this);
                addStaticVariableDeclaration(uidDecl);
	    }
	}
        if( CsmKindUtilities.isFunctionDeclaration(decl) ) {
            FunctionImpl fi = (FunctionImpl) decl;
            if( ! NamespaceImpl.isNamespaceScope(fi) ) {
                fi.setScope(this);
                addStaticFunctionDeclaration(uidDecl);
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

    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        _removeDeclaration(declaration);
    }
    
    private void _removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uidDecl;
        try {
            declarationsLock.writeLock().lock();
            uidDecl = declarations.remove(getOffsetSortKey(declaration));
        } finally {
            declarationsLock.writeLock().unlock();
        }
        RepositoryUtils.remove(uidDecl);
        // update repository
        RepositoryUtils.put(this);
    }
    
    private OffsetSortedKey getOffsetSortKey(CsmOffsetableDeclaration declaration) {
        return new OffsetSortedKey(declaration);
    }

    private NameSortedKey getOffsetSortKey(CsmMacro macro) {
        return new NameSortedKey(macro);
    }
    
    public String getAbsolutePath() {
        return fileBuffer.getFile().getAbsolutePath();
    }
    
    public File getFile() {
	return fileBuffer.getFile();
    }

    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
        //TODO: add static functions
        for( Iterator iter = getDeclarations().iterator(); iter.hasNext(); ) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            // TODO: remove this dirty hack!
            if( decl instanceof VariableImpl ) {
                VariableImpl v = (VariableImpl) decl;
                if( ! NamespaceImpl.isNamespaceScope(v, true) ) {
                    l.add(v);
                }
            }
        }
        return l;
    }
    
    public boolean isValid() {
        CsmProject project = _getProject(false);
        return project != null && project.isValid();    
    }

    public boolean isParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED;
        }
    }

    public boolean isParsingOrParsed() {
        synchronized (changeStateLock) {
            return state == State.PARSED || state == State.BEING_PARSED;
        }
    }
    
    public void scheduleParsing(boolean wait) throws InterruptedException {
        scheduleParsing(wait, null);
    }
  
    public void scheduleParsing(boolean wait, APTPreprocHandler.State ppState) throws InterruptedException {
        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("> File " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
        boolean fixFakes = false;
        synchronized( stateLock ) {
            //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  sync " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
            if (SKIP_UNNECESSARY_FAKE_FIXES) {
                if (isParsed()) {
                    fixFakes = wait;
                } else {
                    while( ! isParsed() ) {
                        ParserQueue.instance().add(this, ppState, ParserQueue.Position.HEAD);
                        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  !prs " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
                        if( wait ) {
                            stateLock.wait();
                        }
                        else {
                            return;
                        }
                        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("< wait " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
                    }
                }
            } else {
                while( ! isParsed() ) {
                    ParserQueue.instance().add(this, ppState, ParserQueue.Position.HEAD);
                    //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  !prs " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
                    if( wait ) {
                        stateLock.wait();
                    }
                    else {
                        return;
                    }
                    //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("< wait " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
                }
            }
        }
        if (SKIP_UNNECESSARY_FAKE_FIXES && fixFakes) {
            fixFakeRegistrations();
        }
        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("< File " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
    }    
    
    public void onFakeRegisration(FunctionImplEx decl) {
        CsmUID<FunctionImplEx> uidDecl = UIDCsmConverter.declarationToUID(decl);
        fakeRegistrationUIDs.add(uidDecl);
    }
    
    private void clearFakeRegistrations() {
        fakeRegistrationUIDs.clear();
    }
    
    public void fixFakeRegistrations() {
        if (fakeRegistrationUIDs.size() == 0 || !isValid()) {
            return;
        }
        if (fakeRegistrationUIDs.size() > 0) {
            List<CsmUID<FunctionImplEx>> fakes = new ArrayList<CsmUID<FunctionImplEx>>(fakeRegistrationUIDs);
            fakeRegistrationUIDs.clear();
            for( CsmUID<? extends CsmDeclaration> fakeUid : fakes ) {
                CsmDeclaration curElem = fakeUid.getObject();
                if (curElem != null) {
                    if( curElem instanceof FunctionImplEx ) {
                        ((FunctionImplEx) curElem).fixFakeRegistration();
                    } else {
                        DiagnosticExceptoins.register(new Exception("Incorrect fake registration class: " + curElem.getClass())); // NOI18N
                    }
                }
            }
        }
    }
    
    public @Override String toString() {
	return "FileImpl @" + hashCode() + ' ' + getAbsolutePath(); // NOI18N
    }

    public CsmUID<CsmFile> getUID() {
        if (uid == null) {
            uid = UIDUtilities.createFileUID(this);
        }
        return uid;
    }
    private CsmUID<CsmFile> uid = null;   
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    public void write(DataOutput output) throws IOException {
        PersistentUtils.writeBuffer(this.fileBuffer, output);
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
        } finally {
            includesLock.readLock().unlock();
        }
        try {
            macrosLock.readLock().lock();
            factory.writeNameSortedToUIDMap(this.macros, output, false);
        } finally {
            macrosLock.readLock().unlock();
        }
        factory.writeUIDCollection(this.fakeRegistrationUIDs, output, false);
        //output.writeUTF(state.toString());
        output.writeInt(fileType);
        
        // not null UID
        assert this.projectUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
	output.writeLong(lastParsed);
	output.writeUTF(state.toString());
        try {
            staticLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticFunctionDeclarationUIDs, output, false);
            UIDObjectFactory.getDefaultFactory().writeUIDCollection(staticVariableUIDs, output, false);
        } finally {
            staticLock.readLock().unlock();
        }
    }
    
    @SuppressWarnings("unchecked")
    public FileImpl(DataInput input) throws IOException {
        this.fileBuffer = PersistentUtils.readBuffer(input);
        
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();        
        factory.readOffsetSortedToUIDMap(this.declarations, input, null);
        factory.readUIDCollection(this.includes, input);
        factory.readNameSortedToUIDMap(this.macros, input, null);
        factory.readUIDCollection(this.fakeRegistrationUIDs, input);
        //state = State.valueOf(input.readUTF());
        fileType = input.readInt();

        this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.projectUID != null;
        this.projectRef = null;
        
        assert fileBuffer != null;
        assert fileBuffer.isFileBased();
	lastParsed = input.readLong();
        state = State.valueOf(input.readUTF());
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticFunctionDeclarationUIDs, input);
        UIDObjectFactory.getDefaultFactory().readUIDCollection(staticVariableUIDs, input);
    }

    public @Override int hashCode() {
	if( hash == 0 ) {   // we don't need sync here - at worst, we'll calculate the same value twice
	    String identityHashPath = getProjectImpl(true).getUniqueName() + "*" + getAbsolutePath(); // NOI18N
	    hash = identityHashPath.hashCode();
	}
        return hash;
    }

    public @Override boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FileImpl)) {
            return false;
        }
	if( obj == this ) {
	    return true;
	}
	FileImpl other = (FileImpl) obj;
	if( this.getAbsolutePath().equals(other.getAbsolutePath()) ) {
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
            if ( text.charAt(offset) == '\n') {
                curLine++;
            }
        }
        // check line
        if (curLine < line) {
            throw new IllegalStateException("no line with index " + line + " in file " + getAbsolutePath()); // NOI18N
        }
        int outOffset = offset + (column - 1);
        // check that column is valid: not on the next line
        if (text.length() < outOffset || (text.substring(offset, outOffset).indexOf('\n') >= 0))  { // NOI18N
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
        int[] lineCol = new int[] { 1, 1 };
        String text = getText();
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
            if ( curChar == '\n') {
                // just increase line number
                lineCol[0] = lineCol[0]+1;
                lineCol[1] = 1;
            } else if (curChar == '\t') {
                int col = lineCol[1];
                int newCol = ( ((col-1)/TABSIZE) + 1) * TABSIZE + 1;         
                lineCol[1] = newCol;
            } else {
                lineCol[1]++;
            }
        }        
        return lineCol;
    }

    public static class OffsetSortedKey implements Comparable<OffsetSortedKey>, Persistent, SelfPersistent {
        private int start = 0;
        private CharSequence name;
        private OffsetSortedKey(CsmOffsetableDeclaration declaration){
            start = ((CsmOffsetable) declaration).getStartOffset();
            name = declaration.getName();
        }
        public int compareTo(OffsetSortedKey o) {
            int res = start - o.start;
            if (res == 0){
                res = CharSequenceKey.Comparator.compare(name, o.name);
            }
            return res;
        }

        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            output.writeUTF(name.toString());
        }
        
        public OffsetSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = NameCache.getManager().getString(input.readUTF());
        }
    }

    public static class NameSortedKey implements Comparable<NameSortedKey>, Persistent, SelfPersistent {
        private int start = 0;
        private CharSequence name;
        private NameSortedKey(CsmMacro macro){
            this(macro.getName(), macro.getStartOffset());
        }
        private NameSortedKey(CharSequence name, int start){
            this.start = start;
            this.name = name;
        }
        public int compareTo(NameSortedKey o) {
            int res = CharSequenceKey.Comparator.compare(name, o.name);
            if (res == 0){
                res = start - o.start;
            }
            return res;
        }

        public static NameSortedKey getStartKey(CharSequence name){
            return new NameSortedKey(CharSequenceKey.create(name),0);
        }

        public static NameSortedKey getEndKey(CharSequence name){
            return new NameSortedKey(CharSequenceKey.create(name),Integer.MAX_VALUE);
        }
        
        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            output.writeUTF(name.toString());
        }
        
        public NameSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = NameCache.getManager().getString(input.readUTF());
        }
    }
}
