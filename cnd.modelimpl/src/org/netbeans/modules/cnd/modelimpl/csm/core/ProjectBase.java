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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
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
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Disposable, Persistent, SelfPersistent {
    
    /** Creates a new instance of CsmProjectImpl */
    public ProjectBase(ModelImpl model, Object platformProject, String name) {
        this.model = model;
        this.platformProject = platformProject;
        this.name = ProjectNameCache.getString(name);
        this.fqn = null;
        if (TraceFlags.USE_REPOSITORY) {
            // remember in repository
            RepositoryUtils.hang(this);
        }
        // create global namespace
        NamespaceImpl ns = new NamespaceImpl(this);
        assert ns != null;
        if (TraceFlags.USE_REPOSITORY) {
            this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
            this.globalNamespaceOLD = null;
        } else {
            this.globalNamespaceOLD = ns;
            this.globalNamespaceUID = null;
        }
        
        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
    }
    
    public CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }
    
    public String getName() {
        return name;
    }
    
    public String getQualifiedName() {
        if (this.fqn == null) {
            this.fqn = ProjectNameCache.getString(ModelSupport.instance().getProjectKey(this));
        }
        return this.fqn;
    }
    
    /** Gets an object, which represents correspondent IDE project */
    public Object getPlatformProject() {
        return platformProject;
    }
    
    /** Gets an object, which represents correspondent IDE project */
    protected void setPlatformProject(Object platformProject) {
        this.platformProject = platformProject;
        this.fqn = null;
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
    
    public NamespaceImpl findNamespaceCreateIfNeeded(NamespaceImpl parent, String name) {
        String qualifiedName = Utils.getNestedNamespaceQualifiedName(name, parent, true);
        NamespaceImpl nsp = _getNamespace(qualifiedName);
        if( nsp == null ) {
            synchronized (namespaceLock){
                nsp = _getNamespace(qualifiedName);
                if( nsp == null ) {
                    nsp = new NamespaceImpl(this, parent, name, qualifiedName);
                }
            }
        }
        return nsp;
    }
    
    public void registerNamespace(NamespaceImpl namespace) {
        _registerNamespace(namespace);
    }
    
    public void unregisterNamesace(NamespaceImpl namespace) {
        _unregisterNamespace(namespace);
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
        CsmClassifier result = _getClassifier(qualifiedName);
        return result;
    }
    
    public CsmDeclaration findDeclaration(String uniqueName) {
        return declarationsSorage.getDeclaration(uniqueName);
    }
    
    public Collection<CsmOffsetableDeclaration> findDeclarations(String uniqueName) {
        return declarationsSorage.findDeclarations(uniqueName);
    }
    
    private CsmClassifier _getClassifier(String qualifiedName) {
        CsmClassifier result;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmClassifier> uid = classifiers.get(qualifiedName);
            result = UIDCsmConverter.UIDtoDeclaration(uid);
        } else {
            result = (CsmClassifier) classifiersOLD.get(qualifiedName);
        }
        return result;
    }
    
    private boolean _putClassifier(CsmClassifier decl) {
        String qn = decl.getQualifiedName();
        if (TraceFlags.USE_REPOSITORY) {
            if (!classifiers.containsKey(qn)) {
                CsmUID<CsmClassifier> uid = UIDCsmConverter.declarationToUID(decl);
                assert uid != null;
                classifiers.put(qn, uid);
                assert (UIDCsmConverter.UIDtoDeclaration(uid) != null);
                return true;
            }
        } else {
            if (!classifiersOLD.containsKey(qn)){
                classifiersOLD.put(qn, decl);
                return true;
            }
        }
        return false;
    }
    
    
//    public void registerClassifier(ClassEnumBase ce) {
//        classifiers.put(ce.getNestedNamespaceQualifiedName(), ce);
//        registerDeclaration(ce);
//    }
    
    public static boolean canRegisterDeclaration(CsmDeclaration decl) {
        // WAS: don't put unnamed declarations
        assert decl != null;
        assert decl.getName() != null;
        if (decl.getName().length()==0) {
            return false;
        }
        CsmScope scope = decl.getScope();
        if (scope instanceof CsmCompoundClassifier) {
            return canRegisterDeclaration((CsmCompoundClassifier)scope);
        }
        return true;
    }
    
    public void registerDeclaration(CsmDeclaration decl) {
        
        if( !ProjectBase.canRegisterDeclaration(decl) ) {
            if (TraceFlags.TRACE_REGISTRATION) {
                System.err.println("not registered " + decl);
                if (TraceFlags.USE_REPOSITORY) {
                    System.err.println("not registered UID " + decl.getUID());
                }
            }
            
            return;
        }
        if (TraceFlags.CHECK_DECLARATIONS) {
            CsmDeclaration old = declarationsSorage.getDeclaration(decl.getUniqueName());
            if (old != null && old != decl) {
                System.err.println("\n\nRegistering different declaration with the same name:" + decl.getUniqueName());
                System.err.print("WAS:");
                new CsmTracer().dumpModel(old);
                System.err.print("\nNOW:");
                new CsmTracer().dumpModel(decl);
            }
        }
        declarationsSorage.putDeclaration(decl);
        
        if( decl instanceof CsmClassifier ) {
            String qn = decl.getQualifiedName();
            if (!_putClassifier((CsmClassifier)decl) && TraceFlags.CHECK_DECLARATIONS) {
                CsmClassifier old = _getClassifier(qn);
                if (old != null && old != decl) {
                    System.err.println("\n\nRegistering different classifier with the same name:" + qn);
                    System.err.print("ALREADY EXISTS:");
                    new CsmTracer().dumpModel(old);
                    System.err.print("\nFAILED TO ADD:");
                    new CsmTracer().dumpModel(decl);
                }
            }
        }
        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("registered " + decl);
            if (TraceFlags.USE_REPOSITORY) {
                System.err.println("registered UID " + decl.getUID());
            }
        }
        
    }
    
    public void unregisterDeclaration(CsmDeclaration decl) {
        if (TraceFlags.TRACE_REGISTRATION) {
            System.err.println("unregistered " + decl);
            if (TraceFlags.USE_REPOSITORY) {
                System.err.println("unregistered UID " + decl.getUID());
            }
        }
        if( decl instanceof CsmClassifier ) {
            _removeClassifier(decl);
        }
        declarationsSorage.removeDeclaration(decl);
    }
    
    
    private void _removeClassifier(CsmDeclaration decl) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmClassifier> uid = classifiers.remove(decl.getQualifiedName());
            assert (uid == null) || (UIDCsmConverter.UIDtoCsmObject(uid) != null) : " no object for UID " + uid;
        } else {
            classifiersOLD.remove(decl.getQualifiedName());
        }
    }
    
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
    
    public boolean isLanguageSupported(NativeFileItem.Language language) {
        return language == NativeFileItem.Language.C || language == NativeFileItem.Language.CPP || language == NativeFileItem.Language.C_HEADER;
    }
    
    protected synchronized void ensureFilesCreated() {
        if( status ==  Status.Initial ) {
            try {
                status = Status.AddingFiles;
                if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                    System.err.println("suspend queue");
                    ParserQueue.instance().suspend();
                }
                ParserQueue.instance().onStartAddingProjectFiles(this);
                ModelSupport.instance().registerProjectListeners(this, platformProject);
                NativeProject nativeProject = ModelSupport.instance().getNativeProject(platformProject);
                if( nativeProject != null ) {
                    createProjectFilesIfNeed(nativeProject);
                }
                if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                    try {
                        System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                        Thread.currentThread().sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
                        System.err.println("woke up after sleep");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    ParserQueue.instance().resume();
                }
                ParserQueue.instance().onEndAddingProjectFiles(this);
            } finally {
                status = Status.Ready;
            }
        }
    }
    
    private void createProjectFilesIfNeed(NativeProject nativeProject) {
        
        if( TraceFlags.DEBUG ) Diagnostic.trace("Using new NativeProject API"); // NOI18N
        // first of all visit sources, then headers
        
        if( TraceFlags.TIMING ) {
            System.err.println("Getting files from project system");
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before getting files from project");
                Thread.currentThread().sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        long time = System.currentTimeMillis();
        final Set<NativeFileItem> removedFiles = Collections.synchronizedSet(new HashSet<NativeFileItem>());
        NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {
            public void fileAdded(NativeFileItem fileItem) {}
            public void filesAdded(List<NativeFileItem> fileItems) {}
            public void fileRemoved(NativeFileItem fileItem) { removedFiles.add(fileItem); }
            public void filesRemoved(List<NativeFileItem> fileItems) {removedFiles.addAll(fileItems);}
            public void fileRenamed(String oldPath, NativeFileItem newFileIetm){}
            public void filePropertiesChanged(NativeFileItem fileItem) {}
            public void filesPropertiesChanged(List<NativeFileItem> fileItems) {}
            public void filesPropertiesChanged() {}
        };
        nativeProject.addProjectItemsListener(projectItemListener);
        List<NativeFileItem> sources = nativeProject.getAllSourceFiles();
        List<NativeFileItem> headers = nativeProject.getAllHeaderFiles();
        
        if( TraceFlags.TIMING ) {
            time = System.currentTimeMillis() - time;
            System.err.println("Got files from project system. Time = " + time);
            System.err.println("FILES COUNT:\nSource files:\t" + sources.size() + "\nHeader files:\t" + headers.size() + "\nTotal files:\t" + (sources.size() + headers.size()));
        }
        if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
            try {
                System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec after getting files from project");
                Thread.currentThread().sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if(TraceFlags.DUMP_PROJECT_ON_OPEN ) {
            ModelSupport.instance().dumpNativeProject(nativeProject);
        }
        
        for( NativeFileItem nativeFileItem : sources ) {
            if (removedFiles.contains(nativeFileItem)){
                continue;
            }
	    synchronized (disposeLock) {
		if( ProjectBase.this.isProjectDisposed ) {
		    if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ProjevtBase.ensureFilesCreated interrupted");
		    return;
		}
		assert (nativeFileItem.getFile() != null) : "native file item must have valid File object";
		if( TraceFlags.DEBUG ) ModelSupport.instance().trace(nativeFileItem);
                try {
                    createIfNeed(nativeFileItem, true);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
	    }
        }
        
        for( NativeFileItem nativeFileItem : headers ) {
            if (removedFiles.contains(nativeFileItem)){
                continue;
            }
	    synchronized (disposeLock) {
		if( ProjectBase.this.isProjectDisposed ) {
		    if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ProjevtBase.ensureFilesCreated interrupted");
		    return;
		}
		assert (nativeFileItem.getFile() != null) : "native file item must have valid File object";
		if( TraceFlags.DEBUG ) ModelSupport.instance().trace(nativeFileItem);
                try {
                    createIfNeed(nativeFileItem, false);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
	    }
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
        // in fact if visitor used for parsing => visitor will parse all included files
        // recursively starting from current source file
        // so, when we visit headers, they should not be reparsed if already were parsed
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
        return new APTPreprocStateImpl(new APTFileMacroMap(),
                new APTIncludeHandlerImpl(FileContainer.getFileKey(file, true)), false);
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
        return new APTIncludeHandlerImpl(FileContainer.getFileKey(nativeFile.getFile(), true),
                sysIncludePaths, userIncludePaths);
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
    public CsmFile testAPTParseFile(String path, APTPreprocState preprocState) {
        File file = new File(path);
        APTPreprocState.State state = getPreprocStateState(file);
        if( state == null ) {
            // remember the first state
            return findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true, preprocState.getState());
        } else {
            preprocState = restorePreprocState(file, preprocState, state);
            return findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true, null);
        }
    }
    
    protected void putPreprocStateState(File file, APTPreprocState.State state) {
        fileContainer.putPreprocStateState(file, state);
    }
    
    protected APTPreprocState.State getPreprocStateState(File file) {
        return fileContainer.getPreprocStateState(file);
    }
    
    public void invalidateFiles() {
        fileContainer.clearState();
        for (Iterator it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.invalidateFiles();
        }
    }
    
    /**
     * called to inform that file was #included from another file with specific preprocState
     *
     * @param file included file path
     * @param preprocState preprocState with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(String file, APTPreprocState preprocState, int mode) throws IOException {
        FileImpl csmFile = findFile(new File(file), FileImpl.HEADER_FILE, preprocState, false, null);
        
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
            csmFile.stateChanged(true);
        }
        if( update ) {
            state = preprocState.getState();
            putPreprocStateState(file, state);
        }
        return state;
    }
    
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
    protected abstract FileImpl findFile(File file, int fileType, APTPreprocState preprocState, boolean scheduleParseIfNeed, APTPreprocState.State initialState);
    public abstract void onFileAdded(NativeFileItem nativeFile);
    //public abstract void onFileRemoved(NativeFileItem nativeFile);
    public abstract void onFileRemoved(File nativeFile);
    public abstract void onFilePropertyChanged(NativeFileItem nativeFile);
    public abstract void onFilePropertyChanged(List<NativeFileItem> items);
    protected abstract void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocState.State state);
    
    public CsmFile findFile(String absolutePath) {
        File file = new File(absolutePath);
        APTPreprocState preprocState = null;
        if (getPreprocStateState(file) == null){
            // Try to find native file
            if (getPlatformProject() instanceof NativeProject){
                NativeProject prj = (NativeProject)getPlatformProject();
                if (prj != null){
                    NativeFileItem nativeFile = prj.findFileItem(file);
                    if( nativeFile == null ) {
                        // if not belong to NB project => not our file
                        return null;
                        // nativeFile = new DefaultFileItem(prj, absolutePath);
                    }
                    if( ! isLanguageSupported(nativeFile.getLanguage()) ) {
                        return null;
                    }
                    preprocState = getDefaultPreprocState(nativeFile);
                }
            }
            if (preprocState != null) {
                return findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true, preprocState.getState());
            }
        }
        return findFile(file, FileImpl.UNDEFINED_FILE, preprocState, true, null);
    }
    
    public FileImpl getFile(File file) {
        return fileContainer.getFile(file);
    }
    
    protected void removeFile(File file) {
        fileContainer.removeFile(file);
    }
    
    protected void putFile(File file, FileImpl impl, APTPreprocState.State state) {
        fileContainer.putFile(file,impl, state);
    }
    
    public Collection<CsmProject> getLibraries() {
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
    
    public void setDisposed() {
	synchronized (disposeLock) {
	    isProjectDisposed = true;
	}
        ParserQueue.instance().removeAll(this);
    }
    
    public boolean isDisposed() {
        return isProjectDisposed;
    }
    
    public void dispose() {
	synchronized (disposeLock) {
	    isProjectDisposed = true;
	}
        ParserQueue.instance().removeAll(this);
        disposeFiles();
        
        platformProject = null;
        // we have clear all collections
        // to protect IDE against the code model client
        // that stores the instance of the project
        // and does not release it upon project closure
        _clearNamespaces();
        _clearClassifiers();
        declarationsSorage.clearDeclarations();
        if (TraceFlags.USE_DEEP_REPARSING) {
            getGraph().clear();
        }
        sysAPTData = new APTSystemStorage();
        unresolved = null;
        uid = null;
        RepositoryUtils.closeUnit(getUID());
    }
    
    private void _clearClassifiers() {
        if (TraceFlags.USE_REPOSITORY) {
            classifiers.clear();
        } else {
            classifiersOLD.clear();
        }
    }
    
    private void disposeFiles() {
        List<FileImpl> list;
        synchronized (fileContainer) {
            list = fileContainer.getFiles();
            fileContainer.clear();
        }
        for (FileImpl file : list){
            file.onProjectDispose();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(file);
            } else {
                APTDriver.getInstance().invalidateAPT(file.getBuffer());
            }
// don't remove files from repository on project closure
//            if (TraceFlags.USE_REPOSITORY) {
//                RepositoryUtils.remove(file.getUID());
//            }
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
            CsmUID<CsmNamespace> uid = namespaces.get(key);
            NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(uid);
            return ns;
        } else {
            return namespacesOLD.get(key);
        }
    }
    
    private void _registerNamespace(NamespaceImpl ns ) {
        assert (ns != null);
        String key = ns.getQualifiedName();
        assert (key != null);
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespace> uid = RepositoryUtils.put(ns);
            assert uid != null;
            namespaces.put(key, uid);
        } else {
            namespacesOLD.put(key, ns);
        }
    }
    
    private void _unregisterNamespace(NamespaceImpl ns ) {
        assert (ns != null);
        assert !ns.isGlobal();
        String key = ns.getQualifiedName();
        assert (key != null);
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespace> uid = namespaces.remove(key);
            assert uid != null;
            RepositoryUtils.remove(uid);
        } else {
            namespacesOLD.remove(key);
        }
    }
    
    private void _clearNamespaces() {
        if (TraceFlags.USE_REPOSITORY) {
            Collection<CsmUID<CsmNamespace>> uids = namespaces.values();
            RepositoryUtils.remove(uids);
            namespaces.clear();
        } else {
            namespacesOLD.clear();
        }
    }
    
    protected ModelImpl getModel() {
        return model;
    }
    
    public void onFileEditStart(FileBuffer buf, NativeFileItem nativeFile) {
    }
    
    public void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile) {
    }
    
    private CsmUID<CsmProject> uid = null;
    public final CsmUID<CsmProject> getUID() { // final because called from constructor
        if (uid == null) {
            uid = UIDUtilities.createProjectUID(this);
        }
        return uid;
    }
    
    public boolean isStable(CsmFile skipFile) {
        if( isStableStatus() ) {
//            if( ! hasChangedFiles(skipFile) ) {
            return ! ParserQueue.instance().hasFiles(this, (FileImpl)skipFile);
//            }
        }
        return false;
    }
    
    protected boolean isStableStatus() {
        return status == Status.Ready;
    }
    
    public void onParseFinish() {
        synchronized( waitParseLock ) {
            waitParseLock.notifyAll();
        }
        if( ! isProjectDisposed ) {
            for (Iterator it = getFileList().iterator(); it.hasNext();) {
		FileImpl file= (FileImpl) it.next();
		synchronized (disposeLock) {
		    if( isProjectDisposed ) {
			break;
		    }
		    file.fixFakeRegistrations();
		}
            }
        }
    }
    
    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public Collection<FileImpl> getFileList() {
        return fileContainer.getFiles();
    }
    
    public Collection<CsmFile> getSourceFiles() {
        List<CsmFile> res = new ArrayList<CsmFile>();
        for(Iterator<FileImpl> i = getFileList().iterator(); i.hasNext();){
            FileImpl file = i.next();
            if (file.isSourceFile())
                res.add(file);
        }
        return res;
    }
    
    public Collection<CsmFile> getHeaderFiles() {
        List<CsmFile> res = new ArrayList<CsmFile>();
        for(Iterator i = getFileList().iterator(); i.hasNext();){
            FileImpl file = (FileImpl)i.next();
            if (file.isHeaderFile())
                res.add(file);
        }
        return res;
    }
    
    public long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return fileContainer.getSize();
    }
    
    public String toString() {
        return getName() + ' ' + getClass().getName() + " @" + hashCode(); // NOI18N
    }
    
    /*package*/final void cleanPreprocStateAfterParse(FileImpl fileImpl, APTPreprocState.State state2Clean) {
        if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
            File file = fileImpl.getBuffer().getFile();
            Object stateLock = fileContainer.getLock(file);
            synchronized (stateLock) {
                APTPreprocState.State rememberedState = (APTPreprocState.State) getPreprocStateState(file);
                if (TRACE_PP_STATE_OUT) System.err.println("was " + rememberedState);
                if (rememberedState != null) {
                    if (state2Clean.equals(rememberedState)) {
                        if (!rememberedState.isCleaned()) {
                            if (TRACE_PP_STATE_OUT) System.err.println("cleaning for " + file.getAbsolutePath());
                            rememberedState.cleanExceptIncludeStack();
                        } else {
                            if (TRACE_PP_STATE_OUT) System.err.println("not need cleaning for " + file.getAbsolutePath());
                        }
                    } else {
                        if (TRACE_PP_STATE_OUT) System.err.println("don't need to clean replaced state for " + file.getAbsolutePath());
                    }
                }
                if (TRACE_PP_STATE_OUT) System.err.println("after cleaning " + rememberedState);
            }
        }
    }
    
    private APTPreprocState restorePreprocState(File interestedFile, APTPreprocState preprocState, APTPreprocState.State state) {
        if (state != null) {
            Object stateLock = fileContainer.getLock(interestedFile);
            synchronized (stateLock) {
                if (state.isCleaned()) {
                    if (TRACE_PP_STATE_OUT) System.err.println("restoring for " + interestedFile);
                    APTPreprocState.State cleanedState = state.copy();
                    // walk through include stack to restore preproc information
                    Stack reverseInclStack = cleanedState.cleanIncludeStack();
                    // we need to reverse includes stack
                    assert (reverseInclStack != null && !reverseInclStack.empty()) : "state of stack is " + reverseInclStack;
                    Stack inclStack = new Stack();
                    do {
                        inclStack.push(reverseInclStack.pop());
                    } while (!reverseInclStack.empty());
                    
                    APTPreprocState.State oldState = preprocState.getState();
                    preprocState.setState(cleanedState);
                    if (TRACE_PP_STATE_OUT) System.err.println("before restoring " + preprocState); // NOI18N
                    APTIncludeHandler inclHanlder = preprocState.getIncludeHandler();
                    assert inclHanlder != null;
                    // start from the first file, then use include stack
                    String startFile = inclHanlder.getStartFile();
                    FileImpl csmFile = getFile(new File(startFile));
                    if (csmFile == null) {
                        preprocState.setState(oldState);
                        return preprocState;
                    }
                    assert csmFile != null;
                    APTFile aptLight = null;
                    try {
                        aptLight = getAPTLight(csmFile);
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                    if (aptLight != null) {
                        // for testing remember restored file
                        long time = REMEMBER_RESTORED ? System.currentTimeMillis() : 0;
                        int stackSize = inclStack.size();
                        APTWalker walker = new APTRestorePreprocStateWalker(aptLight, csmFile,
                                preprocState, inclStack, FileContainer.getFileKey(interestedFile, false));
                        walker.visit();
                        if (REMEMBER_RESTORED) {
                            if (testRestoredFiles == null) {
                                testRestoredFiles = new ArrayList();
                            }
                            FileImpl interestedFileImpl = getFile(interestedFile);
                            assert interestedFileImpl != null;
                            String msg = interestedFile.getAbsolutePath() +
                                    " [" + (interestedFileImpl.isHeaderFile() ? "H" : interestedFileImpl.isSourceFile() ? "S" : "U") + "]"; // NOI18N
                            time = System.currentTimeMillis() - time;
                            msg = msg + " within " + time + "ms" + " stack " + stackSize + " elems"; // NOI18N
                            System.err.println("#" + testRestoredFiles.size() + " restored: " + msg); // NOI18N
                            testRestoredFiles.add(msg);
                        }
                        if (TRACE_PP_STATE_OUT) System.err.println("after restoring " + preprocState); // NOI18N
                        APTPreprocState.State fullState = preprocState.getState();
                        putPreprocStateState(interestedFile, fullState);
                    }
                } else {
                    if (TRACE_PP_STATE_OUT) System.err.println("retrurn without restoring for " + interestedFile);
                    preprocState.setState(state);
                }
            }
        }
        return preprocState;
    }
    
    public APTFile getAPTLight(CsmFile csmFile) throws IOException {
        APTFile aptLight = null;
        if (TraceFlags.USE_AST_CACHE) {
            aptLight = CacheManager.getInstance().findAPTLight(csmFile);
        } else {
            aptLight = APTDriver.getInstance().findAPTLight(((FileImpl)csmFile).getBuffer());
        }
        return aptLight;
    }
    
    public GraphContainer getGraph(){
        return graphStorage;
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
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.EMPTY_LIST;
        }
        
        public List getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.EMPTY_LIST;
        }
        
        public List getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.EMPTY_LIST;
        }
        
        public List getSystemIncludePaths() {
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.EMPTY_LIST;
        }
        
        public NativeProject getNativeProject() {
            return project;
        }
        
        public File getFile() {
            return new File(absolutePath);
        }
        
        public Language getLanguage() {
            return NativeFileItem.Language.C_HEADER;
        }
        
        public LanguageFlavor getLanguageFlavor() {
            return NativeFileItem.LanguageFlavor.GENERIC;
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
    
    private final ModelImpl model;
    private Unresolved unresolved;
    private final String name;
    
    // only one of globalNamespace/globalNamespaceOLD must be used (based on USE_REPOSITORY)
    private final NamespaceImpl globalNamespaceOLD;
    private final CsmUID<CsmNamespace> globalNamespaceUID;
    
    private Object platformProject;
    private boolean isProjectDisposed;
    private Object disposeLock = new Object();
    private String fqn = null; // lazy inited
    
    // only one of namespaces/namespacesOLD must be used (based on USE_REPOSITORY)
    private Map<String, NamespaceImpl> namespacesOLD = Collections.synchronizedMap(new HashMap<String, NamespaceImpl>());
    private Map<String, CsmUID<CsmNamespace>> namespaces = Collections.synchronizedMap(new HashMap<String, CsmUID<CsmNamespace>>());
    
    private Map/*<String, ClassImpl>*/ classifiersOLD = Collections.synchronizedMap(new HashMap(/*<String, ClassImpl>*/));
    private Map<String, CsmUID<CsmClassifier>> classifiers = Collections.synchronizedMap(new HashMap<String, CsmUID<CsmClassifier>>());
    
    private DeclarationContainer declarationsSorage = new DeclarationContainer();
    
    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = new APTSystemStorage();
    
    private Object namespaceLock = new String("namespaceLock in Projectbase "+hashCode()); // NOI18N
    
    protected FileContainer fileContainer = new FileContainer();

    private GraphContainer graphStorage = new GraphContainer();
    
    //private NamespaceImpl fakeNamespace;
    
    // test variables.
    protected static final boolean ONLY_LEX_SYS_INCLUDES = Boolean.getBoolean("cnd.modelimpl.lex.sys.include");
    private static final boolean TRACE_PP_STATE_OUT = DebugUtils.getBoolean("cnd.dump.preproc.state", false);
    private static final boolean REMEMBER_RESTORED = TraceFlags.CLEAN_MACROS_AFTER_PARSE && (DebugUtils.getBoolean("cnd.remember.restored", false) || TRACE_PP_STATE_OUT);
    public static final int GATHERING_MACROS    = 0;
    public static final int GATHERING_TOKENS    = 1;
    
    ////////////////////////////////////////////////////////////////////////////
    /**
     * for tests only
     */
    public static List testGetRestoredFiles() {
        return testRestoredFiles;
    }
    
    private static List testRestoredFiles = null;
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput aStream) throws IOException {
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        aStream.writeUTF(this.name);
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, true);
        fileContainer.write(aStream);
        aFactory.writeStringToUIDMap(this.classifiers, aStream, true);
        declarationsSorage.write(aStream);
    }
    
    protected ProjectBase(DataInput aStream) throws IOException {
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        
        this.name = ProjectNameCache.getString(aStream.readUTF());
        assert this.name != null;
        this.globalNamespaceUID = aFactory.readUID(aStream);
        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager());
        fileContainer.read(aStream);
        aFactory.readStringToUIDMap(this.classifiers, aStream, QualifiedNameCache.getManager());
        declarationsSorage.read(aStream);
        
        this.model = (ModelImpl) CsmModelAccessor.getModel();
        
        this.globalNamespaceOLD = null;
    }
}
