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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.cache.impl.FileCacheImpl;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.GuardBlockWalker;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.LazyCsmCollection;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 * CsmFile implementations
 * @author Vladimir Kvashin
 */
public class FileImpl implements CsmFile, MutableDeclarationsContainer, 
        ChangeListener, Disposable, Persistent, SelfPersistent {
    
    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | TraceFlags.DEBUG;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    
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
    private Map<SortedKey, CsmUID<CsmOffsetableDeclaration>> declarations = new TreeMap<SortedKey, CsmUID<CsmOffsetableDeclaration>>();
    private ReadWriteLock  declarationsLock = new ReentrantReadWriteLock();

    private Set<CsmUID<CsmInclude>> includes = createIncludes();
    private ReadWriteLock includesLock = new ReentrantReadWriteLock();

    private Set<CsmUID<CsmMacro>> macros = createMacros();
    private ReadWriteLock macrosLock = new ReentrantReadWriteLock();
    
    private int errorCount = 0;
    
    private enum State { 
	INITIAL, 
	PARSED, 
	MODIFIED, 
	BEING_PARSED 
    }
    
    private State state;

    private int fileType = UNDEFINED_FILE;
    
    private Object stateLock = new Object();
    
    private Collection<FunctionImplEx> fakeRegistrationsOLD = new ArrayList<FunctionImplEx>();
    private Collection<CsmUID<FunctionImplEx>> fakeRegistrationUIDs = new CopyOnWriteArrayList<CsmUID<FunctionImplEx>>();
    
    // TODO: move this field and correspondent logic to FileContainer.MyFile
    private final GuardBlockState guardState;
    
    private long lastParsed = Long.MIN_VALUE;
    
    /** Cache the hash code */
    private int hash; // Default to 0
    
    /** 
     * Stores the UIDs of the static functions declarations (not definitions) 
     * This is necessary for finding definitions/declarations 
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    private Collection<CsmUID<CsmFunction>> staticFunctionDeclarationUIDs;
    
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
        this.guardState = new GuardBlockState();
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
        return getProjectImpl().getNativeFileItem(getUID());
    }
    
    private ProjectBase _getProject(boolean assertNotNull) {
        ProjectBase prj = this.projectRef;
        if (prj == null) {
            prj = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            if( assertNotNull ) {
                assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
            }
        }
        return prj;
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
    public APTLanguageFilter getLanguageFilter() {        
        String lang  = APTLanguageSupport.GNU_CPP;
        String name =  getName().toString();
                      
        if (name.length() > 2 && name.endsWith(".c")) { // NOI18N
            lang = APTLanguageSupport.GNU_C;                  
        }
        
        return APTLanguageSupport.getInstance().getFilter(lang);
    }
    
    public APTPreprocHandler getPreprocHandler() {
        return getProjectImpl()==null ? null : getProjectImpl().getPreprocHandler(fileBuffer.getFile());
    }
    
    public void setBuffer(FileBuffer fileBuffer) {
        synchronized (changeStateLock) {
            if( this.fileBuffer != null ) {
                this.fileBuffer.removeChangeListener(this);
            }
            this.fileBuffer = fileBuffer;
            if( state != State.INITIAL ) {
                state = State.MODIFIED;
            }
            this.fileBuffer.addChangeListener(this);
        }
    }
    
    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }
    
    public void ensureParsed(APTPreprocHandler preprocHandler) {
	synchronized( stateLock ) {
	    switch( state ) {
		case INITIAL:
		    parse(preprocHandler);
		    if( TraceFlags.DUMP_PARSE_RESULTS ) new CsmTracer().dumpModel(this);
		    break;
		case MODIFIED:
		    reparse(preprocHandler);
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
    
    private Object changeStateLock = new Object();
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        stateChanged(false);
    }

    public void stateChanged(boolean invalidateCache) {
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
    
    /**
     * Removes old content from te file and model,
     * then parses the current buffer
     */
    public void reparse(APTPreprocHandler preprocHandler) {
        synchronized( stateLock ) {
            state = State.BEING_PARSED;
            try {
                _reparse((preprocHandler == null) ? getPreprocHandler() : preprocHandler);
            }
            finally {
                synchronized (changeStateLock) {
                    if (state != State.MODIFIED) {
                        state = State.PARSED;
                    }
                }
                stateLock.notifyAll();
            }
        }
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
                getProjectImpl().getGraph().putFile(this);
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
    
    public void onProjectDispose(){
        onDispose();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.projectRef = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
        }
    }
    
    private void disposeAll(boolean clearNonDisposable) {
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        //for( Iterator iter = declarations.values().iterator(); iter.hasNext(); ) {
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        try {
            declarationsLock.writeLock().lock();
            uids = declarations.values();
            declarations = new TreeMap<SortedKey, CsmUID<CsmOffsetableDeclaration>>();
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
        Set<CsmUID<CsmMacro>> copy = macros;
        macros = createMacros();
        RepositoryUtils.remove(copy);
    }
    
    private Set<CsmUID<CsmMacro>> createMacros() {
        return new TreeSet<CsmUID<CsmMacro>>(UID_START_OFFSET_COMPARATOR);
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
    
    public AST parse(APTPreprocHandler preprocHandler) {
        synchronized( stateLock ) {
            state = State.BEING_PARSED;
            try {
                return _parse((preprocHandler == null) ? getPreprocHandler() : preprocHandler);
            }
            finally {
                synchronized (changeStateLock) {
                    if (state != State.MODIFIED) {
                        state = State.PARSED;
                    }
                }
                stateLock.notifyAll();
            }
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
                getProjectImpl().getGraph().putFile(this);
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
        ProjectBase startProject = ProjectBase.getStartProject(preprocHandler.getState());
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(preprocHandler.getState()) + // NOI18N
                    "\n while getting TS of file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
            return null;
        }
        APTParseFileWalker walker = new APTParseFileWalker(startProject, apt, this, preprocHandler);
        return walker.getFilteredTokenStream(getLanguageFilter());        
    }
    
    private final String tokStreamLock = new String("TokenStream lock"); // NOI18N
    private Reference<OffsetTokenStream> ref = new SoftReference(null);
    
    public TokenStream getTokenStream(int startOffset, int endOffset) {
        try {
            OffsetTokenStream stream;
            synchronized (tokStreamLock) {
                stream = ref != null ? ref.get() : null;
                ref = new SoftReference(null);
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
    
    private AST doParse(APTPreprocHandler preprocHandler) {
//        if( "cursor.hpp".equals(fileBuffer.getFile().getName()) ) {
//            System.err.println("cursor.hpp");
//        }  
        if( reportParse || TraceFlags.DEBUG ) {
            System.err.println("# APT-based AST-cached Parsing " + fileBuffer.getFile().getPath() + " (Thread=" + Thread.currentThread().getName() + ')');
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
            // set guard info
            updateGuardAfterParse(preprocHandler, aptFull);
            // make real parse
            ProjectBase startProject = ProjectBase.getStartProject(preprocHandler.getState());
            if (startProject == null) {
                System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(preprocHandler.getState()) + // NOI18N
                    "\n while parsing file " + getAbsolutePath() + "\n of project " + getProject()); // NOI18N
                return null;
            }
            APTParseFileWalker walker = new APTParseFileWalker(startProject, aptFull, this, preprocHandler);
            walker.addMacroAndIncludes(true);
            if (TraceFlags.DEBUG) {
                System.err.println("doParse " + getAbsolutePath() + " with " + ParserQueue.tracePreprocState(oldState));
            }
            CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile().getName(), walker.getFilteredTokenStream(getLanguageFilter()), flags);
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
	if( TraceFlags.TRACE_VALIDATION ) System.err.printf("PARSED    %s \n\tlastModified=%d\n\t  lastParsed=%d  diff=%d\n", 
		getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified()-lastParsed);
	Hook aHook = hook;
	if( aHook != null ) {
	    aHook.parsingFinished(this, preprocHandler);
	}
        return ast;
    }
    
    /*package*/void initGuardIfNeeded(APTPreprocHandler preprocHandler, APTFile apt) {
        if (!getGuardState().isInited()) {
            setGuardState(preprocHandler, apt);
        }
    }

    private void updateGuardAfterParse(APTPreprocHandler preprocHandler, APTFile apt) {
        if (!getGuardState().isInited()) {
            setGuardState(preprocHandler, apt);
        } else {
            getGuardState().setGuardBlockState(preprocHandler, getGuardState().getGuard());
        }
    }
    
    private void setGuardState(APTPreprocHandler preprocHandler, APTFile aptLight) {
        synchronized (getGuardState()) {
            GuardBlockWalker guard = new GuardBlockWalker(aptLight, preprocHandler);
            TokenStream ts = guard.getTokenStream();
            try {
                Token token = ts.nextToken();
                while (!APTUtils.isEOF(token)) {
                    if (!APTUtils.isCommentToken(token)) {
                        guard.clearGuard();
                        break;
                    }
                    token = ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                guard.clearGuard();
            }
            getGuardState().setGuardBlockState(preprocHandler, guard.getGuard());
        }
    }
    
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
        
        public @Override boolean equals(Object obj) {
            return super.equals(obj);
        }

        public @Override int hashCode() {
            return 11; // any dummy value
        }          
    };
        
    static final private Comparator<CsmUID> UID_START_OFFSET_COMPARATOR = new Comparator<CsmUID>() {
        public int compare(CsmUID o1, CsmUID o2) {
            if (o1 == o2) {
                return 0;
            }
            Comparable<CsmUID> i1 = (Comparable<CsmUID>)o1;
            assert i1 != null;
            return i1.compareTo(o2);
        }   
        
        public @Override boolean equals(Object obj) {
            return super.equals(obj);
        }

        public @Override int hashCode() {
            return 11; // any dummy value
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
        return _getProject(true);
    }

    /** Just a convenient shortcut to eliminate casts */
    public ProjectBase getProjectImpl() {
        return _getProject(true);
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
    
    public void addMacro(CsmMacro macro) {
        CsmUID<CsmMacro> macroUID = RepositoryUtils.put(macro);
        assert macroUID != null;
        try {
            macrosLock.writeLock().lock();
            macros.add(macroUID);
        } finally {
            macrosLock.writeLock().unlock();
        }
    }
    
    public Collection<CsmMacro> getMacros() {
       Collection<CsmMacro> out;
       try {
            macrosLock.readLock().lock();
            out = UIDCsmConverter.UIDsToMacros(macros);
        } finally {
            macrosLock.readLock().unlock();
        }
        return out;
    }
    
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uidDecl = RepositoryUtils.put(decl);
        try {
            declarationsLock.writeLock().lock();
            declarations.put(getSortKey(decl), uidDecl);
        } finally {
            declarationsLock.writeLock().unlock();
        }
        // TODO: remove this dirty hack!
	if( decl instanceof VariableImpl ) {
            VariableImpl v = (VariableImpl) decl;
	    if( isOfFileScope(v) ) {
		v.setScope(this);
	    }
	}
        if( CsmKindUtilities.isFunctionDeclaration(decl) ) {
            FunctionImpl fi = (FunctionImpl) decl;
            if( fi.isStatic() ) {
                addStaticFunctionDeclaration(fi);
            }
        }
    }
    
    private void addStaticFunctionDeclaration(FunctionImpl func) {
        if( staticFunctionDeclarationUIDs == null ) {
            staticFunctionDeclarationUIDs = new ArrayList<CsmUID<CsmFunction>>();
        }
        staticFunctionDeclarationUIDs.add(func.getUID());
    }

    /** 
     * Gets the list of the static functions declarations (not definitions) 
     * This is necessary for finding definitions/declarations 
     * since file-level static functions (i.e. c-style static functions) aren't registered in project
     */
    public Collection<CsmFunction> getStaticFunctionDeclarations() {
        if( staticFunctionDeclarationUIDs == null ) {
            return Collections.<CsmFunction>emptyList();
        } else {
            return new LazyCsmCollection<CsmFunction>(new ArrayList<CsmUID<CsmFunction>>(staticFunctionDeclarationUIDs), true);
        }
    }
    
    public static boolean isOfFileScope(VariableImpl v) {
	if( v.isStatic() ) {
	    return true;
	}
	else if( v.isConst() ) {
	    if( ! v.isExtern() ) {
		return true;
	    }
	}
	else {
	    return false;
//	    if( ! v.isExtern() ) {
//		return true;
//	    }
	}
	return false;
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        _removeDeclaration(declaration);
    }
    
    private void _removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uidDecl;
        try {
            declarationsLock.writeLock().lock();
            uidDecl = declarations.remove(getSortKey(declaration));
        } finally {
            declarationsLock.writeLock().unlock();
        }
        RepositoryUtils.remove(uidDecl);
        // update repository
        RepositoryUtils.put(this);
    }
    
    private SortedKey getSortKey(CsmOffsetableDeclaration declaration) {
        return new SortedKey(declaration);
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
                if( isOfFileScope(v) ) {
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
                        ParserQueue.instance().addFirst(this, ppState, false);
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
                    ParserQueue.instance().addFirst(this, ppState, false);
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
    
    public void fixFakeRegistrations() {
        if (!isValid()) {
            return;
        }
        Collection<FunctionImplEx> fakes = Collections.<FunctionImplEx>emptySet();
        
        if (fakeRegistrationUIDs.size() > 0) {
            fakes = UIDCsmConverter.UIDsToDeclarationsUnsafe(fakeRegistrationUIDs);
            fakeRegistrationUIDs.clear();
        }
	for (FunctionImplEx curElem: fakes) {
            // Due to lazy list the client should check value on null.
            if (curElem != null) {
                curElem.fixFakeRegistration();
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
            factory.writeSortedStringToUIDMap(this.declarations, output, false);
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
            factory.writeUIDCollection(this.macros, output, false);
        } finally {
            macrosLock.readLock().unlock();
        }
        factory.writeUIDCollection(this.fakeRegistrationUIDs, output, false);
        //output.writeUTF(state.toString());
        output.writeInt(fileType);
        
        // not null UID
        assert this.projectUID != null;
        UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
        guardState.write(output);
	output.writeLong(lastParsed);
	output.writeUTF(state.toString());
    }
    
    public FileImpl(DataInput input) throws IOException {
        this.fileBuffer = PersistentUtils.readBuffer(input);
        
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();        
        factory.readSortedStringToUIDMap(this.declarations, input, null);
        factory.readUIDCollection(this.includes, input);
        factory.readUIDCollection(this.macros, input);
        factory.readUIDCollection(this.fakeRegistrationUIDs, input);
        //state = State.valueOf(input.readUTF());
        fileType = input.readInt();

        this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // not null UID
        assert this.projectUID != null;
        this.projectRef = null;
        
        assert fileBuffer != null;
        assert fileBuffer.isFileBased();
        guardState = new GuardBlockState(input);
	lastParsed = input.readLong();
        state = State.valueOf(input.readUTF());
    }

    public @Override int hashCode() {
	if( hash == 0 ) {   // we don't need sync here - at worst, we'll calculate the same value twice
	    String identityHashPath = getProjectImpl().getUniqueName() + "*" + getAbsolutePath(); // NOI18N
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
	    return this.getProjectImpl().getUniqueName().equals(other.getProjectImpl().getUniqueName());
	}
	return false;
    }
    
    private GuardBlockState getGuardState() {
        return guardState;
    }

    // for tests only
    public GuardBlockState testGetGuardState() {
        return guardState;
    }
    
    public boolean isNeedReparse(APTPreprocHandler.State oldState, APTPreprocHandler newState){
        boolean update = false;
        if (oldState == null || !oldState.isValid()) {
            update = true;
        } else if (!oldState.isCompileContext() && newState.isCompileContext()) {
            update = true;
        } else {
            update = getGuardState().isNeedReparse(newState);
        }
        return update;
    }

    public boolean isNeedReparse(APTPreprocHandler.State oldState, APTPreprocHandler.State preprocState){
        if (oldState == null || !oldState.isValid()) {
            return true;
        } else if (oldState.isCompileContext()) {
            // do nothing
            if (preprocState != null && isNeedReparseGuardBlock(preprocState)) {
                // override state with new one
                return true;
            }
        } else if (preprocState != null && preprocState.isCompileContext()) {
            // override state with new one
            return true;
        }
        return false;
    }

    private boolean isNeedReparseGuardBlock(APTPreprocHandler.State preprocState){
        StartEntry startEntry = new StartEntry(getAbsolutePath(), RepositoryUtils.UIDtoKey(getProject().getUID()));
        APTPreprocHandler preprocHandler = APTHandlersSupport.createEmptyPreprocHandler(startEntry);
        preprocHandler.setState(preprocState);
        return getGuardState().isNeedReparse(preprocHandler);
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

    public static class SortedKey implements Comparable<SortedKey>, Persistent, SelfPersistent {
        private int start = 0;
        private CharSequence name;
        private SortedKey(CsmOffsetableDeclaration declaration){
            start = ((CsmOffsetable) declaration).getStartOffset();
            name = declaration.getName();
        }
        public int compareTo(SortedKey o) {
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
        
        public SortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = NameCache.getManager().getString(input.readUTF());
        }
    }
    
}
