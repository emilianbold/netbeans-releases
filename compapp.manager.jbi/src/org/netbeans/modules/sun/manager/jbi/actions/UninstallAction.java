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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.netbeans.modules.sun.manager.jbi.nodes.Uninstallable;
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
public class UninstallAction extends NodeAction {
    
    /**
     *
     */
    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    // a list of nodes that need refreshing
                    final List<Node> parentNodes = new ArrayList<Node>();    
                    for (int i = 0; i < activatedNodes.length; i++) {
                        Node node = activatedNodes[i];
                        Lookup lookup = node.getLookup();
                        Object obj = lookup.lookup(Uninstallable.class);
                        
                        if (obj instanceof Uninstallable) {
                            Uninstallable uninstallable = (Uninstallable)obj;
                            uninstallable.uninstall();
                            // There will be at most one parent node that
                            // needs refreshing
                            Node parentNode = node.getParentNode();
                            if (!parentNodes.contains(parentNode)) {
                                parentNodes.add(parentNode);
                            }
                        }
                    }
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            for (Iterator<Node> it = parentNodes.iterator(); it.hasNext();) {
                                Node parentNode = it.next();
                                final RefreshCookie refreshAction =
                                        (RefreshCookie) parentNode.getCookie(RefreshCookie.class);
                                if (refreshAction != null){
                                    refreshAction.refresh();
                                }
                            }
                        }
                    });                  
                    
                } catch(java.lang.RuntimeException rex) {
                    //gobble up exception
                }
            }
        });
    }
    
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        
        if (nodes != null && nodes.length > 0) {
            
            ret = true;
            
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                Lookup lookup = node.getLookup();
                Object obj = lookup.lookup(Uninstallable.class);
                
                try {
                    if(obj instanceof Uninstallable) {
                        Uninstallable uninstallable = (Uninstallable)obj;
                        if (!uninstallable.canUninstall()) {
                            ret = false;
                            break;
                        }
                    }
                } catch(java.lang.RuntimeException rex) {
                    //gobble up exception
                }
            }
        }
        
        return ret;
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
        return NbBundle.getMessage(UninstallAction.class, "LBL_UninstallAction"); // NOI18N
    }
    
}
