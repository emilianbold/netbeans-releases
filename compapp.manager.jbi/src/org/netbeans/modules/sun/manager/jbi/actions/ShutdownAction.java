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
import org.netbeans.modules.sun.manager.jbi.nodes.Refreshable;
import org.netbeans.modules.sun.manager.jbi.nodes.Shutdownable;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action to shutdown one or more JBI Components and/or Service Assemblies.
 *
 * @author jqian
 */
public abstract class ShutdownAction extends NodeAction {
    
    protected void performAction(final Node[] activatedNodes) {
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    for (Node node : activatedNodes) {
                        Lookup lookup = node.getLookup();
                        Shutdownable shutdownable = lookup.lookup(Shutdownable.class);
                        
                        if (shutdownable != null) {
                            shutdownable.shutdown(isForceAction());
                            
                            final Refreshable refreshable =
                                    lookup.lookup(Refreshable.class);
                            if (refreshable != null){
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        refreshable.refresh();
                                    }
                                });
                            }
                        }
                    }
                } catch (RuntimeException rex) {
                    //gobble up exception
                }
            }
        });
    }
    
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        
        if (nodes != null && nodes.length > 0) {
            ret = true;
            for (Node node : nodes) {
                Lookup lookup = node.getLookup();
                Shutdownable shutdownable = lookup.lookup(Shutdownable.class);
                try {
                    if (shutdownable != null && !shutdownable.canShutdown()) {
                        ret = false;
                        break;
                    }
                } catch (RuntimeException rex) {
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
    
//    void clearEnabledState() {
//        putProperty(PROP_ENABLED, null);
//    }
    
    protected abstract boolean isForceAction();
    
    
    //========================================================================//
    
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
    public static class Force extends ShutdownAction {
        
        public String getName() {
            return NbBundle.getMessage(ShutdownAction.class, "LBL_ForceShutdownAction");  // NOI18N
        }
        
        protected boolean isForceAction() {
            return true;
        }
    }
}
