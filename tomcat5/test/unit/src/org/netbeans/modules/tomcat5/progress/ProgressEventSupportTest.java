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

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProgressEventSupportTest extends NbTestCase {
    
    public ProgressEventSupportTest(String testName) {
        super(testName);
    }
    
    public void testListeners() {
        ProgressEventSupport pes = new ProgressEventSupport(this);
        
        ProgressEvent evt1 = new ProgressEvent("fake", new TargetModuleIDImpl("test","test", "test", "http://localhost"),
                new Status(ActionType.EXECUTE, CommandType.START, "test1", StateType.RUNNING));
        ProgressEvent evt2 = new ProgressEvent("fake", new TargetModuleIDImpl("test","test", "test", "http://localhost"),
                new Status(ActionType.CANCEL, CommandType.DISTRIBUTE, "test2", StateType.RUNNING));
        
        ProgressListener listener1 = new ProgressListenerImpl(new ProgressEvent[] {evt1, evt2 }, this);
        ProgressListener listener2 = new ProgressListenerImpl(new ProgressEvent[] {evt1, evt2 }, this);
        
        pes.addProgressListener(listener1);
        pes.addProgressListener(listener2);
        
        pes.fireHandleProgressEvent(evt1.getTargetModuleID(), evt1.getDeploymentStatus());
        pes.fireHandleProgressEvent(evt2.getTargetModuleID(), evt2.getDeploymentStatus());
        
        pes.removeProgressListener(listener1);
        pes.removeProgressListener(listener2);
        
        pes.fireHandleProgressEvent(evt1.getTargetModuleID(), evt1.getDeploymentStatus());
    }
    
}
