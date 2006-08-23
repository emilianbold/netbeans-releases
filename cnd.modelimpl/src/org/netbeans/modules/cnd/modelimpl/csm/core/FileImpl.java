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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.antlr2.CPPParserEx;
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallback;
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallbackImpl;
import org.netbeans.modules.cnd.modelimpl.antlr2.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.cache.FileCache;

import java.io.*;
import java.util.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APTFile;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTWalker;

/**
 * CsmFile implementations
 * @author Vladimir Kvashin
 */
public class FileImpl implements 
	CsmFile, MutableDeclarationsContainer, ChangeListener, Disposable {
    
    public static final boolean reportErrors = TraceFlags.REPORT_PARSING_ERRORS | Diagnostic.DEBUG;
    private static final boolean reportParse = Boolean.getBoolean("parser.log.parse");
    
    private static final boolean emptyAstStatictics = Boolean.getBoolean("parser.empty.ast.statistics");

    private FileBuffer fileBuffer;
    private ProjectBase project;

    /** 
     * It's a map since we need to eliminate duplications 
     */
    private SortedMap/*<String, CsmDeclaration>*/ declarations = new TreeMap/*<String, CsmDeclaration>*/();

    private Set/*<CsmInclude>*/ includes = new TreeSet(START_OFFSET_COMPARATOR);
    private Set/*CsmMacro*/ macros = new TreeSet(START_OFFSET_COMPARATOR);    
    
    private int errorCount = 0;
    
    private static final int STATE_INITIAL = -1;
    private static final int STATE_PARSED = 0;
    private static final int STATE_MODIFIED = 1;
    private static final int STATE_BEING_PARSED = 2;
    
    private int state = STATE_INITIAL;
    
    private PPCallback callback;
    private APTPreprocState preprocState;
    
    private Object stateLock = new Object();
    
    public FileImpl(FileBuffer fileBuffer, ProjectBase project, PPCallback callback) {
        setBuffer(fileBuffer);
        this.project = project;
        this.callback = callback;
        Notificator.instance().registerNewFile(this);
    }

    public FileImpl(FileBuffer fileBuffer, ProjectBase project, APTPreprocState preprocState) {
        setBuffer(fileBuffer);
        this.project = project;
        this.preprocState = preprocState;
        Notificator.instance().registerNewFile(this);
    }    
    
    public FileImpl(FileBuffer fileBuffer, ProjectBase project) {
	this(fileBuffer, project, (PPCallback)null);
    }
    
    private PPCallback getCreateCallback() {
        // use current
        PPCallback callback = this.callback;
        // else ask project
        if (callback == null && (getProjectImpl() != null)) {
            callback = getProjectImpl().getCallback(fileBuffer.getFile());
        }
        // otherwise create default
        if (callback == null) {
            callback = new PPCallbackImpl(getProjectImpl(), getAbsolutePath());
        }
        return callback;
    }
    
    private APTPreprocState getCreatePreprocState() {
        // use current
        APTPreprocState preprocState = this.preprocState;
        // else ask project
        if (preprocState == null && (getProjectImpl() != null)) {
            preprocState = getProjectImpl().getPreprocState(fileBuffer.getFile());
        }
        // otherwise create default
        if (preprocState == null) {
            preprocState = new APTPreprocStateImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl());
        }
        return preprocState;
    }
    
    public void setBuffer(FileBuffer fileBuffer) {
        synchronized( stateLock ) {
            if( this.fileBuffer != null ) {
                this.fileBuffer.removeChangeListener(this);
            }
            this.fileBuffer = fileBuffer;
            if( state != STATE_INITIAL ) {
                state = STATE_MODIFIED;
            }
            this.fileBuffer.addChangeListener(this);
        }
    }
    
    public FileBuffer getBuffer() {
        return this.fileBuffer;
    }
    
    public void ensureParsed(PPCallback callback) {
        synchronized( stateLock ) {
            switch( state ) {
                case STATE_INITIAL:
                    parse(callback);
                    break;
                case STATE_MODIFIED:
                    reparse(callback);
                    break;
            }
        }
    }
    
    // copy
    public void ensureParsed(APTPreprocState preprocState) {
        synchronized( stateLock ) {
            switch( state ) {
                case STATE_INITIAL:
                    parse(preprocState);
                    break;
                case STATE_MODIFIED:
                    reparse(preprocState);
                    break;
            }
        }
    }   
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        state = STATE_MODIFIED;
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
    public void reparse(PPCallback callback) {
        synchronized( stateLock ) {
            state = STATE_BEING_PARSED;
            try {
                if( callback != null ) {
                    setCallback(callback);
                }
                _reparse();
            }
            finally {
                state = STATE_PARSED;
                stateLock.notifyAll();
            }
        }
    }
    
    /**
     * C&P of above
     */
    public void reparse(APTPreprocState preprocState) {
        synchronized( stateLock ) {
            state = STATE_BEING_PARSED;
            try {
                if( preprocState != null ) {
                    setPreprocState(preprocState);
                }
                _reparse();
            }
            finally {
                state = STATE_PARSED;
                stateLock.notifyAll();
            }
        }
    }    
    
    private void _reparse() {
        if (! ParserThreadManager.instance().isParserThread() && ! ParserThreadManager.instance().isStandalone()) {
            String text = "Reparsing should be done only in a special Code Model Thread!!!";
            Diagnostic.trace(text);
            new Throwable(text).printStackTrace(System.err);
        }
        if( Diagnostic.DEBUG ) Diagnostic.trace("------ reparsing " + fileBuffer.getFile().getName());
	//Notificator.instance().startTransaction();
	try {
	    FileCache cache = doParse();
	    if( cache != null ) {
		CacheManager.instance().storeCache(getProject(), fileBuffer.getFile(), cache.getAST(), cache.getIncludes());
		disposeAll();
		render(cache.getAST());
	    }
	}
	finally {
	    //Notificator.instance().endTransaction();
            Notificator.instance().flush();
	}
	    
    }

    public void dispose() {
        Notificator.instance().registerRemovedFile(this);
	disposeAll();
    }
    
    private void disposeAll() {
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        //for( Iterator iter = declarations.values().iterator(); iter.hasNext(); ) {
        Object[] arr = declarations.values().toArray(new Object[0]);
        for (int i = 0; i < arr.length; i++) {
            //Object o = iter.next();
            if( arr[i]  instanceof Disposable ) {
                ((Disposable) arr[i]).dispose();
            }
        }
        declarations.clear();
	includes.clear();
	macros.clear();
    }
        
    public AST parse(PPCallback callback) {
        synchronized( stateLock ) {
            state = STATE_BEING_PARSED;
            try {
                if( callback != null ) {
                    setCallback(callback);
                }
                return _parse();
            }
            finally {
                state = STATE_PARSED;
                stateLock.notifyAll();
            }
        }
    }
    
    // C&P of above
    public AST parse(APTPreprocState preprocState) {
        synchronized( stateLock ) {
            state = STATE_BEING_PARSED;
            try {
                if( preprocState != null ) {
                    setPreprocState(preprocState);
                }
                return _parse();
            }
            finally {
                state = STATE_PARSED;
                stateLock.notifyAll();
            }
        }
    }    
    
    public AST _parse() {
        
        if (reportErrors) {
	    if (! ParserThreadManager.instance().isParserThread()  && ! ParserThreadManager.instance().isStandalone()) {
		String text = "Reparsing should be done only in a special Code Model Thread!!!";
		Diagnostic.trace(text);
		new Throwable(text).printStackTrace(System.err);
	    }
        }        
	
	Diagnostic.StopWatch sw = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
	
        FileCache cache = CacheManager.instance().getCache(getProject(), fileBuffer.getFile());
        if( cache == null ) {
            cache = doParse();
	    if( cache != null ) {
		CacheManager.instance().storeCache(getProject(), fileBuffer.getFile(), cache.getAST(), cache.getIncludes());
	    }
        }
        else {
            if( Diagnostic.DEBUG ) Diagnostic.trace("# Getting AST from cache " + fileBuffer.getFile().getPath());
            // TODO: review due to new CsmInclude interface implementation
            // TODO: restore macros
            addIncludes(cache.getIncludes());
        }
	
        if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) sw.stopAndReport("Parsing of " + fileBuffer.getFile().getName() + " took \t");

        if( cache != null ) {
            AST ast = cache.getAST();
	    Diagnostic.StopWatch sw2 = TraceFlags.TIMING_PARSE_PER_FILE_DEEP ? new Diagnostic.StopWatch() : null;
            //Notificator.instance().startTransaction();
            try {
                render(ast);
            }
            finally {
                //Notificator.instance().endTransaction();
                Notificator.instance().flush();
            }
	    if (TraceFlags.TIMING_PARSE_PER_FILE_DEEP) sw2.stopAndReport("Rendering of " + fileBuffer.getFile().getName() + " took \t");
            return ast;
        }
        return null;
    }
    
    private boolean isEmpty(AST ast) {
        if( ast != null ) {
            AST child = ast.getFirstChild();
            if( child != null && child.getType() != CPPTokenTypes.EOF ) {
                return false;
            }
        }
        return true;
    }

    
    private FileCache doParse() {
        if (TraceFlags.USE_APT) {
            return doAPTParse();
        } else {
            return doOldParse();
        }
    }

    
    private FileCache doOldParse() {
//        if( "cursor.hpp".equals(fileBuffer.getFile().getName()) ) {
//            System.err.println("cursor.hpp");
//        }  
        if( reportParse || Diagnostic.DEBUG ) {
            System.err.println("# Parsing " + fileBuffer.getFile().getPath() + " (Thread=" + Thread.currentThread().getName() + ')');
        }
        InputStream fileStream = null;
        try {
            int flags = CPPParserEx.CPP_CPLUSPLUS;
            if( ! reportErrors ) {
                flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
            }
            PPCallback callback = getCreateCallback();
            fileStream = fileBuffer.getInputStream();
            CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile(), fileStream, callback, flags);
            long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
            parser.translation_unit();
            if( emptyAstStatictics ) {
                time = System.currentTimeMillis() - time;
                System.err.println("PARSED FILE " + getAbsolutePath() + (isEmpty(parser.getAST()) ? " EMPTY" : "") + ' ' + time + " ms");
            }
            if( TraceFlags.DUMP_AST ) {
                System.err.println("\n");
                System.err.print("AST: ");
		System.err.print(getAbsolutePath());
		System.err.print(' ');
                AstUtil.toStream(parser.getAST(), System.err);
                System.err.println("\n");
                
            }
            Collection/*<CsmInclude>*/ includes = callback.getIncludes(fileBuffer.getFile().getAbsolutePath());   
            Collection/*<CsmMacro>*/ macros = callback.getMacros(fileBuffer.getFile().getAbsolutePath());
	    setCallback(null);
            errorCount = parser.getErrorCount();
            
//            addIncludes(parser.getIncludes());
            addIncludes(includes);
            addMacros(macros);
            
            // TODO: need to update includes of FileCache
            return new FileCache(parser.getAST(), parser.getIncludes());

        } catch(Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;        
    }
    
    private FileCache doAPTParse() {
//        if( "cursor.hpp".equals(fileBuffer.getFile().getName()) ) {
//            System.err.println("cursor.hpp");
//        }  
        if( reportParse || Diagnostic.DEBUG ) {
            System.err.println("# APT-based Parsing " + fileBuffer.getFile().getPath() + " (Thread=" + Thread.currentThread().getName() + ')');
        }
        
        int flags = CPPParserEx.CPP_CPLUSPLUS;
        if( ! reportErrors ) {
            flags |= CPPParserEx.CPP_SUPPRESS_ERRORS;
        }

        APTPreprocState preprocState = getCreatePreprocState(); 
        APTFile apt = null;
        try {
            apt = APTDriver.getInstance().findAPT(fileBuffer);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        if (apt == null) {
            return null;
        }
        APTWalker walker = new APTParseFileWalker(apt, this, preprocState);
        CPPParserEx parser = CPPParserEx.getInstance(fileBuffer.getFile(), walker.getTokenStream(), flags);
        long time = (emptyAstStatictics) ? System.currentTimeMillis() : 0;
        try {
            parser.translation_unit();
        } catch (RecognitionException ex) {
            ex.printStackTrace(System.err);
        } catch (TokenStreamException ex) {
            ex.printStackTrace(System.err);
        }
        if( emptyAstStatictics ) {
            time = System.currentTimeMillis() - time;
            System.err.println("PARSED FILE " + getAbsolutePath() + (isEmpty(parser.getAST()) ? " EMPTY" : "") + ' ' + time + " ms");
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
        setPreprocState(null);
        // TODO: need to update includes of FileCache
        return new FileCache(parser.getAST(), parser.getIncludes());
    }
    
    // TODO: review due to new CsmInclude interface implementation
    private void addIncludes(List/*<String>*/ includes) {
        for(Iterator i = includes.iterator(); i.hasNext(); ) {
            String fileName = (String)i.next();
            addInclude(fileName.substring(1, fileName.length()-1), fileName.charAt(0) == '<');
        }
    }
    
    private void addIncludes(Collection/*<CsmInclude>*/ includes) {
        addMacrosOrIncludes(includes, this.includes, false);     
    }
    
    private void addMacros(Collection/*<CsmMacro>*/ macros) {
        addMacrosOrIncludes(macros, this.macros, true);        
    }
    
    public void addInclude(IncludeImpl includeImpl) {
	includes.add(includeImpl);
    }
    
    private void addMacrosOrIncludes(Collection/*<CsmOffsetable>*/ src,
            Set/*<CsmOffsetable>*/ dest, boolean macrosVsIncludes) {
        // set container file information
        for (Iterator it = src.iterator(); it.hasNext();) {
            CsmOffsetable elem = (CsmOffsetable) it.next();
            if (!dest.contains(elem)) {
                if (elem instanceof LineColOffsetableBase) {
                    assert (macrosVsIncludes ? (elem instanceof CsmMacro) : (elem instanceof CsmInclude));
                    ((LineColOffsetableBase)elem).setContainingFile(this);
                } else {
                    CsmOffsetable newElem = null;
                    if (macrosVsIncludes) {
                        assert(elem instanceof CsmMacro);
                        newElem = new MacroImpl((CsmMacro) elem, this);
                    } else {
                        assert(elem instanceof CsmInclude);
                        newElem = new IncludeImpl((CsmInclude) elem, this);
                    }
                    elem = newElem;
                }
                dest.add(elem);
            }
        }
        if( Diagnostic.DEBUG) {
            System.err.println((macrosVsIncludes?"#Defined":"#Included")+" in file " + fileBuffer.getFile().getPath());
            if (dest.size() == 0) {
                System.err.println("   no elements");
            }                
            for (Iterator it = dest.iterator(); it.hasNext();) {
                CsmOffsetable elem = (CsmOffsetable) it.next();
                System.err.println("   " + elem);
            }
        }         
    }
    
    static final private Comparator/*<? super CsmOffsetable>*/ START_OFFSET_COMPARATOR = new Comparator() {
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
                return (ofs1 > ofs2) ? +1 : -1;
            }
        }   
        
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        public int hashCode() {
            return 11; // any dummy value
        }          
    };
        
//    private static boolean globalDebugFunAdded = false;
//    
//    private void lightParse() {
//        LightParser.Callback callback = new LightParser.Callback() {
//            public void addClass(CsmDeclaration.Kind kind, String name, 
//                                int start, int end, boolean template, NamespaceImpl namespace) {
//                
//                if( kind == CsmDeclaration.Kind.ENUM ) {
//                    EnumImpl impl = new EnumImpl(name, namespace, FileImpl.this,  start, end);
//                    //Faker.fill(impl);
//                    addDeclaration(impl);
//                }
//                else if( kind == CsmDeclaration.Kind.CLASS 
//                    || kind == CsmDeclaration.Kind.STRUCT 
//                    || kind == CsmDeclaration.Kind.UNION ) {
//                    
//                    ClassImpl impl = new ClassImpl(kind, name, namespace, FileImpl.this,  start, end);
//                    //Faker.fill(impl);
//                    addDeclaration(impl);
//                }
//                if (!globalDebugFunAdded && namespace.isGlobal()) {
//                    //Faker.fill(namespace, FileImpl.this);  
//                    globalDebugFunAdded = true;
//                }                   
//            }
//            
//        };
//        try {
//            LightParser lp = new LightParser(this, callback, (NamespaceImpl) project.getGlobalNamespace());
//            try {
//                if( Diagnostic.DEBUG ) Diagnostic.trace("Parsing " + this.getName() + "...");
//                lp.parse();
//            }
//            catch( LightParser.SyntaxError e ) {
//                System.err.println(e.getMessage());
//            }
//        }
//        catch( IOException e ) {
//            e.printStackTrace(System.err);
//        }
//    }
    

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
        return project;
    }

    /** Just a convenient shortcut to eliminate casts */
    public ProjectBase getProjectImpl() {
        return project;
    }

    public String getName() {
        return fileBuffer.getFile().getName();
    }

    public List/*<CsmInclude>*/ getIncludes() {
        return new ArrayList(includes);
    }

    // TODO: review due to new CsmInclude interface implementation
    public void addInclude(String fileName, boolean system) {
        includes.add(new IncludeImpl(fileName, system, this));
    }

    public List/*<CsmDeclaration>*/ getDeclarations() {
        return new ArrayList(declarations.values());
    }
    
//    public List/*<CsmDeclaration>*/ getObjects() {
//        return objects;
//    }
    
    public void addMacro(CsmMacro macro) {
        macros.add(macro);
    }
    
    public List/*<CsmMacro>*/ getMacros() {
        return new ArrayList(macros);
    }
    
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        declarations.put(getSortKey(decl), decl);
        // TODO: remove this dirty hack!
	if( decl instanceof VariableImpl ) {
            VariableImpl v = (VariableImpl) decl;
	    if( isOfFileScope(v) ) {
		v.setScope(this);
	    }
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
        declarations.remove(getSortKey(declaration));
    }
    
    public static String getSortKey(CsmDeclaration declaration) {
        StringBuffer sb = new StringBuffer();
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

    public List getScopeElements() {
        List l = new ArrayList();
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
        CsmProject project = getProject();
        return project != null && project.isValid();    
    }

    public void setCallback(PPCallback callback) {
        this.callback = callback;
    }

    public void setPreprocState(APTPreprocState preprocState) {
        this.preprocState = preprocState;
    }
    
    public APTPreprocState.State getPreprocStateState() {
        if (preprocState != null) {
            return preprocState.getState();
        } else {
            return null;
        }
    }    
    
    public boolean isParsed() {
        return state == STATE_PARSED;
    }

    public void scheduleParsing(boolean wait) throws InterruptedException {
        scheduleParsing(wait, this.getPreprocStateState());
    }
  
    public void scheduleParsing(boolean wait, APTPreprocState.State ppStateState) throws InterruptedException {
        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("> File " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
        synchronized( stateLock ) {
            //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  sync " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
            while( ! isParsed() ) {
                ParserQueue.instance().addFirst(this, ppStateState);
                //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("  !prs " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
                if( wait ) {
                    stateLock.wait();
                }
                //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("< wait " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
            }
        }
        //if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("< File " + getName() + " @" + hashCode() + " waiting for parse; thread: " + Thread.currentThread().getName());
    }    
}
