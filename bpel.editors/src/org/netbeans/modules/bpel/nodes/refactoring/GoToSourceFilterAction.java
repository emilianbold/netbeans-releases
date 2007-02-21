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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes.refactoring;

import javax.swing.Action;
import org.netbeans.modules.bpel.nodes.actions.GoToSourceAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class GoToSourceFilterAction extends NodeAction {
    
    public GoToSourceFilterAction() {
    }

    protected void performAction(Node[] activatedNodes) {
        if (!enable(activatedNodes)) {
            return;
        }
        
        //search for GoToSource action and invoke it
        UsageFilterNode usageNode = (UsageFilterNode)activatedNodes[0];
        Action[] actions = usageNode.getActions(true);
        for (Action elem : actions) {
            if (elem instanceof GoToSourceAction) {
                invokeAction((GoToSourceAction)elem, usageNode);
            }
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length < 1) {
            return false;
        }
        
        return activatedNodes[0] instanceof UsageFilterNode;
    }

    public String getName() {
        return NbBundle.getMessage(GoToSourceAction.class,"ACT_GoToSourceAction");// NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private void invokeAction(GoToSourceAction action, UsageFilterNode usageNode) {
        Node[] nodes = new Node[] {usageNode.getOriginal()};
        if (action.enable(nodes)) {
            action.performAction(nodes);
        }
    }
}
