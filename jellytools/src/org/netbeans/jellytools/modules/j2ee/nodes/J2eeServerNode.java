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

package org.netbeans.jellytools.modules.j2ee.nodes;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ExploreFromHereAction;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.modules.j2ee.actions.CustomizerAction;
import org.netbeans.jellytools.modules.j2ee.actions.DebugAction;
import org.netbeans.jellytools.modules.j2ee.actions.RefreshAction;
import org.netbeans.jellytools.modules.j2ee.actions.RemoveInstanceAction;
import org.netbeans.jellytools.modules.j2ee.actions.RestartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StopAction;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;

/** Node representing a J2EE Server node under Servers node.
 * @author Martin.Schovanek@sun.com
 */
public class J2eeServerNode extends Node {
    
    static final CustomizerAction customizerAction = new CustomizerAction();
    static final DebugAction debugAction = new DebugAction();
    static final RefreshAction refreshAction = new RefreshAction();
    static final RemoveInstanceAction removeInstanceAction =
            new RemoveInstanceAction();
    static final RestartAction restartAction = new RestartAction();
    static final StartAction startAction = new StartAction();
    static final StopAction stopAction = new StopAction();
    private static final String SERVERS = Bundle.getString(
            "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
            "SERVER_REGISTRY_NODE");
    
    /** Finds Source Packages node under project with given name
     * @param projectName display name of project
     */
    public J2eeServerNode(String serverName) {
        super(new RuntimeTabOperator().getRootNode(), SERVERS+"|"+serverName);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            customizerAction,
            debugAction,
            refreshAction,
            removeInstanceAction,
            restartAction,
            startAction,
            stopAction
        });
    }
    
    /** performs 'Properties' with this node */
    public void properties() {
        customizerAction.perform(this);
    }
    
    /** performs 'Start in Debug Mode' with this node */
    public void debug() {
        debugAction.perform(this);
        waitDebugging();
    }
    
    /** performs 'Refresh' with this node */
    public void refresh() {
        refreshAction.perform(this);
        waitNotWaiting();
    }
    
    /** performs 'Remove' with this node */
    public void remove() {
        removeInstanceAction.perform(this);
    }
    
    /** performs 'Restart' with this node */
    public void restart() {
        restartAction.perform(this);
    }
    
    /** performs 'Start' with this node */
    public void start() {
        startAction.perform(this);
        waitRunning();
    }
    
    /** performs 'Stop' with this node */
    public void stop() {
        stopAction.perform(this);
        waitStopped();
    }
    
    //// PRIVATE METHODS ////
    
    private void waitDebugging() {
        waitServerState(ServerInstance.STATE_DEBUGGING);
    }
    
    private void waitRunning() {
        waitServerState(ServerInstance.STATE_RUNNING);
    }
    
    private void waitStopped() {
        waitServerState(ServerInstance.STATE_STOPPED);
    }

    private void waitServerState(int state) {
        org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        final ServerInstance server = (ServerInstance) ideNode.getCookie(
                ServerInstance.class);
        final int targetState = state;
        waitFor(new Waitable() {
            public Object actionProduced(Object obj) {
                if (server.getServerState() == targetState) {
                    return "Server state: "+getStateName()+" reached.";
                }
                return null;
            }
            public String getDescription() {
                return "Wait for server state: "+getStateName();
            }
            private String getStateName() {
                switch (targetState) {
                    case ServerInstance.STATE_DEBUGGING:
                        return "DEBUGGING";
                    case ServerInstance.STATE_RUNNING:
                        return "RUNNING";
                    case ServerInstance.STATE_STOPPED:
                        return "STOPPED";
                    case ServerInstance.STATE_SUSPENDED:
                        return "SUSPENDED";
                    case ServerInstance.STATE_WAITING:
                        return "WAITING";
                    default:
                        return "UNKNOWN STATE";
                }
            }
        });
    }
    
    private void waitNotWaiting() {
        org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        final ServerInstance server = (ServerInstance) ideNode.getCookie(
                ServerInstance.class);
        waitFor(new Waitable() {
            public Object actionProduced(Object obj) {
                if (server.getServerState() != ServerInstance.STATE_WAITING) {
                    return "Server leaves WAITING state.";
                }
                return null;
            }
            public String getDescription() {
                return "Wait till server leaves state WAITING.";
            }
        });
    }
    
    private static Object waitFor(Waitable action) {
        Waiter waiter = new Waiter(action);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription()+" has been " +
                    "interrupted.", ex);
        }
    }
}
