/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import junit.framework.*;
import org.netbeans.junit.*;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;
import org.openide.filesystems.*;

/**
 *
 * @author nn136682
 */
public class ServerInstanceTest extends ServerRegistryTestBase {
    
    public ServerInstanceTest(java.lang.String testName) {
        super(testName);
    }
    
    /**
     * Test start instance make sure its running.
     * Test stop instance make sure its stopped.
     */
    public void testStartStopInstance()  throws java.io.IOException {
        // setup
        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:testStartStopInstance";
        registry.addInstance(url, "user", "password", "TestInstance");
        ServerInstance instance = registry.getServerInstance(url);
        ServerTarget target = instance.getServerTarget("Target 1");

        //fail("");
    }
    
    /**
     * Test start target, case admin is also target make sure its started.
     * Test stop target.
     */
//    public void testStartStopTarget() throws java.io.IOException {
//        // setup
//        ServerRegistry registry = ServerRegistry.getInstance();
//        String url = "fooservice:testStartStopTarget";
//        registry.addInstance(url, "user", "password", "TestInstance");
//        ServerInstance instance = registry.getServerInstance(url);
//        ServerTarget target = instance.getServerTarget("Target 1");
//        
//        // start target
//        DeployProgressUI ui = new DeployProgressMonitor(false, true);
//        ui.startProgressUI(10);
//        boolean success = instance.startTarget(target.getTarget(), ui);
//        ui.recordWork(10);
//        if (! success)
//            fail("Failed to start 'Target 1'");
//        DepManager dm = (DepManager) instance.getDeploymentManager();
//        if (dm.getState() != DepManager.RUNNING)
//            fail("DepManager is not running after ServerInstance.startTarget() call!");
//        
//        /*
//        // stop target
//        ui = new DeployProgressMonitor(false, true);
//        ui.startProgressUI(10);
//        success = instance._test_stop(target.getTarget(), ui);
//        ui.recordWork(10);
//        if (! success)
//            fail("Failed to stop target 'Target 1'");
//        if (dm.getState() != DepManager.STOPPED)
//            fail("DepManager is not stopped after ServerInstance.stopTarget() call");
//        */
//        // cleanup
//        instance.remove();
//    }
}
