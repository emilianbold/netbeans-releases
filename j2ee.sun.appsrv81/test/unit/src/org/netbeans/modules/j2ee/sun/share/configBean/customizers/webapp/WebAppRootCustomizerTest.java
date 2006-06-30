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
 * WebAppRootCustomizerTest.java
 * JUnit based test
 *
 * Created on March 11, 2004, 3:32 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

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


/*import java.util.ResourceBundle;
import java.util.Set;
import java.util.Iterator;
import java.beans.Customizer;
import java.beans.PropertyVetoException;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.ServletVersion;
import org.netbeans.modules.j2ee.sun.share.configbean.ErrorMessageDB;
import org.netbeans.modules.j2ee.sun.share.configbean.ValidationError;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BaseCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;*/
import junit.framework.*;

import org.netbeans.modules.j2ee.sun.share.configbean.WebAppRoot;


/**
 *
 * @author vkraemer
 */
public class WebAppRootCustomizerTest extends TestCase {
    
    public void testCreate() {
        WebAppRootCustomizer warc = new WebAppRootCustomizer();
        assertNotNull(warc);
        //WebAppRoot war = new WebAppRoot();
        // it is never this easy!!!
        MockDDBeanRoot waddbean = new MockDDBeanRoot();
        waddbean.setXpath("/web-app");
        waddbean.setRoot(waddbean);
        WebAppRoot war = null;
        try {
            war = (WebAppRoot) DC.getDConfigBeanRoot(waddbean);
            assertNotNull(war);
        }
        catch (ConfigurationException ce) {
            fail("this should not fail");
        }
       warc.setBean(war);
        warc.initFields();
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
    
    public WebAppRootCustomizerTest(java.lang.String testName) {
        super(testName);
    }
    
}
