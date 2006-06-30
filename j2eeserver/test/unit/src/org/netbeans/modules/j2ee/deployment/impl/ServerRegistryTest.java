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

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
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
        
        DeploymentManager manager = null;
        try {
            manager = testPlugin.getDisconnectedDeploymentManager();
            assertNotNull ("No DeploymentManager for test plugin", manager);
        } catch (DeploymentManagerCreationException dce) {
            fail(dce.getLocalizedMessage());
        }
        
        IncrementalDeployment incrementalDepl = optionalFactory.getIncrementalDeployment(manager);
        assertNotNull ("No IncrementalDeployment for test plugin", incrementalDepl);
        
        StartServer start = optionalFactory.getStartServer(manager);
        assertNotNull ("No StartServer for test plugin", start);
        
        String url = "fooservice";
        ServerInstance instance = registry.getServerInstance(url);
        if (instance == null || ! instance.getUrl().equals(url)) {
            fail("Failed: expected: " + url + " got: " + instance);
        }
    }
    
    public void testDeploymentFileNames() {
        ServerRegistry registry = ServerRegistry.getInstance();
        Server testPlugin = registry.getServer("Test");
        if (testPlugin == null || ! testPlugin.getShortName().equals("Test")) {
            fail("Could not get testPlugin: "+testPlugin);
        }
        String expected = "META-INF/context.xml";
        String[] names = testPlugin.getDeploymentPlanFiles(ModuleType.WAR);
        if (names == null || names.length != 1) {
            fail("Got null or incorrect deploy plan file paths: " + names);
        } else if (! names[0].equals(expected)) {
            fail("Expected: "+expected+" Got: "+names[0]);
        }
    }
}
