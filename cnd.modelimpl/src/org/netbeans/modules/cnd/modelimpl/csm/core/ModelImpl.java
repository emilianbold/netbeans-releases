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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.netbeans.modules.cnd.modelimpl.Installer;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;

import java.util.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryListener;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryNotifier;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import java.lang.ref.WeakReference;
        
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

    private static boolean isStandalone() {
        return ! ModelImpl.class.getClassLoader().getClass().getName().startsWith("org.netbeans.");
    }
    
    private void initThreasholds() {
	String value, propertyName;
	propertyName = "cnd.model.memory.warning.threashold";
	value = System.getProperty(propertyName);
	if( value != null ) {
	    try {
		warningThreshold = Double.parseDouble(value);
	    }
	    catch(NumberFormatException e) {
		Utils.LOG.severe("Incorrect format for property " + propertyName + ": " + value);
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
                prj = (ProjectBase) platf2csm.get(id);
            }
        }
        return prj;
    }
    
    public CsmProject getProject(Object id) {
        ProjectBase prj = null;
        if (id != null) {
            synchronized( lock ) {
                prj = (ProjectBase) platf2csm.get(id);
                if( prj == null && id instanceof Project) {
                    // for compatibility 
                    if (Diagnostic.DEBUG) {
                        System.err.println("getProject called with Project... expected NativeProject");
                        new Throwable().printStackTrace(System.err);
                    }
                    id = ((Project)id).getLookup().lookup(NativeProject.class);
                    prj = id != null ? (ProjectBase) platf2csm.get(id) : null;
                }
                if (prj == null) {
                    if( disabledProjects.contains(id) ) {
                        return null;
                    }
                    String name;
                    if( id instanceof NativeProject ) {
                        name = ((NativeProject) id).getProjectDisplayName();
                    }
                    else {
                        new IllegalStateException("CsmProject does not exist: " + id).printStackTrace(System.err);
                        name = "<unnamed>";
                    }
                    prj = new ProjectImpl(this, id,  name);
                    platf2csm.put(id,  prj);
                }
            }
        }
        return prj;
    }
    
//    public void onProjectOpen(Object platformProject) {
//    }

    public ProjectBase addProject(Object id, String name) {
        ProjectBase prj = null;
        synchronized( lock ) {
            prj = (ProjectBase) platf2csm.get(id);
            if( prj == null ) {
                prj = new ProjectImpl(this, id,  name);
                platf2csm.put(id,  prj);
            }
            else {
                if( ! prj.getName().equals(name) ) {
                    new IllegalStateException("Existing project name differ: " + prj.getName() + " - expected " + name).printStackTrace(System.err);
                }
            }
        }
        fireProjectOpened(prj);
        return prj;
    }
    
    // for testing purposes only
    public ProjectBase addProject(ProjectBase prj) {
        synchronized( lock ) {
            Object id = prj.getPlatformProject();
            if( platf2csm.get(id) != null ) {
                new IllegalStateException("CsmProject already exists: " + id).printStackTrace(System.err);
                return null;
            }
            platf2csm.put(id,  prj);
        }
        fireProjectOpened(prj);
        return prj;
    }
    
    public void removeProject(Object platformProject) {
        ProjectBase prj = null;
        synchronized( lock ) {
            prj = (ProjectBase) platf2csm.get(platformProject);
            if( prj != null ) {
                platf2csm.remove(platformProject);
            }
        }
        if( prj != null ) {
            prj.setDisposed();
            fireProjectClosed(prj);
            prj.dispose();
        }
    }
    
    public void removeProject(ProjectBase prj) {
        Object platformProject = prj.getPlatformProject();
        synchronized( lock ) {
            platf2csm.remove(platformProject);
        }
	prj.setDisposed();
	fireProjectClosed(prj);
	prj.dispose();
    }
    
    
    public Collection/*<CsmProject>*/ projects() {
        return platf2csm.values();
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
        ParserQueue.instance().addProgressListener(listener);
    }
    
    public void removeProgressListener(CsmProgressListener listener) {
        ParserQueue.instance().removeProgressListener(listener);
    }

    public Iterator<CsmModelStateListener> getModelStateListeners() {
	return modelStateListeners.iterator();
    }
    
    public Iterator<CsmProgressListener> getProgressListeners() {
	return ParserQueue.instance().getProgressListeners();
    }
    
    public Iterator<CsmModelListener> getModelListeners() {
	return modelListeners.iterator();
    }
    
    private void fireProjectOpened(final ProjectBase csmProject) {
        for ( CsmModelListener listener : modelListeners ) {
            listener.projectOpened(csmProject);
        }
	csmProject.onAddedToModel();
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
    
    protected ProjectBase getLibrary(String includePath) {
        ProjectBase project = (ProjectBase) libraries.get(includePath);
        if( project == null ) {
            synchronized( this ) {
                project = (ProjectBase) libraries.get(includePath);
                if( project == null ) {
                    project = new LibProjectImpl(this, includePath);
                    libraries.put(includePath,  project);
                }
            }
        }
        return project;
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
                 ProjectBase ownerPrj = ((ProjectBase)curPrj).resolveFileProject(absPath);
                 if (ownerPrj != null){
                     CsmFile csmFile = ownerPrj.findFile(absPath);
                     if (csmFile != null){
                        return csmFile;
                     }
                 }
             }
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
	
	if( ! isStandalone() ) {
	    for( NativeProject nativeProject : ModelSupport.instance().getNativeProjects() ) {
		addProject(nativeProject, nativeProject.getProjectDisplayName());
	    }
	}
    }
    
    public void shutdown() {

    if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.shutdown");
        
        ParserThreadManager.instance().shutdown();

	if( TraceFlags.CHECK_MEMORY ) {
	    LowMemoryNotifier.instance().removeListener(this);
	}
        
        Collection/*<CsmProject>*/ projectsColl = new HashSet(projects());
        for (Iterator projIter = projects().iterator(); projIter.hasNext();) {
            ProjectBase project = (ProjectBase) projIter.next();
            for (Iterator libIter = project.getLibraries().iterator(); libIter.hasNext();) {
                projectsColl.add(libIter.next());
                
            }
        }
	synchronized( lock ) {
	    platf2csm.clear();
	    libraries.clear();
	}

        for (Iterator it = projectsColl.iterator(); it.hasNext();) {
            ProjectBase project = (ProjectBase) it.next();
            project.dispose();
            fireProjectClosed(project);
        }
        setState(CsmModelState.OFF);
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
		Thread.currentThread().setName("Code model low memory handler");
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
        this.enqueue(new Runnable() {
            public void run() {
                disableProject3(csmProject);
            }
        }, "Disabling code model for project " + csmProject.getName());
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
            
            boolean clearLibs;
            synchronized( lock ) {
                removeProject(csmProject);
                clearLibs = platf2csm.isEmpty();
            }
            
            if( clearLibs ) {
                if( TraceFlags.TRACE_MODEL_STATE ) System.err.println("ModelImpl.disableProject3: cleaning libs");
                Set/*ProjectBase*/ libs = new HashSet(libraries.values());
                for (Iterator it = libs.iterator(); it.hasNext();) {
                    ProjectBase lib = (ProjectBase) it.next();
                    ParserQueue.instance().removeAll(lib);
                    fireProjectClosed(lib);
                    lib.dispose();
                    //CacheManager.getInstance().projectClosed(lib);
                }
                libraries.clear();
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
	ProjectBase csmProject = (ProjectBase) getProject(nativeProject);
	fireProjectOpened(csmProject);
    }
    
    public boolean isProjectEnabled(NativeProject nativeProject) {
        ProjectBase project = (ProjectBase) findProject(nativeProject);
        return (project != null) && (!project.isDisposed());
    }
    
    private Object lock = new Object();
    
    /** maps platform project to project */
    private Map platf2csm = new HashMap();
    
    private Map libraries = new HashMap();
    
    private WeakList<CsmModelListener> modelListeners = new WeakList<CsmModelListener>();
    private WeakList<CsmModelStateListener> modelStateListeners = new WeakList<CsmModelStateListener>();
    
    private CsmModelState state;

    private double warningThreshold = 0.98;
    //private double fatalThreshold = 0.99;
    
    private Set disabledProjects = new HashSet();
}
