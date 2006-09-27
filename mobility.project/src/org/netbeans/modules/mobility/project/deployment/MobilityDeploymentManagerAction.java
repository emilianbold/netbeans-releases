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

package org.netbeans.modules.mobility.project.deployment;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 *
 * @author Adam Sotona
 */
public class MobilityDeploymentManagerAction extends CallableSystemAction {
    
    /** Creates a new instance of MobilityDeploymentManagerAction */
    public MobilityDeploymentManagerAction() {
    }

    public synchronized void performAction() {
        DialogDisplayer.getDefault().notify(new DialogDescriptor(new MobilityDeploymentManagerPanel(), NbBundle.getMessage(MobilityDeploymentManagerAction.class, "Title_DeploymentManager"), true, new Object[] {DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION, DialogDescriptor.DEFAULT_ALIGN, getHelpCtx(), null));  //NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(MobilityDeploymentManagerAction.class, "LBL_DeploymentManagerAction"); //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MobilityDeploymentManagerPanel.class);
    }

    protected boolean asynchronous() {
        return false;
    }
    
}
