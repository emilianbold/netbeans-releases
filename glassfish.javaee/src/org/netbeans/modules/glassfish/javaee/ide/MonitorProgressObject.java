/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.openide.filesystems.FileUtil;

/**
 * Progress object that monitors events from GlassFish Common and translates
 * them into JSR-88 equivalents.
 *
 * @author Peter Williams
 */
public class MonitorProgressObject implements ProgressObject, OperationStateListener {

    private final Hk2DeploymentManager dm;
    private final Hk2TargetModuleID moduleId;
    private final CommandType commandType;

    public MonitorProgressObject(Hk2DeploymentManager dm, Hk2TargetModuleID moduleId) {
        this(dm, moduleId, CommandType.DISTRIBUTE);
    }
    
    public MonitorProgressObject(Hk2DeploymentManager dm, Hk2TargetModuleID moduleId, CommandType commandType) {
        this.dm = dm;
        this.moduleId = moduleId;
        this.commandType = commandType;
        this.operationStatus = new Hk2DeploymentStatus(commandType, 
                StateType.RUNNING, ActionType.EXECUTE, "Initializing...");
    }

    public DeploymentStatus getDeploymentStatus() {
        return operationStatus;
    }

    public TargetModuleID[] getResultTargetModuleIDs() {
        if (null == moduleId) {
            return computeResultTMID();
        } else {
            synchronized (moduleId) {
                return computeResultTMID();
            }
        }
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID moduleId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isCancelSupported() {
        return false;
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Cancel not supported yet.");
    }

    public boolean isStopSupported() {
        return false;
    }

    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Stop not supported yet.");
    }

    /**
     * OperationState listener - translates state events from common instance
     * manager to JSR-88 compatible type.
     * 
     * @param newState Current state of operation
     * @param message Informational message about latest state change
     */
    public void operationStateChanged(OperationState newState, String message) {
        Logger.getLogger("glassfish-javaee").log(Level.FINE, message);
        // Suppress message except in cases of failure.  Returning an empty
        // string prevents status from being displayed in build output window.
        String relayedMessage = newState == OperationState.FAILED ? message : "";
        fireHandleProgressEvent(new Hk2DeploymentStatus(commandType,
                translateState(newState), ActionType.EXECUTE, relayedMessage));
    }

    private TargetModuleID[] computeResultTMID() {
        TargetModuleID[] retVal = new TargetModuleID[]{moduleId};
         try {
            retVal = createModuleIdTree(moduleId);
         } catch (InterruptedException ex) {
             Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ex);
         } catch (ExecutionException ex) {
             Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ex);
         } catch (TimeoutException ex) {
             Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ex);
         }
         return retVal;
    }

    private void loopThroughListeners(DeploymentStatus status) {
        operationStatus = status;
        ProgressEvent event = new ProgressEvent(dm, moduleId, status);
        for (ProgressListener target : listeners) {
            target.handleProgressEvent(event);
        }
    }

    private StateType translateState(OperationState commonState) {
        if(commonState == OperationState.RUNNING) {
            return StateType.RUNNING;
        } else if(commonState == OperationState.COMPLETED) {
            return StateType.COMPLETED;
        } else {
            return StateType.FAILED;
        }
    }

    // ProgressEvent/Listener support

    private volatile DeploymentStatus operationStatus;
    private CopyOnWriteArrayList<ProgressListener> listeners = 
            new CopyOnWriteArrayList<ProgressListener>();

    public void addProgressListener(ProgressListener listener) {
        listeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        listeners.remove(listener);
    }  

    public void fireHandleProgressEvent(DeploymentStatus status) {
        if (null == moduleId) {
            loopThroughListeners(status);
        } else {
            synchronized(moduleId) {
                loopThroughListeners(status);
            }
        }
    }

    static final private String[] TYPES = {"web", "ejb"};

    private TargetModuleID[] createModuleIdTree(Hk2TargetModuleID moduleId) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (moduleId) {
            // this should only get called in the ear deploy case...
            Hk2TargetModuleID root = Hk2TargetModuleID.get((Hk2Target) moduleId.getTarget(),
                    moduleId.getModuleID(), null, moduleId.getLocation(), true);
            // build the tree of submodule
            GetPropertyCommand gpc = new GetPropertyCommand("*." + moduleId.getModuleID() + ".*");
            Future<OperationState> result =
                    dm.getCommonServerSupport().execute(gpc);
            if (result.get(60, TimeUnit.SECONDS) == OperationState.COMPLETED) {
                Map<String, String> data = gpc.getData();
                for (Entry<String, String> e : data.entrySet()) {
                    String k = e.getKey();
                    int dex1 = k.lastIndexOf(".module."); // NOI18N
                    int dex2 = k.lastIndexOf(".name"); // NOI18N
                    String moduleName = e.getValue();
                    if (dex2 > dex1 && dex1 > 0 && !moduleId.getModuleID().equals(moduleName)) {
                        for (String guess : TYPES) {
                            String type = data.get("applications.application." + moduleId.getModuleID() + ".module." + moduleName + ".engine." + guess + ".sniffer"); // NOI18N
                            if (null != type) {
                                Hk2TargetModuleID kid = Hk2TargetModuleID.get(
                                        (Hk2Target) moduleId.getTarget(), moduleName,
                                        "web".equals(guess) ? determineContextRoot(root,moduleName) : null,
                                        moduleId.getLocation() + File.separator +
                                        FastDeploy.transform(moduleName));
                                root.addChild(kid);
                            }
                        }
                    }
                }
            }

            return new TargetModuleID[]{root};
        }
    }

    private String determineContextRoot(Hk2TargetModuleID root, String moduleName) {
        String retVal = "/" + moduleName;  // incorrect falback
        int dex = moduleName.lastIndexOf('.');
        if (dex > -1) {
            retVal = "/" + moduleName.substring(0, dex);
        }
        // look for the application.xml
        File appxml = new File(root.getLocation(), "META-INF"+File.separator+"application.xml");
        if (appxml.exists()) {
            try {
                // TODO read the entries
                DDProvider ddp = DDProvider.getDefault();
                Application app = ddp.getDDRoot(FileUtil.createData(FileUtil.normalizeFile(appxml)));
                // TODO build a map
                Module[] mods = app.getModule();
                for (Module m : mods) {
                    Web w = m.getWeb();
                    if (null != w && moduleName.equals(w.getWebUri())) {
                        retVal = w.getContextRoot();
                        break;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger("glassfish-javaee").log(Level.INFO, null, ex);
            }
        }
        return retVal;
    }
}
