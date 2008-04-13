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

import java.util.concurrent.CopyOnWriteArrayList;
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
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;

/**
 * Progress object that monitors events from GlassFish Common and translates
 * them into JSR-88 equivalents.
 *
 * @author Peter Williams
 */
public class MonitorProgressObject implements ProgressObject, OperationStateListener {

    private final Hk2DeploymentManager dm;
    private final TargetModuleID moduleId;
    private final CommandType commandType;

    public MonitorProgressObject(Hk2DeploymentManager dm, TargetModuleID moduleId) {
        this(dm, moduleId, CommandType.DISTRIBUTE);
    }
    
    public MonitorProgressObject(Hk2DeploymentManager dm, TargetModuleID moduleId, CommandType commandType) {
        this.dm = dm;
        this.moduleId = moduleId;
        this.commandType = commandType;
        this.operationStatus = new Hk2DeploymentStatus(commandType, 
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
        fireHandleProgressEvent(new Hk2DeploymentStatus(commandType, 
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
