/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
