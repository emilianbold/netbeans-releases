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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.j2ee.deployment.config.Utils;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Resfresh action refreshes the server state.
 *
 * @author  nn136682
 */
public class RefreshAction extends NodeAction {
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "LBL_Refresh");
    }
    
    protected void performAction(Node[] nodes) {
        performActionImpl(nodes);
    }
    
    protected boolean enable(Node[] nodes) {
        return enableImpl(nodes);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() { 
        return false; 
    }
    
    // private helper methods -------------------------------------------------
    
    private static void performActionImpl(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance)nodes[i].getCookie(ServerInstance.class);
            if (si != null) {
                si.refresh();
            }
        }
    }
    
    private static boolean enableImpl(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance)nodes[i].getCookie(ServerInstance.class);
            if (si == null || si.getServerState() == ServerInstance.STATE_WAITING) {
                return false;
            }
        }
        return true;
    }
    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ServerInstance.StateListener {
        
        private static final String ICON = 
                "org/netbeans/modules/j2ee/deployment/impl/ui/resources/refresh.png"; // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private Node node;
        
        public OutputAction(Node node) {
            super(NbBundle.getMessage(DebugAction.class, "LBL_RefreshOutput"),
                  new ImageIcon(Utilities.loadImage(ICON)));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(DebugAction.class, "LBL_RefreshOutputDesc"));
            this.node = node;
            
            // start listening to changes
            ServerInstance si = (ServerInstance)node.getCookie(ServerInstance.class);
            si.addStateListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            performActionImpl(new Node[] {node});
        }

        public boolean isEnabled() {
            return enableImpl(new Node[] {node});
        }
        
        // ServerInstance.StateListener implementation --------------------------
        
        public void stateChanged(final int oldState, final int newState) {
            Utils.runInEventDispatchThread(new Runnable() {
                public void run() {
                    firePropertyChange(
                        PROP_ENABLED, 
                        null,
                        isEnabled() ? Boolean.TRUE : Boolean.FALSE);
                }
            });
        }
    }
}
