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

package org.netbeans.modules.cnd.debugger.gdb.profiles.ui;

import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.nodes.Sheet;

import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;

import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;

public class ProfileNodeProvider {

    private ResourceBundle bundle;

    public CustomizerNode createDebugNode() {

	CustomizerNode debugRootNode = new CndProfileGeneralCustomizerNode(
		    "Debug", // NOI18N
		    getString("Debug"), // NOI18N
		    null);

	return debugRootNode;
    }

    class CndProfileGeneralCustomizerNode extends CustomizerNode {

	public CndProfileGeneralCustomizerNode(String name, String displayName,
		    CustomizerNode[] children) {
	    super(name, displayName, children);
	}

	public Sheet getSheet(Project project,
		    ConfigurationDescriptor configurationDescriptor,
		    Configuration configuration) {
	    GdbProfile profile = (GdbProfile) configuration.getAuxObject(GdbProfile.GDB_PROFILE_ID);
	    return profile == null ? null : profile.getSheet();
	}
    }

    /** Look up i18n strings here */
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ProfileNodeProvider.class);
	}
	return bundle.getString(s);
    }
}
