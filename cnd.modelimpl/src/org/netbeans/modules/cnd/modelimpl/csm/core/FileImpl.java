/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import org.netbeans.modules.cnd.apt.support.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;
import org.netbeans.modules.cnd.modelimpl.cache.impl.FileCacheImpl;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.GuardBlockWalker;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModel;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

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
    
    // only one of project/projectUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)  
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;

    /** 
     * It's a map since we need to eliminate duplications 
     */
    private Map/*<String, CsmOffsetableDeclaration>*/ declarationsOLD = Collections.synchronizedSortedMap(new TreeMap/*<String, CsmOffsetableDeclaration>*/());
    private Map<String, CsmUID<CsmOffsetableDeclaration>> declarations = new TreeMap<String, CsmUID<CsmOffsetableDeclaration>>();
    private ReadWriteLock  declarationsLock = new ReentrantReadWriteLock();

    private Set<CsmInclude> includesOLD = Collections.synchronizedSortedSet(new TreeSet<CsmInclude>(START_OFFSET_COMPARATOR));
    private Set<CsmUID<CsmInclude>> includes = new TreeSet<CsmUID<CsmInclude>>(UID_START_OFFSET_COMPARATOR);
    private ReadWriteLock includesLock = new ReentrantReadWriteLock();

    private Set<CsmMacro> macrosOLD = Collections.synchronizedSortedSet(new TreeSet<CsmMacro>(START_OFFSET_COMPARATOR));    
    private Set<CsmUID<CsmMacro>> macros = new TreeSet<CsmUID<CsmMacro>>(UID_START_OFFSET_COMPARATOR);    
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
    
    private APTPreprocHandler myPreprocHandler;
    
    private Object stateLock = new Object();
    
    private Collection/*<FunctionDefinitionImpl>*/ fakeRegistrationsOLD = new ArrayList();
    private Collection<CsmUID<FunctionImplEx>> fakeRegistrationUIDs = new CopyOnWriteArrayList();
    
    private final Object fakeLock;

    private final GuardBlockState guardState;
    
    private long lastParsed = Long.MIN_VALUE;
    
    /** Cache the hash code */
    private int hash; // Default to 0
    

    public FileImpl(FileBuffer fileBuffer, ProjectBase project, int fileType, APTPreprocHandler preprocHandler) {
	state = State.INITIAL;
        setBuffer(fileBuffer);
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.projectUID = UIDCsmConverter.projectToUID(project);
            this.projectRef = null;
        } else {
            this.projectRef = project;
            this.projectUID = null;
        }
        this.myPreprocHandler = preprocHandler;
        this.fileType = fileType;
        this.fakeLock = new String("File Lock for " + fileBuffer.getFile().getAbsolutePath()); // NOI18N
        this.guardState = new GuardBlockState();
        Notificator.instance().registerNewFile(this);
    }    
    
    public FileImpl(FileBuffer fileBuffer, ProjectBase project) {
	this(fileBuffer, project, UNDEFINED_FILE, (APTPreprocHandler)null);
    }
    
    private ProjectBase _getProject(boolean assertNotNull) {
        ProjectBase prj = this.projectRef;
        if (prj == null) {
            if (TraceFlags.USE_REPOSITORY) {
                prj = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
		if( assertNotNull ) {
		    assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
		}
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
        String name =  getName();
                      
        if (name.length() > 2 && name.endsWith(".c")) { // NOI18N
            lang = APTLanguageSupport.GNU_C;                  
        }
        
        return APTLanguageSupport.getInstance().getFilter(lang);
    }
    
    private APTPreprocHandler getCreatePreprocHandler() {
        // use current
        APTPreprocHandler preprocHandler = this.myPreprocHandler;
        // else ask project
        if (preprocHandler == null && (getProjectImpl() != null)) {
            preprocHandler = getProjectImpl().getPreprocHandler(fileBuffer.getFile());
        }
        // otherwise create default
        if (preprocHandler == null && getProject() != null) {
            StartEntry startEntry = new StartEntry(getAbsolutePath(), RepositoryUtils.UIDtoKey(getProject().getUID()));
            preprocHandler = APTHandlersSupport.createEmptyPreprocHandler(startEntry);
        }
        return preprocHandler;
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
                if( preprocHandler != null ) {
                    setPreprocHandler(preprocHandler);
                }
                _reparse();
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
    
    private void _reparse() {
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
            AST ast = doParse();
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
            if (TraceFlags.USE_REPOSITORY) {
                RepositoryUtils.put(this);
            }
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
        if (TraceFlags.USE_REPOSITORY) {
            Collection<CsmUID<CsmOffsetableDeclaration>> uids;
            try {
                declarationsLock.writeLock().lock();
                uids = declarations.values();
                declarations = new TreeMap<String, CsmUID<CsmOffsetableDeclaration>>();
            }   finally {
                declarationsLock.writeLock().unlock();
            }
            
            if (clearNonDisposable) {
                _clearIncludes();
                _clearMacros();
            }
            List<CsmOffsetableDeclaration> arr = UIDCsmConverter.UIDsToDeclarations(uids);
            Utils.disposeAll(arr);          
            RepositoryUtils.remove(uids);
        } else {
            Collection<CsmOffsetableDeclaration> arr;
            synchronized (declarationsOLD) {
                arr = declarationsOLD.values();
                declarationsOLD = Collections.synchronizedSortedMap(new TreeMap/*<String, CsmOffsetableDeclaration>*/());
                if (clearNonDisposable) {
                    _clearIncludes();
                    _clearMacros();
                }
            }
            Utils.disposeAll(arr);
        }

    }
        
    private void _clearMacros() {
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.remove(macros);
        } else {
            macrosOLD.clear();
        }        
    }
    
    private void _clearIncludes() {
        if (TraceFlags.USE_REPOSITORY) {
            try {
                includesLock.writeLock().lock();
		RepositoryUtils.remove(includes);
            } finally {
                includesLock.writeLock().unlock();
            }
        } else {
            includesOLD.clear();
        }
    }
    
    public AST parse(APTPreprocHandler preprocHandler) {
        synchronized( stateLock ) {
            state = State.BEING_PARSED;
            try {
                if( preprocHandler != null ) {
                    setPreprocHandler(preprocHandler);
                }
                return _parse();
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
    
    private AST _parse() {
        
        if (reportErrors) {
	    if (! ParserThreadManager.instance().isParserThread()  && ! ParserThreadManager.instance().isStandalone()) {
		String text = "Reparsing should be done only in a special Code Model Thread!!!"; // NOI18N
		Diagnostic.trace(text);
		new Throwable(text).printStackTrace(System.err);
	    }
        }        
	
	Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
	
        try {
            AST ast = doParse();
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
            if (TraceFlags.USE_REPOSITORY && isValid()) {   // FIXUP: use a special lock here
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

    public TokenStream getTokenStream() {
        APTPreprocHandler preprocHandler = getCreatePreprocHandler(); 
        APTFile apt = null;
	if (TraceFlags.USE_AST_CACHE) {
	    apt = CacheManager.getInstance().findAPT(this);
	}
	else {
	    try {
		apt = APTDriver.getInstance().findAPT(fileBuffer);
	    } catch (IOException ex) {
		ex.printStackTrace(System.err);
	    }
	}
        if (apt == null) {
            return null;
        }
        APTParseFileWalker walker = new APTParseFileWalker(ProjectBase.getStartProject(preprocHandler.getIncludeHandler()), apt, this, preprocHandler);
        return walker.getFilteredTokenStream(getLanguageFilter());
    }
    
    private AST doParse() {
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

        APTPreprocHandler preprocHandler = getCreatePreprocHandler(); 
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
                System.err.println("File "+getBuffer().getFile().getAbsolutePath()+" not found.");
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
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
                APTParseFileWalker walker = new APTParseFileWalker(ProjectBase.getStartProject(preprocHandler.getIncludeHandler()), aptLight, this, preprocHandler);
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
            // init guard info
            setGuardState(preprocHandler, aptFull);
            // make real parse
            APTParseFileWalker walker = new APTParseFileWalker(ProjectBase.getStartProject(preprocHandler.getIncludeHandler()), aptFull, this, preprocHandler);
            if (TraceFlags.DEBUG) {
                System.err.println("doParse " + getAbsolutePath() + " with " + ParserQueue.tracePreprocState(oldState));
            }
            CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile().getName(), walker.getFilteredTokenStream(getLanguageFilter()), flags);
            long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
            parser.translation_unit();
            
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
        // we need keeping state for TraceModel. It will set it to null afterwards
        if(!TraceModel.LEAVE_PP_STATE_UNCLEANED) {
            setPreprocHandler(null);
            if (getProjectImpl() != null && getBuffer().isFileBased()) {
                getProjectImpl().cleanPreprocStateAfterParse(this, oldState);
            }
        }
	lastParsed = Math.max(System.currentTimeMillis(), fileBuffer.lastModified());
	if( TraceFlags.TRACE_VALIDATION ) System.err.printf("PARSED    %s \n\tlastModified=%d\n\t  lastParsed=%d  diff=%d\n", 
		getAbsolutePath(), fileBuffer.lastModified(), lastParsed, fileBuffer.lastModified()-lastParsed);
        return ast;
    }

    private void setGuardState(APTPreprocHandler preprocHandler, APTFile aptLight) {
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
    
    public void addInclude(IncludeImpl includeImpl) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmInclude> uid = RepositoryUtils.put(includeImpl);
            assert uid != null;
            try {
                includesLock.writeLock().lock();
                includes.add(uid);
            } finally {
                includesLock.writeLock().unlock();
            }
        } else {
            includesOLD.add(includeImpl);
        }
    }
    
    public static final Comparator/*<? super CsmOffsetable>*/ START_OFFSET_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            CsmOffsetable i1 = (CsmOffsetable)o1;
            CsmOffsetable i2 = (CsmOffsetable)o2; 
            int ofs1 = i1.getStartOffset();
            int ofs2 = i2.getStartOffset();
            if (ofs1 == ofs2) {
                return 0;
            } else {
                return (ofs1 - ofs2);
            }
        }   
        
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        public int hashCode() {
            return 11; // any dummy value
        }          
    };
        
    static final private Comparator<CsmUID> UID_START_OFFSET_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            if (o1 == o2) {
                return 0;
            }
            Comparable i1 = (Comparable)o1;
            Comparable i2 = (Comparable)o2; 
            assert i1 != null;
            assert i2 != null;
            return i1.compareTo(i2);
        }   
        
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        public int hashCode() {
            return 11; // any dummy value
        }          
    };
    
    public String getText(int start, int end) {
        try {
            return fileBuffer.getText(start, end);
        }
        catch( IOException e ) {
            e.printStackTrace(System.err);
            return "";
        }
    }

    public String getText() {
        try {
            return fileBuffer.getText();
        }
        catch( IOException e ) {
            e.printStackTrace(System.err);
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

    public String getName() {
        return fileBuffer.getFile().getName();
    }

    public List<CsmInclude> getIncludes() {
        if (TraceFlags.USE_REPOSITORY) {
            List out;
            try {
                includesLock.readLock().lock();
                out = UIDCsmConverter.UIDsToIncludes(includes);
            } finally {
                includesLock.readLock().unlock();
            }
            return out;
        } else {
            synchronized (includesOLD) {
                return new ArrayList(includesOLD);
            }
        }
    }

    public List<CsmOffsetableDeclaration> getDeclarations() {
        if (!SKIP_UNNECESSARY_FAKE_FIXES) {
            fixFakeRegistrations();
        }
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmOffsetableDeclaration> decls;
            try {
                declarationsLock.readLock().lock();
                Collection<CsmUID<CsmOffsetableDeclaration>> uids = declarations.values();
                decls = UIDCsmConverter.UIDsToDeclarations(uids);
            } finally {
                declarationsLock.readLock().unlock();
            }
            return decls;
        } else {
            synchronized (declarationsOLD) {
                return new ArrayList(declarationsOLD.values());
            }
        }
    }
    
    public void addMacro(CsmMacro macro) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmMacro> uid = RepositoryUtils.put(macro);
            assert uid != null;
            try {
                macrosLock.writeLock().lock();
                macros.add(uid);
            } finally {
                macrosLock.writeLock().unlock();
            }
        } else {
            macrosOLD.add(macro);
        }
    }
    
    public List<CsmMacro> getMacros() {
        if (TraceFlags.USE_REPOSITORY) {
            List out;
           try {
                macrosLock.readLock().lock();
                out = UIDCsmConverter.UIDsToMacros(macros);
            } finally {
                macrosLock.readLock().unlock();
            }
            return out;
        } else {
            synchronized (macrosOLD) {
                return new ArrayList(macrosOLD);
            }
        }
    }
    
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        _addDeclaration(decl);
        // TODO: remove this dirty hack!
	if( decl instanceof VariableImpl ) {
            VariableImpl v = (VariableImpl) decl;
	    if( isOfFileScope(v) ) {
		v.setScope(this);
	    }
	}
    }
    
    private void _addDeclaration(CsmOffsetableDeclaration decl) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
            try {
                declarationsLock.writeLock().lock();
                declarations.put(getSortKey(decl), uid);
            } finally {
                declarationsLock.writeLock().unlock();
            }
        } else {
            declarationsOLD.put(getSortKey(decl), decl);
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
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid;
            try {
                declarationsLock.writeLock().lock();
                uid = declarations.remove(getSortKey(declaration));
            } finally {
                declarationsLock.writeLock().unlock();
            }
            RepositoryUtils.remove(uid);
            // update repository
            RepositoryUtils.put(this);
        } else {
            declarationsOLD.remove(getSortKey(declaration));
        }
    }
    
    public static String getSortKey(CsmDeclaration declaration) {
        StringBuilder sb = new StringBuilder();
        if( declaration instanceof CsmOffsetable ) {
            int start = ((CsmOffsetable) declaration).getStartOffset();
            String s = Integer.toString(start);
            int gap = 8 - s.length();
            while( gap-- > 0 ) {
                sb.append('0');
            }
            sb.append(s);
            sb.append(declaration.getName());
        }
        else {
            assert false;
            // actually this never happens 
            // since of all declarations only CsmBuiltin isn't CsmOffsetable
            // and CsmBuiltin is never added to any file
            sb.append(declaration.getUniqueName());
        }
        return sb.toString();
    }
    
    public String getAbsolutePath() {
        return fileBuffer.getFile().getAbsolutePath();
    }
    
    public File getFile() {
	return fileBuffer.getFile();
    }

    public List<CsmScopeElement> getScopeElements() {
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

    public void setPreprocHandler(APTPreprocHandler preprocHandler) {
        this.myPreprocHandler = preprocHandler;
    }
    
    // for tests only
    public APTPreprocHandler.State testGetPreprocState() {
        if (myPreprocHandler != null) {
            return myPreprocHandler.getState();
        } else {
            return null;
        }
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
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<FunctionImplEx> uid = UIDCsmConverter.declarationToUID(decl);
                fakeRegistrationUIDs.add(uid);
        } else {
            synchronized( fakeLock ) {
                fakeRegistrationsOLD.add(decl);
            }
        }
    }
    
    public void fixFakeRegistrations() {
        if (!isValid()) {
            return;
        }
        Collection<FunctionImplEx> fakes = Collections.EMPTY_SET;
        
        if (TraceFlags.USE_REPOSITORY) {
            if (fakeRegistrationUIDs.size() > 0) {
                fakes = UIDCsmConverter.UIDsToDeclarationsUnsafe(fakeRegistrationUIDs);
                fakeRegistrationUIDs.clear();
            }
        } else {
            synchronized( fakeLock ) {
                // Right now we do not need to make a copy, fakeRegistrations is cleared anyway
                fakes = fakeRegistrationsOLD;
                //fakes = (FunctionDefinitionImpl[]) fakeRegistrations.toArray(new FunctionDefinitionImpl[fakeRegistrations.size()]);
                fakeRegistrationsOLD = new ArrayList();
            }
        }
	for (FunctionImplEx curElem: fakes) {
	    curElem.fixFakeRegistration();
	}
    }
    
    public String toString() {
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
            factory.writeStringToUIDMap(this.declarations, output, false);
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
        factory.readStringToUIDMap(this.declarations, input, null);
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
        fakeLock = new String("File Lock for " + fileBuffer.getFile().getAbsolutePath()); // NOI18N 
        guardState = new GuardBlockState(input);
	lastParsed = input.readLong();
        state = State.valueOf(input.readUTF());
        assert TraceFlags.USE_REPOSITORY;
    }

    public int hashCode() {
	if( hash == 0 ) {   // we don't need sync here - at worst, we'll calculate the same value twice
	    String identityHashPath = getProject().getQualifiedName() + "*" + getAbsolutePath(); // NOI18N
	    hash = identityHashPath.hashCode();
	}
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CsmFile)) {
            return false;
        }
	if( obj == this ) {
	    return true;
	}
	CsmFile other = (CsmFile)obj;
	if( this.getAbsolutePath().equals(other.getAbsolutePath()) ) {
	    return this.getProject().getQualifiedName().equals(other.getProject().getQualifiedName());
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
        if (oldState == null) {
            update = true;
        } else if (!oldState.isStateCorrect() && newState.isStateCorrect()) {
            update = true;
        } else {
            update = getGuardState().isNeedReparse(newState);
        }
        return update;
    }

    public boolean isNeedReparse(APTPreprocHandler.State ppState, APTPreprocHandler.State preprocState){
        if (ppState != null && ppState.isStateCorrect()) {
            // do nothing
            if (preprocState != null && isNeedReparseGuardBlock(preprocState)) {
                // override state with new one
                return true;
            }
        } else if (preprocState != null && preprocState.isStateCorrect()) {
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
        int startLineOffset = 0;
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
                startLineOffset = curOffset+1;
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
}
