/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.jellytools.modules.j2ee.nodes;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.j2ee.actions.StartDebugAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.modules.j2ee.actions.CustomizerAction;
import org.netbeans.jellytools.modules.j2ee.actions.RefreshAction;
import org.netbeans.jellytools.modules.j2ee.actions.RemoveInstanceAction;
import org.netbeans.jellytools.modules.j2ee.actions.RestartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StopAction;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;

/** Node representing a J2EE Server node under Servers node. Default timeout
 * for all actions is 120 seconds.
 * <p>
 * Usage:<br>
 * <pre>
 *      J2eeServerNode server = J2eeServerNode.invoke("GlassFish");
 *      server.start();
 *      ....
 *      server.stop();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class J2eeServerNode extends Node {
    
    static final CustomizerAction customizerAction = new CustomizerAction();
    static final StartDebugAction startDebugAction = new StartDebugAction();
    static final RefreshAction refreshAction = new RefreshAction();
    static final RemoveInstanceAction removeInstanceAction =
            new RemoveInstanceAction();
    static final RestartAction restartAction = new RestartAction();
    static final StartAction startAction = new StartAction();
    static final StopAction stopAction = new StopAction();
    private static final String SERVERS = Bundle.getString(
            "org.netbeans.modules.j2ee.deployment.impl.ui.Bundle",
            "SERVER_REGISTRY_NODE");
    
    /** Creates new instance of J2eeServerNode with given name
     * @param serverName display name of project
     */
    public J2eeServerNode(String serverName) {
        super(new RuntimeTabOperator().getRootNode(), SERVERS+"|"+serverName);
    }
    
    /** Finds J2EE Server node with given name
     * @param serverName display name of project
     */
    public static J2eeServerNode invoke(String serverName) {
        RuntimeTabOperator.invoke();
        return new J2eeServerNode(serverName);
    }
    
    /** tests popup menu items for presence */
    public void verifyPopup() {
        verifyPopup(new Action[]{
            customizerAction,
            startDebugAction,
            refreshAction,
            removeInstanceAction,
            restartAction,
            startAction,
            stopAction
        });
    }
    
    /** performs 'Properties' with this node */
    public void properties() {
        waitNotWaiting();
        customizerAction.perform(this);
    }
    
    /** performs 'Start in Debug Mode' with this node */
    public void debug() {
        waitNotWaiting();
        startDebugAction.perform(this);
        waitDebugging();
    }
    
    /** performs 'Refresh' with this node */
    public void refresh() {
        waitNotWaiting();
        refreshAction.perform(this);
        waitNotWaiting();
    }
    
    /** performs 'Remove' with this node */
    public void remove() {
        waitNotWaiting();
        removeInstanceAction.perform(this);
    }
    
    /** performs 'Restart' with this node */
    public void restart() {
        waitNotWaiting();
        restartAction.perform(this);
    }
    
    /** performs 'Start' with this node */
    public void start() {
        waitNotWaiting();
        startAction.perform(this);
        waitRunning();
    }
    
    /** performs 'Stop' with this node */
    public void stop() {
        waitNotWaiting();
        stopAction.perform(this);
        waitStopped();
    }
    
    /** waits till server finishes current action */
    public void waitFinished() {
        waitNotWaiting();
    }
    
    /** Waits till server is running in debug mode. */
    private void waitDebugging() {
        waitServerState(ServerInstance.STATE_DEBUGGING);
    }
    
    /** Waits till server is running. */
    private void waitRunning() {
        waitServerState(ServerInstance.STATE_RUNNING);
    }
    
    /** Waits till server is stopped. */
    private void waitStopped() {
        waitServerState(ServerInstance.STATE_STOPPED);
    }
    
    //// PRIVATE METHODS ////

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
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription()+" has been " +
                    "interrupted.", ex);
        }
    }
}
