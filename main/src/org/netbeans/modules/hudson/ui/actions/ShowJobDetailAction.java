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

import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.HudsonJobView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 * Action which shows HudsonJobView component.
 */
public class ShowJobDetailAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        for (Node n : nodes) {
            HudsonJob job = n.getLookup().lookup(HudsonJob.class);
            
            if (null == job)
                continue;
            
            // Create TopComponent
            final TopComponent win = HudsonJobView.getInstance(job);
            
            Mutex.EVENT.postReadRequest(new Runnable() {
                public void run() {
                    win.open();
                    win.requestActive();
                }
            });
        }
    }
    
    protected boolean enable(Node[] nodes) {
        for (Node n : nodes) {
            HudsonJob job = n.getLookup().lookup(HudsonJob.class);
            
            if (null == job)
                return false;
        }
        
        return true;
    }
    
    public String getName() {
        return NbBundle.getMessage(ShowJobDetailAction.class, "LBL_ShowJobDetailAction");
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}