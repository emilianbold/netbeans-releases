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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.components.strikeiron.actions;

import org.netbeans.modules.websvc.components.strikeiron.ui.FindServicelDialog;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Search for web services offered through StrikeIron marketplace.
 * 
 * @author nam
 */
public class FindServiceAction extends NodeAction {

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(FindServiceAction.class, "LBL_FindStrikeIronServices");
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected void performAction(Node[] activatedNodes) {
        FindServicelDialog dialog = new FindServicelDialog();
        dialog.setVisible(true);
        if (dialog.getReturnStatus() == FindServicelDialog.RET_OK) {
            System.out.println("OK Add");
        } else {
            System.out.println("Cancel");
        }
    }

}
