/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.actions;

import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

import org.netbeans.modules.websvc.registry.nodes.*;
import org.netbeans.modules.websvc.registry.nodes.WebServiceGroupNode;
import org.openide.nodes.Node;

/** Add a webservice group node to the root node
 * @author  Winston Prakash
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
