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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.ErrorManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Action to open the project(s) corresponding to selected folder(s).
 * Enabled only for projects which are not already open, and thus useful
 * for opening projects encountered in the Favorites tab, as well as nested
 * projects found beneath open projects in the Files tab.
 * @see "#54122"
 * @author Jesse Glick
 */
public final class OpenProjectFolderAction extends AbstractAction implements ContextAwareAction {
    
    public OpenProjectFolderAction() {
        // Label not likely displayed in GUI.
        super(NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_action"));
    }
    
    public void actionPerformed(ActionEvent e) {
        // Cannot be invoked without any context.
        assert false;
    }
    
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        /** Projects to be opened. */
        private final Set/*<Project>*/ projects;
        
        public ContextAction(Lookup context) {
            projects = new HashSet();
            // Collect projects corresponding to selected folders.
            Iterator it = context.lookupAll(DataFolder.class).iterator();
            while (it.hasNext()) {
                DataFolder d = (DataFolder) it.next();
                Project p = null;
                try {
                    p = ProjectManager.getDefault().findProject(d.getPrimaryFile());
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                if (p != null) {
                    projects.add(p);
                }
                // Ignore folders not corresponding to projects (will not disable action if some correspond to projects).
            }
            // Ignore projects which are already open (will not disable action if some can be opened).
            projects.removeAll(Arrays.asList(OpenProjectList.getDefault().getOpenProjects()));
            int size = projects.size();
            if (size == 1) {
                String name = ProjectUtils.getInformation((Project) projects.iterator().next()).getDisplayName();
                putValue(Action.NAME, NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_menu_one", name));
            } else if (size > 1) {
                putValue(Action.NAME, NbBundle.getMessage(OpenProjectFolderAction.class, "OpenProjectFolderAction.LBL_menu_multiple", new Integer(size)));
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            // Run asynch so that UI is not blocked; might show progress dialog (?).
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    OpenProjectList.getDefault().open((Project[]) projects.toArray(new Project[projects.size()]), false, true);
                }
            });
        }
        
        public JMenuItem getPopupPresenter() {
            class Presenter extends JMenuItem implements DynamicMenuContent {
                public Presenter() {
                    super(ContextAction.this);
                }
                public JComponent[] getMenuPresenters() {
                    if (!projects.isEmpty()) {
                        return new JComponent[] {this, null};
                    } else {
                        // Disabled, so do not display at all.
                        return new JComponent[0];
                    }
                }
                public JComponent[] synchMenuPresenters(JComponent[] items) {
                    return getMenuPresenters();
                }
            }
            return new Presenter();
        }
        
    }
    
}
