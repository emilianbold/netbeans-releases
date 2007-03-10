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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.netbeans.modules.sun.manager.jbi.nodes.Undeployable;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author jqian
 */
public abstract class UndeployAction extends NodeAction {

    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    Node parentNode = null; // the node that needs refreshing
                    for (int i = 0; i < activatedNodes.length; i++) {
                        Node node = activatedNodes[i];
                        Lookup lookup = node.getLookup();
                        Object obj = lookup.lookup(Undeployable.class);
                        
                        if (obj instanceof Undeployable) {
                            // There will be at most one parent node that
                            // needs refreshing
                            if (parentNode == null) {
                                parentNode = node.getParentNode();
                            }
                            Undeployable undeployable = (Undeployable)obj;
                            undeployable.undeploy(isForceAction());
                        }
                    }
                    
                    if (parentNode != null) {
                        final RefreshCookie refreshAction =
                                (RefreshCookie) parentNode.getCookie(RefreshCookie.class);
                        if (refreshAction != null){
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    refreshAction.refresh();
                                }
                            });
                        }
                    }
                } catch(RuntimeException rex) {
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
                Object obj = lookup.lookup(Undeployable.class);
                
                try {
                    if(obj instanceof Undeployable) {
                        Undeployable undeployable = (Undeployable)obj;
                        if (!undeployable.canUndeploy()) {
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
    
    protected boolean asynchronous() {
        return false;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    protected abstract boolean isForceAction();
    
    
    /**
     * Normal undeploy action.
     */
    public static class Normal extends UndeployAction {
        
        protected boolean isForceAction() {
            return false;
        }
        
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_UndeployAction");  // NOI18N
        }
    }
    
    /**
     * Force undeploy action.
     */
    public static class Force extends UndeployAction implements Presenter.Popup {
        
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ForceUndeployAction");  // NOI18N
        }
        
        public JMenuItem getPopupPresenter() {
            JMenu result = new JMenu(
                    NbBundle.getMessage(ShutdownAction.class, "LBL_Advanced"));  // NOI18N
            result.add(new JMenuItem(SystemAction.get(ShutdownAction.Force.class)));
            result.add(new JMenuItem(this));
            return result;
        }        
                  
        protected boolean isForceAction() {
            return true;
        }        
    }
    
}
