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

package org.netbeans.modules.glassfish.javaee;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.javaee.ide.Hk2DeploymentStatus;
import org.netbeans.modules.glassfish.javaee.ide.Hk2PluginProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Ludovic Champenois
 * @auther Peter Williams
 */
public class Hk2StartServer extends StartServer implements ProgressObject {
   
    public static enum MODE { RUN, DEBUG };
    
    private MODE mode;
    private DeploymentStatus deploymentStatus;
    private Hk2DeploymentManager dm;
    private String serverName;
    private List<ProgressListener> listeners = 
            new CopyOnWriteArrayList<ProgressListener>();
    private InstanceProperties ip;
    private static Map<String, Object> isDebugModeUri = 
            Collections.synchronizedMap(new HashMap<String, Object>(2,1));
    private String url;
    
    public Hk2StartServer(DeploymentManager jdm) {
        if (!(jdm instanceof Hk2DeploymentManager)) {
            throw new IllegalArgumentException("Only GlassFish V3 is supported"); //NOI18N
        }
        this.dm = (Hk2DeploymentManager) jdm;
        this.ip = dm.getProperties().getInstanceProperties();
        this.serverName = ip.getProperty(GlassfishModule.DISPLAY_NAME_ATTR);
        this.url = ip.getProperty(InstanceProperties.URL_ATTR);
    }
    
    @Override
    public boolean supportsStartDebugging(Target target) {
        return true;
    }
    
    public InstanceProperties getInstanceProperties() {
        return ip;
    }
    
    private GlassfishModule getCommonServerSupport() {
        ServerInstance si = dm.getServerInstance();
        return si.getBasicNode().getLookup().lookup(GlassfishModule.class);
    }
    
    // start server
    public ProgressObject startDeploymentManager() {
        mode = MODE.RUN;
        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                CommandType.START, StateType.RUNNING, ActionType.EXECUTE, 
                NbBundle.getMessage(Hk2StartServer.class, "MSG_START_SERVER_IN_PROGRESS", serverName)
                )); // NOI18N
        GlassfishModule glassfishSupport = getCommonServerSupport();
        if(glassfishSupport != null) {
            glassfishSupport.startServer(new OperationStateListener() {
                public void operationStateChanged(OperationState newState, String message) {
                    fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                            CommandType.START, translateState(newState), ActionType.EXECUTE, 
                            message));
                }
            });
        }
        removeDebugModeUri();

//        // Temporary for debugging
//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                fireHandleProgressEvent(null, new Hk2DeploymentStatus(
//                        ActionType.EXECUTE, CommandType.START, StateType.COMPLETED,
//                        "Debugging server: " + serverName
//                        )); // NOI18N
//            }
//        });
        return this;
    }
    
    public ProgressObject stopDeploymentManager() {
        fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                CommandType.START, StateType.RUNNING, ActionType.EXECUTE, 
                NbBundle.getMessage(Hk2StartServer.class, "MSG_STOP_SERVER_IN_PROGRESS", serverName)
                )); // NOI18N
        GlassfishModule glassfishSupport = getCommonServerSupport();
        if(glassfishSupport != null) {
            glassfishSupport.stopServer(new OperationStateListener() {
                public void operationStateChanged(OperationState newState, String message) {
                    fireHandleProgressEvent(null, new Hk2DeploymentStatus(
                            CommandType.STOP, translateState(newState), ActionType.EXECUTE, 
                            message));
                }
            });
        }
        removeDebugModeUri();
        return this;
    }
    
    private static StateType translateState(OperationState commonState) {
        switch(commonState) {
            case RUNNING:
                return StateType.RUNNING;
            case COMPLETED:
                return StateType.COMPLETED;
            case FAILED:
                return StateType.FAILED;
        }
        // Should never happen, but we have to return something.  UNKNOWN state
        // would be convenient, but again, this should never happen.
        return StateType.FAILED;
    }
    
    public ProgressObject startDebugging(Target target) {
        mode = MODE.DEBUG;
//        RequestProcessor.getDefault().post(new Hk2StartRunnable(dm, this), 0, Thread.NORM_PRIORITY);
        addDebugModeUri();
        return this;
    }
    
    private void addDebugModeUri() {
        isDebugModeUri.put(url, new Object());
    }
    
    private void removeDebugModeUri() {
        isDebugModeUri.remove(url);
    }
    
    private boolean existsDebugModeUri() {
        return isDebugModeUri.containsKey(url);
    }
    
    public boolean isDebuggable(Target target) {
        if (!existsDebugModeUri()) {
            return false;
        }
        if (!isRunning()) {
            return false;
        }
        return true;
    }
    
    public boolean isAlsoTargetServer(Target target) {
        return true;
    }
    
    public ServerDebugInfo getDebugInfo(Target target) {
        return new ServerDebugInfo(ip.getProperty(GlassfishModule.HOSTNAME_ATTR), dm.getProperties().getDebugPort());
    }
    
    public boolean supportsStartDeploymentManager() {
        return true;
    }
    
    public boolean needsStartForTargetList() {
        return false;
    }
    
    public boolean needsStartForConfigure() {
        return false;
    }
    
    public boolean needsStartForAdminConfig() {
        return false;
    }
    
    public boolean isRunning() {
        return Hk2PluginProperties.isRunning(ip.getProperty(GlassfishModule.HOSTNAME_ATTR),
                ip.getProperty(InstanceProperties.HTTP_PORT_NUMBER));
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        return null;
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    
    public boolean isCancelSupported() {
        return false;
    }
    
    public void cancel() throws OperationUnsupportedException {
    }
    
    public boolean isStopSupported() {
        return true;
    }
    
    public void stop() throws OperationUnsupportedException {
    }
    
    public void addProgressListener(ProgressListener progressListener) {
        listeners.add(progressListener);
    }
    
    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }
    
    public void fireHandleProgressEvent(TargetModuleID targetModuleID, DeploymentStatus deploymentStatus) {
        ProgressEvent evt = new ProgressEvent(this, targetModuleID, deploymentStatus);
        this.deploymentStatus = deploymentStatus;
        
        Iterator<ProgressListener> iter = listeners.iterator();
        while(iter.hasNext()) {
            iter.next().handleProgressEvent(evt);
        }
    }
    
    public MODE getMode() {
        return mode;
    }
}
