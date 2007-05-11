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

/*
 * TestCreatePlatforms.java
 * NetBeans JUnit based test
 *
 * Created on 14 September 2004, 15:37
 */

package projects;

import java.io.InputStream;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import org.netbeans.jellytools.JellyTestCase;

import org.netbeans.junit.*;
import junit.framework.*;

import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;

/**
 *
 */
public class PlatformsTest extends JellyTestCase {

    public static final String JDK13_NAME = "JDK1.3";
    public static final String JDK14_NAME = "JDK1.4";
    public static final String JDK15_NAME = "JDK1.5";
    
    public PlatformsTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //TestSuite suite = new NbTestSuite(TestCreatePlatforms.class);
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PlatformsTest("testCreatePlatforms"));
        suite.addTest(new PlatformsTest("testAvailablePlatforms"));
        return suite;
    }
    
    // -------------------------------------------------------------------------
    
    public void testAvailablePlatforms() {
        
        JavaPlatformManager platMan = JavaPlatformManager.getDefault();
        JavaPlatform platforms[] = platMan.getInstalledPlatforms();
        String[] platNames = new String[platforms.length];
        for (int i = 0; i < platforms.length; i++) {
            System.out.println("Display Name: " + platforms[i].getDisplayName());
            platNames[i] = platforms[i].getDisplayName();
        }
        // there should be test if all added platforms are really added in IDE
        
    }
    
    // TODO Javadoc can be also added to platform
    public void testCreatePlatforms() {
        
        // learn hostname
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            fail("Cannot get hostname: " + uhe.getMessage()); // NOI18N
        }
        hostName = hostName.replace('-', '_');
        
        // load platforms.properties file
        InputStream is = this.getClass().getResourceAsStream("platforms.properties");
        Properties props = new Properties();
        try {
            props.load(is);
        } catch (java.io.IOException ioe) {
            fail("Cannot load platforms properties: " + ioe.getMessage()); // NOI18N
        }
        
        // get folder from prop file
        
        // XXX add correct paths to platform.properties
        String folderJDK13Path = props.getProperty(hostName + "_jdk13_folder");
        TestProjectUtils.addPlatform(JDK13_NAME, folderJDK13Path);
        String folderJDK14Path = props.getProperty(hostName + "_jdk14_folder");
        TestProjectUtils.addPlatform(JDK14_NAME, folderJDK14Path);
        String folderJDK15Path = props.getProperty(hostName + "_jdk15_folder");
        TestProjectUtils.addPlatform(JDK15_NAME, folderJDK15Path);
        
    }
    
}
