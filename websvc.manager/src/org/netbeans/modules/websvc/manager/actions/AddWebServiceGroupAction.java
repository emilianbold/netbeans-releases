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

package org.netbeans.modules.websvc.manager.actions;

import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

import org.netbeans.modules.websvc.manager.nodes.*;
import org.openide.nodes.Node;

/** Add a webservice group node to the root node
 */
public class AddWebServiceGroupAction extends NodeAction {
    
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new HelpCtx(AddWebServiceGroupAction.class);
    }
    
    public String getName() {
        return NbBundle.getMessage(AddWebServiceGroupAction.class, "ADD_GROUP");
    }
    
    protected void performAction(Node[] nodes) {
        WebServiceListModel wsNodeModel = WebServiceListModel.getInstance();
        String newName = NbBundle.getMessage(AddWebServiceGroupAction.class, "NEW_GROUP"); 
        WebServiceGroup wsGroup =  new WebServiceGroup();
        wsGroup.setName(newName);
        wsNodeModel.addWebServiceGroup(wsGroup);
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    protected boolean asynchronous() {
        return false;
    }
}
