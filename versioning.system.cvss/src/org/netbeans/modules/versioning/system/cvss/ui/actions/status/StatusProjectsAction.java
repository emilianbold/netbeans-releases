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

package org.netbeans.modules.versioning.system.cvss.ui.actions.status;

import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;

import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;

/**
 * Open the Versioning status view for all projects
 * 
 * @author Maros Sandor
 */
public class StatusProjectsAction extends SystemAction {
    
    private static final ResourceBundle loc = NbBundle.getBundle(StatusProjectsAction.class);

    public StatusProjectsAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N        
    }

    public String getName() {
        return loc.getString("CTL_MenuItem_StatusProjects_Label");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(StatusProjectsAction.class);
    }

    public boolean isEnabled() {
        return CvsModuleConfig.getDefault().getManagedRoots().size() > 0;
    }

    public void actionPerformed(ActionEvent e) {
        CvsSynchronizeTopComponent stc = CvsSynchronizeTopComponent.getInstance();
        Project [] projects = OpenProjects.getDefault().getOpenProjects();

        File [] roots = (File[]) Utils.getProjectsSources(projects).toArray(new File[0]);
        String title;
        if (projects.length == 1) {
            Project project = projects[0];
            ProjectInformation pinfo = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
            if (pinfo != null) {
                title = pinfo.getDisplayName();
            } else {
                title = FileUtil.toFile(project.getProjectDirectory()).getName(); 
            }            
        } else {
            title = MessageFormat.format(loc.getString("CTL_StatusProjects_WindowTitle"), 
                                         new Object [] { Integer.toString(projects.length) });
        }
        stc.setContentTitle(title);
        stc.setRoots(roots);
        stc.open(); 
        stc.requestActive();
        if (shouldPostRefresh()) {
            stc.performRefreshAction();
        }
    }

    protected boolean shouldPostRefresh() {
        return true;
    }
}
