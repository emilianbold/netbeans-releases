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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
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
import org.openide.windows.WindowManager;

/**
 * Various hacks that should be solved better later.
 */
public class Hacks {
    
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    
    /**
     * Show name of project corresponding to selection in Main Window title bar.
     * @author Jesse Glick
     */
    static void keepCurrentProjectNameUpdated() {
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
                    pname = NbBundle.getMessage(Hacks.class, "LBL_MultipleProjects");
                }
                Project p = OpenProjectList.getDefault().getMainProject();
                final String mname = p != null? 
                    ProjectUtils.getInformation(p).getDisplayName():
                    NbBundle.getMessage(Hacks.class, "LBL_NoMainProject");
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // depends on exported keys in core/windows
                        String format = NbBundle.getBundle("org.netbeans.core.windows.view.ui.Bundle").
                                getString(pname != null ? "CTL_MainWindow_Title" : "CTL_MainWindow_Title_No_Project");
                        String title = pname != null?
                            // Note that currently mname is ignored.
                            MessageFormat.format(format, BUILD_NUMBER, pname, mname) :
                            MessageFormat.format(format, BUILD_NUMBER, mname);
                        WindowManager.getDefault().getMainWindow().setTitle(title);
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
    
    
    /** Forces reload of panels in TemplateWizard. Public method updateState doesn't re-read
     * the new panels from new iterator.
     * Note: it should be solved better either fixing TemplateWizard or implement
     * whole NewFileWizard (w/o TemplateWizard).
     *
     * @param tw instance of TemplateWizard (thus NewFileWizard)
     * @param dobj template
     */    
    static void reloadPanelsInWizard (final TemplateWizard tw, final DataObject dobj) {
        try {
            Class<?> twClazz = Class.forName(
                "org.openide.loaders.TemplateWizard", true, // NOI18N
                Thread.currentThread().getContextClassLoader());
            if (twClazz != null) {
                Method reloadPanels = twClazz.getDeclaredMethod("reload", DataObject.class); // NOI18N
                reloadPanels.setAccessible (true);
                reloadPanels.invoke(tw, dobj);
            }
        } catch (Exception e) {
            // XXX
            e.printStackTrace();
        }
    }
}
