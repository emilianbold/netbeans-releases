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
