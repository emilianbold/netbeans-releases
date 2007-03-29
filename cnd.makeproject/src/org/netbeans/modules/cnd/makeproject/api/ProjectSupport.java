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

package org.netbeans.modules.cnd.makeproject.api;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.filesystems.FileObject;

public class ProjectSupport {
    public static boolean saveAllProjects(String extraMessage) {
	boolean ok = true;
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
	    MakeConfigurationDescriptor projectDescriptor = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(openProjects[i]);
	    if (projectDescriptor != null)
		ok = ok && projectDescriptor.save(extraMessage);
	}
	return ok;
    }

    public static Date lastModified(Project project) {
	FileObject projectFile = null;
	try {
	    projectFile = project.getProjectDirectory().getFileObject("nbproject" + File.separator + "Makefile-impl.mk"); // NOI18N
	}
	catch (Exception e) {
	    // happens if project is not a MakeProject
	}
	if (projectFile == null)
	    projectFile = project.getProjectDirectory();
	return projectFile.lastModified();
    }

    public static void executeCustomAction(Project project, CustomProjectActionHandler customProjectActionHandler) {
        ConfigurationDescriptorProvider pdp = (ConfigurationDescriptorProvider)project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        if (pdp == null)
            return;
        MakeConfigurationDescriptor projectDescriptor = (MakeConfigurationDescriptor)pdp.getConfigurationDescriptor();
        MakeConfiguration conf = (MakeConfiguration)projectDescriptor.getConfs().getActive();
        
        MakeActionProvider ap = (MakeActionProvider)project.getLookup().lookup(MakeActionProvider.class );
        if (ap == null)
            return;
        
        ProjectInformation info = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class );
        String projectName = info.getDisplayName();
        
        ArrayList actionEvents = new ArrayList();
        ap.addAction(actionEvents, projectName, projectDescriptor, conf, MakeActionProvider.COMMAND_CUSTOM_ACTION, null);
	ActionEvent ae = new ActionEvent((ProjectActionEvent[])actionEvents.toArray(new ProjectActionEvent[actionEvents.size()]), 0, null);
        DefaultProjectActionHandler defaultProjectActionHandler = new DefaultProjectActionHandler();
        defaultProjectActionHandler.setCustomActionHandlerProvider(customProjectActionHandler);
        defaultProjectActionHandler.actionPerformed(ae);
    }
}
