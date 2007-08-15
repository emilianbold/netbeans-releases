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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import junit.framework.TestCase;

/**
 *
 * @author Petr Hejl
 */
public class ProgressListenerImpl implements ProgressListener {

    private ProgressEvent[] events;

    private Object expectedSource;

    private int counter;

    public ProgressListenerImpl(ProgressEvent[] events, Object expectedSource) {
        this.events = events;
        this.expectedSource = expectedSource;
    }

    public void handleProgressEvent(ProgressEvent evt) {
        if (counter > events.length) {
            TestCase.fail("Event arrive - unregistered listener");
        }

        ProgressEvent toCompare = events[counter++];

        TestCase.assertEquals(expectedSource, evt.getSource());
        TestCase.assertEquals(toCompare.getTargetModuleID(), evt.getTargetModuleID());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getAction(), evt.getDeploymentStatus().getAction());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getCommand(), evt.getDeploymentStatus().getCommand());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getMessage(), evt.getDeploymentStatus().getMessage());
        TestCase.assertEquals(toCompare.getDeploymentStatus().getState(), evt.getDeploymentStatus().getState());
    }
}
