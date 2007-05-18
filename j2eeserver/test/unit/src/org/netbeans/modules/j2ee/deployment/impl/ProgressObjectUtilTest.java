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

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.impl.ui.ProgressUI;
import org.openide.util.RequestProcessor;

/**
 *
 * @author sherold
 */
public class ProgressObjectUtilTest extends NbTestCase {
    
    /** Creates a new instance of ProgressObjectUtilTest */
    public ProgressObjectUtilTest(String testName) {
        super(testName);
    }
    
    public void testTrackProgressObject() throws Exception {
        ProgressUI ui = new ProgressUI("alreadyFinishedSuccessfully", false);
        ui.start();
        ProgressObject po = alreadyFinishedSuccessfully();
        assertTrue(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
        
        ui = new ProgressUI("alreadyFinishedWithFailure", false);
        ui.start();
        po = alreadyFinishedWithFailure();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
        
        ui = new ProgressUI("willNeverFinish", false);
        ui.start();
        po = willNeverFinish();
        try {
            assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 1000));
            fail("the task should time out");
        } catch (TimedOutException e) {
            // exception should be thrown
        }
        
        ui = new ProgressUI("willFinishSuccessfully", false);
        ui.start();
        po = willFinishSuccessfully();
        assertTrue(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
        
        ui = new ProgressUI("willFail", false);
        ui.start();
        po = willFail();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
        
        ui = new ProgressUI("willReleaseAndFail", false);
        ui.start();
        po = willReleaseAndFail();
        assertFalse(ProgressObjectUtil.trackProgressObject(ui, po, 5000));
    }
    
    private ProgressObject neverEndingTask() {
        return null;
    }
    
    private ProgressObject alreadyFinishedSuccessfully() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "finished", StateType.COMPLETED));
        return po;
    }
    
    private ProgressObject alreadyFinishedWithFailure() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
        return po;
    }
    
    private ProgressObject willNeverFinish() {
        ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        return po;
    }
    
    private ProgressObject willFinishSuccessfully() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "finished", StateType.COMPLETED));
            }
        }, 1000);
        return po;
    }
    
    private ProgressObject willFail() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
            }
        }, 1000);
        return po;
    }
    
    private ProgressObject willReleaseAndFail() {
        final ProgressObjectImpl po = new ProgressObjectImpl();
        po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "running", StateType.RUNNING));
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "released", StateType.RELEASED));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                po.getProgressEventSupport().fireProgressEvent(null, new DeploymentStatusImpl(CommandType.START, "failed", StateType.FAILED));
            }
        }, 1000);
        return po;
    }
    
    private static class ProgressObjectImpl implements ProgressObject {
        
        private final ProgressEventSupport progressEventSupport = new ProgressEventSupport(this);
        
        public ProgressEventSupport getProgressEventSupport() {
            return progressEventSupport;
        }       

        public DeploymentStatus getDeploymentStatus() {
            return progressEventSupport.getDeploymentStatus();
        }

        public TargetModuleID[] getResultTargetModuleIDs() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public ClientConfiguration getClientConfiguration(TargetModuleID arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isCancelSupported() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void cancel() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public boolean isStopSupported() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void stop() throws OperationUnsupportedException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void addProgressListener(ProgressListener arg0) {
            progressEventSupport.addProgressListener(arg0);
        }

        public void removeProgressListener(ProgressListener arg0) {
            progressEventSupport.removeProgressListener(arg0);
        }
    }
}
