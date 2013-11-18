/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.cloud.javaee;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.*;
import org.glassfish.tools.ide.TaskEvent;
import org.glassfish.tools.ide.TaskState;
import org.glassfish.tools.ide.TaskStateListener;

/**
 * Adapter between TaskStateListener and ProgressObject interface.
 * Receives events from command runner about execution of command
 * and notifies registered listeners with new <code>DeploymentStatusImpl</code>.
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class ProgressObjectDeploy implements ProgressObject, TaskStateListener {

    /* Actual status of command. */
    private DeploymentStatus status;
    /* Registered listeners. */
    private LinkedList<ProgressListener> listeners;
    /* DeploymentManager that is executing the deployment.*/
    private DeploymentManager dm;
    /* ID of module which is the subject of deploy command.*/
    private TargetModuleID moduleID; 
    

    public ProgressObjectDeploy(DeploymentManager dm, TargetModuleID moduleID) {
        this.dm = dm;
        this.moduleID = moduleID;
        this.status = new DeploymentStatusImpl(CommandType.DISTRIBUTE, 
                StateType.RUNNING, ActionType.EXECUTE, "Initializing...");
        listeners = new LinkedList<ProgressListener>();
    }
    
    
    @Override
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }

    @Override
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[] {moduleID};
    }

    @Override
    public ClientConfiguration getClientConfiguration(TargetModuleID tmid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isCancelSupported() {
        return false;
    }

    @Override
    public void cancel() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isStopSupported() {
        return false;
    }

    @Override
    public void stop() throws OperationUnsupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addProgressListener(ProgressListener pl) {
        listeners.add(pl);
    }

    @Override
    public void removeProgressListener(ProgressListener pl) {
        listeners.remove(pl);
    }

    @Override
    public void operationStateChanged(TaskState ts, TaskEvent te, String... strings) {
        String message = strings != null && strings.length > 0 ? strings[0] : "";
        Logger.getLogger("glassfish-cloud").log(Level.FINE, message);
        // Suppress message except in cases of failure.  Returning an empty
        // string prevents status from being displayed in build output window.
        String relayedMessage = TaskState.FAILED.equals(ts) ? message : "";
        status = new DeploymentStatusImpl(CommandType.DISTRIBUTE,
                translateState(ts), ActionType.EXECUTE, relayedMessage);
        notifyListeners();
//        notifyListeners(new Hk2DeploymentStatus(CommandType.DISTRIBUTE,
//                translateState(ts), ActionType.EXECUTE, relayedMessage));
    }
    
    private StateType translateState(TaskState commonState) {
        switch (commonState) {
            case COMPLETED: return StateType.COMPLETED;
            case RUNNING: return StateType.RUNNING;
            case READY: return StateType.RUNNING;
            case FAILED: return StateType.FAILED;
            default: throw new UnsupportedOperationException("Unknown task state!");
        }
    }
    
    private void notifyListeners() {
        ProgressEvent event = new ProgressEvent(dm, null, status);
        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(event);
        }
    }
    
}
