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

package org.netbeans.modules.j2ee.deployment.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;

/**
 *
 * @author sherold
 */
public final class ProgressObjectUtil {
    
    private static final Logger LOGGER = Logger.getLogger(ProgressObjectUtil.class.getName());
    
        /**
     * Waits till the progress object is in final state or till the timeout runs out.
     *
     * @param ui progress ui which will be notified about progress object changes .
     * @param po progress object which will be tracked.
     * @param timeout timeout in millis
     *
     * @return true if the progress object completed successfully, false otherwise.
     *         This is a workaround for issue 82428.
     */
    public static boolean trackProgressObject(ProgressUI ui, final ProgressObject po, long timeout) {
        final AtomicBoolean completed = new AtomicBoolean();
        ui.setProgressObject(po);
        try {
            final CountDownLatch progressFinished = new CountDownLatch(1);
            ProgressListener listener = new ProgressListener() {
                public void handleProgressEvent(ProgressEvent progressEvent) {
                    DeploymentStatus status = progressEvent.getDeploymentStatus();
                    if (status.isCompleted()) {
                        completed.set(true);
                    }
                    if (status.isCompleted() || status.isFailed()) {
                        progressFinished.countDown();
                    }
                }
            };
            po.addProgressListener(listener);
            try {
                // the completion event might have arrived before the progress listener 
                // was registered, wait only if not yet finished
                DeploymentStatus status = po.getDeploymentStatus();
                if (!status.isCompleted() && !status.isFailed()) {
                    try {
                        progressFinished.await(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.INFO, null, e);
                    }
                } else if (status.isCompleted()) {
                    completed.set(true);
                }
            } finally {
                po.removeProgressListener(listener);
            }
        } finally {
            ui.setProgressObject(null);
        }
        return completed.get();
    }
    
}
