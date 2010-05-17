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
    
}
