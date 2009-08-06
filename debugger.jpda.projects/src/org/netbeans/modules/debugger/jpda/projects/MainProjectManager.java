/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;

import javax.swing.SwingUtilities;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;

/**
 * Provies access to the main or currently selected project.
 *
 * @author   Jan Jancura, Martin Entlicher
 */
public class MainProjectManager implements ProjectActionPerformer, PropertyChangeListener {

    public static final String PROP_MAIN_PROJECT = "mainProject";   // NOI18N

    public static final String PROP_SELECTED_PROJECT = "selectedProject";   // NOI18N

    private static MainProjectManager mainProjectManager = new MainProjectManager ();

    public static MainProjectManager getDefault () {
        return mainProjectManager;
    }
    
    
    private Action a;
    private Project currentProject;
    private Reference<Project> lastSelectedProjectRef = new WeakReference(null);
    private boolean isMainProject;
    private PropertyChangeSupport pcs;


    private MainProjectManager () {
        pcs = new PropertyChangeSupport (this);
        a = ProjectSensitiveActions.projectSensitiveAction (
            this, "x", null
        );
        OpenProjects.getDefault().addPropertyChangeListener(this);
        currentProject = OpenProjects.getDefault().getMainProject();
        isMainProject = currentProject != null;
        a.addPropertyChangeListener(this); // I'm listening on it so that I get enable() called.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                a.isEnabled();
            }
        });
    }

    public synchronized Project getMainProject () {
        Project lastSelectedProject = lastSelectedProjectRef.get();
        if (isMainProject && lastSelectedProject != null &&
            !isDependent(lastSelectedProject, currentProject)) {
            // If there's a main project set, but the current project has no
            // dependency on it, return the current project.
            //System.err.println("getMainProject() = (LS) "+lastSelectedProject);
            return lastSelectedProject;
        }
        //System.err.println("getMainProject() = "+currentProject);
        return currentProject;
    }

    public void perform (Project p) {
        assert false : "Fake action should never really be called";
    }

    public boolean enable (Project p) {
        Project old = p;
        Project oldSelected = p;
        synchronized (this) {
            if (isMainProject) {
                oldSelected = lastSelectedProjectRef.get();
            }
            lastSelectedProjectRef = new WeakReference(p);
            if (!isMainProject) {
                if (currentProject != p) {
                    old = currentProject;
                    currentProject = p;
                }
            }
        }
        //System.err.println("MainProjectManager.enable("+p+") old = "+old+", oldSelected = "+oldSelected);
        if (old != p) {
            pcs.firePropertyChange (PROP_MAIN_PROJECT, old, p);
        }
        if (oldSelected != p) {
            pcs.firePropertyChange (PROP_SELECTED_PROJECT, oldSelected, p);
        }
        return true; // unused
    }

    /**
     * Test whether one project is dependent on the other.
     * @param p1 dependent project
     * @param p2 main project
     * @return <code>true</code> if project <code>p1</code> depends on project <code>p2</code>
     */
    private static boolean isDependent(Project p1, Project p2) {
        Set<URL> p1Roots = getProjectRoots(p1);
        Set<URL> p2Roots = getProjectRoots(p2);

        for (URL root : p2Roots) {
            Set<URL> dependentRoots = SourceUtils.getDependentRoots(root);
            for (URL sr : p1Roots) {
                if (dependentRoots.contains(sr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Set<URL> getProjectRoots(Project p) {
        Set<URL> projectRoots = new HashSet<URL>(); // roots
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] sgs = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            URL root;
            try {
                root = sg.getRootFolder().getURL();
                projectRoots.add(root);
            } catch (FileStateInvalidException fsiex) {
                continue;
            } catch (NullPointerException npe) {
                // http://www.netbeans.org/issues/show_bug.cgi?id=148076
                if (sg == null) {
                    npe = Exceptions.attachMessage(npe, "Null source group returned from "+sources+" of class "+sources.getClass());
                } else if (sg.getRootFolder() == null) {
                    npe = Exceptions.attachMessage(npe, "Null root folder returned from "+sg+" of class "+sg.getClass());
                }
                Exceptions.printStackTrace(npe);
            }
        }
        return projectRoots;
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //System.err.println("MainProjectManager.propertyChange("+evt+") name = "+evt.getPropertyName());
        if (OpenProjects.PROPERTY_MAIN_PROJECT.equals(evt.getPropertyName())) {
            Project theMainProject = OpenProjects.getDefault().getMainProject();
            Project old = theMainProject;
            synchronized (this) {
                isMainProject = theMainProject != null;
                old = currentProject;
                if (isMainProject) {
                    currentProject = theMainProject;
                } else {
                    currentProject = lastSelectedProjectRef.get();
                }
            }
            //System.err.println(" main project = "+theMainProject+", old = "+old);
            if (old != theMainProject) {
                pcs.firePropertyChange (PROP_MAIN_PROJECT, old, theMainProject);
            }
        } else if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            // Test if the current project is still opened:
            Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
            Project theMainProject = OpenProjects.getDefault().getMainProject();
            Project currentGone = null;
            Project lastGone = null;
            synchronized (this) {
                boolean isCurrent = false;
                boolean isLast = false;
                Project last = lastSelectedProjectRef.get();
                for (Project p : openProjects) {
                    if (p == currentProject) {
                        isCurrent = true;
                    }
                    if (p == last) {
                        isLast = true;
                    }
                }
                if (!isCurrent && currentProject != null) {
                    currentGone = currentProject;
                    currentProject = null;
                }
                if (!isLast && last != null) {
                    if (isMainProject) {
                        lastGone = last;
                    }
                    lastSelectedProjectRef = new WeakReference<Project>(null);
                }
                isMainProject = theMainProject != null;
            }
            if (currentGone != null) {
                pcs.firePropertyChange (PROP_MAIN_PROJECT, currentGone, null);
            }
            if (lastGone != null) {
                pcs.firePropertyChange (PROP_SELECTED_PROJECT, lastGone, null);
            }
        }
    }
}
