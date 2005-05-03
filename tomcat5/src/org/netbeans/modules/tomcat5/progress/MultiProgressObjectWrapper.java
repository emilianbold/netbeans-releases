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


package org.netbeans.modules.tomcat5.progress;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

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
        return new Status(ds.getAction(), ds.getCommand(), message, state);
    }
    
    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs() {
        List returnVal = new ArrayList();
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
        throw new OperationUnsupportedException("cancel not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public boolean isStopSupported() {
        return false;
    }
    
    /** JSR88 method. */
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("stop not supported in Tomcat deployment"); // NOI18N
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
