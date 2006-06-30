/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * Various hacks that should be solved better later.
 */
public class Hacks {
    
    private static Object windowSystemImpl = null;
    private static Method setProjectName = null;
    /**
     * Show name of project corresponding to selection in Main Window title bar.
     * @author Jesse Glick
     */
    static void keepCurrentProjectNameUpdated() {
        try {
            Class<?> windowSystemImplClazz = Class.forName(
                "org.netbeans.core.NbTopManager$WindowSystem", true, 
                Thread.currentThread().getContextClassLoader());
            windowSystemImpl = Lookup.getDefault().lookup(windowSystemImplClazz);
            if (windowSystemImpl != null) {
                setProjectName = windowSystemImplClazz.getMethod(
                    "setProjectName", new Class[] {String.class});
            }
        } catch (Exception e) {
            // OK.
            e.printStackTrace();
        }
        if (setProjectName != null) {
            final TopComponent.Registry r = TopComponent.getRegistry();
            final RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    Node[] sel = r.getActivatedNodes();
                    Set<Project> projects = new HashSet<Project>();
                    for (int i = 0; i < sel.length; i++) {
                        Lookup l = sel[i].getLookup();
                        Project p = l.lookup(Project.class);
                        if (p != null) {
                            projects.add(p);
                        } else {
                            DataObject d = l.lookup(DataObject.class);
                            if (d != null) {
                                FileObject f = d.getPrimaryFile();
                                p = FileOwnerQuery.getOwner(f);
                                if (p != null) {
                                    projects.add(p);
                                }
                            }
                        }
                    }
                    final String pname;
                    if (projects.size() == 1) {
                        Project p = projects.iterator().next();
                        pname = ProjectUtils.getInformation(p).getDisplayName();
                        assert pname != null : p;
                    } else if (projects.isEmpty()) {
                        pname = null;
                    } else {
                        pname = NbBundle.getMessage(Hacks.class, "LBL_MultipleProjects"); // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                setProjectName.invoke(windowSystemImpl, pname);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            r.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                        task.schedule(200);
                    }
                }
            });
        }
    }
    
    
    /** Forces reload of panels in TemplateWizard. Public method updateState doesn't re-read
     * the new panels from new iterator.
     * Note: it should be solved better either fixing TemplateWizard or implement
     * whole NewFileWizard (w/o TemplateWizard).
     *
     * @param tw instance of TemplateWizard (thus NewFileWizard)
     * @param dobj tempate
     */    
    static void reloadPanelsInWizard (final TemplateWizard tw, final DataObject dobj) {
        try {
            Class twClazz = Class.forName(
                "org.openide.loaders.TemplateWizard", true, // NOI18N
                Thread.currentThread().getContextClassLoader());
            if (twClazz != null) {
                Method reloadPanels = twClazz.getDeclaredMethod ("reload", new Class[] {DataObject.class}); // NOI18N
                reloadPanels.setAccessible (true);
                reloadPanels.invoke (tw, new Object[] {dobj});
            }
        } catch (Exception e) {
            // XXX
            e.printStackTrace();
        }
    }
}
