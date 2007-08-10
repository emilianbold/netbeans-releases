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

package org.netbeans.modules.sun.manager.jbi.nodes;

import javax.swing.Action;

import org.netbeans.modules.sun.manager.jbi.management.AppserverJBIMgmtController;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author jqian
 */
public abstract class AppserverJBIMgmtLeafNode extends AppserverJBIMgmtNode {

    /**
     * Abstract constructor for an AppserverLeafNode called by subclass.
     *
     * @param nodeType The type of leaf node to construct (e.g. JVM, etc.)
     */
    public AppserverJBIMgmtLeafNode(final AppserverJBIMgmtController controller, 
            final NodeType nodeType) {
        super(controller, Children.LEAF, nodeType);
    }
    
  
    /**
     * Return the actions associated with the menu drop down seen when
     * a user right-clicks on a node in the plugin.
     *
     * @param boolean true/false
     * @return An array of Action objects.
     */
    public Action[] getActions(boolean flag) {
        return new SystemAction[] {
            SystemAction.get(PropertiesAction.class)
        };
    }
    
    /**
     * Return the default action for the node
     * a user double-clicks on a node in the plugin.
     */
    public Action getPreferredAction(){
        return SystemAction.get(PropertiesAction.class);
    }
}
