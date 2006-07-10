/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

import com.sun.collablet.CollabManager;
import org.netbeans.modules.collab.ui.options.AccountNode;

/**
 *
 *
 */
public class SetDefaultAccountAction extends NodeAction {
    /**
     *
     *
     */
    protected boolean enable(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();

        if (manager == null) {
            return false;
        }

        AccountNode node = (AccountNode) nodes[0].getCookie(AccountNode.class);

        return (node != null) && !node.isDefault();
    }

    public String getName() {
        return NbBundle.getMessage(SetDefaultAccountAction.class, "LBL_SetDefaultAccountAction_Name");
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    protected void performAction(Node[] nodes) {
        AccountNode node = (AccountNode) nodes[0].getCookie(AccountNode.class);

        // Sanity check to make sure the collab manager is present
        CollabManager manager = CollabManager.getDefault();
        assert manager != null : "CollabManager was null; action should not have been enabled"; // NOI18N
        manager.getUserInterface().setDefaultAccount(node.getAccount());
    }
}
