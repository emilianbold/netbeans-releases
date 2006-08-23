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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.antlr2.MacroExpander;
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallback;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.apt.impl.support.parser.APTSystemStorage;
import org.netbeans.modules.cnd.modelimpl.apt.structure.APTFile;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTDriver;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.apt.utils.APTMacroUtils;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Disposable {

    /** Creates a new instance of CsmProjectImpl */
    public ProjectBase(ModelImpl model, Object platformProject, String name) {
        this.model = model;
        this.platformProject = platformProject;
        this.name = name;
        globalNamespace = new NamespaceImpl(this, true);
    }
    
    public CsmNamespace getGlobalNamespace() {
        return globalNamespace;
    }
    
    public String getName() {
        return name;
    }
    
    
    /** Gets an object, which represents correspondent IDE project */
    public Object getPlatformProject() {
        return platformProject;
    }

    /** Finds namespace by its qualified name */
    public CsmNamespace findNamespace( String qualifiedName, boolean findInLibraries ) {
        CsmNamespace result = findNamespace(qualifiedName);
        if( result == null && findInLibraries ) {
            for (Iterator it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = (CsmProject) it.next();
                result = lib.findNamespace(qualifiedName);
                if( result != null ) {
                    break;
                }
            }
        }
        return result;
    }
    
    /** Finds namespace by its qualified name */
    public CsmNamespace findNamespace( String qualifiedName ) {
        CsmNamespace nsp = (CsmNamespace) namespaces.get(qualifiedName);
        return nsp;
    }
    
    public NamespaceImpl findNamespace(NamespaceImpl parent, String name, boolean createIfNotFound) {
        String qualifiedName = (parent == null || parent.isGlobal()) ? name : parent.getQualifiedName() + "::" + name;
        NamespaceImpl nsp = (NamespaceImpl) namespaces.get(qualifiedName);
        if( createIfNotFound ) {
            if( nsp == null ) {
                nsp = new NamespaceImpl(this, parent, name);
            }
        }
        return nsp;
    }
    
    public void registerNamespace(NamespaceImpl namespace) {
        namespaces.put(namespace.getQualifiedName(),  namespace);
    }
    
    public CsmClassifier findClassifier(String qualifiedName, boolean findInLibraries) {
        CsmClassifier result = findClassifier(qualifiedName);
        if( result == null && findInLibraries ) {
            for (Iterator it = getLibraries().iterator(); it.hasNext();) {
                CsmProject lib = (CsmProject) it.next();
                result = lib.findClassifier(qualifiedName);
                if( result != null ) {
                    break;
                }
            }
        }
        return result;
    }
    
    public CsmClassifier findClassifier(String qualifiedName) {
        CsmClassifier result = (CsmClassifier) classifiers.get(qualifiedName);
        if( result == null ) {
//TODO think over: of the project isn't completely parsed, classifier might not be found
//            waitParse();
            
            
//            result = (CsmCompoundClassifier) classifiers.get(qualifiedName);
//            if( result == null ) {
//                CsmDeclaration decl = (CsmDeclaration) declarations.get(qualifiedName);
//                if( decl != null && decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
//                    CsmTypedef typedef = (CsmTypedef) decl;
//                    CsmType type = typedef.getType();
//                    if( type != null ) {
//                        CsmClassifier classifier = type.getClassifier();
//                        if( classifier instanceof CsmCompoundClassifier ) {
//                            result = (CsmCompoundClassifier) classifier;
//                        }
//                    }
//                }
//            }
        }
        return result;
    }

    public CsmDeclaration findDeclaration(String uniqueName) {
        CsmDeclaration result = (CsmDeclaration) declarations.get(uniqueName);
//TODO think over: of the project isn't completely parsed, declaration might not be found
//        if( result == null ) {
//            waitParse();
//            result = (CsmDeclaration) declarations.get(uniqueName);
//        }
        return result;
    }
    
//    public void registerClassifier(ClassEnumBase ce) {
//        classifiers.put(ce.getQualifiedName(), ce);
//        registerDeclaration(ce);
//    }
    
    public void registerDeclaration(CsmDeclaration decl) {
        
        // don't put unnamed declarations into namespace
        if( decl.getName().length() == 0 ) {
            return;
        }
        
        declarations.put(decl.getUniqueName(),  decl);
        
	if( decl instanceof CsmClassifier ) {
	    classifiers.put(decl.getQualifiedName(), decl);
	}
    }
    
    public void unregisterDeclaration(CsmDeclaration decl) {
        declarations.remove(decl.getUniqueName());
    }

//    public Collection /*<CsmFile>*/ getFiles() {
//        return files;
//    }
    
    
    public void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if( insideParser ) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err);
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        if( insideParser ) {
            return;
        }
        if( ! insideParser && ParserQueue.instance().hasFiles(this) ) {
            try {
                synchronized( waitParseLock ) {
                    waitParseLock.wait();
                }
            } catch (InterruptedException ex) {
                // do nothing
            }
        }
    }
    
//    protected synchronized void parseAllIfNeed() {
//        ModelSupport.instance().visitProjectFiles(this, platformProject, new FileVisitor() {
//            public void visit(NativeFileItem file) throws FileVisitor.StopException {
//                createIfNeed(file);
//            }
//        });
//    }

    protected synchronized void ensureChangedFilesEnqueued() {
    }
    
    protected synchronized boolean hasChangedFiles() {
        return false;
    }
    
    protected synchronized void ensureFilesCreated() {
        if( first ) {
            first = false;
            ParserQueue.instance().onStartAddingProjectFiles(this);
	    ModelSupport.instance().registerProjectListeners(this, platformProject);
            ModelSupport.instance().visitProjectFiles(this, platformProject, new FileVisitor() {
                public void visit(NativeFileItem file) throws FileVisitor.StopException {
                    createIfNeed(file);
                }
            });
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }
    
    protected abstract PPCallback getDefaultCallback(File file);
    
        
    protected APTPreprocState createDefaultPreprocState(File file) {
        return new APTPreprocStateImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl());
    }
    
    protected PPCallback getDefaultCallback(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        assert (nativeFile.getFile() != null);
        PPCallback callback = updateCallback(getDefaultCallback(nativeFile.getFile()), nativeFile);      
        return callback;
    }
    
    // copy
    protected APTPreprocState getDefaultPreprocState(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocState preprocState = new APTPreprocStateImpl(macroMap, inclHandler);
        return preprocState;
    }
    
    public static void fillCallback(PPCallback callback, 
            List/*<String>*/ userMacros, List/*<String>*/ sysMacros, 
            List/*<String>*/ quotedIncludePaths, List/*<String>*/systemIncludePaths, 
            File file) {
        // update callback with user macros information      
        putMacrosInCallback(callback, userMacros, false, file);

        // update callback with system macros information
        putMacrosInCallback(callback, sysMacros, true, file);       
        // update callback with include paths
        if (Diagnostic.DEBUG) {
            if (quotedIncludePaths.size() > 0) {
                System.err.println("adding quoted include paths " + quotedIncludePaths +
                        " to callback of file " + file.getAbsolutePath());
            }          
        }
        callback.addQuoteIncludePaths(quotedIncludePaths);

        if (Diagnostic.DEBUG) {
            if (systemIncludePaths.size() > 0) {
                System.err.println("adding system include paths " + systemIncludePaths +
                        " to callback of file " + file.getAbsolutePath());
            }          
        }
        callback.addSystemIncludePaths(systemIncludePaths);
    }
    
    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        List userIncludePaths = nativeFile.getUserIncludePaths();
        List sysIncludePaths = nativeFile.getSystemIncludePaths();
        sysIncludePaths = sysAPTData.getIncludes(sysIncludePaths.toString(), sysIncludePaths);
        return new APTIncludeHandlerImpl(sysIncludePaths, userIncludePaths);
    }
    
    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        List/*<String>*/ userMacros = nativeFile.getUserMacroDefinitions();
        List/*<String>*/ sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap sysMap = getSysMacroMap(sysMacros);
        APTFileMacroMap map = new APTFileMacroMap(sysMap);
        APTMacroUtils.fillMacroMap(map, userMacros);
        return map;        
    }
    
    private APTMacroMap getSysMacroMap(List/*<String>*/ sysMacros)
    {   
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }
    
    private PPCallback updateCallback(PPCallback callback, NativeFileItem nativeFile) {
        fillCallback(callback, 
                    nativeFile.getUserMacroDefinitions(), 
                    nativeFile.getSystemMacroDefinitions(), 
                    nativeFile.getUserIncludePaths(), 
                    nativeFile.getSystemIncludePaths(), 
                    nativeFile.getFile());
        return callback;
    }
    
    static private void putMacrosInCallback(PPCallback callback, List/*<String>*/ macros, boolean system, File file) {
        // update callback with user macros information
        for (Iterator it = macros.iterator(); it.hasNext();) {
            String macro = (String) it.next();
            if (Diagnostic.DEBUG) {
                System.err.println("adding " + (system?"system":"user") +" macro with body " + macro +
                        " to callback of file " + file.getAbsolutePath());
            }
            MacroExpander.putMacroInCallback(macro, callback, false);
        }        
    } 
    
    public PPCallback getCallback(File file) { 
        PPCallback callback = getDefaultCallback(file);
        PPCallback.State state = (PPCallback.State) filesCallbacks.get(file.getAbsolutePath());
        if (state != null) {
            callback.setState(state);
        }        
        return callback;
    }
    
    // copy
    public APTPreprocState getPreprocState(File file) {
        APTPreprocState preprocState = createDefaultPreprocState(file);
        APTPreprocState.State state = (APTPreprocState.State) filesHandlers.get(file.getAbsolutePath());
        if (state != null) {
            preprocState.setState(state);
        }        
        return preprocState;
    }
    
    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public CsmFile testParseFile(String file, PPCallback callback) {
        PPCallback.State state = (PPCallback.State) filesCallbacks.get(file);
        boolean firsTime = (state == null);
        if( firsTime ) {           
            // remember the first callback state
            filesCallbacks.put(file, callback.getState());
        } else {            
            callback.setState(state);
        }               
        CsmFile csmFile = findFile(file, callback, true);
        return csmFile;        
    }
    
    /**
     * copy
     */
    public CsmFile testAPTParseFile(String file, APTPreprocState preprocState) {
        APTPreprocState.State state = (APTPreprocState.State) filesHandlers.get(file);
        boolean firsTime = (state == null);
        if( firsTime ) {           
            // remember the first callback state
            filesHandlers.put(file, preprocState.getState());
        } else {            
            preprocState.setState(state);
        }               
        CsmFile csmFile = findFile(file, preprocState, true);
        return csmFile;        
    }
    
    public void invalidateFiles() {
        filesCallbacks.clear();
        filesHandlers.clear();
        for (Iterator it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.invalidateFiles();
        }
    }
    /**
     * called to inform that file was #included from another file with specific callback
     * @param file included file path
     * @param callback callback with which the file is including
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public CsmFile onFileIncluded(String file, PPCallback callback) {
        PPCallback.State state = (PPCallback.State) filesCallbacks.get(file);
        boolean firsTime = (state == null);
        if( firsTime ) {
            // TODO: now remember the first callback state
            filesCallbacks.put(file, callback.getState());
        }   
        if( ONLY_LEX_INCLUDES || 
                (NO_REPARSE_INCLUDE && LEX_NEXT_INCLUDES && !firsTime )) {          
            return null;
        }            
            
        CsmFile csmFile = findFile(file, callback, false);
        if( NO_REPARSE_INCLUDE ) {
            if (csmFile instanceof FileImpl) {             
                ((FileImpl) csmFile).ensureParsed(callback);
            }
            return csmFile;
        }
        if (/*!firsTime &&*/ csmFile instanceof FileImpl) {             
            FileImpl fileImpl = (FileImpl)csmFile;
            // need to reparse file
            // we needn't restore old callback since it's always null
            fileImpl.parse(callback);
        }
        return csmFile;
    }   
    
    public static final int GATHERING_MACROS    = 0;
    public static final int GATHERING_TOKENS    = 1;
    
    /**
     * COPY
     * called to inform that file was #included from another file with specific preprocState
     * 
     * @param file included file path
     * @param preprocState preprocState with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(String file, APTPreprocState preprocState, int mode) {
        APTPreprocState.State state = null;
        if (mode == GATHERING_TOKENS) {
            state = preprocState.getState();
            if( filesHandlers.get(file) == null ) {
                // TODO: now remember the first preprocState state
                filesHandlers.put(file, state);
            }   
        }
        
        FileImpl csmFile = (FileImpl)findFile(file, preprocState, false);            
        // first of all gather macro map from all includes
        try {
            APTFile apt = APTDriver.getInstance().findAPT(csmFile.getBuffer());
            APTParseFileWalker walker = new APTParseFileWalker(apt, csmFile, preprocState);            
            walker.visit();                
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        
        if (mode == GATHERING_TOKENS) {
            scheduleIncludedFileParsing(csmFile, state);
        }
        
        return csmFile;
    }   
    
    protected Map/*<String, PPCallback.State>*/ filesCallbacks = new HashMap(/*<File, PPCallback.State>*/);
    protected Map/*<String, APTPreprocState.State>*/ filesHandlers = new HashMap(/*<File, APTPreprocState.State>*/);
 
    public ProjectBase resolveFileProject(String absPath) {
        ProjectBase owner = null;
        // check own files
        if (getFiles().containsKey(new File(absPath))) {
            owner = this;
        } else {
            // else check in libs
            for (Iterator it = getLibraries().iterator(); it.hasNext() && (owner == null);) {
                ProjectBase lib = (ProjectBase) it.next();
                assert (lib != null);
                owner = lib.resolveFileProject(absPath);
            }
        }
        return owner;
    }

    protected abstract void createIfNeed(NativeFileItem nativeFile);
    protected abstract FileImpl findFile(File file, PPCallback callback, boolean scheduleParseIfNeed);
    protected abstract FileImpl findFile(File file, APTPreprocState preprocState, boolean scheduleParseIfNeed);
    public abstract void onFileAdded(NativeFileItem nativeFile);
    public abstract void onFileRemoved(NativeFileItem nativeFile);
    protected abstract void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state);

    public CsmFile findFile(String absolutePath) {
        if (TraceFlags.USE_APT) {
            return findFile(absolutePath, (APTPreprocState)null, true);
        } else {
            return findFile(absolutePath, (PPCallback)null, true);
        }
    }
    
    public FileImpl findFile(String absolutePath, PPCallback callback, boolean scheduleParseIfNeed) {
        return findFile(new File(absolutePath), callback, scheduleParseIfNeed);
    }
    
    // copy
    public FileImpl findFile(String absolutePath, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        return findFile(new File(absolutePath), preprocState, scheduleParseIfNeed);
    }
    
    protected Map getFiles() {
        return files;
    }
    
    public Collection/*<CsmProject>*/ getLibraries() {
        ProjectBase lib = getModel().getLibrary("/usr/include");
        return lib == null ? Collections.EMPTY_LIST : Collections.singletonList(lib);
    }
    
    protected ModelImpl getModelImpl() {
        return null;
    }
    
    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map
     * @param nameTokens name
     * @param nameTokens file file that contains unresolved name (used for the purpose of statictics)
     * @param nameTokens name offset that contains unresolved name (used for the purpose of statictics)
     */
    public CsmClass getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset) {
        if( unresolved == null ) {
            unresolved = new Unresolved(this);
        }
        if (Diagnostic.needStatistics()) Diagnostic.onUnresolvedError(nameTokens, file, offset);
        return unresolved.getDummyForUnresolved(nameTokens);
    }
    
    public boolean isValid() {
        return platformProject != null;
    }
    
    public void dispose() {
        ParserQueue.instance().removeAll(this);
        for (Iterator it = getFiles().values().iterator(); it.hasNext();) {
            FileImpl file = (FileImpl) it.next();
            APTDriver.getInstance().invalidateAPT(file.getBuffer());
        }        
        platformProject = null;
    }
    
    protected ModelImpl getModel() {
        return model;
    }
    
    public void onFileEditStart(FileBuffer buf) {
    }

    public void onFileEditEnd(FileBuffer buf) {
    }
    
    public boolean isStable() {
        if( ! first ) {
            if( ! hasChangedFiles() ) {
                return ! ParserQueue.instance().hasFiles(this);
            }
        }
        return false;
    }
    
    public void onParseFinish() {
        synchronized( waitParseLock ) {
            waitParseLock.notifyAll();
        }
    }

    /** 
     * We'd better name this getFiles(); 
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public Collection/*CsmFile*/ getFileList() {
	return new ArrayList(files.values());
    }
    
    private boolean first = true;

    private Object waitParseLock = new Object();
    
    private ModelImpl model;
    private Unresolved unresolved;
    private String name = "<MyProject>";
    private NamespaceImpl globalNamespace;
    private Object platformProject;
    private Map/*<String, CsmNamespaceImpl>*/ namespaces = new HashMap/*<String, CsmNamespaceImpl>*/();
    private Map/*<File, FileImpl>*/ files = new HashMap(/*<File, FileImpl>*/);
    private Map/*<String, ClassImpl>*/ classifiers = new HashMap(/*<String, ClassImpl>*/);
    private Map/*<String, ClassImpl>*/ declarations = new HashMap(/*<String, CsmDeclaration>*/);
//    private Collection files = new HashSet();
    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = new APTSystemStorage();
    
    //private NamespaceImpl fakeNamespace;
    
    // test variables.
    protected static final boolean NO_REPARSE_INCLUDE = Boolean.getBoolean("cnd.modelimpl.no.reparse.include");
    protected static final boolean LEX_NEXT_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.next.include");
    protected static final boolean ONLY_LEX_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.include");
    protected static final boolean ONLY_LEX_SYS_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.sys.include");
}
