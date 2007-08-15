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


package org.netbeans.modules.tomcat5.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.Parameters;

/**
 * MultiProgressObjectWrapper wraps multiple progress objects into a single one.
 * <p>
 * If all wrapped objects are in COMPLETED or FAILED state the state object
 * will be changed to:
 * <ul>
 *     <li>{@link StateType.COMPLETED} if all wrapped objects reached the COMPLETED state
 *     <li>{@link StateType.FAILED} if any of wrapped objects reached the FAILED state
 * </ul>
 * <p>
 * Note that all wrapped objects have to be in COMPLETED or FAILED state to
 * invoke the change of the state of this object. However this does not mean
 * the events from the wrapped objects are not propagated to the listeners of
 * this object.
 * <p>
 * The behaviour of {@link StateType.RELEASED} is quite unsure from JSR-88.
 * This implementation does not consider it as end state of the ProgressObject.
 *
 * @author herolds
 * @author Petr Hejl
 */
public class MultiProgressObjectWrapper implements ProgressObject, ProgressListener {

    private final ProgressEventSupport pes = new ProgressEventSupport(this);

    private final ProgressObject[] progressObjects;

    private String message = ""; // NOI18N

    private StateType state = StateType.RUNNING;

    /** Creates a new instance of MultipleOpsProgressObject */
    public MultiProgressObjectWrapper(ProgressObject[] objects) {
        Parameters.notNull("progObjs", state);

        if (objects.length == 0) {
            throw new IllegalArgumentException("At least one progress object must be passed."); // NOI18N
        }

        progressObjects = new ProgressObject[objects.length];
        System.arraycopy(objects, 0, progressObjects, 0, objects.length);

        for (int i = 0; i < objects.length; i++) {
            ProgressObject po = objects[i];
            // XXX unsafe publication
            po.addProgressListener(this);
        }

        updateState(null);
    }

    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING
    }

    /** JSR88 method. */
    public synchronized DeploymentStatus getDeploymentStatus() {
        DeploymentStatus ds = progressObjects[0].getDeploymentStatus();
        // all deployment objects are supposed to be of the same action and command type
        return new Status(ds.getAction(), ds.getCommand(), message, state);
    }

    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs() {
        List<TargetModuleID> returnVal = new ArrayList<TargetModuleID>();
        for (int i = 0; i < progressObjects.length; i++) {
            ProgressObject po = progressObjects[i];
            if (po.getDeploymentStatus().isCompleted()) {
                returnVal.addAll(Arrays.asList(po.getResultTargetModuleIDs()));
            }
        }
        return returnVal.toArray(new TargetModuleID[returnVal.size()]);
    }

    /** JSR88 method. */
    public boolean isCancelSupported() {
        return false;
    }

    /** JSR88 method. */
    public void cancel() throws OperationUnsupportedException {
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

    /**
     * Handles the progress events from wrapped objects.
     */
    public synchronized void handleProgressEvent(ProgressEvent progressEvent) {
        updateState(progressEvent.getDeploymentStatus().getMessage());

        pes.fireHandleProgressEvent(progressEvent.getTargetModuleID(), progressEvent.getDeploymentStatus());
    }

    private synchronized void updateState(String receivedMessage) {
        if (state == StateType.COMPLETED || state == StateType.FAILED) {
            return;
        }

        boolean completed = true;
        boolean failed = false;

        for (ProgressObject progress : progressObjects) {
            DeploymentStatus status = progress.getDeploymentStatus();

            if (status == null || (!status.isCompleted() && !status.isFailed())) {
                completed = false;
                break;
            }

            if (status.isFailed()) {
                failed = true;
            }
        }

        if (completed) {
            state = failed ? StateType.FAILED : StateType.COMPLETED;
            message = receivedMessage == null ? "" : receivedMessage;
        }
    }
}
