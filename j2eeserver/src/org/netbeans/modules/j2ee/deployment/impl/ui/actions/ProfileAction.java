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

import org.netbeans.modules.j2ee.deployment.impl.ServerException;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Profile server action starts the server in the profile mode.
 *
 * @author sherold
 */
public class ProfileAction extends NodeAction {
    
    public String getName() {
        return NbBundle.getMessage(DebugAction.class, "LBL_Profile");
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
        if (nodes.length != 1) {
            return;
        }
        final ServerInstance si = (ServerInstance)nodes[0].getCookie(ServerInstance.class);
        if (si != null) {
            Profiler profiler = ServerRegistry.getProfiler();
            if (profiler == null) {
                return;
            }
            final ProfilerServerSettings settings = profiler.getSettings(si.getUrl());
            if (settings == null) {
                return;
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    String title = NbBundle.getMessage(DebugAction.class, "LBL_Profiling", si.getDisplayName());
                    ProgressUI progressUI = new ProgressUI(title, false);
                    try {
                        progressUI.start();
                        si.startProfile(settings, false, progressUI);
                    } catch (ServerException ex) {
                        String msg = ex.getLocalizedMessage();
                        NotifyDescriptor desc = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } finally {
                        progressUI.finish();
                    }
                }
            });
        }
    }
    
    private static boolean enableImpl(Node[] nodes) {
        if (nodes.length != 1) {
            return false;
        }
        ServerInstance si = (ServerInstance)nodes[0].getCookie(ServerInstance.class);
        if (si == null || si.getServerState() != ServerInstance.STATE_STOPPED 
            || !si.isProfileSupported()) {
            return false;
        }
        return true;
    }
}
