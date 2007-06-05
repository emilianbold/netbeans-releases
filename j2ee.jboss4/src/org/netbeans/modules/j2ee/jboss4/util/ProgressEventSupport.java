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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.jboss4.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;

/** 
 * Progress event support
 * 
 * @author sherold
 */
public final class ProgressEventSupport {

    private final Object eventSource;
    private final List<ProgressListener> listeners = new CopyOnWriteArrayList<ProgressListener>();
    private DeploymentStatus status;
    private TargetModuleID targetModuleID;
    
    
    public ProgressEventSupport(Object eventSource) {
        if (eventSource == null) {
            throw new NullPointerException();
        }
        this.eventSource = eventSource;
    }
    
    public void addProgressListener(ProgressListener progressListener) {
        listeners.add(progressListener);
    }
    
    public void removeProgressListener(ProgressListener progressListener) {
        listeners.remove(progressListener);
    }

    public void fireProgressEvent(TargetModuleID targetModuleID, DeploymentStatus status) {
	synchronized (this) {
            this.status = status;
            this.targetModuleID = targetModuleID;
	}
	ProgressEvent evt = new ProgressEvent(eventSource, targetModuleID, status);
        for (ProgressListener listener : listeners) {
            listener.handleProgressEvent(evt);
        }
    }
    
    public synchronized DeploymentStatus getDeploymentStatus() {
        return status;
    }
}
