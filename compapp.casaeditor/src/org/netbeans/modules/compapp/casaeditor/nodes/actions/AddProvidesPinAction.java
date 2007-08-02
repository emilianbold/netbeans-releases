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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;

import javax.swing.SwingUtilities;
import javax.swing.Action;

/**
 * Add a consumes pin to the selected service unit.
 *
 * User: tli
 * Date: Aug 1, 2007
 * To change this template use File | Settings | File Templates.
 */
public class AddProvidesPinAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(LoadWSDLPortsAction.class, "LBL_AddProvidesPinAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Action getAction() {
        return this;
    }

    public void performAction(Node[] activatedNodes) {
        if (activatedNodes.length < 1) {
            return;
        }

        if (activatedNodes[0] instanceof ServiceUnitNode) {
            final ServiceUnitNode node = ((ServiceUnitNode) activatedNodes[0]);
            final CasaWrapperModel model = node.getModel();
            final CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) node.getData();
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    model.addEndpointToServiceEngineServiceUnit(su, false);
                }
            });
        }
    }
}

