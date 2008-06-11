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


package org.netbeans.modules.glassfish.javaee.progress;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.glassfish.javaee.ide.Hk2DeploymentStatus;

/**
 * MultiProgressObjectWrapper wraps multiple progress objects into a single one.
 * 
 * @author herolds
 */
public class MultiProgressObjectWrapper implements ProgressObject, ProgressListener {
    
    /** Support for progress notifications. */
    private ProgressEventSupport pes;
    
    private ProgressObject[] progObjs;
    
    private String message = ""; // NOI18N
    
    private int completedCounter;
    
    private StateType state = StateType.RUNNING;
    
    /** Creates a new instance of MultipleOpsProgressObject */
    public MultiProgressObjectWrapper(ProgressObject[] progObjs) {
        if (progObjs == null) {
            throw new NullPointerException("The progObjs argument must not be null."); // NOI18N
        }
        if (progObjs.length == 0) {
            throw new IllegalArgumentException("At least one progress object must be passed."); // NOI18N
        }
        pes = new ProgressEventSupport(this);
        this.progObjs = progObjs;
        for(int i = 0; i < progObjs.length; i++) {
            ProgressObject po = progObjs[i];
            po.addProgressListener(this);
        }
    }
    
    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public DeploymentStatus getDeploymentStatus() {
        DeploymentStatus ds = progObjs[0].getDeploymentStatus();
        // all deployment objects are supposed to be of the same action and command type
        return new Hk2DeploymentStatus(ds.getCommand(), state, ds.getAction(), message);
    }
    
    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs() {
        List<TargetModuleID> returnVal = new ArrayList<TargetModuleID>();
        for (int i = 0; i < progObjs.length; i++) {
            ProgressObject po = progObjs[i];
            if (po.getDeploymentStatus().isCompleted()) {
                returnVal.add(po.getResultTargetModuleIDs()[0]);
            }
        }
        return (TargetModuleID[])returnVal.toArray(new TargetModuleID[returnVal.size()]);
    }
    
    /** JSR88 method. */
    public boolean isCancelSupported() {
        return false;
    }
    
    /** JSR88 method. */
    public void cancel() 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException("cancel not supported in deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public boolean isStopSupported() {
        return false;
    }
    
    /** JSR88 method. */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported in deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public void addProgressListener(ProgressListener l) {
        pes.addProgressListener(l);
    }
    
    /** JSR88 method. */
    public void removeProgressListener(ProgressListener l) {
        pes.removeProgressListener(l);
    }

    public void handleProgressEvent(ProgressEvent progressEvent) {
        message = progressEvent.getDeploymentStatus().getMessage();
        StateType stateType = progressEvent.getDeploymentStatus().getState();
        if (stateType == StateType.FAILED) {
            state = StateType.FAILED;
        } else if (stateType == StateType.RELEASED) {
            state = StateType.RELEASED;
        } else if (stateType == StateType.COMPLETED) {
            if (++completedCounter == progObjs.length) {
                state = StateType.COMPLETED;
            }
        }
        pes.fireHandleProgressEvent(progressEvent.getTargetModuleID(), progressEvent.getDeploymentStatus());
    }
}
