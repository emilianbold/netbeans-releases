/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui;

import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import javax.swing.Action;


/**
 * Target base node is a base for any target node. The behaviour of this target 
 * base node can be customized/extended by the target node provided by the plugin.
 *
 * @author Nam Nguyen
 */
public class TargetBaseNode extends AbstractNode {
    
    public TargetBaseNode(Children children, ServerTarget target) {
		super(children);
        setDisplayName(target.getName());
        setIconBase(target.getInstance().getServer().getIconBase());
        getCookieSet().add(target);
    }
    
    public Action[] getActions(boolean b) {
        return new Action[] {};
    }
    
    protected ServerTarget getServerTarget() {
        return (ServerTarget) getCookie(ServerTarget.class);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Sheet createSheet() {
        return Sheet.createDefault();
    }
}
