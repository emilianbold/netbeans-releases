/*
 * ServerInstanceTest.java
 * NetBeans JUnit based test
 *
 * Created on September 22, 2003, 4:19 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import junit.framework.*;
import org.netbeans.junit.*;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import org.netbeans.tests.j2eeserver.plugin.jsr88.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;

/**
 *
 * @author nn136682
 */
public class ServerInstanceTest extends NbTestCase {
    
    public ServerInstanceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ServerInstanceTest.class);
        suite.addTest(new ServerInstanceTest("testStartStopInstance"));
        suite.addTest(new ServerInstanceTest("testStartStopTarget"));
        //suite.addTest(new ServerInstanceTest("testStartDebugTarget"));
        return suite;
    }
    
    /**
     * Test start instance make sure its running.
     * Test stop instance make sure its stopped.
     */
    public void testStartStopInstance()  throws java.io.IOException {
        // setup
        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:testStartStopInstance";
        registry.addInstance(url, "user", "password");
        ServerInstance instance = registry.getServerInstance(url);
        ServerTarget target = instance.getServerTarget("Target 1");

        //fail("");
    }
    
    /**
     * Test start target, case admin is also target make sure its started.
     * Test stop target.
     */
    public void testStartStopTarget() throws java.io.IOException {
        // setup
        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:testStartStopTarget";
        registry.addInstance(url, "user", "password");
        ServerInstance instance = registry.getServerInstance(url);
        ServerTarget target = instance.getServerTarget("Target 1");
        
        // start target
        DeployProgressUI ui = new DeployProgressMonitor(false, true);
        ui.startProgressUI(10);
        boolean success = instance.startTarget(target.getTarget(), ui);
        ui.recordWork(10);
        if (! success)
            fail("Failed to start 'Target 1'");
        DepManager dm = (DepManager) instance.getDeploymentManager();
        if (dm.getState() != DepManager.RUNNING)
            fail("DepManager is not running after ServerInstance.startTarget() call!");
        
        // stop target
        ui = new DeployProgressMonitor(false, true);
        ui.startProgressUI(10);
        success = instance._test_stop(target.getTarget(), ui);
        ui.recordWork(10);
        if (! success)
            fail("Failed to stop target 'Target 1'");
        if (dm.getState() != DepManager.STOPPED)
            fail("DepManager is not stopped after ServerInstance.stopTarget() call");
        
        // cleanup
        instance.remove();
    }
}
