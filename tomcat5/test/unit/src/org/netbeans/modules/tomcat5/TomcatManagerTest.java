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
import java.io.FileInputStream;
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
import org.netbeans.modules.j2ee.deployment.impl.ui.ServerRegistryNode;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

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
    
    /** Test for adding instance (needs to be ide test). */
//    public void testAddToRegistry () {
//        System.out.println ("testAddToRegistry");
//        ServerRegistryNode srn = new ServerRegistryNode ();
//        try {
//            org.netbeans.modules.j2ee.deployment.impl.ServerRegistry
//                .getInstance().addInstance("tomcat:dummy manager", "", "");
//        }
//        catch (java.io.IOException ioe) {
//            fail(ioe.getMessage ());
//            return;
//        }
//        
//        boolean found = false;
//        Node [] nodes = srn.getChildren ().getNodes ();
//        for (int i=0; i<nodes.length; i++) {
//            Node [] subnodes = nodes[i].getChildren ().getNodes ();
//            for (int j=0; j<nodes.length; j++) {
//                if (subnodes[j].getDisplayName ().indexOf ("dummy manager") >= 0) {
//                    found = true;
//                    break;
//                }
//            }
//            if (found) {
//                break;
//            }
//        }
//        assertTrue ("Added instance not found in registry", found);
//        
//    }
    
    
}
