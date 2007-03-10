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

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.netbeans.modules.j2ee.sun.bridge.apis.RefreshCookie;
import org.netbeans.modules.sun.manager.jbi.nodes.Shutdownable;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author jqian
 */
public abstract class ShutdownAction extends NodeAction {
    
    protected void performAction(final Node[] activatedNodes) {
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < activatedNodes.length; i++) {
                        Node node = activatedNodes[i];
                        Lookup lookup = node.getLookup();
                        Object obj = lookup.lookup(Shutdownable.class);
                        
                        if (obj instanceof Shutdownable) {
                            Shutdownable shutdownable = (Shutdownable)obj;
                            shutdownable.shutdown(isForceAction());
                            
                            final RefreshCookie refreshAction =
                                    (RefreshCookie) node.getCookie(RefreshCookie.class);
                            if (refreshAction != null){
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        refreshAction.refresh();
                                    }
                                });
                            }
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
                Lookup lookup = nodes[i].getLookup();
                Object obj = lookup.lookup(Shutdownable.class);
                
                try {
                    if(obj instanceof Shutdownable) {
                        Shutdownable shutdownable = (Shutdownable)obj;
                        if (!shutdownable.canShutdown()) {
                            ret = false;
                            break;
                        }
                    }
                } catch(RuntimeException rex) {
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
     * Normal shutdown action.
     */
    public static class Normal extends ShutdownAction {
        
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ShutdownAction");  // NOI18N
        }
        
        protected boolean isForceAction() {
            return false;
        }
    }
    
    /**
     * Force shutdown action.
     */
    public static class Force extends ShutdownAction /*implements DynamicMenuContent*/ {
        
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ForceShutdownAction");  // NOI18N
        }
        
        /*
        public JComponent[] getMenuPresenters() {
            return new JComponent [] { createMenu() };
        }
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return new JComponent [] { createMenu() };
        }
        
        private JMenu createMenu() {
            JMenu result = new JMenu(
                    NbBundle.getMessage(ShutdownAction.class, "LBL_Advanced"));  // NOI18N
            result.add(new JMenuItem(this));
            result.add(new JMenuItem(SystemAction.get(UninstallAction.Force.class)));
            return result;
        }
        */
        
        protected boolean isForceAction() {
            return true;
        }
    }
}
