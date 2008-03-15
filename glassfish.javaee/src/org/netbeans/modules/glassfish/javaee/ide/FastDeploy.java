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

package org.netbeans.modules.glassfish.javaee.ide;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.javaee.Hk2DeploymentManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.api.AppChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;


/**
 *
 * @author Ludovic Champenois
 * @author Peter Williams
 */
public class FastDeploy extends IncrementalDeployment {
    
    private Hk2DeploymentManager dm;
    
    /** 
     * Creates a new instance of FastDeploy 
     * 
     * @param dm The deployment manager for the server instance this object
     *   deploys to.
     */
    public FastDeploy(Hk2DeploymentManager dm) {
        this.dm = dm;
    }
    
    /**
     * 
     * @param target 
     * @param app 
     * @param configuration 
     * @param file 
     * @return 
     */
    public ProgressObject initialDeploy(Target target, J2eeModule app, ModuleConfiguration configuration, File dir) {
        // !PW FIXME Hack from old V3 plugin for name field.  What is the correct way?
        String moduleName = dir.getParentFile().getParentFile().getName();
        Hk2TargetModuleID moduleId = new Hk2TargetModuleID(target, moduleName, 
                moduleName, dir.getAbsolutePath());
        DeployProgressObject progressObject = new DeployProgressObject(moduleId);

        GlassfishModule commonSupport = dm.getCommonServerSupport();
        commonSupport.deploy(progressObject, dir, moduleName);

        return progressObject;
    }
    
    public ProgressObject initialDeploy(Target target,  File dir, String moduleName) {
        // !PW FIXME Hack from old V3 plugin for name field.  What is the correct way?
        Hk2TargetModuleID moduleId = new Hk2TargetModuleID(target, moduleName, 
                moduleName, dir.getAbsolutePath());
        DeployProgressObject progressObject = new DeployProgressObject(moduleId);
        GlassfishModule commonSupport = dm.getCommonServerSupport();
        commonSupport.deploy(progressObject, dir, moduleName);
        return progressObject;
    }
    
    /**
     * 
     * @param targetModuleID 
     * @param appChangeDescriptor 
     * @return 
     */
    public ProgressObject incrementalDeploy(TargetModuleID targetModuleID, AppChangeDescriptor appChangeDescriptor) {
        Hk2TargetModuleID hk2tid = (Hk2TargetModuleID) targetModuleID;
        DeployProgressObject progressObject = new DeployProgressObject(targetModuleID);
        GlassfishModule commonSupport = dm.getCommonServerSupport();
        commonSupport.deploy(progressObject, new File(hk2tid.getLocation()), hk2tid.getModuleID());
        return progressObject;
    }
    
    /**
     * 
     * @param target 
     * @param deployable 
     * @return 
     */
    public boolean canFileDeploy(Target target, J2eeModule deployable) {
        if (null == target){
            return false;
        }
        if (null == deployable){
            return false;
        }
        
        if (deployable.getModuleType() == ModuleType.EAR ||
                deployable.getModuleType() == ModuleType.EJB){
            return false;
        }
        // return dm.isLocal();
//        System.out.println("canFileDeploy");
        return true;
        
    }
    
    /**
     * @return Absolute path root directory for the specified app or null if
     *   server can accept the deployment from an arbitrary directory.
     */
    public File getDirectoryForNewApplication(Target target, J2eeModule app, ModuleConfiguration configuration) {
//        System.out.println("getDirectoryForNewApplication");
        return null;
    }
    
    /**
     * 
     * @param file 
     * @param string 
     * @param app 
     * @param configuration 
     * @return 
     */
    public File getDirectoryForNewModule(File file, String string, J2eeModule app, ModuleConfiguration configuration) {
        return null;
    }
    
    /**
     * 
     * @param targetModuleID 
     * @return 
     */
    public File getDirectoryForModule(TargetModuleID targetModuleID) {
        return null;
    }

    /**
     * Progress object that monitors events from GlassFish Common and translates
     * them into JSR-88 equivalents.
     */
    private class DeployProgressObject implements ProgressObject, OperationStateListener {
       
        private final TargetModuleID moduleId;
        
        public DeployProgressObject(TargetModuleID moduleId) {
            this.moduleId = moduleId;
            this.operationStatus = new Hk2DeploymentStatus(CommandType.DISTRIBUTE, 
                    StateType.RUNNING, ActionType.EXECUTE, "Initializing...");
        }
        
        public DeploymentStatus getDeploymentStatus() {
            return operationStatus;
        }

        public TargetModuleID [] getResultTargetModuleIDs() {
            return new TargetModuleID [] { moduleId };
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID moduleId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isCancelSupported() {
            return false;
        }

        public void cancel() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("GFV3: Cancel not supported yet.");
        }

        public boolean isStopSupported() {
            return false;
        }

        public void stop() throws OperationUnsupportedException {
            throw new OperationUnsupportedException("GFV3: Stop not supported yet.");
        }

        /**
         * OperationState listener - translates state events from common instance
         * manager to JSR-88 compatible type.
         * 
         * @param newState Current state of operation
         * @param message Informational message about latest state change
         */
        public void operationStateChanged(OperationState newState, String message) {
            fireHandleProgressEvent(new Hk2DeploymentStatus(CommandType.DISTRIBUTE, 
                    translateState(newState), ActionType.EXECUTE, message));
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
            operationStatus = status;
            ProgressEvent event = new ProgressEvent(dm, moduleId, status);
            for(ProgressListener target: listeners) {
                target.handleProgressEvent(event);
            }
        }

    }
    
    /**
     * ProgressObject implementation that is in a permanent completed state.
     * For returning from methods that must return a ProgressObject, but do not
     * need to implement any asynchronous functionality.
     */
    public static class DummyProgressObject implements ProgressObject {

        private final TargetModuleID [] moduleIDs;
        private final DeploymentStatus status = new Hk2DeploymentStatus(CommandType.DISTRIBUTE,
                StateType.COMPLETED, ActionType.EXECUTE, "");

        public DummyProgressObject(final TargetModuleID moduleID) {
            moduleIDs = new TargetModuleID [] { moduleID };
        }
        
        public DeploymentStatus getDeploymentStatus() {
            return status;
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            return moduleIDs;
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isCancelSupported() {
            return true;
        }

        public void cancel() throws OperationUnsupportedException {
        }

        public boolean isStopSupported() {
            return true;
        }

        public void stop() throws OperationUnsupportedException {
        }

        public void addProgressListener(ProgressListener listener) {
        }

        public void removeProgressListener(ProgressListener listener) {
        }
        
    }
    
}
