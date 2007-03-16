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
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.MakeSources;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class RemoveItemAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes)  {
	return true;
    }

    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_RemoveItemActionName"); // NOI18N
    }

    public void performAction(Node[] activatedNodes) {
	for (int i = 0; i < activatedNodes.length; i++) {
	    Node n = activatedNodes[i];
	    Project project = (Project)n.getValue("Project"); // NOI18N
	    Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	    Item item = (Item)n.getValue("Item"); // NOI18N
	    folder.removeItemAction(item);
	    if (IpeUtils.isPathAbsolute(item.getPath()))
		((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();
	}
    }

    public HelpCtx getHelpCtx() {
	return null;
    }

    protected boolean asynchronous() {
	return false;
    }
}
