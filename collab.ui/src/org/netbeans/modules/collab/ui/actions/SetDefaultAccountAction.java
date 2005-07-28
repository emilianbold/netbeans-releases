/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.actions;

import com.sun.collablet.CollabManager;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;
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
