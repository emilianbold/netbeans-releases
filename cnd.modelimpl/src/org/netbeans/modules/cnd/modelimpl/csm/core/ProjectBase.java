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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Terminator;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTRestorePreprocStateWalker;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Base class for CsmProject implementation
 * @author Dmitry Ivanov
 * @author Vladimir Kvashin
 */
public abstract class ProjectBase implements CsmProject, Disposable, Persistent, SelfPersistent {
    
    private transient boolean needParseOrphan;
    
    /** Creates a new instance of CsmProjectImpl */
    protected ProjectBase(ModelImpl model, Object platformProject, String name) {
        setStatus(Status.Initial);
        this.name = ProjectNameCache.getString(name);
        init(model, platformProject);
        NamespaceImpl ns = new NamespaceImpl(this);
        assert ns != null;
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.openUnit(this);
            this.globalNamespaceUID = UIDCsmConverter.namespaceToUID(ns);
            this.globalNamespaceOLD = null;
            declarationsSorageKey = new DeclarationContainer(this).getKey();
            fileContainerKey = new FileContainer(this).getKey();
            graphStorageKey = new GraphContainer(this).getKey();
        } else {
            this.globalNamespaceOLD = ns;
            this.globalNamespaceUID = null;
        }
    }

    private void init(ModelImpl model, Object platformProject) {
        this.model = model;
        this.platformProject = platformProject;
        if (TraceFlags.USE_REPOSITORY) {
            // remember in repository
            RepositoryUtils.hang(this);
        }
        // create global namespace
        
        if (TraceFlags.CLOSE_AFTER_PARSE) {
            Terminator.create(this);
        }
        needParseOrphan = ModelSupport.instance().needParseOrphan(platformProject);
    }
    
    private void setStatus(Status newStatus) {
	//System.err.printf("CHANGING STATUS %s -> %s for %s (%s)\n", status, newStatus, name, getClass().getName());
	status = newStatus;
    }
    
    protected static void cleanRepository(Object platformProject, boolean articicial) {
        Key key = KeyUtilities.createProjectKey(getUniqueName(platformProject));
        RepositoryUtils.closeUnit(key, null, true);
    }
    
    public static ProjectBase readInstance(ModelImpl model, Object platformProject, String name) {
        
        long time = 0;
        if( TraceFlags.TIMING ) {
            System.err.printf("Project %s: instantiating...\n", name);
            time = System.currentTimeMillis();
        }
        
        assert TraceFlags.PERSISTENT_REPOSITORY;
        String qName = getUniqueName(platformProject);
        Key key = KeyUtilities.createProjectKey(qName);
        RepositoryUtils.openUnit(key);
        Persistent o = RepositoryUtils.get(key);
        if( o != null ) {
            assert o instanceof ProjectBase;
            ProjectBase impl = (ProjectBase) o;
            if( ! impl.name.equals(name) ) {
                impl.setName(name);
            }
            impl.init(model, platformProject);
            if (TraceFlags.TIMING) {
                time = System.currentTimeMillis() - time;
                System.err.printf("Project %s: loaded. %d ms\n", name, time);
            }
            
            return impl;
        }
        return null;
    }
    
    public CsmNamespace getGlobalNamespace() {
        return _getGlobalNamespace();
    }
    
    public String getName() {
        return name;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns a string that uniquely identifies this project.
     * One should never rely on this name structure, 
     * just use it as in unique identifier
     */
    public String getUniqueName() {
        if (this.uniqueName == null) {
            this.uniqueName = getUniqueName(getPlatformProject());
        }
        return this.uniqueName;
    }
    
    public static String getUniqueName(Object platformProject) {
	String result;
        if (platformProject instanceof NativeProject) {
            result = ((NativeProject)platformProject).getProjectRoot() + 'N';
        } else if( platformProject instanceof String ) {
            result = (String) platformProject + 'L';
        } else if( platformProject == null ) {
	    throw new IllegalArgumentException("Incorrect platform project: null"); // NOI18N
        } else {
	    throw new IllegalArgumentException("Incorrect platform project class: " + platformProject.getClass()); // NOI18N
        }
        return ProjectNameCache.getString(result);
    }
    
    /** Gets an object, which represents correspondent IDE project */
    public Object getPlatformProject() {
        return platformProject;
    }
    
    /** Gets an object, which represents correspondent IDE project */
    protected void setPlatformProject(Object platformProject) {
        this.platformProject = platformProject;
        this.uniqueName = null;
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
        CsmClassifier result = classifierContainer.getClassifier(qualifiedName);
        return result;
    }
    
    public CsmDeclaration findDeclaration(String uniqueName) {
        return getDeclarationsSorage().getDeclaration(uniqueName);
    }
    
    public Collection<CsmOffsetableDeclaration> findDeclarations(String uniqueName) {
        return getDeclarationsSorage().findDeclarations(uniqueName);
    }
    
    public Collection<CsmOffsetableDeclaration> findDeclarationsByPrefix(String prefix) {
        return getDeclarationsSorage().getDeclarationsRange(prefix, prefix+"z"); // NOI18N
    }
    
    public Collection<CsmFriend> findFriendDeclarations(CsmOffsetableDeclaration decl) {
        return getDeclarationsSorage().findFriends(decl);
    }
    
    public static boolean isCppFile(CsmFile file){
        return (file instanceof FileImpl) && ((FileImpl)file).isCppFile();
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
    
    public void registerDeclaration(CsmOffsetableDeclaration decl) {
        
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
            CsmDeclaration old = getDeclarationsSorage().getDeclaration(decl.getUniqueName());
            if (old != null && old != decl) {
                System.err.println("\n\nRegistering different declaration with the same name:" + decl.getUniqueName());
                System.err.print("WAS:");
                new CsmTracer().dumpModel(old);
                System.err.print("\nNOW:");
                new CsmTracer().dumpModel(decl);
            }
        }
        getDeclarationsSorage().putDeclaration(decl);
        
        if( decl instanceof CsmClassifier ) {
            String qn = decl.getQualifiedName();
            if (!classifierContainer.putClassifier((CsmClassifier)decl) && TraceFlags.CHECK_DECLARATIONS) {
                CsmClassifier old = classifierContainer.getClassifier(qn);
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
            classifierContainer.removeClassifier(decl);
        }
        getDeclarationsSorage().removeDeclaration(decl);
    }
    
    public void waitParse() {
        boolean insideParser = ParserThreadManager.instance().isParserThread();
        if( insideParser ) {
            new Throwable("project.waitParse should NEVER be called in parser thread !!!").printStackTrace(System.err); // NOI18N
        }
        if( insideParser ) {
            return;
        }
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        waitParseImpl();
    }
    
    private void waitParseImpl() {
        synchronized( waitParseLock ) {
            while ( ParserQueue.instance().hasFiles(this, null) ) {
                try {
                    waitParseLock.wait();
                } catch (InterruptedException ex) {
                    // do nothing
                }
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
    
    public boolean acceptNativeItem(NativeFileItem item) {
        NativeFileItem.Language language = item.getLanguage();
        return (language == NativeFileItem.Language.C ||
                language == NativeFileItem.Language.CPP ||
                language == NativeFileItem.Language.C_HEADER) &&
                !item.isExcluded();
    }
    
    protected synchronized void ensureFilesCreated() {
        if( status ==  Status.Initial || status == Status.Restored ) {
            try {
                setStatus( (status == Status.Initial) ? Status.AddingFiles : Status.Validating );
                long time = 0;
                if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                    System.err.println("suspend queue");
                    ParserQueue.instance().suspend();
                    if (TraceFlags.TIMING) {
                        time = System.currentTimeMillis();
                    }
                }
                ParserQueue.instance().onStartAddingProjectFiles(this);
                ModelSupport.instance().registerProjectListeners(this, platformProject);
                NativeProject nativeProject = ModelSupport.instance().getNativeProject(platformProject);
                if( nativeProject != null ) {
                    try {
                        ParserQueue.instance().suspend();
                        createProjectFilesIfNeed(nativeProject);
                    } finally {
                        ParserQueue.instance().resume();
                    }
                }
                if (TraceFlags.SUSPEND_PARSE_TIME != 0) {
                    if (TraceFlags.TIMING) {
                        time = System.currentTimeMillis() - time;
                        System.err.println("getting files from project system + put in queue took " + time + "ms");
                    }
                    try {
                        System.err.println("sleep for " + TraceFlags.SUSPEND_PARSE_TIME + "sec before resuming queue");
                        Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
                        System.err.println("woke up after sleep");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    ParserQueue.instance().resume();
                }
                ParserQueue.instance().onEndAddingProjectFiles(this);
            } finally {
                setStatus(Status.Ready);
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
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
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
	    public void projectDeleted(NativeProject nativeProject) {}
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
                Thread.sleep(TraceFlags.SUSPEND_PARSE_TIME  * 1000);
                System.err.println("woke up after sleep");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if(TraceFlags.DUMP_PROJECT_ON_OPEN ) {
            ModelSupport.instance().dumpNativeProject(nativeProject);
        }
        
        try {
            disposeLock.readLock().lock();
            
            if( ProjectBase.this.isProjectDisposed ) {
                if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ProjevtBase.ensureFilesCreated interrupted");
                return;
            }
            
            ProjectSettingsValidator validator = null;
            if( status == Status.Validating ) {
                validator = new ProjectSettingsValidator(this);
                validator.restoreSettings();
            }
            projectRoots.addSources(sources);
            projectRoots.addSources(headers);
            createProjectFilesIfNeed(sources, true, removedFiles, validator);
            createProjectFilesIfNeed(headers, false, removedFiles, validator);
            
        } finally {
            disposeLock.readLock().unlock();
        }
        nativeProject.removeProjectItemsListener(projectItemListener);
        // in fact if visitor used for parsing => visitor will parse all included files
        // recursively starting from current source file
        // so, when we visit headers, they should not be reparsed if already were parsed
    }
    
    private void createProjectFilesIfNeed(List<NativeFileItem> items, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator) {
        
        for( NativeFileItem nativeFileItem : items ) {
            if (removedFiles.contains(nativeFileItem)){
                continue;
            }
            assert (nativeFileItem.getFile() != null) : "native file item must have valid File object";
            if( TraceFlags.DEBUG ) ModelSupport.instance().trace(nativeFileItem);
            try {
                createIfNeed(nativeFileItem, sources, validator);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Creates FileImpl instance for the given file item if it hasn/t yet been created.
     * Is called when initializing the project or new file is added to project.
     * Isn't intended to be used in #included file processing.
     */
    protected void createIfNeed(NativeFileItem nativeFile, boolean isSourceFile, ProjectSettingsValidator validator) {
        
        assert (nativeFile != null && nativeFile.getFile() != null);
        if( ! acceptNativeItem(nativeFile)) {
            return;
        }
        File file = nativeFile.getFile();
        APTPreprocHandler preprocHandler = createPreprocHandler(nativeFile);
        assert preprocHandler != null;
        int fileType = isSourceFile ? getFileType(nativeFile) : FileImpl.HEADER_FILE;
        
        FileAndHandler fileAndHandler = createOrFindFileImpl(ModelSupport.instance().getFileBuffer(file), nativeFile, fileType);
        
        if( fileAndHandler.preprocHandler == null ) {
            fileAndHandler.preprocHandler = createPreprocHandler(nativeFile);
        }
        if (isSourceFile || needParseOrphan) {
            ParserQueue.instance().addLast(fileAndHandler.fileImpl, fileAndHandler.preprocHandler.getState());
        }
        
        if( validator != null ) {
            if( fileAndHandler.fileImpl.validate() ) {
                if( validator.arePropertiesChanged(nativeFile) ) {
                    if( TraceFlags.TRACE_VALIDATION ) System.err.printf("Validation: %s properties are changed \n", nativeFile.getFile().getAbsolutePath());
                    DeepReparsingUtils.reparseOnPropertyChanged(nativeFile, this);
                }
// clients should listen for ProjectLoaded instead
//		else if( fileAndHandler.fileImpl.isParsed() ) {
//		    //ProgressSupport.instance().fireFileParsingStarted(fileAndHandler.fileImpl);
//		    ProgressSupport.instance().fireFileParsingFinished(fileAndHandler.fileImpl);
//		}
            } else {
                if( TraceFlags.TRACE_VALIDATION ) System.err.printf("Validation: file %s is changed\n", nativeFile.getFile().getAbsolutePath());
                DeepReparsingUtils.reparseOnEdit(fileAndHandler.fileImpl, this, true);
            }
        }
    }
    
    
    /**
     * Is called after project is added to model
     * and all listeners are notified
     */
    public final void onAddedToModel() {
        final boolean isRestored = status == Status.Restored;
	//System.err.printf("onAddedToModel isRestored=%b status=%s for %s (%s) \n", isRestored, status, name, getClass().getName());
        if( status == Status.Initial || status == Status.Restored ) {
            Runnable r = new Runnable() {
                public void run() {
                    onAddedToModelImpl(isRestored);
                };
            };
            String text = (status == Status.Initial) ? "Filling parser queue for " : "Validating files for ";	// NOI18N
            CodeModelRequestProcessor.instance().post(r, text + getName());
        }
    }
    
    protected Status getStatus() {
        return status;
    }
    
    protected void onAddedToModelImpl(boolean isRestored) {
        
//	System.err.printf("SLEEPING...\n");
//	try {
//		Thread.sleep(10000);
//	} catch (InterruptedException ex) {
//		ex.printStackTrace();
//	}
//	System.err.printf("AWOKE...\n");
        
        ensureFilesCreated();
        ensureChangedFilesEnqueued();
        if( isRestored ) {
            ProgressSupport.instance().fireProjectLoaded(ProjectBase.this);
        }
        waitParseImpl();
        if( isRestored ) {
            // FIXUP for #109105 fix the reason instead!
            try {
                checkForRemoved();
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
        Notificator.instance().flush();
    }
    
    /**
     * For the project that is restored from persistence,
     * is called when 1-st time parsed.
     * Cheks whether there are files in code model, that are removed from the project system
     */
    private void checkForRemoved() {
        
        NativeProject nativeProject = (platformProject instanceof NativeProject) ? (NativeProject) platformProject : null;
        
        // we might just ask NativeProject to find file,
        // but it's too ineffective; so we have to create a set of project files paths
        Set<String> projectFiles = null;
        if( nativeProject != null ) {
            projectFiles = new HashSet<String>();
            for( NativeFileItem item : nativeProject.getAllHeaderFiles() ) {
                projectFiles.add(item.getFile().getAbsolutePath());
            }
        }
        
        Set<FileImpl> candidates = new HashSet<FileImpl>();
        Set<FileImpl> removedPhysically = new HashSet<FileImpl>();
        for( FileImpl file : getAllFileImpls() ) {
            if( ! file.getFile().exists() ) {
                removedPhysically.add(file);
            } else if( projectFiles != null ) { // they might be null for library
                if( ! projectFiles.contains(file.getAbsolutePath()) ) {
                    candidates.add(file);
                }
            }
        }
        for( FileImpl file : removedPhysically ) {
            if( TraceFlags.TRACE_VALIDATION ) System.err.printf("Validation: removing (physically deleted) %s\n", file.getAbsolutePath()); //NOI18N
            onFileRemoved(file);
        }
        for( FileImpl file : candidates ) {
            boolean remove = true;
            Set<CsmFile> parents = getGraphStorage().getParentFiles(file);
            for( CsmFile parent : parents ) {
                if( ! candidates.contains(parent) ) {
                    remove = false;
                    break;
                }
            }
            if( remove ) {
                if( TraceFlags.TRACE_VALIDATION ) System.err.printf("Validation: removing (removed from project) %s\n", file.getAbsolutePath()); //NOI18N
                onFileRemoved(file);
            }
        }
    }
    
    protected APTPreprocHandler createDefaultPreprocHandler(File file) {
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(file, true),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createEmptyPreprocHandler(startEntry);
    }
    
    protected APTPreprocHandler createPreprocHandler(NativeFileItem nativeFile) {
        assert (nativeFile != null);
        APTMacroMap macroMap = getMacroMap(nativeFile);
        APTIncludeHandler inclHandler = getIncludeHandler(nativeFile);
        APTPreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(macroMap, inclHandler, isSourceFile(nativeFile));
        return preprocHandler;
    }
    
    private APTIncludeHandler getIncludeHandler(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List<String> userIncludePaths = nativeFile.getUserIncludePaths();
        List<String> sysIncludePaths = nativeFile.getSystemIncludePaths();
        sysIncludePaths = sysAPTData.getIncludes(sysIncludePaths.toString(), sysIncludePaths);
        StartEntry startEntry = new StartEntry(FileContainer.getFileKey(nativeFile.getFile(), true),
                RepositoryUtils.UIDtoKey(getUID()));
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludePaths, userIncludePaths);
    }
    
    private APTMacroMap getMacroMap(NativeFileItem nativeFile) {
        if (!isSourceFile(nativeFile)){
            nativeFile = new DefaultFileItem(nativeFile);
        }
        List<String> userMacros = nativeFile.getUserMacroDefinitions();
        List<String> sysMacros = nativeFile.getSystemMacroDefinitions();
        APTMacroMap map = APTHandlersSupport.createMacroMap(getSysMacroMap(sysMacros), userMacros);
        return map;
    }
    
    protected boolean isSourceFile(NativeFileItem nativeFile){
        int type = getFileType(nativeFile);
        return type == FileImpl.SOURCE_CPP_FILE || type == FileImpl.SOURCE_C_FILE || type == FileImpl.SOURCE_FILE;
        //return nativeFile.getSystemIncludePaths().size()>0;
    }
    
    protected int getFileType(NativeFileItem nativeFile) {
        Language lang = nativeFile.getLanguage();
        if (lang == NativeFileItem.Language.C){
            return FileImpl.SOURCE_C_FILE;
        } else if (lang == NativeFileItem.Language.CPP){
            return FileImpl.SOURCE_CPP_FILE;
        } else if (lang == NativeFileItem.Language.C_HEADER){
            return FileImpl.HEADER_FILE;
        }
        return FileImpl.UNDEFINED_FILE;
    }
    
    private APTMacroMap getSysMacroMap(List<String> sysMacros) {
        //TODO: it's faster to use sysAPTData.getMacroMap(configID, sysMacros);
        // but we need this ID to get somehow... how?
        APTMacroMap map = sysAPTData.getMacroMap(sysMacros.toString(), sysMacros);
        return map;
    }
    
    /*package*/ final APTPreprocHandler getPreprocHandler(File file) {
        APTPreprocHandler preprocHandler = createDefaultPreprocHandler(file);
        APTPreprocHandler.State state = getPreprocState(file);
        preprocHandler = restorePreprocHandler(file, preprocHandler, state);
        return preprocHandler;
    }
    
    public final APTPreprocHandler.State getPreprocState(FileImpl fileImpl) {
        APTPreprocHandler.State state = null;
        FileContainer fc = getFileContainer();
        if (fc != null) {
            File file = fileImpl.getBuffer().getFile();
            state = fc.getPreprocState(file);
        }
        return state;
    }
    
    /**
     * This method for testing purpose only. Used from TraceModel
     */
    public CsmFile testAPTParseFile(String path, APTPreprocHandler preprocHandler) {
        File file = new File(path);
        APTPreprocHandler.State state = getPreprocState(file);
        if( state == null ) {
            // remember the first state
            return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState());
        } else {
            preprocHandler = restorePreprocHandler(file, preprocHandler, state);
            return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, null);
        }
    }
    
    protected void putPreprocState(File file, APTPreprocHandler.State state) {
        getFileContainer().putPreprocState(file, state);
    }
    
    protected APTPreprocHandler.State getPreprocState(File file) {
        return getFileContainer().getPreprocState(file);
    }
    
    protected void invalidatePreprocState(File file) {
        getFileContainer().invalidatePreprocState(file);
    }
    
    public void invalidateFiles() {
        getFileContainer().clearState();
        for (Iterator it = getLibraries().iterator(); it.hasNext();) {
            ProjectBase lib = (ProjectBase) it.next();
            lib.invalidateFiles();
        }
    }
    
    /**
     * called to inform that file was #included from another file with specific preprocHandler
     *
     * @param file included file path
     * @param preprocHandler preprocHandler with which the file is including
     * @param mode of walker forced onFileIncluded for #include directive
     * @return true if it's first time of file including
     *          false if file was included before
     */
    public FileImpl onFileIncluded(ProjectBase base, String file, APTPreprocHandler preprocHandler, int mode) throws IOException {
        try {
            disposeLock.readLock().lock();
            if( isProjectDisposed ) {
                return null;
            }
            FileImpl csmFile = findFile(new File(file), FileImpl.HEADER_FILE, preprocHandler, false, null);
            
            APTPreprocHandler.State state = updateFileStateIfNeeded(csmFile, preprocHandler);
            
            // gather macro map from all includes
            APTFile aptLight = getAPTLight(csmFile);
            if (aptLight != null) {
                APTParseFileWalker walker = new APTParseFileWalker(base, aptLight, csmFile, preprocHandler);
                walker.visit();
            }
            
            if (state != null) {
                scheduleIncludedFileParsing(csmFile, state);
            }
            return csmFile;
        } finally {
            disposeLock.readLock().unlock();
        }
    }
    
//    protected boolean needScheduleParsing(FileImpl file, APTPreprocHandler preprocHandler) {
//        APTPreprocHandler.State curState = (APTPreprocHandler.State) filesHandlers.get(file);
//        if (curState != null && !curState.isStateCorrect() && preprocHandler != null && preprocHandler.isStateCorrect()) {
//            return true;
//        }
//        return !file.isParsingOrParsed() || !TraceFlags.APT_CHECK_GET_STATE ;
//    }
    
    protected APTPreprocHandler.State updateFileStateIfNeeded(FileImpl csmFile, APTPreprocHandler preprocHandler) {
        APTPreprocHandler.State state = null;
        File file = csmFile.getBuffer().getFile();
        if (csmFile.isNeedReparse(getPreprocState(file), preprocHandler)){
            state = preprocHandler.getState();
            // need to prevent corrupting shared object => copy
            APTPreprocHandler.State copy = APTHandlersSupport.copyPreprocState(state);
            putPreprocState(file, copy);
            // invalidate file
            csmFile.stateChanged(true);
        }
        return state;
    }
    
    public ProjectBase findFileProject(String absPath) {
        // check own files
        // Wait while files are created. Otherwise project file will be recognized as library file.
        ensureFilesCreated();
        File file = new File(absPath);
        if (getFile(file) != null) {
            return this;
        } else {
            // else check in libs
            for (CsmProject prj : getLibraries()) {
                // Wait while files are created. Otherwise project file will be recognized as library file.
                ((ProjectBase)prj).ensureFilesCreated();
                if (((ProjectBase)prj).getFile(file) != null){
                    return (ProjectBase)prj;
                }
            }
        }
        return null;
    }
    
    public boolean isMySource(String includePath){
        return projectRoots.isMySource(includePath);
    }
    
    public abstract void onFileAdded(NativeFileItem nativeFile);
    public abstract void onFileAdded(List<NativeFileItem> items);
    //public abstract void onFileRemoved(NativeFileItem nativeFile);
    public abstract void onFileRemoved(FileImpl fileImpl);
    public abstract void onFileRemoved(List<NativeFileItem> items);
    public abstract void onFilePropertyChanged(NativeFileItem nativeFile);
    public abstract void onFilePropertyChanged(List<NativeFileItem> items);
    protected abstract void scheduleIncludedFileParsing(FileImpl csmFile, APTPreprocHandler.State state);
    
    public void onFileRemoved(File nativeFile) {
        onFileRemoved(getFile(nativeFile));
    }
    
    public CsmFile findFile(String absolutePath) {
        File file = new File(absolutePath);
        APTPreprocHandler preprocHandler = null;
        if (getPreprocState(file) == null){
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
                    if( ! acceptNativeItem(nativeFile) ) {
                        return null;
                    }
                    preprocHandler = createPreprocHandler(nativeFile);
                }
            }
            if (preprocHandler != null) {
                return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, preprocHandler.getState());
            }
        }
        return findFile(file, FileImpl.UNDEFINED_FILE, preprocHandler, true, null);
    }
    
    protected FileImpl findFile(File file, int fileType, APTPreprocHandler preprocHandler,
            boolean scheduleParseIfNeed, APTPreprocHandler.State initial) {
        
        FileImpl impl = getFile(file);
        if( impl == null ) {
            synchronized( getFileContainer() ) {
                impl = getFile(file);
                if( impl == null ) {
                    preprocHandler = preprocHandler == null ? getPreprocHandler(file) : preprocHandler;
                    impl = new FileImpl(ModelSupport.instance().getFileBuffer(file), this, fileType, preprocHandler);
                    putFile(file, impl, initial);
                    // NB: parse only after putting into a map
                    if( scheduleParseIfNeed ) {
                        APTPreprocHandler.State ppState = preprocHandler == null ? null : preprocHandler.getState();
                        ParserQueue.instance().addLast(impl, ppState);
                    }
                }
            }
        }
        if (fileType == FileImpl.SOURCE_FILE && !impl.isSourceFile()){
            impl.setSourceFile();
        } else if (fileType == FileImpl.HEADER_FILE && !impl.isHeaderFile()){
            impl.setHeaderFile();
        }
        if (initial != null && getPreprocState(file)==null){
            putPreprocState(file, initial);
        }
        return impl;
    }
    
//    protected FileImpl createOrFindFileImpl(final NativeFileItem nativeFile) {
//	File file = nativeFile.getFile();
//	assert file != null;
//	return createOrFindFileImpl(ModelSupport.instance().getFileBuffer(file), nativeFile);
//    }
    
    protected FileImpl createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile) {
        return createOrFindFileImpl(buf, nativeFile, FileImpl.UNDEFINED_FILE).fileImpl;
    }
    
    private static class FileAndHandler {
        public FileAndHandler(FileImpl fileImpl, APTPreprocHandler preprocHandler) {
            this.fileImpl = fileImpl;
            this.preprocHandler = preprocHandler;
        }
        public FileImpl fileImpl;
        public APTPreprocHandler preprocHandler;
    }
    
    private FileAndHandler createOrFindFileImpl(final FileBuffer buf, final NativeFileItem nativeFile, int fileType) {
        APTPreprocHandler preprocHandler = null;
        File file = buf.getFile();
        FileImpl impl = getFile(file);
        if( impl == null ) {
            synchronized( getFileContainer() ) {
                impl = getFile(file);
                if( impl == null ) {
                    preprocHandler = createPreprocHandler(nativeFile);
                    assert preprocHandler != null;
                    impl = new FileImpl(buf, this, fileType, preprocHandler);
                    putFile(file, impl, preprocHandler.getState());
                }
            }
        }
        return new FileAndHandler(impl, preprocHandler);
    }
    
    
    public FileImpl getFile(File file) {
        return getFileContainer().getFile(file);
    }
    
    protected void removeFile(File file) {
        getFileContainer().removeFile(file);
    }
    
    protected void putFile(File file, FileImpl impl, APTPreprocHandler.State state) {
        getFileContainer().putFile(file, impl, state);
    }
    
    protected Collection<Key> getLibrariesKeys() {
        List<Key> res = new ArrayList<Key>();
        if (platformProject instanceof NativeProject){
            for(NativeProject nativeLib : ((NativeProject)platformProject).getDependences()){
                final String qName = getUniqueName(nativeLib);
                final Key key = KeyUtilities.createProjectKey(qName);
                if (key != null) {
                    res.add(key);
                }
            }
        }
        // Last dependent project is common library.
        //final Key lib = KeyUtilities.createProjectKey("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for(CsmUID<CsmProject> library : LibraryManager.getInsatnce().getLirariesKeys(getUID())){
                res.add(RepositoryUtils.UIDtoKey(library));
            }
        }
        return res;
    }
    
    public Collection<CsmProject> getLibraries() {
        List<CsmProject> res = new ArrayList<CsmProject>();
        if (platformProject instanceof NativeProject){
            for(NativeProject nativeLib : ((NativeProject)platformProject).getDependences()){
                CsmProject prj = model.findProject(nativeLib);
                if (prj != null) {
                    res.add(prj);
                }
            }
        }
        // Last dependent project is common library.
        //ProjectBase lib = getModel().getLibrary("/usr/include"); // NOI18N
        //if (lib != null) {
        //    res.add(lib);
        //}
        if (!isArtificial()) {
            for(LibProjectImpl library : LibraryManager.getInsatnce().getLiraries((ProjectImpl)this)){
                res.add(library);
            }
        }
        return res;
    }
    
    public List<ProjectBase> getDependentProjects(){
        List<ProjectBase> res = new ArrayList<ProjectBase>();
        for(CsmProject prj : model.projects()){
            if (prj instanceof ProjectBase){
                if (prj.getLibraries().contains(this)){
                    res.add((ProjectBase)prj);
                }
            }
        }
        return res;
    }
    
    
    protected ModelImpl getModelImpl() {
        return null;
    }
    
    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map
     * @param nameTokens name
     * @param file file that contains unresolved name (used for the purpose of statictics)
     * @param name offset that contains unresolved name (used for the purpose of statictics)
     */
    public CsmClass getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset) {
        if (Diagnostic.needStatistics()) Diagnostic.onUnresolvedError(nameTokens, file, offset);
        return getUnresolved().getDummyForUnresolved(nameTokens);
    }

    /**
     * Creates a dummy ClassImpl for uresolved name, stores in map.
     * Should be used only when restoring from persistence:
     * in contrary to getDummyForUnresolved(String[] nameTokens, CsmFile file, int offset),
     * it does not gather statistics!
     * @param nameTokens name
     */
    public CsmClass getDummyForUnresolved(String name) {
        return getUnresolved().getDummyForUnresolved(name);
    }
    
    public CsmNamespace getUnresolvedNamespace( ) {
        return getUnresolved().getUnresolvedNamespace();
    }
    
    public CsmFile getUnresolvedFile( ) {
        return getUnresolved().getUnresolvedFile();
    }
    
    private Unresolved getUnresolved() {
	// we don't sinc here since this isn't important enough:
	// at worst a map with one or two dummies will be thrown away
        if( unresolved == null ) {
            unresolved = new Unresolved(this);
        }
        return unresolved;
    }
    
    public boolean isValid() {
        return platformProject != null  && !isProjectDisposed;
    }
    
    public void setDisposed() {
        try {
            disposeLock.writeLock().lock();
            isProjectDisposed = true;
        } finally {
            disposeLock.writeLock().unlock();
        }
        ParserQueue.instance().removeAll(this);
    }
    
    public boolean isDisposed() {
        return isProjectDisposed;
    }
    
    public void dispose() {
        dispose(!TraceFlags.PERSISTENT_REPOSITORY);
    }
    
    public void dispose(final boolean cleanPersistent) {
        try {
            disposeLock.writeLock().lock();
            isProjectDisposed = true;
        } finally {
            disposeLock.writeLock().unlock();
        }
        ParserQueue.instance().removeAll(this);
        
        /*
         * if the repository is not used - clean all collections as we did before
         * in the other case - just close the corresponding unit
         * collections are not cleared to write the valid project content
         */
        if (!TraceFlags.USE_REPOSITORY){
            disposeFiles();
            
            // we have clear all collections
            // to protect IDE against the code model client
            // that stores the instance of the project
            // and does not release it upon project closure
            _clearNamespaces();
            classifierContainer.clearClassifiers();
            getDeclarationsSorage().clearDeclarations();
            if (TraceFlags.USE_DEEP_REPARSING) {
                getGraph().clear();
            }
        } else {
            ProjectSettingsValidator validator = new ProjectSettingsValidator(this);
            validator.storeSettings();
            RepositoryUtils.closeUnit(getUID(), getRequiredUnits(), cleanPersistent);
        }
        
        platformProject = null;
        unresolved = null;
        uid = null;
    }
    
    protected Set<String> getRequiredUnits() {
        Set<String> requiredUnits = new HashSet<String>();
        for(Key dependent: this.getLibrariesKeys()) {
            requiredUnits.add(dependent.getUnit());
        }
        return requiredUnits;
    }
    
    private void disposeFiles() {
        List<FileImpl> list;
//        synchronized (fileContainer) {
        list = getFileContainer().getFileImpls();
        getFileContainer().clear();
//        }
        for (FileImpl file : list){
            file.onProjectDispose();
            if (TraceFlags.USE_AST_CACHE) {
                CacheManager.getInstance().invalidate(file);
            } else {
                APTDriver.getInstance().invalidateAPT(file.getBuffer());
            }
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
            CsmUID<CsmNamespace> nsUID = namespaces.get(key);
            NamespaceImpl ns = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(nsUID);
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
            CsmUID<CsmNamespace> nsUID = RepositoryUtils.put(ns);
            assert nsUID != null;
            namespaces.put(key, nsUID);
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
            CsmUID<CsmNamespace> nsUID = namespaces.remove(key);
            assert nsUID != null;
            RepositoryUtils.remove(nsUID);
        } else {
            namespacesOLD.remove(key);
        }
    }
    
    private void _clearNamespaces() {
        if (TraceFlags.USE_REPOSITORY) {
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
        // it's ok to move the entire sycle into synchronized block,
        // because from inter-session persistence point of view,
        // if we don't fix fakes, we'll later consider that files are ok,
        // which is incorrect if there are some fakes
        try {
            disposeLock.readLock().lock();
            
            if( ! isProjectDisposed ) {
                for (Iterator it = getAllFiles().iterator(); it.hasNext();) {
                    FileImpl file= (FileImpl) it.next();
                    file.fixFakeRegistrations();
                }
            }
        } finally {
            disposeLock.readLock().unlock();
            ProjectComponent.setStable(declarationsSorageKey);
            ProjectComponent.setStable(fileContainerKey);
            ProjectComponent.setStable(graphStorageKey);
        }
    }
    
    /**
     * CsmProject implementation
     */
    public Collection<CsmFile> getAllFiles() {
        return (Collection<CsmFile>) getFileContainer().getFiles();
    }
    
    /**
     * We'd better name this getFiles();
     * but unfortunately there already is such method,
     * and it is used intensively
     */
    public Collection<FileImpl> getAllFileImpls() {
        return getFileContainer().getFileImpls();
    }
    
    public Collection<CsmFile> getSourceFiles() {
        List<CsmFile> res = new ArrayList<CsmFile>();
        for(FileImpl file : getAllFileImpls()){
            if (file.isSourceFile()) {
                res.add(file);
            }
        }
        return res;
    }
    
    public Collection<CsmFile> getHeaderFiles() {
        List<CsmFile> res = new ArrayList<CsmFile>();
        for(FileImpl file : getAllFileImpls()){
            //if (file.isHeaderFile()) {
            if (!file.isSourceFile()) {
                res.add(file);
            }
        }
        return res;
    }
    
    public long getMemoryUsageEstimation() {
        //TODO: replace with some smart algorythm
        return getFileContainer().getSize();
    }
    
    @Override
    public String toString() {
        return getName() + ' ' + getClass().getName() + " @" + hashCode(); // NOI18N
    }
    
    /*package*/final void cleanPreprocStateAfterParse(FileImpl fileImpl, APTPreprocHandler.State state2Clean) {
        if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
            File file = fileImpl.getBuffer().getFile();
            Object stateLock = getFileContainer().getLock(file);
            synchronized (stateLock) {
                APTPreprocHandler.State rememberedState = getPreprocState(file);
                if (TRACE_PP_STATE_OUT) System.err.println("was " + rememberedState);
                if (rememberedState != null) {
                    if (state2Clean.equals(rememberedState)) {
                        if (!rememberedState.isCleaned()) {
                            if (TRACE_PP_STATE_OUT) System.err.println("cleaning for " + file.getAbsolutePath());
                            APTPreprocHandler.State cleaned = APTHandlersSupport.createCleanPreprocState(state2Clean);
                            putPreprocState(file, cleaned);
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
    
    private APTPreprocHandler restorePreprocHandler(File interestedFile, APTPreprocHandler preprocHandler, APTPreprocHandler.State state) {
        if (state != null) {
            Object stateLock = getFileContainer().getLock(interestedFile);
            synchronized (stateLock) {
                if (state.isCleaned()) {
                    if (TRACE_PP_STATE_OUT) System.err.println("restoring for " + interestedFile);
                    APTPreprocHandler.State cleanedState = APTHandlersSupport.copyPreprocState(state);
                    // walk through include stack to restore preproc information
                    List<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(cleanedState);
                    // we need to reverse includes stack
                    assert (reverseInclStack != null && !reverseInclStack.isEmpty()) : "state of stack is " + reverseInclStack;
                    Stack<APTIncludeHandler.IncludeInfo> inclStack = new Stack<APTIncludeHandler.IncludeInfo>();
                    for (int i = reverseInclStack.size() - 1; i >= 0; i--) {
                        APTIncludeHandler.IncludeInfo inclInfo = reverseInclStack.get(i);
                        inclStack.push(inclInfo);
                    }
                    
                    APTPreprocHandler.State oldState = preprocHandler.getState();
                    preprocHandler.setState(cleanedState);
                    if (TRACE_PP_STATE_OUT) System.err.println("before restoring " + preprocHandler); // NOI18N
                    APTIncludeHandler inclHanlder = preprocHandler.getIncludeHandler();
                    assert inclHanlder != null;
		    ProjectBase startProject = getStartProject(inclHanlder);
                    //FileImpl csmFile = getStartFile(inclHanlder);
		    FileImpl csmFile = startProject.getFile(new File(inclHanlder.getStartEntry().getStartFile()));
                    if (csmFile == null) {
                        preprocHandler.setState(oldState);
                        return preprocHandler;
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
                        APTWalker walker = new APTRestorePreprocStateWalker(startProject, aptLight, csmFile,
                                preprocHandler, inclStack, FileContainer.getFileKey(interestedFile, false));
                        walker.visit();
                        if (REMEMBER_RESTORED) {
                            if (testRestoredFiles == null) {
                                testRestoredFiles = new ArrayList<String>();
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
                        if (TRACE_PP_STATE_OUT) System.err.println("after restoring " + preprocHandler); // NOI18N
                        APTPreprocHandler.State fullState = preprocHandler.getState();
                        putPreprocState(interestedFile, fullState);
                    }
                } else {
                    if (TRACE_PP_STATE_OUT) System.err.println("retrurn without restoring for " + interestedFile);
                    preprocHandler.setState(state);
                }
            }
        }
        return preprocHandler;
    }

    public static ProjectBase getStartProject(final APTIncludeHandler inclHanlder) {
        // start from the first file, then use include stack
        String startFile = inclHanlder.getStartEntry().getStartFile();
        Key key = inclHanlder.getStartEntry().getStartFileProject();
        ProjectBase prj = (ProjectBase)RepositoryUtils.get(key);
        return prj;
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
        return getGraphStorage();
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
        
        public List<String> getUserMacroDefinitions() {
            if (project != null) {
                return project.getUserMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }
        
        public List<String> getUserIncludePaths() {
            if (project != null) {
                return project.getUserIncludePaths();
            }
            return Collections.<String>emptyList();
        }
        
        public List<String> getSystemMacroDefinitions() {
            if (project != null) {
                return project.getSystemMacroDefinitions();
            }
            return Collections.<String>emptyList();
        }
        
        public List<String> getSystemIncludePaths() {
            if (project != null) {
                return project.getSystemIncludePaths();
            }
            return Collections.<String>emptyList();
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
        
        public boolean isExcluded() {
            return false;
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
     * whether it contains files that belong to this projec or not)
     */
    protected static enum Status {
        Initial,
        Restored,
        AddingFiles,
        Validating,
        Ready;
    }
    
    private transient Status status;
    
    private Object waitParseLock = new Object();
    
    private ModelImpl model;
    private Unresolved unresolved;
    private String name;
    
    // only one of globalNamespace/globalNamespaceOLD must be used (based on USE_REPOSITORY)
    private final NamespaceImpl globalNamespaceOLD;
    private final CsmUID<CsmNamespace> globalNamespaceUID;
    
    private Object platformProject;
    private boolean isProjectDisposed;
    //private Object disposeLock = new Object();
    private ReadWriteLock disposeLock = new ReentrantReadWriteLock();
    
    private String uniqueName = null; // lazy initialized
    
    // only one of namespaces/namespacesOLD must be used (based on USE_REPOSITORY)
    private Map<String, NamespaceImpl> namespacesOLD = new ConcurrentHashMap<String, NamespaceImpl>();
    private Map<String, CsmUID<CsmNamespace>> namespaces =new ConcurrentHashMap<String, CsmUID<CsmNamespace>>();
   
    private ClassifierContainer classifierContainer = new ClassifierContainer();
    
    // collection of sharable system macros and system includes
    private APTSystemStorage sysAPTData = APTSystemStorage.getDefault();
    
    private Object namespaceLock = new String("namespaceLock in Projectbase "+hashCode()); // NOI18N
    
    private Key declarationsSorageKey;
    private Key fileContainerKey;
    private Key graphStorageKey;
    
    protected final SourceRootContainer projectRoots = new SourceRootContainer();
    
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
    
    private static List<String> testRestoredFiles = null;
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput aStream) throws IOException {
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null;
        assert this.name != null;
        aStream.writeUTF(this.name);
        aStream.writeUTF(RepositoryUtils.getUnitName(getUID()));
        aFactory.writeUID(this.globalNamespaceUID, aStream);
        aFactory.writeStringToUIDMap(this.namespaces, aStream, false);
        classifierContainer.write(aStream);
        
        ProjectComponent.writeKey(fileContainerKey, aStream);
        ProjectComponent.writeKey(declarationsSorageKey, aStream);
        ProjectComponent.writeKey(graphStorageKey, aStream);
	
	PersistentUtils.writeUTF(this.uniqueName, aStream);
    }
    
    protected ProjectBase(DataInput aStream) throws IOException {
	
        setStatus(Status.Restored);
	
        assert aStream != null;
        UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        assert aFactory != null : "default UID factory can not be bull";
        
        this.name = ProjectNameCache.getString(aStream.readUTF());
        assert this.name != null : "project name can not be null";
	
        String unitName = aStream.readUTF();
	
        this.globalNamespaceUID = aFactory.readUID(aStream);
	assert  globalNamespaceUID != null : "globalNamespaceUID can not be null";
	
        aFactory.readStringToUIDMap(this.namespaces, aStream, QualifiedNameCache.getManager());
        this.classifierContainer = new ClassifierContainer(aStream);
        
        fileContainerKey = ProjectComponent.readKey(aStream);
	assert fileContainerKey != null : "fileContainerKey can not be null";
	
        declarationsSorageKey = ProjectComponent.readKey(aStream);
	assert declarationsSorageKey != null : "declarationsSorageKey can not be null";
	
        graphStorageKey = ProjectComponent.readKey(aStream);
	assert graphStorageKey != null : "graphStorageKey can not be null";
	
	this.uniqueName = PersistentUtils.readUTF(aStream);
	assert uniqueName != null : "uniqueName can not be null";
        
        this.model = (ModelImpl) CsmModelAccessor.getModel();
        
        this.globalNamespaceOLD = null;
    }
    
    DeclarationContainer getDeclarationsSorage() {
        return (DeclarationContainer) RepositoryUtils.get(declarationsSorageKey);
    }
    
    FileContainer getFileContainer() {
        FileContainer fc = (FileContainer) RepositoryUtils.get(fileContainerKey);
        if( fc == null ) {
            System.err.printf("Failed to get FileContainer by key %s\n", fileContainerKey);
        }
        return fc;
    }
    
    public GraphContainer getGraphStorage() {
        return (GraphContainer) RepositoryUtils.get(graphStorageKey);
    }
}
