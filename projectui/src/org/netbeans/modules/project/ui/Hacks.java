/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

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
            Class windowSystemImplClazz = Class.forName(
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
            r.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent ev) {
                    if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                        Node[] sel = r.getActivatedNodes();
                        Set/*<Project>*/ projects = new HashSet();
                        for (int i = 0; i < sel.length; i++) {
                            Lookup l = sel[i].getLookup();
                            Project p = (Project)l.lookup(Project.class);
                            if (p != null) {
                                projects.add(p);
                            } else {
                                DataObject d = (DataObject)l.lookup(DataObject.class);
                                if (d != null) {
                                    FileObject f = d.getPrimaryFile();
                                    p = FileOwnerQuery.getOwner(f);
                                    if (p != null) {
                                        projects.add(p);
                                    }
                                }
                            }
                        }
                        String pname;
                        if (projects.size() == 1) {
                            Project p = (Project) projects.iterator().next();
                            pname = ProjectUtils.getInformation(p).getDisplayName();
                            assert pname != null : p;
                        } else if (projects.isEmpty()) {
                            pname = null;
                        } else {
                            pname = NbBundle.getMessage (Hacks.class, "LBL_MultipleProjects"); // NOI18N
                        }
                        try {
                            setProjectName.invoke(windowSystemImpl, new Object[] {pname});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
