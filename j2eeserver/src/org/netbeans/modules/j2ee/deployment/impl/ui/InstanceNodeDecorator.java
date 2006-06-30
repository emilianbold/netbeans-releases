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

package org.netbeans.modules.j2ee.deployment.impl.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.j2ee.deployment.config.Utils;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.DebugAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.CustomizerAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.ProfileAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RefreshAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RemoveInstanceAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.RestartAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.StartAction;
import org.netbeans.modules.j2ee.deployment.impl.ui.actions.StopAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * This filter node is used to add additional features to the InstanceNode and 
 * InstanceTargetNode. This filter node defines the node name, displaName, 
 * enhances the original node set of actions with the genaral server instance 
 * actions. Registers a server state changes listener, which will display a server 
 * status badge over the original node icon. Everything else is delegated to the 
 * original node.
 *
 * @author sherold
 */
public class InstanceNodeDecorator extends FilterNode 
        implements ServerInstance.StateListener {
    
    private static final String WAITING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/waiting.png"; // NOI18N
    private static final String RUNNING_ICON 
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/running.png"; // NOI18N
    private static final String DEBUGGING_ICON 
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/debugging.png"; // NOI18N
    private static final String SUSPENDED_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/suspended.png"; // NOI18N
    private static final String PROFILING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profiling.png"; // NOI18N
    private static final String PROFILER_BLOCKING_ICON
            = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/profilerblocking.png"; // NOI18N
    
    private ServerInstance si;
    
    /** Creates a new instance of InstanceNodeDecorator */
    public InstanceNodeDecorator(Node original, ServerInstance si) {
        super(original);
        this.si = si;
        si.addStateListener(this);
    }
    
    public String getDisplayName() {
        return si.getDisplayName();
    }
    
    public String getName() {
        return si.getUrl(); // unique identifier
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        actions.addAll(Arrays.asList(new Action[] {
                                        SystemAction.get(StartAction.class),
                                        SystemAction.get(DebugAction.class)
        }));
        if (si.isProfileSupported()) {
            actions.add(
                                        SystemAction.get(ProfileAction.class)
            );
        }
        actions.addAll(Arrays.asList(new Action[] {
                                        SystemAction.get(RestartAction.class),
                                        SystemAction.get(StopAction.class),
                                        SystemAction.get(RefreshAction.class),
                                        null,
                                        SystemAction.get(RemoveInstanceAction.class)
        }));
        actions.addAll(Arrays.asList(getOriginal().getActions(context)));
        actions.add(null);
        actions.add(SystemAction.get(CustomizerAction.class));
        return (Action[])actions.toArray(new Action[actions.size()]);
    }
    
    public Image getIcon(int type) {
        return badgeIcon(getOriginal().getIcon(type));
    }
    
    public Image getOpenedIcon(int type) {
        return badgeIcon(getOriginal().getOpenedIcon(type));
    }
    
    // private helper methods -------------------------------------------------
        
    private Image badgeIcon(Image origImg) {
        Image badge = null;        
        switch (si.getServerState()) {
            case ServerInstance.STATE_WAITING : 
                badge = Utilities.loadImage(WAITING_ICON);
                break;
            case ServerInstance.STATE_RUNNING : 
                badge = Utilities.loadImage(RUNNING_ICON);
                break;
            case ServerInstance.STATE_DEBUGGING : 
                badge = Utilities.loadImage(DEBUGGING_ICON);
                break;
            case ServerInstance.STATE_SUSPENDED : 
                badge = Utilities.loadImage(SUSPENDED_ICON);
                break;
            case ServerInstance.STATE_PROFILING : 
                badge = Utilities.loadImage(PROFILING_ICON);
                break;
            case ServerInstance.STATE_PROFILER_BLOCKING : 
                badge = Utilities.loadImage(PROFILER_BLOCKING_ICON);
                break;
            case ServerInstance.STATE_PROFILER_STARTING : 
                badge = Utilities.loadImage(WAITING_ICON);
                break;
        }
        return badge != null ? Utilities.mergeImages(origImg, badge, 15, 8) : origImg;
    }
    
    // StateListener implementation -------------------------------------------
    
    public void stateChanged(int oldState, int newState) {
        // invoke icon change - this causes the server status icon badge to be updated
        Utils.runInEventDispatchThread(new Runnable() {
            public void run() {
                fireIconChange();
            }
        });
    }
}
