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

package org.netbeans.modules.websvc.registry.actions;

import org.openide.nodes.Node;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.ui.AddWebServiceDlg;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;


/**
 *
 * @author  octav
 */
public class AddWebServiceAction extends NodeAction {

    WebServiceListModel wsListModel = WebServiceListModel.getInstance();

    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceAction.class, "ADD_WEB_SERVICE");
    }
    
    protected void performAction(Node[] nodes) {
        // Show dialog that enables to browse for W/S 
        //WebServiceGroup wsGroup = wsListModel.getWebServiceGroup(nodes[0].getName());
		
		// !PW sometimes this action is called with no active node.  In such cases
		// the service will be added to the default group.  Otherwise, the group
		// indicated by the activated node will be used.
        Node activatedNode = null;
		if(nodes != null && nodes.length > 0) {
			activatedNode = nodes[0];
		}
		
        AddWebServiceDlg dlg = new AddWebServiceDlg(true, activatedNode);
        dlg.displayDialog();
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
