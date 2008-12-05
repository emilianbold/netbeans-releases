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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    public static final int CHECK_EXECUTABLE = 6;
    public static final int CUSTOM_ACTION = 7;
    
    public static final String[] actionNames  = {
        getString("BuildActionName"),
        getString("CleanActionName"),
        getString("RunActionName"),
        getString("DebugActionName"),
        getString("DebugActionName"),
        getString("DebugActionName"),
        getString("CheckExecutableActionName"),
        getString("CustomActionName"),
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
