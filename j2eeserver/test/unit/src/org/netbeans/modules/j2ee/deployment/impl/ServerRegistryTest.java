/*
 * ServerRegistryTest.java
 * NetBeans JUnit based test
 *
 * Created on September 22, 2003, 4:19 PM
 */

package org.netbeans.modules.j2ee.deployment.impl;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.j2ee.deployment.impl.gen.nbd.*;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;
import java.util.*;
import java.io.*;
import java.util.logging.*;

/**
 *
 * @author nn136682
 */
public class ServerRegistryTest extends NbTestCase {
    
    public ServerRegistryTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ServerRegistryTest.class);
        suite.addTest(new ServerRegistryTest("testPluginLayerFile"));
        suite.addTest(new ServerRegistryTest("testRemoveDefaultInstance"));
        return suite;
    }
    
    /** 
     * Test plugin layer file which install 1 plugin instance.
     * @precondition: test plugin is installed
     * @postcondition: getServer("Test") to get testplugin 
     * @postcondition: getInstance("fooservice") to get testplugin instance
     */
    public void testPluginLayerFile() {
        ServerRegistry registry = ServerRegistry.getInstance();
        Server testPlugin = registry.getServer("Test");
        if (testPlugin == null || ! testPlugin.getShortName().equals("Test"))
            fail("Could not get testPlugin: "+testPlugin);
        
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
        registry.addInstance(url1, "user", "password");
        registry.addInstance(url2, "user", "password");
        ServerInstance instance1 = registry.getServerInstance(url1);
        registry.setDefaultInstance(new ServerString(instance1.getTargets()[0]));
        
        ServerInstance defaultInstance = registry.getDefaultInstance().getServerInstance();
        
        if (defaultInstance == null || ! instance1.equals(defaultInstance)) {
            System.out.println("testRemoveDefaultInstance: check1 failed!");
            fail("Default instance retrieved not same as the one used in setDefaultInstance");
        }
        
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
