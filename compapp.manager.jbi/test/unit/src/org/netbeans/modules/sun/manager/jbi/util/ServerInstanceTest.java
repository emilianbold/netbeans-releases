/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.sun.manager.jbi.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jqian
 */
public class ServerInstanceTest {

    List<ServerInstance> instances;
            
    public ServerInstanceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws URISyntaxException {
        URI xmlURI = getClass().getResource("resources/nbattrs").toURI();
        File xmlFile = new File(xmlURI);
        ServerInstanceReader reader = 
                new ServerInstanceReader(xmlFile.getAbsolutePath());
        instances = reader.getServerInstances();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDisplayName method, of class ServerInstance.
     */
    @Test
    public void getDisplayName() {
        System.out.println("getDisplayName");      
        
        assertEquals("GlassFish V2 (b54)", instances.get(0).getDisplayName());
        assertEquals("GlassFish V2 (b54 personal domain 2)", instances.get(1).getDisplayName());
        assertEquals("GlassFish V2", instances.get(2).getDisplayName());
    }

    /**
     * Test of getDomain method, of class ServerInstance.
     */
    @Test
    public void getDomain() {
        System.out.println("getDomain");
        
        assertEquals("domain1", instances.get(0).getDomain());
        assertEquals("GFb54_PersonalDomain2", instances.get(1).getDomain());
        assertEquals("", instances.get(2).getDomain());
    }

    /**
     * Test of getHttpMonitorOn method, of class ServerInstance.
     */
    @Test
    public void getHttpMonitorOn() {
        System.out.println("getHttpMonitorOn");
        
        assertEquals("true", instances.get(0).getHttpMonitorOn());
        assertEquals("true", instances.get(1).getHttpMonitorOn());
        assertEquals(null, instances.get(2).getHttpMonitorOn());
    }

    /**
     * Test of getHttpPortNumber method, of class ServerInstance.
     */
    @Test
    public void getHttpPortNumber() {
        System.out.println("getHttpPortNumber");
        
        assertEquals("8080", instances.get(0).getHttpPortNumber());
        assertEquals("8105", instances.get(1).getHttpPortNumber());
        assertEquals("2848", instances.get(2).getHttpPortNumber());
    }

    /**
     * Test of getLocation method, of class ServerInstance.
     */
    @Test
    public void getLocation() {
        System.out.println("getLocation");
        
        assertEquals("C:\\Glassfish-v2-b54\\glassfish\\domains", instances.get(0).getLocation());
        assertEquals("C:\\tmp", instances.get(1).getLocation());
        assertEquals("", instances.get(2).getLocation());
    }

    /**
     * Test of getPassword method, of class ServerInstance.
     */
    @Test
    public void getPassword() {
        System.out.println("getPassword");
        
        assertEquals("adminadmin", instances.get(0).getPassword());
        assertEquals("adminadmin", instances.get(1).getPassword());
        assertEquals("adminadmin", instances.get(2).getPassword());
    }

    /**
     * Test of getUrl method, of class ServerInstance.
     */
    @Test
    public void getUrl() {
        System.out.println("getUrl");
        
        assertEquals("[C:\\Glassfish-v2-b54\\glassfish]deployer:Sun:AppServer::localhost:4848", instances.get(0).getUrl());
        assertEquals("[C:\\Glassfish-v2-b54\\glassfish]deployer:Sun:AppServer::localhost:4873", instances.get(1).getUrl());
        assertEquals("[C:\\Glassfish-v2-b54\\glassfish]deployer:Sun:AppServer::cordova.stc.com:2848", instances.get(2).getUrl());
    }

    /**
     * Test of getUrlLocation method, of class ServerInstance.
     */
    @Test
    public void getUrlLocation() {
        System.out.println("getUrlLocation");
        
        assertEquals("C:\\Glassfish-v2-b54\\glassfish", instances.get(0).getUrlLocation());
        assertEquals("C:\\Glassfish-v2-b54\\glassfish", instances.get(1).getUrlLocation());
        assertEquals("C:\\Glassfish-v2-b54\\glassfish", instances.get(2).getUrlLocation());       
    }

    /**
     * Test of getUserName method, of class ServerInstance.
     */
    @Test
    public void getUserName() {
        System.out.println("getUserName");
        
        assertEquals("admin", instances.get(0).getUserName());
        assertEquals("admin", instances.get(1).getUserName());
        assertEquals("admin", instances.get(2).getUserName());
    }

    /**
     * Test of getAdminPort method, of class ServerInstance.
     */
    @Test
    public void getAdminPort() {
        System.out.println("getAdminPort");
        
        assertEquals("4848", instances.get(0).getAdminPort());
        assertEquals("4873", instances.get(1).getAdminPort());
        assertEquals("2848", instances.get(2).getAdminPort());
    }

    /**
     * Test of getHostName method, of class ServerInstance.
     */
    @Test
    public void getHostName() {
        System.out.println("getHostName");
        
        assertEquals("localhost", instances.get(0).getHostName());
        assertEquals("localhost", instances.get(1).getHostName());
        assertEquals("cordova.stc.com", instances.get(2).getHostName());
    }
}
