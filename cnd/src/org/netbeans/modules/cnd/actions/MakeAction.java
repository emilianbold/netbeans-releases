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

package org.netbeans.modules.cnd.actions;

import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * Implements Make action
 */
public class MakeAction extends MakeBaseAction {

    public String getName () {
	return getString("BTN_Execute");	// NOI18N
    }

    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++)
            performAction(activatedNodes[i], ""); // NOI18N
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param nodes A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node) {
        ((MakeAction)SystemAction.get(MakeAction.class)).performAction(node, ""); // NOI18N
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param nodes A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node, String target) {
        ((MakeAction)SystemAction.get(MakeAction.class)).performAction(node, target);
    }

    protected String iconResource() {
        return "org/netbeans/modules/cnd/resources/MakeAction.gif"; // NOI18N
    }

    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeAction.class); // FIXUP ???
    }
}
