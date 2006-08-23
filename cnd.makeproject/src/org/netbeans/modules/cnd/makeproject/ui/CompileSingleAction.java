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

package org.netbeans.modules.cnd.makeproject.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.spi.project.ActionProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.Lookups;

public class CompileSingleAction extends NodeAction {
    protected boolean enable(Node[] activatedNodes)  {
	boolean enabled = true;
	for (int i = 0; i < activatedNodes.length; i++) {
	    Node n = activatedNodes[i];
	    Project project = (Project)n.getValue("Project"); // NOI18N
	    Item item = (Item)n.getValue("Item"); // NOI18N
            if (project == null) {
                enabled = false;
                break;
            }
	    ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
	    if (ap != null)
		enabled = ap.isActionEnabled(ActionProvider.COMMAND_COMPILE_SINGLE, Lookups.fixed(new Object[] {project, n}));
	    if (!enabled)
		break;
	}
	return enabled;
    }

    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_CompileSingleAction"); // NOI18N
    }

    public void performAction(Node[] activatedNodes) {
	for (int i = 0; i < activatedNodes.length; i++) {
	    Node n = activatedNodes[i];
	    Project project = (Project)n.getValue("Project"); // NOI18N
	    Item item = (Item)n.getValue("Item"); // NOI18N
	    ActionProvider ap = (ActionProvider)project.getLookup().lookup(ActionProvider.class);
	    if (ap != null) 
		ap.invokeAction(ActionProvider.COMMAND_COMPILE_SINGLE, Lookups.fixed(new Object[] {project, n}));
	}
    }

    public HelpCtx getHelpCtx() {
	return null;
    }

    protected boolean asynchronous() {
	return false;
    }
}
