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

import java.io.File;
import java.util.Date;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
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


    /**
     * Returns all open Projects that can build the executable specified by 'executablePath'.
     */
    /*
    public static Project[] findOpenProjects(String executablePath) {
	Vector found = new Vector();
	Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
	for (int i = 0; i < openProjects.length; i++) {
	    MakeArtifactProvider map = (MakeArtifactProvider)openProjects[i].getLookup().lookup(MakeArtifactProvider.class);
	    if (map == null)
		continue;
	    for (int j = 0; j < map.getBuildArtifacts().length; j++) {
		MakeArtifact makeArtifact =  map.getBuildArtifacts()[j];
		if (!makeArtifact.getType().equals(MakeArtifact.APP))
		    break;
		String path = map.getBuildArtifacts()[j].getArtifactApp();
		if (path != null && path.equals(executablePath)) {
		    found.add(openProjects[i]);
		    break;
		}
	    }
	}
	return (Project[]) found.toArray(new Project[found.size()]);
    }
    */

    /**
     * 
     */
    /*
    public static boolean canBuildExecutable(Project project, String executablePath) {
	    MakeArtifactProvider map = (MakeArtifactProvider)project.getLookup().lookup(MakeArtifactProvider.class);
	    if (map == null)
		return false;
	    for (int j = 0; j < map.getBuildArtifacts().length; j++) {
		MakeArtifact makeArtifact =  map.getBuildArtifacts()[j];
		if (!makeArtifact.getType().equals(MakeArtifact.APP))
		    return false;
		String path = map.getBuildArtifacts()[j].getArtifactApp();
		if (path != null && path.equals(executablePath)) {
		    return true;
		}
	    }
	return false;
    }
    */
}
