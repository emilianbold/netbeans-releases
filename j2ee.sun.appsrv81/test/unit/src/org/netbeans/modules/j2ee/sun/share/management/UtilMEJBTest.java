/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * UtilMEJBTest.java
 * JUnit based test
 *
 * Created on May 20, 2004, 3:12 PM
 */

package org.netbeans.modules.j2ee.sun.share.management;

//import javax.management.*;
//import javax.management.j2ee.Management;
//import javax.management.j2ee.ManagementHome;
//import javax.management.remote.JMXConnector;
//import javax.management.remote.JMXServiceURL;
import javax.management.MBeanServerConnection;
//import javax.management.MBeanException;
//import javax.management.ReflectionException;
//import javax.management.IntrospectionException;
//import javax.management.InstanceNotFoundException;
//import javax.management.AttributeNotFoundException;
//import javax.management.InvalidAttributeValueException;
//import java.io.File;
//import javax.swing.JOptionPane;
//import java.util.Set;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Hashtable;
//import javax.naming.*;
//import javax.rmi.PortableRemoteObject;
//import java.util.Iterator;
//import java.util.Properties;
//import javax.enterprise.deploy.spi.DeploymentManager;
//import java.rmi.RemoteException;
//import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
//import com.sun.enterprise.admin.jmx.remote.SunOneHttpJmxConnectorFactory;
//import com.sun.enterprise.deployment.util.DeploymentProperties;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.mbmapping.*;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Constants;
//import org.netbeans.modules.j2ee.sun.ide.j2ee.DeploymentManagerProperties;
//import org.netbeans.modules.j2ee.sun.share.SunDeploymentManagerInterface;
import junit.framework.*;

import org.netbeans.modules.j2ee.sun.share.management.UtilMEJB;

/**
 *
 * @author vkraemer
 */
public class UtilMEJBTest extends TestCase {
    
    public void testCreate() {
        UtilMEJB foo = new UtilMEJB("iasengsol7.red.iplanet.com",4849,"admin","adminadmin"); 
        MBeanServerConnection bar = foo.getConnection();            
        foo.getServerName();
 
    }

    
    public UtilMEJBTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(UtilMEJBTest.class);
        return suite;
    }
}
    
    /**
     * Test of getServerName method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testGetServerName() {
        System.out.println("testGetServerName");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of getConnection method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testGetConnection() {
        System.out.println("testGetConnection");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    
    /**
     * Test of updateGetAttributes method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testUpdateGetAttributes() {
        System.out.println("testUpdateGetAttributes");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of updateGetAttribute method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testUpdateGetAttribute() {
        System.out.println("testUpdateGetAttribute");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of updateInvoke method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testUpdateInvoke() {
        System.out.println("testUpdateInvoke");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of queryServer method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testQueryServer() {
        System.out.println("testQueryServer");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of updateMBeanInfo method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testUpdateMBeanInfo() {
        System.out.println("testUpdateMBeanInfo");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /**
     * Test of updateSetAttribute method, of class org.netbeans.modules.j2ee.sun.share.management.UtilMEJB.
     *
    public void testUpdateSetAttribute() {
        System.out.println("testUpdateSetAttribute");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}*/
