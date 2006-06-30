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
