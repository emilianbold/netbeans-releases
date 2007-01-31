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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTPreprocStateImpl;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTSystemStorage;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.utils.APTMacroUtils;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Disposable, Persistent {
    
    /** Creates a new instance of CsmProjectImpl */
    public ProjectBase(ModelImpl model, Object platformProject, String name) {
        this.model = model;
        this.platformProject = platformProject;
        this.name = name;
        _setGlobalNamespace(new NamespaceImpl(this, true));
    }
    
    public CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
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
        CsmNamespace nsp = _getNamespace( qualifiedName );
        return nsp;
    }
    
    public NamespaceImpl findNamespace(NamespaceImpl parent, String name, boolean createIfNotFound) {
        String qualifiedName = (parent == null || parent.isGlobal()) ? name : parent.getQualifiedName() + "::" + name; // NOI18N
        NamespaceImpl nsp = _getNamespace(qualifiedName);
        if( nsp == null && createIfNotFound ) {
            synchronized (namespaceLock){
                nsp = _getNamespace(qualifiedName);
                if( nsp == null ) {
                    nsp = new NamespaceImpl(this, parent, name);
                }
            }
        }
        return nsp;
    }
    
    public void registerNamespace(NamespaceImpl namespace) {
        _putNamespace(namespace.getQualifiedName(),  namespace);
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
            String qn = decl.getQualifiedName();
            if (!classifiers.containsKey(qn)){
                classifiers.put(qn, decl);
            }
        }
    }
    
    public void unregisterDeclaration(CsmDeclaration decl) {
        declarations.remove(decl.getUniqueName());
        if( decl instanceof CsmClassifier ) {
            classifiers.remove(decl.getQualifiedName());
        }
    }
    
//    public Collection /*<CsmFile>*/ getFiles() {
//        return files;
//    }
    
    
    public void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if( insideParser ) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        if( insideParser ) {
            return;
        }
        while ( ! insideParser && ParserQueue.instance().hasFiles(this, null) ) {
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
    
    protected void ensureChangedFilesEnqueued() {
    }
    
    /**
     * @param skipFile if null => check all files, otherwise skip checking
     * this file
     *
     */
    protected boolean hasChangedFiles(CsmFile skipFile) {
        return false;
    }
    
    protected synchronized void ensureFilesCreated() {
        if( status ==  Status.Initial ) {
            try {
                status = Status.AddingFiles;
                ParserQueue.instance().onStartAddingProjectFiles(this);
                ModelSupport.instance().registerProjectListeners(this, platformProject);
                ModelSupport.instance().visitProjectFiles(this, platformProject, new FileVisitor() {
                    public void visit(NativeFileItem file, boolean isSourceFile) throws FileVisitor.StopException {
                        if( ProjectBase.this.isProjectDisposed ) {
                            if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ProjevtBase.ensureFilesCreated interrupted");
                            // TODO: remove visitor and this exception-driven flow
                            throw new FileVisitor.StopException();
                        }
                        createIfNeed(file, isSourceFile);
                    }
                });
                ParserQueue.instance().onEndAddingProjectFiles(this);
            } finally {
                status = Status.Ready;
            }
        }
    }
    
    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public void onAddedToModel() {
        if( status == Status.Initial ) {
            Runnable r = new Runnable() {
                public void run() {
                    ensureFilesCreated();
                }
            };
            CodeModelRequestProcessor.instance().post(r, "Filling parser queue for " + getName()); // NOI18N
        }
    }
    
    
    protected APTPreprocState createDefaultPreprocState(File file) {
        return new APTPreprocStateImpl(new APTFileMacroMap(), new APTIncludeHandlerImpl(getFileKey(file)), false);
    }
    
    protected APTPreprocState getDefaultPreprocState(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocState preprocState = new APTPreprocStateImpl(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocState;
    }
    
    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List userIncludePaths = nativeFile.getUserIncludePaths();
        List sysIncludePaths = nativeFile.getSystemIncludePaths();
        sysIncludePaths = sysAPTData.getIncludes(sysIncludePaths.toString(), sysIncludePaths);
        return new APTIncludeHandlerImpl(getFileKey(nativeFile.getFile()), sysIncludePaths, userIncludePaths);
    }
    
    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List/*<String>*/ userMacros = nativeFile.getUserMacroDefinitions();
        List/*<String>*/ sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap sysMap = getSysMacroMap(sysMacros);
        APTFileMacroMap map = new APTFileMacroMap(sysMap);
        APTMacroUtils.fillMacroMap(map, userMacros);
        return map;
    }
    
    protected boolean isSourceFile(NativeFileItem nativeFile){
        return nativeFile.getSystemIncludePaths().size()>0;
//        NativeProject prj = nativeFile.getNativeProject();
//        if (prj != null){
//            return prj.getAllSourceFiles().contains(nativeFile);
//        }
//        return false;
    }
    
    private APTMacroMap getSysMacroMap(List/*<String>*/ sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }
    
    /*package*/ final APTPreprocState getPreprocState(File file) {
        APTPreprocState preprocState = createDefaultPreprocState(file);
        APTPreprocState.State state = getPreprocStateState(file);
        preprocState = restorePreprocState(file, preprocState, state);
        return preprocState;
    }
    
    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public CsmFile testAPTParseFile(String file, APTPreprocState preprocState) {
        APTPreprocState.State state = (APTPreprocState.State) getPreprocStateState(new File(file));
        boolean firsTime = (state == null);
        if( firsTime ) {
            // remember the first callback state
            putPreprocStateState(new File(file), preprocState.getState());
        } else {
            preprocState = restorePreprocState(new File(file), preprocState, state);
        }
        CsmFile csmFile = findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true);
        return csmFile;
    }
    
    protected void putPreprocStateState(File file, APTPreprocState.State state) {
        String path = getFileKey(file);
        filesHandlers.put(path, state);
        if (TRACE_PP_STATE_OUT) {
            System.err.println("\nPut state for file" + path + "\n");
            System.err.println(state);
        }
    }
    
    protected APTPreprocState.State getPreprocStateState(File file) {
        String path = getFileKey(file);
        return (APTPreprocState.State) filesHandlers.get(path);
    }
    
    public void invalidateFiles() {
        filesHandlers.clear();
        for (Iterator it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.invalidateFiles();
        }
    }
    
    public static final int GATHERING_MACROS    = 0;
    public static final int GATHERING_TOKENS    = 1;
    
    /**
     * called to inform that file was #included from another file with specific preprocState
     *
     * @param file included file path
     * @param preprocState preprocState with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(String file, APTPreprocState preprocState, int mode) {
        FileImpl csmFile = (FileImpl)findFile(file, FileImpl.HEADER_FILE, preprocState, false);
        
        APTPreprocState.State state = updateFileStateIfNeeded(csmFile, preprocState);
        
        // gather macro map from all includes
        APTFile aptLight = getAPTLight(csmFile);
        if (aptLight != null) {
            APTParseFileWalker walker = new APTParseFileWalker(aptLight, csmFile, preprocState);
            walker.visit();
        }
        
        if (state != null) {
            scheduleIncludedFileParsing(csmFile, state);
        }
        return csmFile;
    }
    
//    protected boolean needScheduleParsing(FileImpl file, APTPreprocState preprocState) {
//        APTPreprocState.State curState = (APTPreprocState.State) filesHandlers.get(file);
//        if (curState != null && !curState.isStateCorrect() && preprocState != null && preprocState.isStateCorrect()) {
//            return true;
//        }
//        return !file.isParsingOrParsed() || !TraceFlags.APT_CHECK_GET_STATE ;
//    }
    
    protected APTPreprocState.State updateFileStateIfNeeded(FileImpl csmFile, APTPreprocState preprocState) {
        APTPreprocState.State state = null;
        File file = csmFile.getBuffer().getFile();
        APTPreprocState.State curState = (APTPreprocState.State) getPreprocStateState(file);
        boolean update = false;
        if (curState == null) {
            update = true;
        } else if (!curState.isStateCorrect() && preprocState.isStateCorrect()) {
            update = true;
            // invalidate file
            csmFile.stateChanged(null, true);
        }
        if( update ) {
            state = preprocState.getState();
            putPreprocStateState(file, state);
        }
        return state;
    }
    
    protected Map/*<String, APTPreprocState.State>*/ filesHandlers = Collections.synchronizedMap(new HashMap(/*<File, APTPreprocState.State>*/));
    
    public ProjectBase resolveFileProject(String absPath) {
        return resolveFileProject(absPath, false);
    }
    
    public ProjectBase resolveFileProjectOnInclude(String absPath) {
        return resolveFileProject(absPath, true);
    }
    
    protected ProjectBase resolveFileProject(String absPath, boolean onInclude) {
        ProjectBase owner = null;
        // check own files
        if (getFile(new File(absPath)) != null) {
            owner = this;
        } else {
            // else check in libs
            for (Iterator it = getLibraries().iterator(); it.hasNext() && (owner == null);) {
                LibProjectImpl lib = (LibProjectImpl) it.next();
                assert (lib != null);
                owner = lib.resolveFileProject(absPath, false);
                if (owner == null) {
                    Object p = getPlatformProject();
                    if (p instanceof NativeProject){
                        owner = lib.resolveFileProject(absPath, onInclude, ((NativeProject)p).getSystemIncludePaths());
                    }
                }
            }
        }
        // during include phase of parsing process we should help user with project
        // config. If he forgot to add header to project we should add them anyway to not lost
        if (owner == null && onInclude) {
            owner = this;
        }
        return owner;
    }
    
    protected abstract void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile);
    protected abstract FileImpl findFile(File file, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed);
    public abstract void onFileAdded(NativeFileItem nativeFile);
    public abstract void onFileRemoved(NativeFileItem nativeFile);
    protected abstract void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state);
    
    public CsmFile findFile(String absolutePath) {
        APTPreprocState preprocState = null;
        if (getPreprocStateState(new File(absolutePath)) == null){
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject){
                NativeProject prj = (NativeProject)getPlatformProject();
                if (prj != null){
                    NativeFileItem nativeFile = prj.findFileItem(new File(absolutePath));
                    if( nativeFile == null ) {
                        nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    preprocState = getDefaultPreprocState(nativeFile);
                }
            }
        }
        return findFile(absolutePath, FileImpl.UNDEFINED_FILE, preprocState, true);
    }
    
    public FileImpl findFile(String absolutePath, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed) {
        return findFile(new File(absolutePath), fileType, preprocState, scheduleParseIfNeed);
    }
    
    protected Map getFiles() {
        return files;
    }
    
    public FileImpl getFile(File file) {
        String path = getFileKey(file);
        return (FileImpl) files.get(path);
    }
    
    protected void removeFile(File file) {
        String path = getFileKey(file);
        files.remove(path);
    }
    
    protected void putFile(File file, FileImpl impl) {
        String path = getFileKey(file);
        files.put(path, impl);
    }
    
    private String getFileKey(File file) {
        String key = null;
        if (TraceFlags.USE_CANONICAL_PATH) {
            try {
                key = file.getCanonicalPath();
            } catch (IOException ex) {
                key = file.getAbsolutePath();
            }
        } else {
            key = file.getAbsolutePath();
        }
        return FilePathCache.getString(key);
    }
    
    public Collection/*<CsmProject>*/ getLibraries() {
        ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
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
        return platformProject != null  && !isProjectDisposed;
    }
    
    public void setDisposed(){
        isProjectDisposed = true;
        ParserQueue.instance().removeAll(this);
    }
    
    public boolean isDisposed() {
        return isProjectDisposed;
    }
    
    public void dispose() {
        isProjectDisposed = true;
        ParserQueue.instance().removeAll(this);
        ArrayList list = new ArrayList(getFiles().values());
        for (Iterator it = list.iterator(); it.hasNext();) {
            FileImpl file = (FileImpl) it.next();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(file);
            } else {
                APTDriver.getInstance().invalidateAPT(file.getBuffer());
            }
        }
        platformProject = null;
        // we have clear all collections
        // to protect IDE against the code model client
        // that stores the instance of the project
        // and does not release it upon project closure
        _clearNamespaces();
        files.clear();
        classifiers.clear();
        declarations.clear();
        _setGlobalNamespace(new NamespaceImpl(this, true));
        filesHandlers.clear();
        sysAPTData = new APTSystemStorage();
        unresolved = null;
        uid = null;
    }
    
    private void _setGlobalNamespace(NamespaceImpl ns) {
        assert ns != null;
        if (TraceFlags.USE_REPOSITORY) {
            if (globalNamespaceUID != null) {
                RepositoryUtils.remove(globalNamespaceUID);
                globalNamespaceUID = null;
            }
            globalNamespaceUID = RepositoryUtils.put(ns);
        } else {
            globalNamespaceOLD = ns;
        }
    }
        
    private NamespaceImpl _getGlobalNamespace() {
        if (TraceFlags.USE_REPOSITORY) {
            NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(globalNamespaceUID);
            assert ns != null;
            return ns;
        } else {
            assert globalNamespaceOLD != null;
            return globalNamespaceOLD;
        }        
    }
    
    private NamespaceImpl _getNamespace( String key ) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID uid = namespaces.get(key);
            NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(uid);
            return ns;
        } else {
            return namespacesOLD.get(key);
        }
    }
    
    private void _putNamespace( String key, NamespaceImpl ns ) {
        assert (key != null);
        assert (ns != null);
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID uid = RepositoryUtils.put(ns);
            assert uid != null;
            namespaces.put(key, uid);
            
        } else {
            namespacesOLD.put(key, ns);
        }
    }
    
    private void _clearNamespaces() {
        if (TraceFlags.USE_REPOSITORY) {
            Collection<CsmUID> uids = namespaces.values();
            RepositoryUtils.remove(uids);
            namespaces.clear();
        } else {
            namespacesOLD.clear();
        }
    }
    
    protected ModelImpl getModel() {
        return model;
    }
    
    public void onFileEditStart(FileBuffer buf) {
    }
    
    public void onFileEditEnd(FileBuffer buf) {
    }
    
    private CsmUID uid = null;
    public CsmUID getUID() {
        if (uid == null) {
            uid = UIDUtilities.createProjectUID(this);
        }
        return uid;
    }
    
    public boolean isStable(CsmFile skipFile) {
        if( status == Status.Ready ) {
            if( ! hasChangedFiles(skipFile) ) {
                return ! ParserQueue.instance().hasFiles(this, (FileImpl)skipFile);
            }
        }
        return false;
    }
    
    public void onParseFinish() {
        synchronized( waitParseLock ) {
            waitParseLock.notifyAll();
        }
        for (Iterator it = getFileList().iterator(); it.hasNext();) {
            FileImpl file= (FileImpl) it.next();
            file.fixFakeRegistrations();
        }
    }
    
    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public Collection/*FileImpl*/ getFileList() {
        return new ArrayList(files.values());
    }
    
    public Collection getSourceFiles() {
        List res = new ArrayList();
        for(Iterator i = getFileList().iterator(); i.hasNext();){
            FileImpl file = (FileImpl)i.next();
            if (file.isSourceFile())
                res.add(file);
        }
        return res;
    }
    
    public Collection getHeaderFiles() {
        List res = new ArrayList();
        for(Iterator i = getFileList().iterator(); i.hasNext();){
            FileImpl file = (FileImpl)i.next();
            if (file.isHeaderFile())
                res.add(file);
        }
        return res;
    }
    
    public long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return files.size();
    }
    
    public String toString() {
        return getName() + ' ' + getClass().getName() + " @" + hashCode(); // NOI18N
    }
    
    /**
     * Repository Serialization
     */
    public void write(OutputStream out) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    /**
     * Repository Deserialization
     */
    public void read(InputStream in) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
    
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    public static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && (DebugUtils.getBoolean("cnd.remember.restored", false) || TRACE_PP_STATE_OUT);
    
    /*package*/final void cleanPreprocStateAfterParse(FileImpl fileImpl) {
        if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
            APTPreprocState.State state = (APTPreprocState.State) getPreprocStateState(fileImpl.getBuffer().getFile());
            if (TRACE_PP_STATE_OUT) System.err.println("was " + state);
            if (state != null && !state.isCleaned()) {
                state.cleanExceptIncludeStack();
            }
            if (TRACE_PP_STATE_OUT) System.err.println("after cleaning " + state);
        }
    }

    private APTPreprocState restorePreprocState(File interestedFile, APTPreprocState preprocState, APTPreprocState.State state) {
        if (state != null) {
            if (state.isCleaned()) {
                // walk through include stack to restore preproc information
                Stack reverseInclStack = state.cleanIncludeStack();
                // we need to reverse includes stack
                assert (reverseInclStack != null && !reverseInclStack.empty());
                Stack inclStack = new Stack();
                do {
                    inclStack.push(reverseInclStack.pop());
                } while (!reverseInclStack.empty());

                preprocState.setState(state);
                if (TRACE_PP_STATE_OUT) System.err.println("before restoring " + preprocState); // NOI18N
                APTIncludeHandler inclHanlder = preprocState.getIncludeHandler();
                assert inclHanlder != null;
                // start from the first file, then use include stack
                String startFile = inclHanlder.getStartFile();
                FileImpl csmFile = getFile(new File(startFile));
                assert csmFile != null;
                APTFile aptLight = getAPTLight(csmFile);
                if (aptLight != null) {
                    // for testing remember restored file
                    long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
                    int stackSize = inclStack.size();
                    APTWalker walker = new APTRestorePreprocStateWalker(aptLight, csmFile, preprocState, inclStack, getFileKey(interestedFile));
                    walker.visit();
                    if (REMEMBER_RESTORED) {
                        if (restoredFiles == null) {
                            restoredFiles = new ArrayList();
                        }
                        FileImpl interestedFileImpl = getFile(interestedFile);
                        assert interestedFileImpl != null;
                        String msg = interestedFile.getAbsolutePath() + 
                                " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                        time = System.currentTimeMillis() - time;
                        msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                        System.err.println("#" + restoredFiles.size() + " restored: " + msg); // NOI18N
                        restoredFiles.add(msg);
                    }
                    if (TRACE_PP_STATE_OUT) System.err.println("after restoring " + preprocState); // NOI18N
                    APTPreprocState.State fullState = preprocState.getState();
                    putPreprocStateState(interestedFile, fullState);
                }
            } else {
                preprocState.setState(state);
            }
        }
        return preprocState;
    }
    
    public APTFile getAPTLight(CsmFile csmFile) {
        APTFile aptLight = null;
        try {
            if (TraceFlags.USE_AST_CACHE) {
                aptLight = CacheManager.getInstance().findAPTLight(csmFile);
            } else {
                aptLight = APTDriver.getInstance().findAPTLight(((FileImpl)csmFile).getBuffer());
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }        
        return aptLight;
    }
    
    private static class DefaultFileItem implements NativeFileItem {
        
        private NativeProject project;
        private String absolutePath;
        
        public DefaultFileItem(NativeProject project, String absolutePath) {
            this.project = project;
            this.absolutePath = absolutePath;
        }
        
        public DefaultFileItem(NativeFileItem nativeFile) {
            this.project = nativeFile.getNativeProject();
            this.absolutePath = nativeFile.getFile().getAbsolutePath();
        }
        
        public List getUserMacroDefinitions() {
            return project.getUserMacroDefinitions();
        }
        
        public List getUserIncludePaths() {
            return project.getUserIncludePaths();
        }
        
        public List getSystemMacroDefinitions() {
            return project.getSystemMacroDefinitions();
        }
        
        public List getSystemIncludePaths() {
            return project.getSystemIncludePaths();
        }
        
        public NativeProject getNativeProject() {
            return project;
        }
        
        public File getFile() {
            return new File(absolutePath);
        }
        
    }
    
    /**
     * Represent the project status.
     *
     * Concerns only initial stage of project lifecycle:
     * allows to distingwish just newly-created project,
     * the phase when files are being added to project (and to parser queue)
     * and the phase when all files are already added.
     *
     * It isn't worth tracking further stages (stable/unstable)
     * since it's error prone (it's better to ask, say, parser queue
     * whether it contains files that belong to this projec tor not)
     */
    protected static enum Status {
        Initial,
        AddingFiles,
        Ready;
    }
    private Status status = Status.Initial;
    
    private Object waitParseLock = new Object();
    
    private ModelImpl model;
    private Unresolved unresolved;
    private String name = "<MyProject>"; // NOI18N
    
    // only one of globalNamespace/globalNamespaceOLD must be used (based on USE_REPOSITORY)
    private NamespaceImpl globalNamespaceOLD;
    private CsmUID globalNamespaceUID;
    
    private Object platformProject;
    private boolean isProjectDisposed;
    
    // only one of namespaces/namespacesOLD must be used (based on USE_REPOSITORY)
    private Map<String, NamespaceImpl> namespacesOLD = Collections.synchronizedMap(new HashMap<String, NamespaceImpl>());
    private Map<String, CsmUID> namespaces = Collections.synchronizedMap(new HashMap<String, CsmUID>());

    private Map/*<File, FileImpl>*/ files = Collections.synchronizedMap(new HashMap(/*<File, FileImpl>*/));
    private Map/*<String, ClassImpl>*/ classifiers = Collections.synchronizedMap(new HashMap(/*<String, ClassImpl>*/));
    private Map/*<String, ClassImpl>*/ declarations = Collections.synchronizedMap(new HashMap(/*<String, CsmDeclaration>*/));
//    private Collection files = new HashSet();
    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = new APTSystemStorage();
    
    private Object namespaceLock = new String("namespaceLock in Projectbase "+hashCode()); // NOI18N
    
    //private NamespaceImpl fakeNamespace;
    
    // test variables.
    protected static final boolean NO_REPARSE_INCLUDE = Boolean.getBoolean("cnd.modelimpl.no.reparse.include");
    protected static final boolean LEX_NEXT_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.next.include");
    protected static final boolean ONLY_LEX_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.include");
    protected static final boolean ONLY_LEX_SYS_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.sys.include");

    /**
     * for tests only
     */
    public static List testGetRestoredFiles() {
        return restoredFiles;
    }
    private static List restoredFiles = null;
}
