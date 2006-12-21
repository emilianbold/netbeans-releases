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

package org.netbeans.modules.cnd.makeproject.runprofiles;

import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;

public class RunProfileNodeProvider {
    public CustomizerNode createProfileNode() {
            return new RunProfileCustomizerNode(
                "Running", // NOI18N
                getString("RUNNING"),
                null);
    }

    class RunProfileCustomizerNode extends CustomizerNode {
	public RunProfileCustomizerNode(String name, String displayName, CustomizerNode[] children) {
	    super(name, displayName, children);
	}

	public Sheet getSheet(Project project, ConfigurationDescriptor configurationDescriptor, Configuration configuration) {
	    RunProfile runProfile = (RunProfile) configuration.getAuxObject(RunProfile.PROFILE_ID);
	    return runProfile != null ? runProfile.getSheet() : null;
	    //return configurationDescriptor.getSheet(project, configuration);
	}
    }
    
    /** Look up i18n strings here */
    private ResourceBundle bundle;
    protected String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(RunProfileNodeProvider.class);
	}
	return bundle.getString(s);
    }
}
