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

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.modelimpl.Installer;

import java.util.*;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.FilePathCache;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryListener;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryNotifier;
import org.netbeans.modules.cnd.modelimpl.options.CodeAssistanceOptions;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.FileNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.ProjectNameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDManager;

/**
 * CsmModel implementation
 * @author Vladimir Kvashin
 */
public class ModelImpl implements CsmModel, LowMemoryListener, Installer.Startupable, CsmModelAccessor.CsmModelEx {
    
    public ModelImpl() {
        if( ! isStandalone() ) {
            ModelSupport.instance().init(this);
        }
    }

    public static boolean isStandalone() {
        return ! ModelImpl.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    private void initThreasholds() {
	String value, propertyName;
	propertyName = "cnd.model.memory.warning.threashold"; // NOI18N
	value = System.getProperty(propertyName);
	if( value != null ) {
	    try {
		warningThreshold = Double.parseDouble(value);
	    }
	    catch(NumberFormatException e) {
		Utils.LOG.severe("Incorrect format for property " + propertyName + ": " + value); // NOI18N
	    }
	}
//	propertyName = "cnd.model.memory.fatal.threashold";
//	value = System.getProperty(propertyName);
//	if( value != null ) {
//	    try {
//		fatalThreshold = Double.parseDouble(value);
//	    }
//	    catch(NumberFormatException e) {
//		Utils.LOG.severe("Incorrect format for property " + propertyName + ": " + value);
//	    }
//	}
    }
    
    public CsmProject findProject(Object id) {
        ProjectBase prj = null;
        if (id != null) {
            synchronized( lock ) {
                prj = obj2Project(id);
            }
        }
        return prj;
    }
    
    private ProjectBase obj2Project(Object obj) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmProject> prjUID = platf2csm.get(obj);
            ProjectBase prj = (ProjectBase) UIDCsmConverter.UIDtoProject(prjUID);
            assert prj != null || prjUID == null : "null object for UID " + prjUID;
            return prj;
        } else {
            return platf2csmOLD.get(obj);
        }

    }

    public CsmProject getProject(Object id) {
        if(id instanceof Project) {
            NativeProject prj = (NativeProject) ((Project)id).getLookup().lookup(NativeProject.class);
            if (prj != null) {
                id = prj;
            }
        }
        return findProject(id);
    }
    
    public CsmProject _getProject(Object id) {
        ProjectBase prj = null;
        if (id != null) {
            synchronized( lock ) {
                prj = obj2Project(id);
                if( prj == null && id instanceof Project) {
                    // for compatibility 
                    if (TraceFlags.DEBUG) {
                        System.err.println("getProject called with Project... expected NativeProject");
                        new Throwable().printStackTrace(System.err);
                    }
                    id = ((Project)id).getLookup().lookup(NativeProject.class);
                    prj = id != null ? obj2Project(id) : null;
                }
                if (prj == null) {
                    if( disabledProjects.contains(id) ) {
                        return null;
                    }
                    String name;
                    if( id instanceof NativeProject ) {
                        name = ((NativeProject) id).getProjectDisplayName();
                        if (false) {
                            String root = ((NativeProject) id).getProjectRoot();
                            for (Object o : platf2csm.keySet()){
                                if (o instanceof NativeProject) {
                                    if (name.equals( ((NativeProject)o).getProjectDisplayName() )){
                                        System.err.println("ModelImpl.getProject() creates a duplicated project "+name+":");
                                        System.err.println("   existent root:"+root);
                                        System.err.println("   new      root:"+((NativeProject)o).getProjectRoot());
                                        System.err.println("   Code model features can work wrong");
                                    }
                                }
                            }
                        }
                    }
                    else {
                        new IllegalStateException("CsmProject does not exist: " + id).printStackTrace(System.err); // NOI18N
                        name = "<unnamed>"; // NOI18N
                    }
                    prj = ProjectImpl.createInstance(this, id,  name);
                    putProject2Map(id,  prj);
                }
            }
        }
        return prj;
    }
    
//    public void onProjectOpen(Object platformProject) {
//    }

    public ProjectBase addProject(Object id, String name, boolean enableModel) {
        ProjectBase prj = null;
        if (enableModel) {
            synchronized( lock ) {
                if( state != CsmModelState.ON ) {
                    return null;
                }
                prj = obj2Project(id);
                if( prj == null ) {
                    prj = ProjectImpl.createInstance(this, id,  name);
                    putProject2Map(id,  prj);
                } else {
                    String fqn = ProjectBase.getQualifiedName(id);
                    if( ! prj.getQualifiedName().equals(fqn) ) {
                        new IllegalStateException("Existing project qualified name differ: " + prj.getName() + " - expected " + name).printStackTrace(System.err); // NOI18N
                    }
                }
            }
            fireProjectOpened(prj);
        } else {
            disabledProjects.add(id);
        }
        return prj;
    }
    
    // for testing purposes only
    public ProjectBase addProject(ProjectBase prj) {
        synchronized( lock ) {
            Object id = prj.getPlatformProject();
            if( obj2Project(id) != null ) {
                new IllegalStateException("CsmProject already exists: " + id).printStackTrace(System.err); // NOI18N
                return null;
            }
            putProject2Map(id, prj);
        }
        fireProjectOpened(prj);
        return prj;
    }

    private void putProject2Map(final Object id, final ProjectBase prj) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmProject> uid = UIDCsmConverter.projectToUID(prj);
            platf2csm.put(id,  uid);
        } else {
            platf2csmOLD.put(id,  prj);
        }
    }
    
    public void closeProject (Object platformProject) {
        _closeProject(null, platformProject);
    }
    
    public void closeProjectBase(ProjectBase prj) {
        _closeProject(prj, prj.getPlatformProject());
    }
    
    private void _closeProject(final ProjectBase csmProject, final Object platformProjectKey) {
        if (SwingUtilities.isEventDispatchThread()) {
            Runnable task = new Runnable() {
                                public void run() {
                                    _closeProject2(csmProject, platformProjectKey);
                                }
                            };
            this.enqueue(task, "Model: Closing Project "); // NOI18N
        } else {
            _closeProject2(csmProject, platformProjectKey);
        }           
    }
    
    public void removeProject(Object platformProject) {
        _removeProject(null, platformProject);
    }
    
    public void removeProjectBase(ProjectBase prj) {
        _removeProject(prj, prj.getPlatformProject());
    }
    
    private void _removeProject(final ProjectBase csmProject, final Object platformProjectKey) {
        if (SwingUtilities.isEventDispatchThread()) {
            Runnable task = new Runnable() {
                                public void run() {
                                    _closeProject2(csmProject, platformProjectKey, true);
                                }
                            };
            this.enqueue(task, "Model: Closing Project and cleaning the repository"); // NOI18N
        } else {
            _closeProject2(csmProject, platformProjectKey, true);
        }        
    }
    
    private void _closeProject2(ProjectBase csmProject, Object platformProjectKey) {
	_closeProject2(csmProject, platformProjectKey, !TraceFlags.PERSISTENT_REPOSITORY);
    }
    
    private void _closeProject2(ProjectBase csmProject, Object platformProjectKey, boolean cleanRepository) {
        ProjectBase prj = csmProject;
        boolean cleanModel = false;
        synchronized( lock ) {
            if (TraceFlags.USE_REPOSITORY) {
                CsmUID<CsmProject> uid = platf2csm.remove(platformProjectKey);
                if (uid != null) {
                    prj = (prj == null) ? (ProjectBase)UIDCsmConverter.UIDtoProject(uid) : prj;
                    assert prj != null  : "null object for UID " + uid;
                }            
            } else {
                ProjectBase value = platf2csmOLD.remove(platformProjectKey);
                if (value != null) {
                    prj = (prj == null) ? value : prj;
                }
            }
            if (TraceFlags.USE_REPOSITORY) {
                cleanModel = (platf2csm.size() == 0);                
            } else {
                cleanModel = (platf2csmOLD.size() == 0);                
            }  
        }
        
        if( prj != null ) {
            disposeProject(prj, cleanRepository);
            if (!prj.isArtificial()){
                LibraryManager.getInsatnce().onProjectClose(prj.getUID());
            }
        }   
      
        if (cleanModel) {
            // time to clean everything
            cleanModel();
        }
    }

    /*package-local*/ void disposeProject(final ProjectBase prj) {
	disposeProject(prj, !TraceFlags.PERSISTENT_REPOSITORY);
    }
    
    /*package-local*/ void disposeProject(final ProjectBase prj, boolean cleanRepository) {
        assert prj != null;
        String name = prj.getName();
        if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("dispose project " + name);
        prj.setDisposed();
        fireProjectClosed(prj);
        ParserThreadManager.instance().waitEmptyProjectQueue(prj);
        prj.dispose(cleanRepository);
        if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("project closed " + name);
    }
    
    public Collection<CsmProject> projects() {
        if (TraceFlags.USE_REPOSITORY) {
            Collection<CsmUID<CsmProject>> vals;
            synchronized (lock) {
                vals = new ArrayList<CsmUID<CsmProject>>(platf2csm.values());
            }
            Collection out = new ArrayList(vals.size());
            for (CsmUID<CsmProject> uid : vals) {
                ProjectBase prj = (ProjectBase)UIDCsmConverter.UIDtoProject(uid);
                assert prj != null : "null project for UID " + uid;
                out.add(prj);
            }
            return out;
        } else {
            synchronized (lock) {
                return new ArrayList<CsmProject>(platf2csmOLD.values());
            }
        }
    }
    
    public void addModelListener(CsmModelListener listener) {
        modelListeners.add(listener);
    }
    
    public void removeModelListener(CsmModelListener listener) {
        modelListeners.remove(listener);
    }

    public void addModelStateListener(CsmModelStateListener listener) {
        modelStateListeners.add(listener);
    }

    public void removeModelStateListener(CsmModelStateListener listener) {
        modelStateListeners.remove(listener);
    }
    
    public void addProgressListener(CsmProgressListener listener) {
        ProgressSupport.instance().addProgressListener(listener);
    }
    
    public void removeProgressListener(CsmProgressListener listener) {
        ProgressSupport.instance().removeProgressListener(listener);
    }

    public Iterator<CsmModelStateListener> getModelStateListeners() {
	return modelStateListeners.iterator();
    }
    
    public Iterator<CsmProgressListener> getProgressListeners() {
	return ProgressSupport.instance().getProgressListeners();
    }
    
    public Iterator<CsmModelListener> getModelListeners() {
	return modelListeners.iterator();
    }
    
    /*package-local*/ void fireProjectOpened(final ProjectBase csmProject) {
        csmProject.onAddedToModel();
        for ( CsmModelListener listener : modelListeners ) {
            listener.projectOpened(csmProject);
        }
    }
    
    private void fireProjectClosed(CsmProject csmProject) {
        for ( CsmModelListener listener : modelListeners ) {
            listener.projectClosed(csmProject);
        }
    }
    
    /**  */
    void fireModelChanged(CsmChangeEvent e) {
        for ( CsmModelListener listener : modelListeners ) {
            listener.modelChanged(e);
        }
    }
    
    void fireModelStateChanged(CsmModelState newState, CsmModelState oldState) {
        for ( CsmModelStateListener listener : modelStateListeners ) {
            listener.modelStateChanged(newState, oldState);
        }
    }
    
    public void enqueue(Runnable task, String name) {
        CodeModelRequestProcessor.instance().post(task, name);
    }

    public void enqueue(Runnable task) {
        CodeModelRequestProcessor.instance().post(task);
    }

    public CsmFile findFile(String absPath){
        Collection/*<CsmProject>*/ projects = projects();
        for (Iterator it = projects.iterator(); it.hasNext();) {
             CsmProject curPrj = (CsmProject) it.next();
             if (curPrj instanceof ProjectBase){
                 ProjectBase ownerPrj = ((ProjectBase)curPrj).findFileProject(absPath);
                 if (ownerPrj != null){
                     CsmFile csmFile = ownerPrj.findFile(absPath);
                     if (csmFile != null){
                        return csmFile;
                     }
                 }
             }
        }
        // try the same with canonical path
        String canonical;
        try {
            canonical = new File(absPath).getCanonicalPath();
        } catch (IOException ex) {
            canonical=null;
        }
        if (canonical != null && !canonical.equals(absPath)) {
            return findFile(canonical);
        }
        return null;
    }
    
    public CsmModelState getState() {
        return state;
    }
    
    public void startup() {
        
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.startup");

        setState(CsmModelState.ON);
        
	if( TraceFlags.CHECK_MEMORY && warningThreshold > 0 ) {
	    LowMemoryNotifier.instance().addListener(this);
	    LowMemoryNotifier.instance().setThresholdPercentage(warningThreshold);
	}
        
        ParserThreadManager.instance().startup(isStandalone());
	RepositoryUtils.startup();
	
	//if( ! isStandalone() ) {
	//    for( NativeProject nativeProject : ModelSupport.instance().getNativeProjects() ) {
	//    	addProject(nativeProject, nativeProject.getProjectDisplayName());
	//    }
	//}
    }
    
    public void shutdown() {

	if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.shutdown");

        ParserThreadManager.instance().shutdown();

	if( TraceFlags.CHECK_MEMORY ) {
	    LowMemoryNotifier.instance().removeListener(this);
	}

        Collection<CsmProject> prjsColl;
        Collection<CsmProject> libs = new HashSet<CsmProject>();
	
	synchronized( lock ) {
            prjsColl = projects();
            if (TraceFlags.USE_REPOSITORY) {
                platf2csm.clear();
            } else {
                platf2csmOLD.clear();
            }
	}
        
        // clearFileExistenceCache all opened projects, UIDs will be removed in disposeProject
        for (Iterator projIter =prjsColl.iterator(); projIter.hasNext();) {
            ProjectBase project = (ProjectBase) projIter.next();
            disposeProject(project);
            libs.addAll(project.getLibraries());
        }
        for (Iterator projIter =libs.iterator(); projIter.hasNext();) {
            disposeProject((ProjectBase) projIter.next());
        }
        
        cleanModel();      
	
        setState(CsmModelState.OFF);
	
	RepositoryUtils.shutdown();
        
        ModelSupport.instance().shutdown();
    }
    
    public void unload() {
        shutdown();
        setState(CsmModelState.UNLOADED);
    }

    public void memoryLow(final LowMemoryEvent event) {
	
	double percentage = ((double) event.getUsedMemory() / (double) event.getMaxMemory());
        
        final boolean warning = percentage >= warningThreshold && projects().size() > 0;

//	final boolean fatal = percentage >= fatalThreshold && projects().size() > 0;
//	
//	if( fatal ) {
//	    LowMemoryNotifier.instance().removeListener(this);
//	}
//	else {
//	    LowMemoryNotifier.instance().setThresholdPercentage(fatalThreshold);
//	}

	Runnable runner = new Runnable() {
	    public void run() {
		Thread.currentThread().setName("Code model low memory handler"); // NOI18N
//		if( fatal ) {
//		    ParserThreadManager.instance().shutdown();
//		    ModelSupport.instance().onMemoryLow(event, true);
//		}
//		else {
		    ModelSupport.instance().onMemoryLow(event, false);
//		}
	    }	    
	};
	// I have to use Thread directly here (instead of Request processor)
	// for the following reasons:
	// 1) I have to return control very fast
	// 2) if I use RequestProcessor, I can't be sure the thread will be launched -
	// what if we already reached the limit for this RequestProcessor?
	new Thread(runner).start();
	
    }
    
    private void setState(CsmModelState newState) {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("  ModelImpl.setState " + state + " -> " + newState);
        if( newState != state ) {
            CsmModelState oldState = state;
            state = newState;
            fireModelStateChanged(newState, oldState);
        }
    }
    
    public void suspend() {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.suspend");
        setState(CsmModelState.SUSPENDED);
        ParserQueue.instance().suspend();
    }
    
    public void resume() {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.resume");
        setState(CsmModelState.ON);
        ParserQueue.instance().resume();
    }

    public void disableProject(NativeProject nativeProject) {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.disableProject " + nativeProject.getProjectDisplayName());
        synchronized( lock ) {
            disabledProjects.add(nativeProject);
        }
        ProjectBase csmProject = (ProjectBase) findProject(nativeProject);
        if( csmProject != null ) {
            disableProject2(csmProject);
        }
    }

    public void disableProject(ProjectBase csmProject) {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.disableProject " + csmProject);
	if( csmProject != null ) {
            synchronized( lock ) {
                disabledProjects.add(csmProject.getPlatformProject());
            }
            disableProject2(csmProject);
        }
    }

    private void disableProject2(final ProjectBase csmProject) {
        csmProject.setDisposed();
        Project project = findProjectByNativeProject(ModelSupport.instance().getNativeProject(csmProject.getPlatformProject()));
	if( project != null ) {
	    new CodeAssistanceOptions(project).setCodeAssistanceEnabled(Boolean.FALSE);
	}
	// that's a caller's responsibility to launch disabling in a separate thread
        disableProject3(csmProject);
    }
    
    private void disableProject3(ProjectBase csmProject) {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.disableProject3");
        suspend();
        try {
            while( ParserQueue.instance().isParsing(csmProject) ) {
                try {
                    if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.disableProject3: waiting for current parse...");
                    Thread.currentThread().sleep(100);
                } catch( InterruptedException e ) {}
            }
            
            closeProjectBase(csmProject);
            boolean cleanModel;
            synchronized( lock ) {
                cleanModel = TraceFlags.USE_REPOSITORY ? platf2csm.isEmpty() : platf2csmOLD.isEmpty();
            }
            
            if( cleanModel ) {
                cleanModel();
            }
        }
        finally {
            resume();
        }
    }

    
//    /**
//     * Checks whether there are only library projects.
//     * If yes, returns the set of remaining library projects.
//     * Otherwise returns null
//     */
//    private Set<LibProjectImpl> getLastLibs() {
//	Collection/*<CsmProjects>*/ projects = projects();
//	Set<LibProjectImpl> lastLibs = new HashSet<LibProjectImpl>(projects.size());
//	for (Iterator it = projects.iterator(); it.hasNext();) {
//	    Object e = it.next();
//	    if( e instanceof LibProjectImpl ) {
//		lastLibs.add((LibProjectImpl) e);
//	    }
//	    else {
//		return null;
//	    }
//	}
//	return lastLibs;
//    }
    
    /** Enables/disables code model for the particular ptoject */
    public void enableProject(NativeProject nativeProject) {
        if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.enableProject " + nativeProject.getProjectDisplayName());
        synchronized( lock ) {
            disabledProjects.remove(nativeProject);
        }
	Project project = findProjectByNativeProject(nativeProject);
	if( project != null ) {
	    new CodeAssistanceOptions(project).setCodeAssistanceEnabled(Boolean.TRUE);        
	}
        addProject(nativeProject, nativeProject.getProjectDisplayName(), Boolean.TRUE);
	//ProjectBase csmProject = (ProjectBase) _getProject(nativeProject);
	//fireProjectOpened(csmProject);
        //new CodeAssistanceOptions(findProjectByNativeProject(nativeProject)).setCodeAssistanceEnabled(Boolean.TRUE);
    }
    
    public static Project findProjectByNativeProject(NativeProject nativeProjectToSearch) {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for( int i = 0; i < projects.length; i++ ) {
            NativeProject nativeProject = (NativeProject) projects[i].getLookup().lookup(NativeProject.class);
            if (nativeProject != null && nativeProject == nativeProjectToSearch) {
                return projects[i];
            }
        }
        return null;
    }
    
    public boolean isProjectEnabled(NativeProject nativeProject) {
        ProjectBase project = (ProjectBase) findProject(nativeProject);
        return (project != null) && (!project.isDisposed());
    }

    private void cleanModel() {
        cleanCaches();
    }

    private void cleanCaches() {
        TextCache.dispose();
        FilePathCache.dispose();
        QualifiedNameCache.dispose();
        FileNameCache.dispose();
        ProjectNameCache.dispose();
        if (TraceFlags.USE_AST_CACHE) {
            CacheManager.getInstance().close();
        } else {
            APTDriver.getInstance().close();
        }   
        if (TraceFlags.USE_REPOSITORY) {
            UIDManager.instance().dispose();
        }
        APTIncludeUtils.clearFileExistenceCache();
        APTSystemStorage.getDefault().dispose();
    }
    
    private Object lock = new Object();
    
    /** maps platform project to project */
    // only one of platf2csmOLD/platf2csm must be used (based on USE_REPOSITORY)
    private Map<Object, ProjectBase> platf2csmOLD = new HashMap<Object, ProjectBase>();
    private Map<Object, CsmUID<CsmProject>> platf2csm = new HashMap<Object, CsmUID<CsmProject>>();

    private WeakList<CsmModelListener> modelListeners = new WeakList<CsmModelListener>();
    private WeakList<CsmModelStateListener> modelStateListeners = new WeakList<CsmModelStateListener>();
    
    private CsmModelState state;

    private double warningThreshold = 0.98;
    //private double fatalThreshold = 0.99;

    private Set disabledProjects = new HashSet();
}
