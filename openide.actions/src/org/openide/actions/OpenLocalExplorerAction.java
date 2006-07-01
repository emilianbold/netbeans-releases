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
package org.openide.actions;

import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/** Open an Explorer window with a particular root node.
* @see NodeOperation#explore
* @author   Ian Formanek
*/
public final class OpenLocalExplorerAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        NodeOperation.getDefault().explore(activatedNodes[0]);
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1) || (activatedNodes[0].isLeaf())) {
            return false;
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(OpenLocalExplorerAction.class, "OpenLocalExplorer");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenLocalExplorerAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/openLocalExplorer.gif"; // NOI18N
    }
}
