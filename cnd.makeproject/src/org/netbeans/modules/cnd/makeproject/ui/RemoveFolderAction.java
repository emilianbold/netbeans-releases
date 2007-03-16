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

import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class RemoveFolderAction extends NodeAction {
    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_RemoveFolderActionName"); // NOI18N
    }

    public void performAction(Node[] activatedNodes) {
	for (int i = 0; i < activatedNodes.length; i++) {
	    Node n = activatedNodes[i];
	    Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	    assert folder != null;
	    String txt = NbBundle.getMessage(getClass(), "LBL_RemoveFolderActionDialogTxt", folder.getDisplayName()); // NOI18N
	    NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, NbBundle.getMessage(getClass(), "LBL_RemoveFolderActionDialogTitle"), NotifyDescriptor.OK_CANCEL_OPTION); // NOI18N
	    if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
		Folder parentFolder = folder.getParent();
		assert parentFolder != null;
		parentFolder.removeFolderAction(folder);
	    }
	}
    }

    public boolean enable(Node[] activatedNodes) {
	return true;
    }

    public HelpCtx getHelpCtx() {
	return null;
    }

    protected boolean asynchronous() {
	return false;
    }
}
