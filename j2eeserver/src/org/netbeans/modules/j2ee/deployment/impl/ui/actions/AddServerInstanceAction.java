/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import java.awt.Dialog;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.DialogDisplayer;
import org.netbeans.modules.j2ee.deployment.impl.ui.wizard.AddServerInstanceWizard;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class AddServerInstanceAction extends NodeAction {
    public void performAction(Node[] nodes) {
        AddServerInstanceWizard wizard = new AddServerInstanceWizard();
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        dialog.setVisible(true);
    }
    
    public String getName() {
        return NbBundle.getMessage(AddServerInstanceAction.class, "LBL_Add_Server_Instance");
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
