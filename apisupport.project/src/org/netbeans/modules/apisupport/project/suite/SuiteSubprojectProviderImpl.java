/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.suite;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 * Lists modules in a suite.
 * @author Jesse Glick
 */
final class SuiteSubprojectProviderImpl implements SubprojectProvider {
    
    private Set/*<Project>*/ projects;
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    
    private final Set/*<ChangeListener>*/ listeners = new HashSet();
    private boolean reloadNeeded;
    
    public SuiteSubprojectProviderImpl(AntProjectHelper helper, PropertyEvaluator eval) {
        this.helper = helper;
        this.eval = eval;
        eval.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("modules".equals(evt.getPropertyName())) { // NOI18N
                    SuiteSubprojectProviderImpl.this.reloadNeeded = true;
                    SuiteSubprojectProviderImpl.this.fireChange();
                }
            }
        });
    }
    
    public Set/*<Project>*/ getSubprojects() {
        if (projects == null || reloadNeeded) {
            projects = loadProjects();
            reloadNeeded = false;
        }
        return projects;
    }
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    private void fireChange() {
        Iterator it;
        ChangeEvent e = new ChangeEvent(this);
        synchronized (listeners) {
            if (listeners.isEmpty()) {
                return;
            }
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ChangeListener l = (ChangeListener) it.next();
            l.stateChanged(e);
        }
    }
    
    private Set/*<Project>*/ loadProjects() {
        Set/*<Project>*/ newProjects = new HashSet();
        String modules = eval.getProperty("modules"); // NOI18N
        if (modules != null) {
            String[] pieces = PropertyUtils.tokenizePath(modules);
            for (int i = 0; i < pieces.length; i++) {
                FileObject dir = helper.resolveFileObject(pieces[i]);
                if (dir != null) {
                    try {
                        Project subp = ProjectManager.getDefault().findProject(dir);
                        if (subp != null) {
                            newProjects.add(subp);
                        }
                    } catch (IOException e) {
                        Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(newProjects);
    }
    
}
