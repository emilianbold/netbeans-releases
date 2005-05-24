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
 * SunONEDeploymentFactoryTest.java
 * JUnit based test
 *
 * Created on March 25, 2003, 8:50 AM
 */

package org.netbeans.modules.j2ee.sun.share;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import java.io.*;
import javax.enterprise.deploy.spi.Target;
//import com.sun.enterprise.deployapi.SunDeploymentPlan;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.TargetModuleID;
import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class SunONEDeploymentFactoryTest extends TestCase implements Constants {
	
	public void testGetPortFromURI() {
		assertEquals(-1,DF.getPortFromURI("anyother thing without a colon in it"));
		assertEquals(12345, DF.getPortFromURI("deployer:Sun:S1AS::host:12345"));
		assertEquals(-1,DF.getPortFromURI("anyother thing with a : in it"));
		assertEquals(-1,DF.getPortFromURI("deployer:Sun:S1AS::host:12345:"));
	}
	
	public void testGetHost() {
		assertEquals("host", DF.getHostFromURI("deployer:Sun:S1AS::host:12345"));
		assertEquals("host", DF.getHostFromURI("host:12345"));
		assertEquals(null, DF.getHostFromURI("host12345"));
	}
		
	public void testTomcatUriIssues() {
		if (DF.handlesURI("tomcat:home=jakarta-tomcat-5.0.5:base=jakarta-tomcat-5.0.5_base:http://localhost:8080/manager/"))
			fail("tomcat uri accepted");
	}
    
    public SunONEDeploymentFactoryTest(java.lang.String testName) {
        super(testName);
    }
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static SunDeploymentFactory DF = 
        new SunDeploymentFactory();
    
    
    /** Test of getDeploymentManager method, of class org.netbeans.modules.j2ee.sun.share.SunONEDeploymentFactory. */
    public void testGetDeploymentManager() {
//        System.out.println("testGetDeploymentManager");
		DeploymentManager c;
        jsr88Logger.config("level is "+jsr88Logger.getLevel());
        jsr88Logger.entering("","level is "+jsr88Logger.getLevel());
        jsr88Logger.exiting("","level is "+jsr88Logger.getLevel());
        jsr88Logger.fine("level is "+jsr88Logger.getLevel());
        jsr88Logger.finer("level is "+jsr88Logger.getLevel());
        jsr88Logger.finest("level is "+jsr88Logger.getLevel());       
        jsr88Logger.info("level is "+jsr88Logger.getLevel());  
        jsr88Logger.severe("level is "+jsr88Logger.getLevel());       
        jsr88Logger.throwing("","level is "+jsr88Logger.getLevel(),new RuntimeException("ABC"));       
        jsr88Logger.warning("level is "+jsr88Logger.getLevel());               
		try {
			String bogusuri = "a:foo:bar::plink:fudge";
			String validuri = "deployer:Sun:S1AS::localhost:4848";
			String adminName = "admin";
			String adminPassword = "admin321";
			c = DF.getDeploymentManager(validuri, adminName, adminPassword);
			//assert
			assertNotNull("valid case failed",c);
	        try {
				c = DF.getDeploymentManager(bogusuri, "a", "a");
				fail("negative case failed");
			}
			catch (DeploymentManagerCreationException dmce) {
				assertEquals("invalid URI",
					dmce.getMessage());			
			}
		}
		catch (junit.framework.AssertionFailedError afe) {
			throw afe;
		}
		catch (Throwable t) {
			t.printStackTrace();
			fail("unexpected exception");
		}
    }
    
    /** Test of getDisconnectedDeploymentManager method, of class org.netbeans.modules.j2ee.sun.share.SunONEDeploymentFactory. */
    public void testGetDisconnectedDeploymentManager() {
        System.out.println("testGetDisconnectedDeploymentManager");
        
		DeploymentManager c;
		try {
			String bogusuri = "a:foo:bar::plink:fudge";
			String validuri = "deployer:Sun:S1AS::localhost:4848";
			String adminName = "admin";
			String adminPassword = "admin321";
			c = DF.getDisconnectedDeploymentManager(validuri);
			assertNotNull("valid case failed",c);
	        try {
				c = DF.getDisconnectedDeploymentManager(bogusuri);
				fail("negative case failed");
			}
			catch (DeploymentManagerCreationException dmce) {
				assertEquals("invalid URI",
					dmce.getMessage());			
			}
		}
		catch (junit.framework.AssertionFailedError afe) {
			throw afe;
		}
		catch (Throwable t) {
			t.printStackTrace();
			fail("unexpected exception");
		}
    }
    
    /** Test of handlesURI method, of class org.netbeans.modules.j2ee.sun.share.SunONEDeploymentFactory. */
    public void testHandlesDisconnectedURI() {
        //System.out.println("testHandlesURI");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
        //try {
            if (!DF.handlesURI("deployer:Sun:S1AS"))
                fail("disconnected URI is not accepted");
	}
    public void testHandlesCompleteURI() {	
            if (!DF.handlesURI("deployer:Sun:S1AS::localhost:4848"))
                fail("complete URI is not accpeted");
	}
    public void testHandlesIPURI() {
            if (!DF.handlesURI("deployer:Sun:S1AS::127.0.0.1:4848"))
                fail("IP address not accepted");
	}
            
    public void testHandleMisspelledURI() {
             if (DF.handlesURI("deployerr:Sun:S1AS")) 
                fail("misspelled URI accepted");
	}
            
    public void testHandleIllegalThirdSepURI() {
            if (!DF.handlesURI("deployer:Sun:S1AS:localhost:4848"))
                fail("illegal host sep ':' not accepted");
	}
    public void testHandleIllegalFirstSepURI() {
            if (DF.handlesURI("deployer@Sun:S1AS::localhost:4848"))
                fail("illegal first sep '@' accepted");
	}
    public void testHandleIllegalSecondSepURI() {
            if (DF.handlesURI("deployer:Sun@S1AS::localhost:4848"))
                fail("illegal second sep '@' accepted");
	}
			
    /*public void testHandleEmptyHostAndPortURI() {
			if (DF.handlesURI("deployer:Sun:S1AS:::"))
				fail("empty host and port accepted");
	}*/
			/*
            if (DF.handlesURI("deployer:Sun:S1AS@localhost:4848"))
                fail("illegal host sep '@' accepted");
             
            if (DF.handlesURI("deployer:Sun:S1AS:::4848"))
                fail("empty host value accepted");
			 
            if (DF.handlesURI("deployer:Sun:S1AS::127.0.0.280:4848"))
                fail("illegal IP address accepted");
			 
            if (DF.handlesURI("deployer:Sun:S1AS::localhost:1234567890"))
                fail("illegal port accpted");
			 *
            if (DF.handlesURI("deployer:Sun:S1AS::illegal host name:4848"))
                fail("invalid host name accepted");
			 */
        /*}
        catch (junit.framework.AssertionFailedError afe) {
            throw afe;
        }
        catch (Throwable t) {
            t.printStackTrace();
            fail("positive test throws exception");
        }*/
    //}
    
	public static Test suite() {
		TestSuite suite = new TestSuite(SunONEDeploymentFactoryTest.class);
		
		return suite;
	}
	
	/** Test of getDisplayName method, of class org.netbeans.modules.j2ee.sun.share.SunONEDeploymentFactory. */
	public void testGetDisplayName() {
		System.out.println("testGetDisplayName");
		String dn = DF.getDisplayName();
		System.out.println("dn = "+dn);
		assertNotNull("the dn is null",dn);
		assertEquals("empty dn", dn.length() > 0, true);
	}
	
	/** Test of getProductVersion method, of class org.netbeans.modules.j2ee.sun.share.SunONEDeploymentFactory. */
	public void testGetProductVersion() {
		System.out.println("testGetProductVersion");
		String pv = DF.getProductVersion();
		System.out.println("pv = "+pv);
		assertNotNull("the pv is null",pv);
		assertEquals("empty pv", pv.length() > 0, true);
	}
	
}
