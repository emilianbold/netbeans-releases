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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Dialog;
import org.netbeans.modules.hudson.ui.wizard.InstanceWizard;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Add Hudson instance action launches the wizard.
 *
 * @author Michal Mocnak
 */
public class AddInstanceAction extends NodeAction {
    
    public void performAction(Node[] nodes) {
        InstanceWizard wizard = new InstanceWizard();
        Dialog dialog = null;
        
        try {
            dialog = DialogDisplayer.getDefault().createDialog(wizard);
            dialog.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(AddInstanceAction.class, "ACSD_Add_Instance"));
            dialog.setVisible(true);
        } finally {
            if (dialog != null)
                dialog.dispose();
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(AddInstanceAction.class, "LBL_Add_Instance");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean enable(Node[] nodes) {
        return true;
    }
    
    public boolean asynchronous() {
        return false;
    }
}