/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * ServerRegNode.java -- synopsis
 *
 */
package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.*;

/**
 * @author George FinKlang
 */

public class InstanceNode extends AbstractNode {
    
    public InstanceNode(ServerInstance instance) {
        super(new Children.Array());
        setDisplayName(instance.getDisplayName());
        setIconBase(instance.getServer().getIconBase());
        getCookieSet().add(instance);
        ServerTarget[] targets = instance.getTargets();
        for(int i = 0; i < targets.length; i++) 
            getChildren().add(new Node[] {new TargetNode(targets[i])});
    }
    
 /*   public SystemAction[] createActions() {
        return new SystemAction[] { 
            SystemAction.get(SetAsDefaultServerAction.class),
            SystemAction.get(NodeHelpAction.class)
        };
    }
    */
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
