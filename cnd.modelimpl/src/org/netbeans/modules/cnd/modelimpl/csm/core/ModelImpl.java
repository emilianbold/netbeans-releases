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
import org.netbeans.modules.cnd.modelimpl.cache.LibProjectImpl;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.platform.*;

import java.util.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;

/**
 * CsmModel implementation
 * @author Vladimir Kvashin
 */
public class ModelImpl implements CsmModel {

    public ModelImpl() {
        this(isStandalone());
    }
    
    private static boolean isStandalone() {
        return ! ModelImpl.class.getClassLoader().getClass().getName().startsWith("org.netbeans.");
    }
    
    public ModelImpl(boolean standalone) {
        if( ! standalone ) {
            ModelSupport.instance().init(this);
        }
    }

    public CsmProject getProject(Object id) {
        ProjectBase prj;
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
            fireProjectClosed(prj);
            prj.dispose();
        }
    }
    
    public Collection/*<CsmProject>*/ projects() {
        return platf2csm.values();
    }
    
    public synchronized void addModelListener(CsmModelListener listener) {
        modelListeners.add(listener);
    }
    
    public synchronized void removeModelListener(CsmModelListener listener) {
        modelListeners.remove(listener);
    }
    
    public void addProgressListener(CsmProgressListener listener) {
        ParserQueue.instance().addProgressListener(listener);
    }
    
    public void removeProgressListener(CsmProgressListener listener) {
        ParserQueue.instance().removeProgressListener(listener);
    }

    private void fireProjectOpened(CsmProject csmProject) {
        CsmModelListener[] listeners = getModelListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].projectOpened(csmProject);
        }
    }
    
    private void fireProjectClosed(CsmProject csmProject) {
        CsmModelListener[] listeners = getModelListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].projectClosed(csmProject);
        }
    }
    
    /**  */
    void fireModelChanged(CsmChangeEvent e) {
        CsmModelListener[] listeners = getModelListeners();
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].modelChanged(e);
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
    
    private synchronized CsmModelListener[] getModelListeners() {
        return (CsmModelListener[]) modelListeners.toArray(new CsmModelListener[modelListeners.size()]);
    }
    
    private Object lock = new Object();
    
    /** maps platform project to project */
    private Map platf2csm = new HashMap();
    
    private Map libraries = new HashMap();
    
    private Collection/*<CsmModelListener>*/ modelListeners = new  LinkedList/*<CsmModelListener>*/();
        
}
