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
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.openide.filesystems.*;
import org.openide.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
/**
 *
 * @author nn136682
 */
public class ServerRegistryTest extends ServerRegistryTestBase {
    
    public ServerRegistryTest(String testName) {
        super(testName);
    }
    
    /** 
     * Test plugin layer file which install 1 plugin instance.
     * @precondition: test plugin is installed
     * @postcondition: getServer("Test") to get testplugin 
     * @postcondition: getInstance("fooservice") to get testplugin instance
     */
    public void testPluginLayerFile() {
        ServerRegistry registry = ServerRegistry.getInstance();
        System.out.println ("registry:" + registry);
        Server testPlugin = registry.getServer("Test");
        if (testPlugin == null || ! testPlugin.getShortName().equals("Test"))
            fail("Could not get testPlugin: "+testPlugin);
        
        DeploymentFactory factory = testPlugin.getDeploymentFactory();
        assertNotNull ("No DeploymentFactory for test plugin", factory);
        
        RegistryNodeProvider nodeProvider = testPlugin.getNodeProvider();
        assertNotNull ("No RegistryNodeProvider for test plugin", nodeProvider);
        
        OptionalDeploymentManagerFactory optionalFactory = testPlugin.getOptionalFactory();
        assertNotNull ("No OptionalDeploymentManagerFactory for test plugin", optionalFactory);
        
        DeploymentManager manager = testPlugin.getDeploymentManager();
        assertNotNull ("No DeploymentManager for test plugin", manager);
        
        IncrementalDeployment incrementalDepl = optionalFactory.getIncrementalDeployment(manager);
        assertNotNull ("No IncrementalDeployment for test plugin", incrementalDepl);
        
        StartServer start = optionalFactory.getStartServer(manager);
        assertNotNull ("No StartServer for test plugin", start);
        
        DeploymentPlanSplitter splitter = testPlugin.getDeploymentPlanSplitter();
        assertNotNull ("No DeploymentPlanSplitter for test plugin", splitter);
        
        String url = "fooservice";
        ServerInstance instance = registry.getServerInstance(url);
        if (instance == null || ! instance.getUrl().equals(url)) {
            fail("Failed: expected: " + url + " got: " + instance);
        }
    }
    
    /**
     * Test case for remove instance when instance is default instance.
     */
    public void testRemoveDefaultInstance() throws java.io.IOException {
        // create 2 instances and set first one as default
        ServerRegistry registry = ServerRegistry.getInstance();
        String url1 = "fooservice:instance1";
        String url2 = "fooservice:instance2";
        registry.addInstance(url1, "user", "password", "TestInstance1");
        registry.addInstance(url2, "user", "password", "TestInstance2");
        ServerInstance instance1 = registry.getServerInstance(url1);
        registry.setDefaultInstance(new ServerString(instance1));
        
        ServerInstance defaultInstance = registry.getDefaultInstance().getServerInstance();
        
//        assertNotNull ("no default instance", defaultInstance);
//        assertEquals("Default instance retrieved not same as the one used in setDefaultInstance", instance1, defaultInstance);
        
        try {
            Thread.sleep(2000);
            int instanceCount = registry.getServerInstances().length;
            defaultInstance.remove();
            ServerInstance instance2 = registry.getServerInstance(url2);
            defaultInstance = registry.getDefaultInstance().getServerInstance();

            if ((instanceCount == 2 && defaultInstance == null) ||
                (defaultInstance != null && defaultInstance.getUrl().equals(instance1.getUrl()))) {
                System.out.println("testRemoveDefaultInstance: defaultInstance=" + (
                (defaultInstance == null) ? "null" : (new ServerString(defaultInstance)).toString()));
                System.out.println("testRemoveDefaultInstance: check2 failed!");
                fail("No instance promoted to default instance after removal of default instance");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
