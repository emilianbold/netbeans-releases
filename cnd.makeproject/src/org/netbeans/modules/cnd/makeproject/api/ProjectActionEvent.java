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

import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.util.NbBundle;

public class ProjectActionEvent {
    public static final int BUILD = 0;
    public static final int CLEAN = 1;
    public static final int RUN = 2;
    public static final int DEBUG = 3;
    public static final int DEBUG_STEPINTO = 4;
    public static final int DEBUG_LOAD_ONLY = 5;
    
    public static final String[] actionNames  = {
        getString("BuildActionName"),
        getString("CleanActionName"),
        getString("RunActionName"),
        getString("DebugActionName"),
        getString("DebugActionName"),
        getString("DebugActionName"),
    };

    private Project project;
    private int type;
    private String tabName;
    private String executable;
    private Configuration configuration;
    private RunProfile profile;
    private boolean wait;

    public ProjectActionEvent(Project project, int type, String tabName, String executable, Configuration configuration, RunProfile profile, boolean wait) {
        this.project = project;
        this.type = type;
	this.tabName = tabName;
	this.executable = executable;
	this.configuration = configuration;
	this.profile = profile;
	this.wait = wait;
    }
    
    public Project getProject() {
        return project;
    }
    
    public int getID() {
        return type;
    }

    public String getTabName() {
	return tabName;
    }

    public String getActionName() {
        return actionNames[getID()];
    }

    public String getExecutable() {
	return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public Configuration getConfiguration() {
	return configuration;
    }

    public RunProfile getProfile() {
	if (profile != null)
	    return profile;
	else
	    return configuration.getProfile();
    }

    public boolean getWait() {
	return wait;
    }
    
     /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ProjectActionEvent.class);
        }
        return bundle.getString(s);
    }
}
