/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
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
import java.util.*;
import java.io.*;
import org.netbeans.modules.j2ee.deployment.execution.DeploymentTarget;
import org.netbeans.modules.j2ee.deployment.impl.*;
import javax.enterprise.deploy.spi.*;
import org.netbeans.tests.j2eeserver.plugin.jsr88.DepManager;
import org.netbeans.modules.j2ee.deployment.impl.ui.*;

/**
 *
 * @author nn136682
 */
public class TargetServerTest extends NbTestCase {
    
    public TargetServerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TargetServerTest.class);
        //suite.addTest(new TargetServerTest("testDistributeSuccess"));
        //suite.addTest(new TargetServerTest("testDistributeFailed"));
        //suite.addTest(new TargetServerTest("testRedeploySuccess"));
        //suite.addTest(new TargetServerTest("testRedeployFailed"));
        //suite.addTest(new TargetServerTest("testDeployWhenServerDown"));
        //suite.addTest(new TargetServerTest("testDeployWhenStartServerFailed"));
        return suite;
    }

    ServerString server;
    ServerString getSuiteTargetServer() {
        return getTargetServer(null);
    }
    ServerString getTargetServer(String name){
        if (server != null)
            return server;

        ServerRegistry registry = ServerRegistry.getInstance();
        String url = "fooservice:TargetServerTest";
        if (name != null)
            url += "_"+name;
        try {
            registry.addInstance(url, "user", "password", "TestInstance");
        } catch (IOException ioe) { throw new RuntimeException(ioe); }
        
        server = new ServerString(registry.getServerInstance(url).getServerTarget("Target 1"));
        return server;
    }
        
    /** Test of processLastTargetModules method, of class org.netbeans.modules.j2ee.deployment.impl.TargetServer. */
    /*
    public void testDistributeSuccess() {
        System.out.println("testDistributeSuccess");
        
        ServerInstance instance = getSuiteTargetServer().getServerInstance();
        DepManager dm = (DepManager) instance.getDeploymentManager();
        boolean started = instance.start();
        if (! started || dm.getState() != DepManager.RUNNING)
            fail("Failed to start: state="+dm.getState());
        try {Thread.sleep(2000); } catch(Exception e) {}
        TargetModule[] modules = getSuiteDeployTarget().getTargetModules();
        assertTrue(modules == null || modules.length == 0);
        DeploymentTarget dt = getSuiteDeployTarget();
        ServerExecutor.instance().deploy(dt);
        //FIXME: this.assertTrue(dm.hasDistributed(dt.getTargetModules()[0].getId()));
    }
    
    // Precondtion: testDistributeSuccess
    public void testRedeploySuccess() {
        System.out.println("testRedeploySuccess");
        DepManager dm = (DepManager) getSuiteTargetServer().getServerInstance().getDeploymentManager();
        //FIXME: this.assertFalse(dm.hasRedeployed(getSuiteDeployTarget().getTargetModules()[0].toString()));
        ServerExecutor.instance().deploy(getSuiteDeployTarget());
        //FIXME: this.assertTrue(dm.hasRedeployed(getSuiteDeployTarget().getTargetModules()[0].toString()));
    }
    */
    
    /*public void testNoChangesRedeploy() {
        System.out.println("testRedeployFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    /*public void testDistributeFailed() {
        System.out.println("testDistributeFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    /*public void testDeployWhenServerDown() {
        System.out.println("testDeployWhenServerDown");
        // Make sure server is down
        
        // deploy or redeploy
        
        // make sure server is up
        fail("The test case is empty.");
    }*/
    
    /*public void testDeployWhenStartServerFailed() {
        System.out.println("testDeployWhenStartServerFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }*/
    
    public void testWebContextRoot() {
        
    }
    
}
