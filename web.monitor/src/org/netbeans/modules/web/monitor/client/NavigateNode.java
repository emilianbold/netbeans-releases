/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * NavigateNode.java
 *
 *
 * Created: Fri May 19 17:02:05 2000
 *
 * @author Ana von Klopp Lemon
 * @version
 */

package org.netbeans.modules.web.monitor.client; 

import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;

public class NavigateNode extends AbstractNode {
    
    public NavigateNode(Children ch) {
	super(ch);
	setIconBase("/org/netbeans/modules/web/monitor/client/icons/folder"); //NOI18N
	setName(NbBundle.getBundle(NavigateNode.class).getString("MON_All_transactions_2"));
	//initialize();
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {
	return new SystemAction[] {
	    SystemAction.get(DeleteAllAction.class),
	};
    }
    
} // NavigateNode
