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

package org.netbeans.modules.hudson.ui.actions;

import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which synchronize instance's jobs
 *
 * @author Michal Mocnak
 */
public class SynchronizeAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (Node node : nodes) {
            HudsonInstanceImpl instance = node.getLookup().lookup(HudsonInstanceImpl.class);
            
            if(instance != null)
                instance.synchronize();
        }
    }
    
    protected boolean enable(Node[] nodes) {
        for (Node node : nodes) {
            HudsonInstanceImpl instance = node.getLookup().lookup(HudsonInstanceImpl.class);
            
            if (instance != null)
                return true;
        }
        
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(OpenUrlAction.class, "LBL_SynchronizeAction"); // NOI18N
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}