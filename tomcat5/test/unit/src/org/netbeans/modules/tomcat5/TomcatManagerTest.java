/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ProgressObject;
import junit.framework.*;
import org.openide.ErrorManager;

/**
 *
 * @author Radim Kubacki
 */
public class TomcatManagerTest extends TestCase {
    
    public static Test suite () {
        TestSuite suite = new TestSuite (TomcatManagerTest.class);
        return suite;
    }

    private TomcatManager tm;
    private java.io.File datadir;
    
    public TomcatManagerTest (java.lang.String testName) {
        super (testName);
    }
    
    protected void setUp () throws java.lang.Exception {
        super.setUp ();
        
        tm = (TomcatManager)TomcatFactory.create ().getDeploymentManager (
            "tomcat:"+TomcatFactoryTest.TOMCAT_URI, 
            TomcatFactoryTest.TOMCAT_UNAME, 
            TomcatFactoryTest.TOMCAT_PASSWD
        );
        datadir = new java.io.File ("/usr/local/home/radim/devel/prj40/nb_all/tomcatint/tomcat5/test/unit/data");
    }
    
    /** Test of getUri method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testGetUri () {
        System.out.println ("testGetUri");
        assertEquals ("Uri string doesn't match", TomcatFactoryTest.TOMCAT_URI, tm.getUri ());
    }
    
    /** Test of getUsername method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testGetUsername () {
        System.out.println ("testGetUsername");
        assertEquals (TomcatFactoryTest.TOMCAT_UNAME, tm.getUsername ());
    }
    
    /** Test of getPassword method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testGetPassword () {
        System.out.println ("testGetPassword");
        assertEquals (TomcatFactoryTest.TOMCAT_PASSWD, tm.getPassword ());
    }
    
//    /** Test of createConfiguration method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testCreateConfiguration () {
//        System.out.println ("testCreateConfiguration");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
    /** Test of getCurrentLocale method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testGetCurrentLocale () {
        System.out.println ("testGetCurrentLocale");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
    */
    
    /** Test of getDefaultLocale method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testGetDefaultLocale () {
        System.out.println ("testGetDefaultLocale");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of getSupportedLocales method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testGetSupportedLocales () {
        System.out.println ("testGetSupportedLocales");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of isLocaleSupported method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testIsLocaleSupported () {
        System.out.println ("testIsLocaleSupported");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of setLocale method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testSetLocale () {
        System.out.println ("testSetLocale");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of getTargets method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testGetTargets () {
        System.out.println ("testGetTargets");
        Target [] tgts = tm.getTargets ();
        assertTrue ("There should be one target", tgts != null && tgts.length == 1);
    }
    
    /** Test of getDConfigBeanVersion method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testGetDConfigBeanVersion () {
        System.out.println ("testGetDConfigBeanVersion");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of setDConfigBeanVersion method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testSetDConfigBeanVersion () {
        System.out.println ("testSetDConfigBeanVersion");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of isDConfigBeanVersionSupported method, of class org.netbeans.modules.tomcat5.TomcatManager. * /
    public void testIsDConfigBeanVersionSupported () {
        System.out.println ("testIsDConfigBeanVersionSupported");
        
        // Add your test code below by replacing the default call to fail.
        fail ("The test case is empty.");
    }
     */
    
    /** Test of isRedeploySupported method, of class org.netbeans.modules.tomcat5.TomcatManager. */
    public void testIsRedeploySupported () {
        System.out.println ("testIsRedeploySupported");
        assertFalse (tm.isRedeploySupported ());
    }
    
//    /** Test of redeploy method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testRedeploy () {
//        System.out.println ("testRedeploy");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of release method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testRelease () {
//        System.out.println ("testRelease");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of start method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testStart () {
//        System.out.println ("testStart");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of stop method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testStop () {
//        System.out.println ("testStop");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of undeploy method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testUndeploy () {
//        System.out.println ("testUndeploy");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
    /** Test for deployment and undeployment of web module. */
    public void testDeploymentOfDirectory () {
        java.io.File webapp  = new java.io.File (datadir, "sampleweb");
        java.io.File context = new java.io.File (datadir, "sampleweb.xml");
        System.out.println("testDistribute of "+webapp+" using "+context);
        ProgressObject po = tm.distribute (tm.getTargets (), webapp, context);
        try {
            Thread.sleep (5000);
        }
        catch (InterruptedException ie) {
            // do nothing
        }
        try {
            checkResponse (new URL("http://localhost:8080/sampleweb/index.jsp"));
        } catch (Exception e) {
            fail (e.getMessage ());
        }
        TargetModuleID [] tmIDs = po.getResultTargetModuleIDs ();
        assertTrue ("There should be one result target module", tmIDs != null && tmIDs.length == 1);

        ProgressObject po2 = tm.undeploy (tmIDs);
        try {
            Thread.sleep (5000);
        }
        catch (InterruptedException ie) {
            // do nothing
        }
        try {
            checkResponse (new URL("http://localhost:8080/sampleweb/index.jsp"));
            fail ("deployed application is still accessible");
        } catch (Exception e) {
            // OK
            System.out.println("correctly thrown exception: "+e.getMessage ());
        }
    }
    
    /** Tries to connect to given URL and scans the output whether
     * its first line starts with OK.
     */
    private boolean checkResponse (URL url) throws Exception {
        java.net.URLConnection conn = url.openConnection();
        HttpURLConnection hconn = (HttpURLConnection) conn;

        // Set up standard connection characteristics
        hconn.setAllowUserInteraction(false);
        hconn.setUseCaches(false);
        hconn.setDoOutput(false);
        hconn.setRequestMethod("GET");
        // Establish the connection with the server
        hconn.connect();

        // Process the response message
        java.io.Reader reader = new InputStreamReader(hconn.getInputStream());
        StringBuffer buff = new StringBuffer();
        String error = null;
        boolean first = true;
        while (true) {
            int ch = reader.read();
            if (ch < 0) {
                break;
            } else if ((ch == '\r') || (ch == '\n')) {
                String line = buff.toString();
                buff.setLength(0);
                // PENDING : fireProgressEvent
                TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, line);
                if (first) {
                    if (!line.startsWith("OK")) {
                        error = line;
                    }
                    first = false;
                }
            } else {
                buff.append((char) ch);
            }
        }
        if (buff.length() > 0) {
            System.out.println(buff);;
        }
        if (buff.length () == 0 && first) {
            // actually bug in Tomcat - after remove there is  empty page returned.
            throw new Exception ("URL stream content is empty");
        }
        if (error != null) {
            throw new Exception ("URL reading failed: "+error);
        }
        return true;
    }
    
//    /** Test of getAvailableModules method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testGetAvailableModules () {
//        System.out.println ("testGetAvailableModules");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of getNonRunningModules method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testGetNonRunningModules () {
//        System.out.println ("testGetNonRunningModules");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
//    /** Test of getRunningModules method, of class org.netbeans.modules.tomcat5.TomcatManager. */
//    public void testGetRunningModules () {
//        System.out.println ("testGetRunningModules");
//        
//        // Add your test code below by replacing the default call to fail.
//        fail ("The test case is empty.");
//    }
//    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
