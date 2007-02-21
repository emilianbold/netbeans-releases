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

package org.netbeans.modules.sun.manager.jbi.actions;

import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.netbeans.modules.sun.manager.jbi.nodes.Deployable;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author jqian
 */
public class DeployAction extends NodeAction {

    /**
     *
     *
     */
    protected void performAction(Node[] activatedNodes) {
        final Node node = activatedNodes[0];
        Lookup lookup = node.getLookup();
        final Object obj = lookup.lookup(Deployable.class);
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    if (obj instanceof Deployable) {
                        Deployable deployable = (Deployable)obj;
                        deployable.deploy();
                        
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                RefreshCookie refreshAction =
                                        (RefreshCookie) node.getCookie(
                                        RefreshCookie.class);
                                if (refreshAction != null){
                                    refreshAction.refresh();
                                }
                            }
                        });
                    }
                } catch(java.lang.RuntimeException rex) {
                    //gobble up exception
                }
            }
        });
    }
    
    /**
     *
     *
     */
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1;
    }
    
    
    /**
     *
     *
     */
    protected boolean asynchronous() {
        return false;
    }
    
    
    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    
    /**
     *
     */
    public String getName() {
        return NbBundle.getMessage(DeployAction.class, "LBL_DeployAction"); // NOI18N
    }
    
}
