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

package org.netbeans.modules.cnd.makeproject.api.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class NewFolderAction extends NodeAction {
    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_NewFolderAction"); // NOI18N
    }

    public void performAction(Node[] activatedNodes) {
	Node n = activatedNodes[0];
	Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	assert folder != null;
	Node thisNode = (Node)n.getValue("This"); // NOI18N
	assert thisNode != null;
	Project project = (Project)n.getValue("Project"); // NOI18N
	assert project != null;
	Folder newFolder = folder.addNewFolder(true);
	MakeLogicalViewProvider.setVisible(project, newFolder); 
    }

    public boolean enable(Node[] activatedNodes) {
	if (activatedNodes.length != 1)
	    return false;
	Folder folder = (Folder)activatedNodes[0].getValue("Folder"); // NOI18N
	if (folder == null)
	    return false;
	if (!folder.isProjectFiles())
	    return false;
	return true;
    }

    public HelpCtx getHelpCtx() {
	return null;
    }

    protected boolean asynchronous() {
	return false;
    }
}
