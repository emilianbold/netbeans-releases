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
 * EjbJarRootCustomizerTest.java
 * JUnit based test
 *
 * Created on March 11, 2004, 12:12 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory;
import org.netbeans.modules.j2ee.sun.share.MockDeployableObject;
import org.netbeans.modules.j2ee.sun.share.configbean.MockDDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.modules.j2ee.sun.share.SunDeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
/*import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.j2ee.sun.common.dd.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.common.dd.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.common.dd.ejb.PmDescriptor;
import org.netbeans.modules.j2ee.sun.common.dd.ejb.PmDescriptors;
import org.netbeans.modules.j2ee.sun.common.dd.ejb.PmInuse;
import org.netbeans.modules.j2ee.sun.common.dd.ejb.SchemaGeneratorProperties;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
//import org.netbeans.modules.j2ee.sun.share.configbean.customizers.MessageDestinationPanel;
//import org.netbeans.modules.j2ee.sun.share.configbean.customizers.WebserviceDescriptionPanel;
//import org.netbeans.modules.j2ee.sun.share.configbean.customizers.PmDescriptorPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;*/

import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class EjbJarRootCustomizerTest extends TestCase {
    
    public void testCreate() {
        EjbJarRootCustomizer ejc = new EjbJarRootCustomizer();
        assertNotNull(ejc);
        //WebAppRoot war = new WebAppRoot();
        // it is never this easy!!!
        MockDDBeanRoot waddbean = new MockDDBeanRoot();
        waddbean.setXpath("/ejb-jar");
        waddbean.setRoot(waddbean);
        EjbJarRoot war = null;
        try {
            war = (EjbJarRoot) DC.getDConfigBeanRoot(waddbean);
            assertNotNull(war);
        }
        catch (ConfigurationException ce) {
            fail("this should not fail");
        }
       ejc.setObject(war);
       ejc.getErrors();
       //ejcc.initFields();
    }

    static SunDeploymentFactory DF = new SunDeploymentFactory();
    static DeploymentManager DM = null;
    static DeploymentConfiguration DC = null;
    static DConfigBeanRoot WAR = null;
    static {
        try {
            DM = DF.getDisconnectedDeploymentManager("deployer:Sun:AppServer::localhost:4848");
            DC =  DM.createConfiguration(new MockDeployableObject());
//            WAR = DC.getDConfigBeanRoot(new MockDDBeanRoot());
        }
        catch (Throwable t) {
            fail(t.getMessage());
        }
    }
    
    public EjbJarRootCustomizerTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(EjbJarRootCustomizerTest.class);
        return suite;
    }
        
}
