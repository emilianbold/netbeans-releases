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
import java.io.IOException;
import java.net.URL;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectGenerator;

public class ProjectGenerator {

    public static String getDefaultProjectFolder() {
	return MakeProjectGenerator.getDefaultProjectFolder();
    }

    public static String getValidProjectName(String projectFolder) {
	return MakeProjectGenerator.getValidProjectName(projectFolder);
    }

    public static String getValidProjectName(String projectFolder, String suggestedProjectName) {
	return MakeProjectGenerator.getValidProjectName(projectFolder, suggestedProjectName);
    }

    public static Project createBlankProject(boolean open) throws IOException {
	return MakeProjectGenerator.createBlankProject(open);
    }

    public static Project createBlankProject(String projectName, String projectFolder) throws IOException {
	return MakeProjectGenerator.createBlankProject(projectName, projectFolder, false);
    }
    
    public static Project createBlankProject(String projectName, String makefileName, String projectFolder) throws IOException {
	return MakeProjectGenerator.createBlankProject(projectName, makefileName, projectFolder, false);
    }

    public static Project createBlankProject(String projectName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
	return MakeProjectGenerator.createBlankProject(projectName, projectFolder, confs, open);
    }

    public static void createProjectFromTemplate(URL url, String projectName, String projectFolder) throws IOException {
	 MakeSampleProjectGenerator.createProjectFromTemplate(url, new File(projectFolder + "/" + projectName), projectName); // NOI18N
    }
}
