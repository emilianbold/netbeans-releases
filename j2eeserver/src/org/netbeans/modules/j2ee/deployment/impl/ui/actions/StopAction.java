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

package org.netbeans.modules.j2ee.deployment.impl.ui.actions;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.j2ee.deployment.impl.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;


/**
 * Stop action stops the server.
 *
 * @author  sherold
 */
public class StopAction extends NodeAction {
    
    public String getName() {
        return NbBundle.getMessage(StopAction.class, "LBL_Stop");
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
            final ServerInstance si = (ServerInstance)nodes[i].getCookie(ServerInstance.class);
            si.setServerState(ServerInstance.STATE_WAITING);
            if (si != null) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        String title = NbBundle.getMessage(StopAction.class, "LBL_Stopping", si.getDisplayName());
                        ProgressUI progressUI = new ProgressUI(title, false);
                        try {
                            progressUI.start();
                            si.stop(progressUI);
                        } finally {
                            progressUI.finish();
                        }
                    }
                });
            }
        }
    }
    
    private static boolean enableImpl(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = (ServerInstance)nodes[i].getCookie(ServerInstance.class);
            if (si == null) {
                return false;
            }
            int state = si.getServerState();
            if (state != ServerInstance.STATE_RUNNING && state != ServerInstance.STATE_DEBUGGING) {
                return false;
            }
        }
        return true;
    }
    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractAction implements ServerInstance.StateListener {
    
        private static final String ICON = 
                "org/netbeans/modules/j2ee/deployment/impl/ui/resources/stop.png"; // NOI18N
        private static final String PROP_ENABLED = "enabled"; // NOI18N
        private Node node;
        
        public OutputAction(Node node) {
            super(NbBundle.getMessage(StopAction.class, "LBL_StopOutput"),
                  new ImageIcon(Utilities.loadImage(ICON)));
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(StopAction.class, "LBL_StopOutputDesc"));
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
